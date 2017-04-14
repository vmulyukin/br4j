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
import com.aplana.cms.ContentRequest;
import com.aplana.cms.ContentUtils;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;

public class UrlTag implements TagProcessor
{
	public static final String ATTR_PAGE = "page";
	
	public static final String PORTAL_CONTEXT = "/portal/auth/portal";
	
	protected Log logger = LogFactory.getLog(getClass());
	protected String link = null;
	
	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception
	{
		String page = tag.getAttribute(ATTR_PAGE);
		while (page == null && ContentIds.TPL_AREA.equals(item.getTemplate())) {
			page = ((StringAttribute) item.getAttributeById(ContentIds.ATTR_PAGEID)).getValue();
			if (page != null) {
				page = page.trim();
				if (page.length() == 0)
					page = null;
			}
			if (page != null)
				break;
			ObjectId parentId = ContentUtils.getParentAreaId(item);
			if (parentId == null)
				break;
			item = (Card) cms.getContentDataServiceFacade().getSiteArea(parentId);
		}
		if (page != null)
			//page = "$" + page;
			page = PORTAL_CONTEXT + page;	//***** JBoss only
		link = page;
		/*link = Portal.getFactory().getPortletService().generateLink(
				page, null, null, /*"dbmi.cms.w.Content",
				Collections.singletonMap(ContentIds.TPL_AREA.equals(item.getTemplate()) ?
						ContentViewPortlet.PARAM_AREA : ContentViewPortlet.PARAM_ITEM,
						item.getId().getId().toString()),* /
				cms.getRequest(), cms.getResponse());*/
		return true;
	}

	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms)
	{
		out.write(link + "?");
		out.write(ContentIds.TPL_AREA.equals(item.getTemplate()) ?
				ContentRequest.PARAM_AREA : ContentRequest.PARAM_ITEM);
		out.write("=" + item.getId().getId().toString());
	}

}
