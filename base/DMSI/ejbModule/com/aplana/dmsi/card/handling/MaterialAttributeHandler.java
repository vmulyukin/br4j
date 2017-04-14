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
import com.aplana.dbmi.model.MaterialAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dmsi.DMSIException;
import com.aplana.dmsi.util.ServiceUtils;

class MaterialAttributeHandler extends AttributeHandler {

	public static interface CardIdSource {
		ObjectId getCardId();
	}

    private CardIdSource cardIdSource;

    public MaterialAttributeHandler(CardIdSource cardIdSource, Attribute attribute,
    		DataServiceFacade serviceBean) {
	super(attribute, serviceBean);
	this.cardIdSource = cardIdSource;
    }

    @Override
    public Object getAttributeValue() throws DMSIException {
	MaterialAttribute materialAttribute = ((MaterialAttribute) getAttribute());
	if (materialAttribute.getMaterialType() == MaterialAttribute.MATERIAL_URL) {
	    return materialAttribute.getMaterialName();
	}
	return ServiceUtils.getMaterial(getServiceBean(), this.cardIdSource.getCardId());
    }

    @Override
    public void setAttributeValue(Object value) {
	if (value instanceof String) {
	    MaterialAttribute materialAttribute = ((MaterialAttribute) getAttribute());
	    materialAttribute.setMaterialType(MaterialAttribute.MATERIAL_URL);
	    materialAttribute.setMaterialName((String) value);
	}
    }
}