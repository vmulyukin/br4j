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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.portlet.PortletRequest;

import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerDescriptor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.filter.StateIdListFilter;
import com.aplana.dbmi.model.filter.TemplateIdListFilter;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * Editor for Card's Link documents in dialog Request-to-Response, which makes filtering by Template, Status
 *  
 * @author Panichev
 *
 */

public class ResponseTransmittedAttributeEditor extends CardLinkPickerAttributeEditor {
	
	public static final String KEY_DOC_TYPES = "docTypes";
	public static final String KEY_DOC_STATUS = "docStatus";
	public static final String KEY_DROPDAWN_DESCRIPTOR = "dropDawnDescriptor";
	public static final String KEY_LIST_STATES = "states";
	
	public ResponseTransmittedAttributeEditor() {
		super();
		setParameter(PARAM_INIT_JSP, "/WEB-INF/jsp/html/attr/ResponseTransmittedInclude.jsp");
	}
	
	@SuppressWarnings("unchecked")
	public List<Template> getFullTemplates(DataServiceBean dataServiceBean,
			Collection<Template> filterTemplates) throws DataException {
		try {
			final TemplateIdListFilter filter = new TemplateIdListFilter(filterTemplates);
			return  (List<Template>) dataServiceBean.filter(Template.class, filter);
		} catch (ServiceException e) {
			throw new DataException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<CardState> getFullStates(DataServiceBean dataServiceBean,
			Collection<String> filterStates) throws DataException {
		try {
			Collection<String> filterStatesId = new HashSet<String>();
			for (String flilterState : filterStates) {
				ObjectId flilterStateId = ObjectId.predefined(CardState.class, flilterState);
				filterStatesId.add(flilterStateId.getId().toString());
			}
			final StateIdListFilter filter = new StateIdListFilter(filterStatesId);
			List<CardState> listCS = (List<CardState>)dataServiceBean.filter(CardState.class, filter);
			return listCS;
		} catch (Exception e) {
			throw new DataException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initEditor(PortletRequest request, Attribute attr)
			throws DataException {

		super.initEditor(request, attr);

		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/ResponseTransmitted.jsp");

		CardPortletSessionBean sessionBean = (CardPortletSessionBean)request.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		DataServiceBean dataServiceBean = sessionBean.getServiceBean();	

		CardLinkPickerDescriptor descriptor = (CardLinkPickerDescriptor) cardInfo
				.getAttributeEditorData(attr.getId(), KEY_DESCRIPTOR);
		

		//gets possible filter templates 
		final Collection<Template> filterTemlates = descriptor.getDefaultVariantDescriptor().getSearch().getTemplates();

		if (!filterTemlates.isEmpty()) {
				final List<Template> documentTypes = getFullTemplates( dataServiceBean, filterTemlates);
				Collections.sort(documentTypes,
						new Comparator<Object>() {
						@SuppressWarnings("null")
						public int compare(Object o1, Object o2) {
							final Template tpl1 = (Template)o1, tpl2 = (Template)o2;
							final boolean isNull2 = (tpl2 == null) || (tpl2.getName() == null);
							if (tpl1 == null || tpl1.getName() == null)
								return (isNull2) ? 0 : -1;
							if (isNull2)
								return 1;
							return tpl1.getName().compareTo(tpl2.getName());
						}
					}
				);
				cardInfo.setAttributeEditorData(attr.getId(), KEY_DOC_TYPES, documentTypes);
		}
		
		try {
			final HashMap<String, Collection<String>> dropDawnItems = descriptor.getDropDownItems();
			final Collection<String> filterStates = dropDawnItems.get(KEY_LIST_STATES);
			if (filterStates != null && !filterStates.isEmpty()) {
				final List<CardState> documentStates = getFullStates( dataServiceBean, filterStates);
				cardInfo.setAttributeEditorData(attr.getId(), KEY_DOC_STATUS, documentStates);
			}
		} catch (Exception e) {
			throw new DataException(e);
		}
		
		
	}	
}
