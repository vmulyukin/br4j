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
package com.aplana.dbmi.replication.action;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.replication.query.DoGetPerson.PersonWrapper;

public class GetPerson implements Action<PersonWrapper> {
	private static final long serialVersionUID = 1L;
	private String login;
	private String email;
	private String fullName;
	private String uuid;
	
	public GetPerson(String login, String email, String fullName) {
		this.login = login;
		this.email = email;
		this.fullName = fullName;
	}
	
	public GetPerson(String login, String email, String fullName, String uuid) {
		this(login, email, fullName);
		this.uuid = uuid;
	}
	
	public Class<?> getResultType() {
		return PersonWrapper.class;
	}

	public String getLogin() {
		return login;
	}

	public String getEmail() {
		return email;
	}

	public String getFullName() {
		return fullName;
	}
	
	public String getUuid() {
		return uuid;
	}
}