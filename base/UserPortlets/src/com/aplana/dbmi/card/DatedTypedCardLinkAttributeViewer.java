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
import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.PortletException;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.service.DataException;

/**
 * 
 * @author ppolushkin
 *
 */
public class DatedTypedCardLinkAttributeViewer extends TypedCardLinkAttributeViewer {
	
	public static final String PARAM_DATE_TYPE_CAPTION = "dateCaption";
	
	public static final String KEY_DATE_TYPE_CAPTION = "dateCaption";
	
	private String dateCaption;
	
	public DatedTypedCardLinkAttributeViewer() {
		//setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/TypedLinksView.jsp");
		//setParameter(PARAM_INIT_JSP, "/WEB-INF/jsp/html/attr/TypedLinksViewInclude.jsp");
	}
	
	public void setParameter(String name, String value) {
		if (PARAM_DATE_TYPE_CAPTION.equalsIgnoreCase(name))
			dateCaption = value;
		else
			super.setParameter(name, value);
	}
	
	public void initEditor(PortletRequest request, Attribute attr) throws DataException {
		super.initEditor(request, attr);
	}
	
	public void loadAttributeValues(Attribute attr, PortletRequest request) {
		super.loadAttributeValues(attr, request);
		CardPortletSessionBean sessionBean = (CardPortletSessionBean)request.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);		
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		cardInfo.setAttributeEditorData(attr.getId(), KEY_DATE_TYPE_CAPTION, dateCaption);
	}

	public boolean isValueCollapsable() {
		return true;
	}

	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException {
		return false;
	}
	
	public void writeEditorCode(RenderRequest request, RenderResponse response,
			Attribute attr) throws IOException, PortletException {
		super.writeEditorCode(request, response, attr);
	}

}
