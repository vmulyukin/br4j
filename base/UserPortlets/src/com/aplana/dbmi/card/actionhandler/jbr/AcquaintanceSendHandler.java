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
import com.aplana.dbmi.actionhandler.ActionsManager;
import com.aplana.dbmi.card.ActionsSupportingAttributeEditor;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.card.actionhandler.CardPortletAttributeEditorActionHandler;
import com.aplana.dbmi.card.hierarchy.Messages;
import com.aplana.dbmi.card.Parametrized; //
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

public class AcquaintanceSendHandler extends CardPortletAttributeEditorActionHandler implements Parametrized
{
	private static final ObjectId STATE_DRAFT =
		ObjectId.predefined(CardState.class, "draft");
	private static final ObjectId ATTR_PERSON =
		ObjectId.predefined(PersonAttribute.class, "jbr.information.person");
	private static final ObjectId MOVE_SEND =
		ObjectId.predefined(WorkflowMove.class, "jbr.info.send");
	
	private static final String PARAM_MAINDOC_DENY_STATUSES = "maindoc_deny_statuses";
	private List<Long> _denied_statuses = null;

	protected void process(Attribute attr, List<ObjectId> cardIds, ActionRequest request,
			ActionResponse response) throws DataException {
		if (cardIds == null || cardIds.size() == 0)
			cardIds = new ArrayList<ObjectId>(((CardLinkAttribute) attr).getIdsLinked());
		Map<ObjectId, Card> cardMap;
		try {
			cardMap = getCards(cardIds);
		} catch (Exception e) {
			getCardPortletSessionBean().setMessage(e.getMessage());
			return;
		}
		
		// �� ��������� �� ��������-������ � ����������� ��������?
		if( checkDeniedStatuses() )
		{
			getCardPortletSessionBean().setMessage( getMessage("maindoc_stateError") );
			return;
		}

		if (!storeCard(request)) {
			return; // �������������� ��������-��������� ����� ��������� �� ������������
		}

		StringBuffer message = new StringBuffer();
		int sentCount = 0;
		for (Iterator<ObjectId> itr = cardIds.iterator(); itr.hasNext(); ) {
			ObjectId itemId = (ObjectId) itr.next();
			Card item = (Card) cardMap.get(itemId);
			if (!STATE_DRAFT.equals(item.getState())) {
				logger.info("Card " + item.getId().getId() + " already sent");
				continue;
			}
			ChangeState move = new ChangeState();
			move.setWorkflowMove((WorkflowMove) DataObject.createFromId(MOVE_SEND));
			move.setCard(item);
			try {
				serviceBean.doAction(move);
			} catch (Exception e) {
				message.append("<br>").append(getMessage("sendError",
						new Object[] { item.getAttributeById(ATTR_PERSON).getStringValue(), e.getMessage() }));
				continue;
			}
			sentCount++;
		}
		if (sentCount == 0)
			message.insert(0, getMessage("sendNone"));
		else
			message.insert(0, getMessage("sendOk", new Object[] { new Integer(sentCount) }));
		getCardPortletSessionBean().setMessage(message.toString());
	}

	private Map<ObjectId, Card> getCards(List<ObjectId> ids) throws DataException, ServiceException {
		Search search = new Search();
		search.setByAttributes(false);
		search.setByCode(true);
		search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(ids));
		search.setColumns(new ArrayList(3));
		SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(Card.ATTR_STATE);
		search.getColumns().add(col);
		col = new SearchResult.Column();
		col.setAttributeId(ATTR_PERSON);
		search.getColumns().add(col);
		SearchResult result = (SearchResult) serviceBean.doAction(search);
		
		HashMap<ObjectId, Card> map = new HashMap<ObjectId, Card>(ids.size());
		for (Iterator<Card> itr = result.getCards().iterator(); itr.hasNext(); ) {
			Card card = (Card) itr.next();
			map.put(card.getId(), card);
		}
		return map;
	}

	private boolean storeCard(ActionRequest request) {
		CardPortletSessionBean sessionBean = getCardPortletSessionBean();
		Card card = sessionBean.getActiveCard();
		try {
			sessionBean.getServiceBean().saveObject(card);
			return true;
		} catch (Exception e) {
			String msg = getMessage("saveError") + ": " + e.getMessage();
			sessionBean.setMessageWithType(msg, PortletMessageType.ERROR);
			return false;
		}
	}

	private Messages messages = null;
	private String getMessage(String key, Object[] params) {
		if (messages == null) {
			ActionsManager mgr = (ActionsManager) getCardPortletSessionBean()
					.getActiveCardInfo().getAttributeEditorData(getAttribute().getId(),
							ActionsSupportingAttributeEditor.ACTIONS_MANAGER_KEY);
			messages = mgr.getActionsDescriptor().getMessages();
		}
		return MessageFormat.format(messages.getMessage(key).getValue(), params);
	}
	
	private String getMessage(String key) {
		return getMessage(key, null);
	}
	
	// implements com.aplana.dbmi.card.Parametrized.setParameter
	public void setParameter( String name, String value )
	{
		if( PARAM_MAINDOC_DENY_STATUSES.equals(name) )
		{
			if( value != null && !value.equals("") )
			{
				String[] s_ids = value.split(",");
				if( s_ids.length > 0 )
				{
					_denied_statuses = new ArrayList<Long>( s_ids.length );
					for( String s_id: s_ids )
						_denied_statuses.add( Long.parseLong(s_id) );
				}
			}
			else _denied_statuses = null;
		}
	}
	
	
	/**
	 * ���������, �� ��������� �� ��������-��������� � "�����������" ������� ��� �������� ������������ �� ������������ 
	 * @return true, ���� �������� � "�����������" �������, ����� false
	 */
	private boolean checkDeniedStatuses()
	{
		if( _denied_statuses == null ) return false; // �� ������ ����� �������
		CardPortletSessionBean sessionBean = getCardPortletSessionBean();
		Card card = sessionBean.getActiveCard();
		ObjectId stateId = card.getState();
		if( _denied_statuses.contains( stateId.getId() ) )
			return true;
		return false;
	}
}
