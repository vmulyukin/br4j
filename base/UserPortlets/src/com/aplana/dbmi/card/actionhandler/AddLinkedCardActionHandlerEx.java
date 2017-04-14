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
package com.aplana.dbmi.card.actionhandler;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.doclinked.DoclinkUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.filter.PersonCardIdFilter;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

public class AddLinkedCardActionHandlerEx extends AddLinkedCardActionHandler {
	public static final String SOURCE_ATTR_ID_PARAM = "sourcePersonAttrId";
	public static final String DESTINATION_ATTR_ID_PARAM = "destinationPersonAttrId";
	
	private ObjectId srcId;
	private ObjectId dstId;

	@Override
	protected Card createCard() throws DataException, ServiceException 
	{
		final Card newCard = super.createCard();
		final CardPortletSessionBean sessionBean = getCardPortletSessionBean();
		final Card curCard = sessionBean.getActiveCard();

//		final PersonAttribute srcAttr = (PersonAttribute)curCard.getAttributeById(srcId);
//		final PersonAttribute dstAttr = (PersonAttribute)newCard.getAttributeById(dstId);
//		dstAttr.setValues(new ArrayList(srcAttr.getValues()));
		final AttributeAccessor srcAttr = new AttributeAccessor( curCard, srcId); 
		final AttributeAccessor dstAttr = new AttributeAccessor( newCard, dstId);
		dstAttr.getValuesFrom( srcAttr);

		return newCard;
	}


	/**
	 * ���������� ���������. 
	 */
	static final String MSG_CARD_HAS_NO_ATTRIBUTE_2 = "Card {0} DOES NOT CONTAIN attribute ''{1}'' -> creation of consideration card skipped";
	static final String MSG_CARD_ATTRIBUTE_HAS_INVALID_CLASS_5 = "Card {0} contains attribute ''{1}'' with class ''{2}'' but supported classes are only ''{3}'' or ''{4}''-> creation of consideration card skipped";
	static final String MSG_CARD_HAS_EMPTY_ATTR_2 = "Card {0} contains EMPTY attribute ''{1}'' -> creation of consideration card skipped";


	static String makeAttrInfo(String fmt_2, Card card, ObjectId attrId)
	{
		return MessageFormat.format( fmt_2,
					new Object[] { (card == null) ? null : card.getId(), attrId}
				);
	}


	@Override
	public void setParameter(String name, String value) {
		if (SOURCE_ATTR_ID_PARAM.equals(name)) {
			// this.srcId = ObjectIdUtils.getObjectId(PersonAttribute.class, value, false);
			this.srcId = DoclinkUtils.tryFindPredefinedObjectId(value, PersonAttribute.class, false);
		}else if (DESTINATION_ATTR_ID_PARAM.equals(name)) {
			// this.dstId = ObjectIdUtils.getObjectId(PersonAttribute.class, value, false);
			this.dstId = DoclinkUtils.tryFindPredefinedObjectId(value, PersonAttribute.class, false);
		}else super.setParameter(name, value);
	}


	/**
	 * �����-��������� ��� ��������� ������� � ��������� CardLinkAttribute ��� 
	 * PersonAttribute.
	 */
	class AttributeAccessor {

		private Card card = null;
		private ObjectId attrId = null;

		private boolean attrIsCardLink = false;
		private boolean attrIsPerson = false;
		private Attribute attr = null;

		/**
		 * @param card
		 * @param attrinuteId
		 */
		public AttributeAccessor( Card card, ObjectId attributeId) {
			setAttrPersonOrCardList(card, attributeId);
		}

		public AttributeAccessor() {
		}

		/**
		 * @return true, ���� ������� ���� � ����� ��� CardLinkAttribute.
		 */
		boolean isCardLinkAttr() {
			return (attr != null) && attrIsCardLink;
		}

		/**
		 * @return true, ���� ������� ���� � ����� ��� PersonAttribute.
		 */
		boolean isPersonAttr() {
			return (attr != null) && (attrIsPerson);
		}

		/**
		 * @return �������, ���� �� ���� CardLinkAttribute.
		 */
		CardLinkAttribute getCardLinkAttr() {
			return (attrIsCardLink) ? (CardLinkAttribute) attr : null;
		}

		/**
		 * @return �������, ���� �� ���� PersonAttribute.
		 */
		PersonAttribute getPersonAttr() {
			return (attrIsPerson) ? (PersonAttribute) attr : null;
		}

		/**
		 * @return id ��������, �������� ������� setAttrPersonOrCardList.
		 */
		public ObjectId getAttrId() {
			return this.attrId;
		}

		/**
		 * @return �������, �������� ������� setAttrPersonOrCardList.
		 */
		public Attribute getAttr() {
			return this.attr;
		}

		/**
		 * �������� ��������� ������� � ��������, �������� ��������, ����� 
		 * ������� ���� ���� ��� CardLinkAttribute, ���� PersonAttribute.
		 * @param card: ��������, � ������� ������ �������.
		 * @param attributeId: id ��������.
		 * @return true, ���� � �������� ���� ������� attrId � ����� 
		 * CardLinkAttribute ��� PersonAttribute, 
		 * � false, ���� ������ ��� �������� attrId, ���� ����, �� ������� ����.
		 */
		boolean setAttrPersonOrCardList(Card card, ObjectId attributeId)
		{
			// ��� ��������� ������ (PersonAttribute) ��� (CardlinkAttribute)
			this.card = card;
			this.attrId = attributeId;
			this.attr = (card != null && attrId != null) 
					? card.getAttributeById(attrId) : null;
			if (attr == null) {
				this.attrIsCardLink = false;
				this.attrIsPerson = false;
				logger.info( makeAttrInfo( MSG_CARD_HAS_NO_ATTRIBUTE_2, card, attrId));
				return false;
			}

			this.attrIsPerson = 
					PersonAttribute.class.isAssignableFrom(attr.getClass());
			this.attrIsCardLink = 
					CardLinkAttribute.class.isAssignableFrom(attr.getClass());
			if (!attrIsPerson && !attrIsCardLink) {
				logger.warn( MessageFormat.format( MSG_CARD_ATTRIBUTE_HAS_INVALID_CLASS_5,
						(card != null) ? card.getId() : null,
						attrId, 
						attr.getClass().getName(),
						PersonAttribute.class.getName(),
						CardLinkAttribute.class.getName()
					));
				return false;
			}
			return true;
		}


		/**
		 * �������� ������ ������ �� �������� � ����� CardLinkAttrinute ��� PersonAttribute. 
		 * @param doc
		 * @param addrListAttrId
		 * @return
		 * @throws DataException 
		 * @throws ServiceException 
		 */
		private Collection<Person> getPersonsList() 
			throws DataException, ServiceException 
		{
			// ��� ��������� ������ (PersonAttribute) ��� (CardlinkAttribute)
			if (!attrIsPerson && !attrIsCardLink)
				return null;

			if (this.isPersonAttr()) {
				final PersonAttribute persons = this.getPersonAttr();
				if (persons.getValues() == null || persons.getValues().size() == 0) {
					logger.info( makeAttrInfo( MSG_CARD_HAS_EMPTY_ATTR_2, this.card, this.attrId));
					return null;
				}
				return new ArrayList<Person>(persons.getValues());
			}

			// ����� ��� ��� ����� CardLink ...
			// if (this.isCardLinkAttr()) 
			final CardLinkAttribute cards = this.getCardLinkAttr();
			if (cards.getLinkedCount() < 1) {
				logger.info( makeAttrInfo( MSG_CARD_HAS_EMPTY_ATTR_2, this.card, this.attrId));
				return null;
			}

			/*
			 * ��������� ������ ������ �� id �������� ������...
			 */
			final PersonCardIdFilter filter = new PersonCardIdFilter();
			filter.setCardIds( cards.getIdsLinked() ); // (Collection<ObjectId>)
			final Collection<Person> result =  
				getCardPortletSessionBean().getServiceBean().filter(Person.class, filter);

			return result;
		}

		private Collection<ObjectId> getCardIdsList() 
			throws DataException, ServiceException 
		{
			// ��� ��������� ������ (PersonAttribute) ��� (CardlinkAttribute)
			if (!attrIsPerson && !attrIsCardLink)
				return null;

			if (this.isCardLinkAttr()) {
				final CardLinkAttribute persons = this.getCardLinkAttr();
				if (persons.isEmpty()) {
					logger.info( makeAttrInfo( MSG_CARD_HAS_EMPTY_ATTR_2, this.card, this.attrId));
					return null;
				}
				return new ArrayList<ObjectId>(persons.getIdsLinked());
			}

			// ��������� �� �������� ������ ��������...
			final ArrayList<ObjectId> result = new ArrayList<ObjectId>();

			final Collection<Person> persons = this.getPersonsList();
			if (persons != null && !persons.isEmpty()) {
				for (Person person : persons) {
					if (person != null && person.getCardId() != null)
						result.add( person.getCardId());
				}
			}
			return result;
		}


		/**
		 * @param source �������� ������ ������ �� source.
		 * @throws ServiceException 
		 * @throws DataException 
		 */
		public void getValuesFrom(AttributeAccessor source) 
			throws DataException, ServiceException 
		{
			if (this.attr == null)
				return;

			// ���������� ������� ...
			if (source == null || source.attr == null) {
				if (this.isCardLinkAttr())
					this.getCardLinkAttr().setIdsLinked(null);
				if (this.isPersonAttr())
					this.getPersonAttr().setValues(null);
				return;
			}

			/*
			 * �������� ����� (������������ ���������� ������).
			 */
			if (source.isPersonAttr()) {
				if (this.isPersonAttr()) { // (simple) Person -> Person
					this.getPersonAttr().setValues(source.getPersonAttr().getValues());
				} else if (this.isCardLinkAttr()) { // Person -> CardLink
					this.getCardLinkAttr().addIdsLinked( source.getCardIdsList());
				}	
			} else if (source.isCardLinkAttr()) {
				if (this.isPersonAttr()) { // CardLink -> Person
					this.getPersonAttr().setValues( source.getPersonsList() );
				} else if (this.isCardLinkAttr()) { //(simple) CardLink -> CardLink
					this.getCardLinkAttr().setIdsLinked(source.getCardLinkAttr().getIdsLinked()); 
				}	
			}
		}
	}

}
