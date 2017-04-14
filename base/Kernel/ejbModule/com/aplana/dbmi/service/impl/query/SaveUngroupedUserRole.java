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

import java.io.StringWriter;
import java.sql.Types;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.aplana.dbmi.action.GetTask;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.model.UngroupedRole;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.access.AccessRuleManager;

/**
 * Query used to save {@link UngroupedRole} object instances.
 * Create/Updates single row in PERSON_UNGROUPED_ROLE and PERSON_ROLE tables.
 */
public class SaveUngroupedUserRole extends SaveQueryBase {
	private static final long serialVersionUID = 1L;
	/**
	 * Identifier of 'New role added' log event
	 */
	public static final String EVENT_ID_CREATE = "NEW_ROLE";
	/**
	 * Identifier of 'Role changed' log event
	 */
	public static final String EVENT_ID_CHANGE = "CHG_ROLE";
	
	private static final String TASK_NAME = "UpdateRulesByRole";
	private static final String CRON_EXPRESSION = "0 0 19 1/1 * ? *";

	private UngroupedRole role;

	/**
	 * @return {@link #EVENT_ID_CREATE} if new {@link UngroupedRole} object is saved,
	 * {@link #EVENT_ID_CHANGE} otherwise.
	 */
	public String getEvent()
	{
		return isNew() ? EVENT_ID_CREATE : EVENT_ID_CHANGE;
	}

	/**
	 * @return identifier of {@link Person} whose role is being saved
	 */
	public ObjectId getEventObject()
	{
		return ((UngroupedRole) getObject()).getPerson();
	}

	protected ObjectId processNew() throws DataException
	{
		role = (UngroupedRole) getObject();
		
		checkLock(role.getPerson());

		int count = getJdbcTemplate().queryForInt(
				"SELECT COUNT(1) FROM person_role pr WHERE pr.role_code = ? and pr.person_id = ?",
				new Object[] { role.getType(), role.getPerson().getId() },
				new int[] { Types.VARCHAR, Types.NUMERIC }
				);
		if (count == 0) {
			long id = (getJdbcTemplate().queryForLong("SELECT nextval('seq_prole_id')"));
			getJdbcTemplate().update(
				"INSERT INTO person_role (prole_id, person_id, role_code) VALUES (?, ?, ?)",
				new Object[] { id, role.getPerson().getId(), role.getType() },
				new int[] { Types.NUMERIC, Types.NUMERIC, Types.VARCHAR }
				);
			addUpdateRoleRulesTask(new ObjectId(Role.class, id));
		}
		
		role.setId(getJdbcTemplate().queryForLong("SELECT nextval('seq_person_ungrouped_role_id')"));
		getJdbcTemplate().update(
				"INSERT INTO person_ungrouped_role (prole_id, person_id, role_code) VALUES (?, ?, ?)",
				new Object[] { role.getId().getId(), role.getPerson().getId(), role.getType() },
				new int[] { Types.NUMERIC, Types.NUMERIC, Types.VARCHAR }
				);


		return role.getId();
	}

	protected void processUpdate() throws DataException
	{
		throw new NotImplementedException("User role update is not implemented");
	}

	/**
	 * Checks validity of {@link UngroupedRole} object being saved.
	 * @throws DataException if no {@link Person} is specified for {@link Role} object, or if
	 * there is an attempt to add second 'Administrator' role to user profile. 
	 */
	public void validate() throws DataException
	{
		this.role = (UngroupedRole) getObject();
		if (role == null || role.getPerson() == null)
			throw new DataException("store.role.nobody");
		if (Role.ADMINISTRATOR.equals(role.getType())) {
			try {
				long id = getJdbcTemplate().queryForLong(
						"SELECT prole_id FROM person_ungrouped_role WHERE person_id=? AND role_code=?",
						new Object[] { role.getPerson().getId(), Role.ADMINISTRATOR },
						new int[] { Types.NUMERIC, Types.VARCHAR }
						);
				if (!new ObjectId(UngroupedRole.class, id).equals(role.getId()))
					throw new DataException("store.role.duplicate");
			} catch (IncorrectResultSizeDataAccessException e) {
				// It's ok, there's no such role for this user
			}
		}
		super.validate();
	}
	
	private void addUpdateRoleRulesTask(ObjectId roleId) throws DataException{
		Calendar startTime = Calendar.getInstance();
		startTime.add(Calendar.DAY_OF_MONTH, 1);
		startTime.set(Calendar.HOUR_OF_DAY, 0);
		startTime.set(Calendar.MINUTE, 0);
		startTime.clear(Calendar.SECOND);
		
		GetTask action = new GetTask();
		action.setDefaultCronExpression(CRON_EXPRESSION);
		action.setTaskName(TASK_NAME);
		action.setDefaultStartTime(startTime.getTime());
		ActionQueryBase aqb = getQueryFactory().getActionQuery(action);
		aqb.setAccessChecker(null);
		aqb.setAction(action);
		aqb.setUser(getSystemUser());
		String taskId = (String)getDatabase().executeQuery(getSystemUser(), aqb);
		if (taskId==null || taskId.isEmpty()){
			logger.warn("Task "+TASK_NAME+" not fount in system. Processor cancel.");
			return;
		}
		
		final String paramData = GenerateParameter(roleId);
		final String sql = 
			"insert into scheduler_parameter(param_id, task_id, param_data, task_module) \n" +
			"select \n" +
			"	nextval('seq_param_id') as param_id, \n" +
			"	st.task_id, \n" +
			"	? as param_data, \n" +
			"	st.task_module \n" +
			"from scheduler_task st \n" +
			"where st.task_id = ?"; 
		int count = getJdbcTemplate().update(sql, new Object[]{new SqlLobValue(paramData), taskId},
				new int[] { Types.BLOB, Types.VARCHAR });
		logger.info("Insert "+count+" parameters for task "+ TASK_NAME +"/"+taskId);
	}
	
	private String GenerateParameter(ObjectId roleId) throws DataException{
		Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element parentNode = doc.createElement("config");
			doc.appendChild(parentNode);
	
			Element cardNode = doc.createElement("proleId");
			cardNode.appendChild(doc.createTextNode(roleId.getId().toString()));
			parentNode.appendChild(cardNode);
			
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD,"xml");
			StringWriter sw = new StringWriter();

			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);
			return sw.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DataException(e);
		}
	}
}
