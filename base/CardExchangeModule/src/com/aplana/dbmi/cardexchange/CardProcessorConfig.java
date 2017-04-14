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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.cardexchange.xml.AttributeXMLHandler;
import com.aplana.dbmi.cardexchange.xml.AttributeXMLValue;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;

public class CardProcessorConfig {
	private final static Log logger = LogFactory.getLog(CardProcessorConfig.class);
	
	private String className;
	private Map parameters;	
	private Set templates;
	private Set states;
	private Map attributes;
	
	public boolean isFeasibleForCard(Card card) {
		if (!templates.contains(card.getTemplate()))  {
			return false;
		}
		
		if (!states.contains(card.getState())) {
			return false;
		}
		
		Iterator i = attributes.keySet().iterator();
		while (i.hasNext()) {
			ObjectId attrId = (ObjectId)i.next();			
			Attribute attr = card.getAttributeById(attrId);
			if (attr == null) {
				logger.warn("Attribute not found in card [cardId = " + card.getId().getId() + ", attrCode = " + attrId.getId() + "]");
				return false;
			}
			AttributeXMLValue xmlValue = (AttributeXMLValue)attributes.get(attrId);
			AttributeXMLHandler xmlType = AttributeXMLHandler.getXmlType(attrId.getType());
			if (!xmlType.matchXmlValue(attr, xmlValue)) {
				return false;
			}
		}
		return true;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public Map getParameters() {
		return parameters;
	}

	public void setParameters(Map parameters) {
		this.parameters = parameters;
	}

	public void setTemplates(Set templates) {
		this.templates = templates;
	}

	public void setStates(Set states) {
		this.states = states;
	}

	public void setAttributes(Map attributes) {
		this.attributes = attributes;
	}
}