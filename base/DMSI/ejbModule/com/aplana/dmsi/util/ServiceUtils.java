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
package com.aplana.dmsi.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.w3c.dom.Document;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.DownloadFile;
import com.aplana.dbmi.action.GetCardIdByUUID;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UploadFile;
import com.aplana.dbmi.action.Search.Filter;
import com.aplana.dbmi.action.SearchResult.Column;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ErrorMessage;
import com.aplana.dbmi.model.InfoMessage;
import com.aplana.dbmi.model.LogEntry;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.WarningMessage;
import com.aplana.dbmi.model.filter.PersonCardIdFilter;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.LogEventBean;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dmsi.DMSIException;

public final class ServiceUtils {

    protected static Log logger = LogFactory.getLog(ServiceUtils.class);

    private static final String CONFIG_DIR = "dbmi/dmsi";

    private ServiceUtils() {
    }

    public static Collection<Card> searchCards(DataServiceFacade serviceBean,
            Search search, Collection<ObjectId> requiredAttributeIds) throws DMSIException {

        search.getFilter().setCurrentUserRestrict(Filter.CU_RW_PERMISSIONS);
        Collection<Column> columns = convertRequiredAttributesToColumns(requiredAttributeIds);

        search.setColumns(columns);
        try {
            SearchResult result = (SearchResult) serviceBean.doAction(search);
            @SuppressWarnings("unchecked")
            List<Card> cards = result.getCards();
            Set<Card> distinctCards = new HashSet<Card>();
            distinctCards.addAll(cards);
            for (Iterator<Card> iter = distinctCards.iterator(); iter.hasNext();) {
                if (!iter.next().getCanRead()) {
                    iter.remove();
                }
            }
            return distinctCards;
        } catch (DataException ex) {
        	throw new DMSIException("Error during search using: " + ServiceUtils.getSearchDescription(search), ex);
        }
    }
    
    /**
     * �������� Id �������� ����� ��������� ������� JBR_UUID
     * @param serviceBean
     * @param uuid
     * @return
     */
    public static ObjectId getCardIdByUUID(DataServiceFacade serviceBean,
            String uuid)  throws DMSIException {
    	return getCardIdByUUID(serviceBean, uuid, Attribute.ID_UUID);
    }

    /**
     * �������� Id �������� ����� ������� ������������ �������
     * @param serviceBean
     * @param uuid
     * @param attrId
     * @return
     */
    public static ObjectId getCardIdByUUID(DataServiceFacade serviceBean,
            String uuid, ObjectId attrId)  throws DMSIException {
        if (StringUtils.isEmpty(uuid))
            return null;

        try {
            GetCardIdByUUID getter = new GetCardIdByUUID();
            getter.setUuid(uuid);
            getter.setAttrId(attrId);
            return (ObjectId) serviceBean.doAction(getter);
        } catch (DataException ex) {
            throw new DMSIException("Error during get card by UUID", ex);
        }
    }

    private static Collection<Column> convertRequiredAttributesToColumns(
            Collection<ObjectId> requiredAttributeIds) {
        Collection<Column> columns = new ArrayList<Column>();

        Collection<ObjectId> attributeIds;
        if (requiredAttributeIds == null) {
            attributeIds = new ArrayList<ObjectId>(2);
        } else {
            attributeIds = new ArrayList<ObjectId>(requiredAttributeIds);
        }
        attributeIds.add(Card.ATTR_TEMPLATE);
        attributeIds.add(Card.ATTR_STATE);
        for (ObjectId attribute : attributeIds) {
            Column col = new Column();
            col.setAttributeId(attribute);
            columns.add(col);
        }
        return columns;
    }

    public static Collection<Card> fetchCards(DataServiceFacade serviceBean,
            ObjectId[] cardIds, Collection<ObjectId> requiredAttributeIds) throws DMSIException {
        Search search = new Search();
        search.setByCode(true);
        search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(Arrays
                .asList(cardIds)));
        return searchCards(serviceBean, search, requiredAttributeIds);
    }

    public static Collection<ObjectId> getPersonCards(
    		DataServiceFacade serviceBean, Collection<Person> persons) {
        Collection<ObjectId> cardIds = new HashSet<ObjectId>();
        if (persons == null) {
            logger.warn("Persons collection is null. Return empty collection");
            return cardIds;
        }

        for (Person person : persons) {
            if (person.getCardId() == null) {
                try {
                    Person reloadedPerson = (Person) serviceBean.getById(person
                            .getId());
                    cardIds.add(reloadedPerson.getCardId());
                } catch (DataException ex) {
                    logger.error("Error during person fetch with id "
                            + person.getId(), ex);
                }
            } else {
                cardIds.add(person.getCardId());
            }
        }
        return cardIds;
    }

    public static Collection<Person> getPersonsByCards(
    		DataServiceFacade serviceBean, ObjectId[] cardIds) {

        if (cardIds == null) {
            logger.warn("Cards collection is null. Return empty collection");
            return new ArrayList<Person>(0);
        }

        try {
            return serviceBean.filter(Person.class, new PersonCardIdFilter(
                    Arrays.asList(cardIds)));
        } catch (DataException ex) {
            logger.error("Error during get persons by cards", ex);
        }

        return new ArrayList<Person>(0);
    }

    @SuppressWarnings("unchecked")
    public static Collection<Card> getBackLinkedCards(
    		DataServiceFacade serviceBean, ObjectId cardId, ObjectId attributeId,
            ObjectId... requiredAttributes) {
        ListProject fetcher = new ListProject();
        fetcher.setAttribute(attributeId);
        fetcher.setCard(cardId);
        fetcher.setColumns(convertRequiredAttributesToColumns(Arrays
                .asList(requiredAttributes)));

        try {
            SearchResult result = (SearchResult) serviceBean.doAction(fetcher);
            return result.getCards();
        } catch (DataException ex) {
            logger.error("Error during cards searching", ex);
        }
        return new ArrayList<Card>(0);
    }

    public static Material getMaterial(DataServiceFacade serviceBean,
            ObjectId cardId) {
        DownloadFile downloadFile = new DownloadFile();
        downloadFile.setCardId(cardId);
        try {
            return (Material) serviceBean.doAction(downloadFile);
        } catch (DataException ex) {
            throw new IllegalStateException(
                    "Error during downloading of material", ex);
        }
    }

    public static byte[] getMaterialData(Material material) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            outStream = new ByteArrayOutputStream();
            InputStream inStream = material.getData();

            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, read);
            }
            outStream.flush();
        } catch (IOException ex) {
            logger.error("Error during reading file", ex);
        }
        return outStream.toByteArray();
    }

    public static void uploadMaterial(DataServiceFacade serviceBean,
            ObjectId cardId, Material material) {
        String name = material.getName();
        if (name == null || name.trim().length() == 0) {
            logger.warn("File name is not specified. Exit.");
            return;
        }
        if (material.getData() == null) {
            logger.warn("No data. Exit");
            return;
        }
        UploadFile uploadFile = new UploadFile();
        uploadFile.setCardId(cardId);
        uploadFile.setFileName(name);
        uploadFile.setData(material.getData());
        try {
            serviceBean.doAction(uploadFile);
        } catch (DataException ex) {
            logger.error(
                    "Error during uploading of material to card " + cardId, ex);
        }
    }

    public static InputStream readConfig(String fileName) throws IOException {
        InputStream sourceStream = null;
        try {
            sourceStream = Portal.getFactory().getConfigService()
                    .loadConfigFile(
                            String.format("%s/%s", CONFIG_DIR, fileName));
            return new ByteArrayInputStream(IOUtils.toByteArray(sourceStream));
        } finally {
            IOUtils.closeQuietly(sourceStream);
        }
    }

    public static Document readXmlConfig(String fileName) throws DMSIException {
        try {
            InputStream stream = readConfig(fileName);
            return XmlUtils.parseDocument(stream);
        } catch (XMLException ex) {
            throw new DMSIException(ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new DMSIException(ex.getMessage(), ex);
        }
    }

    public static byte[] readFile(File file) throws IOException {
        InputStream fileStream = null;
        try {
            fileStream = new FileInputStream(file);
            return IOUtils.toByteArray(fileStream);
        } finally {
            IOUtils.closeQuietly(fileStream);
        }
    }

    public static void setErrorMessage(String message,
            String description_message) {
        try {
            EventLog eventLog = EventLog.getInstance();
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setMessage(message);
            errorMessage.setDescriptionMessage(description_message);
            errorMessage.isSucces(0L);
            errorMessage.setTimestamp(new Date());
            eventLog.getEvent().add(errorMessage);
        } catch (Exception e) {
            logger.error("Error in eventlog procedure (ErrorMessage) !", e);
        }
    }

    public static void setWarningMessage(String message,
            String description_message) {
        try {
            EventLog eventLog = EventLog.getInstance();
            WarningMessage warningMessage = new WarningMessage();
            warningMessage.setMessage(message);
            warningMessage.setDescriptionMessage(description_message);
            warningMessage.isSucces(1L);
            warningMessage.setTimestamp(new Date());
            eventLog.getEvent().add(warningMessage);
        } catch (Exception e) {
            logger.error("Error in eventlog procedure (WarningMessage) !", e);
        }
    }

    public static void setInfoMessage(String message, String description_message) {
        try {
            EventLog eventLog = EventLog.getInstance();
            InfoMessage infoMessage = new InfoMessage();
            infoMessage.setMessage(message);
            infoMessage.setDescriptionMessage(description_message);
            infoMessage.isSucces(1L);
            infoMessage.setTimestamp(new Date());
            eventLog.getEvent().add(infoMessage);
        } catch (Exception e) {
            logger.error("Error in eventlog procedure (InfoMessage) !", e);
        }
    }

    public static void clearEventLog() {
        try {
            EventLog eventLog = EventLog.getInstance();
            eventLog.getEvent().clear();
        } catch (Exception e) {
            logger.error("Error while cleaning up the queue event !", e);
        }
    }

    public static void setLogIntoDatabase(BeanFactory beanFactory, long cardId,
            UserData user, String event) {
        if (logger.isWarnEnabled()) {
            try {
                if (!beanFactory.containsBean(LogEventBean.BEAN_ID))
                    throw new DataException("Not found bean LogEventBean: "
                            + LogEventBean.BEAN_ID + " !");
                LogEventBean logEventBean = (LogEventBean) beanFactory
                        .getBean(LogEventBean.BEAN_ID);
                ObjectId card_id = new ObjectId(Card.class, cardId);
                EventLog eventLog = EventLog.getInstance();
                String adress = user.getAddress() == null ? "internal" : user
                        .getAddress();
                Person person = user.getPerson() == null ? (Person) DataObject
                        .createFromId(Person.ID_SYSTEM) : user.getPerson();
                for (LogEntry eventMessage : eventLog.getEvent()) {
                    eventMessage.setEvent(event);
                    eventMessage.setObject(card_id);
                    eventMessage.setAddress(adress);
                    eventMessage.setUser(person);
                    logEventBean.logEventExt(user, eventMessage);
                }
                eventLog.getEvent().clear();
            } catch (Exception e) {
                logger.error("Error when entering events into the database !",
                        e);
            }
        }
    }

    public static String getSearchDescription(Search search) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        search.storeToXml(stream);
        return new String(stream.toByteArray());
    }

}
