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

import com.aplana.dbmi.card.JspAttributeEditor;
import com.aplana.dbmi.model.Attribute;

public class DateTimeDisabler extends DisablerBaseBuilder{
	
	@Override
	protected void addDisabler(Attribute attr, String valueAttrId, String values, boolean valuesToEnable, StringBuilder stringBuilder) {
		String attrHtmlId = JspAttributeEditor.getAttrHtmlId(attr);
		String functionName = attrHtmlId + "_from_" + valueAttrId.replace(".", "_") + "_dateTimeDisabler";
		String getDateControlExpression = "dijit.byId('" + attrHtmlId + "_date')";
		//String getTimeControlExpression = "dijit.byId('" + attrHtmlId + "_time')";

		stringBuilder.append("function " + functionName + "(attrCode, attrHtmlId, isInline, value, param) { ");
		if (values != null) {
			stringBuilder.append(getDateControlExpression + ".setDisabled(");
			addCheckValuesExpression(values, valuesToEnable, stringBuilder);
			stringBuilder.append(");");
			//stringBuilder.append(getTimeControlExpression + ".setDisabled(");
			//addCheckValuesExpression(values, valuesToEnable, stringBuilder);
			//stringBuilder.append(");");
		}
		else {
			stringBuilder.append(getDateControlExpression + ".setDisabled(value == null || typeof(value) == 'undefined' || value == '');");
			//stringBuilder.append(getTimeControlExpression + ".setDisabled(value == null || typeof(value) == 'undefined' || value == '');");
		}
		stringBuilder.append("}");

		stringBuilder.append("dojo.addOnLoad(function() { ");
		stringBuilder.append("editorEventManager.subscribe('" + attr.getId().getId() + "', '" + valueAttrId + "', '" + functionName + "', null);");
		stringBuilder.append("} );");
	}

}
