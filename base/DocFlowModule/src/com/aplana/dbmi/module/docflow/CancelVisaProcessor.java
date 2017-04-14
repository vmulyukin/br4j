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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class CancelVisaProcessor extends DataServiceClient implements DocumentProcessor {
	public static final String INCLUSE_IDS_LIST = "visa.states.include";
	
	private VisaConfiguration config;
	private ObjectId docId;
	private String name;
	private Card document;
	private List<Card> visas;

	public void setConfig(VisaConfiguration config) {
		this.config = config;
	}
	
	public void setDocumentId(ObjectId docId) {
		this.docId = docId;
	}
	
	public void setBeanName(String name) { 
		this.name = name; 
	}
	
	public String getName() { 
		return name; 
	}

	public void process() throws DataException {
		for (Iterator<Card> i = getVisas().iterator(); i.hasNext();) {
			Card visa = (Card) i.next();
			sendForVisa(visa);
		}
//		proceedDocument();
	}

	private void sendForVisa(Card visa) throws DataException {
		logger.info("[" + getName() + ":" + docId.getId() + "] Sending visa card #" + visa.getId().getId());
		doAction(new LockObject(visa));
		try {
			ChangeState move = new ChangeState();
			move.setWorkflowMove((WorkflowMove) DataObject.createFromId(
					config.getObjectId(WorkflowMove.class, VisaConfiguration.MOVE_SEND)));
			move.setCard(visa);
			doAction(move);
			try {
				PersonAttribute attrTo = (PersonAttribute) visa.getAttributeById(
						config.getObjectId(PersonAttribute.class, VisaConfiguration.ATTR_PERSON));
				logger.info("Document " + docId.getId() + " sent for visa to " + attrTo.getPersonName());
			} catch (Exception e) {
				// just logging failed - ignore
			}
		} finally {
			doAction(new UnlockObject(visa));
		}
	}

	/*private void proceedDocument() throws DataException {
		logger.info("[" + getName() + ":" + docId.getId() + "] Sending document to the next stage");
		doAction(new LockObject(docId));
		try {
			ChangeState move = new ChangeState();
			move.setWorkflowMove((WorkflowMove) DataObject.createFromId(
					config.getObjectId(WorkflowMove.class, VisaConfiguration.MOVE_PROCEED)));
			move.setCard((Card) DataObject.createFromId(docId));
			doAction(move);
			logger.info("Document " + docId.getId() + " proceeded to the next stage");
		} finally {
			doAction(new UnlockObject(docId));
		}
	}*/

	private Object doAction(Action action) throws DataException {
		ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAccessChecker(null);
		query.setAction(action);
		return getDatabase().executeQuery(getSystemUser(), query);
	}

	@SuppressWarnings("unchecked")
	private List<Card> getVisas() throws DataException {
		if (visas == null) {
			logger.info("[" + getName() + ":" + docId.getId() + "] Fetching document's visas");
			Search search = new Search();
			search.setByCode(true);
			
			final ObjectId id = config.getObjectId(CardLinkAttribute.class, VisaConfiguration.ATTR_VISAS);
			// getDocument().getAttributeById(id).getStringValue()
			search.setWords( getDocument().getCardLinkAttributeById(id).getLinkedIds() ); // (2009/12/11m RuSA) OLD: getStringValue()
			search.setColumns(new ArrayList<SearchResult.Column>());
			SearchResult.Column col = new SearchResult.Column();
			col.setAttributeId(config.getObjectId(PersonAttribute.class, VisaConfiguration.ATTR_PERSON));
			search.getColumns().add(col);
			col = new SearchResult.Column();
			col.setAttributeId(Card.ATTR_STATE);
			search.getColumns().add(col);
			SearchResult result = (SearchResult) doAction(search);
			visas = (List<Card>) result.getCards();
			for (ListIterator<Card> i = visas.listIterator(); i.hasNext();) {
				Card visa = i.next();
				if (!config.isListedId(visa.getState(), INCLUSE_IDS_LIST))
					i.remove();
			}
		}
		return visas;
	}
	
	@SuppressWarnings("unchecked")
	private Card getDocument() throws DataException {
		ObjectId visaSetAttr = config.getObjectId(CardLinkAttribute.class, VisaConfiguration.ATTR_VISAS);
		if (document == null) {
			logger.info("[" + getName() + ":" + docId.getId() + "] Fetching the document");
			Search search = new Search();
			search.setByCode(true);
			search.setWords(docId.getId().toString());
			search.setColumns(new ArrayList<SearchResult.Column>());
			SearchResult.Column col = new SearchResult.Column();
			col.setAttributeId(visaSetAttr);
			search.getColumns().add(col);
			SearchResult result = (SearchResult) doAction(search);
			if (result.getCards().size() != 1)
				throw new DataException("docflow.visa.document",
						new Object[] { docId.getId().toString() });
			document = (Card) result.getCards().iterator().next();
			// Search does not return some attributes in cards if they don't have values,
			// so we must care about their presence in results.
			if (document.getAttributeById(visaSetAttr) == null)
				document.getAttributes().add(DataObject.createFromId(visaSetAttr));
		}
		return document;
	}
	
	public void setDocument(Card document) {
		this.document = document;
	}
}
