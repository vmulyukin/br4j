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
package com.aplana.dmsi.card;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.Search.TextSearchConfigValue;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dmsi.DMSIException;
import com.aplana.dmsi.Parametrized;
import com.aplana.dmsi.config.ClassConfig;
import com.aplana.dmsi.config.ClassConfigManager;
import com.aplana.dmsi.config.ConfigurationException;
import com.aplana.dmsi.config.FieldConfig;
import com.aplana.dmsi.object.ObjectParser;
import com.aplana.dmsi.object.ObjectParser.AttributeValue;
import com.aplana.dmsi.object.ObjectParserConfigurator;
import com.aplana.dmsi.types.DMSIObject;
import com.aplana.dmsi.util.ServiceUtils;

public class DMSIObjectBySearchConverter extends DMSIObjectConverter implements Parametrized {

	private static final String CREATE_CARD_PARAM = "createCard";
	private static final String STATES_PARAM = "states";
	private static final String STATIC_SEARCH_ATTR_PARAM = "staticSearchAttribute";
	private static final String NULL_RESULT_PARAM = "returnNullIfMany";

	private boolean createCardIfNotFound;
	private boolean nullResultIfManyFound;

	private Collection<ObjectId> stateIds = new HashSet<ObjectId>();
	private Map<ObjectId, String> predefinedAttributes = new HashMap<ObjectId, String>();

	protected Log logger = LogFactory.getLog(getClass());

	protected DMSIObject processingObject;

	public void setParameter(String key, Object value) {
		if (NULL_RESULT_PARAM.equals(key)) {
			setNullResultIfManyFound(Boolean.parseBoolean((String) value));
		} else if (CREATE_CARD_PARAM.equals(key)) {
			setCreateCardIfNotFound(Boolean.parseBoolean((String) value));
		} else if (STATES_PARAM.equals(key)) {
			String statesDescription = ((String) value).trim();
			if ("".equals(statesDescription))
				return;
			String[] idDescriptions = statesDescription.split("\\s*,\\s*");
			for (String idDescription : idDescriptions) {
				addStateId(ObjectIdUtils.getObjectId(CardState.class, idDescription, true));
			}
		} else if (key.startsWith(STATIC_SEARCH_ATTR_PARAM)) {
			String[] paramParts = ((String) value).split("\\s*=\\s*");
			if (paramParts.length != 2) {
				throw new ConfigurationException("Incorrect value of " + key
						+ " param: should be in format [objectId=value]");
			}
			ObjectId attrId = ObjectIdUtils.getAttrObjectId(paramParts[0], ":");
			predefinedAttributes.put(attrId, paramParts[1]);
		}
	}

	protected void addStateId(ObjectId stateId) {
		stateIds.add(stateId);
	}

	@Override
	protected ObjectId convert(DMSIObject value) throws DMSIException {
		if (serviceBean == null) {
			throw new IllegalStateException("Service bean should be set before converter using");
		}
		processingObject = getObject(value);
		Search search = getSearch(processingObject, new SearchByConfigParserConfigurator(serviceBean), TextSearchConfigValue.EXACT_MATCH);
		if (search == null) {
			return null;
		}

		Collection<Card> cards = ServiceUtils.searchCards(serviceBean, search, getRequiredAttributes());

		if (cards.isEmpty()) {
			if (createCardIfNotFound) {
				ServiceUtils.setWarningMessage("Not found any card. New card will be created.", "Search description: "
						+ ServiceUtils.getSearchDescription(search));
				return createCard(processingObject);
			}
			ServiceUtils.setWarningMessage("Not found any card.",
					"Search description: " + ServiceUtils.getSearchDescription(search));
			return null;
		}

		if (cards.size() > 1) { 
			String warnMessage = "More than one card were found. " + 
					(isNullResultIfManyFound() ? "Null will be returned." : "First will be used.");
			ServiceUtils.setWarningMessage(warnMessage,
					String.format("Search description: " + ServiceUtils.getSearchDescription(search)));
			logger.warn(warnMessage);

			if (isNullResultIfManyFound()) {
				return null;
			}
		}
		return getCardId(cards.iterator().next());
	}

	protected ObjectId getCardId(Card card) {
		return card.getId();
	}

	protected Collection<ObjectId> getRequiredAttributes() {
		return Collections.emptyList();
	}

	protected boolean isCreateCardIfNotFound() {
		return this.createCardIfNotFound;
	}

	protected void setCreateCardIfNotFound(boolean createCardIfNotFound) {
		this.createCardIfNotFound = createCardIfNotFound;
	}

	public boolean isNullResultIfManyFound() {
		return nullResultIfManyFound;
	}

	public void setNullResultIfManyFound(boolean nullResultIfManyFound) {
		this.nullResultIfManyFound = nullResultIfManyFound;
	}

	protected DMSIObject getObject(DMSIObject value) {
		return value;
	}

	protected Search getSearch(DMSIObject object, ObjectParserConfigurator parserConfigurator, int typeTextSearch) throws DMSIException {
		ObjectParser objectParser = new ObjectParser(parserConfigurator);
		Collection<AttributeValue> values = objectParser.parseValues(object);

		ClassConfig config = ClassConfigManager.instance().getConfigByClass(object.getClass());

		Search search = new Search();
		if (stateIds != null) {
			Collection<Object> idStrings = new ArrayList<Object>();
			for (ObjectId stateId : stateIds) {
				idStrings.add(stateId.getId());
			}
			search.setStates(idStrings);
		}
		search.setTemplates(Collections.singleton(DataObject.createFromId(config.getTemplateId())));
		search.setByAttributes(true);

		for (Entry<ObjectId, String> predefinedAttribute : predefinedAttributes.entrySet()) {
			values.add(new AttributeValue(predefinedAttribute.getKey(), predefinedAttribute.getValue(), null));
		}

		boolean isSearchParameterPresent = false;
		for (AttributeValue attributeValue : values) {
			Object value = attributeValue.getValue();
			ObjectId attributeId = attributeValue.getAttributeId();
			SearchValueSetter setter = resolveValueSetter(value, attributeId, typeTextSearch);
			isSearchParameterPresent |= setter.setValue(search, attributeId, value);
		}
		if (!isSearchParameterPresent) {
			logger.error("There are no parameters for search. Ignored " + object.getClass().getName());
			return null;
		}
		return search;
	}

	public static class SearchByConfigParserConfigurator implements ObjectParserConfigurator {

		private DataServiceFacade serviceBean;

		public SearchByConfigParserConfigurator(DataServiceFacade serviceBean) {
			this.serviceBean = serviceBean;
		}

		public ClassConfig getConfig(DMSIObject dmsiObject) {
			return ClassConfigManager.instance().getConfigByClass(dmsiObject.getClass());
		}

		public DMSIObjectConverter getFieldConverter(FieldConfig fieldConfig) {
			DMSIObjectConverter complexValueConverter;
			if (fieldConfig.getComplexFieldConverter() != null) {
				complexValueConverter = fieldConfig.getComplexFieldConverter();
			} else {
				complexValueConverter = new StubConverter();
			}
			complexValueConverter.setServiceBean(serviceBean);
			return complexValueConverter;
		}

		public boolean isFieldIgnoring(FieldConfig fieldConfig) {
			return !fieldConfig.isUseInSearch();
		}
	}

	private static class StubConverter extends DMSIObjectConverter {
		public StubConverter() {
		}

		@Override
		protected ObjectId convert(DMSIObject value) throws DMSIException {
			return null;
		}
	}

	private static interface SearchValueSetter {
		boolean setValue(Search search, ObjectId attributeId, Object value);
	}

	private static class StringSetter implements SearchValueSetter {
		private int typeSearch;
		public StringSetter(int typeSearch) {
			this.typeSearch = typeSearch;
		}

		public boolean setValue(Search search, ObjectId attrId, Object attrValue) {
			if (!(attrValue instanceof String)) {
				throw new UnsupportedOperationException("Only string value is supported. Actual is "
						+ attrValue.getClass());
			}
			String str = (String) attrValue;
			boolean isSearchParameterPresent = false;
			if (!"".equals(str)) {
				isSearchParameterPresent = true;
				search.addStringAttribute(attrId, str, typeSearch);
			}
			return isSearchParameterPresent;
		}
	}

	private static class CardLinkSetter implements SearchValueSetter {
		public CardLinkSetter() {
		}

		public boolean setValue(Search search, ObjectId attrId, Object attrValue) {
			ObjectId[] ids;
			if (attrValue instanceof ObjectId) {
				ids = new ObjectId[] { (ObjectId) attrValue };
			} else if (attrValue instanceof ObjectId[]) {
				ids = (ObjectId[]) attrValue;
			} else {
				throw new UnsupportedOperationException("Only id value is supported. Actual is " + attrValue.getClass());
			}

			for (ObjectId cardId : ids) {
				search.addCardLinkAttribute(attrId, cardId);
			}

			return true;
		}
	}

	private static class NumberSetter implements SearchValueSetter {
		public NumberSetter() {
		}

		public boolean setValue(Search search, ObjectId attrId, Object attrValue) {
			if (attrValue instanceof BigInteger) {
				attrValue = ((BigInteger) attrValue).intValue();
			}
			if (!(attrValue instanceof Integer)) {
				throw new UnsupportedOperationException("Only integer value is supported. Actual is "
						+ attrValue.getClass());
			}
			Integer value = (Integer) attrValue;
			search.addIntegerAttribute(attrId, value, value);
			return true;
		}
	}

	private static class ListSetter implements SearchValueSetter {
		public ListSetter() {
		}

		public boolean setValue(Search search, ObjectId attributeId, Object attrValue) {
			ReferenceValue val = new ReferenceValue();
			if (attrValue instanceof String) {
				ObjectId valueId = ObjectIdUtils.getObjectId(ReferenceValue.class, (String) attrValue, true);
				val.setId(valueId);
			} else if (attrValue instanceof ObjectId) {
				val.setId((ObjectId) attrValue);
			} else if (attrValue instanceof ReferenceValue) {
				val = (ReferenceValue) attrValue;
			} else {
				throw new UnsupportedOperationException("Type [" + attrValue.getClass()
						+ "] of list attribute is not supported");
			}
			search.addListAttribute(attributeId, Collections.singleton(val));
			return true;
		}
	}

	private SearchValueSetter resolveValueSetter(Object value, ObjectId attributeId, int typeSearch) {

		if (attributeId == null) {
			throw new ConfigurationException("Attribute is not defined. Value is " + value);
		}
		Class<?> attributeType = attributeId.getType();
		if (StringAttribute.class.isAssignableFrom(attributeType)
				|| TextAttribute.class.isAssignableFrom(attributeType)) {
			return new StringSetter(typeSearch);
		} else if (CardLinkAttribute.class.isAssignableFrom(attributeType)) {
			return new CardLinkSetter();
		} else if (IntegerAttribute.class.isAssignableFrom(attributeType)) {
			return new NumberSetter();
		} else if (ListAttribute.class.isAssignableFrom(attributeType)) {
			return new ListSetter();
		}

		throw new UnsupportedOperationException("This attribute type is not supported here " + attributeType);

	}

	protected ObjectId createCard(DMSIObject object) throws DMSIException {
		return new CardHandler(serviceBean).createCard(object);
	}
}
