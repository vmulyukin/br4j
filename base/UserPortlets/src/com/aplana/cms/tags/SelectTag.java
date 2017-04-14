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
import java.text.ParseException;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.cms.ContentProducer;
import com.aplana.cms.ContentUtils;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.web.tag.util.StringUtils;

public class SelectTag implements TagProcessor
{
	public static final String ATTR_VALUE = "value";
	
	public static final String TAG_CASE = "case";
	public static final String ATTR_CASE_EQUAL = "equal";
	public static final String ATTR_CASE_CONTAIN = "contain";
	public static final String EQUAL_DELIMITER = "||";
	
	protected final Log logger = LogFactory.getLog(getClass());
	private String selected;
	
	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception {
		String pattern = "";
		if (tag.hasAttribute(ContentProducer.ATTR_FIELD)) {
			Attribute attr = ContentUtils.getAttribute(item, tag.getAttribute(ContentProducer.ATTR_FIELD));
			if (attr != null && attr.getStringValue() != null)
				pattern = attr.getStringValue();
			else
				logger.warn("Card " + item.getId().getId() + " doesn't have attribute " +
						tag.getAttribute(ContentProducer.ATTR_FIELD));
		} else if (tag.hasAttribute(ATTR_VALUE)) {
			pattern = cms.expandContent(tag.getAttribute(ATTR_VALUE), item);
		} else
			throw new IllegalArgumentException("Neither " + ATTR_VALUE + " nor " +
					ContentProducer.ATTR_FIELD + " attribute set");
		pattern = pattern.trim();
		
		Pattern selector = Pattern.compile("<" + TAG_CASE, Pattern.CASE_INSENSITIVE);
		String content = tag.getContent();
		int start = 0;
		while (start < content.length()) {
			while (start < content.length() && Character.isWhitespace(content.charAt(start)))
				start++;
			if (start == content.length())
				break;
			if (!selector.matcher(content.substring(start)).lookingAt())
				throw new ParseException("Tag " + TAG_CASE + " expected", start);
			Tag nested = new Tag();
			start = cms.parseTag(content, start, nested);
			if (checkCase(pattern, nested, item, cms)) {
				selected = nested.getContent();
				return true;
			}
		}
		return false;
	}

	private boolean checkCase(String pattern, Tag tag, Card item, ContentProducer cms) {
		boolean matched = false;
		if (tag.hasAttribute(ATTR_CASE_EQUAL)) {
			String equalStr = tag.getAttribute(ATTR_CASE_EQUAL);
			if (StringUtils.hasLength(equalStr)) {
				String[] entries = StringUtils.tokenizeToStringArray(equalStr, EQUAL_DELIMITER);
				for (String entry : entries) {
					matched = pattern.equalsIgnoreCase(cms.expandContent(entry.trim(), item).trim());
					if (matched) {
						break;
					}
				}
			} else {
				if (!StringUtils.hasLength(pattern)) {
					matched = true;
				}
			}
			return matched;
		}
		
		if (tag.hasAttribute(ATTR_CASE_CONTAIN))
			return pattern.contains(cms.expandContent(tag.getAttribute(ATTR_CASE_CONTAIN), item));
		
		// Tag case with no attributes works as default
		return true;
	}

	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms) {
		cms.writeContent(out, selected, item);
	}
}
