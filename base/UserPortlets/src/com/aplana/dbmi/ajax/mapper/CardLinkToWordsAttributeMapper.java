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
package com.aplana.dbmi.ajax.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;

/**
 * ����������� �����-������, ������� ��������� ���������� ������ �������� � ��������� ��������� ������-���� ��������.
 * ��������, � �������� "������ �� ��������� ����������������" � ���� "���������������" ��� ������ ����� ���������� ������ �������� "������������" 
 * �� �������� "���������������" �� ���������� ��
 * 
 * ��������� ����������� �������� ������ �������� �� �������. ��� ����� ���������� �������� ���������:<br/>
 * <li/> {@code<parameter name="filterByStatus">true</parameter>}
 * � ������� ����������� ������� � ��������� states ����� �������, ��������:<br/>
 * <li/> {@code<parameter name="states">poruchcancelled,closedviaparent</parameter>}<br/><br/>
 * 
 * ���� �������� ���� {@code<parameter name="inverse">true</parameter>}, �� ���������� ��� �������� �� ����������� ���� � ��������� ��������.
 * 
 */
public class CardLinkToWordsAttributeMapper extends SearchParametersMapper {

	private static final String WORDS_DELIMETER = ", ";
	private static final String FILTER_BY_STATUS = "filterByStatus";
	private static final String INVERSE = "inverse";
	private static final String STATES = "states";

	protected final Log logger = LogFactory.getLog(getClass());

	List<ObjectId> states = Collections.emptyList();
	boolean inverse;

	@Override
	public void perform(Search search, String cardIds) {
		try {
			initParams();

			StringBuilder words = new StringBuilder();

			if (getParameter(FILTER_BY_STATUS) != null && Boolean.valueOf(getParameter(FILTER_BY_STATUS))) {
				Collection<ObjectId> ids = Collections.emptyList();

				List<Card> cards = CardUtils.loadCardsByCode(getDataServiceBean(), cardIds, Card.ATTR_STATE);

				if (cards != null) {
					ids = new ArrayList<ObjectId>(cards.size());
					for (Card card : cards) {
						boolean contains = states.contains(card.getState());
						if (inverse) {
							contains = !contains;
						}
						if (contains) {
							ids.add(card.getId());
						}
					}
				}
				words.append(ObjectIdUtils.numericIdsToCommaDelimitedString(ids));

				if (logger.isDebugEnabled()) {
					printFilterDebugInfo(cardIds, words.toString());
				}
			} else {
				words.append(cardIds);
			}

			if (search.getWords() != null && !search.getWords().isEmpty()) {
				words.append(WORDS_DELIMETER);
				words.append(search.getWords());
			}

			search.setByAttributes(false);
			search.setByCode(true);
			search.setWords(words.toString());
		} catch (Exception e) {
			logger.error("Error during processing dependency search paramebers: " + e.getMessage(), e);
		}
	}

	private List<ObjectId> getStatesByIds(String statesIds) {
		List<ObjectId> states = Collections.emptyList();
		if (statesIds != null && !statesIds.isEmpty()) {
			String[] ids = statesIds.split(",");
			if (ids != null && ids.length > 0) {
				states = new ArrayList<ObjectId>(ids.length);
				for (String id : ids) {
					states.add(ObjectId.state(id));
				}
			}
		}
		return states;
	}

	private void printFilterDebugInfo(String initialCardIds, String filteredCardIds) {
		logger.debug("Filter with params: {states=[" + ObjectIdUtils.numericIdsToCommaDelimitedString(states) + "], " +
					 "inversion flag = [" + inverse + "]} " +
					 "was applied to [" + initialCardIds + "] and result is [" + filteredCardIds + "]");
	}

	private void initParams() {
		states = getStatesByIds(getParameter(STATES));
		inverse = Boolean.valueOf(getParameter(INVERSE));
	}
}