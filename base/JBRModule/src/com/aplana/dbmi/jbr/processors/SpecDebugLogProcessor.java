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
import java.util.List;
import java.util.Map;

import com.aplana.dbmi.jbr.util.AttributeLocator;
import com.aplana.dbmi.jbr.util.AttributeSelector;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.BasePropertySelector;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * @author YNikitin(2014/04/03, GoRik)
 * ����������� ��������� ��� ����������� ���������� �� ������������ ��������� �������� �������� 
 * � ����� ���������� ���������� �������� �������
 */
public class SpecDebugLogProcessor extends ProcessCard {

	private static final long serialVersionUID = 1L;

	public static final int DEFAULT_ARGSTRLEN = 60;
	public static final int MAX_PREFIX_LENGTH = 100;
	/**
	 * �������������� �������. �������� ��������� ��� ������ ����� ��������� ��� ������
	 */
	private static final String PARAM_SRC_ATTR_IDS = "srcAttrIds";
	/**
	 * �������������� ��������. �������� �������� � sql-��������
	 */
	private static final String PARAM_SQL_STRING = "sqlString";

	private static final String PARAM_SQL_NAME = "sqlName";

	private static final String PARAM_ATTR_CONDITION = "attr_condition";

	private final List<AttributeLocator> attrIdList = new ArrayList<AttributeLocator>();

	private String sqlString = null;
	private String sqlName = null;
	
	protected final List<BasePropertySelector> conditions = new ArrayList<BasePropertySelector>();
	/*
	 *      .
	 * @see com.aplana.dbmi.service.impl.ProcessorBase#process()
	 */
	@Override
	public Object process() throws DataException {
		// ��������� �������� ������ � DEBUG ��� TRACE-������ �����������
		if (!(logger.isDebugEnabled()||logger.isTraceEnabled()))
			return null;
		
		final Card card = super.getCard();
		if (card == null)
			return null;
		final ObjectId cardId = getCardId();

		if ((cardId==null&&!checkCardConditons(card))||(cardId!=null&&!checkCardConditons(cardId))) {
		    logger.warn("Card " + (cardId!=null?cardId.getId():card)
			    + " did not satisfies coditions. Exiting");
		    return null;
		}
		if (!conditions.isEmpty())
			if( logger.isDebugEnabled() )
				logger.debug("Card " + (cardId!=null?cardId.getId():card) + " satisfies coditions");

		logger.debug("cardId = "+cardId.getId()+", templateId = "+card.getTemplate().getId());
		try{
			final String[] sIds = super.getParameter( PARAM_SRC_ATTR_IDS, "name").trim().split("\\s*[;,]\\s*");
			if (sIds.length >0) {
				this.attrIdList.clear();
				int i = 0;
				for (String s: sIds) {
					if (s == null || s.length() == 0) continue;
					String[] array = s.split("(?<=\\[[^\\]]{0," + MAX_PREFIX_LENGTH + "}\\])", 2);
					String prefix = array.length == 2 ? array[0].replaceAll("[^\\]\\[]*\\[([^\\]]*)\\]", "$1") : "";
					s = array[array.length - 1].trim();
					final AttributeLocator id = makeObjectId(s);
					this.attrIdList.add( id);
					i++;
				}
			}
			// ������� �������� ������� ���������
			for(AttributeLocator id : attrIdList){
				final Attribute attr =  card.getAttributeById(id.getAttrId());
				if (attr!=null){
					logger.debug("attribute "+id.getAttrId().getId()+" value = "+((attr instanceof CardLinkAttribute)?((CardLinkAttribute)attr).getLinkedIds():attr.getStringValue()));
				}
			}
			// ������ ��������� ������, ���� �� �� ������
			if (sqlString!=null&&!sqlString.isEmpty()){
				// ��������� ������ � ������� ��������� ������� � ���
				List<Map<String, Object>> rows = getJdbcTemplate().queryForList(
						sqlString,
						new Object[] { card.getId().getId() }, 
						new int[] {Types.NUMERIC});
				StringBuffer strBuf = new StringBuffer(); 
				for (Map<String, Object> row : rows){
					StringBuffer names = new StringBuffer();
					StringBuffer columns = new StringBuffer();
					for(Object column : row.keySet()){
						names.append(column).append("\t");
						columns.append(row.get(column)).append("\t");
					}
					if (strBuf.length() == 0){
						strBuf.append(sqlName).append("\n");
						strBuf.append(names).append("\n");
					}
					strBuf.append(columns).append("\n");
				}
				logger.debug((strBuf.length()==0)?sqlName+" - Empty":strBuf.toString());
			}
		} catch (Exception e){
			e.printStackTrace();		
		}
		return null;
	}

	
	@Override
	public void setParameter(String name, String value) {
		if (name == null || value == null)
			return;
		if (name.equalsIgnoreCase(PARAM_SQL_STRING)){
			sqlString = value;
		} else if (name.equalsIgnoreCase(PARAM_SQL_NAME)){
				sqlName = value;
		} else if (name.startsWith(PARAM_ATTR_CONDITION)) {
			try {
				final AttributeSelector selector = AttributeSelector
					.createSelector(value);
				this.conditions.add(selector);
			} catch (DataException ex) {
				ex.printStackTrace();
			}
		} else 
			super.setParameter(name, value);
	}

	/**
	 * @param s ������ ���� "{���:} id {@id2}",
	 * {x} = �������������� ����� x.
	 * ��������, "string: jbr.organization.shortName"
	 * 			 "back: jbr.sender: fullname"
	 * ���� ��� ������, �� ������ ���������������� �������� � ����� ������ �
	 * �����-���� �����, ���� ��� ������, �� ����������� �� "string".
	 * @return
	 * @throws DataException
	 */
	protected static AttributeLocator makeObjectId(final String s)
		throws DataException {
		return (s == null) ? null : new AttributeLocator(s);
	}
	
	private boolean checkCardConditons(ObjectId cardId) throws DataException {
		if (conditions == null || conditions.isEmpty())
		    return true;
		final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(
			Card.class);
		cardQuery.setAccessChecker(null);
		cardQuery.setId(cardId);
		final Card card = (Card) getDatabase().executeQuery(getSystemUser(),
			cardQuery);
		return checkConditions(conditions, card);
	}

	private boolean checkCardConditons(Card card) throws DataException {
		if (conditions == null || conditions.isEmpty())
		    return true;
		return checkConditions(conditions, card);
	}
    /**
     * ��������� ��������� �� ������� conds ��� �������� card.
     *
     * @param conds
     * @param card
     * @return true, ���� ������� ��������� (� ��� ����� ���� �� ��� �����),
     *         false, �����.
     * @throws DataException
     */
	private boolean checkConditions(List<BasePropertySelector> conds, Card card) {
		if (conds == null || card == null)
		    return true;
		for (BasePropertySelector cond : conds) {
		    if (!cond.satisfies(card)) {
			logger.debug("Card " + (card.getId()!=null?card.getId().getId():card)
				+ " did not satisfies codition " + cond);
			return false;
		    }
		}
		return true;
	}
}