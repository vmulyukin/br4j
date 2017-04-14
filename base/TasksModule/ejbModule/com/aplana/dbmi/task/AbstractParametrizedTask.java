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

import com.aplana.dbmi.action.LoadParameters;
import com.aplana.dbmi.action.RemoveParameters;
import com.aplana.dbmi.model.SchedulerParameter;

import java.util.List;

/**
 * ����������� ����� ���������, ������� ��������� � ������� ��������� ����������
 *
 * @author ynikitin
 */
public abstract class AbstractParametrizedTask extends AbstractTask {

	protected List<SchedulerParameter> getTaskParameters(String taskId) {
		// ���� id ��������� �������� �� ����, �� �������� ������� ��������� �������
		if (taskId != null) {
			LoadParameters loadAction = new LoadParameters();
			loadAction.setTask_id(taskId);
			try {
				return serviceBean.doAction(loadAction);
			} catch (Exception e) {
				return null;
			}
		} else {    // ���� �� ���, �� �������� �� ����� ������
			return null;
		}
	}

	protected Long deleteParameters(String taskId, List<Long> paramIds) {
		RemoveParameters removeAction = new RemoveParameters();
		removeAction.setTask_id(taskId);
		removeAction.setParamIds(paramIds);
		try {
			return (Long) serviceBean.doAction(removeAction);
		} catch (Exception e) {
			return null;
		}
	}
}
