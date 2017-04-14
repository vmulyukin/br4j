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
package com.aplana.dbmi.card.extra;

import javax.portlet.PortletException;

import com.aplana.dbmi.card.JspAttributeEditor;
import com.aplana.dbmi.model.Attribute;

/**
 * Adds javascript code checking whether the value entered in the field contains latin symbols
 * If it does, it shows a tooltip with the text provided in 'message' parameter
 * 
 */
public class LatinSymbolsDetector extends ExtraJavascriptBuilder {

	public static final String DETECT_MSG = "message";
	
	@Override
	public void addJavascript(Attribute attr, StringBuilder stringBuilder)
			throws PortletException {
		
		String attrId = JspAttributeEditor.getAttrHtmlId(attr);
		
		String message = getParam(DETECT_MSG);
		if(message == null)
			message = "contains latin symbols";
		
		stringBuilder.append("dojo.require('dijit.Tooltip');dojo.addOnLoad(function() {");
		stringBuilder.append("var ").append(attrId).append("_inputElem = document.getElementById('").append(attrId).append("');if(").append(attrId).append("_inputElem) {");
		stringBuilder.append(attrId).append("_inputElem.addEventListener('blur', function(event) {dijit.hideTooltip(").append(attrId).append("_inputElem);});");
		stringBuilder.append(attrId).append("_inputElem.addEventListener('keyup', function(event) {");
		stringBuilder.append("var value = ").append(attrId).append("_inputElem.value;");
		stringBuilder.append("if(").append(attrId).append("_containsLatinSymbols(value)) {");
		stringBuilder.append("dijit.showTooltip('").append(message).append("', ").append(attrId).append("_inputElem, ['below']);");
		stringBuilder.append("} else dijit.hideTooltip(").append(attrId).append("_inputElem);}, false);}");
		stringBuilder.append(attrId).append("_inputElem.addEventListener('focus', function(event) {");
		stringBuilder.append("var value = ").append(attrId).append("_inputElem.value;");
		stringBuilder.append("if(").append(attrId).append("_containsLatinSymbols(value)) {");
		stringBuilder.append("dijit.showTooltip('").append(message).append("', ").append(attrId).append("_inputElem, ['below']);");
		stringBuilder.append("} else dijit.hideTooltip(").append(attrId).append("_inputElem);}, false);");
		stringBuilder.append("function ").append(attrId).append("_containsLatinSymbols(str) {");
		stringBuilder.append("if(!str || str==null || str==''){return false;}");
		stringBuilder.append("var latin = /[\u0041-\u005A\u0061-\u007A]/;");
		stringBuilder.append("if (latin.test(str)){return true;}return false;}");
		stringBuilder.append("});");
	}

}
