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
package com.aplana.dbmi.module.archive;

import com.aplana.dbmi.action.*;
import com.aplana.dbmi.action.Search.ExistAttribute;
import com.aplana.dbmi.archive.ArchiveConfig;
import com.aplana.dbmi.archive.ArchiveConfigReader;
import com.aplana.dbmi.archive.AttributeValueArchiveValue;
import com.aplana.dbmi.archive.CardArchiveValue;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.module.DataServiceBeanPartLoader;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.client.MaterialStream;
import com.aplana.dbmi.service.impl.query.AttributeUtils;
import com.aplana.dbmi.task.AbstractTask;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.*;

public class ArchiveRemovedCardsFromXML extends AbstractTask {

	private static final long serialVersionUID = 1L;
	
	private static final ObjectId CARD_STATE_DELO = ObjectId.predefined(CardState.class, "delo");
	
	private static final ObjectId ATTR_SAVED_CARD = ObjectId.predefined(CardLinkAttribute.class, "jbr.saved.card");
	
	protected final Log logger = LogFactory.getLog(getClass());

    private static Boolean working = false;
	
	public void process(Map<?, ?> parameters) {
        synchronized (ArchiveRemovedCardsFromXML.class) {
            if (working) {
                logger.warn("Process is already working. Skipping.");
                return;
            }
            working = true;
        }
        logger.info(getClass() + " TASK started");
        try {
            processArchive(serviceBean);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            synchronized (ArchiveRemovedCardsFromXML.class) {
                working = false;
                logger.info(getClass() + " TASK finished");
            }
        }
    }
	
	private void processArchive(DataServiceBean service) throws DataException, ServiceException {
		
		final Set<ArchiveConfig> archiveConfig = ArchiveConfigReader.getArchiveConfigSet();
		
		if(CollectionUtils.isEmpty(archiveConfig)) {
			return;
		}
		
		Search search = new Search();
		search.setByAttributes(true);
		search.setColumns(CardUtils.createColumns(Card.ATTR_ID, ATTR_SAVED_CARD));
		search.setStates(Collections.singleton(CARD_STATE_DELO));
		search.addAttribute(ATTR_SAVED_CARD, ExistAttribute.INSTANCE);
		
		final List<DataObject> templates = new ArrayList<DataObject>(1);
		ObjectId template;
		for(ArchiveConfig ac : archiveConfig) {
			try {
				template = new ObjectId(Template.class, ac.getTemplate());
				templates.add(DataObject.createFromId(template));
			} catch (Exception e) {
				logger.error("Bad template id: " + ac.getTemplate());
			}	
		}
		search.setTemplates(templates);
		
		List<Card> list = CardUtils.getCardsList((SearchResult) service.doAction(search));
		
		if(CollectionUtils.isEmpty(list)) {
			return;
		}
		
		CardLinkAttribute clAttr;
		List<Card> materialCards;
		Card materialCard;
		MaterialAttribute mAttribute;
		MaterialStream materialStream = null;
		for(Card card : list) {
			clAttr = card.getCardLinkAttributeById(ATTR_SAVED_CARD);
			if(AttributeUtils.isEmpty(clAttr)) {
				continue;
			}
			search = new Search();
			search.setByCode(true);
			search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(clAttr.getIdsLinked()));
			search.setColumns(CardUtils.createColumns(Card.ATTR_ID, Attribute.ID_MATERIAL));
			materialCards = CardUtils.getCardsList((SearchResult) service.doAction(search));
			if(CollectionUtils.isEmpty(materialCards)) {
				continue;
			}
			materialCard = materialCards.get(materialCards.size() - 1);
			mAttribute = (MaterialAttribute) materialCard.getAttributeById(Attribute.ID_MATERIAL);
			if (mAttribute.getMaterialName() != null && mAttribute.getMaterialName().length() > 0) {
				try {
					materialStream = getMaterial(service, materialCard.getId(), 0);
					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(materialStream);
					List<CardArchiveValue> cardArchiveValues = parseDocument(doc, archiveConfig, card.getId());
					
					AddToArchive action = new AddToArchive();
					action.setModelValues(cardArchiveValues);
					service.doAction(action);

				} catch(Exception e) {
					logger.warn("Error loading data from filestorage for file " + materialCard.getId());
					logger.debug(e);
				} finally {
					if(materialStream != null) {
						try {
							materialStream.close();
						} catch (IOException e) {
							logger.error("Error closing configuration file InputStream", e);
						}
					}
				}
				/*Document doc = null;
				try {
					doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(materialStream);			
				} catch (Exception e) {
					logger.error("Error initializing attributes configuration", e);
				} finally {
					if(materialStream != null) {
						try {
							materialStream.close();
						} catch (IOException e) {
							logger.error("Error closing configuration file InputStream", e);
						}
					}
				}*/
			}
		}
	}
	
	private List<CardArchiveValue> parseDocument(final Document doc, final Set<ArchiveConfig> archiveConfigs, final ObjectId cardId) {
		//Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ms);
		List<CardArchiveValue> list = new ArrayList<CardArchiveValue>();
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = null;
		CardArchiveValue cav = null;
		try {
			xpath = factory.newXPath();
			String expression = "/archiveCard/card";
			
			Node mainCard = ((NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET)).item(0);
			
			Double id = (Double) xpath.evaluate("@id", mainCard, XPathConstants.NUMBER);
			Double template = (Double) xpath.evaluate("@template", mainCard, XPathConstants.NUMBER);
			
			if(id == null || template == null) {
				return null;
			}
			
			ArchiveConfig archiveConfig = null;
			for(ArchiveConfig ac : archiveConfigs) {
				if(ac.getTemplate().equals(template.longValue())) {
					archiveConfig = ac;
					break;
				}
			}
			
			if(archiveConfig == null) {
				return null;
			}
			
			cav = new CardArchiveValue();
			cav.setCardId(id.longValue());
			cav.setTemplateId(template.longValue());
			cav.setStatusId((Long) CARD_STATE_DELO.getId());

			Set<AttributeValueArchiveValue> attrValues = getAttributesData(xpath, mainCard, "blocks/block/attributes/attr", archiveConfig.getAttributes());
			
			cav.setAttrValues(attrValues);
			list.add(cav);
			
			recursiveFillChildrenCardsData(xpath, mainCard, archiveConfig, expression, list);
			
			return list;
		} catch(XPathExpressionException e) {
			logger.warn("Error parsing xml document for card " + cardId);
			logger.debug(e);
			return null;
		}
	}
	
	private void recursiveFillChildrenCardsData(final XPath xpath, final Node cardNode, final ArchiveConfig archiveConfig, 
			String expression, List<CardArchiveValue> list) throws XPathExpressionException {
		
		expression = "blocks/block/attributes/attr/values/card";
		
		NodeList nl = (NodeList) xpath.evaluate(expression, cardNode, XPathConstants.NODESET);
		
		Node node;
		Double id;
		Double mainCardId;
		Double template;
		Double state;
		CardArchiveValue cav;
		Set<AttributeValueArchiveValue> attrValues;
		for(int i = 0; i < nl.getLength(); i++) {
			if(nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
				node = (Node) nl.item(i);
		
				id = (Double) xpath.evaluate("@id", node, XPathConstants.NUMBER);
				mainCardId = (Double) xpath.evaluate("@id", cardNode, XPathConstants.NUMBER);
				template = (Double) xpath.evaluate("@template", node, XPathConstants.NUMBER);
				state = (Double) xpath.evaluate("@state", node, XPathConstants.NUMBER);
		
				if(id == null || template == null || state == null) {
					continue;
				}
				
				Set<ObjectId> attrs = null;
				for(Long tmplt : archiveConfig.getChildren().keySet()) {
					if(tmplt.equals(template.longValue())) {
						attrs = archiveConfig.getChildren().get(tmplt);
						break;
					}
				}
				
				if(attrs == null) {
					continue;
				}
				
				cav = new CardArchiveValue();
				cav.setCardId(id.longValue());
				cav.setTemplateId(template.longValue());
				cav.setStatusId(state.longValue());
				
				if(mainCardId != null) {
					cav.setParentCardId(mainCardId.longValue());
				}

				attrValues = getAttributesData(xpath, node, "blocks/block/attributes/attr", attrs);
		
				cav.setAttrValues(attrValues);
				list.add(cav);
				
				recursiveFillChildrenCardsData(xpath, node, archiveConfig, expression, list);
			}
		}
	}
	
	private Set<AttributeValueArchiveValue> getAttributesData(final XPath xpath, final Node attrNode, final String expression, 
			final Set<ObjectId> attrIds) throws XPathExpressionException {
		NodeList nl = (NodeList) xpath.evaluate(expression, attrNode, XPathConstants.NODESET);
		
		Set<AttributeValueArchiveValue> attrValuesCommon = new HashSet<AttributeValueArchiveValue>();
		Node node;
		String attrCode;
		String type;
		for(int i = 0; i < nl.getLength(); i++) {
			if(nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
				node = (Node) nl.item(i);
				type = (String) xpath.evaluate("@type", node, XPathConstants.STRING);
				attrCode = (String) xpath.evaluate("@code", node, XPathConstants.STRING);
				if(type == null || attrCode == null
						|| Attribute.TYPE_BACK_LINK.equals(type)
						|| Attribute.TYPE_CARD_HISTORY.equals(type)) {
					continue;
				}
				Set<AttributeValueArchiveValue> attrValues;
				for(ObjectId objId : attrIds) {
					if(attrCode.equalsIgnoreCase((String) objId.getId())) {
 
						attrValues = setAttrValue(xpath, node, type, objId);
						
						if(CollectionUtils.isEmpty(attrValues)) {
							break;
						}
						
						attrValuesCommon.addAll(attrValues);
						break;
					}
				}
			}
		}
		
		return attrValuesCommon;
	}
	
	private Set<AttributeValueArchiveValue> setAttrValue(final XPath xpath, final Node node, String type, ObjectId objId) 
		throws XPathExpressionException {
		
		List<String> values = getValue(xpath, node);
		
		if(CollectionUtils.isEmpty(values)) {
			return null;
		}
		
		Set<AttributeValueArchiveValue> attrValues = new HashSet<AttributeValueArchiveValue>();
		AttributeValueArchiveValue attrValue;
		
		for(String value : values) {
			
			attrValue = new AttributeValueArchiveValue();
			
			attrValue.setAttributeCode(objId);
		
			if(Attribute.TYPE_CARD_LINK.equals(type)
					|| Attribute.TYPE_TYPED_CARD_LINK.equals(type)
					|| Attribute.TYPE_DATED_TYPED_CARD_LINK.equals(type)
					|| Attribute.TYPE_PERSON.equals(type)
					|| Attribute.TYPE_INTEGER.equals(type)
					|| Attribute.TYPE_LONG.equals(type)) {
				attrValue.setNumberValue(Long.valueOf(value));
			} else if(Attribute.TYPE_STRING.equals(type)
					|| Attribute.TYPE_TEXT.equals(type)) {
				attrValue.setStringValue(value);
			} else if(Attribute.TYPE_DATE.equals(type)) {
				attrValue.setDateValue(new Date(Long.valueOf(value)));
			} else if(Attribute.TYPE_LIST.equals(type)
					|| Attribute.TYPE_TREE.equals(type)) {
				attrValue.setValueId(Long.valueOf(value));
			} else if(Attribute.TYPE_HTML.equals(type)) {
				attrValue.setLongBinaryValue(value);
			} else {
				continue;
			}
			attrValues.add(attrValue);
		}
		return attrValues;
	}
	
	private List<String> getValue(XPath xpath, Node node) throws XPathExpressionException {
		List<String> values = null;
		String value = (String) xpath.evaluate("@value", node, XPathConstants.STRING);
		if(!StringUtils.isEmpty(value)) {
			return Collections.singletonList(value);
		}
		
		NodeList nl = (NodeList) xpath.evaluate("values/value", node, XPathConstants.NODESET);
		if(nl != null && nl.getLength() > 0) {
			Node n;
			values = new ArrayList<String>();
			for(int i = 0; i < nl.getLength(); i++) {
				if(nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
					n = (Node) nl.item(i);
					value = (String) xpath.evaluate("text()", n, XPathConstants.STRING);
					if(!StringUtils.isEmpty(value)) {
						values.add(value);
					}
				}
			}
			return values;
		}
		
		nl = (NodeList) xpath.evaluate("values/card", node, XPathConstants.NODESET);
		if(nl != null && nl.getLength() > 0) {
			Node n;
			values = new ArrayList<String>();
			for(int i = 0; i < nl.getLength(); i++) {
				if(nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
					n = (Node) nl.item(i);
					value = (String) xpath.evaluate("@id", n, XPathConstants.STRING);
					if(!StringUtils.isEmpty(value)) {
						values.add(value);
					}
				}
			}
			return values;
		}

		return values;
	}
	
	private MaterialStream getMaterial(DataServiceBean service, ObjectId cardId, int versionId) throws DataException, ServiceException {
		DownloadFile download = new DownloadFile();
		download.setCardId(cardId);
		Material material = (Material) service.doAction(download);
		if (material == null)
			return null;
		return new MaterialStream(material.getLength(), new DataServiceBeanPartLoader(service, cardId, versionId,
				material.getUrl()));
	}

}
