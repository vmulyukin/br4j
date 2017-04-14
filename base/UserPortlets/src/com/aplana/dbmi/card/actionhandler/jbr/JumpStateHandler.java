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

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.card.actionhandler.CardPortletAttributeEditorActionHandler;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.SearchUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;

public class JumpStateHandler extends CardPortletAttributeEditorActionHandler {
	private Log logger = LogFactory.getLog(getClass());
	private DataServiceBean serviceBean;

	public static final ObjectId ATTR_CAME_NOTICE = ObjectId.predefined(
			BackLinkAttribute.class, "jbr.incoming.incomeNotification"); //"JBR_MEDO_IN_NOTIFY"
	public static final ObjectId IN_ACCEPTED = ObjectId.predefined(
		    WorkflowMove.class, "jbr.state_service.accepted");

	@Override
	protected void process(Attribute attr, List<ObjectId> cardIds,
			ActionRequest request, ActionResponse response)
			throws DataException {

		serviceBean = PortletUtil.createService(request);
		Card card = getCardPortletSessionBean().getActiveCard();
		jumpStateLinksCards(card);
	}

	private void jumpStateLinksCards(Card baseDoc) {
		try{
			List<ObjectId> linkedCardIds = SearchUtils.getBackLinkedCardsObjectIds(baseDoc, ATTR_CAME_NOTICE, serviceBean);
			for(ObjectId linkNoticeId : linkedCardIds) {
				setMoveCard(IN_ACCEPTED, linkNoticeId);
			}
		}
		catch(Exception e) {
			logger.error("Error in JumpStateHandler::jumpStateLinksCards !", e);
		}
	}

	private void setMoveCard(ObjectId workflowMove, ObjectId cardId) throws DataException, ServiceException {
		Card card = (Card) serviceBean.getById(cardId);
		if (card != null) {
			try {
				LockObject lock = new LockObject(card.getId());
				serviceBean.doAction(lock);
				try {
				    final ChangeState move = new ChangeState(); // �������� ��������-�������� ��� ��������
				    //serviceBean.saveObject(card);
				    move.setCard(card); // ���������� ��������, ������� ���� ����������.
				    move
					    .setWorkflowMove((WorkflowMove) DataObject
						    .createFromId(workflowMove));
				    serviceBean.doAction(move);
				} finally {
				    UnlockObject unlock = new UnlockObject(
					    card.getId());
				    serviceBean.doAction(unlock);
				}
			} catch(Exception e) {
				logger.error("Error in JumpStateHandler::setMoveCard !", e);
			}
		} else
			throw new DataException("com.aplana.dbmi.card.actionhandler.jbr.JumpStateHandler.CardNotFound: id = " + cardId);
	}
}
