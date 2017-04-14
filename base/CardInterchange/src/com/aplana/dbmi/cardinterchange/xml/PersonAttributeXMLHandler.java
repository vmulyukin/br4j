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
package com.aplana.dbmi.cardinterchange.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;

public class PersonAttributeXMLHandler extends AttributeXMLHandler {

	protected PersonAttributeXMLHandler(String value, Class clazz) {
		super(value, clazz);
	}

	public AttributeXMLValue[] getValue(Attribute attr) {
		Collection values = ((PersonAttribute)attr).getValues();
		AttributeXMLValue[] result = new AttributeXMLValue[values.size()];
		int index = 0;
		Iterator i = values.iterator();
		while (i.hasNext()) {
			Person val = (Person)i.next();
			result[index] = new AttributeXMLValue(val.getId().getId().toString(), val.getFullName());
			++index;
		}
		return result;
	}

	public void setValues(List values, Attribute attr) {
		PersonAttribute a = (PersonAttribute)attr;
		List persons = new ArrayList(values.size());
		for (int i = 0; i < values.size(); ++i) {
			ObjectId personId = ObjectIdUtils.getObjectId(Person.class, ((AttributeXMLValue)values.get(i)).getValue(), true);
			persons.add(DataObject.createFromId(personId));
			if (!attr.isMultiValued()) {
				break;
			}
		}
		a.setValues(persons);
	}
}
