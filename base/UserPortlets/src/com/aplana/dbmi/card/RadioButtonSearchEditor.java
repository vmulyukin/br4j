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
import java.util.Map;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ReferenceValue;

public class RadioButtonSearchEditor extends ListSearchEditor {

	/**
	 * Name of the parameter that defined objectId value of selected item
	 */
	protected final static String SELECTED_ID_KEY = "selectedId";
	/**
	 * Name of the parameter that defines the orientation of the radio buttons
	 */
	private static final String PARAM_ORIENTATION = "orientation";
	
	/**
	 * Value of parameter orientation - vertically
	 */
	private static final String ORIENT_VERT = "vertically";

	private boolean isVerticalAlignment;
	
	/**
	 * Create new RadioButtonAttributeEditor with PARAM_JSP = /WEB-INF/jsp/html/attr/RadioButton.jsp
	 */
	public RadioButtonSearchEditor() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/RadioButton.jsp");
		setParameter(PARAM_INIT_JSP, "/WEB-INF/jsp/html/attr/RadioButtonInclude.jsp");
	}
	
	/**
	 * Sets parameters
	 */
	public void setParameter(String name, String value) {
		if (name.equals(PARAM_ORIENTATION)) {
			isVerticalAlignment = value.equals(ORIENT_VERT);
		} else {
			super.setParameter(name, value);
		}
	}
	
	public void writeEditorCode(RenderRequest request, RenderResponse response,
			Attribute attr) throws IOException, PortletException {
		try {
			request.setAttribute("isVerticalAlignment", new Boolean(isVerticalAlignment));
			super.writeEditorCode(request, response, attr);
		} finally {
			request.removeAttribute("isVerticalAlignment");
		}
	}
	
	@Override
	protected Map<String, Object> getReferenceData(Attribute attr, PortletRequest request) throws PortletException {
		Map<String, Object> referenceData =  super.getReferenceData(attr, request);
		Collection<ReferenceValue> valuesList = getValueList(request, attr);

		referenceData.put(VALUES_LIST_KEY, valuesList);
		referenceData.put("query", getDataQuery());
		return referenceData;
	}
}
