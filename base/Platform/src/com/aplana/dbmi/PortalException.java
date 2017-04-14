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
package com.aplana.dbmi;

public class PortalException extends Exception
{
	private static final long serialVersionUID = 1L;
	private String userId;

	public PortalException() {
		super();
	}
	
	public PortalException(String message) {
		super(message);
	}
	
	public PortalException(String message, String userId) {
		this(message);
		this.userId = userId;
	}
	
	public PortalException(Throwable cause) {
		super(cause);
	}
	
	public PortalException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public PortalException(String message, String userId, Throwable cause) {
		this(message, cause);
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}
}
