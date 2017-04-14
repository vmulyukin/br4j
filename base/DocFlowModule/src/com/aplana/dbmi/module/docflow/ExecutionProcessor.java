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

import java.util.Collection;
import java.util.Iterator;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.service.DataException;

public class ExecutionProcessor extends VisaProcessor {

	@Override
	@SuppressWarnings("rawtypes")
	protected void sendNextVisas(Collection nextVisas) throws DataException {
	}

	protected boolean hasWaitingVisas() throws DataException {
		for (Iterator<Card> itr = getRootVisaCardList().iterator(); itr.hasNext(); ) {
			Card visa = (Card) itr.next();
			if (!isListedIdForTemplate(visa.getState(), VisaConfiguration.STATES_AGREED, visa.getTemplate()))
				return true;
		}
		return false;
	}

}
