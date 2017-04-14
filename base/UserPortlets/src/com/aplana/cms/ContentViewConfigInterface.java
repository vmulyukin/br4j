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

import java.util.Collection;
import java.util.List;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

/**
 * Represents content view configuration  
 * It provides functions for getting content view configurations
 *  
 *
 */
public interface ContentViewConfigInterface {
	
	
	/**
	 * Returns site area configuration for given area 
	 * @param areaId area identifier
	 *   
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public Card getSiteArea(ObjectId areaId) throws DataException, ServiceException;
	
	

	/**
	 * Finds area's identifier by given Page identifier
	 * @param pageId page identifier
	 * @param navigator navigator name
	 * 
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
 
	 */
	public ObjectId findAreaIdByPortalPageName(String pageId, String navigator) throws DataException, ServiceException;	

	
	/**
	 * Returns content configuration by given content Identifier
	 * @param contentId content identifier
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 * 
	 */
	public Card getContent(ObjectId contentId) throws DataException, ServiceException;
	

	/**
	 * Returns default content identifier for given area
	 * 
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
 
	 * @TODO discuss if we need to split this method
	 *   
	 */
	public ObjectId getDefaultContent(Card area) throws DataException, ServiceException;
	
	
	/**
	 * Returns view's configurations for given views identifiers.
	 * 
	 * @param viewIds comma-delimited list of view identifiers
	 */
	public Collection<Card> getDefaultViews(String viewIds) throws DataException, ServiceException;
	
	
	
	/**
	 * Returns HTML content presentation for given view identifier 
	 * @param viewId view identifier
	 * 
	 * @TODO discuss if we need to split this method
	 */
	public String getContentPresentation(ObjectId viewId);
	
	
	/**
	 * Returns navigator by passed identifier
	 * @param navigatorId navigator Identifier
	 * @return
	 */
	public Card getNavigatorById(ObjectId navigatorId);
	
	
	
	/**
	 * Generic method
	 * Returns all card attributes by given identifier
	 * 
	 * @param cardId card identifier
	 * 
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public Card getCardById(ObjectId cardId) throws DataException, ServiceException;	
	
	
	/**
	 * Returns configuration view card 
	 * @param id configuration view identifier
	 */
	public Card getViewCardById(ObjectId id) throws DataException, ServiceException;
	
	

    public List<Card> getChildren(ObjectId areaId);
    
    
	public Collection<Card> getLinkedCards(Card card, LinkAttribute attr,
			Search filter, ObjectId areaId) throws DataException,
			ServiceException;

}
