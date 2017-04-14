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
package com.aplana.dbmi.replication.query;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.replication.action.GetPerson;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import org.apache.commons.lang.StringUtils;

import java.sql.Types;

public class DoGetPerson extends ActionQueryBase {
	private static final long serialVersionUID = 1L;

	@Override
	public Object processQuery() throws DataException {
		GetPerson action = getAction();
		String login 	= StringUtils.trim(action.getLogin());
		String email 	= StringUtils.trim(action.getEmail());
		String fullName = StringUtils.trim(action.getFullName());
		String uuid 	= action.getUuid();
		try {
			PersonWrapper result;
			//задан uuid - сначала ищем по нему
			if (!StringUtils.isEmpty(uuid)) {
				result = getPerson(uuid);
				if (result != null) {
					return result;
				}
			}
			//если uuid не задан, или по нему ничего не нашли, тогда ищем по логин/email/имя
			result = getPerson(login, email, fullName);
			return result;
		} catch (DataException ex) {
			logger.error("Can't get person by params: login="+login+", email="+email+", full_name="+fullName, ex);
			throw ex;
		} catch (Exception ex) {
			logger.error("Can't get person by params: login="+login+", email="+email+", full_name="+fullName, ex);
			throw new DataException("Error on execute do process in " + this.getClass().getName(), ex);
		}
	}
	
	private PersonWrapper getPerson(String login, String email, String fullName) throws DataException {
		String isActiveSuffix = "";
		long count = getJdbcTemplate().queryForLong(
			"select count(1) from person where trim(person_login) = ? and trim(email) = ? and trim(full_name) = ?",
			new Object[] { login, email, fullName },
			new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR });
		if (count == 0) {
			return null;
		} else if (count > 1) {
			isActiveSuffix = " and is_active = 1";
		}
		
		long id = getJdbcTemplate().queryForLong(
			"select person_id from person where trim(person_login) = ? and trim(email) = ? and trim(full_name) = ?"
					+ isActiveSuffix,
			new Object[] { login, email, fullName },
			new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR });
		
		PersonWrapper person = loadPerson(id);
		return person;
	}
	
	private PersonWrapper getPerson(String uuid) throws DataException {
		long count = getJdbcTemplate().queryForLong(
			"select count(1) from person where replication_uuid = ?",
			new Object[] { uuid },
			new int[] { Types.VARCHAR });
		if (count == 0) {
			return null;
		}
		
		long id = getJdbcTemplate().queryForLong(
			"select person_id from person where replication_uuid = ?",
			new Object[] { uuid },
			new int[] { Types.VARCHAR });
		
		PersonWrapper person = loadPerson(id);
		return person;
	}
	
	private PersonWrapper loadPerson(long id) throws DataException {
		ObjectQueryBase query = getQueryFactory().getFetchQuery(Person.class);
		query.setId(new ObjectId(Person.class, id));
		Person person = getDatabase().executeQuery(getUser(), query);
		
		String uuid = (String)getJdbcTemplate().queryForObject(
				"select replication_uuid from person where person_id = ?",
				new Object[] { id },
				new int[] { Types.NUMERIC },
				String.class);
		
		PersonWrapper wrap = new PersonWrapper(person);
		wrap.setUuid(uuid);
		return wrap;
	}
	
	/**
	 * Обертка для базового класса Person.
	 * СОдержит доп поле uuid для репликации персон.
	 * @author desu
	 */
	public static class PersonWrapper {
		private Person person;
		private String uuid;
		
		PersonWrapper(Person pers) {
			this.person = pers;
		}
		
		public String getUuid() {
			return uuid;
		}
		
		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
		
		public Person getPerson() {
			return person;
		}
	}
}
