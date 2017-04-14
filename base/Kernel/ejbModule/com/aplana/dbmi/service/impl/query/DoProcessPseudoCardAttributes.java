/**
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to you under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.aplana.dbmi.service.impl.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortalException;
import com.aplana.dbmi.PortalUser;
import com.aplana.dbmi.action.GetPersonByCard;
import com.aplana.dbmi.action.GetPersonByLogin;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.ProcessPseudoCardAttributes;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.Group;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PortalUserLoginAttribute;
import com.aplana.dbmi.model.PseudoAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.SystemGroup;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.UngroupedRole;
import com.aplana.dbmi.model.UserRolesAndGroupsAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;

/**
 * {@link ActionQueryBase} descendant used to process defined collection of card pseudo-attributes.
 * Currently processes two pseudo attributes: {@link PortalUserLoginAttribute} and {@link UserRolesAndGroupsAttribute}
 * - if PortalUserLoginAttribute is defined, then new user is created in database (person table)
 * - if UserRolesAndGroupsAttribute is defined, then user roles and groups are updated in database
 * User synchronization to portal happens in {@SyncPortalUserProcessor}
 * @see ProcessPseudoCardAttributes
 *
 */
public class DoProcessPseudoCardAttributes extends ActionQueryBase implements WriteQuery {
	private static final long serialVersionUID = 1L;

	protected final Log logger = LogFactory.getLog(getClass());

	private DataServiceFacade serviceFacade;

	@Override
	public Object processQuery() throws DataException 
	{
		final ProcessPseudoCardAttributes action = getAction();
		final ObjectId cardId = action.getCardId();
		if (null == cardId) {
			return null;
		}
		
		final Collection<PseudoAttribute> attrs = action.getAttributes();
		if (attrs.size() == 0) {
			return null;
		}

		checkLock(cardId);

		ObjectQueryBase fetchQuery = getQueryFactory().getFetchQuery(cardId.getType());
		fetchQuery.setId(cardId);
		Card card = getDatabase().executeQuery( this.getSystemUser(), fetchQuery);
		
		if (null == card) {
			return null;
		}

		Person person = null;
		UserRolesAndGroupsAttribute rolesAttr = null;
	
		for(PseudoAttribute attr : attrs) {
			if (card.getTemplate().equals(ObjectId.predefined(Template.class, "jbr.internalPerson"))) {
				if (attr instanceof PortalUserLoginAttribute) {
					String login = ((StringAttribute)(attr)).getValue();
					if (null != login) {
						checkForDuplicatePersons(login, cardId);
						//Create new person
						person = new Person();
						person.setLogin(login);
						person.setEmail(getUserEmail(card));
						person.setFullName(getUserFullName(card));
						person.setCardId(cardId);
						final SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(person);
						saveQuery.setObject(person);
						ObjectId objId = getDatabase().executeQuery( this.getUser(), saveQuery);
						person.setId(objId);
					}
				}else if (attr instanceof UserRolesAndGroupsAttribute) {
					Set<String> newRoleCodes = ((UserRolesAndGroupsAttribute)attr).getAssignedRoles();
					Set<String> newGroupCodes = ((UserRolesAndGroupsAttribute)attr).getAssignedGroups();
					if (null != newRoleCodes && null != newGroupCodes) {
						rolesAttr = (UserRolesAndGroupsAttribute)attr;
					}
				}
			}
		}

		//Process roles attribute
		if (null != rolesAttr) {
			if (null == person) {
				//Get existing person by card
				GetPersonByCard personByCardAction = new GetPersonByCard(cardId);
				person = getDataServiceBean().doAction(personByCardAction);
				if (null == person) {
					logger.error("Cannot find user in person table by cardId " + cardId.getId());
					throw new DataException("user.not.found");
				}
			}
			processUserRolesAttribute(person, cardId, rolesAttr);
		}
		return null;
	}
	
	private void processUserRolesAttribute(Person person, final ObjectId cardId, 
			final UserRolesAndGroupsAttribute rolesAttr) throws DataException {
		// Lock person
		getDataServiceBean().doAction(new LockObject(person));
		try {
			//1. Get new role groups and ungrouped roles assigned to user
			List<String> newRoleCodes = new LinkedList<String>(rolesAttr.getAssignedRoles());
			List<String> newGroupCodes = new LinkedList<String>(rolesAttr.getAssignedGroups());
			Map<String, Set<String>> excludedGroupRoleCodes = rolesAttr.getExcludedGroupRoleCodes();

			//2. Get old role groups and ungrouped roles assigned to user
			final ChildrenQueryBase listRolesQuery = getQueryFactory().getChildrenQuery(Person.class, UngroupedRole.class);
			listRolesQuery.setParent(person.getId());
			List<UngroupedRole> oldRoles = getDatabase().executeQuery( this.getUser(), listRolesQuery);
									
			final ChildrenQueryBase listGroupsQuery = getQueryFactory().getChildrenQuery(Person.class, Group.class);
			listGroupsQuery.setParent(person.getId());
			List<Group> oldGroups = getDatabase().executeQuery( this.getUser(), listGroupsQuery);

			//3. Collect unassigned roles and groups and old role and group names
			List<UngroupedRole> rolesToDelete = new ArrayList<UngroupedRole>();
			Set<String> oldRoleCodes = new HashSet<String>();
			for (UngroupedRole role : oldRoles){
				if (!newRoleCodes.contains(role.getType())) {
					rolesToDelete.add(role);
				}
				oldRoleCodes.add(role.getType());
			}
			List<Group> groupsToDelete = new ArrayList<Group>();
			Set<String> oldGroupCodes = new HashSet<String>();
			for (Group group : oldGroups){
				if (!newGroupCodes.contains(group.getType())) {
					groupsToDelete.add(group);
				}
				oldGroupCodes.add(group.getType());
			}
			//4. Assign new roles
			newRoleCodes.removeAll(oldRoleCodes);
			for (String roleCode : newRoleCodes){
				final UngroupedRole role = new UngroupedRole();
				ObjectId systemRoleId = new ObjectId(SystemRole.class, roleCode);
				SystemRole systemRole = (SystemRole)DataObject.createFromId(systemRoleId);				
				role.setSystemRole(systemRole);
				role.setPerson(person.getId());
				final SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(role);
				saveQuery.setObject(role);
				getDatabase().executeQuery( this.getUser(), saveQuery);
			}

			// 5. Assign new groups
			newGroupCodes.removeAll(oldGroupCodes);
			for (String groupCode : newGroupCodes) {
				final Group group = new Group();
				ObjectId systemGroupId = new ObjectId(SystemGroup.class, groupCode);
				SystemGroup systemGroup = (SystemGroup)DataObject.createFromId(systemGroupId);					
				group.setSystemGroup(systemGroup);
				group.setPerson(person.getId());
				group.setExcludedRoles(excludedGroupRoleCodes.get(groupCode));
				SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(group);
				saveQuery.setObject(group);
				getDatabase().executeQuery( this.getUser(), saveQuery);
			}
			
			// 6. Update existing groups
			oldGroups.removeAll(groupsToDelete);
			for (Group oldGroup : oldGroups) {
				// Update excluded roles list
				oldGroup.setExcludedRoles(excludedGroupRoleCodes.get(oldGroup.getType()));
				SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(oldGroup);
				saveQuery.setObject(oldGroup);
				getDatabase().executeQuery( this.getUser(), saveQuery);
			}

			// 7. Delete unassigned roles
			for(UngroupedRole role : rolesToDelete) {
				final ObjectQueryBase deleteQuery = getQueryFactory().getDeleteQuery(role.getId());
				deleteQuery.setId(role.getId());
				getDatabase().executeQuery( this.getUser(), deleteQuery);
			}
			
			// 8. Delete unassigned groups
			for(Group group : groupsToDelete) {
				final ObjectQueryBase deleteQuery = getQueryFactory().getDeleteQuery(group.getId());
				deleteQuery.setId(group.getId());
				getDatabase().executeQuery( this.getUser(), deleteQuery);
			}
			

		} finally {
			// Unlock person
			getDataServiceBean().doAction(new UnlockObject(person));
		}
	}
	
	private String getUserFullName(Card card) throws DataException
	{
		final StringBuilder builder = new StringBuilder(50);

		StringAttribute lastNameAttr = card.getAttributeById(ObjectId.predefined(StringAttribute.class, "jbr.person.lastName"));
		StringAttribute firstNameAttr = card.getAttributeById(ObjectId.predefined(StringAttribute.class, "jbr.person.firstName"));

		if (null != lastNameAttr) {
			builder.append(lastNameAttr.getValue());
			builder.append(" ");
		}
		if (null != firstNameAttr) {
			builder.append(firstNameAttr.getValue());
			builder.append(" ");
		}

		return builder.toString();
	}
	
	private String getUserEmail(Card card) throws DataException
	{
		String email = "none@none.com";
		StringAttribute emailAttr = card.getAttributeById(ObjectId.predefined(StringAttribute.class, "jbr.person.email"));
		if (null != emailAttr && !emailAttr.isEmpty()) {
			email = emailAttr.getStringValue();
		}

		return email;
	}
	
	private void checkForDuplicatePersons (String login, ObjectId cardId) throws DataException {
		GetPersonByLogin personByLoginAction = new GetPersonByLogin(login);
		Person user = getDataServiceBean().doAction(personByLoginAction);

		if (null != user) {
			logger.warn("Person with the same login already exists, login: " + login);
			throw new DataException("user.login.already.exists");
		}
		GetPersonByCard personByCardAction = new GetPersonByCard(cardId);
		user = getDataServiceBean().doAction(personByCardAction);
		
		try {
			PortalUser pUser = Portal.getFactory().getUserService().getByLogin(login);
			if (null != pUser) {
				logger.warn("User with the same login already exists in portal database, login: " + login);
				throw new DataException("user.login.already.exists");
			}
		} catch (PortalException e) {
			logger.error("Cannot check user presence in portal database by login: " + login, e);
			throw new DataException("user.login.check.fail");
		}
		
	}
	
	private DataServiceFacade getDataServiceBean() throws DataException {
		if (this.serviceFacade == null) {
			serviceFacade = new DataServiceFacade();
			serviceFacade.setUser(getUser());
			serviceFacade.setDatabase(getDatabase());
			serviceFacade.setQueryFactory(getQueryFactory());
		}
		return this.serviceFacade;
	}
}