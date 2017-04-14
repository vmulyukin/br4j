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
package com.aplana.dbmi.service.impl.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.aplana.dbmi.action.SearchResult;


/**
 * @author RAbdullin
 * ������ ����������� (�� ��) �������������� ���������-������� � ���������� ������.
 * �������������� ����������� ����� �� �����-���� �������� (attribute_code).
 */
public class ColumnDefSet extends HashSet {

	private static final long serialVersionUID = 1L;
	
	/**
	 * �������������� �� �����.
	 */
	final private Map indexByName = new HashMap(); // <String, SearchResult.Column>
	
	public static ColumnDefSet createByCollection(Collection /*<Column>*/ collection)
	{
		if (collection == null) return null;
		final ColumnDefSet result = new ColumnDefSet();
		for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
			result.add( (SearchResult.Column) iterator.next() );
		}
		return result;
	}
	
	static String getKeyStr( SearchResult.Column item)
	{
		return (	item == null 
				|| (item.getAttributeId() == null)
				|| (item.getAttributeId().getId() == null)
			)
			? "" 
			: item.getAttributeId().getId().toString();
	}
	
	public boolean add(SearchResult.Column columnDef)
	{
		final String key = getKeyStr(columnDef);
		if (key == "") return false;
		indexByName.put( key, columnDef);
		return super.add(columnDef);
	}

	public boolean remove(SearchResult.Column columnDef)
	{
		final String key = getKeyStr(columnDef);
		if (key == "") return false;
		indexByName.remove( key);
		return super.remove( columnDef);
	}
	
	public SearchResult.Column[] toArrayIds()
	{
		return (SearchResult.Column[]) super.toArray( new SearchResult.Column[0]);
	}
	
	public SearchResult.Column getByCodeName( String attrCodeName)
	{
		return (SearchResult.Column) indexByName.get(attrCodeName);
	}
	
}
