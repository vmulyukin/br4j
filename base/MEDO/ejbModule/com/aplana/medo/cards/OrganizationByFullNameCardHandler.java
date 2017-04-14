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
import java.util.Map;
import java.util.UUID;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.SearchResult.Column;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * One of <code>CardHandler</code> that allows to find organization -
 * IEDMS-client by dictionary name.
 */
public class OrganizationByFullNameCardHandler extends OrganizationCardHandler {
    public static final ObjectId MEDO_CLIENT_ATTRIBUTE_ID = ObjectId
	    .predefined(ListAttribute.class, "jbr.organization.medoClient");
    public static final ObjectId ORGANIZATION_UUID_ATTRIBUTE_ID = ObjectId
	    .predefined(TextAttribute.class, "jbr.organization.UUID");
    
    public static final ObjectId ORGANIZATION_FULL_NAME_ATTRIBUTE_ID = ObjectId.predefined(TextAttribute.class, "jbr.organization.fullName");
    public static final ObjectId SUPER_ORGANIZATION_ATTRIBUTE_ID = ObjectId
	    .predefined(CardLinkAttribute.class,
		    "jbr.organization.superOrganization");

    public static final ObjectId YES_VALUE_ID = ObjectId.predefined(
	    ReferenceValue.class, "jbr.commission.control.yes");

    public OrganizationByFullNameCardHandler(String fullName) {
    	super(fullName);
    }

    /**
     * Tries to find client of IEDMS - organization card - by UUID. In case if
     * required organization card was not found, it tries to find by dictionary
     * name. Organizations that has 'MEDO client' attribute equals to 'Yes' are
     * been searching only.
     *
     * @return id of found or -1 if not found
     * @throws CardException
     */
    @Override
    protected long calculateCardId() throws CardException {
	List<ObjectId> cards = findCards();

	if (cards.size() > 1) {
	    logger.warn("More than one organization was found. "
		    + "First of them will be used");
	}

	if (!cards.isEmpty()) {
	    return (Long) cards.get(0).getId();
	}
	return -1;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aplana.medo.cards.CardHandler#search()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected List<ObjectId> search() throws CardException {

	List<ObjectId> cardIds = new ArrayList<ObjectId>();

	    // If name is defined and cards by name were found then
	    // returns.
	    if (getFullName() != null && !"".equals(getFullName())) {
			Search search = getFetchAction();
			search.addStringAttribute(ORGANIZATION_FULL_NAME_ATTRIBUTE_ID, getFullName(), Search.TextSearchConfigValue.EXACT_MATCH);
			
			cardIds = new ArrayList<ObjectId>(ObjectIdUtils.collectionToSetOfIds(doSearch(search)));
			if (!cardIds.isEmpty()) {
			    return cardIds;
			}
	    }

	return cardIds;
    }

    private SearchResult.Column createColumn(ObjectId id) {
	final SearchResult.Column col = new SearchResult.Column();
	col.setAttributeId(id);
	return col;
    }

    private Search getFetchAction() {
	Search search = new Search();
	search.setTemplates(Collections.singleton(DataObject
		.createFromId(TEMPLATE_ID)));
	search.setByAttributes(true);
	search.addListAttribute(MEDO_CLIENT_ATTRIBUTE_ID, Collections
		.singleton(DataObject.createFromId(YES_VALUE_ID)));

	List<Column> columns = new ArrayList<Column>();
	columns.add(createColumn(ORGANIZATION_UUID_ATTRIBUTE_ID));
	columns.add(createColumn(SUPER_ORGANIZATION_ATTRIBUTE_ID));
	search.setColumns(columns);
	return search;
    }

    @SuppressWarnings("unchecked")
    private List<Card> doSearch(Search search) throws CardException {
	try {
	    DataServiceBean serviceBean = getServiceBean();
	    return ((SearchResult) serviceBean.doAction(search)).getCards();
	} catch (DataException ex) {
	    throw new CardException("jbr.medo.card.organization.searchFailed",
		    ex);
	} catch (ServiceException ex) {
	    throw new CardException("jbr.medo.card.organization.searchFailed",
		    ex);
	}
    }

}