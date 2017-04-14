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

import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.ajax.JasperReportServlet;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.actionhandler.CardPortletAttributeEditorActionHandler;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;

public class PrintRelatedDocsHandler extends CardPortletAttributeEditorActionHandler {
	protected void process(Attribute attr, List cardIds, ActionRequest request, ActionResponse response) throws DataException {
		CardPortletSessionBean sessionBean = getCardPortletSessionBean();
		ObjectId cardId = sessionBean.getActiveCard().getId();
		
		if (cardId != null) {
			String urlReport = request.getContextPath() + "/servlet/JasperReportServlet?" +
				JasperReportServlet.PARAM_NAME_CONFIG + "=" + JasperReportServlet.CONFIG_BOUND_CARDS+ "&" +
				JasperReportServlet.PARAM_CARDID + "=" + JasperReportServlet.PREFIX_TYPEDATA_LONG + cardId.getId().toString();
			try {
				response.sendRedirect(urlReport);
			} catch (Exception e) {
				throw new DataException(e);
			}
		} else {
			logger.warn("Cannot print related documents for newly created card. Ignoring.");
		}
	}
}
