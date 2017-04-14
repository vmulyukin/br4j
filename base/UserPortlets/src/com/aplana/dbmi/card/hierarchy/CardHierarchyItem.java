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
package com.aplana.dbmi.card.hierarchy;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.management.RuntimeErrorException;

import com.aplana.dbmi.model.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.CertificateInfo;
import com.aplana.dbmi.card.hierarchy.descriptor.StylingDescriptor;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class CardHierarchyItem extends HierarchyItem {
	private Card card;
	private Attribute labelAttr;
	private boolean labelAsLink;
	private boolean labelAsDownloadLink;
	private String labelFormat;
	private int labelMaxLength = 0;
	private List children;
	private List infoItems;
	private List actions;
	private String columnsKey;
	private boolean checked;
	private DataServiceBean serviceBean;
	private boolean checkChildren;
	private boolean terminalNodesOnly;
	private boolean showOrg;
	
	private static final ObjectId PERS_LAST_NAME_ATTR_ID = ObjectId.predefined(
			StringAttribute.class, "jbr.person.lastName");
	private static final ObjectId PERS_FIRST_NAME_ATTR_ID = ObjectId.predefined(
			StringAttribute.class, "jbr.person.firstName");
	private static final ObjectId PERS_MIDDLE_NAME_ATTR_ID = ObjectId.predefined(
			StringAttribute.class, "jbr.person.middleName");
	private static final ObjectId PERS_ORG_ATTR_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.person.organization");
	private static final ObjectId ATTR_ORG_SHORTNAME = ObjectId.predefined(StringAttribute.class, "jbr.organization.shortName");
	
	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public List getActions() {
		return actions;
	}

	public void setActions(List actions) {
		this.actions = actions;
	}

	public CardHierarchyItem(Hierarchy hierarchy, DataServiceBean serviceBean) {
		super(hierarchy);
		this.serviceBean = serviceBean;
	}
	
	public Card getCard() {
		return card;
	}

	public int getLabelMaxLength() {
		return labelMaxLength;
	}

	public void setLabelMaxLength(int labelMaxLength) {
		this.labelMaxLength = labelMaxLength;
	}

	public void setCard(Card card) {
		this.card = card;
	}
	
	public Attribute getLabelAttr() {
		return labelAttr;
	}
	
	public void setLabelAttr(Attribute labelAttr) {
		this.labelAttr = labelAttr;
	}
	
	public boolean isLabelAsLink() {
		return labelAsLink;
	}

	public void setLabelAsLink(boolean labelAsLink) {
		this.labelAsLink = labelAsLink;
	}

	public String getLabelFormat() {
		return labelFormat;
	}

	public void setLabelFormat(String labelFormat) {
		this.labelFormat = labelFormat;
	}
	
	public boolean isShowOrg() {
		return showOrg;
	}

	public void setShowOrg(boolean showOrg) {
		this.showOrg = showOrg;
	}

	/**
	 * @return list of {@link HierarchicalCardList } items representing set of children cards
	 */
	public List getChildren() {
		return children;
	}
	
	/**
	 * list of {@link HierarchicalCardList } items
	 * @param children
	 */
	public void setChildren(List children) {
		this.children = children;
	}
	public boolean isChildrenLoaded() {
		return children != null;
	}
	
	public List getInfoItems() {
		return infoItems;
	}

	public void setInfoItems(List infoItems) {
		this.infoItems = infoItems;
	}
	
	public boolean isInfoItemsLoaded() {
		return infoItems != null;
	}

	public String getColumnsKey() {
		return columnsKey;
	}

	public void setColumnsKey(String columnsKey) {
		this.columnsKey = columnsKey;
	}
	
	public boolean isCheckChildren(){
		return checkChildren;
	}
	
	public void setCheckChildren(boolean checkChildren){
		this.checkChildren = checkChildren;
	}

	public String toString() {
		return "CardId = " + card.getId();
	}
	
	public boolean isTerminalNodesOnly() {
		return terminalNodesOnly;
	}

	public void setTerminalNodesOnly(boolean terminalNodesOnly) {
		this.terminalNodesOnly = terminalNodesOnly;
	}

	public JSONObject toJSONObject() throws JSONException {
		JSONObject jo = super.toJSONObject();
		jo.put("cardId", card.getId().getId());
		if (card.getTemplate()!=null){
			jo.put("templateId", card.getTemplate().getId());
		}
		String label = null;
		if (labelAttr != null) {
			if (labelAttr instanceof PersonAttribute){
				Collection<Person> persCards = ((PersonAttribute)labelAttr).getValues();
				try{
                    StringBuilder stringBuilder = new StringBuilder();
                    Iterator<Person> i = persCards.iterator();
                    while(i.hasNext()) {
                        Card persCard = (Card) serviceBean.getById(i.next().getCardId());
                        String persFio = getPersonFio(persCard);
                        if (!persFio.isEmpty()) {
                            stringBuilder.append(" ").append(persFio);
                            if (isShowOrg()) {
                                String persOrg = getPersonOrganization(persCard);
                                if (!persOrg.isEmpty()) {
                                    stringBuilder.append(", ").append(persOrg);
                                }
                            }
                            if(i.hasNext()){
                                stringBuilder.append(";");
                            }
                        } else {
                            continue;
                        }
                    }
                    label = stringBuilder.toString();
				}catch(Exception ex) {
					label = labelAttr.getStringValue();
				}
			}else {
				label = labelAttr.getStringValue();
			}
		}else {
			label = ResourceBundle.getBundle("com.aplana.dbmi.card.nl.CardPortletResource", ContextProvider.LOCALE_RUS).getString("cardlink.stub");
		}
		if (labelFormat != null)
			label = MessageFormat.format(labelFormat, new Object[] { label });
		if (labelMaxLength != 0 && (labelMaxLength < label.length()))//restrict label length
			label = label.substring(0, labelMaxLength)+"...";
		jo.put("label", label);
		jo.put("asLink", isLabelAsLink());
		jo.put("asDownloadLink", labelAsDownloadLink);
		jo.put("checkChildren", checkChildren);
		jo.put("terminalNodesOnly", terminalNodesOnly);
		StylingDescriptor sd = hierarchy.getDescriptor().getStylingDescriptor(card); 
		if (sd != null) {
			if (sd.getStyle() != null) {
				jo.put("style", sd.getStyle());
			}
			if (sd.getIconPath() != null) {
				jo.put("icon", sd.getIconPath());
			}
		}
		JSONArray jsonColumns = new JSONArray();
		Iterator i = hierarchy.getDescriptor().getColumns(columnsKey).iterator();
		while (i.hasNext()) {
			SearchResult.Column colDescr = (SearchResult.Column)i.next();
			if (colDescr.getAttributeId().getType().equals(DateAttribute.class)) {
				jsonColumns.put(getDateAttrValue(colDescr));
			} else
				jsonColumns.put(getAttrValue(colDescr.getAttributeId()));
		}
		/*
		JSONObject jsonColumns = new JSONObject();
		Iterator i = hierarchy.getDescriptor().getColumns().iterator();
		while (i.hasNext()) {
			SearchResult.Column colDescr = (SearchResult.Column)i.next();
			jsonColumns.put(
				(String)colDescr.getAttributeId().getId(), 
				getAttrValue(colDescr.getAttributeId())
			);
		}
		*/
		jo.put("columns", jsonColumns);
		jo.put("checked", checked);
		
		JSONArray jsonActions = new JSONArray();
		if (actions != null) {
			i = getActions().iterator();
			while (i.hasNext()) {
				String actionId = (String)i.next();
				jsonActions.put(actionId);
			}
		}
		jo.put("actions", jsonActions);
		
		if (children != null && !children.isEmpty()) {
			JSONArray array = new JSONArray();
			Iterator j = children.iterator();
			while (j.hasNext()) {
				writeHierarchicalCardListToJson(array, (HierarchicalCardList)j.next());
			}
			jo.put("children", array);
		}
		if (infoItems != null && !infoItems.isEmpty()) {
			JSONArray array = new JSONArray();
			Iterator j = infoItems.iterator();
			while (j.hasNext()) {
				writeHierarchicalCardListToJson(array, (HierarchicalCardList) j.next());
			}
			jo.put("infoItems", array);
		}
		
		return jo;
	}
	
	private void writeHierarchicalCardListToJson(JSONArray array, HierarchicalCardList cardList) throws JSONException {
		Iterator j = cardList.getTopLevelItems().iterator();
		while (j.hasNext()) {
			HierarchyItem item = (HierarchyItem)j.next();
			array.put(item.toJSONObject());
		}
	}
	
	private String getAttrValue(ObjectId attrId) {
		Attribute attr = card.getAttributeById(attrId);
		if (attr != null) {
			if (CertificateInfo.SIGNATURE_ATTR_ID.equals(attrId)){
				DateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy");
				Card linkCard = null;
				ResourceBundle bundle = ResourceBundle.getBundle("com.aplana.dbmi.card.nl.CardPortletResource", new Locale("ru")); 
				try{
					linkCard = (Card)serviceBean.getById(card.getId());
				}catch(Exception ex){
					//throw new RuntimeException("Can not get card", ex);
					//����� �. 16.05.2011 ����� �� ���� ����. ����� ���������� ��� �� ���������
					return bundle.getString("sign.none");
				}
				List<CertificateInfo> cerInfo = CertificateInfo.readCertificateInfo(linkCard, serviceBean, bundle, dateFormat);
				if (cerInfo != null && cerInfo.size() > 0){
					boolean valid = true;
					String displayStatus = null;
					for (CertificateInfo certificateInfo : cerInfo) {
						displayStatus = certificateInfo.getSignState(); 
						if (!certificateInfo.isSignValid()){
							valid = false;
							break;
						}
					}
					return displayStatus;
				}else{
					return bundle.getString("sign.none");
				}
			}else{			
				return attr.getStringValue();
			}
		} else {
			return null;
		}
	}
	
	private String getDateAttrValue(SearchResult.Column col) {		
		final Attribute attr = card.getAttributeById(col.getAttributeId());
		if (attr == null)
			return null;
		else if (col.getTimePattern() != null)
			return ((DateAttribute) attr).getStringValue(col.getTimePattern());
		// �� ���������, �.�. defaultTimePattern �������� �� MIShowListPortlet, 
		// � CardProtlet ������������ �������� �� AttributeOption
		/*
		else if (!col.isDefaultTimePatternUsed())
			return attr.getStringValue(); */
		else
			return attr.getStringValue();
	}

	protected String getType() {
		return "card";
	}
	
	public CardHierarchyItem makeCopy() {
		CardHierarchyItem result = new CardHierarchyItem(hierarchy, serviceBean);
		result.setCard(card);
		result.setCollapsed(isCollapsed());
		result.setLabelAttr(labelAttr);
		result.setActions(actions);
		
		result.setChecked(checked);
		result.setLabelAsLink(labelAsLink);
		result.setLabelFormat(labelFormat);
		result.setChildren(children);
		result.setInfoItems(infoItems);
		result.setColumnsKey(columnsKey);
		result.setCheckChildren(checkChildren);
		result.setTerminalNodesOnly(terminalNodesOnly);
		result.setShowOrg(showOrg);
		return result;
	}

	private String getPersonFio(final Card persCard) {
		StringBuilder persNameFio = new StringBuilder();
		StringAttribute persNameAttr = (StringAttribute)persCard.getAttributeById(PERS_LAST_NAME_ATTR_ID);
		if (null != persNameAttr && !persNameAttr.getStringValue().isEmpty()) {
			persNameFio.append(persNameAttr.getStringValue());
			persNameAttr = (StringAttribute)persCard.getAttributeById(PERS_FIRST_NAME_ATTR_ID);
			if (null != persNameAttr && !persNameAttr.getStringValue().isEmpty()) {
				persNameFio.append(" ");
				persNameFio.append(persNameAttr.getStringValue().substring(0, 1));
				persNameFio.append(".");
				persNameAttr = (StringAttribute)persCard.getAttributeById(PERS_MIDDLE_NAME_ATTR_ID);
				if (null != persNameAttr && !persNameAttr.getStringValue().isEmpty()) {
					persNameFio.append(persNameAttr.getStringValue().substring(0, 1));
					persNameFio.append(".");
				}
			}
		}
		return persNameFio.toString();
	}
	
	public String getPersonOrganization(final Card persCard) throws DataException, ServiceException{

		if(persCard == null)
			return "";
		
		CardLinkAttribute orgAttr = (CardLinkAttribute) persCard.getAttributeById(PERS_ORG_ATTR_ID);
		if(orgAttr == null || orgAttr.isEmpty())
			return "";
		
		ObjectId orgCardId = orgAttr.getSingleLinkedId();
		Search search = new Search();
		search.setByCode(true);
		search.setWords(orgCardId.getId().toString());
		SearchResult.Column column = new SearchResult.Column();
		column.setAttributeId(ATTR_ORG_SHORTNAME);
		search.setColumns(Collections.singletonList(column));
		Card orgCard = (Card) ((SearchResult) serviceBean.doAction(search)).getCards().iterator().next();

		if(orgCard == null || orgCard.getAttributeById(ATTR_ORG_SHORTNAME) == null) 
			return "";
		return orgCard.getAttributeById(ATTR_ORG_SHORTNAME).getStringValue();
	}

	public boolean isLabelAsDownloadLink() {
		return labelAsDownloadLink;
	}

	public void setLabelAsDownloadLink(boolean labelAsDownloadLink) {
		this.labelAsDownloadLink = labelAsDownloadLink;
	}
}
