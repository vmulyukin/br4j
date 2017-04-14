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

import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.AttributeSelector;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.jbr.util.parser.BooleanParser;
import com.aplana.dbmi.jbr.util.parser.FactoryBooleanParser;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.BasePropertySelector;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.UserData;

import java.sql.Types;
import java.util.*;

/**
 * ����������� ������� ������� �������� � ������� ��������� ��������.
 * ����� ����� ���� CardLink ��� BackLink.
 * (!) ���� ��������� ������� ���������.
 */
public class CopyAttributeToChildren extends BaseCopyAttributeProcessor implements Parametrized {
	private static final long serialVersionUID = 1L;

	// �������� ��� ����������� �������� �� �������: "children" � "linkAttrId"
	static final String PARAM_CHILDREN = "children";
	static final String PARAM_LINK = "linkAttrId";
	static final String PARAM_ATTR_CONDITION = "attr_condition";
	private static final String PARAM_PATH_CONDITION = "path_condition";

	protected String attrCodeOrKeyChildren;
	protected String attrcodeChild;
    protected final HashMap<BasePropertySelector, String> nameConditions = new HashMap<BasePropertySelector, String>();
    protected final HashMap<String, String> pathCond = new HashMap<String, String>();

	//private AccessRuleManager accessManager;

	protected final List<BasePropertySelector> conditions = new ArrayList<BasePropertySelector>();

	@Override
	public Object process() throws DataException {
		super.process();

		final ObjectId cardId = getCardId();
		final Card card = getCard();
		if (cardId == null) {
			logger.warn("Impossible to copy attributes until card is saved -> exiting");
			return null;
		}

		// �������� ������� �� ��������...
		if (!checkCardConditions(cardId)) {
			logger.warn("Card "+ cardId.getId() +" did not satisfies coditions -> exiting");
			return null;
		}
		// ��������� �� ���� ���������, ������� ������ ���� ��������
		// � ������� �� ���� �������� �������, ���������� ���� ������ ����������� �������� ���� ���� �����
		if (changeAttributes != null) {
			boolean someAttributesChanged = false;
			for (ObjectId changeAttributeCode: changeAttributes) {
				final Attribute changeAttribute = card.getAttributeById(changeAttributeCode);
				if (!super.isAttributeChanged(changeAttribute, card)) {
					if (isMultiplicationChangeAttributeOption) {
						logger.warn("Attribute "+changeAttribute.getId() +" in card " + (card.getId()!=null?card.getId():null) + " is not change => exit");
						return null;
					}
				} else {
					someAttributesChanged = true;
				}
			}
			if (!someAttributesChanged) {
				logger.warn("No attribute changed in card " + (card.getId()!=null?card.getId():null) + " => exit");
				return null;
			}
		}

		if (!conditions.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Card " + cardId.getId() + " satisfies conditions");
			}
		}

		int oldCount = -1;
		int copiedCount = -1;
		String infoCardTo = "";

		attrcodeChild = attrCodeOrKeyChildren;
		
		if (attrcodeChild != null) {
			
			// final ObjectId id = chkInitAttr( attrcodeChild, CardLinkAttribute.class);
			final ObjectId linkedAttrId = IdUtils.smartMakeAttrId(attrcodeChild, CardLinkAttribute.class);
			attrcodeChild = (String) linkedAttrId.getId();

			if (BackLinkAttribute.class.isAssignableFrom( linkedAttrId.getType())) {
				final List<Card> docToCards = execCardListProject( cardId,
					linkedAttrId,
					getSystemUser());

				final String cardIds = IdUtils.makeCardIdEnum(docToCards, ",");
				// ������� ������ ������ ������ �����, ����� ���� ��������� ���������
				if (logger.isDebugEnabled()) {
					logger.debug("Back-linked (B) card for current " + cardId + " is found as " + cardIds);
				}

				if (docToCards != null) {
					ArrayList<Card> lockedCards = new ArrayList<Card>(docToCards.size());
					for (Card c: docToCards) {
						execAction(new LockObject(c));
						lockedCards.add(c);
					}
					try {
						// ������� ������� ���� ��� ��������, � ������� �������� �������� �����, ����� � ���������� ���������� ��� ���������
						if (updateAccessList){
							cleanAccessList(docToCards);
						}
						if (!append) {
							oldCount = getJdbcTemplate().update(
								"DELETE FROM attribute_value " +
								"WHERE attribute_code=? AND card_id in ("+cardIds+")",
								new Object[] { attrcodeTo},
								new int[] { Types.VARCHAR} );
						}
						copiedCount = getJdbcTemplate().update(
							"INSERT INTO attribute_value (card_id, attribute_code, \n" +
							"	number_value, string_value, date_value, value_id, another_value) \n" +
							"\t SELECT \n" +
							"\t\t	cardTo.card_id, aTo.attribute_code, \n" +
							"\t\t	v.number_value, v.string_value, \n" +
							"\t\t	v.date_value, v.value_id, \n" +
							"\t\t	v.another_value \n " +
							"\t FROM attribute_value v \n" +
							"\t		JOIN attribute aFrom on aFrom.attribute_code=v.attribute_code \n" +
							"\t		JOIN card cardTo on cardTo.card_id in ("+cardIds+") \n" +
							"\t		JOIN attribute aTo on aTo.attribute_code=? \n" +
							"\t			and aTo.data_type=aFrom.data_type \n" +
							"\t WHERE v.card_id = ? \n" +
							"		and v.attribute_code=? \n"+
							/* ��������� �������� �� ������������ ��������*/
							"\t 	and not exists( \n"+
							"\t 					select 1 from attribute_value av \n"+
							"\t 					where	av.attribute_code = aTo.attribute_code \n"+
							"\t 							and av.card_id = cardTo.card_id \n"+
							"\t 							and coalesce(av.number_value, -1) = coalesce(v.number_value, -1) \n"+
							"\t 							and coalesce(av.string_value, '') = coalesce(v.string_value, '') \n"+
							"\t 							and coalesce(av.date_value, '1970-01-01') = coalesce(v.date_value, '1970-01-01') \n"+
							"\t 							and coalesce(av.value_id, -1) = coalesce(v.value_id, -1) \n"+
							"\t 							and coalesce(av.another_value, '') = coalesce(v.another_value, '') \n"+
							"\t 	)"
							,
							new Object[] {attrcodeTo, cardId.getId(), attrcodeFrom},
							new int[] {Types.VARCHAR, Types.NUMERIC, Types.VARCHAR}
						);
						// ��������� ������� ���� ��� ��������, � ������� �������� �������� �����, ����� � ���������� ���������� ��� ���������
						if (updateAccessList){
							recalculateAccessList(docToCards);
						}
					} finally {
						for (Card c: lockedCards) {
							execAction(new UnlockObject(c));
						}
					}
				}
				infoCardTo = " of card "+ cardIds;

			} else if (CardLinkAttribute.class.isAssignableFrom(linkedAttrId.getType())) {
				Collection<ObjectId> colCardIds = CardUtils.getAttrLinks(card, linkedAttrId);
				ArrayList<ObjectId> lockedCards = new ArrayList<ObjectId>();
				try {
					// ����������� ��������� ������ � ��� ������, ���� ��������� �������� ������� 
					// ��������� ������ �� ������ - �������� ������ ���, �� CardUtils ���������� ���� ������ � ������� ��������� (�������� ��������, �� � ��������� �� �������� �����������)
					if (colCardIds!=null&&!(colCardIds.size()==1&&colCardIds.contains(new ObjectId(Card.class, 0)))){
						for (ObjectId cCardId: colCardIds) {
							// ������� ������� ���� ��� ��������, � ������� �������� �������� �����, ����� � ���������� ���������� ��� ���������
							if (updateAccessList){
								cleanAccessList(cCardId);
							}
							execAction(new LockObject(cCardId));
							lockedCards.add(cCardId);
						}
						// ������� ������ ������ ������ �����, ����� ���� ��������� ���������
						if (!append) {
							oldCount = getJdbcTemplate().update(
								"DELETE FROM attribute_value " +
								"WHERE attribute_code=? AND card_id IN " +
									"(SELECT number_value FROM attribute_value " +
									"WHERE card_id=? AND attribute_code=?)",
								new Object[] { attrcodeTo, cardId.getId(), attrcodeChild});
						}
						copiedCount = getJdbcTemplate().update(
								"INSERT INTO attribute_value (card_id, attribute_code, \n" +
								"	number_value, string_value, date_value, value_id, another_value) \n" +
								"SELECT clink.number_value, aTo.attribute_code, v.number_value, v.string_value, \n" +
								"	v.date_value, v.value_id, v.another_value \n" +
								"FROM attribute_value v \n" +
								"	JOIN attribute aFrom on aFrom.attribute_code=v.attribute_code \n" +
								"	JOIN attribute aTo on aTo.attribute_code=? \n" +
								"		and aTo.data_type=aFrom.data_type \n" +
								"	JOIN attribute_value clink \n" +
								"		on clink.card_id = v.card_id and clink.attribute_code = ? \n" +
								"WHERE v.card_id=? AND v.attribute_code=? "+
								/* ��������� �������� �� ������������ ��������*/
								"\t 	and not exists( \n"+
								"\t 					select 1 from attribute_value av \n"+
								"\t 					where	av.attribute_code = aTo.attribute_code \n"+
								"\t 							and av.card_id = clink.number_value \n"+
								"\t 							and coalesce(av.number_value, -1) = coalesce(v.number_value, -1) \n"+
								"\t 							and coalesce(av.string_value, '') = coalesce(v.string_value, '') \n"+
								"\t 							and coalesce(av.date_value, '1970-01-01') = coalesce(v.date_value, '1970-01-01') \n"+
								"\t 							and coalesce(av.value_id, -1) = coalesce(v.value_id, -1) \n"+
								"\t 							and coalesce(av.another_value, '') = coalesce(v.another_value, '') \n"+
								"\t 	)"
								,
								new Object[] {  attrcodeTo, 		attrcodeChild,
												cardId.getId(),		attrcodeFrom },
								new int[] { Types.VARCHAR,	Types.VARCHAR,
											Types.NUMERIC,	Types.VARCHAR}
							);
					}
				} finally {
					// ��������� ������� ���� ��� ��������, � ������� �������� ��������
					if (colCardIds!=null){
						for (ObjectId cCardId: colCardIds) {
							// ��������� ������� ���� ��� ��������, � ������� �������� �������� �����, ����� � ���������� ���������� ��� ���������
							if (updateAccessList){
								recalculateAccessList(cCardId);
								//getAccessManager().updateAccessToCard(cCardId);
							}
						}
					}
					for (ObjectId lockedId : lockedCards) {
						execAction(new UnlockObject(lockedId));
					}
				}
				infoCardTo = " inside child cards under attribute '" + attrcodeChild + "'";
			} else {
				throw new DataException("factory.list", new Object[] {linkedAttrId} );
			}

		} else {
			// �� ������ ������ �� ����� -> ����������� ��������� ������ ��������...
			logger.debug(" coping attributes inside card "+ cardId.getId());
			execAction(new LockObject(cardId));
			try {
				// ������� ������� ���� ��� ��������, � ������� �������� �������� �����, ����� � ���������� ���������� ��� ���������
				if (updateAccessList){
					cleanAccessList(cardId);
				}
				// ������� ������ ������ ������ �����, ����� ���� ��������� ���������
				if (!append) {
					oldCount = getJdbcTemplate().update(
						"DELETE FROM attribute_value WHERE attribute_code=? AND card_id = ?",
						new Object[] { attrcodeTo, cardId.getId() },
						new int[]{ Types.VARCHAR, Types.NUMERIC }
					);
				}
				copiedCount = getJdbcTemplate().update(
					"INSERT INTO attribute_value (card_id, attribute_code, \n" +
					"	number_value, string_value, date_value, value_id, another_value) \n" +
					"SELECT v.card_id, aTo.attribute_code, v.number_value, v.string_value, \n" +
					"	v.date_value, v.value_id, v.another_value \n" +
					"FROM attribute_value v \n" +
					"	JOIN attribute aFrom on aFrom.attribute_code=v.attribute_code \n" +
					"	JOIN attribute aTo on aTo.attribute_code=? \n" +
					"		and aTo.data_type=aFrom.data_type \n" +
					"WHERE v.card_id=? AND v.attribute_code=? " +
					/* ��������� �������� �� ������������ ��������*/
					"\t 	and not exists( \n"+
					"\t 					select 1 from attribute_value av \n"+
					"\t 					where	av.attribute_code = aTo.attribute_code \n"+
					"\t 							and av.card_id = v.card_id \n"+
					"\t 							and coalesce(av.number_value, -1) = coalesce(v.number_value, -1) \n"+
					"\t 							and coalesce(av.string_value, '') = coalesce(v.string_value, '') \n"+
					"\t 							and coalesce(av.date_value, '1970-01-01') = coalesce(v.date_value, '1970-01-01') \n"+
					"\t 							and coalesce(av.value_id, -1) = coalesce(v.value_id, -1) \n"+
					"\t 							and coalesce(av.another_value, '') = coalesce(v.another_value, '') \n"+
					"\t 	)"
					,
					new Object[] {  attrcodeTo,	cardId.getId(),	attrcodeFrom },
					new int[] { Types.VARCHAR,	Types.NUMERIC,	Types.VARCHAR}
				);
				// ��������� ������� ���� ��� ��������, � ������� �������� �������� �����, ����� � ���������� ���������� ��� ���������
				if (updateAccessList){
					recalculateAccessList(cardId);
				}
				infoCardTo = " of the same card";
			} finally {
				execAction(new UnlockObject(cardId));
			}		
		}

		logger.info( copiedCount + " value(s) of attribute '" + attrcodeFrom +
				"' of card "+ cardId.getId() +" did replace " + oldCount +
				" old value(s) of attribute '" + attrcodeTo + "' " + infoCardTo);

		return getResult();
	}

	/**
	 * @throws DataException
	 */
	private boolean checkCardConditions(ObjectId cardId)
		throws DataException {
		if (conditions == null || conditions.isEmpty())
			return true;
		final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(Card.class);
		cardQuery.setAccessChecker(null);
		cardQuery.setId(cardId);
		final Card card = getDatabase().executeQuery( getSystemUser(), cardQuery);
		return checkConditions(conditions, card);
	}

	@Override
	public void setParameter(String name, String value) {
		if (PARAM_CHILDREN.equalsIgnoreCase(name) ||
			PARAM_LINK.equalsIgnoreCase(name)) {
			attrCodeOrKeyChildren = value;
		} else if (name.startsWith(PARAM_ATTR_CONDITION)) {
			try {
				final AttributeSelector selector = AttributeSelector.createSelector(value);
				this.conditions.add( selector);
				this.nameConditions.put(selector, name);
			} catch (DataException ex) {
				ex.printStackTrace();
			}
		} else if (name.startsWith(PARAM_PATH_CONDITION)) {
			final String[] desc = value.trim().split("=");
			if (desc.length > 1) { // ���� ������� �� �������, ���������� ��������
				String condName = desc[0].trim();
				String path = desc[1].trim();
				this.pathCond.put(condName, path);
			}
		} else {
			super.setParameter(name, value);
		}
	}

	protected Card execListProject( ObjectId cardId, ObjectId backLinkAttrId,
			UserData user) throws DataException
	{
		final ListProject action = new ListProject();
		action.setCard( cardId);
		action.setAttribute( backLinkAttrId);
		final List<Card> cards = CardUtils.execSearchCards(action, getQueryFactory(), getDatabase(), user);
		return (cards == null || cards.isEmpty()) ? null : cards.get(0);
	}

	protected List<Card> execCardListProject( ObjectId cardId, ObjectId backLinkAttrId,
			UserData user) throws DataException
	{
		final ListProject action = new ListProject();
		action.setCard( cardId);
		action.setAttribute( backLinkAttrId);
		final List<Card> cards = CardUtils.execSearchCards(action, getQueryFactory(), getDatabase(), user);
		return (cards == null || cards.isEmpty()) ? null : cards;
	}

	/**
	 * ��������� ��������� �� ������� conds ��� �������� card.
	 * @param conds ������ ������� ��� ��������
	 * @param card �������� ������� ���������
	 * @return true, ���� ������� ��������� (� ��� ����� ���� �� ��� �����),
	 * false, �����.
	 * @throws DataException
	 */
	private boolean checkConditions(List<BasePropertySelector> conds, Card card) throws DataException {
		if (conds == null || card == null) return true;
		BooleanParser parser = FactoryBooleanParser.create(booleanExpression, FactoryBooleanParser.Parser.POLISH);
		HashMap<String, Boolean> conditionMap = new HashMap<String, Boolean>();
		for (BasePropertySelector cond: conds) {
			String nameCondition = nameConditions.get(cond);
			Boolean isSatis = true;
			// ���� ��� ������� ����� ����, ��������� ������� �� ��������
			// �������� ����� �� �������
			if (pathCond.containsKey(nameCondition)) {
				String path = pathCond.get(nameCondition);
				final String[] attrs = path.split("->");
				final LinkedList<ObjectId> attrList = new LinkedList<ObjectId>();
				for (String attr : attrs) {
					attrList.add(makeObjectId(attr, CardLinkAttribute.class));
				}
				if (!walkWithCondition(card, attrList, cond)) {
					logger.debug("Card " + card.getId().getId()
							+ " did not satisfy condition " + cond);
					// return false;
					isSatis = false;
				}
			} else {
				if (!cond.satisfies(card)) {
					logger.debug("Card " + card.getId().getId()
							+ " did not satisfies condition " + cond);
					isSatis = false;
				}
			}
			conditionMap.put(nameCondition, isSatis);
		}
		if (conditionMap.isEmpty())
			return true;
		boolean resumeExpr = true;
		// ���� ��� ������� - ��������� ������ "�"
		if (null == parser) {
			for (boolean exp : conditionMap.values()) {
				if (!exp) {
					resumeExpr = false;
					break;
				}
			}
		} else {
			resumeExpr = parser.calculate(conditionMap);
		}
		return resumeExpr;
	}
	
    protected ObjectId makeObjectId(String typeCode, Class<?> defType) {
		Class<?> type;
		String code;
		final String[] desc = typeCode.trim().split(":");
		if (desc.length == 1) { // ��� ("xxx:") �� ������
			type = defType;
			code = desc[0].trim();
		} else {
			type = AttrUtils.getAttrClass(desc[0].trim());
			code = desc[1].trim();
		}
		return ObjectIdUtils.getObjectId(type, code, false);
	}
	
	protected boolean walkWithCondition(Card card, LinkedList<ObjectId> attrList, BasePropertySelector cond) throws DataException {
		final List<ObjectId> cardsEndLayer = calculateChildren(card, attrList);
		// ��������� ������� �� ���� �������� ���������
		boolean isSatisfies = cardsEndLayer.isEmpty();
		for(ObjectId cardId : cardsEndLayer) {
			Card cardEndLayer = fetchCard(cardId);
			// ���� ���� �� ���� �������� �������� �������� - ������� �������� �������� (�������� '���')
			if (cond.satisfies(cardEndLayer))
				isSatisfies = true;
		}
		return isSatisfies;
	}
}