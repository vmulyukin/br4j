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
package com.aplana.ireferent.endpoint.impl;

import java.util.HashMap;
import javax.xml.ws.WebServiceContext;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.PagedSearchObjectFactory;
import com.aplana.ireferent.WSObjectFactory;
import com.aplana.ireferent.types.WSOCollection;
import com.aplana.ireferent.types.WSOContext;
import com.aplana.ireferent.types.WSOSearchRequest;
import com.aplana.ireferent.types.WSOSearchResult;
import com.aplana.ireferent.util.ServiceUtils;


public class GetFavoritesOperation implements
	ServiceOperation<WSOSearchResult> {
    private static final String CONFIG_FILE_FORMAT = "getDocuments%s.xml";
    private static final String[] SET_IDS = { "Favorites" };
    
    private final boolean includeAttachments;
    private final WSOContext context;
    private final boolean isMObject;
    private final WebServiceContext contextEndpoint;
    private final WSOSearchRequest searchRequest;

    public GetFavoritesOperation(boolean isMObject, boolean includeAttachments,
    		WSOSearchRequest searchRequest, WSOContext context, WebServiceContext contextEndpoint) {
    	this.includeAttachments = includeAttachments;
    	this.context = context;
    	this.isMObject = isMObject;
    	this.searchRequest = searchRequest;
    	this.contextEndpoint = contextEndpoint;
        }

    public String getName() {
    	return "getFavorites";
    }

    public Object[] getParameters() {
    	return new Object[] {  this.isMObject, this.includeAttachments,
			this.context  };
    }

    public void processInputData() {
    }

	public WSOSearchResult execute() throws Exception {
		DataServiceBean serviceBean = ServiceUtils
			.authenticateUser(this.context, this.contextEndpoint);
		String pageNum = "";
	    String pageSize = "";
	    String keyword = "";
		if (null != searchRequest) {
			pageNum = searchRequest.getPageNum();
		    pageSize = searchRequest.getPageSize();
		    keyword = searchRequest.getKeyword();
		}
		int pageNumberInt = (!ServiceUtils.isInteger(pageNum)) ? 0 : Integer.parseInt(pageNum);
		int pageSizeInt = (!ServiceUtils.isInteger(pageSize))? 0 : Integer.parseInt(pageSize);
			WSObjectFactory objectFactory = WSObjectFactory
					.createPagedSearchObjectFactory(serviceBean, "Document", pageNumberInt,
							pageSizeInt, keyword);
		objectFactory.setMObject(this.isMObject);
		objectFactory.setUsingMObjectsAllLevels("attachments",
			!this.includeAttachments);
		HashMap<WSObjectFactory, String[]> setObjectFactory = new HashMap<WSObjectFactory, String[]>();
		setObjectFactory.put(objectFactory, SET_IDS);
		ObjectsGetterByConfig objectsCreator = new ObjectsGetterByConfig(
			CONFIG_FILE_FORMAT, setObjectFactory);
		objectsCreator.setAddExtension();
		WSOCollection favorites = objectsCreator.createObjects();
		WSOSearchResult result = new WSOSearchResult();
		result.getData().addAll(favorites.getData());
		PagedSearchObjectFactory pagedSearchObjectFactory = (PagedSearchObjectFactory) objectFactory;
		int totalCount = pagedSearchObjectFactory.getTotalCount();
		result.setTotalCount(totalCount);
		return result;
    }
}