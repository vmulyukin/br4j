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
package com.aplana.medo.cards;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

/**
 * @author PPanichev
 *
 */
//TODO use PersonInternal to wrap com.aplana.dbmi.model.util.PersonCard class
public class PersonInternal extends ExportCardHandler {
    
    public static final ObjectId TEMPLATE_ID = ObjectId.predefined(
	    Template.class, "");
    
    public static final ObjectId LAST_NAME = ObjectId.predefined(
	    StringAttribute.class, "jbr.person.lastName");
    public static final ObjectId FIRST_NAME = ObjectId.predefined(
	    StringAttribute.class, "jbr.person.firstName");
    public static final ObjectId MIDDLE_NAME = ObjectId.predefined(
	    StringAttribute.class, "jbr.person.middleName");
    public static final ObjectId POSITION = ObjectId.predefined(
	    StringAttribute.class, "jbr.person.position");
    public static final ObjectId CONTACT_INFO = ObjectId.predefined(
	    StringAttribute.class, "jbr.person.contactInfo");
    
    
    private static final ObjectId ORGANIZATION = ObjectId
    .predefined(CardLinkAttribute.class, "jbr.incoming.organization");
    private static final ObjectId DEPARTMENT = ObjectId
    .predefined(CardLinkAttribute.class, "jbr.personInternal.department");
    
    private String id = null;
    private StringAttribute lastName = null;
    private StringAttribute firstName = null;
    private StringAttribute middleName = null;
    private StringAttribute position = null;
    private StringAttribute contactInfo = null;
    
    private CardLinkAttribute organization = null;
    private CardLinkAttribute department = null;
    
    public PersonInternal(ObjectId card_id) throws DataException,
    ServiceException {
	serviceBean = getServiceBean();
	card = (Card) serviceBean.getById(card_id);
	if (card != null) {
	    id = card.getId().getId().toString();
	    lastName = (StringAttribute) card.getAttributeById(LAST_NAME);
	    firstName = (StringAttribute) card.getAttributeById(FIRST_NAME);
	    middleName = (StringAttribute) card.getAttributeById(MIDDLE_NAME);
	    position = (StringAttribute) card.getAttributeById(POSITION);
	    contactInfo = (StringAttribute) card.getAttributeById(CONTACT_INFO);
	    organization = (CardLinkAttribute)card.getAttributeById(ORGANIZATION);
	    department = (CardLinkAttribute)card.getAttributeById(DEPARTMENT);
	} else
	    throw new CardException("jbr.medo.card.PersonInternal.notFound");
	logger.info("Create object PersonInternal with current parameters: "
		+ getParameterValuesLog());
    }

    /* (non-Javadoc)
     * @see com.aplana.medo.cards.ExportCardHandler#getCardId()
     */
    @Override
    public long getCardId() throws CardException {
	if (card != null ) return (Long)card.getId().getId();
		throw new CardException("jbr.medo.card.PersonInternal.notFound");
    }
    
    /**
     * @return the id
     */
    public String getId() {
	return id;
    }
    
    public Card getCard() throws CardException {
	if (card != null)
	return card;
	throw new CardException("jbr.medo.card.PersonInternal.notFound");
    }

    /* (non-Javadoc)
     * @see com.aplana.medo.cards.ExportCardHandler#getParameterValuesLog()
     */
    @Override
    protected String getParameterValuesLog() {
	StringBuilder logBuilder = new StringBuilder();
	logBuilder.append(String.format("id='%s', ", id));
	return logBuilder.toString();
    }

    /**
     * @return the organization
     */
    public CardLinkAttribute getOrganization() {
        return this.organization;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
	if (this.lastName == null) return null;
	return this.lastName.getValue();
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
	if (this.firstName == null) return null;
	return this.firstName.getValue();
    }

    /**
     * @return the middleName
     */
    public String getMiddleName() {
	if (this.middleName == null) return null;
	return this.middleName.getValue();
    }

    /**
     * @return the position
     */
    public String getPosition() {
	if (this.position == null) return null;
	return this.position.getValue();
    }

    /**
     * @return the contactInfo
     */
    public String getContactInfo() {
	if (this.contactInfo == null) return null;
	return this.contactInfo.getValue();
    }

    /**
     * @return the department
     */
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
	    logger
	    	.warn("jbr.medo.PersonInternal.Organizations.isNull");
	    return null;
	}
	return  Organizations[0];
    }
    
    private ObjectId calcDepartment() {
	ObjectId[] Departments = this.department.getIdsArray();
	if (Departments == null) {
	    logger
	    	.warn("jbr.medo.PersonInternal.Departments.isNull");
	    return null;
	}
	return  Departments[0];
    }

}
