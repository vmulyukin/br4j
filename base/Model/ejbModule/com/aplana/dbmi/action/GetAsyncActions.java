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

import java.util.List;

import com.aplana.dbmi.model.LogEntry;

/**
 * �������� ��������� {@link com.aplana.dbmi.model.LogEntry LogEntry},
 * ����������� ��������, ���������� ���������� ������������� � ����������� �
 * ���������<br>
 * ���� {@link #setRunActions(boolean)} ���������� � <code>true</code>, ��
 * ������������ ��� ���������� ��������, ����� ������������ ��������, ���������
 * �������. (��-��������� ����� <code>true</code>)
 */
public class GetAsyncActions implements Action<List<LogEntry>> {

	public enum ActionState {
		/**
		 * ���������� ��������
		 */
		RUNNING,
		/**
		 * �������� ��������� �������
		 */
		WAITING,
		/**
		 * ��������, ������������ �������� �� ��������� ��������� ����������
		 */
		REPEATED,
		/**
		 * �������� ��������� ���������� ������� ����� ���������� ����������� �������
		 */
		WAITING_FOR_REPEAT
	}

	private static final long serialVersionUID = -5990507898771553224L;

	private ActionState runActions = ActionState.WAITING;

	@Override
	public Class<?> getResultType() {

		return List.class;
	}

	/**
	 * @return the runActions
	 */
	public ActionState getRunActions() {
		return runActions;
	}

	/**
	 * @param runActions
	 *            the runActions to set
	 */
	public void setRunActions(ActionState runActions) {
		this.runActions = runActions;
	}

}
