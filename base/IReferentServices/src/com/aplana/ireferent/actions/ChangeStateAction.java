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
package com.aplana.ireferent.actions;

import java.util.Collection;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.ireferent.IReferentException;
import com.aplana.ireferent.types.WSObject;

public class ChangeStateAction extends CardAction {

    public static final String DEST_STATE_PARAM = "toState";
    public static final String MOVE_PARAM = "workflowMove";
    public static final String OPTIONALLY_PARAM = "optional";

    private ObjectId destinationState = null;
    private ObjectId moveId = null;
    protected boolean isOptional;

    @Override
    public void setParameter(String key, Object value) {
	if (DEST_STATE_PARAM.equals(key)) {
	    destinationState = ObjectIdUtils.getObjectId(CardState.class,
		    (String) value, true);
	} else if (MOVE_PARAM.equals(key)) {
	    moveId = ObjectIdUtils.getObjectId(WorkflowMove.class,
		    (String) value, true);
	} else if (OPTIONALLY_PARAM.equals(key)) {
	    isOptional = Boolean.parseBoolean((String) value);
	} else {
	    super.setParameter(key, value);
	}
    }

    public void doAction(DataServiceBean serviceBean, WSObject object)
	    throws IReferentException {
	Collection<Card> cards = getFilteredCards(serviceBean, object);

	if (!isOptional && cards.isEmpty()) {
	    throw new IReferentException("Cards for processing were not found");
	}

	for (Card card : cards) {
	    WorkflowMove workflowMove = calculateWorkflowMove(serviceBean, card);
	    if (workflowMove == null) {
		throw new IReferentException(
			"It is impossible to change state of card "
				+ card.getId().getId());
	    }
	    changeState(serviceBean, card, workflowMove);
	}
    }

    protected void changeState(DataServiceBean serviceBean, Card card,
	    WorkflowMove workflowMove) throws IReferentException {
	try {
	    LockObject lock = new LockObject(card);
	    serviceBean.doAction(lock);
	    try {
		ChangeState changeStateAction = new ChangeState();
		changeStateAction.setCard(card);
		changeStateAction.setWorkflowMove(workflowMove);
		serviceBean.doAction(changeStateAction);
	    } finally {
		UnlockObject unlock = new UnlockObject(card);
		serviceBean.doAction(unlock);
	    }
	} catch (DataException ex) {
	    throw new IReferentException(ex.getMessage(), ex);
	} catch (ServiceException ex) {
	    throw new IReferentException(ex.getMessage(), ex);
	}
    }

    protected WorkflowMove calculateWorkflowMove(DataServiceBean serviceBean,
	    Card card) throws IReferentException {
	WorkflowMove workflowMove;
	if (moveId == null) {
	    workflowMove = findWorkFlowMoveX(serviceBean, card.getId(),
		    destinationState);
	} else {
	    workflowMove = (WorkflowMove) DataObject.createFromId(moveId);
	}
	return workflowMove;
    }

    private WorkflowMove findWorkFlowMoveX(DataServiceBean serviceBean,
	    ObjectId cardId, ObjectId destState) throws IReferentException {
	if (cardId == null || destState == null)
	    return null;

	try {
	    @SuppressWarnings("unchecked")
	    Collection<WorkflowMove> possibleMoves = serviceBean.listChildren(
		    cardId, WorkflowMove.class);
	    for (WorkflowMove move : possibleMoves) {
		if (destState.equals(move.getToState())) {
		    return move;
		}
	    }
	} catch (DataException ex) {
	    throw new IReferentException(
		    "Unable to find workflow moves for card " + cardId.getId(),
		    ex);
	} catch (ServiceException ex) {
	    throw new IReferentException(
		    "Unable to find workflow moves for card " + cardId.getId(),
		    ex);
	}
	return null;
    }

	public ObjectId getDestinationState() {
		return destinationState;
	}

	public ObjectId getMoveId() {
		return moveId;
	}

	public boolean isOptional() {
		return isOptional;
	}
}
