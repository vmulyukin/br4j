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
package com.aplana.dbmi.service.impl.query;

import java.sql.Types;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * Query used to perform {@link ChangeState} action.<br>
 * Changes state of single {@link Card} as described in given {@link WorkflowMove} object
 */
public class DoChangeState extends ActionQueryBase implements WriteQuery
{
 	private static final long serialVersionUID = -7745801218455394546L;

	/**
	 * @return action code, specified for given {@link WorkflowMove}	 * 
	 */
	public String getEvent() {
		final ChangeState action = (ChangeState) getAction();
		if (action == null)
			return "chgState.action=null";
		if (action.getWorkflowMove() == null)
			return "chgState.action.wfm=null";
		final ObjectId logActionId = action.getWorkflowMove().getLogAction();
		if (logActionId == null)
			return "chgState.wfmId="+ action.getWorkflowMove().getId();
		if (logActionId.getId() == null)
			return "chgState.logId=null,wfmId="+ action.getWorkflowMove().getId();
		return (String) logActionId.getId();
	}

	/**
	 * Performs checks if given {@link WorkflowMove} could be performed.
	 * If checks succeed then changes state of given {@link Card}.
	 * @return null
	 * @throws DataException if given {@link WorkflowMove} couldn't be performed.
	 */
	public Object processQuery() throws DataException
	{
		//�������� �� ������� ������ ������������ ����������� {@link ValidateState}
		//����� �� ������ ��������� ������� (���� �� ���� ���� ��������� ��������)
		validateState();
		ChangeState advance = (ChangeState) getAction();
		cleanAccessList(advance.getCard().getId());
		getJdbcTemplate().update(
			"UPDATE card SET status_id=? WHERE card_id=?",
			new Object[] { advance.getWorkflowMove().getToState().getId(), advance.getCard().getId().getId() },
			new int[] { Types.NUMERIC, Types.NUMERIC }
		);
		// � ����� ����������� ��� ��������� ������� ���� ����� ����������� ����� ���������� ���� ����-����������� ������������� �����        
		this.getPrimaryQuery().putCardIdInRecalculateAL(advance.getCard().getId());	
		return null;
	}
	
	/**
	 * Fills the WorkFlowMove in action. Fetch wfm from database by id.
	 * @throws DataException
	 */
	private void validateState() throws DataException {
		final ChangeState advance = (ChangeState) getAction();
		// fetch WorkFlowMove by id ...
		final ObjectQueryBase wfmSubQuery = getQueryFactory().getFetchQuery(WorkflowMove.class);
		wfmSubQuery.setId(advance.getWorkflowMove().getId());
		final WorkflowMove wfm = (WorkflowMove)getDatabase().executeQuery(getUser(), wfmSubQuery);
		advance.setWorkflowMove(wfm);
	}

	@Override
	public String toString() {
		final ChangeState action = (ChangeState) getAction();
		return "Change State \"" +  action.getWorkflowMove().getName() + 
				"\" for card \"" + (action.getCard().getTemplateName() != null ? action.getCard().getTemplateName() : "") + 
				" id=" +  action.getCard().getId().getId().toString()+"\"";
	}
	

}
