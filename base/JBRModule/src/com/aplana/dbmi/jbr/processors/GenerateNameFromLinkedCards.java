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

import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.processors.CheckStringAttrLength;

import java.util.*;

public class GenerateNameFromLinkedCards extends GenerateName {

	private static final String PARAM_FORMAT_PREFIX = "format";
	private final static String PARAM_LINK_ATTR = "linkedAttrId";
	private final static String PARAM_DELIMITER = "delimiter";

	private ObjectId linkAttrId;
	private String delimiter = " ";
	private List<String> formats = new ArrayList<String>();

	public GenerateNameFromLinkedCards() throws DataException {
		super();
	}

	@Override
	public void setParameter(String name, String value) {
		if (name.startsWith(PARAM_FORMAT_PREFIX)) {
			formats.add(value);
		} else {
			super.setParameter(name, value);
		}
	}

	@Override
	public Object process() throws DataException {
		if (formats.isEmpty()) {
			return null;
		}
		prepareParameters();
		Card card = getCard();
		if (logger.isDebugEnabled()) {
			logger.debug("Processor [" + getClass() + "] is started for card [" + card.getId().getId() + "]");
		}
		List<Card> linkedCards = getLinkedCards(card, linkAttrId);
		if (logger.isDebugEnabled()) {
			logger.debug("There were found " + linkedCards.size() + " cards.");
		}
		StringBuilder resultBuilder = new StringBuilder();
		for (Card linkedCard : linkedCards) {
			StringBuilder resultForCardBuilder = new StringBuilder();
			for (String format : formats) {
				List<String> args = prepareArguments(linkedCard);
				if (isArgHaveValuesForFormat(args, format)) {
					String formattedString = createFormattedString(format, args);
					resultForCardBuilder.append(formattedString);
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Result for card [" + linkedCard.getId().getId() + "] is ["
						+ resultForCardBuilder.toString() + "]");
			}
			if (resultBuilder.length() > 0 && resultForCardBuilder.length() > 0) {
				resultBuilder.append(delimiter);
			}
			resultBuilder.append(resultForCardBuilder.toString());
		}
		if (logger.isDebugEnabled()) {
			logger.debug("The value [" + resultBuilder.toString() + "] is calculated for card [" + card.getId().getId()
					+ "]");
		}
		if (resultBuilder.length() > 0) {
			String result = resultBuilder.toString();
			if(result.length() > CheckStringAttrLength.MAX_TEXT_LENGTH){
				result = result.substring(CheckStringAttrLength.MAX_TEXT_LENGTH);
			}
			saveResult(card, result);
		}
		return null;
	}

	@Override
	protected void prepareParameters() throws DataException {
		super.prepareParameters();
		String linkAttrParam = super.getParameter(PARAM_LINK_ATTR, "");
		if (linkAttrParam.isEmpty()) {
			throw new DataException(new IllegalStateException("Mandatory parameter " + PARAM_LINK_ATTR
					+ " is not defined"));
		}
		this.linkAttrId = ObjectIdUtils.getAttrObjectId(linkAttrParam, ":");
		this.delimiter = getParameter(PARAM_DELIMITER, " ");
	}

	protected List<Card> getLinkedCards(Card card, ObjectId attrId) throws DataException {
		Attribute attr = card.getAttributeById(attrId);
		List<Card> linkedCards = loadAllLinkedCardsByAttr(card.getId(), attr);
		if (linkedCards == null) {
			return Collections.emptyList();
		}
		Collections.sort(linkedCards, new Comparator<Card>() {
			@Override
			public int compare(Card card1, Card card2) {
				DateAttribute card1CreateDateAttr = card1.getAttributeById(Attribute.ID_CREATE_DATE);
				DateAttribute card2CreateDateAttr = card2.getAttributeById(Attribute.ID_CREATE_DATE);
				Date date1 = null;
				Date date2 = null;

				if (card1CreateDateAttr != null) {
					date1 = card1CreateDateAttr.getValue();
				}
				if (card2CreateDateAttr != null) {
					date2 = card2CreateDateAttr.getValue();
				}
				if (date1 == null && date2 == null) {
					return 0;
				} else if (date1 == null) {
					return -1;
				} else if (date2 == null) {
					return 1;
				}
				return date1.compareTo(date2);
			}
		});
		return linkedCards;
	}

	protected void saveResult(Card dstCard, String formattedString) throws DataException {
		StringAttribute attr = dstCard.getAttributeById(getDestAttrId());
		if (attr == null) {
			return;
		}
		attr.setValue(formattedString);
		super.doOverwriteCardAttributes(dstCard.getId(), attr);
	}
}
