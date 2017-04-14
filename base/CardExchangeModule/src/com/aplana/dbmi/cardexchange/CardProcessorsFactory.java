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
package com.aplana.dbmi.cardexchange;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aplana.dbmi.ConfigService;
import com.aplana.dbmi.Portal;
import com.aplana.dbmi.cardexchange.service.CardExchangeException;
import com.aplana.dbmi.cardexchange.xml.AttributeXMLHandler;
import com.aplana.dbmi.cardexchange.xml.AttributeXMLValue;
import com.aplana.dbmi.cardexchange.xml.CardExchangeUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.ProcessorBase;

public class CardProcessorsFactory {
	private final static Log logger = LogFactory.getLog(CardProcessorsFactory.class);
	private final static String DELIM = "#";
	private static Map cardProcessors = new HashMap();
	
	private synchronized static List getCardProcessors(String confFile) throws CardExchangeException {
		confFile = confFile.trim();
		List result = (List)cardProcessors.get(confFile); 
		if (result == null) {
			result = loadConfig(confFile);
			cardProcessors.put(confFile, result);
		}
		return result;
	}
	
	private static List loadConfig(String confFile) throws CardExchangeException {
		List cardProcessors = new ArrayList();
		logger.info("Loading card processors configuration from file: " + confFile);
		ConfigService cs = Portal.getFactory().getConfigService();
		try {
			InputStream configFile = cs.loadConfigFile(confFile);
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = documentBuilder.parse(configFile);
			Element root = doc.getDocumentElement();
			NodeList nList = root.getElementsByTagName("processor");
			for (int i = 0; i < nList.getLength(); ++i) {
				Element processorElem = (Element)nList.item(i);
				Element conditionElem = (Element)processorElem.getElementsByTagName("condition").item(0);
				NodeList children = conditionElem.getElementsByTagName("template");
				Set templates = new HashSet(children.getLength());
				for (int j = 0; j < children.getLength(); ++j) {
					ObjectId templateId = CardExchangeUtils.getObjectId(Template.class, children.item(j).getTextContent(), true);
					templates.add(templateId);
				}
				children = conditionElem.getElementsByTagName("state");
				Set states = new HashSet(children.getLength());
				for (int j = 0; j < children.getLength(); ++j) {
					ObjectId stateId = CardExchangeUtils.getObjectId(CardState.class, children.item(j).getTextContent(), true);
					states.add(stateId);
				}
				children = conditionElem.getElementsByTagName("attribute");
				Map attributes = new HashMap(children.getLength());
				for (int j = 0; j < children.getLength(); ++j) {
					Element attrElem = (Element)children.item(j);
					String type = attrElem.getAttribute("type");
					AttributeXMLHandler xmlType = AttributeXMLHandler.getXmlType(type);
					if (xmlType == null) {
						logger.warn("Ignoring unknown attribute type: " + type);
						continue;
					}					
					ObjectId attrId = CardExchangeUtils.getObjectId(xmlType.getType(), attrElem.getAttribute("code"), false);
					AttributeXMLValue xmlValue = new AttributeXMLValue(attrElem.getTextContent());
					attributes.put(attrId, xmlValue);
				}
				children = processorElem.getElementsByTagName("parameter");
				Map parameters = new HashMap(children.getLength());
				for (int j = 0; j < children.getLength(); ++j) {
					Element paramElem = (Element)children.item(j);
					parameters.put(paramElem.getAttribute("name"), paramElem.getAttribute("value"));
				}
				
				CardProcessorConfig cfg = new CardProcessorConfig();
				cfg.setClassName(processorElem.getAttribute("class"));
				cfg.setTemplates(templates);
				cfg.setStates(states);
				cfg.setAttributes(attributes);
				cfg.setParameters(parameters);
				cardProcessors.add(cfg);
			}
			return cardProcessors;
		} catch (Exception e) {
			logger.error("Error occured while loading card processor configuration", e);
			throw new CardExchangeException("cardProcessor.confFile.load.error", new Object[] {confFile, e.getMessage()});
		}		
	}

	public static List getCardProcessors(String confFile, Card card) throws DataException {
		List result = new ArrayList();
		Iterator i = getCardProcessors(confFile).iterator();
		while (i.hasNext()) {
			CardProcessorConfig cfg = (CardProcessorConfig)i.next();
			if (cfg.isFeasibleForCard(card)) {
				try {
					ProcessorBase processor = (ProcessorBase)Class.forName(cfg.getClassName()).newInstance();
					if (processor instanceof Parametrized) {
						Parametrized proc = (Parametrized)processor;
						Iterator j = cfg.getParameters().entrySet().iterator();
						while (j.hasNext()) {
							Map.Entry param = (Map.Entry) j.next();
							proc.setParameter((String) param.getKey(), (String) param.getValue());
						}
					}
					result.add(processor);
				} catch (Exception e) {
					throw new DataException(e);
				}
			}
		}
		return result;
	}
}
