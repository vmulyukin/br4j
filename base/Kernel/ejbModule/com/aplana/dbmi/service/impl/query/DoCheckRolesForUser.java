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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.CheckRolesForUser;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoCheckRolesForUser extends ActionQueryBase {

	@Override
	public Object processQuery() throws DataException {
		CheckRolesForUser action = (CheckRolesForUser )this.getAction();
		String personLogin = action.getPersonLogin();
		// превращаем список ролей список ролей для запроса
		String roles = wrappingRoles(action.getRoles().trim(), "'");
		
		if(personLogin == null||personLogin.isEmpty()) return null;
		// если входные роли не заданы, то значит проверка на любую роль у пользователя 
		final int rolesCount;
		if (roles==null){
			rolesCount= getJdbcTemplate().queryForInt(
					"SELECT count(pr.prole_id) \n" +
					"FROM person_role pr  \n" +
					"	join person p on p.person_id = pr.person_id \n" +
					"WHERE \n" +
					"	p.person_login = ? \n",
					new Object[] { personLogin},
					new int[] { Types.VARCHAR});
		} else {
			rolesCount= getJdbcTemplate().queryForInt(
				"SELECT count(pr.prole_id) \n" +
				"FROM person_role pr  \n" +
				"	join person p on p.person_id = pr.person_id \n" +
				"WHERE \n" +
				"	p.person_login = ? \n"+
				"	and pr.role_code in ("+roles+")",
				new Object[] { personLogin },
				new int[] { Types.VARCHAR});
		}
		return (rolesCount>0);
	}
	
	/**
	 * Обертка каждой роли из списка спецсимволом 
	 * @param roles - список ролей с разделителем CheckRolesForUser.ROLES_DELIMETER 
	 * @param quote - спецсимвол для оборачивания (например "'")
	 * @return тот же список, только с оберткой каждой роли
	 */
	private String wrappingRoles( final String roles, String quote)
	{
		// если на входе ролей нет, возвращаем null
		if (roles == null || roles.isEmpty())
			return null;
		if (quote == null) quote = "";
		final String[] roleArray = roles.split(CheckRolesForUser.ROLES_DELIMETER);
		final StringBuffer result = new StringBuffer(roleArray.length);
		for( int i=0; i<roleArray.length; i++ ) 
		{
			String role = roleArray[i];
			final String strItem = (role == null||role.isEmpty()) ? "\'\'" : role;
			result.append(quote).append(strItem).append(quote);
			if (i<roleArray.length-1) {
				result.append(CheckRolesForUser.ROLES_DELIMETER);
			}
		}
		return result.toString();
	}
}