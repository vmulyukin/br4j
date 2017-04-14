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
import com.aplana.dbmi.action.SearchResult.Column;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.utils.SimpleDBUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ExecFetchCardsFromIdsArray extends ExecFetchCardsEx {
	private List<ObjectId> m_objectIds;

	public ExecFetchCardsFromIdsArray(JdbcTemplate jdbc, UserData auser, Integer session) {
		super(jdbc, auser, session);
	}

	public ExecFetchCardsFromIdsArray(JdbcTemplate jdbc, UserData auser, Search search, Integer session) {
		super(jdbc, auser, search, session);
	}

	public void setObjectIds(List objectIds) {
		m_objectIds = objectIds;
		m_objectIdsAsList = null;
	}

	@Override
	public SearchResult execute() {
		/* �������� �������� ��� ���������� ... */
		final SearchResult result = new SearchResult();

		/*if (m_objectIds.size() > 0){*/

			// �������� ���� ������� �� ��������
			final Map<Long, Card> cardACLMap = loadCardsPermissions( user.getPerson().getId());

			//���������� �� �������� SearchResult.Column
			boolean sortingByColumns = isSortingByColumns(search);
			   		
			/* ������� �� �������... */
			loadResultAttributes(sortingByColumns);

			result.setColumns( this.resultColumns );

			final List<Card> foundCards = buildCards( cardACLMap, result.getColumns(), this.resultAttributes);

			addSignatureAttrIfnotExists(foundCards);
			
			if(!sortingByColumns) {
				//���������� �������� �������� ������ ���������������
				final List<Card> sortedCards = sortCards(foundCards);
				result.setCards( sortedCards);
				
			} else result.setCards( foundCards);

			logger.debug("FetchCards.Attributes.Options cache reads:"+ miss+" hits:"+hits);
		/*}else{
			result.setColumns(new ArrayList());
			result.setCards(new ArrayList());
		}*/
		return result;
	}
	
	/**
	 * adds signature attribute if it exists in SearchResult columns list but wasn't found in a card
	 * @param cards
	 */
	private void addSignatureAttrIfnotExists(List<Card> cards) {
		final ObjectId attrId = ObjectId.predefined(HtmlAttribute.class, "jbr.uzdo.signature");
		final HtmlAttribute attr = new HtmlAttribute();
		attr.setId(attrId);
		
		for (Column c : resultColumns) {
			if (attrId.equals(c.getAttributeId())) {
				for (Card card : cards) {
					Attribute a = card.getAttributeById(attrId);
					if (a == null) {
						card.getAttributes().add(attr);
					}
				}
				break;
			}
		}
	}
	
	/**
	 * �������� ���������� ���������� �� ��������
	 * @param search
	 * @return
	 */
	private boolean isSortingByColumns(Search search) {
		if (search == null
				|| search.getFilter() == null
			    || search.getFilter().getOrderedColumns() == null)
			return false;
		
		for (Object o : search.getFilter().getOrderedColumns()) {
			if (o != null)
				return false;
		}
		
		if (search.getColumns() != null && !search.getColumns().isEmpty()) {
			for (Column c : search.getColumns()) {
				if (c.getSorting() != Column.SORT_NONE)
					return true;
			}
		}
		return false;
	}

	/**
	 * ���������� �������� � ��� �� ������� ��� � � ���������� ������� ���������������
	 * @param foundCards
	 * @return
	 */
	private List<Card> sortCards(List<Card> foundCards) {
		List<Card> result = new ArrayList<Card>();
		for (ObjectId objectId : m_objectIds) {
			for (Card card : foundCards) {
				if (objectId.equals(card.getId())) {
					result.add(card);
					break;
				}
			}
		}
		return result;
	}

	protected void loadResultAttributes(boolean sortingByColumns) {
		// ������ ��������, �� ������� ������ ���� ����������� ���������� ������ ��������
		final List<Column> sortingColumns = new ArrayList<Column>();
		
		if (sortingByColumns) {
			if (resultColumns != null) {
				for (Column column : resultColumns) {
					if (column != null && column.getSorting() > 0)
						sortingColumns.add(column);
				}
			}
			sortingByColumns = !sortingColumns.isEmpty();
			if(!sortingColumns.isEmpty()) {
				Collections.sort(sortingColumns, new Comparator<Column>() {
					@Override
					public int compare(Column col1, Column col2) {
						return col1.getSortOrder() - col2.getSortOrder();
					}
				});
			}
		}
		
		/*if (m_objectIds.size() > 0){*/
			int predefined_columns
				// = SimpleDBUtils.sqlGetTableRowsCount( getJdbcTemplate(), "gtemp_attr_fetch");
				= (this.resultColumns != null) ? this.resultColumns.size() : 0;

			// final boolean addedDefaultColumns = predefined_columns == 0 ? true : false;

			if (predefined_columns == 0) {

				// int template_count = SimpleDBUtils.sqlGetTableRowsCount( getJdbcTemplate(), "gtemp_template_list");
				long templateId = -1; // (-1) = multi templates

				try {
					templateId = getJdbcTemplate().queryForLong(
								"select distinct c.template_id \n" +
								"from card c \n" +
								"where c.card_id in (" + getCardIdList() + ") \n"
							);
				} catch (Exception e) {
					// ���� �� �������, ���� � sql-������� ����� ����� ������ ��������
					// template_count > 1;
					templateId = -1;
				}

				if (templateId > 0) { // ��������� �������� �� ������������� ������� ...
					this.resultColumns = getJdbcTemplate().query(
							"SELECT t.attribute_code, t.column_width, t.order_in_list, \n"
							+ "\t\t  a.attr_name_rus, a.attr_name_eng, a.data_type, \n"
							+ "\t\t  null as sorting, null as linked \n"

							// >>> (2010/01, RuSA) ������� �������� ����������.
							+ "\t FROM template_attribute t \n"
							+ "\t INNER JOIN attribute a ON a.attribute_code=t.attribute_code \n"
							+ "WHERE t.template_id = " + String.valueOf(templateId) + "\n"
							+ "\t	and t.order_in_list is not null \n"
							+ "ORDER BY t.order_in_list\n",
							new InternalColumnFetchMapper()
							// <<< (2010/01, RuSA)
						);
					predefined_columns = (this.resultColumns != null) ? this.resultColumns.size() : 0;
				}

	/*			if (predefined_columns > 0) {
					if (templateId == 224 || templateId == 364 || templateId == 784 || templateId == 764 || templateId == 864 || templateId == 865) {
						List defColumns = initDefColumns(templateId);
						this.resultColumns.addAll(0, defColumns);
						predefined_columns = this.resultColumns.size();
					}
				}
	*/		}

			if (predefined_columns == 0) { // ��������� ����������� ����� �������
				// (2009/12/12, RuSA) �������� ������� ����� Insert-�������� �����...
				this.resultColumns = getJdbcTemplate().query(
						"SELECT * FROM (\n" +
						"  SELECT \n" +
						"    attribute_code, \n" +
						"    column_width,\n" +
						"    (CASE \n" +
						"        WHEN attribute_code = 'NAME'    then 1\n" +
						"        WHEN attribute_code = 'DESCR'   then 2\n" +
						"        WHEN attribute_code = 'REGION'  then 3\n" +
						"        WHEN attribute_code = 'AUTHOR'  then 4\n" +
						"        WHEN attribute_code = 'CHANGED' then 5\n" +
						"        ELSE 6 \n" + // for safety
						"     END \n" +
						"    ) as order_in_list,\n" +

						"    attr_name_rus,\n" +
						"    attr_name_eng,\n" +
						"    data_type,\n" +

						"    cast(null as numeric) as sorting,\n" +
						"    (CASE \n" +
						"        WHEN attribute_code = 'NAME' then 1 \n" +
						"        ELSE cast(null as numeric) \n" +
						"     END \n" +
						"    ) as linked\n" +
						"  FROM attribute\n" +

						"  WHERE attribute_code in ( 'NAME', 'DESCR', 'REGION', 'AUTHOR', 'CHANGED')\n" +
						"\n" +
						"  UNION SELECT \n" +

						"     '_TEMPLATE',\n" +
						"     20, \n" +
						"     6, \n" +

						//    '������', \n" + // ResourceBundle.getBundle(ContextProvider.MESSAGES, ContextProvider.LOCALE_RUS).getString("search.common.template")
						"     '" + ResourceBundle.getBundle(ContextProvider.MESSAGES, ContextProvider.LOCALE_RUS).getString("search.common.template") + "',\n" +
						"     'Template', \n" + // ResourceBundle.getBundle(ContextProvider.MESSAGES, ContextProvider.LOCALE_ENG).getString("search.common.template")
						"     'L', \n" +

						"     cast(null as numeric) as sorting, \n" +
						"     cast(null as numeric) as linked \n" +
						") AS Tt ORDER BY 3 \n"
						, new InternalColumnFetchMapper()
					);
				predefined_columns = (this.resultColumns != null) ? this.resultColumns.size() : 0;
			}

			final String sAttrCodesList = makeAttrCodesSqlList( this.resultColumns);

			/***********************************************************************
			 * ������������ ������ ������� �� �������� ���������...
			 */
			final StringBuffer lv_sql
				= new StringBuffer();

			lv_sql.append(
				"SELECT \n"
				+ "\t  a.attr_value_id, \n"		// 1
				+ "\t  a.card_id, \n"			// 2
				+ "\t  a.attribute_code, \n"	// 3

				+ "\t  a.number_value, \n"		// 4
				+ "\t  a.string_value, \n"		// 5
				+ "\t  a.date_value,  \n"		// 6

				+ "\t  a.value_id, \n"			// 7
				+ "\t  a.another_value, \n"		// 8
				+ "\t  v.value_rus, \n"			// 9

				+ "\t  v.value_eng, \n" 		// 10
				+ "\t  p.full_name, \n"			// 11
				+ "\t  0 as ord, \n"	// 12

				+ "\t  attr.data_type, \n"		// 13
				+ "\t  a.long_binary_value, \n"	// 14
				+ "\t  p.card_id \n"			// 15
				);
			
			if(sortingByColumns) {
				for(int i = 0 ; i < sortingColumns.size(); i++) {
					SearchResult.Column column = (SearchResult.Column) sortingColumns.get(i);
					lv_sql.append(
							getTableColumnByAttrType(column, i)
							);
				}
			}
			
			lv_sql.append(
				"\n FROM attribute_value a  \n"
				+ "		INNER JOIN attribute attr on attr.attribute_code=a.attribute_code \n"
				+ "		LEFT OUTER JOIN values_list v on a.value_id=v.value_id \n"
				+ "		LEFT OUTER JOIN person p on a.number_value=p.person_id \n"
				+ "WHERE \n"
				+ "		a.attribute_code in ("
					+ sAttrCodesList
					+ ((sAttrCodesList != null && !sAttrCodesList.equals("")) ? "," : "")
					+"'CREATED') \n"
				+ "	AND a.card_id in (" + getCardIdList() + ") \n"
			);
			
			// ���� ������������� "������" ...
			int need_attribute = countAttributes( this.resultColumns, ATTR_TEMPLATE); // "_TEMPLATE"
			if (need_attribute > 0)
			{
				lv_sql.append(
					"\n UNION ALL SELECT \n"
					+ "\t  null as attr_value_id, \n"						// 1
					+ "\t  c.card_id, \n"									// 2
					+ "\t '_TEMPLATE' as attribute_code, \n"				// 3

					+ "\t  null as number_value, \n" 						// 4
					+ "\t  null as string_value, \n"						// 5
					+ "\t null as date_value, \n"							// 6

					+ "\t  t.template_id as value_id, \n"					// 7
					+ "\t  null as another_value, \n"						// 8
					+ "\t  t.template_name_rus as value_rus, \n"			// 9

					+ "\t  t.template_name_eng as value_eng, \n"			// 10
					+ "\t  null as full_name, \n"							// 11
					+ "\t  0 as ord, \n"							// 12

					+ "\t \'"+ Attribute.TYPE_LIST + "\', \n"				// 13
					+ "\t null as long_binary_value, \n"					// 14
					+ "\t null as card_id \n"								// 15
					);
				if(sortingByColumns) {	
					addNullSortingColumns(sortingColumns, lv_sql);
				}

				lv_sql.append("\n FROM \n"
					+ "\t  card c \n"
					+ "\t  inner join \n"
					+ "\t  template t on c.template_id=t.template_id \n"
					+ "where c.card_id in (" + getCardIdList() + ") \n"

				);
			}

			// ���� ������������� "���������" ...
			need_attribute = countAttributes( this.resultColumns, ATTR_STATE); // "_STATE"
			if (need_attribute > 0) {
				lv_sql.append(
					"\n UNION ALL SELECT \n"

					+ "\t   null as attr_value_id, \n"				// 1
					+ "\t   c.card_id, \n"							// 2
					+ "\t   '_STATE' as attribute_code, \n"			// 3

					+ "\t   null as number_value, \n"				// 4
					+ "\t   null as string_value, \n"				// 5
					+ "\t   null as date_value, \n"					// 6

					+ "\t   c.status_id as value_id, \n"			// 7
					+ "\t   null as another_value, \n"				// 8
					// '||-- null as long_binary_value,
					+ "\t   s.name_rus as value_rus, \n"			// 9

					+ "\t   s.name_eng as value_eng, \n"			// 10
					+ "\t   null as full_name, \n"					// 11
					+ "\t   0 as ord, \n"				// 12

					+ "\t \'"+ Attribute.TYPE_LIST + "\', \n"		// 13
					+ "\t null as long_binary_value, \n"			// 14
					+ "\t null as card_id \n"						// 15
					);
				
				if(sortingByColumns) {	
					addNullSortingColumns(sortingColumns, lv_sql);
				}
					
					lv_sql.append(
					"\n FROM \n"
					+ "\t   card c inner join card_status s on c.status_id=s.status_id \n"
					+ "where c.card_id in (" + getCardIdList() + ") \n"

				);
			}

			// ���� ������������� "��������" ...
			need_attribute = countAttributesByType( this.resultColumns, "M" );
			if (need_attribute > 0) {
				lv_sql.append(
					"\n UNION SELECT \n"

					+ "\t  null as attr_value_id, \n"			// 1
					+ "\t  c.card_id, \n"						// 2
					+ "\t  'MATERIAL' as attribute_code, \n"	// 3

					+ "\t  null as number_value,  \n"			// 4
					+ "\t  coalesce(c.file_name, c.external_path) as string_value,  \n"	// 5
					+ "\t  null as date_value, \n"				// 6
					+ "\t  CASE \n"
					+ "\t\t  WHEN c.file_name IS NOT NULL THEN " + MaterialAttribute.MATERIAL_FILE + "\n"
					+ "\t\t  WHEN c.external_path IS NOT NULL THEN " + MaterialAttribute.MATERIAL_URL + "\n"
					+ "\t  END as value_id, \n"				// 7
					+ "\t  null as another_value, \n"			// 8
					// '||-- c.file_storage as long_binary_value,
					+ "\t  null as value_rus, \n"				// 9

					+ "\t  null as value_eng, \n"				// 10
					+ "\t  null as full_name, \n"				// 11
					+ "\t  0 as ord, \n"				// 12

					+ "\t \'"+ Attribute.TYPE_MATERIAL + "\', \n"	// 13
					+ "\t null as long_binary_value, \n"			// 14
					+ "\t null as card_id \n"						// 15
					);
				
				if(sortingByColumns) {	
					addNullSortingColumns(sortingColumns, lv_sql);
				}

				lv_sql.append("\n FROM card c \n"
					+ "where c.card_id in (" + getCardIdList() + ") \n"

				);
			}
			// ���� ������������� "back-link" ...
			need_attribute = countAttributesByType( this.resultColumns, "B");
			if (need_attribute > 0) {
				lv_sql.append(
					"\n UNION SELECT \n"

					+ "\t  null as attr_value_id, \n"					// 1
					+ "\t  c.card_id, \n"								// 2
					+ "\t  a.attribute_code, \n"						// 3

					// ��������, �� ������� ���� ������ �� ������ ����� cardlink, ����-��� backlink'�...
					//+ "\t  avLinkFrom.card_id as number_value, \n"		// 4
					+ "\t  av.card_id as number_value, \n" //4
					+ "\t  null as string_value,  \n"					// 5
					+ "\t  null as date_value, \n"						// 6

					+ "\t  null as value_id, \n"						// 7
					+ "\t  null as another_value, \n" 					// 8
					+ "\t  null as value_rus, \n"						// 9

					+ "\t  null as value_eng, \n"						// 10
					+ "\t  null as full_name, \n"						// 11
					+ "\t  0 as ord, \n"						// 12

					+ "\t \'"+ Attribute.TYPE_BACK_LINK + "\', \n"		// 13
					+ "\t null as long_binary_value, \n"				// 14
					+ "\t null as card_id \n"							// 15
					);
				
				if(sortingByColumns) {	
					addNullSortingColumns(sortingColumns, lv_sql);
				}

				lv_sql.append("\n FROM card c \n"
					+ "\t INNER JOIN template_block tb on tb.template_id = c.template_id \n"
					+ "\t INNER JOIN attribute a on a.block_code=tb.block_code and a.data_type='B' and a.attribute_code in (" + sAttrCodesList + ") \n"
			/*		+ "\t LEFT OUTER JOIN attribute_value avLinkFrom \n"
					+ "\t		on avLinkFrom.number_value=c.card_id \n"
					+ "\t		and avLinkFrom.attribute_code=( \n"
					+ "\t			select o.option_value \n"
					+ "\t			from attribute_option o \n"
					+ "\t			where o.attribute_code=a.attribute_code \n"
					+ "\t				and o.option_code='LINK' \n"
					+ "\t		) \n"   */
					+ "\t INNER JOIN attribute_option ao on a.attribute_code = ao.attribute_code \n"
					+ "\t 	and ao.option_code = '" + AttributeOptions.LINK + "' \n"
					+ "\t INNER JOIN attribute_value av on av.number_value = c.card_id and av.attribute_code in (select * from functionsplit(ao.option_value, ';')) \n"
					+ "where c.card_id in (" + getCardIdList() + ") \n"

				);
				
				lv_sql.append(
				"\n UNION SELECT \n"

					+ "\t  null as attr_value_id, \n"					// 1
					+ "\t  c.card_id, \n"								// 2
					+ "\t  a.attribute_code, \n"						// 3

					// ��������, �� ������� ���� ������ �� ������ ����� cardlink, ����-��� backlink'�...
					//+ "\t  avLinkFrom.card_id as number_value, \n"		// 4
					+ "\t  av1.card_id as number_value, \n" //4
					+ "\t  null as string_value,  \n"					// 5
					+ "\t  null as date_value, \n"						// 6

					+ "\t  null as value_id, \n"						// 7
					+ "\t  null as another_value, \n" 					// 8
					+ "\t  null as value_rus, \n"						// 9

					+ "\t  null as value_eng, \n"						// 10
					+ "\t  null as full_name, \n"						// 11
					+ "\t  0 as ord, \n"								// 12

					+ "\t \'"+ Attribute.TYPE_BACK_LINK + "\', \n"		// 13
					+ "\t null as long_binary_value, \n"				// 14
					+ "\t null as card_id \n"							// 15
					);
				
				if(sortingByColumns) {	
					addNullSortingColumns(sortingColumns, lv_sql);
			}
				
				lv_sql.append("\n FROM card c \n"
						+ "\t INNER JOIN template_block tb on tb.template_id = c.template_id \n"
						+ "\t INNER JOIN attribute a on a.block_code=tb.block_code and a.data_type='B' and a.attribute_code in (" + sAttrCodesList + ") \n"
						+ "\t INNER JOIN attribute_option ao on a.attribute_code = ao.attribute_code \n"
						+ "\t 	and ao.option_code = '" + AttributeOptions.LINK + "' \n"
						+ "\t INNER JOIN attribute_value av on av.number_value = c.card_id and av.attribute_code in (select * from functionsplit(ao.option_value, ';')) \n"
						+ "\t INNER JOIN attribute_option ao1 on a.attribute_code = ao1.attribute_code \n"
						+ "\t   and ao1.option_code = '" + AttributeOptions.UPLINK + "' \n"
						+ "\t INNER JOIN attribute_value av1 on av1.number_value = av.card_id and av1.attribute_code in (select * from functionsplit(ao1.option_value, ';')) \n"
						+ "where c.card_id in (" + getCardIdList() + ") \n"

					);
			}
			/* �.�. - 28.12.2010
			 * ����������� ������������� - ������� ������������
			 * (��������� ��� ��������� �������� ��������� ��������
			 * �������)
			 *
			 */
			need_attribute = countAttributes( this.resultColumns, APPROVAL_STATE); // "_P_APPROVE_STATE"
			if (need_attribute > 0) {
				lv_sql.append(
					"\n UNION ALL SELECT \n"

					+ "\t   null as attr_value_id, \n"				// 1
					+ "\t   c.card_id, \n"							// 2
					+ "\t   '_P_APPROVE_STATE' as attribute_code, \n"			// 3

					+ "\t  null as number_value,  \n"				// 4
					+ "\t   null as string_value, \n"				// 5
					+ "\t   null as date_value, \n"					// 6

					+ "case \n"
 					+ "\t	when c.status_id in (101, 103, 104, 108, 200, 206, 48909) then 1 \n"
 					+ "\t	when c.status_id = 107 and (plan_neg_date.date_value < CURRENT_DATE or \n"
 					+ "\t   exists \n"
                    + "\t   \t  ( \n"
                    + "\t   \t  \t  select it.date_value from attribute_value visa \n"
                    + "\t   \t  \t  join attribute_value it on it.card_id = visa.number_value and it.attribute_code = 'JBR_VISA_TODATE' \n"
                    + "\t   \t  \t  where visa.card_id = c.card_id and visa.attribute_code = 'JBR_VISA_VISA' and it.date_value < CURRENT_DATE \n"
                    + "\t   \t  )  \n"
 					+ "\t   ) then 3 \n"
 					+ "\t   when c.status_id = 107 and (plan_neg_date.date_value >= CURRENT_DATE or plan_neg_date.date_value is null) then 2 \n"
 					+ "\t	when c.status_id in (1, 106) and negotiation_route.number_value is null then 4 \n"
 					+ "\t	when c.status_id = 6092498 or  \n"
 					+ "\t	( c.status_id = 106 and exists \n"
 					+ "\t	\t	( \n"
 					+ "\t	\t	\t	select number_value from \n"
 					+ "\t	\t	\t	( \n"
 					+ "\t	\t	\t	\t	select iter.number_value from attribute_value visa \n"
 					+ "\t	\t	\t	\t	join attribute_value iter on iter.card_id = visa.number_value and iter.attribute_code = 'JBR_VISA_VISA_RND' \n"
 					+ "\t	\t	\t	\t	where visa.card_id = c.card_id and visa.attribute_code = 'JBR_VISA_VISA' \n"
 					+ "\t	\t	\t	\t	union \n"
 					+ "\t	\t	\t	\t	select iter.number_value from attribute_value sign \n"
 					+ "\t	\t	\t	\t	join attribute_value iter on iter.card_id = sign.number_value and iter.attribute_code = 'JBR_SIGN_SIGN_RND' \n"
 					+ "\t	\t	\t	\t	where sign.card_id = c.card_id and sign.attribute_code = 'JBR_SIGN_SIGNING' \n"
 					+ "\t	\t	\t	) iters \n"
 					+ "\t	\t	\t	where number_value > 0 \n"
 					+ "\t	\t	)  \n"
 					+ "\t	) then 5 \n"
 					+ "\t	else 0 \n"
 					+ "end as value_id, \n"						// 7
					+ "\t   null as another_value, \n"				// 8
					// '||-- null as long_binary_value,
					+ "\t   null as value_rus, \n"			// 9

					+ "\t   null as value_eng, \n"			// 10
					+ "\t   null as full_name, \n"					// 11
					+ "\t   0 as ord, \n"				// 12

					+ "\t \'"+ Attribute.TYPE_LIST + "\', \n"		// 13
					+ "\t null as long_binary_value, \n"				// 14
					+ "\t null as card_id \n"							// 15
					);
				
				if(sortingByColumns) {	
					addNullSortingColumns(sortingColumns, lv_sql);
				}

				lv_sql.append("\n  from card c \n"
 					+ "left outer join attribute_value plan_neg_date on c.card_id = plan_neg_date.card_id and plan_neg_date.attribute_code ='ADMIN_5976960' \n"
 					+ "left outer join attribute_value negotiation_date on c.card_id = negotiation_date.card_id and negotiation_date.attribute_code = 'ADMIN_5976961' \n"
 					+ "left outer join attribute_value negotiation_route on c.card_id = negotiation_route.card_id and negotiation_route.attribute_code = 'ADMIN_5971141' \n"
					+ "where c.card_id in (" + getCardIdList() + ") \n"

				);
			}
			/* �.�. - 24.03.2011
			 * ����������� ������������� - ������������� ����
			 *
			 */
			need_attribute = countAttributes( this.resultColumns, ATTR_UNIVERSAL_TERM); // "_UNITERM"
			if (need_attribute > 0) {
				lv_sql.append(
						"\n UNION ALL SELECT \n"

						+ "\t   null as attr_value_id, \n"				// 1
						+ "\t   c.card_id, \n"		// 2
						+ "\t   '_UNITERM' as attribute_code, \n"			// 3

						+ "\t  null as number_value,  \n"				// 4
						+ "\t   null as string_value, \n"				// 5
						+ "\t   min(av.date_value) as date_value, \n"// 6

						+ "\t   null as value_id, \n"	// 7
						+ "\t   null as another_value, \n"				// 8
						// '||-- null as long_binary_value,
						+ "\t   null as value_rus, \n"			// 9

						+ "\t   null as value_eng, \n"			// 10
						+ "\t   null as full_name, \n"// 11
						+ "\t   0 as ord, \n"				// 12

						+ "\t \'"+ Attribute.TYPE_DATE + "\', \n"		// 13
						+ "\t null as long_binary_value, \n"			// 14
						+ "\t null as card_id \n"						// 15
						);
				
				if(sortingByColumns) {	
					addNullSortingColumns(sortingColumns, lv_sql);
				}

				lv_sql.append("\n from card c \n"
						+ "join attribute_value av on av.card_id = c.card_id and "
						+	"av.attribute_code in "
						+	"("
						+		"'JBR_VISA_TODATE', "
						+		"'ADMIN_893447', "
						+		"'JBR_INFORM_DATA', "
						+		"'JBR_INF_TERM', "
						+		"'ADMIN_726875', "
						+		"'ADMIN_5976960', "
						+		"'JBR_SIGN_TODATE', "
						+		"'JBR_RASSM_TODATE', "
						+		"'JBR_TCON_TERM', "
						+		"'JBR_IMPL_DEADLINE', "
						+		"'JBR_PCON_DATE'"
						+	") \n"
						+ "where c.card_id in (" + getCardIdList() + ") \n"
						+ "group by c.card_id"
				);
			}
			
			/* 
             *  ������������� - ������� ����������� �������� �������
             */
            need_attribute = countAttributes( this.resultColumns, ATTR_DIGITAL_SIGNATURE); // "_DIGITAL_SIGNATURE"
            if (need_attribute > 0) {
                lv_sql.append(
                        "\n UNION ALL SELECT \n"

                        + "\t   null as attr_value_id, \n"                  // 1
                        + "\t   c.card_id, \n"                              // 2
                        + "\t   '_DIGITAL_SIGNATURE' as attribute_code, \n" // 3

                        + "\t  1 as number_value,  \n"                      // 4
                        + "\t   null as string_value, \n"                   // 5
                        + "\t   null as date_value, \n"// 6

                        + "\t   null as value_id, \n"   // 7
                        + "\t   null as another_value, \n"                  // 8
                        // '||-- null as long_binary_value,
                        + "\t   null as value_rus, \n"                      // 9

                        + "\t   null as value_eng, \n"                      // 10
                        + "\t   null as full_name, \n"                      // 11
                        + "\t   0 as ord, \n"                               // 12

                        + "\t \'"+ Attribute.TYPE_MATERIAL + "\', \n"       // 13
                        + "\t null as long_binary_value, \n"                // 14
                        + "\t null as card_id \n"                           // 15
                        );
                
                if(sortingByColumns) {	
					addNullSortingColumns(sortingColumns, lv_sql);
				}

				lv_sql.append("\n FROM card c \n"
                        + "JOIN attribute_value av ON (av.card_id = c.card_id AND av.attribute_code = 'JBR_SIGN_SIGNING') \n"  // �������� ����������
                        + "JOIN attribute_value avs ON (avs.card_id = av.number_value AND avs.attribute_code = 'ADMIN_67129') \n"  // ADMIN_67129 - ������� � �������������
                        + "where c.card_id in (" + getCardIdList() + ") \n"
                );
            }
			/* 
             *  ������������� - ������� ����������� �������� ��� �� ���������� ���������� � �������� ��
             */
            need_attribute = countAttributes( this.resultColumns, ATTR_ALL_DOCLINKS); // "_ALL_DOCLINKS"
            if (need_attribute > 0) {
                lv_sql.append(
                        "\n UNION ALL SELECT \n"
            
                        + "\t   null as attr_value_id, \n"                  // 1
                        + "\t   c.card_id, \n"                              // 2
                        + "\t   '_ALL_DOCLINKS' as attribute_code, \n" // 3

                        + "\t  1 as number_value,  \n"                      // 4
                        + "\t   null as string_value, \n"                   // 5
                        + "\t   null as date_value, \n"// 6

                        + "\t   null as value_id, \n"   // 7
                        + "\t   null as another_value, \n"                  // 8
                        // '||-- null as long_binary_value,
                        + "\t   null as value_rus, \n"                      // 9

                        + "\t   null as value_eng, \n"                      // 10
                        + "\t   null as full_name, \n"                      // 11
                        + "\t   0 as ord, \n"                               // 12

                        + "\t \'"+ Attribute.TYPE_MATERIAL + "\', \n"       // 13
                        + "\t null as long_binary_value, \n"                // 14
                        + "\t null as card_id \n"                           // 15
                        );
                
                if(sortingByColumns) {	
					addNullSortingColumns(sortingColumns, lv_sql);
				}

				lv_sql.append("\n FROM (" +
						"--������������ \n" +
						"select c.card_id from card c  \n" +
						"join attribute_value av_parent on av_parent.number_value = c.card_id and av_parent.attribute_code in ('JBR_IMPL_ACQUAINT','JBR_SIGN_SIGNING')  \n" +
						"join attribute_value av on av_parent.card_id = av.card_id and av.attribute_code in ('DOCLINKS','INFO_DOCLINKS') \n" +
						"where c.card_id in (" + getCardIdList() + ") \n" +
							"UNION \n" +
						"--�� \n" +
						"select c.card_id from card c  \n" +
						"join attribute_value av on c.card_id = av.card_id and av.attribute_code in ('DOCLINKS','INFO_DOCLINKS') \n" +
						"where c.template_id in (224,364,784,864,1226,764,1255,1600) and c.card_id in (" + getCardIdList() + ") \n" +
							"UNION \n" +
						"--������������ \n" +
						"select couple.card_id from  \n" +
						"(select c.card_id, functionbacklink(c.card_id,'ADMIN_713517','JBR_INFORM_LIST') as parent from card c \n" +
						"where c.card_id in (" + getCardIdList() + ")) as couple --13222574,13222575 \n" +
						"join attribute_value av on couple.parent = av.card_id and av.attribute_code in ('DOCLINKS','INFO_DOCLINKS') \n" +
							"UNION \n" +
						"--������ �� ����������/����� �� ���������� ������� ������������/������������ � ���������� \n" +
						"select c.card_id from card c  \n" +
						"join attribute_value av_parent on av_parent.card_id = c.card_id and av_parent.attribute_code in ('ADMIN_702604','ADMIN_702602','ADMIN_726877')  \n" +
						"join attribute_value av on av_parent.number_value = av.card_id and av.attribute_code in ('DOCLINKS','INFO_DOCLINKS') \n" +
						"where c.card_id in (" + getCardIdList() + ")  \n" +
							"UNION \n" +
						"--��������� \n" +
						"select c.card_id from card c  \n" +
						"join attribute_value av_parent on av_parent.card_id = c.card_id and av_parent.attribute_code in ('JBR_MAINDOC')  \n" +
						"join attribute_value av on av_parent.number_value = av.card_id and av.attribute_code in ('DOCLINKS','INFO_DOCLINKS') \n" +
						"where c.card_id in (" + getCardIdList() + ")  \n" +
							"UNION \n" +
						"--������������ \n" +
						"select couple.card_id from  \n" +
						"(select c.card_id, functionbacklink(c.card_id,'ADMIN_6814498','JBR_VISA_VISA') as parent from card c \n" +
						"where c.card_id in (" + getCardIdList() + ")) as couple \n" +
						"join attribute_value av on couple.parent = av.card_id and av.attribute_code in ('DOCLINKS','INFO_DOCLINKS')) as c\n"
                );
            }
         // ����������� ������������� - ��������������� ����
     			need_attribute = countAttributes( this.resultColumns, ATTR_PRELIMINARY_TERM); // "_STATE"
     			if (need_attribute > 0) {
     				lv_sql.append(
     					"\n UNION ALL SELECT \n"

     					+ "\t   null as attr_value_id, \n"				// 1
     					+ "\t   c.card_id, \n"							// 2
     					+ "\t   '_PRELIMINARY_TERM' as attribute_code, \n"			// 3

     					+ "\t   null as number_value, \n"				// 4
     					+ "\t   null as string_value, \n"				// 5
     					+ "\t   null as date_value, \n"					// 6

     					+ "\t   case when av.date_value > now() then 1 end as value_id, \n"			// 7
     					+ "\t   null as another_value, \n"				// 8
     					+ "\t   null as value_rus, \n"			// 9

     					+ "\t   null as value_eng, \n"			// 10
     					+ "\t   null as full_name, \n"					// 11
     					+ "\t   0 as ord, \n"				// 12

     					+ "\t \'"+ Attribute.TYPE_LIST + "\', \n"		// 13
     					+ "\t null as long_binary_value, \n"			// 14
     					+ "\t null as card_id \n"						// 15
     					);
     				
     				if(sortingByColumns) {	
    					addNullSortingColumns(sortingColumns, lv_sql);
    				}

    				lv_sql.append("\n FROM \n"
     					+ "\t   card c "
     					+ "left outer join attribute_value res on res.card_id = c.card_id and res.attribute_code = 'ADMIN_702311' "
     					+ "join attribute_value av on av.card_id = coalesce(res.number_value, c.card_id) and av.attribute_code = 'JBR_TCON_TERM_PRELIM' "
     					+ "where c.card_id in (" + getCardIdList() + ") "

     				);
     			}
     		
     		if(sortingByColumns) {
     			lv_sql.append("\t order by ");
    			for(int i=0; i < sortingColumns.size(); i++) {
    				SearchResult.Column column = sortingColumns.get(i);
    				if(i != 0) {
    					lv_sql.append(", ");
    				}
    				lv_sql.append(" order_" + i);
    				if(column.getSorting() == 1) {
    					lv_sql.append(" asc nulls last");
    				} else lv_sql.append(" desc nulls last");
    			}
    			if(!sortingColumns.isEmpty())
    				lv_sql.append(",");
    			lv_sql.append(" attr_value_id asc nulls last");
    		} else {
    			// (!) ������� �������� ������ �������� ������ - ���������������� ��������.
    			// ��������� ��������� multi valued �������� � ��� �������, � ������� ��� ���� ���������,
    			// ��� ����� � ������ �� ������� ��������������, ��������.
    			lv_sql.append("\t order by attr_value_id asc \n");
    		}

     		/* Getting result */
			// (!) ����������...
			// open pout_cards for lv_sql;
			this.resultAttributes = getJdbcTemplate().query(
					lv_sql.toString(),
					new RowMapper()
					{
						public Object mapRow(ResultSet rs, int rowNum)
							throws SQLException
						{
							Object[] row = new Object[COL_COUNT];

							row[COL_CARDID] = rs.getLong(2);
							row[COL_ATTRID] = rs.getString(3);
							row[COL_VAL_INT] = rs.getLong(4);

							row[COL_VAL_STRING] = rs.getString(5);
							row[COL_VAL_DATE] = rs.getTimestamp(6);
							row[COL_VAL_REF] = rs.getLong(7);

							row[COL_VAL_OTHER] = rs.getString(8);
							row[COL_VAL_REF_RU] = rs.getString(9);
							row[COL_VAL_REF_EN] = rs.getString(10);

							row[COL_VAL_PERSON] = rs.getString(11);
							row[COL_VAL_TYPE]= rs.getString(13);

							String strBinary;
							try {
								strBinary = SimpleDBUtils.getBlobAsStr(rs, 14, "UTF-8");
							} catch (UnsupportedEncodingException e) {
								strBinary = rs.getString(14);
							}
							row[COL_VAL_BINARY]= strBinary;
							row[COL_PERSON_CARDID] = rs.getLong(15);

							return row;
						}
					}
			);
			logger.debug("Values of columns fetched. Total attributes number:" + this.resultAttributes.size());
		/*}*/
	}

	/**
	 * �������� ������ ��� �������� �� ������ this.m_objectIds.
	 * �������� �������������������� �������� � �������������� ������:
	 *    1) Id,
	 *    2) � ���������� (permission View/Edit) ��� ������������ personId.
	 * @param personId ������������, ��� �������� ���� ��������� ����������.
	 * @return map {cardId -> Card}, (!) ������ �������� � ������ ������������
	 * ������ id � canRead/canWrite.
	 */
	protected Map<Long, Card> loadCardsPermissions(ObjectId personId) {
		final Map<Long, Card> resultMap = new HashMap<Long, Card>();
		Search.Filter filter = (this.search!=null)?this.search.getFilter():null;
		if (m_objectIds.size() > 0){

			/* 
			 * �.�. - 22.07.2011
			 * ����� ������� �������� ���� 
			*/
			StringBuilder sqlBuf = new StringBuilder();
			
// �������� ������������ ����. ���� ������ ��� �������� �������� ������� ����
// � ������ BR4J00039100 (����. �������� ����� ������� ������)
//*********************************************************************
			if (!Person.ID_SYSTEM.equals(personId)) {
				sqlBuf.append("with delegation_rules as( \n");
				sqlBuf.append("SELECT c.card_id, cr.operation_code, dr.link_attr_code, ao.option_code, ao.option_value FROM delegation_access_rule dr \n");
				sqlBuf.append("JOIN access_rule r ON r.rule_id=dr.rule_id \n");
				sqlBuf.append("JOIN access_card_rule cr ON cr.rule_id=r.rule_id \n");
				sqlBuf.append("LEFT OUTER JOIN attribute_option ao ON ao.attribute_code = dr.link_attr_code \n");
				sqlBuf.append(", card c \n");
				sqlBuf.append("WHERE c.card_id in (").append(getCardIdList()).append(") \n");
				sqlBuf.append("AND c.template_id = 284 \n"); // ������ ��� ������� ����
				sqlBuf.append("AND (r.template_id = c.template_id) \n");
				sqlBuf.append("AND (r.status_id=c.status_id) \n");
				sqlBuf.append("AND cr.operation_code in ('R', 'W') \n");
				sqlBuf.append("), t as( \n");
				sqlBuf.append("select c.card_id as main_card, c.template_id, del.card_id as child_card, del.operation_code from card c \n");
				sqlBuf.append("join attribute_value av on av.card_id = c.card_id \n");
				sqlBuf.append("join delegation_rules del on del.option_value = av.attribute_code and av.number_value = del.card_id \n");
				sqlBuf.append(") select	t.child_card, acr.operation_code from access_list al  \n");
				sqlBuf.append("join access_card_rule acr on acr.rule_id = al.rule_id \n");
				sqlBuf.append("join t on t.main_card = al.card_id \n");
				sqlBuf.append("and t.operation_code = acr.operation_code \n");
				sqlBuf.append("and al.person_id = ").append(personId.getId()).append(" \n");
				sqlBuf.append("group by t.child_card, acr.operation_code \n");
				sqlBuf.append("union \n");
			}
//*********************************************************************	
			
			// ���� ������� ������������ - �������, �� ��������� ��� �������� � ������ �������������� � ������
			if (Person.ID_SYSTEM.equals(personId)){
				sqlBuf.append("select c.card_id, 'W' from card c \n");
				sqlBuf.append("where c.card_id in (").append(getCardIdList()).append(")");
				sqlBuf.append("union all \n");
				sqlBuf.append("select c.card_id, 'R' from card c \n");
				sqlBuf.append("where c.card_id in (").append(getCardIdList()).append(")");
			} else if((filter == null) ||
					(filter.getCurrentUserPermission().equals(Search.Filter.CU_DONT_CHECK_PERMISSIONS))){
				// ���� ������ �� ����� ��� � ��� ���������� �������, ��� ��������� ����� �� ����, 
				// �� ��� �������� �������� �������� ��� ������ ��� ������, ����� ������� �� �� ��������� � ������ ������ � ������,
				// � ������ ��� ������� �������� �������������, ������� ���� ����� ����� ��-���� �� �� ����� ���� (�������� ������������)
				sqlBuf.append("select c.card_id, 'R' from card c \n");
				sqlBuf.append("where c.card_id in (").append(getCardIdList()).append(")");
			} else {
				if (!filter.getTemplatesWithoutPermCheck().isEmpty()) {
					// skip permissions check for specified templates
					sqlBuf.append("select c.card_id, 'R' from card c \n");
					sqlBuf.append("where c.card_id in (").append(getCardIdList()).append(") and c.template_id in (");
					sqlBuf.append(SimpleDBUtils.stringIdentifiersToCommaSeparatedSqlString(filter.getTemplatesWithoutPermCheck()));
					sqlBuf.append(")\n");
					sqlBuf.append("union \n");
				}
				sqlBuf.append("select card_id, acr.operation_code from access_list al \n");
				sqlBuf.append("join access_card_rule acr on acr.rule_id = al.rule_id \n");
				sqlBuf.append("where al.card_id in (").append(getCardIdList()).append(") and al.person_id = ").append(personId.getId()).append(" \n");
				sqlBuf.append("union \n");
				sqlBuf.append("select c.card_id, acr.operation_code from card c \n");
				sqlBuf.append("join ( \n");
				sqlBuf.append("select ar.rule_id, ar.template_id, ar.status_id  from person_role pr \n");
				sqlBuf.append("join role_access_rule rar on rar.role_code is null or rar.role_code = pr.role_code  \n");
				sqlBuf.append("join access_rule ar on ar.rule_id = rar.rule_id \n");
				sqlBuf.append("where person_id = ").append(personId.getId()).append(" \n");
				sqlBuf.append(") t on (t.template_id is null or t.template_id = c.template_id) and (t.status_id is null or t.status_id = c.status_id) \n");
				sqlBuf.append("join access_card_rule acr on acr.rule_id = t.rule_id \n");
				sqlBuf.append("where card_id in (").append(getCardIdList()).append(")\n");
				sqlBuf.append(" union  \n");
	            sqlBuf.append("select c.card_id, acr.operation_code from card c, role_access_rule rr\n");
	            sqlBuf.append("\tjoin access_rule r on r.rule_id=rr.rule_id \n");
	            sqlBuf.append("\tjoin access_card_rule acr on acr.rule_id = r.rule_id\n");
	            sqlBuf.append("WHERE \n\t c.card_id in (").append(getCardIdList()).append(")\n");
	            sqlBuf.append("\t\t and rr.role_code is NULL  and (r.template_id=c.template_id)\n");
	            sqlBuf.append("\t\t and (r.status_id=c.status_id)");
			}
			
            final String sqlText = sqlBuf.toString();
			

			final String sInfo =
				"Get permissions SQL is:" + SimpleDBUtils.getSqlQueryInfo( sqlText, null, null);// SimpleDBUtils.getSqlQueryInfo( sqlText, args, argTypes);


			try {
// (2012/12/11, YNikitin) ���� ����������, ��� �������� ������ �������� ���� ���� ��������� � ����������� �� ����, �� ����� ���� ����� �������
				//m_objectIds.clear(); 
				final Set<ObjectId> resolvedCard = new HashSet<ObjectId>();
				getJdbcTemplate().query( sqlText, 
						new RowCallbackHandler()
						{
							public void processRow(ResultSet rs)
									throws SQLException
							{
								if (rs == null) return;
								// supposed to get columns in order:
								// 		1) (int)card_id,
								//		2) (String) permission_type, 
								final Long idCard = rs.getLong(1);
								Card card = resultMap.get(idCard);
								if (card == null) { // !resultMap.containsKey(idCard)
									// ������� ����� ��������...
									card = createEmptyCard(idCard);
									resultMap.put(idCard, card);
// (2012/12/11, YNikitin) ���� ����������, ��� �������� ������ �������� ���� ���� ��������� � ����������� �� ����, �� ����� ���� ����� �������
									resolvedCard.add(card.getId());
									//m_objectIds.add(card.getId());
								};

								// ���� �����...
								final String permission = rs.getString(2);
								if ("R".equals(permission))
									card.setCanRead(true);
								// else 
								if ("W".equals(permission))
									card.setCanWrite(true);
							}
						}
				);
				//
				
					// ��������� �������� ������ �������� �� ��, � ������� ���� ������
					removeCardsNotPermission(resolvedCard);
				
			} finally {
				logger.debug( sInfo + "\n Got permissions cards counter :"+ resultMap.size());
			}
		}
		return resultMap;
	}

	private void removeCardsNotPermission(Set<ObjectId> objectIds){
		Iterator<ObjectId> iterator = this.m_objectIds.iterator();
		while(iterator.hasNext()) {			
			if(!objectIds.contains(iterator.next())){
				iterator.remove();				
			}
		}		
	}

	private String m_objectIdsAsList = null;
	private String getCardIdList() {
		if (m_objectIdsAsList == null) {
			m_objectIdsAsList =
					(m_objectIds != null && m_objectIds.size() > 0)
					? m_objectIdsAsList = ObjectIdUtils.numericIdsToCommaDelimitedString(m_objectIds)
					: "0";
		}
		return m_objectIdsAsList;
	}
	
	private String getTableColumnNameByAttrType(Class<?> attrType, Class<?> attrLabelType, String alias) {
		if((ListAttribute.class).equals(attrType)
				|| (TreeAttribute.class).equals(attrType)) {
			return "vl.value_rus";
		} else if((StringAttribute.class).equals(attrType)
				|| (TextAttribute.class).equals(attrType)) {
			return alias + "string_value";
		} else if((IntegerAttribute.class).equals(attrType) 
				|| (LongAttribute.class).equals(attrType)) {
			return alias + "number_value";
		} else if((PersonAttribute.class).equals(attrType)) {
			return "p.full_name";
		} else if(attrType.isAssignableFrom(CardLinkAttribute.class)) {
			return getTableColumnNameByAttrType(attrLabelType, null, "av1.");
		} else if((DateAttribute.class).equals(attrType)) {
			// TODO: ������ ��� �������� ������ � PostgreSQL
			return "date_trunc('day', " + alias + "date_value)";
		}
		return alias + "string_value";
	}
	
	private String getTableColumnByAttrType(Column column, int index) {
		StringBuilder sb = new StringBuilder();
		final Class<?> attrClass = column.getAttributeId().getType();
		final Class<?> attrLabelClass = column.getLabelAttrId() != null ? column.getLabelAttrId().getType() : null;
		sb.append(", (select ");
		sb.append(getTableColumnNameByAttrType(attrClass, attrLabelClass, "av."));
		sb.append("\t from attribute_value av ");
		if(attrClass.isAssignableFrom(CardLinkAttribute.class)) {
			if(column.getLabelAttrId() != null) {
				sb.append("\t join attribute_value av1 on av.number_value = av1.card_id and av1.attribute_code = '");
				sb.append(column.getLabelAttrId().getId());
				sb.append("' \n");
				sb.append(additionalJoin(attrLabelClass, "av1."));
			}
		} else {
			sb.append(additionalJoin(attrClass, "av."));
		}
		sb.append("\t where av.card_id = a.card_id ");
		sb.append("\t and av.attribute_code = ").append("\t '").append(column.getAttributeId().getId()).append("' \n");
		
		sb.append(") as order_" + index);
		
		return sb.toString();
	}
	
	private String additionalJoin(Class<?> attrClass, String alias) {
		if((ListAttribute.class).equals(attrClass)
				|| (TreeAttribute.class).equals(attrClass)) {
			return "\t join values_list vl on vl.value_id = " + alias + "value_id";
		} else if((PersonAttribute.class).equals(attrClass)) {
			return "\t join person p on p.person_id = " + alias + "number_value";
		}
		return "";
	}
	
	private void addNullSortingColumns(List<SearchResult.Column> columns, StringBuffer sb) {
		for(int i = 0 ; i < columns.size(); i++) {
			SearchResult.Column column = columns.get(i);
			if(column.getSorting() > 0) {
				sb.append(", null as order_").append(i);
			}
		}
	}


}
