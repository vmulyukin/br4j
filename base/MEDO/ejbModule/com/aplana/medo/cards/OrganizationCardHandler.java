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
package com.aplana.medo.cards;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aplana.dbmi.action.StrictSearch;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * One of <code>CardHandler</code> that allows to find or create organization
 * with given parameters.
 */
public class OrganizationCardHandler extends CardHandler {

    public static final ObjectId TEMPLATE_ID = ObjectId.predefined(
	    Template.class, "jbr.organization");

    public static final ObjectId FULL_NAME_ATTRIBUTE_ID = ObjectId.predefined(
	    TextAttribute.class, "jbr.organization.fullName");

    private String fullName;

    /**
     * Creates instance of class that allows to find organization by fullName.
     *
     * @param fullName
     * @throws CardException
     */
    public OrganizationCardHandler(String fullName) {
	this.fullName = fullName;
    }

    /**
     * <p>
     * Method is used in the {@link #getCardId()} and implements behavior of
     * card id calculating.
     * </p>
     * <p>
     * The following behavior is implemented:
     * <ol>
     * <li>Searches cards according to current state</li>
     * <li>If there were found more than one cards returns ID of first of them</li>
     * <li>If no cards were found creates new one with defined
     * <code>fullName</code>, returns its ID
     * </ol>
     * </p>
     *
     * @return ID of first of found cards or ID of created organization
     * @throws CardException
     */
    @Override
    protected long calculateCardId() throws CardException {
	if (fullName == null || "".equals(fullName)) {
	    logger.warn("fullName is empty");
	    throw new CardException("jbr.medo.card.organization.emptyArgument");
	}

	List<ObjectId> cards = findCards();

	if (cards.size() > 1) {
	    logger.warn("More than one organization was found. "
		    + "First of them will be used");
	}

	if (!cards.isEmpty()) {
	    return (Long) cards.get(0).getId();
	}

	// If organization was not found create it
	logger.info(String.format(
		"Organization with '%s' fullName was not found.", fullName));

	return createCard();
    }

    /**
     * Creates card that have given {@link #fullName} and returns id of that.
     *
     * @return id of created card
     * @throws CardException
     */
    private long createCard() throws CardException {
	logger.info("Trying to create card acording to current state: "
		+ getParameterValuesLog());

	Map<ObjectId, Object> attributeValues = new HashMap<ObjectId, Object>();
	attributeValues.put(FULL_NAME_ATTRIBUTE_ID, fullName);
	return createCard(TEMPLATE_ID, attributeValues,
		"jbr.medo.card.organization.creationFailed");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aplana.medo.cards.CardHandler#getParameterValuesLog()
     */
    @Override
    protected String getParameterValuesLog() {
	return super.getParameterValuesLog()
		+ String.format("fullName='%s'", fullName);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aplana.medo.cards.CardHandler#search()
     */
    @Override
    protected List<ObjectId> search() throws CardException {
	DataServiceBean serviceBean = getServiceBean();

	StrictSearch search = new StrictSearch();
	search.addStringAttribute(FULL_NAME_ATTRIBUTE_ID, fullName);
	search.setTemplates(Collections.singleton(DataObject
		.createFromId(TEMPLATE_ID)));
	try {
	    @SuppressWarnings("unchecked")
	    List<ObjectId> cards = (List<ObjectId>) serviceBean
		    .doAction(search);
	    return cards;
	} catch (DataException ex) {
	    throw new CardException("jbr.medo.card.organization.searchFailed",
		    ex);
	} catch (ServiceException ex) {
	    throw new CardException("jbr.medo.card.organization.searchFailed",
		    ex);
	}
    }

    public String getFullName() {
	return this.fullName;
    }

    public void setFullName(String fullName) {
	this.fullName = fullName;
    }
}
