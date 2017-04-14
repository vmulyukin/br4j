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
import java.util.ArrayList;
import java.util.Collection;

import javax.portlet.ActionRequest;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerDescriptor;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerDescriptorReader;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerVariantDescriptor;
import com.aplana.dbmi.card.graph.Graph;
import com.aplana.dbmi.card.graph.GraphDescriptor;
import com.aplana.dbmi.card.graph.GraphDescriptorReader;
import com.aplana.dbmi.card.graph.GraphLoader;
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

public class HierarchicalCardLinkAttributeViewer extends ActionsSupportingAttributeEditor implements Parametrized {
	public static final String HIERARCHY_VIEW = "hierarchyView";
	public static final String HIERARCHY_VIEW_DESCRIPTOR = "hierarchyViewDescriptor";
	public final static String HIERARCHY_VIEW_STORED_CARDS = "hierarchyViewStoredCards";
	private String config;

	public static final String PARAM_CONFIG_GRAPH = "configGraph";

	public static final String GRAPH_IS_VIEW = "graphIsView";
	public static final String GRAPH_DATA = "graphData";
	private String configGraph;

	@Override
	public boolean doesFullRendering(Attribute attr) {
		return true;
	}

	public HierarchicalCardLinkAttributeViewer() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/HierarchicalCardListView.jsp");
		setParameter(PARAM_INIT_JSP, "/WEB-INF/jsp/html/attr/HierarchicalCardListInclude.jsp");
	}

	@Override
	public void initEditor(PortletRequest request, Attribute attr)
			throws DataException {
		CardPortletSessionBean sessionBean = (CardPortletSessionBean)request.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		ObjectId attrId = attr.getId();
		if (cardInfo.getAttributeEditorData(attrId, HIERARCHY_VIEW_DESCRIPTOR) == null) {
			InputStream stream;
			try {
				stream = Portal.getFactory().getConfigService().loadConfigFile(AttributeEditorFactory.CONFIG_FOLDER + config);
			} catch (IOException e) {
				logger.error("Couldn't open hierarchy descriptor file: " + config, e);
				throw new DataException(e);
			}
			HierarchyDescriptor descriptor = null;
			DataServiceBean serviceBean = sessionBean.getServiceBean();
			try {
				//HierarchyDescriptorReader reader = new HierarchyDescriptorReader();
				//descriptor = reader.read(stream, serviceBean);
			    try {
				//������ xml-�� ���������� CardLinkPickerDescriptorReader � CardLinkPickerDescriptor.
				//����� �������� HierarchyDescriptor �� CardLinkPickerDescriptor'�.
				//������ � ��� Messages � ������� action'� �� ���������: "accept" � "cancel".
				final CardLinkPickerDescriptorReader reader = new CardLinkPickerDescriptorReader(serviceBean);
				final CardLinkPickerDescriptor d = reader.read(stream, attr);

				final ObjectId chAttrObjId = d.getChoiceAttrId();

				if(chAttrObjId != null){
					final ListAttribute chAttr = (ListAttribute) cardInfo.getCard().getAttributeById(chAttrObjId);
					if(chAttr != null) {
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

				if (descriptor == null){
					descriptor = d.getDefaultVariantDescriptor().getHierarchyDescriptor();
				}

				descriptor.setMessages(d.getMessages());
				descriptor.getActionsDescriptor().removeItem(CardLinkPickerAttributeEditor.ACTION_ACCEPT);
				descriptor.getActionsDescriptor().removeItem(CardLinkPickerAttributeEditor.ACTION_CANCEL);
			} catch (Exception e) {
				throw new DataException(e);
			}
				Collection /*<ObjectId>*/ cardIds;
				if (attr instanceof CardLinkAttribute) {
					// cardIds = ObjectIdUtils.getObjectIds(((CardLinkAttribute)attr).getValues());
					cardIds = new ArrayList(((CardLinkAttribute) attr).getIdsLinked());
				} else if (attr instanceof BackLinkAttribute) {
					BackLinkAttribute a = (BackLinkAttribute)attr;
					ListProject projectAction = new ListProject();
					projectAction.setCard(sessionBean.getActiveCard().getId());
					projectAction.setAttribute(a.getId());
					final SearchResult r = (SearchResult)serviceBean.doAction(projectAction);
					cardIds = ObjectIdUtils.getObjectIds(r.getCards());
				} else {
					throw new IllegalArgumentException("wrong attribute type: " + attr.getClass().getCanonicalName());
				}
				cardInfo.setAttributeEditorData(attr.getId(), HIERARCHY_VIEW_STORED_CARDS, cardIds);
			} catch (Exception e) {
				throw new DataException(e);
			}
			cardInfo.setAttributeEditorData(attr.getId(), HIERARCHY_VIEW_DESCRIPTOR, descriptor);
			initActionsManager(sessionBean, descriptor.getActionsDescriptor(), attr);
		}

		loadAttributeValues(attr, request);
	}

	@Override
	public void loadAttributeValues(Attribute attr, PortletRequest request) {
		CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		cardInfo.setAttributeEditorData(attr.getId(), HIERARCHY_VIEW, null);
		try{
			cardInfo.setAttributeEditorData(attr.getId(), HIERARCHY_VIEW_STORED_CARDS, getStoredCards(attr, sessionBean, request));
		} catch(Exception e){
			logger.error("Error refreshing value of attribute " + attr); 
			e.printStackTrace();
		}
		// ��������� ���� ���� �� �����
		if (configGraph != null) {
			DataServiceBean serviceBean = sessionBean.getServiceBean();
			InputStream stream;
			try {
				stream = Portal.getFactory().getConfigService().loadConfigFile(AttributeEditorFactory.CONFIG_FOLDER + configGraph);
				GraphDescriptorReader reader = new GraphDescriptorReader();
				GraphDescriptor descripGraph = reader.read(stream);
				GraphLoader loader = new GraphLoader(descripGraph, serviceBean);
				Long cardId = (Long)sessionBean.getActiveCard().getId().getId();
				Graph graph = loader.load(cardId);
				cardInfo.setAttributeEditorData(attr.getId(), GRAPH_IS_VIEW, Boolean.TRUE);
				cardInfo.setAttributeEditorData(attr.getId(), GRAPH_DATA, graph);
			} catch (Exception e) {
				logger.error("Failed to load graph described in file " + configGraph, e);
			}
		} else {
			cardInfo.setAttributeEditorData(attr.getId(), GRAPH_IS_VIEW, Boolean.FALSE);
			cardInfo.setAttributeEditorData(attr.getId(), GRAPH_DATA, null);
		}
	}

	public static HierarchyDescriptor getHierarchyDescriptor(ObjectId attrId, PortletRequest request) {
		CardPortletSessionBean bean = (CardPortletSessionBean)request.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);
		return (HierarchyDescriptor)bean.getActiveCardInfo().getAttributeEditorData(attrId, HIERARCHY_VIEW_DESCRIPTOR);
	}

	@Override
	public boolean gatherData(ActionRequest request, Attribute attr) throws DataException {
		return false;
	}

	@Override
	public void setParameter(String name, String value) {
		if ("config".equals(name)) {
			this.config = value;
		} else if (PARAM_CONFIG_GRAPH.equalsIgnoreCase(name)) {
			configGraph = value;
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
	@SuppressWarnings("unchecked")
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
			projectAction.setCard(sessionBean.getActiveCard().getId());
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