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

import com.aplana.cms.ContentProducer;
import com.aplana.cms.ContentUtils;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;

public class LinkTag extends UrlTag implements TagProcessor
{
	
	private static final String ATTR_LABEL = "label";
	
	protected String text = null;

	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception
	{
		if (!super.prepareData(tag, item, cms))
			return false;
		
		if(tag.hasAttribute(ATTR_LABEL)) {
			text = tag.getAttribute(ATTR_LABEL);
			return true;
		}
		
		Attribute attr = null;
		if (tag.hasAttribute(ContentProducer.ATTR_FIELD)) {
			attr = ContentUtils.getAttribute(item, tag.getAttribute(ContentProducer.ATTR_FIELD));
			if (attr == null)
                if ( logger.isDebugEnabled() ) {
                    logger.debug("Attribute '" + tag.getAttribute(ContentProducer.ATTR_FIELD) +
                            "' not found in card " + item.getId().getId());
                }
		}
		
		if (attr == null) {
			attr = ContentUtils.getAttribute(item, Attribute.ID_NAME.getId().toString());
		}
		
		if(null != attr) {
			text = attr.getStringValue();
		}
		
		return text != null && text.length() > 0;
	}
	
	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms)
	{
		out.write("<a href='");
		super.writeHtml(out, tag, item, cms);
		out.write("'");
		cms.writeHtmlAttributes(out, tag, item);
		out.write(">");
		out.write(text);
		out.write("</a>");
	}
}
