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

import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.cms.ContentProducer;
import com.aplana.cms.PagedList;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.model.Card;

public class PageLinkTag implements TagProcessor
{
	public static final String ATTR_PAGE = "page";
	
	protected Log logger = LogFactory.getLog(getClass());

	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception
	{
		if (!tag.hasAttribute(ATTR_PAGE))
			throw new Exception("Mandatory attribute " + ATTR_PAGE + " not defined");
		return true;
	}
	
	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms)
	{
		String link = ((RenderResponse) cms.getResponse()).createRenderURL().toString();
		out.write("<a href='");
		out.write(link);
		out.write(link.toString().contains("?") ? "&" : "?");
		out.write(PagedList.PARAM_PAGE + "=");
		cms.writeContent(out, tag.getAttribute(ATTR_PAGE), item);
		out.write("'");
		cms.writeHtmlAttributes(out, tag, item);
		out.write(">");
		String text = tag.getContent();
		if (text == null || text.length() == 0)
			text = tag.getAttribute(ATTR_PAGE);
		cms.writeContent(out, text, item);
		out.write("</a>");
	}
}
