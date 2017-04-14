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

import java.util.ArrayList;
import java.util.List;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.query.WriteQuery;
import com.aplana.dbmi.support.action.ProcessGroupExecution;

public class DoProcessGroupExecution extends ActionQueryBase implements WriteQuery {
	private final String GROUP_EXECUTION_OPERATION_TYPE = "jbr.group.execution";

	private final ObjectId CURRENT_REPORT_ATTR_ID = ObjectId.predefined(
			TextAttribute.class, "jbr.report.currentText");
	private final ObjectId REPORT_ATTACHMENTS_ATTR_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.report.attachments");
	private final ObjectId REPORT_RESULT_ATTR_ID = ObjectId.predefined(
			TypedCardLinkAttribute.class, "jbr.report.result");

	private static final long serialVersionUID = 1L;

	@Override
	public Object processQuery() throws DataException {
		ProcessGroupExecution action = (ProcessGroupExecution) getAction();
		copyAttributes(action);
		if (action.isOnlyCopy()) {
			return null;
		} else {
			WorkflowMove workflowMove = (WorkflowMove) loadObject(ObjectId
					.predefined(WorkflowMove.class,
							"jbr.report.int.execute"));

			for (ObjectId reportId : action.getReports()) {
				ChangeState changeState = new ChangeState();
				Card reportCard = (Card) loadObject(reportId);

				changeState.setCard(reportCard);
				changeState.setWorkflowMove(workflowMove);
				execAction(new LockObject(reportCard));
				try{
					execAction(changeState);
				} finally {
					execAction(new UnlockObject(reportCard));
				}
			}
		}
		return null;
	}

	private void copyAttributes(ProcessGroupExecution action)
			throws DataException {
		List<Attribute> attributes = new ArrayList<Attribute>();

		attributes.add(action.getCurrentReport().getAttributeById(
				CURRENT_REPORT_ATTR_ID));
		attributes.add(action.getCurrentReport().getAttributeById(
				REPORT_ATTACHMENTS_ATTR_ID));
		attributes.add(action.getCurrentReport().getAttributeById(
				REPORT_RESULT_ATTR_ID));
		for (ObjectId reportId : action.getReports()) {
			OverwriteCardAttributes writer = new OverwriteCardAttributes();
			writer.setCardId(reportId);
			writer.setAttributes(attributes);
			execAction(new LockObject(reportId));
			try {
				execAction(writer);
			} finally {
				execAction(new UnlockObject(reportId));
			}
		}
	}

	private Object execAction(Action action) throws DataException {
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		return getDatabase().executeQuery(getUser(), query);
	}

	private DataObject loadObject(ObjectId id) throws DataException {
		final ObjectQueryBase fetchQuery = getQueryFactory().getFetchQuery(
				id.getType());
		fetchQuery.setId(id);
		return (DataObject) getDatabase().executeQuery(getUser(), fetchQuery);
	}
}