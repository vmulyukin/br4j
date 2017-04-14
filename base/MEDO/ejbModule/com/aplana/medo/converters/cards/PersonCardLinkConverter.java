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
package com.aplana.medo.converters.cards;

import java.util.Properties;

import com.aplana.medo.cards.CardException;
import com.aplana.medo.cards.OrganizationCardHandler;
import com.aplana.medo.cards.PersonCardHandler;
import com.aplana.medo.converters.ConverterException;

/**
 * <p>
 * PersonCardLinkConverter is one of 'cardLink' converters (see
 * {@link CardConverter}). It tries to find person according to non-empty
 * parameters. In case if ones were not found, new card is created. The
 * 'attribute' DOM element (see
 * {@link #convert(org.w3c.dom.Document, org.w3c.dom.Element)} that represents
 * cardLink to one, is returned.
 */
public class PersonCardLinkConverter extends CardConverter {

    public PersonCardLinkConverter(Properties properties, String name) {
	super(properties, name);
    }

    /**
     * Perform processing of read values - try to find person according to
     * non-empty values or create if not found.
     * 
     * @throws ConverterException
     * 
     * @see com.aplana.medo.converters.cards.CardConverter#processValues()
     * @see OrganizationCardHandler#getCardId()
     * @see PersonCardHandler#getPersonId()
     */
    @Override
    protected long processValues() throws ConverterException {
	String[] fio = getValueOfTagByKey("anyone.person").split(" ", 3);
	String[] fioParts = { "", "", "" };
	for (int i = 0; i < fio.length; i++) {
	    fioParts[i] = fio[i];
	}
	String lastname = fioParts[0];
	String firstname = fioParts[1];
	String middlename = fioParts[2];
	String organizationFullName = getValueOfTagByKey("anyone.organization");
	String position = getValueOfTagByKey("anyone.position");
	String department = getValueOfTagByKey("anyone.department");

	long organizationId = -1;
	if (!"".equals(organizationFullName)) {
	    try {
		organizationId = new OrganizationCardHandler(
			organizationFullName).getCardId();
	    } catch (CardException ex) {
		throw new ConverterException(
			"jbr.medo.converter.person.findOrganization",
			new Object[] { name }, ex);
	    }
	}

	long personId = -1;
	if (!"".equals(firstname) || !"".equals(middlename)
		|| !"".equals(lastname)) {
	    try {
		personId = new PersonCardHandler(firstname, middlename,
			lastname, organizationId, department, position)
			.getCardId();
	    } catch (CardException ex) {
		throw new ConverterException(
			"jbr.medo.converter.person.findPerson",
			new Object[] { name }, ex);
	    }
	}
	return personId;
    }
}