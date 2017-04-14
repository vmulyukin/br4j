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
package com.aplana.dbmi.ajax;

import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.card.CardLinkPickerAttributeEditor;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerDescriptor;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.search.SearchFilterPortlet;
import com.aplana.dbmi.search.SearchFilterPortletSessionBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * Represents parameters for CardLinkPickerSearchEditor
 * 
 * @author skashanski
 *
 */
public class CardLinkPickerSearchFilterParameters extends CardLinkPickerSearchParameters {

	public static final String PARAM_SHOW_INACTIVE_PERSONS = "showInactive";
	public static final String CALLER = "cardLinkPickerSearch";
	private static final ObjectId CARDSTATE_INACTIVE_USER = ObjectId.predefined(CardState.class, "user.inactive");
	private static final ObjectId CARDSTATE_ACTIVE_USER = ObjectId.predefined(CardState.class, "user.active");
	
	/*
	 * 26.06.2012, O.E. - Using generic one for compatibility
	@Override
	protected ObjectId getAttributeId(String attrCode,
			HttpServletRequest request) {
		
		
		String attrCodeType = request.getParameter(PARAM_ATTR_TYPE_CODE);
		
		return AttrUtils.getAttributeId(attrCodeType);

	}
	 */
	
	@Override
	protected CardLinkPickerDescriptor getCardLinkPickerDescriptor(
			ObjectId attrId, HttpServletRequest request) {
		
		SearchFilterPortletSessionBean sessionBean = getSessionBean(request);
		
		return (CardLinkPickerDescriptor)sessionBean.getSearchEditorData(attrId, CardLinkPickerAttributeEditor.KEY_DESCRIPTOR);
		
	}

	@Override
	protected Card getUserCard(HttpServletRequest request)
			throws ServiceException, DataException {
		
		SearchFilterPortletSessionBean sessionBean = getSessionBean(request);
		
		return  (Card) sessionBean.getServiceBean().getById(sessionBean.getServiceBean().getPerson().getCardId());
	}

	@Override
	protected void initDataServiceBean(HttpServletRequest request) {
		
		SearchFilterPortletSessionBean sessionBean = getSessionBean(request);
		
		dataServiceBean = sessionBean.getServiceBean();
	}
	
	
	private SearchFilterPortletSessionBean getSessionBean(HttpServletRequest request) {
		
		
		String namespace = request.getParameter(PARAM_NAMESPACE);

		return SearchFilterPortlet.getSessionBean(request, namespace);
		
	}

	@Override
	public void initialize(HttpServletRequest request,
			DataServiceBean serviceBean) throws ServletException {
		super.initialize(request, serviceBean);
		
		boolean showInactivePersons = parseShowInactivePersonsParam(request);
		//add inactive persons if appropriate parameter was passed
		addInactivePesonStatus(showInactivePersons);
	}

	
	private boolean existActiveUserStatus() {
		
		Collection<Long> statuses = (Collection<Long>)search.getStates();
		Long activeUserStatus = (Long)CARDSTATE_ACTIVE_USER.getId();
		for(Long status : statuses) {
			if (activeUserStatus.equals(status))
				return true;
		}
		return false;
	}
	
	private void addInactivePesonStatus(boolean showInactivePersons) {
		if (!showInactivePersons)
			return;
		
		if (!existActiveUserStatus())
			return;//skip to add inactive person if there is no restriction to use just active persons 
			
		search.getStates().add(CARDSTATE_INACTIVE_USER.getId());

	}

	private boolean parseShowInactivePersonsParam(HttpServletRequest request) {
		
		String showInactiveParam = request.getParameter(PARAM_SHOW_INACTIVE_PERSONS);
		
		boolean showInactivePersons = false;
		if (showInactiveParam != null || (!"".equals(showInactiveParam)))
			showInactivePersons = Boolean.parseBoolean(showInactiveParam);
		
		return showInactivePersons;
	}
	
	
	

}
