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

public class SignUrgentlyWorkstationQuery extends JdbcDaoSupport {

	public static class AllFolderSupQuery extends JdbcDaoSupport implements AreaWorkstationQueryInterface {

		/**
		 * ������������/��������� folderSign_2_all.xml
		 * 
		 * @param userId
		 * @param permissionTypes
		 * @param page
		 * @param pageSize
		 * @return
		 */
		public List getCards(int userId, long[] permissionTypes, int page,
				int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			StringBuilder sql = buildSupervisorQuery(false, userId,
					permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter);
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard(rs.getLong(1), rs.getLong(2), rs.getLong(3));
						}
					});
		}

		/**
		 * ������������/��������� - ���-��
		 * 
		 * @param userId
		 * @param permissionTypes
		 * @return
		 */
		public List getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildSupervisorQuery(true, userId,
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
	}

	
	public static class ForCoordinationFolderSupQuery extends
			JdbcDaoSupport implements AreaWorkstationQueryInterface {

		/**
		 * ������������/���������/�� ������������ folderAgree_2.xml.xml
		 * 
		 * @param userId
		 * @param permissionTypes
		 * @param page
		 * @param pageSize
		 * @return
		 */
		public List getCards(int userId, long[] permissionTypes, int page,
				int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			StringBuilder sql = buildSupervisorForCoordinationQuery(false,
					userId, permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter);
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard(rs.getLong(1), rs.getLong(2), rs.getLong(3));
						}
					});
		}

		/**
		 * ������������/���������/�� ������������ - ���-��
		 * 
		 * @param userId
		 * @param permissionTypes
		 * @return
		 */
		public List getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildSupervisorForCoordinationQuery(true,
					userId, permissionTypes, 0, 0, null, simpleSearchFilter);
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

	public static class ForSignFolderSupQuery extends
			JdbcDaoSupport implements AreaWorkstationQueryInterface {

		/**
		 * ������������/���������/�� ������� folderSign_2.xml
		 * 
		 * @param userId
		 * @param permissionTypes
		 * @param page
		 * @param pageSize
		 * @return
		 */
		public List getCards(int userId, long[] permissionTypes, int page,
				int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			StringBuilder sql = buildSupervisorForSigningQuery(false, userId,
					permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter);
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard(rs.getLong(1), rs.getLong(2), rs.getLong(3));
						}
					});
		}

		/**
		 * ������������/���������/�� �������
		 * 
		 * @param userId
		 * @param permissionTypes
		 * @return
		 */
		public List getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildSupervisorForSigningQuery(true, userId,
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
	}


    private static StringBuilder buildSupervisorQuery(boolean count, int userId, long[] permissionTypes, 
    		int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
        StringBuilder sql = new StringBuilder();
        sql.append(Util.userPermissionCheckWithClause(userId));
        if(count) {
        	sql.append( "select count(*), " ).append( howFastColumn() );
        } else {
        	sql.append( "select c.CARD_ID, c.TEMPLATE_ID, c.STATUS_ID " );
        }

        sql.append("from (SELECT c.card_id, c.template_id, c.status_id ");
        sql.append("  FROM card c INNER JOIN attribute_value vlink ON (vlink.card_id = c.card_id) ");
        sql.append("       INNER JOIN card clink ON (clink.card_id = vlink.number_value) ");
        sql.append("       INNER JOIN attribute_value vuser ON (vuser.card_id = clink.card_id) ");
        sql.append("		inner join attribute_value vUrg on (vUrg.card_id = cLink.card_id) ");
        sql.append(" WHERE c.status_id IN (107, 108, 6833780) ");
        sql.append("   AND vlink.attribute_code IN ('JBR_VISA_VISA_HIDDEN', 'JBR_SIGN_SIGNING') ");
        sql.append("   AND clink.status_id IN (107, 108, 6833780) ");
        sql.append("	and vUrg.attribute_code = 'JBR_HOWFAST' " );
        sql.append("	and vUrg.value_id is not null ");     	
        sql.append("   AND vuser.attribute_code IN ('JBR_VISA_RESPONSIBLE', 'JBR_SIGN_RESPONSIBLE') ");
        sql.append("   AND vuser.number_value = ").append(userId);
        sql.append("   AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = c.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) ");
        sql.append(") c inner join template t on (t.template_id=c.template_id)");
        sql.append(" WHERE " ).append( userPermissionCheck( userId, permissionTypes ) );
        sql.append(simpleSearchFilter(simpleSearchFilter));
        
        if(count) {
        	sql.append( groupByHowFast() );
        } else {
        	sql.append( supervisorOrderByTemplateName ( sortAttributes ) );
        	sql.append( limitAndOffset( page, pageSize ) );
        }
        
        return sql;
    }

    private static StringBuilder buildSupervisorForCoordinationQuery(boolean count, int userId, long[] permissionTypes, 
    		int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
        StringBuilder sql = new StringBuilder();
        sql.append(Util.userPermissionCheckWithClause(userId));
        if(count) {
        	 sql.append( "select count(*), " ).append( howFastColumn() );
        } else {
        	 sql.append( "select c.CARD_ID, c.TEMPLATE_ID, c.STATUS_ID " );
        }
        
        sql.append( "from (SELECT c.card_id, c.template_id, c.status_id ");
        sql.append("  FROM card c INNER JOIN attribute_value vlink ON (vlink.card_id = c.card_id) ");
        sql.append("       INNER JOIN card clink ON (clink.card_id = vlink.number_value) ");
        sql.append("       INNER JOIN attribute_value vuser ON (vuser.card_id = clink.card_id) ");
        sql.append("		inner join attribute_value vUrg on (vUrg.card_id = cLink.card_id) ");
        sql.append(" WHERE c.status_id in (107, 6833780) ");
        sql.append("   AND vlink.attribute_code = 'JBR_VISA_VISA_HIDDEN' ");
        sql.append("   AND clink.status_id in (107, 6833780) ");
        sql.append("	and vUrg.attribute_code = 'JBR_HOWFAST' " );
        sql.append("	and vUrg.value_id is not null ");     	
        sql.append("   AND vuser.attribute_code = 'JBR_VISA_RESPONSIBLE' ");
        sql.append("   AND vuser.number_value = ").append(userId);
        sql.append("   AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = c.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) ");
        sql.append(") c inner join template t on (t.template_id=c.template_id)" );
        sql.append( " WHERE " ).append( userPermissionCheck( userId, permissionTypes ) );
        sql.append(simpleSearchFilter(simpleSearchFilter));
        
        if(count) {
        	sql.append( groupByHowFast() );
        } else {
        	sql.append( supervisorOrderByTemplateName( sortAttributes ) );
        	sql.append( limitAndOffset( page, pageSize ) );
        }
        
        return sql;
    }

    private static StringBuilder buildSupervisorForSigningQuery(boolean count, int userId, long[] permissionTypes, 
    		int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
        StringBuilder sql = new StringBuilder();
        sql.append(Util.userPermissionCheckWithClause(userId));
        if(count) {
        	sql.append( "select count(*), " ).append( howFastColumn() );
        } else {
        	sql.append( "select c.CARD_ID, c.TEMPLATE_ID, c.STATUS_ID " );
        }
        
        sql.append("from (SELECT c.card_id, c.template_id, c.status_id ");
        sql.append("  FROM card c INNER JOIN attribute_value vlink ON (vlink.card_id = c.card_id) ");
        sql.append("       INNER JOIN card clink ON (clink.card_id = vlink.number_value) ");
        sql.append("       INNER JOIN attribute_value vuser ON (vuser.card_id = clink.card_id) ");
        sql.append("		inner join attribute_value vUrg on (vUrg.card_id = cLink.card_id) ");
        sql.append(" WHERE c.status_id = 108 ");
        sql.append("   AND vlink.attribute_code = 'JBR_SIGN_SIGNING' ");
        sql.append("   AND clink.status_id = 108 ");
        sql.append("	and vUrg.attribute_code = 'JBR_HOWFAST' " );
        sql.append("	and vUrg.value_id is not null ");     	
        sql.append("   AND vuser.attribute_code = 'JBR_SIGN_RESPONSIBLE' ");
        sql.append("   AND vuser.number_value = ").append(userId);
        sql.append("   AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = c.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) ");
        sql.append(") c inner join template t on (t.template_id=c.template_id)" );
        sql.append(" WHERE " ).append( userPermissionCheck( userId, permissionTypes ) );
        sql.append(simpleSearchFilter(simpleSearchFilter));
        
        if(count) {
        	sql.append( groupByHowFast() );
        } else {
        	sql.append( supervisorOrderByTemplateName( sortAttributes ) );
        	sql.append( limitAndOffset( page, pageSize ) );
        }
        
        return sql;
    }
    
    private static StringBuilder supervisorOrderBy(List<SortAttribute> sortAttributes) {
    	return supervisorOrderBy(sortAttributes, false);
    }
    
    private static StringBuilder supervisorOrderByTemplateName(List<SortAttribute> sortAttributes) {
    	return supervisorOrderBy(sortAttributes, true);
    }
    
    private static StringBuilder supervisorOrderBy(List<SortAttribute> sortAttributes, boolean byTemplateName) {
    	if(null == sortAttributes || sortAttributes.size() != 1 || 
    			!"JBR_INFD_RECEIVER".equals(sortAttributes.get(0).getCode())) {
    		if(byTemplateName) {
    			return orderByTemplateName ( sortAttributes );
    		} else return orderBy( sortAttributes );
    	}
    	
    	// Overriding with Custom ordering when sorting by receiver
		StringBuilder orderClause = new StringBuilder("order by (");
		
		orderClause.append("case when c.template_id = 364 then ");
		orderClause.append("(select avLinkedOrder1.string_value from attribute_value avLinkedOrder1 ");
		orderClause.append("inner join attribute_value avOrder1 on avLinkedOrder1.card_id = avOrder1.number_value ");
		orderClause.append("where avOrder1.card_id = c.card_id and avOrder1.attribute_code = 'JBR_RECEIVER_EXT_USR' ");
		orderClause.append("and avLinkedOrder1.attribute_code = 'NAME') ");
		
		orderClause.append("when c.template_id = 784 then ");
		orderClause.append("(select avLinkedOrder1.string_value from attribute_value avLinkedOrder1 ");
		orderClause.append("inner join person pOrder1 on pOrder1.card_id = avLinkedOrder1.card_id ");
		orderClause.append("inner join attribute_value avOrder1 on pOrder1.person_id = avOrder1.number_value ");
		orderClause.append("where avOrder1.card_id = c.card_id and avOrder1.attribute_code = 'JBR_INFD_RECEIVER' ");
		orderClause.append("and avLinkedOrder1.attribute_code = 'JBR_PERS_SNAME_NM') ");
		
		orderClause.append("when c.template_id = 764 then ");
		orderClause.append("(select avLinkedOrder1.string_value from attribute_value avLinkedOrder1 ");
		orderClause.append("inner join person pOrder1 on pOrder1.card_id = avLinkedOrder1.card_id ");
		orderClause.append("inner join attribute_value avOrder1 on pOrder1.person_id = avOrder1.number_value ");
		orderClause.append("where avOrder1.card_id = c.card_id and avOrder1.attribute_code = 'JBR_INFD_RECEIVER' ");
		orderClause.append("and avLinkedOrder1.attribute_code = 'JBR_PERS_SNAME_NM') ");
		
		orderClause.append("when c.template_id = 775 then ");
		orderClause.append("(select avLinkedOrder1.string_value from attribute_value avLinkedOrder1 ");
		orderClause.append("inner join person pOrder1 on pOrder1.card_id = avLinkedOrder1.card_id ");
		orderClause.append("inner join attribute_value avOrder1 on pOrder1.person_id = avOrder1.number_value ");
		orderClause.append("where avOrder1.card_id = c.card_id and avOrder1.attribute_code = 'JBR_INFD_RECEIVER' ");
		orderClause.append("and avLinkedOrder1.attribute_code = 'JBR_PERS_SNAME_NM') ");
		
		orderClause.append("when c.template_id = 777 then ");
		orderClause.append("(select avLinkedOrder1.string_value from attribute_value avLinkedOrder1 ");
		orderClause.append("inner join person pOrder1 on pOrder1.card_id = avLinkedOrder1.card_id ");
		orderClause.append("inner join attribute_value avOrder1 on pOrder1.person_id = avOrder1.number_value ");
		orderClause.append("where avOrder1.card_id = c.card_id and avOrder1.attribute_code = 'JBR_INFD_RECEIVER' ");
		orderClause.append("and avLinkedOrder1.attribute_code = 'JBR_PERS_SNAME_NM') ");
		
		orderClause.append("else null end) ");
		
		if(sortAttributes.get(0).isAsc()) {
			orderClause.append("asc ");
		} else {
			orderClause.append("desc ");
		}
		
		return orderClause;
    }

}