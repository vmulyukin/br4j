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
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
//import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;

public class ImageTag implements TagProcessor
{
	public static final String IMAGE_SERVLET = "/DBMI-UserPortlets/MaterialDownloadServlet";

	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception
	{
		//String alt = item.getAttributeById(Attribute.ID_NAME).getStringValue();
		return item.getMaterialType() == Card.MATERIAL_FILE;
	}
	
	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms)
	{
		out.write("<img src='" + IMAGE_SERVLET + "?MI_CARD_ID_FIELD=" + item.getId().getId() +
				/*"' alt='" + alt.replaceAll("'", "\"") +*/ "&noname=1'");
		cms.writeHtmlAttributes(out, tag, item);
		out.write(">");
	}
}
