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
 * {@link DataException} descendant used to notify user about data integrity violations
 * occured in system. (Used in SQLException translator) 
 */
public class DataIntegrityException extends DataException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Key of message shown in a case of unique constraint violation 
	 */
	public static final String MSG_UNIQUE = "general.unique";
	/**
	 * Key of message shown in a case of attempt to set null value in non-nullable column
	 */
	public static final String MSG_NULL = "general.null";
	/**
	 * Not used constant
	 */
	public static final String MSG_NUMBER = "general.number";
	/**
	 * Key of message shown in a case of attempt to insert child record without parent 
	 */
	public static final String MSG_PARENT = "general.parent";

	/**
	 * Creates DataIntergrityException object with given message pattern.
	 * @param msgId key of message pattern in properties file 
	 * @param resourceId key of message with description of exception cause 
	 */
	public DataIntegrityException(String msgId, String resourceId)
	{
		super(msgId, new Object[] { RESOURCE_PREFIX + resourceId });
	}
}
