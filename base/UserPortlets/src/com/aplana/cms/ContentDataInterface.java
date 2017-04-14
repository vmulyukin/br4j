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
package com.aplana.cms;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;

import com.aplana.cms.view_template.CardViewData;
import com.aplana.cms.view_template.ColumnSortAttributes;
import com.aplana.dbmi.action.Action;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.*;

import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;


/**
 * Represents content data service  
 * It provides functions for getting content attribute data 
 *
 */
public interface ContentDataInterface {
	
	
	
	/**
	 * Returns all card attributes by given identifier
	 * 
	 * @param cardId card identifier
	 * 
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public Card getCardById(ObjectId cardId) throws DataException, ServiceException;
	
	
	
	/**
	 * Returns passed attributes for given card identifier
	 * 
	 * @param cardId card identifier
	 * @param attributes collection of attributes  
	 *
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public Card getCardAttributesById(ObjectId cardId, Collection<Attribute> attributes)  throws DataException, ServiceException;	

	
	/**
	 *  
	 * Returns linked cards for given card link attribute.
	 * It takes linked identifiers from passed attribute.
	 *  
	 * @param areaId area identifier it serves to decide where we are...at which area we are working
	 * @param attr {@link CardLinkAttribute}
	 * @param filter {@link Search}
	 * 
	 */
	public Collection<Card> getLinkedCards(Card card, LinkAttribute attr, Search filter, ObjectId areaId, ObjectId viewId) throws DataException, ServiceException;
	
	/**
	 * Returns cards referenced to given card via passed back link card attribute.
	 *    
	 * @param card {@link Card}
	 * @param attr {@link BackLinkAttribute}
	 * 
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public Collection<Card> getLinkedCards(Card card, BackLinkAttribute attr, ObjectId viewId) throws DataException, ServiceException;
	
	
	/**
	 * Returns linked cards for given card link attribute.
	 * It takes linked identifiers from passed attribute. 
 	 *	
	 * @param card {@link Card}
	 * @param attr {@link TypedCardLinkAttribute}
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public Collection<Card> getLinkedCards(Card card, TypedCardLinkAttribute attr, ObjectId viewId) throws DataException, ServiceException;
	
	
	/**
	 * Returns linked cards for given person link attribute.
	 * It takes linked identifiers from passed attribute. 
	 * 
	 * @param filter {@link Search}
	 * @param filter {@link PersonAttribute}
 
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public Collection<Card> getLinkedCards(PersonAttribute attr, Search filter, ObjectId viewId) throws DataException, ServiceException;
	
	
	/**
	 * Returns children objects of given type for passed card identifier
	 * @param id card identifier
	 * @param type child's type
	 * 
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public <T extends DataObject> Collection<T> getChildren(ObjectId id, Class<T> type) throws DataException, ServiceException;


    public Collection<Template> allTemplates();

    public boolean canDo(Action<?> action) throws DataException, ServiceException;

    public boolean canChange(ObjectId objectId) throws DataException, ServiceException;

    public <T> T doAction(Action<T> action) throws DataException, ServiceException;
    
    
	/**
	 * Returns area documents by given area identifier 
	 * @param areaId area identifier
	 */
	public List listAreaDocuments(ObjectId areaId) throws Exception;
 
    	
	/**
	 * Generic method for getting data for specific folder with cards
	 * @param folderId folder(area) identifier
	 * @param search
	 * @param page page number
	 * @param pageSize page number 
	 */
	public List getDocumentsByFolder(ObjectId folderId, String simpleSearchFilter, Search search, int page, int pageSize, String sortColumnId, Boolean isStraightOrder) throws Exception;

	
	/**
	 * Returns documents quantity for given folder
	 * @param folderId folder(area) identifier
	 * @param search
	 */	
	public FolderDocumentsQuantities getDocumentsQtyByFolder(ObjectId folderId, String simpleSearchFilter, Search search) throws Exception;

	
	/**
	 * Returns linked card by given found card
	 * @param areaId area identifier
	 * @param found  {@link Card}
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public Card getLinkedCardByFound(ObjectId areaId, ObjectId viewId, Card found) throws ServiceException, DataException;
	
	/**
	 * Returns CardLinkAttribute from given card by given attribute code
	 * @param attrField - attribute field name
	 * @param attrCode attribute linked code
     *
	 * @param card {@link Card}
	 * @param areaId area identifier
	 * @param viewId view identifier
	 */
	public LinkAttribute getCardLinkAttribute(String attrField, String attrCode, Card card);

    /**
     * returns card by given id
     * the only attributes defined by viewId will be returned
     * @param cardIdAndTemplate
     * @param view
     * @return
     */
    public Card getCardPresentationByViewId(Card cardIdAndTemplate, CardViewData view) throws DataException, ServiceException;
    
    /**
     * returns card by given id
     * the only attributes defined by viewId will be returned
     * @param cardIdAndTemplate
     * @param view
     * @param cardAccess
     * @return
     */
    public Card getCardPresentationByViewId(Card cardIdAndTemplate, CardViewData view, Long cardAccess) throws DataException, ServiceException;

    /**
     * read templateId for given CardId
     * @param card
     * @return
     * @throws DataException
     * @throws ServiceException
     */
    public void readCardTemplateIdAndStatusId(Card card) throws DataException, ServiceException;

    
	/**
	 * Returns default content by given Search attribute.    
	 * It returns first card from cards taken by given search parameters  
	 * 
	 * @param areaId area identifier
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public Card getDefaultContentBySearch(Search search, ObjectId areaId, String simpleSearchFilter, String sortColumnId, Boolean straightOrder)
			throws DataException, ServiceException;
	
	/**
	 * @param card
	 * @param attr  
	 * @param names filter attributes names
	 * @param values filter attributes values
	 * @return single liked card found using filter
	 */
	public Card findSingleLinkedCard(Card card, LinkAttribute attr, String[] names, String[] values);


    public boolean doesUserHavePermissions(Card card)
			throws DataException, ServiceException;

    /**
     * detects by filter type ability to read by new approach
     * @param filter
     * @return
     */
    public boolean canBeProcessedWithNewApproach(Search filter);
    
    /**
     * Initializes Search object by given Xml string
     * @return {@link Search}
     * @throws DataException if there any exceptions during xml parsing
     * @throws UnsupportedEncodingException
     */
    public Search initFromXml(String xml) throws DataException , UnsupportedEncodingException;
    
    public List<ColumnSortAttributes> getFolderSortColumns(ObjectId folderId);
}
