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

import com.aplana.dbmi.model.ObjectId;

public class AttributeIdValueSource extends ValueSource {
    private ObjectId attributeId;
    private Target target = Target.ATTR_VALUE;

    public static enum Target {
	ATTR_NAME, ATTR_VALUE
    }

    public AttributeIdValueSource(ObjectId attributeId, boolean isReadOnly) {
	super(isReadOnly);
	this.attributeId = attributeId;
    }

    public Target getTarget() {
	return target;
    }

    protected void setTarget(Target target) {
	if (target == null) {
	    throw new NullPointerException(
		    "Attribute target should not be null value");
	}
	this.target = target;
    }

    @Override
    public ObjectId getAttributeId() {
	return attributeId;
    }

    @Override
    public Collection<ObjectId> getRequiredAttributeIds() {
	Collection<ObjectId> attributeIds = super.getRequiredAttributeIds();
	attributeIds.add(attributeId);
	return attributeIds;
    }
}