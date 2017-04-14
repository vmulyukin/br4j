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
import com.aplana.dbmi.model.MultipleStateSearchItemAttribute;
import com.aplana.dbmi.model.StateSearchItem;
import com.aplana.dbmi.service.DataException;
import org.jboss.logging.Logger;
import org.json.JSONException;

import javax.portlet.ActionRequest;
import javax.portlet.PortletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ResourceBundle;

public class MultipleStateSearchItemAttributeEditor extends JspAttributeEditor {

	public static final int SELECTED_VAL = 1;
	public static final int UNSELECTED_VAL = 0;
	
	private static final String BUNDLE_NAME = "search";
	
	private static final Logger logger = Logger.getLogger(MultipleStateSearchItemAttributeEditor.class);

	public MultipleStateSearchItemAttributeEditor() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/MultipleStateSearchItemEdit.jsp");
	}

	@Override
	public boolean gatherData(ActionRequest request, Attribute attr) throws DataException {
		if (!(attr instanceof MultipleStateSearchItemAttribute))
			throw new IllegalArgumentException("Invalid attribute's type. It should be instance of MultipleSelectedSearchAttribute!");
		
		MultipleStateSearchItemAttribute attribute = (MultipleStateSearchItemAttribute)attr;

		//loop through all values and find selected values 
		for(StateSearchItem stateSearchItem : attribute.getValues()) {
			
			String itemParamValue = request.getParameter(stateSearchItem.getId()+"_value");
			if (itemParamValue == null){
				return false;
			}
			int itemParamIntValue = Integer.parseInt(itemParamValue);
			
			if (itemParamIntValue == SELECTED_VAL)
				stateSearchItem.setState(StateSearchItem.SearchItemState.CHECKED);
			else
				stateSearchItem.setState(StateSearchItem.SearchItemState.UNCHECKED);
		}
		return true;
	}
	
	public static String getJsonData(HttpServletRequest request, MultipleStateSearchItemAttribute attr) throws IOException, PortletException {
		ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, request.getLocale());
		try {
			return attr.toJSON(bundle).toString();
		} catch (IllegalArgumentException e) {
			logger.error(e);
		} catch (JSONException e) {
			logger.error(e);
		}
		return null;
	}
}
