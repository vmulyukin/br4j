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
package com.aplana.dbmi.search;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.gui.BlockSearchView;
import com.aplana.dbmi.gui.SearchAttributeView;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.util.Init;

import java.util.Map;

/**
 * Represents class for initialization {@link Search} object
 * It copies data from search filter attributes
 *   
 * @author skashanski
 *
 */
public class SearchInitializer {

	/** contains SearchAttributeInitializer classes for  specific Search Attribute Class*/
	private Map<Class<? extends Attribute>, SearchAttributeInitializer> attrInitializersByClassMap = Init.hashMap();
	
	private Search search = null;
	
	private SearchFilterPortletSessionBean sessionBean = null;

	public Map<Class<? extends Attribute>, SearchAttributeInitializer> getAttrInitializersByClassMap() {
		return attrInitializersByClassMap;
	}

	public void setAttrInitializersByClassMap(Map<Class<? extends Attribute>, SearchAttributeInitializer> attrInitializersByClassMap) {
		this.attrInitializersByClassMap = attrInitializersByClassMap;
	}

	public Search getSearch() {
		return search;
	}

	public void setSearch(Search search) {
		this.search = search;
	}

	public SearchFilterPortletSessionBean getSessionBean() {
		return sessionBean;
	}

	public void setSessionBean(SearchFilterPortletSessionBean sessionBean) {
		this.sessionBean = sessionBean;
	}

	@SuppressWarnings("unchecked")
	public <T extends Attribute> SearchAttributeInitializer<T> getSearchAttributeInitializerByClass(Class<T> attributeClass) {
		return attrInitializersByClassMap.get(attributeClass);
	}
	
	protected void initializeParameters() {
		search.setWords(sessionBean.getSearchWords());
		search.setStrictWords(sessionBean.isSearchStrictWords());
		search.setByMaterial(sessionBean.isByMaterial());
	}

	protected void initializeAttributes(boolean saveMode) {
		for(BlockSearchView blockSearchView : sessionBean.getSearchBlockViews()) {
			for(SearchAttributeView searchAttributeView : blockSearchView.getSearchAttributes()) {
				Attribute searchFilterAttribute = searchAttributeView.getAttribute();
				SearchAttributeInitializer searchAttributeInitializer =
							getSearchAttributeInitializerByClass(searchFilterAttribute.getClass());
				if (searchAttributeInitializer == null) 
					continue;
				searchAttributeInitializer.setAttribute(searchFilterAttribute);
				searchAttributeInitializer.initialize(search, saveMode );
			}
		}
	}

	public void initialize(boolean saveMode) {
		initializeParameters();
		initializeAttributes(saveMode);
	}
}
