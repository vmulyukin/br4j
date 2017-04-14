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

import java.io.ByteArrayInputStream;
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
import com.aplana.dbmi.service.ServiceLocator;
import com.aplana.dbmi.service.workstation.CommonCardDataServiceInterface;

/**
 * Represents Delegate for Content Producer Data service
 * It delegates all calls to legacy Content producer  
 *
 */
public class ContentProducerDataServiceDelegate implements ContentDataInterface, ContentProducerSetterInterface {
	
 
	private ContentProducer contentProducer = null;
	

	public ContentProducerDataServiceDelegate() {

	}
	
	

	public void setContentProducer(ContentProducer contentProducer) {
		
		this.contentProducer = contentProducer;
		
	}



	public Card getCardAttributesById(ObjectId cardId,
			Collection<Attribute> attributes) throws DataException,
			ServiceException {
 
		return null;
		
	}

	public Card getCardById(ObjectId cardId) throws DataException,
			ServiceException {

		return contentProducer.getCardById(cardId);
	}

	public Collection<Card> getLinkedCards(Card card, LinkAttribute attr, Search filter, ObjectId areaId, ObjectId viewId)
			throws DataException, ServiceException {

		return contentProducer.getLinkedCards(attr, filter);
	}

	public Collection<Card> getLinkedCards(Card card, BackLinkAttribute attr, ObjectId viewId)
			throws DataException, ServiceException {
		
		return contentProducer.getLinkedCards(card, attr);

	}

	public Collection<Card> getLinkedCards(Card card,
			TypedCardLinkAttribute attr, ObjectId viewId) throws DataException, ServiceException {
		
		return contentProducer.getLinkedCards(card, attr);
		
	}

	public Collection<Card> getLinkedCards(PersonAttribute attr, Search filter, ObjectId viewId)
			throws DataException, ServiceException {
        // legacy code works here
		return contentProducer.getLinkedCards(attr, filter);
	}

	public Collection<Card> getChildren(ObjectId id, Class type)
			throws DataException, ServiceException {

		return contentProducer.getChildren(id,type);
		
	}

    public Collection<Template> allTemplates(){
        return contentProducer.allTemplates();
    }

    public boolean canDo(Action action) throws DataException, ServiceException{
        return contentProducer.getService().canDo(action);
    }

    public boolean canChange(ObjectId objectId) throws DataException, ServiceException {
        return contentProducer.getService().canChange(objectId);
    }

    public <T> T doAction(Action<T> action) throws DataException, ServiceException {
        return contentProducer.getService().doAction(action);
    }

    
	public List listAreaDocuments(ObjectId areaId) throws Exception {
		
		return contentProducer.listAreaDocuments(areaId);
	}
    

	public List getDocumentsByFolder(ObjectId folderId, String simpleSearchFilter, Search search, int page, int pageSize, String sortColumnId, Boolean straightOrder) throws Exception {		
		return contentProducer.searchCards(search);		
	}


	public FolderDocumentsQuantities getDocumentsQtyByFolder(ObjectId folderId, String simpleSearchFilter, Search search) throws Exception {
		List cards =  contentProducer.searchCards(search);	
		return null == cards ? new FolderDocumentsQuantities() : new FolderDocumentsQuantities((long) cards.size());
	}



	public Card getLinkedCardByFound(ObjectId areaId, ObjectId viewId, Card found)
			throws ServiceException, DataException {

		return contentProducer.getLinkedCardByFound(found);
	}



	
	public LinkAttribute getCardLinkAttribute(String attrField, String attrCode, Card card) {

		return contentProducer.getCardLinkAttribute(attrCode, card);
	}

	public Card getCardPresentationByViewId(Card cardIdAndTemplate, CardViewData view) throws DataException, ServiceException {
		return getCardById(cardIdAndTemplate.getId());
	}

	public Card getCardPresentationByViewId(Card cardIdAndTemplate, CardViewData view, Long cardAccess) throws DataException, ServiceException {
		return getCardById(cardIdAndTemplate.getId());
	}

	public void readCardTemplateIdAndStatusId(Card card) throws DataException, ServiceException {
        CommonCardDataServiceInterface commonCardDataService =
            ServiceLocator.getInstance().getKernelService( CommonCardDataServiceInterface.NAME, CommonCardDataServiceInterface.class);

        long []templateStatusId = commonCardDataService.getCardTemplateIdAndStatusId(Long.valueOf(card.getId().getId().toString()));
        card.setTemplate(templateStatusId[0]);
        card.setState(new ObjectId( CardState.class, templateStatusId[1]));
    }


	public Card getDefaultContentBySearch(Search search, ObjectId areaId, String simpleSearchFilter, String sortColumnId, Boolean straightOrder)
			throws DataException, ServiceException {

		return contentProducer.getDefaultContentBySearch(search);
		
	}

	public boolean doesUserHavePermissions(Card card) throws DataException, ServiceException {
        // always true for legacy code, permissions check will be done during card reading
        return true;
    }

    public boolean canBeProcessedWithNewApproach(Search filter) {
        return false;
    }

	public Card findSingleLinkedCard(Card card, LinkAttribute attr, String[] names, String[] values) {
		return contentProducer.findSingleLinkedCard(card, attr, names, values);
	}
	
    public Search initFromXml(String xml) throws DataException , UnsupportedEncodingException {
    	
		final Search search = new Search();
		search.initFromXml(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    	return search;
    }
	
    public List<ColumnSortAttributes> getFolderSortColumns(ObjectId folderId) {
        return null;
    }
}
