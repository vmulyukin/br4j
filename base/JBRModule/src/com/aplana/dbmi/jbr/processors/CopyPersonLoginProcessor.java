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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.AttributeSelector;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.BasePropertySelector;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;
/**
 * ���������, �������������� ����������� ������ ������������, ������������ �� ������� ������-��������, ���� ������ ������������ ������� �������� ������� ����������
 * ������� ���������:
 * sourceAttrId - (������ ��� _ID) ��� �������� �������� 
 * destAttrId - ��� ���������� ��� ���������� ��������, ���� ����������� �����
 * saveKind - (NO_SAVE/SAVE_CARD/SAVE_ATTR) - ��������� ��������� ��� ��� � � ����� ����
 * attr_condition_n - ������� ������� ����������
 * @author ynikitin
 */

public class CopyPersonLoginProcessor extends ProcessCard {
	private static final long serialVersionUID = 1L;
	
	private static final String INVALID_NUMBER_VALUE_0_FOR_ATTRIBUTE_1 =
		"Invalid integer string ''{0}'' -> 0 is assigned as value of attribute ''{1}''";

	private static final String INVALID_DATE_VALUE_0_FOR_ATTRIBUTE_1 =
		"Invalid date string ''{0}'' -> NULL is assigned as date value of attribute ''{1}''";

	protected static final String DEST_ATTRIBUTE_ID_PARAM = "destAttrId";
	protected static final String SRC_ATTRIBUTE_ID_PARAM = "sourceAttrId";
	protected static final String PARAM_ATTR_CONDITION = "attr_condition";
	protected static final String DEST_UPDATE_KIND_PARAM = "saveKind";
	protected enum EUPDATEKIND {
		NO_SAVE,
		SAVE_CARD,
		SAVE_ATTR
	}
	static final EUPDATEKIND DEFAULT_EUPDATEKIND = EUPDATEKIND.NO_SAVE;

	private ObjectId sourceAttrId;
	private ObjectId destAttrId;
	protected final List<BasePropertySelector> conditions = new ArrayList<BasePropertySelector>();
	/**
	 * �������� ��� ���������� ����-��� ��������� ���������.
	 */
	static final protected EUPDATEKIND getSaveKind(String value){
		if (value == null || "".equals(value.trim()))
			return DEFAULT_EUPDATEKIND;
		final EUPDATEKIND result = Enum.valueOf( EUPDATEKIND.class, value.trim().toUpperCase());
		return result;
	}
	// ������ ���������� ��������� ...
	protected EUPDATEKIND saveKind = DEFAULT_EUPDATEKIND;

	@Override
	public Object process() throws DataException {

		if(sourceAttrId == null || destAttrId==null) {
			logger.info("Mandatory parameter isn't set. Exiting.");
			return null;
		}

		Card card = getCard();
		if ((card.getId()==null&&!checkCardConditons(card))||(card.getId()!=null&&!checkCardConditons(card.getId()))) {
		    logger.warn("Card " + (card.getId()!=null?card.getId().getId():card)
			    + " did not satisfies coditions. Exiting");
		    return null;
		}
		if (!conditions.isEmpty())
			if( logger.isDebugEnabled() )
				logger.debug("Card " + (card.getId()!=null?card.getId().getId():card) + " satisfies coditions");

		final String login;
		if (sourceAttrId.equals(Card.ATTR_ID)){
			final List<String> attrValues = super.getJdbcTemplate().queryForList(
					"select person_login from person " +
					"where card_id=?",
					new Object[]{card.getId().getId()},
					new int[]{Types.NUMERIC},
					String.class);
			if (attrValues==null||attrValues.isEmpty()){
				logger.info("Do not find person login for card "+card.getId().getId()+". Mayby is not person card.");
				return null;
			} else
				login = attrValues.get(0);
		} else {
			PersonAttribute personAttr = (PersonAttribute)card.getAttributeById(sourceAttrId);
			Long personId = (Long)personAttr.getPerson().getId().getId();
			final List<String> attrValues = super.getJdbcTemplate().queryForList(
					"select person_login from person " +
					"where person_id=?",
					new Object[]{personId},
					new int[]{Types.NUMERIC},
					String.class);
			if (attrValues==null||attrValues.isEmpty()){
				logger.info("Do not find person login for person "+personId+". Mayby is not exists person in system.");
				return null;
			} else
				login = attrValues.get(0);
		}
		StringAttribute destAttr = (StringAttribute)card.getAttributeById(destAttrId);
		if (destAttr == null) {
			logger.warn( "Card "+ card.getId().getId() + " has no attribute "+ destAttr.getId());
			return null;
		}
		if( setValue( card, destAttr, login) )
			updateCard( card, destAttr );
		else
			logger.info( MessageFormat.format("Attribute ''{0}'' of card ''{1}'' not changed",
					destAttr.getId(), card.getId() ));
		
		return null;
	}

	@Override
	public void setParameter(String name, String value) {
		if (DEST_ATTRIBUTE_ID_PARAM.equalsIgnoreCase(name)) {
			this.destAttrId = IdUtils.smartMakeAttrId(value, StringAttribute.class);
		} else if (SRC_ATTRIBUTE_ID_PARAM.equalsIgnoreCase(name)) {
			this.sourceAttrId = IdUtils.smartMakeAttrId(value, IntegerAttribute.class);
		} else if (DEST_UPDATE_KIND_PARAM.equalsIgnoreCase(name)) {
			this.saveKind = getSaveKind(value);
		} else if (name.startsWith(PARAM_ATTR_CONDITION)) {
			try {
				final AttributeSelector selector = AttributeSelector
					.createSelector(value);
				this.conditions.add(selector);
			} catch (DataException ex) {
				ex.printStackTrace();
			}
		} else {
			super.setParameter( name, value);
		}
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

	private boolean checkCardConditons(Card card) throws DataException {
		if (conditions == null || conditions.isEmpty())
		    return true;
		return checkConditions(conditions, card);
	}
    /**
     * ��������� ��������� �� ������� conds ��� �������� card.
     *
     * @param conds
     * @param card
     * @return true, ���� ������� ��������� (� ��� ����� ���� �� ��� �����),
     *         false, �����.
     * @throws DataException
     */
	private boolean checkConditions(List<BasePropertySelector> conds, Card card) {
		if (conds == null || card == null)
		    return true;
		for (BasePropertySelector cond : conds) {
		    if (!cond.satisfies(card)) {
			logger.debug("Card " + (card.getId()!=null?card.getId().getId():card)
				+ " did not satisfies codition " + cond);
			return false;
		    }
		}
		return true;
	}	

	protected void updateCard(Card card, Attribute attr) throws DataException {
		switch(saveKind) {
			case NO_SAVE: {
				logger.debug( MessageFormat.format("Attribute ''{0}'' of card ''{1}'' configured not to be saved",
						attr.getId(), card.getId() ));
				break;
			}
			case SAVE_CARD: { saveCard(card); break; }
			case SAVE_ATTR: { saveAttribute( card, attr); break; }
			default:
				logger.error( "Invalid save kind: "+ saveKind);
		}
	}

	private void saveCard(Card card) throws DataException {
		logger.trace("Saving card : " + card.getId() );
		final SaveQueryBase query = getQueryFactory().getSaveQuery(card);
		query.setObject(card);
		boolean unlock = false;
		if (card.getId() != null) {
			execAction(new LockObject(card.getId()));
			unlock = true;
		}
		try {
			if (card.getId() != null) {
				getDatabase().executeQuery(getSystemUser(), query);
			} else {
				getDatabase().executeQuery(getUser(), query);
			}
		} finally {
			if (unlock)
				execAction(new UnlockObject(card.getId()));
		}
		logger.debug("Card saved : " + card.getId() );
	}


	/**
	 * @param card
	 * @param attr
	 * @throws DataException
	 */
	protected void saveAttribute(Card card, Attribute attr) throws DataException {
		if (card == null || attr == null) return;
		logger.debug( MessageFormat.format("Saving attribute ''{0}'' into card ''{1}''",
				attr.getId(), card.getId() ));

		final OverwriteCardAttributes action = new OverwriteCardAttributes();
		action.setCardId(card.getId());
		action.setAttributes(Collections.singletonList(attr));
		action.setInsertOnly(false);

		boolean unlock = false;
		if (card.getId() != null) {
			execAction(new LockObject(card.getId()));
			unlock = true;
		}
		try {
			super.execAction(action, getSystemUser());
		} finally {
			if (unlock)
				execAction(new UnlockObject(card.getId()));
		}
		logger.debug( MessageFormat.format("Attribute ''{0}'' updated at card ''{1}''",
				attr.getId(), card.getId() ));
	}

	/**
	 * @param destAttr
	 * @param value
	 * @return ���-�� true, ���� ���� ��������� ��������� �������� ��������;
	 * false, ���� ��������� ��� (��� ���������� ����������).
	 */
	protected boolean setValue(Card card, Attribute destAttr, String value)
	{
		if (destAttr == null)
			return false;

		if (Attribute.TYPE_STRING.equals(destAttr.getType())
					|| Attribute.TYPE_TEXT.equals(destAttr.getType())
				) {
			// (!) TextAttribute is derived from StringAttribute ...

			final StringAttribute destStrAttr = (StringAttribute) destAttr;
			final boolean isEqual =
						(value == null && destStrAttr.getValue() == null)
						|| ( value != null && value.equals(destStrAttr.getValue()));
			if (isEqual) return /*��� ���������*/false;

			destStrAttr.setValue(value);
		} else
			throw new UnsupportedOperationException(String.format(
					"Type %s of attribute is not supported by this processor",
					destAttr.getType()));

		logger.debug( MessageFormat.format("Card ''{0}'' attribute ''{1}'' has new value ''{2}''",
				card.getId(), destAttr.getId(), destAttr.getStringValue()) );

		return true; /* ���� ��������� */
	}
}
