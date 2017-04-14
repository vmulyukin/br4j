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

import java.util.List;

import com.aplana.dbmi.action.GetCardIdByUUID;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoGetCardIdByUUID extends ActionQueryBase {
	private static final long serialVersionUID = 2L;

	@Override
	public Object processQuery() throws DataException {
		GetCardIdByUUID action = getAction();
		String sql = "select distinct card_id from attribute_value "
				+ "where attribute_code=? and lower(string_value)=lower(?)";

		List<?> ids = getJdbcTemplate().queryForList(
				sql,
				new Object[] { action.getAttrId().getId(), action.getUuid() },
				Long.class);
		if (ids.size() > 1) {
			throw new DataException("general.card.moreThanOneFound");
		} else if (ids.size() != 1) {
			return null;
		} else {
			return new ObjectId(Card.class, ids.get(0));
		}
	}
}