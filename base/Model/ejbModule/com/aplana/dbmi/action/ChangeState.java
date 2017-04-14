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
package com.aplana.dbmi.action;

import java.util.HashMap;
import java.util.Map;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.WorkflowMove;

/**
 * Action used to change {@link com.aplana.dbmi.model.CardState state} of
 * given {@link Card card}. To be more precise, this action performs {@link WorkflowMove one step}
 * of card's {@link com.aplana.dbmi.model.Workflow}.
 * <br>
 * It is necessary that value returned by {@link WorkflowMove#getFromState()} method of
 * corresponding {@link WorkflowMove} object was equal to the 
 * {@link com.aplana.dbmi.model.CardState state} of
 * given card.
 * <br>
 * Please note that access to various {@link WorkflowMove} objects could be restricted
 * to specific group of users in CARD_ACCESS table (see {@link com.aplana.dbmi.model.CardAccess}).
 * If value of {@link #getWorkflowMove() workflowMove} property of ChangeState instance
 * will be set to {@link WorkflowMove} which is not accessible for current user, then
 * {@link com.aplana.dbmi.service.DataException} will be thrown during processing of this action.
 * <br>
 * Additionally execution of this action could fail with {@link com.aplana.dbmi.service.DataException}
 * if given card object misses some of mandatory attributes.
 * <br> 
 * Returns null as result 
 */
public class ChangeState implements ObjectAction<Void>
{
	private static final long serialVersionUID = 3L;
	private Card card;
	private WorkflowMove workflowMove;
	private boolean lastDialogOk = false;
	private Map<String, Object> actionContext = new HashMap<String, Object>();

	/**
	 * Gets card object, state of which needs to be changed
	 * @return card object, state of which needs to be changed
	 */
	public Card getCard() {
		return card;
	}

	/**
	 * Sets card object, state of which needs to be changed
	 * @param card card object, state of which needs to be changed
	 */
	public void setCard(Card card) {
		this.card = card;
	}

	/**
	 * @see Action#getResultType()
	 */
	public Class<?> getResultType()
	{
		return null;
	}

	/**
	 * @see ObjectAction#getObjectId()
	 */
	public ObjectId getObjectId() {
		return getCard().getId();
	}

	/**
	 * Returns identifier of {@link WorkflowMove} object to be executed
	 * on given {@link Card} object
	 * @return identifier of {@link WorkflowMove} object to be executed
	 */
	public WorkflowMove getWorkflowMove() {
		return workflowMove;
	}

	/**
	 * Sets identifier of {@link WorkflowMove} object to be executed
	 * on given {@link Card} object. 
	 * It is necessary that value returning by {@link WorkflowMove#getFromState()} method of
	 * corresponding {@link WorkflowMove} object was equal to the 
	 * {@link com.aplana.dbmi.model.CardState state} of given card.
	 * @param workflowMove identifier of {@link WorkflowMove} object to be executed
	 */
	public void setWorkflowMove(WorkflowMove workflowMove) {
		this.workflowMove = workflowMove;
	}
	
	public boolean isLastDialogOk() {
		return lastDialogOk;
	}
	
	public void setLastDialogOk(boolean lastDialogOk) {
		this.lastDialogOk = lastDialogOk;
	}
	
	public final Object putParameter(String key, Object value){
		return actionContext.put(key, value);
	}
	
	public final Object getParameter(String key){
		return actionContext.get(key);
	}
	
	public final Object removeParameter(String key){
		return actionContext.remove(key);
	}
}
