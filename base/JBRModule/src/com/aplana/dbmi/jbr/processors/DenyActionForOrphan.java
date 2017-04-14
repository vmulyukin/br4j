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

import java.util.Collections;
import java.util.List;

import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.Validator;

public class DenyActionForOrphan extends AbstractCardProcessor implements Parametrized, Validator
{
	public static final String PARAM_LINK_ATTR = "linkAttr";
	public static final String PARAM_MESSAGE_KEY = "messageKey";
	
	private ObjectId linkAttrId;
	private String messageKey = "jbr.card.move.orphan";

	@SuppressWarnings("unchecked")
	@Override
	public Object process() throws DataException {
		List<Card> cardList;
		if(BackLinkAttribute.class.isAssignableFrom(linkAttrId.getType())) {
			ListProject search = new ListProject();
			search.setAttribute(linkAttrId);
			search.setCard(getCardId());
			SearchResult.Column col = new SearchResult.Column();
			col.setAttributeId(Card.ATTR_STATE);
			search.setColumns(Collections.singletonList(col));
			ActionQueryBase query = getQueryFactory().getActionQuery(search);
			query.setAction(search);
			SearchResult linked = (SearchResult) getDatabase().executeQuery(getSystemUser(), query);
			cardList = linked.getCards();
		} else {
			cardList = getLinkedCards(getCardId(), linkAttrId, false);
		}
		if (cardList == null || cardList.isEmpty())
			throw new DataException(messageKey);
		return null;
	}

	public void setParameter(String name, String value) {
		if (PARAM_LINK_ATTR.equalsIgnoreCase(name))
			linkAttrId = IdUtils.smartMakeAttrId(value, BackLinkAttribute.class);
		else if (PARAM_MESSAGE_KEY.equalsIgnoreCase(name))
			messageKey = value;
		else
			throw new IllegalArgumentException("Unknown parameter: " + name);
	}

}
