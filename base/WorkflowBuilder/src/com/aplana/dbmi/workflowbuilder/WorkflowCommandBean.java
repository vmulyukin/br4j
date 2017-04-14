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
package com.aplana.dbmi.workflowbuilder;

import java.util.Collection;
import java.util.Map;

import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.LockableObject;
import com.aplana.dbmi.model.LogAction;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Workflow;
import com.aplana.dbmi.model.WorkflowMove;

/**
 * Command-bean ��� {@link WorkflowController}
 * @author dsultanbekov
 */
public class WorkflowCommandBean extends LockableObjectListCommandBean {
	private Map cardStates;
	
	private Map moves;	
	private Collection logActions;
	private WorkflowMove selectedMove;
	private String selectedMoveKey;
	private LockableObject addedObject;
	private String addedObjectKey;
	
	public String getAddedObjectKey() {
		return addedObjectKey;
	}

	public void setAddedObjectKey(String addedObjectKey) {
		this.addedObjectKey = addedObjectKey;
	}

	public LockableObject getAddedObject() {
		return addedObject;
	}

	public void setAddedObject(LockableObject addedObject) {
		this.addedObject = addedObject;
	}

	public Map getCardStates() {
		return cardStates;
	}

	public void setCardStates(Map cardStates) {
		this.cardStates = cardStates;
	}

	public long getInitialStateId() {
		Workflow w = (Workflow)getSelectedObject();
		if (w == null || w.getInitialState() == null) {
			return WorkflowController.NOT_SELECTED;
		} else {
			return ((Long)w.getInitialState().getId()).longValue();
		}
	}

	public void setInitialStateId(long initialStateId) {
		Workflow w = (Workflow)getSelectedObject();
		if (initialStateId != WorkflowController.NOT_SELECTED) {
			w.setInitialState(CardState.getId(initialStateId));
		} else {
			w.setInitialState(null);
		}
	}

	public Map getMoves() {
		return moves;
	}

	public void setMoves(Map moves) {
		this.moves = moves;
	}

	public Collection getLogActions() {
		return logActions;
	}

	public void setLogActions(Collection logActions) {
		this.logActions = logActions;
	}

	public WorkflowMove getSelectedMove() {
		return selectedMove;
	}

	public void setSelectedMove(WorkflowMove selectedMove) {
		this.selectedMove = selectedMove;
	}

	public String getSelectedMoveKey() {
		return selectedMoveKey;
	}

	public void setSelectedMoveKey(String selectedMoveKey) {
		this.selectedMoveKey = selectedMoveKey;
	}

	public long getSelectedMoveFromStateId() {
		ObjectId id = selectedMove.getFromState();
		return id == null ? WorkflowController.NOT_SELECTED : ((Long)id.getId()).longValue();
	}
	
	public void setSelectedMoveFromStateId(long value) {
		CardState cs = (CardState)cardStates.get(new Long(value));
		if (cs != null) {
			selectedMove.setFromState(cs.getId());
		} else {
			selectedMove.setFromState(null);
		}
	}
	
	public long getSelectedMoveToStateId() {
		ObjectId id = selectedMove.getToState();
		return id == null ? WorkflowController.NOT_SELECTED : ((Long)id.getId()).longValue();
	}
	
	public void setSelectedMoveToStateId(long value) {
		CardState cs = (CardState)cardStates.get(new Long(value));
		if (cs != null) {
			selectedMove.setToState(cs.getId());
		} else {
			selectedMove.setToState(null);
		}
	}
	
	public String getSelectedMoveLogActionId() {
		ObjectId id = selectedMove.getLogAction();
		return id == null ? String.valueOf(WorkflowController.NOT_SELECTED) : (String)id.getId();
	}
	
	public void setSelectedMoveLogActionId(String value) {
		if (String.valueOf(WorkflowController.NOT_SELECTED).equals(value)) {
			selectedMove.setLogAction(null);
		} else {
			selectedMove.setLogAction(new ObjectId(LogAction.class, value));
		}
	}
}
