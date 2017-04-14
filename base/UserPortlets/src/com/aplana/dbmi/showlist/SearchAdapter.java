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
package com.aplana.dbmi.showlist;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.CheckIgnoreStates;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.SearchResult.Column;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;

public class SearchAdapter
{
	protected final Log logger = LogFactory.getLog(getClass());
	
	private static String RUSSIAN = "ru";
	private static String ENGLISH = "en";
	private SearchResult result;
	private String cardLinkDelimiter;	// �������������� ������ ��� ��������� CardLink, Person � TypedCardLink
	private DataServiceBean serviceBean;
	
	public SearchAdapter(DataServiceBean serviceBean) {
		this.serviceBean = serviceBean;
	}
	
	// �������� ��� �������������
	public SearchAdapter() {
	}
	
	public SearchResult getSearchResult() {
		return result;
	}

	public void setSearchResult(SearchResult result) {
		this.result = result;
	}
	
	public String getCardLinkDelimiter() {
		return cardLinkDelimiter;
	}

	public void setCardLinkDelimiter(String cardLinkDelimiter) {
		this.cardLinkDelimiter = cardLinkDelimiter.replaceAll(Matcher.quoteReplacement("\\n"), "\n").replaceAll(Matcher.quoteReplacement("\\t"), "\t").replaceAll(Matcher.quoteReplacement("\\\\"), "\\").replaceAll(Matcher.quoteReplacement("\\'"), "\'").replaceAll(Matcher.quoteReplacement("\\r"), "\r");	// esc-������������������ �������� ������ � ��������� �������� �� �������������� �������
	}

	public void executeSearch(DataServiceBean service, Action search) 
		throws DataException, ServiceException
	{
		result = (SearchResult) service.doAction(search);
	}
	
	/**
	 * ���������� ������ ����-������ �� ��������. 
	 * @param exportLocale Locale �������. ������������ ������ ��� �������� � Excel. ����� ����� null.
	 * @return ������ �������
	 */
	@SuppressWarnings("unchecked")
	public List<SearchResult.Column> getMetaData(String exportLocale)
	{
		ArrayList<SearchResult.Column> orginalColumns = result.getActiveColumns();
		final ArrayList<SearchResult.Column> list = new ArrayList<SearchResult.Column>(result.getColumns().size());
		SearchResult.Column column = new SearchResult.Column();
		column.setNameEn(ResourceBundle.getBundle(ContextProvider.MESSAGES, ContextProvider.LOCALE_ENG).getString("search.column.id"));
		column.setNameRu(ResourceBundle.getBundle(ContextProvider.MESSAGES, ContextProvider.LOCALE_RUS).getString("search.column.id"));
		//column.setWidth(10);
		//column.setSorting(SearchResult.Column.SORT_ASCENDING);
		list.add(column);
		
		if(exportLocale==null){
			column = new SearchResult.Column();
			column.setNameEn("");
			column.setNameRu("");
			column.setWidth(4);
			list.add(column);
		}

		//list.addAll( result.getColumns());
		list.addAll(orginalColumns);
		return list;
	}
	
	/**
	 * ���������� ������ ������� �������������� �������, �� �������, ������� �������� ������������
	 * @return ������ �������
	 */
	public List<SearchResult.Column> getMetaDataNotReplaceColumn(){
		return getMetaDataNotReplaceColumn(null);
	}
	public List<SearchResult.Column> getMetaDataNotReplaceColumn(String exportLocale){
		List<Column> columns = getMetaData(exportLocale);
		Iterator<Column> iterator = columns.iterator();
		while(iterator.hasNext()){
			Column column = iterator.next();
			if(column.isReplaceAttribute()){
				iterator.remove();
			}
		}		
		return columns;
	}
	
	public List<ArrayList<Object>> getData(){
		return getData(null);
	}
	
	public List<ArrayList<Object>> getData(String exportLocale) {
		return getData(exportLocale, false);
	}
	
	public List<ArrayList<Object>> getData(String exportLocale, boolean exportToExcel) 
	{
		final Object[] columns = result.getColumns().toArray();
		final ArrayList<ArrayList<Object>> list = 
				new ArrayList<ArrayList<Object>>(result.getCards().size());

		for (ListIterator<?> itr =  result.getCards().listIterator(); itr.hasNext();) {
			final Card card = (Card) itr.next();
			if (card == null || !(card.getCanRead() || card.getCanWrite()) )
				// RuSA: ���������� ����� �� ������� ��� ���� �� ������ ��� ������ ...
				continue;
			final ArrayList<Object> cardAttrList = new ArrayList<Object>();
			// ������ ������� = id;
			cardAttrList.add(card.getId().getId());
			// ������ ������� = ������� ����������� �������������� 
			// ���� ������� � Excel �� �� �������
			if(!exportToExcel){
				cardAttrList.add(new Boolean(card.getCanWrite()));
			}
			final List<ColumnWrapper> cols = new ArrayList<ColumnWrapper>();
			
			// � ������� ������� ���������� �����������
			for (int i = 0; i < columns.length; i++) {

				/*final*/ SearchResult.Column originalColumn = (SearchResult.Column) columns[i];
				// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
				// (YNikitin, 2012/08/06) ��� ��� ������ ��������� ��������� �� ���������
				SearchResult.Column column = null;
				if (originalColumn.isReplaceAttribute()){	// �� ��������, �� ������� ���� �������� ������, � ������ ���� �� ������������ 
					continue;
				}
				
				column = SearchResult.getRealColumnForCardIfItReplaced(originalColumn, card, result.getColumns());	// �������� ������������ ������� �� ���������� (���� � ������ ��� ���������, �� ������������ ���� �������)
				// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
				Attribute attr = null;
				if (column.getGroupId() > 0
						&& column.getSecondaryColumns() == null
						&& column.getPrimaryColumn() == null) {
					
					int groupId = column.getGroupId();
					int count = i; 
					while (groupId == column.getGroupId() && count < columns.length ) {
						count++;
						if (count < columns.length) {
							column = (SearchResult.Column) columns[count];
						}
					}

					for (int j = i; j < count; j++) {
						SearchResult.Column groupColumn = (SearchResult.Column) columns[j];
						Attribute attribute = card.getAttributeById(groupColumn.getAttributeId());
						if (attribute != null && !attribute.isEmpty()) {
							attr = attribute;
							column = (SearchResult.Column) columns[j];
						}
					}
					i = count - 1;
				} else {
					attr = card.getAttributeById(column.getAttributeId());
				}

				//Attribute attr = SearchResult.smartFindAttr( card, column.getAttributeId());
				if ((attr == null && !column.isShowNullValue())|| (attr!=null&&attr.isHidden()) || column.isHidden())
					addAttrValue(column, attr, cols, null);
				// (YNikitin, 2011/11/16) ���� ������� �� �������� � ���� ���������������� null-��������
				else if (attr == null && column.isShowNullValue())
					addAttrValue(column, attr, cols, new String(column.getNullValue()));
				// ��������� ��������� �������� ��� ListAttribute ��� �������� � Excel
				else if (exportLocale!=null && exportToExcel && Attribute.TYPE_LIST.equals(attr.getType())){
					if (RUSSIAN.equals(exportLocale))
						addAttrValue(column, attr, cols, ((ListAttribute) attr).getValue().getValueRu());
					if (ENGLISH.equals(exportLocale))
						addAttrValue(column, attr, cols, ((ListAttribute) attr).getValue().getValueEn());
				}
				else if (column.isIcon()) {
					if (Attribute.TYPE_LIST.equals(attr.getType())) {
						addAttrValue(column, attr, cols, ((ListAttribute) attr).getValue().getId().getId());
					} else if (	Attribute.TYPE_CARD_LINK.equals(attr.getType()) ||
								Attribute.TYPE_TYPED_CARD_LINK.equals(attr.getType()) ||
								Attribute.TYPE_BACK_LINK.equals(attr.getType())) {

						if (((LinkAttribute) attr).getIdsLinked() != null) {
							Long countCards = checkLinkedCard((LinkAttribute)attr, column);
							//���� ��� ������� ��� ������, ��� ������ ������� 1 �� ������� ���������� ��������� (���������� ��������� ����������)
							if(exportLocale != null || (countCards != null && countCards > 1)) {
								addAttrValue(column, attr, cols, countCards);
							} else {
								Long linkValue = geLinkAttributeValueForIconColumn(column, card, (LinkAttribute) attr);
								addAttrValue(column, attr, cols, linkValue);
							}
						} else {
							addAttrValue(column, attr, cols, null);
						}

					} else if (	Attribute.TYPE_DATE.equals(attr.getType())) {
						if (attr.isEmpty()) {
							addAttrValue(column, attr, cols, null);
						} else {
							addAttrValue(column, attr, cols, 0L);
						}
					} else {
						addAttrValue(column, attr, cols, null);
					}
				}
				// (YNikitin, 2011/11/16) ���� ������� ������ � ���� ���������������� null-��������
				else if (column.isShowNullValue()&&(attr.getStringValue()==null||attr.getStringValue().length()==0)) {
					addAttrValue(column, attr, cols, new String(column.getNullValue()));
				} else if (Attribute.TYPE_INTEGER.equals(attr.getType())) {
					addAttrValue(column, attr, cols, new Integer(((IntegerAttribute) attr).getValue()));
				} else if (Attribute.TYPE_LONG.equals(attr.getType())) {
					addAttrValue(column, attr, cols, new Long(((LongAttribute) attr).getValue()));
				} else if (Attribute.TYPE_DATE.equals(attr.getType())) {
					addAttrValue(column, attr, cols, getDateAttributeValue(column, (DateAttribute)attr));
				} else if (Attribute.TYPE_CARD_LINK.equals(attr.getType()) 
						|| Attribute.TYPE_TYPED_CARD_LINK.equals(attr.getType()) 
						|| Attribute.TYPE_DATED_TYPED_CARD_LINK.equals(attr.getType())) {
					addAttrValue(column, attr, cols, getLinkAttributeValue(column, card, (CardLinkAttribute)attr));
				} else if (Attribute.TYPE_BACK_LINK.equals(attr.getType())) {
					addAttrValue(column, attr, cols, getLinkAttributeValue(column, card, (BackLinkAttribute)attr));
				} else if (Attribute.TYPE_PERSON.equals(attr.getType())) {
					addAttrValue(column, attr, cols, getPersonAttributeValue(column, (PersonAttribute)attr));
				} else {
					addAttrValue(column, attr, cols, attr.getStringValue());
				}
			}
			for(ColumnWrapper wr : cols) {
				cardAttrList.add(wr.getValue());
			}
			list.add(cardAttrList);
		}
		return list;
	}
	
	private Long geLinkAttributeValueForIconColumn(Column column, Card card, LinkAttribute attr) {
		//����� �������� id'����� icon'a (�������� 1432(��), 1433(���), � ��)
		String strValue = getLinkAttributeValue(column, card, attr);
		if (strValue != null && !strValue.trim().isEmpty()) {
			if (strValue.contains(cardLinkDelimiter)) {
				return (long) strValue.split(cardLinkDelimiter).length;
			} else {
				return Long.valueOf(strValue);
			}
		} else {
			return null;
		}
	}
	
	private Long checkLinkedCard(LinkAttribute attr, Column column) {
		List<ObjectId> ignoreStates = column.getLinkCardsStatesIgnore();
		List<ObjectId> linkedCardsIds = new ArrayList<ObjectId>();
		linkedCardsIds.addAll(attr.getIdsLinked());
		Long countCards = new Long(linkedCardsIds.size());
		if (!(ignoreStates == null || ignoreStates.isEmpty())) {		
				try {
					// �������������� ���� ��� �������� �������� �� ����������� � ������������ �������
					CheckIgnoreStates checkIgnoreState = new CheckIgnoreStates();
					checkIgnoreState.setLinkedCardsIds(linkedCardsIds);
					checkIgnoreState.setIgnoreStates(ignoreStates);
					countCards = (Long)serviceBean.doAction(checkIgnoreState);
				} catch (Exception e) {
					logger.error("Unable to check the linked cards.", e);
				}
		}
		// ���� ���������� �������� ���������� ������ 0, 
		// ����������� ���������� null ��� ���������� ��������� 
		// (�������� ��� ��� ������) � TableDecorator. 
		if (countCards == 0L) 
			countCards = null;
		return countCards;
	}
	
	/**
	 * Gets attribute value from CardLink and BackLink attributes
	 * @param column
	 * @param card
	 * @param attr
	 * @return
	 */
	private String getLinkAttributeValue(Column column, Card card, LinkAttribute attr) {
		final StringBuilder buf = new StringBuilder();
		if (column.getLabelAttrId()!=null){	// ���� ������� �������� ����������� � ���������� ������� LabelAttrId
			for (final Card curCard : result.getCardsListForLabelColumn(column)) {    // ��� ������� �������������� ������� � ����� labelColumnsForCards SearchResult-�
				if (curCard.getId().getId().equals(card.getId().getId())) {        // ������� �������������� �������� ����� ��������
					if (attr instanceof BackLinkAttribute) {
						attr = (BackLinkAttribute) curCard.getAttributeById(column.getAttributeId());
					} else if (attr instanceof CardLinkAttribute) {
						attr = (CardLinkAttribute) curCard.getAttributeById(column.getAttributeId());
					}
					break;
				}
			}
		}

		ObjectId[] array = null;
		if (attr instanceof BackLinkAttribute) {
			array = attr.getIdsArray();	// � ����� �� ��� ������
		} else if(attr instanceof CardLinkAttribute){
			array = attr.getIdsArray();
		}
		if (array != null && attr != null) {
			// (!) ��� ��������� ����� � ������ "A->B"
			// assignColumnTags(column, attr);

			// ��������� ������ ...
			for (int k = 0; k < array.length; k++) {
				String sValue = "";
				if (attr instanceof BackLinkAttribute) {
					sValue = attr.getLinkedCardLabelText(array[k], "");
				} else if(attr instanceof CardLinkAttribute){
					sValue = attr.getLinkedCardLabelText(array[k], "");
				}
				buf.append(sValue.trim());
				if (k < array.length - 1) {
					buf.append(cardLinkDelimiter);
				}
			}
		} else {
			buf.append("");
		}
		return buf.toString();
	}

	/**
	 * Gets attribute value from person attribute
	 * @param column
	 * @param attr
	 * @return
	 */
	private String getPersonAttributeValue(Column column, PersonAttribute attr) {
		Collection values = attr.getValues(); // ��� ������ ���������� Values
					final StringBuffer buf = new StringBuffer();
					if (values == null || values.isEmpty())
						buf.append("");
					else {
						for( Iterator itrV = values.iterator(); itrV.hasNext(); )
						{
							final Person item = (Person) itrV.next();
							String info;
							if (item == null) { 
								info = "null";
							} else {
								info = item.getFullName(); // (!) ��� ���������� ������
								if (info == null || info.trim().length() == 0)
									// ���� ����� - ���������� id...
									info = (item.getId() != null) 
											? MessageFormat.format( "id({0})", new Object[] {item.getId().getId()} )
											: "no id";
							}
							buf.append(info.trim());
							if (itrV.hasNext())
								buf.append(cardLinkDelimiter);
						}
					}
		return buf.toString();
				}
	
	/**
	 * Gets date attribute value
	 * @param column
	 * @param attr
	 * @return
	 */
	private String getDateAttributeValue(Column column, DateAttribute attr) {
		if (column.getTimePattern() != null) {
			return attr.getStringValue(column.getTimePattern());
		} else if(!column.isDefaultTimePatternUsed()) {
			return attr.getStringValue();
		} else {
			return attr.getStringValue(DateAttribute.defaultTimePattern);
			}
		}
	
	/**
	 * Adds attribute values to the list of ColumnWrapper objects
	 * If the column has a primary column and the primary column attribute value is empty
	 * 	then the value of the primary column will be replaced by the value of the secondary column
	 * @param column search column
	 * @param attr attribute
	 * @param cols list of ColumnWrapper objects
	 * @param value attribute value
	 */
	private void addAttrValue(Column column, Attribute attr, List<ColumnWrapper> cols, Object value) {
		Column primary = column.getPrimaryColumn();
		if(value != null && !value.equals("") && primary != null) { //if the current column has a primary one
			for(ColumnWrapper wr : cols) { //looking for it's primary column to replace a value
				if(wr.getColumn().getAttributeId().getId().equals(primary.getAttributeId().getId())
						&& ( wr.getValue() == null || "".equals(wr.getValue().toString())))
					wr.setValue(value);
	}
		} else cols.add(new ColumnWrapper(column, value));
	}

	private class ColumnWrapper {
		private Column column;
		private Object value;
		
		public ColumnWrapper(Column column, Object value) {
			this.column = column;
			this.value = value;
				}

		public Column getColumn() {
			return column;
			}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
			}

	}

	public int getDataOffset()
	{
		return 2;	// 1-�� ������� - ������������� ��������
					// 2-�� ������� - ������� ����������� ��������������
	}

	/**
	 * @param column
	 * @param attr
	 */
	private void assignColumnTags(final SearchResult.Column column, Attribute attr) {
		if (attr == null || column == null) return;
		column.setNameRu(attr.getNameRu());
		column.setNameEn(attr.getNameEn());
	}

	public ReferenceValue getTitle() {
		ReferenceValue title = new ReferenceValue();
		title.setValueEn(result.getNameEn());
		title.setValueRu(result.getNameRu());
		return title;
	}
	
	public SearchResult getResultObject() {
		return result;
	}

}