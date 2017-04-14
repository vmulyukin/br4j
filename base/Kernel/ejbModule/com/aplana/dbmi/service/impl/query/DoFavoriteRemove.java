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

import java.sql.Types;

import com.aplana.dbmi.action.RemoveFromFavorites;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

/**
 * Query used to perform {@link RemoveFromFavorites} action
 */
public class DoFavoriteRemove extends ActionQueryBase implements WriteQuery {
	private static final long serialVersionUID = 1L;

	/**
	 * Remove row from PERSON_CARD table
	 * @return null
	 */
	public Object processQuery() throws DataException {
		RemoveFromFavorites action = (RemoveFromFavorites) getAction();
		getJdbcTemplate().update(
				"DELETE FROM person_card WHERE person_id=? AND card_id=?",
				new Object[] { getUser().getPerson().getId().getId(), action.getCard().getId() },
				new int[] { Types.NUMERIC, Types.NUMERIC }
				);
		return null;
	}
}
