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

import java.util.Collection;

import com.aplana.dbmi.model.AccessRule;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.QueryBase;

public class GetAccessRule extends ObjectQueryBase {

	public Object processQuery() throws DataException {
		QueryBase query = getQueryFactory().getListQuery(AccessRule.class);
		query.setFilter(new AccessRuleIdFilter(getId()));
		Collection result = (Collection) getDatabase().executeQuery(getUser(), query);
		if (result.size() == 0)
			throw new DataException("fetch.notfound",
					new Object[] { "@" + getId().getType(), getId().getId() });
		else if (result.size() > 1)
			logger.error("Data integrity error or incorrect query: " +
					result.size() + " access rules found by id " + getId());
		return result.iterator().next();
	}

}
