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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.GetEventLog;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.EventEntry;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.utils.SimpleDBUtils;

public class DoGetEventLog extends ActionQueryBase {

	private static final long serialVersionUID = 2388789047871938710L;

	@Override
	public Object processQuery() throws DataException
	{
		GetEventLog action = (GetEventLog) getAction();
		
		final List<Object> argList = new ArrayList<Object>();
		final List<Integer> typeList = new ArrayList<Integer>();
		
		StringBuilder sql = new StringBuilder();
		
		sqlFormer(sql, action, argList, typeList);
		
		final Object[] args = argList.toArray();
		final int[] types = SimpleDBUtils.makeTypes(typeList);
		
		final List<?> searchResult =
				getJdbcTemplate().query(sql.toString(), args, types, new RowMapper() {
				
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						EventEntry ee = new EventEntry();
						
						ee.setTimestamp(rs.getTimestamp(1));
						
						final Person pers = new Person();
						pers.setId(new ObjectId(Person.class, rs.getLong(2)));
						pers.setFullName(rs.getString(3));
						ee.setUser(pers);
						
						ee.setEvent(rs.getString(4));
						
						if(rs.getLong(5) != 0) {
							final ObjectId id = new ObjectId(Card.class, rs.getLong(5));							
							ee.setObject(id);
						} else if(rs.getLong(6) != 0) {
							final ObjectId id = new ObjectId(Template.class, rs.getLong(6));
							final Template t = new Template();
							t.setId(id);
							t.setNameRu(rs.getString(7));
							t.setNameEn(rs.getString(8));
							ee.setTemplate(t);
							ee.setObject(id);
						}						
						
						if(rs.getLong(9) == 1)
							ee.setSuccess(true);
						else
							ee.setSuccess(false);
						
						ee.setMessage(rs.getString(10));
						
						return ee;
					}
				});
		
		logger.info(searchResult.size() + " log entries found");
		
		return searchResult;
	}
	
	private void sqlFormer(StringBuilder sql, GetEventLog action, 
						   List<Object> argList, List<Integer> typeList) {
		
		sql.append("select \n");
		sql.append("el.log_date, p.person_id, p.full_name, \n"); // 1,2,3
		sql.append("el.action_code, \n"); // 4
		sql.append("el.card_id, el.template_id, t1.template_name_rus, t1.template_name_eng, \n"); // 5,6,7,8
		sql.append("el.is_succes \n"); //9
		
		if(action.getShowMsg())
			sql.append(", eld.description \n"); //10
		else
			sql.append(", null::text \n"); //10
		
		sql.append("	from event_log el \n");
		
		sql.append("	left join person p on p.person_id = el.actor_id \n");
		
		sql.append("	left join card c on c.card_id = el.card_id \n");
		sql.append("	left join template t1 on t1.template_id = el.template_id \n");
		
		if(action.getShowMsg())
			sql.append("	left join event_log_detail eld on eld.action_log_id = el.action_log_id \n");
		
		sql.append("where (1=1) \n");
		
		//������������ ����� (admin/user)
		if(action.getUser() != null) {
			try {
				sql.append(MessageFormat.format(
						" and el.actor_id = {0} \n",
						new Object[] {Long.parseLong(action.getUser())}
					));
			} catch(Exception e) {
			sql.append(MessageFormat.format(
					" and p.person_login = {0} \n",
					new Object[] {"'" + action.getUser() + "'"}
				));
			}
		}
		
		//������������ ��������
		if(action.getIgnoredActions() != null && !action.getIgnoredActions().isEmpty())
			sql.append(MessageFormat.format(
					" and el.action_code not in ({0}) \n",
					new Object[] {listToString(action.getIgnoredActions())}
				));
		
		//������������ ��������� ������� (success/failure/all)
		if(action.getResultSuccess() != null && action.getResultSuccess())
			sql.append("and el.is_succes = 1 \n");
		else if(action.getResultSuccess() != null && !action.getResultSuccess())
			sql.append("and el.is_succes = 0 \n");
		
		//������������ ������ (today, week, all time)
		if (action.getFromDate() != null) {
			sql.append("and el.log_date >= (?) \n");
			argList.add(action.getFromDate());
			typeList.add(new Integer(java.sql.Types.TIMESTAMP));

		}
		if (action.getToDate() != null) {
			sql.append("and el.log_date < (?) \n");
			argList.add(action.getToDate());
			typeList.add(new Integer(java.sql.Types.TIMESTAMP));
		}
		
		//sql.append("offset 0 limit 1000");
	}
	
	private String listToString(List<String> list) {
		String s = "";
		for(String str : list) {
			if(!"".equals(s))
				s += ",";
			s += "'" + str.toString() + "'";
		}
		return s;
	}

}
