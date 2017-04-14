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

//import java.io.IOException;
//import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.AccessListItem;
import com.aplana.dbmi.model.Distribution;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Reference;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.utils.SimpleDBUtils;

/**
 * {@link ObjectQueryBase} descendant used to fetch single {@link Distribution} instance from database 
 */
public class GetDistribution extends ObjectQueryBase
{
	/**
	 * Fetches single {@link Distribution} instance from database
	 * @return fetched {@link Distribution} instance
	 */
	public Object processQuery() throws DataException
	{
		final Distribution distr = (Distribution) getJdbcTemplate().queryForObject(
				"SELECT n.notif_rule_id, n.rule_name, n.rule_description, n.search_param, " +
					"n.creation_date, n.reoccurrence_interval, n.person_id, n.last_send_date, p.full_name " +
				"FROM notification_rule n INNER JOIN person p ON n.person_id=p.person_id " +
				"WHERE n.is_forced=1 AND n.notif_rule_id=?",
				new Object[] { getId().getId() },
				new int[] { Types.NUMERIC },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						Distribution distr = new Distribution();
						distr.setId(rs.getLong(1));
						distr.setName(rs.getString(2));
						distr.setDescription(rs.getString(3));
						distr.setSearchXml(rs.getString(4));
						distr.setCreationDate(rs.getDate(5));
						distr.setFrequency(rs.getString(6));
						distr.setCreator(new ObjectId(Person.class, rs.getLong(7)));
						distr.setCreatorName(rs.getString(9));
						/*try {
							Clob data = rs.getClob(4);
							char[] xml = new char[(int) data.length()];
							data.getCharacterStream().read(xml);
							distr.setSearchXml(new String(xml));
						} catch (IOException e) {
							throw new ExceptionEnvelope(new DataException("fetch.notification.search", e));
						}*/
						distr.setSearchXml(SimpleDBUtils.getClobAsStr(rs, 4));

						return distr;
					}
				});
		distr.setRegions(getJdbcTemplate().query(
				"SELECT v.value_id, v.ref_code, v.value_rus, v.value_eng, " +
					"v.order_in_level, v.is_active, v.parent_value_id " +
				"FROM notif_access_control_list a " +
				"INNER JOIN values_list v ON a.region_value_id=v.value_id " +
				"WHERE a.notif_rule_id=? AND a.region_value_id IS NOT NULL",
				new Object[] { distr.getId().getId() },
				new int[] { Types.NUMERIC },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						ReferenceValue region = new ReferenceValue();
						region.setId(rs.getLong(1));
						region.setReference(new ObjectId(Reference.class, rs.getString(2)));
						region.setValueRu(rs.getString(3));
						region.setValueEn(rs.getString(4));
						region.setOrder(rs.getInt(5));
						region.setActive(rs.getBoolean(6));
						if (rs.getObject(7) != null)
							region.setParent(new ObjectId(ReferenceValue.class, rs.getLong(7)));
						return region;
					}
				}));
		distr.setAccessList(getJdbcTemplate().query(
				"SELECT a.acl_id, a.role_code, v.value_id, v.ref_code, v.value_rus, v.value_eng, " +
					"v.order_in_level, v.is_active, v.parent_value_id, a.person_id, p.full_name " +
				"FROM notif_access_control_list a " +
				//"LEFT OUTER JOIN system_role r ON a.role_code=r.role_code " +
				"LEFT OUTER JOIN values_list v ON a.dept_value_id=v.value_id " +
				"LEFT OUTER JOIN person p ON a.person_id=p.person_id " +
				"WHERE a.notif_rule_id=? AND a.region_value_id IS NULL",
				new Object[] { distr.getId().getId() },
				new int[] { Types.NUMERIC },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						AccessListItem item = new AccessListItem();
						item.setId(rs.getLong(1));
						if (rs.getObject(2) != null) {
							item.setRoleType(rs.getString(2));
						} else if (rs.getObject(3) != null) {
							ReferenceValue dept = new ReferenceValue();
							dept.setId(rs.getLong(3));
							dept.setReference(new ObjectId(Reference.class, rs.getString(4)));
							dept.setValueRu(rs.getString(5));
							dept.setValueEn(rs.getString(6));
							dept.setOrder(rs.getInt(7));
							dept.setActive(rs.getBoolean(8));
							if (rs.getObject(9) != null)
								dept.setParent(new ObjectId(ReferenceValue.class, rs.getLong(9)));
							item.setDepartment(dept);
						} else if (rs.getObject(10) != null) {
							Person person = new Person();
							person.setId(rs.getLong(10));
							person.setFullName(rs.getString(11));
							item.setPerson(person);
						}
						return item;
					}
				}));
		return distr;
	}
}
