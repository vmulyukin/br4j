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
package com.aplana.dbmi.module.notif;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class CardAttrSelectorRecipients extends DataServiceClient implements RecipientGroup
{
	private String attribute;
	private HashMap groups;

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public void setGroups(Map groups) {
		this.groups = new HashMap();
		for (Iterator itr = groups.entrySet().iterator(); itr.hasNext(); ) {
			Map.Entry entry = (Map.Entry) itr.next();
			ObjectId id;
			if (entry.getKey() instanceof ObjectId)
				id = (ObjectId) entry.getKey();
			else {
				id = ObjectId.predefined(ReferenceValue.class, entry.getKey().toString());
				if (id == null)
					try {
						id = new ObjectId(ReferenceValue.class, Long.parseLong(entry.getKey().toString()));
					} catch (NumberFormatException e) {
						logger.warn("Can't convert " + entry.getKey() + " to a reference value id; ignored");
						continue;
					}
			}
			this.groups.put(id, entry.getValue());
		}
	}

	public Collection discloseRecipients(NotificationObject object) {
		if (!SingleCardNotification.class.isAssignableFrom(object.getClass()))
			throw new IllegalArgumentException("This recipient group can only be used for card notifications");
		
		Card card = ((SingleCardNotification) object).getCard();
		ObjectId attrId = ObjectId.predefined(ListAttribute.class, attribute);
		if (attrId == null)
			attrId = new ObjectId(ListAttribute.class, attribute);
		if (card.getAttributeById(attrId) == null)
			card = fetchCard(card.getId(), attrId);
		if (card == null)
			return Collections.EMPTY_LIST;
		ListAttribute attr = (ListAttribute) card.getAttributeById(attrId);
		ReferenceValue value = attr.getValue();
		if (value == null) {
			logger.info("Attribute " + attrId.getId() + " not set in card " +
					card.getId().getId() + "; selection isn't possible");
			return Collections.EMPTY_LIST;
		}
		RecipientGroup group = (RecipientGroup) groups.get(value.getId());
		if (group == null) {
			logger.info("Recipient group not set for attribute value " + value.getId().getId() + "; skipped");
			return Collections.EMPTY_LIST;
		}
		return group.discloseRecipients(object);
	}

	private Card fetchCard(ObjectId cardId, ObjectId attrId) {
		try {
			Search search = new Search();
			search.setByAttributes(false);
			search.setByCode(true);
			search.setWords(cardId.getId().toString());
			search.setColumns(new ArrayList(1));
			SearchResult.Column col = new SearchResult.Column();
			col.setAttributeId(attrId);
			search.getColumns().add(col);
			ActionQueryBase query = getQueryFactory().getActionQuery(search);
			query.setAction(search);
			SearchResult result = (SearchResult) getDatabase().executeQuery(getSystemUser(), query);
			if (result.getCards().size() != 1) {
				logger.error(result.getCards().size() + " cards found by ID " + cardId.getId());
				return null;
			}
			return (Card) result.getCards().iterator().next();
		} catch (DataException e) {
			logger.error("Error fetching card " + cardId.getId(), e);
			return null;
		}
	}

}
