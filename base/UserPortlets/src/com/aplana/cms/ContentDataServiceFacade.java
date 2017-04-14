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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.aplana.cms.tags.ListTag;
import com.aplana.cms.view_template.CardViewData;
import com.aplana.cms.view_template.ColumnSortAttributes;
import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.DataAccessException;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents Facade for CMS data service providers
 * It delegates calls to specific content data/view configuration service provider.
 *
 */
public class ContentDataServiceFacade {

    protected Log logger = LogFactory.getLog(getClass());

	public static String[] cardTemplates = {"348",// template.jbr.visa
                                            "347",// template.jbr.index
											"346",// template.jbr.journal
											"224",// template.jbr.incoming
											"364",// template.jbr.outcoming
											"324",// template.jbr.resolution
											"10",//  template.jbr.internalPerson
											"464",// template.jbr.externalPerson
											"284",// template.jbr.file
											"222",// template.jbr.organization
											"484",// template.jbr.department
											"424",// template.jbr.report
											"1044",// template.jbr.report.internal
											"1064",// template.jbr.report.external
											"524",// template.jbr.inform_incoming
											"345",// template.jbr.storagePlace
											"684",// template.jbr.foiv
											"704",// template.jbr.DistributionListElement
											"764",// template.jbr.ord
											"784",// template.jbr.interndoc
											"865",// template.jbr.infreq
											"775",// template.jbr.infreq.answer
											"864",// template.jbr.incomingpeople
											"777",// template.jbr.incomingpeople.answer
											"865",// template.jbr.informationrequest
											"924",// template.jbr.authtypeitem
											"1224",// template.jbr.ProcessingDistribution
											"2244",// template.jbr.SendInfo
											"2264",// template.jbr.ImportedDocument
											"964",// template.med.resolutionAccept
											"1025",// template.med.documentRegister
											"1025",// template.med.documentRegister
                                            "1226",// template.jbr.npa
                                            "1255",// template.jbr.independent.resolution
											};


	private ContentDataInterface contentDataService;
	private ContentViewConfigInterface contentViewService;

	private ContentProducer contetProducer;


	public ContentDataServiceFacade() {

	}

	public void setContetProducer(ContentProducer contetProducer) {
		this.contetProducer = contetProducer;


		initContentProducerSetter(contentDataService);
		initContentProducerSetter(contentViewService);

	}

	/**
	 * Checks if given template is from Card data
	 */
	private boolean isDataTemplate(ObjectId templateId) {

		Long templateIdLong = (Long)templateId.getId();

		for(int i = 0; i < cardTemplates.length; i++) {
			String templateIdCode = cardTemplates[i];
			if (templateIdCode.equals(templateIdLong.toString()))
				return true;
		}
		return false;

	}

    private void initContentProducerSetter(Object service) {

		if (service instanceof ContentProducerSetterInterface)
			((ContentProducerSetterInterface)service).setContentProducer(contetProducer);
	}


	public void setContentDataService(ContentDataInterface contentDataService) {
		this.contentDataService = contentDataService;
	}


	public void setContentViewService(ContentViewConfigInterface contentViewService) {
		this.contentViewService = contentViewService;
	}



	public Collection<Card> getDefaultViews(String viewIds) throws DataException, ServiceException {

		return contentViewService.getDefaultViews(viewIds);

	}

	/**
	 * Could be used for getting content GUI and data attributes
	 * @TODO implement logic to check what kind of card do we need?
	 *
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public Card getSiteArea(ObjectId areaId) throws DataException, ServiceException {

		return contentViewService.getSiteArea(areaId);

	}


	public ObjectId findAreaIdByPageId(String pageId, String navigator) throws DataException, ServiceException {

		return contentViewService.findAreaIdByPortalPageName(pageId, navigator);

	}


	public Card getContent(ObjectId contentId) throws DataException, ServiceException {
        return contentViewService.getContent(contentId);
	}

	/**
	 * Returns configuration view card
	 */
	public Card getViewCardById(ObjectId cardId) throws DataException, ServiceException {

		return contentViewService.getCardById(cardId);
	}


	public List<Card> getChildren(ObjectId cardId) {

		return contentViewService.getChildren(cardId);

	}

    public List<ColumnSortAttributes> getFolderSortColumns(ObjectId folderId) {
        return contentDataService.getFolderSortColumns(folderId);
    }

	/**
	 * Generic method..It is used when it is not cleared what we want to return : configuration view card or Data card
	 * @TODO implement logic to check what kind of card do we need?
	 * Returns card attributes by given identifier
	 * @param cardId configuration view identifier
	 */
	public Card getCommonCardById(ObjectId cardId) throws DataException, ServiceException {

		return contentDataService.getCardById(cardId);

	}

	/**
	 * Returns all card data attributes by given identifier
	 *
	 * @param cardId card identifier
	 *
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public Card getDataCardById(ObjectId cardId) throws DataException, ServiceException {

		return contentDataService.getCardById(cardId);

	}


	/**
	 * Returns linked cards for given card link attribute.
	 * It is used when it is not cleared what we want to return : configuration view card or Data card
	 * @TODO implement logic to check what kind of card do we need?
	 * @param card {@link Card}
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public Collection<Card> getLinkedCards(Card card, LinkAttribute attr, Search filter, ObjectId areaId, ObjectId viewId) throws DataException, ServiceException {

		//String area = areaId.getId().toString();

		if (isDataTemplate(card.getTemplate())) {
			return contentDataService.getLinkedCards(card, attr, filter, areaId, viewId);
		} else
			return contentViewService.getLinkedCards(card, attr, filter, areaId);


	}

	/**
	 * Returns cards referenced to given card via passed back link card attribute.
	 * It is used when it is not cleared what we want to return : configuration view card or Data card
	 * @TODO implement logic to check what kind of card do we need?
	 *
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public Collection<Card> getLinkedCards(Card card, BackLinkAttribute attr, ObjectId viewId) throws DataException, ServiceException {

		return contentDataService.getLinkedCards(card, attr, viewId);

	}


	/**
	 * Returns linked cards for given card link attribute.
	 * It is used when it is not cleared what we want to return : configuration view card or Data card
	 * @TODO implement logic to check what kind of card do we need?
 	 *
	 * @param card {@link Card}
	 * @param attr {@link TypedCardLinkAttribute}
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public Collection<Card> getLinkedCards(Card card, TypedCardLinkAttribute attr, ObjectId viewId) throws DataException, ServiceException {

		return contentDataService.getLinkedCards(card, attr, viewId);

	}


	/**
	 * Returns linked cards for given card link attribute.
	 * It is used when it is not cleared what we want to return : configuration view card or Data card
	 * @TODO implement logic to check what kind of card do we need?
	 *
	 * @param filter {@link Search}
	 * @param filter {@link PersonAttribute}

	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public Collection<Card> getLinkedCards(PersonAttribute attr, Search filter, ObjectId viewId) throws DataException, ServiceException {

        Collection<Card> linkedCards = contentDataService.getLinkedCards(attr, filter, viewId);
        return linkedCards;

	}


	public <T extends DataObject> Collection<T> getChildren(ObjectId id, Class<T> type) throws DataException, ServiceException {

		return contentDataService.getChildren(id, type);
	}


    public Collection<Template> allTemplates() {

    	return contentDataService.allTemplates();

    }

    public boolean canDo(Action<?> action) throws DataException, ServiceException {

    	return contentDataService.canDo(action);

    }

    public boolean canChange(ObjectId objectId) throws DataException, ServiceException {

    	return contentDataService.canChange(objectId);
    }

    public <T> T doAction(Action<T> action) throws DataException, ServiceException {

    	return contentDataService.doAction(action);

    }


	/**
	 * Returns area documents by given area identifier
	 * @param areaId area identifier
	 */
	public List listAreaDocuments(ObjectId areaId) throws Exception {

		return contentDataService.listAreaDocuments(areaId);

	}


	/**
	 * Returns documents by given Search for passed folder
	 * @param search {@link Search}
	 * @param folderId folder(area) identifier
	 * @throws Exception
	 */
	public List getDocumentsByFolder(ObjectId folderId, String simpleSearchFilter, Search search, int page, int pageSize, String sortColumnId, Boolean straightOrder) throws Exception {

		return contentDataService.getDocumentsByFolder(folderId, simpleSearchFilter, search, page, pageSize, sortColumnId, straightOrder);

	}


	/**
	 * Returns documents quantity by given Search for passed folder
	 * @param search {@link Search}
	 * @param folderId folder(area) identifier
	 * @throws Exception
	 */
	public FolderDocumentsQuantities getDocumentsQtyByFolder(ObjectId folderId, String simpleSearchFilter, Search search) throws Exception {

		return contentDataService.getDocumentsQtyByFolder(folderId, simpleSearchFilter, search); 
    }

	public Card getLinkedCardByFound(ObjectId areaId, ObjectId viewId, Card found)
			throws ServiceException, DataException {

		return contentDataService.getLinkedCardByFound(areaId, viewId, found);

	}

	public LinkAttribute getCardLinkAttribute(String attrField, String attrCode, Card card) {

		return contentDataService.getCardLinkAttribute(attrField, attrCode, card);

	}

    public void readCardTemplateIdAndStatusId(Card card) throws DataException, ServiceException {
        contentDataService.readCardTemplateIdAndStatusId(card);
    }

    
	public Card getDefaultContentBySearch(Search search, ObjectId areaId, String simpleSearchFilter, String sortColumnId, Boolean straightOrder)
		throws DataException, ServiceException {
		
		return contentDataService.getDefaultContentBySearch(search, areaId, simpleSearchFilter, sortColumnId, straightOrder);
		
	}
    

    public Card getCardPresentationByViewId(Card cardIdAndTemplate, ObjectId viewId) throws DataException, ServiceException {
        if (isDataTemplate(cardIdAndTemplate.getTemplate())){
            if (viewId != null){
                CardViewData cardViewData = CardViewData.getBean(viewId.getId().toString());
                if (cardViewData != null) {
//                    logger.info("Reading attributes for card " + cardIdAndTemplate.getId().getId() + " by view " + cardViewData);
                    return contentDataService.getCardPresentationByViewId(cardIdAndTemplate, cardViewData);
                } else {
                    return getFullCard(cardIdAndTemplate, viewId);
                }
            } else {
                return getFullCard(cardIdAndTemplate, viewId);
            }
        }
        return contentViewService.getContent(cardIdAndTemplate.getId());

    }
    
    /**
     * reads Card by from list tag
     * depends on tag attributes
     * fetch only if no CardViewData bean defined or card retrieved using FILTER option
     * @param card to be filled
     * @param tag
     */
    public Card fetchCard(Card card, Tag tag, Search filter) throws DataException, ServiceException {
    	return fetchCard(card, tag, filter, CardAccess.READ_CARD);
    }

    /**
     * reads Card by from list tag
     * depends on tag attributes
     * fetch only if no CardViewData bean defined or card retrieved using FILTER option
     * @param card to be filled
     * @param tag
     */
    public Card fetchCard(Card card, Tag tag, Search filter, Long cardAccess) throws DataException, ServiceException {
        if (tag.hasAttribute(ListTag.ATTR_VIEW)){
            if (CardViewData.containsBean( tag.getAttribute(ListTag.ATTR_VIEW))){
				if (CardAccess.READ_CARD.equals(cardAccess)) {
					// check permissions
					if (!contentDataService.doesUserHavePermissions(card)) {
						// User doesn't have permissions to read card
						throw new DataAccessException("general.access");
					}

					// if linked cards were received by new methods, it should
					// contain all needed attributes
					if (contentDataService.canBeProcessedWithNewApproach(filter)) {
						return card;
					}
				}

                // reading attributes defined in CardViewData
                if (card.getTemplate() == null || card.getState() == null || card.getStateName() == null ){
                    readCardTemplateIdAndStatusId(card);
                }
                return contentDataService.getCardPresentationByViewId(card, CardViewData.getBean( tag.getAttribute(ListTag.ATTR_VIEW)), cardAccess);
            }
        }
        // MinMax tag doesn't have view
        else {
            // if linked cards were received by new methods, it should contain all needed attributes
            if (contentDataService.canBeProcessedWithNewApproach(filter)) {
                return card;
            }
        }
        // in any other cases use legacy approach
        return getCommonCardById(card.getId());
    }
    
    public Card findSingleLinkedCard(Card card, LinkAttribute attr, String[] names, String[] values) {
    	return contentDataService.findSingleLinkedCard(card, attr, names, values);
    }


    private Card getFullCard(Card cardIdAndTemplate, ObjectId viewId) throws DataException, ServiceException {
        logger.warn("No view bean found in ApplicationContext for viewId " + (viewId == null ? null : viewId.getId().toString()));
        logger.warn("Reading full card attributes for card " + cardIdAndTemplate.getId().getId());
        return contentDataService.getCardById(cardIdAndTemplate.getId());
    }
    
    public Search initFromXml(String xml) throws DataException , UnsupportedEncodingException {
    	
    	return contentDataService.initFromXml(xml);
    }

	public List<Card> getLinkedCards(Card card, ObjectId fieldAttr, ObjectId linkAttr, ObjectId itemAttr) throws DataException, ServiceException {
    	
    	LinkAttribute link = null;
		Collection<DataObject> attrs = card.getAttributes();
    	for(Iterator<DataObject> itr = attrs.iterator(); itr.hasNext();) {
			DataObject attr = itr.next();
			if (attr instanceof LinkAttribute && linkAttr.equals(attr.getId()) && ((LinkAttribute) attr).getLabelAttrId() == null)
				link = (LinkAttribute) attr;
		}
    	    	
    	if(link == null)
    		return new ArrayList<Card>();
    	
    	Search search = new Search();
    	search.setByCode(true);
    	search.setWords(link.getLinkedIds());
    	
    	List<SearchResult.Column> cols = new ArrayList<SearchResult.Column>();
		
		SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(Card.ATTR_TEMPLATE);
		cols.add(col);
		col = new SearchResult.Column();
		col.setAttributeId(Card.ATTR_STATE);
		cols.add(col);
		
		col = new SearchResult.Column();
		col.setAttributeId(fieldAttr);
		cols.add(col);
		
		col = new SearchResult.Column();
		col.setAttributeId(itemAttr);
		cols.add(col);
		
		search.setColumns(cols);
		
		SearchResult searchResult = (SearchResult) doAction(search);
		
    	return searchResult.getCards() != null ? searchResult.getCards() : new ArrayList<Card>();
		
    }
}

