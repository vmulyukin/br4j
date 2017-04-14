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
package com.aplana.dbmi.jbr.processors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;

public class CopyBackLink extends CopyCardLink {

	private static final long serialVersionUID = 1L;
	private Map<ObjectId, ObjectId> toIdsByTemplate = new HashMap<ObjectId, ObjectId>();

	@Override
	protected Set<ObjectId> getToIds() {
		Set<ObjectId> idSet = new HashSet<ObjectId>();
		if (getToId() != null) {
			idSet.add(getToId());
		}
		idSet.addAll(toIdsByTemplate.values());
		return idSet;
	}

	@Override
	protected Set<CardLinkAttribute> getToAttrs(Card c) {
		Set<CardLinkAttribute> idSet = new HashSet<CardLinkAttribute>();
		if (getToId() != null) {
			idSet.add(c.getCardLinkAttributeById(getToId()));
		}
		if (this.toIdsByTemplate.containsKey(c.getTemplate())) {
			idSet.add(c.getCardLinkAttributeById(this.toIdsByTemplate.get(c.getTemplate())));
		}
		return idSet;
	}

	@Override
	protected void addFromValuesToAttribute(CardLinkAttribute copyAttr, Card c) throws DataException {
		Attribute attr = c.getAttributeById(getFromId());
		copyAttr.addIdsLinked(getAllLinkedIdsByAttr(c.getId(), attr, getUser()));
	}

	@Override
	public void setParameter(String name, String value){
		if(name.startsWith("toByTemplate")) {
			String[] pair = value.split("\\?");
			if (pair.length != 2) {
				throw new IllegalStateException("Incorrect config: should be in format <templateId>?<attributeId>");
			}
			ObjectId templateId = ObjectIdUtils.getObjectId(Template.class, pair[0].trim(), true);
			ObjectId toId = IdUtils.smartMakeAttrId(pair[1].trim(), CardLinkAttribute.class);
			toIdsByTemplate.put(templateId, toId);
		}  else {
			super.setParameter(name, value);
		}
	}


}
