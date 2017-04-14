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
package com.aplana.dbmi.column;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletRequest;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.showlist.SearchAdapter;

/**
 *	Abstract class for <code>ColumnViewer</code> interface 
 *
 */
public abstract class CommonColumnViewer implements ColumnViewer {

	public static final String PARAM_COLS="cols";
	public static final String PARAM_PREFIX="prefix";
	public static final String PARAM_SUFFIX="suffix";
	public static final String PARAM_COLUMN="column";
	public static final String PARAM_SEARCH="search";
	public static final String PARAM_DEFAULT_VALUE="defaultValue";

	public static final String DEFAULT_SPLIT=",";

	protected Search search=null;
	protected String words=null;

	public void initViewer(PortletRequest request, String searchWords)
			throws DataException, ServiceException {
		parseParamsAttributeForSearch(searchWords);
		search = buildSearch();
		DataServiceBean serviceBean = getDataServiceBean(request);
		createContent(loadCards(serviceBean, search).getData());
	}

	protected DataServiceBean getDataServiceBean(PortletRequest request) {		
		return PortletUtil.createService(request);		
	}

	protected SearchAdapter loadCards(DataServiceBean serviceBean, Search search) throws DataException, ServiceException{
		SearchAdapter adapter = new SearchAdapter();
		adapter.executeSearch(serviceBean, search);
		return adapter;
	}

	protected void parseParamsAttributeForSearch(String searchWords){
		words = searchWords;
	}

	protected abstract void createContent(List<ArrayList<Object>> columnData);

	protected abstract Search buildSearch();
}
