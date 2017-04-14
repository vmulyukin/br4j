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
package com.aplana.dmsi.processors;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.action.ImportCardFromXml.ImportCard;
import com.aplana.dbmi.action.ImportCardFromXml.ImportCard.Result;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.dmsi.DMSIException;
import com.aplana.dmsi.card.CardHandler;
import com.aplana.dmsi.types.common.Packet;

public class SetResultOfLoading extends ProcessorBase {

	private static final long serialVersionUID = 1L;

	private static final WorkflowMove successfulWorkflowMove = (WorkflowMove) DataObject.createFromId(ObjectId
			.predefined(WorkflowMove.class, "jbr.importedDoc.cardProcessed"));
	private static final WorkflowMove unsuccessfulWorkflowMove = (WorkflowMove) DataObject.createFromId(ObjectId
			.predefined(WorkflowMove.class, "jbr.importedDoc.loadFailed"));

	@Override
	public Object process() throws DataException {
		ImportCard importAction = (ImportCard) getAction();
		Result res = (Result) getResult();
		ObjectId packetCardId = new ObjectId(Card.class, importAction.getPacketCardId());
		try {
			Packet packet = new Packet();
			packet.setId(String.valueOf(importAction.getPacketCardId()));
			packet.setProcessingResult(res.getStatusDescription().getResult());
			packet.setErrorCode(res.getStatusDescription().getStatusCode());
			CardHandler cardHandler = new CardHandler(getDataServiceBean());
			cardHandler.updateCard(packet);
		} catch (DMSIException ex) {
			throw new DataException("distribution.resultProcessing", new Object[] { packetCardId.getId() }, ex);
		}
		if (res.isResultSuccessful()) {
			changeState(packetCardId, successfulWorkflowMove);
		} else {
			changeState(packetCardId, unsuccessfulWorkflowMove);
		}
		return res;
	}

	private DataServiceFacade getDataServiceBean() {
		DataServiceFacade serviceBean = new DataServiceFacade();
		serviceBean.setUser(getUser());
		serviceBean.setDatabase(getDatabase());
		serviceBean.setQueryFactory(getQueryFactory());
		return serviceBean;
	}

	private void changeState(ObjectId cardId, WorkflowMove workflowMove) throws DataException {
		DataServiceFacade dataService = getDataServiceBean();
		Card card = (Card) DataObject.createFromId(cardId);
		LockObject lock = new LockObject(card);
		dataService.doAction(lock);
		try {
			ChangeState changeStateAction = new ChangeState();
			changeStateAction.setCard(card);
			changeStateAction.setWorkflowMove(workflowMove);
			dataService.doAction(changeStateAction);
		} finally {
			UnlockObject unlock = new UnlockObject(card);
			dataService.doAction(unlock);
		}
	}
}
