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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.aplana.dbmi.model.CardAccess;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Workflow;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.SaveQueryBase;

/**
 * Query used to save single {@link Workflow} instance in database
 */
public class SaveWorkflow extends SaveQueryBase {
	protected ObjectId processNew() throws DataException {
		Workflow w = (Workflow)getObject();
		Long id = new Long(generateId("seq_system_id"));
		getJdbcTemplate().update(
			"insert into workflow (workflow_id, initial_status_id, name_rus, name_eng, is_active)" +
			" values (?, ?, ?, ?, ?)",
			new Object[] {
				id, 
				w.getInitialState().getId(), 
				w.getName().getValueRu(), 
				w.getName().getValueEn(), 
				w.isActive() ? new Integer(1) : new Integer(0)
			},
			new int[] {
				Types.NUMERIC, 
				Types.NUMERIC, 
				Types.VARCHAR, 
				Types.VARCHAR, 
				Types.NUMERIC
			}
		);
		ObjectId workflowId = new ObjectId(Workflow.class, id); 
		insertWorkflowMoves(w.getMoves(), workflowId);
		return workflowId;
	}

	protected void processUpdate() throws DataException {
		checkLock();
		Workflow w = (Workflow)getObject();
		getJdbcTemplate().update(
			"update workflow set" +
			" initial_status_id = ?" +
			", name_rus = ?" +
			", name_eng = ?" +
			", is_active = ?" +
			" where workflow_id = ?",
			new Object[] {
				w.getInitialState().getId(), 
				w.getName().getValueRu(), 
				w.getName().getValueEn(), 
				w.isActive() ? new Integer(1) : new Integer(0), 
				w.getId().getId()
			},
			new int[] {
				Types.NUMERIC, 
				Types.VARCHAR, 
				Types.VARCHAR, 
				Types.NUMERIC, 
				Types.NUMERIC
			}
		);
		insertWorkflowMoves(w.getMoves(), w.getId());
	}
	
	private void insertWorkflowMoves(List moves, ObjectId workflowId) {
		JdbcTemplate jt = getJdbcTemplate();
		final Set oldWorkflowMoveIds = new HashSet(); 
		jt.query(
			"select wfm_id from workflow_move where workflow_id = ?",
			new Object[] {workflowId.getId()},
			new int[] {Types.NUMERIC},
			new RowCallbackHandler() {
				public void processRow(ResultSet rs) throws SQLException {
					oldWorkflowMoveIds.add(new Long(rs.getLong(1)));
				}
			}
		);
		

		final List addedMoves = new ArrayList();
		final List updatedMoves = new ArrayList();
		Iterator i = moves.iterator();
		while (i.hasNext()) {
			WorkflowMove move = (WorkflowMove)i.next();
			if (move.getId() == null) {
				addedMoves.add(move);
			} else {
				updatedMoves.add(move);
				oldWorkflowMoveIds.remove(move.getId().getId());
			}
		}
		
		/* (2010/03) POSGRE OLD:
		String sql = "insert into workflow_move (" +
			"wfm_id, workflow_id, name_rus, name_eng, from_status_id" +
			", to_status_id, need_confirmation, action_code, confirmation_rus, confirmation_eng" +
			") values (seq_system_id.nextval, " + workflowId.getId().toString() + ", ?, ?, ?, ?, ?, ?, ?, ?)";
		 */	
		String sql = "insert into workflow_move (" +
			"wfm_id, workflow_id, name_rus, name_eng, from_status_id" +
			", to_status_id, need_confirmation, action_code, confirmation_rus, confirmation_eng, close_card, apply_ds" +
			") values (nextval('seq_system_id'), " + workflowId.getId().toString() + ", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		BatchPreparedStatementSetter pss = new BatchPreparedStatementSetter() {
			Iterator iter = addedMoves.iterator();
			public int getBatchSize() {
				return addedMoves.size();
			}

			public void setValues(PreparedStatement stmt, int index) throws SQLException {
				WorkflowMove move = (WorkflowMove)iter.next();
				stmt.setString(1, move.getName().getValueRu());
				stmt.setString(2, move.getName().getValueEn());
				stmt.setObject(3, move.getFromState().getId(), Types.NUMERIC);
				stmt.setObject(4, move.getToState().getId(), Types.NUMERIC);
				stmt.setLong(5, move.isNeedConfirmation() ? 1 : 0);
				if (move.getLogAction() != null) {
					stmt.setString(6, (String)move.getLogAction().getId());	
				} else {
					stmt.setNull(6, Types.VARCHAR);
				}
				if (move.isNeedConfirmation()) {
					stmt.setString(7, move.getConfirmation().getValueRu());
					stmt.setString(8, move.getConfirmation().getValueEn());
				} else {
					stmt.setNull(7, Types.VARCHAR);
					stmt.setNull(8, Types.VARCHAR);
				}
				stmt.setLong(9, move.isCloseCard() ? 1 : 0);
				stmt.setInt(10, move.getApplyDigitalSignatureOnMove());
			}
		};		
		jt.batchUpdate(sql, pss);
		
		sql = "update workflow_move set workflow_id = " + workflowId.getId().toString() + 
			", name_rus = ?, name_eng = ?, from_status_id = ?, to_status_id = ?" +
			", need_confirmation = ?, action_code = ?, confirmation_rus = ?, confirmation_eng = ?" +
			", close_card = ?, apply_ds = ?" +
			" where wfm_id = ?";
		pss = new BatchPreparedStatementSetter() {
			Iterator iter = updatedMoves.iterator();
			public int getBatchSize() {
				return updatedMoves.size();
			}

			public void setValues(PreparedStatement stmt, int index) throws SQLException {
				WorkflowMove move = (WorkflowMove)iter.next();
				stmt.setString(1, move.getName().getValueRu());
				stmt.setString(2, move.getName().getValueEn());
				stmt.setObject(3, move.getFromState().getId(), Types.NUMERIC);
				stmt.setObject(4, move.getToState().getId(), Types.NUMERIC);
				stmt.setLong(5, move.isNeedConfirmation() ? 1 : 0);
				if (move.getLogAction() != null) {
					stmt.setString(6, (String)move.getLogAction().getId());	
				} else {
					stmt.setNull(6, Types.VARCHAR);
				}
				if (move.isNeedConfirmation()) {
					stmt.setString(7, move.getConfirmation().getValueRu());
					stmt.setString(8, move.getConfirmation().getValueEn());
				} else {
					stmt.setNull(7, Types.VARCHAR);
					stmt.setNull(8, Types.VARCHAR);
				}
				stmt.setLong(9, move.isCloseCard() ? 1 : 0);
				stmt.setInt(10, move.getApplyDigitalSignatureOnMove());
				stmt.setObject(11, move.getId().getId(), Types.NUMERIC);
			}
		};
		jt.batchUpdate(sql, pss);
		
		if (!oldWorkflowMoveIds.isEmpty()) {
			StringBuffer deleteSql = new StringBuffer("delete from workflow_move where wfm_id in (");
			i = oldWorkflowMoveIds.iterator();
			while (i.hasNext()) {
				deleteSql.append(i.next());
				if (i.hasNext()) {
					deleteSql.append(',');
				}
			}
			deleteSql.append(')');
			jt.update(deleteSql.toString());
		}
		
        //TODO Because workflow move removal is possible, here may be required the deletion of corresponding access rules.
		/*jt.update("delete from card_access ca where permission_type = ?" +
			" and not exists (select 1 from workflow_move wm where wm.wfm_id = ca.object_id)",
			new Object[] { CardAccess.WORKFLOW_MOVE },
			new int[] { Types.NUMERIC } 
		);  */
	}

	public void validate() throws DataException {
		Workflow w = (Workflow)getObject();
		if (w.getName() == null || w.getName().hasEmptyValues()) {
			throw new DataException("store.workflow.empty.name");
		}
		if (w.getInitialState() == null) {
			throw new DataException("store.workflow.empty.initialState");
		}
		super.validate();
	}
}
