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
package com.aplana.dmsi.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dmsi.card.DMSIObjectConverter;
import com.aplana.dmsi.value.controllers.ControllerFactory;
import com.aplana.dmsi.value.controllers.ValueController;
import com.aplana.dmsi.value.converters.Converter;
import com.aplana.dmsi.value.filters.CollectionFilter;

public class FieldConfig {
    private String fieldName;
    private Queue<ValueSource> valueSources = new LinkedList<ValueSource>();
    private Converter converter;
    private String typeName;
    private boolean isReadonly;
    private boolean isNewCard;
    private boolean useInSearch;
    private ValueController valueController;
    private DMSIObjectConverter complexFieldConverter;
    private ObjectId attributeId;
    private ClassConfig subConfig;
    private String[] ignoringFields = new String[0];
    private CollectionFilter filter;

    protected FieldConfig() {
    }

    public String getFieldName() {
	return this.fieldName;
    }

    protected void setFieldName(String fieldName) {
	this.fieldName = fieldName;
    }

    public ObjectId getAttributeId() {
	if (attributeId == null) {
	    attributeId = calculateAttributeId();
	}
	return attributeId;
    }

    private ObjectId calculateAttributeId() {
	ObjectId foundAttributeId = null;
	for (Iterator<ValueSource> iterator = valueSources.iterator(); iterator
		.hasNext();) {
	    ValueSource valueSource = iterator.next();
	    if (!valueSource.isReadOnly()
		    && valueSource.getAttributeId() != null) {
		foundAttributeId = valueSource.getAttributeId();
		break;
	    }
	}
	return foundAttributeId;
    }

    public ClassConfig getConfig() {
	if (subConfig == null) {
	    subConfig = calculateConfig();
	}
	return subConfig;
    }

    private ClassConfig calculateConfig() {
	ClassConfig config = null;
	for (Iterator<ValueSource> iterator = valueSources.iterator(); iterator
		.hasNext();) {
	    ValueSource valueSource = iterator.next();
	    if (!valueSource.isReadOnly() && valueSource.getConfig() != null) {
		config = valueSource.getConfig();
		break;
	    }
	}
	return config;
    }

    protected void addValueSource(ValueSource valueSource) {
	valueSources.add(valueSource);
    }

    public Queue<ValueSource> getValueSources() {
	return new LinkedList<ValueSource>(valueSources);
    }

    public Collection<ObjectId> getRequiredAttributeIds() {
	Collection<ObjectId> attributeIds = new HashSet<ObjectId>();
	for (Iterator<ValueSource> iterator = valueSources.iterator(); iterator
		.hasNext();) {
	    ValueSource valueSource = iterator.next();
	    attributeIds.addAll(valueSource.getRequiredAttributeIds());
	}
	return attributeIds;
    }

    public Converter getConverter() {
	return this.converter;
    }

    protected void setConverter(Converter converter) {
	this.converter = converter;
    }

    public String getTypeName() {
	return this.typeName;
    }

    protected void setTypeName(String type) {
	this.typeName = type;
    }

    public boolean isReadonly() {
	return this.isReadonly;
    }

    protected void setReadonly(boolean isReadonly) {
	this.isReadonly = isReadonly;
    }

    public boolean isNewCard() {
	return this.isNewCard;
    }

    protected void setNewCard(boolean isNewCard) {
	this.isNewCard = isNewCard;
    }

    public boolean isUseInSearch() {
	return this.useInSearch;
    }

    protected void setUseInSearch(boolean useInSearch) {
	this.useInSearch = useInSearch;
    }

    public ValueController getValueController() {
	return valueController;
    }

    private void setValueController(ValueController valueController) {
	this.valueController = valueController;
    }

    protected void setControllerFactory(ControllerFactory factory) {
	if (factory == null) {
	    return;
	}
	setValueController(factory.newController());
	setConverter(factory.newConverter());
    }

    public DMSIObjectConverter getComplexFieldConverter() {
	return this.complexFieldConverter;
    }

    protected void setComplexFieldConverter(
	    DMSIObjectConverter complexValueConverter) {
	this.complexFieldConverter = complexValueConverter;
    }

    public String[] getIgnoringFields() {
	return this.ignoringFields;
    }

    protected void setIgnoringFields(String[] ignoringFields) {
	this.ignoringFields = ignoringFields;
    }

    public CollectionFilter getFilter() {
	return this.filter;
    }

    protected void setFilter(CollectionFilter filter) {
	this.filter = filter;
    }
}