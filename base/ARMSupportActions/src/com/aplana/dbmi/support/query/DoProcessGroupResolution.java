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
package com.aplana.dbmi.support.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.service.impl.query.WriteQuery;
import com.aplana.dbmi.support.action.LightCreateCard;
import com.aplana.dbmi.support.action.ProcessGroupResolution;

public class DoProcessGroupResolution extends ActionQueryBase implements WriteQuery {

	private static final long serialVersionUID = 1L;

	private final static ObjectId EXAM_PARENT_ATTR_ID = ObjectId.predefined(
			BackLinkAttribute.class, "jbr.exam.parent");

	private final static ObjectId MAIN_DOC_ATTR_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.main.doc");
	private final static ObjectId BY_DOC_ATTR_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.rimp.bydoc");
	private final static ObjectId BY_RIMP_ATTR_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.rimp.byrimp");
	private final static ObjectId REPORT_INT_PARENT_ATTR_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.report.int.parent");
	private final static ObjectId RESOLUTION_TEXT_ATTR_ID = ObjectId
			.predefined(TextAttribute.class, "jbr.resolutionText");
	private final static ObjectId EXECUTOR_ATTR_ID = ObjectId.predefined(
			PersonAttribute.class, "jbr.AssignmentExecutor");
	private final static ObjectId COEXECUTOR_ATTR_ID = ObjectId.predefined(
			PersonAttribute.class, "jbr.CoExecutor");
	private final static ObjectId EXT_EXECUTOR_ATTR_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.ExtExecutor");
	private final static ObjectId SIGNER_ATTR_ID = ObjectId.predefined(
			PersonAttribute.class, "jbr.resolution.FioSign");
	private final static ObjectId CONTROL_ATTR_ID = ObjectId.predefined(
			ListAttribute.class, "jbr.oncontrol");
	private final static ObjectId INSPECTOR_ATTR_ID = ObjectId.predefined(
			PersonAttribute.class, "jbr.commission.inspector");
	private final static ObjectId TERM_ATTR_ID = ObjectId.predefined(
			DateAttribute.class, "jbr.resolutionTerm");
	private final static ObjectId FILES_ATTR_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.files");

	private final static ObjectId SIGN_DATE_ATTR_ID = ObjectId.predefined(
			DateAttribute.class, "jbr.resolution.SignDate");
	private final static ObjectId EXECUTION_SENDER_ATTR_ID = ObjectId
			.predefined(PersonAttribute.class, "jbr.report.sent.execute");

	private final static ObjectId REPORT_IN_PROGRESS_STATE_ID = ObjectId
			.predefined(CardState.class, "jbr.report.inprogress");
	private final static ObjectId ON_EXECUTION_1_WFM_ID = ObjectId
			.predefined(WorkflowMove.class, "jbr.commission.execute1");	
	public static final ObjectId TEMPLATE_PERSONAL_CONTROL = ObjectId
			.predefined(Template.class, "jbr.boss.control");
	public static final ObjectId TEMPLATE_INDEP_RESOLUTION = ObjectId
			.predefined(Template.class, "jbr.independent.resolution");
	public static final ObjectId ATTR_PCON_DOC = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.boss.control.document");
	public static final ObjectId ATTR_PCON_DATE = ObjectId.predefined(
			DateAttribute.class, "jbr.boss.control.date");
	public static final ObjectId ATTR_PCON_PERSON = ObjectId.predefined(
			PersonAttribute.class, "jbr.boss.control.inspector");
	

	@SuppressWarnings("serial")
	private final static List<ObjectId> copyAttributesId = new ArrayList<ObjectId>() {
		{
			add(RESOLUTION_TEXT_ATTR_ID);
			add(EXECUTOR_ATTR_ID);
			add(COEXECUTOR_ATTR_ID);
			add(EXT_EXECUTOR_ATTR_ID);
			add(SIGNER_ATTR_ID);
			add(CONTROL_ATTR_ID);
			add(INSPECTOR_ATTR_ID);
			add(TERM_ATTR_ID);
			add(FILES_ATTR_ID);
		}
	};

	private final static ObjectId RASSM_TEMPLATE_ID = ObjectId.predefined(
			Template.class, "jbr.rassm");
	private final static ObjectId REPORT_TEMPLATE_ID = ObjectId.predefined(
			Template.class, "jbr.report.internal");
	private final static ObjectId RESOLUTION_TEMPLATE_ID = ObjectId.predefined(
			Template.class, "jbr.resolution");

	Card groupResolution;
	
	List<Card> resolutions = new ArrayList<Card>();

	@Override
	public Object processQuery() throws DataException {
		ProcessGroupResolution action = (ProcessGroupResolution) getAction();
		groupResolution = action.getCard();
		List<ObjectId> docs = action.getDocs();
		for (ObjectId doc : docs) {
			Card docCard = (Card) loadObject(doc);
			if (RASSM_TEMPLATE_ID.equals(docCard.getTemplate())) {
				processRassm(action, docCard);
			} else if (REPORT_TEMPLATE_ID.equals(docCard.getTemplate())) {
				processReport(docCard);
			}
		}
		if(!action.isOnlyCreate()){
			WorkflowMove execute1Wfm = (WorkflowMove) loadObject(ON_EXECUTION_1_WFM_ID);
			for(Card resolution: resolutions){
				resolution = (Card) loadObject(resolution.getId());
				doSafeChangeState(resolution, execute1Wfm);
			}
		}
		return null;
	}

	private void processReport(Card docCard) throws DataException {
		if(!docCard.getState().equals(REPORT_IN_PROGRESS_STATE_ID))
			doSafeChangeState(docCard, findMove(docCard, REPORT_IN_PROGRESS_STATE_ID));
		
		ObjectId resolution_id = ((CardLinkAttribute) docCard
				.getAttributeById(REPORT_INT_PARENT_ATTR_ID)).getFirstIdLinked();
		//��������� �������� ������ �� ����������
		Card resolutionCard = (Card) loadObject(resolution_id); 

		Card newResolution;
		
		if(TEMPLATE_INDEP_RESOLUTION.equals(resolutionCard.getTemplate())){
			newResolution = fillResolution(groupResolution,
					createResolution(resolutionCard, BY_DOC_ATTR_ID));
		} else {
			newResolution = fillResolution(groupResolution,
					createResolution(resolutionCard, BY_RIMP_ATTR_ID));
		}
		
		ObjectId newResolutionId = (ObjectId) saveObject(newResolution, getSystemUser());
		execAction(new UnlockObject(newResolutionId));
		newResolution.setId(newResolutionId);
		
		resolutions.add(newResolution);
		if(TEMPLATE_INDEP_RESOLUTION.equals(resolutionCard.getTemplate())){
			updatePersonControl(resolutionCard.getId());
		} else {
			updatePersonControl(resolutionCard.getCardLinkAttributeById(MAIN_DOC_ATTR_ID).getFirstIdLinked());
		}
	}

	private void processRassm(ProcessGroupResolution action, Card docCard)
			throws DataException {
		ObjectId exam_parent_id = ((BackLinkAttribute) docCard
				.getAttributeById(EXAM_PARENT_ATTR_ID)).getFirstIdLinked();
		Card parentCard = (Card) loadObject(exam_parent_id);

		Card newResolution = fillResolution(groupResolution,
				createResolution(parentCard, BY_DOC_ATTR_ID));
		
		ObjectId newResolutionId = (ObjectId) saveObject(newResolution, getSystemUser());
		execAction(new UnlockObject(newResolutionId));
		newResolution.setId(newResolutionId);
		
		resolutions.add(newResolution);
		
		updatePersonControl(exam_parent_id);
	}

	private Card fillResolution(Card groupResolution, Card newResolution) {
		for (ObjectId attr : copyAttributesId) {
			newResolution.getAttributeById(attr).setValueFromAttribute(
					groupResolution.getAttributeById(attr));
		}
		((DateAttribute)groupResolution.getAttributeById(SIGN_DATE_ATTR_ID)).setValue(new Date());
		((PersonAttribute)groupResolution.getAttributeById(EXECUTION_SENDER_ATTR_ID)).setPerson(getUser().getPerson());
		return newResolution;
	}
	
	private void updatePersonControl(ObjectId baseCardId){
		if(((ProcessGroupResolution) getAction()).getPersonalControlDate()==null){
			return;
		}
		Card personalControlCard = searchPersonalControlCard(baseCardId);
		if(personalControlCard == null){
			addPersonControl(baseCardId);
		} else {
			boolean locked = false;
			try {
				execAction(new LockObject(personalControlCard));
				locked = true;
				Card card = (Card) loadObject(personalControlCard.getId());
				setAttributePersonControl(card, baseCardId);
				saveObject(card, getUser());
			} catch (Exception e) {
				logger.error("Error to update the card 'on personal control':",	e);
			} finally {
				if (locked) {
					try{
						execAction(new UnlockObject(personalControlCard));
					} catch (Exception e){
						logger.error("Can't unlock personalControlCard: ",	e);
					}
				}
			}
		}
	}
	
	private ObjectId addPersonControl(ObjectId baseCardId) {
		CreateCard createCard = new CreateCard(TEMPLATE_PERSONAL_CONTROL);
		Card card = null;
		try {
			card = (Card) execAction(createCard);
			setAttributePersonControl(card, baseCardId);
			ObjectId cardId = (ObjectId) saveObject(card, getUser());
			execAction(new UnlockObject(cardId));
			return cardId;
		} catch (Exception e) {
			logger.error("Error saving card 'On personal control':", e);
		}
		return null;
	}
	
	private void setAttributePersonControl(Card personalControlCard, ObjectId baseCardId) {
		//���������
		((PersonAttribute) personalControlCard.getAttributeById(ATTR_PCON_PERSON)).setPerson(getUser().getPerson());
		//�������� ��� ��������
		final CardLinkAttribute attr = (CardLinkAttribute)personalControlCard.getAttributeById(ATTR_PCON_DOC);
		attr.addSingleLinkedId(baseCardId);
		//���� ��������
		((DateAttribute) personalControlCard.getAttributeById(ATTR_PCON_DATE))
			.setValue(((ProcessGroupResolution) getAction()).getPersonalControlDate());
		//��������
		((StringAttribute) personalControlCard.getAttributeById(Attribute.ID_NAME))
				.setValue("������ ��������");
	}

	private Card createResolution(Card parentCard, ObjectId sourceLink) throws DataException {
		LightCreateCard lightCreateCard = new LightCreateCard();
		lightCreateCard.setParent(parentCard);
		lightCreateCard.setLinked(true);
		lightCreateCard.setTemplate(RESOLUTION_TEMPLATE_ID);
		Card newResolution = (Card) execAction(lightCreateCard);
		newResolution.getCardLinkAttributeById(sourceLink).addLinkedId(
				parentCard.getId());

		CardLinkAttribute mainDocParent = parentCard
				.getCardLinkAttributeById(MAIN_DOC_ATTR_ID);
		// == null ������ ���� ������ ��
		if (mainDocParent != null) {
			CardLinkAttribute mainDoc = newResolution
					.getCardLinkAttributeById(MAIN_DOC_ATTR_ID);
			List<ObjectId> list = mainDocParent.getIdsLinked();
			if (list != null && !list.isEmpty()) {
				mainDoc.addLinkedId(list.get(0));
			} else {
				logger.error("Parent doc " + parentCard.getId() != null ? parentCard
						.getId().getId() : parentCard.getId()
						+ " have not link to MainDoc");
			}
			if (list.size() > 1) {
				logger.warn("Parent doc " + parentCard.getId() != null ? parentCard
						.getId().getId() : parentCard.getId()
						+ " have more than 1 link to MainDoc");
			}
		} else {
			// ���� ������ ��, �� ������ �� ��, ��� � � parentAttr
			CardLinkAttribute mainDoc = newResolution
					.getCardLinkAttributeById(MAIN_DOC_ATTR_ID);
			mainDoc.addLinkedId(parentCard.getId());
		}
		return newResolution;
	}

	private Object execAction(Action<?> action) throws DataException {
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		return getDatabase().executeQuery(getUser(), query);
	}

	private DataObject loadObject(ObjectId id) throws DataException {
		final ObjectQueryBase fetchQuery = getQueryFactory().getFetchQuery(
				id.getType());
		fetchQuery.setId(id);
		return (DataObject) getDatabase().executeQuery(getUser(), fetchQuery);
	}
	
	private Object saveObject(DataObject object, UserData user) throws DataException {
		final SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(object);
		saveQuery.setObject(object);
		return getDatabase().executeQuery(user, saveQuery);
	}
	
	protected WorkflowMove findMove(Card card, ObjectId dstStateId) throws DataException {
		final WorkflowMove wfm =
			CardUtils.findWorkFlowMove( card.getId(), dstStateId, getQueryFactory(), getDatabase(), getSystemUser());
		if (wfm != null)
			return wfm;
		throw new DataException("jbr.linked.nomove",
			new Object[] { card.getId().getId().toString(), "@state." + dstStateId.getId() });
	}
	
	protected void doSafeChangeState(Card card, WorkflowMove wfm) throws DataException {
		final LockObject lock = new LockObject(card);
		ActionQueryBase query = getQueryFactory().getActionQuery(lock);
		query.setAction(lock);
		getDatabase().executeQuery(getUser(), query);
		try {
			doChangeState(card, wfm);
		} finally {
			final UnlockObject unlock = new UnlockObject(card);
			query = getQueryFactory().getActionQuery(unlock);
			query.setAction(unlock);
			getDatabase().executeQuery(getUser(), query);
		}
	}
	
	protected void doChangeState(Card card, WorkflowMove wfm) throws DataException {
		ChangeState move = new ChangeState();
		move.setCard(card);
		move.setWorkflowMove(wfm);
		ActionQueryBase query = getQueryFactory().getActionQuery(move);
		query.setAction(move);
		getDatabase().executeQuery(getUser(), query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void validate() throws DataException {
		super.validate();
		ProcessGroupResolution action = (ProcessGroupResolution) getAction();
		groupResolution = action.getCard();
		List<ObjectId> docs = action.getDocs();
		StringBuilder stringQuery = new StringBuilder();
		//������� ����� sql ���� �������� �������� ����������� GetCard'��
		stringQuery.append("select av_res.card_id \n");
		stringQuery.append("  from card c \n");
		stringQuery.append("join attribute_value av_p on c.card_id = av_p.number_value and av_p.attribute_code = 'JBR_IMPL_ACQUAINT' \n");
		stringQuery.append("join attribute_value av_res on av_res.number_value = av_p.card_id and av_res.attribute_code = 'JBR_DOCB_BYDOC' \n");
		stringQuery.append("join card c_res on c_res.card_id = av_res.card_id \n");
		stringQuery.append("join attribute_value av_rassm on av_rassm.card_id = c.card_id and av_rassm.attribute_code = 'JBR_RASSM_PERSON' \n");
		stringQuery.append("join attribute_value av_res_signer on av_res_signer.card_id = av_res.card_id and av_res_signer.attribute_code = 'JBR_INFD_SGNEX_LINK' \n");
		stringQuery.append("where c.card_id in (" + ObjectIdUtils.numericIdsToCommaDelimitedString(docs) + ") \n");
		stringQuery.append("and c.template_id = 504 \n");
		stringQuery.append("and c_res.status_id <> 303990 \n");
		stringQuery.append("and av_rassm.number_value = av_res_signer.number_value");
		
		List<ObjectId> resolutionList = getJdbcTemplate().query(stringQuery.toString(),
				new RowMapper(){
					@Override
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						return new ObjectId(Card.class, rs.getLong(1));
					}
					
				}
			);
		for(ObjectId resId: resolutionList){
			Card resolution = (Card) loadObject(resId);
			String name = resolution.getAttributeById(Attribute.ID_NAME).getStringValue();
			if(resolution.getAttributeById(RESOLUTION_TEXT_ATTR_ID).isEmpty()){
				throw new DataException("action.attr.in.res.is.empty", 
						new Object[] {resolution.getAttributeById(RESOLUTION_TEXT_ATTR_ID).getName(), name});
			}
			if(resolution.getAttributeById(EXECUTOR_ATTR_ID).isEmpty()){
				throw new DataException("action.attr.in.res.is.empty", 
						new Object[] {resolution.getAttributeById(EXECUTOR_ATTR_ID).getName(), name});
			}
			DateAttribute deadline = (DateAttribute) resolution.getAttributeById(TERM_ATTR_ID);
			if(deadline.getValue().before(new Date())){
				throw new DataException("action.deadline.in.res.is.not.valid", 
						new Object[] {resolution.getAttributeById(TERM_ATTR_ID).getName(), name});
			}
		}
	}
	
	private Card searchPersonalControlCard(ObjectId baseCardId) {
		// ���� �������� "������ ��������" ��������� � ���������� ����������
		final Search search = new Search();
		search.setWords("");
		search.setByAttributes(true);

		search.addCardLinkAttribute(ATTR_PCON_DOC, baseCardId);
		search.addPersonAttribute(ATTR_PCON_PERSON, getUser().getPerson().getId());

		// ������ "������ ��������"
		final List<DataObject> templates = new ArrayList<DataObject>(1);
		templates.add(DataObject.createFromId(TEMPLATE_PERSONAL_CONTROL));
		search.setTemplates(templates);

		// ������� "���� ��������"
		final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		final SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(ATTR_PCON_DATE);
		columns.add(col);
		search.setColumns(columns);

		try {
			final Collection<Card> controlCards = ((SearchResult)execAction(search)).getCards();
			if (controlCards != null && controlCards.size() > 0) {
				if (controlCards.size() > 1) {
					logger.warn("More than one card, \"Personal Control\" links to the order id="
							+ baseCardId);
				}
				return controlCards.iterator().next();
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error("Error search of card \"Person control\":", e);
			return null;
		}
	}
	
	
}
