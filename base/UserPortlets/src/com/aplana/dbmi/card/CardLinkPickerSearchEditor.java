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

import java.util.ArrayList;
import java.util.Iterator;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.actionhandler.ActionsManager;
import com.aplana.dbmi.actionhandler.descriptor.ActionHandlerDescriptor;
import com.aplana.dbmi.actionhandler.descriptor.SelectionType;
import com.aplana.dbmi.ajax.CardLinkPickerSearchParameters;
import com.aplana.dbmi.card.actionhandler.SearchPortletAttributeEditorActionsManager;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerDescriptor;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerVariantDescriptor;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerVariantDescriptor.SearchDependency;
import com.aplana.dbmi.card.hierarchy.descriptor.HierarchyDescriptor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.search.SearchFilterPortlet;
import com.aplana.dbmi.search.SearchFilterPortletSessionBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;

/**
 * Represents CardLinkPicker editor for search filter
 *  
 * @author skashanski
 *
 */
public class CardLinkPickerSearchEditor extends CommonCardLinkPickerEditor {

	public static final String PARAM_MULTI_VALUED = "multiValued";
	public static final String PARAM_SCROLL_HEIGHT = "scrollHeight";

	public static final String KEY_SCROLL_HEIGHT = PARAM_SCROLL_HEIGHT;

	private Boolean multiValued = null;
	private int scrollHeight = -1;

	@Override
	protected ActionsManager getActionsManager(ObjectId attrId,
			CardLinkPickerVariantDescriptor vd, PortletRequest request) {

		SearchFilterPortletSessionBean searchFilterBean = getSessionBean(request);

		return (ActionsManager)searchFilterBean.getSearchEditorData(attrId, ACTION_MANAGER_PREFIX + vd.getAlias()); 
	}

	@Override
	protected CardLinkPickerVariantDescriptor getActiveVariantDescriptor(
			CardLinkPickerDescriptor d, PortletRequest request) {

		SearchFilterPortletSessionBean searchFilterBean = getSessionBean(request); 

		return d.getDefaultVariantDescriptor();
	}

	@Override
	protected CardLinkPickerDescriptor getCardLinkPickerDescriptor(
			Attribute attr, PortletRequest request) {

		SearchFilterPortletSessionBean searchFilterBean = getSessionBean(request);

		return (CardLinkPickerDescriptor)searchFilterBean.getSearchEditorData(attr.getId(), KEY_DESCRIPTOR);
	}

	@Override
	protected CardLinkPickerVariantDescriptor getCardLinkVariantDescriptor(
			Attribute attr, PortletRequest request) {

		CardLinkPickerDescriptor descriptor = getCardLinkPickerDescriptor(attr, request);

		return descriptor.getDefaultVariantDescriptor();

	}

	@Override
	protected DataServiceBean getDataServiceBean(PortletRequest request) {

		SearchFilterPortletSessionBean searchFilterBean = getSessionBean(request);

		return searchFilterBean.getServiceBean();
	}

	@Override
	protected void initializeActions(PortletRequest request, Attribute attr, CardLinkPickerDescriptor d) {

		ObjectId attrId = attr.getId();

		SearchFilterPortletSessionBean searchFilterBean = getSessionBean(request);

		for (Iterator<?> i = d.getVariants().iterator(); i.hasNext();) {

			CardLinkPickerVariantDescriptor vd = (CardLinkPickerVariantDescriptor) i
					.next();

			if (vd.getHierarchyDescriptor() != null) {
				SearchPortletAttributeEditorActionsManager am = SearchPortletAttributeEditorActionsManager
						.getInstance(searchFilterBean, vd.getHierarchyDescriptor().getActionsDescriptor(), attr);
						//.getInstance(searchFilterBean, new ActionsDescriptor(), attr); -- ������ ��� ������. ����� ���������� ��������? :)

				searchFilterBean.setSearchEditorData(attrId, ACTION_MANAGER_PREFIX
						+ vd.getAlias(), am);
			}
		}
	}

	@Override
	protected void storeAttributeEditorsParameters(PortletRequest request,
			Attribute attr) throws DataException {

		SearchFilterPortletSessionBean searchFilterBean = getSessionBean(request);
		if(SearchFilterPortletSessionBean.PageState.WORKSTATION_EXTENDED_SEARCH_PAGE.equals(searchFilterBean.getPage())) {
			showTitle = false;
		}

		searchFilterBean.setSearchEditorData(attr.getId(), KEY_SHOW_TITLE, new Boolean(showTitle));
		searchFilterBean.setSearchEditorData(attr.getId(), KEY_SHOW_EMPTY, new Boolean(showEmpty));
		searchFilterBean.setSearchEditorData(attr.getId(), KEY_TYPE_CAPTION, typeCaption);
		searchFilterBean.setSearchEditorData(attr.getId(), PARAM_CONNECTION_TYPE_SHOW, enableConnectionTypeShow);
		searchFilterBean.setSearchEditorData(attr.getId(), KEY_SCROLL_HEIGHT, scrollHeight);
	}

	protected SearchFilterPortletSessionBean getSessionBean(PortletRequest request) {

		PortletSession session = request.getPortletSession();

		return (SearchFilterPortletSessionBean)session.getAttribute(SearchFilterPortlet.SESSION_BEAN);

	}

	@Override
	protected void storeKeyDescriptor(PortletRequest request, Attribute attr,
			CardLinkPickerDescriptor d) {

		//cleans hierarchy descriptor for search  
		//d.getDefaultVariantDescriptor().setHierarchyDescriptor(null);

		SearchFilterPortletSessionBean searchFilterBean = getSessionBean(request);

		searchFilterBean.setSearchEditorData(attr.getId(), KEY_DESCRIPTOR, d);

	}

	@Override
	protected JSONObject getVariantsJSON(CardLinkPickerDescriptor descriptor,PortletRequest request, Attribute attr)
			throws JSONException 
	{
		JSONObject result = super.getVariantsJSON(descriptor, request, attr);

		addDependencies(request, descriptor, result);

		return result;
	}

	protected void addDependencies(PortletRequest request,
			CardLinkPickerDescriptor descriptor, JSONObject variants)
			throws JSONException {

		final JSONArray dependencies = new JSONArray();

		final JSONObject queryObject = new JSONObject();

		for (CardLinkPickerVariantDescriptor vd : descriptor.getVariants()) {

			final String key = vd.getAlias();
			//gets variant to add dependencies 
			JSONObject jv = (JSONObject)variants.get(key);

			for (int j = 0; j < vd.getSearchDependencies().size(); ++j) {
				final SearchDependency sd = (SearchDependency) vd
						.getSearchDependencies().get(j);
				if (sd.isSpecial()) {
					queryObject.put(CardLinkPickerSearchParameters.PARAM_DEP_SPECIAL_PREFIX
											+ j, sd.getSpecialValue());
				} else dependencies.put(sd.getValueAttrId().getId());
			}

			jv.put("dependencies", dependencies);
			jv.put("query", queryObject);

			@SuppressWarnings("unchecked")
			ArrayList<SearchResult.Column> columns = (ArrayList<SearchResult.Column>)vd.getColumns();
			int i = -1;
			int j = 0;
			for (SearchResult.Column col : columns) {
				if(col.isLinked()) {
					i = j;
				}
				j++;
			}
			jv.put("fieldLinked", i);

		}

	}

	@Override
	public void setParameter(String name, String value) {
		if (PARAM_SCROLL_HEIGHT.equalsIgnoreCase(name)) {
			try {
				this.scrollHeight = Integer.valueOf(value.trim());
			} catch (NumberFormatException ex) {
				logger.error("Unable to set scrollHeight value due to " + ex.getMessage(), ex);
			}
		}
		else {
			super.setParameter(name, value);
		}
	}

}
