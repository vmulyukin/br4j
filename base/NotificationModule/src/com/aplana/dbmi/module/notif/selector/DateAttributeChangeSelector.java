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
package com.aplana.dbmi.module.notif.selector;

import org.apache.commons.lang.time.DateUtils;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.module.notif.DataServiceClient;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.Selector;

public abstract class DateAttributeChangeSelector extends DataServiceClient implements Selector {

	@Override
	public boolean satisfies(Object object) {
		try {
			Card card = (Card) object;
			DateAttribute attr = (DateAttribute) card.getAttributeById(getAttributeId());
			
			ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
			query.setId(card.getId());
			Card oldCard = (Card) getDatabase().executeQuery(getSystemUser(), query);
			DateAttribute oldAttr = (DateAttribute) oldCard.getAttributeById(getAttributeId());
			
			if (oldAttr.getValue() == null) {
				logger.debug("Date value in old attr (from db) is null");
				return false;
			}
			if (attr.getValue() == null) {
				logger.debug("Date value in new attr (model) is null");
				return false;
			}
			boolean sameDay = DateUtils.isSameDay(attr.getValue(), oldAttr.getValue());
			logger.debug("Was: " + oldAttr.getValue() + "; became: " + attr.getValue() +
					"; equal=" + sameDay);
			return !sameDay;
		} catch (Exception e) {
			logger.warn("Error trying to determine whether commission's term was changed or not", e);
			return false;
		}
	}

	protected abstract ObjectId getAttributeId();
}
