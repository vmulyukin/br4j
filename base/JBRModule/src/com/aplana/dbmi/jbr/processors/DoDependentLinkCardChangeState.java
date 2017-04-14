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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.service.DataException;

/**
 * ��������� ������������ ��� ����� ������� � ������� �������� � ������ �������� �������� ������������ ������� ������������ (������� "�������� ������� ����")
 * ���� ������� linkAttr ��������� ���������� ��������������� ������ �������� ������������ ��� ��� ����������.
 * @author ppolushkin
 */
public class DoDependentLinkCardChangeState extends DoDependentChangeState {

	private static final long serialVersionUID = 1L;
	
	static final String PARAM_CURRENT_CARD_CHECK_ATTRS = "curCheckAttrs";
	static final String PARAM_REMOTE_CARD_CHECK_ATTRS = "remoteCheckAttrs";
	
	// personattribute.jbr.exam.person=JBR_RASSM_PERSON = "���������������"
	static final ObjectId examPrevPersonAttrId = ObjectId.predefined(PersonAttribute.class, "jbr.exam.last.previous.person");
	// jbr.resolution.FioSign = "��������� �������"
	static final ObjectId signAttrId = ObjectId.predefined(PersonAttribute.class, "jbr.resolution.FioSign");
	
	private List<ObjectId> curCheckLinks = new ArrayList<ObjectId>();
	private ObjectId curCheckAttrPerson = examPrevPersonAttrId;
	private List<ObjectId> remoteCheckLinks = new ArrayList<ObjectId>();
	private ObjectId remoteCheckAttrPerson = signAttrId;
	/**
	 * �������������� ����� �������� ��������� ��������. � ������ ��������� �������� ������� ������ ��,
	 * � ������� ��������� ��������� � ���������������\���������� ����������������,
	 * BR4J00039374
	 */
	@Override
	protected Collection<Card> getDependentCards(Card baseCard) throws DataException {
		List<Card> result = Collections.emptyList();
		Collection<Card> linkedCards;
		if (CardLinkAttribute.class.equals(linkId.getType())) {
			linkedCards = getLinkedCards((CardLinkAttribute) baseCard.getAttributeById(linkId));
		} else {
			/*BackLinkAttribute*/
			linkedCards = getBackLinkedCards(linkId, baseCard.getId());
		}
		if (null == linkedCards || linkedCards.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("linked cards is null or empty by link " + linkId + " in card " + baseCard);
			}
			return result;
		}

		final Card actionCard = getActionCard();
		Card actualActionCard = actionCard; //CardUtils.loadCard(actionCard.getId(), getQueryFactory(), getDatabase(), getSystemUser());
		for(ObjectId id : curCheckLinks) {
			 LinkAttribute lAttr = actualActionCard.getAttributeById(id);
			 if(lAttr == null) {
				 logger.warn("Current card check chain break. Attribute " + id + " in card " + actualActionCard.getId() + " is null.");
				 return result;
			 }
			 final Card tempCard = CardUtils.loadCard(lAttr.getIdsLinked().get(0), getQueryFactory(), getDatabase(), getSystemUser());
			 if(tempCard == null) {
				 logger.warn("Current card check chain break. Card " + lAttr.getIdsLinked().get(0) + " for attr " + id + " in card " + actualActionCard.getId() + " not found.");
				 return result;
			 }
			 actualActionCard = tempCard;
		}
		final PersonAttribute curCheckPerson = actualActionCard.getAttributeById(curCheckAttrPerson);
		if(curCheckPerson == null || curCheckPerson.isEmpty()) {
			logger.warn("Check person " + curCheckAttrPerson + " not found in card " + actualActionCard.getId() + ". No one link card status is not changed.");
			return result;
		}
		final PersonAttribute assistantsPerson = CardUtils.retrieveAssistantsByProfile(curCheckPerson, getJdbcTemplate());

		result = new ArrayList<Card>();
		for (Card card : linkedCards) {
			Card actualCard = CardUtils.loadCard(card.getId(), getQueryFactory(), getDatabase(), getSystemUser());
			final Card linkedCard = actualCard;
			for(ObjectId id : remoteCheckLinks) {
				 LinkAttribute rAttr = actualCard.getAttributeById(id);
				 if(rAttr == null) {
					 logger.warn("Remote card check chain break. Attribute " + id + " in card " + actualCard.getId() + " is null.");
					 actualCard = null;
					 break;
				 }
				 actualCard = CardUtils.loadCard(rAttr.getIdsLinked().get(0), getQueryFactory(), getDatabase(), getSystemUser());
				 if(actualCard == null) {
					 logger.warn("Remote card check chain break. Card " + rAttr.getIdsLinked().get(0) + " for attr " + id);
					 break;
				 }
			}
			if(actualCard != null) {
				final PersonAttribute remoteCheckPerson = (PersonAttribute) actualCard.getAttributeById(remoteCheckAttrPerson);
				if (remoteCheckPerson.intersectionValue(curCheckPerson) || remoteCheckPerson.intersectionValue(assistantsPerson)) {
					result.add(linkedCard);
				}
			}
		}
		return result;
	}
	
	public void setParameter(String name, String value) {
		if (PARAM_CURRENT_CARD_CHECK_ATTRS.equalsIgnoreCase(name)) {
			String[] links = value.split(LINK_SEPARATOR);
			if (links.length == 0) {
				logger.warn("param " + PARAM_CURRENT_CARD_CHECK_ATTRS + " has zero size");
				return;
			}
			curCheckAttrPerson = IdUtils.smartMakeAttrId(links[links.length - 1], PersonAttribute.class, false);
			if(!PersonAttribute.class.isAssignableFrom(curCheckAttrPerson.getType())) {
				logger.warn("param " + PARAM_CURRENT_CARD_CHECK_ATTRS + " must ends with PersonAttribute id");
				return;
			}
			ObjectId curCheckAttr = null;
			for(int i = 0; i < links.length - 1; i++) {
				curCheckAttr = IdUtils.smartMakeAttrId(links[i], CardLinkAttribute.class, false);
				curCheckLinks.add(curCheckAttr);
			}
			
		} else if (PARAM_REMOTE_CARD_CHECK_ATTRS.equalsIgnoreCase(name)) {
			String[] links = value.split(LINK_SEPARATOR);
			if (links.length == 0) {
				logger.warn("param " + PARAM_REMOTE_CARD_CHECK_ATTRS + " has zero size");
				return;
			}
			remoteCheckAttrPerson = IdUtils.smartMakeAttrId(links[links.length - 1], PersonAttribute.class, false);
			if(!PersonAttribute.class.isAssignableFrom(remoteCheckAttrPerson.getType())) {
				logger.warn("param " + PARAM_REMOTE_CARD_CHECK_ATTRS + " must ends with PersonAttribute id");
				return;
			}
			ObjectId remoteCheckAttr = null;
			for(int i = 0; i < links.length - 1; i++) {
				remoteCheckAttr = IdUtils.smartMakeAttrId(links[i], CardLinkAttribute.class, false);
				remoteCheckLinks.add(remoteCheckAttr);
			}
			
		} else
			super.setParameter(name, value);
	}

}
