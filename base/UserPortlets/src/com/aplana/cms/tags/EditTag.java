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

import org.apache.commons.lang.StringEscapeUtils;

import com.aplana.cms.ContentProducer;
import com.aplana.cms.FieldProcessor;
import com.aplana.cms.ProcessRequest;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataServiceBean;

public class EditTag extends AttributeProcessor implements TagProcessor, FieldProcessor
{
	public static final String PREFIX = "edit_";
	
	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception
	{
		return super.prepareData(tag, item, cms) && (
			attribute instanceof StringAttribute || attribute instanceof TextAttribute);
	}

	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms)
	{
		if (attribute instanceof StringAttribute) {
			String value = getDefaultValue(tag);
			if (value == null)
				value = ((StringAttribute) attribute).getValue();
			out.write("<input type='text' name='" + makeAttributeId() + "' value='" +
					StringEscapeUtils.escapeHtml(cms.expandContent(getDefaultValue(tag), item)) + "'");
			cms.writeHtmlAttributes(out, tag, item);
			out.write(">");
		} else if (attribute instanceof TextAttribute) {
			String value = getDefaultValue(tag);
			if (value == null)
				value = ((StringAttribute) attribute).getValue();
			out.write("<textarea name='" + makeAttributeId() + "'");
			cms.writeHtmlAttributes(out, tag, item);
			out.write(">" + StringEscapeUtils.escapeHtml(cms.expandContent(getDefaultValue(tag), item)) +
					"</textarea>");
		}
	}

	public boolean processFields(String param, Card card,
			ProcessRequest request, DataServiceBean service)
	{
		if (!super.processFields(param, card, request, service))
			return false;
		if (attribute instanceof StringAttribute)
			((StringAttribute) attribute).setValue(request.getParameter(param));
		else if (attribute instanceof TextAttribute)
			((TextAttribute) attribute).setValue(request.getParameter(param));
		else {
			logger.error("Unsupported attribute type: " + attribute.getClass().getName());
			return false;
		}
		return true;
	}

	public String getPrefix() {
		return PREFIX;
	}
}
