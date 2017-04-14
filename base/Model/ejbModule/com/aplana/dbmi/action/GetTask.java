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
package com.aplana.dbmi.action;

import java.util.Date;

import com.aplana.dbmi.model.ObjectId;

/**
 * �����, ������� �� �������� �������� ���������� task_id ����������� � ������� ���������
 * ���� ��������� ���, �� ��������� ����� � �������� �� ���������  
 * @author ynikitin
 *
 */
public class GetTask implements Action {
	private String taskName;
	private String defaultCronExpression;
	private Date defaultStartTime;

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getDefaultCronExpression() {
		return defaultCronExpression;
	}

	public void setDefaultCronExpression(String defaultCronExpression) {
		this.defaultCronExpression = defaultCronExpression;
	}

	public Date getDefaultStartTime() {
		return defaultStartTime;
	}

	public void setDefaultStartTime(Date defaultStartTime) {
		this.defaultStartTime = defaultStartTime;
	}

	public Class getResultType() {
		// TODO Auto-generated method stub
		return String.class;
	}
}