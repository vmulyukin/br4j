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

import com.aplana.dbmi.actionhandler.ActionsManager;
import com.aplana.dbmi.card.ActionsSupportingAttributeEditor;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.hierarchy.Hierarchy;
import com.aplana.dbmi.card.hierarchy.descriptor.HierarchyDescriptor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;

public class HierarchicalCardListAttributeParameters implements CardHierarchyServletParameters {
	public final static String CALLER = "hierarchicalCardListAttribute";
	
	public static final String PARAM_DESCRIPTOR_KEY = "descriptorKey";
	public static final String PARAM_STORED_CARDS_KEY = "storedCardsKey";

	private CardPortletSessionBean sessionBean;
	private ObjectId attrId;
	private Collection storedCards;
	private HierarchyDescriptor hierarchyDescriptor;
	private HierarchyConnection hconn;
	private String hierarchyKey;
	
	public HierarchyConnection getHierarchyConnection() {
		return hconn;
	}

	public HierarchyDescriptor getHierarchyDescriptor() {
		return hierarchyDescriptor;
	}

	public Collection getStoredCards() {
		return storedCards;
	}

	public void storeHierarchyConnection(HierarchyConnection hconn) {
		if (hierarchyKey != null) {
			sessionBean.setAttributeEditorData(attrId, hierarchyKey, hconn);
		}
	}

	public void init(HttpServletRequest request) throws ServletException {
		String namespace = request.getParameter(PARAM_NAMESPACE);
		sessionBean = CardPortlet.getSessionBean(request, namespace);

		hierarchyKey = request.getParameter(PARAM_HIERARCHY_KEY);

		String descriptorKey = request.getParameter(PARAM_DESCRIPTOR_KEY),
			storedCardsKey = request.getParameter(PARAM_STORED_CARDS_KEY);
		
		Card card = sessionBean.getActiveCard();

		Attribute attr;
		String attrCode = request.getParameter(PARAM_ATTR_CODE);		
		attrId = new ObjectId(CardLinkAttribute.class, attrCode);
		
		// TODO: ��� ���������� ���� ����� �������������: ���������� ��� ��������������
		// ���� �������� ������ ��������� ��������
		attr = card.getAttributeById(attrId);
		if (attr == null) {
			attrId = new ObjectId(BackLinkAttribute.class, attrCode);
			attr = card.getAttributeById(attrId);
		}
		if (attr == null) {
			throw new ServletException("Couldn't find attribute with code " + attrCode);
		}

		hierarchyDescriptor = (HierarchyDescriptor)sessionBean.getAttributeEditorData(attrId, descriptorKey);		
		// TODO: �������� ��������� null-�������� hierarchyKey
		hconn = (HierarchyConnection)sessionBean.getAttributeEditorData(attrId, hierarchyKey == null ? "" : hierarchyKey);
		storedCards = (Collection)sessionBean.getAttributeEditorData(attr.getId(), storedCardsKey);		
	}

	/**
	 * @return ��� ��������, ��� �������� ���������
	 */
	public ObjectId getAttrId() {
		return attrId;
	}

	public ActionsManager getActionsManager() {
		return ActionsSupportingAttributeEditor.getActionsManager(sessionBean, attrId);
	}
}
