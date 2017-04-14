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
import com.aplana.cms.FieldProcessor;
import com.aplana.cms.ProcessRequest;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataServiceBean;

public class RemoveTag extends AttributeProcessor implements TagProcessor, FieldProcessor
{
	public static final String PREFIX = "remove_";
	
	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception {
		return super.prepareData(tag, item, cms) && (
			attribute instanceof CardLinkAttribute);// || attribute instanceof PersonAttribute);
	}

	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms) {
		if (attribute instanceof CardLinkAttribute) {
			out.write("<input type='hidden' name='" + makeAttributeId() + "' value='" +
					cms.expandContent(getDefaultValue(tag), item) + "'");
			cms.writeHtmlAttributes(out, tag, item);
			out.write(">");
		}
	}

	public boolean processFields(String param, Card card,
			ProcessRequest request, DataServiceBean service) {
		if (!super.processFields(param, card, request, service))
			return false;
		if (attribute instanceof CardLinkAttribute) {
			final String[] ids = request.getParameter(param).split(",");

			// >>> (2010/02, RuSA)
			final CardLinkAttribute attrLink = (CardLinkAttribute) attribute; 
			if ( attrLink.getLinkedCount() < 1 ) {
				logger.warn("No linked cards in attribute " + attribute.getName() +
						" in card " + card.getId().getId());
				return false;
			}

			for (int i = 0; i < ids.length; i++) {
				try {
					final ObjectId id = new ObjectId(Card.class, Long.parseLong(ids[i]));
					if (!attrLink.removeLinkedId(id))
						logger.warn("Card " + ids[i] + " is not linked to " + card.getId().getId() +
								" in attribute " + attribute.getName());
				} catch (NumberFormatException e) {
					logger.warn("Invalid card id: " + ids[i]);
				}
			}
			// <<< (2010/02, RuSA)

		} else {
			logger.error("Unsupported attribute type: " + attribute.getClass().getName());
			return false;
		}
		return true;
	}

	public String getPrefix() {
		return PREFIX;
	}
}
