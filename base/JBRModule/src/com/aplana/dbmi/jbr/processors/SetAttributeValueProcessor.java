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
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.AttributeSelector;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.LongAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.BasePropertySelector;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.QueryBase.QueryExecPhase;

public class SetAttributeValueProcessor
		extends ProcessCard
		// implements Parametrized
{

	private static final long serialVersionUID = 1L;

	protected static final String DEST_ATTRIBUTE_ID_PARAM = "attributeId";
	protected static final String DEST_VALUE_PARAM = "value";
	protected static final String SRC_LINK_ID_PARAM = "linkId";
	protected static final String SRC_IS_REVERSED_LINK_PARAM = "isReversedLink";
	protected static final String ONLY_CURRENT_CARD = "onlyCurrentCard";
	protected static final String PARAM_ATTR_CONDITION = "attr_condition";

	/**
	 * ������ ���������� ��������� (�� ����� {@link:EUPDATEKIND} ):
	 * 		no_save		��� ���������� (���-���������������),
	 * 		save_card 	���������� �������� (default),
	 * 		save_attr	���������� ������ ���������� ��������.
	 */
	protected static final String DEST_UPDATE_KIND_PARAM = "saveKind";
	protected enum EUPDATEKIND {
		NO_SAVE,
		SAVE_CARD,
		SAVE_ATTR
	}
	static final EUPDATEKIND DEFAULT_EUPDATEKIND = EUPDATEKIND.NO_SAVE;
	protected final List<BasePropertySelector> conditions = new ArrayList<BasePropertySelector>();
	/*
	final static Map<String, EUPDATEKIND> cfgKindMap = new HashMap<String, EUPDATEKIND>(3);
	{
		cfgKindMap.put("", EUPDATEKIND.NO_SAVE);
		cfgKindMap.put("NOSAVE", EUPDATEKIND.NO_SAVE);
		cfgKindMap.put("SAVECARD", EUPDATEKIND.SAVE_CARD);
		cfgKindMap.put("SAVEATTR", EUPDATEKIND.SAVE_ATTR);
	}
	 */
	/**
	 * �������� ��� ���������� ����-��� ��������� ���������.
	 */
	static final protected EUPDATEKIND getSaveKind(String value){
		if (value == null || "".equals(value.trim()))
			return DEFAULT_EUPDATEKIND;
		final EUPDATEKIND result = Enum.valueOf( EUPDATEKIND.class, value.trim().toUpperCase());
		return result;
	}

	private static final String INVALID_NUMBER_VALUE_0_FOR_ATTRIBUTE_1 =
		"Invalid integer string ''{0}'' -> 0 is assigned as value of attribute ''{1}''";

	private static final String INVALID_DATE_VALUE_0_FOR_ATTRIBUTE_1 =
		"Invalid date string ''{0}'' -> NULL is assigned as date value of attribute ''{1}''";

	private ObjectId attributeId = null;
	private String value = null;

	private ObjectId linkId = null;
	private boolean isReversedLink = false;
	private boolean onlyCurrentCard = false;

	// ������ ���������� ��������� ...
	protected EUPDATEKIND saveKind = DEFAULT_EUPDATEKIND;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.aplana.dbmi.service.impl.Parametrized#setParameter(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public void setParameter(String name, String value) {
		if (DEST_ATTRIBUTE_ID_PARAM.equalsIgnoreCase(name)) {
			this.attributeId = IdUtils.smartMakeAttrId(value, StringAttribute.class);
		} else if (DEST_VALUE_PARAM.equalsIgnoreCase(name)) {
			this.value = value;
		} else if (SRC_LINK_ID_PARAM.equalsIgnoreCase(name)) {
			this.linkId = IdUtils.smartMakeAttrId(value, CardLinkAttribute.class);
		} else if (SRC_IS_REVERSED_LINK_PARAM.equalsIgnoreCase(name)) {
			this.isReversedLink = Boolean.parseBoolean(value);
		} else if (DEST_UPDATE_KIND_PARAM.equalsIgnoreCase(name)) {
			this.saveKind = getSaveKind(value);
		} else if (ONLY_CURRENT_CARD.equalsIgnoreCase(name)) {
			this.onlyCurrentCard = Boolean.parseBoolean(value);
		} else if (name.startsWith(PARAM_ATTR_CONDITION)) {
			try {
				final AttributeSelector selector = AttributeSelector
					.createSelector(value);
				this.conditions.add(selector);
			} catch (DataException ex) {
				logger.error(ex.getMessage());
			}
		} else {
			// throw new IllegalArgumentException("Unsupported parameter: " + name);
			// logger.warn( MessageFormat.format("Unsupported parameter ''{0}''=''{1}''", name, value));
			super.setParameter( name, value);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.aplana.dbmi.service.impl.ProcessorBase#process()
	 */
	@Override
	public Object process() throws DataException {
		if (attributeId == null || value == null) {
			logger.error("Not all mandatory parameters were set");
			throw new DataException("jbr.card.configfail");
		}
		final Set<ObjectId> workingCardIds;
		if (this.onlyCurrentCard)
		{
			workingCardIds = Collections.singleton(getCardId());
		}
		else
		{
			workingCardIds = getWorkingCardIds();
		}

		final Card card = this.getCard();

		if (card == null) {
			logger.debug( "Card is null -> exiting ");
			return null;
		}

		// ���� ������ ������������ �������, �� ��������� �� � ��
		if (isMainDocConditionSet() && (validateAndCalculateParent(card) == null)) {
			return null;
		}


		for (ObjectId workingCardId : workingCardIds)
		{
			Card workingCard = null;
			if( (workingCardId != null) && (getCurExecPhase().equals(QueryExecPhase.POSTPROCESS)) )
			{
				workingCard = fetchCard( workingCardId );
			}
			else
			{
				if( workingCardIds.size() == 1 )
					workingCard = getCard(); // ������, ������� �������� ��� �� ��������� � ����
									  // ��� ���������� ���������� � ���-����
			}
			if ((card.getId()==null&&!checkCardConditons(card))||(card.getId()!=null&&!checkCardConditons(card.getId()))) {
				if( logger.isWarnEnabled() )
					logger.warn("Card " + (card.getId()!=null?card.getId().getId():card)
				    + " did not satisfies coditions. Exiting");
			    return null;
			}
			if (!conditions.isEmpty())
				if( logger.isDebugEnabled() )
					logger.debug("Card " + (card.getId()!=null?card.getId().getId():card) + " satisfies coditions");
			final Attribute destAttr = workingCard.getAttributeById(attributeId);
			if (destAttr == null) {
				logger.warn( "Card "+ workingCard.getId() + " has no attribute "+ destAttr);
				continue;
			}
			if( setValue( workingCard, destAttr, value) )
				updateCard( workingCard, destAttr );
			else{
				if( logger.isInfoEnabled() )
					logger.info( MessageFormat.format("Attribute ''{0}'' of card ''{1}'' not changed",
						destAttr.getId(), workingCard.getId() ));
			}
		}
		return null;
	}

	/**
	 * @param card
	 * @throws DataException
	 */
	protected void updateCard(Card card, Attribute attr) throws DataException {
		switch(saveKind) {
			case NO_SAVE: {
				if( logger.isDebugEnabled() )
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
		if (logger.isTraceEnabled())
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
		if (logger.isDebugEnabled())
			logger.debug("Card saved : " + card.getId() );
	}


	/**
	 * @param card
	 * @param attr
	 * @throws DataException
	 */
	protected void saveAttribute(Card card, Attribute attr) throws DataException {
		if (card == null || attr == null) return;
		if (logger.isDebugEnabled())
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
		if (logger.isDebugEnabled())
			logger.debug( MessageFormat.format("Attribute ''{0}'' updated at card ''{1}''",
				attr.getId(), card.getId() ));
	}

	/**
	 * ������� �������� �� link- ��� backlink- ��������.
	 * @param cardId
	 * @param linkAttrId
	 * @param isReverseLink
	 * @return
	 * @throws DataException
	 */
	protected Set<ObjectId> getLinkedCardsSet(ObjectId cardId,
			ObjectId linkAttrId, boolean isReverseLink
		) throws DataException
	{
		final List<Card> cards
			= super.getLinkedCards(cardId, linkAttrId, isReversedLink, null, getSystemUser());
		return (cards == null) ? new HashSet<ObjectId>() : ObjectIdUtils.collectionToSetOfIds(cards);
	}

	final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

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

		if (Attribute.TYPE_LIST.equals(destAttr.getType())) {
			final ReferenceValue referenceValue = new ReferenceValue();
			referenceValue.setId((Long) ObjectIdUtils.getObjectId(
					ReferenceValue.class, value, true).getId());

			final ListAttribute destListAttr = (ListAttribute) destAttr;
			/*
			final boolean isEqual =
				(referenceValue.getId() == null && destListAttr.getValue() == null)
				|| (	referenceValue.getId() != null
						&& referenceValue.getId().equals(destListAttr.getValue().getId()));
			if (isEqual) return false;
			*/
			destListAttr.setValue(referenceValue);
		} else if (Attribute.TYPE_STRING.equals(destAttr.getType())
					|| Attribute.TYPE_TEXT.equals(destAttr.getType())
					|| Attribute.TYPE_HTML.equals(destAttr.getType())
				) {
			// (!) TextAttribute is derived from StringAttribute ...
			// (!) HtmlAttribute is derived from TextAttribute ...

			final StringAttribute destStrAttr = (StringAttribute) destAttr;
			final boolean isEqual =
						(value == null && destStrAttr.getValue() == null)
						|| ( value != null && value.equals(destStrAttr.getValue()));
			if (isEqual) return /*��� ���������*/false;

			destStrAttr.setValue(value);
		} else if (Attribute.TYPE_INTEGER.equals(destAttr.getType())) {
			int intVal = 0;
			try {
				intVal = (value == null || "".equals(value.trim()))
								? 0
								: Integer.parseInt(value.trim());
			} catch (NumberFormatException ex) {
				if (logger.isWarnEnabled())
					logger.warn( MessageFormat.format( INVALID_NUMBER_VALUE_0_FOR_ATTRIBUTE_1,
						value, destAttr.getId()), ex);
			}
			final IntegerAttribute destIntAttr = (IntegerAttribute) destAttr;
			final boolean isEqual = (intVal == destIntAttr.getValue());
			if (isEqual) return /*��� ���������*/false;

			destIntAttr.setValue( intVal);
		} else if (Attribute.TYPE_LONG.equals(destAttr.getType())) {
			long intVal = 0;
			try {
				intVal = (value == null || "".equals(value.trim()))
								? 0
								: Long.parseLong(value.trim());
			} catch (NumberFormatException ex) {
				if (logger.isWarnEnabled())
					logger.warn( MessageFormat.format( INVALID_NUMBER_VALUE_0_FOR_ATTRIBUTE_1,
						value, destAttr.getId()), ex);
			}
			final LongAttribute destIntAttr = (LongAttribute) destAttr;
			final boolean isEqual = (intVal == destIntAttr.getValue());
			if (isEqual) return /*��� ���������*/false;

			destIntAttr.setValue( intVal);
		} else if (Attribute.TYPE_DATE.equals(destAttr.getType())) {
			Date dtVal = null;
			try {
				dtVal = (value == null || "".equals(value.trim()))
								? null
								: dateFormat.parse(value.trim());
			} catch (ParseException ex) {
				if (logger.isWarnEnabled())
					logger.warn( MessageFormat.format( INVALID_DATE_VALUE_0_FOR_ATTRIBUTE_1,
						value, destAttr.getId()), ex);
			}

			final DateAttribute destDateAttr = (DateAttribute) destAttr;
			final boolean isEqual =
						(dtVal == null && destDateAttr.getValue() == null)
						|| ( dtVal != null && dtVal.equals(destDateAttr.getValue()));
			if (isEqual) return /*��� ���������*/false;

			destDateAttr.setValue( dtVal);
		} else if (Attribute.TYPE_CARD_LINK.equals(destAttr.getType())) {
			long longVal = 0;
			try {
				// ���� ���� ��������� ID ������� ��������, �� ������ ��������� ������� �-������� ���� ID
				if (Card.ATTR_ID.getId().toString().equalsIgnoreCase(value)){
					longVal = Long.parseLong(card.getId().getId().toString());
				} else
				longVal = (value == null || "".equals(value.trim()))
								? 0
								: Long.parseLong(value.trim());
			} catch (NumberFormatException ex) {
				if (logger.isWarnEnabled())
					logger.warn( MessageFormat.format( INVALID_NUMBER_VALUE_0_FOR_ATTRIBUTE_1,
						value, destAttr.getId()), ex);
			}
			final CardLinkAttribute destCLAttr = (CardLinkAttribute) destAttr;
			boolean isEqual = false;
			boolean isNull = (destCLAttr.getIdsArray() != null) ? false : true;
			if (!isNull)
				for( ObjectId val: destCLAttr.getIdsArray())
				{
					if ( ((Long)val.getId()).longValue() == longVal )
					{
						isEqual = true;
						break;
					}
				}
			if (isEqual) return /*��� ���������*/false;

			if ( destCLAttr.isMultiValued() == true || (destCLAttr.isMultiValued() == false && destCLAttr.isEmpty()) )
				destCLAttr.addLinkedId(longVal);
			else
				if (logger.isInfoEnabled())
					logger.info( MessageFormat.format("Not processed: Card ''{0}'' attribute ''{1}'' isMultiValued ''{2}'' isEmpty ''{3}''",
						card.getId(), destAttr.getId(), destCLAttr.isMultiValued(),destCLAttr.isEmpty()));
		} else if (Attribute.TYPE_PERSON.equals(destAttr.getType())) {
			long longVal = 0;
			try {
				// ���� ���� ��������� ID ������� ��������, �� ��� person-��������� �������� �������� person_id ������� �������� (���� �������� - �� ������� (����������), �� �������� � �������)
				if (Card.ATTR_ID.getId().toString().equalsIgnoreCase(value)){
					longVal = getJdbcTemplate().queryForLong("select person_id from person where card_id = ?", new Object[] { card.getId().getId() }, 
							new int[] {Types.NUMERIC});
				} else
				longVal = (value == null || "".equals(value.trim()))
								? 0
								: Long.parseLong(value.trim());
			} catch (NumberFormatException ex) {
				if (logger.isWarnEnabled())
					logger.warn( MessageFormat.format( INVALID_NUMBER_VALUE_0_FOR_ATTRIBUTE_1,
						value, destAttr.getId()), ex);
			} catch (Exception ex){
				if (logger.isErrorEnabled())
					logger.error( "Error while get value for PersonAttribute", ex);
				return false;
			}
			
			final PersonAttribute destCLAttr = (PersonAttribute) destAttr;
			boolean isEqual = false;
			boolean isNull = (destCLAttr.getValues() != null) ? false : true;
			if (!isNull)
				for( Object val: destCLAttr.getValues())
				{
					Person p = (Person)val;
					if ( ((Long)p.getId().getId()).longValue() == longVal )
					{
						isEqual = true;
						break;
					}
				}
			if (isEqual) return /*��� ���������*/false;

			if ( destCLAttr.isMultiValued() == true || (destCLAttr.isMultiValued() == false && destCLAttr.isEmpty()) ){
				Collection values = destCLAttr.getValues();
				if (values == null){
					destCLAttr.setPerson(new ObjectId(Person.class, longVal));
				} else {
					values.add(DataObject.createFromId(new ObjectId(Person.class, longVal)));
				}
			}else
				if (logger.isInfoEnabled())
					logger.info( MessageFormat.format("Not processed: Card ''{0}'' attribute ''{1}'' isMultiValued ''{2}'' isEmpty ''{3}''",
						card.getId(), destAttr.getId(), destCLAttr.isMultiValued(),destCLAttr.isEmpty()));
		}
		else
			throw new UnsupportedOperationException(String.format(
					"Type %s of attribute is not supported by this processor",
					destAttr.getType()));

		if (logger.isDebugEnabled())
			logger.debug( MessageFormat.format("Card ''{0}'' attribute ''{1}'' has new value ''{2}''",
				card.getId(), destAttr.getId(), destAttr.getStringValue()) );

		return true; /* ���� ��������� */
	}

	protected Set<ObjectId> getWorkingCardIds() throws DataException {
		if (linkId != null) {
			return getLinkedCardsSet(getCardId(), linkId, isReversedLink);
		}
		return Collections.singleton(getCardId());
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
     */
	private boolean checkConditions(List<BasePropertySelector> conds, Card card) {
		if (conds == null || card == null)
		    return true;
		for (BasePropertySelector cond : conds) {
		    if (!cond.satisfies(card)) {
				if (logger.isDebugEnabled())
					logger.debug("Card " + (card.getId()!=null?card.getId().getId():card)
				+ " did not satisfies codition " + cond);
			return false;
		    }
		}
		return true;
	}
}
