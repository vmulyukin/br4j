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
package com.aplana.dbmi.action;

/**
 * {@link Action} implementation used to perform search for {@link Card} cards
 * with delegating access checking.
 * This action returns {@link SearchResult} object as a result
 * <br>
 * TODO ������ ��� ������ �� ������������ ������������ �����
 * ����� ���������� ������� ������ ����� ���� ����������� ������ �� ��������� 
 * � ������� ������� �� ���� �������������� �� ������������ ������.
 * ��.  {@link DoSearchWithDelegatingAccess}
 */
public class SearchWithDelegatingAccess implements Action<SearchResult>
{
	private static final long serialVersionUID = 12L;
	private Search search;
	
	public SearchWithDelegatingAccess (Search search) {
		this.search = search;
	}

	public Search getSearch() {
		return search;
	}

	/**
	 * @see Action#getResultType()
	 */
	public Class<?> getResultType() {
		return SearchResult.class;
	}
}
