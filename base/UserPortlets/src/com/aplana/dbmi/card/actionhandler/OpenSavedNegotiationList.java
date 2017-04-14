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
import com.aplana.dbmi.card.OrderWithAttachmentList;
import com.aplana.dbmi.card.PortletFormManager;
import com.aplana.dbmi.card.PortletFormManagerAware;
import com.aplana.dbmi.card.SavedNegotiationRoutes;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;



public class OpenSavedNegotiationList extends ActionHandler
implements PortletFormManagerAware{
	@SuppressWarnings("unused")
	private PortletFormManager portletFormManager;

	@Override
	public void process(List<ObjectId> cardIds, ActionRequest request,
			ActionResponse response) throws DataException {
		portletFormManager.openForm(new SavedNegotiationRoutes());
		
	}

	public void setPortletFormManager(PortletFormManager portletFormManager) {
		this.portletFormManager = portletFormManager;
	}

}
