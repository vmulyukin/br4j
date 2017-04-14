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
package com.aplana.dbmi.portlet;

import com.aplana.dbmi.service.DataException;

public class ResolutionReportPortletException extends DataException{
	
	/**
	 * Default constructor.
	 * Creates exception object with message found in properties-file under key "general"
	 */
	public ResolutionReportPortletException() {
		super();
	}
	

	/**
	 * Creates exception with given message  
	 * @param msgId key of the required message in nls *.properties file
	 */
	public ResolutionReportPortletException(String msgId) {
		super(msgId);
	}

	/**
	 * Takes a set of objects and inserts the formatted strings
	 * into the given message pattern at the appropriate places
	 * ({like @link java.text.MessageFormat#format(String, Object[])})
	 * Resulting string is used as the exception message 
	 * @param msgId key of the required message pattern in nls *.properties file.
	 * This pattern should have several placeholders for parameters 
	 * @param params set of object to replace placeholders in message string.
	 * If one of parameters is a string starting with {@link #RESOURCE_PREFIX} then 
	 * value of this parameter will be replaced with a string stored in 
	 * properties file under key equals to parameter's ending   
	 */
	public ResolutionReportPortletException(String msgId, Object[] params) {
		super(msgId, params);
	}
	
	/**
	 * Wraps Throwable object with DataException.
	 * For a message uses message defined in properties-file under key "general"
	 * @param cause Throwable object to be wrapped
	 */
	public ResolutionReportPortletException(Throwable cause) {
		super(cause);
	}

	/**
	 * Wraps Throwable object with DataException and sets given message for it.
	 * @param msgId key of the required message in nls *.properties file
	 * @param cause Throwable object to be wrapped
	 */
	public ResolutionReportPortletException(String msgId, Throwable cause) {
		super(msgId, cause);
	}
	
	/**
	 * Wraps Throwable object with DataException.
	 * For the message uses formatted string received after replacing all placeholders
	 * in given message pattern with given parameters. 
	 * @param msgId key of the required message pattern in nls *.properties file.
	 * This message should have several placeholders for parameters
	 * @param params set of object to replace placeholders in message pattern string
	 * If one of parameters is a string starting with {@link #RESOURCE_PREFIX} then 
	 * value of this parameter will be replaced with a string stored in 
	 * properties file under key equals to parameter's ending   
	 * @param cause Throwable object to be wrapped
	 */
	public ResolutionReportPortletException(String msgId, Object[] params, Throwable cause) {
		super(msgId, params, cause);
	}

}
