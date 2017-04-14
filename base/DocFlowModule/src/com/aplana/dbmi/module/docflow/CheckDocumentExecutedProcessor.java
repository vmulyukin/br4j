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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.FetchChildrenCards;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.ObjectAction;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.ParametrizedProcessor;

@SuppressWarnings("unused")
public class CheckDocumentExecutedProcessor extends ParametrizedProcessor {
	private static final long serialVersionUID = 1L;

	public static final String PARAM_DOCUMENT_LINK_ATTR = "documentLinkAttr";
	public static final String PARAM_DOCUMENT_EXCEPT_TEMPLATE = "documentExceptTemplate";

	private static final ObjectId parentResolutionAttrId = ObjectId.predefined(CardLinkAttribute.class, "jbr.rimp.byrimp");
	private static final ObjectId mainDocId = ObjectId.predefined(CardLinkAttribute.class, "jbr.main.doc");
	private static final ObjectId resolutionsAttrId = ObjectId.predefined(BackLinkAttribute.class, "jbr.resolutions");
	private static final ObjectId reportsAttrId = ObjectId.predefined(BackLinkAttribute.class, "jbr.reports");
	//private static final ObjectId resolutionExecutingStateId = ObjectId.predefined(CardState.class, "jbr.commission.executing");
	private static final ObjectId examsAttrId = ObjectId.predefined(CardLinkAttribute.class, "jbr.exam.set");
	private static final ObjectId examAssignedStateId = ObjectId.predefined(CardState.class, "jbr.exam.assigned");
	private static final ObjectId examWaitingStateId = ObjectId.predefined(CardState.class, "jbr.exam.waiting");
	private static final ObjectId examAssistantWaitingStateId = ObjectId.predefined(CardState.class, "boss.assistant");
	private static final ObjectId incomingExecutingDoneWfmId = ObjectId.predefined(WorkflowMove.class, "jbr.incoming.execution.done");
	private static final ObjectId incomingReadyToWriteOffId = ObjectId.predefined(WorkflowMove.class, "jbr.incoming.execution.done");
	private static final ObjectId interndocExecutingDoneWfmId = ObjectId.predefined(WorkflowMove.class, "jbr.interndoc.execution.done");
	private static final ObjectId interndocReadyToWriteOffId = ObjectId.predefined(WorkflowMove.class, "interndoc.done.ready-to-write-off");
	private static final ObjectId ordExecutingDoneWfmId = ObjectId.predefined(WorkflowMove.class, "jbr.ord.execution.done");
	private static final ObjectId ordReadyToWriteOffId = ObjectId.predefined(WorkflowMove.class, "jbr.ord.done.ready-to-write-off");
	private static final ObjectId incomingTemplateId = ObjectId.predefined(Template.class, "jbr.incoming");
	private static final ObjectId interndocTemplateId = ObjectId.predefined(Template.class, "jbr.interndoc");
	private static final ObjectId ordTemplateId = ObjectId.predefined(Template.class, "jbr.ord");
	private static final ObjectId npaTemplateId = ObjectId.predefined(Template.class, "jbr.npa");
	private static final ObjectId citreqTemplateId = ObjectId.predefined(Template.class, "jbr.citizenrequest");
	private static final ObjectId resolutionTemplateId = ObjectId.predefined(Template.class, "jbr.resolution");
	private static final ObjectId considerationStateId = ObjectId.predefined(CardState.class, "consideration");
	private static final ObjectId executionStateId = ObjectId.predefined(CardState.class, "execution");

	private static final ObjectId incomingExecutionWfmId = ObjectId.predefined(WorkflowMove.class, "jbr.incoming.consideration.execution");
	private static final ObjectId interndocExecutionWfmId = ObjectId.predefined(WorkflowMove.class, "jbr.interndoc.consideration.execution");
	private static final ObjectId commissionCancelExecutionWfmId = ObjectId.predefined(WorkflowMove.class, "jbr.commission.cancel.execution");
	private static final ObjectId commissionDeletedWfmId = ObjectId.predefined(WorkflowMove.class, "jbr.commission.drop");
	private static final ObjectId attrId_OnCont = ObjectId.predefined(ListAttribute.class, "jbr.incoming.oncontrol");
	private static final ObjectId valId_Yes = ObjectId.predefined(ReferenceValue.class, "jbr.incoming.control.yes");
	private static final ObjectId valId_No = ObjectId.predefined(ReferenceValue.class, "jbr.incoming.control.no");
	private static final List <ObjectId> reportControlStateList = Arrays.asList(
			ObjectId.predefined(CardState.class, "draft"),
			ObjectId.predefined(CardState.class, "consideration"),
			ObjectId.predefined(CardState.class, "done"),
			ObjectId.predefined(CardState.class, "sent"),
			ObjectId.predefined(CardState.class, "jbr.report.inprogress"),
			ObjectId.predefined(CardState.class, "jbr.visa.assistent")
			
	);
	private static final List <ObjectId> reportNotControlStateList = Arrays.asList(
			ObjectId.predefined(CardState.class, "draft"),
			ObjectId.predefined(CardState.class, "sent"),
			ObjectId.predefined(CardState.class, "jbr.report.inprogress"),
			ObjectId.predefined(CardState.class, "jbr.visa.assistent")
			
	);
	private static final List <ObjectId> resolutionNotExecutedStateList = Arrays.asList(
			ObjectId.predefined(CardState.class, "draft"),
			ObjectId.predefined(CardState.class, "execution"),
			ObjectId.predefined(CardState.class, "agreement")
	);
	
	private ObjectId documentLinkAttrId = null;
	private Set<ObjectId> documentExceptTemplates = new HashSet();
	
	@SuppressWarnings("unchecked")
	@Override
	public Object process() throws DataException {
		Card doc = null;
		Card resol = null;
		Action action = getAction();
		if ((action instanceof ChangeState) && 
				(((ChangeState)action).getWorkflowMove().getId().equals(commissionCancelExecutionWfmId)) ||
				((ChangeState)action).getWorkflowMove().getId().equals(commissionDeletedWfmId))
		{
			resol = ((ChangeState)action).getCard();
			doc = getParent(resol.getId(), mainDocId);
		} else {
			doc = getDocumentCard();
		}

		if (doc != null) {
			boolean unlock = false;
			if (doc.getId() != null) {
				execAction(new LockObject(doc));
				unlock = true;
			}
			try {
				final CardLinkAttribute examsAttr = (CardLinkAttribute)doc.getAttributeById(examsAttrId);
				List<Card> resolutionsList = CardUtils.execListProject(resolutionsAttrId, doc.getId(), 
						getQueryFactory(), getDatabase(), getSystemUser());
				boolean isWaitingExamExists = false;
				
				if(examsAttr!=null){				
					final Search examsSearch = new Search();
					examsSearch.setByCode(true);
					examsSearch.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(examsAttr.getIdsLinked()));
					final List<SearchResult.Column> examsColumns = new ArrayList<SearchResult.Column>();
					examsColumns.add( CardUtils.createColumn(Card.ATTR_STATE));
					examsSearch.setColumns(examsColumns);
					SearchResult examsResult = (SearchResult) execAction(examsSearch);
	
					Set<ObjectId> examWaitingStates = new HashSet<ObjectId>();
					examWaitingStates.add(examWaitingStateId);
					examWaitingStates.add(examAssignedStateId);
					examWaitingStates.add(examAssistantWaitingStateId);
					
					
					List<Card> examCardList = examsResult.getCards();
					for (int i = 0; i < examCardList.size(); i++) {
						Card examCard = examCardList.get(i);
						if (examWaitingStates.contains(examCard.getState())) {
							isWaitingExamExists = true;
						}
					}
				}

				if (!isWaitingExamExists&&doc.getTemplate()!=null&&!documentExceptTemplates.contains(doc.getTemplate())) {
					final Search search = new Search();
					search.setByCode(true);
					search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(ObjectIdUtils.cardsToObjectIdsSet(resolutionsList)));
					final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
					columns.add( CardUtils.createColumn(Card.ATTR_STATE));
					search.setColumns(columns);
					SearchResult resolutionsResult = (SearchResult) execAction(search);
					
					boolean notExecutedResolutionExists = false;
					List<Card> resolutionCardList = resolutionsResult.getCards();
					if ((action instanceof ChangeState) && 
							(((ChangeState)action).getWorkflowMove().getId().equals(commissionCancelExecutionWfmId)) ||
							((ChangeState)action).getWorkflowMove().getId().equals(commissionDeletedWfmId))
					{
						boolean notExecutedReportExists = false;
						for (Card resolutionCard : resolutionCardList) {
							if (resolutionNotExecutedStateList.contains(resolutionCard.getState())) {
								notExecutedResolutionExists = true; 
								break;
							}
							List<Card> reportCardList;
							if(BackLinkAttribute.class.isAssignableFrom(reportsAttrId.getType())) {
								reportCardList = CardUtils.execListProject(reportsAttrId, resolutionCard.getId(), 
										getQueryFactory(), getDatabase(), getUser());
							} else {
								reportCardList = loadReports(resolutionCard.getId());
							}
							ListAttribute onControl = (ListAttribute) doc.getAttributeById(attrId_OnCont);
							if (onControl != null && onControl.getValue() != null) {
								if (onControl.getValue().getId().equals(valId_Yes) && reportCardList != null) {
									for (Card reportCard : reportCardList) {
										if (reportControlStateList.contains(reportCard.getState())) {
											notExecutedReportExists = true;
											break;
										}
									}
								}
								if (onControl.getValue().getId().equals(valId_No) && reportCardList != null) {
									for (Card reportCard : reportCardList) {
										if (reportNotControlStateList.contains(reportCard.getState())) {
											notExecutedReportExists = true;
											break;
										}
									}	
								}
							}

						}
						if (!notExecutedResolutionExists && !notExecutedReportExists) {
							if (doc.getState().equals(executionStateId)){
								moveToExecuted(doc);
							}else{
								logger.error("Couldn't execute workflow move for document: " + doc.getId().getId() + " template: " + doc.getTemplate().getId() + ", because it is not in "+executionStateId.getId()+" state");
							}
						}
						
					} else {
						for (Card resolutionCard : resolutionCardList) {
							if (resolutionNotExecutedStateList.contains(resolutionCard.getState())) {
								notExecutedResolutionExists = true; 
								break;
							}
						}
						if (!notExecutedResolutionExists) {
							if (doc.getState().equals(considerationStateId)) {
								if (moveToExecution(doc)) {
									moveToExecuted(doc);
								}
							} else {
								if (doc.getState().equals(executionStateId)){
									moveToExecuted(doc);
								}else{
									logger.error("Couldn't execute workflow move for document: " + doc.getId().getId() + " template: " + doc.getTemplate().getId() + ", because it is not in "+executionStateId.getId()+" state");
								}
							}
						}
					}
				}
			}
			finally {
				if (unlock) {
					execAction( new UnlockObject(doc.getId()));
				}
			}
		}
		return null;
	}

	protected boolean moveToExecution(Card doc) throws DataException {
		WorkflowMove wfm = null;
		if (incomingTemplateId.equals(doc.getTemplate()) ||
				citreqTemplateId.equals(doc.getTemplate())) {
			ObjectQueryBase query = getQueryFactory().getFetchQuery(WorkflowMove.class);
			query.setAccessChecker(null);
			query.setId(incomingExecutionWfmId);
			wfm = (WorkflowMove)getDatabase().executeQuery(getSystemUser(), query);		
		}
		else if (interndocTemplateId.equals(doc.getTemplate())) {
			ObjectQueryBase query = getQueryFactory().getFetchQuery(WorkflowMove.class);
			query.setAccessChecker(null);
			query.setId(interndocExecutionWfmId);
			wfm = (WorkflowMove)getDatabase().executeQuery(getSystemUser(), query);		
		}

		if (wfm != null) {
			final ChangeState move = new ChangeState();
			move.setWorkflowMove( wfm );
			move.setCard(doc);
			execAction(move);
			logger.debug( "[CheckDocumentExecuted:" + doc.getId().getId() 
					+ "] Document proceeded to the next stage by WorkFlowMove " 
					+ wfm.getId() + " '"+ wfm.getMoveName() + "'" 
					+ ", fromStatus=" + wfm.getFromState()
					+ ", toStatus=" + wfm.getToState()
				);
			return true;
		}
		else {
			logger.warn("Couldn't find right workflow move for send document to execution: " + doc.getId().getId() + " template: " + doc.getTemplate().getId() + ". Exiting.");
			return false;
		}
	}

	protected boolean moveToExecuted(Card doc) throws DataException {
		WorkflowMove wfm = null;
		if (incomingTemplateId.equals(doc.getTemplate()) || citreqTemplateId.equals(doc.getTemplate())) {
			ObjectQueryBase query = getQueryFactory().getFetchQuery(WorkflowMove.class);
			query.setAccessChecker(null);
			query.setId(incomingExecutingDoneWfmId);
			wfm = (WorkflowMove)getDatabase().executeQuery(getSystemUser(), query);		
		}
		else if (interndocTemplateId.equals(doc.getTemplate())) {
			ObjectQueryBase query = getQueryFactory().getFetchQuery(WorkflowMove.class);
			query.setAccessChecker(null);
			query.setId(interndocExecutingDoneWfmId);
			wfm = (WorkflowMove)getDatabase().executeQuery(getSystemUser(), query);		
		}
		else if (ordTemplateId.equals(doc.getTemplate()) || npaTemplateId.equals(doc.getTemplate())) {
			ObjectQueryBase query = getQueryFactory().getFetchQuery(WorkflowMove.class);
			query.setAccessChecker(null);
			query.setId(ordExecutingDoneWfmId);
			wfm = (WorkflowMove)getDatabase().executeQuery(getSystemUser(), query);		
		}

		if (wfm != null) {
			final ChangeState move = new ChangeState();
			move.setWorkflowMove( wfm );
			move.setCard(doc);
			execAction(move);
			logger.debug( "[CheckDocumentExecuted:" + doc.getId().getId() 
					+ "] Document proceeded to the next stage by WorkFlowMove " 
					+ wfm.getId() + " '"+ wfm.getMoveName() + "'" 
					+ ", fromStatus=" + wfm.getFromState()
					+ ", toStatus=" + wfm.getToState()
				);
			return true;
		}
		else {
			logger.warn("Couldn't find right workflow move for done document executing: " + doc.getId().getId() + " template: " + doc.getTemplate().getId() + ". Exiting.");
			return false;
		}
	}
	
	private ObjectId getCardId() {
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
	
	public void setParameter(String name, String value) {
		if (PARAM_DOCUMENT_LINK_ATTR.equalsIgnoreCase(name)) {
			documentLinkAttrId = IdUtils.smartMakeAttrId(value, BackLinkAttribute.class);
		} else if (PARAM_DOCUMENT_EXCEPT_TEMPLATE.equalsIgnoreCase(name)) {
			ObjectId templateId = IdUtils.tryFindPredefinedObjectId(value, Template.class); 
			if (documentExceptTemplates==null){
				documentExceptTemplates = new HashSet();
			}
			documentExceptTemplates.add(templateId);
		} else {
			super.setParameter(name, value);
		}
	}
	
	private Card getDocumentCard() throws DataException {
		boolean isChildResolution = false;
		Card objectCard = loadCard(getCardId());
		if (resolutionTemplateId.equals(objectCard.getTemplate())) {
			final CardLinkAttribute cla = objectCard.getCardLinkAttributeById(parentResolutionAttrId);
			Card parentResolution = null;
			if(cla != null && !cla.isEmpty()) {
				parentResolution = loadCard(cla.getIdsLinked().get(0));
			}
			if (parentResolution != null) {
				isChildResolution = true;
			}
			if(cla.getIdsLinked().size() > 1) {
				logger.info("Attribute " + parentResolutionAttrId + " in card " + objectCard.getId().getId() + " contain more than 1 value");
			}
		}
		if (isChildResolution) {
			return null;
		}
		else if (documentLinkAttrId != null) {
			if(BackLinkAttribute.class.equals(documentLinkAttrId.getType())) {
				final List<Card> list = loadProjects(objectCard.getId(), documentLinkAttrId);
				if (list != null && !list.isEmpty()) {
					return loadCard(list.get(0).getId());
				}
				return null;
			} else if(CardLinkAttribute.class.equals(documentLinkAttrId.getType())) {
				final CardLinkAttribute linkAttr = objectCard.getCardLinkAttributeById(documentLinkAttrId);
				if (linkAttr != null && linkAttr.getIdsLinked() != null && linkAttr.getIdsLinked().size() > 0) {
					if(linkAttr.getIdsLinked().size() > 1) {
						logger.info("Attribute " + documentLinkAttrId + " in card " + objectCard.getId().getId() + " contain more than 1 value");
					}
					return loadCard(linkAttr.getIdsLinked().get(0));
				}
				return null;
			} else {
				logger.error("Attribute " + documentLinkAttrId + " is not LinkAttribute");
				return null;
			}
			
		}
		else {
			return objectCard;
		}
	}

	private Object execAction(Action action) throws DataException {
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		return getDatabase().executeQuery(getSystemUser(), query);
	}
	
	private Card loadCard(ObjectId cardId) throws DataException{
		final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(Card.class);
		cardQuery.setAccessChecker(null);
		cardQuery.setId(cardId);
		return (Card) getDatabase().executeQuery(getSystemUser(), cardQuery);
	}

	private List<Card> loadProjects(ObjectId cardId, ObjectId linkAttrId) throws DataException {
		final ListProject listProject = new ListProject();
		listProject.setCard(cardId);
		listProject.setAttribute(linkAttrId);
	
		final List<Card> list = CardUtils.execSearchCards(listProject, getQueryFactory(), getDatabase(), getSystemUser());
		return (list == null || list.isEmpty()) ? null : list;
	}

	private Card getParent(ObjectId cardId, ObjectId linkAttrId) throws DataException {
		final FetchChildrenCards action = new FetchChildrenCards();
		action.setCardId(cardId);
		action.setLinkAttributeId(linkAttrId);
		action.setReverseLink(false);
		List<Card> list = CardUtils.execSearchCards(action, getQueryFactory(),
				getDatabase(), getSystemUser());
		if (list != null && !list.isEmpty()) {
			return loadCard(list.get(0).getId());
		}  
			return null;
	}
	
	@SuppressWarnings("unchecked")
	private List<Card> loadReports(ObjectId cardId) throws DataException {
		Card card = loadCard(cardId);
		final CardLinkAttribute reportsAttr = (CardLinkAttribute)card.getAttributeById(reportsAttrId);
		if (reportsAttr == null) return null;
		final Search reportsSearch = new Search();
		reportsSearch.setByCode(true);
		reportsSearch.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(reportsAttr.getIdsLinked()));
		final List<SearchResult.Column> reportsColumns = new ArrayList<SearchResult.Column>();
		reportsColumns.add( CardUtils.createColumn(Card.ATTR_STATE));
		reportsSearch.setColumns(reportsColumns);
		SearchResult reportsResult = (SearchResult) execAction(reportsSearch);

		List<Card> reportCardList = reportsResult.getCards();
		return (reportCardList == null || reportCardList.isEmpty()) ? null : reportCardList;
	}
	
	protected boolean moveToWriteOff(Card doc) throws DataException {
		WorkflowMove wfm = null;
		if (incomingTemplateId.equals(doc.getTemplate()) || citreqTemplateId.equals(doc.getTemplate())) {
			ObjectQueryBase query = getQueryFactory().getFetchQuery(WorkflowMove.class);
			query.setAccessChecker(null);
			query.setId(incomingExecutingDoneWfmId);
			wfm = (WorkflowMove)getDatabase().executeQuery(getSystemUser(), query);		
		}
		else if (interndocTemplateId.equals(doc.getTemplate())) {
			ObjectQueryBase query = getQueryFactory().getFetchQuery(WorkflowMove.class);
			query.setAccessChecker(null);
			query.setId(interndocExecutingDoneWfmId);
			wfm = (WorkflowMove)getDatabase().executeQuery(getSystemUser(), query);		
		}
		else if (ordTemplateId.equals(doc.getTemplate()) || npaTemplateId.equals(doc.getTemplate())) {
			ObjectQueryBase query = getQueryFactory().getFetchQuery(WorkflowMove.class);
			query.setAccessChecker(null);
			query.setId(ordReadyToWriteOffId);
			wfm = (WorkflowMove)getDatabase().executeQuery(getSystemUser(), query);		
		}

		if (wfm != null) {
			final ChangeState move = new ChangeState();
			move.setWorkflowMove( wfm );
			move.setCard(doc);
			execAction(new LockObject(doc));
			try {
				execAction(move);
			} finally {
				execAction(new UnlockObject(doc));
			}
			logger.debug( "[CheckDocumentExecuted:" + doc.getId().getId() 
					+ "] Document proceeded to the next stage by WorkFlowMove " 
					+ wfm.getId() + " '"+ wfm.getMoveName() + "'" 
					+ ", fromStatus=" + wfm.getFromState()
					+ ", toStatus=" + wfm.getToState()
				);
			return true;
		}
		else {
			logger.warn("Couldn't find right workflow move for done document executing: " + doc.getId().getId() + " template: " + doc.getTemplate().getId() + ". Exiting.");
			return false;
		}
	}


}