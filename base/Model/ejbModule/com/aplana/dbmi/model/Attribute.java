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
package com.aplana.dbmi.model;

/**
 * Abstract class representing single attribute of the card. This class is used to present information
 * <ul>
 *  <li>about attribute definition as it is defined in ATTRIBUTE and TEMPLATE_ATTRIBUTE tables</li>
 *  <li>value of attribute in specific {@link Card} object stored in ATTRIBUTE_VALUE table</li>
 * </ul>
 * This class is not used directly, but it's descendants does.
 */
abstract public class Attribute extends LockableObject implements Comparable<Attribute>
{
	private static final long serialVersionUID = 1L;

	///////////////////////////////////////////////////////
	// Attribute types
	///////////////////////////////////////////////////////
	/**
	 * Type for {@link StringAttribute}
	 */
	public static final Object TYPE_STRING = "S";
	/**
	 * Type for {@link TextAttribute}
	 */
	public static final Object TYPE_TEXT = "T";
	/**
	 * Type for {@link IntegerAttribute}
	 */
	public static final Object TYPE_INTEGER = "I";
    /**
	 * Type for {@link LongAttribute}
	 */
	public static final Object TYPE_LONG = "N";
	/**
	 * Type for {@link DateAttribute}
	 */
	public static final Object TYPE_DATE = "D";
	/**
	 * Type for {@link ListAttribute}
	 */
	public static final Object TYPE_LIST = "L";
	/**
	 * Type for {@link TreeAttribute}
	 */
	public static final Object TYPE_TREE = "H";
	/**
	 * Type for {@link PersonAttribute}
	 */
	public static final Object TYPE_PERSON = "U";
	/**
	 * Type for {@link SecurityAttribute}
	 */
	public static final Object TYPE_SECURITY = "A";
	/**
	 * Type for {@link CardLinkAttribute}
	 */
	public static final Object TYPE_CARD_LINK = "C";
	/**
	 * Type for {@link HtmlAttribute}
	 */
	public static final Object TYPE_HTML = "W";
	/**
	 * Type for {@link MaterialAttribute}
	 */
	public static final Object TYPE_MATERIAL = "M";
	/**
	 * Type for {@link BackLinkAttribute}
	 */
	public static final Object TYPE_BACK_LINK = "B";

	public static final Object TYPE_TYPED_CARD_LINK = "E";
	
	public static final Object TYPE_DATED_TYPED_CARD_LINK = "F";

	public static final Object TYPE_CARD_HISTORY = "Y";

	public static final Object TYPE_DATE_PERIOD = "R";

	/**
	 * Type for {@link MultipleStateSearchItemAttribute}
	 */
	public static final Object TYPE_MULTIPLE_STATE_SEARCH_ITEM = "X";
	
	public static final String TYPE_PORTAL_USER_LOGIN = "Z";
	
	public static final String TYPE_USER_ROLES_AND_GROUPS = "V";

	/**
	 * 
	 */
	public static final String LABEL_ATTR_PARTS_SEPARATOR = "->";

	/////////////////////////////////////////////////////////////
	// System Attributes
	/////////////////////////////////////////////////////////////
	/**
	 * {@link MaterialAttribute Material} associated with {@link Card} object.
	 */
	public static final ObjectId ID_MATERIAL = new ObjectId(MaterialAttribute.class, "MATERIAL");
	/**
	 * {@link PersonAttribute Person} created given {@link Card} object.
	 */
	public static final ObjectId ID_AUTHOR = new ObjectId(PersonAttribute.class, "AUTHOR");
	/**
	 * {@link DateAttribute Date} of {@link Card} creation.
	 */
	public static final ObjectId ID_CREATE_DATE = new ObjectId(DateAttribute.class, "CREATED");
	/**
	 * {@link DateAttribute Date} of last {@link Card} edition.
	 */
	public static final ObjectId ID_CHANGE_DATE = new ObjectId(DateAttribute.class, "CHANGED");
	/**
	 * {@link IntegerAttribute Size} of attached material
	 */
	public static final ObjectId ID_FILE_SIZE = new ObjectId(IntegerAttribute.class, "FILESIZE");
	/**
	 * {@link ListAttribute Region} of given card
	 */
	public static final ObjectId ID_REGION = new ObjectId(TreeAttribute.class, "REGION");
	/**
	 * {@link SecurityAttribute Security settings} defining access to card's material
	 */
	public static final ObjectId ID_SECURITY = new ObjectId(SecurityAttribute.class, "SECURITY");
	/**
	 * {@link TextAttribute Reason of card approval failure}
	 */
	public static final ObjectId ID_REJECT_REASON = new ObjectId(TextAttribute.class, "RREASON");
	/**
	 * {@link CardLinkAttribute Parent} card
	 */
	public static final ObjectId ID_PARENT = new ObjectId(CardLinkAttribute.class, "PARENT");
	/**
	 * {@link CardLinkAttribute Children} cards (Pseudo-attribute)
	 */
	public static final ObjectId ID_CHILDREN = new ObjectId(BackLinkAttribute.class, "CHILDREN");

	/** @deprecated */
	@Deprecated
	public static final ObjectId ID_STATE = new ObjectId(ListAttribute.class, "STATE");


	////////////////////////////////////////////////////////////////
	// Often used Attributes
	////////////////////////////////////////////////////////////////
	/**
	 * {@link StringAttribute Name of card}
	 */
	public static final ObjectId ID_NAME = new ObjectId(StringAttribute.class, "NAME");
	/**
	 * {@link TextAttribute Description of card}
	 */
	public static final ObjectId ID_DESCR = new ObjectId(TextAttribute.class, "DESCR");
	/**
	 * {@link TreeAttribute type of attached material}
	 */
	public static final ObjectId ID_FILE_TYPE = new ObjectId(TreeAttribute.class, "FILETYPE");
	/**
	 * {@link TextAttribute unique id (UUID) of card}
	 */
	public static final ObjectId ID_UUID = new ObjectId(TextAttribute.class, "JBR_UUID");

	private String nameRu;
	private String nameEn;
	private ObjectId blockId;
	private int blockOrder;
	private boolean active;
	private boolean system;
	private boolean searchShow;
	private int searchOrder;
	private int columnWidth;
	private boolean mandatory;
	private boolean hidden;
	private boolean readOnly;

	/**
	 * Sets the identity for Attribute object. Matches field ATTRIBUTE_CODE in ATTRIBUTE table
	 * @param id attribute code
	 */
	public void setId(String id) {
		super.setId(id == null ? null : new ObjectId(getClass(), id));
	}

	/**
	 * Returns true if given attribute object is active, false otherwise. Inactive attributes are removed from {@link Card} object during fetch operation.
	 * @return true if given attribute object is active, false otherwise
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Sets isActive on attribute object
	 * @param active desired value of isActive flag
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Gets identifier of {@link AttributeBlock} containing this attribute object
	 * @return identifier of {@link AttributeBlock} containing this attribute object
	 */
	public ObjectId getBlockId() {
		return blockId;
	}

	/**
	 * Sets identifier of {@link AttributeBlock} containing this attribute object
	 * @param blockId desired value of block identifier
	 */
	public void setBlockId(ObjectId blockId) {
		this.blockId = blockId;
	}

	/**
	 * Returns order of this attribute in {@link AttributeBlock} (starting from 0)
	 * @return position of attribute in {@link AttributeBlock}
	 */
	public int getBlockOrder() {
		return blockOrder;
	}

	/**
	 * Sets order of this attribute in {@link AttributeBlock} (starting from 0)
	 * @param blockOrder desired position in {@link AttributeBlock}
	 */
	public void setBlockOrder(int blockOrder) {
		this.blockOrder = blockOrder;
	}

	/**
	 * Gets width of column associated with this attribute object in {@link com.aplana.dbmi.action.SearchResult search result} table
	 * @return Width of column associated with this attribute object in search result
	 */
	public int getColumnWidth() {
		return columnWidth;
	}

	/**
	 * Sets width  of column associated with this attribute object in {@link com.aplana.dbmi.action.SearchResult search result} table
	 * @param columnWidth desired column width
	 */
	public void setColumnWidth(int columnWidth) {
		this.columnWidth = columnWidth;
	}

	/**
	 * Returns  true if given template is mandatory by default,  false otherwise. Mandatory attributes should not be empty to perform any {@link com.aplana.dbmi.action.ChangeState} action.
	 * This value could be overriden in TEMPLATE_ATTRIBUTE and ATTRIBUTE_VIEW_PARAM tables ({@link AttributeViewParamDetail} and {@link AttributeViewParam})
	 * @return default value of isMandatory flag of given attribute
	 */
	public boolean isMandatory() {
		return mandatory;
	}

	/**
	 * Sets default value of isMandatory flag for Attribute.
	 * This value could be overriden in TEMPLATE_ATTRIBUTE and ATTRIBUTE_VIEW_PARAM tables ({@link AttributeViewParamDetail} and {@link AttributeViewParam})
	 * @param mandatory desired value of isMandatory flag
	 */
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	/**
	 * Returns english name of attribute
	 * @return english name of attribute
	 */
	public String getNameEn() {
		return nameEn;
	}

	/**
	 * Sets english name of attribute object
	 * @param nameEn desired value of english name
	 */
	public void setNameEn(String nameEn) {
		this.nameEn = nameEn;
	}

	/**
	 * Returns russian name of attribute
	 * @return russian name of attribute
	 */
	public String getNameRu() {
		return nameRu;
	}

	/**
	 * Sets russian name of attribute object
	 * @param nameRu desired value of russian name
	 */
	public void setNameRu(String nameRu) {
		this.nameRu = nameRu;
	}

	/**
	 * Returns localized name of attribute object
	 * @return returns value of {@link #getNameRu} or {@link #getNameEn} properties depending of caller's locale preferences
	 */
	public String getName() {
		return ContextProvider.getContext().getLocaleString(nameRu, nameEn);
	}

	/**
	 * Returns order of column associated with given attribute object in {@link com.aplana.dbmi.action.SearchResult search result} table
	 * @return order of column in search result table
	 */
	public int getSearchOrder() {
		return searchOrder;
	}

	/**
	 * Sets order of column associated with given attribute object in {@link com.aplana.dbmi.action.SearchResult search result} table
	 * @param searchOrder desired order of column in search result table
	 */
	public void setSearchOrder(int searchOrder) {
		this.searchOrder = searchOrder;
	}

	/**
	 * Return value of isSystem flag of given attribute.
	 * System attributes shouldn't be editable through GUI
	 * @return  true if given attribute is system attribute,  false otherwise
	 */
	public boolean isSystem() {
		return system;
	}

	/**
	 * Sets value of isSystem flag of given attribute
	 * System attributes shouldn't be editable through GUI
	 * @param system desired value of isSystem flag
	 */
	public void setSystem(boolean system) {
		this.system = system;
	}

	/**
	 * Returns  true if given template is read-only by default or if given attribute is inactive,  false otherwise.
	 * Read-only attributes are not editable in {@link Card} editing page
	 * This value could be overriden in TEMPLATE_ATTRIBUTE and ATTRIBUTE_VIEW_PARAM tables ({@link AttributeViewParamDetail} and {@link AttributeViewParam})
	 * @return default value of isReadonly flag of given attribute
	 */
	public boolean isReadOnly() {
		return readOnly || !isActive();
	}

	/**
	 * Returns  true if given template is hidden by default,  false otherwise.
	 * Hidden attributes is not shown in {@link Card} editing page
	 * This value could be overriden in TEMPLATE_ATTRIBUTE and ATTRIBUTE_VIEW_PARAM tables ({@link AttributeViewParamDetail} and {@link AttributeViewParam})
	 * @return default value of isHidden flag of given attribute
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * Sets default value of isHidden flag for Attribute.
	 * Hidden attributes is not shown in {@link Card} editing page
	 * This value could be overriden in TEMPLATE_ATTRIBUTE and ATTRIBUTE_VIEW_PARAM tables ({@link AttributeViewParamDetail} and {@link AttributeViewParam})
	 * @param hidden desired value of isMandatory flag
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * Sets default value of isReadOnly flag for Attribute.
	 * Read-only attributes are not editable in {@link Card} editing page
	 * This value could be overriden in TEMPLATE_ATTRIBUTE and ATTRIBUTE_VIEW_PARAM tables ({@link AttributeViewParamDetail} and {@link AttributeViewParam})
	 * @param readOnly desired value of isReadOnly flag
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 * Returns  true if given attribute should be shown in {@link com.aplana.dbmi.action.SearchResult search result} table by default,
	 * false otherwise.<br>
	 * Note that this value is used only if card search is performed by single template and no {@link com.aplana.dbmi.action.Search#setColumns(java.util.Collection) columns}
	 * where specified in {@link com.aplana.dbmi.action.Search object}.
	 * @return  true if given attribute should be shown in {@link com.aplana.dbmi.action.SearchResult search result} table by default,
	 * false otherwise
	 */
	public boolean isSearchShow() {
		return searchShow;
	}

	/**
	 * Sets default visiblility of given attribute in {@link com.aplana.dbmi.action.SearchResult search result} table,  false otherwise
	 * @param searchShow  true if this attribute should be shown in search result table by default,  false otherwise
	 */
	public void setSearchShow(boolean searchShow) {
		this.searchShow = searchShow;
	}

	/**
	 * Returns  true if given attribute could have more than one value,  false otherwise
	 * This method should be overriden in descendant classes.
	 * @return  true if given attribute could have more than one value,  false otherwise
	 */
	/*abstract*/ public boolean isMultiValued() {
		return false;
	}

	/**
	 * Constant value (usually {@link java.lang.String}) defining type of given attribute.
	 * Each descendant of Attribute class should match to exactly one value of type field.
	 * See constants {@link #TYPE_STRING}, {@link #TYPE_INTEGER} and etc.
	 * @return type of given attribute
	 */
	abstract public Object getType();
	/**
	 * Abstract method should return {@link java.lang.String} representation of attribute's value.
	 * Used in {@link Card} view page and in {@link com.aplana.dbmi.action.SearchResult search result}
	 * table
	 * @return string representation of attribute's value
	 */
	abstract public String getStringValue();
	/**
	 * Checks if value assigned to this attribute is valid.
	 * For all attribute types except of {@link PersonAttribute} and {@link TreeAttribute}
	 * simply returns true
	 * @return  true if attribute's value is valid,  false otherwise
	 */
	abstract public boolean verifyValue();
	/**
	 * Checks if value of this attributes is equal to value of @param attr attribute
	 * Throws IllegalArgumentException if type of this attribute is different from type of attr
	 * @param attr Attribute to compare with
	 * @return true if values are equal, false otherwise
	 */
	abstract public boolean equalValue(Attribute attr);
	
	/**
	 * Copy value from attribute passed as parameter to current attribute considering current one is assignable
	 * from passed one. If descendents actually store any value they should implement this method, otherwise
	 * they should throw {@link UnsupportedOperationException}.
	 * @param attr
	 */
	abstract public void setValueFromAttribute(Attribute attr);
	/**
	 * Checks if given attribute have no value
	 * This method should be used only for attributes which represents value of attribute
	 * in some card.
	 * Mehtod must be overriden in subclasses. Default implementation always throws UnsupportedOperationException
	 * @return true if attribute is empty, false - otherwise
	 */
	public boolean isEmpty() {
		throw new UnsupportedOperationException("Method isEmpty is not overriden for class " + getClass().getName());
	}

	/**
	 * Clears value of this attribute.
	 * Mehtod must be overriden in subclasses. Default implementation always throws UnsupportedOperationException
	 */
	public void clear() {
		throw new UnsupportedOperationException("Method clear is not overriden for class " + getClass().getName());
	}

	public int compareTo(Attribute attr) {
		String stringValue = getStringValue();
		if (stringValue == null) {
			stringValue = "";
		}
		String attrStringValue = attr.getStringValue();
		if (attrStringValue == null) {
			attrStringValue = "";
		}
		return stringValue.compareTo(attrStringValue);
	}
}
