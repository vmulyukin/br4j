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

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.DatabaseClient;
import com.aplana.dbmi.service.impl.ProcessorBase;


public class CheckRoleDuplicateProcessor extends ProcessorBase implements DatabaseClient {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected final Log logger = LogFactory.getLog(getClass());
	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Object process() throws DataException {
		SystemRole role = (SystemRole) getObject();
		
		int maxCount = 0;
		String roleId;
		if (role.getId() != null) {
			maxCount = 1;
			roleId = (String)role.getId().getId();
		} else {
			roleId = role.getRoleCode();
		}
		List<String> duplicateFields = new ArrayList<String>();
		int dupCount = getJdbcTemplate().queryForInt(
				"select count(role_code) from system_role " +
				"where role_code = ?",
				new Object[] { roleId },
				new int[] { Types.VARCHAR }
			);
		if (dupCount > maxCount) {
			duplicateFields.add("��� ����");
		}
		
		dupCount = getJdbcTemplate().queryForInt(
				"select count(role_name_eng) from system_role " +
				"where role_name_eng = ?",
				new Object[] { role.getNameEn() },
				new int[] { Types.VARCHAR }
			);
		if (dupCount > maxCount) {
			duplicateFields.add("���������� �������� ����");
		}
		
		dupCount = getJdbcTemplate().queryForInt(
				"select count(role_name_rus) from system_role " +
				"where role_name_rus = ?",
				new Object[] { role.getNameRu() },
				new int[] { Types.VARCHAR }
			);
		if (dupCount > maxCount) {
			duplicateFields.add("������� �������� ����");
		}
		if (!duplicateFields.isEmpty())
			throw new DataException("duplicate.role.exception", new Object[]{duplicateFields});
		
		return null;
	}
}
