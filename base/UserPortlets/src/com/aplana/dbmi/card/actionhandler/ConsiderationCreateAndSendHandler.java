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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.apache.commons.lang.time.DateUtils;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.Parametrized;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.DatedTypedCardLinkAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.filter.PersonCardIdFilter;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.SystemUser;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.web.tag.util.StringUtils;

/**
 * 
 * @author ppolushkin
 *
 */
public class ConsiderationCreateAndSendHandler extends
					CardPortletAttributeEditorActionHandler implements Parametrized {
	
	protected static final String PARAM_STATES_ALLOWED = "statesAllowed";
	protected static final String PARAM_TARGET_CARD_ATTR_ID = "targetCardAttrId";
	protected static final String PARAM_TARGET_CARD_TEMPLATE_ID = "targetCardTemplateId";
	protected static final String PARAM_TARGET_CARD_PERSON_ATTR_ID = "targetCardPersonAttributeId";
	protected static final String PARAM_MAIN_CARD_PERSON_ATTR_ID = "mainCardPersonAttributeId";
	protected static final String PARAM_SEND_WORKFLOW_MOVE_ID = "sendWorkflowMoveId";
	protected static final String PARAM_ASSAIGN_WORKFLOW_MOVE_ID = "assaignWorkflowMoveId";
	protected static final String PARAM_DATE_TERM = "dateTerm";
	protected static final String PARAM_RESPONSIBLE = "responsible";
	
	protected ObjectId ATTR_VISA_ORDER =
			ObjectId.predefined(IntegerAttribute.class, "jbr.exam.order");
	
	protected ObjectId CARD_STATE_ASSAIGN =
			ObjectId.predefined(CardState.class, "jbr.exam.assigned");
	
	protected Collection<ObjectId> statesAllowed;
	protected ObjectId targetCardAttrId;
	protected ObjectId targetCardTemplateId;
	protected ObjectId targetCardPersonAttributeId;
	protected ObjectId mainCardPersonAttributeId;
	protected ObjectId assignWorkflowMoveId;
	protected ObjectId sendWorkflowMoveId;
	protected ObjectId responsible;
	protected ObjectId dateTerm;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void process(Attribute attr, List<ObjectId> cardIds,
			ActionRequest request, ActionResponse response)
			throws DataException {
		
		CardPortletCardInfo cardInfo = getCardPortletSessionBean().getActiveCardInfo();
		Card card = cardInfo.getCard();
		CardLinkAttribute targetAttr = (CardLinkAttribute) card.getAttributeById(targetCardAttrId);
		CardLinkAttribute sourceAttr = (CardLinkAttribute) attr;
		if(
			sourceAttr == null || sourceAttr.isEmpty() || targetAttr == null 
			|| targetCardTemplateId == null || targetCardPersonAttributeId == null
		) return;
		
		PersonAttribute mainCardPersonAttr = (PersonAttribute) card.getAttributeById(mainCardPersonAttributeId);
		
		CreateCard create = new CreateCard();
		create.setTemplate(targetCardTemplateId);
		create.setLinked(true);
			
		try{
			//�������� ����� ������������
			if(DatedTypedCardLinkAttribute.class.equals(sourceAttr.getClass())){
				DatedTypedCardLinkAttribute datedTypedCardLinkAttribute = (DatedTypedCardLinkAttribute) sourceAttr;
				for(Entry<Long,Date> entry: datedTypedCardLinkAttribute.getDates().entrySet()){
					if(entry.getValue()!=null && entry.getValue().before(DateUtils.truncate(new Date(), Calendar.DATE))){
						throw new DataException("���� ������������ �� ������ ���� ������ ����������� ����");
					}
				}
				
			}
			
			ChangeState changeState1 = new ChangeState();
			final WorkflowMove wfmAssign = (WorkflowMove) serviceBean.getById(assignWorkflowMoveId);
			
			ChangeState changeState2 = new ChangeState();
			final WorkflowMove wfmSend = (WorkflowMove) serviceBean.getById(sendWorkflowMoveId);
			
			
			/*
			 * ��������� ������ ������ �� id �������� ������...
			 */
			final PersonCardIdFilter filter = new PersonCardIdFilter();
			filter.setCardIds(sourceAttr.getIdsLinked());
			final Collection<Person> personsToProcess =  
					serviceBean.filter(Person.class, filter);
			
			// ����� id ������, ������� ��� ���� � ������ "������������"
			Set<ObjectId> existsPersonsIds = 
					getPersonIds(targetAttr, targetCardPersonAttributeId);
			
			Collection<Card> linkedCards = new ArrayList<Card>();
			
			for (Person person : personsToProcess) {
				if (existsPersonsIds.contains(person.getId())) 
				{
					logger.info("Consideration card for user '" + person.getFullName() + "' already exists");
					continue;
				}
				
				/*
				 * �������� ����� ��������... 
				 */
				Card linked = (Card) serviceBean.doAction(create);
				
				// ������ ����������������... 
				PersonAttribute personAttribute = (PersonAttribute) linked.getAttributeById(targetCardPersonAttributeId);
				personAttribute.setPerson(person);

				// ���������� �����������...
				final IntegerAttribute order = (IntegerAttribute) 
						linked.getAttributeById(ATTR_VISA_ORDER);
				order.setValue(0);
				
				// ������ ���� � ��������������� ���� �����
				if(TypedCardLinkAttribute.class.equals(sourceAttr.getClass())
						|| DatedTypedCardLinkAttribute.class.equals(sourceAttr.getClass())) {
					
					TypedCardLinkAttribute a = (TypedCardLinkAttribute) sourceAttr;
					
					final ReferenceValue val;
					final Long valueLong = (Long) a.getTypes().get(person.getCardId().getId());
					if(valueLong != null) {
						final ObjectId valueId = new ObjectId(ReferenceValue.class, valueLong);
						val = new ReferenceValue();
						val.setId(valueId);
						
						final ListAttribute resp = (ListAttribute)
								linked.getAttributeById(responsible);
						
						resp.setValue(val);
					}
				}
				
				final DateAttribute term = (DateAttribute)
						linked.getAttributeById(dateTerm);
				
				if(DatedTypedCardLinkAttribute.class.equals(sourceAttr.getClass())) {
					
					DatedTypedCardLinkAttribute a = (DatedTypedCardLinkAttribute) sourceAttr;
					
					final Date valueDate = (Date) a.getDates().get(person.getCardId().getId());
					if(valueDate != null) {
						term.setValue(valueDate);
					} else {
						Calendar calendar = Calendar.getInstance();
						calendar.add(Calendar.DATE, 29);
						term.setValue(calendar.getTime());
					}
				} else {
					term.setValue(new Date());
				}
				
				try{
					linked.setId(serviceBean.saveObject(linked, ExecuteOption.SYNC));
				} finally {
					serviceBean.doAction(new UnlockObject(linked));
				}
				targetAttr.addLinkedId(linked.getId());
				if(mainCardPersonAttr != null && mainCardPersonAttr.getValues() != null) {
					mainCardPersonAttr.getValues().add(person);
				}
				linkedCards.add(linked);
			}
			
			boolean locked = false;
			attr.clear();
			cardInfo.setRefreshRequired(true);
			
			if(cardInfo.getMode().equals(CardPortlet.CARD_EDIT_MODE)){
				serviceBean.saveObject(card, ExecuteOption.SYNC);
			} else {
				try{
					serviceBean.doAction(new LockObject(card));
					locked = true;
					serviceBean.saveObject(card, ExecuteOption.SYNC);
				} finally {
					if (locked){
						serviceBean.doAction(new UnlockObject(card));
					}
				}
			}
			cardInfo.setRefreshRequired(true);
			
			if(assignWorkflowMoveId != null){
				for(Card linked : linkedCards){
					/*
					 * PPanichev 02.02.2015
					 * ������ setWorkflowMove �� ������ ��������,
					 * �� ��������� �������� ��� � sendWorkflowMoveId
					 * 
					 */
					changeState1.setWorkflowMove(wfmAssign);
					changeState1.setCard(linked);
					//serviceBean.setUser(new SystemUser());
					locked = false;
					try{
						serviceBean.doAction(new LockObject(linked));
						locked = true;
						// (2014/03/04 ����� � YNikitin, 2013/05/13) ������ �������� �� ������������ ������� ������������ � ����������, 
						// ����� �������� ���������� �������� � ���������� �� ��������.
						serviceBean.doAction(changeState1, ExecuteOption.SYNC);
					} finally {
						if (locked){
							serviceBean.doAction(new UnlockObject(linked));
						}
						//serviceBean.setUser(request.getUserPrincipal());
					}
				}
			}
			
			if(sendWorkflowMoveId != null){
				for(Card linked : linkedCards){
					/*
					 * PPanichev 02.02.2015
					 * ������ setWorkflowMove �� ������ ��������, ����� ��������������� move'� 
					 * �� ��������� ����������� �� ������ �������
					 * 
					 */
					changeState2.setWorkflowMove(wfmSend); 
					//������������ ��������, ����� �������� �� ���������� ���������
					Long begin = System.currentTimeMillis();
					Card currentCard = (Card) serviceBean.getById(linked.getId());
					Long end = System.currentTimeMillis();
					logger.info("Get card time '" + (end - begin) + "' millis.");
					if (currentCard.getState().equals(CARD_STATE_ASSAIGN)) {
						changeState2.setCard(currentCard);
						serviceBean.setUser(new SystemUser());
						locked = false;
						try{
							serviceBean.doAction(new LockObject(linked));
							locked = true;
							serviceBean.doAction(changeState2, ExecuteOption.SYNC);
						} finally {
							if (locked){
								serviceBean.doAction(new UnlockObject(linked));
							}
							serviceBean.setUser(request.getUserPrincipal());
						}
					}
				}
			}
			// ����� ����, ��� ��� ������������ ���������� �� ������������, ���������� �������� ������� ������ ��������
			Card currentCard = (Card) serviceBean.getById(card.getId());
			cardInfo.setCard(currentCard);
		} catch (DataException e){
            //Show error message from ChkDateProcessor validator to user
            getCardPortletSessionBean().setMessage(e.getMessage());
			throw e;
		} catch (Exception e){throw new DataException(e);}
		finally{
			cardInfo.setRefreshRequired(true);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected Set<ObjectId> getPersonIds(CardLinkAttribute targetAttr, ObjectId targetPersonAttributeId) throws DataException, ServiceException {
		
		Set<ObjectId> result = new HashSet<ObjectId>();
		
		if(targetAttr.getIdsLinked() == null || targetAttr.getIdsLinked().isEmpty()) {
			return result;
		}
		
		Search search = new Search();
		search.setByCode(true);
		search.setWords(StringUtils.collectionToCommaDelimitedString(targetAttr.getIdsLinked()));
		List<SearchResult.Column> cols = new ArrayList<SearchResult.Column>();
		SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(targetPersonAttributeId);
		cols.add(col);
		search.setColumns(cols);
		
		SearchResult searchResult = (SearchResult) serviceBean.doAction(search);
		
		List<Card> requestCards = searchResult.getCards();
		if(requestCards == null && requestCards.isEmpty()) {
			return result;
		}
		
		for(Card card : requestCards) {
			/*if ((statesAllowed != null) && !statesAllowed.contains(card.getState()))
				continue;*/
			PersonAttribute a = (PersonAttribute) card.getAttributeById(targetPersonAttributeId);
			result.add(a.getPerson().getId());
		}
		
		return result;
	}
	
	@Override
	public boolean isApplicableForUser() {
		Card card = getCardPortletSessionBean().getActiveCard();
		return statesAllowed == null || statesAllowed.contains(card.getState());
	}

	public void setParameter(String name, String value) {
		if(PARAM_STATES_ALLOWED.equalsIgnoreCase(name)){
			statesAllowed = ObjectIdUtils.commaDelimitedStringToNumericIds(value, CardState.class);
		} else if(PARAM_TARGET_CARD_ATTR_ID.equalsIgnoreCase(name)){
			targetCardAttrId =  IdUtils.smartMakeAttrId(value, CardLinkAttribute.class);
		} else if(PARAM_TARGET_CARD_TEMPLATE_ID.equalsIgnoreCase(name)){
			targetCardTemplateId = ObjectIdUtils.getObjectId(Template.class, value, true);
		} else if(PARAM_TARGET_CARD_PERSON_ATTR_ID.equalsIgnoreCase(name)){
			targetCardPersonAttributeId = ObjectIdUtils.getObjectId(PersonAttribute.class, value, false);
		} else if(PARAM_MAIN_CARD_PERSON_ATTR_ID.equalsIgnoreCase(name)){
			mainCardPersonAttributeId = ObjectIdUtils.getObjectId(PersonAttribute.class, value, false);
		} else if(PARAM_ASSAIGN_WORKFLOW_MOVE_ID.equalsIgnoreCase(name)){
			assignWorkflowMoveId = ObjectIdUtils.getObjectId(WorkflowMove.class, value, true);
		} else if(PARAM_SEND_WORKFLOW_MOVE_ID.equalsIgnoreCase(name)){
			sendWorkflowMoveId = ObjectIdUtils.getObjectId(WorkflowMove.class, value, true);
		} else if(PARAM_DATE_TERM.equalsIgnoreCase(name)){
			dateTerm = ObjectIdUtils.getObjectId(DateAttribute.class, value, false);
		} else if(PARAM_RESPONSIBLE.equalsIgnoreCase(name)) {
			responsible = ObjectIdUtils.getObjectId(ListAttribute.class, value, false);
		}
	}
	
}
