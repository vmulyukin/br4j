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
import java.util.Collections;
import java.util.List;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.Search.TextSearchConfigValue;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class ServiceCard extends CardHandler {

    private static final ObjectId TEMPLATE_ID = ObjectId.predefined(
	    Template.class, "jbr.state_service.dictionary");

    private static final ObjectId CODE_ATTRIBUTE_ID = ObjectId.predefined(
	    StringAttribute.class, "jbr.state_service.serviceCode");

    private final String code;

    public ServiceCard(String code) {
	super();
	this.code = code;
    }

    @Override
    protected List<ObjectId> search() throws CardException {
	DataServiceBean serviceBean = getServiceBean();

	Search search = new Search();

	search.setTemplates(Collections.singleton(DataObject
		.createFromId(TEMPLATE_ID)));
	search.setByAttributes(true);
	if (this.code == null || "".equals(this.code)) {
	    logger.error("Service code is not defined");
	    return Collections.emptyList();
	}
	search.addStringAttribute(CODE_ATTRIBUTE_ID, this.code,
		TextSearchConfigValue.EXACT_MATCH);
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
	    throw new CardException("jbr.medo.card.service.searchFailed", ex);
	} catch (ServiceException ex) {
	    throw new CardException("jbr.medo.card.service.searchFailed", ex);
	}
    }
}
