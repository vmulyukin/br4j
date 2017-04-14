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
package com.aplana.dbmi.jbr.processors;


import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.CardLinkLoader;
import com.aplana.dbmi.service.impl.QueryBase;

/**
 *  O.E. 13.04.2011
 *  ��������� ���������  ��������� �������� ��� ����� ������ ���������� ��������, ��������� �� ������ ��������� ��������.
 *  ��������� �������� "��������" � �� ���������������/���������������� ��������� �� ��������������.
 */
public class DoDependentChangeStateRecursive extends DoDependentChangeState 
{
	@Override
	protected void processDependentCard(Card card, Card actionCard, HashSet<ObjectId> sourceStateIds, ObjectId targetStateId, Card parentCard) throws DataException 
	{
		if (!targetStateId.equals(card.getState()) && (sourceStateIds == null || sourceStateIds.contains(card.getState())))
			doSafeChangeState(card, findMove(card));
		Collection <Card> children = getDependentCards(card);
		if (!children.isEmpty()) 
			for(Iterator<Card> i = children.iterator(); i.hasNext();)
			{
				processDependentCard(i.next(), card, sourceStateIds, targetStateId, parentCard);
			}	
	}
	@Override
	protected void doChangeState(final Card card, final WorkflowMove wfm) throws DataException {
		this.getDatabase().executeQuery(getSystemUser(), new QueryBase()
		{public Object processQuery(){
			getJdbcTemplate().update(
				"UPDATE card SET status_id=? WHERE card_id=?",
				new Object[] { wfm.getToState().getId(), card.getId().getId() },
				new int[] { Types.NUMERIC, Types.NUMERIC }
			);
			return null;
		}});
	}
	@SuppressWarnings("unchecked")
	@Override
	protected Collection<Card> getLinkedCards(CardLinkAttribute attr) throws DataException {

		if (attr == null || attr.getLinkedCount() < 1)
			return Collections.emptyList();

		return CardLinkLoader.loadCardsByLink(attr, new ObjectId[] { Card.ATTR_STATE, attr.getId() },
				getOperUser(), getQueryFactory(), getDatabase());
	}
	@Override
	protected Collection<Card> getBackLinkedCards(ObjectId attrId, ObjectId cardId) throws DataException {
		final ListProject list = new ListProject();
		list.setAttribute(attrId);
		list.setCard(cardId);
		final List<SearchResult.Column> cols = new ArrayList<SearchResult.Column>(2);
		cols.add( CardUtils.createColumn(Card.ATTR_STATE));
		cols.add( CardUtils.createColumn(attrId));
		list.setColumns( cols);
		return CardUtils.execSearchCards(list, getQueryFactory(), getDatabase(), getOperUser());
	}
}