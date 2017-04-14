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
package com.aplana.dbmi.module.docflow;

import java.util.Date;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.ObjectAction;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ParametrizedProcessor;

public class CalcNegotiationDateProcessor extends ParametrizedProcessor {
	private static final long serialVersionUID = 2L;
	
	static final ObjectId planNegotiationDateId = ObjectId.predefined(DateAttribute.class, "jbr.plan_negotiation_date"); 
	
	public Object process() throws DataException {
		CalcNegotiationDate action = new CalcNegotiationDate(getCardId());
		execAction(action);
		return null;
	}
	
	private ObjectId getCardId() {
		if (getObject() != null) {
			return getObject().getId();
		}
		Action action = getAction(); 

		if (action instanceof ChangeState) {
			return ((ChangeState) getAction()).getObjectId();
		} if (action instanceof ObjectAction) {
			ObjectAction objectAction = (ObjectAction)action;
			if (objectAction.getObjectId().getType().equals(Card.class)) {
				return objectAction.getObjectId();	
			}
		}
		return null;
	}
	
	private Object execAction(Action action) throws DataException {
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		return getDatabase().executeQuery(getSystemUser(), query);
	}
}