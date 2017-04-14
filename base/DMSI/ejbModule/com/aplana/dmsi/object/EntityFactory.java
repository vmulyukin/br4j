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
package com.aplana.dmsi.object;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dmsi.DMSIException;
import com.aplana.dmsi.card.handling.CardFacade;
import com.aplana.dmsi.config.AttributeIdValueSource;
import com.aplana.dmsi.config.ClassConfig;
import com.aplana.dmsi.config.ClassConfigManager;
import com.aplana.dmsi.config.ClassConfigValueSource;
import com.aplana.dmsi.config.FieldConfig;
import com.aplana.dmsi.config.GroupValueSource;
import com.aplana.dmsi.config.SelfValueSource;
import com.aplana.dmsi.config.StaticValueSource;
import com.aplana.dmsi.config.ValueSource;
import com.aplana.dmsi.config.AttributeIdValueSource.Target;
import com.aplana.dmsi.types.DMSIObject;
import com.aplana.dmsi.util.ReflectionUtils;
import com.aplana.dmsi.value.filters.AllElementFilter;
import com.aplana.dmsi.value.filters.CollectionFilter;

public class EntityFactory extends DMSIObjectFactory {

	protected EntityFactory() {
    }

    @Override
    protected Object newDMSIObject(Card card) throws DMSIException {
	this.processingCard = card;
	Class<? extends DMSIObject> clazz = getObjectClassByTemlate(card
		.getTemplate());
	ClassConfig config = ClassConfigManager.instance().getConfigByClass(
		clazz);
	return newObject(config);
    }

    protected Object newObject(ClassConfig config) throws DMSIException {
	Class<?> clazz = config.getType();
	Object obj = ReflectionUtils.instantiateClass(clazz);

	CardFacade cardFacade = new CardFacade(serviceBean, processingCard);
	boolean isEmpty = true;
	for (Field field : ReflectionUtils.getAllFields(clazz)) {
	    final String fieldName = field.getName();
	    if (isFieldIgnored(field)) {
		continue;
	    }

	    FieldConfig fieldConfig = config.getFieldConfig(fieldName);
	    if (fieldConfig == null) {
		continue;
	    }

	    Queue<ValueSource> valueSources = fieldConfig.getValueSources();

	    Object value = null;
	    ValueSource valueSource = valueSources.poll();
	    while (valueSource != null && value == null) {
		FieldValueCreator valueCreator = createValueCreator(
			valueSource, field, fieldConfig, cardFacade);
		value = valueCreator.createValue();
		valueSource = valueSources.poll();
	    }

	    if (value == null) {
		logger.warn(String.format("Value for %s field was not get",
			fieldName));
		continue;
	    }

	    isEmpty = false;
	    FieldHandler fieldHandler = new FieldHandler(field, fieldConfig);
	    fieldHandler.setFieldValue(obj, value);
	}

	if (isEmpty) {
	    return null;
	}
	return obj;
    }

    private FieldValueCreator createValueCreator(ValueSource valueSource,
	    Field field, FieldConfig fieldConfig, CardFacade cardFacade) {
	FieldValueCreator valueCreator = null;
	if (valueSource instanceof AttributeIdValueSource) {
	    valueCreator = new ValueByAttributeIdCreator(field, fieldConfig,
		    (AttributeIdValueSource) valueSource, cardFacade);
	} else if (valueSource instanceof ClassConfigValueSource) {
	    valueCreator = new ValueByConfigCreator(
		    (ClassConfigValueSource) valueSource);
	} else if (valueSource instanceof SelfValueSource) {
	    valueCreator = new ValueBySelfCardCreator(field, fieldConfig);
	} else if (valueSource instanceof GroupValueSource) {
	    Collection<FieldValueCreator> creators = new ArrayList<FieldValueCreator>();
	    for (ValueSource groupValueSource : ((GroupValueSource) valueSource)
		    .getSources()) {
		creators.add(createValueCreator(groupValueSource, field,
			fieldConfig, cardFacade));
	    }
	    valueCreator = new GroupValueCreator(creators);
	} else if (valueSource instanceof StaticValueSource) {
	    valueCreator = new StaticValueCreator(((StaticValueSource) valueSource).getValue());
	} else {
	    valueCreator = new StubValueCreator();
	}
	return valueCreator;
    }

    private interface FieldValueCreator {
	public abstract Object createValue() throws DMSIException;
    }

    private abstract class ValueByCardsCreator implements FieldValueCreator {

	protected final Field field;
	protected FieldConfig fieldConfig;

	public ValueByCardsCreator(Field field, FieldConfig fieldConfig) {
	    this.fieldConfig = fieldConfig;
	    this.field = field;
	}

	public abstract Object createValue() throws DMSIException;

	protected Object convert(ObjectId[] ids) throws DMSIException {
	    DMSIObjectFactory relatedFactory = createFactoryForField(fieldConfig);
	    CollectionFilter filter = fieldConfig.getFilter() == null ? new AllElementFilter()
		    : fieldConfig.getFilter();
	    ids = filter.filterIds(ids);
	    Collection<?> values = relatedFactory.newCollection(ids);
	    if (Collection.class.isAssignableFrom(field.getType())) {
		filter.filterCollection(values);
		return values;
	    }
	    return filter.selectOneObject(values);
	}
    }

    private class ValueByAttributeIdCreator extends ValueByCardsCreator {

	private CardFacade cardFacade;
	private final AttributeIdValueSource valueSource;

	public ValueByAttributeIdCreator(Field field, FieldConfig fieldConfig,
		AttributeIdValueSource valueSource, CardFacade cardFacade) {
	    super(field, fieldConfig);
	    this.valueSource = valueSource;
	    this.cardFacade = cardFacade;
	}

	@Override
	public Object createValue() throws DMSIException {
	    ObjectId attributeId = valueSource.getAttributeId();
	    cardFacade.setAttributeValueController(attributeId, fieldConfig
		    .getValueController());
	    Object value = getValue(attributeId);
	    if (value instanceof ObjectId[])
		return convert((ObjectId[]) value);
	    return value;
	}

	private Object getValue(ObjectId attributeId) throws DMSIException {
	    Target target = valueSource.getTarget();
	    Object value = null;
	    switch (target) {
	    case ATTR_NAME:
		value = cardFacade.getAttributeName(attributeId);
		break;
	    case ATTR_VALUE:
		value = cardFacade.getAttributeValue(attributeId);
		break;
    }
	    return value;

	}
    }

    private class ValueBySelfCardCreator extends ValueByCardsCreator {
	public ValueBySelfCardCreator(Field field, FieldConfig fieldConfig) {
	    super(field, fieldConfig);
	}

	@Override
	public Object createValue() throws DMSIException {
	    return convert(new ObjectId[] { processingCard.getId() });
	}
    }

    private class ValueByConfigCreator implements FieldValueCreator {

	private final ClassConfigValueSource valueSource;

	public ValueByConfigCreator(ClassConfigValueSource valueSource) {
	    this.valueSource = valueSource;
	}

	public Object createValue() throws DMSIException {
	    ClassConfig subClassConfig = valueSource.getConfig();
	    return newObject(subClassConfig);
	}
    }

    private class StubValueCreator implements FieldValueCreator {
	public StubValueCreator() {
	}

	public Object createValue() throws DMSIException {
	    return null;
	}
    }

    private class GroupValueCreator implements FieldValueCreator {
	private Collection<FieldValueCreator> creators = new ArrayList<FieldValueCreator>();

	public GroupValueCreator(Collection<FieldValueCreator> creators) {
	    this.creators.addAll(creators);
	}

	public Object createValue() throws DMSIException {
	    Collection<Object> values = new HashSet<Object>();
	    for (FieldValueCreator creator : creators) {
		Object value = creator.createValue();
		if (value instanceof Collection) {
		    values.addAll((Collection<?>) value);
		} else if (value != null) {
		    values.add(value);
		}
	    }
	    return new ArrayList<Object>(values);
	}
    }

	private static class StaticValueCreator implements FieldValueCreator {

		private String value;

		public StaticValueCreator(String value) {
			super();
			this.value = value;
		}

		public Object createValue() throws DMSIException {
			return value;
		}
	}

}
