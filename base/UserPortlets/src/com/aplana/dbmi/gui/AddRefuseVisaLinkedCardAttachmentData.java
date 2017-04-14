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
package com.aplana.dbmi.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.RenderRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.BulkFetchChildrenCards;
import com.aplana.dbmi.action.GetDeepChildren;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.MaterialAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.model.util.SearchUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class AddRefuseVisaLinkedCardAttachmentData extends FilteredCards {
	
	private static final int depth = 20;
	
	private Card card;
	
	private Map<ObjectId, String> attachedFiles;
	
	protected Log logger = LogFactory.getLog(getClass());
	
	
	/** related card link attribute */
	public static final ObjectId ID_ATTR_RELAT_DOCS = ObjectId.predefined(
			TypedCardLinkAttribute.class, "jbr.relatdocs");

	/** related attached files attribute */
	public static final ObjectId ID_ATTR_FILES = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.files");

	// backlinkattribute.jbr.resolutions=JBR_IMPL_RESOLUT = "���������"
	static final ObjectId rootAttrId = ObjectId.predefined(
			BackLinkAttribute.class, "jbr.resolutions");

	// backlinkattribute.jbr.linkedResolutions=JBR_RIMP_RELASSIG =
	// "��������� ���������"
	static final ObjectId linkedAttrId = ObjectId.predefined(
			BackLinkAttribute.class, "jbr.linkedResolutions");

	// backlinkattribute.jbr.reports=JBR_RIMP_REPORT = "����� �� ����������"
	final static ObjectId reportsAttrId = ObjectId.predefined(
			BackLinkAttribute.class, "jbr.reports");
	// external resolution report
	final static ObjectId reportExternalAttrId = ObjectId.predefined(
			BackLinkAttribute.class, "jbr.resolution.ExtReport");
	// external FYI report
	final static ObjectId reportFyiAttrId = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.resolution.FyiReport");

	/** related attached files attribute */
	public static final ObjectId ID_ATTR_REPORT_FILES = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.report.attachments");
	
	public static final ObjectId VISA_PARENT = ObjectId.predefined(BackLinkAttribute.class, "jbr.visa.parent");	
	
	
	private static final String JSP_VISA_LINKED_SEARCH_PATH = "/WEB-INF/jsp/html/RefuseVisaLinkedCardSearchForm.jsp";
	
	public static String ATTACHMENT_SEARCH_TYPE = "cardAttachmentSearchType";
	
	public static final String FIELD_BY_LINKED_CARDS = "cardAttachmentLinkedCardSearch";
	public static final String FIELD_BY_ALL_ATTACHMENTS = "cardAllAttachmentSearch";
	
	/**
	 * flag to indicate what search type we are using : search by linked card attachments or search by all attachments
	 */
	private boolean searchByLinkedCards = true;
	
	

	public AddRefuseVisaLinkedCardAttachmentData(DataServiceBean service,
			Search filter, String title) throws DataException, ServiceException {
		super(service, filter, title);
	}

	public AddRefuseVisaLinkedCardAttachmentData(Search searchFilter, Card card, DataServiceBean service, Map<ObjectId, String> attachedFiles)
			throws DataException, ServiceException {
		
		super(service);

		this.card = card;

		this.attachedFiles = attachedFiles;
		
		//don't display ID column in ListForm 
		setAddIdColumn(false); 

		setFilter( searchFilter);

		final SearchResult searchResult = doSearch();
		setCards( searchResult.getCards());

		initializeDisplayParameters(searchResult, null);
		
		setSelectedList(new ArrayList(attachedFiles.keySet()));


	}

	
	
	@Override
	protected void initSearchParameters(ActionRequest request) {
		String fieldWords = request.getParameter(FIELD_WORDS);
		getFilter().addStringAttribute(Attribute.ID_NAME, fieldWords, Search.TextSearchConfigValue.CONTAINS);
		
		String attachmentSearchType = request.getParameter(ATTACHMENT_SEARCH_TYPE);
		
		if (FIELD_BY_LINKED_CARDS.equalsIgnoreCase(attachmentSearchType)) {
			searchByLinkedCards = true;
		} else {
			searchByLinkedCards = false;
		}

	}
	
	
	
	@Override
	protected SearchResult doSearch()
			throws DataException, ServiceException 
	{

		final Search filter = getFilter();
		Search cloneFilter = filter.makeCopy(); 
		
		if (searchByLinkedCards) {

			cloneFilter.setByCode(true);
			List<ObjectId> cardsIds = buildListDataIds(card);
			String cardIdStr = ObjectIdUtils.numericIdsToCommaDelimitedString(cardsIds);
			cloneFilter.setWords(cardIdStr);
		} else {
			cloneFilter.setByAttributes(true);
		}
			

		return (SearchResult) service.doAction(cloneFilter);

	}
	
	/**
	 * Returns parent card for given visa card by BackLink attribute 
	 * @param card passed Visa card
	 * @return
	 */
	protected Card getParentCard(Card visa) throws DataException, ServiceException {

		Card result = null;
		
		final BackLinkAttribute documentBackLinkAttr = (BackLinkAttribute) visa.getAttributeById(VISA_PARENT);
		ListProject listAction = new ListProject();
		listAction.setCard(visa.getId());
		listAction.setAttribute(documentBackLinkAttr.getId());
		
		final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();

		SearchResult.Column relatDocsColumn = new SearchResult.Column();
		relatDocsColumn.setAttributeId(ID_ATTR_RELAT_DOCS);
		columns.add( relatDocsColumn);
		listAction.setColumns(columns);
		
		Object execResult = service.doAction(listAction);
		
		if (execResult instanceof SearchResult) {
			SearchResult searchResult = (SearchResult)execResult;
			//�������� �� ������� ���������� � ���������. ���� ��� �� ������ ����� ��������� ��������
			//��� ���������� ���������
			if (searchResult.getCards().size() > 0){
				result = (Card)searchResult.getCards().get(0);
			}	
		}	
		
		return result; 
		
	}
	
	
	
	/**
	 * Builds list of Card's ObjectIds to search
	 */
	protected List<ObjectId> buildListDataIds(Card visa) throws DataException,
			ServiceException {

		final List<ObjectId> result = new ArrayList<ObjectId>();
		
		if (visa == null)
			return result;

		CardLinkAttribute relatDocs = visa
				.getCardLinkAttributeById(ID_ATTR_RELAT_DOCS);
		
		if (relatDocs == null)
			return result; 

		final Collection<ObjectId> relatDocsIds = relatDocs.getIdsLinked();

		if (relatDocsIds == null || relatDocsIds.size() ==0)
			return result; 
		

		for (ObjectId attachId : relatDocsIds) {

			Card attachmentCard = getCardById(attachId);
			if (attachmentCard == null)
				continue;//skip card if we don't have enough permissions

			CardLinkAttribute attachedFilesAttribute = attachmentCard
					.getCardLinkAttributeById(ID_ATTR_FILES);

			Collection<ObjectId> attachedFilesIds = getAttachedFilesIds(
					attachmentCard, ID_ATTR_FILES);
			result.addAll(attachedFilesIds);

			Collection<ObjectId> resolutionsIds = loadResolutions(attachmentCard);
			result.addAll(resolutionsIds);

		}


		//add selected cards
		result.addAll(attachedFiles.keySet());

		return result;
	}

	private Card getCardById(ObjectId attachId)  {
		Card attachmentCard = null;
		try {
			attachmentCard = (Card) service.getById(attachId);
		} catch (ServiceException e) {
			logger.error(e);
		} catch (DataException e) {
			logger.error(e);
		}
		return attachmentCard;
	}

	private Collection<ObjectId> getAttachedFilesIds(Card acard,
			ObjectId idAttrAttachedFiles) {

		Collection<ObjectId> result = new ArrayList<ObjectId>();
		
		if (acard == null)
			return result;

		CardLinkAttribute cardAttachedFilesAttr = acard
				.getCardLinkAttributeById(idAttrAttachedFiles);

		if (cardAttachedFilesAttr != null
				&& cardAttachedFilesAttr.getIdsLinked() != null)
			result.addAll(cardAttachedFilesAttr.getIdsLinked());

		return result;

	}

	
	@Override
	public void setSelectedList(List data) {
		super.setSelectedList(data);
		
		//stores selected ids 
		attachedFiles.clear();
		
		Map<ObjectId, String> attachedFilesMap = new LinkedHashMap<ObjectId, String>();
		if (data != null) {
			Collection<ObjectId> attachedFileIds = (Collection<ObjectId>)data;
			for (ObjectId attachedFileId : attachedFileIds) {
				Card attachedCard = getCardById(attachedFileId);
				MaterialAttribute materialAttr = (MaterialAttribute) attachedCard.getAttributeById(Attribute.ID_MATERIAL);
				attachedFilesMap.put(attachedFileId, materialAttr.getMaterialName());
			}
		}
		
		attachedFiles.putAll(attachedFilesMap);
		
	}
	

	/**
	 * Gets Cards's resolutions ObjetctIds for passed card 
	 * @param card the card to get resolutions 
	 * @return Collection of {@link com.aplana.dbmi.model.ObjectId} of resolutions for given card
	 */
	protected Collection<ObjectId> loadResolutions(Card card)
			throws DataException, ServiceException {

		Collection<ObjectId> result = new ArrayList<ObjectId>();

		/*
		 * �������� ������ ("���������") ...
		 */
		final List<ObjectId> resolutionIdLinked = SearchUtils.getBackLinkedCardsObjectIds(card, rootAttrId, service);

		if (resolutionIdLinked == null)
			return result;
	
		result.addAll(getAttachedFilesIds(resolutionIdLinked));

		/*
		 * ��������� ��� ��������� ("��������� ���������")...
		 */
		Collection<ObjectId> linkedResolutionIds = loadDeepChildren(
				linkedAttrId, resolutionIdLinked);
		result.addAll(getAttachedFilesIds(linkedResolutionIds));
	
		linkedResolutionIds.addAll(resolutionIdLinked); 
	
		/*
		 * ��������� ��� ����������� ������ �������� ��������
		 * ("������ �� ����������")...
		 */
		Map<ObjectId, List<Card>> reports = loadReportsInfo(reportsAttrId,
				linkedResolutionIds, ID_ATTR_REPORT_FILES, ID_ATTR_FILES);

		result.addAll(getReportsAttachedFilesIds(reports.values()));

		Map<ObjectId, List<Card>> externalReports = loadReportsInfo(
				reportExternalAttrId, linkedResolutionIds, ID_ATTR_REPORT_FILES, ID_ATTR_FILES);

		result.addAll(getReportsAttachedFilesIds(externalReports.values()));

		Map<ObjectId, List<Card>> fyiReports = loadReportsInfo(reportFyiAttrId,
				linkedResolutionIds, ID_ATTR_REPORT_FILES, ID_ATTR_FILES);

		result.addAll(getReportsAttachedFilesIds(fyiReports.values()));

		return result;

	}

	private Collection<ObjectId> getAttachedFilesIds(
			Collection<ObjectId> resolutionIdLinked) throws DataException,
			ServiceException {

		Collection<ObjectId> result = new ArrayList<ObjectId>();

		for (ObjectId resolutionId : resolutionIdLinked) {
			Card resolutionCard = getCardById(resolutionId);

			Collection<ObjectId> resolutionAttachedFilesIds = getAttachedFilesIds(
					resolutionCard, ID_ATTR_FILES);
			result.addAll(resolutionAttachedFilesIds);

		}

		return result;
	}

	private Collection<ObjectId> getReportsAttachedFilesIds(
			Collection<List<Card>> fullReports) {

		final Collection<ObjectId> result = new ArrayList<ObjectId>();

		final Iterator<List<Card>> reportIterator = fullReports.iterator();
		while (reportIterator.hasNext()) {

			Collection<Card> reports = reportIterator.next();

			for (Card report : reports) {

				Collection<ObjectId> reportAttachedFilesIds1 = getAttachedFilesIds(
						report, ID_ATTR_FILES);
				result.addAll(reportAttachedFilesIds1);

				Collection<ObjectId> reportAttachedFilesIds2 = getAttachedFilesIds(
						report, ID_ATTR_REPORT_FILES);
				result.addAll(reportAttachedFilesIds2);
			}

		}

		return result;

	}

	/**
	 * �������� �� �� ���� ����� (�� ������� this.depth), ������� � ��������
	 * ��������, ������ �� �������� this.cres.
	 * 
	 * @param roots : ������ �������� ��������.
	 * @return ������ �����.
	 * @throws DataException
	 */
	@SuppressWarnings("unchecked")
	private Collection<ObjectId> loadDeepChildren(ObjectId attrId,
			final Collection<ObjectId> roots) throws DataException,
			ServiceException {
		GetDeepChildren action = new GetDeepChildren();

		action.setDepth(depth);
		action.setChildTypeId(attrId);
		action.setRoots(roots);

		return (Collection<ObjectId>) service.doAction(action);

	}

	@Override
	public void initSearchForm(RenderRequest request) {

		super.initSearchForm(request);
		
		if (searchByLinkedCards)
			request.setAttribute(FIELD_BY_LINKED_CARDS, "true");
	}

	@Override
	public String getFormJspPath() {
		
		return JSP_VISA_LINKED_SEARCH_PATH;
	}

	@Override
	public void processSearch(ActionRequest request) {
		
		super.processSearch(request);
			
	}
	
	
	
	

}

