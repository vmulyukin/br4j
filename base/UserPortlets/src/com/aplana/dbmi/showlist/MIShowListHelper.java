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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.apache.commons.io.IOUtils;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.SearchResult.Column;
import com.aplana.dbmi.column.ColumnViewer;
import com.aplana.dbmi.column.ColumnViewerFactory;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class MIShowListHelper {

	private MIShowListHelper(){
	}

	/**
	 * ��� ���� ������� � ������� ���� ������������� Viewer, ������� �������������.
	 * @param request
	 * @param searchResult - �������� � ���� ���������� ���������� ���������� �������.
	 * @param serviceBean
	 * @return
	 * @throws DataException
	 * @throws ServiceException
	 */
	@SuppressWarnings("unchecked")

	public static RowExData loadCardsFromAttribute(PortletRequest request, SearchResult searchResult, DataServiceBean serviceBean) throws DataException, ServiceException{		
		List<Card> cards = searchResult.getCards();
		RowExData rowExData = new RowExData();
		ExDataCell exDataCell=null;
		for (Card card : cards) {
				exDataCell = loadCardsCorrespondingAttribute(request, card, searchResult, serviceBean);
				rowExData.addExDataCell((Long)card.getId().getId(), exDataCell);				
		}	
		return rowExData;
	}

	/**
	 * �������� �������������� ������ ��� ��������� ��������
	 * @param request
	 * @param card - �������� ��� ������� ���������� ��������� �������������� ������
	 * @param columns - ������ ������� ��� ������� ����� ��������� ������
	 * @param serviceBean
	 * @return exDataCell �������� ������� ������������� ��� ������ � �������� �������.
	 * @throws DataException
	 * @throws ServiceException
	 */
	private static ExDataCell loadCardsCorrespondingAttribute(PortletRequest request, Card card, SearchResult searchResult, DataServiceBean serviceBean) throws DataException, ServiceException{
		Attribute attribute = null;
		ColumnViewer columnViewer = null;
		ExDataCell exDataCell = null;
		List<SearchResult.Column> columns = getColumnsThatSearch((List<Column>) searchResult.getColumns());

		if (columns != null && !columns.isEmpty()) {
			exDataCell = new ExDataCell(); 
			SearchResult.Column commonColumn = columns.get(0);
			for (Column column : columns) {

				attribute = card.getAttributeById(column.getAttributeId());
				if(attribute==null){
					continue;
				}
				String searchWords = getLinkAttributeValue(searchResult, column, card, attribute);
				columnViewer=(ColumnViewer)column.getParamByName(Column.COLUNM_VIEWER);
				columnViewer.initViewer(request, searchWords);
				exDataCell.addCellData((String) commonColumn.getAttributeId().getId(), columnViewer.getHtmlCode());
			}
		}
		return exDataCell;
	}

	private static String getLinkAttributeValue(SearchResult result, Column column, Card card, Attribute attr) {
		final StringBuffer buf = new StringBuffer();
		if (column.getLabelAttrId() !=null && !column.getLabelAttrId().equals(attr.getId())) {// ���� ������� �������� ����������� � ���������� ������� LabelAttrId  
			for (ListIterator<?> initr =  result.getCardsListForLabelColumn(column).listIterator(); initr.hasNext();) { // ��� ������� �������������� ������� � ����� labelColumnsForCards SearchResult-� 
				final Card curCard = (Card)initr.next(); 
				if (curCard.getId().getId().equals(card.getId().getId())){ // ������� �������������� �������� ����� ��������
					if(attr instanceof BackLinkAttribute) {
						attr = (BackLinkAttribute)curCard.getAttributeById(column.getAttributeId());
					} else if(attr instanceof CardLinkAttribute) {
						attr = (CardLinkAttribute)curCard.getAttributeById(column.getAttributeId());
					}
					break;
				}
			}
		}

		ObjectId[] array = null;
		if (attr instanceof BackLinkAttribute) {
			array = ((BackLinkAttribute)attr).getIdsArray();	// � ����� �� ��� ������ 
		} else if(attr instanceof CardLinkAttribute){
			array = ((CardLinkAttribute)attr).getIdsArray();
		}
		if (array != null && attr != null) {
			// (!) ��� ��������� ����� � ������ "A->B"
			// assignColumnTags(column, attr);

			// ��������� ������ ...  
			for (int k = 0; k < array.length; k++) {
				String sValue = "";
				if (attr instanceof BackLinkAttribute) {
					sValue = ((BackLinkAttribute)attr).getLinkedCardLabelText(array[k], "");
				} else if(attr instanceof CardLinkAttribute){
					sValue = ((CardLinkAttribute)attr).getLinkedCardLabelText(array[k], "");
				}
				buf.append(sValue.trim()); 
				if (k < array.length - 1) {
					buf.append(", ");
				}
			}
		} else
			buf.append("");
		return buf.toString();
	}

	/**
	 * ���������� ������ columns � ������� ����, Viewer
	 * @param collection ������ �������
	 * @return
	 */
	public static List<SearchResult.Column> getColumnsThatSearch(List<Column> collection){
		List<SearchResult.Column> cols = new ArrayList<SearchResult.Column>();
		for(SearchResult.Column column : collection){
			if(column.getParamByName(Column.COLUNM_VIEWER)==null){
				continue;
			}
			cols.add(column);
		}
		return cols;
	}

	/**
	 * ���������� ������ ���������� xml ����� � ���� ��������� Viwer ���� ��� ����.
	 * @param pathFile
	 * @return
	 * @throws IOException
	 * @throws DataException
	 */
	public static Search createSearchFromFullParseXml(String pathFile) throws IOException, DataException{
		Search search = createSearchFromParseXml(pathFile);
		parseColumnsXml(search);
		return search;
	}

	/**
	 * ���������� ������ ���������� xml �����. ��� ������� Viwer � columns
	 * @param pathFile
	 * @return
	 * @throws IOException
	 * @throws DataException
	 */
	public static Search createSearchFromParseXml(String pathFile) throws IOException, DataException{
		Search search = new Search();
		final InputStream inputStream = Portal.getFactory().getConfigService().loadConfigFile(pathFile);
		try{
			search.initFromXml(inputStream);
		}finally{
			IOUtils.closeQuietly(inputStream);
		}
		return search;
	}

	/**
	 * ���������� ������ xml ������ Viewer. � ������� ����������� ���������� ColumnViewer
	 * @param searchAction
	 * @throws IOException
	 * @throws DataException
	 */
	public static void parseColumnsXml(Search searchAction) throws IOException, DataException{
		String viewerFileName = null;
		for (Column column : searchAction.getColumns()) {
			viewerFileName = (String) column.getParamByName(Column.COLUNM_FILE_VIEWER);
			if(viewerFileName==null){
				continue;
			}
			ColumnViewer columnViewer = ColumnViewerFactory.getFactory().getViewer(column);
			column.addParam(Column.COLUNM_VIEWER, columnViewer);			
		}
	}

}
