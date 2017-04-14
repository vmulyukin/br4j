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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.cardexchange.service.ServicesProvider;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.util.PersonCard;

public class PersonAttributeXMLHandler extends AttributeXMLHandler {

	protected Log logger = LogFactory.getLog(getClass());
	
	public static final String TEMPLATE_PERSON = "jbr.internalPerson";
	public static final String PERSON_NAME = "name";

	protected PersonAttributeXMLHandler(String value, Class clazz) {
		super(value, clazz);
	}

	public AttributeXMLValue[] getValue(Attribute attr) {
		Collection values = ((PersonAttribute) attr).getValues();
		AttributeXMLValue[] result = new AttributeXMLValue[values.size()];
		int index = 0;
		Iterator i = values.iterator();
		while (i.hasNext()) {
		    	Person val = (Person) i.next();
			if (val == null) {
			    result[index] = null;
			} else {
				String personFio;
				try {
					PersonCard personCard = new PersonCard(val.getCardId(), ServicesProvider.ServiceBeanInstance());
					personFio = personCard.getFio();
				} catch (Exception ex) {
					if (logger.isWarnEnabled()) {
						logger.warn("Cannot retreive PersonCard object for gathering FIO", ex);
					}
					personFio = val.getFullName();
				}

				result[index] = new AttributeXMLValue(val.getId().getId().toString(), personFio);
			}
			++index;
		}
		return result;
	}

	public void setValues(List values, Attribute attr) {
		PersonAttribute a = (PersonAttribute) attr;
		List persons = new ArrayList(values.size());
		String stringValue = null;
		ObjectId personId = null;
		boolean isID = true;
		Long cardID = null;
		System.out.println("Processing person!");
		try {
			for (int i = 0; i < values.size(); ++i) {
				stringValue =((AttributeXMLValue) values.get(i)).getValue();
				System.out.println("String value is: " + stringValue);
				try {
					System.out.println("Person String Value: " + stringValue);
					cardID = new Long(stringValue);
				} catch (NumberFormatException e) {
					System.out.println("Not an ID!");
					isID = false;
				}
				if (!isID) {
					System.out.println("Person Name!");
					personId = CardExchangeUtils.getPersonIdByName(stringValue);
					// getPersonId(stringValue);
				} else {
					System.out.println("Person ID!");
					personId = CardExchangeUtils.getObjectId(Person.class,
							((AttributeXMLValue) values.get(i)).getValue(),
							true);
				}
				if (personId != null)
					persons.add(DataObject.createFromId(personId));
				if (!attr.isMultiValued()) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		a.setValues(persons);
	}

}
