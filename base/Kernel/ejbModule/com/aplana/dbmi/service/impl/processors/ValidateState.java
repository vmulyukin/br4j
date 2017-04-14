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
package com.aplana.dbmi.service.impl.processors;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.CheckWfmConditions;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeViewParam;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TemplateBlock;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.WorkflowMoveRequiredField;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.DatabaseClient;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.dbmi.service.impl.QueryBase;
import com.aplana.dbmi.service.impl.Validator;

import com.aplana.dbmi.model.PseudoAttribute;

/**
 * ���������, ����������� �������� �� ����������� �������� ��� ������ {@link ChangeState}.
 * ���������� ��� ������� ������ � �������� ���-���������� � queries.xml � Kernel.
 * �������� � ��������� ���������, ����� ������������� ���������� ���������� � ����� ������ ������������.
 * @author desu
 *
 */
public class ValidateState extends ProcessorBase implements DatabaseClient, Validator {

	private static final long serialVersionUID = -1893146916391389858L;
	private JdbcTemplate jdbc;

	@Override
	public Object process() throws DataException {
		final ChangeState advance = (ChangeState) getAction();
		final boolean isCardMustReAccess = CheckCardForRecalculate(advance.getCard().getId());	// ������� ����, ��� ������� �������� ���� � ������ �� �������� ����  
		
		// fetch WorkFlowMove by id ...
		final ObjectQueryBase wfmSubQuery = getQueryFactory().getFetchQuery(WorkflowMove.class);
		wfmSubQuery.setId(advance.getWorkflowMove().getId());
		final WorkflowMove wfm = (WorkflowMove)getDatabase().executeQuery(getUser(), wfmSubQuery);
		advance.setWorkflowMove(wfm);

		// fetch Card by id ...
		final ObjectQueryBase cardSubQuery = getQueryFactory().getFetchQuery(Card.class);
		cardSubQuery.setId(advance.getCard().getId());
		// ���� ������� �������� ��� ���� � ������ �� �������� ����, ������ �� ����� ������� ��� ���������� ������ ����� ������� ��� ���������� (������ ����� ����������� ����� BatchChangeState) 
		// => ������ ���� �� ������ �������� � �������� ������������ ���, ������ � �������, � ���� �������� ��� ��������������
		final Card card = (Card)getDatabase().executeQuery((!isCardMustReAccess)?getUser():getSystemUser(), cardSubQuery);

		if (!card.getState().equals(wfm.getFromState())) {
			throw new DataException("action.state.wrongstate", new Object[] { 
							wfm.getName() 
							+ " '" +   wfm.getMoveName() + "'"
							+ ", " +   wfm.getId() 
							+ ": " +  wfm.getFromState() 
							+ " -> "+  wfm.getToState(),
							card.getId() + ", state: " + card.getState()
					});
		}
		
		// Fetch view attributes ...
		final ChildrenQueryBase viewQuery = getQueryFactory().getChildrenQuery(Card.class, AttributeViewParam.class);
		viewQuery.setParent(advance.getCard().getId());		
		final List attrViewParams = (List) getDatabase().executeQuery(getUser(), viewQuery);

		final Set mandatoryAttrIds = new HashSet();
		final Set blankAttrIds = new HashSet();

		// ���������� ������������ ��������� �� view...
		//
		for ( Iterator i = attrViewParams.iterator(); i.hasNext(); ) 
		{
			final AttributeViewParam rec = (AttributeViewParam)i.next();
			if (rec.isMandatory()) {
				mandatoryAttrIds.add(rec.getId().getId());
			}
		}

		// ���������� ���������, ���������� ����������� ��� ������� �������� ...
		getJdbcTemplate().query(
			"SELECT ta.attribute_code, w.must_be_set \n" +
			"FROM workflow_move_required_field w \n" +
			"	JOIN template_attribute ta \n" +
			"		on ta.template_attr_id = w.template_attr_id \n" +
			"WHERE w.wfm_id = ? and ta.template_id = ?",
			new Object[] {
				wfm.getId().getId(),
				card.getTemplate().getId()
			},
			new int[] {
				Types.NUMERIC,
				Types.NUMERIC
			},
			new RowCallbackHandler() {
				public void processRow(ResultSet rs) throws SQLException {
					final String attrCode = rs.getString(1);
					switch(rs.getInt(2)) {
						case WorkflowMoveRequiredField.MUSTBESET_ASSIGNED:
							mandatoryAttrIds.add(attrCode);
							blankAttrIds.remove(attrCode);
							break;
						case WorkflowMoveRequiredField.MUSTBESET_BLANK:
							mandatoryAttrIds.remove(attrCode);
							blankAttrIds.add(attrCode);
							break;
						default: // ������ ������ ������� �������� 
							// ��������, ��� �������� � �������, ������ �� ���������� ���������.
							mandatoryAttrIds.remove(attrCode);
							blankAttrIds.remove(attrCode);
					}
				}
			}
		);

		CheckWfmConditions checkWfmConditions = new CheckWfmConditions();
		checkWfmConditions.setCard(card);
		checkWfmConditions.setWfm(wfm.getId());
		ActionQueryBase actionQueryBase = getQueryFactory().getActionQuery(checkWfmConditions);
		actionQueryBase.setAction(checkWfmConditions);
		Set<String> result = (Set<String>) getDatabase().executeQuery(getUser(), actionQueryBase);
		mandatoryAttrIds.removeAll(result);
		blankAttrIds.removeAll(result);
		
		// ������ �� ���� ��������� ���� �������� �������� � ��������
		// ������������ ��� �������...  
		for(Iterator i = card.getAttributes().iterator(); i.hasNext(); ) 
		{
			final TemplateBlock block = (TemplateBlock)i.next();
			final Iterator j = block.getAttributes().iterator();
			while (j.hasNext()) {
				final Attribute attr = (Attribute)j.next();
				final String attrCode = (String)attr.getId().getId();

				final boolean isMandatory = mandatoryAttrIds.contains(attrCode);
				final boolean isBlank = blankAttrIds.contains(attrCode);
				final boolean hasVal = !attr.isEmpty();

				if (isMandatory && !(attr instanceof PseudoAttribute)) {
					if (!hasVal) {
						throw new DataException("action.state.attrmandatory",
								new Object[] { attr.getNameRu(), attr.getNameEn() + "!!!" } );
					}
					mandatoryAttrIds.remove(attrCode); // ... ��� ���������
				}

				if (isBlank) {
					if (hasVal) {
						throw new DataException("action.state.attrmustbeempty",
								new Object[] { attr.getName() } );
					}
					blankAttrIds.remove(attrCode); // ... ��� ���������
				}
			}
		}
		
		return null;
	}

	public void setJdbcTemplate(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}
	
	public JdbcTemplate getJdbcTemplate() {
		return jdbc;
	}

	private boolean CheckCardForRecalculate(ObjectId cardId){
		QueryBase primaryQuery = this.getPrimaryQuery();
		Collection<ObjectId> recalculateList = (primaryQuery!=null)?primaryQuery.getCardsForRecalculateAL():null;
		if (recalculateList!=null&&recalculateList.contains(cardId)){
			return true;
		}
		return false;
	}
}
