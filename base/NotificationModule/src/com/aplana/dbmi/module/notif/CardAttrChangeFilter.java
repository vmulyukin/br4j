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
import java.util.Iterator;
import java.util.List;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardVersion;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

public class CardAttrChangeFilter extends DataServiceClient implements RecipientGroup
{
	private String attribute;
	private Collection recipients;
	private boolean oldFromVersion = true;
	
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	
	public void setRecipients(Collection recipients) {
		this.recipients = recipients;
	}

	public void setOldFromVersion(boolean oldFromVersion) {
		this.oldFromVersion = oldFromVersion;
	}

	public Collection discloseRecipients(NotificationObject object) {
		Card card = ((SingleCardNotification) object).getCard();
		if (attributeChanged(card)) {
			ArrayList people = new ArrayList();
			for (Iterator itr = recipients.iterator(); itr.hasNext(); )
				people.addAll(((RecipientGroup) itr.next()).discloseRecipients(object));
			return people;
		} else
			return Collections.EMPTY_LIST;
	}

	private boolean attributeChanged(Card card) {
		try {
			Attribute newAttr = findAttribute(card, attribute);
			if (newAttr == null)
				return false;
			Card oldCard;
			if (oldFromVersion) {
				ChildrenQueryBase versionQuery = getQueryFactory().getChildrenQuery(Card.class, CardVersion.class);
				versionQuery.setAccessChecker(null);
				versionQuery.setParent(card.getId());
				List versions = (List) getDatabase().executeQuery(getSystemUser(), versionQuery);
				/* No need to sort: versions are returned as sorted already
				Collections.sort(versions, new Comparator() {
					public int compare(Object obj1, Object obj2) {
						int idLeft = ((CardVersion.CompositeId) ((CardVersion) obj1).getId().getId()).getVersion();
						int idRight = ((CardVersion.CompositeId) ((CardVersion) obj2).getId().getId()).getVersion();
						if (idLeft < idRight)
							return -1;
						else if (idLeft > idRight)
							return 1;
						return 0;
					}});*/
				ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(CardVersion.class);
				cardQuery.setAccessChecker(null);
				cardQuery.setId(((CardVersion) versions.get(versions.size() - 1)).getId());
				oldCard = (Card) getDatabase().executeQuery(getSystemUser(), cardQuery);
			} else {
				ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(Card.class);
				cardQuery.setAccessChecker(null);
				cardQuery.setId(card.getId());
				oldCard = (Card) getDatabase().executeQuery(getSystemUser(), cardQuery);
			}
			Attribute oldAttr = findAttribute(oldCard, attribute);
			return !newAttr.equalValue(oldAttr);
		} catch (DataException e) {
			logger.error("Error fetching attribute " + attribute, e);
			return false;	// skip further processing
		}
	}

	private Attribute findAttribute(Card card, String attrId) {
		for (Iterator itrBlock = card.getAttributes().iterator(); itrBlock.hasNext(); ) {
			AttributeBlock block = (AttributeBlock) itrBlock.next();
			for (Iterator itrAttr = block.getAttributes().iterator(); itrAttr.hasNext(); ) {
				Attribute attr = (Attribute) itrAttr.next();
				if (attr.getId().getId().equals(attribute) ||
						attr.getId().equals(ObjectId.predefined(attr.getId().getType(), attribute)))
					return attr;
			}
		}
		return null;
	}
}
