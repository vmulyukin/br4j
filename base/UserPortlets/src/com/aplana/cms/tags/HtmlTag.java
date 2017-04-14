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

public class HtmlTag implements TagProcessor
{
	protected Log logger = LogFactory.getLog(getClass());
	private String text = null;

	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception
	{
		if (tag.hasAttribute(ContentProducer.ATTR_FIELD)) {
			Attribute attr = ContentUtils.getAttribute(item, tag.getAttribute(ContentProducer.ATTR_FIELD));
			if (attr == null) {
                if ( logger.isDebugEnabled() ) {
                    logger.debug("Attribute '" + tag.getAttribute(ContentProducer.ATTR_FIELD) +
                            "' not found in card " + item.getId().getId());
                }
				//attr = item.getAttributeById(Attribute.ID_NAME);
				return false;
			}
			text = attr.getStringValue();
		} else
			text = cms.getMaterial(item.getId());
		return text != null && text.length() > 0;
	}
	
	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms)
	{
		cms.writeContent(out, text, item);
	}
}
