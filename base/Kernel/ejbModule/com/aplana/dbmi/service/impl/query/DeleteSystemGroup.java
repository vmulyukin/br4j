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
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.Group;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.access.AccessRuleManager;

public class DeleteSystemGroup extends DeleteAccessRule implements WriteQuery {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected AccessRuleManager manager;
	
	public void setJdbcTemplate(JdbcTemplate jdbc) {
		super.setJdbcTemplate(jdbc);
		manager = new AccessRuleManager(jdbc);
	}

	public Object processQuery() throws DataException {
		
		// �������� ������ ���������������� ����� ��� �������� ��� ������� ������
		String sqlRoleForDelete = 
				/*"WITH \n" +
						"-- ��������������� ���� � ������ ���� ������� \n" +
						"uniq_roles_var as ( \n" +
						   "select role_code from ( \n" +
						   "select gr.role_code, count(gr.role_code) as cnt from group_role gr \n" +
						   "group by gr.role_code \n" +
						   "order by gr.role_code) as ur \n" +
						   "where ur.cnt = 1 \n" +
						"), \n" +
						"-- ������: ���� - ������ - ���� \n" +
						"group_roles_var as ( \n" +
						   "select prg.prgroup_id, prg.person_id, prg.group_code, gr.role_code from person_role_group prg \n" +
						   "inner join group_role gr on gr.group_code = prg.group_code \n" +
						   "where prg.group_code = ?  \n" +
						   "order by prg.person_id, prg.group_code, gr.role_code \n" +
						"), \n" +
						"-- ���� ������������� �� ��������� ������, ������� ����������� � ������ ������� \n" +
						"group_roles_by_user_var as ( \n" +
						   "select prg.prgroup_id, prg.person_id, prg.group_code, gr.role_code from person_role_group prg \n" +
						   "inner join group_role gr on gr.group_code = prg.group_code \n" +
						   "where prg.group_code != ? \n" +
						   "and prg.person_id in (select distinct person_id from group_roles_var)   \n" +
						   "order by prg.person_id, prg.group_code, gr.role_code \n" +
						"), \n" +
						"-- ������ �� person_excluded_role, ����������� � �������� ������ �� �������� \n" +
						"included_group_roles_var as ( \n" +
						   "select per.prgroup_id,  grbuv.person_id, grbuv.group_code, grbuv.role_code from group_roles_by_user_var grbuv \n" +
						   "inner join person_excluded_role per on grbuv.role_code = per.role_code and grbuv.prgroup_id = per.prgroup_id \n" +
						"), \n" +
						"-- ���������� ���� � �������������� ������������� ������ ������ ������ \n" +
						"uniq_group_roles_var as ( \n" +
						   "select grv.prgroup_id, grv.person_id, grv.group_code, grv.role_code from uniq_roles_var urv \n" +
						   "inner join group_roles_var grv on  \n" +
						   "urv.role_code = grv.role_code \n" +
						"), \n" +
						"-- ��������� ���� �� exluded �� �������� \n" +
						"group_roles_add_excl_var as ( \n" +
							"select ugrv.* from uniq_group_roles_var ugrv \n" +
							"union  \n" +
							"select igrv.* from included_group_roles_var igrv \n" +
						"), \n" +
						"-- �������� �� ���������� ���������� ����� person_ungrouped_role \n" +
						"uniq_group_roles_excl_var as ( \n" +
						   "select graev.* from group_roles_add_excl_var graev \n" +
						   "left join person_ungrouped_role pur on pur.role_code = graev.role_code and pur.person_id = graev.person_id \n" +
						   "where pur.person_id is null and pur.role_code is null \n" +
						"),  \n" +
						"roles_for_delete_var as ( \n" +
						   "select pr.prole_id from person_role pr  \n" +
						   "inner join uniq_group_roles_excl_var ugrev on pr.person_id = ugrev.person_id and pr.role_code = ugrev.role_code \n" +
						") \n" +
						"select rfdv.prole_id from roles_for_delete_var rfdv";*/
				"WITH \n" +
				"-- ������: ���� - ������ - ���� �� ������� excluded \n" +
				"group_roles_var as ( \n" +
				   "select prg.prgroup_id, prg.person_id, prg.group_code, gr.role_code from person_role_group prg \n" +
				   "inner join group_role gr on gr.group_code = prg.group_code \n" +
				   "left join person_excluded_role per on per.prgroup_id = prg.prgroup_id and per.role_code = gr.role_code \n" +
				   "where prg.group_code = ?  \n" +
				   "and per.prgroup_id is NULL and per.role_code is NULL \n" +
				   "order by prg.person_id, prg.group_code, gr.role_code \n" +
				"), \n" +
				"-- ���� ������������� �� ������ �����, ������� ������� �� ���� �� ������� excluded \n" +
				"group_roles_by_user_var as ( \n" +
				   "select prg.prgroup_id, prg.person_id, prg.group_code, gr.role_code from person_role_group prg \n" +
				   "inner join group_role gr on gr.group_code = prg.group_code \n" +
				   "left join person_excluded_role per on per.prgroup_id = prg.prgroup_id and per.role_code = gr.role_code \n" +
				   "where prg.group_code != ? \n" +
				   "and prg.person_id in (select distinct person_id from group_roles_var)   \n" +
				    "and per.prgroup_id is NULL and per.role_code is NULL \n" +
				    "order by prg.person_id, prg.group_code, gr.role_code \n" +
				"), \n" +
				" \n" +
				"-- �������� �� ������� ��������� ������ \n" +
				"group_roles_add_excl_var as ( \n" +
				 "select grv.* from group_roles_var grv \n" +
				 "left join group_roles_by_user_var grbuv \n" +
				 "on grv.role_code = grbuv.role_code and grv.person_id = grbuv.person_id \n" +
				 "where \n" +
				 "grbuv.role_code is NULL and grbuv.person_id is NULL \n" +
				"), \n" +
				"-- �������� �� ���������� ����� person_ungrouped_role \n" +
				"uniq_group_roles_excl_var as ( \n" +
				   "select graev.* from group_roles_add_excl_var graev \n" +
				   "left join person_ungrouped_role pur on pur.role_code = graev.role_code and pur.person_id = graev.person_id \n" +
				   "where pur.person_id is null and pur.role_code is null \n" +
				"),  \n" +
				"roles_for_delete_var as ( \n" +
				   "select pr.prole_id from person_role pr  \n" +
				   "inner join uniq_group_roles_excl_var ugrev on pr.person_id = ugrev.person_id and pr.role_code = ugrev.role_code \n" +
				") \n" +
				"select rfdv.prole_id from roles_for_delete_var rfdv";

		Object objId = this.getId().getId();
		@SuppressWarnings("unchecked")
		List<Role> roleListForDelete = getJdbcTemplate().query(
				sqlRoleForDelete,
				new Object[] { objId, objId },
				new int[] { Types.VARCHAR, Types.VARCHAR },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						final Role role = new Role();
						role.setId(rs.getLong(1));
						return role;
					}
				}
			);
		
		for (Role role : roleListForDelete) {
			manager.cleanAccessListByRole(role.getId());
			getJdbcTemplate().update(
					"DELETE FROM person_role WHERE prole_id=?",
					new Object[] { role.getId().getId() },
					new int[] { Types.NUMERIC }
					);
		}
		
		@SuppressWarnings("unchecked")
		List<Group> groupListForDelete = getJdbcTemplate().query(
				"SELECT prg.prgroup_id FROM person_role_group prg WHERE prg.group_code = ?",
				new Object[] {this.getId().getId()},
				new int[] { Types.VARCHAR},
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						final Group group = new Group();
						group.setId(rs.getLong(1));
						return group;
					}
				}
			);
		
		for(Group group : groupListForDelete) {
			getJdbcTemplate().update(
					"DELETE FROM person_excluded_role WHERE prgroup_id = ?",
					new Object[] { group.getId().getId() }
					);
		}
		
		getJdbcTemplate().update(
				"DELETE FROM person_role_group WHERE group_code = ?",
				new Object[] { this.getId().getId() }
				);

		getJdbcTemplate().update(
				"DELETE FROM group_role WHERE group_code = ?",
				new Object[] { getId().getId() });

		getJdbcTemplate().update(
				"DELETE FROM system_group WHERE group_code = ?",
				new Object[] { getId().getId() });

		return null;
	}
}
