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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.QueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;

@SuppressWarnings("all")
public class VisaProcessor extends DataServiceClient implements DocumentProcessor
{
	static final ObjectId enclosedCompleteDateId = ObjectId.predefined(DateAttribute.class, "jbr.visa.enclosedCompleteDate");

	private VisaConfiguration config;
	private ObjectId docId;
	private Card document;
	private List rootVisaCardList;
	private Integer m_currentStage;
	private ObjectId VISA_MARK_PROCESS_ID = ObjectId.predefined(IntegerAttribute.class, "jbr.visa.mark.process");

	private final static Integer DEFAULT_STAGE_NUMBER = Integer.valueOf(0);
	private ObjectId orderAttributeId;

	public void setConfig(VisaConfiguration config) {
		this.config = config;
	}

	public void setDocumentId(ObjectId docId) {
		this.docId = docId;
	}

	public void process() throws DataException {
		if (config.isPropertySet(VisaConfiguration.ATTR_ORDER)) {
			orderAttributeId = config.getObjectId(IntegerAttribute.class, VisaConfiguration.ATTR_ORDER);
		}
		//�������������� ����� �����
		initStageNumber();
		// ���������� ���
		rootVisaCardList = null;

		boolean hasRootRejectedVisas = hasRootRejectedVisas();

		// 1. If there are any rejections and document should be sent back at once, do it
		/* ���������� �������, � ������� ���� ��������� ������� 2,5. (�� ����������� ������� ��������).
		 * ���� �� ���������, �������������� ���������� �� �� ��������� ��� ������ �� ��������������� ����, 
		 * �� ��� �� ������� � ���������� "���������� ��� ������ ������"
		 * if (isReturnImmediately() && hasRootRejectedVisas) {
			returnDocument();
			return;
		}*/

		checkEnclosedSet();

		//���� ����� ���� ��� ���������� �������� ��� ������ �� ������������� ����
		//� ���� ����� �� ���� ������������� ����
		//�� ��������� proceedDocument
		if (isProcessDocumentAtFirstAgreeVisa() && hasAgreeVisas()){
			proceedDocument();
		}

		// 2. If there are any waiting for agreement cards, stop processing
		//�� ��� ��� ��� ������� ������ � ������� �������� ������������
		if (hasRootWaitingVisas() && !hasCurrentStageAssignedVisas())
			return;

		// 2,5. Return document if it should be returned at end of stage and there are any rejections
		// ���� ���������� "���������� ��� ������ ������" � ���� ��������������� ���� � ��� ��������� ��� 
		// �� ������� �������� (�.�. ����� �����), �� ���������� ��������.
		if (hasRootRejectedVisas && isReturnAtEndOfStage() && !hasRootWaitingVisas()) {
			returnDocument();
			return;
		}

		// 3. Send the next group of visas to their destinations.
		//    If there are none of them, proceed the document to the next stage or return it
		Collection nextVisas = null;
		if (hasCurrentStageAssignedVisas()){
			nextVisas = getCurrentStageAssignedVisas();
		}else{
			nextVisas = getRootNextStageVisas();
		}

		if (!nextVisas.isEmpty()) {
			sendNextVisas(nextVisas);
		}
		else{
			if (hasRootRejectedVisas || (hasMistakeSendedVisa() && !hasAgreeStatesVisas()))
				returnDocument();
			else
				proceedDocument();
		}
	}

	protected void sendNextVisas(Collection nextVisas) throws DataException {
		for (Iterator itr = nextVisas.iterator(); itr.hasNext(); ) {
			sendForVisa((Card) itr.next());
		}
	}

	/**
	 * ����� ���������� true ���� ���� ���� �� ���� ������������� ����
	 * � ������� ��������� � ������������ � �������� option.process.document.at.first.agree.visa
	 * @return
	 * @throws Exception
	 */
	private boolean hasAgreeVisas() throws DataException {
		boolean result = false;
		//������������� �������
		final Set agreeStateSet = config.getObjectIdSet(
				CardState.class,
				VisaConfiguration.OPTION_PROCESS_DOCUMENT_AT_FIRST_AGREE_VISA, true);
		//���� �� �����
		for (Iterator itr = getRootVisaCardList().iterator(); itr.hasNext(); ) {
			Card visa = (Card) itr.next();
			//�������� �� �� ��� ���� ������������
			if (agreeStateSet != null && agreeStateSet.contains(visa.getState())){
				result = true;
				break;
			}
		}
		return result;
	}

	private boolean isProcessDocumentAtFirstAgreeVisa() {
		boolean result = false;
		String strResult = config.getValue(VisaConfiguration.OPTION_PROCESS_DOCUMENT_AT_FIRST_AGREE_VISA);
		if (strResult != null && strResult.length()>0){
			result = true;
		}
		return result;
	}

	/**
	 * ��������� ������ ��� � ������� �������� ������������
	 * � �� ������� ������
	 * @return
	 * @throws DataException
	 */
	protected Collection getCurrentStageAssignedVisas() throws DataException {
		ArrayList result = new ArrayList();
		//���� �� �����
		for (Iterator itr = getRootVisaCardList().iterator(); itr.hasNext(); ) {
			Card visa = (Card) itr.next();
			//�������� �� �� ��� ���� �� �������� ������������
			if (visa.getState().equals(getObjectIdByTemplate(CardState.class, VisaConfiguration.STATE_ASSIGNED,
					visa.getTemplate()))) {
				//�������� ����� ����� � ���������� � ������� �������
				int stage = getOrder(visa);
				//��������� ������� ������ � ������ ����
				if (m_currentStage != null && m_currentStage.intValue() == stage){
					result.add(visa);
				}
			}
		}
		return result;
	}

	protected int getOrder(Card card) throws DataException {
		if (!config.isPropertySet(VisaConfiguration.ATTR_ORDER)) {
			return DEFAULT_STAGE_NUMBER;
		}

		IntegerAttribute attr = (IntegerAttribute) card.getAttributeById(orderAttributeId);
		if (attr == null){
			throw new DataException("docflow.visa.noattr",
					new Object[] { card.getId().getId().toString(), "@attribute." + orderAttributeId.getId() });
		}
		return attr.getValue();
	}

	/**
	 * ������� ���������� true ���� ���� ���� �� ���� ���� � ������� VisaConfiguration.STATE_ASSIGNED
	 * �� ������� ������ ���������
	 * @return
	 * @throws DataException
	 */
	private boolean hasCurrentStageAssignedVisas() throws DataException {
		boolean result = false;
		//���� �� �����
		for (Iterator itr = getRootVisaCardList().iterator(); itr.hasNext(); ) {
			Card visa = (Card) itr.next();
			//�������� �� �� ��� ���� �� �������� ������������
			if (visa.getState().equals(getObjectIdByTemplate(CardState.class, VisaConfiguration.STATE_ASSIGNED,
					visa.getTemplate()))) {
				//�������� ����� ����� � ���������� � ������� �������
				int stage = getOrder(visa);
				//��������� ������� ������ � ������ ����
				if (m_currentStage != null && m_currentStage.intValue() == stage){
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * ������������� ������ �������� ����� ������������
	 * ���������� ��� �������� ���� � ��������, ��������� � ���������� �
	 * ������ ��������� VisaConfiguration.STATES_WAITING, � �
	 * ������ �� ��� �������� ����� �����. ���������� �������� ���������� � ���� ������ m_currentStage
	 * ������������ ��� ���� � ������� �� ������������ ������ ����� ���������� ����� �����
	 * @throws DataException
	 */
	private void initStageNumber() throws DataException {
		final Set waitingStateSet = config.getObjectIdSet( CardState.class, VisaConfiguration.STATES_WAITING);
		final Set draftStateSet = config.getObjectIdSet( CardState.class, VisaConfiguration.STATE_DRAFT);
		final Set assignedStageSet = config.getObjectIdSet( CardState.class, VisaConfiguration.STATE_ASSIGNED);
		//���� �� �����
		logger.debug("initStageNumber()");
		for (Iterator itr = getRootVisaCardList().iterator(); itr.hasNext(); ) {
			Card visa = (Card) itr.next();
			//�������� �� �� ��� ���� �� ������������
			if (waitingStateSet.contains(visa.getState())
					|| draftStateSet.contains(visa.getState())
					|| assignedStageSet.contains(visa.getState())) {
				//�������� ����� ����� � ���������� � m_currentStage
				m_currentStage = Integer.valueOf(getOrder(visa));
				break;
			}
		}
	}

	final protected boolean doChangeState( final ObjectId cardId,
			final ObjectId templateId, final String configWfmIdName,
			final String infoTag
			) throws DataException
	{
		if(logger.isInfoEnabled())
			logger.info( "[" + getName() + ":" + cardId.getId() + "] " + infoTag);

		ObjectId wfmId;
		try{
			wfmId = getObjectIdByTemplate( WorkflowMove.class, configWfmIdName, templateId);
		} catch(DataException de){
			wfmId = null;
		}
		if (wfmId == null) {
			if(logger.isWarnEnabled())
				logger.warn("[" + getName() + ":" + cardId.getId() + "] wfmId for config parameter '"
					+ configWfmIdName+ "' is null -> state was not changed");
			return false;
		}

		ObjectQueryBase query = getQueryFactory().getFetchQuery(WorkflowMove.class);
		query.setAccessChecker(null);
		query.setId(wfmId);
		final WorkflowMove wfm = (WorkflowMove)getDatabase().executeQuery(getSystemUser(), query);
		if (wfm == null) {
			if(logger.isWarnEnabled())
				logger.warn("[" + getName() + ":" + cardId.getId() + "] wfm is null -> state was not changed");
			return false;
		}

		//�������� ������ ���������, ����� �� ���������� �������� � ��� �� ������
		ObjectQueryBase docQuery = getQueryFactory().getFetchQuery(Card.class);
		docQuery.setAccessChecker(null);
		docQuery.setId(cardId);
		Card document = (Card)getDatabase().executeQuery(getSystemUser(), docQuery);
		if (document.getState().equals(wfm.getToState())){
			if(logger.isInfoEnabled())
				logger.info( "[" + getName() + ":" + cardId.getId() + "] " + "Not change state, document already in state " + document.getState());
			return false;
		}

		doAction(new LockObject(cardId));
		try {
			final ChangeState move = new ChangeState();
			move.setWorkflowMove( wfm );
			move.setCard((Card) DataObject.createFromId(cardId));
			doAction(move);
			if(logger.isDebugEnabled())
				logger.debug( "[" + getName() + ":" + cardId.getId()
					+ "] Document proceeded to the next stage by WorkFlowMove "
					+ wfm.getId() + " '"+ wfm.getMoveName() + "'"
					+ ", fromStatus=" + wfm.getFromState()
					+ ", toStatus=" + wfm.getToState()
				);
		} finally {
			doAction(new UnlockObject(cardId));
		}
		return true;
	}

	protected void proceedDocument() throws DataException {
		if (doChangeState( docId, getDocument().getTemplate(), VisaConfiguration.MOVE_PROCEED, "Sending document to the next stage" ))
			logger.info("Document " + docId.getId() + " sent");
	}

	protected void returnDocument() throws DataException {
		if (doChangeState( docId, getDocument().getTemplate(), VisaConfiguration.MOVE_RETURN, "Returning document to the previous stage" ))
			logger.info("Document " + docId.getId() + " returned to author");
	}

	protected boolean isReturnImmediately() throws DataException {
		try {
			config.getObjectId(ReferenceValue.class, VisaConfiguration.VALUES_IMMED);
		} catch (DataException e) {
			return false;		// VALUES_IMMED not defined => can't return immediately
		}
		if (VisaConfiguration.OPTION_RETURN_ATTR.equalsIgnoreCase(config.getValue(VisaConfiguration.OPTION_RETURN))) {
			ObjectId attrId = config.getObjectId(ListAttribute.class, VisaConfiguration.ATTR_RETURN);
			ListAttribute attr = (ListAttribute) getDocument().getAttributeById(attrId);
			if (attr == null) {
				//throw new DataException("docflow.document.noattr",
				//		new Object[] { getDocument().getId().getId().toString(), "@attribute." + attrId.getId() });
				if(logger.isWarnEnabled())
					logger.warn("Attribute " + attrId.getId() + " not found in card; assume not returning immediately");
			}
			return attr != null && attr.getValue() != null &&	// NOT return immediately if attribute is missing
				config.isListedId(attr.getValue().getId(), VisaConfiguration.VALUES_IMMED);
				//config.getObjectId(ReferenceValue.class, VisaConfiguration.VALUE_IMMED).equals(attr.getValue().getId());
		}
		return !VisaConfiguration.OPTION_RETURN_END.equalsIgnoreCase(config.getValue(VisaConfiguration.OPTION_RETURN));
	}

	protected boolean isReturnAtEndOfStage() throws DataException {
		try {
			config.getObjectId(ReferenceValue.class, VisaConfiguration.VALUES_STAGE);
		} catch (DataException e) {
			return false;		// VALUES_IMMED not defined => can't return at end of stage
		}
		if (VisaConfiguration.OPTION_RETURN_ATTR.equalsIgnoreCase(config.getValue(VisaConfiguration.OPTION_RETURN))) {
			ObjectId attrId = config.getObjectId(ListAttribute.class, VisaConfiguration.ATTR_RETURN);
			ListAttribute attr = (ListAttribute) getDocument().getAttributeById(attrId);
			if (attr == null && logger.isWarnEnabled()) {
				logger.warn("Attribute " + attrId.getId() + " not found in card; assume not returning at end of stage");
			}
			return attr != null && attr.getValue() != null &&	// NOT return at end of stage if attribute is missing
				config.isListedId(attr.getValue().getId(), VisaConfiguration.VALUES_STAGE);
		}
		return !VisaConfiguration.OPTION_RETURN_END.equalsIgnoreCase(config.getValue(VisaConfiguration.OPTION_RETURN));
	}

	protected boolean hasRootRejectedVisas() throws DataException {
		return hasRejectedVisas(getRootVisaCardList().iterator(), false);
	}

	protected boolean hasAnyRejectedVisas() throws DataException {
		return hasRejectedVisas(getRootVisaCardList().iterator(), true);
	}

	protected boolean hasRejectedVisas(Iterator visaCardIterator, boolean isRecursive) throws DataException {
		for (Iterator itr = visaCardIterator; itr.hasNext(); ) {
			final Card visa = (Card) itr.next();
			//������������� �������� ��� �����������, ������ ���� � ��� �������� VISA_MARK_PROCESS �� 0
			IntegerAttribute markOfProcessAttribute = (IntegerAttribute)visa.getAttributeById(VISA_MARK_PROCESS_ID);
			String value = null;
			if (markOfProcessAttribute!=null)
				value = String.valueOf(markOfProcessAttribute.getValue());
			else
				value = "null";
			if(logger.isDebugEnabled())
				logger.debug("VISA_MARK_PROCESS_ID ("+markOfProcessAttribute+") = "+value);
			if(!(markOfProcessAttribute!=null && markOfProcessAttribute.getValue()!=0)){
				continue;
			}
			//if (visas.getState().equals(config.getObjectId(CardState.class, VisaConfiguration.STATE_REJECTED))) {
			if (config.isListedId(visa.getState(), VisaConfiguration.STATES_REJECTED)) {
				try {
					PersonAttribute attrTo = (PersonAttribute) visa.getAttributeById(
							getObjectIdByTemplate(PersonAttribute.class, VisaConfiguration.ATTR_PERSON, visa.getTemplate()));
					if(logger.isInfoEnabled())
						logger.info("Document " + docId.getId() + " has a rejection visas from " +
							attrTo.getPersonName());
				} catch (Exception e) {
					// just logging failed - ignore
				}
				return true;
			}
			else if (isRecursive && hasRejectedVisas(getChildVisaCardList(visa).iterator(), true)) {
				return true;
			}
		}
		return false;
	}

	protected boolean hasRootWaitingVisas() throws DataException {
		for (Iterator itr = getRootVisaCardList().iterator(); itr.hasNext(); ) {
			final Card avisa = (Card) itr.next();
			/*
			 * >>> (08/07/2010, RuSA) �����, ��� �������, ��� ��������� ��������:
			 * "� ���������" � "� ���������", ��� ��� ������ ����� ��������� ��������...
			 * OLD:
			 * if (avisa.getState().equals(getObjectIdByTemplate(CardState.class,
			 * 			VisaConfiguration.STATE_WAITING, avisa.getTemplate())))
			 */
			if (isListedIdForTemplate(avisa.getState(), VisaConfiguration.STATES_WAITING, avisa.getTemplate())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param cardId: id ����������� ��������.
	 * @param wfmId: id ��������.
	 * @return true, ���� �������� ���������� ��� ��������� � �������� ��������� WorkFlowMove.
	 * @throws DataException
	 */
	final protected boolean isCardAlreadyAtWFMState( final ObjectId cardId,
			final ObjectId wfmId) throws DataException
	{
		final Boolean cardAlreadyAtState = (Boolean)
			this.getDatabase().executeQuery(getSystemUser(), new QueryBase(){
				@Override
				public Object processQuery() throws DataException {
					final int result = getJdbcTemplate().queryForInt(
						"select count(*) \n" +
						"from card c \n" +
						"		join template t on t.template_id = c.template_id \n" +
						"		join workflow_move wfm on wfm.workflow_id = t.workflow_id \n" +
						"where \n" +
						"		c.status_id=wfm.to_status_id \n" +
						"	and c.card_id=? \n" +
						"	and wfm.wfm_id=? \n",
						new Object[] { cardId.getId(), wfmId.getId() },
						new int[] { Types.NUMERIC, Types.NUMERIC }
					);
					return new Boolean(result > 0);
				}
			});
		//	final ObjectQueryBase query = this.getQueryFactory().getFetchQuery(WorkflowMove.class);
		//	query.setId(wfmId);
		//	final WorkflowMove wfm = (WorkflowMove) this.getDatabase().executeQuery( getSystemUser(), query);
		//	if (wfm.getToState().equals(visas.getState())) ...
		return cardAlreadyAtState.booleanValue();
	}

	protected void sendForVisa(final Card visa) throws DataException {
		if(logger.isInfoEnabled())
			logger.info("[" + getName() + ":" + docId.getId() + "] Sending visas card #" + visa.getId().getId());
		doAction(new LockObject(visa));
		try {
			final ObjectId wfmId = getObjectIdByTemplate( WorkflowMove.class, VisaConfiguration.MOVE_SEND, visa.getTemplate() );

			// ���� �������� ��� � ������ ��������� - ������ ������ ����� �� ����...
			if (isCardAlreadyAtWFMState( visa.getId(), wfmId)) {
				// ��� � ������ ���������...
				if(logger.isWarnEnabled())
					logger.warn( "Card " + visa.getId().getId() + " already at waiting visas state: "+ visa.getState() );
				return;
			}

			final ChangeState move = new ChangeState();
			move.setWorkflowMove((WorkflowMove) DataObject.createFromId( wfmId));
			move.setCard(visa);
			doAction(move);
			try {
				final ObjectId personAttrId = getObjectIdByTemplate(PersonAttribute.class, VisaConfiguration.ATTR_PERSON, visa.getTemplate());
				if (personAttrId != null) {
					final PersonAttribute attrTo = (PersonAttribute) visa.getAttributeById(personAttrId);
					if (attrTo != null) // ok
						logger.info("Document " + docId.getId() + " sent for visas to " + attrTo.getPersonName());
					else
						logger.warn("Document " + docId.getId() + " sent for visas (" +  VisaConfiguration.ATTR_PERSON + " configured but not present in the card)");
				} else
					logger.info("Document " + docId.getId() + " sent for visas ( parameter '" +  VisaConfiguration.ATTR_PERSON + "' is not configured)");
			} catch (Exception e) {
				// just logging failed - ignore
			}
		} finally {
			doAction(new UnlockObject(visa));
		}
	}

	protected void returnVisaFromEnclosed(final Card visa) throws DataException {
		if(logger.isInfoEnabled())
			logger.info("[" + getName() + ":" + docId.getId() + "] Return from enclosed visas card #" + visa.getId().getId());
		doAction(new LockObject(visa));
		try {
			ObjectId historyId = null;
			WorkflowMove wfm = null;
			try{
				historyId = getObjectIdByTemplate
					(HtmlAttribute.class, VisaConfiguration.ATTR_DECISION_HISTORY, visa.getTemplate());
			}catch (DataException e){}
			if (historyId != null)
			{
				ObjectId returnState = getPreviousCardStateFromHistory(visa.getId(), historyId);
				wfm = CardUtils.findWorkFlowMove(visa.getId(), returnState,
						this.getQueryFactory(), this.getDatabase(), getSystemUser()
				);
			}
			if(wfm == null)
			{
				if(logger.isWarnEnabled())
					logger.warn("Can't get previous state from history. Will use default one from .properties file.");
				final ObjectId wfmId = getObjectIdByTemplate( WorkflowMove.class, VisaConfiguration.MOVE_RETURN_FROM_ENCLOSED, visa.getTemplate() );
				wfm = (WorkflowMove) DataObject.createFromId( wfmId);
			}
			// ���� �������� ��� � ������ ��������� - ������ ������ ����� �� ����...
			if (isCardAlreadyAtWFMState( visa.getId(), wfm.getId())) {
				// ��� � ������ ���������...
				if(logger.isWarnEnabled())
					logger.warn( "Card " + visa.getId().getId() + " already at waiting visas state: "+ visa.getState() );
				return;
			}
			final ChangeState move = new ChangeState();
			move.setWorkflowMove(wfm);
			move.setCard(visa);
			doAction(move);
			try {
				final ObjectId personAttrId = getObjectIdByTemplate(PersonAttribute.class, VisaConfiguration.ATTR_PERSON, visa.getTemplate());
				if (personAttrId != null) {
					final PersonAttribute attrTo = (PersonAttribute) visa.getAttributeById(personAttrId);
					if (attrTo != null) // ok
						logger.info("Document " + docId.getId() + " sent for visas to " + attrTo.getPersonName());
					else
						logger.warn("Document " + docId.getId() + " sent for visas (" +  VisaConfiguration.ATTR_PERSON + " configured but not present in the card)");
				} else
					logger.info("Document " + docId.getId() + " sent for visas ( parameter '" +  VisaConfiguration.ATTR_PERSON + "' is not configured)");
			} catch (Exception e) {
				// just logging failed - ignore
			}
		} finally {
			doAction(new UnlockObject(visa));
		}
	}

	protected Collection getRootNextStageVisas() throws DataException {
		boolean reverse = VisaConfiguration.OPTION_ORDER_REVERSE.equalsIgnoreCase(
				config.getValue(VisaConfiguration.OPTION_ORDER));
		ArrayList selected = new ArrayList();
		int stage = reverse ? Integer.MIN_VALUE : Integer.MAX_VALUE;
		for (Iterator itr = getRootVisaCardList().iterator(); itr.hasNext(); ) {
			Card visa = (Card) itr.next();
			if (visa.getState().equals(getObjectIdByTemplate(CardState.class, VisaConfiguration.STATE_ASSIGNED,
					visa.getTemplate()))) {
				int visaStage = getOrder(visa);
				if (reverse ? visaStage > stage : visaStage < stage) {
					selected = new ArrayList();
					stage = visaStage;
				}
				if (visaStage == stage)
					selected.add(visa);
			}
		}
		if (selected.size() > 0)
			logger.info("Document " + docId.getId() + " visas, stage " + stage + ": " +
					selected.size() + " persons assigned");
		return selected;
	}

	protected Card getDocument() throws DataException {
		Set visaSetAttrs = config.getObjectIdSet(CardLinkAttribute.class, VisaConfiguration.ATTR_VISAS);
		if (document == null) {
			logger.info("[" + getName() + ":" + docId.getId() + "] Fetching the document");
			Search search = new Search();
			search.setByCode(true);
			search.setWords(docId.getId().toString());
			search.setColumns(new ArrayList());
			for (Iterator itr = visaSetAttrs.iterator(); itr.hasNext(); ) {
				ObjectId attrId = (ObjectId) itr.next();
				SearchResult.Column col = new SearchResult.Column();
				col.setAttributeId(attrId);
				search.getColumns().add(col);
			}
			SearchResult.Column col = new SearchResult.Column();
			col.setAttributeId(Card.ATTR_TEMPLATE);
			search.getColumns().add(col);
			if (VisaConfiguration.OPTION_RETURN_ATTR.equalsIgnoreCase(config.getValue(VisaConfiguration.OPTION_RETURN))) {
				col = new SearchResult.Column();
				col.setAttributeId(config.getObjectId(ListAttribute.class, VisaConfiguration.ATTR_RETURN));
				search.getColumns().add(col);
			}
			SearchResult result = (SearchResult) doAction(search);
			if (result.getCards().size() != 1)
				throw new DataException("docflow.visa.document",
						new Object[] { docId.getId().toString() });
			document = (Card) result.getCards().iterator().next();
			// Search does not return some attributes in cards if they don't have values,
			// so we must care about their presence in results.
			for (Iterator itr = visaSetAttrs.iterator(); itr.hasNext(); ) {
				ObjectId attrId = (ObjectId) itr.next();
				if (document.getAttributeById(attrId) == null)
					document.getAttributes().add(DataObject.createFromId(attrId));
			}
		}
		return document;
	}

	protected List<Card> getRootVisaCardList() throws DataException {
		int size = 0;
		if (rootVisaCardList!=null)
			size = rootVisaCardList.size();
		if(logger.isDebugEnabled())
			logger.debug("[getRootVisaCardList()]: rootVisaCardList contain "+size+" rows");
		if (rootVisaCardList == null) {
			logger.info("[" + getName() + ":" + docId.getId() + "] Fetching document's visas");
			rootVisaCardList = new ArrayList();
			Set visaAttrs = config.getObjectIdSet(CardLinkAttribute.class, VisaConfiguration.ATTR_VISAS);
			String visaSet = "null";
			if (visaAttrs!=null)
				visaSet = visaAttrs.toString();
			if(logger.isDebugEnabled())
				logger.debug("[getRootVisaCardList()]: visaAttrs = "+visaSet);
			for (Iterator itr = visaAttrs.iterator(); itr.hasNext(); ) {
				ObjectId id = (ObjectId) itr.next();
				if(CardLinkAttribute.class.isAssignableFrom(id.getType())) {
					CardLinkAttribute cLinkAttr = getDocument().getCardLinkAttributeById(id);
					size = 0;
					if (cLinkAttr!=null)
						size = cLinkAttr.getLinkedCount();
					if(logger.isDebugEnabled())
						logger.debug("[getRootVisaCardList()]: CardLinkAttribute for "+id.toString() + " contain "+size + " rows");
					rootVisaCardList.addAll(getVisaCardList(cLinkAttr));
				} else if(BackLinkAttribute.class.isAssignableFrom(id.getType())) {
					BackLinkAttribute cLinkAttr = (BackLinkAttribute) getDocument().getAttributeById(id);
					final ListProject lp = new ListProject();
					lp.setAttribute(id);
					lp.setCard(getDocument().getId());
					final List<Card> result = CardUtils.getCardsList((SearchResult) doAction(lp));
					size = 0;
					if (result != null)
						size = result.size();
					if(logger.isDebugEnabled())
						logger.debug("[getRootVisaCardList()]: BackLinkAttribute for "+id.toString()+" contain " + size + " rows");
					rootVisaCardList.addAll(getVisaCardList(ObjectIdUtils.numericIdsToCommaDelimitedString(ObjectIdUtils.cardsToObjectIdsSet(result))));
				} else
					throw new IllegalArgumentException("Attribute " + id.getId() + " must be CardLink or BackLink");
			}
			/* 04.03.2011, O.E. -
			 * �.�. ���� ������������ ����� ��������� ������� ��������� �������
			 * �������������� ������ ���� ���������������.
			 */
			if(visaAttrs.size() > 1 ) sortCardsByOrder(rootVisaCardList);
			size = 0;
			if (rootVisaCardList!=null)
				size = rootVisaCardList.size();
			if(logger.isDebugEnabled())
				logger.debug("[getRootVisaCardList()]: after execute method rootVisaCardList contain "+size+" rows");
		}
		return rootVisaCardList;
	}

	protected List getChildVisaCardList(Card visaCard) throws DataException {
		ArrayList result = new ArrayList();
		Set visaAttrs = config.getObjectIdSet(CardLinkAttribute.class, VisaConfiguration.ATTR_ENCLOSED_VISAS);
		for (Iterator itr = visaAttrs.iterator(); itr.hasNext(); ) {
			ObjectId id = (ObjectId) itr.next();
			if (visaCard.getCardLinkAttributeById(id) != null){
				result.addAll(getVisaCardList(visaCard.getCardLinkAttributeById(id)));
			}
		}
		return result;
	}
	
	protected List getVisaCardList(CardLinkAttribute visaLinkAttribute) throws DataException {
		return getVisaCardList(visaLinkAttribute.getLinkedIds());
	}

	protected List getVisaCardList(String ids) throws DataException {
		Search search = new Search();
		search.setByCode(true);
		search.setWords(ids); // (2009/12/11m RuSA) OLD: getStringValue()
		search.setColumns(new ArrayList());
		SearchResult.Column col;
		if (config.getValue(VisaConfiguration.ATTR_ORDER) != null) {
			col = new SearchResult.Column();
			col.setAttributeId(config.getObjectId(IntegerAttribute.class, VisaConfiguration.ATTR_ORDER));
			col.setSorting(SearchResult.Column.SORT_ASCENDING);
			search.getColumns().add(col);
		}
		if (config.getValue(VisaConfiguration.ATTR_PERSON) != null) {
			col = new SearchResult.Column();
			col.setAttributeId(config.getObjectId(PersonAttribute.class, VisaConfiguration.ATTR_PERSON));
			search.getColumns().add(col);
		}
		if (config.getValue(VisaConfiguration.ATTR_ENCLOSED_VISAS) != null) {
			col = new SearchResult.Column();
			col.setAttributeId(config.getObjectId(CardLinkAttribute.class, VisaConfiguration.ATTR_ENCLOSED_VISAS));
			search.getColumns().add(col);
		}
		if (config.getValue(VisaConfiguration.ATTR_ENCLOSED_COMPLETE_DATE) != null) {
			col = new SearchResult.Column();
			col.setAttributeId(config.getObjectId(DateAttribute.class, VisaConfiguration.ATTR_ENCLOSED_COMPLETE_DATE));
			search.getColumns().add(col);
		}
		if (config.getValue(VisaConfiguration.FLAG_VISA_MARK_PROCESS) != null) {
			col = new SearchResult.Column();
			col.setAttributeId(config.getObjectId(IntegerAttribute.class, VisaConfiguration.FLAG_VISA_MARK_PROCESS));
			search.getColumns().add(col);
		}
		Map<Object, ObjectId> personAttrs = config.getObjectIdMap(VisaConfiguration.ATTR_PERSON + VisaConfiguration.INFIX_TEMPLATE,
				Template.class, PersonAttribute.class);
		for (Iterator<ObjectId> itrIn = personAttrs.values().iterator(); itrIn.hasNext(); ) {
			ObjectId idIn = itrIn.next();
			col = new SearchResult.Column();
			col.setAttributeId(idIn);
			search.getColumns().add(col);
		}
		col = new SearchResult.Column();
		col.setAttributeId(Card.ATTR_STATE);
		search.getColumns().add(col);
		col = new SearchResult.Column();
		col.setAttributeId(Card.ATTR_TEMPLATE);
		search.getColumns().add(col);
		SearchResult result = (SearchResult) doAction(search);
		/* 04.03.2011, O.E. -
		 * �.�. searchByWords �� ������ ������ ��������� �� ��������� ,
		 * ��������� ����������.
		 */
		sortCardsByOrder(result.getCards());
		return result.getCards();
	}

	protected Object doAction(Action action) throws DataException {
		ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAccessChecker(null);
		query.setAction(action);
		return getDatabase().executeQuery(getSystemUser(), query);
	}

	protected ObjectId getObjectIdByTemplate(Class type, String key, ObjectId template) throws DataException {
		Map<Object, ObjectId> specific = config.getObjectIdMap(key + VisaConfiguration.INFIX_TEMPLATE,
				Template.class, type);
		if (specific.containsKey(template))
			return specific.get(template);
		return config.getObjectId(type, key);
	}

	protected boolean isListedIdForTemplate(ObjectId id, String key, ObjectId template) throws DataException {
		Map specific = config.getObjectIdSetMap(key + VisaConfiguration.INFIX_TEMPLATE,
				Template.class, id.getType());
		if (specific.containsKey(template)) {
			Set ids = (Set) specific.get(template);
			return ids.contains(id);
		}
		return config.isListedId(id, key);
	}

	private String name;
	public void setBeanName(String name) { this.name = name; }
	public String getName() { return name; }

	private void assignVisa(Card card) throws DataException {

		final ObjectId cardStateId = card.getState();
		if(logger.isInfoEnabled())
			logger.info("[" + config.getName() + "] Sending visa card " + card.getId().getId() + " to appropriate person");
		final ObjectId desiredState =
			// config.getObjectId(CardState.class, VisaConfiguration.STATE_ASSIGNED);
			getObjectIdByTemplate(CardState.class, VisaConfiguration.STATE_ASSIGNED, card.getTemplate());
		if (desiredState.equals(cardStateId))
			return;

		if (config.isListedId(cardStateId, VisaConfiguration.STATES_IGNORED)) {
			if(logger.isInfoEnabled())
				logger.info("[" + config.getName() + "] State changing of card " + card.getId().getId() + " is not required as it is in ignored state: " + cardStateId.getId());
			return;
		}

		final WorkflowMove wfm = findProperMove(card, desiredState);
		if(logger.isInfoEnabled())
			logger.info("[" + config.getName() + "] Workflow move to be " + wfm.getId().getId() + " for visa " + card.getId().getId());

		lockCard( card.getId());
		try {
			final ChangeState move = new ChangeState();
			move.setCard(card);
			move.setWorkflowMove(wfm);
			execAction(move);
		} finally {
			unlockCard(card.getId());
		}
	}

	private void lockCard(ObjectId cardId) throws DataException
	{
		final LockObject lock = new LockObject(cardId);
		execAction(lock);
	}


	private void unlockCard(ObjectId cardId) throws DataException
	{
		final UnlockObject unlock = new UnlockObject(cardId);
		execAction(unlock);
	}

	private Object execAction(Action action) throws DataException
	{
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAccessChecker(null);
		query.setAction(action);
		return getDatabase().executeQuery(getSystemUser(), query);
	}

	/**
	 * ����� ������� ��� �������� � ��������� ��������� �� ��������.
	 * @param card: ��������
	 * @param destStateId: ������� ���������.
	 * @return ������������ id-��������.
	 * @throws DataException ���� ��� �������� �� �������� ��������� � �������.
	 */
	private WorkflowMove findProperMove(Card card, ObjectId destStateId)
		throws DataException
	{
		final ChildrenQueryBase query = getQueryFactory().getChildrenQuery(Card.class, WorkflowMove.class);
		query.setParent(card.getId());
		Collection moves = (Collection) getDatabase().executeQuery(getSystemUser(), query);
		if (moves != null) {
			for (Iterator itr = moves.iterator(); itr.hasNext(); ) {
				final WorkflowMove wfm = (WorkflowMove) itr.next();
				if (destStateId.equals(wfm.getToState()))
					return wfm;
			}
		}
		throw new DataException("jbr.docflow.nomove",
			new Object[] { card.getId().getId().toString(), DataException.RESOURCE_PREFIX + "state." + destStateId.getId() });
	}

	protected void checkEnclosedSet() throws DataException {
		final Set waitEnclosedStateSet = config.getObjectIdSet( CardState.class, VisaConfiguration.STATES_WAIT_ENCLOSED);
		for (Iterator itr = getRootVisaCardList().iterator(); itr.hasNext(); ) {
			Card visa = (Card) itr.next();
			if (waitEnclosedStateSet != null && waitEnclosedStateSet.contains(visa.getState())) {
				processWaitEnclosedVisa(visa);
			}
		}
	}

	protected void processWaitEnclosedVisa(Card visaCard) throws DataException {
		final Set waitingStateSet = config.getObjectIdSet( CardState.class, VisaConfiguration.STATES_WAITING);
		final Set waitEnclosedStateSet = config.getObjectIdSet( CardState.class, VisaConfiguration.STATES_WAIT_ENCLOSED);
		final ObjectId draftState = config.getObjectId(CardState.class, VisaConfiguration.STATE_DRAFT);
		boolean hasWaitingChildVisa = false;
		for (Iterator childItr = getChildVisaCardList(visaCard).iterator(); childItr.hasNext(); ) {
			final Card childVisa = (Card) childItr.next();
			final ObjectId cardStateId = childVisa.getState();
			if (draftState.equals(cardStateId)) {
				assignVisa(childVisa);
				cleanEnclosedCompleteDate(childVisa.getId());
			}
			if (waitEnclosedStateSet.contains(childVisa.getState())) {
				processWaitEnclosedVisa(childVisa);
			}
			if (waitingStateSet.contains(childVisa.getState())) {
				hasWaitingChildVisa = true;
			}
		}
		if (!hasWaitingChildVisa) {
			final Collection nextVisas = getChildNextStageVisas(visaCard);
			if (!nextVisas.isEmpty()) {
				cleanEnclosedCompleteDate(visaCard.getId());
				for (Iterator itr = nextVisas.iterator(); itr.hasNext(); ) {
					sendForVisa((Card) itr.next());
				}
			}
			else {
				setEnclosedCompleteDate(visaCard.getId());
				returnVisaFromEnclosed(visaCard);
			}
		}
	}

	protected Collection getChildNextStageVisas(Card visaCard) throws DataException {
		boolean reverse = VisaConfiguration.OPTION_ORDER_REVERSE.equalsIgnoreCase(
				config.getValue(VisaConfiguration.OPTION_ORDER));
		ArrayList selected = new ArrayList();
		int stage = reverse ? Integer.MIN_VALUE : Integer.MAX_VALUE;
		for (Iterator itr = getChildVisaCardList(visaCard).iterator(); itr.hasNext(); ) {
			Card visa = (Card) itr.next();
			if (visa.getState().equals(getObjectIdByTemplate(CardState.class, VisaConfiguration.STATE_ASSIGNED,
					visa.getTemplate()))) {
				int visaStage = getOrder(visa);
				if (reverse ? visaStage > stage : visaStage < stage) {
					selected = new ArrayList();
					stage = visaStage;
				}
				if (visaStage == stage)
					selected.add(visa);
			}
		}
		if (selected.size() > 0)
			logger.info("Document " + docId.getId() + " visas, stage " + stage + ": " +
					selected.size() + " persons assigned");
		return selected;
	}

	protected void cleanEnclosedCompleteDate(ObjectId visaId) throws DataException {
		if(logger.isInfoEnabled())
			logger.info("[" + getName() + ":" + docId.getId() + "] Clean complete date for card #" + visaId.getId());
		Card card = loadCard(visaId);
		DateAttribute dateAttr = (DateAttribute)card.getAttributeById(enclosedCompleteDateId);
		if (dateAttr != null && dateAttr.getValue() != null) {
			doAction(new LockObject(card));
			try {
				dateAttr.setValue(null);
				saveCard(card);
			}
			finally {
				doAction(new UnlockObject(card));
			}
		}
	}

	protected void setEnclosedCompleteDate(ObjectId visaId) throws DataException {
		if(logger.isInfoEnabled())
			logger.info("[" + getName() + ":" + docId.getId() + "] Set enclosed complete date for card #" + visaId.getId());
		Card card = loadCard(visaId);
		doAction(new LockObject(card));
		try {
			((DateAttribute)card.getAttributeById(enclosedCompleteDateId)).setValue(new Date());
			saveCard(card);
		}
		finally {
			doAction(new UnlockObject(card));
		}
	}

	protected ObjectId getPreviousCardStateFromHistory(final ObjectId cardId, final ObjectId historyAttrId)
	{
		try{
			return (ObjectId) this.getDatabase().executeQuery(getSystemUser(), new QueryBase()
			{
				@Override
				public Object processQuery() throws DataException
				{
					final String sql =
						"select wfm.from_status_id from card c" +
						" join attribute_value a on a.card_id = c.card_id and attribute_code = " +
						"'" + historyAttrId.getId() + "'" +
						" join workflow_move wfm on wfm.name_rus = " +
						" xmlserialize(content (xpath(cast('//part[last()]/@action' as varchar)," +
						" cast(convert_from(a.long_binary_value, 'UTF8') as xml)))[1] as varchar(100))" +
						" where c.card_id = " +
						cardId.getId();
					RowMapper rowMapper = new RowMapper() {
						public Object mapRow(ResultSet rs, int index) throws SQLException {
								return new ObjectId (CardState.class, rs.getLong(1));
						}
					};
					return getJdbcTemplate().queryForObject(sql, rowMapper);
				}
			}
			);
		}
		catch(Exception e){e.printStackTrace(); return null;}
	}
	protected void sortCardsByOrder(List<Card> cards) throws DataException
	{
		if (!config.isPropertySet(VisaConfiguration.ATTR_ORDER)) {
			return;
		}

		try {
			Collections.sort(
					cards,
					new Comparator <Card> () {
						public int compare(Card a, Card b)
						{
							Integer aOrder;
							Integer bOrder;
							try {
								aOrder = getOrder(a);
								bOrder = getOrder(b);
							} catch (DataException ex) {
								throw new DataExceptionWrapper(ex);
							}
							return (aOrder.compareTo(bOrder));
						}
					}
			);
		} catch (DataExceptionWrapper ex) {
			throw ex.getException();
		}
	}

	private static class DataExceptionWrapper extends RuntimeException {
		private DataException ex;
		public DataExceptionWrapper(DataException ex) {
			this.ex = ex;
		}

		public DataException getException() {
			return ex;
		}
	}

	private void saveCard(Card card) throws DataException{
		final SaveQueryBase query = getQueryFactory().getSaveQuery(card);
		query.setObject(card);
		getDatabase().executeQuery(getSystemUser(), query);
	}

	private Card loadCard(ObjectId cardId) throws DataException{
		final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(Card.class);
		cardQuery.setAccessChecker(null);
		cardQuery.setId(cardId);
		return (Card) getDatabase().executeQuery(getSystemUser(), cardQuery);
	}

	private boolean hasMistakeSendedVisa() throws DataException {
		boolean result = false;
		//������ ��������� � �������
		final Set mistakeStateSet = config.getObjectIdSet(
				CardState.class,
				VisaConfiguration.STATE_MISTAKE, true);
		if (mistakeStateSet == null) return false;
		//���� �� �����
		for (Iterator itr = getRootVisaCardList().iterator(); itr.hasNext(); ) {
			Card visa = (Card) itr.next();
			//�������� �� �� ��� ���� ���������� ��������
			if (mistakeStateSet.contains(visa.getState())){
				result = true;
				break;
			}
		}
		return result;

	}

	private boolean hasAgreeStatesVisas() throws DataException {
		boolean result = false;
		//������������� �������
		final Set agreeStateSet = config.getObjectIdSet(
				CardState.class,
				VisaConfiguration.STATES_AGREED, true);
		if (agreeStateSet == null) return false;
		//���� �� �����
		for (Iterator itr = getRootVisaCardList().iterator(); itr.hasNext(); ) {
			Card visa = (Card) itr.next();
			//�������� �� �� ��� ���� ������������
			if (agreeStateSet.contains(visa.getState())){
				result = true;
				break;
			}
		}
		return result;
	}
}
