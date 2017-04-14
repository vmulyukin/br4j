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
package com.aplana.dbmi.module.docflow;

import java.util.ArrayList;
import java.util.Iterator;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.CardLinkLoader;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.ProcessorBase;

/**
 * ������������, ������������ �������� ������ ��� ��:
 * <br>1) ��������� ��������� ����� ������� � �������� ������������ 
 * (���� ��� ������� - �������, ��������� ���� � ���������� ������� �����,
 *  ���� ��� - ������������� ���������� docflow.visa.approver.duplicate);
 *  <br>2) ��������������� ���� "�����������" ��� ������� ������������,
 *  ������������� ���������� docflow.visa.approver.missed;
 *  <br>3) �������� �� ������� ����������� (���� ��, �� ��������� ��� ����, � ������� �� ������ �����������).
 */
public class ValidateVisasListProcessor extends ProcessorBase implements Parametrized {
	private static final long serialVersionUID = 1L;
	
	public static final ObjectId VISA_LIST = ObjectId.predefined(CardLinkAttribute.class, "jbr.visa.set");
	public static final ObjectId ADDITIONAL_VISA_LIST = ObjectId.predefined(CardLinkAttribute.class, "jbr.visa.enclosedSet");
	public static final ObjectId VISA_NUMBER = ObjectId.predefined(IntegerAttribute.class, "jbr.visa.number");
	public static final ObjectId VISA_PERSON = ObjectId.predefined(PersonAttribute.class, "jbr.visa.person");
	public static final ObjectId SIGN_PERSON = ObjectId.predefined(PersonAttribute.class, "jbr.sign.person");
	public static final ObjectId CURATOR = ObjectId.predefined(PersonAttribute.class, "jbr.npa.curator");
	public static final ObjectId PERSON_NAME = ObjectId.predefined(StringAttribute.class, "name");
	public static final ObjectId PERSON_POSITION = ObjectId.predefined(StringAttribute.class, "jbr.person.position");
	//public static final ObjectId TO_TRASH = ObjectId.predefined(WorkflowMove.class, "jbr.visa.delete");
	public static final ObjectId TEMPLATE_VISA = ObjectId.predefined(Template.class, "jbr.visa");
	public static final ObjectId CARDSTATE_TRASH = ObjectId.predefined(CardState.class, "trash");
	public static final ObjectId CARDSTATE_DRAFT = ObjectId.predefined(CardState.class, "draft");
	public static final ObjectId DEF_SIGNER = ObjectId.predefined(PersonAttribute.class, "jbr.outcoming.signatory");
	public static final ObjectId SIGN_LIST = ObjectId.predefined(CardLinkAttribute.class, "jbr.sign.set");
	
	public static final String IGNORE_NOT_DRAFT = "ignoreNotDraft";
	
	private Card card;
	private CardLinkAttribute visasRefAttr;
	private ArrayList<Card> visas;
	/*
	 * ���� �������� ignoreNotDraft == true, �� ��� ��������� �� ������������ ������ ����� ������ �� ��� �����, ������� ��� � ������� ��������
	 */
	private boolean ignoreNotDraft;
	
	@SuppressWarnings("unchecked")
	@Override
	public Object process() throws DataException {
		card = ((ChangeState) getAction()).getCard();
		
		if(card.getTemplate().equals(TEMPLATE_VISA))
			visasRefAttr = (CardLinkAttribute) card.getAttributeById(ADDITIONAL_VISA_LIST);
		else
			visasRefAttr = (CardLinkAttribute) card.getAttributeById(VISA_LIST);
		
		visas = (ArrayList<Card>) CardLinkLoader.loadCardsByLink
		(
				visasRefAttr, 
				new ObjectId[] { VISA_PERSON, VISA_NUMBER },
				getSystemUser(), 
				getQueryFactory(), 
				getDatabase()
		);
		
		
		
		validate(visas);
		return null;
	}
	
	private void validate(ArrayList<Card> cards) throws DataException {
		removeTrash(cards);
		validateApproverSet(cards);
		removeCuratorVisaIfSigner(cards);
		validateApproverUnique(cards);
		logger.info("Validation successful.");
	}
	
	private Person getVisaApprover(Card card) {
		return (Person)((PersonAttribute)(card.getAttributeById(VISA_PERSON))).getValues().iterator().next();
	}
	
	private Person getCurator(Card card) {
		if 
		(
			card.getAttributeById(CURATOR) != null &&
			((PersonAttribute)(card.getAttributeById(CURATOR))).getValues().size() > 0
		)
			return (Person)((PersonAttribute)(card.getAttributeById(CURATOR))).getValues().iterator().next();
		else return null;
	}
	
	private String getApproverInfo(Card card) throws DataException {
		return getVisaApprover(card).getFullName(); 
	}
	
	private void removeTrash(ArrayList<Card> cards) {
		for (int i = 0; i < cards.size(); i++) {
			Card iCard = cards.get(i);
			if (iCard.getState().equals(CARDSTATE_TRASH)) cards.remove(i--); 
  		}
	}
	
	private void validateApproverSet(ArrayList<Card> cards) throws DataException {
		for (int i = 0; i < cards.size(); i++) {
			Card iCard = cards.get(i);
			if
			(
				iCard.getAttributeById(VISA_PERSON) == null ||
				((PersonAttribute)(iCard.getAttributeById(VISA_PERSON))).getValues().size() <= 0
			) 
				throw new DataException("docflow.visa.approver.missed");			             
  		}
	}
	
	private void validateApproverUnique(ArrayList<Card> cards) throws DataException {
		long curPid = getCurator(card) != null ? (Long) getCurator(card).getId().getId() : -1;
		for (int i = 0; i < cards.size() - 1; i++)
  		{
			Card iCard = cards.get(i);			             
			long iPid = (Long) getVisaApprover(iCard).getId().getId(); 
			int iOrder = ((IntegerAttribute) iCard.getAttributeById(VISA_NUMBER)).getValue();

			ObjectId iState = iCard.getState(); // �������� ������� ��������. � ������ ���� ������ = "�������" - ����������.			
			if(ObjectId.predefined(CardState.class, "poruchcancelled").equals(iState)) continue;
			
			for (int j = i + 1; j < cards.size(); j++)
			{
				Card jCard = cards.get(j);
				long jPid = (Long)getVisaApprover(jCard).getId().getId();
				
				ObjectId jState = jCard.getState(); // �������� ������� ��������. � ������ ���� ������ = "�������" - ����������.			
				if(ObjectId.predefined(CardState.class, "poruchcancelled").equals(jState)) continue;
				
				if(iPid == jPid)
				{
					int jOrder = ((IntegerAttribute) jCard.getAttributeById(VISA_NUMBER)).getValue();
					if (iPid == curPid)
					{
						logger.info("Curator is an approver in more than one card. Card with lower order will be removed to trash.");
						if (jOrder < iOrder) 
						{
							cards.remove(j--);
							doChangeState(jCard, CardUtils.findWorkFlowMove(
									jCard.getId(), CARDSTATE_TRASH, getQueryFactory(), getDatabase(), getSystemUser()));
						}
						else 
						{
							cards.remove(i--); 
							doChangeState(iCard, CardUtils.findWorkFlowMove(
								iCard.getId(), CARDSTATE_TRASH, getQueryFactory(), getDatabase(), getSystemUser()));
							break;
						}
						//doChangeState(card, (WorkflowMove) DataObject.createFromId(TO_TRASH));
					}
					else if(ignoreNotDraft 
								&& (!iState.equals(jState) 
										|| (iState.equals(jState) && !CARDSTATE_DRAFT.equals(iState)))) {
						continue;
					}
					else throw new DataException("docflow.visa.approver.duplicate", new Object[] 
					{ 
							getApproverInfo(iCard),
							iOrder,
							jOrder
					});
				}
			}
  		}
	}
	
	@SuppressWarnings("unchecked")
	private void removeCuratorVisaIfSigner(ArrayList<Card> cards) throws DataException {
		if(getCurator(card) == null) return;
		Person defSigner = ((PersonAttribute) card.getAttributeById(DEF_SIGNER)).getPerson();
		ArrayList<Card> signs = (ArrayList<Card>) CardLinkLoader.loadCardsByLink
		(
				(CardLinkAttribute) card.getAttributeById(SIGN_LIST), 
				new ObjectId[] { SIGN_PERSON },
				getSystemUser(), 
				getQueryFactory(), 
				getDatabase()
		);
		boolean signsContainsCurator = false;
		for (Iterator<Card> i = signs.iterator(); i.hasNext();) {
			if (((PersonAttribute)i.next().getAttributeById(SIGN_PERSON)).getPerson().getId()
					.equals(getCurator(card).getId())) 
						{ signsContainsCurator = true; break; }
		}
		if(signsContainsCurator == false && (defSigner == null || 
				!defSigner.getId().equals(getCurator(card).getId()))
		) return;
		
		for(int i = 0; i < cards.size(); i++) {
			Card c = cards.get(i);
			if (((PersonAttribute) c.getAttributeById(VISA_PERSON)).getPerson().getId().equals(getCurator(card).getId())) {
				doChangeState(c, CardUtils.findWorkFlowMove(
					c.getId(), CARDSTATE_TRASH, getQueryFactory(), getDatabase(), getSystemUser()));
				cards.remove(i--);
			}
		}
	}
	
	protected void doChangeState(Card card, WorkflowMove wfm) throws DataException {
		if (card == null || wfm == null) {
			return;
		}
		LockObject lockObj = new LockObject(card);
		ActionQueryBase lockQuery = getQueryFactory().getActionQuery(lockObj);
		lockQuery.setAction(lockObj);
		getDatabase().executeQuery(getSystemUser(), lockQuery);
		
		try {
			ChangeState move = new ChangeState();
			move.setCard(card);
			move.setWorkflowMove(wfm);
			ActionQueryBase query = getQueryFactory().getActionQuery(move);
			query.setAction(move);
			getDatabase().executeQuery(getSystemUser(), query);
		} finally {
			UnlockObject unlock = new UnlockObject(card);
			ActionQueryBase unlockQuery = getQueryFactory().getActionQuery(unlock);
			unlockQuery.setAction(unlock);
			getDatabase().executeQuery(getSystemUser(), unlockQuery);
		}
	}
	
	@Override
	public void setParameter(String name, String value) {
		if (name == null || value == null) return;
		
		if (IGNORE_NOT_DRAFT.equals(name)) {
			this.ignoreNotDraft = new Boolean(value.trim());
		}
	}
}