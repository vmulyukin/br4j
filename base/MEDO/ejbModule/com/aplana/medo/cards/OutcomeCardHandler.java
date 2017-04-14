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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * One of <code>CardHandler</code> that allows to search 'Outcome' card.
 */
public class OutcomeCardHandler extends CardHandler {

    private static final ObjectId TEMPLATE_ID = ObjectId.predefined(
	    Template.class, "jbr.outcoming");

    private static final ObjectId REG_NUM_ATTRIBUTE_ID = ObjectId.predefined(
	    StringAttribute.class, "jbr.maindoc.regnum");
    private static final ObjectId REG_DATE_ATTRIBUTE_ID = ObjectId.predefined(
	    DateAttribute.class, "jbr.maindoc.regdate");

    protected static final ObjectId DISTRIBUTION_LIST_ATTRIBUTE_ID = ObjectId
	    .predefined(CardLinkAttribute.class,
		    "jbr.Distribution.DistributionList");

    private String registrationNumber;
    private Date registrationDate;

    public OutcomeCardHandler(Long id){
	super(id);
    }

    /**
     * Creates instance of card allows to find cards with given registration
     * number and given registration date. Cards will be found from given date
     * to end of day.
     *
     * @param registrationNumber -
     *                registration number of finding card
     * @param registrationDate -
     *                registration date of finding card
     */
    public OutcomeCardHandler(String registrationNumber, Date registrationDate) {
	super();
	this.registrationNumber = registrationNumber;
	this.registrationDate = registrationDate;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aplana.medo.cards.CardHandler#getParameterValuesLog()
     */
    @Override
    protected String getParameterValuesLog() {
	StringBuilder logBuilder = new StringBuilder();
	logBuilder.append(String.format("registrationNumber='%s', ",
		registrationNumber));
	logBuilder.append(String.format("registrationDate='%s', ",
		registrationDate));
	return logBuilder.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aplana.medo.cards.CardHandler#search()
     */
    @Override
    protected List<ObjectId> search() throws CardException {
	DataServiceBean serviceBean = getServiceBean();

	Search search = new Search();

	search.setTemplates(Collections.singleton(DataObject
		.createFromId(TEMPLATE_ID)));
	search.setByAttributes(true);
	if (registrationNumber != null) {
	    search.addStringAttribute(REG_NUM_ATTRIBUTE_ID);
	    search.setWords(registrationNumber);
	}

	if (registrationDate != null) {
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(registrationDate);
	    calendar.set(Calendar.HOUR_OF_DAY, 23);
	    calendar.set(Calendar.MINUTE, 59);
	    calendar.set(Calendar.SECOND, 59);
	    calendar.set(Calendar.MILLISECOND, 999);

	    search.addDateAttribute(REG_DATE_ATTRIBUTE_ID, registrationDate,
		    calendar.getTime());
	}

	try {
	    SearchResult result = (SearchResult) serviceBean.doAction(search);
	    @SuppressWarnings("unchecked")
	    List<Card> cards = result.getCards();
	    List<ObjectId> cardIds = new ArrayList<ObjectId>();
	    for (Card card : cards) {
		cardIds.add(card.getId());
	    }
	    return cardIds;
	} catch (DataException ex) {
	    throw new CardException("jbr.medo.card.outcome.searchFailed", ex);
	} catch (ServiceException ex) {
	    throw new CardException("jbr.medo.card.outcome.searchFailed", ex);
	}
    }

    /**
     * @return the registrationNumber
     */
    public String getRegistrationNumber() {
	return this.registrationNumber;
    }

    /**
     * @param registrationNumber
     *                the registrationNumber to set
     */
    public void setRegistrationNumber(String registrationNumber) {
	this.registrationNumber = registrationNumber;
    }

    /**
     * @return the registrationDate
     */
    public Date getRegistrationDate() {
	return this.registrationDate;
    }

    /**
     * @param registrationDate
     *                the registrationDate to set
     */
    public void setRegistrationDate(Date registrationDate) {
	this.registrationDate = registrationDate;
    }

}
