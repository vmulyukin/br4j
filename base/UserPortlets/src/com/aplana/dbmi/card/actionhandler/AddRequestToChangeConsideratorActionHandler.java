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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.CheckLock;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.actionhandler.descriptor.ActionsDescriptor;
import com.aplana.dbmi.card.ActionsSupportingAttributeEditor;
import com.aplana.dbmi.card.AttributeEditor;
import com.aplana.dbmi.card.CardLinkPickerAttributeEditor;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.card.hierarchy.Messages;
import com.aplana.dbmi.card.Parametrized;
import com.aplana.dbmi.card.PortletFormManager;
import com.aplana.dbmi.card.PortletFormManagerAware;
import com.aplana.dbmi.jbr.action.GetBoss;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ObjectNotLockedException;
import com.aplana.dbmi.service.ServiceException;

public class AddRequestToChangeConsideratorActionHandler extends AbstractAddLinkedCardActionHandler implements PortletFormManagerAware, Parametrized {
	
	public static final String CHAGE_STATE_SUCCESS = "changeStateSuccess";
	public static final String CHAGE_STATE_BUTTON_TITLE = "tool.change.status";
	public static final String STORE_BUTTON_TITLE = "edit.page.generate.btn";

	public class ChangeStateGeneratedRequestsHandler implements CardPortletCardInfo.CustomChangeStateHandler {
		private Log logger = LogFactory.getLog(getClass());
		private WorkflowMove flow;
		
		private CardPortletSessionBean session;

		public ChangeStateGeneratedRequestsHandler(CardPortletSessionBean session, WorkflowMove flow) {
			this.session = session;
			this.flow = flow;
		}

		public void changeState() throws DataException, ServiceException {
			if(flow == null) {
				throw new IllegalStateException("flow must be not null");
			}
			
			try {
				ChangeState action = new ChangeState();
				action.setCard(session.getActiveCard());
				action.setWorkflowMove(flow);
				serviceBean.doAction(action);
				logger.info("Card sent to change state");
				String msg = getMessage(CHAGE_STATE_SUCCESS);
				PortletMessageType msgType = PortletMessageType.EVENT;
				session.setMessageWithType(msg, msgType);
			} catch (ServiceException e) {
				throw new DataException(e.getMessage(), e);
			} catch (DataException e) {
				throw new DataException(e.getMessage(), e);
			}
		}

		public String getChangeStateButtonTitle() {
			return CHAGE_STATE_BUTTON_TITLE;
		}

		public WorkflowMove getWorkflowMove() {
			return flow;
		}

		public void setWorkflowMove(WorkflowMove flow) {
			this.flow = flow;
		}

		public String getChangeStateActionName() {
			return CardPortlet.CUSTOM_STORE_AND_CHANGE_STATE_CURRENT_CARD_ACTION;
		}

		public boolean isShowFirstSaveButton() {
			return false;
		}
	}
	
	
	protected static class RequestToChangeConsideratorCloseHandler implements CardPortletCardInfo.CloseHandler {
		protected Log logger = LogFactory.getLog(getClass());	
		protected ObjectId cardLinkId;
		protected ObjectId idsToLinkId;
		protected CardPortletSessionBean sessionBean;
		private CardStateInfo previousCardState = null;
		
		public RequestToChangeConsideratorCloseHandler(ObjectId cardLinkId, ObjectId idsToLinkId, CardPortletSessionBean sessionBean) {
			initialize(cardLinkId, idsToLinkId, sessionBean, null);			
		}
		
		public RequestToChangeConsideratorCloseHandler(ObjectId cardLinkId, ObjectId idsToLinkId, CardPortletSessionBean sessionBean, CardStateInfo previousCardState) {
			initialize(cardLinkId, idsToLinkId, sessionBean, previousCardState);
		}
		
		private void initialize(ObjectId cardLinkId, ObjectId idsToLinkId, CardPortletSessionBean sessionBean, CardStateInfo previousCardState){
			this.cardLinkId = cardLinkId;
			this.sessionBean = sessionBean;
			this.idsToLinkId = idsToLinkId;
			this.previousCardState=previousCardState;
		}
		
		private boolean checkCardNotLocked(ObjectId cardId){
			CheckLock checkLock = new CheckLock(cardId);
			try{
				sessionBean.getServiceBean().doAction(checkLock);
			}catch (ObjectNotLockedException e) {				
				return true;
			}catch (Exception e) {
				return false;
			}
			return false;
		}
		
		
		private void restoreCard(CardPortletCardInfo cardInfo){
			//���� �������� �����������, �� �� ����� � �������
			if(previousCardState == null || cardInfo == null || cardInfo.getCard().getId() == null || !checkCardNotLocked(cardInfo.getCard().getId())){
				return;
			}
			cardInfo.setMode(previousCardState.getMode());
			try {
				sessionBean.getServiceBean().doAction(new LockObject(cardInfo.getCard().getId()));
			} catch (DataException e) {				
				logger.error("Can't locked card", e);
			} catch (ServiceException e) {				
				logger.error("Can't locked card", e);
			}			
		}

		@SuppressWarnings("unchecked")
		public void afterClose(CardPortletCardInfo closedCardInfo, CardPortletCardInfo previousCardInfo) {
			ObjectId newCardId = closedCardInfo.getCard().getId();
			
			restoreCard(previousCardInfo); //��������������� ��������� ������������ ��������

			Attribute attr = previousCardInfo.getCard().getAttributeById(cardLinkId);
			if (newCardId != null && idsToLinkId == null) {
				if (attr.getId().getType().equals(TypedCardLinkAttribute.class)) {
					TypedCardLinkAttribute typedAttr = (TypedCardLinkAttribute) attr;
					Collection<ReferenceValue> values = typedAttr.getReferenceValues();
					if(values == null || values.isEmpty()){
						DataServiceBean serviceBean = sessionBean.getServiceBean();
						try {
							values = serviceBean.listChildren(typedAttr.getReference(), ReferenceValue.class);
						} catch(Exception e) {e.printStackTrace();}
						if(values == null || values.isEmpty()) {
							sessionBean.setMessage("addtypedlink.error");
							return;
						}
					}
					if(!typedAttr.isMultiValued()) typedAttr.clear();
					typedAttr.addType((Long) newCardId.getId(), (Long)values.iterator().next().getId().getId());
				}
				else if(attr.getId().getType().equals(CardLinkAttribute.class)) {
					CardLinkAttribute linkAttr = (CardLinkAttribute) attr;
					if (linkAttr.isMultiValued()) {
						linkAttr.addLinkedId(newCardId);
					} else { // �������� ������ ���� ��������
						linkAttr.addSingleLinkedId(newCardId);
					}
				}
				previousCardInfo.setAttributeEditorData(cardLinkId, AttributeEditor.KEY_VALUE_CHANGED, Boolean.TRUE);
			} else if(idsToLinkId != null && closedCardInfo.getCard().getAttributeById(idsToLinkId) != null){
				Attribute idsToLinkAttr =  closedCardInfo.getCard().getAttributeById(idsToLinkId);
				if(cardLinkId.getType().equals(idsToLinkId.getType())){
					if(cardLinkId.getType().equals(PersonAttribute.class)){
						if(!((PersonAttribute) attr).isMultiValued())
							((PersonAttribute) attr).setPerson(((PersonAttribute) idsToLinkAttr).getPerson());
						else
							((PersonAttribute) attr).getValues().addAll(((PersonAttribute) idsToLinkAttr).getValues());
					} else if(cardLinkId.getType().equals(CardLinkAttribute.class)){
						if(!((CardLinkAttribute) attr).isMultiValued())
							((CardLinkAttribute) attr).addSingleLinkedId(((CardLinkAttribute) idsToLinkAttr).getSingleLinkedId());
						else
							((CardLinkAttribute) attr).addIdsLinked(((CardLinkAttribute) idsToLinkAttr).getIdsLinked());
					} else if(cardLinkId.getType().equals(TypedCardLinkAttribute.class)){
						if(!((TypedCardLinkAttribute) attr).isMultiValued()) {
							Map.Entry<Long,Long> entry = (Map.Entry<Long,Long>) ((TypedCardLinkAttribute) idsToLinkAttr).getTypes().entrySet().iterator().next();
							((TypedCardLinkAttribute) attr).clear();
							((TypedCardLinkAttribute) attr).addType(entry.getKey(), entry.getValue());
						}
						((TypedCardLinkAttribute) attr).getTypes().putAll(((TypedCardLinkAttribute) idsToLinkAttr).getTypes());
					}
					previousCardInfo.setAttributeEditorData(cardLinkId, AttributeEditor.KEY_VALUE_CHANGED, Boolean.TRUE);
				} else if(cardLinkId.getType().equals(TypedCardLinkAttribute.class) 
							&& idsToLinkAttr.getId().getType().equals(PersonAttribute.class)) {
					if(!((TypedCardLinkAttribute) attr).isMultiValued())
						((TypedCardLinkAttribute) attr).addSingleLinkedId(((PersonAttribute) idsToLinkAttr).getPerson().getCardId());
					else {
						for( Iterator<?> itr = ((PersonAttribute) idsToLinkAttr).getValues().iterator(); itr.hasNext(); )
						{
							((CardLinkAttribute) attr).addLinkedId(((Person) itr.next()).getCardId());
						}
					}
				}
			}
			sessionBean.setEditorData(cardLinkId, CardLinkPickerAttributeEditor.KEY_CACHE_RESET, true);
		}
	}

	public static final String TEMPLATE_ID_PARAM = "template";
	public static final String IS_LINKED_PARAM = "isLinked";
	public static final String IDS_TO_LINK_ATTR_PARAM = "useThisAttrInsteadOfCardId";
	public static final String PARAM_ATTR_COPY = "copyToNewCard";
	/**
	 * ������� �������� �� �������� ��������� ��������, ��� ������� ����� ��������
	 */
	public static final String PARAM_STATES_ALLOWED = "statesAllowed";
	/**
	 * ������ �� ������������ �������� �� ���������
	 */
	public static final String PARAM_MAINDOC_LINK = "mainDocLink";
	/**
	 * ������ �� �������� ������������ �� ������������
	 */
	public static final String PARAM_RASSM_LINK = "rassmLink";
	/**
	 * ������� ������� �������� ���� ���������� ���������� ����� �� ������������
	 */
	public static final String PARAM_TARGET_LINK = "targetLink";
	/**
	 * ������� �������� ������������, ��� ������� �������� ������������
	 */
	public static final String PARAM_RASSM_STATES_IGNORED = "ignoredRassmStates";
	/**
	 * ��������, ������ ����� ��������� � ������ ��� ���������� ���������� ����� ����� ��������
	 */
	public static final String PARAM_AVA_WORKFLOW = "availableWorkflow";
	
	public static final ObjectId considSet = ObjectId.predefined(CardLinkAttribute.class, "jbr.exam.set");
	public static final ObjectId considPerson = ObjectId.predefined(PersonAttribute.class, "jbr.exam.person");

	protected ObjectId templateId;
	protected HashMap<ObjectId, ObjectId> parentAttributes = new HashMap<ObjectId, ObjectId>();
	protected boolean isLinked = false;
	protected ObjectId idsToLinkAttrId;
	protected Collection<ObjectId> statesAllowed;
	protected ObjectId mainDocLink;
	protected ObjectId rassmLink;
	protected ObjectId targetLink;
	protected List<ObjectId> ignoredRassmStates;
	private ObjectId availableWorkflow;

	protected PortletFormManager manager;

	@Override
	protected Card createCard() throws DataException, ServiceException {
		CreateCard createCard = new CreateCard();
		createCard.setTemplate(templateId);
		createCard.setLinked(isLinked);
		createCard.setParent(getCardPortletSessionBean().getActiveCard());
		Card card = (Card) serviceBean.doAction(createCard);
				
		// ��������� ������� ��������-���������
		if(mainDocLink != null) {
			setMainDoc(card);
		}
				
		if (parentAttributes != null && parentAttributes.size()>0) {
			Card parentCard = getCardPortletSessionBean().getActiveCard();
			if (parentCard != null) {
				for (Map.Entry<ObjectId, ObjectId> entry: parentAttributes.entrySet()) {
					Attribute attrFrom = parentCard.getAttributeById(entry.getKey());
					Attribute attrTo = card.getAttributeById(entry.getValue());
					if (attrFrom != null && attrTo != null && attrFrom instanceof CardLinkAttribute
							&& attrTo instanceof CardLinkAttribute) {
						((CardLinkAttribute)attrTo)
								.setIdsLinked(((CardLinkAttribute)attrFrom).getIdsLinked());
					}
				}
			}
		}
		
		setDefaultConsiderator(card);
		
		return card;
	}
	
	protected void setDefaultConsiderator(Card card) throws DataException, ServiceException {
		Card parentCard = getCardPortletSessionBean().getActiveCard();
		LinkAttribute rassmAttr = (LinkAttribute) parentCard.getAttributeById(rassmLink);
		if(rassmAttr == null || rassmAttr.isEmpty()) {
			return;
		}
		
		GetBoss getBossAction = new GetBoss();
		getBossAction.setAssistantIds(Collections.singletonList(serviceBean.getPerson().getId()));
		List<ObjectId> personIds = (List<ObjectId>) serviceBean.doAction(getBossAction);
		personIds.add(serviceBean.getPerson().getId());
		
		Search search = new Search();
		search.setByCode(true);
		search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(rassmAttr.getIdsLinked()));
		List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(considPerson);
		columns.add(col);
		col = new SearchResult.Column();
		col.setAttributeId(Card.ATTR_STATE);
		columns.add(col);
		search.setColumns(columns);
		SearchResult result = serviceBean.doAction(search);
		if(result == null || result.getCards() == null || result.getCards().isEmpty()) {
			return;
		}
		List<Card> rassmCards = new ArrayList<Card>();
		
		for(Card rassmCard : result.getCards()) {
			if(ignoredRassmStates != null 
					&& ignoredRassmStates.contains(rassmCard.getState())) {
				continue;
			}
			PersonAttribute rassmUser = rassmCard.getAttributeById(considPerson);
			if(rassmUser == null || rassmUser.isEmpty()) {
				continue;
			}
			boolean flag = false;
			for(Object p : rassmUser.getValues()) {
				if(personIds.contains(((Person)p).getId())) {
					flag = true;
				}
			}
			if(!flag) {
				continue;
			}
			
			rassmCards.add(rassmCard);
		}
		
		if(rassmCards.isEmpty()) {
			return;
		}
		
		LinkAttribute targetAttr = (LinkAttribute) card.getAttributeById(targetLink);
		if(targetAttr != null) {
			targetAttr.clear();
			targetAttr.addSingleLinkedId(rassmCards.get(0).getId());
		}
	}
	
	protected void setMainDoc(Card card) {
		Card parentCard = getCardPortletSessionBean().getActiveCard();
		LinkAttribute linkAttr = (LinkAttribute) card.getAttributeById(mainDocLink);
		if(linkAttr != null) {
			linkAttr.clear();
			linkAttr.addSingleLinkedId((Long) parentCard.getId().getId());
		}
	}
	

	@Override
	/**
	 * ���������� ������ ���� � ������������ ������� ���� �� �������� �� "������ �� ��������� ����������������"
	 * � ���� ����� �� �������������� ������� ��������.
	 */
	public boolean isApplicableForUser() {
		try {
			Card card = getCardPortletSessionBean().getActiveCard();
			boolean isAllowedState = (statesAllowed == null || statesAllowed.contains(card.getState()));
			if(!isAllowedState)
				return false;
			CreateCard action = new CreateCard();
			action.setTemplate(templateId);
			return serviceBean.canDo(action) && serviceBean.canChange(card.getId());
		} catch (Exception e) {
			logger.error("Exception caught while checking user permissions for template", e);
			return false;
		}
	}

	public void setParameter(String name, String value) {
		if(PARAM_STATES_ALLOWED.equalsIgnoreCase(name)){
			statesAllowed = ObjectIdUtils.commaDelimitedStringToNumericIds(value, CardState.class);
		} else if(PARAM_RASSM_STATES_IGNORED.equalsIgnoreCase(name)){
			ignoredRassmStates = ObjectIdUtils.commaDelimitedStringToNumericIds(value, CardState.class);
		} else if (TEMPLATE_ID_PARAM.equals(name)) {
			this.templateId = ObjectIdUtils.getObjectId(Template.class, value, true);
		} else if (IS_LINKED_PARAM.equals(name)) {
			this.isLinked = Boolean.parseBoolean(value);
		} else if(IDS_TO_LINK_ATTR_PARAM.equals(name)){
			idsToLinkAttrId = ObjectIdUtils.getObjectId(
					Arrays.asList(CardLinkAttribute.class, TypedCardLinkAttribute.class, PersonAttribute.class), CardLinkAttribute.class, value, false
			);
		} else if(PARAM_ATTR_COPY.equals(name)){
			String[] pair = value.split("->");
			if (pair.length != 2) {
				logger.error("Illegal value of parameter " + name + ": " + value);
			}
			ObjectId sourceId = AttrUtils.getAttributeId(pair[0].trim());
			ObjectId destId = AttrUtils.getAttributeId(pair[1].trim());
			parentAttributes.put(sourceId, destId);
		} else if(PARAM_MAINDOC_LINK.equalsIgnoreCase(name)) {
			mainDocLink = ObjectIdUtils.getObjectId(CardLinkAttribute.class, value.trim(), false);
		} else if(PARAM_RASSM_LINK.equalsIgnoreCase(name)) {
			rassmLink = ObjectIdUtils.getObjectId(CardLinkAttribute.class, value.trim(), false);
		} else if(PARAM_TARGET_LINK.equalsIgnoreCase(name)) {
			targetLink = ObjectIdUtils.getObjectId(CardLinkAttribute.class, value.trim(), false);
		} else if(PARAM_AVA_WORKFLOW.equals(name)) {
			availableWorkflow = ObjectIdUtils.getObjectId(WorkflowMove.class, value.trim(), true);
		} else {
			logger.warn("param " + name + " is not processed");
		}
	}

	//TODO ����������� � ��������� ������������.
	@Override
	protected void process(Attribute attr, List<ObjectId> cardIds, ActionRequest request, ActionResponse response)
			throws DataException {

		if(templateId == null){
			throw new DataException("param templateId cannot be null");
		}
		else {
			openNewCard();
		}
	}

	public void setPortletFormManager(PortletFormManager portletFormManager) {
		this.manager = portletFormManager;

	}
	
	private CardPortletCardInfo parentCardInfo;

	protected void openNewCard() {
		CardPortletSessionBean sessionBean = getCardPortletSessionBean();
		try {
			
			parentCardInfo = sessionBean.getActiveCardInfo();
			Card child = createCard();
			CardStateInfo activeCardState = saveActiveCardState(sessionBean);
			
			sessionBean.openNestedCard(
	    			child, 
	    			new RequestToChangeConsideratorCloseHandler(attribute.getId(), idsToLinkAttrId, sessionBean, activeCardState), 
	    			true
	    	);
			
			if(availableWorkflow != null) {
				WorkflowMove flowMove = (WorkflowMove) serviceBean.getById(availableWorkflow);
				CardPortletCardInfo.CustomChangeStateHandler changeStateHandler = new ChangeStateGeneratedRequestsHandler(sessionBean, flowMove);
				sessionBean.getActiveCardInfo().setChangeStateHandler(changeStateHandler);
			}
			
			sessionBean.getActiveCardInfo().setParentCardInfo(parentCardInfo);
			
		} catch (Exception e) {
			logger.error("Can't redirect to card editing page", e);
			sessionBean.setMessageWithType("edit.link.error.create", new Object[] {e.getMessage()} , PortletMessageType.ERROR);
		}
	}
	
	private CardStateInfo saveActiveCardState(CardPortletSessionBean sessionBean) {
		CardStateInfo activeCardState = null;
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		ObjectId cardId = cardInfo.getCard().getId();		
		if(CardPortlet.CARD_EDIT_MODE.equals(cardInfo.getMode()) && (cardId != null) && checkCurrentUserLocked(cardId, sessionBean.getServiceBean())){
			activeCardState = createCardStateInfo(cardInfo, sessionBean);	
		}
		return activeCardState;
	}
	
	private CardStateInfo createCardStateInfo(CardPortletCardInfo cardInfo, CardPortletSessionBean sessionBean) {
		CardStateInfo cardStateInfo = new CardStateInfo();		
		cardStateInfo.setMode(cardInfo.getMode());
		cardStateInfo.setPerson(sessionBean.getServiceBean().getPerson());
		return cardStateInfo;
	}
	
	private boolean checkCurrentUserLocked(ObjectId cardId, DataServiceBean serviceBean) {
		CheckLock checkLock = new CheckLock(cardId);
		try{
			serviceBean.doAction(checkLock);
		}catch (Exception e) {
			logger.warn("The object is locked not current user!");
			return false;
		}
		return true;
	}
	
	private Messages messages = null;
	private String getMessage(String key, Object[] params) {
		if (messages == null) {
			ActionsDescriptor desc = (ActionsDescriptor) parentCardInfo.getAttributeEditorData(getAttribute().getId(),
					ActionsSupportingAttributeEditor.ACTIONS_DESCRIPTOR);
			messages = desc.getMessages();
		}
		return MessageFormat.format(messages.getMessage(key).getValue(), params);
	}
	
	private String getMessage(String key) {
		return getMessage(key, null);
	}
	
	protected static class CardStateInfo {
		private String mode = null;
		private Person person = null;
		
		public CardStateInfo() {
			
		}
		
		public CardStateInfo(String mode, Person person) {
			this.mode = mode;
			this.person = person;
		}
		public String getMode() {
			return mode;
		}
		public void setMode(String mode) {
			this.mode = mode;
		}
		public Person getPerson() {
			return person;
		}
		public void setPerson(Person person) {
			this.person = person;
		}
		
	}

}
