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
package com.aplana.distrmanager.processors;

import java.util.Collection;
import java.util.List;

import com.aplana.dbmi.jbr.processors.ProcessCard;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.Validator;

public class CheckLinkedCardTemplate extends ProcessCard implements Validator {

	private static final long serialVersionUID = 1L;
	private static final String MESSAGE_PARAM = "message";
	private static final String LINK_PARAM = "linkAttr";
	private static final String TEMPLATES_PARAM = "allowedTemplates";

	private String messageCode;
	private ObjectId linkAttrId;
	private Collection<ObjectId> allowedTemplates;

	@Override
	public Object process() throws DataException {
		validateParameters();
		if (logger.isDebugEnabled()) {
			logger.debug("Validator of linked card templates is started: linkAttrId: [" + linkAttrId
					+ "], allowed templates: [" + allowedTemplates + "]");
		}

		Card curCard = super.loadCardById(getCardId());
		if (curCard == null)
			throw new DataException("general.null", new Object[] { "card" });

		final Attribute attr = curCard.getAttributeById(linkAttrId);
		if (attr == null) {
			throw new DataException("action.state.attrabsent", new Object[] { linkAttrId });
		}

		final List<Card> cards = super.loadAllLinkedCardsByAttr(curCard.getId(), attr);
		boolean isAllLinkedCardsOfAllowedTemplate = true;
		if (cards != null) {
			for (Card card : cards) {
				if (!allowedTemplates.contains(card.getTemplate())) {
					isAllLinkedCardsOfAllowedTemplate = false;
					break;
				}
			}
		}

		if (!isAllLinkedCardsOfAllowedTemplate) {
			throw new DataException(messageCode);
		}

		return null;
	}

	private void validateParameters() {
		if (messageCode == null || messageCode.trim().length() < 1) {
			throw new IllegalStateException("Processor is not cofigured properly: message code is not set");
		}
		if (linkAttrId == null) {
			throw new IllegalStateException("Processor is not cofigured properly: link attribute ID is not set");
		}
		if (allowedTemplates == null || allowedTemplates.isEmpty()) {
			throw new IllegalStateException("Processor is not cofigured properly: allowedTemplates is empty");
		}
	}

	@Override
	public void setParameter(String name, String value) {
		if (MESSAGE_PARAM.equals(name)) {
			messageCode = value;
		} else if (LINK_PARAM.equals(name)) {
			linkAttrId = IdUtils.smartMakeAttrId(value, CardLinkAttribute.class);
		} else if (TEMPLATES_PARAM.equals(name)) {
			allowedTemplates = ObjectIdUtils.commaDelimitedStringToIds(value, Template.class);
		}
	}

}
