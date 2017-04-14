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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

abstract public class LinkAttribute extends Attribute {

	private static final long serialVersionUID = 1L;
	private static final Log logger = LogFactory.getLog(LinkAttribute.class);
	
	// >>> (2010/02, RuSA) ��������� ������ ������ Ids � ���������� �������� 
	// ������ ���� ����������. �� ���������� ���������� �������� ������� ���.
	// ��������� ���������� �� �������� �� ���������� ��-�� ������������
	// ����� ������ �� "���������" �������� ! �� ���� � ��������� ������� 
	// ���������� ����� ����� ������ ������ id-�� ��������.

	// OLD: private Collection values;

	// ������ id-�� ��������� �������� (������ �������� values: Collection<Card>)
	// final private Set /*<ObjectId>*/ linkedIds = new HashSet();

	// ����� "���������������" ��������. ������������ ��� ����������� (� �������) 
	// ������������� �������� ��������� ��������. ������ ����������� ������ DoSearch.
	// 		captionLinkedCards.keySet: Set<ObjectId> - �������������� ��������� ��������
	// 		captionLinkedCards.values: Collection<String> - ��������� �������� ��������-�����
	// �.�. 02.04.10 - �������� ��������� �������� �� ��, ��� ���������� ����� ����� �������� ��������
	final private Map<ObjectId, String> mapLabels = new LinkedHashMap<ObjectId, String>();

	// private Collection<SearchResult.Column> columns;

	//
	// <<< (2010/02, RuSA)

	// ���� �������� ������� id
	final private List<ObjectId> orderids = new ArrayList<ObjectId>(); // (2011/11/15)
	
	/* id �������� �� ��������� ���������(�), �������� �������� ���� ������� 
	 * ��� ������ getStringValue.
	 */
	private ObjectId labelAttrId;
	
	private boolean multiValued = true;
	
	protected final static String ABSENTTEXT = "";
	
	// abstarct methods begin
	
	abstract public boolean equalValue(Attribute attr);
	
	// ����������� ������ � CardLink � BackLink ���� ����� ���������� �� �������
	abstract public void setLinkedCardLabelText(ObjectId cardId, String value);
	
	abstract public Object getType();
	
	// ����������� ������ � CardLink � BackLink ���� ����� ���������� �� �������
	abstract public void clear();
	
	abstract public void setValueFromAttribute(Attribute attr);
	
	// abstarct methods end
	
	
	/**
	 * @see Attribute#verifyValue
	 */	
	@Override
	public boolean verifyValue() {
		return true;
	}
	
	/**
	 * @see Attribute#isMultiValued
	 */
	@Override
	public boolean isMultiValued() {
		return multiValued;
	}

	public void setMultiValued(boolean multiValued) {
		this.multiValued = multiValued;
	}

	@Override
	public boolean isEmpty() {
		return (this.mapLabels == null) || this.mapLabels.isEmpty();
	}
	
	
	/**
	/* Id �������� � ������������ ���������(�), �������� �������� ���� ������� 
	 * ��� ������ getStringValue
	 * @return the textAttrName
	 */
	public ObjectId getLabelAttrId() {
		return this.labelAttrId;
	}

	/**
	 * @param attrId - ������� � ������������ ��������� ��� ������ ����� getStringValue 
	 */
	public void setLabelAttrId(ObjectId attrId) {
		this.labelAttrId = attrId;
	}
	
	
	public ObjectId[] getIdsArray()
	{
		return (!this.isEmpty()) 
					? orderids.toArray( new ObjectId[0]) //(ObjectId[]) this.mapLabels.keySet().toArray( new ObjectId[0])
					: null;
	}
	
	
	/**
	 * @return ���-�� ��������� �������� (-1 = �� ���, 0 = ��� ���������)
	 */
	public int getLinkedCount()
	{
		return (this.mapLabels != null) ? this.mapLabels.size() : -1;
	}
	
	
	/**
	 * Gets linked cards' ids
	 * @return collection of {@link ObjectId} objects
	 */
	public List<ObjectId> getIdsLinked() {
		// warnIfIsNotMultivalued();
		// return (this.mapLabels != null) ? this.mapLabels.keySet() : null;
		return (orderids != null) ? new ArrayList<ObjectId>(orderids) : null;
	}
	
	
	/**
	 * Gets first linked cards' ids
	 * @return {@link ObjectId} object
	 */
	public ObjectId getFirstIdLinked() {
		return (!CollectionUtils.isEmpty(orderids)) ? orderids.get(0) : null;
	}
	
	
	/**
	 * Gets last linked cards' ids
	 * @return {@link ObjectId} object
	 */
	public ObjectId getLastIdLinked() {
		return (!CollectionUtils.isEmpty(orderids)) ? orderids.get(orderids.size() - 1) : null;
	}
	
	
	/**
	 * ������ ��������� �������� �� ������, ������� ��� ������������ ������.
	 * @param cardsOrIds collection of {@link Card} objects or (@link ObjectId).
	 */
	// public void setLinkedList(Collection value) {
	public void setIdsLinked(Collection<?> cardsOrIds) { 
		// this.values = values;
		this.clear();
		addIdsLinked(cardsOrIds);
	}
	
	
	/**
	 * ������ ��������� �������� (ObjectId), ������� ��� ������������ ������.
	 * @param cardsOrIds object of (@link ObjectId).
	 */
	public void setIdLinked(ObjectId cardOrId) {
		if(cardOrId == null) 
			return;
		setIdsLinked(Collections.singletonList(cardOrId));
	}
	
	
	/**
	 * �������� id � ������ ��������� ��������, �������� ��� ������������.
	 * @param addList ���� ������ id (ObjectId[]) ���� ������ �������� (Card[]).
	 */
	@SuppressWarnings("rawtypes")
	public void addIdsLinked( Collection /*Cards[] or ObjectId[]*/ addList) {
		if (addList == null || addList.isEmpty()) return;
		for (Iterator iterator = addList.iterator(); iterator.hasNext();) {
			final Object item = iterator.next();
			if (item == null) continue;
			if (item instanceof Card) 
				this.addLabelLinkedCard( (Card) item);
			else
				addLinkedId((ObjectId) item);
			//	final ObjectId id = (item instanceof Card) 
			//		? ((Card) item).getId()
			//		: (ObjectId) item;
			//	if (id != null) addLinkedId(id);
		}
		//warnIfIsNotMultivalued();
	}
	
	
	/**
	 * Call of this method results with registering empty-card object with the given card id.
	 * Such empty objects should be initialized later by addLinkedCard.
	 * @param value identifier of card object to add.
	 */
	public void addLinkedId(ObjectId id) {
		if (id == null)
			throw new IllegalArgumentException("Null is not valid as linked cardId");
		if (!Card.class.equals(id.getType()))
			throw new IllegalArgumentException("Invalid linked id type '" + id.getType() + "', supposed to be 'Card'");
		// addValue((Card) DataObject.createFromId(value));
		if (!this.mapLabels.containsKey(id)){
			// ��� ��� ������ id...
			// OLD: this.mapLabels.put(id, (Card) DataObject.createFromId(id) );
			this.mapLabels.put(id, "");
	        orderids.add(id);
		}
	}
	
	
	public void addLinkedId(long id) {
		this.addLinkedId( new ObjectId( Card.class, id));
	}
	
	
	/**
	 * ��������� ��������� ����������� - �.�. ��������� ����� �� ����� ����� 
	 * ��������� �������� � ����� ���������� ��� ���������.
	 * @param id
	 */
	public void addSingleLinkedId(ObjectId id) {
		if (id == null)
			throw new IllegalArgumentException("Null is not valid as linked cardId");
		this.clear();
		this.multiValued = false;
		this.addLinkedId(id);
	}


	public void addSingleLinkedId(long id) {
		this.addSingleLinkedId( new ObjectId( Card.class, id));
	}
	
	
	/**
	 * Remove card with given card id from values collection
	 * @param id identifier of card to be removed 
	 */
	public boolean removeLinkedId(ObjectId id) {
		// values.remove((Card) DataObject.createFromId(id));
		final boolean result = this.mapLabels.containsKey(id);
		if (result) {
			this.mapLabels.remove(id);
			this.orderids.remove(id);
		}
		return result;
	}
	
	
	/**
	 * Remove card from values collection
	 * @param card card object to remove
	 */
	// public void removeValue(Card value) {
	public boolean removeLabelLinkedCard(Card card) {
		return (card != null) && removeLinkedId(card.getId());
	}
	
	
	/**
	 * ������� ��������� id � ��������, ����� ��������� ����� �� ����� ������.
	 * @return id ��������� �������� ��� null, ���� ����� ���.
	 */
	public ObjectId getSingleLinkedId()
	{
		if (isMultiValued())
			logger.warn("CardLinkAttribute is multivalued, but is used as single-valued");
		return (this.isEmpty()) 
					? null 
					: (ObjectId) this.mapLabels.keySet().iterator().next();
	}
	
	public Map<ObjectId, String> getLabelLinkedMap() {
		return this.mapLabels; 
	}
	
	
	List<ObjectId> getOrderIdsList() {
		return this.orderids;
	}
	
	
	/**
	 * Adds {@link Card} to values collection
	 * @param card card to add
	 * @throws IllegalArgumentException in case of attemt to add null reference to collection
	 */
	public void addLabelLinkedCard(Card card) {
		if (card == null)
			throw new IllegalArgumentException("Null is not valid as linked card");

		final ObjectId id = card.getId();
		if (id == null)
			throw new IllegalArgumentException("Null is not valid as linked cardId");

		if (!Card.class.equals(id.getType()))
			throw new IllegalArgumentException("Invalid linked id type '" + id.getType() + "', supposed to be 'Card'");

		//	if ( this.mapLabels.containsKey(id) ){
		//		logger.warn( "WARNING! Duplicate of linked card id " + id + " in attribute " + this.getId() + " was removed");
		//	} else 
		{
                        // ���������� ������ ���������� �������� ...
			if (!this.mapLabels.containsKey(id))
				this.orderids.add(id);

			final String cardText = makeLinkedCardStrText(card, this.labelAttrId, ABSENTTEXT);
			this.mapLabels.put(id, cardText);
			//warnIfIsNotMultivalued();
		}
	}
	
	
	/**
	 * Returns comma-separated list of card labels:
	 *     if labelAttrid is defined: 
	 *     	  then the list of card attributes with such id's,
	 *     otherwise (labelAttrid is null):
	 *     	  the list of referenced card identifiers.
	 * @return comma-separated list of referenced card labels.
	 * @note: (2009/12/11, RuSA) since that time in the cases when cards ids
	 * are in need, you must use another method : (@link getLinkedIds()). 
	 */
	@Override
	public String getStringValue() {
		
		if (this.mapLabels == null)
			return "";
		final StringBuffer buf = new StringBuffer();
		final ObjectId[] array = this.getIdsArray();
		if (array != null)
			for (int i = 0; i < array.length; i++) {
				final String sValue = getLinkedCardLabelText(array[i], ABSENTTEXT);
				buf.append(sValue); 
				if (i < array.length - 1) buf.append(", ");
			}

		return buf.toString();
	}
	
	
	/**
	 * @return comma-separated list of referenced card identifiers, 
	 * like "233, 4056, 5034"
	 */
	public String getLinkedIds() {
		
		if (this.mapLabels == null)
			return "";

		final StringBuffer buf = new StringBuffer();
		final ObjectId[] array = this.getIdsArray();
		if (array != null)
			for (int i = 0; i < array.length; i++) {
				buf.append(getDefaultCardIdText(array[i], ABSENTTEXT)); 
				if (i < array.length - 1) buf.append(", ");
			}
		return buf.toString();
	}
	
	
	/**
	 * @param card: �������� �� ������� ���� �������� �������.
	 * @param attrId: id ��� ��������� � �������� itemCard, ����� ���� null.
	 * @param nullDefault: ��������, ������� ���� ������� ��� ���������� 
	 * �������� attrId � �������� card.
	 * @return �������� �������� ��� nullDefault, ���� ������ ���. 
	 */
	public String getLinkedCardLabelText(ObjectId cardId, String nullDefault) 
	{
		if (cardId != null) {
			final String result = (String) this.mapLabels.get(cardId);
			if (result != null)
				return result;
		}
		return nullDefault;
	}
	
	
	protected static String makeLinkedCardStrText(Card card, ObjectId labelAttrId, String nullDefault) 
	{
		if (card != null) {
			if (labelAttrId == null)
				return getDefaultCardIdText(card.getId(), nullDefault);
			// �������� �������� ��������
			final Attribute attr = card.getAttributeById(labelAttrId);
			if (attr != null)
				// ����� ������ � id
				// return MessageFormat.format("{0} ({1})", new Object[] { attr.getStringValue(), String.valueOf(card.getId().getId()) }); 
				return attr.getStringValue();
			return getDefaultCardIdText(card.getId(), nullDefault);
		}
		return nullDefault;
	}
	
	
	protected static String getDefaultCardIdText(final ObjectId cardId, final String nullDefault) {
		if ((cardId != null) && (cardId.getId() != null))
			return cardId.getId().toString(); 
		return nullDefault;
	}

}
