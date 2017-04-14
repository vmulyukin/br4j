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
package com.aplana.dbmi.access.delegate;

import java.util.Set;

import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * ��������� ��� PermissionDelegate �� ��� id.
 * @author RAbdullin
 */
public class QueryPermissionDelegate extends ObjectQueryBase {

	/**
	 * Load PermissionDelegate object.
	 */
	public Object processQuery() throws DataException {

		if (getId() == null)
			return null;

		final Long delegateId = (Long) getId().getId(); 

		final Set /* PermissionSet */ theSet = 
			DelegatorBean.queryDelegatesByField( "delegation_id", getId(), getJdbcTemplate());

		if (theSet == null || theSet.size() != 1) {
			logger.warn( "No delegate found for id=" + delegateId);
			return null;
		}

		return theSet.iterator().next();
	}

}
