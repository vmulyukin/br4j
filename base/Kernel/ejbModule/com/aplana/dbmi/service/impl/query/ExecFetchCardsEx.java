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
package com.aplana.dbmi.service.impl.query;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.impl.UserData;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

/**
 * Extended version of {@link ExecFetchCards} class.
 * Automatically translates pseudo-attribute columns.
 * 
 * @author apirozhkov
 */
public class ExecFetchCardsEx extends ExecFetchCards {
	private static final ResourceBundle namesRu =
		ResourceBundle.getBundle(ContextProvider.MESSAGES, ContextProvider.LOCALE_RUS);
	private static final ResourceBundle namesEn =
		ResourceBundle.getBundle(ContextProvider.MESSAGES, ContextProvider.LOCALE_ENG);
	private static final Object[][] PSEUDO_ATTRIBUTES = {
			{ ExecFetchCards.ATTR_TEMPLATE, 20, null,
					namesRu.getString("search.column.template"),
					namesEn.getString("search.column.template"),
					AttributeTypes.LIST,
					SearchResult.Column.SORT_NONE },
			{ ExecFetchCards.ATTR_STATE, 15, null,
					namesRu.getString("search.column.state"),
					namesEn.getString("search.column.state"),
					AttributeTypes.LIST,
					SearchResult.Column.SORT_NONE },
			{ ExecFetchCards.APPROVAL_STATE, 15, null,
					null,
					null,
					AttributeTypes.LIST,
					SearchResult.Column.SORT_NONE },
			{ ExecFetchCards.ATTR_UNIVERSAL_TERM, 15, null,
					namesRu.getString("search.column.term"),
					namesEn.getString("search.column.term"),
					AttributeTypes.DATE,
					SearchResult.Column.SORT_NONE },
			{ ExecFetchCards.ATTR_DIGITAL_SIGNATURE, 15, null,
					namesRu.getString("search.column.digitalsignature"),
					namesEn.getString("search.column.digitalsignature"),
					AttributeTypes.LIST,
					SearchResult.Column.SORT_NONE },
	        { ExecFetchCards.ATTR_ALL_DOCLINKS, 15, null,
					namesRu.getString("search.column.alldocslinks"),
					namesEn.getString("search.column.alldocslinks"),
					AttributeTypes.LIST,
					SearchResult.Column.SORT_NONE },
	        { ExecFetchCards.ATTR_PRELIMINARY_TERM, 15, null,
					namesRu.getString("search.column.preliminaryTerm"),
					namesEn.getString("search.column.preliminaryTerm"),
					AttributeTypes.LIST,
					SearchResult.Column.SORT_NONE }
	};
	
	/**
	 * ������� ������ ������ ������ PSEUDO_ATTRIBUTES
	 */
	final static int IDX_CODE = 0;
	final static int IDX_WIDTH = 1;
	// final static int IDX_ORDER = 2; � �������� ��� ��������� ������ �� �����, ����� ���� ��������
	final static int IDX_NAMERU = 3;
	final static int IDX_NAMEEN = 4;
	final static int IDX_TYPE = 5;
	final static int IDX_SORT = 6;

	public ExecFetchCardsEx(JdbcTemplate jdbc, UserData user, Integer session) {
		super(jdbc, user, session);
	}

	public ExecFetchCardsEx(JdbcTemplate jdbc, UserData user, Search search, Integer session) {
		super(jdbc, user, search, session);
	}

	/***
	 * ����� ������� ��-�� ���������� Override.
	 * ��������, ��� ����� ��� �� �������:
	 * 		fetcher.setResultColumns( search.getColumns(), true);
	 * � ���� ������:
	 * 		fetcher.setResultColumns( (List) search.getColumns(), true); 
	 */
	@Override
	public void setResultColumns(Collection<SearchResult.Column> value, boolean reloadMetaData) {
		super.setResultColumns(convertResultColumns(value), reloadMetaData);
	}

	private List<SearchResult.Column> convertResultColumns(final Collection<SearchResult.Column> columns) {
		if (columns == null)
			return null;

		// ������ �������� ���������, � ������� ��� ����� �� �����
		// ���������������
		final List<SearchResult.Column> realAttributes = new ArrayList<SearchResult.Column>(columns.size());

		// NOTE: ������� ORDER ����� ��� �� �����������, �.�. �� �������� ������� �� ������.
		for (SearchResult.Column column : columns) {
			final int idx = findPseudoAttribute(column.getAttributeId());
			if (idx >= 0) { // pseudo attribute -> real attribute
				realAttributes.add(createColumnByPseudoAttr(PSEUDO_ATTRIBUTES[idx], column));
			} else { // real attribute itself
				realAttributes.add(column);
			}
		} // while
		return realAttributes;
	}

	/**
	 * ������� �������� ������� �� ��������-��������� �������������. 
	 * @param pseudoAttributes ������ ������� �������� ��� ������ ���������
	 * @param realColumn: �������� �������� ������-������� (�� search). 
	 * ���� ������������ ������ ��� ��������� ������. ����� ���� null.
	 * @return �������� ������������ �������.
	 */
	static SearchResult.Column createColumnByPseudoAttr(
			final Object[] pseudoAttributes,
			final SearchResult.Column realColumn
			) 
	{
		final SearchResult.Column result = new SearchResult.Column();

		result.setAttributeId(new ObjectId(AttributeTypes
				.getAttributeClass((String) pseudoAttributes[IDX_TYPE]),
				pseudoAttributes[IDX_CODE]));

		// ������� ������ �������...
		if (realColumn.getWidth() > 0)
			result.setWidth(realColumn.getWidth()); // (2010/01, RuSA)
		else if (pseudoAttributes[IDX_WIDTH] != null) {
			int width = (Integer) pseudoAttributes[IDX_WIDTH];
			if (width > 0)
				result.setWidth(width);
		}

		result.setNameRu((String) pseudoAttributes[IDX_NAMERU]);
		result.setNameEn((String) pseudoAttributes[IDX_NAMEEN]);

		if (pseudoAttributes[IDX_SORT] != null) {
			int sort = (Integer) pseudoAttributes[IDX_SORT];
			result.setSorting(sort);
		}

		// ������-������� ����� ���� ������� � ��������
		result.setLinked(realColumn.isLinked());

		// ������-������� ���� ����� ���� ����������
		result.setFullReplaceAttrId(realColumn.getFullReplaceAttrId());
		result.setReplaceStatusId(realColumn.getReplaceStatusId());
		result.setEmptyReplace(realColumn.isEmptyReplace());
		// newCol.setLabelAttrId(null); �� ������������ � ��������������
		result.setIcons(realColumn.getIcons());
		result.setDefaultIcon(realColumn.getDefaultIcon());
		result.setEmptyIcon(realColumn.getEmptyIcon());
		result.setAction(realColumn.getAction());

		return result;
	}

	static public int findPseudoAttribute(ObjectId id) {
		if (id != null && id.getId() != null) {
			for (int idxResult = 0; idxResult < PSEUDO_ATTRIBUTES.length; idxResult++) {
				if (PSEUDO_ATTRIBUTES[idxResult][0].equals(id.getId())) {
					// found
					return idxResult;
				}
			}
		}
		// (!) not found
		return -1;
	}
}
