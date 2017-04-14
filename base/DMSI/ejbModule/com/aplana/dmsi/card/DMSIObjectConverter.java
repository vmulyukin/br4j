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

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dmsi.DMSIException;
import com.aplana.dmsi.card.handling.TypedObjectId;
import com.aplana.dmsi.types.DMSIObject;
import com.aplana.dmsi.types.TypedLink;

public abstract class DMSIObjectConverter {
    protected DataServiceFacade serviceBean;

    private boolean isConverted;

    public DMSIObjectConverter() {
    }

    public void setServiceBean(DataServiceFacade serviceBean) {
	this.serviceBean = serviceBean;
    }

    public ObjectId convert(Object value) throws DMSIException {
	isConverted = false;
	if (value instanceof DMSIObject) {
	    return convertAndUpdateObject((DMSIObject) value);
	} else if (value instanceof TypedLink) {
	    TypedLink link = (TypedLink) value;
	    ObjectId id = convertAndUpdateObject(link.getObject());
	    return new TypedObjectId(id, link.getType().getId());
	} else if (shouldTypeOfValueBeChecked()) {
	    throw new DMSIException(String.format(
		    "Incorrect value type: %s, should be instance of %s or %s",
		    value.getClass().getName(), DMSIObject.class.getName(),
		    TypedLink.class.getName()));
	}
	return null;
    }

    protected boolean shouldTypeOfValueBeChecked() {
	return false;
    }

    private ObjectId convertAndUpdateObject(DMSIObject value)
	    throws DMSIException {
	ObjectId valueId = convert(value);
	if (value != null && valueId != null) {
	    value.setId(String.valueOf(valueId.getId()));
	}
	isConverted = true;
	return valueId;
    }

    public boolean isConverted() {
	return this.isConverted;
    }

    protected abstract ObjectId convert(DMSIObject value) throws DMSIException;

}