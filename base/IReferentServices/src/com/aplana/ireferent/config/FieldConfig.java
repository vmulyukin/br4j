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
package com.aplana.ireferent.config;

import java.util.HashMap;
import java.util.Map;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.ireferent.value.controllers.ControllerFactory;
import com.aplana.ireferent.value.controllers.ValueController;
import com.aplana.ireferent.value.converters.Converter;
import com.aplana.ireferent.value.filters.CollectionFilter;

public class FieldConfig {
    private String fieldName;
    private ObjectId attributeId;
    private ClassConfig config;
    private Converter converter;
    private String typeName;
    private boolean isReadonly;
    private boolean isNewCard;
    private boolean isParent;
    private ValueController valueController;
    private Map<String, CollectionFilter> filters = new HashMap<String, CollectionFilter>();

    protected FieldConfig() {
    }

    public String getFieldName() {
	return this.fieldName;
    }

    protected void setFieldName(String fieldName) {
	this.fieldName = fieldName;
    }

    public ObjectId getAttributeId() {
	return this.attributeId;
    }

    protected void setAttributeId(ObjectId attributeId) {
	this.attributeId = attributeId;
    }

    public ClassConfig getConfig() {
	return this.config;
    }

    protected void setConfig(ClassConfig config) {
	this.config = config;
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
    
    public boolean isParent() {
    	return this.isParent;
    }

    protected void setParent(boolean isParent) {
    	this.isParent = isParent;
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
    
    public CollectionFilter getFilter(String setId) {
    	return this.filters.get(setId);
    }
    
    public CollectionFilter getFilter() {
    	return this.filters.get("");
    }

    protected void setFilters(Map<String, CollectionFilter> filters) {
    	this.filters.putAll(filters);
    }
}