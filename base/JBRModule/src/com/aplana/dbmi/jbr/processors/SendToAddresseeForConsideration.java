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

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.*;

public class SendToAddresseeForConsideration extends ProcessCard
	implements Parametrized
{
	/**
	 * (!) ���� � ���������� ����� ����� ���� Person ��� Cardlink.
	 */
	protected ObjectId ATTR_ADDRESSES =
		// personattribute.jbr.incoming.addressee=JBR_INFD_RECEIVER
		ObjectId.predefined(PersonAttribute.class, "jbr.incoming.addressee");
	protected ObjectId ATTR_CONSIDERATIONS =
		// cardlinkattribute.jbr.examby=JBR_IMPL_ACQUAINT
		ObjectId.predefined(CardLinkAttribute.class, "jbr.examby");

	protected ObjectId TEMPL_CONSIDERATION =
		ObjectId.predefined(Template.class, "jbr.examination");
	protected ObjectId ATTR_CONSIDERATION_EXAMINER =
		ObjectId.predefined(PersonAttribute.class, "jbr.exam.person");
	protected ObjectId ATTR_CONSIDERATION_ENDDATE =
		ObjectId.predefined(DateAttribute.class, "jbr.exam.term");
	protected ObjectId ATTR_VISA_ORDER =
		ObjectId.predefined(IntegerAttribute.class, "jbr.exam.order");

	/**
	 * ���������� ���������. 
	 */
	// static final String MSG_CARD_HAS_EMPTY_ATTR_2 = "Card {0} contain EMPTY attribute ''{1}'' -> creation of consideration card skipped";
	// static final String MSG_CARD_ATTRIBUTE_HAS_INVALID_CLASS_5 = "Card {0} contain attribute ''{1}'' with class ''{2}'' but supported classes are only ''{3}'' or ''{4}''-> creation of consideration card skipped";
	protected static final String MSG_CARD_HAS_NO_ATTRIBUTE_2 = "Card {0} DOES NOT CONTAIN attribute ''{1}'' -> creation of consideration card skipped";
	protected static final String MSG_SKIPPED = "-> creation of consideration card skipped";

	protected static final String MSG_ATTR_NOT_FOUND = "Attribute ''{1}'' not found at card {0}";
	protected static final String MSG_PERSON_ATTRIBUTE_NOT_FOUND_2 = "Person attribute ''{1}'' not found at card {0}";
	protected static final String MSG_CONSIDERATION_SAVED_FOR_CARD_2 = "Consideration saved for card ''{0}'': added to attr-list ''{1}'' card id is ''{2}''";

	protected static final String MSG_PARAMETER_ASSIGNED_3 = "assigned parameter ''{0}''=''{1}''  ->  ''{2}''";
	protected static final String MSG_CARD_NOT_SAVED_1 = "Card ''{0}'' has no changes -> not saved";
	protected static final String MSG_CARD_SAVED_1 = "Card ''{0}'' saved SUCCESSFULLY";
	
	// ������� ��� ������
	// <parameter name="ignoredStates" value="draft;cancelled;poruchcancelled"/>
	protected static final String PARAM_IGNORED_STATES = "IgnoredStates";
	
	protected static final String PARAM_ADDRESS_ATTR = "addressAttr";
	protected static final String PARAM_CONSIDERATION_TEMPLATE = "considerationTemplate";
	protected static final String PARAM_CARD_LIST_ATTR = "cardListAttr";
	protected static final String PARAM_CONSIDERATION_EXAMINER = "considerationExaminer";
	protected static final String PARAM_CONSIDERATION_ENDDATE = "considerationEndDate";
	
	// ��������� �������� ��� ������
	protected Set<ObjectId> ignoredStateIds;
	// C����� ���������� ������
	protected Collection<Person> personsToProcess;
	// ������� �� ������� �������� ("������������")
	protected CardLinkAttribute considerations;
	// ����� id ������, ������� ��� ���� � ������ "������������"
	protected Set<ObjectId> existsPersonsIds;

	@Override
	public Object process() throws DataException 
	{
		final ChangeState move = (ChangeState) getAction();
		
		final Card doc = reloadCard(move);
		
		if(!initWorkSets(doc)) {
			return null;
		}

		/*
		 * ������ �� ���������, ��������� � ������ ������������...
		 */
		boolean isDocUpdated = false; // true, ���� ���� ���������
		for (Person person : personsToProcess) {
			if (existsPersonsIds.contains(person.getId()) ) 
			{
				logger.info("Consideration card for user '" + person.getFullName() + "' already exists");
				continue;
			}

			/*
			 * �������� ����� ��������... 
			 */
			final Card consideration = createNewConsideration();

			// ������ ����������������... 
			final PersonAttribute examiner = (PersonAttribute) 
					consideration.getAttributeById(ATTR_CONSIDERATION_EXAMINER);
			examiner.setPerson(person);

			// ���������� ������� ����...
			final DateAttribute term = (DateAttribute) 
					consideration.getAttributeById(ATTR_CONSIDERATION_ENDDATE);
			term.setValue(new Date());

			// ���������� �����������...
			final IntegerAttribute order = (IntegerAttribute) 
					consideration.getAttributeById(ATTR_VISA_ORDER);
			order.setValue(0);			
			
			// ���������� ����� ��������...
			final ObjectId considerationId = saveCardByUser(consideration, getSystemUser());
			considerations.addLinkedId(considerationId);
			isDocUpdated = true;

			logger.info(MessageFormat.format(MSG_CONSIDERATION_SAVED_FOR_CARD_2,
						doc.getId(), considerations.getId(), considerationId ));
		}

		/*
		 * ���������� �������� ��������
		 */
		if (isDocUpdated) {
			saveCardByUser(doc, getSystemUser());
			move.setCard(doc);
			logger.info(MessageFormat.format(MSG_CARD_SAVED_1, doc.getId()));
		} else {
			logger.info(MessageFormat.format(MSG_CARD_NOT_SAVED_1, doc.getId()));
		}
		return (isDocUpdated) ? doc : null;
	}
	
	/**
	 * ��������� ������ ��� ������
	 * @param doc - ��
	 * @return true ���� ��� ������������ ��� ������ ������ ������� ��������, false � ��������� ������
	 */
	protected boolean initWorkSets(Card doc) throws DataException {
		/*
		 * ��������� ������ ���������� ������
		 */
		personsToProcess = 
				getPersonsList(doc, ATTR_ADDRESSES);
		if (personsToProcess == null) 
			return false;

		/*
		 * ��������� �������� �� ������� �������� ("������������")... 
		 */
		considerations = 
				(CardLinkAttribute) doc.getAttributeById(ATTR_CONSIDERATIONS);
		if (considerations == null) {
			logger.warn(makeAttrInfo(MSG_CARD_HAS_NO_ATTRIBUTE_2, doc, ATTR_CONSIDERATIONS));
			return false;
		}

		/*
		 * ��������� ������ id ������, ������� ��� ���� � ������ "������������"...
		 */
		existsPersonsIds = 
			getPersonIds(considerations, ATTR_CONSIDERATION_EXAMINER);
		
		return true;
	}
	
	protected Card reloadCard(ChangeState move) throws DataException {
		// ������������ �������� �� ����, ��� ��� � ChangeState ����� ������ ���������� ���������
		ObjectQueryBase q = getQueryFactory().getFetchQuery(Card.class);
		q.setId(move.getCard().getId());
		return (Card)getDatabase().executeQuery(getSystemUser(), q);
	}

	/**
	 * �������� ������ ������ �� �������� � ����� CardLinkAttrinute ��� PersonAttribute. 
	 * @param doc
	 * @param addrListAttrId
	 * @return
	 * @throws DataException 
	 */
	protected Collection<Person> getPersonsList(Card doc, ObjectId addrListAttrId) 
		throws DataException
	{
		final Collection<Person> result = super.getPersonsList(doc, addrListAttrId, true);
		if (result == null || result.isEmpty())
			logger.info(MSG_SKIPPED);
		return result;
	}

	protected ObjectId saveCardByUser(Card card, UserData user)
		throws DataException 
	{
		ObjectId id = null; 
		boolean needUnlock = card.getId() == null;
		if (card.getId() != null && !card.isLocked()) {
			final LockObject lock = new LockObject(card);
			final ActionQueryBase lockQuery = getQueryFactory().getActionQuery(lock);
			lockQuery.setAction(lock);
			getDatabase().executeQuery(user, lockQuery);
			needUnlock = true;
		}
		try {
			final SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(card);
			saveQuery.setObject(card);
			id = (ObjectId) getDatabase().executeQuery(user, saveQuery);
		}
		finally {
			if (needUnlock) {
				final UnlockObject unlock = new UnlockObject(id);
				final ActionQueryBase unlockQuery = getQueryFactory().getActionQuery(unlock);
				unlockQuery.setAction(unlock);
				getDatabase().executeQuery(user, unlockQuery);
			}
		}
		return id;
	}

	/**
	 * ������������ ��������� �� ������� fmt_2 ������ �����������:
	 * 		������ - id ��������, 
	 * 		������ - �������� ��������. 
	 * @param fmt_2: ������ ��� MessageFormat � ����� �����������.
	 * @param card
	 * @param attrId
	 * @return
	 */
	protected static String makeAttrInfo(String fmt_2, Card card, ObjectId attrId)
	{
		return MessageFormat.format( fmt_2,
					new Object[] { ObjectIdUtils.getIdFrom(card), attrId}
				);
	}

	/**
	 * ��������� ������ id ������, ������� ������������� � ��������� attr_examiner
	 * ������ �������� ������ considaerationsList.
	 * @param considerationsList: ������ ��������,
	 * @param attrExaminer: ������� � id �������.
	 * @return ����� id ������
	 * @throws DataException 
	 */
	protected Set<ObjectId> getPersonIds(CardLinkAttribute considerationsList,
			ObjectId attrExaminer) throws DataException {

		final Set<ObjectId> result = new HashSet<ObjectId>();

		if (	attrExaminer != null 
				&& (considerationsList != null) && !considerationsList.isEmpty()) 
		{
			// ��������� �� ��������� �������� "����������������"... 
			final Collection<Card> considerationCards = 
					expandLinks( considerationsList.getIdsLinked(), new ObjectId[] { attrExaminer }, getSystemUser());

			// ��������� ������ PersonId...
			if (considerationCards != null) {
				for (Card card : considerationCards) {
					if ((ignoredStateIds != null) && ignoredStateIds.contains(card.getState()))
						continue;
					final PersonAttribute examiner = 
						(PersonAttribute) card.getAttributeById(attrExaminer);
					if (examiner == null || examiner.getPerson() == null) {
						logger.warn( makeAttrInfo( MSG_PERSON_ATTRIBUTE_NOT_FOUND_2,
								card, attrExaminer));
						continue;
					}
					result.add(examiner.getPerson().getId());
				}
			}
		}
		return result;
	}

//	private ObjectId searchForExisting(Collection<Card> considerations, ObjectId examinerId) 
//	{
//		for (Iterator<Card> itr = considerations.iterator(); itr.hasNext(); ) {
//			final Card card = itr.next();
//			final PersonAttribute examiner = 
//				(PersonAttribute) card.getAttributeById(ATTR_CONSIDERATION_EXAMINER);
//			if (examiner == null) {
//				logger.warn( makeAttrInfo( MSG_ATTR_NOT_FOUND,
//						card, ATTR_CONSIDERATION_EXAMINER));
//				continue;
//			}
//			if (examinerId.equals(examiner.getPerson().getId()))
//				return card.getId();
//		}
//		return null;
//	}

	protected Card createNewConsideration() throws DataException
	{
		final CreateCard creator = new CreateCard(TEMPL_CONSIDERATION);
		final ActionQueryBase createQuery = getQueryFactory().getActionQuery(creator);
		createQuery.setAction(creator);
        return (Card) getDatabase().executeQuery(getSystemUser(), createQuery);
	}

	@Override
	public void setParameter(String name, String value) {
		if (name == null || value == null) return;

		boolean assigned = true;
		Object result = null;
		if (PARAM_ADDRESS_ATTR.equals(name)) {
			this.ATTR_ADDRESSES = IdUtils.smartMakeAttrId(value, CardLinkAttribute.class);
			result = this.ATTR_ADDRESSES;
		} else if (PARAM_IGNORED_STATES.equalsIgnoreCase(name)) {
			ignoredStateIds = IdUtils.makeStateIdsList(value);
		} else if (PARAM_CONSIDERATION_TEMPLATE.equals(name)){
			this.TEMPL_CONSIDERATION = IdUtils.tryFindPredefinedObjectId(value, Template.class, true);
			result = this.TEMPL_CONSIDERATION;
		} else if (PARAM_CARD_LIST_ATTR.equals(name)){
			this.ATTR_CONSIDERATIONS = IdUtils.smartMakeAttrId(value, CardLinkAttribute.class);
			result = this.ATTR_CONSIDERATIONS;
		} else if (PARAM_CONSIDERATION_EXAMINER.equals(name)){
			this.ATTR_CONSIDERATION_EXAMINER = IdUtils.smartMakeAttrId(value, PersonAttribute.class);
			result = this.ATTR_CONSIDERATION_EXAMINER;
		} else if (PARAM_CONSIDERATION_ENDDATE.equals(name)){
			this.ATTR_CONSIDERATION_ENDDATE = IdUtils.smartMakeAttrId(value, DateAttribute.class);
			result = this.ATTR_CONSIDERATION_ENDDATE;
		} else {
			assigned = false;
		}

		logSettingParam(assigned, name, value, result);
	}
	
	protected void logSettingParam(boolean assigned, String name, String value, Object result) {
		// ����������� ����������...
				if (assigned && logger.isDebugEnabled())
					logger.debug( MessageFormat.format( MSG_PARAMETER_ASSIGNED_3,
							name, value, result));
	}

}