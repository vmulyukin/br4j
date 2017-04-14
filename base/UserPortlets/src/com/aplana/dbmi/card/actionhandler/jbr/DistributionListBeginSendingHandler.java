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
package com.aplana.dbmi.card.actionhandler.jbr;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.card.HierarchicalCardLinkAttributeViewer;
import com.aplana.dbmi.card.actionhandler.CardPortletAttributeEditorActionHandler;
import com.aplana.dbmi.card.hierarchy.Messages;
import com.aplana.dbmi.card.hierarchy.descriptor.HierarchyDescriptor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

public class DistributionListBeginSendingHandler extends CardPortletAttributeEditorActionHandler {

	private static final ObjectId STATE_DRAFT =
		ObjectId.predefined(CardState.class, "draft");
	private static final ObjectId MOVE_BEGIN_SEND =
		ObjectId.predefined(WorkflowMove.class, "jbr.distributionItem.ready");
	
	//Stores all operation's statuses
    private Messages messages = null;
	
	protected void process(Attribute attr, List cardIds, ActionRequest request,
			ActionResponse response) throws DataException {
		if (cardIds == null || cardIds.size() == 0)
			cardIds = new ArrayList(((CardLinkAttribute) attr).getIdsLinked());
		Map cardMap;
		try {
			cardMap = getCards(cardIds);
		} catch (Exception e) {
			getCardPortletSessionBean().setMessage(e.getMessage());
			return;
		}
		StringBuffer message = new StringBuffer();
		int sentCount = 0;
		for (Iterator itr = cardIds.iterator(); itr.hasNext(); ) {
			ObjectId itemId = (ObjectId) itr.next();
			Card item = (Card) cardMap.get(itemId);
			if (!STATE_DRAFT.equals(item.getState())) {
				logger.info("Card " + item.getId().getId() + " already begin sent");
				continue;
			}
			ChangeState move = new ChangeState();
			move.setWorkflowMove((WorkflowMove) DataObject.createFromId(MOVE_BEGIN_SEND));
			move.setCard(item);
			try {
				serviceBean.doAction(move);
			} catch (ServiceException e) {
				throw new DataException(e);
			}
			sentCount++;
		}
		
		if (sentCount == 0)
            message.insert(0, getMessage("sendNone"));
        else
            message.insert(0, getMessage("sendOk", new Object[] { new Integer(sentCount) }));

		getCardPortletSessionBean().setMessage(message.toString());
	}
	
	private String getMessage(String key, Object[] params) {
        if (messages == null) {
            HierarchyDescriptor desc = (HierarchyDescriptor) getCardPortletSessionBean()
                    .getActiveCardInfo().getAttributeEditorData(getAttribute().getId(),
                            HierarchicalCardLinkAttributeViewer.HIERARCHY_VIEW_DESCRIPTOR);
            messages = desc.getMessages();
        }
        return MessageFormat.format(messages.getMessage(key).getValue(), params);
    }
	
	private String getMessage(String key) {
		return getMessage(key, null);
	}

	private Map getCards(List ids) throws DataException, ServiceException {
		Search search = new Search();
		search.setByAttributes(false);
		search.setByCode(true);
		search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(ids));
		search.setColumns(new ArrayList(3));
		SearchResult.Column col = new SearchResult.Column();
		col = new SearchResult.Column();
		col.setAttributeId(Card.ATTR_STATE);
		search.getColumns().add(col);
		SearchResult result = (SearchResult) serviceBean.doAction(search);
		
		HashMap map = new HashMap(ids.size());
		for (Iterator itr = result.getCards().iterator(); itr.hasNext(); ) {
			Card card = (Card) itr.next();
			map.put(card.getId(), card);
		}
		return map;
	}
}
