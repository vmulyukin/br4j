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
package com.aplana.dbmi.jbr.processors.card.runcheck;

import com.aplana.dbmi.service.DataException;

/**
 * @author RAbdullin
 *
 */
public class CardCheckException extends DataException {

	private static final long serialVersionUID = 1L;

	public CardCheckException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param msgId
	 * @param params
	 * @param cause
	 */
	public CardCheckException(String msgId, Object[] params, Throwable cause) {
		super(msgId, params, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param msgId
	 * @param params
	 */
	public CardCheckException(String msgId, Object[] params) {
		super(msgId, params);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param msgId
	 * @param cause
	 */
	public CardCheckException(String msgId, Throwable cause) {
		super(msgId, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param msgId
	 */
	public CardCheckException(String msgId) {
		super(msgId);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public CardCheckException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
