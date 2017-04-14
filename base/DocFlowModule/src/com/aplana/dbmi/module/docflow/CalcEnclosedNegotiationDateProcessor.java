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

import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.ObjectAction;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.module.docflow.calendar.CalendarAPI;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ParametrizedProcessor;
import com.aplana.dbmi.service.impl.DatabaseClient;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

public class CalcEnclosedNegotiationDateProcessor extends ParametrizedProcessor implements DatabaseClient {
	private static final long serialVersionUID = 2L;

	public static final String PARAM_VISA_LINK_ATTR = "visaLinkAttr";

	//����: ������� ������������
	static final ObjectId orderAttrId = ObjectId.predefined(IntegerAttribute.class, "jbr.visa.order");
	//����: ���� �����������
	static final ObjectId negotiationPeriodAttrId = ObjectId.predefined(IntegerAttribute.class, "jbr.visa.negotiation_period");
	//����: �������������� �����������
	static final ObjectId enclosedSetAttrId = ObjectId.predefined(CardLinkAttribute.class, "jbr.visa.enclosedSet");
	//����: ���� ����������� ������
	static final ObjectId visaIncomeDateAttrId = ObjectId.predefined(DateAttribute.class, "jbr.visa.income_date");
	//����: ������������ ��
	static final ObjectId visaToDateAttrId = ObjectId.predefined(DateAttribute.class, "jbr.visa.to_date");
	//����: ���� ������������ ������������
	static final ObjectId visaActualConsentDateAttrId = ObjectId.predefined(DateAttribute.class, "jbr.visa.date_actual_consent");
	
	static final ObjectId draftStateId = ObjectId.predefined(CardState.class, "jbr.visa.draft");
	static final ObjectId assignedStateId = ObjectId.predefined(CardState.class, "jbr.visa.assigned");
	static final ObjectId waitingStateId = ObjectId.predefined(CardState.class, "jbr.visa.waiting");
	static final ObjectId assistantStateId = ObjectId.predefined(CardState.class, "boss.assistant");
	static final ObjectId disagreedStateId = ObjectId.predefined(CardState.class, "jbr.visa.disagreed");
	static final ObjectId agreedStateId = ObjectId.predefined(CardState.class, "jbr.visa.agreed");
	static final ObjectId bossAgreedStateId = ObjectId.predefined(CardState.class, "jbr.visa.boss.agreed");
	static final ObjectId canceledStateId = ObjectId.predefined(CardState.class, "jbr.visa.cancelled");
	static final ObjectId trashStateId = ObjectId.predefined(CardState.class, "trash");

	static final Set<ObjectId> runningStateSet = new HashSet<ObjectId>();
	static final Set<ObjectId> finishedStateSet = new HashSet<ObjectId>();
	
	private ObjectId visaLinkAttrId = null;
	private JdbcTemplate jdbcTemplate = null;
	
	@Override
	public Object process() throws DataException {
		runningStateSet.add(assignedStateId);
		runningStateSet.add(waitingStateId);
		runningStateSet.add(assistantStateId);
		
		finishedStateSet.add(disagreedStateId);
		finishedStateSet.add(agreedStateId);
		finishedStateSet.add(bossAgreedStateId);
		finishedStateSet.add(canceledStateId);
		
		Card visaCard = getVisaCard();

		if (visaCard != null) {
			CalendarAPI workCalendar = CalendarAPI.getInstance();
			
			final CardLinkAttribute enclosedSetAttr = (CardLinkAttribute)visaCard.getAttributeById(enclosedSetAttrId);
			if (enclosedSetAttr.getLinkedCount() > 0) {
				int currentLevelOrder = 0;
				int maxLevelOrder = 0;
				int minLevelOrder = -1;
				HashMap<Integer, NegotiationLevel> negotiationLevelMap = new HashMap<Integer, NegotiationLevel>();
				for(Iterator<ObjectId> iterator = enclosedSetAttr.getIdsLinked().iterator(); iterator.hasNext();){
					Card childVisa = loadCard(iterator.next());
					if (!trashStateId.equals(childVisa.getState())) {
						IntegerAttribute orderAttr = (IntegerAttribute) childVisa.getAttributeById(orderAttrId);
						IntegerAttribute negotiationPeriodAttr = (IntegerAttribute) childVisa.getAttributeById(negotiationPeriodAttrId);
						DateAttribute incomeDateAttr = (DateAttribute) childVisa.getAttributeById(visaIncomeDateAttrId);
						DateAttribute factDateAttr = (DateAttribute) childVisa.getAttributeById(visaActualConsentDateAttrId);
		
						if (orderAttr.getValue() > maxLevelOrder) {
							maxLevelOrder = orderAttr.getValue();
						}
						if (minLevelOrder == -1) {
							minLevelOrder = orderAttr.getValue();
						}
						else if (minLevelOrder > orderAttr.getValue()){
							minLevelOrder = orderAttr.getValue();
						}
						
						NegotiationLevel negotiationLevel = null;
						if (negotiationLevelMap.containsKey(orderAttr.getValue())) {
							negotiationLevel = negotiationLevelMap.get(orderAttr.getValue());
						}
						else {
							negotiationLevel = new NegotiationLevel();
							negotiationLevelMap.put(orderAttr.getValue(), negotiationLevel);
						}
						
						negotiationLevel.visaCardList.add(childVisa);
						if (negotiationLevel.maxVisaPeriod < negotiationPeriodAttr.getValue()) {
							negotiationLevel.maxVisaPeriod = negotiationPeriodAttr.getValue();
						}
						if (draftStateId.equals(childVisa.getState())) {
							if (negotiationLevel.state < 0) {
								negotiationLevel.state = 0;
							}
						}
						else if (runningStateSet.contains(childVisa.getState())) {
							negotiationLevel.state = 1;
							currentLevelOrder = orderAttr.getValue();
							if (incomeDateAttr.getValue() != null && incomeDateAttr.getValue().before(negotiationLevel.startDate)) {
								negotiationLevel.startDate = incomeDateAttr.getValue();
							}
						}
						else if (finishedStateSet.contains(childVisa.getState())) {
							if (negotiationLevel.state < 1) {
								negotiationLevel.state = 2;
							}
							if (incomeDateAttr.getValue() != null && incomeDateAttr.getValue().before(negotiationLevel.startDate)) {
								negotiationLevel.startDate = incomeDateAttr.getValue();
							}
							if (factDateAttr.getValue() != null && factDateAttr.getValue().after(negotiationLevel.maxVisaEndDate)) {
								negotiationLevel.maxVisaEndDate = factDateAttr.getValue();
							}
						}
					}
				}
				
				if (currentLevelOrder == 0 && minLevelOrder >= 0) {
					currentLevelOrder = minLevelOrder;
				}
				
				Date prevLevelEndDate = new Date();
				for (int levelOrder = currentLevelOrder; levelOrder <= maxLevelOrder; levelOrder++) {
					NegotiationLevel negotiationLevel = negotiationLevelMap.get(levelOrder);
					if (negotiationLevel != null) {
						Date maxVisaEndDate = new Date(0);
						if (negotiationLevel.state < 2) {
							if (levelOrder == currentLevelOrder && negotiationLevel.state == 1) {
								prevLevelEndDate = negotiationLevel.startDate;
							}
							
							for (int i = 0; i < negotiationLevel.visaCardList.size(); i++) {
								Card childVisa = negotiationLevel.visaCardList.get(i);
								DateAttribute toDateAttr = (DateAttribute) childVisa.getAttributeById(visaToDateAttrId);
								IntegerAttribute negotiationPeriodAttr = (IntegerAttribute) childVisa.getAttributeById(negotiationPeriodAttrId);
								
								if (draftStateId.equals(childVisa.getState())) {
									toDateAttr.setValue(workCalendar.addToDate(negotiationPeriodAttr.getValue(), prevLevelEndDate));
	
									insertCardDateAttributeValue(childVisa.getId(), toDateAttr.getId(), toDateAttr.getValue(), false);
									
									if (maxVisaEndDate.before(toDateAttr.getValue())) {
										maxVisaEndDate = toDateAttr.getValue();
									}
								}
								else if (runningStateSet.contains(childVisa.getState())) {
									if (toDateAttr.getValue() != null && maxVisaEndDate.before(toDateAttr.getValue())) {
										maxVisaEndDate = toDateAttr.getValue();
									}
								}
								else {
									DateAttribute factDateAttr = (DateAttribute) childVisa.getAttributeById(visaActualConsentDateAttrId);
									if (factDateAttr.getValue() != null && maxVisaEndDate.before(factDateAttr.getValue())) {
										maxVisaEndDate = factDateAttr.getValue();
									}
								}
							}
						}
						else {
							maxVisaEndDate = negotiationLevel.maxVisaEndDate;
						}
						prevLevelEndDate = maxVisaEndDate;
					}
				}
			}
		}
		return null;
	}

	public void setParameter(String name, String value) {
		if (PARAM_VISA_LINK_ATTR.equalsIgnoreCase(name)) {
			visaLinkAttrId = ObjectIdUtils.getObjectId(BackLinkAttribute.class, value, false);
		}
		else {
			super.setParameter(name, value);
		}
	}
	
	private Card getVisaCard() throws DataException {
		Card objectCard = loadCard(getCardId());
		if (visaLinkAttrId != null) {
			final List<Card> list = loadProjects(objectCard.getId(), visaLinkAttrId);
			if (list != null && !list.isEmpty()) {
				return loadCard(list.get(0).getId());
			}
			else {
				return null;
			}
		}
		else {
			return objectCard;
		}
	}
	
	private List<Card> loadProjects(ObjectId cardId, ObjectId linkAttrId) throws DataException {
		final ListProject listProject = new ListProject();
		listProject.setCard(cardId);
		listProject.setAttribute(linkAttrId);
	
		final List<Card> list = CardUtils.execSearchCards(listProject, getQueryFactory(), getDatabase(), getSystemUser());
		return (list == null || list.isEmpty()) ? null : list;
	}

	private ObjectId getCardId()
	{
		if (getObject() != null) {
			return getObject().getId();
		}
		Action action = getAction(); 

		if (action instanceof ChangeState) {
			return ((ChangeState) getAction()).getObjectId();
		} if (action instanceof ObjectAction) {
			ObjectAction objectAction = (ObjectAction)action;
			if (objectAction.getObjectId().getType().equals(Card.class)) {
				return objectAction.getObjectId();	
			}
		}
		return null;
	}
	
	private Card loadCard(ObjectId cardId) throws DataException{
		final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(Card.class);
		cardQuery.setAccessChecker(null);
		cardQuery.setId(cardId);
		return (Card) getDatabase().executeQuery(getSystemUser(), cardQuery);
	}

	private Object execAction(Action action) throws DataException
	{
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		return getDatabase().executeQuery(getSystemUser(), query);
	}
	
	protected void insertCardDateAttributeValue(ObjectId cardId, ObjectId attributeId, Date value, boolean updateIfNullOnly) throws DataException {
		logger.debug("About to write changes into database.");
		if (value != null) {
			int dateRecordExists = getJdbcTemplate().queryForInt(
					"select count(*) from attribute_value where card_id = ? and attribute_code = ?", 
					new Object []{cardId.getId(), attributeId.getId()}, new int[] { Types.NUMERIC, Types.VARCHAR }
			);
			
			if (dateRecordExists == 0){
				getJdbcTemplate().update(
						"insert into attribute_value (date_value, card_id, attribute_code) values(?, ?, ?);",
						new Object[] { value,  cardId.getId(), attributeId.getId()},
						new int[] { Types.DATE, Types.NUMERIC, Types.VARCHAR }
					);
			} else{
				
				String sql = "update attribute_value set date_value=? ";
				sql += "where card_id=? and attribute_code=?";
				if (updateIfNullOnly) {
					sql += " and date_value is null";
				}
				execAction(new LockObject(cardId));
				try {
					getJdbcTemplate()
						.update(
								sql,
								new Object[] { value, cardId.getId(),
										attributeId.getId() },
								new int[] { Types.DATE, Types.NUMERIC,
										Types.VARCHAR });
				} finally {
					execAction(new UnlockObject(cardId));
				}
			}
			logger.debug("Records inserted.");
		} else {
			logger.debug("Nothing to insert.");
		}
	}

	private class NegotiationLevel {
		int state = -1;
		int maxVisaPeriod = 0;
		Date startDate = new Date();
		Date maxVisaEndDate = new Date(0);
		ArrayList<Card> visaCardList = new ArrayList<Card>();
	}

	public void setJdbcTemplate(JdbcTemplate jdbc) {
		jdbcTemplate = jdbc;
	}
	
	protected JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
}