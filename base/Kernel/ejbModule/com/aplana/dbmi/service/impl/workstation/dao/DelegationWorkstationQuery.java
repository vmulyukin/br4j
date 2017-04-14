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
import com.aplana.dbmi.service.impl.query.GetUserViewsFromSameDepartament;
import com.aplana.dbmi.service.impl.workstation.EmptyCard;
import com.aplana.dbmi.service.impl.workstation.Util;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.aplana.dbmi.service.impl.workstation.Util.*;

public class DelegationWorkstationQuery extends JdbcDaoSupport{

    public static class AllFolderSupQuery extends JdbcDaoSupport implements AreaWorkstationQueryInterface {

		public List getCards(int userId, long[] permissionTypes, int page,
				int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {

			StringBuilder sql = buildQuery(false, userId,
					permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter);
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard(rs.getLong(1), rs.getLong(2));
						}
					});
		}

		public List getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildQuery(true, userId,
					permissionTypes, 0, 0, null, simpleSearchFilter);
			List queryResult = executeSimpleQuery(getJdbcTemplate(), sql
					.toString(), new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					return new Long[] { rs.getLong(1), rs.getLong(2) };
				}
			});
			return queryResult;
		}

        private static StringBuilder buildQuery( boolean count, int userId, long[] permissionTypes,
			int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter ) {
            StringBuilder sql = new StringBuilder();
            sql.append(Util.userPermissionCheckWithClause(userId));
            if(count) {
                sql.append( "select count(*), " ).append( howFastColumn() );
            } else {
                sql.append( "select c.CARD_ID, c.TEMPLATE_ID, c.STATUS_ID " );
            }

            sql.append("from card c ");
            sql.append("where c.template_id = 2290 ");
            sql.append(     "and ((");
            sql.append(             numericValueCondition("DLGT_FROM", GetUserViewsFromSameDepartament.getPersonIdsQuery((long) userId))).append(" ");
            sql.append(             "and c.status_id in (67424, 67425)) ");
            sql.append(			  "and not exists (select 1 from ATTRIBUTE_VALUE av where av.ATTRIBUTE_CODE = 'DLGT_TO' and av.NUMBER_VALUE = " + userId);
            sql.append(								" and av.CARD_ID = c.CARD_ID and c.status_id <> 67425) ");
            sql.append(           "or (").append(numericValueCondition("DLGT_TO", userId)).append(" ");
            sql.append(             "and c.status_id = 67425) ");
            sql.append(          ") ");
            sql.append("and ").append(userPermissionCheck( userId, permissionTypes ) );
            sql.append(simpleSearchFilter(simpleSearchFilter));

            if(count) {
                sql.append(groupByHowFast());
            } else {
                sql.append(customOrderBy(sortAttributes));
                sql.append(limitAndOffset(page, pageSize));
            }

            return sql;
        }

        private static StringBuilder customOrderBy(List<SortAttribute> sortAttributes) {
            if(null == sortAttributes || sortAttributes.size() != 1 ||
                    !"CREATED".equals(sortAttributes.get(0).getCode())) {
                return orderBy( sortAttributes );
            }

            // Overriding with Custom ordering when sorting by created
            StringBuilder orderClause = new StringBuilder("order by (");

            orderClause.append("case when ");
            orderClause.append(     "(not exists (select date_value from attribute_value avOrder1 where avOrder1.card_id = c.card_id and avOrder1.attribute_code = 'DLGT_DATE_START') ");
            orderClause.append(     "or (select date_value from attribute_value avOrder1 where avOrder1.card_id = c.card_id and avOrder1.attribute_code = 'DLGT_DATE_START') <= now()) ");
            orderClause.append(     "and (not exists (select date_value from attribute_value avOrder1 where avOrder1.card_id = c.card_id and avOrder1.attribute_code = 'DLGT_DATE_END') ");
            orderClause.append(     "or (select date_value from attribute_value avOrder1 where avOrder1.card_id = c.card_id and avOrder1.attribute_code = 'DLGT_DATE_END') >= now()) ");
            orderClause.append("then 1 else 0 end) ");
            if(sortAttributes.get(0).isAsc()) {
                orderClause.append("asc, ");
            } else {
                orderClause.append("desc, ");
            }

            orderClause.append("(select date_value from attribute_value avOrder2 ");
            orderClause.append("where avOrder2.card_id = c.card_id and avOrder2.attribute_code = 'CREATED') ");

            if(sortAttributes.get(0).isAsc()) {
                orderClause.append("asc ");
            } else {
                orderClause.append("desc ");
            }

            return orderClause;
        }
	}

    public static class MyDelegationsFolderSupQuery extends JdbcDaoSupport implements AreaWorkstationQueryInterface {

		public List getCards(int userId, long[] permissionTypes, int page,
				int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {

			StringBuilder sql = buildQuery(false, userId,
					permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter);
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard(rs.getLong(1), rs.getLong(2));
						}
					});
		}

		public List getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildQuery(true, userId,
					permissionTypes, 0, 0, null, simpleSearchFilter);
			List queryResult = executeSimpleQuery(getJdbcTemplate(), sql
					.toString(), new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					return new Long[] { rs.getLong(1), rs.getLong(2) };
				}
			});
			return queryResult;
		}

        private static StringBuilder buildQuery( boolean count, int userId, long[] permissionTypes,
			int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter ) {
            StringBuilder sql = new StringBuilder();
            sql.append(Util.userPermissionCheckWithClause(userId));
            if(count) {
                sql.append( "select count(*), " ).append( howFastColumn() );
            } else {
                sql.append( "select c.CARD_ID, c.TEMPLATE_ID, c.STATUS_ID " );
            }

            sql.append("from card c ");
            sql.append("where c.template_id = 2290 and c.status_id in (67424, 67425) ");
            sql.append(     "and ( ").append(numericValueCondition(
            		"DLGT_FROM",  
            		
            		"select p.person_id from person p where (exists ( "+
						"SELECT 1 FROM person p_o JOIN person_role pr ON (pr.person_id = p_o.person_id AND pr.role_code IN ('A', 'JBR_DELEGATE_MGR') ) "+
						"WHERE p_o.person_id = "+userId+
					") or p.person_id = "+userId+")"
					
            		));
            
            sql.append(" or ").append(numericValueCondition(
            		"AUTHOR",  
            		
            		"select p.person_id from person p where (exists ( "+
						"SELECT 1 FROM person p_o JOIN person_role pr ON (pr.person_id = p_o.person_id AND pr.role_code IN ('A', 'JBR_DELEGATE_MGR') ) "+
						"WHERE p_o.person_id = "+userId+
					") or p.person_id = "+userId+")"
					
            		));
            sql.append(" ) ");
            		
            //sql.append(     "and (not exists (select date_value from attribute_value avOrder1 where avOrder1.card_id = c.card_id and avOrder1.attribute_code = 'DLGT_DATE_START') ");
            //sql.append(          "or (select date_value from attribute_value avOrder1 where avOrder1.card_id = c.card_id and avOrder1.attribute_code = 'DLGT_DATE_START') <= now()) ");
            sql.append(     "and (not exists (select date_value from attribute_value avOrder1 where avOrder1.card_id = c.card_id and avOrder1.attribute_code = 'DLGT_DATE_END') ");
            sql.append(          "or (select date_value from attribute_value avOrder1 where avOrder1.card_id = c.card_id and avOrder1.attribute_code = 'DLGT_DATE_END') >= now()) ");
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

    public static class DelegatedToMeFolderSupQuery extends JdbcDaoSupport implements AreaWorkstationQueryInterface {

		public List getCards(int userId, long[] permissionTypes, int page,
				int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {

			StringBuilder sql = buildQuery(false, userId,
					permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter);
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard(rs.getLong(1), rs.getLong(2));
						}
					});
		}

		public List getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildQuery(true, userId,
					permissionTypes, 0, 0, null, simpleSearchFilter);
			List queryResult = executeSimpleQuery(getJdbcTemplate(), sql
					.toString(), new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					return new Long[] { rs.getLong(1), rs.getLong(2) };
				}
			});
			return queryResult;
		}

        private static StringBuilder buildQuery( boolean count, int userId, long[] permissionTypes,
			int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter ) {
            StringBuilder sql = new StringBuilder();
            sql.append(Util.userPermissionCheckWithClause(userId));
            if(count) {
                sql.append( "select count(*), " ).append( howFastColumn() );
            } else {
                sql.append( "select c.CARD_ID, c.TEMPLATE_ID, c.STATUS_ID " );
            }

            sql.append("from card c ");
            sql.append("where c.template_id = 2290 and c.status_id in (67424, 67425) ");
            sql.append(     "and ").append(numericValueCondition("DLGT_TO", userId)).append(" ");
            sql.append(     "and (not exists (select date_value from attribute_value avOrder1 where avOrder1.card_id = c.card_id and avOrder1.attribute_code = 'DLGT_DATE_START') ");
            sql.append(          "or (select date_value from attribute_value avOrder1 where avOrder1.card_id = c.card_id and avOrder1.attribute_code = 'DLGT_DATE_START') <= now()) ");
            sql.append(     "and (not exists (select date_value from attribute_value avOrder1 where avOrder1.card_id = c.card_id and avOrder1.attribute_code = 'DLGT_DATE_END') ");
            sql.append(          "or (select date_value from attribute_value avOrder1 where avOrder1.card_id = c.card_id and avOrder1.attribute_code = 'DLGT_DATE_END') >= now()) ");      
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

    public static class DelegationHistoryFolderSupQuery extends JdbcDaoSupport implements AreaWorkstationQueryInterface {

		public List getCards(int userId, long[] permissionTypes, int page,
				int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {

			StringBuilder sql = buildQuery(false, userId,
					permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter);
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard(rs.getLong(1), rs.getLong(2));
						}
					});
		}

		public List getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildQuery(true, userId,
					permissionTypes, 0, 0, null, simpleSearchFilter);
			List queryResult = executeSimpleQuery(getJdbcTemplate(), sql
					.toString(), new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					return new Long[] { rs.getLong(1), rs.getLong(2) };
				}
			});
			return queryResult;
		}

        private static StringBuilder buildQuery( boolean count, int userId, long[] permissionTypes,
			int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter ) {
            StringBuilder sql = new StringBuilder();
            sql.append(Util.userPermissionCheckWithClause(userId));
            if(count) {
                sql.append( "select count(*), " ).append( howFastColumn() );
            } else {
                sql.append( "select c.CARD_ID, c.TEMPLATE_ID, c.STATUS_ID " );
            }

            sql.append("from card c ");
            sql.append("where c.template_id = 2290 ");
            sql.append(     "and ((");
            sql.append(             numericValueCondition("DLGT_FROM", GetUserViewsFromSameDepartament.getPersonIdsQuery((long) userId))).append(" ");
            sql.append(             "and c.status_id in (67424, 67425)) ");
            sql.append(           "or (").append(numericValueCondition("DLGT_TO", userId)).append(" ");
            sql.append(             "and c.status_id = 67425) ");
            sql.append(     ") and (");
            sql.append(             dateValueGreaterNow("DLGT_DATE_START")).append(" ");
            sql.append(             "or ").append(dateValueLessNow("DLGT_DATE_END")).append(" ");
            sql.append(     ") and ").append(userPermissionCheck( userId, permissionTypes ) );
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
}
