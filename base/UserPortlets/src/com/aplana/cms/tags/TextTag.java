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
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import com.aplana.cms.ContentProducer;
import com.aplana.cms.ContentUtils;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.TextAttribute;

public class TextTag implements TagProcessor
{
	public static final String ATTR_LIMIT = "limit";
	public static final String ATTR_ELLIPSIS = "ellipsis";
	public static final String ATTR_FORMAT = "format";
	
	protected Log logger = LogFactory.getLog(getClass());
	private String text = null;
	private boolean clip = false;
	private String ellipsis = "...";

	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception
	{
		if (tag.hasAttribute(ContentProducer.ATTR_FIELD)) {
			Attribute attr = null;
			
			if(tag.hasAttribute(ContentProducer.ATTR_LINK)) {
				if(tag.hasAttribute(ContentProducer.ATTR_ITEM)) {
					
					ObjectId linkId = IdUtils.smartMakeAttrId(tag.getAttribute(ContentProducer.ATTR_LINK), CardLinkAttribute.class, false);
					ObjectId fieldId = IdUtils.smartMakeAttrId(tag.getAttribute(ContentProducer.ATTR_FIELD), TextAttribute.class, false);
					ObjectId itemId = IdUtils.smartMakeAttrId(tag.getAttribute(ContentProducer.ATTR_ITEM).split("=")[0], PersonAttribute.class, false);
					List<Card> linkCards = cms.getContentDataServiceFacade().getLinkedCards(item, fieldId, linkId, itemId);
					for(Card linkCard : linkCards) {
						attr = cms.getStringValueAttr(tag, linkCard, fieldId, itemId);
						if(attr != null)
							break;
					}
				} else {
				LinkAttribute cardLinkAttr = 
					cms.getContentDataServiceFacade().getCardLinkAttribute(
							tag.getAttribute(ContentProducer.ATTR_FIELD), 
							tag.getAttribute(ContentProducer.ATTR_LINK), item);
				if(null != cardLinkAttr && null != cardLinkAttr.getLabelAttrId()) {
					attr = cardLinkAttr;
				}
			}
			}
			
			if(null == attr) {
				attr = ContentUtils.getAttribute(item, tag.getAttribute(ContentProducer.ATTR_FIELD), true);
			}
			
			if (attr == null) {
                if ( logger.isDebugEnabled() ) {
                    logger.debug("Attribute '" + tag.getAttribute(ContentProducer.ATTR_FIELD) +
                            "' not found in card " + item.getId().getId());
                }
				//attr = item.getAttributeById(Attribute.ID_NAME);
			}
			else {
				text = attr.getStringValue();

				if (tag.hasAttribute(ATTR_FORMAT) && StringUtils.hasLength(text)) {
					String pattern = tag.getAttribute(ATTR_FORMAT);
					text = new SimpleDateFormat(pattern).format(ContextProvider.getContext().getDateFromDefaultPatternString(text));
				}
			}
			
		} else
			text = cms.getMaterial(item.getId());
		if (text == null || text.length() == 0)
			return false;
		
		if (tag.hasAttribute(ATTR_LIMIT)) {
			try {
				int lim = Integer.parseInt(tag.getAttribute(ATTR_LIMIT));
				clip = text.length() > lim;
				if (clip)
					text = text.substring(0, lim);
			} catch (NumberFormatException e) {
				logger.warn("Invalid limit value: " + tag.getAttribute(ATTR_LIMIT));
			}
		}
		if (clip && tag.hasAttribute(ATTR_ELLIPSIS))
			ellipsis = tag.getAttribute(ATTR_ELLIPSIS);
		return true;
	}
	
	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms)
	{
		ContentUtils.writeText(out, text);
		if (clip)
			cms.writeContent(out, ellipsis, item);
	}
}
