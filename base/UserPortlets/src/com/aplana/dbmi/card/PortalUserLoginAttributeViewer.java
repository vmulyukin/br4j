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

import java.io.IOException;
import java.util.Collection;

import javax.portlet.ActionRequest;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.aplana.dbmi.action.GetPersonByCard;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;

public class PortalUserLoginAttributeViewer extends ActionsSupportingAttributeEditor {
	public final static String USER_LOGIN  = "portalUserLogin";
	
	public PortalUserLoginAttributeViewer() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/PortalUserLoginView.jsp");
	}
	
	@Override
	public void initEditor(PortletRequest request, Attribute attr)
		throws DataException {
		super.initEditor(request, attr);
	}
	
	@Override
	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException {
		return false;
	}
	
	@Override
	public void writeEditorCode(RenderRequest request, RenderResponse response, Attribute attr) throws IOException, PortletException {
		CardPortletSessionBean sessionBean = getCardPortletSessionBean(request);
		DataServiceBean serviceBean = sessionBean.getServiceBean();
		GetPersonByCard action = new GetPersonByCard(sessionBean.getActiveCard().getId());
		try {
			Person person = (Person)serviceBean.doAction(action);
			if (null != person) {
				sessionBean.getActiveCardInfo().setAttributeEditorData(attr.getId(), USER_LOGIN, person.getLogin());
			}
			super.writeEditorCode(request, response, attr);
		} catch (Exception e) {
			logger.error("Cannot get user login.", e);
		}
	}
}

