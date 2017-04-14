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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.cms.ContentUtils;
import com.aplana.dbmi.action.SearchByTemplateStateNameAction;
import com.aplana.dbmi.actionhandler.ActionsManager;
import com.aplana.dbmi.card.CardLinkPickerAttributeEditor;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.CommonCardLinkPickerEditor;
import com.aplana.dbmi.card.ResponseTransmittedAttributeEditor;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerDescriptor;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerVariantDescriptor;
import com.aplana.dbmi.card.hierarchy.descriptor.HierarchyDescriptor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.web.tag.util.StringUtils;

public class ResponseTransmittedParameters implements CardHierarchyServletParameters {
	
	private final Log logger = LogFactory.getLog(getClass());
	
	public static final String CALLER = "cardLinkResponseDialogHierarchy";
	public static final String PARAM_FILTER_TEMPLATE = "filterTemplate";
	public static final String PARAM_FILTER_STATE = "filterState";
	public static final String PARAM_FILTER_QUERY = "filterQuery";
	public static final String PARAM_ACTIVE_VARIANT = "activeVariant";
	public static final ObjectId REQUEST_OBJECT_ID = ObjectId.predefined(
		    CardLinkAttribute.class, "jbr.medo_og.reqObject");
	
	//limit size data to return due to optimization
	//it doesn't make sense to display more then 300 records in dialog
	public static int DATA_SIZE = 500;
	
	private SearchByTemplateStateNameAction action = new SearchByTemplateStateNameAction(); 
	
	private CardPortletSessionBean sessionBean;
	private String requestType = null;
	private CardPortletCardInfo cardInfo;
	private ObjectId attrId;
	private CardLinkPickerVariantDescriptor variantDescriptor;
	private DataServiceBean serviceBean;
	private HierarchyConnection hconn;
	private String hierarchyKey;
	private HashMap<Long, String> mapItemsTemplates;
	private HashMap<String, String> mapItemsAttributes;
	private HashMap<ObjectId, ObjectId> additionalFilter;
	private Card requestObject;

	public ActionsManager getActionsManager() {
		return CardLinkPickerAttributeEditor.getActionsManager(attrId, variantDescriptor, cardInfo);
	}

	public HierarchyConnection getHierarchyConnection() {
		// Returns stored connection in case we adding Items in Tree as we need to get NEXT following items
		//otherwise returns null as we need always filter by Template and State
		if (CardHierarchyServlet.REQUEST_ADD_ITEMS.equalsIgnoreCase(requestType) 
				|| CardHierarchyServlet.REQUEST_ALL_ITEMS.equalsIgnoreCase(requestType))
			return hconn;
		return null;
	}

	public HierarchyDescriptor getHierarchyDescriptor() {
		return variantDescriptor.getHierarchyDescriptor();
	}

	public Collection getStoredCards() {
		try {
			return (Collection<ObjectId>)serviceBean.doAction(action);
			
		} catch (Exception e) {
			logger.error("Failed to load stored cards", e);
			return new ArrayList(0);
		}
	}

	public void init(HttpServletRequest request) throws ServletException {		
		initializeVariables(request);
		requestObject = getRequestObject();
		if (requestObject != null) {
			parseFilterTemplate(request);
			parseFilterState(request);
		}
	}

	public void storeHierarchyConnection(HierarchyConnection hconn) {
		if (hierarchyKey != null) {
			cardInfo.setAttributeEditorData(attrId, hierarchyKey, hconn);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void parseFilterTemplate(HttpServletRequest request) throws ServletException {
		String filterTemplateVal = request.getParameter(PARAM_FILTER_TEMPLATE);
		additionalFilter = new HashMap<ObjectId, ObjectId>();
		List<Template> filterTemplates = new ArrayList<Template>();
		if (StringUtils.hasText(filterTemplateVal)) {
			setTemplate(filterTemplateVal, filterTemplates);
		} else {
			List<Template> fullTemplates = (List<Template>)cardInfo.getAttributeEditorData(attrId, ResponseTransmittedAttributeEditor.KEY_DOC_TYPES);
			if (fullTemplates != null && !fullTemplates.isEmpty()) {
				for (Template template : fullTemplates) {
					setTemplate(template.getId().getId().toString(), filterTemplates);
				}
			}
		}
		action.setTemplates(filterTemplates);
		action.setAdditionalFilter(additionalFilter);
	}
	
	private void setTemplate(String filterTemplateVal, List<Template> filterTemplates) throws ServletException {
		Template filterTemplate = getFilterTemplate(filterTemplateVal);
		filterTemplates.add(filterTemplate);
	}
	
	private Template getFilterTemplate(String filterTemplate) throws ServletException {
		try {
			long filterTemplateIdInt = Long.parseLong(filterTemplate);
			setLinkAttribute(filterTemplateIdInt);
			ObjectId filterTemplateId = new ObjectId(Template.class, filterTemplateIdInt);
			return (Template)Template.createFromId(filterTemplateId);
		} catch (NumberFormatException e) {
			throw new ServletException("Invalid filter template value", e );
		}
	}
	
	@SuppressWarnings("unchecked")
	private void parseFilterState(HttpServletRequest request) throws ServletException {
		String filterStateVal = request.getParameter(PARAM_FILTER_STATE);
		List<CardState> filterStates = new ArrayList<CardState>();
		if (StringUtils.hasText(filterStateVal)) {
			setState(filterStateVal, filterStates);
		} else {
			List<CardState> fullStates = (List<CardState>)cardInfo.getAttributeEditorData(attrId, ResponseTransmittedAttributeEditor.KEY_DOC_STATUS);
			if (fullStates != null && !fullStates.isEmpty())
				for (CardState state : fullStates) {
					setState(state.getId().getId().toString(), filterStates);
				}
		}
		action.setStates(filterStates);
	}
	
	private void setState(String filterStateVal, List<CardState> filterStates) throws ServletException {
		CardState filterState = getFilterState(filterStateVal);
		filterStates.add(filterState);
	}
	
	private CardState getFilterState(String filterState) throws ServletException {
		try {
			long filterStateIdInt = Long.parseLong(filterState);
			ObjectId filterStateId = new ObjectId(CardState.class, filterStateIdInt);
			return (CardState)CardState.createFromId(filterStateId);
		} catch (NumberFormatException e) {
			throw new ServletException("Invalid filter state value", e );
		}
	}
	
	public SearchByTemplateStateNameAction getAction() {
		return action;
	}
	
	private void initSearchAction(String filterQuery) {	
		action.setPage(1);
		action.setPageSize(DATA_SIZE);
		action.setName(filterQuery);
		// ����������� ������ �� ������
		long[] permissionTypesArray = ContentUtils.getPermissionTypes(variantDescriptor.getRequiredPermissions());//(Filter.CU_READ_PERMISSION);	 
		action.setPermissionTypes(permissionTypesArray);
	}
	
	private void initializeVariables(HttpServletRequest request)
	throws ServletException {
		String namespace = request.getParameter(PARAM_NAMESPACE);
		sessionBean = CardPortlet.getSessionBean(request, namespace);

		serviceBean = sessionBean.getServiceBean();
		cardInfo = sessionBean.getActiveCardInfo();
		Card card = cardInfo.getCard();
		
		String attrCode = request.getParameter(PARAM_ATTR_CODE);
		Attribute attr = AttrUtils.getAttributeByCode(attrCode, card);
		if (attr == null) {
			throw new ServletException("Couldn't find attribute with code '" + attrCode + "' in card");
		}
		attrId = attr.getId();		
		hierarchyKey = request.getParameter(PARAM_HIERARCHY_KEY);
		requestType = request.getParameter(CardHierarchyServlet.PARAM_REQUEST_TYPE); 
		hconn = (HierarchyConnection)cardInfo.getAttributeEditorData(attrId, hierarchyKey);
		CardLinkPickerDescriptor d = (CardLinkPickerDescriptor)cardInfo.getAttributeEditorData(attrId, CardLinkPickerAttributeEditor.KEY_DESCRIPTOR);
		String paramAlias = request.getParameter(PARAM_ACTIVE_VARIANT);
		variantDescriptor = d.getVariantDescriptor(paramAlias);
		if (variantDescriptor == null) {
			throw new ServletException("Couldn't find variant descriptor with alias: '" + paramAlias + "'");
		}
		CardLinkPickerDescriptor descriptor = (CardLinkPickerDescriptor) cardInfo
		.getAttributeEditorData(attrId, CommonCardLinkPickerEditor.KEY_DESCRIPTOR);
		HashMap<String, String> map = descriptor.getMapItemsTemplates();
		mapItemsTemplates = new HashMap<Long, String>();
		if (map != null && !map.isEmpty())
			for (String key : map.keySet()) {
				ObjectId templateKeyId = ObjectId.predefined(Template.class, key);
				if (templateKeyId != null) 
					mapItemsTemplates.put((Long)templateKeyId.getId(), map.get(key));
			}
		mapItemsAttributes = descriptor.getMapItemsAttributes();
		
		// to future
		String filterQuery = request.getParameter(PARAM_FILTER_QUERY);
		initSearchAction(filterQuery);
	}
	
	private Card getRequestObject() throws ServletException {
		try {
			Card cardRequestObject= null;
			CardLinkAttribute requestObject = (CardLinkAttribute) cardInfo.getCard()
				.getAttributeById(REQUEST_OBJECT_ID); // �������� �������� �� ������� �������� �� ��������: "������ �������"
			ObjectId[] requestObjectIds = requestObject.getIdsArray();																// !!!!! null ?
			if (requestObjectIds == null) {
				logger.error("com.aplana.dbmi.card.ResponseTransmittedAttributeEditor: requestObjectIds is null !");
			} else {
				ObjectId requestObjectId = requestObjectIds[0]; // �������� id ��������: "������ �������"
				if (requestObjectId == null) {
					logger.error("com.aplana.dbmi.card.ResponseTransmittedAttributeEditor: requestObjectId is null !");
				} else {
					cardRequestObject = (Card)serviceBean.getById(requestObjectId); // �������� ��������: "������ �������"
					if (cardRequestObject == null) {
						logger.error("com.aplana.dbmi.card.ResponseTransmittedAttributeEditor: cardRequestObject is null !");
					} else {
						if (mapItemsTemplates != null && !mapItemsTemplates.isEmpty()) {
							String state = mapItemsTemplates.get((Long)cardRequestObject.getTemplate().getId());
							ObjectId state_id = null;
							if (state != null && !"".equals(state)) {
								state_id = ObjectId.predefined(CardState.class, state);
								if (((Long)state_id.getId()).equals((Long)cardRequestObject.getState().getId())) {
									return cardRequestObject;
								} else
									logger.info("com.aplana.dbmi.card.ResponseTransmittedAttributeEditor: Request-Object in state 'Object is found' for outbox card not found.");
							} else
								logger.error("com.aplana.dbmi.card.ResponseTransmittedAttributeEditor: mapItems/templates/item in xml is empty or incorrect for template: " + cardRequestObject.getTemplate().getId() + " !");
						} else 
							logger.error("com.aplana.dbmi.card.ResponseTransmittedAttributeEditor: mapItems/templates in xml is empty or incorrect !");
					}
				}
			}
			return cardRequestObject;
		} catch (Exception e) {
			throw new ServletException("com.aplana.dbmi.card.ResponseTransmittedAttributeEditor: Error in getAdditionalFilter: /r/n", e);
		}
	}
	
	private void setLinkAttribute(long template) {
		ObjectId card_id_og= null;
		ObjectId attr_id= null;
		// ����: �������� ����� ��������� �� �������� ������� ������� (��� ��) ��� �������� ������������� �� �������� ������� ������� (��� ���������)
		String value = mapItemsTemplates.get(template);
		if (value != null && !"".equals(value)) {
			card_id_og = getCardAttributeId(value);
			if (card_id_og != null)
				// ���� � ����: ������� ����� ��������� (��� ����� ��������� �� �������� ������� �������) ��� ������� ����������� (��� ������������� �� �������� ������� �������)
				if (mapItemsAttributes != null && !mapItemsAttributes.isEmpty()) {
					String valueAttr = mapItemsAttributes.get(value);
					if (valueAttr != null && !"".equals(valueAttr)) {
						attr_id = ObjectId.predefined(CardLinkAttribute.class, valueAttr);
						additionalFilter.put(attr_id, card_id_og);
					}
				} else 
					logger.error("com.aplana.dbmi.card.ResponseTransmittedAttributeEditor: mapItems/attributes in xml is empty or incorrect !");
		} else
			logger.error("com.aplana.dbmi.card.ResponseTransmittedAttributeEditor: mapItems/templates/item in xml is empty or incorrect for template: " + template + " !");
	}
	
	private ObjectId getCardAttributeId(final String cardLink) {
		final ObjectId REQUEST_ATTR_ID = ObjectId.predefined(
			    CardLinkAttribute.class, cardLink);
		CardLinkAttribute requestAttr = (CardLinkAttribute) requestObject
			.getAttributeById(REQUEST_ATTR_ID); // �������� �������� �� �������� "������ �������" �� �������� �� cardLink
		ObjectId[] requestAttrIds = requestAttr.getIdsArray();																	// null ? !!!!!!!!!!!!!!!!!!!!
		if (requestAttrIds == null) {
			logger.error("com.aplana.dbmi.card.ResponseTransmittedAttributeEditor: requestAttrIds is null ! Cardlink: " + cardLink + "; ");
		} else {
			ObjectId requestAttrId = requestAttrIds[0]; // �������� �� ��������
			if (requestAttrId == null) {
				logger.error("com.aplana.dbmi.card.ResponseTransmittedAttributeEditor: requestAttrId is null ! Cardlink: " + cardLink + "; ");
			} else {
				return requestAttrId;		
			}
		}
		return null;
	}

	/**
	 * @return ��� ��������, ��� �������� ���������
	 */
	public ObjectId getAttrId() {
		return attrId;
	}
}
