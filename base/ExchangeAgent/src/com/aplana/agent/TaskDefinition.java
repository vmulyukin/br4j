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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.aplana.agent.conf.NodeList;
import com.aplana.agent.conf.routetable.Agent;

import org.apache.commons.lang.StringUtils;

/**
 * Bean defines a task and store state of execution order
 */
public class TaskDefinition {

	private final String taskName;
	private final String agentName;
	private final NodeList nodes;
	private final Date startTime;
	private final Long period;
	private final Long retries;
	private final String cron;
	private final String configuration;
	
	private Date nextStartTime;
	private Long iteration;
	
	public TaskDefinition(Agent agent, NodeList nodes) {
		if (StringUtils.isBlank(agent.getName())){
			throw new IllegalArgumentException("Task name is empty");
		}
		this.period = agent.getPeriod();
		if (period != null && period <= 0){
			throw new IllegalArgumentException("Period parameter should be positive integer. Check bean = \"" + agent.getName() + "\n in Route Table");
		}
		this.retries = agent.getRetries();
		if (retries <= 0){
			throw new IllegalArgumentException("Retries parameter should be positive integer. Check bean = \"" + agent.getName() + "\n in Route Table");
		}
		this.cron = agent.getCron();
		this.nodes = nodes;
		this.configuration = agent.getConfig();
		
		StringBuilder nameSuffix = new StringBuilder();
		nameSuffix.append("c");
		if (cron != null){
			nameSuffix.append(cron);
		} else {
			nameSuffix.append("*");
		}
		nameSuffix.append(".p");
		if (period != null){
			nameSuffix.append(period);
		} else {
			nameSuffix.append("*");
		}
		nameSuffix.append(".r");
		if (retries != null){
			nameSuffix.append(retries);
		} else {
			nameSuffix.append("*");
		}
		//nameSuffix.append(".cfg:");
		nameSuffix.append(".");
		if (configuration != null){
			nameSuffix.append(configuration);
		} else {
			nameSuffix.append("*");
		}
		
		this.taskName = agent.getName() + "-" + nameSuffix;
		this.agentName = agent.getName();
		
		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.SECOND, 10); // ������ ����� ������ - ����� 10 ������
		startTime = cal.getTime();
		nextStartTime = startTime;
		
		iteration = 0l;
	}

	public String getAgentName() {
		return agentName;
	}
	
	@Override
	public String toString() {
		return "Task " + agentName + " will be executed at "
				+ nextStartTime + ". Current iteration is " + iteration;
	}

	public String getCron() {
		return cron;
	}

	public String getConfiguration() {
		return configuration;
	}

	public NodeList getNodes() {
		return nodes;
	}

    public Date getStartTime() {
		return startTime;
	}

	public Long getPeriod() {
		return period;
	}

	public Date getNextStartTime() {
		return nextStartTime;
	}

	public void setNextStartTime(Date nextStartTime) {
		this.nextStartTime = nextStartTime;
	}

	public Long getIteration() {
		return iteration;
	}

	public void setIteration(Long iteration) {
		this.iteration = iteration;
	}

	public Long getRetries() {
		return retries;
	}

	public String getTaskName() {
		return taskName;
	}
}
