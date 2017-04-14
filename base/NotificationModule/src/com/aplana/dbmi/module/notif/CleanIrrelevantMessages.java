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

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.jbr.processors.ProcessCard;
import com.aplana.dbmi.jbr.util.AttributeSelector;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.CardLinkLoader;
import com.aplana.dbmi.service.impl.BasePropertySelector;

import java.util.*;

/**
 * This processor remove irrelevant popup messages.
 * Must be executed before all CardNotification processors.
 * If linkId is specified, popup messages will be removed for linked cards.
 * Conditions - conditions for action card.
 * @author echirkov
 */
public class CleanIrrelevantMessages extends ProcessCard
{
    public static final String PARAM_LINK_ATTR = "linkAttr";
    public static final String PARAM_CHECKS_VALUES = "conditions";

    protected ObjectId linkId;
    protected List<BasePropertySelector> conditions = new LinkedList<BasePropertySelector>();

	@Override
	public Object process() throws DataException {

		if(getAction() instanceof ChangeState){
			ChangeState action = (ChangeState) getAction();
            Card actionCard = action.getCard();

            if(!checkConditions(conditions, actionCard)){
                return null;
            }

            if(linkId == null){
                clearMessageForCard(actionCard.getId());
            } else {
                Collection<Card> linkedCards = getLinkedCards(actionCard);
                for(Card linkedCard: linkedCards){
                    clearMessageForCard(linkedCard.getId());
                }
            }
		}
		return null;

	}

    protected void clearMessageForCard(ObjectId cardId){
        getJdbcTemplate().update("DELETE FROM message WHERE eventCard_id = " + cardId.getId());
    }

    protected Collection<Card> getLinkedCards(Card baseCard) throws DataException {
        Collection<Card> res = null;
        if (BackLinkAttribute.class.equals(linkId.getType())) {
            res = getBackLinkedCards(linkId, baseCard.getId());
        } else {
            res = getCardLinkedCards((CardLinkAttribute) baseCard.getAttributeById(linkId));
        }
        return res != null ? res : Collections.<Card>emptyList();
    }

    protected Collection<Card> getBackLinkedCards(ObjectId attrId, ObjectId cardId) throws DataException {
        final ListProject list = new ListProject();
        list.setAttribute(attrId);
        list.setCard(cardId);
        final List<SearchResult.Column> cols = new ArrayList<SearchResult.Column>(1);
        cols.add( CardUtils.createColumn(Card.ATTR_STATE));
        list.setColumns( cols);
        return CardUtils.execSearchCards(list, getQueryFactory(), getDatabase(), getSystemUser());
    }

    protected Collection<Card> getCardLinkedCards(CardLinkAttribute attr) throws DataException {

        if (attr == null || attr.getLinkedCount() < 1)
            return Collections.emptyList();

        return CardLinkLoader.loadCardsByLink(attr, new ObjectId[]{Card.ATTR_STATE},
                getSystemUser(), getQueryFactory(), getDatabase());
    }

    public void setParameter(String name, String value) {
        if (PARAM_LINK_ATTR.equalsIgnoreCase(name)) {
            this.linkId = IdUtils.smartMakeAttrId(value, CardLinkAttribute.class, false);
        } else 	if (PARAM_CHECKS_VALUES.equals(name)) {
            conditions = formConditions(value);
        }
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