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
package com.aplana.dbmi.task;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.aplana.dbmi.task.TaskInfo;

public interface Scheduler
{
	public static final String JNDI_TASK_ROOT = "ejb/task";

	/**
	 * �������� ������ �������� ������������������ � ������� �������, ������� 
	 * ������ ����� �����������. 
	 * @return Collection<String>
	 * @throws SchedulerException
	 */
	public Collection getAvailableTasks() throws SchedulerException;

	/**
	 * �������� ������ ������� ����� ������������.
	 * @return Collection<com.aplana.dbmi.task.TaskInfo>
	 * @throws SchedulerException
	 */
	public Collection getScheduledTasks() throws SchedulerException;

	/**
	 * �������� � ����������� ����� ������.
	 * @param TaskInfo
	 * @return void
	 * @throws SchedulerException
	 */
	public void startTask(TaskInfo task)
			throws SchedulerException;

	/**
	 * ������ �� �������� ������ ������������ ��������� ������.
	 * @param task id
	 * @throws SchedulerException
	 */
	public void cancelTask(String id) throws SchedulerException;

	/**
	 * ������ �� �������� ������ ������������ ��������� ������ � ���������/����������� ���������� �������.
	 * @param task id
	 * @throws SchedulerException
	 */
	public void cancelTask(String id, boolean isDeleteParams) throws SchedulerException;
	
	/**
	 * �������� ���������� ���������� ���������� ��� ������
	 * @param task id
	 * @throws SchedulerException
	 */
	public int getSchedulerParametersCount(String id) throws SchedulerException;
}
