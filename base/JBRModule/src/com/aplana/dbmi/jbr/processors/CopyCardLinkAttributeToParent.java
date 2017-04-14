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

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.AttributeSelector;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.BasePropertySelector;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.service.impl.query.AttributeTypes;

// (YNikitin, 2013/07/23) ������� ����������� ����������� id-���� ������� �������� � �������� ������������
public class CopyCardLinkAttributeToParent extends BaseCopyAttributeProcessor
	// implements Parametrized
{
	private static final long serialVersionUID = 1L;

	static final String PARAM_PARENT = "parent";
	static final String PARAM_REMEMBER = "rememberLastParent";
	private static final String PARAM_ATTR_CONDITION = "attr_condition";

	private String parentKeyOrCode;
	private String attrcodeParent;
	/*����, ����������� ������������� ������ ��� ������ DoListLinkedCards. 
	  �� ��������� = false � �� �������� ������ ������.
	  ���� = true, �� ��� ������ �� ��������� ��� ��������� query � DoListLinkedCards
	    ����� ������������ ID ��������� (����� �������) ��������, �.�. � ������������� �� ����,
	    �������� �� ��� ������������ ��������� ��� ���
	*/
	private boolean remember = false;

	protected final List<BasePropertySelector> conditions = new ArrayList<BasePropertySelector>();

	@Override
	public Object process() throws DataException {
		typeCh = AttributeTypes.CARD_LINK;
		super.process();

		attrcodeParent = parentKeyOrCode;
		if (attrcodeParent == null) {
			logger.warn( "parent link is NULL -> exiting (if you want to copy attributes inside same card use another processor CopyAttributeToChildren in the same way)" );
			return null;
		}

		final ObjectId cardId = getCardId();
		final Card card = getCard();
		if (cardId == null) {
			logger.warn("Impossible to copy attributes until card is saved -> exiting");
			return null;
		}

		if (!checkCardConditons(cardId)) {
		    logger.warn("Card " + cardId.getId()
			    + " did not satisfies coditions. Exiting");
		    return null;
		}
		if (!conditions.isEmpty())
		    logger.debug("Card " + cardId.getId() + " satisfies coditions");

		Card docTo = null;

		if("@Self".equalsIgnoreCase(parentKeyOrCode)) {
			docTo = card;
		} else {
			final ObjectId parentAttrId = IdUtils.smartMakeAttrId(attrcodeParent, BackLinkAttribute.class);
			attrcodeParent = (String) parentAttrId.getId();

			if(BackLinkAttribute.class.isAssignableFrom( parentAttrId.getType())) {
				docTo = execListProject( cardId, new ObjectId( BackLinkAttribute.class, attrcodeParent), getSystemUser());
			} else if(CardLinkAttribute.class.isAssignableFrom( parentAttrId.getType())) {
				final CardLinkAttribute cla = card.getCardLinkAttributeById(new ObjectId(CardLinkAttribute.class, attrcodeParent));
				if(cla == null || cla.isEmpty()) {
					logger.warn("Attribute for current "+ cardId + " is not found by attribute or is empty " + attrcodeParent);
					return null;
				}
				if(cla.getIdsLinked().size() > 1) {
					logger.info("Attribute for current "+ cardId + " contain more 1 value of attribute " + attrcodeParent);
				}
				docTo = loadCardById(cla.getIdsLinked().get(0));
			}
			
			if(null == docTo) {
				logger.info("Back-linked (B) card for current "+ cardId + " is not found by attribute " + attrcodeParent);
				return null;
			}
		}

		CardLinkAttribute attribute = ((String)Card.ATTR_ID.getId()).equals(attrcodeFrom)?null:card.getCardLinkAttributeById(new ObjectId(CardLinkAttribute.class, attrcodeFrom));
		if(!((String)Card.ATTR_ID.getId()).equals(attrcodeFrom) && (null == attribute || null == attribute.getIdsLinked() || attribute.getIdsLinked().isEmpty())) {
			logger.info("Current card "+ cardId + " doesn't have attribute " + attrcodeFrom + " to copy");
			return null;
		}

		final Long cardIdTo = (Long) docTo.getId().getId();
		logger.debug( "Back-linked (B) card for current "+ cardId + " is found as "+ cardIdTo);

		execAction(new LockObject(docTo));
		try {
			final int oldCount = getJdbcTemplate().update(				"DELETE FROM attribute_value " +
				"WHERE card_id=? and attribute_code=? and number_value in(" + (((String)Card.ATTR_ID.getId()).equals(attrcodeFrom)? cardId.getId() :(attribute.getLinkedIds().isEmpty()?"null":attribute.getLinkedIds())) + ")",
				new Object[] { cardIdTo, attrcodeTo},
				new int[] { Types.NUMERIC, Types.VARCHAR }
			);

			final int copiedCount;
			if (((String)Card.ATTR_ID.getId()).equals(attrcodeFrom)){
				copiedCount = getJdbcTemplate().update(				"INSERT INTO attribute_value \n" +
						"\t (	card_id, attribute_code, \n" +
						"\t		number_value, string_value, \n" +
						"\t 	date_value, value_id, \n" +
						"\t		another_value ) \n" +
						"\t SELECT \n" +
						"\t\t	?, ?, \n" +
						"\t\t	c.card_id, null as string_value, \n" +
						"\t\t	null as date_value, null as value_id, \n" +
						"\t\t	null as another_value \n " +
						"\t FROM card c \n" +
						"\t WHERE c.card_id = ? \n"
						,
						new Object[] {cardIdTo, attrcodeTo, cardId.getId() },
						new int[] {Types.NUMERIC, Types.VARCHAR, Types.NUMERIC }
					);
			} else
				copiedCount = getJdbcTemplate().update(				"INSERT INTO attribute_value \n" +
					"\t (	card_id, attribute_code, \n" +
					"\t		number_value, string_value, \n" +
					"\t 	date_value, value_id, \n" +
					"\t		another_value ) \n" +
					"\t SELECT \n" +
					"\t\t	?, ?, \n" +
					"\t\t	v.number_value, v.string_value, \n" +
					"\t\t	v.date_value, v.value_id, \n" +
					"\t\t	v.another_value \n " +
					"\t FROM attribute_value v \n" +
					"\t WHERE v.attribute_code=? \n" +
					"\t\t	AND v.card_id = ? \n"
					,
					new Object[] {cardIdTo, attrcodeTo, attrcodeFrom,	cardId.getId() },
					new int[] {Types.NUMERIC, Types.VARCHAR, Types.VARCHAR,	Types.NUMERIC }
				);
			logger.info( copiedCount + " value(s) of attribute '" + attrcodeFrom +
				"' of card "+ cardId +" did replace " + oldCount +
				" old value(s) of '" + attrcodeTo + "' of card " + cardIdTo );
		} finally {
			execAction(new UnlockObject(docTo));
		}
		return null;

	}

	private boolean checkCardConditons(ObjectId cardId) throws DataException {
		if (conditions == null || conditions.isEmpty())
			return true;
		final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(
			Card.class);
		cardQuery.setAccessChecker(null);
		cardQuery.setId(cardId);
		final Card card = (Card) getDatabase().executeQuery(getSystemUser(),
			cardQuery);
		return checkConditions(conditions, card);
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

	@Override
	public void setParameter(String name, String value) {
		if (name.startsWith(PARAM_ATTR_CONDITION)) {
			try {
				final AttributeSelector selector = AttributeSelector
					.createSelector(value);
				this.conditions.add(selector);
			} catch (DataException ex) {
				ex.printStackTrace();
			}
		} else if (PARAM_PARENT.equalsIgnoreCase(name))
			this.parentKeyOrCode = value;
		else if (PARAM_REMEMBER.equalsIgnoreCase(name))
			this.remember = Boolean.parseBoolean(value);
		else
			super.setParameter(name, value);
	}

	@SuppressWarnings("unchecked")
	Card execListProject( ObjectId cardId, ObjectId backLinkAttrId,
			UserData user) throws DataException
	{
		final ListProject action = new ListProject();
		action.setCard( cardId);
		action.setAttribute( backLinkAttrId);
		action.setRememberLastParent(remember);

		final SearchResult rs = (SearchResult) super.execAction(action, user);
		final List<Card> cards = CardUtils.getCardsList(rs);
		return (cards == null || cards.isEmpty()) ? null : cards.get(0);
	}


/*	Card getCard()	{
		Card acard = null;
		if (getObject() instanceof Card) {
			acard = (Card) getObject();
		} else if (getAction() instanceof ChangeState) {
			acard = ((ChangeState) getAction()).getCard();
		}
		return acard;
	}
*/
}