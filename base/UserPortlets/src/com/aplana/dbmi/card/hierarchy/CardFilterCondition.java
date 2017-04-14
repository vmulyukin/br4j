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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.card.hierarchy.util.AttributeHandler;
import com.aplana.dbmi.card.hierarchy.util.ValuesRange;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;

/**
 * @author DSultanbekov
 */
public class CardFilterCondition {
	private static Log logger = LogFactory.getLog(CardFilterCondition.class); 
	private Collection states;
	private Collection templates;
	private Map attributes;
	
	public Collection getStates() {
		return states;
	}
	public void setStates(Collection states) {
		this.states = states;
	}
	public Collection getTemplates() {
		return templates;
	}
	public void setTemplates(Collection templates) {
		this.templates = templates;
	}
	public Map getAttributes() {
		return attributes;
	}
	public void setAttributes(Map attributes) {
		this.attributes = attributes;
	}
	public boolean check(Card card) {
		if (!templates.isEmpty() && !templates.contains(card.getTemplate()))  {
			return false;
		}
		
		if (!states.isEmpty() && !states.contains(card.getState())) {
			return false;
		}		
		Iterator i = attributes.keySet().iterator();
		while (i.hasNext()) {
			ObjectId attrId = (ObjectId)i.next();			
			Attribute attr = card.getAttributeById(attrId);
			if (attr == null) {
                if ( logger.isWarnEnabled() ) {
				    logger.warn("Attribute not found in card [cardId = " + card.getId().getId() + ", attrCode = " + attrId.getId() + "]");
                }
				return false;
			}
			
			AttributeHandler xmlType = AttributeHandler.getXmlType(attrId.getType());
			List values = (List)attributes.get(attrId);
			Iterator j = values.iterator();
			boolean found = false;
			if (values.isEmpty()) {
				// If no values specified then it means that
				// attr shouldn't be empty
				found = !attr.isEmpty();
			} else {
				while (j.hasNext()) {
					Object obj = j.next();
					if (obj instanceof ValuesRange) {
						ValuesRange range = (ValuesRange)obj;
						if (range.match(attr)) {
							found = true;
							break;
						}
					} else {
						if (xmlType.matchValue(attr, obj)) {
							found = true;
							break;
						}
					}
				}			
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}
}
