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
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ObjectId;

import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

/**
 * Represents Delegate for Content Producer View configuration service
 * It delegates all calls to legacy Content producer  
 *
 */
public class ContentProducerViewServiceDelegate implements ContentViewConfigInterface, ContentProducerSetterInterface{

	
	private ContentProducer contentProducer = null;
	
	
	
	public ContentProducerViewServiceDelegate() {

	}
	

	public void setContentProducer(ContentProducer contentProducer) {
		this.contentProducer = contentProducer;
	}




	public ObjectId findAreaIdByPortalPageName(String pageId, String navigator) throws DataException,
			ServiceException {
		
		return contentProducer.findAreaIdByPortalPageName(pageId, navigator);
		
	}

	public Card getCardById(ObjectId cardId) throws DataException,
			ServiceException {

		return contentProducer.getViewCardById(cardId);
		
	}
	
	public Card getViewCardById(ObjectId id) throws DataException, ServiceException {
		
		return contentProducer.getViewCardById(id);
		
	}

	public Card getContent(ObjectId contentId) throws DataException,
			ServiceException {
		
		return contentProducer.getContent(contentId);

	}

	public String getContentPresentation(ObjectId viewId) {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectId getDefaultContent(Card area) throws DataException,
			ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Card> getDefaultViews(String viewIds) throws DataException, ServiceException {
		return contentProducer.getDefaultViews(viewIds);
	}

	public Card getNavigatorById(ObjectId navigatorId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Card getSiteArea(ObjectId areaId) throws DataException,
			ServiceException {

		return contentProducer.getSiteArea(areaId);
		
	}


    public List<Card> getChildren(ObjectId areaId){
        return contentProducer.getChildrenCards(areaId);
    }


	public Collection<Card> getLinkedCards(Card card, LinkAttribute attr,
			Search filter, ObjectId areaId) throws DataException,
			ServiceException {
		
		return contentProducer.getLinkedCards(attr, filter);
	}

}
