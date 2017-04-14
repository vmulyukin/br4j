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
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

/**
 * @author PPanichev
 *
 */
public class Imported extends ExportCardHandler {
    
    private final static ObjectId UID_ATTRIBUTE_ID = ObjectId.predefined(
	    StringAttribute.class, "jbr.importedDoc.UID");
    private final static ObjectId ID_ATTRIBUTE_ID = ObjectId.predefined(
	    StringAttribute.class, "jbr.importedDoc.ID");
    
    private String id = null;
    private StringAttribute notificationUID = null;
    private StringAttribute notificationID = null;
    
    public Imported(ObjectId card_id) throws DataException, ServiceException {
	serviceBean = getServiceBean();
	card = (Card) serviceBean.getById(card_id);
	if (card != null) {
	    id = card.getId().getId().toString();
	    notificationUID = (StringAttribute) card.getAttributeById(UID_ATTRIBUTE_ID);
	    notificationID = (StringAttribute) card.getAttributeById(ID_ATTRIBUTE_ID);
	}
    }

    /* (non-Javadoc)
     * @see com.aplana.medo.cards.ExportCardHandler#getCardId()
     */
    @Override
    public long getCardId() throws CardException {
	if (card != null ) return (Long)card.getId().getId();
	throw new CardException("jbr.medo.card.imported.notFound");
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
	throw new CardException("jbr.medo.card.imported.notFound");
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
    
    public StringAttribute getDocUID() {
	return this.notificationUID;
    }
    
    public StringAttribute getDocID() {
	return this.notificationID;
    }

}
