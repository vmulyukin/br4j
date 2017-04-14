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
 * Represents exceptions thrown when card isn't unique (is doublet) 
 */
public class CardDoubletException extends DataException {

	private static final long serialVersionUID = 1L;

	public CardDoubletException() {
		super();
	}

	/**
	 * Creates exception object with given message
	 * @param msg desired value of exception's message
	 */
	public CardDoubletException(String msg) {
		super(msg);
	}

	/**
	 * Creates CardDoubletException object that wraps given {@link java.lang.Throwable}
	 * @param cause Throwable instance to be wrapped
	 */
	public CardDoubletException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Creates CardDoubletException object with have given message text
	 * and wraps given {@link java.lang.Throwable} object
	 * @param msg desired value of exception's message
	 * @param cause Throwable instance to be wrapped
	 */
	public CardDoubletException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public CardDoubletException(String msgId, Object[] params) {
		super(msgId, params);
	}
}
