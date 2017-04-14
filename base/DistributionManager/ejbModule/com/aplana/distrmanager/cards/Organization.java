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
package com.aplana.distrmanager.cards;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;

public class Organization {

	protected Log logger = LogFactory.getLog(getClass());

    public static final ObjectId UUID = ObjectId.predefined(
	    StringAttribute.class, "jbr.organization.uniqueId");
    public static final ObjectId FULL_NAME_ATTRIBUTE_ID = ObjectId.predefined(
    	    TextAttribute.class, "jbr.organization.fullName");
    public static final ObjectId OVERHEAD_ORGANIZATION = ObjectId.predefined(
	    CardLinkAttribute.class, "jbr.organization.superOrganization");

    private String id = null;
    private StringAttribute uuid = null;
    private Card card = null;
    private StringAttribute fullName = null;
    private CardLinkAttribute  overheadOrganization = null;
    private DataServiceFacade serviceBean = null;

    public Organization(DataServiceFacade serviceBean) {
    	this.serviceBean = serviceBean;
    }

    @SuppressWarnings("unused")
	private Organization() {
    }

    public void init(ObjectId card_id) throws Exception {
    	card = (Card) serviceBean.getById(card_id);
    	if (card != null) {
    	    id = card.getId().getId().toString();
    	    uuid = (StringAttribute) card.getAttributeById(UUID);
    	    fullName = (StringAttribute) card
    	    	.getAttributeById(FULL_NAME_ATTRIBUTE_ID);
    	    overheadOrganization = card
    	    	.getCardLinkAttributeById(OVERHEAD_ORGANIZATION);
    	} else
    	    throw new DataException("jbr.medo.cards.organization.notFound");
    	logger.info("Create object RecipientExport with current parameters: "
    		+ getParameterValuesLog());
    }

    /**
     * @return the id
     */
    public String getId() {
	return id;
    }

    public Card getCard() throws DataException {
	if (card != null)
	return card;
	throw new DataException("jbr.medo.cards.organization.notFound");
    }

    public String getFullName() {
	if (this.fullName == null) return null;
	return this.fullName.getValue();
    }

    /**
     * Returns id of card found according to current state of class. If there
     * was found more than one card the first will be returned.
     *
     * @return id of searched card
     *                 if card was not found or other error
     */
    public long getCardId() throws DataException {
	if (card != null ) return (Long)card.getId().getId();
	throw new DataException("jbr.DistributionManager.cards.organization.notFound");
    }

    private String getParameterValuesLog() {
	StringBuilder logBuilder = new StringBuilder();
	logBuilder.append(String.format("id='%s', ", id));
	return logBuilder.toString();
    }

    /**
     * @return the UUID
     */
    public String getUUID() {
	if (this.uuid == null) return null;
        return this.uuid.getValue();
    }


    /**
     * @return Overhead Organization
     */
    public ObjectId getOverheadOrganization() {
	CardLinkAttribute oo = overheadOrganization;
	ObjectId[] oos = null;
	if (oo != null)	{
	    oos = this.overheadOrganization.getIdsArray();
	    if (oos != null) return oos[0];
	}
	return null;
    }

}
