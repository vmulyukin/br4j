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
package com.aplana.dbmi.model.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.PersonalSearch;
import com.aplana.dbmi.model.SearchBean;

public class WebSearchBean extends SearchBean {

    public static final String SEARCH_ACTION = "SEARCH_ACTION";
    public static final String EXTENDED_SEARCH_FORM_NAME = "extendedSearchForm";
    public static final String ATTRIBUTE_SEARCH_ACTION = "ATTRIBUTE_SEARCH_ACTION";
    public static final String SAVE_PERSONAL_SEARCH_ACTION = "SAVE_PERSONAL_SEARCH_ACTION";
    public static final String DELETE_PERSONAL_SEARCH_ACTION = "DELETE_PERSONAL_SEARCH_ACTION";
    public static final String LOAD_PERSONAL_SEARCH_ACTION = "LOAD_PERSONAL_SEARCH_ACTION";
    public static final String OPEN_EXTENDED_SEARCH_ACTION = "OPEN_EXTENDED_SEARCH_ACTION";
    public static final String CLOSE_EXTENDED_SEARCH_ACTION = "CLOSE_EXTENDED_SEARCH_ACTION";

    public static final String SEARCH_VIEW_TYPE_SMALL = "small";
    public static final String SEARCH_VIEW_TYPE_VERTICAL = "vertical";
    public static final String SEARCH_VIEW_TYPE_FULL = "full";

    private Boolean isExtendedSearch = Boolean.FALSE;
    private Boolean isAttributeSearch = Boolean.FALSE;
    private Boolean isPersonalSearch = Boolean.FALSE;

    private Boolean showProjectNumberSearch;
    
    private String extendedSearchPath;
    private String extendedSearchForm;
    
	// Personal searches for current user
	private List<PersonalSearch> personalSearches = new ArrayList<PersonalSearch>();

    private String resolutionSearchForm;

    private String submitSearchPath;
    private String searchViewType;

    private List viewTemplates;
    private List viewAttributeTemplates;
    private WebBlock viewMainBlock;
    private Collection viewBlocks1;
    private Collection viewBlocks2;
    private String action;

    private Boolean showTemplates = Boolean.TRUE;

    private Boolean isAllTemplates = Boolean.FALSE;
    
    //������������ �� �� ������ �������, ����������� ����� ������ �� ���� ����
    private Boolean canUseWholeBase = Boolean.FALSE;
    
    //����� ������ ������������ search �������, ���� �������� ����� ������ �� ���� ����
    private Search currentTabSearch;

    private String name;
    private String description;
    private Long id;

    private PersonalSearch personalSearch = null;
    private String message;

    private Map valuesDate;
    private Collection columns; // Collection<SearchResult.Columns>
    
    private Boolean visibleCurrentYear = Boolean.FALSE;
    
    //����������� ������ �� ��������� ���. ������� (JBR_REGD_REGNUM � JBR_REGD_NUMOUT)
    //�� ��������� �������� == 0, ����� �� ���� ��������� ��������
    //���� == 1, �� ������ ������ �� ������ �������� (JBR_REGD_REGNUM)
    //���� == 2, �� ������ �� ���� ��������� (JBR_REGD_REGNUM � JBR_REGD_NUMOUT) �������� OR
    private Integer searchByRegnum = 0;
    private Boolean hideExtendedLink = Boolean.FALSE;

	/**
	 * resets the fields for reuse bean.
     */
    public void reset() {
		getTemplates().clear();
	}

	public Boolean getIsExtendedSearch() {
        return isExtendedSearch;
    }

    public void setIsExtendedSearch(Boolean isExtendedSearch) {
        this.isExtendedSearch = isExtendedSearch;
    }

    public List getViewTemplates() {
        return viewTemplates;
    }

    public void setViewTemplates(List viewTemplates) {
        this.viewTemplates = viewTemplates;
    }

    public List getViewAttributeTemplates() {
        return viewAttributeTemplates;
    }

    public void setViewAttributeTemplates(List viewAttributeTemplates) {
        this.viewAttributeTemplates = viewAttributeTemplates;
    }

    public Boolean getIsAttributeSearch() {
        return isAttributeSearch;
    }

    public void setIsAttributeSearch(Boolean isAttributeSearch) {
        this.isAttributeSearch = isAttributeSearch;
    }

    public String getAction() {
        return action;
    }

    public Boolean getShowProjectNumberSearch() {
        return showProjectNumberSearch;
    }

    public void setShowProjectNumberSearch(Boolean showProjectNumberSearch) {
        this.showProjectNumberSearch = showProjectNumberSearch;
    }
    public void setAction(String action) {
        this.action = action;
    }

    public Collection getViewBlocks1() {
        return viewBlocks1;
    }

    public void setViewBlocks1(Collection viewBlocks1) {
        this.viewBlocks1 = viewBlocks1;
    }

    public Collection getViewBlocks2() {
        return viewBlocks2;
    }

    public void setViewBlocks2(Collection viewBlocks2) {
        this.viewBlocks2 = viewBlocks2;
    }

    public Boolean getShowTemplates() {
        return showTemplates;
    }

    public void setShowTemplates(Boolean showTemplates) {
        this.showTemplates = showTemplates;
    }

    public WebBlock getViewMainBlock() {
        return viewMainBlock;
    }

    public void setViewMainBlock(WebBlock viewMainBlock) {
        this.viewMainBlock = viewMainBlock;
    }

    public Boolean getIsPersonalSearch() {
        return isPersonalSearch;
    }

    public void setIsPersonalSearch(Boolean isPersonalSearch) {
        this.isPersonalSearch = isPersonalSearch;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PersonalSearch getPersonalSearch() {
        return personalSearch;
    }

    public void setPersonalSearch(PersonalSearch personalSearch) {
        this.personalSearch = personalSearch;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSearchViewType() {
        return searchViewType;
    }

    public void setSearchViewType(String searchViewType) {
        this.searchViewType = searchViewType;
    }

    public String getExtendedSearchPath() {
        return extendedSearchPath;
    }

    public void setExtendedSearchPath(String extendedSearchPath) {
        this.extendedSearchPath = extendedSearchPath;
    }

    public String getExtendedSearchForm() {
		return extendedSearchForm;
	}

	public void setExtendedSearchForm(String extendedSearchForm) {
		this.extendedSearchForm = extendedSearchForm;
	}

    public boolean isFullSearchView() {
        return searchViewType == null || SEARCH_VIEW_TYPE_FULL.equals(searchViewType);
    }

    public boolean isSmallSearchView() {
        return SEARCH_VIEW_TYPE_SMALL.equals(searchViewType);
    }

    public boolean isVerticalSearchView() {
        return SEARCH_VIEW_TYPE_VERTICAL.equals(searchViewType);
    }

    public String getSubmitSearchPath() {
        return submitSearchPath;
    }

    public void setSubmitSearchPath(String submitSearchPath) {
        this.submitSearchPath = submitSearchPath;
    }

    public Boolean getIsAllTemplates() {
        return isAllTemplates;
    }

    public void setIsAllTemplates(Boolean isAllTemplates) {
        this.isAllTemplates = isAllTemplates;
    }
    
    public Boolean getCanUseWholeBase() {
        return canUseWholeBase;
    }

    public void setCanUseWholeBase(Boolean canUseWholeBase) {
        this.canUseWholeBase = canUseWholeBase;
    }
    
    public Search getCurrentTabSearch() {
        return currentTabSearch;
    }

    public void setCurrentTabSearch(Search currentTabSearch) {
        this.currentTabSearch = currentTabSearch;
    }
    
    public void setValuesDate(Map valuesDate) {
    	this.valuesDate = valuesDate;
    }
    public Map getValuesDate() {
    	return valuesDate;
    }
    public void addValueDate(String name, String date) {
    	if (valuesDate == null) {
    		valuesDate = new HashMap();
    	}
    	valuesDate.put(name, date);
    }

	/**
	 * @return the desired search columns SearchResult.Column[]
	 */
	public Collection getColumns() {
		return this.columns;
	}

	/**
	 * @param columns set Collection<SearchResult.Column>
	 */
	public void setColumns(Collection columns) {
		this.columns = columns;
	}
	
	public void setResolutionSearchForm(String resolutionSearchForm) {
		this.resolutionSearchForm = resolutionSearchForm;
	}
	
	public String getResolutionSearchForm() {
		return resolutionSearchForm;
	}

	public Boolean getVisibleCurrentYear() {
		return visibleCurrentYear;
	}

	public void setVisibleCurrentYear(Boolean visibleCurrentYear) {
		this.visibleCurrentYear = visibleCurrentYear;
	}
	
	public Integer getSearchByRegnum() {
		return searchByRegnum;
	}
	
	public void setSearchByRegnum(Integer searchByRegnum) {
		this.searchByRegnum = searchByRegnum;
	}
	
    public Boolean getHideExtendedLink() {
		return hideExtendedLink;
	}

	public void setHideExtendedLink(Boolean hideExtendedLink) {
		this.hideExtendedLink = hideExtendedLink;
	}
	
	public List<PersonalSearch> getPersonalSearches() {
		return personalSearches;
	}

	public void setPersonalSearches(List<PersonalSearch> personalSearches) {
		this.personalSearches = personalSearches;
	}
}
