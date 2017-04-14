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

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mvel2.MVEL;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.util.StringUtils;

import com.aplana.dbmi.action.CheckWfmConditions;
import com.aplana.dbmi.action.FetchChildrenCards;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoCheckWfmConditions extends ActionQueryBase {

	public Object processQuery() throws DataException {
		Set<String> result = new HashSet<String>();
		CheckWfmConditions action = (CheckWfmConditions)getAction();
		final List<WorkFlowMoveCondition> workFlowMoveConditions = new ArrayList<WorkFlowMoveCondition>();
		final Map<Long,WfmTermValue> termValues = new HashMap<Long, WfmTermValue>();
		getJdbcTemplate().query(
				"select wfm_c.condition, wfm_c.message_rus, wfm_c.message_en \n"+
				"from workflow_move_conditions wfm_c where wfm_c.wfm_id =?",
				new Object[] {
					action.getWfm().getId(),
				},
				new int[] {
					Types.NUMERIC,
				},
				new RowCallbackHandler() {
					public void processRow(ResultSet rs) throws SQLException {
						workFlowMoveConditions.add(new WorkFlowMoveCondition(rs.getString(1), rs.getString(2), rs.getString(3)));
					}
				}
			);
		Set<Long> allWfmRfId = new HashSet<Long>();
		for(WorkFlowMoveCondition condition : workFlowMoveConditions){
			allWfmRfId.addAll(condition.getWfm_rf_id());
		}
		if(allWfmRfId.isEmpty())
			return result;
		getJdbcTemplate().query(
				"select wfm_rf.wfm_rf_id, ta.attribute_code,\n"+ 
				"((wfm_rf.must_be_set = 0 and av.number_value is null and av.string_value is null and av.date_value is null and av.long_binary_value is null and av.value_id is null) OR\n"+ 
				"(wfm_rf.must_be_set = 1 and av.attr_value_id is not null and (av.number_value = wfm_rf.number_value or wfm_rf.number_value is null)\n"+ 
				"	and (av.string_value = wfm_rf.string_value or wfm_rf.string_value is null)\n"+ 
				"	and (av.date_value = wfm_rf.date_value or wfm_rf.date_value is null)\n"+ 
				"	and (av.value_id = wfm_rf.value_id or wfm_rf.value_id is null)\n"+ 
				"	and (av.long_binary_value = wfm_rf.long_binary_value or wfm_rf.long_binary_value is null)) OR\n"+ 
				"(wfm_rf.must_be_set = 2)), wfm_rf.must_be_set\n"+ 
				"from workflow_move_required_field wfm_rf\n"+ 
				"join card c on c.card_id = ?\n"+ 
				"join template_attribute ta on wfm_rf.template_attr_id = ta.template_attr_id and c.template_id = ta.template_id\n"+ 
				"left join attribute_value av on av.attribute_code = ta.attribute_code and av.card_id = c.card_id\n"+ 
				"where wfm_rf.wfm_rf_id in ("+StringUtils.collectionToCommaDelimitedString(allWfmRfId)+")\n", 
				new Object[] {
					action.getCard().getId().getId(),
				},
				new int[] {
					Types.NUMERIC,
				},
				new RowCallbackHandler() {
					public void processRow(ResultSet rs) throws SQLException {
						termValues.put(rs.getLong(1), new WfmTermValue(rs.getString(2), rs.getBoolean(3), rs.getLong(4)));
					}
				}
			);
		Map<String,Boolean> preEvaluatedValues = action.getPreEvaluatedValues();
		if(preEvaluatedValues != null && preEvaluatedValues.size()>0){
			for(Iterator<Entry<Long,WfmTermValue>> i = termValues.entrySet().iterator();i.hasNext();){
				WfmTermValue termValue  = i.next().getValue();
				if(preEvaluatedValues.containsKey(termValue.getAttrCode())){
					if(termValue.getType() == 0){
						termValue.setValue(!preEvaluatedValues.get(termValue.getAttrCode()));
					} else if(termValue.getType() == 1){
						termValue.setValue(preEvaluatedValues.get(termValue.getAttrCode()));
					}
				}
			}
		}
		for(WorkFlowMoveCondition condition : workFlowMoveConditions){
			if(condition.validate(termValues)){
				result.addAll(condition.getAttributes());
			} else {
				throw new DataException(ContextProvider.getContext().getLocaleString(
						condition.getMessage_rus(), condition.getMessage_eng()));
			}
		}
		return result;
	}
	
	private class WorkFlowMoveCondition{
		
		public final Pattern pattern = Pattern.compile("p{1}[0-9]+");
		public final String prefix = "p";
		private String expression;
		private Set<String> attributes;
		private Set<Long> wfm_rf_id;
		private String message_rus;
		private String message_eng;
		
		
		public WorkFlowMoveCondition(String expression, String message_rus,
				String message_eng) {
			this.expression = expression;
			this.message_rus = message_rus;
			this.message_eng = message_eng;
			fillWfmRfIds();
		}

		private void fillWfmRfIds(){
			this.wfm_rf_id = new HashSet<Long>();
			Matcher m = pattern.matcher(this.expression);
			while (m.find()) {
				wfm_rf_id.add(Long.parseLong(m.group().replace("p", "")));
			}
		}
		
		public boolean validate(Map<Long,WfmTermValue> termValues){
			Map<String, Boolean> args = new HashMap<String, Boolean>();
			attributes = new HashSet<String>();
			for(Long id : wfm_rf_id){
				if(termValues.containsKey(id)){
					args.put(prefix+id.toString(), (Boolean)termValues.get(id).getValue());
					attributes.add((String)termValues.get(id).getAttrCode());
				} else {
					attributes.clear();
					return true;
				}
			}
			Serializable compiled = MVEL.compileExpression(expression);
			return (Boolean) MVEL.executeExpression(compiled, args); 
			
		}
		
		public String getMessage_rus() {
			return message_rus;
		}

		public String getMessage_eng() {
			return message_eng;
		}

		public Set<String> getAttributes() {
			return attributes;
		}

		public Set<Long> getWfm_rf_id() {
			return wfm_rf_id;
		}
	}
	
	private class WfmTermValue{
		public WfmTermValue(String attrCode, Boolean value, Long type) {
			super();
			this.attrCode = attrCode;
			this.type = type;
			this.value = value;
		}
		
		public String getAttrCode() {
			return attrCode;
		}
		public Long getType() {
			return type;
		}
		public Boolean getValue() {
			return value;
		}
		public void setValue(Boolean value) {
			this.value = value;
		}
		private String attrCode;
		private Long type;
		private Boolean value;
	}
}
