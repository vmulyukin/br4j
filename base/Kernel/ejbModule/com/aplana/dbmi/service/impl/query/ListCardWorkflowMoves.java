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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.aplana.dbmi.service.impl.access.AccessRuleManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardAccess;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.LogAction;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.access.CardAccessUtils;

/**
 * {@link ChildrenQueryBase} descendant used to fetch all {@link WorkflowMove} objects
 * which are applicable to given {@link Card} object and are available to user who performs query.
 */
public class ListCardWorkflowMoves extends ChildrenQueryBase {

    private AccessRuleManager manager;

    public void setJdbcTemplate(JdbcTemplate jdbc) {
        super.setJdbcTemplate(jdbc);
        manager = new AccessRuleManager(jdbc);
    }

	/**
	 * Fetches all {@link WorkflowMove} objects which are applicable to given {@link Card} object 
	 * and are available to user who performs query.
	 * @return collection of {@link WorkflowMove} objects which are applicable to given {@link Card} object 
	 * and are available to user who performs query.
	 */

	public Object processQuery() throws DataException 
	{
		final ObjectId userId = getUser().getPerson().getId();
		final ObjectId cardId = getParent();
		
		//final Set userCardRoles 
		//	= CardAccessUtils.getPersonRoles4Card( userId, cardId, getJdbcTemplate());
		final List result = new ArrayList();
		
		final RowCallbackHandler rowResult = new RowCallbackHandler()
		{
			public void processRow(ResultSet rs) throws SQLException 
			{
				final ObjectId wfmId = new ObjectId(WorkflowMove.class, rs.getLong(1));

                //TODO Permissions for the move should be checked right in the main query
                if (!manager.isWorkflowMoveAllowed(cardId, wfmId, userId))
					return;

				final WorkflowMove wfm 
					= (WorkflowMove) DataObject.createFromId(wfmId);
				
				wfm.getName().setValueRu(rs.getString(2));
				wfm.getName().setValueEn(rs.getString(3));
				wfm.setNeedConfirmation(rs.getInt(4) == 1);
				
				wfm.setFromState(CardState.getId(rs.getInt(5)));
				wfm.setToState(CardState.getId(rs.getInt(6)));
				wfm.setLogAction(new ObjectId(LogAction.class, rs.getString(7)));

				wfm.getConfirmation().setValueRu(rs.getString(8));
				wfm.getConfirmation().setValueEn(rs.getString(9));
				wfm.setCloseCard(rs.getInt(10) == 1);
				wfm.setApplyDigitalSignatureOnMove(rs.getInt(11));
				
				wfm.getDefaultName().setValueRu(rs.getString(12));
				wfm.getDefaultName().setValueEn(rs.getString(13));
				result.add(wfm);
			}

		};
		
		String sql = "select wm.wfm_id, wm.name_rus, wm.name_eng, wm.need_confirmation, wm.from_status_id" +
			", wm.to_status_id, wm.action_code, wm.confirmation_rus, wm.confirmation_eng, wm.close_card" +
			", wm.apply_ds, cs.default_move_name_rus, cs.default_move_name_eng" +
			" from workflow_move wm, card_status cs, card c" +
			" where c.card_id = ? and c.status_id = wm.from_status_id" +
			" and wm.to_status_id = cs.status_id" +
			" and exists (select 1 from workflow w, template t where" +
			" w.workflow_id = wm.workflow_id and t.workflow_id = w.workflow_id" +
			" and t.template_id = c.template_id)";
		
		getJdbcTemplate().query(
			sql,
			new Object[] { (Long)cardId.getId() },
			new int[] { Types.NUMERIC },
			rowResult
		);	
		

		return result;
	}
}
