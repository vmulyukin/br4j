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


import org.json.JSONException;
import org.json.JSONWriter;

import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;

import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TemplateSearchAttribute;
import com.aplana.dbmi.model.TreeAttribute;

/**
 * Represents editor class for TemplateSearchAttribute in extended search
 * Provides functionality for handling requests and displaying TemplateSearchAttribute as Html Tree Control
 *    
 * @author skashanski
 *
 */
public class AjaxTreeTemplateSearchEditor  extends AjaxBaseTreeSearchEditor {
	
	
	/*
	
	
	public AjaxTreeTemplateSearchEditor() {
		
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/AjaxTreeSearch.jsp");
		
		setParameter(PARAM_INIT_JSP, "/WEB-INF/jsp/html/attr/AjaxTreeInclude.jsp");
		
	}
	
	
	public boolean isValueCollapsable() {
		return true;
	}
	
	
	
	public static String getJSONValues(TreeAttribute attr) throws ServletException {
		
		StringWriter w = new StringWriter();
		
		JSONWriter jw = new JSONWriter(w);
		
		try {
			jw.array();
			if(attr!=null && attr.getValues()!=null){
			Iterator iterVal = attr.getValues().iterator();
				while(iterVal.hasNext()) {
					Template template = (Template)iterVal.next();
					jw.object();
					jw.key("id").value(template.getId().getId().toString());
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
				values.add(DataObject.createFromId(new ObjectId(
						Template.class, Long.parseLong(idVals[i]))));
			}
		}
		
		treeAttr.setValues(values);
		
		return true;

	}
	
	
	
	private static void writeCardStateCollection(JSONWriter jw, Collection templates, boolean topNodes) throws JSONException {
		
		Iterator iterCardState = templates.iterator();
		
		while(iterCardState.hasNext()) {
			
			Template template = (Template)iterCardState.next();
			
			jw.object();
			jw.key("id").value(template.getId().getId().toString());
			jw.key("name").value(template.getName());
			if (topNodes) 
				jw.key("type").value("topNodes");
			jw.endObject();
		}
	}	
	
	

	public static String getJSONReferences(TemplateSearchAttribute attr) throws ServletException {
		
		try {
			
			StringWriter w = new StringWriter();
			JSONWriter jw = new JSONWriter(w);

			jw.object();
			jw.key("identifier").value("id");
			jw.key("label").value("name");
			jw.key("items");
			jw.array();
			
			writeCardStateCollection(jw, attr.getCardTemplates(), true);
			
			jw.endArray();
			jw.endObject();
			
			return w.toString();
			
		} catch (JSONException e) {
			throw new ServletException(e);
		}
		
		
	}

	

}
*/







	@Override
	protected DataObject createDataObject(long objectId) {

		return DataObject.createFromId(new ObjectId(Template.class, objectId));

	}

	@Override
	protected void writeCardStateCollection(JSONWriter jw, TreeAttribute attr,
			boolean topNodes) throws JSONException {
		
		if (!(attr instanceof TemplateSearchAttribute)) {
			logger.error( "Invalid attribute type! It should instanceof  TemplateSearchAttribute.");
			return;
		}
		
		TemplateSearchAttribute attribute = (TemplateSearchAttribute)attr;
		
		for(Template template : attribute.getCardTemplates()) {
			jw.object();
			jw.key("id").value(template.getId().getId().toString());
			jw.key("name").value(template.getName());
			if (topNodes) 
				jw.key("type").value("topNodes");
			jw.endObject();
		}
		
		

	}

}

