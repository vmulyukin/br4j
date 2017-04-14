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
package com.aplana.medo.cards;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import org.w3c.dom.Document;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.medo.CardImportClient;
import com.aplana.medo.XmlUtils;

/**
 * One of <code>CardHandler</code> that allows to create or update 'Imported
 * document' card with given parameters.
 */
public class ImportedDocumentCardHandler extends CardHandler {
    public static final ObjectId TEMPLATE_ID = ObjectId.predefined(
	    Template.class, "jbr.ImportedDocument");

    private final static ObjectId CARD_PROCESSED_MOVE = ObjectId.predefined(
	    WorkflowMove.class, "jbr.importedDoc.cardProcessed");
    private final static ObjectId LOAD_FAILED_MOVE = ObjectId.predefined(
	    WorkflowMove.class, "jbr.importedDoc.loadFailed");

    private final static ObjectId NAME_ATTRIBUTE_ID = ObjectId.predefined(
	    StringAttribute.class, "name");
    private final static ObjectId RESULT_ATTRIBUTE_ID = ObjectId.predefined(
	    TextAttribute.class, "jbr.importedDoc.result");
    protected final static ObjectId UID_ATTRIBUTE_ID = ObjectId.predefined(
	    StringAttribute.class, "jbr.importedDoc.UID");
    public final static ObjectId PARENT_DOC_ATTRIBUTE_ID = ObjectId.predefined(
	    BackLinkAttribute.class, "ImportedDocument.document");

    private InputStream stream = null;
    private String fileName = "";

    public ImportedDocumentCardHandler(long cardId) {
	super(cardId);
    }

    /**
     * Creates instance of class that allows to create new 'Imported document'
     * card by file stored in input stream.
     *
     * @param stream -
     *                file representation
     * @param fileName -
     *                file name
     */
    public ImportedDocumentCardHandler(InputStream stream, String fileName) {
	this.stream = stream;
	this.fileName = fileName;
    }

    /**
     * Creates an XML that describe document of 'Imported document' type and
     * call import card service to load this document to system.
     *
     * @return cardId of created card or -1 in case of error
     * @see CardImportClient
     * @throws CardException
     */
    public long createCard() throws CardException {
	if (stream == null) {
	    throw new CardException(
		    "It is impossible to create card according to current state: "
			    + getParameterValuesLog());
	}
	CardXMLBuilder cardXmlBuilder = new CardXMLBuilder();
	cardXmlBuilder.setTemplateId((Long) TEMPLATE_ID.getId());
	cardXmlBuilder.appendAttribute(NAME_ATTRIBUTE_ID.getId().toString(),
		CardXMLBuilder.STRING_TYPE, fileName);

	return CardImportClient.callImportCardService(cardXmlBuilder
		.serialize().toByteArray(), stream, fileName);
    }

    /**
     * Update current card (with id equals to {@link #id}) according to given
     * DOM-document.
     *
     * @param cardDocument -
     *                DOM-document that describes card in format of card import
     *                service
     * @return id of updated card or -1 if error was occurred
     * @throws DataException
     */
    public long updateCard(Document cardDocument) throws DataException {
	CardXMLBuilder importedDocBuilder = new CardXMLBuilder(cardDocument);
	importedDocBuilder.setCardId(getCardId());
	return CardImportClient.callImportCardService(XmlUtils.serialize(
		cardDocument).toByteArray(), null);
    }

    /**
     * Set failed status for card.
     *
     * @param message -
     *                description of error
     * @throws CardException
     */
    public void setFailed(String message) throws CardException {
	String result = "";
	try {
	    result = ContextProvider.getContext().getLocaleMessage(
		    "medo.import.failed");
	} catch (MissingResourceException ex) {
	    result = "Import failed";
	}
	setResult(String.format("%s\n%s", result, message), LOAD_FAILED_MOVE);
    }

    /**
     * Set processed status for card.
     *
     * @throws CardException
     */
    public void setProcessed() throws CardException {
	String result = "";
	try {
	    result = ContextProvider.getContext().getLocaleMessage(
		    "medo.import.successful");
	} catch (MissingResourceException ex) {
	    result = "Import successsful";
	}
	setResult(result, CARD_PROCESSED_MOVE);
    }

    /**
     * Process given workflow and set given message.
     *
     * @param message -
     *                description of result
     * @param workflow -
     *                workflow id that should be processed on card
     * @throws CardException
     */
    private void setResult(String message, ObjectId workflow)
	    throws CardException {
	DataServiceBean serviceBean = getServiceBean();
	WorkflowMove workflowMove = null;

	Card card = updateAttribute(RESULT_ATTRIBUTE_ID, message,
		"jbr.medo.importer.importedDocument.updateFailed");

	try {
	    LockObject lock = new LockObject(card.getId());
	    serviceBean.doAction(lock);
	    try {
		workflowMove = (WorkflowMove) serviceBean.getById(workflow);
		if (workflowMove != null) {
		    ChangeState changeStateAction = new ChangeState();
		    changeStateAction.setCard(card);
		    changeStateAction.setWorkflowMove(workflowMove);
		    serviceBean.doAction(changeStateAction);
		}
	    } finally {
		UnlockObject unlock = new UnlockObject(card);
		serviceBean.doAction(unlock);
	    }
	} catch (DataException ex) {
	    throw new CardException(
		    "jbr.medo.importer.importedDocument.changeStatusFailed", ex);
	} catch (ServiceException ex) {
	    throw new CardException(
		    "jbr.medo.importer.importedDocument.changeStatusFailed", ex);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aplana.medo.cards.CardHandler#getParameterValuesLog()
     */
    @Override
    protected String getParameterValuesLog() {
	StringBuilder logBuilder = new StringBuilder();
	logBuilder.append(super.getParameterValuesLog());
	logBuilder.append(String.format("stream='%s', ", stream));
	logBuilder.append(String.format("fileName='%s'", fileName));
	return logBuilder.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aplana.medo.cards.CardHandler#search()
     */
    @Override
    protected List<ObjectId> search() throws CardException {
	return new ArrayList<ObjectId>();
    }
}
