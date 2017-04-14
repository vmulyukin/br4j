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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.ejb.support.AbstractStatelessSessionBean;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.ImportCardFromXml;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.action.ImportCardFromXml.ImportCard.Result;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dmsi.DeloImportObserver.PassportFileResolver;
import com.aplana.dmsi.action.ImportCardByDelo;
import com.aplana.dmsi.card.handling.CardFacade;
import com.aplana.dmsi.config.ConfigurationException;
import com.aplana.dmsi.util.ServiceUtils;

public class DMSITaskBean extends AbstractStatelessSessionBean implements SessionBean {

    private static final String LETTER_FILE_NAME = "_headers_";
    protected static final List<String> PASSPORT_FILE_NAMES = Arrays.asList(
            "passport.xml", "receipt.xml", "refusal.xml");
    private static final long serialVersionUID = 1L;
    private static Boolean working = false;

    private static DateFormat PREFIX_FILE_FORMAT = new SimpleDateFormat(
            "yyyyMMddHHmmssSSS");

    private static final ObjectId DRAFT_STATE_ID = ObjectId.predefined(
            CardState.class, "draft");
    private static final ObjectId DISTRIB_TEMPLATE_ID = ObjectId.predefined(
            Template.class, "jbr.distributionItem");
    private static final ObjectId DISTRIB_METHOD_ID = ObjectId.predefined(
            ListAttribute.class, "jbr.distributionItem.method");
    private static final ObjectId DISTRIB_METHOD_DELO_ID = ObjectId.predefined(
            ReferenceValue.class, "jbr.distributionItem.method.delo");
    private static final ObjectId READY_FOR_SEND_MOVE_ID = ObjectId.predefined(
            WorkflowMove.class, "jbr.distributionItem.ready");

    private File inFolder;
    private File inProcessedFolder;
    private File inDiscardedFolder;

    private Log logger = LogFactory.getLog(getClass());

    DataServiceFacade dataService;

    @Override
	public void setSessionContext(SessionContext sessionContext) {
		super.setSessionContext(sessionContext);
		setBeanFactoryLocator(ContextSingletonBeanFactoryLocator.getInstance());
		setBeanFactoryLocatorKey("businessBeanFactory");
	}

    @Override
    protected void onEjbCreate() throws CreateException {
    	dataService = (DataServiceFacade) getBeanFactory().getBean("systemDataServiceFacade");
    	initializeFolders();
	}

    private void initializeFolders() {
        Configuration config = Configuration.instance();
        inFolder = initializeFolder(config.getInFolderName());
        inProcessedFolder = initializeFolder(config.getProcessedInFolderName());
        inDiscardedFolder = initializeFolder(config.getDiscardedInFolderName());
    }

    private File initializeFolder(String name) {
        File folder = new File(name);
        if (!folder.isAbsolute()) {
            throw new ConfigurationException(
                    "Absolute path should be defined for " + name);
        }
        if (!folder.exists()) {
            throw new ConfigurationException("Directory " + name
                    + " does not exist");
        }
        if (!folder.isDirectory()) {
            throw new ConfigurationException("File " + name
                    + " is not directory");
        }
        return folder;
    }

    public void process(Map<?, ?> parameters) {
        synchronized (DMSITaskBean.class) {
            if (working)
                return;
            working = true;
        }
        try {
            processInFolder();
            sendMessages();
        } finally {
            synchronized (DMSITaskBean.class) {
                working = false;
            }
        }
    }

    private void processInFolder() {
        if (!inFolder.canRead()) {
            logger.error("It is impossible to read from "
                    + inFolder.getAbsolutePath());
            return;
        }

        File[] subDirectories = inFolder.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        for (int i = 0; i < subDirectories.length; i++) {
            File processingDirectory = subDirectories[i];
            String destFolderName = String.format("%s_%s", PREFIX_FILE_FORMAT
                    .format(new Date()), processingDirectory.getName());

            File[] files = processingDirectory.listFiles(new FilenameFilter() {
                public boolean accept(File directory, String name) {
                    return isPassportFile(name);
                }
            });

            File letter = findLetterFile(processingDirectory);
            File destFolder;
            String errorMessage = null;
            ObjectId importedDocumentCardId = null;
            Result importResult = null;
            ImportCardFromXml.ImportCard importAction = null;

            if (files.length > 0) {
                try {
                    importAction = loadPassportIntoSystem(files[0]);
                    importResult = dataService.doAction(importAction);
                    if (!importResult.getStatusDescription().getStatusCode().equals(Long.valueOf(0))) {
                        throw new IllegalStateException(importResult.getStatusDescription().getResult());
                    }
                    destFolder = getResultProcessedFolder(destFolderName);
                } catch (Exception ex) {
                    destFolder = getResultDiscardedFolder(destFolderName);
                    logger.error("Error during processing "
                            + processingDirectory.getAbsolutePath(), ex);
                    errorMessage = createErrorMessage(ex);
                }
            } else {
                destFolder = getResultDiscardedFolder(destFolderName);
                errorMessage = "The passport file is not found";
            }

            if (importAction instanceof ImportCardByDelo) {
            try {
                importedDocumentCardId = new ObjectId(Card.class,
                            ((ImportCardByDelo) importAction).getImportedDocCardId());
                loadLetterInImportedDocument(letter, importedDocumentCardId);
                DeloImportObserver observer = new DeloImportObserver(
                            dataService, importedDocumentCardId,
                        processingDirectory);
                observer.setPassportFileResolver(new PassportFileResolver() {
                    public boolean isPassportFile(String fileName) {
                        return DMSITaskBean.this.isPassportFile(fileName);
                    }
                });

                if (importResult != null) {
                    observer.onSuccessfull(importResult);
                } else {
                    observer.onError(errorMessage);
                }
            } catch (RuntimeException ex) {
                logger.error(
                        "Error during import result processing is occurrred",
                        ex);
                errorMessage = errorMessage + "\n" + createErrorMessage(ex);
            }
            }
            try {
                FileUtils.moveDirectory(processingDirectory, destFolder);
                if (!StringUtils.isBlank(errorMessage)) {
                    try {
                        createLogFile(destFolder, errorMessage);
                    } catch (IOException ex) {
                        logger.error("Error during log file writing", ex);
                    }
                }
            } catch (IOException ex) {
                logger.error("Unable to move directory " + processingDirectory
                        + " to " + destFolder, ex);
            }
        }
    }

    private File findLetterFile(File processingDirectory) {
        File[] letters = processingDirectory.listFiles(new FilenameFilter() {
            public boolean accept(File directory, String name) {
                return LETTER_FILE_NAME.equals(name.toLowerCase());
            }
        });

        File letter = null;
        if (letters.length < 1) {
            logger.warn("Letter file " + LETTER_FILE_NAME + " was not found");
        } else {
            letter = letters[0];
        }
        return letter;
    }

    private void loadLetterInImportedDocument(File letter, ObjectId cardId) {
        try {
            if (letter == null) {
                return;
            }
            String email = getSenderEmailFromLetter(letter);
            if (StringUtils.isBlank(email)) {
                return;
            }
            CardFacade importedDoc = new CardFacade(dataService, cardId);
            importedDoc.addAttributeValue(ObjectId.predefined(StringAttribute.class, "jbr.importedDoc.email"), email);
            importedDoc.updateCard();
        } catch (FileNotFoundException ex) {
            logger.error("A problem during letter processing", ex);
        } catch (MessagingException ex) {
            logger.error("A problem during letter processing", ex);
        } catch (DMSIException ex) {
            logger.error("A problem during letter processing", ex);
        }
    }

    private String getSenderEmailFromLetter(File letter) throws FileNotFoundException, MessagingException {
        InputStream letterStream = null;
        try {
            Session s = Session.getDefaultInstance(new Properties());
            letterStream = new FileInputStream(letter);
            MimeMessage message = new MimeMessage(s, letterStream);
            Address[] senders = message.getFrom();
            if (senders == null || senders.length < 1) {
                logger.error("It is impossible to get sender from letter");
                return null;
            }
            if (senders.length > 1 && logger.isWarnEnabled()) {
                logger.warn("There are more than one sender was get from letter: " + senders.toString()
                        + ". First of them will be used");
            }
            InternetAddress address = (InternetAddress) senders[0];
            return address.getAddress();
        } finally {
            IOUtils.closeQuietly(letterStream);
        }
    }

    protected boolean isPassportFile(String name) {
        return PASSPORT_FILE_NAMES.contains(name.toLowerCase());
    }

    private ImportCardFromXml.ImportCard loadPassportIntoSystem(File passportFile)
            throws FileNotFoundException, DataException {
        InputStream inStream = null;
        try {
            inStream = new FileInputStream(passportFile);
            ImportCardFromXml action = new ImportCardFromXml();
            action.setSource(inStream);
            action.setFileName(passportFile.getName());
            return (ImportCardFromXml.ImportCard) dataService.doAction(action);
        } finally {
            IOUtils.closeQuietly(inStream);
        }
    }

    private File getResultProcessedFolder(String folderName) {
        return new File(inProcessedFolder, folderName);
    }

    private File getResultDiscardedFolder(String folderName) {
        return new File(inDiscardedFolder, folderName);
    }

    private String createErrorMessage(Exception ex) {
        StringBuilder errorMessage = new StringBuilder();
        Throwable cause = ex;
        errorMessage.append(cause.getMessage());
        while ((cause = cause.getCause()) != null) {
            errorMessage.append(" Caused by: ");
            errorMessage.append(cause.getMessage());
        }
        return errorMessage.toString();
    }

    private void createLogFile(File destFolder, String errorMessage)
            throws IOException {
        File logFile = new File(inDiscardedFolder, destFolder.getName()
                + ".log");
        Writer writer = null;
        try {
            writer = new FileWriter(logFile);
            IOUtils.write(errorMessage, writer);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    private void sendMessages() {
        Collection<Card> distributions;
        try {
            distributions = searchDistributionCards();
        } catch (DMSIException ex) {
            logger.error("Error during search cards for sending", ex);
            return;
        }
        for (Card card : distributions) {
            if (isCardReadyForSent(card)) {
                try {
                    changeState(card, (WorkflowMove) DataObject.createFromId(READY_FOR_SEND_MOVE_ID));
                } catch (DMSIException ex) {
                    logger.error("Error during distribution send: " + card.getId().getId(), ex);
                }
            }
        }
    }

    private Collection<Card> searchDistributionCards() throws DMSIException {
        Search search = new Search();
        search.setByAttributes(true);
        search.setTemplates(Collections.singleton(DataObject
                .createFromId(DISTRIB_TEMPLATE_ID)));
        search.setStates(Collections.singleton(DataObject
                .createFromId(DRAFT_STATE_ID)));
        search.addListAttribute(DISTRIB_METHOD_ID, Collections
                .singleton(DataObject.createFromId(DISTRIB_METHOD_DELO_ID)));
        return ServiceUtils.searchCards(dataService, search, null);
    }

    private boolean isCardReadyForSent(Card card) {
        try {
            ListProject baseDocFetcher = new ListProject();
            baseDocFetcher.setCard(card.getId());
            baseDocFetcher.setAttribute(
                    ObjectId.predefined(BackLinkAttribute.class, "jbr.DistributionListElement.ParentDoc"));
            List<Card> parentDocs = ((SearchResult) dataService.doAction(baseDocFetcher)).getCards();
            return !parentDocs.isEmpty();
        } catch(DataException ex) {
            logger.error("Unable to check whether parent doc exists, skip " +  card.getId(), ex);
            return false;
        }
    }

    private void changeState(Card card, WorkflowMove workflowMove)
            throws DMSIException {
        try {
            LockObject lock = new LockObject(card);
            dataService.doAction(lock);
            try {
                ChangeState changeStateAction = new ChangeState();
                changeStateAction.setCard(card);
                changeStateAction.setWorkflowMove(workflowMove);
                dataService.doAction(changeStateAction);
            } finally {
                UnlockObject unlock = new UnlockObject(card);
                dataService.doAction(unlock);
            }
        } catch (DataException ex) {
            throw new DMSIException(ex.getMessage(), ex);
        }
    }

}
