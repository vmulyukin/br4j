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

import com.aplana.dbmi.action.CheckDelegatingReadAccess;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.AccessCheckerBase;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.access.CardRead;

import java.util.ArrayList;
import java.util.List;

public class DoCheckDelegatingReadAccess  extends ActionQueryBase {
	private static final long serialVersionUID = -1858596777456612301L;

	@Override
	public List<Card> processQuery() throws DataException {
		CheckDelegatingReadAccess action = getAction();
		List<Card> listCheckCard = action.getCheckCards();
		List<Card> listVerefiedCard = new ArrayList<Card>();
		if (null != listCheckCard && !listCheckCard.isEmpty()) {
			AccessCheckerBase checkerRead = new CardRead();
			checkerRead.setUser(getUser());
			checkerRead.setJdbcTemplate(getJdbcTemplate());
			for (Card checkCard : listCheckCard) {
				checkerRead.setObject(checkCard);
				if (checkerRead.checkAccess()) {
					listVerefiedCard.add(checkCard);
				}
			}
		}
		return listVerefiedCard;
	}
}
