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
package com.aplana.cms;

import java.util.Collection;
import java.util.List;

import com.aplana.cms.cache.AreaByPortalPageCache;
import com.aplana.cms.view_template.CardViewData;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;


/**
 * Represents Content View configuration Data Adapter
 *  
 * @author skashanski
 *
 */
public class ContentViewConfigAdapter implements ContentViewConfigInterface, ContentProducerSetterInterface  {
	
	private ContentProducer contentProducer = null;
	
	
	public void setContentProducer(ContentProducer contentProducer) {
		this.contentProducer = contentProducer; 
		
	}
	

	public ObjectId findAreaIdByPortalPageName(String pageId, String navigator) throws DataException, ServiceException {
		long[] permissionTypesArray = ContentUtils.getPermissionTypes(Search.Filter.CU_READ_PERMISSION);
        int personId = Integer.parseInt(contentProducer.getService().getPerson().getId().getId().toString());
        
        AreaByPortalPageCache areaCache = AreaByPortalPageCache.instance();
        
        ObjectId areaId = areaCache.getAreaIdByPortalPage(pageId, navigator, personId, permissionTypesArray);
        if (null != areaId) {
            return areaId;
        }
        
        areaId = contentProducer.findAreaIdByPortalPageName(pageId, navigator);
        areaCache.setAreaIdByPortalPage(pageId, navigator, personId, permissionTypesArray, areaId);
        
        return areaId;	
	}

	public Card getCardById(ObjectId cardId) throws DataException,
			ServiceException {

		return contentProducer.getViewCardById(cardId);
		
	}

	public List<Card> getChildren(ObjectId areaId) {

		return contentProducer.getChildrenCards(areaId);
	}

	public Card getContent(ObjectId contentId) throws DataException,
			ServiceException {

		return contentProducer.getContent(contentId);
	}

	public String getContentPresentation(ObjectId viewId) {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectId getDefaultContent(Card area) throws DataException,
			ServiceException {
		// TODO Auto-generated method stub
		return null;
	}


	public Collection<Card> getDefaultViews(String viewIds)
			throws DataException, ServiceException {

		return contentProducer.getDefaultViews(viewIds);
	}

	public Card getNavigatorById(ObjectId navigatorId) {

		return null;
		
	}

	public Card getSiteArea(ObjectId areaId) throws DataException,
			ServiceException {

		return contentProducer.getSiteArea(areaId);
	}

	public Card getViewCardById(ObjectId id) throws DataException,
			ServiceException {

		return contentProducer.getViewCardById(id);
	}

	public Collection<Card> getLinkedCards(Card card, LinkAttribute attr,
			Search filter, ObjectId areaId) throws DataException,
			ServiceException {

		return contentProducer.getLinkedCards(attr, filter);
	}

    public CardViewData getCardViewData(ObjectId viewId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private long[] convertToArray(List<Long> permissionTypes) {
        long[] array = new long[permissionTypes.size()];

        int i = 0;
        for (Long permissionType : permissionTypes) {
            array[i++] = permissionType.longValue();
        }

        return array;
    }

}
