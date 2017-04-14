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
package com.aplana.dbmi.login;

import java.util.Date;

/**
 * Represents DTO for storing SSO Login Data
 *
 */
public class SSOLoginData {
	
	/**
	 * unique session identificator
	 */
	private String id;
	/**
	 * date when SSO session was created at external system in UTC format 
	 */
	private long milliseconds;
	
	/**
	 * login name at external system 
	 */
	private String login;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getMilliseconds() {
		return milliseconds;
	}

	public void setMilliseconds(long milliseconds) {
		this.milliseconds = milliseconds;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	} 
	
	
	
	

}
