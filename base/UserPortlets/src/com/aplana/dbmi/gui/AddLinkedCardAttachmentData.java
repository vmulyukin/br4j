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
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.RenderRequest;

import com.aplana.dbmi.action.BulkFetchChildrenCards;
import com.aplana.dbmi.action.GetDeepChildren;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.Search.TextSearchConfigValue;
import com.aplana.dbmi.action.SearchResult.Column;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.model.util.SearchUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.web.tag.util.StringUtils;

/**
 * Provides data for getting linked card attachments,assignments, execution
 * reports
 * 
 * @author skashanski
 * 
 */
public class AddLinkedCardAttachmentData extends FilteredCards {

	private static final int depth = 20;

	private CardLinkAttribute attribute;

	private Card card;

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

	public AddLinkedCardAttachmentData(Card card, CardLinkAttribute attribute,
			DataServiceBean service) throws DataException, ServiceException {

		super(service);

		this.attribute = attribute;

		this.card = card;

		//don't display ID column in ListForm 
		setAddIdColumn(false); 

		initializeFilter( null);

		final SearchResult searchResult = doSearch();
		setCards( searchResult.getCards());

		initializeDisplayParameters(searchResult, attribute.getName());

		setSelectedList(new ArrayList(attribute.getIdsLinked()));

	}


	@Override
	protected void initializeFilter(Search afilter) throws DataException {
		if ( (attribute != null) && (afilter == null || getFilter() == afilter))
			super.setFilter( attribute.getFilter().makeCopy() );
		else
			super.setFilter( afilter);
	}

	@Override
	protected void initSearchParameters(ActionRequest request) {
		String fieldWords = request.getParameter(FIELD_WORDS);
		getFilter().addStringAttribute(Attribute.ID_NAME, fieldWords, Search.TextSearchConfigValue.CONTAINS);
	}


	@Override
	public void processSearch(ActionRequest request) {
		try {
			initSearchParameters(request);
			final SearchResult result = doSearch();
			// cards = result.getCards();
			setCards( result.getCards());
			titleList = result.getName();
		} catch (Exception e) {
			logger.error("Cards search error", e);
			request.setAttribute(ListEditor.ATTR_MESSAGE, e.getMessage());
		}
	}


	@Override
	public void initSearchForm(RenderRequest request) {

		final Search filter = getFilter();

		final TextSearchConfigValue materialSearchValue = (TextSearchConfigValue)filter.getAttribute(Attribute.ID_NAME);

		if ((materialSearchValue != null) && StringUtils.hasText(materialSearchValue.value))
			request.setAttribute(FIELD_WORDS, materialSearchValue.value);
		else 
			request.setAttribute(FIELD_WORDS, "");

		if (filter.isByCode())
			request.setAttribute(FIELD_BY_NUMBER, "true");

		if (filter.isByAttributes())
			request.setAttribute(FIELD_BY_ATTR, "true");

		if (filter.isByMaterial())
			request.setAttribute(FIELD_BY_TEXT, "true");
	}


	@Override
	protected boolean isLinked(Column column) {
		
		return column.isLinked() && Attribute.ID_MATERIAL.equals(column.getAttributeId());

	}


	@Override
	protected SearchResult doSearch()
			throws DataException, ServiceException 
	{

		final Search filter = getFilter();

		filter.setByCode(true);

		final List<ObjectId> cardsIds = buildListDataIds(card);

		final String cardIdStr = ObjectIdUtils.numericIdsToCommaDelimitedString(cardsIds);

		filter.setWords(cardIdStr);

		return (SearchResult) service.doAction(filter);

	}

	/**
	 * Builds list of Card's ObjectIds to search
	 */
	protected List<ObjectId> buildListDataIds(Card acard) throws DataException,
			ServiceException {

		final List<ObjectId> result = new ArrayList<ObjectId>();

		CardLinkAttribute relatDocs = acard
				.getCardLinkAttributeById(ID_ATTR_RELAT_DOCS);

		final Collection<ObjectId> relatDocsIds = relatDocs.getIdsLinked();

		for (ObjectId attachId : relatDocsIds) {

			Card attachmentCard = (Card) service.getById(attachId);

			CardLinkAttribute attachedFilesAttribute = attachmentCard
					.getCardLinkAttributeById(ID_ATTR_FILES);

			Collection<ObjectId> attachedFilesIds = getAttachedFilesIds(
					attachmentCard, ID_ATTR_FILES);
			result.addAll(attachedFilesIds);

			Collection<ObjectId> resolutionsIds = loadResolutions(attachmentCard);
			result.addAll(resolutionsIds);

		}


		//add selected cards
		result.addAll(attribute.getIdsLinked());

		return result;
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
			Card resolutionCard = (Card) service.getById(resolutionId);

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
	public void setSelectedList(List data) {
		super.setSelectedList(data);
		//stores selected ids in attribute
		attribute.setIdsLinked(data);
	}

}
