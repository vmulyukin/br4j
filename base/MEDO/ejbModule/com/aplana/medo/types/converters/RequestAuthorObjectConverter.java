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
package com.aplana.medo.types.converters;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.Search.TextSearchConfigValue;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dmsi.DMSIException;
import com.aplana.dmsi.card.DMSIObjectBySearchWithDuplicatesConverter;
import com.aplana.dmsi.card.OneToManyDMSIObjectConverter;
import com.aplana.dmsi.config.FieldConfig;
import com.aplana.dmsi.types.DMSIObject;
import com.aplana.dmsi.types.common.RequestAuthor;
import com.aplana.dmsi.util.ServiceUtils;

public class RequestAuthorObjectConverter extends DMSIObjectBySearchWithDuplicatesConverter implements
		OneToManyDMSIObjectConverter {

	private final static String MIDDLE_NAME_PATTERN = "([\\p{L}\\s]*)\\(+(.*)\\)+";
	private final static String PARAM_MAIN_SEARCH_FIELDS = "mainSearchFieldNames";
	private final static String PARAM_EXTRA_SEARCH_FIELD = "extraSearchFieldNames";

	private final static Pattern middleNamePattern = Pattern.compile(MIDDLE_NAME_PATTERN);
	protected Collection<String> mainSearchFieldNames = Collections.emptyList();
	protected Collection<String> extraSearchFieldNames = Collections.emptyList();

	private Set<ObjectId> extraValues = new HashSet<ObjectId>();

	@Override
	public void setParameter(String key, Object value) {
		if (PARAM_MAIN_SEARCH_FIELDS.equals(key)) {
			mainSearchFieldNames = Arrays.asList(((String) value).split("\\s*,\\s*"));
		} else if (PARAM_EXTRA_SEARCH_FIELD.equals(key)) {
			extraSearchFieldNames = Arrays.asList(((String) value).split("\\s*,\\s*"));
		} else {
			super.setParameter(key, value);
		}
	}

	@Override
	protected ObjectId convert(DMSIObject value) throws DMSIException {
		this.extraValues.clear();
		RequestAuthor author = (RequestAuthor) value;
		String middleName = author.getMidleName();
		if (middleName != null) {
			Matcher matcher = middleNamePattern.matcher(middleName);
			if (matcher.matches()) {
				String editedMiddleName = matcher.group(1);
				author.setMidleName(editedMiddleName.trim());
			}
		}

		if (serviceBean == null) {
			throw new IllegalStateException("Service bean should be set before converter using");
		}
		processingObject = getObject(value);
		Search search = getSearch(processingObject, new SearchByConfigParserConfigurator(serviceBean) {
			@Override
			public boolean isFieldIgnoring(FieldConfig fieldConfig) {
				return !mainSearchFieldNames.contains(fieldConfig.getFieldName());
			}
		}, TextSearchConfigValue.EXACT_MATCH_NOT_CASE_SENSITIVE);

		Collection<Card> cards = Collections.emptyList();
		if (search != null) {
			cards = ServiceUtils.searchCards(serviceBean, search, getRequiredAttributes());
		}

		if (cards.isEmpty()) {

			ObjectId createdCardId = null;

			if (search != null && isCreateCardIfNotFound()) {
				ServiceUtils.setWarningMessage("Not found any card. New card will be created.", "Search description: "
						+ ServiceUtils.getSearchDescription(search));
				createdCardId = createCard(processingObject);
			}

			Search extraSearch = getSearch(processingObject, new SearchByConfigParserConfigurator(serviceBean) {
				@Override
				public boolean isFieldIgnoring(FieldConfig fieldConfig) {
					return !extraSearchFieldNames.contains(fieldConfig.getFieldName());
				}
			}, TextSearchConfigValue.EXACT_MATCH_NOT_CASE_SENSITIVE);

			if (extraSearch != null) {
				Collection<Card> extraCards = ServiceUtils.searchCards(serviceBean, extraSearch,
						getRequiredAttributes());
				for (Card extraCard : extraCards) {
					extraValues.add(getCardId(extraCard));
				}
			}

			ServiceUtils.setWarningMessage("Not found exact card.",
					"Search description: " + ServiceUtils.getSearchDescription(search));
			return createdCardId;
		}

		Iterator<Card> cardsIterator = cards.iterator();
		ObjectId firstCard = getCardId(cardsIterator.next());
		cardsIterator.remove();
		for (Card card : cards) {
			extraValues.add(getCardId(card));
		}

		return firstCard;
	}

	public Set<ObjectId> getExtraValues() {
		return extraValues;
	}

}
