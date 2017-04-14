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
/**
 * 
 */
package com.aplana.dbmi.storage.search;

import java.text.MessageFormat;

// import com.aplana.dbmi.service.DataException;

/**
 * @author RAbdullin
 *
 */
public class SearchException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 */
	public SearchException() {
	}

	/**
	 * @param msgId
	 */
	public SearchException(String msgId) {
		super(msgId);
	}

	/**
	 * @param msgId
	 * @param params
	 */
	public SearchException(String msgId, Object[] params) {
		// super(msgId, params);
		this( MessageFormat.format( msgId, params) );
	}

	/**
	 * @param cause
	 */
	public SearchException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param msgId
	 * @param cause
	 */
	public SearchException(String msgId, Throwable cause) {
		super(msgId, cause);
	}

	/**
	 * @param msgId
	 * @param params
	 * @param cause
	 */
	public SearchException(String msgId, Object[] params, Throwable cause) {
		this( MessageFormat.format(msgId, params), cause);
	}

}
