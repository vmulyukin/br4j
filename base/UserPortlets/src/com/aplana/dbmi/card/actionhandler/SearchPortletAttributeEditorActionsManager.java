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
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.actionhandler.ActionHandler;
import com.aplana.dbmi.actionhandler.ActionsManager;
import com.aplana.dbmi.actionhandler.descriptor.ActionHandlerDescriptor;
import com.aplana.dbmi.actionhandler.descriptor.ActionsDescriptor;
import com.aplana.dbmi.card.JspAttributeEditor;
import com.aplana.dbmi.card.SearchPortletAttributeEditorActionHandler;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.search.SearchFilterPortlet;
import com.aplana.dbmi.search.SearchFilterPortletSessionBean;
import com.aplana.dbmi.service.DataException;

/**
 * Specific ActionManager for {@link SearchFilterPortlet} 
 * 
 * @author skashanski
 *
 */
public class SearchPortletAttributeEditorActionsManager extends ActionsManager {
	public final static String ACTION  = "cardPortletActionHandler";
	
	private SearchFilterPortletSessionBean sessionBean;
	private Attribute attribute;

	public Attribute getAttribute() {
		return attribute;
	}

	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}

	public SearchFilterPortletSessionBean getSessionBean() {
		return sessionBean;
	}

	public void setSessionBean(SearchFilterPortletSessionBean sessionBean) {
		this.sessionBean = sessionBean;
	}
	
	@Override
	protected void initializeInstance(ActionHandler handler) {
		if (handler instanceof SearchPortletAttributeEditorActionHandler) {
			SearchPortletAttributeEditorActionHandler h = (SearchPortletAttributeEditorActionHandler)handler;
			h.setAttribute(attribute);
			h.setSessionBean(sessionBean);
		}
	}

	@SuppressWarnings("unused")
	protected void prepareForProcessing(ActionHandler actionHandler,
		ActionRequest request, ActionResponse response) {
	}

	@Override
	public boolean processAction(ActionRequest request, ActionResponse response) throws DataException {
		String action = request.getParameter(SearchFilterPortlet.ACTION_FIELD);
		if (action == null) {
			return false;
		}
		for( String actionId: getActiveActionIds()) {
			String actionCode = ACTION + ':' + actionId;
			if (actionCode.equals(action)) {
				ActionHandler handler = createInstance(actionId);
				handler.process( getCardIds(request, attribute), request, response);
				
				return true;
			}
		}
		return false;
	}

	@Override
	public List<String> getActiveActionIds() {
		final List<String> actions = super.getActiveActionIds();
		final List<String> filtered = new ArrayList<String>(actions.size());
		for (String actionId: actions ) {
			ActionHandlerDescriptor ahd = getActionsDescriptor().getActionHandlerDescriptor(actionId);
			if (ahd.isForEditMode() ||ahd.isForViewMode())
				filtered.add(actionId);
		}
		logger.info(filtered.size() + " of " + actions.size() + " actions available " +
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
	
	public static SearchPortletAttributeEditorActionsManager getInstance(SearchFilterPortletSessionBean sessionBean,
			ActionsDescriptor actionsDescriptor, Attribute attr) {
		
		SearchPortletAttributeEditorActionsManager am = new SearchPortletAttributeEditorActionsManager();
		
		am.setSessionBean(sessionBean);		
		am.setPortletFormManager(sessionBean.getPortletFormManager());
		am.setActionsDescriptor(actionsDescriptor);
		am.setAttribute(attr);
		am.setServiceBean(sessionBean.getServiceBean());
		
		return am;
	}
}