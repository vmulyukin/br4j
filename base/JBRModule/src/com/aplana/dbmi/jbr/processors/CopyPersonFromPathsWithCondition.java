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

import java.util.ArrayList;
import java.util.List;

import com.aplana.dbmi.jbr.util.AttributeSelector;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;

/**
 * ��������� ��������� �������� {@link #PARAM_ATTR_CONDITION �������} �
 * ���������� {@link CopyPersonFromPaths}
 * 
 */

public class CopyPersonFromPathsWithCondition extends CopyPersonFromPaths {

	private static final long serialVersionUID = -577518991466703614L;

	/**
	 * ������ ������� ��������, ��� ������� ����������� ���������
	 * 
	 * @see AttributeSelector
	 */
	public static final String PARAM_ATTR_CONDITION = "condition";

	protected final List<AttributeSelector> conditions = new ArrayList<AttributeSelector>();

	@Override
	public Object process() throws DataException {
		final ObjectId cardId = getCardId();
		if (cardId == null) {
			logger.warn("Impossible to set value until card is saved. Exiting");
			return null;
		}

		if (!checkCardConditons(cardId)) {
			logger.info("Card " + cardId.getId()
					+ " did not satisfies coditions. Exiting");
			return null;
		}
		if (!conditions.isEmpty())
			logger.debug("Card " + cardId.getId() + " satisfies coditions");

		return super.process();
	}

	@Override
	public void setParameter(String name, String value) {
		if (name.equalsIgnoreCase(PARAM_ATTR_CONDITION)) {
			try {
				AttributeSelector selector = AttributeSelector
						.createSelector(value.trim());
				selector.setBeanFactory(getBeanFactory());
				this.conditions.add(selector);
			} catch (DataException e) {
				e.printStackTrace();
			}
		} else
			super.setParameter(name, value);

	}

	private boolean checkCardConditons(ObjectId cardId) throws DataException {
		if (conditions == null || conditions.isEmpty())
			return true;
		final Card card = loadCardById(cardId);
		if (card == null)
			return true;

		for (AttributeSelector cond : conditions) {
			if (!cond.satisfies(card)) {
				logger.info("Card " + card.getId().getId()
						+ " did not satisfies codition " + cond);
				return false;
			}
		}
		return true;

	}
}