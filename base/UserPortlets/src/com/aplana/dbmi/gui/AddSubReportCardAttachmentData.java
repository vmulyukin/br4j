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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.RenderRequest;

import com.aplana.dbmi.action.BulkFetchChildrenCards;
import com.aplana.dbmi.action.GetDeepChildren;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * Provides data for getting subordinate report card attachments
 * 
 * 
 * @author skashanski
 * 
 */
public class AddSubReportCardAttachmentData extends FilteredCards {
	
	private static final int depth = 20;
	
	private CardLinkAttribute attribute;

	private Card card;
	
	public static String SUB_REPORT_SEARCH_TYPE = "cardAttachmentSearchType";
	
	private static final String JSP_SUB_REPORT_SEARCH_PATH = "/WEB-INF/jsp/html/SubReportCardSearchForm.jsp";
	
	public static final String FIELD_BY_SUB_REPORT = "cardAttachmentSubReportSearch";
	public static final String FIELD_BY_ALL_ATTACHMENTS = "cardAllAttachmentSearch";
	
	/**
	 * flag to indicate what search type we are using : search by subordinate report attachments or search by all attachments
	 */
	private boolean searchBySubReports = true;
	
	// backlinkattribute.jbr.linkedResolutions=JBR_RIMP_RELASSIG = "��������� ���������"
	static final ObjectId linkedAttrId = ObjectId.predefined(BackLinkAttribute.class, "jbr.linkedResolutions");
	
	/*
	 * "�� ���������" � �� ����� �� ���������� (1044)
	 */
	public static final ObjectId INT_REPORT_RESOLUTION_ATTR_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.report.int.parent");
	
	/*
	 * "�� ���������" � �� ����� �� ���������� ������� ������������ (1064)
	 */
	public static final ObjectId EXT_REPORT_RESOLUTION_ATTR_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.report.ext.parent");
	
	/*
	 * "����� �� ���������� ������� ������������ (1064)"
	 */
	public static final ObjectId REPORT_EXTERNAL_TEMPLATE = ObjectId.template("jbr.report.external");
	
	/** related attached files attribute */
	public static final ObjectId ID_ATTR_REPORT_FILES = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.report.attachments");

	
	/** related attached files attribute */
	public static final ObjectId ID_ATTR_FILES = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.files");
	
	// backlinkattribute.jbr.reports=JBR_RIMP_REPORT = "����� �� ����������"
	final static ObjectId reportsAttrId = ObjectId.predefined(
			BackLinkAttribute.class, "jbr.reports");
	
	public AddSubReportCardAttachmentData(DataServiceBean service,
			Search filter, String title) throws DataException, ServiceException {
		super(service, filter, title);
	}

	public AddSubReportCardAttachmentData(Search searchFilter, Card card, CardLinkAttribute attribute, DataServiceBean service)
			throws DataException, ServiceException {
		
		super(service);

		this.attribute = attribute;

		this.card = card;

		//don't display ID column in ListForm 
		setAddIdColumn(false); 

		initializeFilter( searchFilter);

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
		
		String subReportSearchtype = request.getParameter(SUB_REPORT_SEARCH_TYPE);
		
		if (FIELD_BY_SUB_REPORT.equalsIgnoreCase(subReportSearchtype)) {
			searchBySubReports = true;
		} else {
			searchBySubReports = false;
		}

	}
	
	
	
	@Override
	protected SearchResult doSearch()
			throws DataException, ServiceException 
	{

		final Search filter = getFilter();
		Search cloneFilter = filter.makeCopy(); 
		
		if (searchBySubReports) {

			cloneFilter.setByCode(true);

			/* ����������� ����� byAttr ����� ���������� ��������, ���� byAttr � byCode �������� 
			 * ��� ����������������� � ��� ������� ������������� � ����� setByCode, �� ���������� 
			 * ������ byAttr � byCode ������������ �������� � ExecSearchByAttributes, ������� 
			 * ���������� ������ ����� byAttr = true, �� � ��� ����������� �������� byCode
			 */
			cloneFilter.setByAttributes(false);

			List<ObjectId> cardsIds = buildListDataIds(card);
			String cardIdStr = ObjectIdUtils.numericIdsToCommaDelimitedString(cardsIds);
			cloneFilter.setWords(cardIdStr);
		} else {
			cloneFilter.setByAttributes(true);
		}
			

		return (SearchResult) service.doAction(cloneFilter);

	}
	
	@SuppressWarnings("unchecked")
	Card execListProject( ObjectId cardId, ObjectId backLinkAttrId) throws ServiceException, DataException
	{
		final ListProject action = new ListProject();
		action.setCard( cardId);
		action.setAttribute( backLinkAttrId);

		final SearchResult rs = (SearchResult) service.doAction(action);
		if (rs == null) return null;

		final List<Card> cards = rs.getCards();
		return (cards == null || cards.isEmpty()) ? null : cards.get(0);
	}
	
	
	
	protected List<ObjectId> buildListDataIds(Card card) throws DataException,
			ServiceException {
		
		final List<ObjectId> result = new ArrayList<ObjectId>();
		
		
		//firstly returns parent resolution
		ObjectId parentResolutionAttrId = REPORT_EXTERNAL_TEMPLATE.getId().equals(card.getTemplate().getId()) ?
				  EXT_REPORT_RESOLUTION_ATTR_ID
				: INT_REPORT_RESOLUTION_ATTR_ID;
		CardLinkAttribute resol = card.getCardLinkAttributeById(parentResolutionAttrId);
		Card parentResolution = (Card) service.getById(resol.getIdsLinked().get(0));
        
		Collection<ObjectId> linkedResolutionIds = loadDeepChildren(
				linkedAttrId, Collections.singletonList(parentResolution.getId()));

		//��������� ������� ��������� � ������
		linkedResolutionIds.add(parentResolution.getId());
		/*
		 * ��������� ��� ����������� ������ �������� ��������
		 * ("������ �� ����������")...
		 */
		Map<ObjectId, List<Card>> reports = loadReportsInfo(reportsAttrId,
				linkedResolutionIds, ID_ATTR_REPORT_FILES, ID_ATTR_FILES);
		
		
		result.addAll(getReportsAttachedFilesIds(reports.values()));
		
		result.addAll(reports.keySet());
		
		//������� �� ������ �������� ������� ��������
		result.removeAll(attribute.getIdsLinked());
		
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
	

	@Override
	public void initSearchForm(RenderRequest request) {

		super.initSearchForm(request);
		
		if (searchBySubReports)
			request.setAttribute(FIELD_BY_SUB_REPORT, "true");
	}

	@Override
	public String getFormJspPath() {
		
		return JSP_SUB_REPORT_SEARCH_PATH;
	}

	@Override
	public void processSearch(ActionRequest request) {
		
		super.processSearch(request);
			
	}
	
	
	
	

}
