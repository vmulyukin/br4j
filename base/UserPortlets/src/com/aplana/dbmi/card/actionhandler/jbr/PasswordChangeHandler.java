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
package com.aplana.dbmi.card.actionhandler.jbr;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.action.GetPersonByCard;
import com.aplana.dbmi.card.Parametrized;
import com.aplana.dbmi.card.PortletFormManager;
import com.aplana.dbmi.card.PortletFormManagerAware;
import com.aplana.dbmi.card.actionhandler.CardPortletAttributeEditorActionHandler;
import com.aplana.dbmi.card.hierarchy.CardFilterCondition;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;

public class PasswordChangeHandler extends CardPortletAttributeEditorActionHandler
		implements PortletFormManagerAware, Parametrized {
	//Password change is allowed for users with roles specified in action settings file
	private static final String PARAM_EDIT_PASSWORD_ROLES = "editPasswordRoles";
	//Parameter indicating whether password change is allowed for card owner or not
	private static final String PARAM_CAN_OWNER_EDIT_PASSWORD = "canOwnerEditPassword";
	
	private PortletFormManager portletFormManager;
	
	private boolean canCardOwnerEdit = false;
	private Set<ObjectId> editRoles = new HashSet<ObjectId>();

	public void setPortletFormManager(PortletFormManager portletFormManager) {
		this.portletFormManager = portletFormManager;
	}

	public void setParameter( String name, String value )
	{
		if (PARAM_EDIT_PASSWORD_ROLES.equals(name)) {
			if( value != null && !value.equals("") ) {
				String[] role_ids = value.split(",");
				if( role_ids.length > 0 ) {
					for( String role_id: role_ids ) {
						editRoles.add(new ObjectId(SystemRole.class, role_id));
					}
				}
			}
		}else if (PARAM_CAN_OWNER_EDIT_PASSWORD.equals(name)) {
			canCardOwnerEdit = Boolean.parseBoolean(value);
		}
	}

	/*
	 * Open password change form
	 * @see com.aplana.dbmi.card.actionhandler.CardPortletAttributeEditorActionHandler#process(com.aplana.dbmi.model.Attribute, java.util.List, javax.portlet.ActionRequest, javax.portlet.ActionResponse)
	 */
	@Override
	protected void process(Attribute attr, List<ObjectId> cardIds,
			ActionRequest request, ActionResponse response)
			throws DataException {
		PasswordChangeForm form = new PasswordChangeForm();
		if (isCardOwner()) {
			form.setPassValidationRequired(true);
		}
		portletFormManager.openForm(form);
	}
	
	@Override
	public boolean isApplicableForUser() {
		final CardFilterCondition condition = getCondition();
		boolean isApplicable = false;
		if (condition != null && condition.check(getCardPortletSessionBean().getActiveCard())) {
			if (canCardOwnerEdit && isCardOwner()) {
				isApplicable = true;
			}else if (!editRoles.isEmpty()) {
				Person currentUser = getCardPortletSessionBean().getServiceBean().getPerson();
				Collection<Role> userRoles = null;
				try {
					userRoles = serviceBean.listChildren(currentUser.getId(), Role.class);
				} catch (Exception e) {
					logger.error("Cannot get roles for current user: " + currentUser.getLogin(), e);
				}
				if (userRoles != null && !userRoles.isEmpty() ) {
					Iterator<Role> ir = userRoles.iterator();
					while (ir.hasNext()) {
						if (editRoles.contains(ir.next().getSystemRole().getId())) {
							isApplicable = true;
							break;
						}
					}
				}
			}
		}
		return isApplicable;
	}
	
	private boolean isCardOwner(){
		boolean isCardOwner = false;
		DataServiceBean serviceBean = getCardPortletSessionBean().getServiceBean();
		ObjectId cardId = getCardPortletSessionBean().getActiveCard().getId();
		GetPersonByCard action = new GetPersonByCard(cardId);
		try {
			Person person = (Person)serviceBean.doAction(action);
			if (null != person) {
				if (person.equals(serviceBean.getPerson()))
					// Card is opened by its owner
					isCardOwner = true;
			}

		} catch (Exception e) {
			logger.error("Cannot get user login by card " + cardId, e);
		}
		return isCardOwner;
	}
}
