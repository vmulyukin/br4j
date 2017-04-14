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
package com.aplana.dmsi.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.aplana.dbmi.model.ObjectId;

public class GroupValueSource extends ValueSource {
    private List<ValueSource> sources = new ArrayList<ValueSource>();

    public GroupValueSource(Collection<ValueSource> sources, boolean isReadOnly) {
	super(isReadOnly);
	this.sources.addAll(sources);
    }

    public List<ValueSource> getSources() {
	return Collections.unmodifiableList(sources);
    }

    @Override
    public ObjectId getAttributeId() {
	for (ValueSource source : sources) {
	    if (!source.isReadOnly() && source.getAttributeId() != null)
		return source.getAttributeId();
	}
	return null;
    }

    @Override
    public ClassConfig getConfig() {
	for (ValueSource source : sources) {
	    if (!source.isReadOnly() && source.getConfig() != null)
		return source.getConfig();
	}
	return null;
    }

    @Override
    public Collection<ObjectId> getRequiredAttributeIds() {
	Collection<ObjectId> attributeIds = super.getRequiredAttributeIds();
	for (ValueSource valueSource : sources) {
	    attributeIds.addAll(valueSource.getRequiredAttributeIds());
	}
	return attributeIds;
    }
}