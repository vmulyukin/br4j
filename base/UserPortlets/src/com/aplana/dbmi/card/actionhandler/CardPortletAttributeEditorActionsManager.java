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
package com.aplana.dbmi.card.actionhandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.json.JSONArray;

import com.aplana.dbmi.actionhandler.ActionHandler;
import com.aplana.dbmi.actionhandler.ActionsManager;
import com.aplana.dbmi.actionhandler.descriptor.ActionHandlerDescriptor;
import com.aplana.dbmi.actionhandler.descriptor.ActionsDescriptor;
import com.aplana.dbmi.card.AttributeEditor;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.JspAttributeEditor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class CardPortletAttributeEditorActionsManager extends ActionsManager {
	public final static String ACTION  = "cardPortletActionHandler";

	private CardPortletSessionBean sessionBean;
	private Attribute attribute;

	public Attribute getAttribute() {
		return attribute;
	}

	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}

	public CardPortletSessionBean getSessionBean() {
		return sessionBean;
	}

	public void setSessionBean(CardPortletSessionBean sessionBean) {
		this.sessionBean = sessionBean;
	}

	@Override
	protected void initializeInstance(ActionHandler handler) {
		if (handler instanceof AttributeEditorActionHandler) {
			AttributeEditorActionHandler h = (AttributeEditorActionHandler)handler;
			h.setAttribute(attribute);
			h.setCardPortletSessionBean(sessionBean);
		}
	}

	@SuppressWarnings("unused")
	protected void prepareForProcessing(ActionHandler actionHandler,
		ActionRequest request, ActionResponse response) {
	}

	@Override
	public boolean processAction(ActionRequest request, ActionResponse response) throws DataException {
		String action = request.getParameter(CardPortlet.ACTION_FIELD);
		if (action == null) {
			return false;
		}
		for( String actionId: getActiveActionIds()) {
			String actionCode = ACTION + ':' + actionId;
			if (actionCode.equals(action)) {
				ActionHandler handler = createInstance(actionId);
				handler.process( getCardIds(request, attribute), request, response);
				sessionBean.getActiveCardInfo().setAttributeEditorData(attribute.getId(), AttributeEditor.KEY_VALUE_CHANGED, Boolean.TRUE);
				return true;
			}
		}
		return false;
	}

	@Override
	public List<String> getActiveActionIds() {
		final String mode = sessionBean.getActiveCardInfo().getMode();
		final DataServiceBean serviceBean = sessionBean.getServiceBean();
		Person person = serviceBean.getPerson();
		// ������ ������������
		Person fetchPerson=null;
		try {
			fetchPerson = (Person)serviceBean.getById(person.getId());
		} catch (DataException e) {
			logger.error(e);
		} catch (ServiceException e) {
			logger.error(e);
		}
		Collection<Role> roles=null;
		if (null != fetchPerson) {
			roles = fetchPerson.getRoles();
		}
		HashSet<String> sysRoles = new HashSet<String>();
		if(null != roles)
			for(Role role : roles) {
				sysRoles.add(role.getSystemRole().getId().getId().toString());
			}
		final List<String> actions = super.getActiveActionIds();
		final List<String> filtered = new ArrayList<String>(actions.size());
		for (String actionId: actions ) {
			boolean isIncludeRole = true;
			ActionHandlerDescriptor ahd = getActionsDescriptor().getActionHandlerDescriptor(actionId);
			String roleAction = ahd.getRoleForMode();
			if (null != roleAction) {
				isIncludeRole = sysRoles.contains(roleAction);
			}
			if (CardPortlet.CARD_EDIT_MODE.equals(mode) && ahd.isForEditMode() && isIncludeRole ||
					CardPortlet.CARD_VIEW_MODE.equals(mode) && ahd.isForViewMode() && isIncludeRole)
				if (!ahd.isNeedWritePermission() || 
						(ahd.isNeedWritePermission() && sessionBean.getActiveCardInfo().isCanChange()))
					filtered.add(actionId);
		}
		logger.info(filtered.size() + " of " + actions.size() + " actions available in " + mode +
				" for " + attribute.getId().getId());
		return filtered;
	}

	@SuppressWarnings("unchecked")
	public static List<ObjectId> getCardIds(ActionRequest request, Attribute attribute) {
		String selectedCards = request.getParameter(getSelectedCardIdsParameterName(attribute));
		return (selectedCards != null && !"".equals(selectedCards))
			? ObjectIdUtils.commaDelimitedStringToNumericIds(selectedCards, Card.class)
			: new ArrayList<ObjectId>(0);
	}

	public static String getSelectedCardIdsParameterName(Attribute attr) {
		return JspAttributeEditor.getAttrHtmlId(attr) + "_selectedItems";
	}

	public static CardPortletAttributeEditorActionsManager getInstance(CardPortletSessionBean sessionBean,
			ActionsDescriptor actionsDescriptor, Attribute attr) {
		CardPortletAttributeEditorActionsManager am = new CardPortletAttributeEditorActionsManager();
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		am.setSessionBean(sessionBean);
		am.setPortletFormManager(cardInfo.getPortletFormManager());
		am.setActionsDescriptor(actionsDescriptor);
		am.setAttribute(attr);
		am.setServiceBean(sessionBean.getServiceBean());
		return am;
	}

	@Override
	public JSONArray getActionsJSON() {
		return getActionsJSON(attribute);
	}
}
