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
 * IEDMS-client by UUID or dictionary name.
 */
public class IedmsClientOrganizationCardHandler extends OrganizationCardHandler {
    public static final ObjectId MEDO_CLIENT_ATTRIBUTE_ID = ObjectId
	    .predefined(ListAttribute.class, "jbr.organization.medoClient");
    public static final ObjectId ORGANIZATION_UUID_ATTRIBUTE_ID = ObjectId
	    .predefined(TextAttribute.class, "jbr.organization.UUID");
    public static final ObjectId ORGANIZATION_NAME_ATTRIBUTE_ID = ObjectId
	    .predefined(TextAttribute.class, "jbr.organization.dictionaryName");
    public static final ObjectId SUPER_ORGANIZATION_ATTRIBUTE_ID = ObjectId
	    .predefined(CardLinkAttribute.class,
		    "jbr.organization.superOrganization");

    public static final ObjectId YES_VALUE_ID = ObjectId.predefined(
	    ReferenceValue.class, "jbr.commission.control.yes");

    private UUID uid = null;

    public IedmsClientOrganizationCardHandler(String fullName) {
	super(fullName);
    }

    public IedmsClientOrganizationCardHandler(UUID uid) {
	super(null);
	this.uid = uid;
    }

    public IedmsClientOrganizationCardHandler(UUID uid, String fullName) {
	super(fullName);
	this.uid = uid;
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

	if (uid != null && !"".equals(uid)) {
	    // If name is defined and cards by UUID and name were found then
	    // returns. It is supposed that pair (UUID,name) is unique
	    if (getFullName() != null && !"".equals(getFullName())) {
		Search search = getFetchAction();
		search.addStringAttribute(ORGANIZATION_UUID_ATTRIBUTE_ID, uid
			.toString());
		search
			.addStringAttribute(ORGANIZATION_NAME_ATTRIBUTE_ID,
				getFullName(),
				Search.TextSearchConfigValue.EXACT_MATCH);
		cardIds = new ArrayList<ObjectId>(ObjectIdUtils
			.collectionToSetOfIds(doSearch(search)));
		if (!cardIds.isEmpty()) {
		    return cardIds;
		}
	    }
	    Search search = getFetchAction();
	    search.addStringAttribute(ORGANIZATION_UUID_ATTRIBUTE_ID, uid
		    .toString());
	    // If name is not defined or cards by UUID and name were not found
	    List<Card> cards = doSearch(search);

	    // If there were found several organizations with same UUID, we
	    // should return super organization with such UUID.
	    if (cards.size() > 1) {
		return Collections.singletonList(findHeadParent(cards));
	    }
	    cardIds = new ArrayList<ObjectId>(ObjectIdUtils
		    .collectionToSetOfIds(cards));
	}
	return cardIds;
    }

    private ObjectId findHeadParent(List<Card> cards) {

	@SuppressWarnings("unchecked")
	Map<ObjectId, Card> cardCache = ObjectIdUtils
		.collectionToObjectIdMap(cards);

	Card activeCard = cards.get(0);

	while (true) {
	    Attribute superOrgAttribute = activeCard
		    .getAttributeById(SUPER_ORGANIZATION_ATTRIBUTE_ID);
	    if (superOrgAttribute == null)
		return activeCard.getId();
	    ObjectId parentCardId = ((CardLinkAttribute) superOrgAttribute)
		    .getSingleLinkedId();
	    if (cardCache.containsKey(parentCardId)) {
		activeCard = cardCache.get(parentCardId);
	    } else {
		return activeCard.getId();
	    }
	}
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