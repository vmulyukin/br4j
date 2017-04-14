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
package com.aplana.quartz.sheduler;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.jboss.SchedulerJdbcDAO;
import com.aplana.dbmi.model.LocalizedString;
import com.aplana.dbmi.task.CronTaskInfo;
import com.aplana.dbmi.task.IntervalTaskInfo;
import com.aplana.dbmi.task.Scheduler;
import com.aplana.dbmi.task.SchedulerException;
import com.aplana.dbmi.task.TaskInfo;
import com.aplana.quartz.utils.XmlUtils;

/**
 * ����� ��� ��������������� �������� ������ ����� "������" �������� ������ 
 * ���������� ������. 
 * ��������������, ��� ��� ����� ����������� � ������ ������� JBoss App Server.
 * TODO: ������� ����������� ������ ���������� JBoss App Srv � ����� ���� ���
 * ������������ "�����������" ����������� ���������.
 * 
 * 
 * @author rabdullin
 */
public class SchedulerAutostartBean
		extends StandardMBean
		implements org.jboss.varia.scheduler.Schedulable
{

	static final long serialVersionUID = 1L;

	static final String CONFIG_FILE = "dbmi/scheduler/config.xml";

	/**
	 * ��� ������� ������� ������� ������������ �����: ����������� ����������� 
	 * ����� � ������� ������������ "������". 
	 */
	static final int DEFAULT_RANDOMADDERMINUTES = 5;

	/**
	 * "̸�����" ����� � �������� ��� ���������� ������������ ������.
	 * �.�. �����, ������������ ������� �������������, � ������� �������� ������ 
	 * ����� �� ����� ��������.
	 */
	static final int DEATH_TIME_START_SEC = 90;

	protected final Log logger = LogFactory.getLog(getClass());

	private final Random random = new Random(System.currentTimeMillis());

	/**
	 * ���-�� �����, ������� ������������ ������ ����������� ��������� ������� 
	 * �������� �� ������� ������� ������ (� ������� ����� ��� ���������� �� 
	 * ���������� ������ ���������� � �� ������� ����� �����).
	 */
	private int randomAdderMaxMinutes = DEFAULT_RANDOMADDERMINUTES;

	public SchedulerAutostartBean() 
		throws NotCompliantMBeanException 
	{
		this( org.jboss.varia.scheduler.Schedulable.class );
		logger.debug("constructor() called");
	}

	/**
	 * @param mbeanInterface
	 * @throws NotCompliantMBeanException
	 */
	public SchedulerAutostartBean(Class mbeanInterface)
			throws NotCompliantMBeanException 
	{
		super( mbeanInterface);
		logger.debug("constructor(interface["+ mbeanInterface +"] ) called");
	}

	/**
	 * @param implementation
	 * @param mbeanInterface
	 * @throws NotCompliantMBeanException
	 */
	public SchedulerAutostartBean(Object implementation, Class mbeanInterface)
			throws NotCompliantMBeanException {
		super(implementation, mbeanInterface);
		if (logger.isDebugEnabled())
			logger.debug("constructor(object["+implementation+"], interface["+mbeanInterface+"]) called");
	}

	/* (non-Javadoc)
	 * @see org.jboss.varia.scheduler.Schedulable#perform(java.util.Date, long)
	 */
	public void perform(Date now, long remainingRepeatitions)
	{
		logger.info( MessageFormat.format( 
				"performint at {0}, remainingRepeatitions: {1} ",
				now, remainingRepeatitions
			));
		run();
	}

	private void run() {
		// �������� �������� ���������������� ����������
		try {
			readConfig();
		} catch (Exception e) { 
			logger.error("Error reading configuration file " + CONFIG_FILE, e);
		}
		try {
			doStartTasks();
		} catch (Exception e) {
			logger.error("problem starting tasks: "+ e.getMessage(), e);
		}
	}

	private void readConfig() throws Exception {
		final URL url = Portal.getFactory().getConfigService().getConfigFileUrl(CONFIG_FILE);
		final File f = new File(url.getFile());
		if (!f.exists()) {
			logger.warn( MessageFormat.format("config not loaded via file not found ''{0}''", f.getPath()));
			return;
		}
		logger.info( MessageFormat.format("loading config from file ''{0}''", f.getPath()));

		final InputStream input = Portal.getFactory().getConfigService().loadConfigFile(CONFIG_FILE);
		try {
			final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);

			final Map<String, String> params = XmlUtils.xmlGetElementsAsMap( 
					doc.getDocumentElement(), "parameter", "name", "value");
			assignParameters(params);
		} finally {
			IOUtils.closeQuietly(input);
		}
	}

	protected void assignParameters( Map<String, ?> params) {
		if (params != null) {
			for (Map.Entry<String, ?> item: params.entrySet()) {
				final Object value = item.getValue();
				setParameter( item.getKey(), (value == null) ? null : value.toString());
			}
		}
	}

	/**
	 * ������� ����������������� ���������
	 * @param name
	 * @param value
	 */
	protected void setParameter(String name, String value) {
		if("randomAdderMaxMinutes".equalsIgnoreCase(name)) {
			if (value == null || value.trim().length() == 0) {
				this.randomAdderMaxMinutes = DEFAULT_RANDOMADDERMINUTES;
			} else { 
				this.randomAdderMaxMinutes = Integer.parseInt(value.trim());
			}
		} else {
			logger.warn( MessageFormat.format( "Ignored unknown parameter ''{0}''=''{1}''", name, value) );
		}
	}

	@SuppressWarnings("unchecked")
	public void doStartTasks()
	{
		String stage = "prepare";
		try {
			final SchedulerJdbcDAO daoObject = new SchedulerJdbcDAO();
			
			stage = "get db data";
			final List<TaskInfo> tasks = daoObject.getActiveDbTasks();

			if (tasks == null || tasks.isEmpty()) {
				logger.warn("No tasks defined -> exiting");
				return;
			}
			logger.info( MessageFormat.format( "Loaded tasks counter is {0}", tasks.size() ));

			stage = "get scheduler";
			final Scheduler scheduler = Portal.getFactory().getSchedulerService();
			stage = "starting tasks at Scheduler";
			int i = 0;
			for (TaskInfo task : tasks) {
				stage = MessageFormat.format("starting task id={0} ''{1}''", 
						task.getId(), 
						task.getModuleName() );

				// ������������� ������ ������� �������
				final Date now = generateStartAfterNow();
				// ����� ��������� �������
				final Date nextFireTime = getNextFireTime(task);
				
				// ��������� ����: ������ ��� ��������� ����, ���� ��� ������ ...
				final boolean isOutOfDate = 
						!(task instanceof CronTaskInfo)
						&& nextFireTime != null
						&& nextFireTime.before(now);
				
				final Date startAt = (isOutOfDate) ? now : nextFireTime;
				task.setStart(startAt);
				try
				{
					scheduler.startTask(task);
				} 
				catch(SchedulerException e) {
					logger.error(
							MessageFormat.format( "Cannot start task id={0}. Skip it. \n", task.getId() ));
					continue;
				}
				if (logger.isDebugEnabled())
					logger.debug( MessageFormat.format(
							"started task (id={0}), module name ''{1}'', cron expression ''{2}'' repeat interval ''{3}'' at {4} \n" +
							"	params: {5} ",
								task.getId(),
								task.getModuleName(), 
								task.getCronExpr(),
								task.getRepeatIntervalStr(),
								task.getStart(),
								task.getArgs()
						));
	
				++i;
			} 
			logger.info( MessageFormat.format("Started {0}/{1} tasks", i, tasks.size() ) );

			// ��� �������� - ������ ������� ����� � ������ ...
			if (logger.isDebugEnabled()) {
				logTaskInfoList(scheduler);
			}
		} catch(Exception e) {
			logger.error(
				MessageFormat.format( "Process error at stage <{0}>: {1} \n", stage, e.getMessage())
				, e);
		}
	}
	
	/**
	 * ���������� ����� ���������� ������� ������ �� ������� ���������� ���������� + �������� ��������
	 * @param task
	 * @return
	 */
	private Date getNextFireTime(TaskInfo task) {
		if(task instanceof IntervalTaskInfo) {
			IntervalTaskInfo intervalTask = (IntervalTaskInfo) task;
			Date lastExec = intervalTask.getLastExecTime();
			if(lastExec == null)
				lastExec = task.getStart();
			return new Date(lastExec.getTime() + intervalTask.getRepeatIntervalMs());
		}
		return task.getStart();
	}


	/**
	 * ������������� ����� ������� ����� ��� "������".
	 * @return ����� �������: [������� ������  + ������ �����] (��� ������) + {@link randomAdderMaxMinutes}
	 */
	private Date generateStartAfterNow() {

		final Calendar c = Calendar.getInstance();
		c.add( Calendar.SECOND, DEATH_TIME_START_SEC);
		c.set( Calendar.SECOND, 0);  // ���������� �� ����� ...

		// ���������� ���������� ���������� ...
		if (randomAdderMaxMinutes > 0) { 
			c.add( Calendar.MINUTE, Math.abs( random.nextInt(randomAdderMaxMinutes)) );
		}

		return c.getTime();
	}

	/**
	 * @param scheduler
	 */
	@SuppressWarnings("unchecked")
	private void logTaskInfoList(Scheduler scheduler) {
		try {
			final StringBuffer buf = new StringBuffer("current task list is: \n");
			final Collection<TaskInfo> names = scheduler.getScheduledTasks();
			buf.append("=======================================================================================================================================================================\n");
			buf.append( MessageFormat.format( "\t{0}\t {1} \t\t\t\t\t {2} \t ''{3}'' \t {4} \t {5}\t {6}\t {7}\n",
					"NN", "ID", "Module Name ", "Method Name", "Start at", "Cron Expression", "Repeat Interval", "Info" )); 
			buf.append("=======================================================================================================================================================================\n");
			int i = 0;
			for (TaskInfo taskInfo : names) {
				++i;
				buf.append(MessageFormat.format( "\t{0}\t {1} \t {2}\t ''{3}'' \t {4} \t {5}\t {6}\t {7}\n", 
						i,
						taskInfo.getId(),
						taskInfo.getModuleName(),
						taskInfo.getMethodName(),
						taskInfo.getStart(), 
						taskInfo.getCronExpr(),
						taskInfo.getRepeatIntervalStr(),
						taskInfo.getInfo() == null ? "" : taskInfo.getInfo()
						));
			}
			buf.append("=======================================================================================================================================================================\n");
			logger.debug(buf);
		} catch (Exception e) {
			logger.error("Scheduled list failure: "+ e.getMessage(), e);
		}
	}
}