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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.ServletException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class MultiSelectAttributeEditor extends JspAttributeEditor {
	
	public MultiSelectAttributeEditor() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/MultiSelect.jsp");
	}

	@Override
	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException {
		String valuesStr = request.getParameter(getAttrHtmlId(attr) + "_values");
		if (valuesStr == null)
			return false;
		
		TreeAttribute treeAttr = (TreeAttribute) attr;
		if ("".equals(valuesStr.trim())) {
			treeAttr.setValues(null);
			return true;
		}
		
		String[] ids = valuesStr.split(",");
		List values = new ArrayList();
		for (int i = 0; i < ids.length; i++) {
			values.add(DataObject.createFromId(new ObjectId(ReferenceValue.class, Long.parseLong(ids[i]))));
		}
		treeAttr.setValues(values);
		return true;
	}

	@Override
	public void writeEditorCode(RenderRequest request, RenderResponse response,
			Attribute attr) throws IOException, PortletException {
		try {
			JSONObject refJOSN = getReferenceJSON(loadReference((TreeAttribute)attr, request));
			request.setAttribute("allValuesList", refJOSN.toString());
			super.writeEditorCode(request, response, attr);
		} catch (Exception e) {
			throw new PortletException(e);
		} finally {
			request.removeAttribute("allValuesList");
		}
	}

	private JSONObject getReferenceJSON(Collection/*<ReferenceValue>*/ ref) throws ServletException {
		try {
			JSONObject json = new JSONObject();
			
			Iterator iterValues/*<ReferenceValue>*/ = ref.iterator();
			while (iterValues.hasNext()) {
				ReferenceValue value = (ReferenceValue)iterValues.next();
				json.put(value.getId().getId().toString(), value.getValue() != null ? value.getValue().trim() : null);
			}
			return json;
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	
	protected DataServiceBean getDataServiceBean(PortletRequest request) {
		
		CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
		return sessionBean.getServiceBean();
		
	}
	
	protected Collection/*<ReferenceValue>*/ loadReference(TreeAttribute attribute, PortletRequest request) throws DataException {
		
		DataServiceBean dataServiceBean = getDataServiceBean(request);
		
		ObjectId id = attribute.getReference();
		
		try {
			
			return dataServiceBean.listChildren(id, ReferenceValue.class);
			
		} catch (ServiceException e) {
			throw new DataException(e);
		}
    }
	
	public static JSONArray getSelectedValuesJSON(TreeAttribute attr) {
		JSONArray json = new JSONArray();
		if (attr.getValues() != null) {
			Iterator iterValues = attr.getValues().iterator();
			while (iterValues.hasNext()) {
				ReferenceValue value = (ReferenceValue)iterValues.next();
				json.put(value.getId().getId().toString());
			}
		}
		return json;
	}
}
