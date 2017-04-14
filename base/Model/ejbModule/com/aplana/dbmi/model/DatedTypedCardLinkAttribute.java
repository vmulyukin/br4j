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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DatedTypedCardLinkAttribute extends TypedCardLinkAttribute {

	/**
	 * @author ppolushkin
	 */
	private static final long serialVersionUID = 1L;
	
	// ����������� card_id �� date_value
	private Map<Long, Date> dates;
	
	public static final String defaultTimePattern = "dd-MM-yyyy";
	private String timePattern;
	
	public Object getType() {
		return TYPE_DATED_TYPED_CARD_LINK;
	}
	
	public void addTypeDate(Long cardId, Long typeId, Date date) {
		addDate(cardId, date);
		super.addType(cardId, typeId);
	}
	
	public void addDate(Long cardId, Date date) {
		if (cardId == null) return;
		if (dates == null)
			dates = new HashMap<Long,Date>();
		dates.put(cardId, date);
	}
	
	/**
	 * Returns type of link for given cardId
	 * @param cardId identifier of Card
	 * @return identifier of link type ({@link ReferenceValue})
	 */
	public Date getCardDate(ObjectId cardId) {
		if (dates == null) {
			return null;
		} else {
			return (Date)dates.get(cardId.getId());
		}
	}
	
	public Map<Long, Date> getDates() {
		if (dates == null)
			dates = new HashMap<Long,Date>();
		return dates;
	}
	
	public void setDates(Map<Long, Date> dates) {
		this.dates = dates;
	}
	
	public String getTimePattern() {
		return timePattern;
	}

	public void setTimePattern(String timePattern) {
		this.timePattern = timePattern;
	}

	public Date getDate(Long id) {
		return getDates().get(id);
	}
	
	public String getFormattedDateValue(Long id) {
		Date date = getDate(id);
		if (date == null)
			return "";	
		if (this.timePattern != null) 
			return ContextProvider.getContext().getLocaleDateTime(date, this.timePattern);
		else
			return ContextProvider.getContext().getLocaleDateTime(date, defaultTimePattern);
	}
	
	@Override
	public boolean equalValue(Attribute attr) {
		if (!(DatedTypedCardLinkAttribute.class.equals(attr.getClass()))) {
			throw new IllegalArgumentException("Incorrect attribute type comparison: " +
					getClass().getName() + " [" + getId().getId() + "] against " +
					attr.getClass().getName() + " [" + attr.getId().getId() + "]");
		}
		
		DatedTypedCardLinkAttribute otherAttr = (DatedTypedCardLinkAttribute) attr;
		
		final Map/*<ObjectId, String>*/ map = getLabelLinkedMap(),
			otherMap = otherAttr.getLabelLinkedMap();
		
		if (map == null || otherMap == null) {
			return map == otherMap;
		}
		
		if (map.size() != otherMap.size()) {
			return false;
		}
		
		Iterator i = map.keySet().iterator();
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
			Date d1 = getCardDate(cardId),
					d2 = otherAttr.getCardDate(cardId);
			if (d1 == null || d2 == null) {
				if (d1 != d2) {
					return false;
				}
			} else {
				if (!d1.equals(d2)) {
					return false;
				}
			}
			
		}
		return true;
	}

	@Override
	public void clear() {
		super.clear();
		dates = new HashMap<Long, Date>();
	}
	
	@Override
	public void setIdsLinked(Collection/*<?>*/ cardsOrIds) {

		if (this.dates == null) {
			super.setIdsLinked(cardsOrIds);
			return;			
		}

		final Map<Long, Date> saved_types = new HashMap<Long, Date>(this.dates);

		super.setIdsLinked(cardsOrIds);

		// ����������� dates...
		if (!isEmpty() && !saved_types.isEmpty()) {
			for (Iterator iterator = getIdsLinked().iterator(); iterator.hasNext();) {
				final Long lcardId = (Long) ((ObjectId) iterator.next()).getId();
				if (lcardId != null)
					this.dates.put(lcardId, saved_types.get(lcardId));
			}
		}
	}
	
	@Override
	public void setValueFromAttribute(Attribute attr){
		if(this.getClass().isAssignableFrom(attr.getClass())){
			this.clear();
			super.setValueFromAttribute(attr);
			DatedTypedCardLinkAttribute dated = (DatedTypedCardLinkAttribute) attr;
			if(dated.getDates() == null){
				return;
			}
			for(Object object : dated.getDates().entrySet()) {
				Map.Entry<?,?> entry = (Map.Entry<?,?>) object;
				this.addDate((Long) entry.getKey(), (Date) entry.getValue());
			}
		}
	}
	
	@Override
	public void addValuesFromAttribute(CardLinkAttribute attr){
		if(attr != null && attr.getIdsLinked() != null){
			if(attr instanceof DatedTypedCardLinkAttribute){
				DatedTypedCardLinkAttribute datedAttr = (DatedTypedCardLinkAttribute) attr;
				if(datedAttr.getDates() == null) return;
				for(Object object : datedAttr.getDates().entrySet()){
					Map.Entry<?, ?> entry = (Map.Entry<?, ?>) object;
					Long key = (Long) entry.getKey();
					Date value = (Date) entry.getValue();
					addDate(key, value);
				}
			} else if(attr instanceof TypedCardLinkAttribute) {
				super.addValuesFromAttribute(attr);
			} else setIdsLinked(attr.getIdsLinked());
		}
	}

}
