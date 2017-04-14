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
package com.aplana.dbmi.storage.search;

import java.io.IOException;
import java.util.List;

// import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.util.ContentStream;

import com.aplana.dbmi.storage.search.FileItem;

/**
 * TODO Javadoc
 *
 * @author Ogalkin, RAbdullin
 *
 */
public interface SearchService {
	
	/**
	 * ���������������� ����� ��� ����� ��� ��������� id ��� �������� ������������.
	 * @param id   ���������� ��������� id ��� ����������� ������,
	 * ���� ����� id ��� ����������, �� ������ ������ �����������;	
	 * @param filename   (����) �������� ��� �����;
	 * @param stream   ������ �����;
	 * @throws SearchException
	 * @throws IOException
	 */
	public void index(String id, String filename, ContentStream stream)
			throws SearchException, IOException;
	
	/**
	 * ��������� ����� ������ �� ���������� �������.
	 * @param query   ����� � �������� (������� �� ���������� interface SearchService, 
	 * ��� Solr ����� ������ �������� Lucene/Solr);
	 * @param startRow   ������ ������� ���-�� ����� � ��������� ������;
	 * @param rowCount   ���-�� ���-�� ������. 
	 * @return   ������ ��������� ������ � ��������� [startRow, startRow+rowCount-1].
	 * @throws SearchException
	 */
	public List<FileItem> query(String query, Integer startRow, Integer rowCount)
			throws SearchException;
	
	/**
	 * ������� ��������� ������ � ��������� id.
	 * @param id
	 * @throws SearchException
	 */
	public void delete( String id)
			throws SearchException;
	
	//  <commit waitFlush="true" waitSearcher="true">
	
	/**
	 * �������������� ������.
	 * @param waitFlush: true, ����� ����� ������ �� �����, null - ������������
	 * 		��������� �� ��������� (������. true). 
	 * @param waitSearcher� true, ����� ����� ���������� ����������, 
	 * 		null - ������������ ��������� �� ��������� (������. true).
	 * @throws SearchException
	 */
	public void optimize( Boolean waitFlush, Boolean waitSearcher)
		throws SearchException;
	
}
