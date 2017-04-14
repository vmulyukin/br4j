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
package com.aplana.ireferent.card;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.DownloadFile;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.ireferent.IReferentException;

/**
 * @author PPanichev
 *
 */
public class PersonCard {

    public static final ObjectId TEMPLATE_PERSON_ID = ObjectId.predefined(
		Template.class, "jbr.internalPerson");
    
    public static final ObjectId NAME = ObjectId.predefined(
	    StringAttribute.class, "name");
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
    public static final ObjectId EMAIL = ObjectId.predefined(
	    StringAttribute.class, "jbr.person.email");
    
    public static final ObjectId ORGANIZATION = ObjectId
    .predefined(CardLinkAttribute.class, "jbr.incoming.organization");
    public static final ObjectId DEPARTMENT = ObjectId
    .predefined(CardLinkAttribute.class, "jbr.personInternal.department");
    
    private String id = null;
    private ObjectId card_id = null;
    private StringAttribute Name = null;
    private StringAttribute lastName = null;
    private StringAttribute firstName = null;
    private StringAttribute middleName = null;
    private StringAttribute position = null;
    private StringAttribute contactInfo = null;
    private StringAttribute email = null;
    private CardLinkAttribute department = null;
    private Principal user = null;
    private String templateId = null;
    private DownloadFile downloadFile = new DownloadFile();
    private Log logger = LogFactory.getLog(getClass());
    private Card card = null;
    private DataServiceBean serviceBean = null;
    
    public PersonCard(String idPerson, DataServiceBean serviceBeanPC) throws DataException,
    ServiceException, IReferentException {
	this.serviceBean = serviceBeanPC;
	card_id = new ObjectId(Card.class, Long.parseLong(idPerson));
	card = (Card) serviceBeanPC.getById(card_id);
	if (card != null) {
	    id = card.getId().getId().toString();
	    Name = (StringAttribute) card.getAttributeById(NAME);
	    lastName = (StringAttribute) card.getAttributeById(LAST_NAME);
	    firstName = (StringAttribute) card.getAttributeById(FIRST_NAME);
	    middleName = (StringAttribute) card.getAttributeById(MIDDLE_NAME);
	    position = (StringAttribute) card.getAttributeById(POSITION);
	    contactInfo = (StringAttribute) card.getAttributeById(CONTACT_INFO);
	    email = (StringAttribute) card.getAttributeById(EMAIL);
	    department = (CardLinkAttribute)card.getAttributeById(DEPARTMENT);
	    templateId = card.getTemplate().getId().toString();
	    downloadFile.setCardId(card_id);
	} else
	    throw new IReferentException("com.aplana.ireferent.card.PersonCard.notFound");
	logger.info("Create object PersonCard with current parameters: "
		+ getParameterValuesLog());
    }
    
    public PersonCard(Card card, DataServiceBean serviceBeanPC) throws IReferentException {
	this.serviceBean = serviceBeanPC;
	if (card != null) {
	    this.card = card;
	    ObjectId cardId = card.getId();
	    id = cardId.getId().toString();
	    Name = (StringAttribute) card.getAttributeById(NAME);
	    lastName = (StringAttribute) card.getAttributeById(LAST_NAME);
	    firstName = (StringAttribute) card.getAttributeById(FIRST_NAME);
	    middleName = (StringAttribute) card.getAttributeById(MIDDLE_NAME);
	    position = (StringAttribute) card.getAttributeById(POSITION);
	    contactInfo = (StringAttribute) card.getAttributeById(CONTACT_INFO);
	    email = (StringAttribute) card.getAttributeById(EMAIL);
	    department = (CardLinkAttribute)card.getAttributeById(DEPARTMENT);
	    templateId = card.getTemplate().getId().toString();
	    downloadFile.setCardId(cardId);
	} else
	    throw new IReferentException("com.aplana.ireferent.card.PersonCard.notExist");
	logger.info("Create object PersonCard with current parameters: "
		+ getParameterValuesLog());
    }
    
    public static List<SearchResult.Column> getColumnsPersonCard() {
	
	final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
	
	// 1� �������: NAME
	final SearchResult.Column name = new SearchResult.Column();
	columns.add(name);
	name.setAttributeId(NAME);
	
	//2� �������: LAST_NAME
	final SearchResult.Column lastName = new SearchResult.Column();
	columns.add(lastName);
	lastName.setAttributeId(LAST_NAME);
	
	//3� �������: FIRST_NAME
	final SearchResult.Column firstName = new SearchResult.Column();
	columns.add(firstName);
	firstName.setAttributeId(FIRST_NAME);
	
	//4� �������: MIDDLE_NAME
	final SearchResult.Column middleName = new SearchResult.Column();
	columns.add(middleName);
	middleName.setAttributeId(MIDDLE_NAME);
	
	//5� �������: POSITION
	final SearchResult.Column position = new SearchResult.Column();
	columns.add(position);
	position.setAttributeId(POSITION);
	
	//6� �������: CONTACT_INFO
	final SearchResult.Column contactInfo = new SearchResult.Column();
	columns.add(contactInfo);
	contactInfo.setAttributeId(CONTACT_INFO);
	
	//7� �������: EMAIL
	final SearchResult.Column email = new SearchResult.Column();
	columns.add(email);
	email.setAttributeId(EMAIL);
	
	//8� �������: DEPARTMENT
	final SearchResult.Column department = new SearchResult.Column();
	columns.add(department);
	department.setAttributeId(DEPARTMENT);
	
	//9� �������: ATTR_TEMPLATE
	final SearchResult.Column template = new SearchResult.Column();
	columns.add(template);
	template.setAttributeId(Card.ATTR_TEMPLATE);
	return columns;
    }

    public long getCardId() throws IReferentException {
	if (card != null ) return (Long)card.getId().getId();
		throw new IReferentException("com.aplana.ireferent.card.PersonCard.notFound");
    }
    
    /**
     * @return the id
     */
    public String getId() {
	return id;
    }
    
    public Card getCard() throws IReferentException {
	if (card != null)
	return card;
	throw new IReferentException("com.aplana.ireferent.card.PersonCard.notFound");
    }

    private String getParameterValuesLog() {
	StringBuilder logBuilder = new StringBuilder();
	logBuilder.append(String.format("id='%s', ", id));
	return logBuilder.toString();
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
	if (this.lastName == null) return "";
	return this.lastName.getValue();
    }
    
    /**
     * @return the Name
     */
    public String getName() {
	if (this.Name == null) return "";
	return this.Name.getValue();
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
	if (this.firstName == null) return "";
	return this.firstName.getValue();
    }

    /**
     * @return the middleName
     */
    public String getMiddleName() {
	if (this.middleName == null) return "";
	return this.middleName.getValue();
    }

    /**
     * @return the position
     */
    public String getPosition() {
	if (this.position == null) return "";
	return this.position.getValue();
    }

    /**
     * @return the contactInfo
     */
    public String getContactInfo() {
	if (this.contactInfo == null) return "";
	return this.contactInfo.getValue();
    }
    
    /**
     * @return the e-mail
     */
    public String getEMail() {
	if (this.email == null) return "";
	return this.email.getValue();
    }
    
    /**
     * @return the user
     */
    public Principal getUser() {
        return this.user;
    }
    
    /**
     * @return the templateId
     */
    public String getTemplateId() {
	return this.templateId;
    }

    /**
     * @return the downloadFile
     */
    public DownloadFile getDownloadFile() {
        return this.downloadFile;
    }

    /**
     * @return the department
     */
    public CardLinkAttribute getDepartments() {
        return this.department;
    }
    
    public ObjectId getDepartmentId() {
        return calcDepartment();
    }
    
    private ObjectId calcDepartment() {
	if (this.department == null) return null;
	ObjectId[] Departments = this.department.getIdsArray();
	if (Departments == null) {
	    logger
	    	.warn("com.aplana.ireferent.card.PersonCard.Departments.isNull");
	    return null;
	}
	return  Departments[0];
    }

    /**
     * @return the serviceBean
     */
    public DataServiceBean getServiceBean() {
        return this.serviceBean;
    }
}
