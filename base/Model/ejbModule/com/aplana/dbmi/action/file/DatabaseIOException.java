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
package com.aplana.dbmi.action.file;

import java.io.IOException;

import com.aplana.dbmi.service.DataException;

/**
 * This class is used in {@link DownloadFileStream} to wrap database-related exceptions in 
 * {@link java.io.IOException} descendant.
 */
public class DatabaseIOException extends IOException
{
	private static final long serialVersionUID = 1L;
	private DataException cause;

	/**
	 * Creates new DatabaseIOException which wraps around given DataException object 
	 * @param cause DataException object to be wrapped
	 */
	public DatabaseIOException(DataException cause) {
		this.cause = cause;
	}
	
	/**
	 * Returns wrapped DataException object
	 * @return wrapped DataException object
	 */
	public DataException getDataException()	{
		return cause;
	}

	/**
	 * Returns message of wrapped DataException object
	 * @return message of wrapped DataException object
	 */
	public String getMessage() {
		return cause.getMessage();
	}
}
