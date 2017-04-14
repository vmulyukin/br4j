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
package com.aplana.dmsi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.action.ImportCardFromXml.ImportCard.Result;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dmsi.card.CardHandler;
import com.aplana.dmsi.types.DocTransfer;
import com.aplana.dmsi.types.LinkDocTransfer;
import com.aplana.dmsi.types.common.ImportedDocCard;
import com.aplana.dmsi.util.ServiceUtils;

public class DeloImportObserver {

    public static interface PassportFileResolver {
        boolean isPassportFile(String fileName);
    }

    private final static ObjectId CARD_PROCESSED_MOVE = ObjectId.predefined(
            WorkflowMove.class, "jbr.importedDoc.cardProcessed");
    private final static ObjectId LOAD_FAILED_MOVE = ObjectId.predefined(
            WorkflowMove.class, "jbr.importedDoc.loadFailed");

    private Log logger = LogFactory.getLog(getClass());

    private final DataServiceFacade serviceBean;
    private final ObjectId importedDocumentCardId;
    private File workingDirectory;
    private PassportFileResolver passportFileResolver = new PassportFileResolver() {
        public boolean isPassportFile(String fileName) {
            return false;
        }
    };

    public DeloImportObserver(DataServiceFacade serviceBean,
            ObjectId importedDocumentCardId, File workingDirectory) {
        this.serviceBean = serviceBean;
        this.importedDocumentCardId = importedDocumentCardId;
        this.workingDirectory = workingDirectory;
    }

    public void onSuccessfull(Result result) {
        if (!canBeProcessed()) {
            return;
        }
        try {
            uploadFiles(result.getPaths());
            Card card = (Card) DataObject.createFromId(importedDocumentCardId);
            WorkflowMove move = (WorkflowMove) DataObject
                    .createFromId(CARD_PROCESSED_MOVE);
            changeState(card, move);
        } catch (DMSIException ex) {
            throw new IllegalStateException(ex);
        } catch (FileNotFoundException ex) {
            throw new IllegalStateException("Unable to upload file", ex);
        }
    }

    public void onError(String errorMessage) {
        if (!canBeProcessed()) {
            return;
        }
        try {
            CardHandler cardHandler = new CardHandler(serviceBean);
            ImportedDocCard importedDocCard = new ImportedDocCard();
            importedDocCard.setId(importedDocumentCardId.getId().toString());
            importedDocCard.setProcessingResult(errorMessage);
            addFilesToImportedDocCard(importedDocCard);
            cardHandler.updateCard(importedDocCard);

            Map<ObjectId, String> fileCards = new HashMap<ObjectId, String>(
                    importedDocCard.getFiles().size());
            for (DocTransfer docTransfer : importedDocCard.getFiles()) {
                fileCards.put(new ObjectId(Card.class, Long.valueOf(docTransfer
                        .getId())), ((LinkDocTransfer) docTransfer).getLink());
            }
            uploadFiles(fileCards);

            Card card = (Card) DataObject.createFromId(importedDocumentCardId);
            WorkflowMove move = (WorkflowMove) DataObject
                    .createFromId(LOAD_FAILED_MOVE);
            changeState(card, move);
        } catch (DMSIException ex) {
            throw new IllegalStateException(ex);
        } catch (FileNotFoundException ex) {
            throw new IllegalStateException("Unable to upload file", ex);
        }
    }

    private void addFilesToImportedDocCard(ImportedDocCard importedDocCard) {
        File[] files = workingDirectory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return !getPassportFileResolver().isPassportFile(name);
            }
        });
        if (files == null) {
            logger.error("Unable to get files from working directory");
            files = new File[0];
        }
        for (File file : files) {
            LinkDocTransfer docTransfer = new LinkDocTransfer();
            docTransfer.setLink(file.getName());
            importedDocCard.getFiles().add(docTransfer);
        }
    }

    private boolean canBeProcessed() {
        if (importedDocumentCardId == null) {
            logger.warn("It is impossible to assign result of processing: "
                    + "file was not imported");
            return false;
        }
        return true;
    }

    private void uploadFiles(Map<ObjectId, String> files)
            throws FileNotFoundException {
        for (Entry<ObjectId, String> cardFile : files.entrySet()) {
            ObjectId cardId = cardFile.getKey();
            String fileName = cardFile.getValue();
            File sourceFile = new File(fileName);
            if (!sourceFile.isAbsolute()) {
                sourceFile = new File(workingDirectory, fileName);
            }
            uploadFile(cardId, sourceFile);
        }
    }

    private void uploadFile(ObjectId cardId, File sourceFile)
            throws FileNotFoundException {
        InputStream fileStream = null;
        try {
            Material material = new Material();
            material.setName(sourceFile.getName());
            fileStream = new FileInputStream(sourceFile);
            material.setData(fileStream);
            ServiceUtils.uploadMaterial(serviceBean, cardId, material);
        } finally {
            IOUtils.closeQuietly(fileStream);
        }
    }

    private void changeState(Card card, WorkflowMove workflowMove)
            throws DMSIException {
        try {
            LockObject lock = new LockObject(card);
            serviceBean.doAction(lock);
            try {
                ChangeState changeStateAction = new ChangeState();
                changeStateAction.setCard(card);
                changeStateAction.setWorkflowMove(workflowMove);
                serviceBean.doAction(changeStateAction);
            } finally {
                UnlockObject unlock = new UnlockObject(card);
                serviceBean.doAction(unlock);
            }
        } catch (DataException ex) {
            throw new DMSIException(ex.getMessage(), ex);
        }
    }

    public PassportFileResolver getPassportFileResolver() {
        return this.passportFileResolver;
    }

    public void setPassportFileResolver(
            PassportFileResolver passportFileResolver) {
        if (passportFileResolver == null) {
            logger.warn("Trying to set null resolver. Ignored");
        } else {
            this.passportFileResolver = passportFileResolver;
        }
    }

}
