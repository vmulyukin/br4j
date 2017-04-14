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
import java.util.UUID;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * One of <code>CardHandler</code> that allows to create or update 'Income
 * document' card with given parameters.
 */
public class IncomeCardHandler extends CardHandler {

    private UUID uid;

    /**
     * Creates instance that allows to find cards by IDs
     *
     * @param id -
     *                ID of card in system
     * @see CardHandler#CardHandler(Long)
     */
    public IncomeCardHandler(Long id) {
	super(id);
    }

    public IncomeCardHandler(UUID uid) {
	super();
	this.uid = uid;

    }

    @Override
    protected String getParameterValuesLog() {
	StringBuilder logBuilder = new StringBuilder();
	logBuilder.append(String.format("uid='%s', ", uid));
	return logBuilder.toString();
    }

    @Override
    protected List<ObjectId> search() throws CardException {
	DataServiceBean serviceBean = getServiceBean();

	Search search = new Search();

	search.setTemplates(Collections.singleton(DataObject
		.createFromId(ImportedDocumentCardHandler.TEMPLATE_ID)));
	search.setByAttributes(true);
	if (uid != null) {
	    search
		    .addStringAttribute(ImportedDocumentCardHandler.UID_ATTRIBUTE_ID);
	    search.setWords(uid.toString());
	}
	search
		.setFetchLink(ImportedDocumentCardHandler.PARENT_DOC_ATTRIBUTE_ID);

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

    public UUID getUid() {
	return this.uid;
    }

    public void setUid(UUID uid) {
	this.uid = uid;
    }
}
