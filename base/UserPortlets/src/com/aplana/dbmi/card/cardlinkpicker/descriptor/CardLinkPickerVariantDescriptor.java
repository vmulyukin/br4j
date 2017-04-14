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
package com.aplana.dbmi.card.cardlinkpicker.descriptor;

import java.util.List;
import java.util.Map;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.Search.Filter;
import com.aplana.dbmi.card.hierarchy.CardFilterCondition;
import com.aplana.dbmi.card.hierarchy.descriptor.HierarchyDescriptor;
import com.aplana.dbmi.card.hierarchy.descriptor.LinkDescriptor;
import com.aplana.dbmi.card.hierarchy.descriptor.ReplaceDescriptor;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;

public class CardLinkPickerVariantDescriptor {
	private List columns;
	private Search search;
	private HierarchyDescriptor hierarchyDescriptor;
	private ObjectId searchAttrId;	
	private ObjectId choiceReferenceValueId;
	private CardLinkPickerVariantCondition conditions;
	private CardFilterCondition selectableCardsCondition;
	private List searchDependencies;
	private boolean useSoftSearch = false;		// ��� ������� ��������� �������� ��� ��������, ���� ����� �� ������ searchDependencies ������ (�.�. ������������ ������ �����)
	private boolean useAltSearch = false; 		//������������ ����� � ��������������� �������������
	private Long requiredPermissions = Filter.CU_READ_PERMISSION; 
	private boolean hideAllValues;
	private String title;
	/**
	 * If not null list elements will be added. Adding list element results into adding all children of list; field describes connection of list with
	 * main card set. 
	 */
	private LinkDescriptor list;
	
	/**
	 * ���� ��������, �� ����������� ��������� �������� ������������� ������� � ������ ������� �������� 
	 */
	private ReplaceDescriptor replaceAttr;
	
	public static class SearchDependency {
		private ObjectId filterAttrId;
		private ObjectId valueAttrId;
		private String special;
		private SpecialType specialType;
		private String mapperClassPath;
		private Map<String, String> mapperParameterMap;
		private List<ObjectId> referenceToCard;
		private boolean useParent;
		//�������� ���� ����������� ��� ��������������� ������
		private boolean alternativeDependency;

		public SearchDependency(ObjectId filterAttrId, ObjectId valueAttrId, String mapperClassPath, Map<String, String> mapperParameterMap,boolean alternativeDependency) {
			ObjectIdUtils.validateId(filterAttrId, CardLinkAttribute.class);
			ObjectIdUtils.validateId(valueAttrId, CardLinkAttribute.class);
			this.filterAttrId = filterAttrId;
			this.valueAttrId = valueAttrId;
			this.mapperClassPath = mapperClassPath;
			this.mapperParameterMap = mapperParameterMap;
			this.alternativeDependency = alternativeDependency;
		}

		public SearchDependency(ObjectId filterAttrId, ObjectId valueAttrId, boolean alternativeDependency) {
			this(filterAttrId, valueAttrId, null, null,alternativeDependency);
		}
		
		public SearchDependency(ObjectId filterAttrId, String special, boolean alternativeDependency) {
			ObjectIdUtils.validateId(filterAttrId, CardLinkAttribute.class, BackLinkAttribute.class);
			if (special == null || special.length() == 0)
				throw new IllegalArgumentException("Special can't be null or empty");
			this.filterAttrId = filterAttrId;
			if(special.indexOf(":") != -1){
				this.specialType = SpecialType.valueOf(special.split(":")[0]);
				this.special = special.split(":")[1];
			} else {
				this.special = special;
				this.specialType = SpecialType.RECURSIVE;
			}
			this.alternativeDependency = alternativeDependency;
		}
		
		
		public ObjectId getFilterAttrId() {
			return filterAttrId;
		}
		
		public ObjectId getValueAttrId() {
			return valueAttrId;
		}
		
		public String getSpecialValue() {
			return special;
		}
		
		public SpecialType getSpecialType(){
			return specialType;
		}
		
		public boolean isStrictSpecialType(){
			return SpecialType.STRICT.equals(specialType); 
		}
		
		public boolean isSpecial() {
			return special != null;
		}

		public String getMapperClassPath() {
			return mapperClassPath;
		}
		
		public Map<String, String> getMapperParameterMap() {
			return mapperParameterMap;
		}

		public boolean isMapperDefined() {
			return mapperClassPath != null && !mapperClassPath.trim().isEmpty();
		}

		public boolean isAlternativeDependency() {
			return alternativeDependency;
		}

		public List<ObjectId> getReferenceToCard() {
			return referenceToCard;
		}

		public void setReferenceToCard(List<ObjectId> referenceToCard) {
			this.referenceToCard = referenceToCard;
		}

		public boolean isUseParent() {
			return useParent;
		}

		public void setUseParent(boolean useParent) {
			this.useParent = useParent;
		}
		
	}

	public Long getRequiredPermissions() {
		return requiredPermissions;
	}
	
	public void setRequiredPermissions(Long requiredPermissions) {
		this.requiredPermissions = requiredPermissions;
	}
	
	public List getColumns() {
		return columns;
	}
	public void setColumns(List columns) {
		this.columns = columns;
	}
	public Search getSearch() {
		Search result = search.makeCopy();
		SearchResult.Column searchColumn = new SearchResult.Column();
		searchColumn.setAttributeId(searchAttrId);
		result.getColumns().add(searchColumn);
		return result;
	}
	public void setSearch(Search search) {
		this.search = search;
	}
	public HierarchyDescriptor getHierarchyDescriptor() {
		return hierarchyDescriptor;
	}
	public void setHierarchyDescriptor(HierarchyDescriptor hierarchyDescriptor) {
		this.hierarchyDescriptor = hierarchyDescriptor;
	}
	public ObjectId getSearchAttrId() {
		return searchAttrId;
	}
	public void setSearchAttrId(ObjectId searchAttrId) {
		this.searchAttrId = searchAttrId;
	}
	public ObjectId getChoiceReferenceValueId() {
		return choiceReferenceValueId;
	}
	public void setChoiceReferenceValueId(ObjectId choiceReferenceValueId) {
		this.choiceReferenceValueId = choiceReferenceValueId;
	}
	public String getAlias() {
		return choiceReferenceValueId == null ? CardLinkPickerDescriptor.DEFAULT_REF_ID : choiceReferenceValueId.getId().toString();
	}
	public CardFilterCondition getSelectableCardsCondition() {
		return selectableCardsCondition;
	}
	public void setSelectableCardsCondition(
			CardFilterCondition selectableCardsCondition) {
		this.selectableCardsCondition = selectableCardsCondition;
	}
	public List getSearchDependencies() {
		return searchDependencies;
	}
	public void setSearchDependencies(List searchDependencies) {
		this.searchDependencies = searchDependencies;
	}
	
	public boolean checkConditions(Card card){
		return conditions.checkCondition(card);
	}

	public CardLinkPickerVariantCondition getConditions() {
		return conditions;
	}

	public void setConditions(CardLinkPickerVariantCondition conditions) {
		this.conditions = conditions;
	}

	public void setHideAllValues(boolean hideAllValues) {
		this.hideAllValues = hideAllValues;
}

	public boolean isHideAllValues() {
		return hideAllValues;
	}

	public LinkDescriptor getList() {
		return list;
	}

	public void setList(LinkDescriptor list) {
		this.list = list;
	}

	public boolean isUseSoftSearch() {
		return useSoftSearch;
	}

	public void setUseSoftSearch(boolean useSoftSearch) {
		this.useSoftSearch = useSoftSearch;
	}

	public boolean isUseAltSearch() {
		return useAltSearch;
	}

	public void setUseAltSearch(boolean useAltSearch) {
		this.useAltSearch = useAltSearch;
	}

	public ReplaceDescriptor getReplaceAttr() {
		return replaceAttr;
	}

	public void setReplaceAttr(ReplaceDescriptor replaceAttr) {
		this.replaceAttr = replaceAttr;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	enum SpecialType {
		STRICT, RECURSIVE
		
	}
	
}
