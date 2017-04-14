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
/**
 * 
 */
package com.aplana.dbmi.jbr.processors;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.processors.EnsureSignCard.PersonsOrder.PersonDatum;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.SaveQueryBase;

/**
 * @author RAbdullin
 * ��������� ������������ ������ �������� �������� �������,
 * ������������ �������� ������� ������������ ����� ��������� ��������
 * ��������� � ���������-���������.
 */
public class EnsureSignCard extends ProcessCard 
{

	/**
	 * id �������� �� ������� (��������) ��������, ��� ������� ����� ����������� 
	 * ������� ������������ � ������ ������ checkingListAttrId.
	 * ���������� ����: U, C, B, E. 
	 */
	// personattribute.jbr.outcoming.signatory=JBR_INFD_SIGNATORY
	protected ObjectId srcListAttrId = 
			ObjectId.predefined( PersonAttribute.class, "jbr.outcoming.signatory");


	/**
	 * ������� �� ��������� ��������� � ���������� ������� ����������.
	 */
	// integerattribute.jbr.sign.order=JBR_SIGN_NUMBER
	protected ObjectId linkedOrderNumberAttrId = 
		ObjectId.predefined(IntegerAttribute.class, "jbr.sign.order");


	/**
	 * ������� �� ��������� ��������� � ����������� �������������.
	 * ����� ����� ��� U ��� C.
	 */
	// personattribute.jbr.sign.person=JBR_SIGN_RESPONSIBLE
	protected ObjectId linkedPersonAttrId = 
		ObjectId.predefined( PersonAttribute.class, "jbr.sign.person");


	/**
	 * id �������� �� �������, � ������� ���� ����� ����� ������������ (��� 
	 * �������� �� srcListAttrIs).
	 * (!) ���������� ���: C (E). 
	 */
	// cardlinkattribute.jbr.sign.set=JBR_SIGN_SIGNING
	protected ObjectId checkingListAttrId = 
			ObjectId.predefined( CardLinkAttribute.class, "jbr.sign.set");


	/**
	 * id ������� ��� ����������� ��������.
	 */
	// template.jbr.sign=365
	protected ObjectId newCardTemplateId = 
			ObjectId.predefined(Template.class, "jbr.sign");
	
	/**
	 * id ������� �������
	 */
	protected ObjectId poruchcancelledStatusId = 
			ObjectId.predefined(CardState.class, "poruchcancelled");

	/**
	 * ������� �������� �������� (����� �������������� ��� ����� � �������)
	 */
	protected Card activeCard;

	/**
	 * true, ���� ������ � activeCard ��������.
	 */
	protected boolean isChangedActiveCard;


	/**
	 * ���� �� ��������� ���������� ���������� ��������.
	 */
	// protected boolean saveAfterChanges = false;
	
	/**
	 * ������ ��� ����������� ��������� �� �������� �������� � �����.
	 */
	final protected AttributesCopyInfo copyInfo = new AttributesCopyInfo();
	
	private static List<ObjectId> SIGN_STATES_IGNORE = new ArrayList<ObjectId>() {{
		add(ObjectId.predefined(CardState.class, "poruchcancelled"));
	}};

	@Override
	public Object process() throws DataException 
	{
		if (!prepare())
			return null;

		// ������� ������...
		// 
		final Attribute destAttr = activeCard.getAttributeById(this.checkingListAttrId);
		if (destAttr == null) {
			logger.warn( MessageFormat.format( MSG_CARD_0_HAS_NO_ATTRIBUTE_1, 
					this.activeCard, this.checkingListAttrId));
			return null;
		}

		// ��������� ������ ��� ��������...
		// (�����: id ������������ ��������  ->  �������) 
		final Set<Person> srcPersons = getSourcePersons();

		//  ������� ������ � ����������, � ������� ������ ���� ������� �� srcPersons ...
		// (� ���� ������������ �������� ��� ��������� ��������)
		final CardLinkAttribute destListAttr = (CardLinkAttribute) destAttr;

		ensureOtherCards( destListAttr, srcPersons);

		if (!this.isChangedActiveCard) {
			logger.info( "Signer(s) from attribute " + this.srcListAttrId 
					+ " of card " + this.activeCard.getId() 
					+ " already found inside the list of attribute "+ this.checkingListAttrId
				);
			return null;
		}

		// (!) ���������� ����������� �������� ... 
		super.saveCard(this.activeCard, getSystemUser());
		reloadCard();

		return null;
	}


	/**
	 * @return true, ���� ���������� ������ � false ���� ���.
	 * @throws DataException 
	 */
	private boolean prepare() throws DataException 
	{
		// �������� �������� �������...
		this.activeCard = super.getCard();
		if (this.activeCard == null) {
			logger.warn("No active card and no id -> exiting");
			return false;
		}
		
		

		this.isChangedActiveCard = false;
		return true;
	}


	private Set<Person> getSourcePersons() 
		throws DataException 
	{
		// ��������� ������ �������� ��� ��������...
		final Set<Person> sourcePerons = 
			super.getPersonsList(this.activeCard, this.srcListAttrId, true);
		if (sourcePerons == null || sourcePerons.isEmpty())
			return new HashSet<Person>();

		// �������� ������ ��, ������� ���� ��������� ����� ...
		this.filterSourceCards(sourcePerons);
		if (sourcePerons.isEmpty()) {
			logger.info("After filtering no persons found -> exiting");
			return new HashSet<Person>();
		}

		return sourcePerons;
	}


	/**
	 * @param destAttr: ������� ������ ��� ������������ ����������� � srcPersons
	 * @param srcPersons: ������ ������, ������� ������� ���� ���������� � destAttr.
	 * ������������ �������� ������ ��� ����, ����� ����� ����������� ����� 
	 * ����� ��������� �� ���������.
	 */
	private void ensurePersons( PersonAttribute destAttr, final Set<Person> srcPersons) 
	{
		final Set<Person> current = (destAttr.getValues() == null)
					? new HashSet<Person>()
					: new HashSet<Person>( CardUtils.getAttrPersons(destAttr));
		// �������� �������...
		for (Person person: srcPersons) {
			if (!current.contains(person)) {
				this.isChangedActiveCard = true;
				current.add(person);
			}
		}
		// ������ ������ � ��������...
		if (this.isChangedActiveCard)
			destAttr.setValues(current);
	}


	/**
	 * @param destAttr: ������� ������ ��� ������������ ����������� � srcPersons
	 * @param srcPersons: ������ ������, ������� ������� ���� ���������� � destAttr.
	 * ������������ �������� ������ ��� ����, ����� ����� ����������� ����� 
	 * ����� ��������� �� ���������.
	 */
	private void ensurePersonCards( CardLinkAttribute destAttr,
			Set<Person> srcPersons) 
	{
		// ������ id ������������ ��������...
		final Collection<ObjectId> ids = CardUtils.getAttrLinks(destAttr);
		final Set<ObjectId> current = (ids == null)
					? new HashSet<ObjectId>()
					: new HashSet<ObjectId>(ids);
		// �������� �������...
		for (Person person: srcPersons) {
			if (!current.contains(person.getCardId())) {
				this.isChangedActiveCard = true;
				current.add(person.getCardId());
			}
		}
		// ������ ������ � ��������...
		if (this.isChangedActiveCard)
			destAttr.setIdsLinked(current);
	}


	/**
	 * ��������� ��� ���� �� ������ ���� ����-��� �������� �, ��� �������������,
	 * ������� �������������.
	 * @param destAttr: ������� ������� ��� ������������ ����������� � srcPersons
	 * @param srcPersons: ������ ������, ������� ������� ���� ���������� � destAttr.
	 * @throws DataException 
	 */
	private void ensureOtherCards(CardLinkAttribute destAttr,
			Set<Person> srcPersons 
		) throws DataException 
	{
		//  ������������ ������ ������, �������� � ���������� ...
		final PersonsOrder curOrder = this.getCurrentPersonsOrder(
					this.activeCard.getId(), this.checkingListAttrId,
					this.linkedPersonAttrId, this.linkedOrderNumberAttrId
				);

		// ������ id ������������ ��������...
		final Collection<ObjectId> ids = CardUtils.getAttrLinks(destAttr);
		final Set<ObjectId> current = (ids == null)
					? new HashSet<ObjectId>(5)
					: new HashSet<ObjectId>(ids);

		// ������� �������� ������������� ������ �������...
		int curMaxNumber = (curOrder == null) ? 0 : curOrder.getMaxOrderNum(); 

		// ��������� ������� ����� ��� ���� � �������� ������ ...
		//
		if (curOrder != null) 
			curOrder.needRenum = false;
		for (Person person : srcPersons) {
			if (person.getId() == null) continue;
			final Long personId = (Long) person.getId().getId();
			// (Smirnov A. : 1.8.12) ��� �����.���������, ����������, ��� � ���
			//						 ����� ������ ��������� �� ������� 
			//						 �������� ���������� ��� ����������.
			
			if (curOrder == null || !curOrder.hasPerson( personId)) {
				//��������� ��� ������� �������� � ������ �������
				for(ObjectId signCardId: destAttr.getIdsLinked()){
					ChangeState changeState = new ChangeState();
					Card signCard = loadCardById(signCardId);
					if(signCard.getState().equals(poruchcancelledStatusId)){
						continue;
					}
					changeState.setCard(signCard);
					changeState.setWorkflowMove(findWorkFlowMove(signCardId, poruchcancelledStatusId, getSystemUser()));
					execAction(changeState,getSystemUser());
				}
				
				// ������������ ����� ��������...
				this.isChangedActiveCard = true;
				curMaxNumber++;
				final ObjectId newCardId = createNewCard(person, curMaxNumber);
				current.add(newCardId);
				if (curOrder != null)
					curOrder.regPerson( personId, newCardId, curMaxNumber);
			} else {
				// ���� ����� ������� - �������� �� ���������� �����
				if ( curOrder.isOrderPresent 
						&& (curOrder.getPersonOrder(personId) < curMaxNumber) 
					) {
					// (!) ����� ������ ���� �������� ���������� ������
					curOrder.needRenum = true;
					++curMaxNumber;
					logger.warn( "For card " + activeCard.getId().getId() + " "
							+ " linked person reordered from " + curOrder.getPersonOrder(personId) 
							+ " to " + curMaxNumber);
					curOrder.setPersonOrder( personId, curMaxNumber);
				}
			}
		}

		// ������ ������ � ��������...
		if (this.isChangedActiveCard)
			destAttr.setIdsLinked(current);

		// ����������� ������� �����������...
		if (curOrder != null && curOrder.isOrderPresent && curOrder.needRenum) 
		{
			// ����� ������� ����� ������ ����� �������:
			// 	this.checkingListAttrId: CardLink
			// 	this.linkedOrderNumberAttrId: Int
			// 	this.linkedPersonAttrId: U- ��� C-link.
			this.reorder( curOrder, this.linkedOrderNumberAttrId);
		}
	}


	/**
	 * ������� �������� ��� ��������� �������.
	 * @param person
	 * @param curNumber ��� ������ � ������� orderNum.
	 * @return id ��������� ��������.
	 * @throws DataException 
	 */
	protected ObjectId createNewCard(Person person, int curNumber) 
			throws DataException
		{
			// creating card
			final CreateCard create = new CreateCard(this.newCardTemplateId);
			final Card newCard = (Card) execAction(create, getSystemUser());

			final String sPersonInfo = person.getFullName() + "(id=" + person.getId().getId() 
				+ "), eMail: " + person.getEmail();
			logger.debug( "For card " + activeCard.getId().getId() + " linked card created for user "+ sPersonInfo);

			ObjectId newCardId = newCard.getId(); 
			try {

				// copying card attributes ...
				final Card parent = this.activeCard;
				int copied = 0;
				for (Iterator<?> itrBlock = parent.getAttributes().iterator(); itrBlock.hasNext(); ) 
				{
					final AttributeBlock block = (AttributeBlock) itrBlock.next();
					for (Iterator<?> itrAttr = block.getAttributes().iterator(); itrAttr.hasNext(); ) {
						final Attribute srcAttr = (Attribute) itrAttr.next();
						if (this.copyInfo.canBeCopied(srcAttr)) {
							replaceAttribute(newCard, srcAttr);
							copied++;
						}
					}
				}
				logger.debug( "For card " + activeCard.getId().getId() + " "
						+ copied + " attribute(s) copied into NEW linked card");

				// setting specific values
				final Attribute owner = newCard.getAttributeById(this.linkedPersonAttrId);
				if (owner != null) {
					if (isAttrType_U(this.linkedPersonAttrId))
						((PersonAttribute) owner).setPerson(person);
					else if (isAttrType_C(this.linkedPersonAttrId))
						((CardLinkAttribute) owner).addLabelLinkedCard(parent);
					else 
						logger.error( "Invalid logic: attribute "+ this.linkedPersonAttrId
								+ " is not of U/C-type");
				} else {
					logger.error(MessageFormat.format(MSG_CARD_0_HAS_NO_ATTRIBUTE_1, 
							new Object[] { newCard.getId().getId(), this.linkedPersonAttrId } ));
				} 

				final IntegerAttribute number = (IntegerAttribute) newCard.getAttributeById(this.linkedOrderNumberAttrId);
				if (number != null) {
					number.setValue(curNumber);
				} else { 
					logger.warn( MessageFormat.format(MSG_CARD_0_HAS_NO_ATTRIBUTE_1, 
							new Object[] { newCard.getId(), this.linkedOrderNumberAttrId } ));
				} 

				// ���������� ����� ��������...
				final SaveQueryBase storeQuery = getQueryFactory().getSaveQuery(newCard);
				storeQuery.setObject(newCard);
				newCardId = (ObjectId) getDatabase().executeQuery(getSystemUser(), storeQuery);
				logger.info( "For card " + activeCard.getId().getId() 
						+ " NEW linked card saved: " + newCardId.getId()
						+ "\t order: "+ curNumber
						+ ", person: "+ sPersonInfo
					);

			} finally {
				// unlocking the newly created card
				if (newCardId != null)
					execAction( new UnlockObject(newCardId), getSystemUser());
			}

			return newCardId;
		}


	@SuppressWarnings("unchecked")
	private void replaceAttribute( final Card card, final Attribute attr) 
	{
		// logger.info("Replacing attribute " + attr.getId().getId());
		for (Iterator<?> itrBlock = card.getAttributes().iterator(); itrBlock.hasNext(); ) {
			final AttributeBlock block = (AttributeBlock) itrBlock.next();
			for (Iterator<?> itrAttr = block.getAttributes().iterator(); itrAttr.hasNext(); ) {
				final Attribute dstAttr = (Attribute) itrAttr.next();
				if (attr.getId().equals(dstAttr.getId())) {
					itrAttr.remove();
					block.getAttributes().add(attr);
					return;
				}
			}
		}
		logger.warn("Attribute " + attr.getId().getId() + " not found in card " + card.getId());
	}


	/**
	 * ������������ ����� ��� �������� ����� �������� ���� ����������,
	 * ����� ���.
	 * ����� ��������� ���������� � ������� ��������:
	 * 		1) ���� ������� � ����� �� ������ copied1, ����������� �����������;
	 * 		2) ����� - ���� ��� �������� ���������� � disabledTypes2 - 
	 * ����������� �� �����������.
	 * 		3) ����� ���� ��� �������� ���� � ������ nonCopied3 - ����������� 
	 * �� �����������.
	 * 		4) ��� ��������� ������������ ���� defaultCopyEnabled4 (�� ��������� false).
	 */
	protected class AttributesCopyInfo {

		/**
		 * ����� ����������� ���������� ��������� (���������� �� ���� ������ �� 
		 * ��� � disabledTypes ��� ���). ���������� �����.
		 */
		final protected Set<String> copied1= new HashSet<String>();

		/**
		 *  ����� ������� ��������� ����������� ����� ���������, ����������� 
		 *  ��� ��������������� ����������� � ����������� �������� ({@link Attribute.TYPE_XXX}).
		 * ���������� ����� �������: backLink, cardLinkm, typedLink, history, material.
		 */
		final protected Set<Object> disabledTypes2 = new HashSet<Object>(); 

		/**
		 * ����� ������������ ���������.
		 * ���������� ����� �������: �����, ���� ��������, ���� ���������.
		 */
		final protected Set<String> nonCopied3 = new HashSet<String>();

		/**
		 * ���� �������� �� ���������, �.�. ����� �� ���� �� ���������� ������ 
		 * 1-3 �� ���������: true= ��������� �����������, false= �� ����������. 
		 */
		final protected boolean defaultCopyEnabled4 = false;

		public AttributesCopyInfo() 
		{
			// ��� ������� �� ��������...
			disabledTypes2.add( Attribute.TYPE_BACK_LINK);
			disabledTypes2.add( Attribute.TYPE_CARD_LINK); 
			disabledTypes2.add( Attribute.TYPE_TYPED_CARD_LINK);
			disabledTypes2.add( Attribute.TYPE_CARD_HISTORY);
			disabledTypes2.add( Attribute.TYPE_MATERIAL);

			nonCopied3.add( (String) Attribute.ID_AUTHOR.getId() );
			nonCopied3.add( (String) Attribute.ID_CREATE_DATE.getId() );
			nonCopied3.add( (String) Attribute.ID_CHANGE_DATE.getId() );

			// copied1.add(ObjectId.predefined(TypedCardLinkAttribute.class, "jbr.attachRes"));
		}

		/**
		 * ��������� ������ ���������� �������.
		 * @param attr
		 * @return true, ���� ����, false �����.
		 */
		public boolean canBeCopied(Attribute attr)
		{
			if ( 	attr == null 
					|| attr.getId() == null 
					|| attr.getId().getId() == null)
				return false;

			final String attrCode = (String) attr.getId().getId();

			// ���� ����� c���������� (?)
			if (copied1.contains(attrCode))
				return true;

			// ����������� ��� (?)
			if (disabledTypes2.contains(attr.getType()))
				return false;

			// ����� �� ���� ���������� (?)
			if (nonCopied3.contains(attrCode))
				return false;

			// ��-��������� 
			return defaultCopyEnabled4;
		}
	}


	/**
	 * �������� � ������ ������ ��������, ������� ���� ������������ ����� ��� 
	 * �������� ������� ������������.
	 * @param scanList
	 */
	protected void filterSourceCards(Set<Person> scanList) {
		// ������ �� ������, ��� ���������...
	}

	/**
	 * ������� � ����������� ��������.
	 * @author RAbdullin
	 */
	static class PersonsOrder {

		static final Integer ORDER_OFF = Integer.MIN_VALUE; 
		static final Integer ORDER_NOPERSON = Integer.MIN_VALUE + 1; 
		static final Integer ORDER_NULL = Integer.MIN_VALUE + 2; 

		/**
		 * @author RAbdullin
		 * ����� ������� + id �������� � ������ �������� � ������ ������� ��������
		 */
		class PersonDatum {
			final long cardId; 	// id �������� � activeCard, �� ������� ���� ������ �� ������ ������� (����� linkedPersonAttrId)
			int orderNum;		// ���������� ����� ������ ������� (������ ��� ������ �����������, ��������)

			PersonDatum( long cardId, int orderNum) {
				this.cardId = cardId;
				this.orderNum = orderNum;
			}
		}

		/**
		 * ������������ �� ��������� ����� ��� ������
		 * (���� �� �������������, �� �������� items[].orderNum == -1)
		 */
		boolean isOrderPresent;

		/**
		 * ����� ������������ ������: id-������� -> ���������� �����
		 * ���� isOrderPresent == false, �� ���������� ������ ������������ (= -1).
		 */
		final Map<Long, PersonDatum> personsOrder = 
				new HashMap<Long, PersonDatum>(10);

		/**
		 * true, ���� ���� ��������� ������������� ��������� � personOrder -
		 * �������� ������ personOrder ����� ������������������ ������, ��
		 * ��� ������ � ������ ������� (��������, 0, 3, 5).
		 */
		boolean needRenum;


		boolean hasPerson(Long personId) {
			return personsOrder.containsKey( personId);
		}

		/**
		 * ���������������� �������.
		 * @param personId:
		 * @param cardId: ��������, �������������� ���� ������ �������.
		 * @param orderNum: ���������� �����, ������������ �� ��������. 
		 */
		public void regPerson(Long personId, ObjectId newCardId, int orderNum) 
		{
			if (personId == null) return;
			personsOrder.put(personId, 
					new PersonDatum((Long)newCardId.getId(), orderNum));
		}

		/**
		 * ������� ���������� ����� �������.
		 * @param personId
		 * @return ���� ��� ���������� ������� (isOrderPresent is false) ������ ���-�� ORDER_OFF, 
		 *   ����� (isOrderPresent is true): ���� ������� ��� � ������ ���-�� ORDER_NOPERSON,
		 *   �����: ���� ���������� ����� == null -> ���-�� ORDER_NULL,
		 *   ����� ���-�� ���������� ����� �������. 
		 */
		int getPersonOrder(Long personId) {
			if (!isOrderPresent)
				return ORDER_OFF;
			if (!personsOrder.containsKey(personId))
				return ORDER_NOPERSON;

			final PersonDatum d = personsOrder.get(personId); 
			return (d == null) ? ORDER_NULL : d.orderNum;
		}

		/**
		 * ������ ����� �������� ��� ����������� ������ �������.
		 * @param personId
		 * @param orderNum
		 * @throws DataException 
		 */
		public void setPersonOrder(Long personId, int orderNum) 
			throws DataException 
		{
			if (personId != null) {
				PersonDatum datum = personsOrder.get(personId);
				if (datum == null) {
					// (!) ��� ��� ����� ���� �� ����� ������� - ����� ��������� ������ �����...
					throw new DataException("general.unique", new Object[] { "internal error" } );
					// personsOrder.put(personId, new PersonDatum( -1, orderNum) );
				}
				datum.orderNum = orderNum;
			}
		}

		/**
		 * ����� ��������� ������ ����� ������������ �����.
		 * @return ������������ ����� ��� {@link ORDER_OFF} ���� ��������� ���.
		 */
		int getMaxOrderNum()
		{
			if (!this.isOrderPresent)
				return ORDER_OFF;
			int result = 0;
			for (Map.Entry<Long, PersonDatum> item: personsOrder.entrySet()) {
				if (item.getKey() == null || item.getValue() == null)
					continue;
				if (item.getValue().orderNum > result)
					result = item.getValue().orderNum;
			}
			return result;
		}

		RowMapper getMapper() {
			return new SQLMapper();
		}

		private class SQLMapper implements RowMapper {

			public SQLMapper() {
			}

			/* (non-Javadoc)
			 * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
			 * �����������: 
			 * 		������ ������� = id ������� (null-�������� ������������)
			 * 		(����) ������ = � ������ �������� �������� id ��������, � ������� ���� ������ �� �������.
			 * 		(����) ������ = ���������� �����.
			 */
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException 
			{
				if (rs.getObject(1) == null)
					return null;

				final long personId = rs.getLong(1);
				final long cardId = rs.getLong(2);
				final int order = rs.getInt(3);
				final ObjectId statusId = new ObjectId(CardState.class,  rs.getLong(4));
				if(SIGN_STATES_IGNORE.contains(statusId)){
					return null;
				}
				personsOrder.put(personId, new PersonDatum(cardId, order) );

				return personId;
			}

		}
	}

	/**
	 * ��������� ����� �� ������ id ��� User.
	 * @param attrId
	 * @return true, ���� ������� ����� ��� User.
	 */
	public static boolean isAttrType_U(final ObjectId attrId)
	{
		return (attrId != null) && (attrId.getId() != null)
				&& PersonAttribute.class.isAssignableFrom(attrId.getType());
	}

	/**
	 * ��������� ����� �� ������ id ��� cardlink.
	 * @param attrId
	 * @return true, ���� ������� ����� ��� Cardlink.
	 */
	public static boolean isAttrType_C(final ObjectId attrId)
	{
		return (attrId != null) && (attrId.getId() != null)
				&& CardLinkAttribute.class.isAssignableFrom(attrId.getType());
	}

	/**
	 * �������� ������ ������ � �� ������� � �������� listAttrId � ��������� ���������.
	 * ��������, ������� ������������� � �� ���������� ������ ��� ������� ��������.
	 * @param cardId: �������� ��� �������� ��������.
	 * @param listAttrId: C-��������, � ������� �������� ��������� ��������.
	 * @param linkPersonAttrId  U-������� ������� � ��������� ��������.
	 * @return ������������������ ������ ������� ������ ��� null, ��� null-����������.
	 */
	protected PersonsOrder getCurrentPersonsOrder(ObjectId cardId, ObjectId listAttrId,
			ObjectId linkPersonAttrId, ObjectId linkOrderNum)
	{
		if (	listAttrId == null || listAttrId.getId() == null 
				|| cardId == null || cardId.getId() == null )
			return null;

		final PersonsOrder result = new PersonsOrder();

		final String sCardId = cardId.getId().toString(); 
		
		final String sCardLinksCondition = 
				"where av.card_id = "+ sCardId +" \n" +
				"\t and av.attribute_code='"+ listAttrId.getId()+ "' \n"
			;

		result.isOrderPresent = false; // by default
		
		// C-������� ��������� �� ��������, � �������: 
		//    1) ������� ���� � linkPersonAttrId (U-�������);
		//    2) ��������� ���� � linkOrderNum (���� �� �� ����).
		result.isOrderPresent = (linkOrderNum != null && linkOrderNum.getId() != null);
		final String orderCode = (result.isOrderPresent && linkOrderNum != null) 
					? (String) linkOrderNum.getId()
					: "";
		final String personCode = (String) linkPersonAttrId.getId();

		getJdbcTemplate().query(
				"select distinct \n" +
				"		av_usr.number_value, \n" +
				"		av_usr.card_id, \n" +		// id ��������, ������ ������� ���� ������ �� ������� 
				"		av_order.number_value, c_usr.status_id \n" + // ���������� ����� 
				"from attribute_value av \n" +

				"	join attribute_value av_usr on \n" +
				"		av_usr.card_id=av.number_value \n"+
				"		and av_usr.attribute_code='"+ personCode+ "' \n"+
				"   join card c_usr on av_usr.card_id = c_usr.card_id \n" + 
				"	left join attribute_value av_order on \n" +
				"		av_order.card_id=av_usr.card_id \n"+
				"		and av_order.attribute_code='"+ orderCode+ "' \n"+

				sCardLinksCondition,
				result.getMapper()
		);

		return result;
	}


	/**
	 * �������� � �� ����� ���������.
	 * @param needOrder
	 * @param orderAttrId
	 */
	private void reorder( PersonsOrder needOrder, ObjectId orderAttrId) 
	{
		if (orderAttrId == null || needOrder == null) return;
		int done = 0;
		final String sql = 
				"update attribute_value \n" +
				"set number_value=? \n" +
				"where card_id=? \n" +
				"	and attribute_code='"+ orderAttrId.getId()+ "' \n"
			;
		for (PersonDatum item: needOrder.personsOrder.values() ) {
			if (item == null) continue;
			done += this.getJdbcTemplate().update( sql,
					new Object[] { item.orderNum, item.cardId },
					new int[] { Types.NUMERIC, Types.NUMERIC}
				);
		}
		logger.warn( "For card "+ this.activeCard.getId().getId()
				+ " signs of list " + this.checkingListAttrId 
				+ " reordered succefully: update "+ done +" record(s)"
				);
	}
}