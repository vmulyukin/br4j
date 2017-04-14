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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.HierarchicalCardLinkAttributeViewer;
import com.aplana.dbmi.card.actionhandler.CardPortletAttributeEditorActionHandler;
import com.aplana.dbmi.card.hierarchy.Messages;
import com.aplana.dbmi.card.hierarchy.descriptor.HierarchyDescriptor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

public class DistributionListSendHandler extends CardPortletAttributeEditorActionHandler
{
	private static final ObjectId STATE_READY_FOR_SEND =
		ObjectId.predefined(CardState.class, "jbr.distributionItem.readyForSend");
	private static final ObjectId ATTR_METHOD =
		ObjectId.predefined(ListAttribute.class, "jbr.distributionItem.method");
	private static final ObjectId VALUE_METHOD_EMAIL =
		ObjectId.predefined(ReferenceValue.class, "jbr.distributionItem.method.email");
	private static final ObjectId VALUE_METHOD_MEDO =
		ObjectId.predefined(ReferenceValue.class, "jbr.distributionItem.method.medo");
	private static final ObjectId VALUE_METHOD_DELO =
		ObjectId.predefined(ReferenceValue.class, "jbr.distributionItem.method.delo");
	private static final ObjectId VALUE_METHOD_GOST =
		ObjectId.predefined(ReferenceValue.class, "jbr.distributionItem.method.gost");
	private static final ObjectId ATTR_RECIPIENT =
		ObjectId.predefined(CardLinkAttribute.class, "jbr.distributionItem.recipient");
	private static final ObjectId MOVE_SEND =
		ObjectId.predefined(WorkflowMove.class, "jbr.distributionItem.send");
	private static final ObjectId ATTR_REG_NUMBER =
		ObjectId.predefined(StringAttribute.class, "jbr.maindoc.regnum");
	private static final ObjectId ATTR_REG_DATE =
		ObjectId.predefined(DateAttribute.class, "jbr.maindoc.regdate");

	private static final List<ObjectId> skippingMethods =
		Arrays.asList(VALUE_METHOD_EMAIL, VALUE_METHOD_DELO,
			VALUE_METHOD_GOST, VALUE_METHOD_MEDO);


	@Override
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
			if (!STATE_READY_FOR_SEND.equals(item.getState())) {
				logger.info("Card " + item.getId().getId() + " is not status 'Ready for send'");
				continue;
			}
			ListAttribute method = (ListAttribute) item.getAttributeById(ATTR_METHOD);
			if (method != null && method.getValue() != null && skippingMethods.contains(method.getValue().getId())) {
				logger.info("Card " + item.getId().getId() +  " not send document to because sending method " +
					method.getValue().getId() + ": " +
					method.getValue().getValue() + " is in ignore list");
				continue;
			}
			ChangeState move = new ChangeState();
			move.setWorkflowMove((WorkflowMove) DataObject.createFromId(MOVE_SEND));
			move.setCard(item);
			try {
				LockObject lock = new LockObject(item);
				serviceBean.doAction(lock);
				serviceBean.doAction(move);
			} catch (Exception e) {
				message.append("<br>").append(getMessage("sendError",
						new Object[] { item.getAttributeById(ATTR_RECIPIENT).getStringValue(), e.getMessage() }));
				continue;
			}
			finally {
				UnlockObject unlock = new UnlockObject(item);
				try {
					serviceBean.doAction(unlock);
				} catch (ServiceException e) {
					logger.info("Card " + item.getId().getId() +  " not locked");
				}
			}
			sentCount++;
		}
		if (sentCount == 0)
			message.insert(0, getMessage("sendNone"));
		else
			message.insert(0, getMessage("sendOk", new Object[] { new Integer(sentCount) }));
		getCardPortletSessionBean().setMessage(message.toString());
	}

	private Map getCards(List ids) throws DataException, ServiceException {
		Search search = new Search();
		search.setByAttributes(false);
		search.setByCode(true);
		search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(ids));
		search.setColumns(new ArrayList(3));
		SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(ATTR_METHOD);
		search.getColumns().add(col);
		col = new SearchResult.Column();
		col.setAttributeId(Card.ATTR_STATE);
		search.getColumns().add(col);
		col = new SearchResult.Column();
		col.setAttributeId(ATTR_RECIPIENT);
		col.setLabelAttrId(Attribute.ID_NAME);
		search.getColumns().add(col);
		SearchResult result = (SearchResult) serviceBean.doAction(search);

		HashMap map = new HashMap(ids.size());
		for (Iterator itr = result.getCards().iterator(); itr.hasNext(); ) {
			Card card = (Card) itr.next();
			map.put(card.getId(), card);
		}
		return map;
	}

	private Messages messages = null;
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

	@Override
	public boolean isApplicableForUser() {
		// ���������� �������� ����� ������ ���� �������� ���������������.
		// �������� ������� ������������������, ���� � ���� ������ ���� ����������� � ��������������� �����
		CardPortletSessionBean sessionBean = getCardPortletSessionBean();
		Card c = sessionBean.getActiveCard();
		StringAttribute sa = (StringAttribute)c.getAttributeById(ATTR_REG_NUMBER);
		DateAttribute da = (DateAttribute)c.getAttributeById(ATTR_REG_DATE);
		return sa != null && da != null && !sa.isEmpty() && !da.isEmpty();
	}
}
