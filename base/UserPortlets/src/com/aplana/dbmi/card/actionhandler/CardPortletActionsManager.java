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

import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.actionhandler.ActionHandler;
import com.aplana.dbmi.actionhandler.ActionsManager;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.service.DataException;

public class CardPortletActionsManager extends ActionsManager {

	public static final String ACTION = "cardPortletActionsManagerAction";
	private CardPortletSessionBean sessionBean;
	
	public void setSessionBean(CardPortletSessionBean sessionBean) {
		this.sessionBean = sessionBean;
	}

	@Override
	protected void initializeInstance(ActionHandler handler) {
		final CardPortletActionHandler h = (CardPortletActionHandler)handler;
		h.setServiceBean(sessionBean.getServiceBean());
		h.setSessionBean(sessionBean);
	}

	@Override
	public boolean processAction(ActionRequest request, ActionResponse response)
			throws DataException {
		final String action = request.getParameter(CardPortlet.ACTION_FIELD);
		final List<String> activeList = getActiveActionIds();
		if (action != null && activeList != null) {
			for (String actionId : activeList) {
				final String actionCode = ACTION + ':' + actionId;
				if (actionCode.equals(action)) {
					final ActionHandler handler = createInstance(actionId);
					handler.process( null, request, response );
					return true;
				}
			}
		}
		return false;
	}

}
