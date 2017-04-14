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
package com.aplana.dbmi.model.filter;

/**
 * {@link Filter} implementation used to search Users by string pattern.
 * This filter selects users whose login of full name contains given string pattern as substring.
 * String comparison is case insensitive. 
 */
public class UserIdFilter implements Filter
{
	private static final long serialVersionUID = 1L;
	private String string;
	
	/**
	 * Default constructor
	 */
	public UserIdFilter() {
	}
	
	/**
	 * Creates UserIdFilter with given string pattern
	 * @param string string pattern to compare with login and fullName properties of listed users
	 */
	public UserIdFilter(String string) {
		this.string = string;
	}

	/**
	 * Gets search string pattern
	 * @return search string pattern
	 */
	public String getString() {
		return string;
	}

	/**
	 * Sets search string pattern
	 * @param string string to be compared with login 
	 * and fullName properties of listed users 
	 */
	public void setString(String string) {
		this.string = string;
	}
}
