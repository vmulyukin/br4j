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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.processors.SetCurrentPersonTo.AttributeWalker.CardEntry;
import com.aplana.dbmi.jbr.processors.SetCurrentPersonTo.AttributeWalker.FilterRuleEntry;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;

/**
 *	������� �������� ������������ � ��������� ������� �������� ��������.
 *	�������� �������� ����� ��� ��������� �������� ������������.
 * 
 * ������ 1: ��������� ���-�� �������� ������������ � ������� 'jbr.resolution.FioSign': 
 			<post-process class="SetCurrentPersonTo">
				<parameter name="cardLinkAttribute" value="jbr.resolution.FioSign"/>
				<parameter name="checkIsLinked" value="true"/>
				<parameter name="userSubordinate" value="BOSS"/>
				<parameter name="addUserIfEmptySubordinate" value="true"/>
			</post-process>

 * ������ 2:
			<!--  ��������� ��� �������� ��������� �������� ('JBR_TCON_INSPECTOR'):
					��������� �� ���� ���������� �������� ���������� ��� ��� ������������� 
					���� ������������, ������� ������ � ���� ���������� ('JBR_INFD_SIGNATORY').
					personattribute.jbr.outcoming.signatory=JBR_INFD_SIGNATORY
					cardlinkattribute.jbr.resolution.FioSign=JBR_INFD_SGNEX_LINK

					544	��������� ��� ��� ������������	JBR_ARM_INSPECTOR	���������/U
			   -->
			<post-process class="SetCurrentPersonTo">
				<parameter name="personAttribute" value="JBR_TCON_INSPECTOR"/>
				<parameter name="checkIsLinked" value="false"/>

				<!-- ���� ������ ������� ��������� ������������ ������������ ������� �������� -->
				<parameter name="userSubordinate" value="BY_RULE"/>
				<parameter name="calcUserPostRule" value="
						link:jbr.resolution.FioSign
						-> $arm
						-> user: JBR_ARM_INSPECTOR
					" />

				<!--  ���� ��������� �� ����� - ������ �� ����������� -->
				<parameter name="addUserIfEmptySubordinate" value="false"/>
			</post-process>
 *	{@link AttributeWalker}
 * @comment RAbdullin
 */
@SuppressWarnings("serial")
public class SetCurrentPersonTo extends ProcessCard {

	public static final ObjectId INACTIVE_USER_STATUS_ID = ObjectId.state("user.inactive");

	/**
	 * (����, �� ����=false) ���� true, �� ����������� �������� ����������� 
	 * ������ ��� ��������� ��������, �.� ��� �����, ��� ����� Card.isLinked==true.
	 */
	private static final String PARAM_CHECK_CURCARD_IS_LINKED = "checkIsLinked";

	/**
	 * (����, ��-����=false) ���� true, �� ���������� �������� �������� (U- ���
	 * C-����) ����� ��������� ������ ��� ������ �������� �������� ��������.
	 */
	private static final String PARAM_CHECK_DEST_IS_EMPTY = "addOnlyIfDestIsEmpty";

	/**
	 * (����, ����=null) ����� ��� ��� �������� U-�������� � ������� ��������.
	 */
	private static final String PARAM_DEST_PERSON_ATTRIBUTE = "personAttribute";
	
	/**
	 * (����, ����=OVERWRITE) ������� ������ ��������� "personAttribute":
	 * �������� �������� ��. {@link ENUM_WRITE_OPERATION}.
	 */
	private static final String PARAM_DEST_PERSON_ATTRIBUTE_WRITE_OPERATION = "personAttributeWriteOperation";

	/**
	 * (����, ����=null) ����� ��� ��� �������� U-�������� � ������ ��������� � ��������� ����.
	 */
	private static final String PARAM_FAR_DEST_PERSON_ATTRIBUTE = "farPersonAttribute";
	/**
	 * (����, ����=OVERWRITE) ������� ������ ��������� "farPersonAttribute":
	 * �������� �������� ��. {@link ENUM_WRITE_OPERATION}.
	 */
	private static final String PARAM_FAR_DEST_PERSON_ATTRIBUTE_WRITE_OPERATION = "farPersonAttributeWriteOperation";

	/**
	 * (����, ����=true) ���� �� ��������� �������� �����
	 * 		true: ��������� �������� �� id (���� ��� �� ����� - �����);
	 * 		false: ������������ getObject/getResult (���� �� ����� ��� 
	 * �������� - �����), ��� ������ ��� ������������� ��� <pre-process>.
	 */
	private static final String PARAM_LOAD_CARD = "loadCard";

	/**
	 * (����, ����=true) ��������� �� ������� �������� �������� ����� ��������� ������� ���������
	 */
	private static final String PARAM_FORCE_SAVE_CARD = "forceSaveCard";

	/**
	 * (����, ����=null) ����� ��� ��� �������� C-��������.
	 */
	public static final String PARAM_DEST_CARDLINK_ATTRIBUTE = "cardLinkAttribute";
	/**
	 * (����, ����=OVERWRITE) ������� ������ ��������� "cardLinkAttribute",
	 * �������� �������� ��. {@link ENUM_WRITE_OPERATION}.
	 */
	private static final String PARAM_DEST_CARDLINK_ATTRIBUTE_WRITE_OPERATION = "cardLinkAttributeWriteOperation";

	/**
	 * �������� ��� ����������� ���� ������ ������ ������� (������������ 
	 * ������������ ������������). 
	 * ��������� �������� ��. ENUM_SUBORDER_STYLE_USER:
	 * 		1) (�� ����) USER: �������� ������������;
	 * 		2) BOSS: (����) ������������ �������� ������������;
	 * 		3) ASSISTENT_FIRST: (����) ������� ���������;
	 * 		4) ASSISTENT_ALL: (������) ���� ����������.
	 * (!) ���� ��� ��������� ���������, �� � ����������� �� ��������� 
	 * "addUserIfEmptySubordinate" ������������� ���� null, ���� ������� ������������. 
	 */
	public static final String PARAM_USER_SUBORDINATE = "userSubordinate";

	/**
	 * ������������ ��� PARAM_USER_SUBORDINATE!=USER, ���� ��� ����� ��� ����������:
	 * 	�������� true (�� ���������): ��������� ���������� ������ ������������,
	 * false: ���������� null. 
	 */
	public static final String PARAM_USE_DEFAULT_USER = "addUserIfEmptySubordinate";

	/**
	 * ������� ��� ��������� ��������� ������������, ����� ��������� ������ ������������.
	 * ���� ����� - ����������� ��������� ������������ ��� ����.
	 */
	public static final String PARAM_CALC_RULE = "calcUserPostRule";
	
	/**
	 * ������ � ���� ������ ����������� �������� � �������� ��� ������ �� ��������,
	 * �� ������� ��������� PARAM_CALC_RULE (���������� �������� ������ ��� CardLink/TypedLink).
	 */
	public static final String PARAM_FILTERS_RULE = "filterPostRule";

	public static final String PARAM_SKIP_INACTIVE_PERSON = "skipInactivePerson";

	/**
	 * ������� ������ �������������� ������ ������������ ��������.
	 */
	public static enum ENUM_SUBORDER_STYLE_USER {
			/**
			 * USER: �������� ������������
			 */
			USER,
			/**
			 * REAL_USER: ������������, ������� � ������ ������ �������� ��� ������������ ��������
			 */
			REAL_USER,
			/**
			 * ������������ �������� ������������
			 */
			BOSS,

			/**
			 * ������� ���������
			 */
			ASSISTENT_FIRST,

			/**
			 * (������) ���� ����������
			 */
			ASSISTENT_ALL,

			/**
			 * ������� ������ ������ � calcUserPostRule ���� 
			 */
			BY_RULE
		};

		/**
		 * �������� ������ � �������� ������� ������ ��������.
		 */
		public static enum ENUM_WRITE_OPERATION {
			/**
			 * ������ ������������ ���� �������� �������� ��� �� ����������� (null)
			 */
			SET_IF_EMPTY,
			/**
			 * ������������ �������� �������� ����� � ����� ������
			 */
			OVERWRITE,
			/**
			 * �������� ����� �������� � ��� ������������� ��������� ��������
			 */
			APPEND
		}

		final HashMap<String, FilterRuleEntry> mapFilterRule = new HashMap<String, FilterRuleEntry>();

		/**
		 * ������������ ������, � ������� �� ������� ��������� � ������� "�� ��������"
		 */
		private boolean isSkipInactivePerson = false;

	@Override
	public Object process() throws DataException {

		final boolean enLoadCard = Boolean.parseBoolean( getParameterTrimmed( PARAM_LOAD_CARD, "true") );
		final boolean forceSaveCard = Boolean.parseBoolean( getParameterTrimmed( PARAM_FORCE_SAVE_CARD, "true") );

		final Collection<Card> farCards = new ArrayList<Card>(5);
		boolean savedAny = false;
		boolean changed = false;
		boolean newBorn = false;

		// ����� ����������� � ���-����, �� ���� ��������� �����������
		final ObjectId cardId = getCardId();
		if (cardId != null) {
			// (!) ��������� ���� �������� ������ ���� � �� ���� � getObject-getResult;
			// card = super.loadCardById( cardId, getUser());
		} else {
			logger.warn( "The card is just created (most probably). I'll use it." );
			newBorn = true;
		}

		final Card card = (enLoadCard && (cardId != null)) ? loadCardById( cardId ): getCard();
		if (card == null) {
			logger.error("The card does not exist. Exit.");
			return null;
		}

		if (getBooleanParameter(PARAM_CHECK_CURCARD_IS_LINKED, false)) {
			final boolean checked = ((CreateCard)getAction()).isLinked() ;
			if (!checked) {
				logger.info("Condition \"checkIsLinked\" is not satisfied.");
				return null;
			}
		}

		// ���� ������ ������������ �������, �� ��������� �� � ��
		if (isMainDocConditionSet() && validateAndCalculateParent(card) == null) {
			return null;
		}

		/*
		 * �������� ��� �������� ������ ������������� ������...
		 */
		final ENUM_SUBORDER_STYLE_USER suborder = Enum.valueOf(
					ENUM_SUBORDER_STYLE_USER.class, 
					getParameterTrimmed(PARAM_USER_SUBORDINATE, ENUM_SUBORDER_STYLE_USER.USER.toString())
				);
		// final Set<Person> persons = getPersonsList( card, suborder, getBooleanParameter(PARAM_USE_DEFAULT_USER, true));

		// ������� �� �������� � �������� � ������������� � ���������� ������ ���� ������ ��������, ���� ������ ������ 
		final CardEntry personsAndCards = getPersonsAndCards( card, suborder, 
				getBooleanParameter(PARAM_USE_DEFAULT_USER, true));

		/*
		 * ���������� �������� ���������
		 */
		final boolean chkDestIsEmpty = getBooleanParameter( PARAM_CHECK_DEST_IS_EMPTY, false);
		String id = getParameterTrimmed(PARAM_DEST_PERSON_ATTRIBUTE, null);
		if (id != null) {
			id = id.trim();
			final ObjectId paId = ObjectIdUtils.getObjectId(PersonAttribute.class, id, false);
			final PersonAttribute destPersonAttr = (PersonAttribute)card.getAttributeById(paId);
			if (destPersonAttr == null) {
				logger.warn(MessageFormat.format(MSG_CARD_0_HAS_NO_ATTRIBUTE_1, card.getId(), paId));
			} else {
				if (chkDestIsEmpty && !destPersonAttr.isEmpty()) {
					// ���������� ��� ����������� (��������) ������� �������...
					logger.info( MessageFormat.format(MSG_CARD_0_HAS_NONEMPTY_ATTR_1, card.getId(), paId));
				} else {
					// ���������� �������� ������ � �������
					final ENUM_WRITE_OPERATION wOp = Enum.valueOf(
							ENUM_WRITE_OPERATION.class, 
							getParameterTrimmed(PARAM_DEST_PERSON_ATTRIBUTE_WRITE_OPERATION, 
											ENUM_WRITE_OPERATION.OVERWRITE.toString()));
					final boolean locChanged = writeToPersonAttribute(destPersonAttr, personsAndCards.getPersons(), wOp);
					if (locChanged) { 
						changed = true;
						if (!newBorn && forceSaveCard) {
							// ��������� �������
							updateAttribute(cardId, destPersonAttr);
							savedAny = true;
						}
					}
				}
			}
		}

		id = getParameterTrimmed(PARAM_DEST_CARDLINK_ATTRIBUTE, null);
		if (id != null){
			final ObjectId clId = ObjectIdUtils.getObjectId(CardLinkAttribute.class,
						id, false);
			final CardLinkAttribute destCardLinkAttr = card.getCardLinkAttributeById(clId);
			if (destCardLinkAttr == null) {
				logger.warn(MessageFormat.format(MSG_CARD_0_HAS_NO_ATTRIBUTE_1, card, clId));
			} else {
				if (chkDestIsEmpty && !destCardLinkAttr.isEmpty()) {
					// ���������� ��� ����������� (��������) ������� �������...
					logger.info( MessageFormat.format(MSG_CARD_0_HAS_NONEMPTY_ATTR_1, card.getId(), clId));
				} else {
					final Set<ObjectId> ids = new HashSet<ObjectId>();
					// ���� ������ �������� ��������, �� ��������� ��, ����� ������� ������
					if (personsAndCards.getCards()!=null&&!personsAndCards.getCards().isEmpty()){
						for (final Iterator<Card> iter = personsAndCards.getCards().iterator(); iter.hasNext(); ) {
							final Card curObject = iter.next();
							final ObjectId currentCardId = curObject.getId(); 
							if (	currentCardId != null 
									&& (currentCardId.getId() != null)
									&& ((Long) currentCardId.getId()).longValue() != 0 )
							{
								logger.info("Found card "+ currentCardId);
								ids.add(currentCardId);
							} else {
								logger.warn("The current card "+ curObject + " is not found or not exist -> skip/remove.");
								iter.remove();
							}
						} // for iter
					} else {
						for (final Iterator<Person> iter = personsAndCards.getPersons().iterator(); iter.hasNext(); ) {
							final Person curObject = iter.next();
							final ObjectId currentCardId = curObject.getCardId(); 
							if (	currentCardId != null 
									&& (currentCardId.getId() != null)
									&& ((Long) currentCardId.getId()).longValue() != 0 )
							{
								logger.info("Found person card "+ currentCardId);
								ids.add(currentCardId);
							} else {
								logger.warn("The person "+ curObject+ " does not have corresponding card -> skip/remove.");
								iter.remove();
							}
						}
					}
					// ���������� �������� ������ � �������
					final ENUM_WRITE_OPERATION wOp = Enum.valueOf(
							ENUM_WRITE_OPERATION.class, 
							getParameterTrimmed(PARAM_DEST_CARDLINK_ATTRIBUTE_WRITE_OPERATION, 
									ENUM_WRITE_OPERATION.OVERWRITE.toString()));
					final boolean locChanged = writeToCardlinkAttribute(destCardLinkAttr, ids, wOp);
					if (locChanged) { 
						changed = true;
						if (!newBorn && forceSaveCard) {
							// ��������� �������
							updateAttribute( cardId, destCardLinkAttr);
							savedAny = true;
						}
					}
				}
			}
		}

		final String path = getParameterTrimmed(PARAM_FAR_DEST_PERSON_ATTRIBUTE, null);
		if (path == null || path.trim().length() == 0) {
			logger.warn( MessageFormat.format( "{0} is not set -> no other cards are used", PARAM_FAR_DEST_PERSON_ATTRIBUTE));
		} else {
			final AttributeWalker walker = new AttributeWalker();
			walker.walkThrought(card, getUser().getPerson(), path);
			final int i = walker.getCardsTrace().size();
			//���� ������������� - ��� ���������� ��������� ����������� �������� 
			// � ��� �������� �������, ���� ���������� ���������
			final CardEntry ce = walker.getCardsTrace().elementAt(i-2);
			final ObjectId dstPersonAttrId = ce.getAttrId();
			if (dstPersonAttrId == null){
				logger.error("Destination attribute is not set. I'll do nothing.");
			} else if(!PersonAttribute.class.isAssignableFrom(dstPersonAttrId.getType())) {
					logger.error("Destination attribute has not PersonAttribute type. " +
							"I'll do nothing.");
			} else {
				// ���������� �������� ������ � �������
				final ENUM_WRITE_OPERATION wOp = Enum.valueOf(
						ENUM_WRITE_OPERATION.class, 
						getParameterTrimmed(PARAM_FAR_DEST_PERSON_ATTRIBUTE_WRITE_OPERATION, 
											ENUM_WRITE_OPERATION.OVERWRITE.toString()));
				farCards.addAll(writeToFarCards(ce.getCards(), dstPersonAttrId, personsAndCards.getPersons(), wOp));
			}
		}

		if (newBorn){
			logger.warn("This card is just created so I won't save it.");
			return null;
		}

//		(2011/01/27, RuSA) ������ ���������� ���� ��������, ��������� (����) 
//		������ ���������� ���������� ��������.
//		if (changed && forceSaveCard) {
//			try {
//				final SaveQueryBase sq = getQueryFactory().getSaveQuery(card);
//				sq.setObject(card);
//				getDatabase().executeQuery( getUser(), sq);
//				savedAny = true;
//			} catch (Exception ex) {
//				logger.error( "Exception saving card "+ card.getId()+ "\n" + ex);
//				throw new DataException( "general.unique", new Object[] {card.getId()}, ex);
//			}
//		}

		if (!farCards.isEmpty()){
			for (Card cCard : farCards){
				try {
					super.saveCard(cCard, (cCard.getId()!=null?getSystemUser():getUser()));
					savedAny = true;
				} catch (Exception ex) {
					logger.error( "Exception saving card "+ cCard.getId()+ "\n" + ex, ex);
					throw new DataException(ex);
				}
			}
		}

		if (!savedAny)
			logger.info( MessageFormat.format( "Nothing was saved into db (changes of card were {0}performed).", 
					(changed ? "" : "NOT "))); // possible changed but not saved

		return card.getId();
	}

	/**
	 * ��������� ���������� �������� ��� �������� cardId.
	 * @param cardId
	 * @param attr
	 * @throws DataException 
	 */
	private void updateAttribute(ObjectId cardId, Attribute attr) 
		throws DataException 
	{
		final OverwriteCardAttributes action = new OverwriteCardAttributes();
		action.setCardId(cardId);
		action.setAttributes(Collections.singletonList(attr));
		action.setInsertOnly(false);

		super.execAction(new LockObject(cardId));
		try {
			super.execAction(action, getSystemUser());
		} finally {
			super.execAction(new UnlockObject(cardId));
		}
	}


	/**
	 * ���� ����� ������������ � ������ writeToFarCards ��� ���������� �������� ��������� ��� ������.
	 * @param �ard ����������� ��������
	 * @return true ���� �������� �������� ��� ������, false � ��������� ������
	 */
	protected boolean isEligibleToWrite(Card card){
		return card != null;
	}
	
	/**
	 * �������� ������ ������ � ������ �� �������� ��������� allCards.
	 * @param allCards ������ ��������� ����������� �������� � ������� ��������� �������� ������ ������
	 * @param dstPersonAttrId ��� ��������, �������� �������� ��������� ����������
	 * @param values ������ ������ ��� ������
	 * @param wOp ����������� �������� ������
	 * @return ��������� ������� ��������� ��������
	 */
	protected Collection<Card> writeToFarCards(Collection<Card> allCards,
					ObjectId dstPersonAttrId, Collection<Person> values,
					ENUM_WRITE_OPERATION wOp){
		Collection<Card> cardsToWrite = new ArrayList<Card>();
		for (Card cCard : allCards){
			if (isEligibleToWrite(cCard)){
				Attribute attr = cCard.getAttributeById(dstPersonAttrId);
				if (attr != null){
					if (writeToPersonAttribute((PersonAttribute)attr, values, wOp))
						cardsToWrite.add(cCard);
				}else{
					logger.error("No attribute "+dstPersonAttrId.getId()+
							" in card #"+cCard.getId().getId()+
							", template "+cCard.getTemplate().getId());
				}
			}
		}
		return cardsToWrite;
	}
	
	protected boolean writeToPersonAttribute(PersonAttribute pa, Collection<Person> values,
			ENUM_WRITE_OPERATION wOp){
		// ��������� ��������� ��������� ������ � �������
		switch (wOp){
		case SET_IF_EMPTY:
			{	// PersonAttribute ����� �������� �� ����� ��������� ��������
				if (pa.getValues()==null || pa.getValues().isEmpty()){
					pa.setValues(values);
					return true;
				}
				break;	
			}
		case OVERWRITE:
			{
				pa.setValues(values);
				return true;
			}
		case APPEND:
			{	// PersonAttribute ����� �������� �� ����� ��������� ��������
				if (pa.getValues()==null)
					pa.setValues( new HashSet<Person>());
				final Set<Person> tmp = new HashSet<Person>( CardUtils.getAttrPersons(pa));
				tmp.addAll(values);
				pa.setValues(tmp);
				return true;
			}
		}
		return false;
	}

	protected boolean writeToCardlinkAttribute(CardLinkAttribute clAttr, 
			Collection<ObjectId> values,
			ENUM_WRITE_OPERATION wOp)
	{
		// ��������� ��������� ��������� ������ � �������
		switch (wOp){
		case SET_IF_EMPTY:
			{	// CardlinkAttribute ����� �������� ����� ����� ��������� ��������
				if (clAttr.isEmpty()){
					clAttr.setIdsLinked(values);
					return true;
				}
				break;	
			}
		case OVERWRITE:
			{
				clAttr.setIdsLinked(values);
				return true;
			}
		case APPEND:
			{
				clAttr.addIdsLinked(values);
				return true;
			}
		}
		return false;
	}

	/**
	 * �������� ������ ������ � ������ �������� ��� ���������� � �������� ����� � ���� ��������� CardEntry: 
	 * @param booleanParameter: true, ����� ��� ��������� ������ ��������� 
	 * ���������� �������� ������������
	 * @return CardEntry - 
	 * @throws DataException 
	 */
	private CardEntry getPersonsAndCards(
			Card card,
			ENUM_SUBORDER_STYLE_USER usrSuborder,
			boolean userAsDefault) throws DataException 
	{
			final Set<Person> persons = new HashSet<Person>();
			final AttributeWalker aw = new AttributeWalker();
			CardEntry result = aw.new CardEntry();
			logger.info( "suborder style is " + usrSuborder);
			switch (usrSuborder) {

				case BOSS: { // ����� ����� ��� �������� ������������...
					final List<Card> arm =
						CardUtils.getArmSettingsCardsByAssistent( getUser().getPerson(), 
								getQueryFactory(), getDatabase(), getSystemUser());
					// ������� �������� �������� ��� � �������� �������� � �������
					if(arm != null) {
						for(Iterator<Card> itr = arm.iterator(); itr.hasNext();) {
							Card c = itr.next();
							if(c != null && c.getState() != null
									&& (c.getState().equals(ObjectId.predefined(CardState.class, "doublet"))
											|| c.getState().equals(ObjectId.predefined(CardState.class, "poruchcancelled"))
										)
							) {
								itr.remove();
							}
						}
					}
					addPersonsFromAttribute( persons, arm, CardUtils.ATTR_BOSS, false);
					result.setPersons(persons);
					break;
				}

				case ASSISTENT_ALL:
				case ASSISTENT_FIRST: { // ����� ���������� ��� �������� ������������...
					final List<Card> arm =
						CardUtils.getArmSettingsCardsByBoss( getUser().getPerson(), 
								getQueryFactory(), getDatabase(), getSystemUser());
					addPersonsFromAttribute( persons, arm, CardUtils.ATTR_ASSISTANT, 
							usrSuborder == ENUM_SUBORDER_STYLE_USER.ASSISTENT_FIRST);
					result.setPersons(persons);
					break;
				}

				case USER:{
					persons.add(getPrimaryUser().getPerson());
					result.setPersons(persons);
					break;
				}
				
				case REAL_USER:{
					persons.add( getPrimaryRealUser() != null ? getPrimaryRealUser().getPerson() : getPrimaryUser().getPerson());
					result.setPersons(persons);
					break;
				}
				
				case BY_RULE: {
					final String path = getParameterTrimmed(PARAM_CALC_RULE, null);
					if (path == null || path.trim().length() == 0) {
						logger.warn("Rule is empty -> user list is empty");
					} else {
						final AttributeWalker walker = new AttributeWalker();
						FilterRuleEntry filterRuleEntry = null;
						try {
							final String filtersRule = getParameterTrimmed(PARAM_FILTERS_RULE, null);
							if (filtersRule == null || filtersRule.trim().length() == 0) {
								logger.warn("Filter rule is empty");
							} else {
								this.mapFilterRule.clear();
								// ��������� ��������� ������� ��� ������� ��������
								final String[] sIds = filtersRule.trim().split(FilterRuleEntry.FILTER_SEPARATOR);
								if (sIds != null) {
									for (String s: sIds) {
										if (s == null || s.length() == 0) continue;
										int index = s.indexOf(FilterRuleEntry.ATTRIBUTE_SEPARATOR);
										String attr = s.substring(0, index).trim(),
											filterUnit = s.substring(index + 1).trim();
										filterRuleEntry = walker.new FilterRuleEntry(filterUnit, attr);
										this.mapFilterRule.put(attr, filterRuleEntry);
									}
								}
							}
						} catch (Exception e) {
							logger.error("Error processing filter rule !", e);
						}
						walker.walkThrought(card, getUser().getPerson(), path);
						final CardEntry entry = walker.getLastCardEntry();
						
						// (YNikitin, 2011/01/19) ��������� ������ ������, ���� �� ����� � ��� ������������ ������ ��������
						// ToDo: �������� �������� � ����������� �� ���� �������� �������� ����������� ���� ������� ��������, ���� ������� ������
						if (entry != null){
							if ( entry.getPersons() == null || entry.getPersons().isEmpty()) 
							{
								// ���� ��� ������� ������ -> ������� ������� ������� ������ 
								// �������� - ��� ������ ������������ -> � ������� �������� 
								// ������ ...
								final Set<Person> found = CardUtils.getPersonsByCards(entry.getCards(), 
										getQueryFactory(), getDatabase(), getSystemUser() );
								entry.setPersons(found);
							}
						}
						result = entry;
					}
					break;
				}

				/*
				default:
					logger.error("Unsupported sub-order switch: "+ usrSuborder);
					throw new DataException( "factory.action", new Object[]{ usrSuborder});
					// return null;
				 */
			} 

			/*
			 * ���� ����� �������
			 */
			if (result!=null) {
				// �������� ������ ������
				if (result.getPersons()!=null&&!result.getPersons().isEmpty()){
					// (!) ��������� ������ ������������ �������� �� �������������...
					for (Person person : result.getPersons()) {
						if (person.getCardId() == null) {
							// ��������� ������������ ��������...
							long cardId = 0;
							try {
								cardId = getJdbcTemplate().queryForLong(
										"select coalesce(p.card_id, 0) from person p where p.person_id=?", 
										new Object[] { person.getId().getId() },
										new int[] {Types.NUMERIC} 
								);
							} catch (Exception ex) {
								cardId = 0;
							}
							if (cardId != 0){
								logger.debug( "got personal card "+ cardId +" for person "+ person.getId().getId() );
							person.setCardId( new ObjectId( Card.class, cardId));
						} else {
							logger.error( " can not find personal card for user "+person.getId().getId());
							person.setCardId(null);
						}
					}
				}
			} else if (userAsDefault) {
				logger.info(" list of person is empty -> using current person " + getUser().getPerson() );
				result.getPersons().add( getUser().getPerson());
			}
		}

		return result;
	}


	/**
	 * �������� � ��������� ������ ������ �� U-�������� ��������.
	 * @param result ������� ������� ������ 
	 * @param list ������ ��������
	 * @param personAttrId id U-��������, � ��������� � ��������� list[]
	 * @param firstonly true, ����� �������� ������ ������������ �������� (�� 
	 * ������ �������� ��������), false, ����� �������� ��� �������� ���� ��������
	 */
	@SuppressWarnings("unchecked")
	private void addPersonsFromAttribute(Set<Person> result, List<Card> list,
			ObjectId personAttrId, boolean firstonly) 
	{
		if (list != null) {
			for (Card card : list) {
				final PersonAttribute attr = (PersonAttribute) card.getAttributeById(personAttrId);
				if (attr != null 
						&& (attr.getValues() != null) 
						&& !attr.getValues().isEmpty() 
					) {
					if (isSkipInactivePerson) {
						result.addAll(getActiveOnlyPersons(attr.getValues()));
					} else {
						result.addAll( attr.getValues() );
					}
					if (firstonly) return;
				}
			}
		}
	}
	
	//TODO ����� ������������ Person.isActive(), �� ��� ����� ���� �������� Fetcher � DoSearch + �������� ���������, ������� ��������� ���� ���� � ������� person 
	/**
	 * ��������� �������� ������ Person, ��������� ��������������� �������� �������(����������) ��� ������ ������� � ��������� ���, 
	 * � ������� ������ �������� �� ����� "���������� ������������"
	 * @param all
	 * @return
	 */
	private List<Person> getActiveOnlyPersons(Collection<Person> all) {
		List<Person> filteredPersonList = new ArrayList<Person>();
		try {
			if (all != null && all.size() > 0) {
				List<ObjectId> personCardIds = new ArrayList<ObjectId>();
				Map<ObjectId, Person> personMap = new HashMap<ObjectId, Person>();
				for (Person p : all) {
					personCardIds.add(p.getCardId());
					personMap.put(p.getCardId(), p);
				}
				Search search = new Search();
				search.setByCode(true);
				search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(personCardIds));

				SearchResult.Column col = new SearchResult.Column();
				col.setAttributeId(Card.ATTR_STATE);
				search.setColumns(Collections.singletonList(col));

				SearchResult result = execAction(search);

				for (Card c : result.getCards()) {
					if (!INACTIVE_USER_STATUS_ID.getId().equals(c.getState().getId())) {
						filteredPersonList.add(personMap.get(c.getId()));
					}
				}
			}
		} catch (Exception e) {
			logger.error("Cannor retreive Persons' cards due to " + e.getMessage(), e);
		}
		return filteredPersonList;
	}

	/**
	 * ����� ��� ������� �� ������ ���������, ����� �������� �������� (������ 
	 * � ���������). 
	 * ��� "����" �� ���������:
	 * 		<������� ��� ������> {-> <������� ��� ������>}*
	 * ���
	 *   <������� ��� ������> ::= <U/C/E-�������> | <������>
	 *   <U/C/E-�������> ::= ��� ��� ����� �������� � ����:
	 *   		���: ���_���_����� 
	 *   ��������: 'link:jbr.resolutions' 
	 *      ��� 'backLink:JBR_IMPL_RESOLUT'
	 *      ��� 'jbr.resolutions'
	 *   // backlinkattribute.jbr.resolutions=JBR_IMPL_RESOLUT
	 *   		���_���_�����
	 *   <������> ::= $getBoss | $getAssistFirst | $getAssist | $getAssistAll | $arm
	 *     1) $getBoss ����� ����� �� �������� 
	 *     2) $getAssistFirst ��� $getAssist:
	 *     3) $getAssistAll 
	 *     4) $arm ������� ������� �������� �� �������� ��� ��� �������� ������������;
	 *  �������� ������� �� "����":
	 *    ����� ���������� "����" �� "������� ��������" ����������� �������� 
	 * ��������, �� "�������� ������������" - ������������, �� ����� �������� 
	 * ������ ������ ���������. �� ���� ��������� "����" ��� ������ "������� 
	 * ��������" ����� ���������� �������� ��������� � "����". 
	 *
	 * @author RAbdullin
	 *
	 */
	class AttributeWalker {

		// protected final Log logger = LogFactory.getLog(getClass());

		/**
		 * ������� ��������.
		 */
		public static final String PREFIX_TAG = "$";

		/**
		 * ��������� ����� ������� �������� �� �������� ��� ������� �������������.
		 * ����� ������� ������ ������ ����� �������� �� ���� �� ������, �
		 * ����� ������� ������ �������� �� �������� ��� ��� ���.
		 */
		private static final String TAG_GET_ARM = "$arm";

		/**
		 * �����-��� ��� ��������� ����� �������� ������������. 
		 */
		public static final String TAG_GET_BOSS = "$getBoss";

		/**
		 * ������� ��������������� ��� ��������� ����������� �������� ������������. 
		 */
		private static final String TAG_PREFIX_GET_ASSIST = "$getAssist";

		/**
		 * �������� �������������� ��� ��������� ���� ����������� �������� ������������. 
		 */
		private static final String TAG_GET_ASSIST_ALL = TAG_PREFIX_GET_ASSIST + "All";

		// ���������� ����
		private List<String> paths = new ArrayList<String>();

		// ���� �� ��������, ���������� ��� ��������� attribute 
		final private Stack<CardEntry> cardsTrace = new Stack<CardEntry>();

		// private UserData dsUser; 
		// private QueryFactory dsFactory;
		// private Database dsDatabase;

		public AttributeWalker() {
		}

		/**
		 * ��������� �������� � ������ �������, ���������� ��� ������� �� paths.
		 * @return
		 */
		public CardEntry getStartCardEntry() {
			return (cardsTrace.isEmpty()) ? null : cardsTrace.elementAt(0);
		}

		CardEntry setStartCard(Card card) {
			cardsTrace.clear();
			if (card == null) return null;
			final CardEntry result = new CardEntry(card); 
			cardsTrace.add( result);
			return result;
		}

		/**
		 * ��������� �������� � �������, ���������� ��� ������� �� paths.
		 * @return
		 */
		public CardEntry getLastCardEntry() {
			return (cardsTrace.isEmpty()) ? null : cardsTrace.lastElement();
		}

		/**
		 * ��� ���������� �������� � �������� ��� ����� paths.
		 * @return
		 */
		public Stack<CardEntry> getCardsTrace() {
			return this.cardsTrace;
		}

		public List<String> getAttrPaths() {
			return this.paths;
		}


		/**
		 * ������ � �������� startCard, ������ �� ���� path � �������� � 
		 * �������� cardTrace �������� �������� � ���������.
		 * @param startCard  �������� ��������
		 * @param path �������� � ����: attr{->attr}
		 * @param startPerson ������� �������� �������
		 * fetchCards true, ���� ���� ��������� �������� ���������
		 * false: ������� ������ ������ ��������.
		 * @throws DataException 
		 */
		@SuppressWarnings("synthetic-access")
		public void walkThrought( final Card startCard, final Person startPerson, 
				final String path) throws DataException
		{
			/**
			 *  ������� �������� ��� ������������ "����"...
			 */
			this.paths.clear();
			{
				final CardEntry entry = setStartCard( startCard);
				// �������� ������ id ������ ...
				if (startPerson != null) {
					entry.setPersons( new HashSet<Person>(5));
					entry.getPersons().add( startPerson);
				}
			}

			/**
			 * ��������� ���������/��������� � "����" ...
			 */
			if (startCard == null || path == null)
				return;
			final String[] sPaths = path.split("->");

			final StringBuffer done = new StringBuffer();
			/**
			 * ������ �� ���� �� ���������-��������...
			 */
			try {
				for (int i=0; i < sPaths.length; i++) {
					final String s = sPaths[i].trim();
					done.append("->").append(s);
					if (s.length() < 1) continue;
					this.paths.add(s);

					// ������� �������� �������� �������� � �������������
					Person curPerson = null;
					if (s.startsWith(PREFIX_TAG)) {
						final CardEntry curEntry = this.cardsTrace.lastElement();
						curEntry.setTag(s);
						curPerson = getCurPerson();
						if (curPerson == null) {
							logger.warn( MessageFormat.format("No current person at attribute calc path ''{0}'' -> no persons found", done.toString()));
							return;
						}
					}

					CardEntry newEntry = null;
					if (s.equalsIgnoreCase(AttributeWalker.TAG_GET_BOSS)) {
						// ������� ������� �������� �� ��� (��� ������� ������������ 
						// �������� �����������) � ����� � ��� ������ ...
						final List<Card> arm =
							CardUtils.getArmSettingsCardsByAssistent( getCurPerson(), 
									getQueryFactory(), getDatabase(), getSystemUser());
						newEntry = regPersonsFromAttribute( arm, CardUtils.ATTR_BOSS, false);
					} else if (s.toLowerCase().startsWith(TAG_PREFIX_GET_ASSIST.toLowerCase())) {
						// ������� ������� �������� �� ��� (��� ������� ������������ 
						// �������� ������) � ����� � ��� ������ ��� ���� �����������...
						final List<Card> arm =
							CardUtils.getArmSettingsCardsByBoss( getCurPerson(), 
									getQueryFactory(), getDatabase(), getSystemUser());
						newEntry = regPersonsFromAttribute( arm, CardUtils.ATTR_ASSISTANT, 
								!s.equalsIgnoreCase(TAG_GET_ASSIST_ALL));
					} else if (s.equalsIgnoreCase(TAG_GET_ARM)) {
						// ������� � ��������� ��� ��������(��) ������������(��) ...
						newEntry = regArmCards( getLastCardEntry().getPersons());
					} else {
						// �������, ��� s ��� U/B/C/E-������� � ������� �������� ...
						newEntry = regAttr(s);
					}
					if (newEntry == null) {
						logger.warn( MessageFormat.format("No new level cards at attribute calc path ''{0}'' -> no persons found", done.toString()));
						return;
					}
					this.cardsTrace.add( newEntry);
				}
			} catch (Exception e) {
				logger.error(MessageFormat.format("Error at attribute calc path ''{0}''", done.toString()),e);
				throw new DataException(e);
			}
		}


		/**
		 * �������� �������� ������������. ����������� ��� ��������������, ���� 
		 * ��� ��� ������ ������ - "��������".
		 * @return ������� ������������ (� ������ ����������� �� "����")
		 * @throws DataException 
		 */
		private Person getCurPerson() throws DataException {
			final CardEntry entry = getLastCardEntry();

			// jbr.processors.SetCurrentPersonTo.active.user.absent=��� ��������� ������������
			// jbr.processors.SetCurrentPersonTo.active.user.several=�������������� ����� ���� �������� ������������ (������ �� {0})
			if (entry == null)
				return null;
			if ( entry.getPersons() == null || entry.getPersons().isEmpty()) 
			{
				// throw new DataException( "jbr.processors.SetCurrentPersonTo.active.user.absent");

				// ���� ��� ������� ������ -> ������� ������� ������� ������ 
				// �������� - ��� ������ ������������ -> � ������� �������� 
				// ������ ...
				final Set<Person> found = CardUtils.getPersonsByCards(entry.getCards(), 
						getQueryFactory(), getDatabase(), getSystemUser() );
				entry.setPersons(found);
				if (found == null || found.isEmpty())
					return null;
			}
			if (entry.getPersons().size() != 1)
				throw new DataException( "jbr.processors.SetCurrentPersonTo.active.user.several", 
						new Object[]{entry.getPersons().size()});
			return entry.getPersons().iterator().next();
		}


		/**
		 * �������� � ������� ������ ������ ���� ������ �� U-�������� ��������.
		 * @param list: ������ ��������.
		 * @param personAttrId: id U-��������, � ��������� � ��������� list[].
		 * @param firstonly: true, ����� �������� ������ ������������ �������� 
		 * (�� ������ �������� ��������), 
		 * false, ����� �������� ��� �������� ���� ��������.
		 * @return ������ ������ 
		 * @throws DataException 
		 */
		private CardEntry regPersonsFromAttribute( List<Card> list, 
				ObjectId personAttrId, boolean firstonly) 
			throws DataException
		{
			final CardEntry result = new CardEntry();
			if (list != null) {
				final Set<Person> persons = new HashSet<Person>(10);

				// result.setCards(list);
				for (Card card : list) {
					final Card fullCard = loadCardById(card.getId(), getSystemUser());
					result.addCard(fullCard);
					// final PersonAttribute attr = (PersonAttribute) card.getAttributeById(personAttrId);
					final Collection<Person> shortList 
						= CardUtils.getAttrPersons( fullCard, personAttrId, false);
					if (shortList != null) {
						persons.addAll( shortList );
						if (firstonly) break;
					}
				}
				result.setPersons(persons);
			}
			return result;
		}


		/**
		 * ��������� �������� ��� ��� ��������� ������������� �� ������.
		 * @param person
		 * @return ��������� ��������� � ���������� ��� � ������� �������������,
		 * ��������� �� ��� �� ������, ��� � personList.
		 * @throws DataException 
		 */
		private CardEntry regArmCards(Collection<Person> personList) 
			throws DataException 
		{
			if (personList == null || personList.isEmpty())
				return null;

			final CardEntry newEntry = new CardEntry();
			newEntry.getPersons().addAll(personList);
			for (Person person : personList) {
				final List<Card> armCards =
					CardUtils.getArmSettingsCardsByBoss( person, 
							getQueryFactory(), getDatabase(), getSystemUser());
				final CardEntry bossEntry = regPersonsFromAttribute( armCards, CardUtils.ATTR_BOSS, false);
				if (bossEntry != null)
					newEntry.addCards( bossEntry.getCards());
			}
			return newEntry;
		}

		/**
		 * ���������:
		 *  1) ������� �� ������ - ���� ������� ���� B/C/E;
		 *  2) ���� ������� ���� Person -> ��������� ������� �������� ��� ������;
		 * @param s
		 * @return
		 * @throws DataException 
		 */
		@SuppressWarnings("synthetic-access")
		private CardEntry regAttr(String s) throws DataException 
		{
			final ObjectId attrId = IdUtils.smartMakeAttrId(s, CardLinkAttribute.class);
			int index = s.indexOf(':');
			String id_attr = s.substring(index + 1).trim();
			final CardEntry entry = getLastCardEntry();
			entry.setAttrId(attrId); // ��������� �������, �� �������� ��� ������� ������...

			final CardEntry newEntry = new CardEntry();
			for (Card card : entry.getCards()) 
			{
				final Attribute attr = card.getAttributeById(attrId);
				if (attr == null) {
					logger.warn( MessageFormat.format( MSG_CARD_0_HAS_NO_ATTRIBUTE_1, 
						new Object[] { card, attrId} ));
					continue;
				}

				if (BackLinkAttribute.class.isAssignableFrom(attr.getClass())) {
					final List<Card> list = loadAllBackLinks( card.getId(), attrId, getSystemUser());
					newEntry.addCards(list);
				} else if (PersonAttribute.class.isAssignableFrom(attr.getClass())) {
					final Collection<Person> persons = CardUtils.getAttrPersons( (PersonAttribute) attr);
					if (persons == null || persons.isEmpty())
						continue;
					newEntry.getPersons().addAll(persons);
					for (Person person : persons) {
						if (person != null) {
							final Card c = loadCardById( person.getCardId(), getSystemUser());
							newEntry.addCard(c);
						}
					}
				} else { // CardLink/TypedLink
					final Set<ObjectId> cardIds = getCardIdsList( attr);
					if (cardIds == null)
						continue;
					for (ObjectId id: cardIds) {
						final Card c = loadCardById( id, getSystemUser());
						try {
							if ( !mapFilterRule.isEmpty() && mapFilterRule.get(id_attr) != null) {
								String _state = c.getState().getId().toString();
								String _template = c.getTemplate().getId().toString();
								FilterRuleEntry filterRuleEntry = mapFilterRule.get(id_attr);
								String state_in_map = filterRuleEntry.getStatesRule().get(_state);
								String template_in_map = filterRuleEntry.getTemplatesRule().get(_template);
								logger.debug("filterRuleEntry: " + filterRuleEntry + 
										";\r\n _state: " + _state + 
										";\r\n _template: " + _template + 
										";\r\n state_in_map: " + state_in_map + 
										";\r\ntemplate_in_map: "+ template_in_map + ";\r\n");
								if ( 
										(_state != null) && (!"".equals(_state)) &&
										(_template != null) && (!"".equals(_template)) &&
										(state_in_map != null) && (!"".equals(state_in_map)) &&
										(template_in_map != null) && (!"".equals(template_in_map)) &&
										(state_in_map.equals(_state)) && 
										(template_in_map.equals(_template)) 
									)
								{
									newEntry.addCard(c);
								}
							} else
								newEntry.addCard(c);
						} catch (Exception e) {
							logger.error("An error occurred while processing attribute of filter: " + s +"; for card: " + id + "; \r\n", e);
						}
					}
				}
			}
			return newEntry;
		}


		/**
		 * ����� ��� �������� ��������, ������ ������ ��������� � ��� � �������� 
		 * (������� ��� ����������� ��� �������� � ������ ��������, ��� ������� 
		 * �� ���� �������� ������ {@link AttributeWalker.walkThrought})
		 * @author RAbdullin
		 *
		 */
		public class CardEntry {

			private ObjectId attrId;
			private String tag;
			final private Set<Card> cards = new HashSet<Card>(3);
			final private Set<Person> persons = new HashSet<Person>(3);

			public CardEntry() {
			}

			public CardEntry(Card card) {
				super();
				addCard(card);
			}

			public CardEntry( Collection<Card> cards, ObjectId attrId) {
				super();
				this.attrId = attrId;
				this.addCards(cards);
			}

			public void addCard(Card card) {
				if (card != null)
					cards.add(card);
			}

			public void addCards(Collection<Card> list) {
				if (list != null && !list.isEmpty()){
					for ( Card c: list)
						addCard(c);
				}
			}

			public ObjectId getAttrId() {
				return this.attrId;
			}

			public void setAttrId(ObjectId attrId) {
				this.attrId = attrId;
			}

			public Set<Person> getPersons() {
				return this.persons;
			}

			public void setPersons(Set<Person> set) {
				this.persons.clear();
				if (set != null)
					this.persons.addAll(set);
			}

			public String getTag() {
				return this.tag;
			}

			public void setTag(String tag) {
				this.tag = tag;
			}

			public Set<Card> getCards() {
				return this.cards;
			}

			public void setCards( Collection<Card> col) {
				this.cards.clear();
				if (col != null)
					this.cards.addAll(col);
			}
		}
		
		/**
		 * ����� ��� �������� ������� �� �������� � ���������� ��� ����������� �������� 
		 * (��������, �� ������� ��������� ������ �������) �� ���� calcUserPostRule  
		 * (������� ��� ����������� ��� �������� � ������ ��������, ��� ������� 
		 * �� ���� �������� ������ {@link AttributeWalker.walkThrought})
		 * @author PPanichev
		 *
		 */
		public class FilterRuleEntry {
			
			public String linkKey;
			final private HashMap<String, String> statesRule = new HashMap<String, String>();
			final private HashMap<String, String> templatesRule = new HashMap<String, String>();
			private static final String STATE_SEPARATOR = "_STATE";
			private static final String TEMPLATE_SEPARATOR = "_TEMPLATE";
			private static final String END_SYMBOL = ";";
			private static final String COMMA_SYMBOL = ",";
			private static final String FILTER_SEPARATOR = "&";
			private static final String ATTRIBUTE_SEPARATOR = ">";
			
			public FilterRuleEntry() {
			}
			
			public FilterRuleEntry(String filterUnit, String linkKey) {
				this.linkKey = linkKey;
				parseFilterUnit(filterUnit, TEMPLATE_SEPARATOR, this.templatesRule);
				parseFilterUnit(filterUnit, STATE_SEPARATOR, this.statesRule);
			}
			
			public HashMap<String, String> getStatesRule() {
				return statesRule;
			}

			public HashMap<String, String> getTemplatesRule() {
				return templatesRule;
			}

			private void parseFilterUnit(String filterUnit, String separator, HashMap<String, String> mapRule) {
				String unit = parse(filterUnit, separator);
				unit = unit.replaceAll(" ", "");
				String[] _unit = unit.split(COMMA_SYMBOL);
				if (TEMPLATE_SEPARATOR.equals(separator)) {
					for (String key : _unit) {
						ObjectId template_element = ObjectId.predefined(
							    Template.class, key);
						String tmpl = template_element.getId().toString();
						mapRule.put(tmpl, tmpl);
					}
				} else {
					for (String key : _unit) {
						mapRule.put(key, key);
					}
				}
			}
			
			private String parse(String unit, String separator) {
				int index = unit.indexOf(separator);
				String element = unit.substring(index + separator.length()).trim();
				int index_end = element.indexOf(END_SYMBOL);
				String result = element.substring(0, index_end);
				return (result != null && !"".equals(result)) ? result.trim().replaceFirst("=", "") : null;
			}
		}

	}

	@Override
	public void setParameter(String name, String value) {
		if (PARAM_SKIP_INACTIVE_PERSON.equals(name)) {
			isSkipInactivePerson = Boolean.valueOf(value);
		} else {
			super.setParameter(name, value);
		}
	}
}
