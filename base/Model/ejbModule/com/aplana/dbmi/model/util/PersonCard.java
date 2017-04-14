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
package com.aplana.dbmi.model.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * Class is used to represent Internal Person Card
 * 
 * @see com.aplana.medo.cards.PersonInternal
 * @author aklyuev
 *
 */
public class PersonCard {

	protected Log logger = LogFactory.getLog(getClass());

	public static final String NAME_PART_DELIMETER = " ";
	public static final ObjectId LAST_NAME = ObjectId.predefined(StringAttribute.class, "jbr.person.lastName");
	public static final ObjectId FIRST_NAME = ObjectId.predefined(StringAttribute.class, "jbr.person.firstName");
	public static final ObjectId MIDDLE_NAME = ObjectId.predefined(StringAttribute.class, "jbr.person.middleName");

	public static final ObjectId POSITION = ObjectId.predefined(StringAttribute.class, "jbr.person.position");
	public static final ObjectId CONTACT_INFO = ObjectId.predefined(StringAttribute.class, "jbr.person.contactInfo");
	private static final ObjectId ORGANIZATION = ObjectId.predefined(CardLinkAttribute.class, "jbr.incoming.organization");
	private static final ObjectId DEPARTMENT = ObjectId.predefined(CardLinkAttribute.class, "jbr.personInternal.department");

	private String id = null;
	private StringAttribute lastName = null;
	private StringAttribute firstName = null;
	private StringAttribute middleName = null;

	private StringAttribute position = null;
	private StringAttribute contactInfo = null;
	private CardLinkAttribute organization = null;
	private CardLinkAttribute department = null;

	public PersonCard(ObjectId cardId, DataServiceBean serviceBean) throws DataException, ServiceException {

		if (cardId == null || serviceBean == null) {
			throw new IllegalArgumentException("cardId or serviceBean is null!");
		}

		Card card = (Card) serviceBean.getById(cardId);
		if (card != null) {
			id = card.getId().getId().toString();
			lastName = (StringAttribute) card.getAttributeById(LAST_NAME);
			firstName = (StringAttribute) card.getAttributeById(FIRST_NAME);
			middleName = (StringAttribute) card.getAttributeById(MIDDLE_NAME);

			position = (StringAttribute) card.getAttributeById(POSITION);
			contactInfo = (StringAttribute) card.getAttributeById(CONTACT_INFO);
			organization = (CardLinkAttribute)card.getAttributeById(ORGANIZATION);
			department = (CardLinkAttribute)card.getAttributeById(DEPARTMENT);
		} else {
			throw new DataException("Cannot instantiate PersonCard");
		}
	}

	public String getId() {
		return id;
	}
	public String getLastName() {
		if (lastName != null && lastName.getValue() != null) {
			return lastName.getValue();
		}
		return "";
	}
	public String getFirstName() {
		if (firstName != null && firstName.getValue() != null) {
			return firstName.getValue();
		}
		return "";
	}
	public String getMiddleName() {
		if (middleName != null && middleName.getValue() != null) {
			return middleName.getValue();
		}
		return "";
	}

	public String getFio() {
		return getLastName() + NAME_PART_DELIMETER + getFirstName() + NAME_PART_DELIMETER + getMiddleName();
	}

	public String getPosition() {
		if (this.position == null) return null;
		return this.position.getValue();
	}

	public String getContactInfo() {
		if (this.contactInfo == null) return null;
		return this.contactInfo.getValue();
	}

	public CardLinkAttribute getDepartment() {
		return this.department;
	}

	public ObjectId getDepartmentId() {
		return calcDepartment();
	}

	public ObjectId getOrganizationId() {
		return calcOrganization();
	}

	private ObjectId calcOrganization() {
		ObjectId[] Organizations = this.organization.getIdsArray();
		if (Organizations == null) {
			logger.warn("Organizations is null");
			return null;
		}
		return  Organizations[0];
	}

	private ObjectId calcDepartment() {
		ObjectId[] Departments = this.department.getIdsArray();
		if (Departments == null) {
			logger.warn("Departments is null");
			return null;
		}
		return  Departments[0];
	}
}
