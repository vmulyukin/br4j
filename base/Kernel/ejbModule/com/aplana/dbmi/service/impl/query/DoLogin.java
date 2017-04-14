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
package com.aplana.dbmi.service.impl.query;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataService;
import com.aplana.dbmi.service.impl.ActionQueryBase;

/**
 * Query used to perform {@link com.aplana.dbmi.action.Login} action 
 */
public class DoLogin extends ActionQueryBase
{
	/**
	 * Identifier of system log event representing successful login attempt
	 */
	public static final String EVENT_ID = "LOG_USER";
	/**
	 * Identifier of system log event representing login failure
	 */
	public static final String EVENT_ID_FAILURE = "ERR_LOGIN";
	
	/**
	 * This query by itself does nothing as all authentification will be
	 * performed during underlying {@link DataService} reference initialization.
	 * @return null
	 */
	public Object processQuery() throws DataException
	{
		return null;
	}

	/**
	 * @return identifier of person who tries to login. For {@link Person#ID_SYSTEM system user}
	 * returns null. 
	 */
	public ObjectId getEventObject() {
		return Person.ID_SYSTEM.equals(getUser().getPerson().getId()) ? null : getUser().getPerson().getId();
	}

	/**
	 * @return {@link #EVENT_ID_FAILURE} if user who tries to login is a {@link Person#ID_SYSTEM system user},
	 * otherwise returns {@link #EVENT_ID}
	 */
	public String getEvent() {
		return Person.ID_SYSTEM.equals(getUser().getPerson().getId()) ? EVENT_ID_FAILURE : EVENT_ID;
	}
}
