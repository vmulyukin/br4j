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

import com.aplana.cms.ContentProducer;
import com.aplana.cms.ContentUtils;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;

public class FileTag implements TagProcessor
{
	public static final String DOWNLOAD_SERVLET = "/DBMI-UserPortlets/MaterialDownloadServlet";
	
	protected Log logger = LogFactory.getLog(getClass());
	private String text = null;

	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception
	{
		if (item.getMaterialType() != Card.MATERIAL_FILE) {
			logger.warn("No file in card " + item.getId().getId());
			return false;
		}
		Attribute attr = null;
		if (tag.hasAttribute(ContentProducer.ATTR_FIELD))
			attr = ContentUtils.getAttribute(item, tag.getAttribute(ContentProducer.ATTR_FIELD));
		if (attr == null)
			text = item.getFileName();
		else
			text = attr.getStringValue();
		return text != null && text.length() > 0;
	}
	
	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms)
	{
		out.write("<a href='" + DOWNLOAD_SERVLET + "?MI_CARD_ID_FIELD=" + item.getId().getId() + "'");
		cms.writeHtmlAttributes(out, tag, item);
		out.write(">");
		out.write(text);
		out.write("</a>");
	}
}
