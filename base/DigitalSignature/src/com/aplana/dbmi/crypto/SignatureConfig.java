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
/**
 * 
 */
package com.aplana.dbmi.crypto;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.util.AttributeUtil;

/**
 * @author nzeltser
 * ������ ����������������� �����
 * ������ �� ������, ��������� � �����
 *
 */
public class SignatureConfig {
	public static final String CONFIG_FOLDER = "dbmi/card/signature";
	public static final String CONFIG_FILE = CONFIG_FOLDER + "/signAttributes.xml";
	public static final String CURRENTCARD_MAPKEY = "0";
	private static final String TAG_TEMPLATE = "template";	
	private static final String TAG_STATE = "state";
	private static final String TAG_STATETO = "stateto";	
	public static final String ATTR_ID = "id";
	private static final String ATTR_STATE = "status";
	public static final String ATTR_TYPE = "type";
	private static final String TAG_ROOT = "attributes";
	private static final String TAG_ATTR = "attribute";
	private static final String TAG_LINK = "link";
	private static final String ANY_VALUE = "*";
	protected final Log logger = LogFactory.getLog(getClass());
	private List<CardSignAttributes> resultMap = null;
	private DataServiceBean serviceBean = null;
	
	public SignatureConfig(DataServiceBean serviceBean, Card contextCard){
		Document doc;
		this.serviceBean = serviceBean;	
		resultMap = new ArrayList<SignatureConfig.CardSignAttributes>();				
		String objIdStr = "";
		
		//Card contextCard = sessionBean.getActiveCard();
		
		try {								
			doc = initFromXml(Portal.getFactory().getConfigService().loadConfigFile(CONFIG_FILE));
			
			Element root = doc.getDocumentElement();
					
			NodeList allTmplList = root.getElementsByTagName(TAG_TEMPLATE);			
			for (int a = 0; a < allTmplList.getLength(); a++){
				Element tmplNode = (Element)allTmplList.item(a);
				if(checkId(Template.class, tmplNode.getAttribute(ATTR_ID), contextCard.getTemplate())){
					collectAttributes(tmplNode, contextCard, true);
				}				
			}					
			
		} catch (Exception e) {
			logger.error("Can't read signature attributes configuration", e);
		}
	}
	public SignatureConfig(){
		resultMap = new ArrayList<SignatureConfig.CardSignAttributes>();
	}
	
	private Document initFromXml(InputStream configFile) {
		Document doc = null;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configFile);			
		} catch (Exception e) {
			logger.error("Error initializing signature attributes configuration", e);
		}
		return doc;
	}
	
	private void collectAttributes(Element aNode, Card card, boolean isContextCard){
		//resultMap	 
		String mapValue = "";
		try {
			NodeList childs = aNode.getChildNodes();			
			for (int a = 0; a < childs.getLength(); a++){
				if(childs.item(a).getNodeName().equals("#text") == false &&
						childs.item(a).getNodeName().equals("#comment") == false){
					Element child = (Element)childs.item(a);
					if(child.getTagName().equalsIgnoreCase(TAG_ATTR)){
						//�������� ����� �������� � map
						if(isContextCard){
							addAttrToMap(aNode,child);
						}else{
							addAttrToMap(aNode,child, card.getId());
						}
					}else if(child.getTagName().equalsIgnoreCase(TAG_LINK)){
						String linkType = child.getAttribute(ATTR_TYPE);
						if(linkType.equalsIgnoreCase("B")){
							//�� �������� ����� ��������
							ObjectId blId = ObjectIdUtils.getObjectId(BackLinkAttribute.class, child.getAttribute(ATTR_ID), false);
							ObjectId linkedCardId = getCardIdFromBackLink(card.getId(), blId);
							Card linkedCard = (Card)serviceBean.getById(linkedCardId);
							if(linkedCard != null){
								collectAttributes(child, linkedCard, false);
							}
						}else{
							//�� ��������� ����� ��� �������� � ������� ���������
							ObjectId clId = ObjectIdUtils.getObjectId(CardLinkAttribute.class, child.getAttribute(ATTR_ID), false);
							CardLinkAttribute clAttr = (CardLinkAttribute)card.getAttributeById(clId);
							if(clAttr != null && clAttr.getIdsLinked() != null){
								Search search = new Search();
								
								search.setByCode(true);
								search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(ObjectIdUtils.collectionToSetOfIds(clAttr.getIdsLinked())));
								
								if(child.hasAttribute(ATTR_STATE) && child.getAttribute(ATTR_STATE).length()>0){
									Collection states = ObjectIdUtils.commaDelimitedStringToNumericIds(child.getAttribute(ATTR_STATE), CardState.class);
									search.setStates(states);	
								}					
		
								final Collection<Card> cards = ((SearchResult)serviceBean.doAction(search)).getCards();
								if (cards.size() > 0){
									final Iterator<Card> iter = cards.iterator();
									while (iter.hasNext()) {
										Card linkedCard = (Card) serviceBean.getById(iter.next().getId());
										collectAttributes(child, linkedCard, false);								
									}
								}
							}
							
						}
					}
				}
			}	
		} catch (Exception e) {
			logger.error("Error while collecting attributes for sign", e);		
		}
	}
	private void addAttrToMap(Element parentNode, Element attrNode){
		addAttrToMap(parentNode, attrNode,null);
	}
	public void addAttrToMap(Element parentNode, Element attrNode, ObjectId cardId){
		String mapKey = CURRENTCARD_MAPKEY; 
		if(cardId != null){
			mapKey = cardId.getId().toString();
		}
		
		ObjectId attrId = AttributeUtil.makeAttributeId(attrNode.getAttribute(ATTR_TYPE), attrNode.getAttribute(ATTR_ID)); 
		attrId = ObjectIdUtils.getObjectId(attrId.getType(), attrId.getId().toString(), false);
		
		CardSignAttributes cardSignAttributes = new CardSignAttributes(mapKey);
		
		if(resultMap.contains(cardSignAttributes)){
			for(CardSignAttributes existSignAtts: resultMap){
				if(existSignAtts.equals(cardSignAttributes)){
					existSignAtts.add(attrId);
					break;
				}
			}
		} else {
			cardSignAttributes.add(attrId);
			resultMap.add(cardSignAttributes);
		}	
	}
	
	private ObjectId getCardIdFromBackLink(ObjectId cardId, ObjectId attrId) {
		ObjectId linkedId = null;
		try {
			ListProject search = new ListProject();
			search.setAttribute(attrId);
			search.setCard(cardId);
			final SearchResult result = (SearchResult) serviceBean.doAction(search);
			final Iterator<Card> itr = result.getCards().iterator();
			Card linked = itr.next();
			linkedId = linked.getId();
		} catch (Exception e) {
			logger.error("Error in get cardId from backlink: " + e );
		}
		return linkedId;
	}	
		
	private boolean checkId(Class className, String id, ObjectId compareId){
		ObjectId idObj = null;
		
		if(id.equalsIgnoreCase(ANY_VALUE)){
			 return true;
		}
		if(className.equals(Template.class) || className.equals(Card.class)){
			idObj = ObjectIdUtils.getObjectId(className, id, true);
		}else{
			idObj = ObjectIdUtils.getObjectId(className, id, false);
		}
				
		return idObj.equals(compareId);
	}
	
	public List<CardSignAttributes> getAttributesMap(){
		return resultMap;
	}
	
	public class CardSignAttributes {
		private String cardId;
		private Vector<ObjectId>  attrArr = new Vector<ObjectId>();
		
		public CardSignAttributes (String cardId){
			this.cardId = cardId;
		}
		
		public void add(ObjectId attrId) {
			attrArr.add(attrId);
			
		}

		public String getCardId() {
			return cardId;
		}
		public void setCardId(String cardId) {
			this.cardId = cardId;
		}
		public Vector<ObjectId> getAttrArr() {
			return attrArr;
		}
		public void setAttrArr(Vector<ObjectId> attrArr) {
			this.attrArr = attrArr;
		}
		
		@Override
		public int hashCode() {
			return cardId.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj  instanceof CardSignAttributes){
				return cardId.equals(((CardSignAttributes)obj).getCardId());
			} else {
				return false;
			}
		}
		
	}

}
