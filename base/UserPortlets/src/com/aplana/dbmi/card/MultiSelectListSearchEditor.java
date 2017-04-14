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

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.MultiListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.portlet.ActionRequest;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MultiSelectListSearchEditor extends ListSearchEditor {

	public MultiSelectListSearchEditor() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/MultiSelectList.jsp");
	}
	
	@Override
	public boolean gatherData(ActionRequest request, Attribute attr) throws DataException {
		String value = request.getParameter(getAttrHtmlId(attr) + "_values");
		if (value == null)
			return false;

		if (INVALID_VALUE_ID.equals(value)) {
			throw new DataException("edit.page.error.id", new Object[] { attr.getName() });
		} else if (value.equals("") || value.equals(EMPTY_VALUE_ID)) {
			((MultiListAttribute) attr).setValues(null);
		} else {
			try {
				String[] values = value.split(",");
				final List<ReferenceValue> referenceValues = new ArrayList<ReferenceValue>();
				DataServiceBean dataServiceBean = getDataServiceBean(request);
				for(String val : values) {
					ReferenceValue refVal = new ReferenceValue();
					refVal.setId(Long.parseLong(val.trim()));
					ObjectId idd = ((MultiListAttribute) attr).getReference();
					Collection<ReferenceValue> attrCol = dataServiceBean.listChildren(idd, ReferenceValue.class);
					for (ReferenceValue obj : attrCol) {
						if (obj.getId().getId().equals(refVal.getId().getId())) refVal = obj;
					}
					referenceValues.add(refVal);
				}
				((MultiListAttribute) attr).setValues(referenceValues);
			} catch (NumberFormatException e) {
				throw new DataException("edit.page.error.id", new Object[] { attr.getName() });
			} catch (ServiceException e) {
				logger.error(e);
			}
		}
		return true;
	}
	
	@Override
	public void writeEditorCode(RenderRequest request, RenderResponse response,
			Attribute attr) throws IOException, PortletException {
		try {
			JSONObject refJSON = getReferenceJSON(getValueList(request, attr));
			request.setAttribute("allValuesList", refJSON.toString());
			super.writeEditorCode(request, response, attr);
		} catch (Exception e) {
			throw new PortletException(e);
		} finally {
			request.removeAttribute("allValuesList");
		}
	}
	
	private JSONObject getReferenceJSON(Collection<ReferenceValue> ref) throws ServletException {
		try {
			JSONObject json = new JSONObject();
			for (ReferenceValue value : ref) {
				json.put(value.getId().getId().toString(), value.getValue() != null ? value.getValue().trim() : null);
			}
			return json;
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
	
	public static JSONArray getSelectedValuesJSON(MultiListAttribute attr) {
		JSONArray json = new JSONArray();
		if (attr.getValues() != null) {
			for (ReferenceValue value : attr.getValues()) {
				json.put(value.getId().getId().toString());
			}
		}
		return json;
	}
}
