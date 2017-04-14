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
package com.aplana.dbmi.jbr.processors;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aplana.dbmi.action.GetPersonByLogin;
import com.aplana.dbmi.model.*;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortalException;
import com.aplana.dbmi.PortalUser;
import com.aplana.dbmi.UserService;

import com.aplana.dbmi.action.GetPersonByCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;

import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.PortalUserProcessException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;


/**
 * Processor used for user and his roles/groups synchronization from database to portal
 *
 * @author Vlad Alexandrov
 * @version 1.0
 * @since   2014-11-26
 */

public class SyncPortalUserProcessor extends ProcessCard
{
	private static final long serialVersionUID = 1L;

	private static ObjectId USER_ACTIVE_CARD_STATE = ObjectId.predefined(CardState.class, "user.active");

	@Override
	public Object process() throws DataException {

		final ObjectId cardId = getCardId();
		Card card = null;
		switch (this.getCurExecPhase()) {
			case POSTPROCESS: {
				card = loadCardById(cardId);
				break;
			}
			case PREPROCESS: {
				card = getCard();
				break;
			}
		}

		if (null == cardId || null == card) {
			logger.warn("Impossible to execute processor until card is saved -> exiting");
			return null;
		}

		//1. Get existing user by card
		GetPersonByCard personByCardAction = new GetPersonByCard(card.getId());
		final Person person = execAction(personByCardAction);
		if (null == person) {
			logger.error("Cannot find user in person table by cardId: " + card.getId().getId());
			throw new DataException("user.not.found");
		}
		Person personByLogin = execAction(new GetPersonByLogin(person.getLogin()));
		if(personByLogin != null && !personByLogin.getId().equals(person.getId())){
			logger.error("There are active user (person_id=" + person.getId().getId() + ") with same login");
			throw new DataException("user.login.conflict", new Object[]{personByLogin.getCardId().getId()});
		}
		person.setEmail(getUserEmail(card));
		person.setFullName(getUserFullName(card));
		person.setActive(card.getState().equals(USER_ACTIVE_CARD_STATE));
		// Lock person
		execAction(new LockObject(person));

		try {
			//2. Refresh person data (full name and email) in person table
			final SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(person);
			saveQuery.setObject(person);
			getDatabase().executeQuery( this.getUser(), saveQuery);
		} catch (DataException e) {
			try {
				// Unlock person
				execAction(new UnlockObject(person));
			} catch (DataException e1) {
				logger.error("An unexpected exception occurred while trying to UNLOCK person with ID: " + person.getId().getId(), e1);
			}
			throw e;
		}
		//3. Get all roles (grouped and ungrouped) granted to user
		final ChildrenQueryBase listRolesQuery = getQueryFactory().getChildrenQuery(Person.class, Role.class);
		listRolesQuery.setParent(person.getId());
		List<Role> assignedRoles = getDatabase().executeQuery( this.getUser(), listRolesQuery);

		final Set<String> allUserRoles = new HashSet<String>();
		for (Role role : assignedRoles) {
			allUserRoles.add((String)role.getSystemRole().getId().getId());
		}

		final Map<String, Collection<String>> mapPersonRoles = new HashMap<String, Collection<String>>();
		mapPersonRoles.put(person.getLogin(), allUserRoles);

		try {
			final PortalUser pUser = Portal.getFactory().getUserService().getByLogin(person.getLogin());
			if (null == pUser) {
				if (TransactionSynchronizationManager.isSynchronizationActive()) {
					TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
						@Override
						public void beforeCommit(boolean readOnly) {
							try {
								// Create new portal user
								final PortalUser newUser = new PortalUser();
								newUser.setEmail(person.getEmail());
								newUser.setFullName(person.getFullName());
								newUser.setLogin(person.getLogin());
								Portal.getFactory().getUserService().createUser(newUser);
								Portal.getFactory().getUserService().grantRole(person.getLogin(), UserService.PREFIX_PORTAL_ROLENAME + UserService.PORTAL_USER);
								// Synchronize portal user roles
								Portal.getFactory().getUserService().synchJbrPersonRoles(mapPersonRoles, false);
							} catch (PortalException e) {
								logger.error("Cannot create portal user with login: " + person.getLogin(), e);
								throw new PortalUserProcessException("user.create.fail");
							}
						}
					});
				}
			}
			else if (TransactionSynchronizationManager.isSynchronizationActive()) {
				TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
					@Override
					public void beforeCommit(boolean readOnly) {
						try {
							// Refresh portal user data (full name and email)
							final PortalUser user = new PortalUser();
							user.setEmail(person.getEmail());
							user.setFullName(person.getFullName());
							user.setLogin(person.getLogin());
							Portal.getFactory().getUserService().updateUser(user);
							// Synchronize portal user roles
							Portal.getFactory().getUserService().synchJbrPersonRoles(mapPersonRoles, false);
						} catch (PortalException e) {
							logger.error("Cannot synchronize portal roles for user " + person.getLogin(), e);
							throw new PortalUserProcessException("user.roles.sync.fail");
						}
					}
				});
			}

		} catch (PortalException e) {
			logger.error("Cannot check user presence in portal database by login: " + person.getLogin(), e);
			throw new DataException("user.login.check.fail");

		}

		return getResult();
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

		return builder.toString().trim();
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
}