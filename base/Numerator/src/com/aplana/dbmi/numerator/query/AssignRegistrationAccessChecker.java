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
package com.aplana.dbmi.numerator.query;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.numerator.action.AssignRegistrationAction;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.AccessCheckerBase;

public class AssignRegistrationAccessChecker extends AccessCheckerBase {

	private static final long serialVersionUID = 1L;

	private final static String ROLE_INCOMING_REGISTRATOR = "JBR_INCOMING";
	private final static String ROLE_INCOMING_SENIOR_REGISTRATOR = "JBR_INCOMING_CHR";
	private final static String ROLE_OG_REGISTRATOR = "JBR_OG";
	private final static String ROLE_OG_SENIOR_REGISTRATOR = "JBR_OG_CHR";
	private final static String ROLE_ADMIN = "A";

	@Override
	public boolean checkAccess() throws DataException {
		AssignRegistrationAction action = getAction();
		if (action.getCard() == null) {
			return false;
		}

		Card card = action.getCard();
		ObjectId template = card.getTemplate();

		StringAttribute regnumber = card.getAttributeById(ObjectId.predefined(StringAttribute.class, "regnumber"));
		boolean isRegNumberEmpty = regnumber == null || regnumber.getValue() == null || regnumber.getValue().trim().isEmpty();

		if (!isRegNumberEmpty) {
			return false;
		}

		final boolean isStateDraft     = ObjectId.predefined(CardState.class, "draft").equals(card.getState());
		final boolean isStateExecution = ObjectId.predefined(CardState.class, "execution").equals(card.getState());
		final boolean isStateExam      = ObjectId.predefined(CardState.class, "jbr.exam.waiting").equals(card.getState());

		final boolean isOG 		 = ObjectId.predefined(Template.class, "jbr.incomingpeople").equals(template);
		final boolean isIncoming = ObjectId.predefined(Template.class, "jbr.incoming").equals(template);

		boolean result = 
				isStateDraft && (isOG || isIncoming) //
				|| ((isStateExecution || isStateExam) //
				&& (isOG && (hasRole(ROLE_OG_REGISTRATOR) || hasRole(ROLE_OG_SENIOR_REGISTRATOR) || hasRole(ROLE_ADMIN)) //
				|| isIncoming && (hasRole(ROLE_INCOMING_REGISTRATOR) || hasRole(ROLE_INCOMING_SENIOR_REGISTRATOR) || hasRole(ROLE_ADMIN)))); //
		
		return result;
	}
}
