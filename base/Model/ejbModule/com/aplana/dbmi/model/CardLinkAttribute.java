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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;

/**
 * {@link Attribute} representing set of links to other cards
 * Set of available cards is defined as result of specific {@link com.aplana.dbmi.action.Search} query defined by {@link #getFilter()} property
 * This filter is stored in ATTRIBUTE_OPTION and XML_DATA tables
 * NOTE: usually {@link Card} instances in values collection have only
 * identifier initialized, without values of any attributes.
 * If it is neccessary to work with full card object, you should 
 * fetch full copy of card's data with separate action
 * <br>
 */
public class CardLinkAttribute extends LinkAttribute {
	private static final long serialVersionUID = 3L;

	
	private String filterXml;
	private transient Search filter;


	/*private void warnIfIsNotMultivalued() {
		if( !isMultiValued() && getLinkedCount() > 1)
			logger.warn("CardLinkAttribute is not multivalued, but has more than one card");
	}*/

	public void �opyIdLabelsFrom(CardLinkAttribute src){
		this.clear();
		Map<ObjectId,String> mapLabels = super.getLabelLinkedMap();
		if (src == null || src.getLabelLinkedMap() == null || src.getLabelLinkedMap().isEmpty()) 
			return;
		List<ObjectId> orderids = getOrderIdsList();
		orderids.addAll(src.getOrderIdsList());
		mapLabels.putAll(src.getLabelLinkedMap());
	}

//	/**
//	 * @deprecated
//	 */
//	public void setLabelLinkedCards(Collection /*<Card>*/ list) {
//
//		this.clearLinked();
//		// if (this.mapCards == null) this.mapCards = new HashMap();
//
//		// ������������� ...
//		if (list == null || list.isEmpty()) return; 
//		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
//			this.addLabelLinkedCard( (Card) iterator.next() );
//		}
//	}

//	/**
//	 * ������ ��������� �������� �� ������, �������� ��� ������������.
//	 * @param cards
//	 */
//	public void addLabelLinkedCards(Collection /*Cards[]*/ addCardsList) {
//		if (addCardsList == null || addCardsList.isEmpty()) return;
//		for (Iterator iterator = addCardsList.iterator(); iterator.hasNext();) {
//			addLabelLinkedCard( (Card) iterator.next() );
//		}
//	}


/*	
 * 	public Collection getColumns()
 * 	public void setColumns(Collection columns)
 * 		use session context, example using at (@see UserPortlets::LinkedCardUtils) 
 */

	/**
	 * Gets {com.aplana.dbmi.action.Search} object defining set of available cards
	 * @return XML-string representation of {@link #getFilter} result
	 */
	public String getFilterXml() {
		return filterXml;
	}

	/**
	 * Sets {com.aplana.dbmi.action.Search} object which defines set of available cards
	 * @param filterXml XML string representation of {com.aplana.dbmi.action.Search} object
	 */
	public void setFilterXml(String filterXml) {
		this.filterXml = filterXml != null ? filterXml.trim() : null;
		this.filter = null;
	}

	/**
	 * Deserializes filterXML property and return resulting {com.aplana.dbmi.action.Search} object.
	 * Deserialization is performed only on the first call. Afterwards  this value is cached. 
	 * @return {com.aplana.dbmi.action.Search} object which defines set of available cards
	 * @throws DataException in case of deserialization error
	 */
	public Search getFilter() throws DataException {
		if (filter == null && filterXml != null) {
			filter = new Search();
			try {
				filter.initFromXml(new ByteArrayInputStream(filterXml.getBytes("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				throw new RuntimeException("UTF-8 support needed");
			}
		}
		return filter;
	}

	/**
	 * Sets {com.aplana.dbmi.action.Search} object which defines set of available cards.
	 * Additionally sets value of filterXML property to XML-string representing given search object
	 * @param filter {com.aplana.dbmi.action.Search} object
	 */
	public void setFilter(Search filter) {
		if (filter == null) {
			LogFactory.getLog(this.getClass()).error("Cannot set null filter!");
			return;
		}
		ByteArrayOutputStream xml = new ByteArrayOutputStream();
		try {
			filter.storeToXml(xml);
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw new RuntimeException("I/O error storing filter object to XML", e);
		}
		this.filter = filter;
		try {
            String strXmlFilter = xml.toString("UTF-8");
            this.filterXml = strXmlFilter != null ? strXmlFilter.trim() : null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException("UTF-8 support needed");
		}
	}


	/**
	 * Checks if this attribute contains same set of links to other cards as given {@link CardLinkAttrbute}
	 * @param attr {@link Attribute} to compare with. Should be CardLinkAttribute instance.
	 * @throws  IllegalArgumentException if attr is not an CardLinkAttribute instance
	 */
	@Override
	public boolean equalValue(Attribute attr) {
		if (!CardLinkAttribute.class.isAssignableFrom(attr.getClass()) ) {
			throw new IllegalArgumentException("Incorrect attribute type comparison: " +
					getClass().getName() + " [" + getId().getId() + "] against " +
					attr.getClass().getName() + " [" + attr.getId().getId() + "]");
		}
		
		// final Map/*<ObjectId, String>*/ otherMap = ((CardLinkAttribute) attr).mapLabels;
		// return ObjectIdUtils.isSameObjectIdsInSameOrder(this.mapLabels.keySet(), otherMap.keySet());
		List<ObjectId> orderids = getOrderIdsList();
		return ObjectIdUtils.isSameObjectIdsInSameOrder(orderids, ((CardLinkAttribute) attr).getOrderIdsList());
	}
	
	// TODO: ����������� ������ � CardLink � BackLink ���� ����� ���������� �� �������
	@Override
	public void setLinkedCardLabelText(ObjectId cardId, String value) 
	{
		if (cardId != null) {
			Map<ObjectId,String> mapLabels = super.getLabelLinkedMap();
			if (!mapLabels.containsKey(cardId)) {
				List<ObjectId> orderids = getOrderIdsList();
				orderids.add(cardId);
			}
			mapLabels.put(cardId, value);
		}
	}

	/**
	 * @see Attribute#getType
	 */
	@Override
	public Object getType() {
		return TYPE_CARD_LINK;
	}

	// TODO: ����������� ������ � CardLink � BackLink ���� ����� ���������� �� �������
	@Override
	public void clear() {
		Map<ObjectId,String> mapLabels = super.getLabelLinkedMap();
		List<ObjectId> orderids = getOrderIdsList();
		mapLabels.clear();
		orderids.clear();
	}

	@Override
	public void setValueFromAttribute(Attribute attr) {
		if(this.getClass().isAssignableFrom(attr.getClass())){
			this.clear();
			this.addIdsLinked(((CardLinkAttribute) attr).getIdsLinked());
		}
		
	}
	
	/**
	 * Add values from Attribute, passed as parameter. Sets of values will be joined.
	 * @param attr
	 */
	public void addValuesFromAttribute(CardLinkAttribute attr){
		if(attr != null && attr.getIdsLinked() != null) for(ObjectId id : attr.getIdsLinked()) addLinkedId(id);
	}
}
