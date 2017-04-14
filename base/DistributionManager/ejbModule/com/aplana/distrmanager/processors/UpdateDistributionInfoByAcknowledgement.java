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
package com.aplana.distrmanager.processors;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.action.ImportCardFromXml.ImportCard.Result;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.distrmanager.exceptions.PrematureAcknowledgementException;
import com.aplana.dmsi.DMSIException;
import com.aplana.dmsi.card.handling.CardFacade;

public class UpdateDistributionInfoByAcknowledgement extends ProcessorBase {

	private static final ObjectId ackTemplateId = ObjectId.predefined(Template.class, "jbr.gost.ack");

	private static final ObjectId ackParentAttributeId = new ObjectId(BackLinkAttribute.class, "JBR_ACK_PARENT_MSG");
	private static final ObjectId ackTypeAttributeId = ObjectId.predefined(ListAttribute.class, "jbr.gost.ack.type");
	private static final ObjectId ackErrorCodeAttributeId = ObjectId.predefined(StringAttribute.class,
			"jbr.gost.ack.errorCode");

	private static final ObjectId ackTypeReceivedValueId = ObjectId.predefined(ReferenceValue.class,
			"jbr.gost.ack.type.recieved");
	private static final ObjectId ackTypeRegisteredValueId = ObjectId.predefined(ReferenceValue.class,
			"jbr.gost.ack.type.registered");
	
	private static final ObjectId worklowMoveRegId = ObjectId.predefined(WorkflowMove.class, "jbr.distributionItem.registered");
	private static final ObjectId worklowMoveRegFromSentId = ObjectId.predefined(WorkflowMove.class, "jbr.distributionItem.registeredFromSent");
	private static final ObjectId worklowMoveDeliveredId = ObjectId.predefined(WorkflowMove.class, "jbr.distributionItem.delivered");
	
	private static final ObjectId worklowMoveRefusedId =ObjectId.predefined(WorkflowMove.class, "jbr.distributionItem.refused");
	private static final ObjectId worklowMoveRefusedFromSentId = ObjectId.predefined(WorkflowMove.class, "jbr.distributionItem.refusedFromSent");
	private static final ObjectId worklowMoveNotDeliveredId = ObjectId.predefined(WorkflowMove.class, "jbr.distributionItem.notDelivered");
	
	private static final ObjectId stateSentId = ObjectId.predefined(CardState.class, "sent");
	private static final ObjectId stateRegisteredId = ObjectId.predefined(CardState.class, "jbr.sendInfo.registered");
	private static final ObjectId stateRefusedId = ObjectId.predefined(CardState.class, "jbr.sendInfo.refused");

	private static Map<ObjectId, ObjectId[]> blockedStatesByAckType;
	//private static Map<ObjectId, ObjectId> successWorkflowMovesByAckType;
	//private static Map<ObjectId, ObjectId> failWorkflowMovesByAckType;

	static {
		blockedStatesByAckType = new HashMap<ObjectId, ObjectId[]>(1);
		/*blockedStatesByAckType.put(ackTypeRegisteredValueId, new ObjectId[] { ObjectId.predefined(CardState.class,
				"sent") });*/
		/*successWorkflowMovesByAckType = new HashMap<ObjectId, ObjectId>(2);
		successWorkflowMovesByAckType.put(ackTypeReceivedValueId, ObjectId.predefined(WorkflowMove.class,
				"jbr.distributionItem.delivered"));
		successWorkflowMovesByAckType.put(ackTypeRegisteredValueId, ObjectId.predefined(WorkflowMove.class,
				"jbr.distributionItem.registered"));
		successWorkflowMovesByAckType.put(ackTypeRegisteredValueId, ObjectId.predefined(WorkflowMove.class,
				"jbr.distributionItem.registeredFromSent"));
		
		failWorkflowMovesByAckType = new HashMap<ObjectId, ObjectId>(2);
		failWorkflowMovesByAckType.put(ackTypeReceivedValueId, ObjectId.predefined(WorkflowMove.class,
				"jbr.distributionItem.notDelivered"));
		failWorkflowMovesByAckType.put(ackTypeRegisteredValueId, ObjectId.predefined(WorkflowMove.class,
				"jbr.distributionItem.refused"));
		failWorkflowMovesByAckType.put(ackTypeRegisteredValueId, ObjectId.predefined(WorkflowMove.class,
				"jbr.distributionItem.refusedFromSent"));*/
	}

	private DataServiceFacade serviceFacade;

	@Override
	public Object process() throws DataException {
		DataServiceFacade serviceBean = getDataServiceBean();
		Result res = (Result) getResult();
		if (!res.isResultSuccessful()) {
			return res;
		}

		ObjectId cardId = res.getCardId();
		CardFacade ackCardFacade = new CardFacade(serviceBean, cardId);

		try {
			if (!ackTemplateId.equals(ackCardFacade.getTemplate())) {
				return res;
			}
			ObjectId[] parentCardIds = (ObjectId[]) ackCardFacade.getAttributeValue(ackParentAttributeId);
			ObjectId ackType = ((ReferenceValue) ackCardFacade.getAttributeValue(ackTypeAttributeId)).getId();
			String errorCode = (String) ackCardFacade.getAttributeValue(ackErrorCodeAttributeId);
			boolean isResultSuccessful = isErrorCodeSuccessful(errorCode);
			for (ObjectId parentCardId : parentCardIds) {
				CardFacade parentCardFacade = new CardFacade(serviceBean, parentCardId);
				ObjectId parentCardStateId = parentCardFacade.getCardState();
				List<ObjectId> blockedStates = getBlockedStatesByAckType(ackType);
				if (blockedStates.contains(parentCardStateId)) {
					throw new PrematureAcknowledgementException();
				}
				if ( !(ackType.equals(ackTypeReceivedValueId) && 
						(parentCardStateId.equals(stateRegisteredId) || parentCardStateId.equals(stateRefusedId))) ) {
					ObjectId worklowMoveId = getWorkflowMoveByAckType(ackType, isResultSuccessful, parentCardStateId);
					changeState(parentCardFacade, worklowMoveId);
				}
			}
		} catch (RuntimeException ex) {
			res.getStatusDescription().setResult(ex.getMessage());
			res.getStatusDescription().setStatusCode(Long.valueOf(-1));
		} catch (DMSIException ex) {
			res.getStatusDescription().setResult(ex.getMessage());
			res.getStatusDescription().setStatusCode(Long.valueOf(-1));
		}

		return res;
	}

	private boolean isErrorCodeSuccessful(String errorCode) {
		try {
			Long errorCodeValue = Long.valueOf(errorCode);
			return Long.valueOf(0).equals(errorCodeValue);
		} catch (NumberFormatException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Value [" + errorCode + "] is not number. Acknowledgement was defined as fail description");
			}
			return false;
		}
	}

	private List<ObjectId> getBlockedStatesByAckType(ObjectId ackType) {
		ObjectId[] blockedStates = blockedStatesByAckType.get(ackType);
		if (blockedStates == null) {
			blockedStates = new ObjectId[] {};
		}
		return Arrays.asList(blockedStates);
	}

	private ObjectId getWorkflowMoveByAckType(ObjectId ackType, boolean isSuccessful, ObjectId parentCardStateId) {
		ObjectId worklowMoveId = null;
		if (isSuccessful) {
			if (ackType.equals(ackTypeRegisteredValueId)) {
				if ( parentCardStateId.equals(stateSentId) )
					worklowMoveId = worklowMoveRegFromSentId;
				else
					worklowMoveId = worklowMoveRegId;
			} else
			if (ackType.equals(ackTypeReceivedValueId)) {
				worklowMoveId = worklowMoveDeliveredId;
			}
		} else {
			if (ackType.equals(ackTypeRegisteredValueId)) {
				if ( parentCardStateId.equals(stateSentId) )
					worklowMoveId = worklowMoveRefusedFromSentId;
				else
					worklowMoveId = worklowMoveRefusedId;
			} else
			if (ackType.equals(ackTypeReceivedValueId)) {
				worklowMoveId = worklowMoveNotDeliveredId;
			}
		}
		if (worklowMoveId == null) {
			throw new IllegalStateException("WorkflowMove is not defined for [" + ackType.getId() + "] and "
					+ (isSuccessful ? "successful result" : "failed result"));
		}
		return worklowMoveId;
	}

	private DataServiceFacade getDataServiceBean() throws DataException {
		if (this.serviceFacade == null) {
			serviceFacade = new DataServiceFacade();
			serviceFacade.setUser(getSystemUser());
			serviceFacade.setDatabase(getDatabase());
			serviceFacade.setQueryFactory(getQueryFactory());
		}
		return this.serviceFacade;
	}

	private void changeState(CardFacade cardFacade, ObjectId moveId) throws DataException, DMSIException {
		DataServiceFacade dataService = getDataServiceBean();
		WorkflowMove workflowMove = (WorkflowMove) dataService.getById(moveId);
		if (cardFacade.getCardState().equals(workflowMove.getToState())) {
			if (logger.isInfoEnabled()) {
				logger.info("The card [" + cardFacade.getCardId() + "] is in [" + cardFacade.getCardState()
						+ "] state yet");
			}
			return;
		}

		Card card = (Card) DataObject.createFromId(cardFacade.getCardId());
		LockObject lock = new LockObject(card);
		dataService.doAction(lock);
		try {
			ChangeState changeStateAction = new ChangeState();
			changeStateAction.setCard(card);
			changeStateAction.setWorkflowMove(workflowMove);
			dataService.doAction(changeStateAction);
		} finally {
			UnlockObject unlock = new UnlockObject(card);
			dataService.doAction(unlock);
		}
	}
}
