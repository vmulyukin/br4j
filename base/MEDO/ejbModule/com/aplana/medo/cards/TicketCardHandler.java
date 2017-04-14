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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;

/**
 * Class provides methods that allows to create ticket card according to given
 * parameters.
 */
public class TicketCardHandler extends CardHandler {
    private static final ObjectId TEMPLATE_ID = ObjectId.predefined(
	    Template.class, "ticket");
    private static final ObjectId TICKET_DELIV_DATE_ATTRIBUTE_ID = ObjectId
	    .predefined(DateAttribute.class, "ticket.deliveryDate");
    private static final ObjectId TICKET_DOC_UID_ATTRIBUTE_ID = ObjectId
	    .predefined(StringAttribute.class, "ticket.documentUID");
    public static final ObjectId TICKET_DOC_BASE_ATTRIBUTE_ID = ObjectId
	    .predefined(CardLinkAttribute.class, "ticket.docBase");

    private final UUID uid;

    private final Date date;

    public TicketCardHandler(UUID uid, Date date) {
	this.uid = uid;
	this.date = date;
    }

    public TicketCardHandler(long id) {
	super(id);
	uid = null;
	date = null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aplana.medo.cards.CardHandler#search()
     */
    @Override
    protected List<ObjectId> search() throws CardException {
	return new ArrayList<ObjectId>();
    }

    /**
     * Creates new ticket card in the system according to current state of the
     * class.
     *
     * @return ID of created card
     * @throws CardException
     */
    public long createCard() throws CardException {
	Map<ObjectId, Object> attributeValues = new HashMap<ObjectId, Object>();
	attributeValues.put(TICKET_DOC_UID_ATTRIBUTE_ID, uid.toString());
	attributeValues.put(TICKET_DELIV_DATE_ATTRIBUTE_ID, date);
	return super.createCard(TEMPLATE_ID, attributeValues,
		"jbr.medo.card.ticket.creationFailed");
    }

    /**
     * Saves current card without attributes modification. Can be used to force
     * card saving processors.
     *
     * @throws CardException
     */
    public void saveCard() throws CardException {
	updateAttributes(new HashMap<ObjectId, Object>(),
		"jbr.medo.card.ticket.saveFailed");
    }
}
