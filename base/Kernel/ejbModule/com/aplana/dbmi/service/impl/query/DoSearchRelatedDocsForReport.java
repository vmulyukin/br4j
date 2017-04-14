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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.SearchRelatedDocsForReport;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoSearchRelatedDocsForReport extends ActionQueryBase {

	@SuppressWarnings("unchecked")
	@Override
	public Object processQuery() throws DataException {
		Collection<ObjectId> ids = null;
		SearchRelatedDocsForReport action = (SearchRelatedDocsForReport) getAction();
		ObjectId reportId = action.getCard().getId();
		ObjectId mainDocId = ((CardLinkAttribute) action.getCard().getAttributeById(SearchRelatedDocsForReport.DOC_ID)).getSingleLinkedId();
			
		if(action.getScope().equals(SearchRelatedDocsForReport.Scope.SUBREPORTS)){
			ids = (Collection<ObjectId>) getJdbcTemplate().query(
				"select docs.number_value from ( \n"
					+ "	with recursive res as( \n"
					+ "	select number_value as res_id from attribute_value where card_id =  ? and attribute_code = ? \n"
					+ "	union \n"
					+ "select distinct \n"
					+ "CASE  \n"
					+ "	WHEN upLink.option_value is null \n"
					+ "		THEN avLinkFrom.card_id \n"
					+ "	WHEN upLink.option_value is not null \n"
					+ "		THEN functionbacklink(c.res_id, upLink.option_value, link.option_value) \n"
					+ "	ELSE NULL \n"
					+ "      END \n"
					+ "FROM res c \n"
					+ "LEFT OUTER JOIN attribute_option link on link.attribute_code = ? \n"
					+ "	and link.option_code='LINK' \n"
					+ "LEFT OUTER JOIN attribute_option upLink on upLink.attribute_code = ? \n"
					+ "	and upLink.option_code='UPLINK' \n"
					+ "JOIN attribute_value avLinkFrom \n"
					+ "	on avLinkFrom.number_value=c.res_id \n"
					+ "	and avLinkFrom.attribute_code=( \n"
					+ "			select o.option_value \n"
					+ "			from attribute_option o \n"
					+ "			where o.attribute_code=? \n"
					+ "			and o.option_code='LINK' \n"
					+ "			) \n"
					+ "	) select res_id from res \n"
					+ "	except ( \n"
					+ "		select res_id from res \n"
					+ "		join attribute_value rep_ex on rep_ex.card_id = ? and rep_ex.attribute_code = ? \n"
					+ "		join attribute_value res_ex on res_ex.card_id = res_id and res_ex.attribute_code = ? and res_ex.number_value = rep_ex.number_value \n"
					+ "		limit 1 \n"
					+ "	) \n"
					+ ") res \n"
					+ "join attribute_value rep on rep.number_value = res_id and rep.attribute_code = ? and rep.card_id <>  ? \n"
					+ "join attribute_value docs on docs.card_id = rep.card_id and docs.attribute_code = ?",
				new Object[]{			
					reportId.getId(), 
					SearchRelatedDocsForReport.REPORT_ID.getId(), 
					SearchRelatedDocsForReport.SUB_RESOLUTION_ID.getId(),
					SearchRelatedDocsForReport.SUB_RESOLUTION_ID.getId(),
					SearchRelatedDocsForReport.SUB_RESOLUTION_ID.getId(),
					reportId.getId(),
					SearchRelatedDocsForReport.EXEC_REPORT_ID.getId(),
					SearchRelatedDocsForReport.EXEC_RESP_ID.getId(),
					SearchRelatedDocsForReport.REPORT_ID.getId(),
					reportId.getId(),
					SearchRelatedDocsForReport.LINKED_DOCS_ID.getId()
				},
				new int[]{
					Types.NUMERIC, // reportId
					Types.VARCHAR, // SearchRelatedDocsForReport.REPORT_ID
					Types.VARCHAR, // SearchRelatedDocsForReport.SUB_RESOLUTION_ID
					Types.VARCHAR, // SearchRelatedDocsForReport.SUB_RESOLUTION_ID 
					Types.VARCHAR, // SearchRelatedDocsForReport.SUB_RESOLUTION_ID
					Types.NUMERIC, // reportId
					Types.VARCHAR, // SearchRelatedDocsForReport.EXEC_REPORT_ID
					Types.VARCHAR, // SearchRelatedDocsForReport.EXEC_RESP_ID
					Types.VARCHAR, // SearchRelatedDocsForReport.REPORT_ID
					Types.NUMERIC, // reportId
					Types.VARCHAR  // SearchRelatedDocsForReport.LINKED_DOCS_ID
				},
				new RowMapper(){
					public Object mapRow(ResultSet set, int rowNum) throws SQLException{
						return new ObjectId(Card.class, set.getLong(1));
					}
				}			
			);
		} else if (action.getScope().equals(SearchRelatedDocsForReport.Scope.WHOLE_DOCUMENT)){
			ids = (Collection<ObjectId>) getJdbcTemplate().query(
				"with recursive res as( \n"
					+ "select distinct \n"
					+ "CASE  \n"
					+ "	WHEN upLink.option_value is null \n"
					+ "		THEN avLinkFrom.card_id \n"
					+ "	WHEN upLink.option_value is not null \n"
					+ "		THEN functionbacklink(c.card_id, upLink.option_value, link.option_value) \n"
					+ "	ELSE NULL \n"
					+ "      END as res_id \n"
					+ "FROM card c \n"
					+ "LEFT OUTER JOIN attribute_option link on link.attribute_code = ? \n"
					+ "	and link.option_code='LINK' \n"
					+ "LEFT OUTER JOIN attribute_option upLink on upLink.attribute_code = ? \n"
					+ "	and upLink.option_code='UPLINK' \n"
					+ "JOIN attribute_value avLinkFrom \n"
					+ "	on avLinkFrom.number_value=c.card_id \n"
					+ "	and avLinkFrom.attribute_code=( \n"
					+ "			select o.option_value \n"
					+ "			from attribute_option o \n"
					+ "			where o.attribute_code=? \n"
					+ "			and o.option_code='LINK' \n"
					+ "			) \n"
					+ "WHERE c.card_id=? \n"
					+ "	union \n"
					+ "select distinct \n"
					+ "CASE  \n"
					+ "	WHEN upLink.option_value is null \n"
					+ "		THEN avLinkFrom.card_id \n"
					+ "	WHEN upLink.option_value is not null \n"
					+ "		THEN functionbacklink(c.res_id, upLink.option_value, link.option_value) \n"
					+ "	ELSE NULL \n"
					+ "      END \n"
					+ "FROM res c \n"
					+ "LEFT OUTER JOIN attribute_option link on link.attribute_code = ? \n"
					+ "	and link.option_code='LINK' \n"
					+ "LEFT OUTER JOIN attribute_option upLink on upLink.attribute_code = ? \n"
					+ "	and upLink.option_code='UPLINK' \n"
					+ "JOIN attribute_value avLinkFrom \n"
					+ "	on avLinkFrom.number_value=c.res_id \n"
					+ "	and avLinkFrom.attribute_code=( \n"
					+ "			select o.option_value \n"
					+ "			from attribute_option o \n"
					+ "			where o.attribute_code=? \n"
					+ "			and o.option_code='LINK' \n"
					+ "			) \n"
					+ ") select docs.number_value from res \n"
					+ "join attribute_value rep on rep.number_value = res_id and rep.attribute_code = ? and rep.card_id <> ? \n"
					+ "join attribute_value docs on docs.card_id = rep.card_id and docs.attribute_code = ? \n"
					+ "union \n"
					+ "select number_value from attribute_value where card_id =  ? and attribute_code = ? \n"
					+ "union \n"
					+ "select card_id from attribute_value where number_value =  ? and attribute_code = ?",
				new Object[]{
					SearchRelatedDocsForReport.TOP_RESOLUTION_ID.getId(),
					SearchRelatedDocsForReport.TOP_RESOLUTION_ID.getId(),
					SearchRelatedDocsForReport.TOP_RESOLUTION_ID.getId(),
					mainDocId.getId(),
					SearchRelatedDocsForReport.SUB_RESOLUTION_ID.getId(),
					SearchRelatedDocsForReport.SUB_RESOLUTION_ID.getId(),
					SearchRelatedDocsForReport.SUB_RESOLUTION_ID.getId(),
					SearchRelatedDocsForReport.REPORT_ID.getId(),
					reportId.getId(),
					SearchRelatedDocsForReport.LINKED_DOCS_ID.getId(),
					mainDocId.getId(),
					SearchRelatedDocsForReport.DOC_DOCS_ID.getId(),
					mainDocId.getId(),
					SearchRelatedDocsForReport.DOC_DOCS_ID.getId(),
				},
				new int[]{
					Types.VARCHAR, // SearchRelatedDocsForReport.TOP_RESOLUTION_ID
					Types.VARCHAR, // SearchRelatedDocsForReport.TOP_RESOLUTION_ID
					Types.VARCHAR, // SearchRelatedDocsForReport.TOP_RESOLUTION_ID
					Types.NUMERIC, // mainDocId
					Types.VARCHAR, // SearchRelatedDocsForReport.SUB_RESOLUTION_ID
					Types.VARCHAR, // SearchRelatedDocsForReport.SUB_RESOLUTION_ID
					Types.VARCHAR, // SearchRelatedDocsForReport.SUB_RESOLUTION_ID
					Types.VARCHAR, // SearchRelatedDocsForReport.REPORT_ID
					Types.NUMERIC, // reportId
					Types.VARCHAR, // SearchRelatedDocsForReport.LINKED_DOCS_ID
					Types.NUMERIC, // mainDocId
					Types.VARCHAR, // SearchRelatedDocsForReport.DOC_DOCS_ID
					Types.NUMERIC, // mainDocId
					Types.VARCHAR  // SearchRelatedDocsForReport.DOC_DOCS_ID
				},
				new RowMapper(){
						public Object mapRow(ResultSet set, int rowNum) throws SQLException{
							return new ObjectId(Card.class, set.getLong(1));
						}
					}
				);
		}	

		return ids;
	}

}
