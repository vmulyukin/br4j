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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.aplana.dbmi.model.*;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.protocol.HTTP;

import com.aplana.cms.ContentProducer;
import com.aplana.cms.FieldProcessor;
import com.aplana.cms.ProcessRequest;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.service.DataServiceBean;

public class SetTag extends AttributeProcessor implements TagProcessor, FieldProcessor
{
	public static final String PREFIX = "set_";
	public static final String CURRENT = "$";
	
	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception
	{
		return super.prepareData(tag, item, cms) && (
			attribute instanceof StringAttribute || attribute instanceof TextAttribute ||
			attribute instanceof IntegerAttribute || attribute instanceof CardLinkAttribute ||
			attribute instanceof PersonAttribute || attribute instanceof DateAttribute);
	}

	public String getPrefix() {
		return PREFIX;
	}

	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms)
	{
		if (attribute instanceof StringAttribute || attribute instanceof TextAttribute ||
				attribute instanceof IntegerAttribute || attribute instanceof CardLinkAttribute) {
			out.write("<input type='hidden' name='" + makeAttributeId() + "' value='" +
					StringEscapeUtils.escapeHtml(cms.expandContent(getDefaultValue(tag), item)) + "'");
			cms.writeHtmlAttributes(out, tag, item);
			out.write(">");
		} else if (attribute instanceof DateAttribute || attribute instanceof PersonAttribute) {
			if (getDefaultValue(tag) != null)
				logger.warn("Default value for date and person attribute not supported");
			out.write("<input type='hidden' name='" + makeAttributeId() + "' value='" + CURRENT + "'");
			cms.writeHtmlAttributes(out, tag, item);
			out.write(">");
		}
	}

	public boolean processFields(String param, Card card,
			ProcessRequest request, DataServiceBean service)
	{
		try {
			if (!super.processFields(param, card, request, service))
				return false;	
			if (attribute instanceof StringAttribute)
				((StringAttribute) attribute).setValue(URLDecoder.decode(request.getParameter(param), HTTP.UTF_8));
			else if (attribute instanceof TextAttribute)
				((TextAttribute) attribute).setValue(URLDecoder.decode(request.getParameter(param), HTTP.UTF_8));
			else if (attribute instanceof IntegerAttribute)
				((IntegerAttribute) attribute).setValue(Integer.parseInt(request.getParameter(param)));
			else if (attribute instanceof CardLinkAttribute) {
				final String[] ids = request.getParameter(param).split(",");
				// final ArrayList cards = new ArrayList(ids.length);
				final CardLinkAttribute cardLink = (CardLinkAttribute) attribute;
				for (int i = 0; i < ids.length; i++) {
					//try {
						// cards.add(DataObject.createFromId(new ObjectId(Card.class, Long.parseLong(ids[i]))));
						cardLink.addLinkedId(  Long.parseLong(ids[i]));
					
					/*} catch (NumberFormatException e) {
						logger.error("Error parsing card id: " + ids[i] + "; skipped", e);
					}*/
				}
				// (2010/02, RuSA) OLD: ((CardLinkAttribute) attribute).setValues(cards);
			} else if (attribute instanceof DateAttribute) {
				String strDateValue = request.getParameter(param);
				
				if (CURRENT.equals(strDateValue))
					((DateAttribute) attribute).setValue(new Date());
				else {
					Date date = parseDateValue(strDateValue);
					if (date != null)
						((DateAttribute) attribute).setValue(date);
					else {
						logger.error("Can't parse date: " + request.getParameter(param));
						return false;
					}
				}
			} else if (attribute instanceof PersonAttribute) {
                String personParamValue = request.getParameter(param);
				if (CURRENT.equals(personParamValue)) {
					((PersonAttribute) attribute).setPerson(service.getPerson());
                } else {
                    try {
                        long personId = Long.valueOf(personParamValue);
                        ((PersonAttribute) attribute).setPerson(new ObjectId(Person.class, personId));
                    } catch (NumberFormatException e) {
                        logger.error("Can't parse user: " + personParamValue);
                        return false;
                    }
				}
			} else {
				logger.error("Unsupported attribute type: " + attribute.getClass().getName());
				return false;
			}
			return true;
		} catch (Exception e) {
			logger.error("Error processing parameter " + param, e);
			return false;
		}
	}

	private Date parseDateValue(String strDateValue) {

		if (strDateValue == null || ("".equals(strDateValue.trim())) )
			return null;

		try {
			
			return (new SimpleDateFormat("dd.MM.yyyy")).parse(strDateValue);
			
		} catch (Exception e) {
			logger.error("Bad format date of term person control:", e);
		}

		return null;

	}
}
