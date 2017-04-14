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
package com.aplana.distrmanager.processors;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.aplana.dbmi.action.ExportCardToXml.Result;
import com.aplana.dbmi.jbr.processors.ProcessCard;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.jbr.util.IdUtils.IdPair;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.distrmanager.LoadSaveDocFacade;
import com.aplana.distrmanager.cards.ElementListMailing;
import com.aplana.distrmanager.cards.MessageGOST;
import com.aplana.distrmanager.cards.StateCard;
import com.aplana.distrmanager.cards.WrapperCardsFactory;
import com.aplana.distrmanager.exceptions.ChangeStateException;
import com.aplana.distrmanager.exceptions.DeleteMessageGostException;
import com.aplana.distrmanager.exceptions.DistributionManagerException;
import com.aplana.distrmanager.exceptions.FindMessageGostException;
import com.aplana.distrmanager.exceptions.LockDeleteException;
import com.aplana.distrmanager.exceptions.SaveCardException;
import com.aplana.distrmanager.exceptions.WriteResultException;
import com.aplana.distrmanager.handlers.CheckDocBase;
import com.aplana.distrmanager.handlers.CreateLetter;
import com.aplana.distrmanager.handlers.CreateOutXml;
import com.aplana.distrmanager.handlers.DownloadFile;
import com.aplana.distrmanager.handlers.InitExportBeanFactory;
import com.aplana.distrmanager.handlers.OperationsOnLetter;
import com.aplana.distrmanager.handlers.SaveAttachments;
import com.aplana.distrmanager.handlers.SetTimeInMessage;
import com.aplana.distrmanager.util.UtilsWorkingFiles;

public class ExportNotification extends ProcessCard {

    /**
     * ��������� ��� �������� �����������, ���������� �� �������� � ��� �� ��� � �������� �������.
     */

    private final Log logger = LogFactory.getLog(getClass());

    private static final long serialVersionUID = 1L;

    private static final String MESSAGE_GOST_ERROR = "jbr.DistributionManager.ExportNotification.notFound.messageGOSTCard";
    private static final String SAVE_XML_ERROR = "jbr.DistributionManager.ExportNotification.errorSaveXml";
    private static final String NONCRITICAL_ERROR = "jbr.DistributionManager.ExportNotification.noncriticalError";
    private static final String SUCCESSFUL = "jbr.DistributionManager.ExportNotification.SUCCESSFUL";
    private static final String SUCCESSFUL_SAVE = "Successful attempt to save a document. ";
    private static final String UNSUCCESSFUL_SAVE = "Unsuccessful attempt to save a document! ";
    private static final String WRITE_RESULT_ERROR_CRITICAL = "jbr.DistributionManager.ExportNotification.write.result Rollback!";
    private static final String SAVE_CARD_ERROR_CRITICAL = "jbr.DistributionManager.ExportNotification.save.card Rollback!";
    private static final String CHANGE_STATE_ERROR_CRITICAL = "jbr.DistributionManager.ExportNotification.change.status Rollback!";
    private static final String ERROR_CRITICAL = "jbr.DistributionManager.ExportNotification.process.criticalError.Rollback";
    private static final String FIND_MESSAGE_GOST_ERROR = "jbr.DistributionManager.ExportNotification.notFound.msgGost";
    private static final String DELETE_MESSAGE_GOST_ERROR_CRITICAL = "jbr.DistributionManager.ExportNotification.DeleteMessageGost.Rollback";
    private static final String SEND_DOCUMENT_ERROR_CRITICAL = "jbr.DistributionManager.ExportNotification.process.sendDocError.Rollback";

    /**
     * �������� ��������� �������: ������������� �� ���� (������� - �������� ��������) � �������� -
     * ���� (������� - �������� ��������), ��������� � ��������� ���������� ��� ������� ��������.
     * ������ ���������: "�������1 ��������" = ��������1{;�������2 ��������" = ��������2}
     */
    private static final String CONDITIONS = "conditions";

    /**
     * ������� �� ����������� ��������.
     */
    public static final String ID_DOC = "idDocumentBase";

    protected static final String REG_SEPARATOR = "[,;]";
    protected static final String COND_SEPARATOR = "[=]";
    protected static final String BOOLEAN_OR_SEPARATOR = "[||]";

    private Exception erRec = null;
    private Boolean isSuc = false;
    private ElementListMailing elmCardWrap = null;
    private Map<Integer, Map<Integer, IdPair>> setAttrPairsList = new HashMap<Integer, Map<Integer, IdPair>>();
    private LoadSaveDocFacade changerBean = null;
    private DataServiceFacade serviceBean = null;
    private ObjectId backLinkDocBase = null;
    private ObjectId docBaseId = null;
    private MessageGOST msgGOSTWrap = null;
    private ObjectId msgGostId = null;
    private ObjectId noticeGostId = null;
    private Card currentCard = null;
    private File file = null;
    private StateCard stateCardWrap = null;
    private Map<ObjectId, String> attachments = new LinkedHashMap<ObjectId, String>();

    @Override
    public Object process() throws DataException {
        try {
            try {
                currentCard = loadCardById(getCardId()); // ������ ��������, ��� ��������� ��������� �� ����. ����������
                if (!checkConditions()) {
                    return null;    // ���� �������� �������� �� ������ � CONDITIONS, �� ������������ ��������
                }
                changerBean = InitExportBeanFactory.instance().initChangerBean(getBeanFactory());
                serviceBean = InitExportBeanFactory.instance().initServiceBean(getBeanFactory());
                elmCardWrap = WrapperCardsFactory.instance(serviceBean).createElmWrapper(currentCard);
                Card docBase = CheckDocBase.instance(serviceBean).handle(currentCard, backLinkDocBase);
                docBaseId = docBase.getId();
                // ������� �������� "��������� ����"
                try {
                    msgGostId = elmCardWrap.findCardMessageGOST();
                    msgGOSTWrap = new MessageGOST(serviceBean);
                    msgGOSTWrap.init(msgGostId);
                    noticeGostId = msgGOSTWrap.getNoticeGostId();
                } catch(Exception e) {
                    // ���� �������� �� �������, ������ ���� ��������� ������������� ������ � ���������� ����������.
                    // �������/���������� ��������� ���� �������� ��������� ��� ����������/������������ �������� ��������.
                    logError(currentCard, MESSAGE_GOST_ERROR, e);
                    if (null == msgGostId) {
                        return null;
                    } else {
                        erRec = e;
                        throw erRec;
                    }
                }
                stateCardWrap = WrapperCardsFactory.instance(serviceBean).createStateCardWrapper(elmCardWrap);

                // ���� �������� ����������, ��� ������ ��������� ����,
                // �.�. �������� �� ������ ��������� ����, �.�. ��� ��� ����������.
                if (!msgGOSTWrap.isExistAttachments()) {
                    Result xmlOutResult = CreateOutXml.instance(serviceBean).handle(currentCard, docBaseId);
                    SaveAttachments.instance().handle(msgGOSTWrap, xmlOutResult);
                    SetTimeInMessage.instance(serviceBean).handle(elmCardWrap, getJdbcTemplate());
                }

                Result xmlLetter = CreateLetter.instance(serviceBean).handle(msgGOSTWrap, stateCardWrap, elmCardWrap);
                attachments = msgGOSTWrap.getAttachments();
                try {
                    //��������� ������� � �������� �������
                    ObjectId letterId = stateCardWrap.saveLetterXml(xmlLetter);
                    // ��������� � ������ ����������� ������ �������
                    attachments.put(letterId, OperationsOnLetter.RESULT_FILE_NAME);
                } catch(Exception exSaveLetter) {
                    erRec = exSaveLetter;
                    logError(currentCard, SAVE_XML_ERROR, erRec);
                    throw erRec;
                }

                file = DownloadFile.instance(changerBean).handle(docBaseId, currentCard.getId(), msgGostId, attachments);

                // �����
                isSuc = true;
                // �������� ������
                elmCardWrap.byPrepareSent(getJdbcTemplate()); // ������ ������ ��� �� "����������"
                stateCardWrap.setResultProcessing("�������");
                UtilsWorkingFiles.saveCard(stateCardWrap.getCard(), serviceBean);
                changerBean.unlockDirectoriesDownloader(file); // ������� ��� � ���������� ��������
                logError(currentCard, SUCCESSFUL, null);

            } catch (ChangeStateException cse) { // ����������� ������
                logError(currentCard, CHANGE_STATE_ERROR_CRITICAL, cse);
                throw new DataException(CHANGE_STATE_ERROR_CRITICAL, cse);

            } catch (WriteResultException wre) { // ����������� ������
                logError(currentCard, WRITE_RESULT_ERROR_CRITICAL, wre);
                throw new DataException(wre);

            } catch (SaveCardException sce) { // ����������� ������
                logError(currentCard, UNSUCCESSFUL_SAVE, sce);
                throw new DataException(sce);

            } catch (LockDeleteException lde) { // ������������� ������
                // ��������� ��� � ������ "������������" � �������������� ��������� ���������.
                logError(currentCard, "jbr.DistributionManager.exporter.unlock.error File: " + file.getAbsolutePath(), lde);
                elmCardWrap.byPrepareNotSent(getJdbcTemplate()); // ����������� ������
                stateCardWrap.setResultProcessing(lde.toString()); // ����������� ������
                UtilsWorkingFiles.saveCard(stateCardWrap.getCard(), serviceBean); // ����������� ������

            } catch (FindMessageGostException fmge) { // ������������� ������ (�� ����� ��������� ����)
                logError(currentCard, FIND_MESSAGE_GOST_ERROR, fmge);
                // ��������� ������� � ��������� �����
                stateCardWrap.setResultProcessing(fmge.toString());
                UtilsWorkingFiles.saveCard(stateCardWrap.getCard(), serviceBean);
                // ��������� ��� � "������������"
                elmCardWrap.byPrepareNotSent(getJdbcTemplate());

            } catch (Exception e) { // ������������� ������
                isSuc = false; // �������
                erRec = e;
                logError(currentCard, NONCRITICAL_ERROR, e);

                // �������� �������. ��������� ��� � "������������"
                elmCardWrap.byPrepareNotSent(getJdbcTemplate()); // ������ ������ ��� �� "������������"
                // ��������� ������� � ��������� �����
                stateCardWrap.setResultProcessing(erRec.toString());
                UtilsWorkingFiles.saveCard(stateCardWrap.getCard(), serviceBean);
            }
        //���-�� ����� �� ���, ���������� ��� ���������� ����������� ��������� � ������� ����������� ��������� �� ������ ������������
        } catch (ChangeStateException cse) { // ����������� ������
            String logCritical = isSuc ? SUCCESSFUL_SAVE : UNSUCCESSFUL_SAVE;
            logError(currentCard, CHANGE_STATE_ERROR_CRITICAL + logCritical, cse);
            throw new DistributionManagerException(SEND_DOCUMENT_ERROR_CRITICAL);

        } catch (WriteResultException wre) { // ����������� ������
            String logCritical = isSuc ? SUCCESSFUL_SAVE : UNSUCCESSFUL_SAVE;
            logError(currentCard, WRITE_RESULT_ERROR_CRITICAL + logCritical, wre);
            throw new DistributionManagerException(SEND_DOCUMENT_ERROR_CRITICAL);

        } catch (SaveCardException sce) { // ����������� ������
            String logCritical = isSuc ? SUCCESSFUL_SAVE : UNSUCCESSFUL_SAVE;
            logError(currentCard, SAVE_CARD_ERROR_CRITICAL + logCritical, sce);
            throw new DistributionManagerException(SEND_DOCUMENT_ERROR_CRITICAL);

        }  catch (DeleteMessageGostException dmge) { // ����������� ������
            logError(currentCard, DELETE_MESSAGE_GOST_ERROR_CRITICAL, dmge);
            throw new DistributionManagerException(SEND_DOCUMENT_ERROR_CRITICAL);

        } catch (DistributionManagerException dme) { // ����������� ������, ��������� �� ������ ������ ���������� ������������
            throw new DistributionManagerException(SEND_DOCUMENT_ERROR_CRITICAL);

        }catch (Exception e) { // ����������� ������
            String logCritical = isSuc ? SUCCESSFUL_SAVE : UNSUCCESSFUL_SAVE;
            logError(currentCard, ERROR_CRITICAL + logCritical, e);
            throw new DistributionManagerException(ERROR_CRITICAL, e);
        }
        return null;
    }

    @Override
    public void setParameter(String name, String value) {
        if (ID_DOC.equalsIgnoreCase(name)) {
            this.backLinkDocBase = ObjectIdUtils.getObjectId(BackLinkAttribute.class, value, false);
        } else
            if (CONDITIONS.equals(name)){ // ���������� �������� �������
            String[] list = value.trim().split(REG_SEPARATOR); // ��������� ������ �� ������ �������
            int j =0;
            for (String regRule : list) {
                // ��������� ������ �� �������, ������������ ������� OR
                String[] boolOrList = regRule.trim().split(BOOLEAN_OR_SEPARATOR);
                Map<Integer, IdPair> attrPairsListOr = new HashMap<Integer, IdPair>();
                if (boolOrList.length >= 2) { // ���� ���� ����, �� ����� �� ������� � ��������.
                                              // ���� ��� > 1 (>=2), ���������� �� ����� OR
                    // ���� OR ���������
                    int i = 0;
                    for(String boolRule : boolOrList) {
                        String[] pairString = boolRule.split(COND_SEPARATOR); // ��������� ������� �� ����: �������, ��������.
                        try {
                            IdPair pair = getPair(pairString, boolRule);
                            attrPairsListOr.put(i, pair);
                        } catch(Exception e) {
                            i++;
                            continue;
                        }
                        i++;
                    }
                    setAttrPairsList.put(j, attrPairsListOr);
                    j++;
                } else {
                // ���� AND ����������
                    String[] pairString = regRule.split(COND_SEPARATOR); // ��������� ������� �� ����: �������, ��������.
                    try {
                        IdPair pair = getPair(pairString, regRule);
                        Map<Integer, IdPair> attrPairsListAnd = new HashMap<Integer, IdPair>();
                        attrPairsListAnd.put(0, pair);
                        setAttrPairsList.put(j, attrPairsListAnd);
                    } catch(Exception e) {
                        j++;
                        continue;
                    }
                }
                j++;
            }
        } else {
            super.setParameter(name, value);
        }
    }

    private IdPair getPair(String[] pairString, String rule) {
        if (pairString.length != 2) { // ���� ��� ���� ��� ��� �����������
            logger.warn("Broken CONDITIONS rule: " + rule
                    + " Skipping.");
            new Exception("Broken CONDITIONS rule;");
        }
        final IdPair pair = new IdPair();
        pair.dest.setId(IdUtils.smartMakeAttrId(pairString[0].trim(),
                ListAttribute.class, false));
        if (pair.destId() == null) {
            logger.warn("Broken CONDITIONS rule: " + rule
                    + " Can not determine " + "destination attribute: "
                    + pairString[0] + " Skipping.");
            new Exception("Broken CONDITIONS rule;");
        }
        if (!"NULL".equalsIgnoreCase(pairString[1])) {
            pair.source.setId(IdUtils.smartMakeAttrId(
                    pairString[1].trim(), ReferenceValue.class));
            if (pair.sourceId() == null) {
                logger.warn("Broken CONDITIONS rule: " + rule
                        + " Can not determine "
                        + "source attribute/value: " + pairString[1]
                        + " Skipping.");
                new Exception("Broken CONDITIONS rule;");
            }
        }
        return pair;
    }

    private boolean checkConditions() {
        boolean result = false;
        if (setAttrPairsList.isEmpty()) {
            return true;
        }
        for(Map.Entry<Integer, Map<Integer, IdPair>> attrPairsListEntry : setAttrPairsList.entrySet()) {
            IdPair pair = null;
            try {
                Map<Integer, IdPair> attrPairsList = attrPairsListEntry.getValue();
                if (attrPairsList.size() <= 0)
                    continue;
                if (attrPairsList.size() > 1) {
                    // ���� ��������� OR
                    for (IdPair pairOr : attrPairsList.values()) {
                        pair = pairOr;
                        ListAttribute attrList = (ListAttribute)currentCard.getAttributeById(pair.destId()); // �������� ������� ListAttribute
                        if (null != attrList && null != attrList.getValue()) {
                            // ���������� �������� ListAttribute �� �������� � �������� ��������� ListAttribute
                            // true - � �������� ������������ ��������, �������� � �������.
                            // false - � �������� ����������� ��������, �������� � �������.
                            if(attrList.getValue().getId().equals(pair.sourceId())) {
                                result = true;
                                break; // true + false = true
                            }
                        } else {
                            logger.warn("The card does not contain the attribute specified in the configuration file. Attribute: "
                                    + pair.dest + " from " + pair.source);
                        }
                    }
                    // ���� ���� ���� �� ������� �����������, ��������� � ��������� ��������
                    if (result)
                        continue; // true + false = true
                    else
                        // ���� �� ���� �� ������� OR �� �����������, ������
                        // ��������� ������ ���, �.�. ���������� ���� - AND
                        break; // false * true = false
                } else {
                    // ���� ���������� AND
                    pair = attrPairsList.get(0);
                    ListAttribute attrList = (ListAttribute)currentCard.getAttributeById(pair.destId()); // �������� ������� ListAttribute
                    if (null != attrList && null != attrList.getValue()) {
                        // ���������� �������� ListAttribute �� �������� � �������� ��������� ListAttribute
                        // true - � �������� ������������ ��������, �������� � �������.
                        // false - � �������� ����������� ��������, �������� � �������.
                        if(attrList.getValue().getId().equals(pair.sourceId())) {
                            result = true;
                        } else {
                            result = false;
                            break; // false * true = false
                        }

                    } else {
                        logger.warn("The card does not contain the attribute specified in the configuration file. Attribute: "
                                + pair.dest + " from " + pair.source);
                        result = false;
                        break; // false * true = false
                    }
                }
            } catch (Exception e) {
            logger.warn(null != pair?"Error setting attribute " + pair.dest +
                    " from " + pair.source : "Error setting attribute: pair isNull", e);
            }
        }
        return result;
    }

    private void logError(Card card, String msgError, Exception e) {
        String error = String.
            format("{%s} docBaseId: {%s}; elmId: {%s}; elmUUID: {%s}; msgGostId: {%s}; noticeGostId: {%s}",
                    (null == msgError)?"null":msgError,
                    (null == docBaseId)?"null":docBaseId.getId(),
                    (null == card)?"null":card.getId().getId(),
                    (null == elmCardWrap)?"null":elmCardWrap.getUid(),
                    (null == msgGostId)?"null":msgGostId.getId(),
                    (null == noticeGostId)?"null":noticeGostId
            );
        if (null == e) {
            logger.error(error);

        } else {
            logger.error(error, e);
        }
    }
}
