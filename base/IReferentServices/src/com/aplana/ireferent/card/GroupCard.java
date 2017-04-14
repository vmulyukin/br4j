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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.ireferent.IReferentException;

/**
 * @author PPanichev
 *
 */
public class GroupCard { // �����������
    
    /*public static final ObjectId TEMPLATE_ID = ObjectId.predefined(
	    Template.class, "");*/
    public static final ObjectId FULL_NAME = ObjectId.predefined(
	    TextAttribute.class, "jbr.department.fullName");
    public static final ObjectId CHIEF = ObjectId
	    .predefined(CardLinkAttribute.class, "jbr.department.chief");
    public static final transient ObjectId OVERHEAD_ORGANIZATION = ObjectId.predefined(
	    CardLinkAttribute.class, "jbr.department.parentDepartment");
    
    private Log logger = LogFactory.getLog(getClass());
    private Card card = null;
    private String id = null;
    private TextAttribute fullName = null;
    private DataServiceBean serviceBean = null;
    private CardLinkAttribute chief = null;
    private CardLinkAttribute department = null;
    
    public GroupCard(String id, DataServiceBean serviceBeanGC) throws DataException,
    ServiceException, IReferentException {
	this.serviceBean = serviceBeanGC;
	this.id = id;
	ObjectId card_id = new ObjectId(Card.class, Long.parseLong(id));
	card = (Card) serviceBeanGC.getById(card_id);
	if (card != null) {
	    fullName = (TextAttribute)card.getAttributeById(FULL_NAME);
	    chief = (CardLinkAttribute)card.getAttributeById(CHIEF);
	    department = (CardLinkAttribute)card.getAttributeById(OVERHEAD_ORGANIZATION);
	}  else
	    throw new IReferentException("jbr.medo.card.GroupCard.notFound");
	logger.info("Create object GroupCard with current parameters: "
		+ getParameterValuesLog());
    }
    
    public GroupCard(Card card, DataServiceBean serviceBeanGC) throws IReferentException {
	this.serviceBean = serviceBeanGC;
	if (card != null) {
	    this.card = card;
	    id = card.getId().getId().toString();
	    fullName = (TextAttribute)card.getAttributeById(FULL_NAME);
	    chief = (CardLinkAttribute)card.getAttributeById(CHIEF);
	    department = (CardLinkAttribute)card.getAttributeById(OVERHEAD_ORGANIZATION);
	}  else
	    throw new IReferentException("jbr.medo.card.GroupCard.notExist");
	logger.info("Create object GroupCard with current parameters: "
		+ getParameterValuesLog());
    }
    
    public static List<SearchResult.Column> getColumnsGroupCard() {
	final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
	// 1� �������: FULL_NAME
	final SearchResult.Column fullName = new SearchResult.Column();
	columns.add(fullName);
	fullName.setAttributeId(GroupCard.FULL_NAME);
	
	//2� �������: CHIEF
	final SearchResult.Column chief = new SearchResult.Column();
	columns.add(chief);
	chief.setAttributeId(GroupCard.CHIEF);
	return columns;
    }

    public long getCardId() throws IReferentException {
	if (card != null ) return (Long)card.getId().getId();
		throw new IReferentException("jbr.medo.card.GroupCard.notFound");
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
	throw new IReferentException("jbr.medo.card.GroupCard.notFound");
    }

    private String getParameterValuesLog() {
	StringBuilder logBuilder = new StringBuilder();
	logBuilder.append(String.format("id='%s', ", id));
	return logBuilder.toString();
    }
    
    /**
     * @return the fullName
     */
    public String getFullName() {
	if (this.fullName == null || this.fullName.getValue() == null) return "";
	return this.fullName.getValue();
    }

    /**
     * @return the serviceBean
     */
    public DataServiceBean getServiceBean() {
        return this.serviceBean;
    }
    
    /**
     * @return the chief
     */
    public CardLinkAttribute getChief() {
        return this.chief;
    }
    
    public ObjectId getChiefId() {
        return calcChief();
    }
    
    private ObjectId calcChief() {
	if (this.chief == null) return null;
	ObjectId[] Chief = this.chief.getIdsArray();
	if (Chief == null) {
	    logger
	    	.warn("com.aplana.ireferent.card.PersonCard.Departments.isNull");
	    return null;
	}
	return  Chief[0];
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
}
