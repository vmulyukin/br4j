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

/**
 * �����, ����������� ������� � ������������ ������������ �����
 * @author ynikitin
 *
 */
public class CheckRolesForUser  implements Action {
	private static final long serialVersionUID = 1L;
	
	private String personLogin;
	private String roles;	// ������ ����������� �����  
	public static final String ROLES_DELIMETER = ",";	// ����������� ��� ������ �����
	
	public String getPersonLogin() {
		return personLogin;
	}

	public void setPersonLogin(String personLogin) {
		this.personLogin = personLogin;
	}

	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}

	@Override
	public Class<?> getResultType() {
		return Boolean.class;
	}
}