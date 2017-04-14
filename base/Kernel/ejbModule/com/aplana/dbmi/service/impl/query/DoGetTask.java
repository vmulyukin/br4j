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

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.GetTask;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceHome;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.task.Scheduler;
import com.aplana.dbmi.task.TaskInfo;
import com.aplana.dbmi.task.TaskInfoBuilder;

public class DoGetTask extends ActionQueryBase {

	public Object processQuery() throws DataException {
		GetTask action = (GetTask)this.getAction();
		final String taskName = action.getTaskName();
		final String defaultCronExpression = action.getDefaultCronExpression();
		final Date defaultStartTime = action.getDefaultStartTime();
		if (taskName==null||taskName.isEmpty()||defaultCronExpression==null||defaultCronExpression.isEmpty()||defaultStartTime==null){
			logger.warn("One or more input parameters are not setting, break.");
			return null;
		}
		
		InitialContext context;
		Scheduler scheduler;
		try {
			context = new InitialContext();
			scheduler = Portal.getFactory().getSchedulerService();;
			if (scheduler==null){
				return null;
			}
			// ���� ������� �������� ����� ����������
			List tasks = (List)scheduler.getScheduledTasks();
			for(Iterator itr = tasks.iterator(); itr.hasNext();){
				TaskInfo task = (TaskInfo)itr.next();
				if (taskName.equalsIgnoreCase(task.getModuleName())){
					return task.getId();
				}
			}
			// ���� �� ����� ����� ����������, ���� ����� ���������
			List names = (List)scheduler.getAvailableTasks();
			for(Iterator itr = names.iterator(); itr.hasNext();){
				String name = (String)itr.next();
				if (taskName.equalsIgnoreCase(name)){
					TaskInfo task = newTask(name, defaultCronExpression, defaultStartTime);
					scheduler.startTask(task);
					return task.getId();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	/**
	 * ��������� �������� ������ ���������
	 * @param name - ��� ���������
	 * @param defaultCronExpression - Cron-��������� ��� ������� �������
	 * @return ������ ���������
	 */
	private TaskInfo newTask(String name, String cronExpression, Date startTime){
		return TaskInfoBuilder.newTaskInfo()
		.forJob(name)
		.withSchedule(cronExpression)
		.withInfo(null)
		.startAt(startTime)
		.persistTask(true).build();
	}

}