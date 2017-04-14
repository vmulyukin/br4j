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
package com.aplana.agent;

import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import com.aplana.agent.conf.NodeList;
import com.aplana.agent.conf.routetable.Agent;
import com.aplana.agent.plugin.PluginExecutor;

/**
 * implementation of TaskContainer based on java.util.Timer
 */
public class TimerTaskContainer implements TaskContainer {

	protected final Log logger = LogFactory.getLog(getClass());

	protected Scheduler sched;

	public TimerTaskContainer() {
		SchedulerFactory scheduleFactory = new StdSchedulerFactory();
		try {
			sched = scheduleFactory.getScheduler();
		} catch (SchedulerException e) {
			logger.error("Could instantiate standart Quartz Scheduler.", e);
		}
	}

	public boolean startExecuting() {
		try {
			sched.start();
			return true;
		} catch (SchedulerException e) {
			logger.error("Could not start standart Quartz Scheduler.", e);
			return false;
		}
	}

	public boolean stopExecuting() {
		try {
			sched.shutdown(true); // true as an argument means waiting until started task is finished before shutting the scheduler down
			//sched.standby();
			return true;
		} catch (SchedulerException e) {
			logger.error("Could not shutdown standart Quartz Scheduler.", e);
			return false;
		}
	}

	public boolean registerTask(Agent agent, NodeList nodeList) {
		TaskDefinition taskDefinition = new TaskDefinition(agent, nodeList);
		try{
			JobDetail job = new JobDetail(taskDefinition.getTaskName(), "TransportAgent", PluginExecutor.class);
			job.setDescription("Agent "+ taskDefinition.getTaskName() + "with period:"+taskDefinition.getPeriod() 
					+ " secs, cron:"+taskDefinition.getCron()+", startTime:" + taskDefinition.getStartTime());
			JobDataMap params = job.getJobDataMap();
			params.put(TaskContainer.TASK_DEF, taskDefinition);

			Trigger trigger;
			if ((taskDefinition.getCron() == null) 
					|| (taskDefinition.getCron().isEmpty())){
				trigger = new SimpleTrigger(taskDefinition.getTaskName(), "TransportAgent",
						SimpleTrigger.REPEAT_INDEFINITELY,
						taskDefinition.getPeriod()*1000);
			} else {
				trigger = new CronTrigger(taskDefinition.getTaskName(), "TransportAgent", taskDefinition.getCron());
			}
			trigger.setStartTime(taskDefinition.getStartTime());
			sched.scheduleJob(job, trigger);
			return true;
		} catch (ParseException e) {
			logger.error("Error parsing parameters for agent "+taskDefinition.getTaskName(), e);
			return false;
		} catch (org.quartz.SchedulerException e) {
			logger.error("Error scheduling task "+taskDefinition.getTaskName(), e);
			return false;
		}
	}

	public void removeTask(Agent agent) {
		throw new UnsupportedOperationException("Not implemented yet!");
	}
}
