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
package com.aplana.dbmi.cardexchange.xml;

import java.util.List;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

public class ListAttributeXMLHandler extends AttributeXMLHandler {
	public ListAttributeXMLHandler(String value, Class clazz) {
		super(value, clazz);
	}
	
	protected static ReferenceValue getReferenceValue(String refCode) {
		ObjectId refId = CardExchangeUtils.getObjectId(ReferenceValue.class, refCode, true);
		return (ReferenceValue)DataObject.createFromId(refId);
	}	
	
	
	public AttributeXMLValue[] getValue(Attribute attr) throws DataException, ServiceException {
		ReferenceValue val  = ((ListAttribute)attr).getValue();
		if (val == null) {
		    return null;
		} else {
		    return new AttributeXMLValue[] {new AttributeXMLValue(val.getId().getId().toString(), val.getValue())};
		}
		
	}

	public void setValues(List values, Attribute attr) {
		ListAttribute a = (ListAttribute)attr;
		if (values.isEmpty()) {
			a.setValue(null);
		} else {
			a.setValue(getReferenceValue(((AttributeXMLValue)values.get(0)).getValue()));
		}
	}

	public boolean matchXmlValue(Attribute attr, AttributeXMLValue value) {		
		ListAttribute a = (ListAttribute)attr;
		if (a.getValue() == null) {
			return "".equals(value.getValue());
		} else {
			return a.getValue().getId().equals(getReferenceValue(value.getValue()).getId());
		}
	}
}
