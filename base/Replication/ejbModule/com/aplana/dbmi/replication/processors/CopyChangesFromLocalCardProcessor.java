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
package com.aplana.dbmi.replication.processors;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.jbr.processors.ProcessCard;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.replication.tool.CardRelationUtils;
import com.aplana.dbmi.replication.tool.ReplicationActiveProcessInfo;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.QueryBase;

import java.util.List;

public class CopyChangesFromLocalCardProcessor extends ProcessCard {
	private static final long serialVersionUID = 1L;

	@Override
	public Object process() throws DataException {
		logger.info("-------Start CopyChangesFromLocalCardProcessor-------");
		ObjectId cardId = getCardId();

		DataServiceFacade facade = new DataServiceFacade(getDatabase(), getSystemUser(), getQueryFactory());
		List<ObjectId> template = CardRelationUtils.resolveLink(cardId, facade, Card.ATTR_TEMPLATE.getId());
		ObjectId templateId = template.get(0);
		if (ReplicationConfiguration.getIndependentTemplates().contains(templateId)) {
			return null;
		}
		if (CardRelationUtils.getReplicationCardForLocalCopy(cardId, facade) == null) {
			return null;
		}

		if (isDuringLoad(getCurrentQuery())) {
			logger.info("Save during load. Copy is not necessary. Exit.");
			return null;
		}

		if (ReplicationActiveProcessInfo.markCardForCopyFromLocal(getPrimaryQuery().getUid(), cardId)) {
			logger.info("-----Marked for copy from local card. ID=" + cardId.getId() + ".-----");

			ExecuteCopyFromLocalCard proc = new ExecuteCopyFromLocalCard(cardId);
			proc.setBeanFactory(getBeanFactory());
			//приоритет в очереди пост-процессоров (должен идти перед ExecuteReplication процессором)
			proc.setQueueOrder(100);
			getPrimaryQuery().addPostProcessor(proc);
		}
		return null;
	}

	private boolean isDuringLoad(QueryBase query) {
		if (query == null) {
			return false;
		}

		if (!(query instanceof ActionQueryBase)) {
			return isDuringLoad(query.getParentQuery());
		}
		Action<?> action = ((ActionQueryBase) query).getAction();
		if (!(action instanceof ChangeState)) {
			return isDuringLoad(query.getParentQuery());
		}
		ObjectId toState = ((ChangeState) action).getWorkflowMove().getToState();
		ObjectId state   = ObjectId.state("jbr.replication.accepted");
		return state.equals(toState) || isDuringLoad(query.getParentQuery());
	}
}
