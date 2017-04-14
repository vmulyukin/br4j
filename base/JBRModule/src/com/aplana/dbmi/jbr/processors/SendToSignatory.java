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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;

/**
 * @comment RAbdullin
 * ����������� ������� ������ �������� CardLinkAttribute (��������, ��������) 
 * ��� ������������ ������ ������ ���� PersonAttribute (�������������).
 * �������� ������ ������������� � ��������, ������ � ������� � �������� ������
 * �������� ������ �������.
 */

public class SendToSignatory extends ProcessCard
{

	// ObjectId.predefined(PersonAttribute.class, "jbr.resolution.FioSign");
	// personattribute.jbr.outcoming.signatory=JBR_INFD_SIGNATORY
	private ObjectId ATTR_FIOSIGNER =
		ObjectId.predefined(PersonAttribute.class, "jbr.outcoming.signatory");

	// cardlinkattribute.jbr.sign.set=JBR_SIGN_SIGNING
	private ObjectId ATTR_SIGNATORIES =
		ObjectId.predefined(CardLinkAttribute.class, "jbr.sign.set");

	// template.jbr.sign=365
	private ObjectId TEMPL_SIGN = // ������ ��� ����� ��������
		ObjectId.predefined(Template.class, "jbr.sign");

	// personattribute.jbr.sign.person=JBR_SIGN_RESPONSIBLE
	private ObjectId ATTR_SIGNER = // ������� ������ �������
		ObjectId.predefined(PersonAttribute.class, "jbr.sign.person");

	@Override
	public void setParameter(String name, String value) 
	{
		if (name == null || value == null) return;

		super.setParameter(name, value);

		boolean assigned = true;
		Object result = null;
		if ("srcPersonListAttrId".equalsIgnoreCase(name)) {
			this.ATTR_FIOSIGNER = ObjectId.predefined(PersonAttribute.class, value);
			result = ATTR_FIOSIGNER;
		} else if ("dstCardsListAttrId".equalsIgnoreCase(name)) {
			this.ATTR_SIGNATORIES = ObjectId.predefined(CardLinkAttribute.class, value);
			result = ATTR_SIGNATORIES;
		} else if ("newCardTemplateId".equalsIgnoreCase(name)) {
			this.TEMPL_SIGN = ObjectId.predefined(Template.class, value);
			result = TEMPL_SIGN;
		} else if ("linkedPersonAttrId".equalsIgnoreCase(name)) {
			this.ATTR_SIGNER = ObjectId.predefined( PersonAttribute.class, value);
			result = ATTR_SIGNER;
		} else {
			// throw new IllegalArgumentException("Unknown parameter: " + name);
			assigned = false;
		}
		// ����������� ����������...
		if (assigned && logger.isDebugEnabled())
			logger.debug( MessageFormat.format( MSG_PARAMETER_ASSIGNED_3,
					name, value, result));
	}

	@Override
	public Object process() throws DataException 
	{
		// final ChangeState move = (ChangeState) getAction();
		// Card doc = move.getCard();
		Card doc = super.getCard();
		if ( (doc == null) || doc.getAttributeById(ATTR_FIOSIGNER) == null)
			doc = loadCardById( getCardId() );

		final PersonAttribute signers = (PersonAttribute) doc.getAttributeById(ATTR_FIOSIGNER);
		if (signers == null) {
			logger.warn( makeAttrInfo( MSG_CARD_0_HAS_NO_ATTRIBUTE_1, doc, ATTR_FIOSIGNER));
			return null;
		}
		if (signers.getValues() == null || signers.getValues().size() == 0){
			logger.warn( makeAttrInfo( MSG_CARD_0_HAS_EMPTY_ATTRIBUTE_1, doc, ATTR_FIOSIGNER));
			return null;
		}

		final CardLinkAttribute signatories = (CardLinkAttribute) doc.getAttributeById(ATTR_SIGNATORIES);
		if (signatories == null) {
			logger.warn( makeAttrInfo( MSG_CARD_0_HAS_NO_ATTRIBUTE_1, doc, ATTR_SIGNATORIES));
			return null;
		}

		final Set<ObjectId> existsSignatories = 
				expandLinksToPersonIds( signatories, ATTR_SIGNER);
		final Collection<Person> plist = CardUtils.getAttrPersons(signers);
		if (plist != null)
		for (Person signer: plist) 
		{
			if (signer == null){
				logger.warn( makeAttrInfo( MSG_CARD_0_HAS_NULL_VALUE_AT_ATTRIBUTE_1, doc, ATTR_SIGNATORIES));
				continue;
			}
			if (existsSignatories != null && existsSignatories.contains(signer.getId()))
			{
				logger.info( MessageFormat.format( 
						MSG_CARD_0_SIGNATORY_ALREADY_EXISTS_FOR_USER_1, (doc == null ? null : doc.getId()), signer.getFullName() ));
				continue;
			}

			/*
			 * �������� ����� ��������... 
			 */
			final CreateCard creator = new CreateCard(TEMPL_SIGN);
			final ActionQueryBase createQuery = getQueryFactory().getActionQuery(creator);
			createQuery.setAction(creator);
			final Card newSignatory = (Card) getDatabase().executeQuery(getUser(), createQuery);

			final PersonAttribute newSigner = 
				(PersonAttribute) newSignatory.getAttributeById(ATTR_SIGNER);
			if (newSigner == null){
				logger.warn( MessageFormat.format( 
						MSG_TEMPLATE_0_DOES_CONTAIN_ATTRIBUTE_1, 
						TEMPL_SIGN, ATTR_SIGNER ));
				return null;
			}
			newSigner.setPerson(signer);
			ObjectId newSignatoryId = saveCard(newSignatory);
			signatories.addLinkedId(newSignatoryId);
		}
		saveCard(doc);
		return null;
	}

	/**
	 * ���������� ���������. 
	 */
	static final String MSG_CARD_0_HAS_NO_ATTRIBUTE_1_EXIT = MSG_CARD_0_HAS_NO_ATTRIBUTE_1 + " -> signatory creation skipped";
	static final String MSG_CARD_0_HAS_EMPTY_ATTRIBUTE_1 = "Card ''{0}'' contains EMPTY attribute ''{1}'' -> signatory creation skipped";
	static final String MSG_CARD_0_HAS_NULL_VALUE_AT_ATTRIBUTE_1 = "Card ''{0}'' contains NULL value inside attribute ''{1}'' -> corresponding signatory creation skipped";

	static final String MSG_CARD_0_SIGNATORY_ALREADY_EXISTS_FOR_USER_1 = "Card ''{0}'' already contains linked signatory card for user ''{1}'' already exists, no new signature created";
	static final String MSG_TEMPLATE_0_DOES_CONTAIN_ATTRIBUTE_1 = "New card of template {0} does contain attribute ''{1}'' -> creation of corresponded signatory card skipped";
	static final String MSG_PARAMETER_ASSIGNED_3 = "assigned parameter ''{0}''=''{1}''  ->  ''{2}''";

	static String makeAttrInfo(String fmt_2, Card card, ObjectId attrId)
	{
		return MessageFormat.format( fmt_2,
					new Object[] { ((card == null) ? null : card.getId()), attrId}
				);
	}

	private ObjectId saveCard(Card card) throws DataException {
		boolean needUnlock = false;
		if (card.getId() != null) {
			LockObject lock = new LockObject(card);
			ActionQueryBase lockQuery = getQueryFactory().getActionQuery(lock);
			lockQuery.setAction(lock);
			getDatabase().executeQuery(getSystemUser(), lockQuery);			needUnlock = true;
		}
		final ObjectId id;
		try {
			final SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(card);
			saveQuery.setObject(card);
			id = (ObjectId) getDatabase().executeQuery(getSystemUser(), saveQuery);
		} finally {
			if (needUnlock) {
				UnlockObject unlock = new UnlockObject(card);
				ActionQueryBase unlockQuery = getQueryFactory().getActionQuery(unlock);
				unlockQuery.setAction(unlock);
				getDatabase().executeQuery(getSystemUser(), unlockQuery);
			}		}
		return id;
	}

	/**
	 * ��������� ������ �������� ������ � �������� id ������.
	 * @param listAttr ������� �� ������� id ��������� ��������.
	 * @param linkedPersonAttr Person-������� ������ ��������� ��������.
	 * @return ��������� Id ������.
	 * @throws DataException
	 */
	Set<ObjectId> expandLinksToPersonIds( final CardLinkAttribute listAttr, final 
			ObjectId linkedPersonAttr) 
		throws DataException 
	{
		if (listAttr == null || linkedPersonAttr == null)
			return null;

		final Search search = 
			CardUtils.getFetchAction(listAttr, new ObjectId[] {linkedPersonAttr});

		final List<Card> cards = CardUtils.execSearchCards(search, getQueryFactory(), getDatabase(), getSystemUser());
		if (cards == null || cards.isEmpty()) 
			return null;

		final Set<ObjectId> result = new HashSet<ObjectId>( cards.size() );
		for (Card card: cards) {
			final PersonAttribute signer = (PersonAttribute) card.getAttributeById(linkedPersonAttr);
			if (signer == null) {
				logger.warn( makeAttrInfo( MSG_CARD_0_HAS_NO_ATTRIBUTE_1_EXIT, card, linkedPersonAttr));
				continue;
			}
			if ( (signer.getPerson() != null) && (signer.getPerson().getId() != null) )
				result.add(signer.getPerson().getId());
		}
		return result;
	}

}