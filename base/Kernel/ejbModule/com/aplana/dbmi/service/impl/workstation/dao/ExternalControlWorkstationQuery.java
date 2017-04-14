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

public class ExternalControlWorkstationQuery extends JdbcDaoSupport {
	
	public static class AllFolderSupQuery extends JdbcDaoSupport implements AreaWorkstationQueryInterface {
		/**
		 * ������������/�������� folderControl.xml
		 * 
		 * @param userId
		 * @param permissionTypes
		 * @param page	
		 * @param pageSize
		 * @return
		 */
		public List getCards(int userId, long[] permissionTypes,
				int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			
			StringBuilder sql = buildSupervisorQuery(false, userId,
					permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter);
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard ( rs.getLong( 1 ), rs.getLong( 2 ) );
						}
					});
		}
		
		/**
		 * ������������/��������
		 * 
		 * @param userId
		 * @param permissionTypes
		 * @return
		 */
		public List getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildSupervisorQuery(true, userId, permissionTypes, 0, 0, null, simpleSearchFilter);
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
	
	public static class DeadlineFolderSupQuery extends DeadlineSupportWorkstationQuery implements AreaWorkstationQueryInterface {
		
		public List<EmptyCard> getCards(int userId, long[] permissionTypes,
				int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			StringBuilder sql = buildSupervisorDeadlineQuery(false,
					userId, permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter, getDeadline());
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard(rs.getLong(1), rs.getLong(2));
						}
					});
		}

		public List getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildSupervisorDeadlineQuery(true, userId, permissionTypes, 0, 0, null, 
					simpleSearchFilter, getDeadline());
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
        	sql.append( "select c.CARD_ID, c.TEMPLATE_ID " );
        }
        
        sql.append( " from card c where c.card_id in " );
        
        //� ��������� ��������� ���������� �������� ��������� � ������� ����������, ����������� ������� �������� �������� ���.�
        sql.append(" (SELECT DISTINCT avMain.number_value card_id ");
        sql.append("      FROM card cp INNER JOIN attribute_value vp ON (vp.card_id = cp.card_id) ");
        sql.append("	  INNER JOIN attribute_value avMain ON (avMain.card_id = cp.card_id ) " );
        sql.append("      WHERE cp.template_id = 324 ");
        sql.append("		AND cp.status_id = 103 ");
        sql.append("		AND vp.attribute_code = 'JBR_INFD_SGNEX_LINK' ");
        sql.append("		AND vp.number_value = ").append(userId).append(" ");
        sql.append("		AND avMain.attribute_code = 'JBR_MAINDOC' " );
        sql.append("		AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cp.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) ");
        
        sql.append( "union " );
        // ���� �� - �� � ������� ����������, ����������� ������� �������� �������� ���.�
        sql.append(" SELECT DISTINCT cp.card_id as card_id ");
        sql.append("      FROM card cp INNER JOIN attribute_value vp ON (vp.card_id = cp.card_id) ");
        sql.append("      WHERE cp.template_id = 1255 ");
        sql.append("		AND cp.status_id = 103 ");
        sql.append("		AND vp.attribute_code = 'JBR_INFD_SGNEX_LINK' ");
        sql.append("		AND vp.number_value = ").append(userId).append(" ");
        sql.append("		AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cp.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) ");
        
        sql.append(")");

        //�������� ��������� ��������� � ����� �� ��������� ��������: ����������/��������/����� � �������� � ����/�� ������������ � ���-��/������������
        sql.append( " and " ).append( "c.status_id in(103,206,48909,10000120,107) ");
        
        sql.append( " and " ).append( userPermissionCheck( userId, permissionTypes ) );
        sql.append(simpleSearchFilter(simpleSearchFilter));
        
        if(count) {
        	sql.append( groupByHowFast() );
        } else {
        	sql.append( orderBy( sortAttributes ) );
        	sql.append( limitAndOffset( page, pageSize ) );
        }
        
        return sql;
    }
	
	private static StringBuilder buildSupervisorDeadlineQuery(boolean count, int userId, long[] permissionTypes, 
    		int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter, int deadline) {
        StringBuilder sql = new StringBuilder();
        sql.append(Util.userPermissionCheckWithClause(userId));
        if(count) {
        	sql.append( "select count(*), " ).append( howFastColumn() );
        } else {
        	sql.append( "select c.CARD_ID, c.TEMPLATE_ID " );
        }
        //� ��������� ��������� ���������� �������� ��������� � ������� ����������, ����������� ������� �������� �������� ���.�
        sql.append(" from card c inner join attribute_value avDeadLine on avDeadLine.card_id = c.card_id ");
        sql.append(" where c.card_id in " );
        sql.append(" (SELECT DISTINCT avMain.number_value card_id ");
        sql.append("   FROM card cp INNER JOIN attribute_value vp ON (vp.card_id = cp.card_id) ");
        sql.append("   INNER JOIN attribute_value avMain ON (avMain.card_id = cp.card_id ) " );
        sql.append("   WHERE cp.template_id = 324 ");
        sql.append("   		AND cp.status_id = 103 ");
        sql.append("        AND vp.attribute_code = 'JBR_INFD_SGNEX_LINK' ");
        sql.append("        AND vp.number_value = ").append(userId).append(" ");
        sql.append("		AND avMain.attribute_code = 'JBR_MAINDOC' " );
        sql.append("		AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cp.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) ");
        sql.append(")");
        //�������� ��������� ��������� � ����� �� ��������� ��������: ����������/��������/����� � �������� � ����/�� ������������ � ���-��/������������
        sql.append( " and " ).append( "c.status_id in(103,206,48909,10000120,107) ");
        
        sql.append( "and avDeadLine.attribute_code = 'JBR_IMPL_DEADLINE' " );
        if(deadline == 0) {
        	sql.append( "and avDeadLine.date_value <= now() " );
        } else {
        	sql.append( "and avDeadLine.date_value > now() ");
        	sql.append( "and avDeadLine.date_value < date_trunc('day', now() + INTERVAL '").append(deadline + 1).append(" days') " );
        }
        
        sql.append( " and " ).append( userPermissionCheck( userId, permissionTypes ) );
        sql.append(simpleSearchFilter(simpleSearchFilter));
        
        if(count) {
        	sql.append( groupByHowFast() );
        } else {
        	sql.append( orderBy( sortAttributes ) );
        	sql.append( limitAndOffset( page, pageSize ) );
        }
        
        return sql;
    }

}
