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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.jbr.util.AttributeSelector;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.BasePropertySelector;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

public class CheckAndDoDependentChangeState extends DoDependentChangeState {
    /**
     * ��������� ���������� �������� ������� ��� ���������� ������� �����
     * �������� ������� ��������� ��������. ��������� �� ������������. ������
     * �������� ��������� ��������, � ��� ����� � ��������� ���������, ���������
     * ������������ ������������ ���������� DoDependentChangeState. ���
     * ���������� ��������� �������� ������� ��� ���������� ������� �����
     * ����������� ������� ��������� ��������. ������ ���������:
     * condition[;condition]* condition ::= codeAttr OP value codeAttr ::=
     * type:code, ��� type - ��� �������� (number, string), code - �������
     * attribute_code �������� � objectid.properties value - �������� ����������
     * ������� ��� ������
     */
    public static final String PARAM_CHECKS_VALUES = "conditions";

    public static final String PARAM_FILTER_CONDS = "filters";

    protected List<BasePropertySelector> conditions = Collections.emptyList();
    protected List<BasePropertySelector> filterConditions = Collections.emptyList();

    private Log logger = LogFactory.getLog(getClass());

    @Override
    public void setParameter(String name, String value) {
	if (PARAM_CHECKS_VALUES.equals(name)) {
	    conditions = formConditions(value);
	} else if (PARAM_FILTER_CONDS.equals(name)) {
	    filterConditions = formConditions(value);
	} else
	    super.setParameter(name, value);
    }

    private List<BasePropertySelector> formConditions(String value) {
	String[] condits = value.split(";");
	List<BasePropertySelector> parsedConditions = new ArrayList<BasePropertySelector>();
	for (int i = 0; i < condits.length; i++) {
	    String condition = condits[i].trim();
	    try {
		AttributeSelector selector = AttributeSelector
			.createSelector(condition);
		parsedConditions.add(selector);
	    } catch (DataException ex) {
		logger.error("Error during parsing condition " + condition, ex);
	    }
	}
	return parsedConditions;
    }

    @Override
    public Object process() throws DataException {
	final ObjectId cardId = getCardId();
	if (cardId == null) {
	    logger.error("Impossible to resolve card");
	    return null;
	}

	if (!checkCardConditons(conditions, cardId)) {
	    logger.warn("Card " + cardId.getId()
		    + " did not satisfies conditions. Exiting");
	    return null;
	}
	if (!conditions.isEmpty())
	    logger.debug("Card " + cardId.getId() + " satisfies coditions");
	return super.process();
    }

    protected ObjectId getCardId() {
	return ((ChangeState) getAction()).getCard().getId();
    }

    @Override
    protected Collection<Card> getDependentCards(Card baseCard)
	    throws DataException {
	Collection<Card> dependentCards = super.getDependentCards(baseCard);
	for (Iterator<Card> iter = dependentCards.iterator(); iter.hasNext();) {
	    Card card = iter.next();
	    if (!checkDependentCard(card)) {
		logger.warn("Card " + card.getId().getId()
			+ " did not satisfies conditions. Skip");
		iter.remove();
	    }
	}
	return dependentCards;
    }

	protected boolean checkDependentCard(Card card) throws DataException {
		ObjectId id = card.getId();
		return checkCardConditons(filterConditions, id);
	}

    private boolean checkCardConditons(List<BasePropertySelector> conds,
	    ObjectId cardId) throws DataException {
	if (conds == null || conds.isEmpty())
	    return true;
	final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(
		Card.class);
	cardQuery.setAccessChecker(null);
	cardQuery.setId(cardId);
	final Card card = (Card) getDatabase().executeQuery(getSystemUser(),
		cardQuery);
	return checkConditions(conds, card);
    }

    private boolean checkConditions(List<BasePropertySelector> conds, Card card) {
	if (conds == null || card == null)
	    return true;
	for (BasePropertySelector cond : conds) {
	    if (!cond.satisfies(card)) {
		logger.debug("Card " + card.getId().getId()
			+ " did not satisfies codition " + cond);
		return false;
	    }
	}
	return true;
    }
}
