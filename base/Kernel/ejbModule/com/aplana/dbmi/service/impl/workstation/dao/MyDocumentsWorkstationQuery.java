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
import java.util.List;

import static com.aplana.dbmi.service.impl.workstation.Util.*;

public class MyDocumentsWorkstationQuery extends JdbcDaoSupport {

	public static class AllFolderSupQuery extends JdbcDaoSupport implements AreaWorkstationQueryInterface {

		public List<EmptyCard> getCards(int userId, long[] permissionTypes,
				int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			StringBuilder sql = buildAllSupervisorQuery(false,
					userId, permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter);
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard(rs.getLong(1), rs.getLong(2), rs.getLong(3));
						}
					});
		}

		public List getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildAllSupervisorQuery(true, userId, permissionTypes, 0, 0, null, simpleSearchFilter);
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

	public static class ForReworkFolderSupQuery extends JdbcDaoSupport  implements AreaWorkstationQueryInterface {

		public List<EmptyCard> getCards(int userId, long[] permissionTypes,
				int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			StringBuilder sql = buildSupervisorForReworkQuery(false,
					userId, permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter);
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard(rs.getLong(1), rs.getLong(2), rs.getLong(3));
						}
					});
		}

		public List getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildSupervisorForReworkQuery(true, userId, permissionTypes, 0, 0, null,
					simpleSearchFilter);
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

	private static StringBuilder buildAllSupervisorQuery( boolean count, int userId, long[] permissionTypes,
	        int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
        StringBuilder sql = new StringBuilder(); 
        sql.append(Util.userPermissionCheckWithClause(userId));
        sql.append( /*with*/", c as  ");
        sql.append( "( select c.CARD_ID, c.TEMPLATE_ID, c.STATUS_ID, t.template_name_rus, c.is_active " ).append(getOrderByColumnsList(sortAttributes));
        sql.append("from card c ");
        sql.append("inner join template t on (t.template_id=c.template_id) " );
        sql.append("where c.template_id in (364, 764, 784, 1255, 1226) ");
        sql.append(     "and c.status_id in (1, 101, 102, 103, 104, 106, 107, 108, 200, 206, 48909) ");
        sql.append(     "and (").append(numericValueCondition("JBR_INFD_EXECUTOR", userId)).append(" or ").append(numericValueCondition("JBR_INFD_SGNEX_LINK", userId)).append(") ");
        sql.append( 	"and not exists (select 1 from attribute_value where card_id = c.card_id and attribute_code = 'JBR_ARM_HIDE' and value_id = 1449) ");
        sql.append(" ) ");
        if(count) {
        	sql.append( "select count(*), " ).append( howFastColumn() );
        } else {
        	sql.append( "select c.CARD_ID, c.TEMPLATE_ID, c.STATUS_ID, c.template_name_rus " );
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
	
	private static StringBuilder buildAgeSupervisorQuery( boolean count, int userId, long[] permissionTypes,
        int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter, int daysAge ) {
        StringBuilder sql = new StringBuilder(); 
        sql.append(Util.userPermissionCheckWithClause(userId));
        sql.append( /*with*/", c as  ");
        sql.append( "( select c.CARD_ID, c.TEMPLATE_ID, c.STATUS_ID, t.template_name_rus, c.is_active " ).append(getOrderByColumnsList(sortAttributes));
        sql.append("from card c ");
        sql.append("inner join template t on (t.template_id=c.template_id) " );
        sql.append("join attribute_value av_cr on av_cr.card_id = c.card_id and av_cr.attribute_code = 'CREATED' and av_cr.date_value > date_trunc('day', now() - INTERVAL '").append(daysAge).append(" days') "); 
        sql.append("where c.template_id in (364, 764, 784, 1255, 1226) ");
        sql.append(     "and c.status_id in (1, 101, 102, 103, 104, 106, 107, 108, 200, 206, 48909) ");
        sql.append(     "and (").append(numericValueCondition("JBR_INFD_EXECUTOR", userId)).append(" or ").append(numericValueCondition("JBR_INFD_SGNEX_LINK", userId)).append(") ");
        sql.append( 	"and not exists (select 1 from attribute_value where card_id = c.card_id and attribute_code = 'JBR_ARM_HIDE' and value_id = 1449) ");
        sql.append(" ) ");
        if(count) {
        	sql.append( "select count(*), " ).append( howFastColumn() );
        } else {
        	sql.append( "select c.CARD_ID, c.TEMPLATE_ID, c.STATUS_ID, c.template_name_rus " );
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

	private static StringBuilder buildSupervisorForReworkQuery(boolean count, int userId, long[] permissionTypes,
    		int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
        StringBuilder sql = new StringBuilder();
        sql.append(Util.userPermissionCheckWithClause(userId));
        if(count) {
        	sql.append( "select count(*), " ).append( howFastColumn() );
        } else {
        	sql.append( "select c.CARD_ID, c.TEMPLATE_ID, c.STATUS_ID " );
        }

        sql.append("from card c ");
        sql.append("where c.template_id in (364, 764, 784, 1226) ");
        sql.append(     "and c.status_id = 106 ");
        sql.append(     "and ").append(numericValueCondition("JBR_INFD_EXECUTOR", userId)).append(" ");
        sql.append(     "and ( ");
        sql.append(          "exists( ");
        sql.append(             "select avRepeat.number_value ");
        sql.append(             "from attribute_value avRepeat ");
        sql.append(             "where avRepeat.card_id = c.card_id ");
        sql.append(                 "and avRepeat.attribute_code in ('JBR_RPT_000000106', 'JBR_RPT_000000001') ");
        sql.append(                 "and avRepeat.number_value > 0 ");
        sql.append(          ") or exists ( ");
        sql.append(             "select 1 from card_version cv where cv.card_id = c.card_id and cv.status_id = 107 ");
        sql.append(          ")) ");
        sql.append( 	"and not exists (select 1 from attribute_value where card_id = c.card_id and attribute_code = 'JBR_ARM_HIDE' and value_id = 1449) " );
        sql.append("and ").append(userPermissionCheck( userId, permissionTypes ) );
        sql.append(simpleSearchFilter(simpleSearchFilter));

        if(count) {
        	sql.append(groupByHowFast());
        } else {
        	sql.append(orderBy(sortAttributes));
        	sql.append(limitAndOffset(page, pageSize));
        }
        return sql;
    }

}
