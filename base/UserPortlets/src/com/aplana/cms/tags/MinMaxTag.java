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
import java.util.List;

import com.aplana.cms.ContentProducer;
import com.aplana.cms.ContentUtils;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.cms.view_template.CardViewData;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;

public class MinMaxTag extends ListBase implements TagProcessor
{
	public static final String TYPE_MIN = "min";
	public static final String TYPE_MAX = "max";
	
	private Card document;
	private String field;

	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception
	{
		if (!tag.hasAttribute(ContentProducer.ATTR_FIELD))
			throw new IllegalStateException("Mandatory attribute 'field' not set");
        if (!super.prepareData(tag, item, cms))
			return false;
		field = tag.getAttribute(ContentProducer.ATTR_FIELD);
		ContentUtils.sortCards(cards, field,
				TYPE_MIN.equals(tag.getAttribute(ContentProducer.ATTR_TYPE)));
		document = (Card) cards.iterator().next();
		if (tag.getContent() == null)
			return ContentUtils.getAttribute(document, field) != null;
		document = cms.getContentDataServiceFacade().fetchCard(document, tag, filter);
		return true;
	}

    @Override
    protected List getLinkedCards(Card item, ContentProducer cms, Attribute attr) {
        return cms.getLinkedCards(item, attr, filter, null);
    }

    public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms)
	{
		if (tag.getContent() == null) {
			out.write(ContentUtils.getAttribute(document, field).getStringValue());
		} else {
			cms.writeContent(out, tag.getContent(), document);
		}
	}

    /**
     * just indicate current app state
     * @return
     */
    @Override
    protected boolean isLegacyApproach() {
        return !CardViewData.containsBean("");
    }
}
