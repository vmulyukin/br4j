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

/**
 * Class used to notify system about technical errors occured during
 * communications with business-logic EJB components (for example with {@link DataService} EJB}.
 * <br>
 * It's often used to wrap {@link java.rmi.RemoteException} objects.
 */
public class ServiceException extends Exception
{
	private static final long serialVersionUID = 1L;

	public ServiceException() {
		super();
	}

	/**
	 * Creates exception object with given message
	 * @param msg desired value of exception's message
	 */
	public ServiceException(String msg) {
		super(msg);
	}

	/**
	 * Creates ServiceException object that wraps given {@link java.lang.Throwable}
	 * @param cause Throwable instance to be wrapped
	 */
	public ServiceException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Creates ServiceException object with have given message text
	 * and wraps given {@link java.lang.Throwable} object
	 * @param msg desired value of exception's message
	 * @param cause Throwable instance to be wrapped
	 */
	public ServiceException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
