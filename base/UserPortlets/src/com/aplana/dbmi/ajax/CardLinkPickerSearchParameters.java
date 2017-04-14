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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.ajax.mapper.SearchParametersMapper;
import com.aplana.dbmi.card.CardLinkPickerAttributeEditor;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerDescriptor;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerVariantDescriptor;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerVariantDescriptor.SearchDependency;
import com.aplana.dbmi.card.hierarchy.descriptor.LinkDescriptor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class CardLinkPickerSearchParameters extends SearchResultLabelBuilder implements SearchCardServletParametersEx {
	public static final String PARAM_NAMESPACE = "namespace";
	public static final String PARAM_ATTR_CODE = "attrCode";
	public static final String PARAM_ATTR_TYPE_CODE = "attrTypeCode";
	public static final String PARAM_VARIANT_ALIAS = "variant";
	public static final String PARAM_DEPENDENCY_PREFIX = "param";
	public static final String PARAM_DEP_SPECIAL_PREFIX = "spec";
	public static final String CALLER = "cardLinkPicker";
	
	protected Log logger = LogFactory.getLog(getClass());
	protected Search search, softSearch, altSearch;	// ��������� ������ ����� ��� ������������� ������������. � altSearch - ����� �� �������������� ������������
	protected boolean useSoftSearch = false;
	protected boolean useAltSearch = false;
	private ObjectId labelAttrId;
	private LinkDescriptor linkDescriptor;
	
	public static final String SPEC_DEPARTMENT = "department";
	public static final String SPEC_CARDPORTLET_CARD_ID = "cardportlet_card_id";
	private static final ObjectId USER_DEPARTMENT =
		ObjectId.predefined(CardLinkAttribute.class, "jbr.personInternal.department");
	private static final ObjectId PARENT_DEPARTMENT =
		ObjectId.predefined(CardLinkAttribute.class, "jbr.department.parentDepartment");
	
	protected DataServiceBean dataServiceBean;
	
	private Card department;
	private ObjectId filterAttrId;
	
	public ObjectId getLabelAttrId() {
		return labelAttrId;
	}

	public Search getSearch() {
		if (department == null)
			return search;
		Search search = this.search.makeCopy();
		//���������� �������� ��� ������ ������ �� getSearch()
		return search;
	}

	public void initialize(HttpServletRequest request, DataServiceBean serviceBean) throws ServletException {
		
		initDataServiceBean(request);
		
		String attrCode = request.getParameter(PARAM_ATTR_CODE);
		String alias = request.getParameter(PARAM_VARIANT_ALIAS);

		ObjectId attrId = getAttributeId(attrCode, request);
		
		CardLinkPickerDescriptor d = getCardLinkPickerDescriptor(attrId, request);
		
		ObjectId choiceRefValueId = 
			d.isLocalChoice() ? new ObjectId(ReferenceValue.class, alias) :
			(CardLinkPickerDescriptor.DEFAULT_REF_ID.equals(alias) ? 
			null : 
			new ObjectId(ReferenceValue.class, Long.parseLong(alias)))	;		
		
		CardLinkPickerVariantDescriptor vd = d.getVariantDescriptor(choiceRefValueId);
		
		if (vd == null) {
			throw new ServletException("CardLinkPickerVariant alias is not found: " + alias);
		}
		search = vd.getSearch();
		softSearch = search.makeCopy();
		altSearch = search.makeCopy();
		useSoftSearch = vd.isUseSoftSearch();
		useAltSearch = vd.isUseAltSearch();
		for (int i = 0; i < vd.getSearchDependencies().size(); ++i) {
				SearchDependency sd = (SearchDependency)vd.getSearchDependencies().get(i);
				Search tempSeach = null;
				if(useAltSearch&& sd.isAlternativeDependency()){
					tempSeach = altSearch;
				}else {
					tempSeach = search;
				}
				if (sd.isSpecial()) {
					if (SPEC_DEPARTMENT.equals(sd.getSpecialValue())) {
						try {
							Card userCard = getUserCard(request);
							
							CardLinkAttribute link = (CardLinkAttribute) userCard.getAttributeById(USER_DEPARTMENT);
							if (link == null) {
								logger.warn("Person card #" + userCard.getId().getId() + " doesn't have the 'Department' attribute");
								continue;
							}
							ObjectId deptId = link.getSingleLinkedId();
							if (deptId != null) {
								department = (Card) dataServiceBean.getById(deptId);
							} else {
								logger.warn("Person card #" + userCard.getId().getId() + " have empty 'Department' attribute");
								continue;
							}
							filterAttrId = sd.getFilterAttrId();
							tempSeach.addCardLinkAttribute(filterAttrId, department.getId());
							if(sd.isStrictSpecialType()){
								filterAttrId = null;
							}
						} catch (Exception e) {
							logger.error("Error fetching person or its department card", e);
							continue;
						}
					} else if (SPEC_CARDPORTLET_CARD_ID.equals(sd.getSpecialValue())) {
						filterAttrId = sd.getFilterAttrId();
						if(BackLinkAttribute.class.equals(filterAttrId.getType())){
							tempSeach.addBackLinkAttribute(filterAttrId, getSessionBean(request).getActiveCard().getId());							
						} else {
							tempSeach.addCardLinkAttribute(filterAttrId, getSessionBean(request).getActiveCard().getId());
						}
						
					} else {
						logger.warn("Unknown special dependency: " + sd.getSpecialValue());
					}
					continue;
				}
	
				String p = request.getParameter(PARAM_DEPENDENCY_PREFIX + i);
				if (vd.isHideAllValues() && (p == null ||  "".equals(p)))  p="-1";
				if (p != null && !"".equals(p)) {
					SearchParametersMapper.newInstance(dataServiceBean, sd).perform(tempSeach, p);
				}
		}
	
			labelAttrId = vd.getSearchAttrId();
			linkDescriptor = vd.getList();
		
	}

	
	protected void initDataServiceBean(HttpServletRequest request) {
				CardPortletSessionBean sessionBean = getSessionBean(request);
		
		dataServiceBean = sessionBean.getServiceBean();
	}
	
	private CardPortletSessionBean getSessionBean(HttpServletRequest request) {
		
		String namespace = request.getParameter(PARAM_NAMESPACE);

		return CardPortlet.getSessionBean(request, namespace);
		
	}
	
	

	protected Card getUserCard(HttpServletRequest request) throws ServiceException, DataException  {
		
		CardPortletSessionBean sessionBean = getSessionBean(request);
		
		return  (Card) sessionBean.getServiceBean().getById(sessionBean.getServiceBean().getPerson().getCardId());
	}

	protected CardLinkPickerDescriptor getCardLinkPickerDescriptor(ObjectId attrId, HttpServletRequest request) {
		
		CardPortletCardInfo cardInfo = getActiveCardInfo(request);
		
		return  (CardLinkPickerDescriptor)cardInfo.getAttributeEditorData(attrId, CardLinkPickerAttributeEditor.KEY_DESCRIPTOR);
		
	}

	private CardPortletCardInfo getActiveCardInfo(HttpServletRequest request) {
		
		CardPortletSessionBean sessionBean = getSessionBean(request);
		
		return  sessionBean.getActiveCardInfo();
	}

	protected ObjectId getAttributeId(String attrCode, HttpServletRequest request) {
		
		if(dataServiceBean == null) initDataServiceBean(request);
		Attribute attr = null;
		try{
			attr = (Attribute) dataServiceBean.getById(new ObjectId(Attribute.class, attrCode));
		} catch(Exception e) {throw new IllegalStateException(e);}
		
		/*
		CardPortletCardInfo cardInfo = getActiveCardInfo(request);
		
		Attribute attr = AttrUtils.getAttributeByCode(attrCode, cardInfo.getCard());
		*/
		
		if (attr == null) {
			throw new IllegalStateException("Couldn't find attribute with code = '" + attrCode + "' in card");
		}
		
		return attr.getId();

	}
	
	public boolean nextSearch(Search oldSearch) {
		if (department == null&&!useSoftSearch&&!useAltSearch)
			return false;
		//���� ������� Search �������� Special �������, �� ������� �������� ������������ ������������� � ��������� �����
		if(oldSearch.getAttribute(filterAttrId)!=null){
			if(getParentDepartment()){
				search.addCardLinkAttribute(filterAttrId, department.getId());
				return true;
			}
		}
		//���� �������� ����� �� ��� �����������, �� ��������� �������������� �����
		if(useAltSearch){ // ��� ��� ������ �����
			logger.info("Hard search "+search+" return 0 card => run alt. search" + altSearch);
			useAltSearch = false;
			search = altSearch;
			return true;
		}
		//���� � �������� � �������������� ����� �� ���� �����������, �� ��������� ����������� �����
		if (useSoftSearch){	// ���� ������ ������ �����, �� ��� ��� ������ �����
			logger.info("Hard search "+search+" and alt. search "+altSearch+" return 0 card => run soft search" + softSearch);
			useSoftSearch = false;
			search = softSearch;	// ������������ ����� ������� �� ������
			return true;
		} 
		return false;		// �� ���� ������ �������, ���������� ������ �� ����������
	}
	
	//�������� ������������ ������������� � ������� ��� � this.department
	private boolean getParentDepartment(){
		// ���� ������ ����������� - ��� ���� �����
		if (department != null){
			CardLinkAttribute link = (CardLinkAttribute) department.getAttributeById(PARENT_DEPARTMENT);
			if (link == null) {
				logger.warn("Department card #" + department.getId().getId() + " doesn't have the 'Parent department' attribute");
				return false;
			}
			ObjectId parentId = link.getSingleLinkedId();
				
			if (parentId == null)	// we reached the top
				return false;
			
			// (YNikitin, 2012/08/30) ���� ������������ ���������� - ��� �������, �� ����� �� �������� ������������, ����������������
			if (parentId.equals(department.getId())){
				logger.warn("Department card #" + department.getId().getId() + " have the same 'Parent department' attribute... it will loop => stop nextSearch()");
				return false;
			}
			try {
				
				department = (Card) dataServiceBean.getById(parentId);
				return true;
			
			} catch (Exception e) {
				logger.error("Error fetching parent department card #" + parentId.getId(), e);
				return false;
			}
		} else {
			return false;
		}
	}

	public LinkDescriptor getList() {
		return linkDescriptor;
	}

}
