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

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;

import com.aplana.cms.ContentProducer;
import com.aplana.cms.FolderDocumentsQuantities;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.Card;

public class CountTag extends ListBase implements TagProcessor
{
	/**
	 * documents quantity 
	 */
	private FolderDocumentsQuantities quantities = null;

	@Override
	protected void processXmlType(Tag tag, Card item, ContentProducer cms)
			throws Exception {
        if (tag.hasAttribute(ATTR_AREA) || tag.hasAttribute(ATTR_LINKS))
			logger.warn("Multiple list definition attributes");
		final String xml = cms.expandContent(tag.getAttribute(ATTR_XML), item);
		try {
			Search search = cms.getContentDataServiceFacade().initFromXml(xml);
			String simpleSearchFilter = (String) cms.getVariable(ContentProducer.VAR_SIMPLE_SEARCH_FILTER);
			
			quantities =  cms.getContentDataServiceFacade().getDocumentsQtyByFolder(item.getId(), simpleSearchFilter, search);
			
		} catch(Exception ex)
		{
			logger.info( "Fail to load search from xml >>>\n"+ xml+ "\n<<<" );
			throw ex;
		}
		
	}
	
	protected boolean containCards() {
		
		return getCardsQuantity().getTotalQty() > 0;
	}
	
	private FolderDocumentsQuantities getCardsQuantity() {
		
		return (cards != null) ? new FolderDocumentsQuantities(cards.size()): this.quantities;
		
	}
	

	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms)
	{

		FolderDocumentsQuantities quantities =  getCardsQuantity();
		
		StringBuilder quantitiesView = new StringBuilder();
		quantitiesView.append(quantities.getTotalQty());
		
		if(quantities.getUrgentQty() > 0) {
			quantitiesView.append("&nbsp;<span class='qtyUrgent'>").append(quantities.getUrgentQty()).append("!</span>");
		}
		if(quantities.getVeryUrgentQty() > 0) {
			quantitiesView.append("&nbsp;<span class='qtyVeryUrgent'>").append(quantities.getVeryUrgentQty()).append("!</span>");
		}
		if(quantities.getImmediateQty() > 0) {
			quantitiesView.append("&nbsp;<span class='qtyImmediately'>").append(quantities.getImmediateQty()).append("!</span>");
		}
			
		cms.writeContent(out, quantitiesView.toString(), item);
	}
	
	
}
