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
package com.aplana.dbmi.card;

import com.aplana.dbmi.action.GetCardDSHistory;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.util.List;

public class CardDSHistoryAttributeViewer extends JspAttributeViewer {
	
	public final static String CARD_HISTORY_LIST = CardHistoryAttributeViewer.CARD_HISTORY_LIST;
	public CardDSHistoryAttributeViewer() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/CardDSHistoryView.jsp");
	}
	
	public void initEditor(PortletRequest request, Attribute attr)
	throws DataException {
	}
	
	public void writeEditorCode(RenderRequest request, RenderResponse response, Attribute attr) throws IOException, PortletException {
		CardPortletSessionBean sessionBean = getCardPortletSessionBean(request);
		DataServiceBean serviceBean = sessionBean.getServiceBean();
		GetCardDSHistory action = new GetCardDSHistory(); 
		action.setCard(sessionBean.getActiveCard().getId());
		try {
			List recs = (List)serviceBean.doAction(action);
			sessionBean.setAttributeEditorData(attr.getId(), CARD_HISTORY_LIST, recs);
			super.writeEditorCode(request, response, attr);
		} catch (Exception e) {
			new DataException(e);
		}
		try {
		} finally {
			request.removeAttribute("valuesList");
		}
	}
	
	public boolean isValueCollapsable() {
		return true;
	}

	public boolean doesFullRendering(Attribute attr) {
		return true;
	}

}
