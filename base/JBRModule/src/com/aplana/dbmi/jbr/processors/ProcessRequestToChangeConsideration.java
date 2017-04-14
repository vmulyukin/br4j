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

import com.aplana.dbmi.action.*;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.util.DateUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.query.AttributeUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * ��������� � ����������� �� ���� ������� (requestType) ��������� ������ ����� ����������������.
 * �.�. ������� ����� �������� ������������ (���� �������� �������� newCons), ������� ������� �������� ������������ (���� ����� ��������� ��������� � �������),
 * ��� ��������� �� � ������ �������� (���� ��� ��������� � ������������ � �������� ��������)
 * @author ppolushkin
 */
public class ProcessRequestToChangeConsideration extends ProcessCard {

	private static final long serialVersionUID = 1L;
	
	/**
	 * ������������. ��� �������
	 */
	public static final String PARAM_REQUEST_TYPE = "requestType";
	/**
	 * ������������. ���� � ������������ �������� ������������
	 */
	public static final String PARAM_CURRENT_CONS = "currentCons";
	/**
	 * �������������� ���� ��� ������� �� ����������. ����� ���������������.
	 */
	public static final String PARAM_NEW_CONS = "newCons";
	/**
	 * �������������� ���� �� ��������� ����� �������� - ���� � ��
	 */
	public static final String PARAM_MAIN_DOC = "mainDoc";
	/**
	 * �������������� ���� �� ��������� ����� �������� - ���� �������� ����� �������� ������ ��
	 */
	public static final String PARAM_CONS_WAY_IN_MAIN = "consWayInMain";
	
	
	// ����������� ������� ��-���������
	public static final ObjectId changeCons = ObjectId.predefined(ReferenceValue.class, "jbr.considerator.change");
	public static final ObjectId changeConsResp = ObjectId.predefined(ReferenceValue.class, "jbr.considerator.change.respon");
	public static final ObjectId addCons = ObjectId.predefined(ReferenceValue.class, "jbr.considerator.add");
	public static final ObjectId removeCons = ObjectId.predefined(ReferenceValue.class, "jbr.considerator.remove");
	public static final ObjectId changeConsTerm = ObjectId.predefined(ReferenceValue.class, "jbr.considerator.change.term");
	public static final ObjectId respNo = ObjectId.predefined(ReferenceValue.class, "jbr.commission.control.no");
	
	public static final ObjectId considPerson = ObjectId.predefined(PersonAttribute.class, "jbr.exam.person");
	public static final ObjectId fioSign = ObjectId.predefined(PersonAttribute.class, "jbr.resolution.FioSign");
	
	public static final ObjectId respPerson = ObjectId.predefined(ListAttribute.class, "jbr.responsibility.consider");
	
	public static final ObjectId datePerson = ObjectId.predefined(DateAttribute.class, "jbr.exam.term");
	public static final ObjectId dateReq = ObjectId.predefined(DateAttribute.class, "jbr.request.change");
	
	public static final ObjectId considerationNew = ObjectId.predefined(CardLinkAttribute.class, "jbr.reqres.cons");
	public static final ObjectId considerationInMain = ObjectId.predefined(CardLinkAttribute.class, "jbr.acquant");
	public static final ObjectId mainDocFromRes = ObjectId.predefined(CardLinkAttribute.class, "jbr.main.doc");
	
	public static final ObjectId allRes = ObjectId.predefined(BackLinkAttribute.class, "jbr.allResolutions");
	
	public static final ObjectId considirationTemplate = ObjectId.predefined(Template.class, "jbr.rassm");
	
	public static final ObjectId consWaiting = ObjectId.predefined(WorkflowMove.class, "jbr.exam.assign");
	public static final ObjectId consAssist = ObjectId.predefined(WorkflowMove.class, "jbr.exam.send.assistant");
	public static final ObjectId consConsid = ObjectId.predefined(WorkflowMove.class, "jbr.exam.send");
	
	
	private static final List<ObjectId> ignoredStates = new ArrayList<ObjectId>();
	private static final List<ObjectId> finalConsStates = new ArrayList<ObjectId>();
	private static final List<ObjectId> finalResStates = new ArrayList<ObjectId>();
	
	public static final ObjectId cancelledState = ObjectId.predefined(CardState.class, "poruchcancelled");
	public static final ObjectId trashState = ObjectId.predefined(CardState.class, "trash");
	public static final ObjectId deloByExecState = ObjectId.predefined(CardState.class, "jbr.exam.archive.boss"); // ��������� ������������ � ����
	public static final ObjectId deloState = ObjectId.predefined(CardState.class, "delo");
	//public static final ObjectId draftState = ObjectId.predefined(CardState.class, "draft");
	public static final ObjectId execState = ObjectId.predefined(CardState.class, "execution");
	public static final ObjectId consState = ObjectId.predefined(CardState.class, "consideration");
	public static final ObjectId doneState = ObjectId.predefined(CardState.class, "done");
	
	static {
		ignoredStates.add(cancelledState);
		finalConsStates.add(deloState);
		finalConsStates.add(execState);
		finalConsStates.add(deloByExecState);
		finalConsStates.add(cancelledState);
		finalResStates.add(cancelledState);
		finalResStates.add(trashState);
		finalResStates.add(doneState);
	}
	
	private ObjectId requestTypeId;
	private ObjectId currentConsId;
	private ObjectId newConsId;
	private ObjectId consWayInMainId;
	ObjectId mainDocId;
	
	Card card;
	Card mainCard;
	private DatedTypedCardLinkAttribute newConsid;
	private List<Card> consLoad;
	private Card currLoad;
	

	@Override
	public Object process() throws DataException {
		
		if(requestTypeId == null || currentConsId == null) {
			throw new DataException("jbr.change.consid.request.mandatory.require");
		}
		
		card = getCard();
		
		ListAttribute requestTypeAttr = card.getAttributeById(requestTypeId);

		if (requestTypeAttr == null)
		{
			throw new DataException("jbr.change.consid.request.mandatory.require");
		}
		else if(requestTypeAttr.isEmpty()) {
			throw new DataException("jbr.change.consid.request.type.require", new Object[] {
					requestTypeAttr.getNameRu(), requestTypeAttr.getNameEn()
			});
		}
		
		if(requestTypeAttr.getValue().getId().equals(changeCons)) {
			processChangeCons();
		} else if(requestTypeAttr.getValue().getId().equals(changeConsResp)) {
			processChangeConsResp();
		} else if(requestTypeAttr.getValue().getId().equals(addCons)) {
			processAddCons();
		} else if(requestTypeAttr.getValue().getId().equals(removeCons)) {
			processRemoveCons();
		} else if(requestTypeAttr.getValue().getId().equals(changeConsTerm)) {
			processChangeConsTerm();
		}
		
		return null;
	}
	
	
	private void initAndValidateCurrentCons() throws DataException {
		// �������
		CardLinkAttribute currConsidAttr = card.getAttributeById(currentConsId);
		if (currConsidAttr == null)
		{
			throw new DataException("jbr.change.consid.request.mandatory.require");
		}
		else if(currConsidAttr.isEmpty()) {
			throw new DataException("jbr.change.consid.request.current.consid.require", new Object[] {
					currConsidAttr.getNameRu(), currConsidAttr.getNameEn()
			});
		}
		List<Card> currLoadList = loadAllLinkedCardsByAttr(card.getId(), currConsidAttr);
		if(CollectionUtils.isEmpty(currLoadList)) {
			throw new DataException("Error during load consideration card from " + card.getId() + " by " + currentConsId);
		}
		Card cc;
		for(Iterator<Card> iter = currLoadList.iterator(); iter.hasNext();) {
			cc = iter.next();
			if(ignoredStates.contains(cc.getState())) {
				iter.remove();
			}
		}
		if(CollectionUtils.isEmpty(currLoadList)) {
			throw new DataException("All loaded consideration card in ignored states");
		}
		currLoad = currLoadList.get(0);
		PersonAttribute personCons = currLoad.getAttributeById(considPerson);
		if (personCons == null)
		{
			throw new DataException("jbr.change.consid.request.mandatory.require");
		}
		else if(personCons.isEmpty()) {
			throw new DataException("jbr.change.consid.request.current.consid.require", new Object[] {
					personCons.getNameRu(), personCons.getNameEn()
			});
		}
		//Person currPerson = personCons.getPerson();

	}
	
	
	void initAndValidateMainCard() throws DataException {
		CardLinkAttribute mainDocAttr = card.getAttributeById(mainDocId);
		if (mainDocAttr == null)
		{
			throw new DataException("jbr.change.consid.request.mandatory.require");
		}
		else if(mainDocAttr.isEmpty()) {
			throw new DataException("jbr.change.consid.request.current.consid.require", new Object[] {
					mainDocAttr.getNameRu(), mainDocAttr.getNameEn()
			});
		}
		List<Card> mainLoad = loadAllLinkedCardsByAttr(card.getId(), mainDocAttr);
		if(CollectionUtils.isEmpty(mainLoad)) {
			throw new DataException("Error during load consideration card from " + card.getId() + " by " + mainDocId);
		}
		mainCard = mainLoad.get(0);
	}
	
	
	private void initAndValidateNewCons() throws DataException {
		// �����
		newConsid = card.getAttributeById(newConsId);
		if(newConsid == null || newConsid.isEmpty()) {
			logger.warn("Attribute " + newConsId + " is empty. exit!");
			return;
		}
	}
	
	
	private void initAndValidateExistingCons() throws DataException {
		CardLinkAttribute consInMainDoc = mainCard.getAttributeById(consWayInMainId);
		if(consInMainDoc == null || consInMainDoc.isEmpty()) {
			logger.warn("Attribute " + consWayInMainId + " is empty. exit!");
			return;
		}
		consLoad = loadAllLinkedCardsByAttr(mainCard.getId(), consInMainDoc);
	}
	
	
	private void createNewCons(List<ObjectId> idsToCreate) throws DataException {
		if(CollectionUtils.isEmpty(idsToCreate)) {
			return;
		}
		// ������� � ��������� ����� ������������
		List<ObjectId> createdCards = new ArrayList<ObjectId>();
		CreateCard createCard;
		Card newConsCard;
		ObjectId newCardId;
		PersonAttribute pa = null;
		for(ObjectId id : idsToCreate) {
			createCard = new CreateCard();
			createCard.setTemplate(considirationTemplate);
			createCard.setLinked(true);
			createCard.setParent(mainCard);
					
			newConsCard = execAction(createCard, getUser());
			
			pa = newConsCard.getAttributeById(considPerson);
			if(pa != null) {
				GetPersonByCardId action = new GetPersonByCardId();
				action.setIds(Collections.singletonList(id));
				List<Person> pers = execAction(action);
				pa.setPerson(pers.get(0));
			}
			ListAttribute la = newConsCard.getAttributeById(respPerson);
			ReferenceValue ref = new ReferenceValue();
			if(la != null) {
				Object newConsidType = newConsid.getType((Long)id.getId());
				if(newConsidType != null) {
					ref.setId(new ObjectId(ReferenceValue.class, newConsidType));
				} else {
					ref.setId(new ObjectId(ReferenceValue.class, respNo.getId()));
				}
				la.setValue(ref);
			}
			
			DateAttribute da = newConsCard.getAttributeById(datePerson);
			if(da != null) {
				Date newConsidDate = newConsid.getDate((Long)id.getId());
				if(newConsidDate != null) {
					da.setValue(newConsidDate);
				} else {
					da.setValue(DateUtils.addDaysToCurrent(29));
				//da.setValue(new Date());
				}
			}
			
			newCardId = saveCard(newConsCard, getUser());
			createdCards.add(newCardId);
					
			UnlockObject unlockObject = new UnlockObject();
			unlockObject.setId(newCardId);
			execAction(unlockObject);
		}
				
		// ����������� ����� ������������ � �� � � �������� �������
		CardLinkAttribute newConsCards = card.getAttributeById(considerationNew);
		CardLinkAttribute consInMainDoc = mainCard.getAttributeById(consWayInMainId);
				
		for(ObjectId id : createdCards) {
			consInMainDoc.addLinkedId(id);
			newConsCards.addLinkedId(id);
		}
				
		doOverwriteCardAttributes(mainCard.getId(), getUser(), consInMainDoc);
		doOverwriteCardAttributes(card.getId(), getUser(), newConsCards);
				
		// ��������� ����� ������������ � ������ "�������� ������������", ����� �� ������������
		PersonAttribute assistantsPerson;
		for(ObjectId id : createdCards) {
			moveCard(id, consWaiting);
			assistantsPerson = CardUtils.retrieveAssistantsByProfile(pa, getJdbcTemplate());
			if(AttributeUtils.isEmpty(assistantsPerson)) {
				moveCard(id, consConsid);
			} else {
				moveCard(id, consAssist);
			}
		}
	}
	
	
	private void editExistingCons(Set<Card> cardsToChange) throws DataException {
		PersonAttribute pa;
		for(Card c : cardsToChange) {
			pa = c.getAttributeById(considPerson);
			ListAttribute la = c.getAttributeById(respPerson);
			if(la != null) {
				ReferenceValue ref = new ReferenceValue();
				ref.setId(new ObjectId(ReferenceValue.class, newConsid.getType((Long)pa.getPerson().getCardId().getId())));
				la.setValue(ref);
				doOverwriteCardAttributes(c.getId(), getSystemUser(), la);
			}
			DateAttribute da = c.getAttributeById(datePerson);
			if(da != null) {
				da.setValue(newConsid.getDate((Long)pa.getPerson().getCardId().getId()));
				doOverwriteCardAttributes(c.getId(), getSystemUser(), da);
			}
		}
	}
	
	
	private void cancelCons() throws DataException {
		GetWorkflowMovesFromTargetState act = new GetWorkflowMovesFromTargetState();
		act.setCard(currLoad);
		act.setToStateId(cancelledState);
		List<Long> moveIds = execAction(act, getSystemUser());
		
		if (moveIds.size() < 1) {
			throw new DataException("Can not find any workflow moves for card=" + currLoad + " to status=" + cancelledState);
		}
		
		ObjectQueryBase wfMoveQuery = getQueryFactory().getFetchQuery(WorkflowMove.class);
		wfMoveQuery.setId(new ObjectId(CardState.class, moveIds.get(0).longValue()));
		WorkflowMove wfMove = getDatabase().executeQuery(getSystemUser(), wfMoveQuery);
		
		ChangeState changeState = new ChangeState();
		Card moveCard = new Card();
		moveCard.setId(currLoad.getId());
		changeState.setCard(moveCard);
		changeState.setWorkflowMove(wfMove);
		
		execAction(new LockObject(currLoad.getId()));
		execAction(changeState);
		execAction(new UnlockObject(currLoad.getId()));
	}
	
	
	private void cancelResolutions() throws DataException {
		List<Card> result = CardUtils.execListProject(allRes, mainCard.getId(), getQueryFactory(), getDatabase(), getSystemUser());
		if(CollectionUtils.isEmpty(result)) {
			return;
		}
		Search search = new Search();
		search.setByCode(true);
		search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(ObjectIdUtils.cardsToObjectIdsSet(result)));
		List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(fioSign);
		columns.add(col);
		col = new SearchResult.Column();
		col.setAttributeId(Card.ATTR_STATE);
		columns.add(col);
		col = new SearchResult.Column();
		col.setAttributeId(Card.ATTR_TEMPLATE);
		columns.add(col);
		search.setColumns(columns);
		result = CardUtils.execSearchCards(search, getQueryFactory(), getDatabase(), getSystemUser());
		PersonAttribute signPerson;
		PersonAttribute personCons;
		Card cr;
		for(int i = 0; i < result.size(); i++) {
			cr = result.get(i);
			signPerson = cr.getAttributeById(fioSign);
			personCons = currLoad.getAttributeById(considPerson);
			if(personCons.getPerson().equals(signPerson.getPerson())) {
				GetWorkflowMovesFromTargetState act = new GetWorkflowMovesFromTargetState();
				act.setCard(cr);
				act.setToStateId(cancelledState);
				List<Long> moveIds = execAction(act, getSystemUser());
				
				if (moveIds.size() < 1){
					continue;
				}
				
				ObjectQueryBase wfMoveQuery = getQueryFactory().getFetchQuery(WorkflowMove.class);
				wfMoveQuery.setId(new ObjectId(CardState.class, moveIds.get(0).longValue()));
				WorkflowMove wfMove = getDatabase().executeQuery(getSystemUser(), wfMoveQuery);
				
				ChangeState changeState = new ChangeState();
				Card moveCard = new Card();
				moveCard.setId(cr.getId());
				changeState.setCard(moveCard);
				changeState.setWorkflowMove(wfMove);
				
				execAction(new LockObject(cr.getId()));
				execAction(changeState);
				execAction(new UnlockObject(cr.getId()));
			}
		}
	}
	
	
	private boolean isMainDocReadyToBeExecuted() throws DataException {
		// ��������� ������������
		Search search = new Search();
		search.setByCode(true);
		search.setWords(ObjectIdUtils.getCardIdToString(mainCard));
		List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(considerationInMain);
		columns.add(col);
		search.setColumns(columns);
		
		List<Card> result = CardUtils.execSearchCards(search, getQueryFactory(), getDatabase(), getSystemUser());
		if(!CollectionUtils.isEmpty(result)) {
			CardLinkAttribute conss = result.get(0).getAttributeById(considerationInMain);
			if(conss != null && !conss.getLinkedIds().equals("")) {
				search = new Search();
				search.setByCode(true);
				search.setWords(conss.getLinkedIds());
				columns = new ArrayList<SearchResult.Column>();
				col = new SearchResult.Column();
				col.setAttributeId(Card.ATTR_STATE);
				columns.add(col);
				search.setColumns(columns);
				result = CardUtils.execSearchCards(search, getQueryFactory(), getDatabase(), getSystemUser());
				if(!CollectionUtils.isEmpty(result)) {
					for(Card cr : result) {
						if(!finalConsStates.contains(cr.getState())) {
							return false;
						}
					}
				}
			}
		}
		
		// ��������� ���������
		search = new Search();
		search.setByAttributes(true);
		search.addCardLinkAttribute(mainDocFromRes, mainCard.getId());
		columns = new ArrayList<SearchResult.Column>();
		col = new SearchResult.Column();
		col.setAttributeId(Card.ATTR_STATE);
		columns.add(col);
		search.setColumns(columns);
		result = CardUtils.execSearchCards(search, getQueryFactory(), getDatabase(), getSystemUser());
		if(!CollectionUtils.isEmpty(result)) {
			for(Card cr : result) {
				if(!finalResStates.contains(cr.getState())) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	
	void mainDocToExecuted() throws DataException {
		if(isMainDocReadyToBeExecuted()) {
			GetWorkflowMovesFromTargetState act = new GetWorkflowMovesFromTargetState();
			act.setCard(mainCard);
			
			List<Long> moveIds;
			// ���� �� � ������� ������������, �� ������� ��������� ��� �� ����������
			if(mainCard.getState().equals(consState)) {
				act.setToStateId(execState);
				moveIds = execAction(act, getSystemUser());
				if (moveIds.size() < 1){
					return;
				}
				moveCard(mainCard.getId(), moveIds.get(0));
			}
			
			// ���������� ����� ������ ��������
			Search search = new Search();
			search.setByCode(true);
			search.setWords(ObjectIdUtils.getCardIdToString(mainCard));
			List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
			SearchResult.Column col = new SearchResult.Column();
			col.setAttributeId(Card.ATTR_STATE);
			columns.add(col);
			col = new SearchResult.Column();
			col.setAttributeId(Card.ATTR_TEMPLATE);
			columns.add(col);
			search.setColumns(columns);
			List<Card> result = CardUtils.execSearchCards(search, getQueryFactory(), getDatabase(), getSystemUser());
			if(!CollectionUtils.isEmpty(result)) {
				// � ��������� �� � ��������
				act = new GetWorkflowMovesFromTargetState();
				act.setCard(result.get(0));
				act.setToStateId(doneState);
				moveIds = execAction(act, getSystemUser());
				
				if (moveIds.size() < 1){
					return;
				}
				
				moveCard(result.get(0).getId(), moveIds.get(0));
			}
		}
	}
	
	
	private void moveCard(ObjectId id, ObjectId move) throws DataException {
		ChangeState changeState = new ChangeState();
		WorkflowMove workflowMove = new WorkflowMove();
		workflowMove.setId(move);

		Card moveCard = new Card();
		moveCard.setId(id);
		changeState.setCard(moveCard);
		changeState.setWorkflowMove(workflowMove);
		
		execAction(new LockObject(id));
		execAction(changeState);
		execAction(new UnlockObject(id));
	}
	
	private void moveCard(ObjectId cardId, Long moveId) throws DataException {
		ObjectQueryBase wfMoveQuery = getQueryFactory().getFetchQuery(WorkflowMove.class);
		wfMoveQuery.setId(new ObjectId(CardState.class, moveId.longValue()));
		WorkflowMove wfMove = getDatabase().executeQuery(getSystemUser(), wfMoveQuery);
		
		moveCard(cardId, wfMove.getId());

	}
	
	
	/**
	 * ��������� ������������ �������� ������������ � ����� ���������������
	 * @param idsToCreate - ������ �������� ������ ��� ������� ���������� ������� ����� �������� ������������
	 * @param cardsToChange - ����� ������������ �������� ������������ ������� ����� ��������
	 * @param isErrorWhenOldInFinalState - ����, ����������� �� ������ ���� ���� �� ������������ � ����� �� ��������� ��������
	 * @throws DataException
	 */
	private void compareNewAndOldConsiderations(List<ObjectId> idsToCreate, Set<Card> cardsToChange, boolean isErrorWhenOldInFinalState) throws DataException {
		Card cc;
		PersonAttribute pa;
		GetPersonByCardId action = new GetPersonByCardId();
		action.setIds(newConsid.getIdsLinked());
		List<Person> newPersons = execAction(action);
		if(newPersons == null) {
			newPersons = new ArrayList<Person>(0);
			logger.error("One of person cards " + ObjectIdUtils.numericIdsToCommaDelimitedString(newConsid.getIdsLinked()) + " do not consists person");
		}
		for(Iterator<Card> iter = consLoad.iterator(); iter.hasNext();) {
			cc = iter.next();
			if(ignoredStates.contains(cc.getState())
					|| (!isErrorWhenOldInFinalState && finalConsStates.contains(cc.getState()))) {
				iter.remove();
				continue;
			}
			pa = cc.getAttributeById(considPerson);
			if(AttributeUtils.isEmpty(pa)) {
				logger.error("Person Attribute " + considPerson.getId() + " in card " + cc.getId().getId() + " do not consists considerator");
				continue;
			}
			if(isErrorWhenOldInFinalState && newPersons.contains(pa.getPerson())
					&& finalConsStates.contains(cc.getState()) && !execState.equals(cc.getState())) {
				PersonAttribute personCons = cc.getAttributeById(considPerson);
				throw new DataException("jbr.change.resp.request.final.state", new Object[]{personCons != null ? personCons.getPersonName() : null});
			} else if(isErrorWhenOldInFinalState && newPersons.contains(pa.getPerson())
					&& (!finalConsStates.contains(cc.getState()) || execState.equals(cc.getState()))) {
				idsToCreate.remove(pa.getPerson().getCardId());
				cardsToChange.add(cc);
			} else if(!isErrorWhenOldInFinalState && newPersons.contains(pa.getPerson())) {
				idsToCreate.remove(pa.getPerson().getCardId());
				cardsToChange.add(cc);
			}
		}
	}
	
	
	private void processChangeCons() throws DataException {
				
		initAndValidateCurrentCons();
		
		initAndValidateMainCard();
		
		initAndValidateNewCons();
		
		initAndValidateExistingCons();
		
		// ������� ����� � �������������
		List<ObjectId> idsToCreate = new ArrayList<ObjectId>(newConsid.getIdsLinked()); //
		Set<Card> cardsToChange = new HashSet<Card>();
		
		compareNewAndOldConsiderations(idsToCreate, cardsToChange, true);
		
		// ��������
		
		createNewCons(idsToCreate);
		
		// ����������� ��� ������������ �����������
		
		editExistingCons(cardsToChange);
		
		// �������� ������� ������������ ���� ���������

		boolean flag = false;
		for(Card c : cardsToChange) {
			if(currLoad.getId().equals(c.getId())) {
				flag = true;
				break;
			}	
		}
		if(!flag) {
			
			// �������� ������������
			
			cancelCons();
			
			// �������� ���������
			
			cancelResolutions();
		}
	}
	
	
	private void processChangeConsResp() throws DataException {
		
		initAndValidateCurrentCons();
		
		initAndValidateMainCard();
		
		initAndValidateNewCons();
		
		initAndValidateExistingCons();
		
		// ������� ����� � �������������
		List<ObjectId> idsToCreate = new ArrayList<ObjectId>(newConsid.getIdsLinked()); //
		Set<Card> cardsToChange = new HashSet<Card>();
		
		compareNewAndOldConsiderations(idsToCreate, cardsToChange, true);
		
		// ��������
		
		createNewCons(idsToCreate);
		
		// ����������� ��� ������������ �����������
		
		editExistingCons(cardsToChange);
		
		// ������������ ������� ������������

		ListAttribute la = currLoad.getAttributeById(respPerson);
		if(la != null) {
			ReferenceValue ref = new ReferenceValue();
			ref.setId(respNo);
			la.setValue(ref);
			doOverwriteCardAttributes(currLoad.getId(), getSystemUser(), la);
		}
	}
	
	
	private void processAddCons() throws DataException {
		
		initAndValidateCurrentCons();
		
		initAndValidateMainCard();
		
		initAndValidateNewCons();
		
		initAndValidateExistingCons();
		
		// ������� ����� � �������������
		List<ObjectId> idsToCreate = new ArrayList<ObjectId>(newConsid.getIdsLinked()); //
		Set<Card> cardsToChange = new HashSet<Card>();
		
		compareNewAndOldConsiderations(idsToCreate, cardsToChange, false);
		
		// ��������
		
		createNewCons(idsToCreate);
		
		// ����������� ��� ������������ �����������
		
		editExistingCons(cardsToChange);
		
	}
	
	
	private void processRemoveCons() throws DataException {
		
		initAndValidateCurrentCons();
		
		initAndValidateMainCard();
		
		initAndValidateExistingCons();
		
		// �������� ������� ������������

		cancelCons();
		
		// �������� ���������
		
		cancelResolutions();
		
		// ��������� �� � ��������
		
		mainDocToExecuted();
	}
	
	
	private void processChangeConsTerm() throws DataException {
		
		initAndValidateCurrentCons();
		
		initAndValidateMainCard();
		
		initAndValidateExistingCons();
		
		// ������������ ������� ������������

		DateAttribute da_cons = currLoad.getAttributeById(datePerson);
		DateAttribute da_req = card.getAttributeById(dateReq);
		if(da_cons != null) {
			da_cons.setValue(da_req.getValue());
			doOverwriteCardAttributes(currLoad.getId(), getSystemUser(), da_cons);
		}
		
		// ��������� �� � ��������
		
		mainDocToExecuted();
	}
	
	
	@Override
	public void setParameter(String name, String value) {
		if (PARAM_CURRENT_CONS.equalsIgnoreCase(name)) {
			currentConsId = IdUtils.smartMakeAttrId(value, CardLinkAttribute.class);
		} else if(PARAM_NEW_CONS.equalsIgnoreCase(name)) {
			newConsId = IdUtils.smartMakeAttrId(value, DatedTypedCardLinkAttribute.class);
		} else if(PARAM_REQUEST_TYPE.equalsIgnoreCase(name)) {
			requestTypeId = IdUtils.smartMakeAttrId(value, ListAttribute.class);
		} else if(PARAM_MAIN_DOC.equalsIgnoreCase(name)) {
			mainDocId = IdUtils.smartMakeAttrId(value, CardLinkAttribute.class);
		} else if(PARAM_CONS_WAY_IN_MAIN.equalsIgnoreCase(name)) {
			consWayInMainId = IdUtils.smartMakeAttrId(value, CardLinkAttribute.class);
		} else {
			super.setParameter(name, value);
		}
	}

}
