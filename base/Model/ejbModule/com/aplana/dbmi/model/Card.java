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

import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Main object in project.
 * Represents document, containing various information in form of collection of {@link Attribute attributes} divided in several {@link AttributeBlock blocks}
 * Set of attribute blocks is defined in {@link Template} object.
 * Each card have {@link CardState state}, which could be changed during card's livecycle.
 * Order of state changes is defined in {@link Workflow}. 
 * Card uses {@link Workflow} associated with {@link Template} used to create this card.
 */
public class Card extends LockableObject
{
	/**
	 * Constant value to compare with {@link #getMaterialType()} result
	 * This value indicates that card have no attached material
	 */
	public static final int MATERIAL_NONE = MaterialAttribute.MATERIAL_NONE;
	/**
	 * Constant value to compare with {@link #getMaterialType()} result
	 * This value indicates that card have attached file 
	 */	
	public static final int MATERIAL_FILE = MaterialAttribute.MATERIAL_FILE;
	/**
	 * Constant value to compare with {@link #getMaterialType()} result
	 * This value indicates that card have external material (link to external resource)
	 */	
	public static final int MATERIAL_URL = MaterialAttribute.MATERIAL_URL;

	/////////////////////////////////////////////////////////////////
	// Set of pseudo-attributes
	/////////////////////////////////////////////////////////////////
	/**
	 * Identifier of pseudo-attribute representing card id 
	 */
	public static final ObjectId ATTR_ID = new ObjectId(IntegerAttribute.class, "_ID");
	/**
	 * Identifier of pseudo-attribute representing card state name 
	 */	
	public static final ObjectId ATTR_STATE = new ObjectId(ListAttribute.class, "_STATE");
	/**
	 * Identifier of pseudo-attribute representing card template name 
	 */	
	public static final ObjectId ATTR_TEMPLATE = new ObjectId(ListAttribute.class, "_TEMPLATE");
	/**
	 * Identifier of pseudo-attribute representing card material type 
	 */
	public static final ObjectId ATTR_MATERIAL_TYPE = new ObjectId(ListAttribute.class, "_MATLTYPE");
	/**
	 * Identifier of pseudo-attribute representing universal term 
	 */
	public static final ObjectId ATTR_UNITERM = new ObjectId(ListAttribute.class, "_UNITERM");
	
	private static final long serialVersionUID = 6L;

	private ObjectId template;
	private boolean active = true;
	private ObjectId state;
	private Collection<? extends DataObject> attributes;
	private String templateNameRu;
	private String templateNameEn;
	private LocalizedString stateName;

	// >>> (2010/02)
	private boolean canRead;
	private boolean canWrite;
	// <<< (2010/02)

	private long reserveId = 0;
	
	/**
	 * Sets card identifier
	 * @param id value of card identifier
	 */
	public void setId(long id) {
		super.setId(new ObjectId(Card.class, id));
	}
	
    public void setId(ObjectId id) {
        super.setId(id);
    }

	/**
	 * Reset value of card's {@link ObjectId} to null
	 * System recognizes cards with empty ObjectId as newly created cards
	 */
	public void clearId() {
		super.setId(null);
	}
	
	/**
	 * Gets identifier of {@link Template} used to create this card 
	 * @return template identifier
	 */
	public ObjectId getTemplate() {
		return template;
	}
	
	/**
	 * Checks if this card is active
	 * @return true if card is active, false otherwise
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * Returns id of parent card record.
	 * @deprecated use getAttributeById(Attribute.ID_PARENT) instead
	 * @return id of parent card record
	 */
	public ObjectId getParent() {
		//return parent;
		/* (2010/02, RuSA)
		if (attr.getValues() == null || attr.getValues().size() == 0)
			return null;
		return ((Card) attr.getValues().iterator().next()).getId();
		 */
		final CardLinkAttribute attr = getAttributeById(Attribute.ID_PARENT);
		if (attr == null) return null;

		final Collection<ObjectId> ids = attr.getIdsLinked();
		return (ids != null && !ids.isEmpty()) ? ids.iterator().next() : null;
	}

	/**
	 * Gets name of file attached to card
	 * @return filename if file is present or null if card doesn't have attached material file
	 */
	public String getFileName() {
		//return fileName;
		MaterialAttribute attr = getAttributeById(Attribute.ID_MATERIAL);
		if (attr == null || attr.getMaterialType() != MATERIAL_FILE)
			return null;
		return attr.getMaterialName();
	}

	/**
	 * Gets URL of attached to card extenal material
	 * @return URL of material if it is present or null if card have doesn't have attached external material 
	 */
	public String getUrl() {
		MaterialAttribute attr = getAttributeById(Attribute.ID_MATERIAL);
		if (attr == null || attr.getMaterialType() != MATERIAL_URL)
			return null;
		return attr.getMaterialName();
	}
	
	/**
	 * Returns collection of {@link AttributeBlock attribute blocks} comprising given card
	 * @return collection of {@link AttributeBlock} objects
	 */
	@SuppressWarnings("unchecked")
	public <T extends DataObject> Collection<T> getAttributes() {
		return (Collection<T>)attributes;
	}

	/**
	 * Sets isActive flag on Card object
	 * @param active desired value of isActive flag
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Sets collection of {@link AttributeBlock attribute blocks} comprising card
	 * @param attributes collection of attributes
	 */
	public void setAttributes(Collection<? extends DataObject> attributes) {
		if (this.attributes == attributes)
			return;
		this.attributes = attributes;
	}

	/**
	 * Don't use this method
	 * @deprecated
	 */
	public void setFileName(String fileName) {
		//this.fileName = fileName;
		throw new UnsupportedOperationException();
	}

	/**
	 * Don't use this method
	 * @deprecated
	 */
	public void setParent(ObjectId parent) {
		//this.parent = parent;
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Don't use this method
	 * @deprecated
	 */
	public void setParent(long parent) {
		//this.parent = new ObjectId(Card.class, parent);
		setParent(new ObjectId(Card.class, parent));
	}

	/**
	 * Gets name of parent card
	 * @deprecated
	 * @return name of parent card or null if no parent card specified
	 */
	public String getParentName() {
		//return parentName;

		/* >>> (2010/02, RuSA) OLD:
		if (attr.getValues() == null || attr.getValues().size() == 0)
			return null;
		return ((Card) attr.getValues().iterator().next())
				.getAttributeById(Attribute.ID_NAME).getStringValue();
		 */
		final CardLinkAttribute attr = getAttributeById(Attribute.ID_PARENT);
		return (attr != null) ? attr.getStringValue() : "";
		// <<< (2010/02, RuSA)
	}

	/**
	 * Don't use this method
	 * @deprecated
	 */
	public void setParentName(String parentName) {
		//this.parentName = parentName;
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets identifier of {@link Template} used for card creation
	 * @param template template identifier
	 */
	public void setTemplate(ObjectId template) {
		this.template = template;
	}

	/**
	 * Sets identifier of {@link Template} used for card creation
	 * @param template template identifier
	 */
	public void setTemplate(long template) {
		this.template = new ObjectId(Template.class, template);
	}

	/**
	 * Don't use this method
	 * @deprecated
	 */
	public void setUrl(String url) {
		//this.url = url;
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets {@link CardState current state} of this card
	 * @return identifier of current status
	 */
	public ObjectId getState() {
		return state;
	}

	/**
	 * Sets {@link CardState current state} of this card
	 * @param state state identifier to be set
	 */
	public void setState(ObjectId state) {
		if (!CardState.class.equals(state.getType())) {
			throw new IllegalArgumentException("Should be CardState Id");
		}
		this.state = state;
	}

	/**
	 * Gets russian name of card's template
	 * @return russian name of card's template
	 */
	public String getTemplateNameRu() {
		return templateNameRu;
	}

	/**
	 * Sets russian name of card's template
	 * @param templateNameRu russian name of cards template
	 */
	public void setTemplateNameRu(String templateNameRu) {
		this.templateNameRu = templateNameRu;
	}

	/**
	 * Gets english name of card's template
	 * @return english name of card's template
	 */
	public String getTemplateNameEn() {
		return templateNameEn;
	}

	/**
	 * Sets english name of card's template
	 * @param templateNameEn english name of card's template
	 */
	public void setTemplateNameEn(String templateNameEn) {
		this.templateNameEn = templateNameEn;
	}

	/**
	 * Gets localized name of card's template (depending of caller's locale context)
	 * @return localized name of card's template
	 */
	public String getTemplateName() {
		return ContextProvider.getContext().getLocaleString(templateNameRu, templateNameEn);
	}
	
	/**
	 * Sets the name of the card's current state
	 * @param stateName Localized string containing both russian and english versions of the state name
	 * @since v.5
	 */
	public void setStateName(LocalizedString stateName) {
		this.stateName = stateName;
	}
	
	/**
	 * Returns the name of the card's current state
	 * @return Localized string containing both russian and english versions of the state name
	 * @since v.5
	 */
	public LocalizedString getStateName() {
		return stateName;
	}
	
	/**
	 * Gets type of material, attached to card
	 * @return on of following constants: {@link #MATERIAL_FILE}, {@link #MATERIAL_NONE}, {@link #MATERIAL_URL}
	 */
	public int getMaterialType() {
		MaterialAttribute attr = getAttributeById(Attribute.ID_MATERIAL);
		if (attr == null)
			return MATERIAL_NONE;
		return attr.getMaterialType();
	}

	/**
	 * Gets attribute included in card by its code
	 * @param id identifier of card's attribute
	 * @return {@link Attribute} object or null if no such attribute exists
	 */
	@SuppressWarnings("unchecked")
	public <T extends Attribute> T getAttributeById(ObjectId id) {
		if (id == null)
			throw new IllegalArgumentException("Attribute id can't be null");
		if (ATTR_ID.getId().equals(id.getId()))	// ������� �������� _ID ����������� �� ���� ��������, �.�. �� ��������� �� ���� ����� ��������� StringAttribute:_ID
			return (T)new IdAttibute();
		if (getAttributes() == null)
			return null;
		for (DataObject block : getAttributes()) {
			if (block instanceof Attribute && id.equals(block.getId()))
				return (T) block;
			if (block instanceof AttributeBlock) {
				Attribute found = ((AttributeBlock) block).getAttributeById(id);
				if (found != null)
					return (T) found;
			}
		}
		return null;
	}

	public <T extends Attribute> T getAttributeById(Class<T> attributeClass, String attributeAlias) {
		ObjectId attrId = ObjectId.predefined(attributeClass, attributeAlias);
		return getAttributeById(attrId);
	}
	
	/**
	 * Gets attributes block included in card by its code
	 * @param id identifier of card's attribute
	 * @return {@link AttributeBlock} object or null if no such attributes block exists
	 */
	public AttributeBlock getAttributeBlockById(ObjectId id){
		if (id == null)
			throw new IllegalArgumentException("AttributeBlock id can't be null");
		if (getAttributes() == null)
			return null;
		if (!AttributeBlock.class.isAssignableFrom(id.getType()))
			return null;

		for (DataObject block : getAttributes()) {
			if (block instanceof AttributeBlock && id.equals(block.getId()))
				return (AttributeBlock) block;
		}
		return null;
	}
	
	public CardLinkAttribute getCardLinkAttributeById(ObjectId id) {
		return getAttributeById(id);
	}
	
	/**
	 * Gets date of last card changes
	 * @return value of 'CHANGED' attribute, included in card or null if no such attribute exists
	 */
	public Date getLastChangeDate()
	{
		DateAttribute attr = getAttributeById(Attribute.ID_CHANGE_DATE);
		if (attr == null || attr.getValue() == null)
			attr = getAttributeById(Attribute.ID_CREATE_DATE);
		if (attr == null)
			return null;
		return attr.getValue();
	}
	
	/**
	 * Class used for pseudo-attribute {@link #ATTR_ID}
	 */
	public class IdAttibute extends IntegerAttribute
	{
		private static final long serialVersionUID = Card.serialVersionUID;

		public String getStringValue() {
			return outerGetId().getId().toString();
		}

		public int getValue() {
			return ((Long) outerGetId().getId()).intValue();
		}

		public ObjectId getId() {
			return ATTR_ID;
		}

		public int getColumnWidth() {
			return 5;
		}

		public String getNameEn() {
			return ResourceBundle.getBundle(ContextProvider.MESSAGES, new Locale("en")).
					getString("search.column.id");
		}

		public String getNameRu() {
			return ResourceBundle.getBundle(ContextProvider.MESSAGES, new Locale("ru")).
					getString("search.column.id");
		}

		public int getSearchOrder() {
			return 0;
		}

		public boolean isActive() {
			return true;
		}

		public boolean isSystem() {
			return true;
		}

		public boolean isReadOnly() {
			return true;
		}
	}
	
	// Just for use by IdAttribute class
	private ObjectId outerGetId() {
		return getId();
	}

	/**
	 * @return is read access enabled for the current user
	 */
	public boolean getCanRead() {
		return this.canRead;
	}

	/**
	 * @param value set read access for the current user
	 */
	public void setCanRead(boolean value) {
		this.canRead = value;
	}

	/**
	 * @return is write access enabled for the current user
	 */
	public boolean getCanWrite() {
		return this.canWrite;
	}

	/**
	 * @param value set write access for the current user
	 */
	public void setCanWrite(boolean value) {
		this.canWrite = value;
	}

	/**
	 * @return ����������������� ������������� ��������. ��������� ��� ����������
	 */
	public long getReserveId() {
		return reserveId;
}

	public void setReserveId(long reserveId) {
		this.reserveId = reserveId;
	}

}
