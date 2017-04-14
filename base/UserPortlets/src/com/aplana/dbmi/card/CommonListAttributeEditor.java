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
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.portlet.ActionRequest;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import java.util.*;

/**
 * Base class for ListAttributeEditor
 * @author skashanski
 *
 */
public abstract class CommonListAttributeEditor extends JspAttributeEditor {

	protected final static String VALUES_LIST_KEY = "valuesList";
	public final static String EMPTY_VALUE_ID = "-1";
	public final static String INVALID_VALUE_ID = "-2";

	public final static String STORE_KEY_IDENTIFIER = "id";
	public final static String STORE_KEY_LABEL = "name";
	public final static String STORE_KEY_VISIBLE = "visible";

	public CommonListAttributeEditor() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/List.jsp");
		setParameter(PARAM_INIT_JSP, "/WEB-INF/jsp/html/attr/ListInclude.jsp");
	}

	@Override
	public boolean gatherData(ActionRequest request, Attribute attr) throws DataException {
		String value = request.getParameter(getAttrHtmlId(attr));
		if (value == null)
			return false;

		value = value.trim();
		if (INVALID_VALUE_ID.equals(value)) {
			throw new DataException("edit.page.error.id", new Object[] { attr.getName() });
		} else if (value.equals("") || value.equals(EMPTY_VALUE_ID)) {
			((ListAttribute) attr).setValue(null);
		} else {
			try {
				long id = Long.parseLong(value);
				ReferenceValue refVal = new ReferenceValue();
				refVal.setId(id);
				DataServiceBean dataServiceBean = getDataServiceBean(request);
				ObjectId idd = ((ListAttribute) attr).getReference();
				Collection<ReferenceValue> attrCol = dataServiceBean.listChildren(idd, ReferenceValue.class);
				for (ReferenceValue obj : attrCol) {
					if (obj.getId().getId().equals(refVal.getId().getId())) {
						refVal = obj;
					}
				}
				((ListAttribute) attr).setValue(refVal);
			} catch (NumberFormatException e) {
				throw new DataException("edit.page.error.id", new Object[] { attr.getName() });
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}


	@Override
	protected Map<String, Object> getReferenceData(Attribute attr, PortletRequest request) throws PortletException {
		Map<String, Object> referenceData =  super.getReferenceData(attr, request);
		Collection<ReferenceValue> valuesList = getValueList(request, attr);
		String selectedValue;

		ObjectId selectedValueId = getSelectedValueId(request, attr);
		if (selectedValueId == null) {
			selectedValue = EMPTY_VALUE_ID;
		} else if (ObjectIdUtils.isIntersectionDataObjects(valuesList, Collections.singleton(selectedValueId))) {
			selectedValue = selectedValueId.getId().toString();
		} else {
			selectedValue = INVALID_VALUE_ID;
		}

		String store = "{}";
		try {
			List<ReferenceValue> extendedValuesList = new ArrayList<ReferenceValue>(valuesList.size() + 2);
			extendedValuesList.add(createReferenceValue(Long.valueOf(EMPTY_VALUE_ID), getMessage(request, "edit.list.null.value")));
			extendedValuesList.add(createReferenceValue(Long.valueOf(INVALID_VALUE_ID), getMessage(request, "edit.list.invalid.value")));
			extendedValuesList.addAll(valuesList);
			store = getJSONStore(extendedValuesList).toString();
		} catch (JSONException ex) {
			logger.error("Error during values list creation", ex);
		}

		referenceData.put(VALUES_LIST_KEY, store);
		referenceData.put("selectedValueId", selectedValue);
		referenceData.put("query", getDataQuery());
		return referenceData;
	}

	protected abstract String getMessage(PortletRequest request, String key);

	private static ReferenceValue createReferenceValue(Long id, String value) {
		ReferenceValue newVal = new ReferenceValue();
		newVal.setId(id);
		newVal.setValueEn(value);
		newVal.setValueRu(value);
		return newVal;
	}

	protected JSONObject getJSONStore(Collection<ReferenceValue> valuesList) throws JSONException {
		JSONObject store = new JSONObject();
		store.put("identifier", STORE_KEY_IDENTIFIER);
		store.put("label", STORE_KEY_LABEL);
		JSONArray array = new JSONArray();
		for (Object value : valuesList) {
			ReferenceValue refValue = (ReferenceValue) value;
			array.put(createJSONStoreValue(refValue));
		}
		store.put("items", array);
		return store;
	}

	private JSONObject createJSONStoreValue(ReferenceValue value) throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put(STORE_KEY_IDENTIFIER, value.getId().getId().toString());
		obj.put(STORE_KEY_LABEL, value.getValue());
		obj.put(STORE_KEY_VISIBLE, isReferenceValueVisisble(value));
		return obj;
	}

	protected boolean isReferenceValueVisisble(ReferenceValue value) {
		return value == null || value.getId() == null || !INVALID_VALUE_ID.equals(value.getId().getId().toString());
	}

	protected String getDataQuery() {
		return STORE_KEY_VISIBLE + ": true";
	}

	/**
	 * Returns value's list for given attribute stored in portlet session
	 * @param request the portlet request
	 * @param attr attribute to get value's list
	 */
	protected abstract Collection<ReferenceValue> getValueList(PortletRequest request, Attribute attr);

	protected abstract ObjectId getSelectedValueId(PortletRequest request, Attribute attr);

	@Override
	public void initEditor(PortletRequest request, Attribute attr) throws DataException {
		Collection<ReferenceValue> valueList = loadReference(attr, request);
		storeValueList(request, attr, valueList);
	}

	/**
	 * Stores value list in portlet session for given attribute
	 * @param request the Portlet request
	 * @param attr the attribute to store value list
	 * @param valueList list of values
	 */
	protected abstract void storeValueList(PortletRequest request, Attribute attr, Collection valueList);


	/**
	 * Returns DataServiceBean from Portlet Session
	 * @param request passed PortletRequest
	 */
	protected abstract DataServiceBean getDataServiceBean(PortletRequest request);


	protected Collection<ReferenceValue> loadReference(Attribute attribute, PortletRequest request) throws DataException {
		ObjectId id = ((ListAttribute) attribute).getReference();
		try {
			DataServiceBean dataServiceBean = getDataServiceBean(request);
			return dataServiceBean.listChildren(id, ReferenceValue.class);
		} catch (ServiceException e) {
			throw new DataException(e);
		}
	}
}
