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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.Parametrized;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.action.BatchAsyncExecution;

public class AcquaintanceCreateAndSendHandler extends
		CardPortletAttributeEditorActionHandler implements Parametrized{

	private static final String RESOURCE_BUNDLE = "com.aplana.dbmi.card.nl.CardPortletResource";
	private Collection<ObjectId> statesAllowed;
	private ObjectId targetCardAttrId;
	private ObjectId targetCardTemplateId;
	private ObjectId targetCardPersonAttributeId;
	private ObjectId sendWorkflowMoveId;
	//������� ���� � ��������, �� ������� ��������� ������������
	private ObjectId dateFromId;
	//������� ���� � ��������� ������������
	private ObjectId dateToId;
	private ObjectId commentFrom;
	private ObjectId commentTo;
	private boolean  commentParentClear;
	
	@Override
	protected void process(Attribute attr, List<ObjectId> cardIds,
			ActionRequest request, ActionResponse response)
			throws DataException {
		
		CardPortletCardInfo cardInfo = getCardPortletSessionBean().getActiveCardInfo();
		Card card = cardInfo.getCard();
		String cardName = card.getAttributeById(Attribute.ID_NAME).getStringValue();
		CardLinkAttribute targetAttr = (CardLinkAttribute) card.getAttributeById(targetCardAttrId);
		PersonAttribute sourceAttr = (PersonAttribute) attr;
		TextAttribute commentFromMainDoc = (TextAttribute) card.getAttributeById(commentFrom);
		if(
			sourceAttr == null || sourceAttr.isEmpty() || targetAttr == null 
			|| (statesAllowed != null && !statesAllowed.contains(card.getState()))
			|| targetCardTemplateId == null || targetCardPersonAttributeId == null
		) return;
		
		try {
			//Retrieve date value
			DateAttribute informDateAttrSrc = null;
			Date dateFromAttributeValue = null;
			if (dateFromId != null) {
				informDateAttrSrc = (DateAttribute) card.getAttributeById(dateFromId);
				if (informDateAttrSrc != null) {
					if (informDateAttrSrc.getValue() != null) { 
						dateFromAttributeValue = informDateAttrSrc.getValue();
					}
				}
			}

			//if date is not null compare it with current. It should be equals or later. Null is also valid value.
			if (dateFromAttributeValue != null) {
				Date currentDate = getCurrentDate();
				if (dateFromAttributeValue.before(currentDate)) {
					SimpleDateFormat formatter = new SimpleDateFormat( "dd.MM.yyyy", new Locale("ru", "RU"));
					throw new DataException( "jbr.card.check.date.toosoon.cardinfo.2", 
							new Object[]{ informDateAttrSrc.getName(), formatter.format(currentDate), cardName });
				}
			}

			CreateCard create = new CreateCard();
			create.setTemplate(targetCardTemplateId);
			create.setLinked(true);
			Collection<Card> linkedCards = new ArrayList<Card>();

			for(Object o : sourceAttr.getValues()){
				Card linked = (Card) serviceBean.doAction(create);
				PersonAttribute personAttribute = (PersonAttribute) linked.getAttributeById(targetCardPersonAttributeId);
				if (dateFromAttributeValue != null) {
					DateAttribute informDateAttrDest =  (DateAttribute) linked.getAttributeById(dateToId);
					informDateAttrDest.setValue(dateFromAttributeValue);
				}
				if(commentFromMainDoc != null) {
					TextAttribute commentToInfDoc = (TextAttribute) linked.getAttributeById(commentTo);
					commentToInfDoc.setValue(commentFromMainDoc.getValue());
				}
				personAttribute.setPerson((Person) o);
				try{
					linked.setId(serviceBean.saveObject(linked, ExecuteOption.SYNC));
				} finally {
					serviceBean.doAction(new UnlockObject(linked));
				}
				targetAttr.addLinkedId(linked.getId());
				linkedCards.add(linked);		
			}

			attr.clear();
			if(commentParentClear && commentFromMainDoc != null) {
				commentFromMainDoc.clear();
			}
			cardInfo.setRefreshRequired(true);
			
			//���� �������� � ������ ��������������
			if(cardInfo.getMode().equals(CardPortlet.CARD_EDIT_MODE)){	
				//��������� �������� �� � ����� ������ ��� ��������� ����������
				cardInfo.setMode(CardPortlet.CARD_VIEW_MODE);
			} else {
				//����� ��������� �������� � ��������� � ������ ������
				serviceBean.doAction(new LockObject(card));
			}
			try {
				serviceBean.saveObject(card, ExecuteOption.SYNC);
			} finally {
				serviceBean.doAction(new UnlockObject(card));
			}
			
			if(sendWorkflowMoveId != null){
				List<Card> lockedCards = new ArrayList<Card>();
				try{
					BatchAsyncExecution action = new BatchAsyncExecution();
					ArrayList<Action<Void>> actions = new ArrayList<Action<Void>>();
					for(Card linked : linkedCards){
						ChangeState changeState = new ChangeState();
						changeState.setWorkflowMove((WorkflowMove) serviceBean.getById(sendWorkflowMoveId));
						changeState.setCard(linked);
						serviceBean.doAction(new LockObject(linked));
						lockedCards.add(linked);
						// (YNikitin, 2013/05/13) ��������� �������� �� ������������ ������� ������������ � ����������, ����� �������� ���������� �������� � ���������� �� ��������
						// ���� �����������, ��� ����������, ������������ � ������ ����� ���� ���� ������� � ������������� ������� ��� ����� ������� ���������� �������������. 
						// serviceBean.doAction(changeState, ExecuteOption.SYNC);
						
						// �������� �� ������������ ��������� � ����������� ����� ������ BatchAsyncExecution � ��������� ��� ������ �������-���� �������� ��� ������ ��������
						// ���� ����� �������� �� ���������������
						actions.add(changeState);
					}
					action.setActions(actions);
					action.setAttrToParent(ObjectId.predefined(BackLinkAttribute.class, "jbr.info.parent"));
					serviceBean.doAction(action, ExecuteOption.ASYNC);
				} finally {
					for(Card linked : lockedCards){
						serviceBean.doAction(new UnlockObject(linked));
					}
				}
			}
			
			ResourceBundle rb = ResourceBundle.getBundle(RESOURCE_BUNDLE, ContextProvider.getContext().getLocale());		
			getCardPortletSessionBean().setMessageWithType(rb.getString("card.async.store.success.msg"), PortletMessageType.EVENT);
			
		} catch (DataException e){
            //Show error message from ChkDateProcessor validator to user
            getCardPortletSessionBean().setMessage(e.getMessage());
			throw e;
		} catch (Exception e){
			throw new DataException(e);
		}
	}

	public void setParameter(String name, String value) {
		if(name.equalsIgnoreCase("statesAllowed")){
			statesAllowed = ObjectIdUtils.commaDelimitedStringToNumericIds(value, CardState.class);
		} else if(name.equalsIgnoreCase("targetCardAttrId")){
			targetCardAttrId = ObjectIdUtils.getObjectId(CardLinkAttribute.class, value, false);
		} else if(name.equalsIgnoreCase("targetCardTemplateId")){
			targetCardTemplateId = ObjectIdUtils.getObjectId(Template.class, value, true);
		} else if(name.equalsIgnoreCase("targetCardPersonAttributeId")){
			targetCardPersonAttributeId = ObjectIdUtils.getObjectId(PersonAttribute.class, value, false);
		} else if(name.equalsIgnoreCase("sendWorkflowMoveId")){
			sendWorkflowMoveId = ObjectIdUtils.getObjectId(WorkflowMove.class, value, true);
		} else if(name.equalsIgnoreCase("commentFrom")){
			commentFrom = ObjectIdUtils.getObjectId(TextAttribute.class, value, false);
		} else if(name.equalsIgnoreCase("commentTo")){
			commentTo = ObjectIdUtils.getObjectId(TextAttribute.class, value, false);
		} else if(name.equalsIgnoreCase("copyInformDate")){
			parseAttrs(value);
		} else if(name.equalsIgnoreCase("commentParentClear")){
			commentParentClear = Boolean.parseBoolean(value);
		}
	}
	
	private void parseAttrs(String value) {
		String[] dates = value.split("->");
		dateFromId = ObjectIdUtils.getObjectId(DateAttribute.class, dates[0].trim(), false);
		dateToId   = ObjectIdUtils.getObjectId(DateAttribute.class, dates[1].trim(), false);
	}
	
	private Date getCurrentDate() {
		Calendar currentDate = Calendar.getInstance();
		currentDate.setTime(Calendar.getInstance().getTime());
		currentDate.set(Calendar.HOUR, 0);
		currentDate.set(Calendar.HOUR_OF_DAY, 0);
		currentDate.set(Calendar.MINUTE, 0);
		currentDate.set(Calendar.SECOND, 0);
		currentDate.set(Calendar.MILLISECOND, 0);
		return currentDate.getTime();
	}

}
