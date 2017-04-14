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
/**
 * 
 */
package com.aplana.dbmi.model.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.BulkFetchChildrenCards;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * @author RAbdullin
 */
public class SearchUtils {
	
	final static Log logger = LogFactory.getLog(SearchUtils.class);

	public static class SimplePattern {
		private String oper, value;

		final public static String OPER_FIRST = "!";
		final public static String OPER_PLUS = "+";
		final public static String OPER_LIST = "-+|";
		final public static String OPER_DEFAULT = "";


		public SimplePattern() {
			super();
		}

		public SimplePattern(String oper, String value) {
			super();
			this.oper = oper;
			this.value = value;
		}

		public String getOper() {
			return this.oper;
		}

		public void setOper(String oper) {
			this.oper = oper;
		}

		public String getValue() {
			return this.value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return this.oper + this.value;
		}
	}

	/**
	 * ����������� ������ ������ � ����� �������
	 * @param query
	 * @return
	 */
	public static List<SimplePattern> getSearchQueryPatterns(String query) {
		/* ���������, ��� � ������ ������ ��������� ������� ����� �����������
		 * ��������� '|' ��� '%'.
		 * (!) ���� ��������� �� �������������, ����� ����� ������������
		 * ������ "\\s*[^\\\\][|%]\\s*" � ������ �������� �����������.
		 */
		//final String[] patterns = query.toLowerCase().split("\\s*[|%]\\s*");
		/* 19.08.2011
		 * ��������� ������� ����� ����������� � "".
		 * ������ ��� "+" - "�"
		 * "-" - "��"
		 * "|" - "���"
		 */
		// final String reg = "[-\\s|+]*((\"(\\.|[^\"])*\")|[^-+|\\s]+)";
		final String reg = "\\s+[-|+\\s]*((\"(\\.|[^\"])*\")|[^\\s]+)";
		final List<SimplePattern> patterns = new ArrayList<SimplePattern>();
		final Pattern pattern = Pattern.compile(reg);
		final Matcher matcher = pattern.matcher(" " + query);
		boolean first = true;
		while (matcher.find()) {
			String word = matcher.group().replace("\"", "").trim();
			String operator = "";
			if (first)
				operator = SimplePattern.OPER_FIRST;
			else if (word.matches("["+ SimplePattern.OPER_LIST+ "].*")) {
				operator = word.substring(0, 1);
				word = word.substring(1).trim(); // ������ ������ ������
			} else if (word.matches("[^\\s]"+ SimplePattern.OPER_PLUS))
				operator = SimplePattern.OPER_PLUS;
			else
				operator = SimplePattern.OPER_DEFAULT;
			patterns.add( new SimplePattern(operator, word) );
			first = false;
		}
		return patterns;
	}
	
	/**
	 * ����� �� ��������
	 * @param baseDoc - ������������ ��������
	 * @param id - �� ��������
	 * @return ���� �������� �������� ��������
	 * @throws DataException
	 * @throws ServiceException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<ObjectId> getBackLinkedCardsObjectIds(Card baseDoc, ObjectId id, DataServiceBean serviceBean) throws DataException, ServiceException {
		final BulkFetchChildrenCards bfcc = new BulkFetchChildrenCards();
		bfcc.setReverseLink(false);
		bfcc.setParentCardIds(Collections.singletonList(baseDoc.getId()));
		bfcc.setLinkAttributeId(id);
		long t1 = System.nanoTime();
		BulkFetchChildrenCards.Result actionResult = serviceBean.doAction(bfcc);
		final Map result = actionResult.getCards();
		long t2 = System.nanoTime();
		final Formatter f = new Formatter();
		logger.info(f.format("getBackLinkedCardsObjectIds %2.3f\n", (t1 - t2)/1.0e9));
		final List<Card> list =  (List<Card>) result.get(baseDoc.getId());
		if(list == null)
			return new ArrayList<ObjectId>();
		final Set<ObjectId> listIds = ObjectIdUtils.cardsToObjectIdsSet(list);
		if(listIds != null) {
			return new ArrayList<ObjectId>(listIds);
		} else {
			return new ArrayList<ObjectId>();
		}
	}
	
	/**
	 * ����� �������� ��������
	 * @param baseDoc - ������������ ��������
	 * @param id - �� ��������
	 * @return ���� �������� ��������
	 * @throws DataException
	 * @throws ServiceException
	 */
	@SuppressWarnings("unchecked")
	public static List<Card> getBackLinkedCards(Card baseDoc, ObjectId id, DataServiceBean serviceBean) throws DataException, ServiceException {
		final ListProject listProject = new ListProject();
		listProject.setAttribute(id);
		listProject.setCard(baseDoc.getId());
		long t1 = System.nanoTime();
		final SearchResult result = (SearchResult) serviceBean.doAction(listProject);
		long t2 = System.nanoTime();
		final Formatter f = new Formatter();
		logger.info(f.format("getBackLinkedCards %2.3f\n", (t1 - t2)/1.0e9));
		if(result.getCards() != null) {
			return result.getCards();
		} else {
			return new ArrayList<Card>();
		}
	}

}
