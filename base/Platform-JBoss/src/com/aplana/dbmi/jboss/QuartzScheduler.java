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

import com.aplana.dbmi.jobs.EjbInvokerInterruptableJob;
import com.aplana.dbmi.task.*;
import com.aplana.dbmi.task.SchedulerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import org.quartz.Scheduler;

import javax.naming.*;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.jobs.ee.ejb.EJBInvokerJob;

import com.aplana.dbmi.task.CronTaskInfo;
import com.aplana.dbmi.task.IntervalTaskInfo;
import com.aplana.dbmi.task.SchedulerException;
import com.aplana.dbmi.task.TaskInfo;
import com.aplana.dbmi.task.TaskInfoBuilder;
public class QuartzScheduler implements com.aplana.dbmi.task.Scheduler
{
	public static final String TASK_METHOD = "process";
	public static final String GROUP_NAME = "DBMI";
	public static final String INTERVAL_TASK = "INTERVAL_TASK";
	public static final String CRON_TASK = "CRON_TASK";
	
	private static final String JOB_LISTENER_NAME = "jobListener";
	
	protected Log logger = LogFactory.getLog(getClass());
	
	private Context ctx;
	private Scheduler quartz;
	private SchedulerJdbcDAO daoObject;
	
	public QuartzScheduler() {
		try {
			ctx = new InitialContext();
			quartz = (Scheduler) ctx.lookup("Quartz");
			quartz.addJobListener(new QuartzJobListener());
			daoObject = new SchedulerJdbcDAO();
		} catch (NamingException e) {
			logger.error("Error connecting Quartz service", e);
		} catch (org.quartz.SchedulerException e) {
			logger.error("Error creating Quartz service", e);
		}
	}
	
	public void cancelTask(String id) throws SchedulerException {
		cancelTask(id, false);
	}

	public void cancelTask(String id, boolean isDeleteParams) throws SchedulerException {
		if (quartz == null)
			throw new IllegalStateException("Quartz service not initialized");
		try {
			quartz.unscheduleJob(id, GROUP_NAME);
			daoObject.deleteTask(id);
			if (isDeleteParams){
				daoObject.deleteParams(id);
			} else {
				daoObject.unbindParams(id);
			}
		} 
		catch (org.quartz.SchedulerException e) {
			logger.error("Error cancelling task", e);
			throw new SchedulerException(e.getMessage());
		}
		catch (SQLException e) {
			logger.error("Error deleting task from database", e);
			throw new SchedulerException(e.getMessage());
		}
	}
	
	public Collection getAvailableTasks() throws SchedulerException {
		if (ctx == null)
			throw new IllegalStateException("Initial context not initialized");
		final Collection tasks = new ArrayList();
		try {
			NamingEnumeration names = ctx.list(JNDI_TASK_ROOT);
			while (names.hasMoreElements()) {
				NameClassPair item = (NameClassPair) names.next();
				tasks.add(item.getName());
			}
			return tasks;
		} catch (NamingException e) {
			logger.error("Error fetching list of available tasks", e);
			throw new SchedulerException(e.getMessage());
		}
	}

	public Collection getScheduledTasks() throws SchedulerException {
		if (quartz == null)
			throw new IllegalStateException("Quartz service not initialized");
		final Collection tasks = new ArrayList(); // <TaskInfo>
		try {
			String[] names = quartz.getTriggerNames(GROUP_NAME);
			for (int i = 0; i < names.length; i++) {
				Trigger trigger = quartz.getTrigger(names[i], GROUP_NAME);
				String ejbTaskName = quartz.getJobDetail(trigger.getJobName(), GROUP_NAME).getJobDataMap().getString(EjbInvokerInterruptableJob.EJB_JNDI_NAME_KEY);
				TaskInfoBuilder taskBuilder = TaskInfoBuilder.newTaskInfo()
					.withIdentity(trigger.getName())
					.forJob(ejbTaskName.substring(ejbTaskName.lastIndexOf("/") + 1))
					.startAt(trigger.getStartTime())
					.withInfo(quartz.getJobDetail(trigger.getJobName(), GROUP_NAME).getDescription());

				if (trigger instanceof SimpleTrigger)
				{
					taskBuilder.withSchedule(((SimpleTrigger) trigger).getRepeatInterval());
				}
				else if (trigger instanceof CronTrigger)
				{
					taskBuilder.withSchedule(((CronTrigger) trigger).getCronExpression());
				}
				else
				{
					logger.error("Unknown task type");
					continue;
				}
				tasks.add(taskBuilder.build());
			}
		} catch (org.quartz.SchedulerException e) {
			logger.error("Error fetching list of scheduled tasks", e);
			throw new SchedulerException(e.getMessage());
		}
		return tasks;
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.task.Scheduler#startTask(com.aplana.dbmi.task.TaskInfo)
	 */
	public void startTask(TaskInfo task) throws SchedulerException 
	{
		try {
			JobDetail job = new JobDetail(generateModuleId(task.getModuleName(), task.getId()), GROUP_NAME, EjbInvokerInterruptableJob.class);
			job.setDescription(task.getInfo());
			
			job.addJobListener(JOB_LISTENER_NAME);
			
			JobDataMap params = job.getJobDataMap();
			params.put(EjbInvokerInterruptableJob.EJB_JNDI_NAME_KEY, JNDI_TASK_ROOT + "/" + task.getModuleName());
			params.put(EjbInvokerInterruptableJob.EJB_METHOD_KEY, task.getMethodName());
			params.put(EjbInvokerInterruptableJob.EJB_ARGS_KEY, task.getArgs());
			params.put(EjbInvokerInterruptableJob.EJB_ARG_TYPES_KEY, task.getArgTypes());
			
			Trigger trigger = null;

			if (task instanceof CronTaskInfo)
			{
				logger.info( MessageFormat.format( "starting job name ''{0}'' by cron expression ''{1}'' at {2}",
						new Object[] {task.getModuleName(), task.getCronExpr(), task.getStart() }));

				trigger = new CronTrigger(task.getId(), GROUP_NAME, task.getCronExpr());

				//��������� ����������� ������� ������� ���������� ���������� ����� ����� 1 ���. ����� Cron expression.
				Date firstFireTime = trigger.getFireTimeAfter(task.getStart());
				Date secondFireTime = trigger.getFireTimeAfter(firstFireTime);

				long sec;
				// ������� ����� ����� ���������� ������ ������ � ��������
				if (firstFireTime != null && secondFireTime != null)
				{
					sec = (secondFireTime.getTime() - firstFireTime.getTime())/1000;
					if (sec < 60 )
					{
						logger.error(
	            			MessageFormat.format(
	            			"Cannot set repeat interval less than one minute", 
							sec )
						);
						throw new SchedulerException(
	            			MessageFormat.format(
	    	            	"Repeat interval should be more than one minute", 
	    					sec )
						);
					}
				}
			}
			else if (task instanceof IntervalTaskInfo)
			{
				logger.info( MessageFormat.format( "starting job name ''{0}'' by interval ''{1}'' at {2}",
						new Object[] {task.getModuleName(), task.getRepeatIntervalStr(), task.getStart() }));
				
				trigger = new SimpleTrigger(task.getId(), GROUP_NAME,
                        SimpleTrigger.REPEAT_INDEFINITELY,
                        ((IntervalTaskInfo)task).getRepeatIntervalMs());
				trigger.setStartTime(task.getStart());
			}

			quartz.scheduleJob(job, trigger);
			if (task.isPersistent())
			{
				daoObject.saveTask(task, GROUP_NAME);
			}
			
		} catch (ParseException e) {
			logger.error("Error parsing parameters", e);
			throw new SchedulerException(e.getMessage());
		} catch (org.quartz.SchedulerException e) {
			logger.error("Error scheduling task", e);
			throw new SchedulerException(e.getMessage());
		} catch (SQLException e) {
			logger.error("Error saving task to database", e);
			throw new SchedulerException(e.getMessage());
		} catch (Exception e) {
			throw new SchedulerException(e.getMessage());
		}
	}
	
	private class QuartzJobListener implements JobListener {

		public String getName() {
			return JOB_LISTENER_NAME;
		}

		public void jobExecutionVetoed(JobExecutionContext ctx) {
			String jobName = ctx.getJobDetail() != null ? ctx.getJobDetail().getName() : "";
			if(logger.isDebugEnabled())
				logger.debug("The job "+jobName+" to be vetoed");
		}

		public void jobToBeExecuted(JobExecutionContext ctx) {
			String jobName = ctx.getJobDetail() != null ? ctx.getJobDetail().getName() : "";
			if(logger.isDebugEnabled())
				logger.debug("The job "+jobName+" to be executed");
		}

		/**
		 * When job was executed then updating the last execution time of this job
		 */
		public void jobWasExecuted(JobExecutionContext ctx,
				JobExecutionException ex) {
			String jobName = ctx.getJobDetail() != null ? ctx.getJobDetail().getName() : "";
			if(logger.isDebugEnabled()) {
				if(ex == null)
					logger.debug("The job "+jobName+" was executed sucessfully");
				else
					logger.debug("The job "+jobName+" was executed with exception: "+ex.getMessage());
			}
			Map jobData = ctx.getJobDetail().getJobDataMap();
			if(jobData != null) {
				Object[] args = (Object[])jobData.get("args");
				if(args != null && args.length > 0) {
					String taskId = "";
					for(Object o : args) {
						if(o instanceof Map) {	
							Map m = (Map)o;
							taskId = (String)m.get(TaskInfoBuilder.CURR_TASK_ID);
							if(taskId != null)
								break;
						}
					}
					if(taskId != null && !taskId.equals(""))
						try {
							daoObject.updateLastExecTime(taskId, new Date());
						} catch (Exception e) {
							logger.error("An error has occured while trying to update task with id: "+taskId);
						}
				}
			}
		}
		
	}
	
	
	public int getSchedulerParametersCount(String id)
			throws SchedulerException {
		// TODO Auto-generated method stub
		return daoObject.getTaskParametersCount(id);
	}

	private String generateModuleId(String name, String id) {
		return name + "_" + id;
	}
}
