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

import javax.portlet.ActionRequest;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.SearchTextCheckedAttribute;
import com.aplana.dbmi.service.DataException;

public class SearchTextAttributeCheckedEditor extends JspAttributeEditor {
	
	public SearchTextAttributeCheckedEditor() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/SearchTextChecked.jsp");
	}
	
	@Override
	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException {
		String attrIdPrefix = getAttrIdPrefix(attr);
		
		String value = request.getParameter(attrIdPrefix+"_inputString");
		String check = request.getParameter(attrIdPrefix+"_checkedFlag");
		if(check!=null){
			((SearchTextCheckedAttribute) attr).setCheckedFlag(check.equals("on") || check.equals("true"));
		}else{
			((SearchTextCheckedAttribute) attr).setCheckedFlag(false);
		}
		if (value == null)
			return false;
		((SearchTextCheckedAttribute) attr).setValue(value.trim());
		return true;
		
	}
	
	public String getAttrIdPrefix(Attribute attr){
		return JspAttributeEditor.getAttrHtmlId(attr);
	}
	
	public boolean isValueCollapsable() {
		return true;
	}

}
