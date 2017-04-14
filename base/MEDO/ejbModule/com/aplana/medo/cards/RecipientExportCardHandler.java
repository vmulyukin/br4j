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

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

/**
 * One of <code>CardHandler</code> that allows to search 'Outcome' card.
 */
public class RecipientExportCardHandler extends ExportCardHandler {
    
    public static final ObjectId UUID = ObjectId.predefined(
	    TextAttribute.class, "jbr.organization.UUID");
    public static final ObjectId ADDRESS_MEDO = ObjectId.predefined(
	    TextAttribute.class, "jbr.organization.mail");
    public static final ObjectId NAME_DIC = ObjectId.predefined(
	    TextAttribute.class, "jbr.organization.dictionaryName");
    public static final ObjectId YES_MEDO = ObjectId.predefined(
	    ReferenceValue.class, "jbr.commission.control.yes"); // ref_code = ADMIN_27736. ref_value = 1449
    public static final ObjectId CLIENT_MEDO = ObjectId.predefined(
	    ListAttribute.class, "jbr.organization.medoClient"); // JBR_MEDO_CLIENT
    public static final ObjectId OVERHEAD_ORGANIZATION = ObjectId.predefined(
	    CardLinkAttribute.class, "jbr.organization.superOrganization");
    
    private String id = null;
    private TextAttribute uuid = null;
    private StringAttribute recipientFullNameAttribute = null;
    private TextAttribute addressMEDO = null; //JBR_ORG_EMAIL
    private TextAttribute nameDic = null;//JBR_ORG_NAME_BYDIC
    private Boolean clientMEDO = false;
    private ListAttribute clientMedo = null;
    private CardLinkAttribute  overheadOrganization = null;
    
    public RecipientExportCardHandler(ObjectId card_id) throws DataException,
	    ServiceException, NullPointerException {
	this.serviceBean = getServiceBean();
	card = (Card) serviceBean.getById(card_id);
	if (card != null) {
	    id = card.getId().getId().toString();
	    uuid = (TextAttribute) card
	    		.getAttributeById(UUID);
	    recipientFullNameAttribute = (StringAttribute) card
	    	.getAttributeById(OrganizationCardHandler.FULL_NAME_ATTRIBUTE_ID);
	    addressMEDO = (TextAttribute) card
	    	.getAttributeById(ADDRESS_MEDO);
	    nameDic = (TextAttribute) card
	    	.getAttributeById(NAME_DIC);
	    overheadOrganization = card
	    	.getCardLinkAttributeById(OVERHEAD_ORGANIZATION);
	    clientMedo = (ListAttribute) card
	    	.getAttributeById(CLIENT_MEDO);
	    ReferenceValue refVal = clientMedo.getValue();
	    final ReferenceValue yesMedo = (ReferenceValue) DataObject
		.createFromId(YES_MEDO);
	    if (refVal != null)
		clientMEDO = yesMedo.getId().getId().equals(refVal.getId().getId());
	} else
	    throw new CardException("jbr.medo.card.recipientexport.notFound");
	logger.info("Create object RecipientExport with current parameters: "
		+ getParameterValuesLog());
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
	throw new CardException("jbr.medo.card.recipientexport.notFound");
    }
    
    public String getRecipientFullNameAttribute() {
	if (this.recipientFullNameAttribute == null) return null;
	return this.recipientFullNameAttribute.getValue();
    }

    /**
     * Returns id of card found according to current state of class. If there
     * was found more than one card the first will be returned.
     * 
     * @return id of searched card
     * @throws CardException
     *                 if card was not found or other error
     */
    @Override
    public long getCardId() throws CardException {
	if (card != null ) return (Long)card.getId().getId();
	throw new CardException("jbr.medo.card.recipientexport.notFound");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aplana.medo.cards.CardHandler#getParameterValuesLog()
     */
    @Override
    protected String getParameterValuesLog() {
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
     * @return the mail
     */
    public String getAddressMEDO() {
	if (this.addressMEDO == null) return null;
        return this.addressMEDO.getValue();
    }

    /**
     * @return the nameDic
     */
    public String getNameDic() {
	if (this.nameDic == null) return null;
        return this.nameDic.getValue();
    }

    /**
     * @return the clientMEDO
     */
    public Boolean getClientMEDO() {
        return this.clientMEDO;
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
