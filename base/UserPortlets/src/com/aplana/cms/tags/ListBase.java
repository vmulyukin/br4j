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
import java.util.List;

import com.aplana.cms.view_template.CardViewData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.cms.ContentProducer;
import com.aplana.cms.ContentUtils;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;

import com.aplana.dbmi.model.ObjectId;


public abstract class ListBase implements TagProcessor
{
	public static final String ATTR_LIST_TYPE = "list";
	public static final String ATTR_LINKS = "links";
	public static final String ATTR_AREA = "area";
	public static final String ATTR_XML = "xml";
	public static final String ATTR_SEARCH = "search";
	public static final String ATTR_FILTER = "filter";
	
	public static final String ATTR_LINKS_DELIMITER = ",";
	
	//public static final String VAR_SEARCH = "search";
	
	protected final Log logger = LogFactory.getLog(getClass());
	protected List cards = null;
	
	//private static final int TYPE_UNKNOWN = 0;
	private static final int TYPE_LINKS = 1;
	private static final int TYPE_AREA = 2;
	private static final int TYPE_XML = 3;
	private static final int TYPE_SEARCH = 4;

    protected Card view = null;
    protected Search filter = null;

	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception
	{
		int type;
		if (tag.hasAttribute(ATTR_LIST_TYPE)) {
			if (ATTR_LINKS.equals(tag.getAttribute(ATTR_LIST_TYPE)))
				type = TYPE_LINKS;
			else if (ATTR_AREA.equals(tag.getAttribute(ATTR_LIST_TYPE)))
				type = TYPE_AREA;
			else if (ATTR_XML.equals(tag.getAttribute(ATTR_LIST_TYPE)))
				type = TYPE_XML;
			else if (ATTR_SEARCH.equals(tag.getAttribute(ATTR_LIST_TYPE)))
				type = TYPE_SEARCH;
			else
				throw new Exception("Unknown list type: " + tag.getAttribute(ATTR_LIST_TYPE));
		} else if (tag.hasAttribute(ATTR_LINKS))
			type = TYPE_LINKS;
		else if (tag.hasAttribute(ATTR_AREA))
			type = TYPE_AREA;
		else if (tag.hasAttribute(ATTR_XML))
			type = TYPE_XML;
		else
			throw new Exception("Document list not defined");

		if (tag.hasAttribute(ATTR_FILTER)) {
			if (type != TYPE_LINKS)
				logger.warn("Filtering supported only in links mode, will be ignored");
			String xml = cms.expandContent(tag.getAttribute(ATTR_FILTER), item);
			filter = new Search();
			filter.initFromXml(new ByteArrayInputStream(xml.getBytes("UTF-8")));
		}

		// fetching documents
		if (type == TYPE_LINKS) {
			if (!tag.hasAttribute(ATTR_LINKS))
				throw new Exception("No link attribute defined for list");
			if (tag.hasAttribute(ATTR_AREA) || tag.hasAttribute(ATTR_XML))
				logger.warn("Multiple list definition attributes");
			String[] attrs = tag.getAttribute(ATTR_LINKS).split(ATTR_LINKS_DELIMITER);
			for(String attr_name : attrs){
				Attribute attr = ContentUtils.getAttribute(item, attr_name.trim(), false, true);
				if (attr == null) {// || !Attribute.TYPE_CARD_LINK.equals(attr.getType()))
					if (isLegacyApproach()) {
						logger.info("Not a card link attribute: " + attr_name.trim());
						//throw new Exception("Not a card link attribute: " + attr_name.trim());
						continue;
					}						
					return false;
				}
				/*if(((CardLinkAttribute)attr).getLabelAttrId()!=null){
					continue;
				}*/
				
				if(cards == null)
					cards = getLinkedCards(item, cms, attr);
				else {
					List tempList = new java.util.ArrayList();
					tempList.addAll(cards);
					tempList.addAll(getLinkedCards(item, cms, attr));
					cards = tempList;
				}
			}
		} else if (type == TYPE_AREA) {
			if (tag.hasAttribute(ATTR_LINKS) || tag.hasAttribute(ATTR_XML))
				logger.warn("Multiple list definition attributes");
			ObjectId areaId;
			if (tag.hasAttribute(ATTR_AREA))
				areaId = new ObjectId(Card.class, Long.parseLong(
						cms.expandContent(tag.getAttribute(ATTR_AREA), item)));
			else
				areaId = cms.getCurrentSiteArea().getId();
			cards = cms.getContentDataServiceFacade().listAreaDocuments(areaId);
		} else if (type == TYPE_XML) {
			processXmlType(tag, item, cms);
		} else if (type == TYPE_SEARCH) {// && cms.getVariable(VAR_SEARCH) != null) {
			processSearchType(cms);
		}
		return containCards();
	}

    protected List getLinkedCards(Card item, ContentProducer cms, Attribute attr) {
        return cms.getLinkedCards(item, attr, filter);
    }

    protected boolean containCards() {
		
		return cards != null && cards.size() > 0;
	}

	protected void processXmlType(Tag tag, Card item, ContentProducer cms) throws Exception {
		
		if (!tag.hasAttribute(ATTR_XML))
			throw new Exception("No xml attribute defined for list");
		
		if (tag.hasAttribute(ATTR_AREA) || tag.hasAttribute(ATTR_LINKS))
			logger.warn("Multiple list definition attributes");
		final String xml = cms.expandContent(tag.getAttribute(ATTR_XML), item);
		if (xml == null || xml.trim().length() < 1)
			throw new Exception("Empty xml attribute defined for list");
		try {
			final Search search = new Search();
			search.initFromXml(new ByteArrayInputStream(xml.getBytes("UTF-8")));
			
			ObjectId folderId = new ObjectId(Card.class, cms.getVariable(ATTR_AREA));
			String sortColumnId = (String) cms.getRequest().getSessionAttribute(ContentProducer.SESS_ATTR_SORT_COLUMN_ID);
			Boolean straightOrder = (Boolean) cms.getRequest().getSessionAttribute(ContentProducer.SESS_ATTR_STRAIGHT_ORDER);
			String simpleSearchFilter = (String) cms.getVariable(ContentProducer.VAR_SIMPLE_SEARCH_FILTER);
			
			cards = cms.getContentDataServiceFacade().getDocumentsByFolder(folderId, simpleSearchFilter, search, 0, 0, 
					sortColumnId, straightOrder);
		} catch(Exception ex) {
			logger.info( "Fail to load search from xml >>>\n"+ xml+ "\n<<<" );
			throw ex;
		}
	}
	
	/**
	 * @param cms
	 */
	protected void processSearchType(ContentProducer cms) {
		//cards = searchCards((Search) cms.getVariable(VAR_SEARCH), cms.getService());
		cards = (List) cms.getVariable(ContentProducer.VAR_FOUND);
	}

    /**
     * temporary method for migrating to the new approach
     * @return
     */
    protected boolean isLegacyApproach(){
        if (view == null) return true;
        return !CardViewData.containsBean(view.getId().getId().toString());
    }
	
}
