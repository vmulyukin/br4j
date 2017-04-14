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
package com.aplana.dbmi.search.init;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.gui.BlockSearchView;
import com.aplana.dbmi.gui.SearchAttributeView;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.search.DataServiceBeanSetter;
import com.aplana.dbmi.search.SearchFilterPortletSessionBean;
import com.aplana.web.tag.util.StringUtils;

import java.util.HashMap;
import java.util.Map;


/**
 * Represents main class to copy values from Search to all specific search filter attributes.
 *  
 * @author skashanski
 */
public class SearchFilterInitializer {
	
	/** contains SearchFilterAttributeInitializer classes for  specific search Filter Attribute Class*/
	private Map<Class, SearchFilterAttributeInitializer> attrInitializersByClassMap = new HashMap<Class, SearchFilterAttributeInitializer>();

	/** contains SearchFilterAttributeInitializer classes for  specific search Filter Attribute Identifier(ObjectId)*/
	private Map<ObjectId, SearchFilterAttributeInitializer> attrInitializersByIdMap = new HashMap<ObjectId, SearchFilterAttributeInitializer>();

	/** search to copy values from */
	private Search search = null;
	
	private SearchFilterPortletSessionBean sessionBean = null;
	
	public  SearchFilterInitializer() {}
	
	public void setSearch(Search search) {
		this.search = search;
	}

	public void setSessionBean(SearchFilterPortletSessionBean sessionBean) {
		this.sessionBean = sessionBean;
	}

	public Map<Class, SearchFilterAttributeInitializer> getAttrInitializersByClassMap() {
		return attrInitializersByClassMap;
	}

	/**
	 * Returns specific {@link SearchFilterAttributeInitializer} for passed  {@link Attribute} class 
	 */
	public SearchFilterAttributeInitializer getSearchFilterAttributeInitializerByClass(Class searchFilterAttributeInitializerClass) {
		return attrInitializersByClassMap.get(searchFilterAttributeInitializerClass);
	}

	/**
	 * 
	 * Returns specific {@link SearchFilterAttributeInitializer} for passed  {@link Attribute} identifier
	 */
	public SearchFilterAttributeInitializer getSearchFilterAttributeInitializerById(ObjectId searchFilterAttributeId) {
		return attrInitializersByIdMap.get(searchFilterAttributeId);
	}	
	
	/**
	 * 
	 * Returns specific {@link SearchFilterAttributeInitializer} for passed  {@link Attribute} 
	 */
	public SearchFilterAttributeInitializer  getSearchFilterAttributeInitializer(Attribute searchFilterAttribute) {
		//firstly try to get specific Initializer for passed attribute identifier
		SearchFilterAttributeInitializer result = getSearchFilterAttributeInitializerById(searchFilterAttribute.getId());
		if (result == null) //then try to get initializer for passed attribute class
			result = getSearchFilterAttributeInitializerByClass(searchFilterAttribute.getClass());
		
		return result;
	}

	public void setAttrInitializersByClassMap(
			Map<Class, SearchFilterAttributeInitializer> attrInitializersByClassMap) {
		this.attrInitializersByClassMap = attrInitializersByClassMap;
	}

	public void setAttrInitializersByIdMap(
			Map<ObjectId, SearchFilterAttributeInitializer> attrInitializersByIdMap) {
		this.attrInitializersByIdMap = attrInitializersByIdMap;
	}

	public void initialize() {
		initializeSearchParameters();
		initializeAttributes();
	}

	protected void initializeSearchParameters() {
		if (StringUtils.hasText(search.getWords())){
			sessionBean.setSearchWords(search.getWords());
			sessionBean.setSearchStrictWords(search.isStrictWords());
		}else{//otherwise clear search words
			sessionBean.setSearchWords("");
			sessionBean.setSearchStrictWords(false);
		}
		
		sessionBean.setByMaterial(search.isByMaterial());
	}

	protected void initializeAttributes() {	
		for (BlockSearchView blockSearchView : sessionBean.getSearchBlockViews()) {
			for (SearchAttributeView searchAttributeView : blockSearchView.getSearchAttributes()) {
				Attribute searchFilterAttribute = searchAttributeView.getAttribute();
				//clear all fields at first
				searchFilterAttribute.clear();
				SearchFilterAttributeInitializer searchFilterAttributeInitializer =
						getSearchFilterAttributeInitializer(searchFilterAttribute);
				if (searchFilterAttributeInitializer == null) 
					continue;
				
				if (searchFilterAttributeInitializer.getPostInitStrategy() != null) 
					initDataServiceBean(searchFilterAttributeInitializer.getPostInitStrategy());
				
				searchFilterAttributeInitializer.initialize(searchFilterAttribute, search);
			}
		}
	}

	private void initDataServiceBean(SearchFilterAttributePostInitStrategy postInitStrategy) {
		if (!(postInitStrategy instanceof DataServiceBeanSetter))
			return;
		
		((DataServiceBeanSetter)postInitStrategy).setDataServiceBean(sessionBean.getServiceBean());
	}
}