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
import com.aplana.cms.NavigationPortlet;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.IntegerAttribute;

public class IdentTag implements TagProcessor
{
	protected Log logger = LogFactory.getLog(getClass());
	private int times;

	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception
	{
		Attribute attr;
		if (tag.hasAttribute(ContentProducer.ATTR_FIELD))
			attr = ContentUtils.getAttribute(item, tag.getAttribute(ContentProducer.ATTR_FIELD));
		else
			attr = item.getAttributeById(NavigationPortlet.ATTR_LEVEL);
		/*if (attr == null || !(attr instanceof IntegerAttribute)) {
			logger.error("Error fetching ident attribute");
			return;
		}*/
		times = ((IntegerAttribute) attr).getValue();
		return times > 0;
	}
	
	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms)
	{
		for (int i = 0; i < times; i++)
			cms.writeContent(out, tag.getContent(), item);
	}
}
