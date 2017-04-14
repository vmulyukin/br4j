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

import java.util.Collection;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;

/**
 * ���� ��� ���������  ������������� �� ��� ������������ �� ������ ������������� ���� ��� ���������.
 */
public class NotifPersonFlagGroupChecker implements Action{
	
	private Collection<Person> persons;
	private ObjectId fieldId;
	private ObjectId flagId;

	@Override
	public Class getResultType() {
		return null;
	}

	public Collection<Person> getPersons() {
		return persons;
	}

	public void setPersons(Collection<Person> persons) {
		this.persons = persons;
	}

	public ObjectId getFlagId() {
		return flagId;
	}

	public void setFlagId(ObjectId flagId) {
		this.flagId = flagId;
	}

	public ObjectId getFieldId() {
		return fieldId;
	}

	public void setFieldId(ObjectId fieldId) {
		this.fieldId = fieldId;
	}



}
