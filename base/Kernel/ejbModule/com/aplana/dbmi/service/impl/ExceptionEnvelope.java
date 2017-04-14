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
package com.aplana.dbmi.service.impl;

import org.springframework.dao.DataAccessException;

import com.aplana.dbmi.service.DataException;

/**
 * DataAccessException descendant used by DBNamesSQLExceptionTranslator
 * to 'seal' DataExceptions containing human-readable information
 * about SQLExceptions caught during working with database.<br>
 */
public class ExceptionEnvelope extends DataAccessException
{
	private static final long serialVersionUID = 1L;
	private DataException sealed;

	/**
	 * Creates new ExceptionEnvelope instance
	 * @param sealed exception caused creation of this ExceptionEnvelope instance 
	 */
	public ExceptionEnvelope(DataException sealed) {
		super("(Exception envelope) " + sealed.getMessageID());
		this.sealed = sealed;
	}

	/**
	 * DataAccessException representing root cause of this ExceptionEnvelope 
	 * @return DataAccessException which caused throwing of this ExceptionEnvelope
	 */
	public DataException getSealedException() {
		return this.sealed;
	}
}
