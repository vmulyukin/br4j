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

import java.util.Collection;
import java.util.Set;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.LinkItem;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

import java.util.Collections;

/**
 * Deny action if linked cards [{@link #PARAM_REVERSE not}] contain cards in
 * determined states.
 * 
 */
public class DenyActionByLinkedCardStatesConsist extends ProcessCard {

	private static final long serialVersionUID = 520821843726056399L;
	/**
	 * Determinate states of linked cards
	 */
	public static final String PARAM_STATES = "states";

	/**
	 * If "true" linked cards should not have cards in determined states
	 * determined states, otherwise should have. Default "false"
	 */
	public static final String PARAM_REVERSE = "reverse";

	/**
	 * ID of CL-attribute which is used for retrieving linked cards
	 */
	public static String PARAM_LINK_ATTR = "linkAttr";

	/**
	 * A key of message for exception, which will be thrown to prevent action's
	 * execution. The message itself shall be defined with the mechanism
	 * provided in DataException (currently -
	 * <code>exceptions_*.properties</code> files)
	 */
	public static String PARAM_MESSAGE_KEY = "messageKey";

	private Set<ObjectId> states;
	private boolean reverse;
	private ObjectId linkAttrId;
	private String messageKey = "jbr.processor.visa.person.error";

	@Override
	public Object process() throws DataException {
		LinkItem it = new LinkItem();
		it.setLinkId(linkAttrId);
		Collection<ObjectId> childs = traverseCardLinksChain(getCardId(),
				Collections.singletonList(it));

		Search search = new Search();
		search.setByCode(true);
		search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(childs));
		SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(Card.ATTR_STATE);
		search.setColumns(Collections.singletonList(col));
		ActionQueryBase query = getQueryFactory().getActionQuery(search);
		query.setAction(search);
		SearchResult linked = (SearchResult) getDatabase().executeQuery(
				getSystemUser(), query);
		if (linked.getCards().size() == 0)
			throw new DataException(messageKey);

		if (states == null) {
			logger.error("Mandatory parameter \"states\" not set. Skipping.");
			return null;
		}
		boolean check = false;
		for (Object c : linked.getCards()) {
			Card card = (Card) c;
			if (states.contains(card.getState())) {
				check = true;
				break;
			}
		}
		if (!(check ^ reverse)) {
			throw new DataException(messageKey);
		}
		return null;

	}

	@Override
	public void setParameter(String name, String value) {
		if (PARAM_STATES.equalsIgnoreCase(name)) {
			states = IdUtils.makeStateIdsList(value.trim());
		} else if (PARAM_REVERSE.equalsIgnoreCase(name)) {
			reverse = Boolean.valueOf(value.trim());
		} else if (PARAM_LINK_ATTR.equalsIgnoreCase(name))
			linkAttrId = ObjectIdUtils.getObjectId(CardLinkAttribute.class,
					value, false);
		else if (PARAM_MESSAGE_KEY.equalsIgnoreCase(name))
			messageKey = value;
		else
			super.setParameter(name, value);

	}

}
