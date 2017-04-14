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
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;

public class DigitalSignatureConfiguration {
	
	public static final String CONFIG_FOLDER = "dbmi/card/signature";
	public static final String CONFIG_FILE = CONFIG_FOLDER + "/signAttributes.xml";
	private static final String TAG_TEMPLATE = "template";	
	private static final String TAG_STATE = "state";
	private static final String TAG_STATETO = "stateto";	
	private static final String ATTR_ID = "id";
	private static final String ATTR_TYPE = "type";
	private static final String TAG_ROOT = "attributes";
	private static final String TAG_ATTR = "attribute";
	private static final String TAG_MOVE = "move";
	private static final String TAG_FROM = "from";
	private static final String TAG_TO = "to";
	private static final String ANY_VALUE = "*";
	
	private static HashMap configuration = null;	
	private static Log logger = LogFactory.getLog(DigitalSignatureConfiguration.class);
	
	public static synchronized HashMap getConfiguration() {
		if(null == configuration) {
			init();
		}
		return configuration;
	}
	
	private static void init() {
		Document doc = null;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
					Portal.getFactory().getConfigService().loadConfigFile(CONFIG_FILE));	
			
			Element root = doc.getDocumentElement(); 
			NodeList templates = root.getElementsByTagName(TAG_TEMPLATE);
			if(templates != null && templates.getLength() > 0) {
				configuration = new HashMap();
				for(int i = 0; i < templates.getLength(); i ++) {
					Node template = templates.item(i);
					Template templateConfig = new Template();
					String idStr = template.getAttributes().getNamedItem(ATTR_ID).getNodeValue();
					templateConfig.setId(Long.valueOf(idStr));
					NodeList templateChilds = template.getChildNodes();
					if(null != templateChilds && templateChilds.getLength() > 0) {
						for(int j = 0; j < templateChilds.getLength(); j ++) {
							Node templateChild = templateChilds.item(j);
							if(TAG_ATTR.equals(templateChild.getNodeName())) {
								if(null == templateConfig.getAttributes()) {
									templateConfig.setAttributes(new ArrayList());
								}
								Attribute attribute = new Attribute();
								attribute.setId(templateChild.getAttributes().getNamedItem(ATTR_ID).getNodeValue());
								attribute.setType(templateChild.getAttributes().getNamedItem(ATTR_TYPE).getNodeValue());
								templateConfig.getAttributes().add(attribute);
							} else if(TAG_MOVE.equals(templateChild.getNodeName())) {
								if(null == templateConfig.getMoves()) {
									templateConfig.setMoves(new ArrayList());
								}
								Move move = new Move();
								NodeList moveChildren = templateChild.getChildNodes();
								if(null != moveChildren && moveChildren.getLength() > 0) {
									for(int k = 0 ; k < moveChildren.getLength(); k ++) {
										Node moveChild = moveChildren.item(k);
										if(TAG_FROM.equals(moveChild.getNodeName())) {
											String fromStr = moveChild.getAttributes().getNamedItem(ATTR_ID).getNodeValue();
											move.setFrom(ObjectId.predefined(CardState.class, fromStr));
										}
										if(TAG_TO.equals(moveChild.getNodeName())) {
											String toStr = moveChild.getAttributes().getNamedItem(ATTR_ID).getNodeValue();
											move.setTo(ObjectId.predefined(CardState.class, toStr));
										}
									}
								}
								templateConfig.getMoves().add(move);
							}
						}
					}
					configuration.put(templateConfig.getId(), templateConfig);
				}
			}
			
		} catch (Exception e) {
			logger.error("Error reading Digital Signature configuration", e);
		}		
	}
	
	
	public static class Template {
		private Long id;
		private List attributes;
		private List moves;
		
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public List getAttributes() {
			return attributes;
		}
		public void setAttributes(List attributes) {
			this.attributes = attributes;
		}
		public List getMoves() {
			return moves;
		}
		public void setMoves(List moves) {
			this.moves = moves;
		}
	}
	
	public static class Attribute {
		private String id;
		private String type;
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
	}
	
	public static class Move {
		private ObjectId from;
		private ObjectId to;
		
		public ObjectId getFrom() {
			return from;
		}
		public void setFrom(ObjectId from) {
			this.from = from;
		}
		public ObjectId getTo() {
			return to;
		}
		public void setTo(ObjectId to) {
			this.to = to;
		}
		
	}
}
