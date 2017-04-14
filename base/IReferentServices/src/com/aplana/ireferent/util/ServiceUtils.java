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
package com.aplana.ireferent.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.CheckDelegatingReadAccess;
import com.aplana.dbmi.action.DownloadFile;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.SearchWithDelegatingAccess;
import com.aplana.dbmi.action.UploadFile;
import com.aplana.dbmi.action.Search.Filter;
import com.aplana.dbmi.action.SearchResult.Column;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.filter.PersonCardIdFilter;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortalPrincipal;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.UserPrincipal;
import com.aplana.ireferent.IReferentException;
import com.aplana.ireferent.types.WSOContext;

public final class ServiceUtils {

    protected static Log logger = LogFactory.getLog(ServiceUtils.class);

    private static final String CONFIG_DIR = "dbmi/ireferent";

    private ServiceUtils() {
    }

    public static DataServiceBean getServiceBean(String userId, WebServiceContext contextEndpoint)
	    throws IReferentException {

    	if (userId == null || "".equals(userId)) {
    		throw new IllegalArgumentException("User ID cannot be empty");
    	}
    	DataServiceBean serviceBean = createService(userId, getSessionId(contextEndpoint), 
    			getSessionCreationTime(contextEndpoint), getRemoteAddr(contextEndpoint) );
    	return serviceBean;
	}

	public static DataServiceBean getServiceBean(String userId, HttpServletRequest request)
		throws IReferentException
	{
		if (userId == null || "".equals(userId)) {
		    throw new IllegalArgumentException("User ID cannot be empty");
		}
		DataServiceBean serviceBean = createService(userId, request.getSession().getId(), 
				request.getSession().getCreationTime(), request.getRemoteAddr() );
		return serviceBean;
	}
	
	public static DataServiceBean createService(String userId, String sessionId, 
			long creationTime, String address ) throws IReferentException
	{
		DataServiceBean serviceBean = null;
		try {
		    Principal user = new PortalPrincipal(userId);
		    HelperServiceBean helperServiceBean = new HelperServiceBean(sessionId, creationTime);
		    helperServiceBean.setUser(user);
		    helperServiceBean.setHelperAddress(address);
		    helperServiceBean.setHelperService(helperServiceBean.getHelperService());
		    helperServiceBean.setHelperAsyncService(helperServiceBean.getHelperAsyncService());
		    helperServiceBean.setHelperIsDelegation(true);
		    helperServiceBean.setHelperRealUser(user);
		    serviceBean =  helperServiceBean;
		} catch (Exception ex) {
		    String msg = "";
		    if (null == serviceBean) {
			    msg = "Unable to init service bean. ";
			}
		    throw new IReferentException(msg + ex.getMessage(), ex);
		}

		return serviceBean;
	}

    public static DataServiceBean authenticateUser(WSOContext context, WebServiceContext contextEndpoint)
    	throws IReferentException 
    {
    	String userId = context.getUserId();
    	try {
    		DataServiceBean serviceBean;
    		serviceBean = getServiceBean(userId, contextEndpoint);
    		return serviceBean;
    	} catch (IReferentException ex) {
    		throw new IReferentException(
    				"Unable to authenticate user with such id: " + userId, ex);
    	}
    }
    
    public static DataServiceBean authenticateUser(String userId, HttpServletRequest request)
    	throws IReferentException 
	{
    	try {
    		DataServiceBean serviceBean;
    		serviceBean = getServiceBean(userId, request);
    		return serviceBean;
    	} catch (IReferentException ex) {
    		throw new IReferentException(
    				"Unable to authenticate user with such id: " + userId, ex);
    	}
	}

    public static DataServiceBean getServiceBean(Principal user, WebServiceContext contextEndpoint)
	    throws IReferentException {
	try {
	    DataServiceBean serviceBean = null;
		try {
		    HelperServiceBean helperServiceBean = new HelperServiceBean(getSessionId(contextEndpoint), getSessionCreationTime(contextEndpoint));
		    helperServiceBean.setHelperUser(user);
		    helperServiceBean.setHelperAddress(getRemoteAddr(contextEndpoint));
		    helperServiceBean.setHelperService(helperServiceBean.getHelperService());
		    helperServiceBean.setHelperAsyncService(helperServiceBean.getHelperAsyncService());
		    helperServiceBean.setHelperIsDelegation(true);
		    helperServiceBean.setHelperRealUser(user);//getSessionUser(contextEndpoint));
		    serviceBean =  helperServiceBean;
		    
		} catch (Exception ex) {
		    String msg = "";
		    if (null == serviceBean) {
			msg = "serviceBean == null. ";
		    }
		    throw new IReferentException(msg + ex.getMessage(), ex);
		}
	    return serviceBean;
	} catch (Exception ex) {
	    throw new IReferentException("Unable to init service bean.", ex);
	}
    }

    public static Collection<Card> searchCards(DataServiceBean serviceBean,
	    Search search, Collection<ObjectId> requiredAttributeIds) {

	Collection<Column> columns = new ArrayList<Column>();

	Collection<ObjectId> allRequiredAttributeIds = new ArrayList<ObjectId>();
	allRequiredAttributeIds.add(Card.ATTR_TEMPLATE);
	allRequiredAttributeIds.add(Card.ATTR_STATE);
	if (requiredAttributeIds != null) {
	    allRequiredAttributeIds.addAll(requiredAttributeIds);
	}

	for (ObjectId attribute : allRequiredAttributeIds) {
	    Column col = new Column();
	    col.setAttributeId(attribute);
	    columns.add(col);
	}
	search.setColumns(columns);
	// TODO ����� ���������� ���������� �������� ������������ ������ � ������.
	SearchWithDelegatingAccess searchWithDelegatingAccess =
		new SearchWithDelegatingAccess(search);

	try {
	    SearchResult result = (SearchResult) serviceBean.doAction(searchWithDelegatingAccess);
	    return result.getCards();
	} catch (DataException ex) {
	    logger.error("Error during cards searching", ex);
	} catch (ServiceException ex) {
	    logger.error("Error during cards searching", ex);
	}
	return new ArrayList<Card>(0);
    }

    public static Collection<Card> fetchCards(DataServiceBean serviceBean,
	    ObjectId[] cardIds, Collection<ObjectId> requiredAttributeIds) {
	Search search = new Search();
	search.setByCode(true);
	search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(Arrays
		.asList(cardIds)));
	return searchCards(serviceBean, search, requiredAttributeIds);
    }

    public static Collection<ObjectId> getPersonCards(
	    DataServiceBean serviceBean, Collection<Person> persons) {
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
		} catch (ServiceException ex) {
		    logger.error("Error during person fetch with id "
			    + person.getId(), ex);
		}
	    } else {
		cardIds.add(person.getCardId());
	    }
	}
	return cardIds;
    }

    @SuppressWarnings("unchecked")
    public static Collection<Person> getPersonsByCards(
	    DataServiceBean serviceBean, ObjectId[] cardIds) {

	if (cardIds == null) {
	    logger.warn("Cards collection is null. Return empty collection");
	    return new ArrayList<Person>(0);
	}

	try {
	    return serviceBean.filter(Person.class, new PersonCardIdFilter(
		    Arrays.asList(cardIds)));
	} catch (DataException ex) {
	    logger.error("Error during get persons by cards", ex);
	} catch (ServiceException ex) {
	    logger.error("Error during get persons by cards", ex);
	}

	return new ArrayList<Person>(0);
    }

    @SuppressWarnings("unchecked")
    public static Collection<ObjectId> getBackLinkedCards(
	    DataServiceBean serviceBean, ObjectId cardId, ObjectId attributeId) {
	ListProject fetcher = new ListProject();
	fetcher.setAttribute(attributeId);
	fetcher.setCard(cardId);

	try {
	    SearchResult result = (SearchResult) serviceBean.doAction(fetcher);
	    return ObjectIdUtils.collectionToSetOfIds(result.getCards());
	} catch (DataException ex) {
	    logger.error("Error during cards searching", ex);
	} catch (ServiceException ex) {
	    logger.error("Error during cards searching", ex);
	}
	return new ArrayList<ObjectId>(0);
    }

    public static Material getMaterial(DataServiceBean serviceBean,
	    ObjectId cardId) {
	DownloadFile downloadFile = new DownloadFile();
	downloadFile.setCardId(cardId);
	try {
	    return (Material) serviceBean.doAction(downloadFile);
	} catch (DataException ex) {
	    logger.error("Error during downloading of material", ex);
	    return null;
	} catch (ServiceException ex) {
	    logger.error("Error during downloading of material", ex);
	    return null;
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

    public static void uploadMaterial(DataServiceBean serviceBean,
	    ObjectId cardId, Material material) {
	String name = material.getName();
	if (name == null || name.trim().length() == 0) {
	    logger.warn("File name is not specified. Exit.");
	    return;
	}
	UploadFile uploadFile = new UploadFile();
	uploadFile.setCardId(cardId);
	uploadFile.setFileName(name);
	uploadFile.setData(material.getData());
	uploadFile.setLength(material.getLength());
	try {
	    serviceBean.doAction(uploadFile);
	} catch (DataException ex) {
	    logger.error("Error during downloading of material", ex);
	} catch (ServiceException ex) {
	    logger.error("Error during downloading of material", ex);

	}
    }

    public static String LongToCommaDelimitedString(HashMap<Long, Long> hmCards) {

	if (hmCards == null || hmCards.isEmpty())
	    return "";
	final StringBuffer result_key = new StringBuffer();
	final StringBuffer result_value = new StringBuffer();
	int i = hmCards.size();
	Collection<Long> ids = hmCards.keySet();
	Long val;
	for (Long id : ids) {
	    if (id == null) {
		i--;
		continue;
	    }
	    val = hmCards.get(id);
	    if (val == null) {
		i--;
		continue;
	    }
	    if ((i > 0) && (result_key.length() != 0)) {
		result_key.append(',');
	    }
	    if ((i > 0) && (result_value.length() != 0)) {
		result_value.append(',');
	    }
	    result_key.append(id.toString());
	    result_value.append(val.toString());
	    i--;
	}
	result_key.append('#');
	result_key.append(result_value);
	return result_key.toString();
    }

    @SuppressWarnings("unchecked")
    public static Collection<Card> getCards(DataServiceBean serviceBean,
	    String commaDelimitedCards, List<SearchResult.Column> columns)
	    throws IReferentException {

	final Search search_cards = new Search();
	search_cards.setWords(commaDelimitedCards);
	search_cards.setByCode(true);
	search_cards.setColumns(columns);
	try {
	    SearchResult cardsSR = (SearchResult) serviceBean
		    .doAction(search_cards);
	    Collection<Card> cards = cardsSR.getCards();
	    return cards;
	} catch (DataException ex) {
	    throw new IReferentException(
		    "com.aplana.ireferent.completion.cards.CompletionPersons.searchFailed",
		    ex);
	} catch (ServiceException ex) {
	    throw new IReferentException(
		    "com.aplana.ireferent.completion.cards.CompletionPersons.searchFailed",
		    ex);
	}
    }

    public static InputStream readConfig(String fileName) throws IOException {
	return Portal.getFactory().getConfigService().loadConfigFile(
		String.format("%s/%s", CONFIG_DIR, fileName));
    }

    public static Document readXmlConfig(String fileName)
	    throws IReferentException {
	try {
	    InputStream stream = readConfig(fileName);
	    return XmlUtils.parseDocument(stream);
	} catch (XMLException ex) {
	    throw new IReferentException(ex.getMessage(), ex);
	} catch (IOException ex) {
	    throw new IReferentException(ex.getMessage(), ex);
	}
    }
    
    public static String getSessionId(WebServiceContext contextEndpoint) {
	String result;
	if (null != contextEndpoint) {
	    HttpServletRequest  request =  ((HttpServletRequest)contextEndpoint.getMessageContext().get(MessageContext.SERVLET_REQUEST));
	    HttpSession session = request.getSession();
	    result = session.getId();
	} else {
	    result = UUID.randomUUID().toString();
	}
	return result;
    }
    
    public static long getSessionCreationTime(WebServiceContext contextEndpoint) {
	long result;
	if (null != contextEndpoint) {
	    HttpServletRequest  request =  ((HttpServletRequest)contextEndpoint.getMessageContext().get(MessageContext.SERVLET_REQUEST));
	    HttpSession session = request.getSession();
	    result = session.getCreationTime();
	} else {
	    result = new GregorianCalendar().getTimeInMillis();
	}
	return result;
    }
    
    public static Principal getSessionUser(WebServiceContext contextEndpoint) {
	Principal result = null;
	if (null != contextEndpoint) {
	    HttpServletRequest  request =  ((HttpServletRequest)contextEndpoint.getMessageContext().get(MessageContext.SERVLET_REQUEST));
	    result = request.getUserPrincipal(); //contextEndpoint.getUserPrincipal();
	}
	return result;
    }
    
    public static String getRemoteAddr(WebServiceContext contextEndpoint) {
	String result = null;
	if (null != contextEndpoint) {
	    HttpServletRequest  request =  ((HttpServletRequest)contextEndpoint.getMessageContext().get(MessageContext.SERVLET_REQUEST));
	    result = request.getRemoteAddr();
	}
	return result;
    }
    
    public static boolean isInteger(String val) {
    	if (val == null || val.trim().length() == 0) return false;
    	val = val.trim();
        int i = 0;
        if (val.charAt(0) == '-') {
           if (val.length() == 1) {
              return false;
           }
           i = 1;
        }

        char c;
        for (; i < val.length(); i++) {
            c = val.charAt(i);
            if (!(c >= '0' && c <= '9')) {
                return false;
            }
        }
        return true;
    }
}
