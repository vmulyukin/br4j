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
package com.aplana.dbmi.module.docflow;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.model.ObjectId;

/**
 * Action ��������� ������ ������� ������������ ��������� �, 
 * �����������, ���������� � �������������.
 * @author larin
 *
 */
public class GetPositionPersons implements Action{
	private static final long serialVersionUID = 1L;
	private ObjectId m_positionId;
	private ObjectId m_departmentId;
	

	public Class<?> getResultType() {
		return ObjectId[].class;
	}

	/**
	 * ��������� �������������� ���������
	 * @param m_positionId
	 */
	public void setPositionId(ObjectId m_positionId) {
		this.m_positionId = m_positionId;
	}

	/**
	 * ��������� �������������� ���������
	 * @return
	 */
	public ObjectId getPositionId() {
		return m_positionId;
	}

	/**
	 * ��������� �������������� �������������
	 * @param m_departmentId
	 */
	public void setDepartmentId(ObjectId m_departmentId) {
		this.m_departmentId = m_departmentId;
	}

	/**
	 * ��������� �������������� �������������
	 * @return
	 */
	public ObjectId getDepartmentId() {
		return m_departmentId;
	}
	
}
