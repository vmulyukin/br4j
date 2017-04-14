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
package com.aplana.dbmi.action;

import com.aplana.dbmi.model.*;

import java.io.*;
import java.util.*;
//import com.aplana.dbmi.service.impl.query.ExecFetchCardsFromIdsArray;

/**
 * Model class representing result of {@link Search} action.
 * <br>
 * This class is often used as a backend for card list tables
 * shown in GUI.
 * <br>
 * Note that {@link Card cards} containing in the SearchResult object will be 
 * initialized only partially and will contain attributes specified in
 * {@link #getColumns() columns} property only. Full copy of {@link Card} to
 * work with could be received by executing 
 * {@link com.aplana.dbmi.service.DataServiceBean#getById(ObjectId)}
 */
public class SearchResult implements Serializable
{
	private static final long serialVersionUID = 5L;
	private String nameRu;
	private String nameEn;
	private Collection<Column> columns;
	private List<Card> cards;
	private Map<String, ArrayList<Card>> labelColumnsForCards;	// ������ label-������� �� ���������� ��� ������ �� ��������� �������� (������ �������� ������ ���� <%Column.attributeId%>-><%Column.fullLabelAttrId%>) 
	
	/**
	 * Gets collection of {@link Card cards} representing result of {@link Search} action.
	 * Note that {@link Card cards} in collection will be initialized only partially and
	 * will contain attributes specified in {@link #getColumns() columns} property only.  
	 * @return collection of {@link Card cards} representing result of {@link Search} action.
	 */
	public List<Card> getCards() {
		return cards;
	}

	/**
	 * Sets collection of {@link Card cards} representing result of {@link Search} action.
	 * @param cards collection of {@link Card cards} representing result of {@link Search} action.
	 */
	public void setCards(List<Card> cards) {
		this.cards = cards;
	}
	
	/**
	 * Gets collection of columns containing in this {@link SearchResult} object
	 * @return collection of columns containing in this {@link SearchResult} object
	 */
	public Collection<Column> getColumns() {
		return columns;
	}
	
	/**
	 * Sets collection of {@link Column} items representing set of columns if {@link SearchResult}
	 * @param columns collection of {@link Column} items representing set of columns if {@link SearchResult}
	 */
	public void setColumns(Collection<Column> columns) {
		this.columns = columns;
	}
	
	/**
	 * Gets collection of columns with grouping containing in this {@link SearchResult} object
	 * @return collection of columns containing in this {@link SearchResult} object
	 */
	public ArrayList<Column> getActiveColumns() {
		ArrayList<Column> result = new ArrayList<Column>();
		int skippedGroup = 0;
		for (Column column : columns) {
			if (column.getGroupId() == 0) {
				result.add(column);
			} else if (column.getGroupId() != skippedGroup || skippedGroup == 0) {
				result.add(column);
				skippedGroup = column.getGroupId();
			}
		}
		return result;
	}

	/**
	 * Gets english name of search result 
	 * @return english name of search result
	 */
	public String getNameEn() {
		return nameEn;
	}
	
	/**
	 * Sets english name of search result
	 * @param nameEn english name of search result
	 */
	public void setNameEn(String nameEn) {
		this.nameEn = nameEn;
	}
	
	/**
	 * Gets russian name of search result
	 * @return russian name of search result
	 */
	public String getNameRu() {
		return nameRu;
	}
	
	/**
	 * Sets russian name of search result
	 * @param nameRu russian name of search result
	 */
	public void setNameRu(String nameRu) {
		this.nameRu = nameRu;
	}
	
	/**
	 * Returns localized name of search result.
	 * This string is used as a header for table displaying search result in GUI
	 * @return returns value of {@link #getNameRu} or {@link #getNameEn} properties 
	 * depending of caller's locale preferences
	 */	
	public String getName() {
		return ContextProvider.getContext().getLocaleString(nameRu, nameEn);
	}

	public static class Action implements Serializable
	{
		private static final long serialVersionUID = SearchResult.serialVersionUID;

		String m_id;
		Map<String, String> m_parameters;

		public String getId() {
			return m_id;
		}

		public void setId(String id) {
			this.m_id = id;
		}
		
		public Map<String, String> getParametrs(){
			return m_parameters;
		}

		public void setParametrs(Map<String, String> parameters) {
			this.m_parameters = parameters;
		}
	}


	/**
	 * This class is used to represent information about one
	 * column in GUI table displaying {@link SearchResult} object.
	 * <br>
	 * Every column of result table is associated with one of {@link Card} attribute.
	 * Name of attribute is used as the name of column in table
	 */
	public static class Column implements Serializable
	{
		/**
		 * Constant used to specify no sorting
		 */
		public static final int SORT_NONE = 0;
		/**
		 * Constant used to specify ascending sorting
		 */
		public static final int SORT_ASCENDING = 1;
		/**
		 * Constant used to specify descending sorting
		 */
		public static final int SORT_DESCENGING = 2;
		/**
		 * Constant used to specify sorting by value_rus/value_eng from value_list (for ReferenceAttribute)
		 */
		public static final int LIST_ORDER_SORT_BY_STRING_NAME = 0;
		/**
		 * Constant used to specify sorting by value_id from attribute_value (for ReferenceAttribute) 
		 */
		public static final int LIST_ORDER_SORT_BY_ORDER_IN_LEVEL = 1;

		//���� ��� ������������ ����� ����� �� params
		public static final String COLUNM_FILE_VIEWER="viewerFile";
		//���� ��� ������������ Viewer �� params
		public static final String COLUNM_VIEWER="columnViewer";

		private static final long serialVersionUID = SearchResult.serialVersionUID;
		private String nameRu;
		private String nameEn;
		private String nullValueRu;
		private String nullValueEn;
		private int width;
		private int textLength;
		private int sorting;
		private int valueOrder = Column.LIST_ORDER_SORT_BY_STRING_NAME;
		private boolean linked;
		private boolean hidden;
		private boolean downloadMaterial; // the flag means that by hitting on this column we need to download a material(file)
		private List<ObjectId> pathToLabelAttr;
		private ObjectId labelAttrId; 
		private String fullReplaceAttrId; 	// (YNikitin, 2012/08/03) ������ ���������� �������� ��������, ������ �������� ����� ���������� ������ ������� 
		private ObjectId replaceStatusId; 	// (YNikitin, 2012/08/03) ������ ������� ��������, ��� ������� ������ ������� ����� �������������� ������ ����������
		private boolean isEmptyReplace = false;
		private String fullLabelAttrId;		// ��� ����������� ������������ ������� � ���������� ����� labelAttrId  
		private boolean isParentName=false;	// (YNikitin, 2011/04/20) ��� ������������ ������� �� ��������� �������� ������ ������������ �������
		private boolean useGivenTitle=false;// ��� ������������ ������� ��������������� �����
		private String timePattern;
		private boolean useDefaultTimePattern = true;
		private boolean showNullValue=false;
		private int groupId = 0;
		private boolean excelIgnore = false;
		private boolean exportOnly = false;
		private int sortOrder;
		private boolean sortable = true;
		private List<ObjectId> linkCardsStatesIgnore;
		
		/* �������� �������������� ������� */
		// ������ �������������� ������� ��� �������, ��� ���������� ������� ����� �������������� ��� ������ �������� �������
		private List<SearchResult.Column> secondaryColumns;
		// ��������� �������. null ��� ��������� � not null (������ �� ���������) ��� ��������������
		private SearchResult.Column primaryColumn;
		
		private Condition columnCond = Condition.NULL;
		
		public enum Condition {
			NULL, STATE, TEMPLATE
		}
		
		// �������� ��� ��������: ������� ��� �������
		private List<ObjectId> condValues;
		
		/**
		 * add action attribute to search. Action like download
		 */
		private Action action;

		private ObjectId attributeId;
		private List<List<ObjectId>> sortAttrPaths;

		private Map<String, String> defaultIcon; // �������� ������ �� ��������� {image: "...", tooltipRU: "...", tooltipEn: "..."}
		private Map<String, String> emptyIcon; // �������� ������ � ������ ���������� �������� {image: "...", tooltipRU: "...", tooltipEn: "..."}
		private Map<String, Map<String, String>> icons; // �������� ������ ��� ���������� �������� {value: {image: "...", tooltipRU: "...", tooltipEn: "..."},...}
		
		/*
		 * Params ������������ ��� �������� ����� ���������� ����������� ��� �������
		 * ��� �������� ��� �������� ���������� � Tooltip, �.�. ������ ��� ����� ������������ Viewer � ��� Viewer.
		 * ����� ��� �������� ������� �� ��� ������. 
		 */
		private Map<String, Object> params = new HashMap<String, Object>();
		
		public Column() {
			
		}

		public Column(ObjectId attributeId) {
			this.attributeId = attributeId;
		}

		public Object getParamByName(String paramName){
			return params.get(paramName);
		}

		public void addParam(String paramName, Object value){
			params.put(paramName, value);
		}

		public Map<String, Object> getParams(){
			return params;
		}

		public void setParams(Map<String, Object> params){
			this.params=params;
		}

		/**
		 * ������� ����� ������ �������.
		 * @return ����� �������.
		 */
		public Column copy()
		{
			final Column result = new Column();

			result.nameRu = this.nameRu;
			result.nameEn = this.nameEn;
			result.nullValueRu = this.nullValueRu;
			result.nullValueEn = this.nullValueEn;
			result.width = this.width;
			result.textLength = this.textLength;

			result.sorting = this.sorting;
			result.valueOrder = this.valueOrder;
			result.linked = this.linked;
			result.hidden = this.hidden;
			result.downloadMaterial = this.downloadMaterial;
			result.labelAttrId = this.labelAttrId; // �� ������� ����� 
			result.fullReplaceAttrId = this.fullReplaceAttrId;
			result.replaceStatusId = this.replaceStatusId;		
			result.isEmptyReplace=this.isEmptyReplace;
			result.isParentName = this.isParentName;	// (YNikitin, 2011/04/20)
			result.useGivenTitle = this.useGivenTitle;

			result.action = this.action;
			result.attributeId = this.attributeId; // �� ������� �����

			result.sortAttrPaths = this.sortAttrPaths;

			result.defaultIcon = this.defaultIcon;
			result.emptyIcon = this.emptyIcon;
			result.icons = this.icons;
			
			result.pathToLabelAttr = this.pathToLabelAttr;
			result.fullLabelAttrId = this.fullLabelAttrId;
			result.timePattern=this.timePattern;
			result.useDefaultTimePattern=this.useDefaultTimePattern;

			result.showNullValue=this.showNullValue;
			result.groupId=this.groupId;
			result.excelIgnore=this.excelIgnore;
			result.exportOnly=this.exportOnly;
			result.sortOrder = this.sortOrder;
			
			result.secondaryColumns = this.secondaryColumns;
			result.primaryColumn = this.primaryColumn;
			result.columnCond = this.columnCond;
			result.condValues = this.condValues;

			result.params=this.params;
			result.sortable = this.sortable;
			result.linkCardsStatesIgnore = this.linkCardsStatesIgnore;

			return result; // return this.clone();
		}
		
		/**
		 * Gets identifier of {@link Attribute} in this column
		 * @return identifier of {@link Attribute} in this column
		 */
		public ObjectId getAttributeId() {
			return attributeId;
		}
		
		/**
		 * Gets identifier of {@link Attribute} in this column
		 * @param attributeId identifier of card attribute in this column. 
		 * Must be one of {@link Attribute} descendants.
		 */
		public void setAttributeId(ObjectId attributeId) {
			this.attributeId = attributeId;
		}
		
		public List<ObjectId> getPathToLabelAttr() {
			return pathToLabelAttr;
		}

		public void setPathToLabelAttr(List<ObjectId> pathToLabelAttr) {
			this.pathToLabelAttr = pathToLabelAttr;
		}

		/**
		 * Get the identifier of {@link Attribute} that is labeling this column
		 * @return labeling id
		 */
		public ObjectId getLabelAttrId() {
			// ������� ��������, ����� ��� ��������������� ���������� ���������� ���
			if (this.labelAttrId!=null){
				if (this.labelAttrId.getId().equals(Card.ATTR_STATE.getId()))
					return new ObjectId(Card.ATTR_STATE.getType(), this.labelAttrId.getId());
				if (this.labelAttrId.getId().equals(Card.ATTR_ID.getId()))
					return new ObjectId(Card.ATTR_ID.getType(), this.labelAttrId.getId());
				if (this.labelAttrId.getId().equals(Card.ATTR_MATERIAL_TYPE.getId()))
					return new ObjectId(Card.ATTR_MATERIAL_TYPE.getType(), this.labelAttrId.getId());
				if (this.labelAttrId.getId().equals(Card.ATTR_TEMPLATE.getId()))
					return new ObjectId(Card.ATTR_TEMPLATE.getType(), this.labelAttrId.getId());
			}
			return this.labelAttrId;
		}

		/**
		 * @param attrId to set for labeling this column
		 */
		public void setLabelAttrId(ObjectId attrId) {
			this.labelAttrId = attrId;
		}

		public String getFullReplaceAttrId() {
			return fullReplaceAttrId;
		}

		public void setFullReplaceAttrId(String fullReplaceAttrId) {
			this.fullReplaceAttrId = fullReplaceAttrId;
		}

		public ObjectId getReplaceStatusId() {
			return replaceStatusId;
		}

		public void setReplaceStatusId(ObjectId replaceStatusId) {
			this.replaceStatusId = replaceStatusId;
		}
		
		public void setEmptyReplace(boolean empty){
			this.isEmptyReplace=empty;			
		}
		
		public boolean isEmptyReplace(){
			return this.isEmptyReplace;
		}

		/**
		 * ������� ������� �������� ���, �� ������� ���������� ������ � ��� ������, ���� ���� �������� ����������� �� ������ � ������ ������ �� ������
		 */
		public boolean isReplaceAttribute(){
			return /*((isEmptyReplace==true || replaceStatusId!=null)&& */fullReplaceAttrId!=null;
		}
		
		public boolean getIsParentName() {
			return this.isParentName;
		}
		
		/**
		 * @param isParentName true if must use parent column name
		 */
		public void setIsParentName(boolean isParentName) {
			this.isParentName = isParentName;
		}
		
		/**
		 * Gets english name of column 
		 * @return english name of column
		 */
		public String getNameEn() {
			return nameEn;
		}
		
		/**
		 * Sets english name of column
		 * @param nameEn desired column name in english 
		 */
		public void setNameEn(String nameEn) {
			this.nameEn = nameEn;
		}

		/**
		 * Gets russian name of column 
		 * @return russian name of column
		 */		
		public String getNameRu() {
			return nameRu;
		}

		/**
		 * Sets russian name of column 
		 * @param nameRu desired column name in russian 
		 */
		public void setNameRu(String nameRu) {
			this.nameRu = nameRu;
		}
		
		/**
		 * Gets russian null-value of column 
		 * @return russian null-value of column
		 */		
		public String getNullValueRu() {
			return nullValueRu;
		}

		/**
		 * Sets russian null-value of column 
		 * @param nullValueRu desired null-value in russian 
		 */
		public void setNullValueRu(String nullValueRu) {
			this.nullValueRu = nullValueRu;
			this.showNullValue = true;
		}

		/**
		 * Gets english null-value of column 
		 * @return english null-value of column
		 */		
		public String getNullValueEn() {
			return nullValueEn;
		}

		/**
		 * Returns localized null-value of column
		 * @return returns value of {@link #nullValueRu} or {@link #nullValueEn} properties 
		 * depending of caller's locale preferences
		 */	
		public String getNullValue() {
			return ContextProvider.getContext().getLocaleString(nullValueRu, nullValueEn);
		}

		/**
		 * Sets english null-value of column 
		 * @param nullValueEn desired null-value in english 
		 */
		public void setNullValueEn(String nullValueEn) {
			this.nullValueEn = nullValueEn;
			this.showNullValue = true;
		}

		/**
		 * Checks if in column must special null-value 
		 * 
		 */
		public boolean isShowNullValue() {
			return showNullValue;
		}

		/**
		 * Returns localized name of column
		 * @return returns value of {@link #getNameRu} or {@link #getNameEn} properties 
		 * depending of caller's locale preferences
		 */	
		public String getName() {
			return ContextProvider.getContext().getLocaleString(nameRu, nameEn);
		}
		
		/**
		 * Gets width of column
		 * @return width of column
		 */
		public int getWidth() {
			return width;
		}
		
		/**
		 * Sets width of column
		 * @param width desired width value
		 */
		public void setWidth(int width) {
			this.width = width;
		}
		
		/**
		 * Gets text length inside column
		 * @return amount of symbols
		 */
		public int getTextLength() {
			return textLength;
		}

		/**
		 * Sets text length inside column
		 * @param textLength of symbols
		 */
		public void setTextLength(int textLength) {
			this.textLength = textLength;
		}

		/**
		 * Checks if current column should contain clickable link, which opens
		 * {@link Card} view page.
		 */
		public boolean isLinked() {
			return linked;//Attribute.ID_NAME.equals(attributeId);
		}
		
		/**
		 * Sets the sign whether this column should be displayed as a clickable link
		 * to a {@link Card} view page.
		 * @param linked <code>true</code> if link should be displayed
		 */
		public void setLinked(boolean linked) {
			this.linked = linked;
		}

		/**
		 * Checks if sorting by this column is allowed
		 * @return true if sorting by this column is allowed, false otherwise
		 */
		public boolean isSortable() {
			return sortable;
		}

		public void setSortable(boolean sortable) {
			this.sortable = sortable;
		}

		/**
		 * Gets sorting order of this column. If cards in {@link SearchResult}
		 * is not sorted by this column returns {@link #SORT_NONE}, otherwise returns
		 * {@link #SORT_ASCENDING} or {@link #SORT_DESCENGING}
		 * @return sorting order of this column.
		 */
		public int getSorting() {
			return sorting;
		}

		/**
		 * Sets sorting order for this column.
		 * @param sorting desired sorting order for this column. Allowed values is:
		 * {@link #SORT_NONE}, {@link #SORT_ASCENDING}, {@link #SORT_DESCENGING}. 
		 */
		public void setSorting(int sorting) {
			this.sorting = sorting;
		}
		
		/**
		 * Gets special kind of sorting of this column -- {@link #LIST_ORDER_SORT_BY_STRING_NAME} or {@link #LIST_ORDER_SORT_BY_ORDER_IN_LEVEL}
		 * (for ReferenceAttribute) 
		 * @return kind of sorting
		 */
		public int getValueOrder() {
			return valueOrder;
		}
		/**
		 * Sets special kind of sorting for this column. (for ReferenceAttribute)
		 * Allowed values is: 
		 * @param valueOrder value of {@link #LIST_ORDER_SORT_BY_STRING_NAME}, {@link #LIST_ORDER_SORT_BY_ORDER_IN_LEVEL}
		 */
		public void setValueOrder(int valueOrder) {
			this.valueOrder = valueOrder;
		}
		/**
		 * Gets action
		 * @return action like download
		 */
		public Action getAction() {
			return action;
		}

		/**
		 * Sets action like download
		 * 
		 * @param action Action like download
		 */
		public void setAction(Action action) {
			this.action = action;
		}

		public Map<String, Map<String, String>> getIcons() {
			return icons;
		}

		public void setIcons(Map<String, Map<String, String>> icons) {
			this.icons = icons;
		}
		
		public boolean isIcon() {
			return icons != null || defaultIcon != null || emptyIcon != null;
		}

		public Map<String, String> getDefaultIcon() {
			return defaultIcon;
		}

		public void setDefaultIcon(Map<String, String> defaultIcon) {
			this.defaultIcon = defaultIcon;
		}

		public Map<String, String> getEmptyIcon() {
			return emptyIcon;
		}

		public void setEmptyIcon(Map<String, String> emptyIcon) {
			this.emptyIcon = emptyIcon;
		}
		
		public List<ObjectId> getCondValues() {
			return condValues;
		}

		public void setCondValues(List<ObjectId> condValues) {
			this.condValues = condValues;
		}
		
		public List<ObjectId> getLinkCardsStatesIgnore() {
			return linkCardsStatesIgnore;
		}

		public void setLinkCardsStatesIgnore(List<ObjectId> linkCardsStatesIgnore) {
			this.linkCardsStatesIgnore = linkCardsStatesIgnore;
		}

/**
 *  ��� ��������� �������� {@code SearchResult.Column} ������������ ������ ��������
 *  {@link #getAttributeId()}, ��� ��� � �������� ������������� ��������� �������
 *	����� ���� �� �������� �������� ���� ������������� ������������� 
 *	(���� ������ ��������� ������ ��� ��� ����� ��� Attrubute.class).
 */
		public boolean equals(Object arg0) {
			if (arg0 == null)
				return false;
			if (!(arg0 instanceof SearchResult.Column))
				return false;
			SearchResult.Column col = (SearchResult.Column)arg0;
			return this.attributeId.getId().equals(col.attributeId.getId()); 
		}

		public List<List<ObjectId>> getSortAttrPaths() {
			return sortAttrPaths;
		}

		public void setSortAttrPaths(List<List<ObjectId>> sortAttrPaths) {
			this.sortAttrPaths = sortAttrPaths;
		}

		public String getFullLabelAttrId() {
			return fullLabelAttrId;
		}

		public void setFullLabelAttrId(String fullLabelAttrId) {
			this.fullLabelAttrId = fullLabelAttrId;
		}

		public void setUseGivenTitle(boolean useGivenTitle) {
			this.useGivenTitle = useGivenTitle;
		}

		public boolean isUseGivenTitle() {
			return useGivenTitle;
		}
		
		public void setUseDefaulTimetPattern(boolean useDefault) {
			this.useDefaultTimePattern = useDefault;
		}

		public boolean isDefaultTimePatternUsed() {
			return useDefaultTimePattern;
		}
		
		public void setTimePattern(String timePattern) {
			this.timePattern = timePattern;
		}

		public String getTimePattern() {
			return timePattern;
		}

		public int getGroupId() {
			return groupId;
		}

		public void setGroupId(int groupId) {
			this.groupId = groupId;
		}

		/**
		 * ������� ������ �������� �������� � ������ ���� labelAttrId
		 * @return
		 * ���� fullLabelAttrId �� ��������, �� ���������� attributeId, 
		 * ����� attributeId+Attribute.LABEL_ATTR_PARTS_SEPARATOR+fullLabelAttrId
		 */
		public String getFullColumnName(){
			return (this.fullLabelAttrId!=null)?this.getAttributeId().getId().toString()+Attribute.LABEL_ATTR_PARTS_SEPARATOR+this.fullLabelAttrId:this.getAttributeId().getId().toString();
		}

		public boolean isExcelIgnore() {
			return excelIgnore;
		}

		public void setExcelIgnore(boolean excelIgnore) {
			this.excelIgnore = excelIgnore;
		}

		public boolean isExportOnly() {
			return exportOnly;
		}

		public void setExportOnly(boolean exportOnly) {
			this.exportOnly = exportOnly;
		}
		
		public int getSortOrder() {
			return sortOrder;
		}
		
		public void setSortOrder(int sortOrder) {
			this.sortOrder = sortOrder;
		}

		public boolean isDownloadMaterial() {
			return downloadMaterial;
		}
		
		public void setDownloadMaterial(boolean downloadMaterial) {
			this.downloadMaterial = downloadMaterial;
		}

		public List<SearchResult.Column> getSecondaryColumns() {
			return secondaryColumns;
		}

		public void setSecondaryColumns(List<SearchResult.Column> secondaryColumns) {
			this.secondaryColumns = secondaryColumns;
		}

		public SearchResult.Column getPrimaryColumn() {
			return primaryColumn;
		}

		public void setPrimaryColumn(SearchResult.Column primaryColumn) {
			this.primaryColumn = primaryColumn;
		}

		public Condition getColumnCond() {
			return columnCond;
		}

		public void setColumnCond(Condition columnCond) {
			this.columnCond = columnCond;
		}

		public boolean isHidden() {
			return hidden;
		}

		public void setHidden(boolean hidden) {
			this.hidden = hidden;
		}
		
	}
		/*public Map getLabelColumnsForCards() {
		return labelColumnsForCards;
	}*/

	/*public void setLabelColumnsForCards(Map labelColumnsForCards) {
		this.labelColumnsForCards = labelColumnsForCards;
	}*/

	/**
	 * This works like card.getAttributeById but workaround get BackLinkAttribute as FoundBackLinkAttribute. 
	 * @param card 
	 * @param attrId
	 * @return found attribute
	 */
	/*final public static Attribute smartFindAttr(Card card, ObjectId attrId) 
	{
		if (card == null || attrId == null)
			return null;

		final Attribute result = card.getAttributeById(attrId);
		// (2010/12/25) ������� ��� bklink ����� ��� FoundBkLink...
		if (result == null && attrId.getType().isAssignableFrom(BackLinkAttribute.class))
			return card.getAttributeById( new ObjectId(FoundBackLinkAttribute.class, attrId.getId()) );
		return result;
	}*/

	/**
	 * ������� ������ �������� � ������������ ����������, �������������� ������� ������� 
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Card> getCardsListForLabelColumn(Column column){
		if (column==null)
			return null;
		if (this.labelColumnsForCards==null)
			this.labelColumnsForCards = new HashMap<String, ArrayList<Card>>();
		final Set<String> keys = this.labelColumnsForCards.keySet();
		final Collection<ArrayList<Card>> values = this.labelColumnsForCards.values();
		for(int i=0; i<keys.size(); i++){
			if ((keys.toArray()[i]).equals(column.getAttributeId().getId().toString()+Attribute.LABEL_ATTR_PARTS_SEPARATOR+column.getFullLabelAttrId()))
				return (ArrayList<Card>) values.toArray()[i];
		}
		final ArrayList<Card> cardsList = new ArrayList<Card>();	// ������� ����� ������ �������� ��� ������� �������
		for (final Card card : cards) {    // �������� ��� ��������� ������� �������� � ����� ����������� ���������
			final Attribute columnAttribute = card.getAttributeById(column.getAttributeId());
			if (columnAttribute != null) {
				// � ����� ������� ����� ������ �������� �� card, ����� ����� ��������� �� � newCard (���������� ������������)
				// TODO: ���� ����� �������� ��������, ��������� �� ������������
				Attribute attr;
				try {
					final ByteArrayOutputStream buf = new ByteArrayOutputStream();
					final ObjectOutputStream o = new ObjectOutputStream(buf);
					o.writeObject(columnAttribute);
					o.flush();
					o.close();
					// ������ �������� �����:
					final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf.toByteArray()));
					attr = (Attribute) in.readObject();
				} catch (Exception e) {
					continue;
				}
				final Card newCard = new Card();
				final long id = (Long) card.getId().getId();
				newCard.setAttributes(new ArrayList<DataObject>());
				newCard.setId(id);
				newCard.getAttributes().add(attr);
				cardsList.add(newCard);
			}
		}
		labelColumnsForCards.put(column.getAttributeId().getId().toString()+Attribute.LABEL_ATTR_PARTS_SEPARATOR+column.getFullLabelAttrId(), cardsList);	// ����� ����� ���� ������ ������ � �������� ����� ������������ �������, �� � ����� ������ ��������� ���������� ������� ��������� �� ������� � ����������� ����� �� �����
		return cardsList;
	}

	// ������ label-������� �� ���������� ��� ������ �� ��������� ��������
	public Map<String, ArrayList<Card>> getLabelColumnsForCards() {
		return labelColumnsForCards;
	}

	/**
	 * ������� ������ �������� � ������������ ����������, �������������� �������� ������ label-������� � ������� ������� 
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<Card> getCardsListForLabelColumn(Map<String, ArrayList<Card>> labelColumnsForCards, Column column){
		if (labelColumnsForCards==null)
			return null;
		if (column==null)
			return null;
		
		final Set<String> keys = labelColumnsForCards.keySet();
		final Collection<ArrayList<Card>> values = labelColumnsForCards.values();
		for(int i=0; i<keys.size(); i++){
			if ((keys.toArray()[i]).equals(column.getAttributeId().getId().toString()+Attribute.LABEL_ATTR_PARTS_SEPARATOR+column.getFullLabelAttrId()))
				return ((ArrayList<Card>)values.toArray()[i]);
		}
		return null;
	}
	
	/**
	 * ������� label ��� ������� ������������
	 * @param card				- ������� ��������
	 * @param column			- ������� �������
	 * @param labelColumns	- c����� �������� � ������������ label-����������
	 * @return label �������
	 */
	public static String getPersonColumnLabel(Card card, SearchResult.Column column, Map<String, ArrayList<Card>> labelColumns) {
		final StringBuilder buf = new StringBuilder();
		PersonAttribute attrVal = null;
		for (final Card curCard : SearchResult.getCardsListForLabelColumn(labelColumns, column)) {
			if (curCard.getId().getId().equals(card.getId().getId())) {
				attrVal = curCard.getAttributeById(column.getAttributeId());
				break;
			}
		}
		if (null != attrVal) {
			Collection<Person> values = attrVal.getValues(); // ��� ������ ���������� Values
			if (values == null || values.isEmpty()) {
				buf.append("");
			}else {
				for( Iterator<Person> itr = values.iterator(); itr.hasNext(); ) {
					final Person item = itr.next();
					if (null != item) {
						String pinfo = item.getFullName();
						if (null != pinfo && !pinfo.isEmpty()) {
							buf.append(pinfo.trim());
							if (itr.hasNext())
								buf.append(", ");
						}
					}
				}
			}
		}
		return buf.toString();
	}
	
	/** (YNikitin, 2012/08/06) 
	 * ������� ������ ������� ������� ��, �� ������� �� ���� �������� � ������� �������� 
	 * @param originalColumn	- ������� �������
	 * @param card				- ������� ��������
	 * @param columns			- ������ ���� �������, � ������� ����� ���� ������� ��, �� ������� ���� ��������
	 * @return
	 * ���������� �������
	 */
	public static Column getRealColumnForCardIfItReplaced(Column originalColumn, Card card, Collection<Column> columns){
		if (columns==null||columns.isEmpty()||card==null)
			return originalColumn;
		
		String originalColumnName = originalColumn.getFullColumnName(); 
		Column column;
		for(Object col: columns){
			column=(Column) col;
			if (((Column)col).getFullColumnName().equals(originalColumnName))
				continue;
			//final Attribute result         = card.getAttributeById(((Column)col).getAttributeId());
			//final Attribute originalResult = card.getAttributeById(originalColumn.getAttributeId());
			if(!(column.isReplaceAttribute()&&originalColumnName.equalsIgnoreCase(column.getFullReplaceAttrId()))){
				continue;				
			}
			
			if(checkColumnReplaceStatus(column, card) && checkColumnReplaceEmpty(originalColumn, column, card)){
				return column;
			}			
		}
		return originalColumn;
	}
	
	private static boolean checkColumnReplaceEmpty(Column original, Column replace, Card card) {
		final Attribute replaceResult = card.getAttributeById(replace.getAttributeId());
		final Attribute originalResult = card.getAttributeById(original.getAttributeId());
		return ((replace.isEmptyReplace() && originalResult == null) || !replace.isEmptyReplace()) && replaceResult != null;
	}
	
	private static boolean checkColumnReplaceStatus(Column column, Card card){
		return (column.getReplaceStatusId() != null && column.getReplaceStatusId().getId().equals(card.getState().getId())) || column.getReplaceStatusId() == null;
	}
}
