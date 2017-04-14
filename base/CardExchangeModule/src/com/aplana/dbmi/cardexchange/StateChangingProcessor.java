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
package com.aplana.dbmi.cardexchange;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.cardexchange.xml.CardExchangeUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.ProcessorBase;

public class StateChangingProcessor extends ProcessorBase implements Parametrized {
	private final static String PARAM_WORKFLOW_MOVE = "workflowMove";
	private ObjectId workflowMoveId;
	public Object process() throws DataException {
		Card card = (Card)getObject();
		logger.debug("Changing state of card " + card.getId().getId());
		ChangeState action = new ChangeState();
		action.setCard(card);
		ObjectQueryBase qWorkflowMove = getQueryFactory().getFetchQuery(WorkflowMove.class);
		qWorkflowMove.setId(workflowMoveId);
		WorkflowMove wm = (WorkflowMove)getDatabase().executeQuery(getSystemUser(), qWorkflowMove);
		logger.debug("Workflow move to be performed is: '" + wm.getMoveName() + "', id = " + wm.getId().getId());
		action.setWorkflowMove(wm);
		ActionQueryBase qAction = getQueryFactory().getActionQuery(action);
		qAction.setAction(action);
		getDatabase().executeQuery(getSystemUser(), qAction);
		return null;
	}

	public void setParameter(String name, String value) {
		if (PARAM_WORKFLOW_MOVE.equals(name)) {
			workflowMoveId = CardExchangeUtils.getObjectId(WorkflowMove.class, value, true);
		}
	}
}
