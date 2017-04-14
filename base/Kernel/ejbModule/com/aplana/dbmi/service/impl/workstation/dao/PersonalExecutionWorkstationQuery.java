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

public class PersonalExecutionWorkstationQuery extends JdbcDaoSupport {
	
	public static class AllFolderSupQuery extends JdbcDaoSupport implements AreaWorkstationQueryInterface {

		public List<EmptyCard> getCards(int userId, long[] permissionTypes, int page, int pageSize, 
				List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			StringBuilder sql = buildSupervisorQuery(false,
					userId, permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter);
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard(rs.getLong(1), rs.getLong(2));
						}
					});
		}
		
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
	
	public static class DeadlineFolderSupQuery extends DeadlineSupportWorkstationQuery 
							implements AreaWorkstationQueryInterface {
		
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

	private static StringBuilder buildSupervisorQuery( boolean count, int userId, long[] permissionTypes, 
    		int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter ) {
        StringBuilder sql = new StringBuilder();
        sql.append(Util.userPermissionCheckWithClause(userId));
        sql.append( /*with*/", t1 as ( ");
        sql.append( " select cp.card_id, cp.template_id " );
        sql.append( "from card cp " );
        sql.append( "inner join attribute_value vr on (vr.number_value = cp.card_id) " );
        sql.append( "inner join card cr on (cr.card_id = vr.card_id) " );
        sql.append( "inner join attribute_value ve on (ve.card_id = cr.card_id) " );
        sql.append( "where cp.template_id in(324,1255)  " );
        sql.append( "and vr.attribute_code ='ADMIN_702311' " );
        sql.append( "and cr.template_id = 1044 " );
        sql.append( "and cr.status_id = 702239 " );
        sql.append( "and ve.attribute_code = 'ADMIN_702335' " );
        sql.append( "and ve.number_value = " ).append( userId ).append( " " );
        sql.append( "and not exists (select 1 from attribute_value where card_id = cp.card_id and attribute_code = 'JBR_ARM_HIDE' and value_id = 1449) ");
        sql.append( ") ");
        
        if(count) {
        	sql.append( "select count(*), " ).append( howFastColumn() );
        } else {
        	sql.append( "select c.CARD_ID, c.TEMPLATE_ID " );
        }
        
        sql.append( "from card c where c.card_id in ( " );

        sql.append( "select distinct avMain.number_value " );
        sql.append( "from  t1 " );
        sql.append( "INNER JOIN attribute_value avMain ON avMain.card_id = t1.card_id AND avMain.attribute_code = 'JBR_MAINDOC' " );
        sql.append( "\n where not exists (select 1 from card cc ")
         	.append( "\n\t\t join attribute_value par on par.number_value = t1.card_id and par.attribute_code = 'JBR_RIMP_PARASSIG' and cc.card_id = par.card_id ")
        	.append( "\n\t\t join attribute_value avPar on avPar.card_id = par.card_id and avPar.attribute_code = 'JBR_INFD_SGNEX_LINK' and avPar.number_value = " )
        	.append( userId )
        	.append( "\n\t where cc.status_id = 103) ");
        
        sql.append( "UNION " );
        
        sql.append( "select t1.card_id from  t1 " );
        sql.append( "where t1.template_id = 1255 " );
        sql.append( "\n AND not exists (select 1 from card cc ")
     		.append( "\n\t\t join attribute_value par on par.number_value = t1.card_id and par.attribute_code = 'JBR_MAINDOC' and cc.card_id = par.card_id ")
     		.append( "\n\t\t join attribute_value avPar on avPar.card_id = par.card_id and avPar.attribute_code = 'JBR_INFD_SGNEX_LINK' and avPar.number_value = " )
     		.append( userId )
     		.append( "\n\t where cc.status_id = 103) ");
        
        sql.append( ") " );
        sql.append( "and " ).append( userPermissionCheck( userId, permissionTypes ) );
        sql.append(simpleSearchFilter(simpleSearchFilter));
        
        if(count) {
        	sql.append( groupByHowFast() );
        } else {
        	sql.append( orderBy( sortAttributes ) );
        	sql.append( limitAndOffset( page, pageSize ) );
        }

        return sql;
    }
	
	private static StringBuilder buildSupervisorDeadlineQuery( boolean count, int userId, long[] permissionTypes, 
    		int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter, int deadline ) {
        StringBuilder sql = new StringBuilder();
        sql.append(Util.userPermissionCheckWithClause(userId));
        if(count) {
        	sql.append( "select count(*), " ).append( howFastColumn() );
        } else {
        	sql.append( "select c.CARD_ID, c.TEMPLATE_ID " );
        }
        sql.append( "from card c inner join attribute_value avDeadLine on avDeadLine.card_id = c.card_id "); 
        sql.append( "where c.card_id in ( " );
        sql.append( "select distinct avMain.number_value " );
        sql.append( "from card cp " );
        sql.append( "inner join attribute_value vr on (vr.number_value = cp.card_id) " );
        sql.append( "inner join card cr on (cr.card_id = vr.card_id) " );
        sql.append( "inner join attribute_value ve on (ve.card_id = cr.card_id) " );
        sql.append( "INNER JOIN attribute_value avMain ON (avMain.card_id = cp.card_id ) " );
        sql.append( "where cp.template_id = 324 " );
        sql.append( "and vr.attribute_code ='ADMIN_702311' " );
        sql.append( "and cr.template_id = 1044 " );
        sql.append( "and cr.status_id = 702239 " );
        sql.append( "and ve.attribute_code = 'ADMIN_702335' " );
        sql.append( "and ve.number_value = " ).append( userId ).append( " " );
        sql.append( "AND avMain.attribute_code = 'JBR_MAINDOC' " );
        sql.append( "and not exists (select 1 from attribute_value where card_id = cp.card_id and attribute_code = 'JBR_ARM_HIDE' and value_id = 1449) ");
        
        sql.append( "\n and not exists (select 1 from card cc ")
     		.append( "\n\t\t join attribute_value par on par.number_value = cp.card_id and par.attribute_code = 'JBR_RIMP_PARASSIG' and cc.card_id = par.card_id ")
     		.append( "\n\t\t join attribute_value avPar on avPar.card_id = par.card_id and avPar.attribute_code = 'JBR_INFD_SGNEX_LINK' and avPar.number_value = " )
     		.append( userId )
     		.append( "\n\t where cc.status_id = 103) ");
        
        sql.append( " UNION " );
        sql.append( "select cp.card_id " );
        sql.append( "from card cp " );
        sql.append( "inner join attribute_value vr on (vr.number_value = cp.card_id) " );
        sql.append( "inner join card cr on (cr.card_id = vr.card_id) " );
        sql.append( "inner join attribute_value ve on (ve.card_id = cr.card_id) " );
        sql.append( "where cp.template_id = 1255 " );
        sql.append( "and vr.attribute_code ='ADMIN_702311' " );
        sql.append( "and cr.template_id = 1044 " );
        sql.append( "and cr.status_id = 702239 " );
        sql.append( "and ve.attribute_code = 'ADMIN_702335' " );
        sql.append( "and ve.number_value = " ).append( userId ).append( " " );
        sql.append( "and not exists (select 1 from attribute_value where card_id = cp.card_id and attribute_code = 'JBR_ARM_HIDE' and value_id = 1449) ");
        
        sql.append( "\n and not exists (select 1 from card cc ")
 			.append( "\n\t\t join attribute_value par on par.number_value = cp.card_id and par.attribute_code = 'JBR_RIMP_PARASSIG' and cc.card_id = par.card_id ")
 			.append( "\n\t\t join attribute_value avPar on avPar.card_id = par.card_id and avPar.attribute_code = 'JBR_INFD_SGNEX_LINK' and avPar.number_value = " )
 			.append( userId )
 			.append( "\n\t where cc.status_id = 103) ");
        
        sql.append( ") " );
        
        sql.append( "and avDeadLine.attribute_code = 'JBR_IMPL_DEADLINE' " );
        if(deadline == 0) {
        	sql.append( "and avDeadLine.date_value <= now() " );
        } else {
        	sql.append( "and avDeadLine.date_value > now() ");
        	sql.append( "and avDeadLine.date_value < date_trunc('day', now() + INTERVAL '").append(deadline + 1).append(" days') " );
        }
        
        sql.append( "and " ).append( userPermissionCheck( userId, permissionTypes ) );
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
