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
package com.aplana.dmsi.card.handling;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dmsi.DMSIException;

abstract class AttributeHandler {

    private Attribute attribute;
    private DataServiceFacade serviceBean;

    public AttributeHandler(Attribute attribute, DataServiceFacade serviceBean) {
	this.attribute = attribute;
	this.serviceBean = serviceBean;
    }

    public AttributeHandler() {
    }

    public Attribute getAttribute() {
	return this.attribute;
    }

    public void setAttribute(Attribute attribute) {
	this.attribute = attribute;
    }

    public String getAttributeName() throws DMSIException {
	if (this.attribute == null) {
	    return "";
	}
	String name = this.attribute.getName();
	if (name == null || "".equals(name)) {
	    try {
		Attribute reloadedAttribute = loadAttribute(attribute.getId());
		name = reloadedAttribute.getName();
	    } catch (DataException ex) {
		throw new DMSIException(ex);
	    }
	}
	return name;
    }

    private Attribute loadAttribute(ObjectId attributeId) throws DataException {
	return (Attribute) getServiceBean().getById(attributeId);
    }

    protected DataServiceFacade getServiceBean() {
	return this.serviceBean;
    }

    public abstract Object getAttributeValue() throws DMSIException;

    public abstract void setAttributeValue(Object value) throws DMSIException;
}