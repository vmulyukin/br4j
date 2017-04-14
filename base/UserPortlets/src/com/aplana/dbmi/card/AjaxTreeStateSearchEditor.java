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

import com.aplana.dbmi.model.*;
import org.json.JSONException;
import org.json.JSONWriter;

import java.util.ArrayList;

/**
 * Represents editor for StateSearchAttribute in extended search 
 * 
 * @author skashanski
 *
 */

public class AjaxTreeStateSearchEditor extends AjaxBaseTreeSearchEditor  {

	protected static final String PARAM_UNCHECKED_STATE = "uncheckedStates";
	protected static final String PARAM_CHECK_ALL = "checkAll";
	
	private boolean isCheckAll = false;
	private ArrayList<Long> uncheckedStates = new ArrayList<Long>();

	public AjaxTreeStateSearchEditor() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/AjaxTreeSearch.jsp");
		setParameter(PARAM_INIT_JSP, "/WEB-INF/jsp/html/attr/AjaxTreeInclude.jsp");
	}

	@Override
	protected DataObject createDataObject(long objectId) {
		return DataObject.createFromId(new ObjectId(CardState.class, objectId));
	}

	@Override
	protected void writeCardStateCollection(JSONWriter jw, TreeAttribute attr,
			boolean topNodes) throws JSONException {
		
		if (!(attr instanceof StateSearchAttribute)) {
			logger.error( "Invalid attribute type! It should instanceof  StateSearchAttribute.");
			return;
		}
		
		StateSearchAttribute attribute = (StateSearchAttribute)attr;

		for (CardState cardState : attribute.getCardStates()) {
			jw.object();
			jw.key("id").value(cardState.getId().getId().toString());
			jw.key("name").value(cardState.getName());
			if (topNodes)
				jw.key("type").value("topNodes");
			if (isCheckAll && !uncheckedStates.contains(cardState.getId().getId())) {
				jw.key("checked").value("true");
			}
			jw.endObject();
		}
	}
	
	public void setParameter(String name, String value) {
        if (PARAM_CHECK_ALL.equals(name)) {
        	isCheckAll = Boolean.parseBoolean(value);
        } else if (PARAM_UNCHECKED_STATE.equals(name)) {
        	String[] unchecked = value.split(",");
        	for(String s: unchecked){
        		uncheckedStates.add(Long.parseLong(s));
			}
        } else {
            super.setParameter(name, value);
        }
    }
	/*
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
					CardState cardState = (CardState)iterVal.next();
					jw.object();
					jw.key("id").value(cardState.getId().getId().toString());
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
						CardState.class, Long.parseLong(idVals[i]))));
			}
		}
		
		treeAttr.setValues(values);
		
		return true;

	}
	
	
	
	private static void writeCardStateCollection(JSONWriter jw, Collection cardStates, boolean topNodes) throws JSONException {
		
		Iterator iterCardState = cardStates.iterator();
		
		while(iterCardState.hasNext()) {
			
			CardState cardState = (CardState)iterCardState.next();
			
			jw.object();
			jw.key("id").value(cardState.getId().getId().toString());
			jw.key("name").value(cardState.getName());
			if (topNodes) 
				jw.key("type").value("topNodes");
			jw.endObject();
		}
	}	
	
	

	public static String getJSONReferences(StateSearchAttribute attr) throws ServletException {
		
		try {
			
			StringWriter w = new StringWriter();
			JSONWriter jw = new JSONWriter(w);

			jw.object();
			jw.key("identifier").value("id");
			jw.key("label").value("name");
			jw.key("items");
			jw.array();
			
			writeCardStateCollection(jw, attr.getCardStates(), true);
			
			jw.endArray();
			jw.endObject();
			
			return w.toString();
			
		} catch (JSONException e) {
			throw new ServletException(e);
		}
		
		
	}
*/
	

}
