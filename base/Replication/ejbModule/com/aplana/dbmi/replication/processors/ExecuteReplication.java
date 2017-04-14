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

import com.aplana.dbmi.jbr.processors.AbstractCardProcessor;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.replication.action.CheckStopCondition;
import com.aplana.dbmi.replication.action.CreateReplicationPackage;
import com.aplana.dbmi.service.DataException;

public class ExecuteReplication extends AbstractCardProcessor {
	private static final long serialVersionUID = 1L;

	private Card card;
	
	public ExecuteReplication(Card c) {
		this.card = c;
	}
	
	@Override
	public Object process() throws DataException {
		doReplicatePackage(card);
		checkStopCondition(card);
		return null;
	}
	
	private void checkStopCondition(Card card) throws DataException {
		CheckStopCondition checkAction = new CheckStopCondition();
		checkAction.setCard(card);
		execAction(checkAction);
	}
	
	private void doReplicatePackage(Card card) throws DataException {
		CreateReplicationPackage action = new CreateReplicationPackage();
		action.setCard(card);
		execAction(action);
	}
}
