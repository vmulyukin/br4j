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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;

import com.aplana.dbmi.action.BulkFetchChildrenCards;
import com.aplana.dbmi.action.GetDeepChildren;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;

/**
 * @comment AbdullinR
 *	������������ ���������� ������ �� ���� ��������� ��������� (� ��� ����� 
 *���������):
 *	1) �� ������� �������� ���������� "���������" � ���, ��������� � ���� 
 * ��������� ("��������� ���������") �� ������� depth;
 *	2) ����������� ������� ��������� ����� � ����:
 * 		���������, �����������  (�� ������� ��������)
 * 		... �����, ���� ��������, ����� (��� ������ �� ���������) ... 
 *	3) ������� ��������� � ������� ������� �������� "��� ����������".
 */
public class CollectExecutionThread extends ProcessCard {

	private static final long serialVersionUID = 1L;

	static final int depth = 20;

	// backlinkattribute.jbr.resolutions=JBR_IMPL_RESOLUT = "���������"
	static final ObjectId rootAttrId = ObjectId.predefined(BackLinkAttribute.class, "jbr.resolutions");

	// backlinkattribute.jbr.linkedResolutions=JBR_RIMP_RELASSIG = "��������� ���������"
	static final ObjectId linkedAttrId = ObjectId.predefined(BackLinkAttribute.class, "jbr.linkedResolutions");

	// backlinkattribute.jbr.reports=JBR_RIMP_REPORT = "����� �� ����������"
	final static ObjectId reportsAttrId = ObjectId.predefined(BackLinkAttribute.class, "jbr.reports");

	// JBR_RIMP_REPTEXT = "����� ������"
	static private ObjectId REPORT_TEXT = new ObjectId(TextAttribute.class, "JBR_RIMP_REPTEXT");

	// personattribute.jbr.incoming.inspector=JBR_IMPL_INSPECTOR = "��������� �� ���������"
	final static ObjectId controlledByAttrId = ObjectId.predefined(PersonAttribute.class, "jbr.incoming.inspector");

	// textattribute.jbr.visa.comment=JBR_VISA_COMMENT = "����������� ����������" 
	final static ObjectId commentAttrId = ObjectId.predefined(TextAttribute.class, "jbr.visa.comment");

	// �������� ������� ��� ������ ������� ����������:
	// ADMIN_221170 = "��� ����������"
	final static ObjectId resultAttrId =  new ObjectId(TextAttribute.class, "ADMIN_221170");

	// ������ �������������� ��������� ��� ����� ��������� �� ���������...
	final List<ObjectId> infoAttrIds = Arrays.asList( new ObjectId[] {
			Attribute.ID_AUTHOR,
			Attribute.ID_CREATE_DATE,
			REPORT_TEXT });


	@Override
	public Object process() throws DataException {

		/*
		 * ������ �������� ��������...
		 */
		final ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
		query.setId(getCardId());
		final Card card = (Card) getDatabase().executeQuery(getSystemUser(), query);
		if (card == null) return null;

		/*
		 * �������� ������ ("���������") ...
		 */
		final Collection<ObjectId> roots = CardUtils.getCardIdsByBackLink(rootAttrId, card.getId(), 
				getQueryFactory(), getDatabase(), getSystemUser());

		/*
		 * ��������� ��� ��������� ("��������� ���������")...
		 */
		final Collection<ObjectId> ids = loadDeepChildren(roots);

		ids.addAll(roots); // (!) ���������� roots ����

		/*
		 * ��������� ��� ����������� ������ �������� �������� ("������ �� ����������")... 
		 */
		final Map<ObjectId, List<Card>> allReports = loadReportsInfo(ids);

		/*
		 * ��������� ���������:
		 * 		���������, �����������  (�� ������� ��������)
		 * 		... �����, ���� ��������, ����� (�� ���������) ... 
		 */
		final StringBuilder message = new StringBuilder();

		safeAddAttrValue(message, card, controlledByAttrId);
		message.append(" , ");
		safeAddAttrValue(message, card, commentAttrId);
		message.append('\n');

		for (Iterator<ObjectId> ir = allReports.keySet().iterator(); ir.hasNext();) {
			try {
				final List<Card> reports = allReports.get(ir.next());
				// �������� ����� ������� �� ����-�������� �����...
				final Card maxReport = Collections.max(reports, new DateOrIdCardComparator());
				boolean flHasData = false;
				for (ListIterator<ObjectId> i = infoAttrIds.listIterator(); i.hasNext(); ) {
					final ObjectId attrId = i.next();
					if (safeAddAttrValue( message, maxReport, attrId)) {
						if (i.hasNext()) message.append(", ");
						flHasData=true;
					}
				}
				if (flHasData)
					message.append('\n');
			} 
			catch (NoSuchElementException e){}
			catch (Exception e) { e.printStackTrace(); }
		}

		/*
		 * ������������ ����������... 
		 */
		// ����� �������������� ������...
		try {
			final TextAttribute txtAttr = (TextAttribute) card.getAttributeById(resultAttrId);
			txtAttr.setValue( message.toString());
		} catch (Exception e) {
			logger.error( MessageFormat.format( MSG_ATTR_NOT_FOUND, resultAttrId,
					(card.getId() != null) ? card.getId().getId() : null
				));
		}

		// ����������...
		execAction(new LockObject(card));
		try {
			final SaveQueryBase sq = getQueryFactory().getSaveQuery(card);
			sq.setObject(card);
			getDatabase().executeQuery(getSystemUser(), sq);
		} finally {
			execAction(new UnlockObject(card));
		}

		return allReports;
	}


	/**
	 * �������� �� �� ���� ����� (�� ������� this.depth), ������� � �������� 
	 * ��������, ������ �� �������� this.cres.
	 * @param roots: ������ �������� ��������.
	 * @return ������ �����.
	 * @throws DataException
	 */
	@SuppressWarnings("unchecked")
	private Collection<ObjectId> loadDeepChildren(
			final Collection<ObjectId> roots
		) throws DataException 
	{
		final GetDeepChildren action = new GetDeepChildren();
		action.setDepth(depth);
		action.setChildTypeId(linkedAttrId);
		action.setRoots(roots);
		final ActionQueryBase aqb = getQueryFactory().getActionQuery(action);
		aqb.setAction(action);
		final Collection<ObjectId> ids = (Collection<ObjectId>)getDatabase().executeQuery(getUser(), aqb);
		return ids;
	}


	/**
	 * ��������� ���� �� ������� ��� ��������� ��������.
	 * @param ids: ������ id ��������.
	 * @return ������ � ���� ����� cardId -> ������ �������� �������.
	 * @throws DataException
	 */
	@SuppressWarnings("unchecked")
	private Map<ObjectId, List<Card>> loadReportsInfo( 
			final Collection<ObjectId> ids) throws DataException 
	{
		final BulkFetchChildrenCards bfcc = new BulkFetchChildrenCards();
		bfcc.setReverseLink(false);
		bfcc.setParentCardIds(ids);
		bfcc.setLinkAttributeId(reportsAttrId);

		/*
		 * ������: "�����", "���� ���������", "����� �� ����������" 
		 */
		final ArrayList<SearchResult.Column> columns =
			new ArrayList<SearchResult.Column>(3);
		columns.add(CardUtils.createColumn(Attribute.ID_AUTHOR));
		columns.add(CardUtils.createColumn(Attribute.ID_CREATE_DATE));
		columns.add(CardUtils.createColumn(REPORT_TEXT));
		bfcc.setColumns(columns);

		final ActionQueryBase aqb = getQueryFactory().getActionQuery(bfcc);
		aqb.setAction(bfcc);
		
		BulkFetchChildrenCards.Result result = getDatabase().executeQuery(getSystemUser(), aqb);
		return result.getCards();
	}

	static final String MSG_ATTR_NOT_FOUND = "Expected attribute ''{0}'' not found at card {1}";
	boolean safeAddAttrValue( final StringBuilder msger, 
			final Card card, 
			final ObjectId attrId)
	{
		if (card != null && attrId != null) {
			final Attribute attr = card.getAttributeById(attrId);
			if (attr != null) {
				msger.append( attr.getStringValue());
				return true;
			} 
			logger.error( MessageFormat.format( MSG_ATTR_NOT_FOUND, attrId,
					(card.getId() != null) ? card.getId().getId() : null
				));
		}
		return false; 
	}

	/**
	 * ��������� �� ����������� �������� �� ����, ����� card id.
	 */
	final static ObjectId dId = Attribute.ID_CREATE_DATE;
	protected class DateOrIdCardComparator implements Comparator<Card>{
		@SuppressWarnings("synthetic-access")
		public int compare(Card o1, Card o2) {
			if (o1 == null)
				return (o2 == null) ? 0 : -1;
			if (o2 == null) return 1;
			try {
				final Date date1 =((DateAttribute)o1.getAttributeById(dId)).getValue();
				final Date date2 =((DateAttribute)o2.getAttributeById(dId)).getValue();
				if (date1.equals(date2))
					return ((Long)o1.getId().getId()).compareTo((Long)o2.getId().getId());
				return date1.compareTo(date2);
			} catch (Exception e) {
				logger.error("Expected attribute '"+dId.getId()+" in card #"+o1.getId().getId()+
				" or in card #"+o2.getId().getId()+" but it is NOT FOUND !");
				return 0;
			}
		}
	}
}