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
package com.aplana.dbmi.service;

import java.io.Serializable;
import java.security.Principal;

/**
 * Principal class implementation, used to represent special 'System user'.
 * 'System user' is an account used by users not registered in DBMI system as persons
 * (for example web-services).
 */
public class SystemUser implements Principal, Serializable 
{
	private static final long serialVersionUID = 1L;

	/**
	 * Returns system users login.
	 * @return always returns "__system__" string
	 */
	public String getName() {
		return "__system__";
	}
}
