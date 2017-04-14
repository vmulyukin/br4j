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
/**
 * 
 */
package com.aplana.medo.cards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

/**
 * @author PPanichev
 *
 */
public class Notification extends ExportCardHandler {

	public static final ObjectId TEMPLATE_NOTIFICATION_REG_ID = ObjectId.predefined(
			Template.class, "med.documentRegister");
	public static final ObjectId TEMPLATE_NOTIFICATION_NOTREG_ID = ObjectId.predefined(
			Template.class, "med.registrDenied");
	public static final ObjectId TEMPLATE_NOTIFICATION_REPORT_SENT_ID = ObjectId.predefined(
			Template.class, "med.reportSent");
	public static final ObjectId TEMPLATE_NOTIFICATION_ORDER_ACCEPTED_ID = ObjectId.predefined(
			Template.class, "med.resolutionAccept");
	public static final ObjectId TEMPLATE_NOTIFICATION_FOR_APPLICANT_ID = ObjectId.predefined(
			Template.class, "med.notifyForApplicant");

	public static final ObjectId CARD_UID = ObjectId.predefined(
			StringAttribute.class, "medo.notification.UID");

	public static final ObjectId PERSON_INTERNAL_SECRETARY = ObjectId.predefined(
			CardLinkAttribute.class, "medo.foiv.director");
	public static final ObjectId PERSON_INTERNAL_MANAGER = ObjectId.predefined(
			CardLinkAttribute.class, "medo.foiv.subDirector");
	public static final ObjectId PERSON_INTERNAL_EXECUTOR = ObjectId.predefined(
			CardLinkAttribute.class, "medo.foiv.executor");

	public static final ObjectId PERSON_INTERNAL_REPORT = ObjectId.predefined(
			CardLinkAttribute.class, "medo.doc.signAnsw");

	/*public static final ObjectId PERSON_INTERNAL_ANSWER = ObjectId.predefined(
	    CardLinkAttribute.class, "medo.doc.signAnsw");*/

	public static final ObjectId DEPARTMENT_EXECUTOR = ObjectId.predefined(
			CardLinkAttribute.class, "medo.foiv.department");

	public static final ObjectId PREPARE_DISPATCH = ObjectId.predefined(
			CardState.class, "prepareDELIVERY");
	public static final ObjectId SUCCESSFULLY_DISPATCH = ObjectId.predefined(
			CardState.class, "sent");
	public static final ObjectId ERROR_DISPATCH = ObjectId.predefined(
			CardState.class, "jbr.distributionItem.notSent");

	public static final ObjectId LAST_ATTEMPT_DISPATCH = ObjectId.predefined(
			IntegerAttribute.class, "lastAttemptDELIVERY");
	public static final ObjectId LAST_TIME_DISPATCH = ObjectId.predefined(
			DateAttribute.class, "lastTimeDELIVERY");
	public static final ObjectId COUNT_ATTEMPT_DISPATCH = ObjectId.predefined(
			IntegerAttribute.class, "countAttemptDELIVERY");

	public static final ObjectId PREPARE_SENT = ObjectId.predefined(
			WorkflowMove.class, "medo.readyToSend.sent");
	public static final ObjectId PREPARE_NOTSENT = ObjectId.predefined(
			WorkflowMove.class, "medo.readyToSend.notsent");

	public static final ObjectId SENDER = ObjectId.predefined(
			CardLinkAttribute.class, "medo.sender");
	public static final ObjectId REG_NUMBER = ObjectId.predefined(
			StringAttribute.class, "medo.incoming.regnumber");
	public static final ObjectId SHORT_DESCRIPTION = ObjectId.predefined(
			TextAttribute.class, "medo.organization.shortName");

	public static final ObjectId SENDING_INFO_ATTRIBUTE_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.distributionItem.processing");
	public static final ObjectId SEND_DATE = ObjectId.predefined(
			DateAttribute.class, "medo.doc.sentdate");

	private String id = null;
	private String templateId = null;
	private CardLinkAttribute sender = null;
	private StringAttribute notificationCardUID = null;
	private StringAttribute regnumber = null;
	private TextAttribute theme = null;
	private String iterator = null;
	private CardLinkAttribute sendingInfoAttribute = null; // ���������� �� ��������
	private CardLinkAttribute DepartmentExecutor = null;
	private CardLinkAttribute PersonInternalSecretary = null;
	private CardLinkAttribute PersonInternalManager = null;
	private CardLinkAttribute PersonInternalExecutor = null;
	//private CardLinkAttribute PersonInternalAnswer = null;
	private CardLinkAttribute PersonInternalReport = null;

	private static Properties optionsSt = null; 
	/**
	 * ����� ������� (5 �������� �����. � ��������)
	 *   - �������� ��������� �������� �� ��������� (24 ���� ��� itr = 5)
	 */
	private Integer countRepeat = null; 

	private Integer lastRepeat = null;	// ����� ��������� �������
	private Date lastDate = null;	// ����� ��������� �������
	private String last_interval = null;	// �� ���������,

	/**
	 * 
	 * @param card_id
	 * @param options
	 * @throws DataException
	 */
	public Notification(ObjectId card_id, Properties options) 
			throws DataException
			// ServiceException 
	{
		String phase = "init";
		try {
			serviceBean = getServiceBean();
			card = (Card) serviceBean.getById(card_id);
			if (card == null)
				throw new CardException("jbr.medo.card.notification.notFound");

			phase = "options";
			optionsSt = options;
			if (optionsSt == null) 
				throw new CardException("jbr.medo.card.notification.notOptions");

			phase = "options";
			id = card.getId().getId().toString();
			templateId = card.getTemplate().getId().toString();

			phase = "getCardLinkAttr " + SENDER;
			sender = card.getCardLinkAttributeById(SENDER);

			phase = "getStringAttr " + CARD_UID;
			notificationCardUID = (StringAttribute)card.getAttributeById(CARD_UID);

			phase = "getStringAttr " + REG_NUMBER;
			regnumber = (StringAttribute)card.getAttributeById(REG_NUMBER);
			if (regnumber.getValue() == null) {
				regnumber.setValue("0");
			}

			phase = "getTextAttr " + SHORT_DESCRIPTION;
			theme = (TextAttribute)card.getAttributeById(SHORT_DESCRIPTION);
			if (theme.getValue() == null) {
				theme.setValue("");
			}

			phase = "getCardLinkAttr " + DEPARTMENT_EXECUTOR;
			DepartmentExecutor = card.getCardLinkAttributeById(DEPARTMENT_EXECUTOR);

			phase = "getCardLinkAttr " + PERSON_INTERNAL_SECRETARY;
			PersonInternalSecretary = card.getCardLinkAttributeById(PERSON_INTERNAL_SECRETARY);

			phase = "getCardLinkAttr " + PERSON_INTERNAL_MANAGER;
			PersonInternalManager = card.getCardLinkAttributeById(PERSON_INTERNAL_MANAGER);

			phase = "getCardLinkAttr " + PERSON_INTERNAL_EXECUTOR;
			PersonInternalExecutor = card.getCardLinkAttributeById(PERSON_INTERNAL_EXECUTOR);

			phase = "getCardLinkAttr " + PERSON_INTERNAL_REPORT;
			PersonInternalReport = card.getCardLinkAttributeById(PERSON_INTERNAL_REPORT);

			/* PersonInternalAnswer = card.getCardLinkAttributeById(); */

			phase = "getProp iteratorLAST";
			last_interval = optionsSt.getProperty("iteratorLAST", "24");

			phase = "getIntAttr " + LAST_ATTEMPT_DISPATCH;
			try {
				lastRepeat = ((IntegerAttribute) card.getAttributeById(LAST_ATTEMPT_DISPATCH)).getValue();
			} finally {
				if (lastRepeat == null) {
					lastRepeat = 0;
				}
			}

			phase = "getDateAttr " + LAST_TIME_DISPATCH;
			try {
				lastDate = ((DateAttribute) card.getAttributeById(LAST_TIME_DISPATCH)).getValue();
			} finally {
				if (lastDate == null) {
					lastDate = new GregorianCalendar().getTime();
				}
			}

			phase = "getIntAttr " + COUNT_ATTEMPT_DISPATCH;
			try {
				countRepeat = ((IntegerAttribute) card.getAttributeById(COUNT_ATTEMPT_DISPATCH)).getValue();
			} finally {
				if (countRepeat == null) {
					countRepeat = 0;
				}
			}

			phase = "getProp iterator " + lastRepeat;
			iterator = optionsSt.getProperty( "iterator" + lastRepeat,
					last_interval); // �� ��������� ��������� �������� ��

			// ���������
			phase = "getCardLinkAttr " + SENDING_INFO_ATTRIBUTE_ID;
			sendingInfoAttribute = card.getCardLinkAttributeById(SENDING_INFO_ATTRIBUTE_ID);

		} catch( Exception e) {
			logger.error("problem creating Notification at phase " + phase, e);
			throw new CardException("jbr.medo.card.notification.notFound", e);
		}

		logger.info("Create object Notification with current parameters: "
				+ getParameterValuesLog());
	}

	public Card getCard() throws CardException {
		if (card != null)
			return card;
		throw new CardException("jbr.medo.card.notification.notFound");
	}

	/* (non-Javadoc)
	 * @see com.aplana.medo.cards.ExportCardHandler#getCardId()
	 */
	@Override
	public long getCardId() throws CardException {
		if (card != null ) return (Long)card.getId().getId();
		throw new CardException("jbr.medo.card.notification.notFound");
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * @return the templateId
	 */
	public String getTemplateId() {
		return this.templateId;
	}

	/* (non-Javadoc)
	 * @see com.aplana.medo.cards.ExportCardHandler#getParameterValuesLog()
	 */
	@Override
	protected String getParameterValuesLog() {
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(String.format("id='%s', ", id));
		return logBuilder.toString();
	}

	/**
	 * @return the sender
	 */
	public CardLinkAttribute getSender() {
		return this.sender;
	}

	/**
	 * @return the notificationCardUID
	 */
	public String getNotificationCardUID() {
	    if (this.notificationCardUID == null) return null;
	    return this.notificationCardUID.getValue();
	}

	/**
	 * @return the regnumber
	 */
	public String getRegNumber() {
	    if (this.regnumber == null) return null;
	    return this.regnumber.getValue();
	}

	/**
	 * @return the theme
	 */
	public String getTheme() {
	    if (this.theme == null) return null;
	    return this.theme.getValue();
	}

	/**
	 * @return ������ �������� ���������
	 */
	public String getItrPeriod() {
		return this.iterator;
	}

	/**
	 * @return ���� ���������� ��������������
	 */
	public Date getLastDate() {
		return this.lastDate;
	}

	/**
	 * @return ����� ��������
	 */
	public Integer getLastRepeat() {
		return this.lastRepeat;
	}

	/**
	 * @return ���������� ��������
	 */
	public Integer getCountRepeat() {
		return this.countRepeat;
	}

	/**
	 * @return ���������� �� ��������
	 */
	public CardLinkAttribute getSendingInfoAttribute() {
		return this.sendingInfoAttribute;
	}

	/**
	 * @return the departmentExecutor type:CardLinkAttribute
	 */
	public CardLinkAttribute getDepartmentExecutor() {
		return this.DepartmentExecutor;
	}

	/**
	 * @return the personInternalSecretary type:CardLinkAttribute
	 * 
	 */
	public CardLinkAttribute getPersonInternalSecretary() {
		return this.PersonInternalSecretary;
	}

	/**
	 * @return the personInternalManager type:CardLinkAttribute
	 */
	public CardLinkAttribute getPersonInternalManager() {
		return this.PersonInternalManager;
	}

	/**
	 * @return the personInternalExecutor type:CardLinkAttribute
	 */
	public CardLinkAttribute getPersonInternalExecutor() {
		return this.PersonInternalExecutor;
	}

	/**
	 * @return the personInternalReport
	 */
	public CardLinkAttribute getPersonInternalReport() {
		return this.PersonInternalReport;
	}

	public ObjectId getDepartmentExecutorId() {
		return calcDepartmentExecutor();
	}

	public ObjectId getPersonInternalSecretaryId() {
		return calcPersonInternalSecretary();
	}

	public ObjectId getPersonInternalManagerId() {
		return calcPersonInternalManager();
	}

	public ObjectId getPersonInternalExecutorId() {
		return calcPersonInternalExecutor();
	}

	public ObjectId getPersonInternalReportId() {
		return calcPersonInternalReport();
	}

	public ObjectId getSenderId() {
		return calcSender();
	}

	// TODO: (2011/02/16, RusA) �� ������� �� - ����� ������� "����" ��������� ������ �������� ?
	// � ���� ��� ����� "NULL Pointer" ��-�� ��������� �������� - ��� ���������?
	public void setLastRepeat(Integer lastRepeat) throws DataException, ServiceException {
		this.lastRepeat = lastRepeat;
		((IntegerAttribute) card.getAttributeById(LAST_ATTEMPT_DISPATCH)).setValue(this.lastRepeat);
		saveCard();
	}

	public void setLastDate(Date lastDate) 
		throws DataException, ServiceException 
	{
		this.lastDate = lastDate;
		((DateAttribute) card.getAttributeById(LAST_TIME_DISPATCH)).setValue(this.lastDate);
		saveCard();
	}

	public void setSendDate(Date sendDate) 
		throws DataException, ServiceException 
	{
		((DateAttribute) card.getAttributeById(SEND_DATE)).setValue(sendDate);
		saveCard();
	}

	public void addLinkedId(Long linkId) throws DataException, ServiceException 
	{
		getSendingInfoAttribute().addLinkedId(linkId);
		saveCard();
	}

	/**
	 * @set WorkflowMove in PREPARE_SENT
	 **/
	public void byPrepareSent() throws DataException, ServiceException {
		setMoveCard(PREPARE_SENT);
	}

	/**
	 * @set WorkflowMove in PREPARE_NOTSENT
	 **/
	public void byPrepareNotSent() throws DataException, ServiceException {
		setMoveCard(PREPARE_NOTSENT);
	}

	public static Collection<Card> findCards() throws CardException {
		Collection<Card> cards = search();
		if (cards == null) {
			cards = new ArrayList<Card>();
		}
		loggerSt.info(String.format("There was found %d cards", cards.size()));
		return cards;
	}

	@SuppressWarnings("unchecked")
	private static Collection<Card> search() throws CardException {

		serviceBeanStatic = getServiceBeanStatic();
		final Search search_ = new Search();
		final List<String> states = new ArrayList<String>(1);
		states.add(PREPARE_DISPATCH.getId().toString());
		search_.setStates(states);
		final List<DataObject> templates = new ArrayList<DataObject>(5);
		templates.add(DataObject.createFromId(TEMPLATE_NOTIFICATION_REG_ID));
		templates.add(DataObject.createFromId(TEMPLATE_NOTIFICATION_NOTREG_ID));
		templates.add(DataObject.createFromId(TEMPLATE_NOTIFICATION_REPORT_SENT_ID));
		templates.add(DataObject.createFromId(TEMPLATE_NOTIFICATION_ORDER_ACCEPTED_ID));
		templates.add(DataObject.createFromId(TEMPLATE_NOTIFICATION_FOR_APPLICANT_ID));
		search_.setTemplates(templates);

		search_.setByAttributes(true);

		try {
			@SuppressWarnings("unchecked")
			final SearchResult cardsSR = (SearchResult)serviceBeanStatic.doAction(search_);
			final Collection<Card> cards = cardsSR.getCards();
			return cards;
		} catch (DataException ex) {
			throw new CardException("jbr.medo.card.notification.searchFailed", ex);
		} catch (ServiceException ex) {
			throw new CardException("jbr.medo.card.notification.searchFailed", ex);
		}
	}

	private void setMoveCard(ObjectId workflowMove) throws DataException, ServiceException {
		final LockObject lock = new LockObject(card.getId());
		serviceBean.doAction(lock);
		try {
			final ChangeState move = new ChangeState(); // ��������
			// ��������-��������
			// ���
			// ��������
			serviceBean.saveObject(card);
			move.setCard(card); // ���������� ��������,
			// ������� ����
			// ����������.
			move.setWorkflowMove((WorkflowMove) DataObject.createFromId(workflowMove));
			serviceBean.doAction(move);
		} finally {
			final UnlockObject unlock = new UnlockObject(card.getId());
			serviceBean.doAction(unlock);
		}
	}

	private ObjectId calcDepartmentExecutor() {
		final ObjectId[] DepartmentExecutors = this.DepartmentExecutor.getIdsArray();
		if (DepartmentExecutors == null) {
			logger.warn("jbr.medo.Notification.DepartmentExecutors.isNull");
			return null;
		}
		return  DepartmentExecutors[0];
	}

	private ObjectId calcPersonInternalSecretary() {
		final ObjectId[] PersonInternalSecretarys = this.PersonInternalSecretary.getIdsArray();
		if (PersonInternalSecretarys == null) {
			logger.warn("jbr.medo.Notification.PersonInternalSecretarys.isNull");
			return null;
		}
		return  PersonInternalSecretarys[0];
	}

	private ObjectId calcPersonInternalManager() {
		final ObjectId[] PersonInternalManagers = this.PersonInternalManager.getIdsArray();
		if (PersonInternalManagers == null) {
			logger.warn("jbr.medo.Notification.PersonInternalManagers.isNull");
			return null;
		}
		return  PersonInternalManagers[0];
	}

	private ObjectId calcPersonInternalExecutor() {
		final ObjectId[] PersonInternalExecutors = this.PersonInternalExecutor.getIdsArray();
		if (PersonInternalExecutors == null) {
			logger.warn("jbr.medo.Notification.PersonInternalExecutors.isNull");
			return null;
		}
		return  PersonInternalExecutors[0];
	}

	private ObjectId calcPersonInternalReport() {
		ObjectId[] PersonInternalReport_ = this.PersonInternalReport.getIdsArray();
		if (PersonInternalReport_ == null) {
			logger.warn("jbr.medo.Notification.PersonInternalReport.isNull");
			return null;
		}
		return  PersonInternalReport_[0];
	}

	private ObjectId calcSender() {
		final ObjectId[] Sender = this.sender.getIdsArray();
		if (Sender == null) {
			logger.warn("jbr.medo.Notification.sender.isNull");
			return null;
		}
		return Sender[0];
	}

}
