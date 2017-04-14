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
package com.aplana.cms.tags;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.aplana.cms.ContentIds;
import com.aplana.cms.ContentProducer;
import com.aplana.cms.PagedList;
import com.aplana.cms.PagedListInterface;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.cms.Variable;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.card.util.CardAttrComparator;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardAccess;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.web.tag.util.StringUtils;

public class ListTag extends ListBase implements TagProcessor
{
	public static final String ATTR_VIEW = "view";
	public static final String ATTR_FETCH = "fetch";
	public static final String ATTR_VERIFY_ACCESS = "verifyaccess";
	public static final String ATTR_SORT_PARAMS = "sortparams";
	
	public static final String VAR_COUNT = "count";
	public static final String VAR_INDEX = "index";
	public static final String VAR_ITEM = "item";
	public static final String VAR_VIEW = "var_view";
	public static final String VAR_PREV = "prev";
	public static final String VAR_NEXT = "next";
	public static final String VAR_FIRST = "first";
	public static final String VAR_LAST = "last"; 
	public static final String VAR_PAGE = "page";
	public static final String VAR_TOTAL_PAGES = "pages";
	public static final String VAR_PAGE_SIZE = "page.size";
	public static final String VAR_PAGE_FIRST = "page.first";
	public static final String VAR_PAGE_LAST = "page.last";
	
	public static final String SORT_DELIMITER = ":";
	public static final String SORT_DESCENDING = "desc";
	
	private int current = -1;
	private boolean fetch = true;
	private boolean verifyAccess = true;
	
	private long cardsCount;
	private int pageSize;
	private int page;

    private ObjectId viewId = null;
    private Card currentContent = null;
    private ObjectId internalPersonTemplateId = ObjectId.predefined(Template.class, "jbr.internalPerson");
    
	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception
	{
		if (!tag.hasAttribute(ATTR_VIEW))
			throw new Exception("Mandatory attribute " + ATTR_VIEW + " not set");
		
        viewId = new ObjectId(Card.class,
                Long.parseLong(cms.expandContent(tag.getAttribute(ATTR_VIEW), item)));

        view = cms.getContentDataServiceFacade().getViewCardById(viewId);
		
		cms.setVariable(VAR_VIEW, view);
		
		if (tag.hasAttribute(ATTR_FETCH)) {
			fetch = Boolean.parseBoolean(tag.getAttribute(ATTR_FETCH));
		}
		
		initPageAttributes(cms);

		// fetching cards
		if (!super.prepareData(tag, item, cms)) {

            cms.clearVariable(VAR_VIEW);

			TextAttribute attr = (TextAttribute) view.getAttributeById(ContentIds.ATTR_HTML_EMPTY);
			//cms.writeContent(out, attr.getValue(), item);
			return attr.getValue() != null && attr.getValue().length() > 0;
		}
		
		if (pageSize != 0) {
			PagedListInterface pages = cms.getPagedList();
			pages.setList(cards);
			pages.setPageSize(pageSize);
			pages.setTotalCount(cardsCount);
				
			cms.getRequest().setAttribute(PagedList.ATTR_PAGES, pages);
			cards = pages.getPage(((Integer) page).intValue());
			cms.setVariable(VAR_PAGE, page);
			cms.setVariable(VAR_TOTAL_PAGES, new Integer(pages.totalPages()));
			cms.setVariable(VAR_PAGE_SIZE, new Integer(pages.pageSize()));
			cms.setVariable(VAR_PAGE_FIRST, new Integer(1));
			cms.setVariable(VAR_PAGE_LAST, new Integer(pages.totalPages()));
		}
		
		if (tag.hasAttribute(ATTR_SORT_PARAMS) && containCards()) {
			sortCards(tag.getAttribute(ATTR_SORT_PARAMS));
		}
		return true;
	}

	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms)
	{	
		if (cards == null || cards.size() == 0) {
			TextAttribute attr = (TextAttribute) view.getAttributeById(ContentIds.ATTR_HTML_EMPTY);
			cms.writeContent(out, attr.getValue(), item);
			return;
		}
		
		Object oldCount = cms.replaceVariable(VAR_COUNT, new Count());
		Object oldIndex = cms.replaceVariable(VAR_INDEX, new Index());
		Object oldItem = cms.replaceVariable(VAR_ITEM, new Item());
		Object oldPrev = cms.replaceVariable(VAR_PREV, new PrevItem());
		Object oldNext = cms.replaceVariable(VAR_NEXT, new NextItem());
		Object oldFirst = cms.replaceVariable(VAR_FIRST, new FirstItem());
		Object oldLast = cms.replaceVariable(VAR_LAST, new LastItem());
		
		try {
			cms.writeContent(out,
					((TextAttribute) view.getAttributeById(ContentIds.ATTR_HTML_HEADER)).getValue(),
					item);
			TextAttribute attr = (TextAttribute) view.getAttributeById(ContentIds.ATTR_HTML_ITEM);
			TextAttribute attrSel = (TextAttribute) view.getAttributeById(ContentIds.ATTR_HTML_SEL_ITEM);
			if (attrSel.getValue() == null || attrSel.getValue().trim().length() == 0)
				attrSel = attr;
			Card[] currentContentAndArea = getCurrent(cms);
			for (Iterator itr = cards.iterator(); itr.hasNext(); ) {
				Card doc = (Card) itr.next();
				//�� ��������� �������� ���� ������� � ��������� �������� ��� ���� ��������, ����� ���������� ������
				//�.�. � ������� "����������" ������� � ��� ���, � ���������� ��������� ��� �����.
				//��� ������������� ����� �������� �������� ���� � ������� �������� verifyaccess  
				if(internalPersonTemplateId.equals(doc.getTemplate()) && !tag.hasAttribute(ATTR_VERIFY_ACCESS)){
					verifyAccess = false;
				} else {
					verifyAccess = tag.hasAttribute(ATTR_VERIFY_ACCESS) ? Boolean.parseBoolean(tag.getAttribute(ATTR_VERIFY_ACCESS)) : true ;
				}
				current++;
				cms.setVariable(ContentProducer.VAR_ROW_NUM_IN_LIST, Integer.valueOf(current));
				String html = isCurrent(currentContentAndArea[0], currentContentAndArea[1], doc) ? attrSel.getValue() : attr.getValue();
				if (html.trim().length() > 0) {
                    if (fetch) {
						try {
							doc = (Card) cms.getContentDataServiceFacade().fetchCard(doc, tag, filter, verifyAccess ? CardAccess.READ_CARD : CardAccess.NO_VERIFYING);
						} catch (Exception e) {
							logger.error("Error fetching document " + doc.getId().getId());
							continue;
						}
                    }
					cms.writeContent(out, html, doc);
				}
				//cms.setVariable(VAR_ITEM, doc.getId().getId().toString());
				if (itr.hasNext())
					cms.writeContent(out,
							((TextAttribute) view.getAttributeById(ContentIds.ATTR_HTML_SEPARATOR)).getValue(),
							item);
			}
			current = -1;
			//cms.setVariable(VAR_ITEM, null);
			cms.writeContent(out,
					((TextAttribute) view.getAttributeById(ContentIds.ATTR_HTML_FOOTER)).getValue(),
					item);
		} finally {
			cms.replaceVariable(VAR_COUNT, oldCount);
			cms.replaceVariable(VAR_INDEX, oldIndex);
			cms.replaceVariable(VAR_ITEM, oldItem);
			cms.replaceVariable(VAR_PREV, oldPrev);
			cms.replaceVariable(VAR_NEXT, oldNext);
			cms.replaceVariable(VAR_FIRST, oldFirst);
			cms.replaceVariable(VAR_LAST, oldLast);
			cms.clearVariable(VAR_VIEW);
		}
	}
	
	protected void processXmlType(Tag tag, Card item, ContentProducer cms) throws Exception {
		
		if (!tag.hasAttribute(ATTR_XML))
			throw new Exception("No xml attribute defined for list");
		
		if (tag.hasAttribute(ATTR_AREA) || tag.hasAttribute(ATTR_LINKS))
			logger.warn("Multiple list definition attributes");
		final String xml = cms.expandContent(tag.getAttribute(ATTR_XML), item);
		try {
			final Search search = new Search();
            if(null != xml && xml.length() > 0) {
			    search.initFromXml(new ByteArrayInputStream(xml.getBytes("UTF-8")));
            }
			
			ObjectId folderId = new ObjectId(Card.class, cms.getVariable(ATTR_AREA));
			String sortColumnId = (String) cms.getRequest().getSessionAttribute(ContentProducer.SESS_ATTR_SORT_COLUMN_ID);
			Boolean straightOrder = (Boolean) cms.getRequest().getSessionAttribute(ContentProducer.SESS_ATTR_STRAIGHT_ORDER);
			String simpleSearchFilter = (String) cms.getVariable(ContentProducer.VAR_SIMPLE_SEARCH_FILTER);

			if(("default").equals(sortColumnId))
				straightOrder = null;
			cardsCount = cms.getContentDataServiceFacade().
									getDocumentsQtyByFolder(folderId, simpleSearchFilter, search).getTotalQty();

            if(cardsCount == 0) {
                // Do not retrieve cards if there are not ones
                cards = Collections.emptyList();
            } else {
                cards = cms.getContentDataServiceFacade().getDocumentsByFolder(folderId, simpleSearchFilter, search,
					page, pageSize, sortColumnId, straightOrder);
            }
			
			// When displaying folders current document is the first one in the table
			String requestItem = (String) cms.getRequest().getAttribute(ContentProducer.REQ_ATTR_ITEM);
			if(null != cards && cards.size() > 0 && null == requestItem) {
				currentContent = (Card) cards.get(0);
			}
		} catch(Exception ex) {
			logger.info( "Fail to load search from xml >>>\n"+ xml+ "\n<<<" );
			throw ex;
		}
	}
	
    @Override
	protected void processSearchType(ContentProducer cms) {
		if (pageSize > 0) {
			Search search = (Search) cms.getRequest().getSessionAttribute(ContentProducer.APP_ATTR_SEARCH);
			if (search != null) {
				if (search.getFilter() == null) {
					search.clearFilter();
				}
				search.getFilter().setPageSize(pageSize);
				search.getFilter().setPage(page);
			}
		}

		super.processSearchType(cms);

		if (cards != null) {
			Object theWholeSize = cms.getRequest().getSessionAttribute(ContentProducer.SESS_ATTR_FOUND_WHOLE_SIZE);
			if (theWholeSize != null) {
				cardsCount = ((Integer) theWholeSize).longValue();
			}
		}
	}

	@Override
    protected List getLinkedCards(Card item, ContentProducer cms, Attribute attr) {
        return cms.getLinkedCards(item, attr, filter, viewId);
    }

    private void initPageAttributes(ContentProducer cms) {
		pageSize = ((IntegerAttribute) view.getAttributeById(ContentIds.ATTR_PAGE_SIZE)).getValue();
		if(pageSize != 0) {
			String pageStr = cms.getRequest().getParameter(PagedList.PARAM_PAGE);
			if (pageStr != null) {
				page = Integer.valueOf(pageStr);
				cms.getRequest().setSessionAttribute(PagedList.ATTR_PAGE_CURRENT, page);	//***** local
			}
			Integer pageAttribute = (Integer) cms.getRequest().getSessionAttribute(PagedList.ATTR_PAGE_CURRENT);	//***** local
			
			if(null != pageAttribute) {
				page = pageAttribute;
			} else {
				page = 1;
			}		
		}
	}
    
	/**
	 * Sorts retrieved cards by the given sort parameters.
	 * Parameters should be passed in the next format: "attribute Id : attribute type : sort direction".
	 * 
	 * @param sortParams
	 */
	private void sortCards(String sortParams) {
		if (StringUtils.hasLength(sortParams)) {
			String[] entries = StringUtils.tokenizeToStringArray(sortParams, SORT_DELIMITER);
			if (entries.length == 3) {
				String sortAttrId = entries[0].trim();
				String sortAttrType = entries[1].trim();
				String sortDirection = entries[2].trim();
				if (StringUtils.hasLength(sortAttrId) && StringUtils.hasLength(sortAttrType)) {
					try {
						ObjectId sortId = ObjectId.predefined(AttrUtils.getAttrTypeClass(sortAttrType), sortAttrId);
						if (sortId != null) {
							boolean isSortDesc = false;
							if (StringUtils.hasLength(sortDirection) && SORT_DESCENDING.equals(sortDirection)) {
								isSortDesc = true;
							}
							CardAttrComparator comparator = new CardAttrComparator(sortId, isSortDesc);
							Collections.sort(cards, comparator);
						} else {
							logger.warn("Error sorting cards in ListTag. ObjectId is undefined for the given attribute id: " + sortAttrId);
						}
					} catch (Exception e) {
						// just log exception and keep cards unsorted if anything was wrong during sorting
						logger.error("Error sorting cards in ListTag: " + e);
					}
				}
			} else {
				logger.warn("Error sorting cards in ListTag. Invalid number of parameters for sorting (three parameters are required), given: " + sortParams);
			}
		}
	}
	
	private boolean isCurrent(Card currentContent, Card currentArea, Card item)
	{
		if (currentContent != null)
			return currentContent.getId().equals(item.getId());

		if (currentArea != null)
			return currentArea.getId().equals(item.getId());
		
		return false;
	}
	
	private Card[] getCurrent(ContentProducer cms)
	{
		if(null != currentContent) {
			return new Card[] {currentContent, null}; 
		}
		
		Card doc = cms.getCurrentContent(true);
		if (doc != null){
			return new Card[] {doc, null};
		}
		
		Card area = cms.getCurrentSiteArea();
		if (area != null) {
			return new Card[] {null, area};
		}
		
		return new Card[] {null, null};
	}
	
	private class Count implements Variable
	{
		public Object getValue() {
			if (cards == null || cards.size() == 0)
				return null;
			return Integer.valueOf(cards.size());
		}
	}
	
	private class Index implements Variable
	{
		public Object getValue() {
			if (cards == null || cards.size() == 0 || current == -1)
				return null;
			return Integer.valueOf(current + 1);
		}
	}
	
	private class Item implements Variable
	{
		public Object getValue() {
			if (cards == null || cards.size() == 0 || current == -1)
				return null;
			return ((Card) cards.get(current)).getId().getId();
		}
	}
	
	private class PrevItem implements Variable
	{
		public Object getValue() {
			if (cards == null || cards.size() == 0 || current <= 0)
				return null;
			return ((Card) cards.get(current - 1)).getId().getId();
		}
	}
	
	private class NextItem implements Variable
	{
		public Object getValue() {
			if (cards == null || cards.size() == 0 || current < 0 || current >= cards.size() - 1)
				return null;
			return ((Card) cards.get(current + 1)).getId().getId();
		}
	}
	
	private class FirstItem implements Variable
	{
		public Object getValue() {
			if (cards == null || cards.size() == 0)
				return null;
			return ((Card) cards.get(0)).getId().getId();
		}
	}
	
	private class LastItem implements Variable
	{
		public Object getValue() {
			if (cards == null || cards.size() == 0)
				return null;
			return ((Card) cards.get(cards.size() - 1)).getId().getId();
		}
	}
}
