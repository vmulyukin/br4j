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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.xml.SearchXmlHelper;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;

public class InlineCardLinkAttributeViewer extends JspAttributeViewer {
	private ObjectId labelAttrId = Attribute.ID_NAME;
	private List<Map<String, Object>> replaceAttributes = new ArrayList();	// (YNikitin, 2012/08/08) ���� ��� �������� ���������� ��������� ��������� 
	
	private final static String PARAM_LABEL_ATTR_ID = "labelAttr";
	private final static String PARAM_REPLACE_ATTR_PARAM = "replaceAttrs";	// (YNikitin, 2012/08/08) ������ ���������� ��� ������ ��������� ����� ����� � �������
	private final static String CARD_LABELS_KEY = "cardLabels";
	private final static String PARAM_IS_LINKED = "isLinked";
	public final static String KEY_IS_LINKED = "isLinked";
	
	private boolean isLinked = true;
	public static class CardLabelBean {
		private String label;
		private long cardId;
		
		public CardLabelBean(long cardId, String label) {
			this.cardId = cardId;
			this.label = label;
		}

		public String getLabel() {
			return label;
		}

		public long getCardId() {
			return cardId;
		}
	}

	public InlineCardLinkAttributeViewer() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/InlineCardLinkViewer.jsp");
		fullRendering = false;
	}
	
	public void initEditor(PortletRequest request, Attribute attr)
			throws DataException {
		super.initEditor(request, attr);
		loadAttributeValues(attr, request);
	}

	public void loadAttributeValues(Attribute attr, PortletRequest request) {
		CardLinkAttribute ca = (CardLinkAttribute)attr;
		CardPortletSessionBean sessionBean = getCardPortletSessionBean(request);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		List cardLabels = new ArrayList(ca.getLinkedCount());
		cardInfo.setAttributeEditorData(attr.getId(), CARD_LABELS_KEY, cardLabels);
		cardInfo.setAttributeEditorData(attr.getId(), KEY_IS_LINKED, new Boolean(isLinked));
		
		if (attr.isEmpty()) {
			return;
		}
		
		Search search = new Search();
		search.setByCode(true);
		search.setWords(ca.getLinkedIds());
		List columns = new ArrayList(2);
		SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(labelAttrId);
		columns.add(col);
		col = new SearchResult.Column();
		col.setAttributeId(Card.ATTR_TEMPLATE);
		columns.add(col);
		for(Map<String, Object> replaceAttribute:replaceAttributes){
			String attrId = (String)replaceAttribute.get("attrId");
			String fullReplaceAttrId = (String)replaceAttribute.get("fullReplaceAttrId");
			ObjectId replaceStatusId = (ObjectId)replaceAttribute.get("replaceStatusId");
			String fullLabelAttrId = "";

			SearchResult.Column replaceCol = new SearchResult.Column();
			// if (caption.length() > 1)
			final String[] pathPcs = attrId.split(Attribute.LABEL_ATTR_PARTS_SEPARATOR);
			replaceCol.setAttributeId(SearchXmlHelper.safeMakeId(pathPcs[0].trim()));
			if (pathPcs.length > 1){
				if (replaceCol.getPathToLabelAttr() == null)
					replaceCol.setPathToLabelAttr(new ArrayList());
				replaceCol.getPathToLabelAttr().clear();
				for (int i=1; i<=pathPcs.length-2; i++){
					replaceCol.getPathToLabelAttr().add(SearchXmlHelper.safeMakeId(pathPcs[i].trim()));
					fullLabelAttrId = fullLabelAttrId+SearchXmlHelper.safeMakeId(pathPcs[i].trim())+Attribute.LABEL_ATTR_PARTS_SEPARATOR;
				}
				replaceCol.setLabelAttrId(SearchXmlHelper.safeMakeId(pathPcs[pathPcs.length-1].trim()));
				fullLabelAttrId = fullLabelAttrId+SearchXmlHelper.safeMakeId(pathPcs[pathPcs.length-1].trim());
			} else {
				replaceCol.setLabelAttrId( null);
				replaceCol.setPathToLabelAttr(null);
			}
			
			replaceCol.setFullLabelAttrId(fullLabelAttrId);	// ��������� ����� � ������� �������� �������� ATTR_LABEL_ATTR_ID � ����� �������� 

			replaceCol.setFullReplaceAttrId(fullReplaceAttrId);
			replaceCol.setReplaceStatusId(replaceStatusId);
			columns.add(replaceCol);
		}
		search.setColumns(columns);
		
		try {
			DataServiceBean serviceBean = sessionBean.getServiceBean(); 
			SearchResult res = (SearchResult)serviceBean.doAction(search);
			Iterator i = res.getCards().iterator();
			while (i.hasNext()) {
				Card c = (Card)i.next();
				Long cardId = (Long)c.getId().getId();
				Iterator<SearchResult.Column> resColumns = res.getColumns().iterator();
				while( resColumns.hasNext()){ 
					// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
					// (YNikitin, 2012/08/06) ��� ��� ������ ��������� ��������� �� ���������
					SearchResult.Column originalColumn = resColumns.next();
					String originalColumnName = "";
					SearchResult.Column column = null;
					if (originalColumn.isReplaceAttribute()){	// �� ��������, �� ������� ���� �������� ������, � ������ ���� �� ������������ 
						continue;
					}
					
					column = SearchResult.getRealColumnForCardIfItReplaced(originalColumn, c, res.getColumns());	// �������� ������������ ������� �� ���������� (���� � ������ ��� ���������, �� ������������ ���� �������)
					// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
					Attribute labelAttr = null;
					if (column.getLabelAttrId()==null){
						// (fix) Avoid NULL Pointer exception and log problem attribute id.
						labelAttr = chkGetAttr(c, column.getAttributeId());
					} else {
						final List<Card> labelCards = SearchResult.getCardsListForLabelColumn(
								res.getLabelColumnsForCards(), column);
						for (Card card: labelCards){
							if (card.getId().getId().equals(c.getId().getId())){		// ������� �������������� �������� ����� ��������
								// (fix) Avoid NULL Pointer exception and log problem attribute id.
								labelAttr = chkGetAttr( card, column.getAttributeId() );
								break;
							}
						}
					}
					cardLabels.add(
						new CardLabelBean(
							cardId.longValue(), 
							labelAttr == null ? "" : labelAttr.getStringValue()
						)
					);
					break;
				}
			}
		} catch (Exception e) {
			logger.error("Exception caught while loading editor values for attribute " + attr.getId().getId(), e);
		}
	}

	protected Map getReferenceData(Attribute attr, PortletRequest request)
			throws PortletException {
		Map res = super.getReferenceData(attr, request);
		CardPortletSessionBean sessionBean = getCardPortletSessionBean(request);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		res.put("cardLabels", cardInfo.getAttributeEditorData(attr.getId(), CARD_LABELS_KEY));
		return res;
	}

	public void setParameter(String name, String value) {
		if (PARAM_LABEL_ATTR_ID.equals(name)) {
			String[] parts = value.split(":");
			if (parts.length != 2) {
				throw new IllegalArgumentException("Couldn't parse string: '" + value + "'");
			}
			Class clazz = SearchXmlHelper.getAttrClass(parts[0]);
			labelAttrId = ObjectIdUtils.getObjectId(clazz, parts[1], false);
		} 
		else if (PARAM_IS_LINKED.equalsIgnoreCase(name)){
			isLinked = Boolean.getBoolean(value);
		// (YNikitin, 2012/08/08) ��������� ���������� ��� ������
		} else if (PARAM_REPLACE_ATTR_PARAM.equalsIgnoreCase(name)){
			// ��������� ��������� ��������� ;
			String[] params = value.split(";");
			for (String param: params){
				String[] parts = param.split(",");
				/* ������ �������� ������� �� 3-� ������
				 * 1. ������ �������� �������, �� ������� ����� ������ (����������� "->" � ������ ���� ����)
				 * 2. ������ �������� �������, ������� ���� �������� (����������� "->" � ������ ���� ����), ������ ������� � ���� �������-����
				 * 3. id �������, ��� ������� ���� ���������� ������
				 */
				if (parts.length != 3) {
					throw new IllegalArgumentException("Couldn't parse string: '" + value + "'");
				}
				
				Map<String, Object> replaceAttribute = new HashMap();
				replaceAttribute.put("attrId", parts[0].trim());
				replaceAttribute.put("fullReplaceAttrId", parts[1].trim());
				replaceAttribute.put("replaceStatusId", ObjectIdUtils.getObjectId(CardState.class, parts[2].trim(), false));
				replaceAttributes.add(replaceAttribute);
			}
		} else {
			super.setParameter(name, value);
		}
	}

	private Attribute chkGetAttr(Card card, ObjectId id)
	{
		final Attribute result = card.getAttributeById(id);
		if (result == null || result.getId() == null || result.getId().getId() == null) {
			logger.warn("No attribute inside card "+ card.getId() 
					+ ": getAttribute( "
					+ id + ") returns " 
					+ ( (result == null) ? "NULL" : result.getId() )
				);
		} 
		return result;
	}
	
}