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
package com.aplana.dbmi.module.docflow;

import java.util.Collection;
import java.util.Iterator;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.UserData;

public class WorkflowMoveCalculator {

	private final QueryFactory queryFactory;
	private UserData systemUser;
	private final Database database;

	public WorkflowMoveCalculator(QueryFactory queryFactory, Database database) {
		this.queryFactory = queryFactory;
		this.database = database;
	}

	protected QueryFactory getQueryFactory() {
		return this.queryFactory;
	}

	protected Database getDatabase() {
		return this.database;
	}

	/**
	 * ����� ������� ��� �������� � ��������� ��������� �� ��������.
	 * @param card: ��������
	 * @param destStateId: ������� ���������.
	 * @return ������������ id-��������.
	 * @throws DataException ���� ��� �������� �� �������� ��������� � �������.
	 */
	@SuppressWarnings("unchecked")
	public WorkflowMove findProperMove(Card card, ObjectId destStateId) throws DataException {
		final ChildrenQueryBase query = getQueryFactory().getChildrenQuery(Card.class, WorkflowMove.class);
		query.setParent(card.getId());
		Collection<WorkflowMove> moves = (Collection<WorkflowMove>) getDatabase().executeQuery(getSystemUser(), query);
		if (moves != null) {
			for (Iterator<WorkflowMove> itr = moves.iterator(); itr.hasNext(); ) {
				final WorkflowMove wfm = itr.next();
				if (destStateId.equals(wfm.getToState()))
					return wfm;
			}
		}
		throw new DataException("jbr.docflow.nomove",
			new Object[] { card.getId().getId().toString(), DataException.RESOURCE_PREFIX + "state." + destStateId.getId() });
	}

	public UserData getSystemUser() throws DataException {
		if (systemUser == null) {
			systemUser = new UserData();
			systemUser.setPerson(getDatabase().resolveUser(Database.SYSTEM_USER));
			systemUser.setAddress("internal");
		}
		return systemUser;
	}
}
