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
/**
 * 
 */
package com.aplana.dbmi.filestorage.query;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.aplana.dbmi.action.SearchInFiles;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.query.WriteQuery;
import com.aplana.dbmi.storage.search.FileItem;
import com.aplana.dbmi.storage.search.SearchService;

/**
 * @author RAbdullin
 * �������� getAction : SearchInFiles, ������������� ����� ������ getWords. 
 * ������ Collection<ObjectId>.
 */
public class doSearchInFiles  extends actionFileStorageUseBase implements WriteQuery {
	private static final long serialVersionUID = 1L;
	private final static int ROWCOUNTALL = 1000;
	private SearchInFiles searchData; // ����������� ������ processQuery()

	/**
	 * Identifier of 'Search file' action to be used in system log
	 */
	// public static final String EVENT_ID = "SEARCH_FILE";

	/**
	 * @return {@link #EVENT_ID}
	 */
	// @Override
	// public String getEvent() {
	// 	return EVENT_ID;
	// }

	public SearchInFiles getSearchData()
	{
		return (SearchInFiles) getAction(); 
	}
	
	/* (non-Javadoc)
	 * @see com.aplana.dbmi.service.impl.QueryBase#processQuery()
	 */
	@Override
	public Object processQuery() 
		throws DataException 
	{
		this.searchData = this.getSearchData();
		if (	searchData == null 
			|| 	searchData.getWords() == null
			|| 	searchData.getWords().trim().length() == 0)
			return null;
		
		try {
			// �������� �� ��������� ��������� ...
			// final FileStorageService fstorage = super.getFileStorageService();
			final SearchService srsrv = super.getSearchService();

			if(srsrv == null)
				throw new DataException("action.search.material.noindexer");

			// ����� �������...
			final String query = prepeareSolrQuery( searchData.getWords() );
			
			// ������...
			final List<FileItem> found = 
				srsrv.query(query, 0, ROWCOUNTALL);

			// ������������ ����������...
			if (found == null || found.isEmpty())
				// ������ �� �������
				return null;
			
			final ArrayList<ObjectId> result = new ArrayList<ObjectId>(found.size());
			for (FileItem item : found) {
				if (item == null) continue;
				if (item.getUrl() == null || item.getUrl().length() < 1) continue;
				
				try {
					// ���� ����������� � �����, ������� ��� ��� id ��������...
					result.add( new ObjectId( Card.class, Long.parseLong(item.getUrl()) ));
				} catch (NumberFormatException e) {
					// (?) ����� ����������� ������� ... 
					// result.add( new ObjectId( Attribute.class, item.getUrl() ));
					continue; // ���� ���������� �����
				}
			}
		
			return result;
		} 
		catch ( Exception e) {
			logger.error( MessageFormat.format(
					"Search files exception [words='{0}']", searchData.getWords()), 
					e);
			if (e instanceof DataException) 
				throw (DataException) e;
			throw new DataException("action.search.material", e);
		}
		
	}
	
	/**
	 * @param words
	 * @return
	 */
	private String prepeareSolrQuery(String words) {
		/*
		this.parseToPhrases();
		final String solrOper =
			(searchData.getWordsOperation().equals(SearchInFiles.OPER_AND)) 
				? " AND "
				: " || " 
			;
		return StrUtils.getAsString(this.phrases, solrOper );
		 */
		return words;

	}

}
