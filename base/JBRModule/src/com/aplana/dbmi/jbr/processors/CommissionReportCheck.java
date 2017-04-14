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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.CardLinkLoader;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.dbmi.service.impl.Validator;

public class CommissionReportCheck extends ProcessorBase implements Validator
{
	public static final ObjectId ID_ATTR_RESPONSIBLE =
		ObjectId.predefined(PersonAttribute.class, "jbr.commission.responsible");
	public static final ObjectId ID_ATTR_REPORTS =
		ObjectId.predefined(BackLinkAttribute.class, "jbr.reports");
	public static final ObjectId ID_STATE_FINAL =
		ObjectId.predefined(CardState.class, "jbr.report.final");

	@Override
	public Object process() throws DataException {
		Card card = ((ChangeState) getAction()).getCard();
		final HashMap<ObjectId, Card> finalReports = new HashMap<ObjectId, Card>();
		final Collection<Card> reports = getReports(card);
		for (Iterator<Card> itr = reports.iterator(); itr.hasNext(); ) {
			final Card report = itr.next();
			if (!ID_STATE_FINAL.equals(report.getState()))
				continue;
			Person author = ((PersonAttribute) report.getAttributeById(Attribute.ID_AUTHOR)).getPerson();
			if (finalReports.containsKey(author.getId()))
				throw new DataException("jbr.commission.finished.multiplereport",
						new Object[] { card.getId().getId().toString(), author.getFullName() });
			finalReports.put(author.getId(), report);
		}
		PersonAttribute attr = (PersonAttribute) card.getAttributeById(ID_ATTR_RESPONSIBLE);
		for (Iterator<?> itr = attr.getValues().iterator(); itr.hasNext(); ) {
			Person resp = (Person) itr.next();
			if (!finalReports.containsKey(resp.getId()))
				throw new DataException("jbr.commission.finished.noreport",
						new Object[] { card.getId().getId().toString(), resp.getFullName() });
		}
		logger.info("Commission " + card.getId().getId() + " processed: " +
				reports.size() + " reports found, " + finalReports.size() + " are final");
		return getObject();
	}

	@SuppressWarnings("unchecked")
	private Collection<Card> getReports(Card card) 
		throws DataException 
	{
		// >>> (2010/02, RuSA)
		final LinkAttribute link = (LinkAttribute) card.getAttributeById(ID_ATTR_REPORTS);
		if (link == null) return null;

		/*
		Collection<Card> reports = link.getLinkedCards();
		if (link.isLinkedEmpty())
			return reports;
		final Card test = reports.iterator().next();
		if (test.getState() == null || test.getAttributeById(Attribute.ID_AUTHOR) == null) 
		{
			Search search = new Search();
			search.setByCode(true);
			search.setByAttributes(false);
			search.setWords( link.getLinkedIds());
			
			final ArrayList<SearchResult.Column> columns = 
				new ArrayList<SearchResult.Column>(); 
			search.setColumns(columns);

			SearchResult.Column col = new SearchResult.Column();
			col.setAttributeId(Card.ATTR_STATE);
			columns.add(col);

			col = new SearchResult.Column();
			col.setAttributeId(Attribute.ID_AUTHOR);
			columns.add(col);

			final ActionQueryBase query = getQueryFactory().getActionQuery(search);
			query.setAction(search);
			final SearchResult result = (SearchResult) getDatabase().executeQuery(getUser(), query);
			reports = result.getCards();
		}
		return reports;
		 */
		if(CardLinkAttribute.class.isAssignableFrom(ID_ATTR_REPORTS.getType())) {
			if (link.getLinkedCount() < 1) 
				return new ArrayList<Card>();//link.getLinkedCards();

			// ��������� "�������" �������� ��������� ��������
			//
			final ObjectId[] attrs = new ObjectId[] { Card.ATTR_STATE, Attribute.ID_AUTHOR};
			return CardLinkLoader.loadCardsByLink( (CardLinkAttribute) link, attrs, 
				getSystemUser(), getQueryFactory(), getDatabase() );
			// >>> (2010/02, RuSA)
		} else if(BackLinkAttribute.class.isAssignableFrom(ID_ATTR_REPORTS.getType())) {
			return CardUtils.execListProject(ID_ATTR_REPORTS, card.getId(), 
					getQueryFactory(), getDatabase(), getUser());
		} else
			throw new ClassCastException("Attribute " + link.getId().getId() + " must be CardLink or BackLink");
	}

}
