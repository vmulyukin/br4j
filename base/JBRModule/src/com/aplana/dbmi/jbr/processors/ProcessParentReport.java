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
package com.aplana.dbmi.jbr.processors;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.LinkedList;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;

import org.springframework.jdbc.core.RowCallbackHandler;

/**
 * ��������� ����������� �������������� ������� ������ �� ���������� ��������� ���������, ��������� � ���������, � �������� ��������� �����
 * � ������ "��������", ���� ��������� �������� ������������� � ���������� ��� ��������� ����������� ���������� �� �������� ������ ���������.
 * @author erentsov
 *
 */
public class ProcessParentReport extends ProcessCard {

	private static final long serialVersionUID = 1L;
	
	private static final ObjectId ON_CONTROL = ObjectId.predefined(ListAttribute.class, "jbr.oncontrol");
	private static final ObjectId COMMISSION_CONTROL_YES = ObjectId.predefined(ReferenceValue.class, "jbr.commission.control.yes");
	
	private static final Object[] queryParams = new Object[]{
		ObjectId.predefined(CardLinkAttribute.class, "jbr.rimp.byrimp").getId(),
		ObjectId.predefined(ListAttribute.class, "jbr.oncontrol").getId(),
		ObjectId.predefined(ReferenceValue.class, "jbr.commission.control.yes").getId(),
		ObjectId.predefined(CardLinkAttribute.class, "jbr.rimp.byrimp").getId(),
		ObjectId.predefined(CardState.class, "execution").getId(),
		ObjectId.predefined(CardLinkAttribute.class, "jbr.report.int.parent").getId(),
		ObjectId.predefined(PersonAttribute.class, "jbr.report.int.executor").getId(),
		ObjectId.predefined(PersonAttribute.class, "jbr.AssignmentExecutor").getId(),
		ObjectId.predefined(CardLinkAttribute.class, "jbr.report.int.parent").getId(),
		ObjectId.predefined(PersonAttribute.class, "jbr.report.int.executor").getId(),
		ObjectId.predefined(PersonAttribute.class, "jbr.resolution.FioSign").getId()
	};
	
	private static final int[] queryTypes = new int[]{
		Types.VARCHAR, Types.VARCHAR, Types.NUMERIC, Types.VARCHAR, Types.NUMERIC, Types.VARCHAR, 
		Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR
	};
	
	private static final ObjectId[] attributesToCopyIds = new ObjectId[]{
		ObjectId.predefined(CardLinkAttribute.class, "jbr.report.attachments"),
		ObjectId.predefined(TypedCardLinkAttribute.class, "jbr.report.result"),
		ObjectId.predefined(HtmlAttribute.class, "jbr.report.text")
	};
	
	private static final ObjectId targetState = ObjectId.predefined(CardState.class, "jbr.commission.executed");

	@Override
	public Object process() throws DataException {
		
		if(checkControlYes()){
			return null;
		}
		
		final ObjectId[] ids = new ObjectId[2];
		StringBuffer sql = new StringBuffer();
		sql.append("select rep.number_value, src_rep.number_value from attribute_value signer")
		.append(" join attribute_value res on res.card_id = signer.card_id and res.attribute_code = ? ")
		.append(" and not exists (select value_id from attribute_value where card_id = res.number_value and attribute_code = ? and value_id = ? )")
		.append(" join attribute_value all_res on all_res.number_value = res.number_value and all_res.attribute_code = ?")
		.append(" join card all_res_states on all_res_states.card_id = all_res.card_id and (all_res_states.status_id not in (?) or all_res_states.card_id = signer.card_id)")
		.append(" join attribute_value rep on rep.number_value = res.number_value and rep.attribute_code = ?")
		.append(" join attribute_value exec on exec.card_id = rep.card_id and exec.attribute_code = ? and exec.number_value = signer.number_value")
		.append(" join attribute_value main_exec on main_exec.card_id = signer.card_id and main_exec.attribute_code = ?")
		.append(" join attribute_value src_rep on src_rep.number_value = main_exec.card_id and src_rep.attribute_code = ?")
		.append(" join attribute_value main_ex on main_ex.card_id = src_rep.card_id and main_ex.attribute_code = ? and main_ex.number_value = main_exec.number_value")
		.append(" where signer.attribute_code = ? and signer.card_id = ").append(getCard().getId().getId().toString()).append(" limit 1");
		getJdbcTemplate().query(sql.toString(), queryParams, queryTypes, new RowCallbackHandler(){
			public void processRow(ResultSet arg0) throws SQLException {
				ids[0] = new ObjectId(Card.class, arg0.getLong(1));
				ids[1] = new ObjectId(Card.class, arg0.getLong(2));
			}
		});
		
		if(ids[0] != null && ids[1] != null){
			Card to = fetchSingleCard(ids[0], Arrays.asList(attributesToCopyIds), true);
			Card from = fetchSingleCard(ids[1], Arrays.asList(attributesToCopyIds), true);
			LinkedList<Attribute> attrsToSave = new LinkedList<Attribute>();
			for(ObjectId id : attributesToCopyIds) {
				Attribute toAttr = to.getAttributeById(id);
				toAttr.setValueFromAttribute(from.getAttributeById(id));
				attrsToSave.add(toAttr);
			}
			doOverwriteCardAttributes(to.getId(), attrsToSave.toArray(new Attribute[attrsToSave.size()]));
			
			WorkflowMove move = findWorkFlowMove(to.getId(), targetState, getSystemUser());
			if(move != null){
				ChangeState action = new ChangeState();
				action.setCard(to);	
				action.setWorkflowMove(move);
				execAction(new LockObject(to.getId()));
				try {
					execAction(action);
				} finally {
					execAction(new UnlockObject(to.getId()));
				}
			} else logger.warn("Cannot perform workflow move - move not found");
		}

		return null;
	}
	
	private boolean checkControlYes() throws DataException{
		Card card = getCard();
		ListAttribute listAttribute = (ListAttribute) card.getAttributeById(ON_CONTROL);
		return listAttribute.getValue().getId().equals(COMMISSION_CONTROL_YES); 
	}
	
	

}
