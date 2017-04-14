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

import java.util.StringTokenizer;

import javax.portlet.PortletException;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;

public abstract class DisablerBaseBuilder extends ExtraJavascriptBuilder{
	
	public final static String RELATED_ATTR_ID = "relatedAttrId";
	public final static String RELATED_ATTR_CLASS = "relatedAttrClass";
	public final static String TO_ENABLE_VALUES = "toEnableValues";
	public final static String TO_DISABLE_VALUES = "toDisableValues";
	
	@Override
	public void addJavascript(Attribute attr, StringBuilder stringBuilder) throws PortletException {
		if (!hasParam(RELATED_ATTR_ID)) {
			throw new PortletException("Can't build javascript. " + RELATED_ATTR_ID + " property required.");
		}
		if (!hasParam(RELATED_ATTR_CLASS)) {
			throw new PortletException("Can't build javascript. " + RELATED_ATTR_CLASS + " property required.");
		}
		if (hasParam(TO_DISABLE_VALUES) && hasParam(TO_ENABLE_VALUES)) {
			throw new PortletException("Can't build javascript. Both enable and disable expression present. Required one.");
		}
		ObjectId valueAttrId = null;
		try {
			valueAttrId = ObjectId.predefined(Class.forName("com.aplana.dbmi.model." + getParam(RELATED_ATTR_CLASS)), getParam(RELATED_ATTR_ID));
			if (valueAttrId == null) {
				valueAttrId = new ObjectId(Class.forName("com.aplana.dbmi.model." + getParam(RELATED_ATTR_CLASS)), getParam(RELATED_ATTR_ID));
			}
		} 
		catch (ClassNotFoundException e) {
			throw new PortletException("Can't build javascript. Can't get toDisableAttrClass", e);
		}
		if (hasParam(TO_DISABLE_VALUES)) {
			addDisabler(attr, (String)valueAttrId.getId(), getParam(TO_DISABLE_VALUES), false, stringBuilder);
		}
		else {
			addDisabler(attr, (String)valueAttrId.getId(), getParam(TO_ENABLE_VALUES), true, stringBuilder);
		}
	}
	
	protected void addCheckValuesExpression(String values, boolean valuesToEnable, StringBuilder stringBuilder) {
		StringTokenizer st = new StringTokenizer(values, ",");
		boolean isFirstExpression = true;
		if (valuesToEnable) {
			stringBuilder.append("!(");
		}
		while (st.hasMoreTokens()) {
			String value = st.nextToken();
			if (isFirstExpression) {
				isFirstExpression = false;
			}
			else {
				stringBuilder.append(" || ");
			}
			if (value.equalsIgnoreCase("null")) {
				stringBuilder.append("(value == null || value.toString() == 'null' || value.toString() == '')");
			}
			else if (value.length() > 0) {
				stringBuilder.append("(value != null && value.toString() == '" + value + "')");
			}
		}
		if (valuesToEnable) {
			stringBuilder.append(")");
		}
	}
	
	protected abstract void addDisabler(Attribute attr, String valueAttrId, String values, boolean valuesToEnable, StringBuilder stringBuilder);

}
