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
package com.aplana.dbmi.cardexchange;

import java.util.Iterator;

import com.aplana.dbmi.action.ObjectAction;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.ProcessorBase;

public class CustomCardProcessor extends ProcessorBase implements Parametrized {
	private final String PARAM_CONF_FILE = "config";
	private String confFile;
	public Object process() throws DataException {
		ObjectId cardId = null;

		if (getObject() != null) {
			cardId = getObject().getId();
		} else if (getAction() != null && getAction() instanceof ObjectAction) {
			ObjectAction action = (ObjectAction)getAction();
			cardId = action.getObjectId();
		}
		assert(cardId != null && cardId.getType().equals(Card.class));
		
		ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(Card.class);
		cardQuery.setId(cardId);
		Card card = (Card)getDatabase().executeQuery(getSystemUser(), cardQuery);
		
		Iterator i = CardProcessorsFactory.getCardProcessors(confFile, card).iterator();
		if (!i.hasNext()) {
			logger.info("No custom processors found for card " + cardId.getId());
		}
		while (i.hasNext()) {
			ProcessorBase cardProcessor = (ProcessorBase)i.next();
			logger.info("Launching " + cardProcessor.getClass() + " for card " + cardId.getId());			
			cardProcessor.init(getCurrentQuery());
			cardProcessor.setObject(card);
			cardProcessor.setBeanFactory(getBeanFactory());
			cardProcessor.setCurExecPhase(getCurExecPhase());
			cardProcessor.process();
		}
		return null;
	}

	public void setParameter(String name, String value) {
		if (PARAM_CONF_FILE.equals(name)) {
			confFile = value;
		}
	}
}
