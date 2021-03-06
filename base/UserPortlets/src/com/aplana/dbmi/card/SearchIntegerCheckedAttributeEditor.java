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
import com.aplana.dbmi.model.SearchIntegerCheckedAttribute;
import com.aplana.dbmi.model.SearchStringCheckedAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.web.tag.util.StringUtils;

import javax.portlet.ActionRequest;

public class SearchIntegerCheckedAttributeEditor extends IntegerAttributeEditor {
	
	
	public SearchIntegerCheckedAttributeEditor() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/SearchIntegerChecked.jsp");
	}
	
	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException {
		String attrIdPrefix = getAttrIdPrefix(attr);
		
		String value = request.getParameter(attrIdPrefix+"_inputInteger");
		String check = request.getParameter(attrIdPrefix+"_checkedFlag");
		if(check!=null){
			((SearchIntegerCheckedAttribute) attr).setCheckedFlag(check.equals("on") || check.equals("true"));
		}else{
			((SearchIntegerCheckedAttribute) attr).setCheckedFlag(false);
		}
		try{
			if(!StringUtils.hasLength(value)) {
				((SearchIntegerCheckedAttribute) attr).clear();				
			}else{
				((SearchIntegerCheckedAttribute) attr).setValue(Integer.parseInt(value));
			}
		} catch (NumberFormatException e) {
				throw new DataException("edit.page.error.number", new Object[] { attr.getName() });
		}
		return true;		
	}
	

	
	
	public String getAttrIdPrefix(Attribute attr){
		return JspAttributeEditor.getAttrHtmlId(attr);
	}

}
