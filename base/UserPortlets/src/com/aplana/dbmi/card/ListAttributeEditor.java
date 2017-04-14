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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.portlet.PortletRequest;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataServiceBean;

public class ListAttributeEditor extends CommonListAttributeEditor
{

	protected static final String PARAM_NOT_VISIBLE_VALUES = "notVisibleValues";

	protected List<ObjectId> notVisibleValues = Collections.emptyList();

	@Override
	protected Collection getValueList(PortletRequest request, Attribute attr) {
		CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
		Collection valuesList = (Collection)sessionBean.getAttributeEditorData(attr.getId(), VALUES_LIST_KEY);
		return valuesList;
	}

	@Override
	protected void storeValueList(PortletRequest request, Attribute attr, Collection valueList) {
		CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
		sessionBean.setAttributeEditorData(attr.getId(), VALUES_LIST_KEY, valueList);
	}

	/**
	 * Returns DataServiceBean from Portlet Session
	 * @param request passed PortletRequest
	 */
	@Override
	protected DataServiceBean getDataServiceBean(PortletRequest request) {
		CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
		return sessionBean.getServiceBean();
	}

	@Override
	protected ObjectId getSelectedValueId(PortletRequest request, Attribute attr) {
		ReferenceValue value = ((ListAttribute)attr).getValue();
		return value == null ? null : value.getId();
	}

	@Override
	protected boolean isReferenceValueVisisble(ReferenceValue value) {
		return super.isReferenceValueVisisble(value) && !notVisibleValues.contains(value.getId());
	}

	@Override
	protected String getMessage(PortletRequest request, String key) {
		return CardPortlet.getSessionBean(request).getResourceBundle().getString(key);
	}

	/**
	 * Sets parameters
	 */
	@Override
	public void setParameter(String name, String value) {
		if (name.equals(PARAM_NOT_VISIBLE_VALUES)) {
			notVisibleValues = ObjectIdUtils.commaDelimitedStringToIds(value, ReferenceValue.class);
		} else {
			super.setParameter(name, value);
		}
	}
}
