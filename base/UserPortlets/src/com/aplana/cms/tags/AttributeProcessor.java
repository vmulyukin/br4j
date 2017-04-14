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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.cms.ContentProducer;
import com.aplana.cms.ContentUtils;
import com.aplana.cms.FieldProcessor;
import com.aplana.cms.ProcessRequest;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.util.AttributeUtil;

public abstract class AttributeProcessor implements TagProcessor, FieldProcessor
{
	public static final String ATTR_VALUE = "value";
	
	protected Log logger = LogFactory.getLog(getClass());
	protected Attribute attribute;

	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception {
		if (!tag.hasAttribute(ContentProducer.ATTR_FIELD))
			throw new Exception("Mandatory attribute " + ContentProducer.ATTR_FIELD + " not set");
		attribute = ContentUtils.getAttribute(item, tag.getAttribute(ContentProducer.ATTR_FIELD));
		return attribute != null;
	}
	
	public abstract String getPrefix();
	
	protected String makeAttributeId()
	{
		return getPrefix() + attribute.getType() + "_" + attribute.getId().getId();
	}
	
	protected String getDefaultValue(Tag tag)
	{
		if (tag.getContent() != null)
			return tag.getContent();
		if (tag.hasAttribute(ATTR_VALUE))
			return tag.getAttribute(ATTR_VALUE);
		return null;
	}

	public boolean processFields(String param, Card card,
			ProcessRequest request, DataServiceBean service) {
		attribute = card.getAttributeById(parseAttributeId(param));
		if (attribute == null) {
			logger.error("Card doesn't contain " + parseAttributeId(param));
			return false;
		}
		return true;
	}
	
	protected ObjectId parseAttributeId(String param)
	{
		String[] parts = param.split("_", 3);
		if (parts.length < 3)
			throw new IllegalArgumentException("Not an attribute id: " + param);
		return AttributeUtil.makeAttributeId(parts[1], parts[2]);
	}
}
