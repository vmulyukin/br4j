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
package com.aplana.dbmi.jboss;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.task.CronTaskInfo;
import com.aplana.dbmi.task.IntervalTaskInfo;
import com.aplana.dbmi.task.TaskInfo;
import com.aplana.dbmi.task.TaskInfoBuilder;

public class SchedulerJdbcDAO {
	protected Log logger = LogFactory.getLog(getClass());

	public static final String INSERT_ARGS_SQL = "INSERT INTO xml_data (xml_data) VALUES (?)";
	public static final String INSERT_INTERVAL_TASK_SQL = "INSERT INTO scheduler_task " +
		"(task_module, \"interval\", unit, \"date\", info, task_group, args_xml_data_id, task_id) \n"
		+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	public static final String INSERT_CRON_TASK_SQL = "INSERT INTO scheduler_task " +
		"(task_module, \"date\", cron_expr, info, task_group, args_xml_data_id, task_id) \n"
		+ " VALUES (?, ?, ?, ?, ?, ?, ?)";
	
	public static final String DELETE_ARGS = "DELETE FROM xml_data WHERE xml_data_id =(SELECT args_xml_data_id FROM scheduler_task WHERE task_id = ?)";
	public static final String DELETE_PARAMS = "DELETE FROM scheduler_parameter WHERE task_id = ?";
	public static final String DELETE_TASK = "DELETE FROM scheduler_task WHERE task_id = ?";
	public static final String UNBIND_PARAMS = "UPDATE scheduler_parameter SET task_id = null WHERE task_id = ?";
	public static final String BIND_PARAMS = "UPDATE scheduler_parameter SET task_id = ? WHERE task_module = ? and task_id is NULL";
	
	public static final String GET_ACTIVE_TASKS = "SELECT stsk.task_id, stsk.task_module, stsk.\"interval\", stsk.unit, \n" +
		" stsk.\"date\", stsk.info, \n" +
		" xmld.xml_data, stsk.cron_expr, stsk.task_group, \n" +
		" stsk.last_exec_time \n" +
		" FROM scheduler_task stsk \n" +
		" LEFT JOIN xml_data xmld ON xmld.xml_data_id = stsk.args_xml_data_id \n" +
		" WHERE stsk.is_active = 1\n";

	public static final String GET_SCHEDULER_PARAMS_COUNT = "SELECT count(sp.param_id) \n" +
	" FROM scheduler_parameter sp \n" +
	" WHERE sp.task_id = ?\n";

	public static final String UPDATE_TASK_LAST_EXEC_TIME = "UPDATE scheduler_task SET last_exec_time = ? WHERE task_id = ?";
	
	public DataSource getDataSource() throws NamingException {
		InitialContext context = new InitialContext();
		DataSource dataSource = (DataSource) context.lookup("java:/jdbc/DBMIDS");
		return dataSource;
	}
	
	public Connection getConnection() throws SQLException, NamingException {
		Connection con = getDataSource().getConnection();
		return con;
	}

	
	public int getTaskParametersCount(String id){
		int paramsCount = 0;

		try
		{
			final JdbcTemplate jdbc = new JdbcTemplate(getDataSource());

			paramsCount = jdbc.queryForInt( GET_SCHEDULER_PARAMS_COUNT, new Object[]{id}, new int[] { Types.VARCHAR });
		}
		catch (Exception e)
		{
			logger.error("Error checkink params for task "+id, e);
		}
		
		return paramsCount;
	}
	
	 public String saveTask (final TaskInfo task, final String groupName) throws SQLException
	 {
		String taskId = null;
		Long args_xml_data_id = null;
		ResultSet generatedKeys = null;
		PreparedStatement insertArgs =null;
		PreparedStatement insertIntervalTask = null;
		PreparedStatement insertCronTask = null;
		Connection con = null;

		try
		{
			con = getConnection();
			con.setAutoCommit(false);

			//��������� ���������
			if (task.getXmlConfig() != null)
			{	
				insertArgs = con.prepareStatement(INSERT_ARGS_SQL, new String[] {"xml_data_id"});
				byte[] xmlBytes  = task.getXmlConfig().getBytes(); 
				insertArgs.setBinaryStream(1,new ByteArrayInputStream(xmlBytes),xmlBytes.length);
				insertArgs .executeUpdate();
				generatedKeys = insertArgs.getGeneratedKeys();
		        if (generatedKeys.next()) {
		             args_xml_data_id = generatedKeys.getLong(1);
		        } else {
		            throw new SQLException("Creating task failed, no generated key obtained after xml_data insert.");
		        }
			}
			//��������� ������������ ������
			if (task instanceof IntervalTaskInfo)
			{
				insertIntervalTask = con.prepareStatement(INSERT_INTERVAL_TASK_SQL);
				insertIntervalTask.setString(1, task.getModuleName());
				insertIntervalTask.setLong(2, ((IntervalTaskInfo)task).getInterval());
				insertIntervalTask.setString(3, ((IntervalTaskInfo)task).getUnit());
				insertIntervalTask.setTimestamp(4, new Timestamp(toUTC(task.getStart()).getTime()));
				if (task.getInfo() != null && !task.getInfo().isEmpty())
					insertIntervalTask.setString(5, task.getInfo());
				else
					insertIntervalTask.setNull(5, Types.CHAR);

				insertIntervalTask.setString(6, groupName);

				if (args_xml_data_id != null)
					insertIntervalTask.setLong(7, args_xml_data_id);
				else
					insertIntervalTask.setNull (7, Types.NUMERIC);
				
				insertIntervalTask.setString(8, task.getId());
				
				insertIntervalTask .executeUpdate();
			}
			//��������� cron ������
			else if (task instanceof CronTaskInfo)
			{
				insertCronTask = con.prepareStatement(INSERT_CRON_TASK_SQL);
				insertCronTask.setString(1, task.getModuleName());
				insertCronTask.setTimestamp(2, new Timestamp(toUTC(task.getStart()).getTime()));
				insertCronTask.setString(3, task.getCronExpr());
				if (task.getInfo() != null && !task.getInfo().isEmpty())
					insertCronTask.setString(4, task.getInfo());
				else
					insertCronTask.setNull(4, Types.CHAR);

				insertCronTask.setString(5, groupName);

				if (args_xml_data_id != null)
					insertCronTask.setLong(6, args_xml_data_id);
				else
					insertCronTask.setNull(6, Types.NUMERIC);
				
				insertCronTask.setString(7, task.getId());

				insertCronTask .executeUpdate();
			}
			bindParams(task.getId(), task.getModuleName());
            con.commit();
       
		} 
		catch (SQLException e ) {
			if (con != null) {
	    			logger.error("Transaction is being rolled back", e);
	                con.rollback();
			}
			throw e;
		}
		catch (Exception e){
			logger.error(
					MessageFormat.format( "Task save error {0} \n", e.getMessage()), e );

		} finally {
			try
			{
				if (insertArgs != null) {
					insertArgs.close();
				}
				if (insertIntervalTask != null) {
					insertIntervalTask.close();
				}
				if (insertCronTask != null) {
					insertCronTask.close();
				}
				if (con != null) {
					con.close();
				}
			}
			catch (SQLException e )
			{
				logger.error("Error during resource cleanup", e);
			}
		}
		return taskId;
	 }
	 
	 public void deleteTask (String id) throws SQLException
	 {
		PreparedStatement selectArgs =null;
		PreparedStatement deleteArgs = null;
		PreparedStatement deleteTask = null;
		Connection con = null;
		try
		{
			con = getConnection();
			con.setAutoCommit(false);

			//������� ���������
			deleteArgs = con.prepareStatement(DELETE_ARGS);

			deleteArgs.setString(1, id);
			deleteArgs.executeUpdate();

			//������� ������
			deleteTask = con.prepareStatement(DELETE_TASK);
			deleteTask.setString(1, id);
			deleteTask.executeUpdate();
			
            con.commit();

		} catch (SQLException e ) {
			if (con != null) {
	    		logger.error("Transaction is being rolled back", e);
	    		con.rollback();
			}
			throw e;
		}
		catch (Exception e){
			logger.error(
					MessageFormat.format( "Task save error {0} \n", e.getMessage()), e );

		} finally {
			try
			{
				if (selectArgs != null) {
					selectArgs.close();
				}
				if (deleteArgs != null) {
					deleteArgs.close();
				}
				if (deleteTask != null) {
					deleteTask.close();
				}
				if (con != null) {
					con.close();
				}
			}
			catch (SQLException e )
			{
				logger.error("Error during resource cleanup", e);
			}
		}
	}
	 
	 public void deleteParams (String id) throws SQLException
	 {
		PreparedStatement deleteParams = null;
		Connection con = null;
		try
		{
    		if (logger.isDebugEnabled())
    			logger.debug(MessageFormat.format("Try delete params for task ''{0}''", new Object[]{id}));
			con = getConnection();
			con.setAutoCommit(false);

			//������� ��������� ����������
			deleteParams = con.prepareStatement(DELETE_PARAMS);
			deleteParams.setString(1, id);
			int updateCount = deleteParams.executeUpdate();
    		if (logger.isDebugEnabled())
    			logger.debug(MessageFormat.format("Delete {0} params for task ''{1}''", new Object[]{updateCount, id}));
			
            con.commit();
		} catch (SQLException e ) {
			if (con != null) {
	    		logger.error("Transaction is being rolled back", e);
	    		con.rollback();
			}
			throw e;
		}
		catch (Exception e){
			logger.error(
					MessageFormat.format( "Params for task delete error {0} \n", e.getMessage()), e );

		} finally {
			try
			{
				if (deleteParams != null) {
					deleteParams.close();
				}
				if (con != null) {
					con.close();
				}
			}
			catch (SQLException e )
			{
				logger.error("Error during resource cleanup", e);
			}
		}
	}
	 
	 public void unbindParams (String id) throws SQLException
	 {
		PreparedStatement unbindParams = null;
		Connection con = null;
		try
		{
    		if (logger.isDebugEnabled())
    			logger.debug(MessageFormat.format("Try unbind params for task ''{0}''", new Object[]{id}));
			con = getConnection();
			con.setAutoCommit(false);

			//������� ��������� ����������
			unbindParams = con.prepareStatement(UNBIND_PARAMS);
			unbindParams.setString(1, id);
			int updateCount = unbindParams.executeUpdate();
    		if (logger.isDebugEnabled())
    			logger.debug(MessageFormat.format("Unbind {0} params for task ''{1}''", new Object[]{updateCount, id}));
			
            con.commit();

		} catch (SQLException e ) {
			if (con != null) {
	    		logger.error("Transaction is being rolled back", e);
	    		con.rollback();
			}
			throw e;
		}
		catch (Exception e){
			logger.error(
					MessageFormat.format( "Params for task unbind error {0} \n", e.getMessage()), e );

		} finally {
			try
			{
				if (unbindParams != null) {
					unbindParams.close();
				}
				if (con != null) {
					con.close();
				}
			}
			catch (SQLException e )
			{
				logger.error("Error during resource cleanup", e);
			}
		}
	}
	 
	 public void bindParams (String id, String name) throws SQLException
	 {
		PreparedStatement bindParams = null;
		Connection con = null;
		try
		{
			con = getConnection();
			con.setAutoCommit(false);

			//������� ��������� ����������
			bindParams = con.prepareStatement(BIND_PARAMS);
			bindParams.setString(1, id);
			bindParams.setString(2, name);
			bindParams.executeUpdate();
			
            con.commit();
		} catch (SQLException e ) {
			if (con != null) {
	    		logger.error("Transaction is being rolled back", e);
	    		con.rollback();
			}
			throw e;
		}
		catch (Exception e){
			logger.error(
					MessageFormat.format( "Params for task bind error {0} \n", e.getMessage()), e );

		} finally {
			try
			{
				if (bindParams != null) {
					bindParams.close();
				}
				if (con != null) {
					con.close();
				}
			}
			catch (SQLException e )
			{
				logger.error("Error during resource cleanup", e);
			}
		}
	}
	 
	@SuppressWarnings("unchecked")
	public List<TaskInfo> getActiveDbTasks()
	{
		List<TaskInfo> tasks = null;

		try
		{
			final JdbcTemplate jdbc = new JdbcTemplate(getDataSource());

			tasks = jdbc.query( GET_ACTIVE_TASKS, new RowMapper() {
					public Object mapRow(final ResultSet rs, final int rowNum)
							throws SQLException
					{
						// ��������� xml-��������� ...
						String xml_text = null;
						if (rs.getObject("xml_data") != null) {
							try {
								xml_text = new String( rs.getBytes("xml_data"), "UTF-8");

							} catch (UnsupportedEncodingException e) {
								logger.error(
										MessageFormat.format("Fail to get xml task args as 'UTF-8' for scheduler_task id={0} \n: {1} ", 
										rs.getLong("task_id"), e.getMessage() )
										, e);
							}
						}

						final TaskInfoBuilder<TaskInfo> taskBuilder = TaskInfoBuilder.newTaskInfo()
								.withIdentity(rs.getString("task_id"))
								.forJob(rs.getString("task_module"))
								.persistTask(false);

						if (rs.getObject("date") != null)
							taskBuilder.startAt(setValueWithTZ(rs.getTimestamp("date")));

						if (rs.getObject("info") != null)
							taskBuilder.withInfo(rs.getString("info"));

						if (rs.getObject("cron_expr") != null)
							taskBuilder.withSchedule(rs.getString("cron_expr"));
						else if (rs.getObject("interval") != null && rs.getObject("unit") != null)
							taskBuilder.withSchedule(rs.getLong("interval"), rs.getString("unit"));
						
						if(rs.getObject("last_exec_time") != null)
							taskBuilder.setLastExecTime(setValueWithTZ(rs.getTimestamp("last_exec_time")));

						if (xml_text != null)
							taskBuilder.usingXmlConfig(xml_text);

						TaskInfo task = taskBuilder.build();

						return task;
					}
				}
			);
		}
		catch (Exception e)
		{
			logger.error("Error loading active tasks", e);
		}
		
		return tasks;
	}
	
	public void updateLastExecTime(String taskId, Date date) throws Exception {
		final JdbcTemplate jdbc = new JdbcTemplate(getDataSource());
		jdbc.update(UPDATE_TASK_LAST_EXEC_TIME, new Object[]{
				new Timestamp(toUTC(date).getTime()),
				taskId
		});
	}
	
	private Date toUTC(Date value) {
		if (value != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(value);
			final int millisTZO = c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET);
			return new Date(c.getTimeInMillis() - millisTZO);
		} else {
			return value;
		}
    }
	
	private Date setValueWithTZ(Date value) {
		if (value!=null){
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(value);
			final int millisTZO = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);
			return new Date(calendar.getTimeInMillis() + millisTZO); 
		} else {
			return value;
		}		
	}
}
