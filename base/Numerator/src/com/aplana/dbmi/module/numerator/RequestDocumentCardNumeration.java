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
package com.aplana.dbmi.module.numerator;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.ObjectAction;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.UserData;

public class RequestDocumentCardNumeration extends CardNumeration {

	public static final ObjectId requestDocumentAttrId = ObjectId.predefined(
			BackLinkAttribute.class, "jbr.reservationRequest.document");

	public Object process() throws DataException 
	{
		Card card = getCard();
		
		execAction( new LockObject(card.getId()));
		Object result = null;
		try {
			result = super.process();
		} finally {			
			execAction( new UnlockObject(card.getId()));
		}

		return result;
	}

	protected boolean isNeedSetRegistrationNumber(Card card) {
		return true;
	}

	protected Card getCard() {
		Card documentCard = null;
		Card acard = (Card) getObject();
		if (acard == null){		
			ObjectId id = ((ObjectAction) getAction()).getObjectId();
			acard = getCard(id);
		}
		
		try {
			BackLinkAttribute documentLink = (BackLinkAttribute) acard.getAttributeById(requestDocumentAttrId);
			ListProject listAction = new ListProject();
			listAction.setCard(acard.getId());
			listAction.setAttribute(documentLink.getId());
			Object listResult = execAction(listAction);
			if (listResult instanceof SearchResult) {
				SearchResult searchResult = (SearchResult)listResult;
				if (searchResult.getCards().size() > 0){
					documentCard = getCard(((Card)searchResult.getCards().get(0)).getId());
				}
			}
		} catch (DataException e) {
			logger.error("Error fetching request document card object", e);
			return null;
		}

		return documentCard;
	}
	
	public Object execAction(Action action) throws DataException {
		return execAction(action, getSystemUser());
	}

	public Object execAction(Action action, UserData user) throws DataException	{
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		return getDatabase().executeQuery(user, query);
	}

	protected boolean saveCard(Card acard, UserData user){
		try {				
			SaveQueryBase query = getQueryFactory().getSaveQuery(acard);
			query.setAccessChecker(null);
			query.setObject(acard);
			getDatabase().executeQuery(user, query);
			return true;
		} catch (DataException e) {
			logger.error("Error saving card object " + acard.getId().toString(), e);
		}
		return false;
	}	
	
}
