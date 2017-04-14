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

import com.aplana.dbmi.card.EditorsDataContainingSessionBean;
import com.aplana.dbmi.gui.BlockSearchView;
import com.aplana.dbmi.gui.EmbeddablePortletFormManager;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonalSearch;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.Init;
import com.aplana.dbmi.service.AsyncDataServiceBean;

import java.util.*;


/**
 * Represents Bean for storing  SearchFilterPortlet instance's data in portlet session.
 */
public class SearchFilterPortletSessionBean implements EditorsDataContainingSessionBean {
	private AsyncDataServiceBean serviceBean = null;
	private ResourceBundle resourceBundle = null;

	public enum PageState {EXTENDED_SEARCH_PAGE, WORKSTATION_EXTENDED_SEARCH_PAGE};

	/**
	 * page where this session bean is used(extendedSearch/workstationExtendedSearch)...by default equals "extendedSearch"
	 */
	private PageState page =  PageState.EXTENDED_SEARCH_PAGE;

	/**
	 * message to display error/info messages on UI side
	 */
	private String message = null;

	/**
	 * Url to return to original place
	 */
	private String backURL = null;

	private EmbeddablePortletFormManager portletFormManager = new EmbeddablePortletFormManager();

	private Map< ObjectId, Map<String, Object>> searchEditorsData = Init.hashMap();

	/**
	 * represents block of displayed attributes
	 */
	private List<BlockSearchView> searchBlockViews = new ArrayList<BlockSearchView>();

	/** search words string  */
	private String searchWords = "";
	private boolean searchStrictWords = false;
	/**  flag to indicate search by material..full-text se4arch */
	private boolean byMaterial = false;

	/* search page card's templates */
	private List<Template> searchPageTemplates = new ArrayList<Template>();

	/** Personal searches for current user */
	private List<PersonalSearch> personalSearches = new ArrayList<PersonalSearch>();

	private String extendedSearchFormName = null;
	private String parentTemplateId = null;
	private String header;
	private String switchNavigatorLink;
	private boolean saveMode = false;

	public boolean isSaveMode() {
		return saveMode;
	}

	public void setSaveMode(boolean saveMode) {
		this.saveMode = saveMode;
	}

	public SearchFilterPortletSessionBean() {
		super();
	}

	public AsyncDataServiceBean getServiceBean() {
		return serviceBean;
	}

	public boolean isByMaterial() {
		return byMaterial;
	}

	public void setByMaterial(boolean byMaterial) {
		this.byMaterial = byMaterial;
	}

	public void setServiceBean(AsyncDataServiceBean serviceBean) {
		this.serviceBean = serviceBean;
	}

	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public void setResourceBundle(ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;
	}

	public PageState getPage() {
		return page;
	}

	public void setPage(PageState page) {
		this.page = page;
	}

	public List<PersonalSearch> getPersonalSearches() {
		return personalSearches;
	}

	public void setPersonalSearches(List<PersonalSearch> personalSearches) {
		this.personalSearches = personalSearches;
	}

	/**
	 * Returns active search page template
	 */
	public ObjectId getActiveTemplate() {

		Template firstTemplate = getSearchPageTemplate();
		if (firstTemplate != null )
			return firstTemplate.getId();
		else
			return null;
	}

	public String getSearchWords() {
		return searchWords;
	}

	public void setSearchWords(String searchWords) {
		this.searchWords = searchWords;
	}

	public boolean isSearchStrictWords() {
		return searchStrictWords;
	}

	public void setSearchStrictWords(boolean searchStrictWords) {
		this.searchStrictWords = searchStrictWords;
	}

	public String getExtendedSearchFormName() {
		return extendedSearchFormName;
	}

	public void setExtendedSearchFormName(String extendedSearchFormName) {
		this.extendedSearchFormName = extendedSearchFormName;
	}

	public List<Template> getSearchPageTemplates() {
		return searchPageTemplates;
	}

	public Template getSearchPageTemplate() {

		if (searchPageTemplates.size() == 1 )
			return searchPageTemplates.iterator().next();
		else
			return null;

	}

	public void setSearchPageTemplates(List<Template> searchPageTemplates) {
		this.searchPageTemplates = searchPageTemplates;
	}

	public String getBackURL() {
		return backURL;
	}

	public void setBackURL(String backURL) {
		this.backURL = backURL;
	}

	public void setSearchEditorData(ObjectId attrId, String key, Object data) {

		Map<String, Object> editorData = searchEditorsData.get(attrId);

		if (editorData == null) {
			editorData = new HashMap<String, Object>();
			searchEditorsData.put(attrId, editorData);
		}

		editorData.put(key, data);
	}

	@SuppressWarnings("unchecked")
	public <T> T getSearchEditorData(ObjectId attrId, String key) {
		Map<String, Object> editorData = searchEditorsData.get(attrId);
		return (editorData == null) ? null : (T) editorData.get(key);
	}

	public void resetAttributeEditorData(ObjectId attrId) {
		if (attrId == null)
			return;
		searchEditorsData.remove(attrId);
	}

	public void resetAttributeEditorData(ObjectId attrId, String key) {
		if (attrId == null || key == null)
			return;

		Map<String, Object> editorData = searchEditorsData.get(attrId);
		if (editorData != null)
			editorData.remove(key);

	}

	public void clearAttributeEditorsData() {
		searchEditorsData.clear();
	}

	public void clearSearchBlockViews() {
		searchBlockViews.clear();
	}

	public void clearCommonParameters() {
		searchWords = "";
		byMaterial = false;
	}

	public EmbeddablePortletFormManager getPortletFormManager() {
		return portletFormManager;
	}

	public List<BlockSearchView> getSearchBlockViews() {
		return searchBlockViews;
	}

	public void setSearchBlockViews(List<BlockSearchView> searchBlockViews) {
		this.searchBlockViews = searchBlockViews;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void clearMessage() {
		this.message = null;
	}

	public void setParentTemplateId(String parentTemplateId) {
		this.parentTemplateId = parentTemplateId;
	}

	public String getParentTemplateId() {
		return parentTemplateId;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getSwitchNavigatorLink() {
		return switchNavigatorLink;
	}

	public void setSwitchNavigatorLink(String switchNavigatorLink) {
		this.switchNavigatorLink = switchNavigatorLink;
	}

	public Object getEditorData(ObjectId attrId, String key) {
		return getSearchEditorData(attrId, key);
	}

	public void setEditorData(ObjectId attrId, String key, Object data) {
		setSearchEditorData(attrId, key, data);
	}
}
