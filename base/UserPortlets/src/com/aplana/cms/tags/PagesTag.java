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

import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.cms.ContentIds;
import com.aplana.cms.ContentProducer;
import com.aplana.cms.PagedList;
import com.aplana.cms.PagedListInterface;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.TextAttribute;

public class PagesTag implements TagProcessor
{
	public static final String VAR_PAGE_PREV = "page.prev";
	public static final String VAR_PAGE_NEXT = "page.next";
	public static final String VAR_ITEM_FIRST = "itemFirst";
	public static final String VAR_ITEM_LAST = "itemLast";
	
	protected Log logger = LogFactory.getLog(getClass());
	private PagedListInterface pages = null;

	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception
	{
		pages = (PagedListInterface) cms.getRequest().getAttribute(PagedList.ATTR_PAGES);
		return null != pages && pages.totalPages() > 1;
	}
	
	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms)
	{
		Object attr = cms.getRequest().getSessionAttribute(PagedList.ATTR_PAGE_CURRENT);	//***** local
		int current = attr == null ? 1 : ((Integer) attr).intValue();
		cms.writeContent(out, ((TextAttribute) item.getAttributeById(current > 1 ?
				ContentIds.ATTR_HTML_PAGE_OPEN : ContentIds.ATTR_HTML_PAGE_NOOPEN)).getValue(), item);
		int startCount = ((IntegerAttribute) item.getAttributeById(ContentIds.ATTR_PAGES_BEGIN)).getValue();
		int endCount = ((IntegerAttribute) item.getAttributeById(ContentIds.ATTR_PAGES_END)).getValue();
		int aroundCount = ((IntegerAttribute) item.getAttributeById(ContentIds.ATTR_PAGES_CURR)).getValue();
		String pageHtml = ((TextAttribute) item.getAttributeById(ContentIds.ATTR_HTML_PAGE)).getValue();
		int page;
		for (page = 1; page <= startCount && page < current && page <= pages.totalPages(); page++) {
			setVariables(cms, pages, page);
			cms.writeContent(out, pageHtml, item);
		}
		if (page < current - aroundCount)
			cms.writeContent(out, ((TextAttribute) item.getAttributeById(ContentIds.ATTR_HTML_PAGE_SKIP)).getValue(), item);
		for (page = Math.max(page, current - aroundCount);
				page <= current + aroundCount && page <= pages.totalPages(); page++) {
			setVariables(cms, pages, page);
			cms.writeContent(out, page == current ?
					((TextAttribute) item.getAttributeById(ContentIds.ATTR_HTML_PAGE_CURR)).getValue() :
					pageHtml, item);
		}
		if (page < pages.totalPages() - endCount + 1)
			cms.writeContent(out, ((TextAttribute) item.getAttributeById(ContentIds.ATTR_HTML_PAGE_SKIP)).getValue(), item);
		for (page = Math.max(page, pages.totalPages() - endCount + 1); page <= pages.totalPages(); page++) {
			setVariables(cms, pages, page);
			cms.writeContent(out, pageHtml, item);
		}
		cms.setVariable(ListTag.VAR_PAGE, new Integer(current));
		cms.writeContent(out, ((TextAttribute) item.getAttributeById(current < pages.totalPages() ?
				ContentIds.ATTR_HTML_PAGE_CLOSE : ContentIds.ATTR_HTML_PAGE_NOCLOSE)).getValue(), item);
	}
	
	private void setVariables(ContentProducer cms, PagedListInterface pages, int page)
	{
		cms.setVariable(ListTag.VAR_PAGE, new Integer(page));
		cms.setVariable(VAR_ITEM_FIRST, new Integer(pages.firstItem(page)));
		cms.setVariable(VAR_ITEM_LAST, new Integer(pages.lastItem(page)));
		if (page > 1)
			cms.setVariable(VAR_PAGE_PREV, new Integer(page - 1));
		else
			cms.clearVariable(VAR_PAGE_PREV);
		if (page < pages.totalPages())
			cms.setVariable(VAR_PAGE_NEXT, new Integer(page + 1));
		else
			cms.clearVariable(VAR_PAGE_NEXT);
	}
}
