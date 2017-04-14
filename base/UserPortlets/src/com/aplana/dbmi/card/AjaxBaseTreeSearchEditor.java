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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.servlet.ServletException;

import org.json.JSONException;
import org.json.JSONWriter;


import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.TreeAttribute;

import com.aplana.dbmi.service.DataException;

/**
 * Represents base class for TreeAttribute's children editors in extended search
 *   
 * @author skashanski
 *
 */
public abstract class AjaxBaseTreeSearchEditor extends JspAttributeEditor  {

	public AjaxBaseTreeSearchEditor() {
		
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/AjaxTreeSearch.jsp");
		
		setParameter(PARAM_INIT_JSP, "/WEB-INF/jsp/html/attr/AjaxTreeInclude.jsp");
		
	}
	
	
	public boolean isValueCollapsable() {
		return true;
	}	
	
	
	
	@Override
	protected Map<String, Object> getReferenceData(Attribute attr, PortletRequest request) 
		throws PortletException 
	{

		Map<String, Object> result = super.getReferenceData(attr, request);
		if (result != null) {
		
				try {
					result.put("values", getJSONReferences((TreeAttribute) attr));
					result.put("selectValues", getJSONValues((TreeAttribute)attr));
					return result;
				} catch (Exception e) {
					throw new PortletException(e);
				}

		}
		return result;
	}	
	
	public String getJSONValues(TreeAttribute attr) throws ServletException {
		
		StringWriter w = new StringWriter();
		
		JSONWriter jw = new JSONWriter(w);
		
		try {
			jw.array();
			if(attr!=null && attr.getValues()!=null){
			Iterator iterVal = attr.getValues().iterator();
				while(iterVal.hasNext()) {
					DataObject dataObject = (DataObject)iterVal.next();
					jw.object();
					jw.key("id").value(dataObject.getId().getId().toString());
					jw.endObject();
				}
			}
			jw.endArray();
			return w.toString();
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}	
	
	
	
	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException {

		TreeAttribute treeAttr = (TreeAttribute) attr;
		String param = request
				.getParameter(getAttrHtmlId(treeAttr) + "_values");

		if (param == null)
			return false;

		String[] idVals = param.split(", ");
		List values = new ArrayList();
		for (int i = 0; i < idVals.length; i++) {
			if (!idVals[i].equals("")) {
				DataObject dataObject = createDataObject(Long.parseLong(idVals[i])); 
				values.add(dataObject);
			}
		}
		
		treeAttr.setValues(values);
		
		return true;

	}	
	
	protected abstract DataObject createDataObject(long objectId);
	
	
	public  String getJSONReferences(TreeAttribute attr) throws ServletException {
		
		try {
			
			StringWriter w = new StringWriter();
			JSONWriter jw = new JSONWriter(w);

			jw.object();
			jw.key("identifier").value("id");
			jw.key("label").value("name");
			jw.key("items");
			jw.array();
			
			writeCardStateCollection(jw, attr, true);
			
			jw.endArray();
			jw.endObject();
			
			return w.toString();
			
		} catch (JSONException e) {
			throw new ServletException(e);
		}
		
		
	}
	
	
	
	protected abstract void writeCardStateCollection(JSONWriter jw, TreeAttribute attr, boolean topNodes) throws JSONException;
}
