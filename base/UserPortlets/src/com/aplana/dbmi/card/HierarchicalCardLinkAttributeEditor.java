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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.io.IOUtils;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.ajax.HierarchyConnection;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerDescriptor;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerDescriptorReader;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerVariantDescriptor;
import com.aplana.dbmi.card.hierarchy.Hierarchy;
import com.aplana.dbmi.card.hierarchy.descriptor.HierarchyDescriptor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;

public class HierarchicalCardLinkAttributeEditor extends ActionsSupportingAttributeEditor implements Parametrized {
	public final static String HIERARCHY_EDIT_DESCRIPTOR = "hierarchyEditDescriptor";
	public final static String HIERARCHY_EDIT = "hierarchyEdit";
	public final static String HIERARCHY_EDIT_STORED_CARDS = "hierarchyEditStoredCards";
	private String config;

	@Override
	public boolean doesFullRendering(Attribute attr) {
		return true;
	}

	public HierarchicalCardLinkAttributeEditor() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/HierarchicalCardList.jsp");
		setParameter(PARAM_INIT_JSP, "/WEB-INF/jsp/html/attr/HierarchicalCardListInclude.jsp");
	}

	@Override
	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException {
		return true;
	}

	@Override
	public void writeEditorCode(RenderRequest request, RenderResponse response,
			Attribute attr) throws IOException, PortletException {
		CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		if (cardInfo.getAttributeEditorData(attr.getId(), HIERARCHY_EDIT_STORED_CARDS) == null) {
			cardInfo.setAttributeEditorData(attr.getId(), HIERARCHY_EDIT_STORED_CARDS, getStoredCards(attr, sessionBean, request));
		}
		super.writeEditorCode(request, response, attr);
	}

	@Override
	public void loadAttributeValues(Attribute attr, PortletRequest request) {
		CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		cardInfo.setAttributeEditorData(attr.getId(), HIERARCHY_EDIT, null);
		try{
			cardInfo.setAttributeEditorData(attr.getId(), HIERARCHY_EDIT_STORED_CARDS, getStoredCards(attr, sessionBean, request));
		} catch (Exception e) {
			logger.error("Error refreshing value of attribute " + attr, e); 
		}
	}

	@Override
	public void initEditor(PortletRequest request, Attribute attr) throws DataException {
		CardPortletSessionBean sessionBean = (CardPortletSessionBean)request.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		HierarchyDescriptor descriptor = null;

		InputStream stream;
		DataServiceBean serviceBean = sessionBean.getServiceBean();
		try {
			stream = Portal.getFactory().getConfigService().loadConfigFile(AttributeEditorFactory.CONFIG_FOLDER + config);
		} catch (IOException e) {
			logger.error("Couldn't open hierarchy descriptor file: " + config, e);
			throw new DataException(e);
		}
		try {
			//������ xml-�� ���������� CardLinkPickerDescriptorReader � CardLinkPickerDescriptor.
			//����� �������� HierarchyDescriptor �� CardLinkPickerDescriptor'�.
			//������ � ��� Messages � ������� action'� �� ���������: "accept" � "cancel".
			final CardLinkPickerDescriptorReader reader = new CardLinkPickerDescriptorReader(serviceBean);
			final CardLinkPickerDescriptor d = reader.read(stream, attr);
			final ObjectId chAttrObjId = d.getChoiceAttrId();
			if(chAttrObjId != null){
				final ListAttribute chAttr = (ListAttribute) cardInfo.getCard().getAttributeById(chAttrObjId);
				if(chAttr != null){
					final ReferenceValue chAttrRefVal = chAttr.getValue();
					if(chAttrRefVal != null){
						for(CardLinkPickerVariantDescriptor vd : d.getVariants()){
							if(chAttrRefVal.getId().equals(vd.getChoiceReferenceValueId())
									&& vd.checkConditions(cardInfo.getCard())) {
								descriptor = vd.getHierarchyDescriptor();
								break;
							}
						}
					}

				}
			}

			if (descriptor == null) {
				descriptor = d.getDefaultVariantDescriptor().getHierarchyDescriptor();
			}

			descriptor.setMessages(d.getMessages());
			descriptor.getActionsDescriptor().removeItem(CardLinkPickerAttributeEditor.ACTION_ACCEPT);
			descriptor.getActionsDescriptor().removeItem(CardLinkPickerAttributeEditor.ACTION_CANCEL);
		} catch (DataException e) {
			throw e;
		} catch (Exception e) {
			throw new DataException(e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
		cardInfo.setAttributeEditorData(attr.getId(), HIERARCHY_EDIT_DESCRIPTOR, descriptor);

		// ��������� ����� action'�� �������� �������� � ����������� ��������, ��
		// ��� ������������ ActionsManager'� ���������� ��, � �� action'� �� actionsConfig
		initActionsManager(sessionBean, descriptor.getActionsDescriptor(), attr);
	}

	public static Hierarchy getHierarchy(ObjectId attrId, PortletRequest request) {
		CardPortletSessionBean sessionBean = (CardPortletSessionBean)request.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		final HierarchyConnection h = (HierarchyConnection)cardInfo.getAttributeEditorData(attrId, HIERARCHY_EDIT);
		return (h == null) ? null : h.getHierarchy();
	}

	public static HierarchyDescriptor getHierarchyDescriptor(ObjectId attrId, PortletRequest request) {
		CardPortletSessionBean sessionBean = (CardPortletSessionBean)request.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		return (HierarchyDescriptor)cardInfo.getAttributeEditorData(attrId, HIERARCHY_EDIT_DESCRIPTOR);
	}

	@Override
	public boolean processAction(ActionRequest request, ActionResponse response, Attribute attr) throws DataException {
		Hierarchy hierarchy = getHierarchy(attr.getId(), request);
		if (hierarchy == null) {
			return false;
		}
		return super.processAction(request, response, attr);
	}

	@Override
	public void setParameter(String name, String value) {
		if ("config".equals(name)) {
			this.config = value;
		} else if (ACTIONS_CONFIG_PARAM.equals(name)) {
			throw new IllegalArgumentException("Actions must be defined in hierarchy descriptor, not in separate file!");
		} else {
			super.setParameter(name, value);
		}
	}

	/**
	 * @param attr
	 * @param sessionBean
	 * @param request
	 * @return
	 * @throws PortletException
	 */
	private Collection<ObjectId> getStoredCards(Attribute attr, CardPortletSessionBean sessionBean, 
				PortletRequest request) throws PortletException 
	{
		DataServiceBean serviceBean = sessionBean.getServiceBean();
		Collection<ObjectId> cardIds;
		if (attr instanceof CardLinkAttribute) {
			// (2010/02, RuSA) OLD: cardIds = ObjectIdUtils.getObjectIds(((CardLinkAttribute)attr).getValues());
			cardIds = ((CardLinkAttribute) attr).getIdsLinked();
		} else if (attr instanceof BackLinkAttribute) {
			BackLinkAttribute a = (BackLinkAttribute)attr;
			ListProject projectAction = new ListProject();
			ObjectId cardId = sessionBean.getActiveCard().getId();
			if (cardId == null) {
				return ((BackLinkAttribute)attr).getIdsLinked();
			}
			projectAction.setCard(cardId);
			projectAction.setAttribute(a.getId());
			try {
				final SearchResult r = (SearchResult)serviceBean.doAction(projectAction);	
				cardIds = ObjectIdUtils.getObjectIds(r.getCards());
			} catch (Exception e) {
				throw new PortletException(e);
			}
		} else {
			throw new IllegalArgumentException("wrong attribute type: " + attr.getClass().getName());
		}
		return cardIds;
	}

	@Override
	public boolean isValueCollapsable() {
		return true;
	}
}