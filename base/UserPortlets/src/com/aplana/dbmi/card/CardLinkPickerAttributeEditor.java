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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.actionhandler.ActionsManager;
import com.aplana.dbmi.ajax.CardLinkPickerSearchParameters;
import com.aplana.dbmi.card.actionhandler.CardPortletAttributeEditorActionsManager;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerDescriptor;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerVariantDescriptor;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerVariantDescriptor.SearchDependency;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataServiceBean;

public class CardLinkPickerAttributeEditor extends CommonCardLinkPickerEditor {
	//public static final String SEARCH = "SEARCH";

	public static final String HIERARCHY_PREFIX = "cardLinkPickerHierarchy_";

	public static final String ACTION_ACCEPT = "accept";
	public static final String ACTION_CANCEL = "cancel";

	public static final String FIELD_LABEL = "label";
	public static final String FIELD_TEMPLATE = "template";
	public static final String FIELD_CARD_ID = "cardId";
	public static final String FIELD_QUERY_FILTER = "queryFilter";
	public static final String ID_DELIMITER = ",";
	
	public static int KEY_FIELD_LINKED;

	@Override
	protected ActionsManager getActionsManager(ObjectId attrId, CardLinkPickerVariantDescriptor vd, PortletRequest request) {
		
		CardPortletSessionBean sessionBean = (CardPortletSessionBean)request.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		
		return (ActionsManager)cardInfo.getAttributeEditorData(attrId, ACTION_MANAGER_PREFIX + vd.getAlias()); 
	}
	
	
	@Override
	protected void storeAttributeEditorsParameters(PortletRequest request, Attribute attr) {

		CardPortletCardInfo cardInfo = getCardInfo(request);
		
		cardInfo.setAttributeEditorData(attr.getId(), KEY_SHOW_TITLE, new Boolean(showTitle));
		cardInfo.setAttributeEditorData(attr.getId(), KEY_SHOW_EMPTY, new Boolean(showEmpty));
		cardInfo.setAttributeEditorData(attr.getId(), KEY_TYPE_CAPTION, typeCaption);
		cardInfo.setAttributeEditorData(attr.getId(), KEY_DATE_TYPE_CAPTION, dateCaption);
		cardInfo.setAttributeEditorData(attr.getId(), PARAM_CONNECTION_TYPE_SHOW, enableConnectionTypeShow);
		
	}
	
	
	@Override
	protected CardLinkPickerVariantDescriptor getCardLinkVariantDescriptor(Attribute attr, PortletRequest request) {

		CardPortletCardInfo cardInfo = getCardInfo(request);
		
		CardLinkPickerDescriptor d = (CardLinkPickerDescriptor)cardInfo.getAttributeEditorData(attr.getId(), KEY_DESCRIPTOR);

		return d.getVariantDescriptor(cardInfo.getCard());
	}
	
	
	
	@Override
	protected DataServiceBean getDataServiceBean(PortletRequest request) {
		
		CardPortletSessionBean sessionBean = getCardPortletSessionBean(request);
		
		return sessionBean.getServiceBean();
		
	}


	@Override
	protected void initializeActions(PortletRequest request, Attribute attr, CardLinkPickerDescriptor d) {
		
		ObjectId attrId = attr.getId();
		
		CardPortletCardInfo cardInfo = getCardInfo(request);
		
		CardPortletSessionBean sessionBean = getCardPortletSessionBean(request);
		
		for (Iterator<?> i = d.getVariants().iterator(); i.hasNext();) {
			
			CardLinkPickerVariantDescriptor vd = (CardLinkPickerVariantDescriptor) i
					.next();
			
			if (vd.getHierarchyDescriptor() != null) {
				CardPortletAttributeEditorActionsManager am = CardPortletAttributeEditorActionsManager
						.getInstance(sessionBean, vd.getHierarchyDescriptor()
								.getActionsDescriptor(), attr);
				cardInfo.setAttributeEditorData(attrId, ACTION_MANAGER_PREFIX
						+ vd.getAlias(), am);
			}
			
			
			this.KEY_FIELD_LINKED = -1;
			ArrayList Tmpcolumns = (ArrayList)vd.getColumns();
			for(int iii = 0; iii<Tmpcolumns.size(); iii++) {
				SearchResult.Column tmpcol = (SearchResult.Column)Tmpcolumns.get(iii); 
				if (tmpcol.isLinked()) {
					this.KEY_FIELD_LINKED = iii;
				}
			}
			
		}
	}


	
	protected CardPortletCardInfo getCardInfo(PortletRequest request) {
		
		CardPortletSessionBean sessionBean = getCardPortletSessionBean(request);
		return  sessionBean.getActiveCardInfo();
	}



	@Override
	protected void storeKeyDescriptor(PortletRequest request, Attribute attr, CardLinkPickerDescriptor d) {
		
		CardPortletCardInfo cardInfo = getCardInfo(request);
		
		cardInfo.setAttributeEditorData(attr.getId(), KEY_DESCRIPTOR, d);
		
	}



	
	@Override
	protected void checkCurrentCard(PortletRequest request) {
		
		CardPortletCardInfo cardInfo = getCardInfo(request);
		
		// ������ �� ����������� ������ ��������� �� ���� �� ����� ������ �������...
		final ObjectId cardId = cardInfo.getCard().getId();
		
		if (canSelectCurCard)
			restrictedCardIds.remove(cardId);
		else
			restrictedCardIds.add(cardId);
		
	}


	@Override
	protected CardLinkPickerVariantDescriptor getActiveVariantDescriptor(CardLinkPickerDescriptor d,  PortletRequest request) {

		CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request); 

		return d.getVariantDescriptor(sessionBean.getActiveCard());
		
	}

	
	@Override
	protected String getChoiceAttrId(CardLinkPickerDescriptor d, PortletRequest request,  Attribute attr) {
		
		CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
		
		Card card = sessionBean.getActiveCard();
		
		String choiceAttrHtmlId = null;
		
		if (d.getChoiceAttrId() != null) {
			if(d.isLocalChoice()){
				choiceAttrHtmlId = getAttrHtmlId(d.getChoiceAttrId().getId().toString().concat(attr.getId().getId().toString()));
			} else {
				choiceAttrHtmlId = getAttrHtmlId((String)(d.getChoiceAttrId().getId()));
			}

		}
		return choiceAttrHtmlId;
	}


	@Override
	protected CardLinkPickerDescriptor getCardLinkPickerDescriptor(Attribute attr, PortletRequest request) {
		
		CardPortletCardInfo cardInfo = getCardInfo(request);

		return  (CardLinkPickerDescriptor)cardInfo.getAttributeEditorData(attr.getId(), KEY_DESCRIPTOR);
		
	}
	

	
	
	public static ActionsManager getActionsManager(ObjectId attrId, CardLinkPickerVariantDescriptor vd, CardPortletCardInfo cardInfo) {
		
		return (ActionsManager)cardInfo.getAttributeEditorData(attrId, ACTION_MANAGER_PREFIX + vd.getAlias()); 
	}

	@Override
	protected JSONObject getVariantsJSON(CardLinkPickerDescriptor descriptor,PortletRequest request, Attribute attr)
			throws JSONException 
	{
		JSONObject result = super.getVariantsJSON(descriptor, request, attr);
		
		addDependencies(request, descriptor, result);
		
		return result;
	}

	private Card getBaseDocByLink(PortletRequest request, Card currentCard, ObjectId baseDocCardLink) {
		try {
			if (currentCard != null && baseDocCardLink != null) {
				LinkAttribute link = currentCard.getAttributeById(baseDocCardLink);
				ObjectId baseDocId = link.getFirstIdLinked();
				return getDataServiceBean(request).getById(baseDocId);
			}
		} catch (Exception e) {
			logger.error("Unable to retreive Object by ObjectId due to " + e.getMessage(), e);
		}
		return null;
	}

	protected void addDependencies(PortletRequest request,
			CardLinkPickerDescriptor descriptor, JSONObject variants)
			throws JSONException {

		CardPortletCardInfo cardInfo = getCardInfo(request);
		Card card = cardInfo.getCard();

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
					queryObject
							.put(
									CardLinkPickerSearchParameters.PARAM_DEP_SPECIAL_PREFIX
											+ j, sd.getSpecialValue());
				} else {

					final ObjectId valueAttrId = sd.getValueAttrId();
					dependencies.put(valueAttrId.getId());
					CardLinkAttribute valueAttr = null;
					if(sd.isUseParent()) {
						final Card parentCard = cardInfo.getParentCardInfo() != null
													? cardInfo.getParentCardInfo().getCard()
													: getBaseDocByLink(request, card, sd.getFilterAttrId());
						if (parentCard != null) {
							valueAttr = (CardLinkAttribute) parentCard.getAttributeById(valueAttrId);
						}

						if(valueAttr == null)
							valueAttr = (CardLinkAttribute) card
							.getAttributeById(valueAttrId);
						// TODO ���������� ������� �� ������� ����������/���������
					} else {
						valueAttr = (CardLinkAttribute) card
								.getAttributeById(valueAttrId);
					}
					
					if (valueAttr == null) {
						logger.warn("Attribute with code '"
								+ sd.getValueAttrId()
								+ "' not found in card. Ignoring");
					} else if (!valueAttr.isEmpty()) {
						queryObject
								.put(
										CardLinkPickerSearchParameters.PARAM_DEPENDENCY_PREFIX
												+ j,
										ObjectIdUtils
												.numericIdsToCommaDelimitedString(valueAttr
														.getIdsLinked()));
					}
				}
			}

			// TODO ��������� ����� �� ��� �������� ��� TypedCardLinkAttribute
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

}