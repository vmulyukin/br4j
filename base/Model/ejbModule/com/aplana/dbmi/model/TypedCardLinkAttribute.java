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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TypedCardLinkAttribute extends CardLinkAttribute implements ReferenceConsumer {
	private static final long serialVersionUID = 1L;

	protected ObjectId reference;
	protected Collection<ReferenceValue> referenceValues;

	// ���� ������ �������� - ����������� card_id �� value_id �����������
	// types ����� ��� HashMap<Long, Long>
	private Map<Long,Long> types;
	
	public Object getType() {
		return TYPE_TYPED_CARD_LINK;
	}

	// Methods ReferenceConsumer
	/**
	 * Gets identifier of dictionary used by this attribute instance
	 * @return identifier of {@link Reference} object used by this attribute
	 */
	public ObjectId getReference() {
		return reference;
	}
	
	/**
	 * Sets identifier of dictionary to be used as the source of available values
	 * for this attribute instance 
	 */
	public void setReference(ObjectId reference) {
		this.reference = reference;		
	}
	
	/**
	 * Gets list of available values
	 * @return list of {@link ReferenceValue} object that could be used as a values 
	 * for this attribute instance
	 */
	public Collection<ReferenceValue> getReferenceValues() {
		return referenceValues;		
	}
	
	/**
	 * Sets list of available values for this attribute instance
	 */
	public void setReferenceValues(Collection<ReferenceValue> referenceValues) {
		this.referenceValues = referenceValues;		
	}

	/**
	 * TODO: �������������! ������ �������� ������ ���� ObjectId<Card>,
	 * ������ - ObjectId<ReferenceValue>
	 * @param cardId
	 * @param typeId
	 */
	public void addType(Long cardId, Long typeId) {
		if (cardId == null) return;
		if (types == null)
			types = new HashMap<Long,Long>();
		types.put(cardId, typeId);
		super.addLinkedId(cardId.longValue());
	}
	
	/**
	 * Returns type of link for given cardId
	 * @param cardId identifier of Card
	 * @return identifier of link type ({@link ReferenceValue})
	 */
	public ObjectId getCardType(ObjectId cardId) {
		if (types == null) {
			return null;
		} else {
			Long refId = types.get(cardId.getId()); 
			return refId == null ? null : new ObjectId(ReferenceValue.class, refId);
		}
	}
	
	public Map<Long,Long> getTypes() {
		return types;
	}
	
	public Object getType(Long id) {
		return getTypes().get(id);
	}
	
	public void setTypes(Map<Long,Long> types) {
		this.types = types;
	}

	public boolean equalValue(Attribute attr) {
		if (!(TypedCardLinkAttribute.class.equals(attr.getClass()))) {
			throw new IllegalArgumentException("Incorrect attribute type comparison: " +
					getClass().getName() + " [" + getId().getId() + "] against " +
					attr.getClass().getName() + " [" + attr.getId().getId() + "]");
		}
		
		TypedCardLinkAttribute otherAttr = (TypedCardLinkAttribute) attr;
		
		final Map<ObjectId, String> map = getLabelLinkedMap(),
			otherMap = otherAttr.getLabelLinkedMap();
		
		if (map == null || otherMap == null) {
			return map == otherMap;
		}
		
		if (map.size() != otherMap.size()) {
			return false;
		}
		
		Iterator<ObjectId> i = map.keySet().iterator();
		while (i.hasNext()) {
			ObjectId cardId = (ObjectId)i.next();
			if (!otherMap.containsKey(cardId)) {
				return false;
			}
			ObjectId refId1 = getCardType(cardId),
				refId2 = otherAttr.getCardType(cardId);
			if (refId1 == null || refId2 == null) {
				if (refId1 != refId2) {
					return false;
				}
			} else {
				if (!refId1.equals(refId2)) {
					return false;
				}
			}
		}
		return true;
	}

	public void clear() {
		super.clear();
		types = new HashMap<Long,Long>();		
	}

	public void setIdsLinked(Collection<?> cardsOrIds) {
		// ����� �� �������� ���� � �����, ����� ���������� ����� �������...
		if (this.types == null) {
			// ������ ���������-���������������...
			super.setIdsLinked(cardsOrIds); // ��� ��� ����� ������ clear() � types ���������...
			return;			
		}

		final Map<Long,Long> saved_types = new HashMap<Long, Long>(types);

		super.setIdsLinked(cardsOrIds); // ��� ��� ����� ������ clear() � types ���������...

		// ����������� ��� ����� � types...
		if (!isEmpty() && !saved_types.isEmpty()) {
			for (Iterator<ObjectId> iterator = getIdsLinked().iterator(); iterator.hasNext();) {
				final Long lcardId = (Long) iterator.next().getId();
				if (lcardId != null)
					this.types.put( lcardId, saved_types.get(lcardId));
			}
		}
	}
	
	@Override
	public void setValueFromAttribute(Attribute attr){
		if(this.getClass().isAssignableFrom(attr.getClass())){
			this.clear();
			TypedCardLinkAttribute typed = (TypedCardLinkAttribute) attr;
			if(typed.getTypes() == null){
				return;
			}
			for(Map.Entry<Long,Long> entry : typed.getTypes().entrySet()) {
				this.addType(entry.getKey(), entry.getValue());
			}
		}
	}
	
	@Override
	public void addValuesFromAttribute(CardLinkAttribute attr){
		if(attr != null && attr.getIdsLinked() != null){
			if(attr instanceof TypedCardLinkAttribute){
				TypedCardLinkAttribute typedAttr = (TypedCardLinkAttribute) attr;
				if(typedAttr.getTypes() == null) return;
				for(Map.Entry<Long,Long> entry : typedAttr.getTypes().entrySet()){
					Long key = entry.getKey();
					Long value = entry.getValue();
					addType(key, value);
				}			
			} else setIdsLinked(attr.getIdsLinked());
		}
	}
}
