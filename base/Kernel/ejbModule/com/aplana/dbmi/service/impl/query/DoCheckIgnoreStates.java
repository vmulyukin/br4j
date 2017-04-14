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

import java.util.ArrayList;
import java.util.List;
import com.aplana.dbmi.action.CheckIgnoreStates;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.AccessCheckerBase;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.access.CardRead;
import com.aplana.dbmi.utils.SimpleDBUtils;

public class DoCheckIgnoreStates extends ActionQueryBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Object processQuery() throws DataException {
		
		CheckIgnoreStates checkIgnoreStates = (CheckIgnoreStates)getAction();
		List<ObjectId> linkedCardsIds = checkIgnoreStates.getLinkedCardsIds();
		List <ObjectId> listVerefiedCard = new ArrayList<ObjectId>();
		
		// ��������� ������ �������� �� ������ �������
		if (null != linkedCardsIds && !linkedCardsIds.isEmpty()) {
			AccessCheckerBase checkerRead = new CardRead();
			checkerRead.setUser(getUser());
			checkerRead.setJdbcTemplate(getJdbcTemplate());
			for(ObjectId checkCardId : linkedCardsIds) {
				checkerRead.setObject(checkCardId);
				if(checkerRead.checkAccess()) {
					listVerefiedCard.add(checkCardId);
				}
			}
		}
		List<ObjectId> ignoreStates = checkIgnoreStates.getIgnoreStates();
		Long count = 0L;
		if (!listVerefiedCard.isEmpty() && !ignoreStates.isEmpty()) {
			// ��������� ���������� ������� �� ������������ �������
			StringBuilder sqlBuf = new StringBuilder();
			sqlBuf.append("select count(card_id) from card \n");
			sqlBuf.append("where status_id not in (" + SimpleDBUtils.stringIdentifiersToCommaSeparatedSqlString(ignoreStates) + ") \n");
			sqlBuf.append("and card_id in(" + SimpleDBUtils.stringIdentifiersToCommaSeparatedSqlString(listVerefiedCard) + "); \n");
	
			final String sqlText = sqlBuf.toString();
			count = getJdbcTemplate().queryForLong(sqlText);
			if (null == count) {
				count = 0L;
			}
		} else {
			count = new Long(listVerefiedCard.size());
		}
		return count;
	}
}
