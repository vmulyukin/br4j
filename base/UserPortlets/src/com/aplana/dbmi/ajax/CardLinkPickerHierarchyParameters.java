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
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.actionhandler.ActionsManager;
import com.aplana.dbmi.card.CardLinkPickerAttributeEditor;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.CommonCardLinkPickerEditor;
import com.aplana.dbmi.card.EditorsDataContainingSessionBean;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerDescriptor;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerVariantDescriptor;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerVariantDescriptor.SearchDependency;
import com.aplana.dbmi.card.hierarchy.descriptor.HierarchyDescriptor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.search.SearchFilterPortlet;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;

public class CardLinkPickerHierarchyParameters implements CardHierarchyServletParameters {
	private final Log logger = LogFactory.getLog(getClass());
	
	public static final String CALLER = "cardLinkPickerHierarchy";
	
	public static final String PARAM_ACTIVE_VARIANT = "activeVariant";
	
	//private CardPortletCardInfo cardInfo;
	private EditorsDataContainingSessionBean sessionBean;
	private ObjectId attrId;
	private CardLinkPickerVariantDescriptor variantDescriptor;
	private DataServiceBean serviceBean;
	private Search storedCardsSearch, softStoredCardsSearch;
	private boolean useSoftSearch = false;		// ��� ������� ��������� �������� ��� ��������, ���� ����� �� ������ searchDependencies ������   
	private HierarchyConnection hconn;
	private String hierarchyKey;
	private boolean cacheReset = false;
	
	
	public ActionsManager getActionsManager() {
		return (ActionsManager) sessionBean.getEditorData(attrId, CommonCardLinkPickerEditor.ACTION_MANAGER_PREFIX + variantDescriptor.getAlias());
		//return CardLinkPickerAttributeEditor.getActionsManager(attrId, variantDescriptor, cardInfo);
	}

	public HierarchyConnection getHierarchyConnection() {
		return hconn;
	}

	public HierarchyDescriptor getHierarchyDescriptor() {
		return variantDescriptor.getHierarchyDescriptor();
	}

	public Collection getStoredCards() {
		try {
			SearchResult storedCards = (SearchResult)serviceBean.doAction(storedCardsSearch);
			// ���� ����� ������, ��� ������ ������ ��������� �������� ��� � storedCardsSearch �� ��������� defaultStoredCardsSearch, �.�. ������� ����������� (dependency)   
			if (storedCards.getCards().isEmpty()&&useSoftSearch&&!storedCardsSearch.equals(softStoredCardsSearch))
				storedCards = (SearchResult)serviceBean.doAction(softStoredCardsSearch);
			return ObjectIdUtils.getObjectIds(storedCards.getCards());
		} catch (Exception e) {
			logger.error("Failed to load stored cards", e);
			return new ArrayList(0);
		}
	}

	public void init(HttpServletRequest request) throws ServletException {
		String namespace = request.getParameter(PARAM_NAMESPACE);
		sessionBean = (EditorsDataContainingSessionBean) CardPortlet.getSessionBean(request, namespace);
		if(sessionBean == null) sessionBean = (EditorsDataContainingSessionBean) SearchFilterPortlet.getSessionBean(request, namespace);
		if(sessionBean == null) throw new ServletException("Cannot resolve sessionBean.");
		
		serviceBean = sessionBean.getServiceBean();
		//cardInfo = sessionBean.getActiveCardInfo();
		
		hierarchyKey = request.getParameter(PARAM_HIERARCHY_KEY);
		
		//Card card = cardInfo.getCard();
		
		String attrCode = request.getParameter(PARAM_ATTR_CODE);
		Attribute attr = null; 
		try{
			attr = (Attribute) serviceBean.getById(new ObjectId(Attribute.class, attrCode));
		}catch(Exception e){throw new ServletException(e);}
		if (attr == null) {
			throw new ServletException("Couldn't find attribute with code '" + attrCode + "' in card");
		}
		attrId = attr.getId();
		CardLinkPickerDescriptor d = (CardLinkPickerDescriptor) sessionBean.getEditorData(attrId, CardLinkPickerAttributeEditor.KEY_DESCRIPTOR);
		String paramAlias = request.getParameter(PARAM_ACTIVE_VARIANT);
		variantDescriptor = d.getVariantDescriptor(paramAlias);
		if (variantDescriptor == null) {
			throw new ServletException("Couldn't find variant descriptor with alias: '" + paramAlias + "'");
		}

		storedCardsSearch = variantDescriptor.getSearch();
		softStoredCardsSearch = storedCardsSearch.makeCopy();
		useSoftSearch = variantDescriptor.isUseSoftSearch();
		for (int i = 0; i < variantDescriptor.getSearchDependencies().size(); ++i) {
			SearchDependency sd = (SearchDependency)variantDescriptor.getSearchDependencies().get(i);
			String p = request.getParameter(CardLinkPickerSearchParameters.PARAM_DEPENDENCY_PREFIX + i);
			if (p != null && !"".equals(p)) {
				Collection ids = ObjectIdUtils.commaDelimitedStringToNumericIds(p, Card.class);
				Iterator j = ids.iterator();
				while (j.hasNext()) {
					storedCardsSearch.addCardLinkAttribute(sd.getFilterAttrId(), (ObjectId)j.next());
				}
			}
		}
		hconn = (HierarchyConnection)sessionBean.getEditorData(attrId, hierarchyKey);
		// �������� �� ���������� ������� ���������� ���� 
		Boolean bCacheReset = (Boolean) sessionBean.getEditorData(attrId, CardLinkPickerAttributeEditor.KEY_CACHE_RESET);
		cacheReset = (bCacheReset==null)?false:bCacheReset.booleanValue(); 
		sessionBean.setEditorData(attrId, CardLinkPickerAttributeEditor.KEY_CACHE_RESET, false);
	}

	public void storeHierarchyConnection(HierarchyConnection hconn) {
		if (hierarchyKey != null) {
			sessionBean.setEditorData(attrId, hierarchyKey, hconn);
		}
	}

	/**
	 * @return ��� ��������, ��� �������� ���������
	 */
	public ObjectId getAttrId() {
		return attrId;
	}

	public boolean isCacheReset() {
		return cacheReset;
	}

}
