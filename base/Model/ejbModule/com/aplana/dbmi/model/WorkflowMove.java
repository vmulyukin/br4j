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
package com.aplana.dbmi.model;

/**
 * Class representing one step in card's {@link Workflow}.
 * Defines start and end {@link CardState statuses} of card in a single workflow step,
 * captions for buttons in GUI used for corresponding card state changes, and several
 * additional parameters of workflow move actions.
 * @author dsultanbekov
 */
public class WorkflowMove extends DataObject implements NamedObject{
	private static final long serialVersionUID = 4L;

	private ObjectId fromState;
	private ObjectId toState;
	private boolean needConfirmation;
	private boolean closeCard;
	private int applyDigitalSignatureOnMove;
	private ObjectId logAction;
	private LocalizedString name;
	private LocalizedString defaultName;	
	private LocalizedString confirmation;

	/**
	 * Default constructor
	 */
	public WorkflowMove() {
		name = new LocalizedString();
		confirmation = new LocalizedString();
		defaultName = new LocalizedString();
	}

    public void setId(ObjectId id) {
        super.setId(id);
        if (id instanceof ObjectIdAndName)
            setName(((ObjectIdAndName) id).getName());
    }
    
    public void setId(String predefinedKey) {
		this.setId(ObjectId.workflowMove(predefinedKey));
	}
	
	/**
	 * Gets identifier of {@link LogAction} used in {@link LogEntry system journals}
	 * to log information about this WorkflowMove.
	 * @return identifier of log action
	 */
	public ObjectId getLogAction() {
		return logAction;
	}

	/**
	 * Sets identifier of {@link LogAction} used in {@link LogEntry system journals}
	 * to log information about this WorkflowMove. 
	 * @param logActionId identifier of log action
	 */
	public void setLogAction(ObjectId logActionId) {
		this.logAction = logActionId;
	}
	
	/**
	 * Checks if GUI should asks user for confirmation to perform this workflow move.
	 * @return true if confirmation is required, false otherwise.
	 */
	public boolean isNeedConfirmation() {
		return needConfirmation;
	}

	/**
	 * Sets isNeedConfirmation flag on this workflow move object.
	 * isNeedConfirmation flag is used to determine if GUI should asks user
	 * for confirmation to perform this workflow move.
	 * @param needConfirmation desired value of isNeedConfirmation flag
	 */
	public void setNeedConfirmation(boolean needConfirmation) {
		this.needConfirmation = needConfirmation;
	}

	public int getApplyDigitalSignatureOnMove() {
		return applyDigitalSignatureOnMove;
	}

	public void setApplyDigitalSignatureOnMove(int applyDigitalSignatureOnMove) {
		this.applyDigitalSignatureOnMove = applyDigitalSignatureOnMove;
	}

	/**
	 * Gets identifier of {@link CardState}, card should have to perform this workflow move
	 * @return identifier of {@link CardState}, card should have to perform this workflow move
	 */
	public ObjectId getFromState() {
		return fromState;
	}

	/**
	 * Sets identifier of {@link CardState}, card should have to perform this workflow move
	 * @param fromState desired identifier of 'from' card state
	 */
	public void setFromState(ObjectId fromState) {
		this.fromState = fromState;
	}

	/**
	 * Gets identifier of target {@link CardState} of this workflow move.
	 * It is a state card will have after performing this workflow move.
	 * @return identifier of target {@link CardState} of this workflow move
	 */
	public ObjectId getToState() {
		return toState;
	}

	/**
	 * Sets identifier of target {@link CardState} of this workflow move.
	 * It is a state card will have after performing this workflow move.
	 * @param toState identifier of target {@link CardState} of this workflow move
	 */
	public void setToState(ObjectId toState) {
		this.toState = toState;
	}

	/**
	 * Gets name of move
	 * @return localized name of move
	 */
	public LocalizedString getName() {
		return name;
	}

    public void setName(LocalizedString name) {
        this.name = name;
    }

	/**
	 * Gets text of confirmation to be displayed to user when he tries to perform this move
	 * @return localized text of confirmation 
	 */
	public LocalizedString getConfirmation() {
		return confirmation;
	}

    public void setConfirmation(LocalizedString confirmation) {
        this.confirmation = confirmation;
    }

	/**
	 * Gets default name of move
	 * @return default name of move
	 * @see CardState#getDefaultMoveName()
	 * @see #getMoveName()
	 */
	public LocalizedString getDefaultName() {
		return defaultName;
	}

    public void setDefaultName(LocalizedString defaultName) {
        this.defaultName = defaultName;
    }

	/**
	 * Gets name of move used as a caption for buttons in GUI
	 * @return result of {@link #getName()#getValue()} or {@link #getDefaultName()#getValue()} if 
	 * first one is empty
	 */
	public String getMoveName() {
		String st = getName().getValue();
		if (st == null || "".equals(st.trim())) {
			st = getDefaultName().getValue();
		}
		return st;
	}

	/**
	 * Checks if GUI should close current card after this workflow move has been performed.
	 * @return true if the card will be closed, false otherwise.
	 */
	public boolean isCloseCard() {
		return closeCard;
	}

	/**
	 * Sets isCloseCard flag on this workflow move object.
	 * isCloseCard flag is used to determine if GUI should close the current
	 * card after this workflow move has been performed.
	 * @param closeCard desired value of isCloseCard flag
	 */
	public void setCloseCard(boolean closeCard) {
		this.closeCard = closeCard;
	}

}
