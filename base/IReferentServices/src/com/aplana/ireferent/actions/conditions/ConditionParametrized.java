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
package com.aplana.ireferent.actions.conditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.config.ConfigurationException;
import com.aplana.ireferent.types.WSObject;
import com.aplana.ireferent.util.ServiceUtils;

public abstract class ConditionParametrized implements Condition {
	
	 public static final String AFFECTED_STATES_PARAM = "affectedStates";
	 public static final String LINK_PARAM = "link";
	 public static final String FILTER_BY_USER_PARAM = "filterByUser";
	 public static final String ID_SOURCE_PARAM = "idSource";
	 public static final String CONFIG_SEARCH_PARAM = "configSearch";
	 
	 private static Log logger = LogFactory.getLog(ConditionParametrized.class);
	    
	 private ObjectId link = null;
	 private ObjectId userFilterAttributeId = null;
	 private String idSource = null;
	 private Set<ObjectId> affectedStates = new HashSet<ObjectId>();
	 private String configSearch;
	    
	public void setParameter(String key, Object value) {
		if (CONFIG_SEARCH_PARAM.equals(key)) {
			configSearch = (String) value;
		} else if (AFFECTED_STATES_PARAM.equals(key)) {
			List<ObjectId> states = ObjectIdUtils.commaDelimitedStringToIds(
					(String) value, CardState.class);
			affectedStates.addAll(states);
		} else if (LINK_PARAM.equals(key)) {
			link = ObjectIdUtils.getObjectId(CardLinkAttribute.class,
					(String) value, false);
		} else if (FILTER_BY_USER_PARAM.equals(key)) {
			userFilterAttributeId = ObjectIdUtils.getObjectId(
					PersonAttribute.class, (String) value, false);
		} else if (ID_SOURCE_PARAM.equals(key)) {
			idSource = (String) value;
		}
	}

	protected ObjectId getLink() {
		return link;
	}

	protected Collection<Card> getFilteredCards(DataServiceBean serviceBean,
			WSObject object) {
		Collection<Card> cards = getCards(serviceBean, object);
		filterCards(serviceBean, cards);
		return cards;
	}

	private Collection<Card> getCards(DataServiceBean serviceBean,
			WSObject object) {
		Search search = new Search();
		Collection<ObjectId> requiredAttributes = new HashSet<ObjectId>();
		if (null != configSearch && !configSearch.trim().isEmpty()) {
			InputStream config;
			try {
				config = ServiceUtils.readConfig(configSearch);
				search.initFromXml(config);
			} catch (DataException de) {
				logger.error("Error initializing config handler:"
						+ configSearch, de);
			} catch (IOException ioe) {
				logger.error("Error reading config handler:" + configSearch,
						ioe);
			}
		} else {
			search.setByCode(true);
			search.setWords(getId(object));
			if (userFilterAttributeId != null) {
				requiredAttributes.add(userFilterAttributeId);
			}
			if (link != null) {
				search.setFetchLink(link);
			}
		}
		return ServiceUtils
				.searchCards(serviceBean, search, requiredAttributes);
	}

	protected String getId(WSObject obj) {
		if (idSource != null) {
			try {
				return (String) PropertyUtils.getProperty(obj, idSource);
			} catch (Exception ex) {
				throw new ConfigurationException("It is unable to get id from "
						+ idSource, ex);
			}
		}
		return obj.getId();
	}

	private void filterCards(DataServiceBean serviceBean, Collection<Card> cards) {
		for (Iterator<Card> iterator = cards.iterator(); iterator.hasNext();) {
			Card card = iterator.next();
			if (!affectedStates.isEmpty()
					&& !affectedStates.contains(card.getState())) {
				iterator.remove();
				continue;
			}

			if (userFilterAttributeId != null) {
				PersonAttribute filterAttribute = (PersonAttribute) card
						.getAttributeById(userFilterAttributeId);
				if (filterAttribute == null) {
					iterator.remove();
					continue;
				}

				Set<ObjectId> personIds = new HashSet<ObjectId>();
				ObjectIdUtils.fillObjectIdSetFromCollection(personIds,
						filterAttribute.getValues());
				if (!personIds.contains(serviceBean.getPerson().getId())) {
					iterator.remove();
				}
			}
		}
	}
}
