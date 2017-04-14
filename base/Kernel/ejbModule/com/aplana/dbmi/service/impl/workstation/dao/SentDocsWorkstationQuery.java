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
package com.aplana.dbmi.service.impl.workstation.dao;

import com.aplana.dbmi.model.workstation.SortAttribute;
import com.aplana.dbmi.service.impl.workstation.EmptyCard;
import com.aplana.dbmi.service.impl.workstation.Util;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.aplana.dbmi.service.impl.workstation.Util.*;

public class SentDocsWorkstationQuery extends JdbcDaoSupport {

	public static class AgeFolderSupQuery extends AgeSupportWorkstationQuery
			implements AreaWorkstationQueryInterface{

		public List<EmptyCard> getCards(int userId, long[] permissionTypes,
				int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			StringBuilder sql = buildAgeSupervisorQuery(false,
					userId, permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter, getDaysAge());
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard(rs.getLong(1), rs.getLong(2), rs.getLong(3));
						}
					});
		}

		public List getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildAgeSupervisorQuery(true, userId, permissionTypes, 0, 0, null, simpleSearchFilter, 
					getDaysAge());
			List queryResult = executeSimpleQuery(getJdbcTemplate(), sql
					.toString(), new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					return new Long[] { rs.getLong(1), rs.getLong(2) };
				}
			});
			return queryResult;
		}
		

	}

	public static class TemplateParams {
		public StringBuilder linksToBaseDoc;
		public String templateId;
		public String statusIds;
		public String userAttr;

		public TemplateParams(String linksToBaseDocAttribute, String templateId, String statusIds, String userAttr) {
			this.linksToBaseDoc = getDelimitedWithNoEscaping(Arrays.asList(linksToBaseDocAttribute.split(",")));
			this.templateId = templateId.trim();
			this.statusIds = statusIds.trim();
			this.userAttr = userAttr.trim();
		}
	}

	private static StringBuilder buildAgeSupervisorQuery( boolean count, int userId, long[] permissionTypes,
	        int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter, int daysAge ) {
		
			ArrayList<TemplateParams> params = new ArrayList<TemplateParams>();
			params.add(new TemplateParams("ADMIN_702604", "1044", "206, 207, 34145", "ADMIN_702335"));
			params.add(new TemplateParams("JBR_RASSM_PARENT_DOC", "504", "477679, 477681, 104, 103, 34145", "JBR_RASSM_PERSON"));
			params.add(new TemplateParams("JBR_INFORM_DOC", "524", "67425", "JBR_FOR_INFORMATION"));
			params.add(new TemplateParams("JBR_SIGN_PARENT", "365", "204, 477934, 205, 1", "JBR_SIGN_RESPONSIBLE"));
			params.add(new TemplateParams("JBR_VISA_PARENT_DOC", "348", "201, 6092498, 202, 1", "JBR_VISA_RESPONSIBLE"));
			params.add(new TemplateParams("JBR_ADO_DOCBASE", "2344", "201, 34145", "JBR_VISA_RESPONSIBLE"));
			params.add(new TemplateParams("JBR_DOCL_RELATDOC,ADMIN_221237", "1255", "505050,103,206", "JBR_INFD_SGNEX_LINK"));
			params.add(new TemplateParams("JBR_DOCL_RELATDOC,ADMIN_221237", "764", "101,103,206,104,48909", "JBR_HIDDEN_CHIEF"));
			params.add(new TemplateParams("JBR_DOCL_RELATDOC,ADMIN_221237", "1226", "101,103,206,104,48909", "JBR_HIDDEN_CHIEF"));
			params.add(new TemplateParams("JBR_MAINDOC", "324", "103,206,34145", "JBR_RESP_DOW_MAINDOC"));
			params.add(new TemplateParams("ADMIN_726877", "1144", "67425", "ADMIN_726874"));

	        StringBuilder sql = new StringBuilder(); 
	        sql.append(Util.userPermissionCheckWithClause(userId));
	        sql.append(", \n").append(generateTempTable(params, "cards_temp_table", userId));
	        sql.append( /*with*/", c as  ");
	        sql.append( "( select c.CARD_ID, c.TEMPLATE_ID, c.STATUS_ID, t.template_name_rus, c.is_active " ).append(getOrderByColumnsList(sortAttributes));
	        sql.append("from cards_temp_table\n");
	        sql.append("inner join card c ON (c.card_id = cards_temp_table.number_value)\n");
	        sql.append("inner join template t on (t.template_id=c.template_id) ");
	        sql.append("where ").append(dateValueInPastDays("CREATED", daysAge)).append(" ");
	        sql.append("and not exists (select 1 from attribute_value where card_id = c.card_id and attribute_code = 'JBR_ARM_HIDE' and value_id = 1449) ");
	        sql.append(" ) ");
	        if(count) {
	        	sql.append( "select count(*), " ).append( howFastColumn() );
	        } else {
	        	sql.append( "select c.CARD_ID, c.TEMPLATE_ID, c.STATUS_ID " );
	        }
	        sql.append("from c where ");
	        sql.append(userPermissionCheck( userId, permissionTypes ) );
	        sql.append(simpleSearchFilter(simpleSearchFilter));

	        if(count) {
	        	sql.append(groupByHowFast());
	        } else {
	        	//sql.append(orderBy(sortAttributes));
	        	sql.append( getOrderByClauseWithColumnsList( sortAttributes, true ) );
	        	sql.append(limitAndOffset(page, pageSize));
	        }

	        return sql;
	}

	public static StringBuilder generateTempTable(ArrayList<TemplateParams> params, String tableName, int userId) {
		StringBuilder sbSubTable = new StringBuilder("\n");
		StringBuilder sbMainTempTable = new StringBuilder(tableName).append(" AS (\n");
		
		for (int i = 0; i < params.size(); i++) {
			TemplateParams tp = params.get(i); 
			if (tp.templateId != null) {
				String subTemplateTableName = "t_" + tp.templateId + "_docs";
				sbSubTable.append(subTemplateTableName).append(" AS (\n");
				sbSubTable.append("\tSELECT c2.card_id FROM  attribute_value av_executor\n");
				sbSubTable.append("\tJOIN card c2 ON (c2.card_id = av_executor.card_id AND c2.template_id = ").append(tp.templateId).append
					(" AND c2.status_id IN (").append(tp.statusIds).append("))\n");
				sbSubTable.append("\tWHERE av_executor.attribute_code IN ('").append(tp.userAttr)
					.append("') AND av_executor.number_value = ").append(userId).append("\n");
				sbSubTable.append("), \n");

				sbMainTempTable.append("--add template ").append(tp.templateId).append("\n");
				sbMainTempTable.append("SELECT DISTINCT av_basecard.number_value FROM attribute_value av_basecard\n");
				sbMainTempTable.append("INNER JOIN attribute a ON (a.attribute_code IN (").append(tp.linksToBaseDoc).append(") AND a.data_type <> 'B')\n");
				sbMainTempTable.append("WHERE av_basecard.card_id IN (SELECT tt.card_id FROM ").append(subTemplateTableName).append(" AS tt) AND av_basecard.attribute_code IN (").append(tp.linksToBaseDoc).append(")\n");

				sbMainTempTable.append("UNION\n");

				sbMainTempTable.append("SELECT functionbacklink(ccc.card_id, o1.option_value, o2.option_value) AS number_value FROM card ccc\n");
				sbMainTempTable.append("   INNER JOIN template_block tb ON tb.template_id = ccc.template_id\n");
				sbMainTempTable.append("   INNER JOIN attribute a ON a.block_code=tb.block_code AND a.data_type='B'\n");
				sbMainTempTable.append("      AND a.attribute_code IN (").append(tp.linksToBaseDoc).append(")\n");
				sbMainTempTable.append("   LEFT JOIN attribute_option o1 ON a.attribute_code = o1.attribute_code\n");
				sbMainTempTable.append("      AND o1.option_code = 'UPLINK'\n");
				sbMainTempTable.append("   LEFT JOIN attribute_option o2 ON a.attribute_code = o2.attribute_code\n");
				sbMainTempTable.append("      AND o2.option_code = 'LINK'\n");
				sbMainTempTable.append("   WHERE ccc.card_id IN (SELECT tt.card_id FROM ").append(subTemplateTableName).append(" AS tt)\n");

				if (i < params.size() - 1) {
					sbMainTempTable.append("\nUNION\n");
				}
			}
		}

		return sbSubTable.append(sbMainTempTable.append(")"));
	}
}
