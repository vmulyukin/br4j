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
package com.aplana.dbmi.numerator.query;

import java.util.Collection;
import java.util.Iterator;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataException;

public class NumeratorPartIFAddressee extends NumeratorPart  {

	final String ATTR_ADDRESSEE = "jbr.incoming.addressee";
	final String ATTR_PERS_NAME = "jbr.person.firstName";
	final String ATTR_PERS_SNAME = "jbr.person.lastName";
	final ObjectId ADDRESSEE = ObjectId.predefined(PersonAttribute.class, ATTR_ADDRESSEE);
	final ObjectId FNAME = ObjectId.predefined(StringAttribute.class, ATTR_PERS_NAME);
	final ObjectId SNAME = ObjectId.predefined(StringAttribute.class, ATTR_PERS_SNAME);
	
	public NumeratorPartIFAddressee(DoSetRegistrationNumber cardnum) {
		super(cardnum);
		// TODO Auto-generated constructor stub
	}

	public String getValue() throws DataException{
		String result = "";
		PersonAttribute attr = (PersonAttribute)cardNumerationObj.getContextCard().getAttributeById(ADDRESSEE);
		Collection attrValues = attr.getValues(); 
		if (attrValues.isEmpty()){
			throw new DataException
			(
					"numerator.attributeNotSet", 
					new Object[] {cardNumerationObj.getAttributeNameById(ADDRESSEE)}
			);				
		}
		Person[] persons = (Person[])attrValues.toArray(new Person[attrValues.size()]);
		Person pers = persons[0];
		Card card = cardNumerationObj.getCard(pers.getCardId());
		String firstName = ((StringAttribute)card.getAttributeById(ObjectId.predefined(StringAttribute.class, this.ATTR_PERS_NAME))).getStringValue();
		String lastName = ((StringAttribute)card.getAttributeById(ObjectId.predefined(StringAttribute.class, this.ATTR_PERS_SNAME))).getStringValue();
		if(firstName.length() >= 1) firstName = firstName.substring(0, 1);
		if(lastName.length() >= 1) lastName = lastName.substring(0, 1);
		result = firstName + lastName;
		return result;
	}
		
}
