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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.aplana.dbmi.model.ObjectId;

public class ClassConfig {
    private Class<?> type;
    private ObjectId templateId;

    private Map<String, FieldConfig> fieldConfigs = new HashMap<String, FieldConfig>();

    protected ClassConfig() {
    }

    @Override
    protected ClassConfig clone() {
	ClassConfig clonedObject = new ClassConfig();
	clonedObject.templateId = getTemplateId();
	clonedObject.fieldConfigs.putAll(fieldConfigs);
	return clonedObject;
    }

    public Collection<ObjectId> getRequiredAttributes() {
	Collection<ObjectId> attributeIds = new HashSet<ObjectId>();
	for (FieldConfig fieldConfig : fieldConfigs.values()) {
	    attributeIds.addAll(fieldConfig.getRequiredAttributeIds());
	}
	return attributeIds;
    }

    public ObjectId getTemplateId() {
	return this.templateId;
    }

    protected void setTemplateId(ObjectId templateId) {
	this.templateId = templateId;
    }

    protected void addFieldConfig(FieldConfig fieldConfig) {
	fieldConfigs.put(fieldConfig.getFieldName(), fieldConfig);
    }

    public FieldConfig getFieldConfig(String fieldName) {
	return fieldConfigs.get(fieldName);
    }

    public Class<?> getType() {
	return this.type;
    }

    protected void setType(Class<?> type) {
	this.type = type;
    }
}
