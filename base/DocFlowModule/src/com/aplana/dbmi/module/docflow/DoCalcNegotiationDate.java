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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.module.docflow.calendar.CalendarAPI;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.service.impl.query.WriteQuery;

public class DoCalcNegotiationDate extends ActionQueryBase implements
		Parametrized, WriteQuery {
	private static final long serialVersionUID = 1L;
	// ��������: ���� ������ ������������
	static final ObjectId startNegotiationDateId = ObjectId.predefined(
			DateAttribute.class, "jbr.start_negotiation_date");
	// ��������: ����
	static final ObjectId visaSetId = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.visa.set");
	// ��������: �������� ���� ������������
	static final ObjectId planNegotiationDateId = ObjectId.predefined(
			DateAttribute.class, "jbr.plan_negotiation_date");

	// ����: ������� ������������
	static final ObjectId VISA_ORDER = ObjectId.predefined(
			IntegerAttribute.class, "jbr.visa.order");
	// ����: ���� �����������
	static final ObjectId negotiationPeriodId = ObjectId.predefined(
			IntegerAttribute.class, "jbr.visa.negotiation_period");
	// ����: ������������ ��
	static final ObjectId visaToDateId = ObjectId.predefined(
			DateAttribute.class, "jbr.visa.to_date");
	// ����: ���� ������������ ������������
	static final ObjectId visaActualConsentDateId = ObjectId.predefined(
			DateAttribute.class, "jbr.visa.date_actual_consent");
	// ����: ���� �����������
	static final ObjectId visaIncomeDateId = ObjectId.predefined(
			DateAttribute.class, "jbr.visa.income_date");

	// ��������� �������� �������� ������������
	static final String WAIT_FOR_NEGOTIATION_STATES_PARAM = "waitForNegotiationStates";
	// ��������� �������� ��������� ������������
	static final String PASS_NEGOTIATION_STATES_PARAM = "passNegotiationStates";

	private String m_waitForNegotiationStates = null;
	private String m_passNegotiationStates = null;

	void swap(Card[] list, int i, int j) {
		Card t = list[i];
		list[i] = list[j];
		list[j] = t;
	}

	public Object processQuery() throws DataException {
		CalcNegotiationDate action = (CalcNegotiationDate) super.getAction();

		// �������� ��������
		final Card card = LoadCard(action.getObjectId());

		// ���������
		execAction(new LockObject(card.getId()));

		try {
		// �������� ���� ������ ������������ ���������
		final DateAttribute startNegotiationDateAttr = (DateAttribute) card
				.getAttributeById(startNegotiationDateId);
		Date startNegotiationDate = new Date();
		Date startNegotiationDateVisa = new Date();
		if (!startNegotiationDateAttr.isEmpty()) {
			startNegotiationDate = startNegotiationDateAttr.getValue();
			startNegotiationDateVisa = startNegotiationDateAttr.getValue();
		}

		CalendarAPI workCalendar = CalendarAPI.getInstance();

		class VisaInfo {
			@SuppressWarnings("unused")
			int period;
			Date toDate;
			// Date actualConsentDate;
		}

		// ���� ���� ��� ������������ ������ �������� � ��� ���� � �������� ����
		// ��� �� ����� �������������
		// ��������� ��� ���� ����� ������ ���� ��� ���������� �������� ����
		// ������������ ���������
		// boolean isStartNegotiation = true;

		// �������� ���� ������������ ���������
		Date result = startNegotiationDate;

		// �������� ��� ���� ���������
		Map<Integer, ArrayList<VisaInfo>> visaSet = new HashMap<Integer, ArrayList<VisaInfo>>();
		final CardLinkAttribute visaSetAttr = (CardLinkAttribute) card
				.getAttributeById(visaSetId);
		IntegerAttribute visaOrderAttrPre = null;
		/*
		 * ��� ���������� ������ ����� ����������� ����� ��� null, ���� ���� -
		 * ������ �� �����������. ����������, ��� ��� visaOrderAttrPre ������
		 * ����� ����� ���������� ����.
		 */
		IntegerAttribute previousOrder = null;
		// DateAttribute visaDateConsentPre = new DateAttribute();
		DateAttribute visaToDatePre = new DateAttribute();
		visaToDatePre.setValue(startNegotiationDate);
		if (visaSetAttr.getLinkedCount() > 0) {

			// ��������� ���� � �������
			ArrayList<Card> list = new ArrayList<Card>();
			for (Iterator<ObjectId> iterator = visaSetAttr.getIdsLinked().iterator(); iterator.hasNext();) {
				Card visa = LoadCard(iterator.next());
				/* ����������� � ���������� action'� ������� ������������. */
				if (checkPassNegotiation(visa.getState())
						|| checkWaitForNegotiation(visa.getState())) {
					list.add(visa);
				}
			}

			// ��������� ���� �� �����������
			Collections.sort(list, new Comparator<Card>() {
				public int compare(Card a, Card b) {
					Integer aOrder = ((IntegerAttribute) a
							.getAttributeById(VISA_ORDER)).getValue();
					Integer bOrder = ((IntegerAttribute) b
							.getAttributeById(VISA_ORDER)).getValue();
					return (aOrder.compareTo(bOrder));
				}
			});

			// ���� �� ����� ����������� � ������� �����������
			for (Iterator<Card> i = list.iterator(); i.hasNext();) {
				Card visa = i.next();
				final IntegerAttribute negotiationPeriodAttr = (IntegerAttribute) visa
						.getAttributeById(negotiationPeriodId);
				final IntegerAttribute visaOrderAttr = (IntegerAttribute) visa
						.getAttributeById(VISA_ORDER);
				final DateAttribute visaToDate = (DateAttribute) visa
						.getAttributeById(visaToDateId);
				final DateAttribute visaActualConsentDate = (DateAttribute) visa
						.getAttributeById(visaActualConsentDateId);
				final DateAttribute visaIncomeDate = (DateAttribute) visa
						.getAttributeById(visaIncomeDateId);
				// ���� �� ������ ������� � ��� �� �������������� �������
				if (visaOrderAttrPre != null
						&& visaOrderAttr.getValue() != visaOrderAttrPre
								.getValue()) {
					previousOrder = visaOrderAttrPre;
					/*
					 * ���� ���������� ����������� ������ ����������� ���
					 * ���������� ���� ���� ��� ����� ������.
					 */
					visaToDatePre.setValue(Collections.max(visaSet
							.get(previousOrder.getValue()),
							new Comparator<VisaInfo>() {
								public int compare(VisaInfo a, VisaInfo b) {
									return (a.toDate.compareTo(b.toDate));
								}
							}).toDate);
				}

				if (visaToDatePre.getValue() != null)
					startNegotiationDateVisa = visaToDatePre.getValue();
				else
					startNegotiationDateVisa = new Date();

				// ��������� ����� ��� �������� ���������� � ��������� ����
				VisaInfo visaInfo = new VisaInfo();

				// �������� �� �� ��� ������� ���� ������� ������������
				if (checkWaitForNegotiation(visa.getState())) {
					// ���� ������� ������������
					if (!negotiationPeriodAttr.isEmpty()
							&& !negotiationPeriodAttr.isNull()) {

						// boruroev: ��������� ����� ���� ������, ����� ����������� ������ 
						// (����� ������������) ��� ���� ����������� ���� ���������
						Date startDate = startNegotiationDateVisa;
						if (!visaIncomeDate.isEmpty() && visaIncomeDate.getValue() != null &&
								visaIncomeDate.getValue().after(startDate)) {
							startDate = visaIncomeDate.getValue();
						}
							
						// ���� ���������� ���� ������������, �� �������������
						// �������� ����
						// ������ ���� ������ ������������ ���� ����
						// ������������
						visaToDate.setValue(workCalendar.addToDate(
								negotiationPeriodAttr.getValue(),
								startDate));
						visaInfo.toDate = visaToDate.getValue();
					}
				} else if (checkPassNegotiation(visa.getState())) {
					// ���� ���� ��������� ������������
					// ������������� ���� ��� ��� �� ������ ������������
					// isStartNegotiation = false;

					if (!visaActualConsentDate.isEmpty()) {
						// ���� ����������� ���� ������������ ������������
						// �� ������������� ���� ������������ ������ �������
						// ����� ����� ������ � ����� ��������� ������������
						negotiationPeriodAttr.setValue(workCalendar.diff(
								visaActualConsentDate.getValue(),
								startNegotiationDateVisa));
						visaInfo.toDate = visaActualConsentDate.getValue();
					}
				}
				visaInfo.period = negotiationPeriodAttr.getValue();

				// visaInfo.actualConsentDate =
				// visaActualConsentDate.getValue();

				// ���������� � ������ ��� �������, ��� �������� ��� ��������
				// ������
				if (!visaSet.containsKey(visaOrderAttr.getValue())) {
					visaSet.put(visaOrderAttr.getValue(),
							new ArrayList<VisaInfo>());
				}
				visaSet.get(visaOrderAttr.getValue()).add(visaInfo);

				// �������� ���� ��� ������������
				final DateAttribute planNegotiationDateVisaAttr = (DateAttribute) visa
						.getAttributeById(visaToDateId);
				insertCardDateAttributeValue(visa.getId(),
						planNegotiationDateVisaAttr.getId(), visaToDate
								.getValue(), false);

				// ��������� ������� ������� ���� ��� ��������� ��������
				visaOrderAttrPre = (IntegerAttribute) visa
						.getAttributeById(VISA_ORDER);

				if (visaInfo.toDate != null && result.before(visaInfo.toDate)) {
					result = visaInfo.toDate;
				}
			}
		}

		// if (isStartNegotiation){

		// ���������� ����� ��������� ����� ����������� ������������ ������
		// ������������ �� ������ ������
		/*
		int period = 0;
		if (!visaSet.isEmpty()) {
			Iterator<ArrayList<VisaInfo>> visaSetValues = visaSet.values()
					.iterator();
			while (visaSetValues.hasNext()) {
				ArrayList<VisaInfo> level = visaSetValues.next();
				int maxPeriod = 0;
				for (VisaInfo visa : level) {
					if (visa.period > maxPeriod) {
						maxPeriod = visa.period;
					}
				}
				period += maxPeriod;
			}
		}

		// ��������� �������� ���� ������������ ���������
		result = workCalendar.addToDate(period, startNegotiationDate);
		*/
		
		// ��������� �������� ���� ��������� ������ � ��� ������, ���� ��� ������� � ������
		if (action.isUpdatePlanDate()){
			final DateAttribute planNegotiationDateAttr = (DateAttribute) card
					.getAttributeById(planNegotiationDateId);
			insertCardDateAttributeValue(card.getId(), planNegotiationDateAttr
					.getId(), result, false);
		}
		return result;
		//end of try
		} finally {
			execAction(new UnlockObject(card.getId()));
		}
	}

	private boolean checkPassNegotiation(ObjectId state) {
		return checkState(m_passNegotiationStates, state);
	}

	private boolean checkWaitForNegotiation(ObjectId state) {
		return checkState(m_waitForNegotiationStates, state);
	}

	private boolean checkState(String strStates, ObjectId checkingState) {
		Pattern p = Pattern.compile("([0-9A-z_\\-\\.]*)");
		Matcher m = p.matcher(strStates);
		while (m.find()) {
			ObjectId state = ObjectId.predefined(CardState.class, m.group());
			if (checkingState.equals(state)) {
				return true;
			}
		}
		return false;
	}

	private Card LoadCard(ObjectId cardId) throws DataException {
		final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(
				Card.class);
		cardQuery.setAccessChecker(null);
		cardQuery.setId(cardId);
		return (Card) getDatabase().executeQuery(getUser(), cardQuery);
	}

	protected void insertCardDateAttributeValue(ObjectId cardId,
			ObjectId attributeId, Date value, boolean updateIfNullOnly) {
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
				getJdbcTemplate()
						.update(
								sql,
								new Object[] { value, cardId.getId(),
										attributeId.getId() },
								new int[] { Types.DATE, Types.NUMERIC,
										Types.VARCHAR });
			}
			logger.debug("Records inserted.");
		} else {
			logger.debug("Nothing to insert.");
		}
	}

	/*
	 * private void SaveCard(Card card) throws DataException{ final
	 * SaveQueryBase query = getQueryFactory().getSaveQuery(card);
	 * query.setObject(card); getDatabase().executeQuery(getUser(), query); }
	 */

	public void setParameter(String name, String value) {
		if (WAIT_FOR_NEGOTIATION_STATES_PARAM.equals(name)) {
			m_waitForNegotiationStates = value;
		} else if (PASS_NEGOTIATION_STATES_PARAM.equals(name)) {
			m_passNegotiationStates = value;
		}
	}

	public Object execAction(Action action) throws DataException {
		return execAction(action, getUser());
	}

	public Object execAction(Action action, UserData user) throws DataException {
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		return getDatabase().executeQuery(user, query);
	}
}
