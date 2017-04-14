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
package com.aplana.cms;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.aplana.dbmi.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.SearchResult.Column;
import com.aplana.dbmi.model.util.ObjectIdUtils;

public class ContentUtils
{
	public static final String NAMES_SEPARATOR = ":";
	
	private static Log logger = LogFactory.getLog(ContentUtils.class);
	
    public static long[] getPermissionTypes( Search search ) {
    	
        ArrayList<Long> result = new ArrayList<Long>();    	
    	
        Search.Filter filter = search.getFilter();
        if ( filter == null ) {
            return convertToArray(result);
        }
        
        return getPermissionTypes(filter.getCurrentUserPermission());
    }	
    
    public static long[] getPermissionTypes(Long userPermission) {
    	
        ArrayList<Long> result = new ArrayList<Long>();    	
        
        if ( userPermission.equals( Search.Filter.CU_DONT_CHECK_PERMISSIONS ) ) {
            return convertToArray(result);
        }

        if ( userPermission.equals( Search.Filter.CU_RW_PERMISSIONS ) ) {
            result.add( Search.Filter.CU_READ_PERMISSION );
            result.add( Search.Filter.CU_WRITE_PERMISSION );
        } else if ( userPermission.equals( Search.Filter.CU_READ_PERMISSION ) ) {
            result.add( Search.Filter.CU_READ_PERMISSION );
        } else if ( userPermission.equals( Search.Filter.CU_WRITE_PERMISSION ) ) {
            result.add( Search.Filter.CU_READ_PERMISSION );
            result.add( Search.Filter.CU_WRITE_PERMISSION );
        } else {
            result.add( 0L );
        }
        return convertToArray(result);
    }

    public static Attribute getAttribute(Card card, String name)
	{
        return getAttribute(card, name, false);
    }
    
    public static Attribute getAttribute(Card card, String name, boolean onlyPlain)
	{
        return getAttribute(card, name, onlyPlain, false);
    }
    
    
      

	public static Attribute getAttribute(Card card, String name, boolean onlyPlain, boolean orderCards)
	{
		if (card.getAttributes() == null)
			throw new IllegalArgumentException("Card must contain attributes");
		name = name.trim();
		
		if (name.equalsIgnoreCase(namesRu.getString(StateAttribute.NAME_KEY)) ||
				name.equalsIgnoreCase(namesEn.getString(StateAttribute.NAME_KEY)) ||
				Card.ATTR_STATE.getId().equals(name)) {
			Attribute attr = card.getAttributeById(Card.ATTR_STATE);
			if (attr == null)
				attr = new StateAttribute(card);
			return attr;
		}
		if (name.equalsIgnoreCase(namesRu.getString(TemplateAttribute.NAME_KEY)) ||
				name.equalsIgnoreCase(namesEn.getString(TemplateAttribute.NAME_KEY)) ||
				Card.ATTR_TEMPLATE.getId().equals(name)) {
			Attribute attr = card.getAttributeById(Card.ATTR_TEMPLATE);
			if (attr == null)
				attr = new TemplateAttribute(card);
			return attr;
		}
			
		String blockName = null;
		if (name.contains(NAMES_SEPARATOR)) {
			blockName = name.substring(0, name.indexOf(NAMES_SEPARATOR)).trim();
			name = name.substring(name.indexOf(NAMES_SEPARATOR) + NAMES_SEPARATOR.length()).trim();
		}
		for (Iterator<DataObject> itr = card.getAttributes().iterator(); itr.hasNext(); ) {
			Object item = itr.next();
			Collection<Attribute> attributes = null;
			if (item instanceof AttributeBlock) {
				AttributeBlock block = (AttributeBlock) item;
				if (blockName != null &&
						!blockName.equalsIgnoreCase(block.getNameRu()) &&
						!blockName.equalsIgnoreCase(block.getNameEn()) &&
						!blockName.equals(block.getId().getId()))
					continue;
				attributes = block.getAttributes();
			} else if (item instanceof Attribute)
				attributes = Collections.singleton((Attribute)item);
			if (attributes == null) {
				logger.error("No attribute list");
				continue;
			}
			for (Iterator<Attribute> itrAttr = attributes.iterator(); itrAttr.hasNext(); ) {
				Attribute attr = itrAttr.next();
				if (name.equalsIgnoreCase(attr.getNameRu()) ||
						name.equalsIgnoreCase(attr.getNameEn()) ||
						name.equals(attr.getId().getId()))
                    if(!(onlyPlain && attr instanceof LinkAttribute) && isOrderCards(attr, orderCards)) {
					    return attr;
                    }
			}
		}
		return null;
	}
	
	//��������� �������� �� ������� ������� ����������� CardLinkAttribute, BackLinkAttribute ��� PersonAttribute
	private static boolean isOrderCards(Attribute attr, boolean orderCards){
		if(!orderCards){
			return true;
		}
		if(attr instanceof LinkAttribute
				&& ((LinkAttribute)attr).getLabelAttrId() == null){
			return true;
		}
		if(attr instanceof PersonAttribute
				&& ((PersonAttribute)attr).getValues() != null){
			return true;
		}		
		return false;
	}
	
	public static ObjectId getParentAreaId(Card area)
	{
		CardLinkAttribute attr = (CardLinkAttribute) area.getAttributeById(ContentIds.ATTR_PARENT);
		// (2010/02, RuSA) CardLinkAttribute::getValues()
		if (attr == null || attr.getLinkedCount() < 1)
			return null;
		// return ((Card) attr.getValues().iterator().next()).getId();
		return attr.getSingleLinkedId();
	}

	public static void sortCards(List<Card> cards, ObjectId attrId, boolean ascending)
	{
		Collections.sort(cards, new CardComparator(attrId, ascending));
	}
	
	public static void sortCards(List<Card> cards, String field, boolean ascending)
	{
		if (cards.size() == 0)
			return;
		Attribute attr = null;
		for (Iterator<Card> itr = cards.iterator(); itr.hasNext() && attr == null; )
			attr = getAttribute(itr.next(), field);
		if (attr != null)
			Collections.sort(cards, new CardComparator(attr.getId(), ascending));
	}
	
	public static List<Card> sortCardsById(List<Card> cards, Collection<ObjectId> ids)
	{
		if (cards.size() <= 1)
			return cards;
		Map<ObjectId, Card> map = ObjectIdUtils.collectionToObjectIdMap(cards);
		List<Card> sorted = new ArrayList<Card>(cards.size());
		for (Iterator<ObjectId> itr = ids.iterator(); itr.hasNext(); ) {
			ObjectId id = itr.next();
			if (map.containsKey(id))
				sorted.add(map.get(id));
			else
				logger.warn("Card " + id + " not found (access problems?)");
		}
		return sorted;
	}

	public static List<Card> extendSearchResult(SearchResult result) {
		List<Card> cards = result.getCards();
		extendSearchResult(cards, result.getColumns(), false);
		return cards;
	}
	
	public static List<Card> extendSearchResult(SearchResult result, boolean wereSortColumns) {
		List<Card> cards = result.getCards();
		extendSearchResult(cards, result.getColumns(), wereSortColumns);
		return cards;
	}
	
	public static void extendSearchResult(List<Card> cards, Collection<Column> searchResultColumns, boolean wereSortColumns)
	{
		final HashMap<ObjectId, Column> attributes = new HashMap<ObjectId, Column>();
		for (Iterator<Column> itr = searchResultColumns.iterator(); itr.hasNext(); ) {
			SearchResult.Column col = itr.next();
			attributes.put(col.getAttributeId(), col);
			if (wereSortColumns) {
				continue; // there were sort columns for search, therefore cards should already be sorted here
			}
			if (col.getSorting() != SearchResult.Column.SORT_NONE) {
				ContentUtils.sortCards(cards, col.getAttributeId(),
						col.getSorting() == SearchResult.Column.SORT_ASCENDING);
			}
		}
		for (Iterator<Card> itrCard = cards.iterator(); itrCard.hasNext(); ) {
			Card card = itrCard.next();
			for (Iterator<?> itr = card.getAttributes().iterator(); itr.hasNext(); ) {
				Attribute attr = (Attribute) itr.next();
				if (attributes.containsKey(attr.getId())) {
					SearchResult.Column col = attributes.get(attr.getId());
					attr.setNameRu(col.getNameRu());
					attr.setNameEn(col.getNameEn());
				}
			}
		}
	}
	
	private static class CardComparator implements Comparator<Card>
	{
		private int sign;
		private ObjectId attrId;
		
		public CardComparator(ObjectId attrId, boolean ascending)
		{
			this.attrId = attrId;
			this.sign = ascending ? 1 : -1;
		}
		
		public int compare(Card left, Card right)
		{
			Attribute lattr = left.getAttributeById(attrId);
			Attribute rattr = right.getAttributeById(attrId);
			if (lattr == null)
				return rattr == null ? 0 : -sign;
			if (rattr == null)
				return sign;
			if (Attribute.TYPE_DATE.equals(lattr.getType()))
				return sign * compareDate(((DateAttribute) lattr).getValue(),
						((DateAttribute) rattr).getValue());
			if (Attribute.TYPE_INTEGER.equals(lattr.getType())) {
				int lval = ((IntegerAttribute) lattr).getValue();
				int rval = ((IntegerAttribute) rattr).getValue();
				return lval < rval ? -sign : (lval > rval ? sign : 0);
			}
            if (Attribute.TYPE_LONG.equals(lattr.getType())) {
				long lval = ((LongAttribute) lattr).getValue();
				long rval = ((LongAttribute) rattr).getValue();
				return lval < rval ? -sign : (lval > rval ? sign : 0);
			}
			return sign * lattr.getStringValue().compareTo(rattr.getStringValue());
		}
		
		private int compareDate(Date date1, Date date2) {
			// Nonset dates go down
			if (date1 == null)
				return date2 == null ? 0 : 1;
			if (date2 == null)
				return -1;
			return date1.compareTo(date2);
		}
	}
	
	private static final ResourceBundle namesRu =
		ResourceBundle.getBundle(ContextProvider.MESSAGES, ContextProvider.LOCALE_RUS);
	private static final ResourceBundle namesEn =
		ResourceBundle.getBundle(ContextProvider.MESSAGES, ContextProvider.LOCALE_ENG);
	
	public static class StateAttribute extends ListAttribute
	{
		//public static final String NAME_RU = "������";
		//public static final String NAME_EN = "State";
		public static final String NAME_KEY = "search.column.state";
		
		private final Card card;
		
		public StateAttribute(Card card) {
			this.card = card;
			setId(Card.ATTR_STATE);
			setNameRu(namesRu.getString(NAME_KEY));
			setNameEn(namesEn.getString(NAME_KEY));
		}

		public String getStringValue() {
			return (card.getStateName()!=null?card.getStateName().getValue():null);	// ����� �� ������� ��� ���������, ����� ������ �� �������� 
		}

		public ReferenceValue getValue() {
			ReferenceValue value = new ReferenceValue();
			value.setValueRu(card.getStateName().getValueRu());
			value.setValueEn(card.getStateName().getValueEn());
			return value;
		}

		public boolean isReadOnly() {
			return true;
		}
	}
	
	public static class TemplateAttribute extends ListAttribute
	{
		//public static final String NAME_RU = "������";
		//public static final String NAME_EN = "Template";
		public static final String NAME_KEY = "search.column.template";
		
		private final Card card;
		
		public TemplateAttribute(Card card) {
			this.card = card;
			setId(Card.ATTR_TEMPLATE);
			setNameRu(namesRu.getString(NAME_KEY));
			setNameEn(namesEn.getString(NAME_KEY));
		}

		public String getStringValue() {
			return card.getTemplateName();
		}

		public ReferenceValue getValue() {
			ReferenceValue value = new ReferenceValue();
			value.setValueRu(card.getTemplateNameRu());
			value.setValueEn(card.getTemplateNameEn());
			return value;
		}

		public boolean isReadOnly() {
			return true;
		}
	}
	
	public static class LinkTypeAttribute extends ListAttribute
	{
		public static final ObjectId ID = new ObjectId(ListAttribute.class, "_LINK_TYPE_");
		//public static final String NAME_RU = "��� �����";
		//public static final String NAME_EN = "Type of link";
		public static final String NAME_KEY = "search.column.linktype";
		
		public LinkTypeAttribute(ReferenceValue value) {
			setId(ID);
			setNameRu(namesRu.getString(NAME_KEY));
			setNameEn(namesEn.getString(NAME_KEY));
			setValue(value);
		}
	}
	
	public static void writeText(PrintWriter writer, String text)
	{
		writer.write(text
				.replaceAll("\\&", "&amp;")
				.replaceAll("<", "&lt;")
				.replaceAll("\\n", "<br>")
				.replaceAll("'", "\\\\'")
				.replaceAll("�", "&rsquo;")
				.replaceAll("�", "&lsquo;")
			);
	}
	
	private static long[] convertToArray(List<Long> permissionTypes) {
        long[] array = new long[permissionTypes.size()];

        int i = 0;
        for (Long permissionType : permissionTypes) {
            array[i++] = permissionType.longValue();
        }

        return array;
    }
}
