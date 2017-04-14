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
package com.aplana.dbmi.support.query;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.support.action.ProcessGroupExecution;

public class SaveGroupExecution extends SaveQueryBase {
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	private final static ObjectId REPORT_CARDS_ATTR_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.report.cards");
	
	@Override
	protected ObjectId processNew() throws DataException {
		process();
		return null;
	}

	@Override
	protected void processUpdate() throws DataException {
		process();
	}
	
	private void process() throws DataException{
		final Card groupExecution = (Card) getObject();
		
		
		ProcessGroupExecution  groupExecutionAction = new ProcessGroupExecution();
		groupExecutionAction.setCurrentReport(groupExecution);
		groupExecutionAction.setReports(groupExecution.
				getCardLinkAttributeById(REPORT_CARDS_ATTR_ID).getIdsLinked());
		groupExecutionAction.setOnlyCopy(true);
		
		final ActionQueryBase query = getQueryFactory().getActionQuery(groupExecutionAction);
		query.setAction(groupExecutionAction);
		getDatabase().executeQuery(getUser(), query);
	}
}
