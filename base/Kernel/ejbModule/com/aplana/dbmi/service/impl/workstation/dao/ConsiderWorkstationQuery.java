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

public class ConsiderWorkstationQuery extends JdbcDaoSupport{
	
	public static class AllFolderSupQuery extends ParentWorkstationQuery implements AreaWorkstationQueryInterface {
		
		public List<?> getCards(int userId, long[] permissionTypes, int page,
				int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			
			StringBuilder sql = buildSupervisorQuery(false, userId,
					permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter, excludeCardsOnService());
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard(rs.getLong(1), rs.getLong(2), rs.getLong(3));
						}
					});
		}

		public List<?> getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildSupervisorQuery(true, userId,
					permissionTypes, 0, 0, null, simpleSearchFilter,excludeCardsOnService());
			List<?> queryResult = executeSimpleQuery(getJdbcTemplate(), sql
					.toString(), new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					return new Long[] { rs.getLong(1), rs.getLong(2) };
				}
			});
			return queryResult;
		}
	}
	
	public static class ConsiderFolderSupQuery extends ParentWorkstationQuery implements AreaWorkstationQueryInterface {
		
		public List<?> getCards(int userId, long[] permissionTypes, int page,
				int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			
			StringBuilder sql = buildSupervisorConsiderQuery(false, userId,
					permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter,excludeCardsOnService());
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard(rs.getLong(1), rs.getLong(2));
						}
					});
		}
		
		public List<?> getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildSupervisorConsiderQuery(true, userId, permissionTypes, 0, 0, null, simpleSearchFilter, excludeCardsOnService());
			List<?> queryResult = executeSimpleQuery(getJdbcTemplate(), sql
					.toString(), new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					return new Long[] { rs.getLong(1), rs.getLong(2) };
				}
			});
			return queryResult;
		}
	}
	
	public static class AcquaintFolderSupQuery extends ParentWorkstationQuery
		implements AreaWorkstationQueryInterface {
		
		public List<?> getCards(int userId, long[] permissionTypes, int page,
				int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			
			StringBuilder sql = buildSupervisorAcquaintQuery(false, userId,
					permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter, excludeCardsOnService());
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard(rs.getLong(1), rs.getLong(2));
						}
					});
		}
		
		public List<?> getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildSupervisorAcquaintQuery(true, userId, permissionTypes, 0, 0, null, simpleSearchFilter, excludeCardsOnService());
			List<?> queryResult = executeSimpleQuery(getJdbcTemplate(), sql
					.toString(), new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					return new Long[] { rs.getLong(1), rs.getLong(2) };
				}
			});
			return queryResult;
		}
	}
	
	public static class ExecuteFolderSupQuery extends ParentWorkstationQuery implements AreaWorkstationQueryInterface {
		
		public List<?> getCards(int userId, long[] permissionTypes, int page,
				int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			
			StringBuilder sql = buildSupervisorExecuteQuery(false, userId,
					permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter, excludeCardsOnService());
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard(rs.getLong(1), rs.getLong(2), rs.getLong(3));
						}
					});
		}
		
		public List<?> getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildSupervisorExecuteQuery(true, userId, permissionTypes, 0, 0, null, simpleSearchFilter, excludeCardsOnService());
			List<?> queryResult = executeSimpleQuery(getJdbcTemplate(), sql
					.toString(), new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					return new Long[] { rs.getLong(1), rs.getLong(2) };
				}
			});
			return queryResult;
		}
	}
    
	public static class AllFolderMinisterQuery extends ParentWorkstationQuery implements AreaWorkstationQueryInterface {

		public List<?> getCards(int userId, long[] permissionTypes,
				int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			
			StringBuilder sql = buildMinisterCardsQuery(false, userId,
					permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter, excludeCardsOnService());
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard(rs.getLong(1), rs.getLong(2));
						}
					});
		}

		public List<?> getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildMinisterCardsQuery(true, userId, permissionTypes, 0, 0, null, simpleSearchFilter, excludeCardsOnService());
			List<?> queryResult = executeSimpleQuery(getJdbcTemplate(), sql
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
			int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter, String excludeCards) {
		StringBuilder sql = new StringBuilder();
		sql.append(Util.userPermissionCheckWithClause(userId));
		if(count) {
    		sql.append( "select count(*), " ).append( howFastColumn() );
    	} else {
    		sql.append( "select CARD_ID, TEMPLATE_ID, STATUS_ID " );
    	}
		
    	sql.append( "from card c where CARD_ID in ( " );
    	
    	sql.append( "select c.CARD_ID " );
    	sql.append( "from card c " );
    	sql.append( 	"\n\t inner join attribute_value vLink on (vLink.card_id = c.card_id) " );
    	sql.append( 	"\n\t inner join card cLink on (cLink.card_id = vLink.number_value) " );
    	sql.append( 	"\n\t inner join attribute_value vUser on (vUser.card_id = cLink.card_id) " );
    	sql.append( "\n where c.status_id in (101,102,103,104,206,48909) " );
    	sql.append( 	"\n and vLink.attribute_code in ('JBR_INFORM_LIST', 'JBR_IMPL_ACQUAINT' ) " );
    	sql.append( 	"\n and cLink.status_id in (102,67424) " );
    	sql.append( 	"\n and vUser.attribute_code in ('JBR_RASSM_PERSON','JBR_FOR_INFORMATION') " );
    	sql.append( 	"\n and vUser.number_value = ").append( userId ).append( " " );
    	sql.append( 	"\n AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cLink.card_id AND attribute_code = 'ADMIN_1082454' AND string_value = '1') " );
    	sql.append( 	"\n AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = c.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) " );
    	sql.append( 	"\n AND EXISTS (SELECT 1 FROM attribute_value WHERE card_id = c.card_id AND attribute_code = 'JBR_REGD_REGNUM') " );
    	sql.append( 	"\n and ( (c.template_id in (764,784,224) and c.status_id in (103)) or cLink.status_id <> 67425) " );
    	sql.append(		"\n AND NOT EXISTS (select 1 from attribute_value av_r	join card c_r on c_r.card_id = av_r.card_id ");
    	sql.append(		"\n\t\t join attribute_value av_c_r on av_c_r.card_id = c_r.card_id and av_c_r.attribute_code = 'JBR_REQUEST_TYPE' and av_c_r.value_id in (1125, 1128) ");
    	sql.append(		"\n\t join attribute_value av_cons on av_cons.card_id = c_r.card_id and av_cons.attribute_code = 'JBR_REQUEST_CONS' ");
    	sql.append(		"\n\t join attribute_value av_rp on av_rp.card_id = av_cons.number_value and av_rp.attribute_code = 'JBR_RASSM_PERSON' and av_rp.number_value = ").append(userId).append(" ");
    	sql.append(		"\n\t\t where c_r.status_id = 102 and av_r.attribute_code = 'JBR_MAINDOC_REQUEST' and av_r.number_value = c.card_id) ");
    	
    	sql.append( "\n union " );
    	
    	sql.append( "select c.CARD_ID " );
    	sql.append( "from ( " );
    	sql.append( 	"\n\t select avMain.number_value cID, cr.status_id, cr.template_id " );
    	sql.append( 	"\n\t from card cp " );
    	sql.append( 		"\n\t\t inner join attribute_value vr on (vr.number_value = cp.card_id) " );
    	sql.append( 		"\n\t\t inner join card cr on (cr.card_id = vr.card_id) " );
    	sql.append( 		"\n\t\t inner join attribute_value ve on (ve.card_id = cr.card_id) " );
    	sql.append(     	"\n\t\t INNER JOIN attribute_value avMain ON (avMain.card_id = cp.card_id ) " );
    	sql.append( 	"\n\t where cp.template_id = 324 " );
    	sql.append( 		"\n\t and cp.status_id in (103,206) " );
    	sql.append( 		"\n\t and vr.attribute_code in ('ADMIN_702311') " );
    	sql.append( 		"\n\t and cr.template_id in (1044) " );
    	sql.append( 		"\n\t and cr.status_id IN (702239,556656,102) " );
    	sql.append( 		"\n\t and ve.attribute_code in ('ADMIN_702335') " );
    	sql.append( 		"\n\t and ve.number_value = ").append( userId ).append( " " );
    	sql.append(     	"\n\t AND avMain.attribute_code = 'JBR_MAINDOC' " );
    	sql.append( 		"\n\t AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cr.card_id AND attribute_code IN ('JBR_RPT_ARMFLAG','JBR_RPT_ARMFLAG2') AND string_value = '1') " );
    	sql.append(			"\n\t AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cp.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) " );
    	sql.append( 		"\n\t AND EXISTS (SELECT 1 FROM attribute_value WHERE card_id = avMain.number_value AND attribute_code = 'JBR_REGD_REGNUM') " );
    	
    	sql.append( 	"\n\t UNION " );
    	sql.append( 	"select avMain.number_value cID, cr.status_id, cr.template_id " );
    	sql.append( 	"\n\t from card cp " );
    	sql.append( 		"\n\t\t inner join attribute_value vr on (vr.card_id = cp.card_id) " );
    	sql.append( 		"\n\t\t inner join card cr on (cr.card_id = vr.number_value) " );
    	sql.append( 		"\n\t\t inner join attribute_value ve on (ve.card_id = cr.card_id) " );
    	sql.append(     	"\n\t\t INNER JOIN attribute_value avMain ON (avMain.card_id = cp.card_id ) " );
    	sql.append( 	"\n\t where cp.template_id = 324 " );
    	sql.append( 		"\n\t and cp.status_id in (103,206) " );
    	sql.append( 		"\n\t and vr.attribute_code in ('ADMIN_713517') " );
    	sql.append( 		"\n\t and cr.template_id in (1144) " );
    	sql.append( 		"\n\t and cr.status_id IN (67424) " );
    	sql.append( 		"\n\t and ve.attribute_code in ('ADMIN_726874') " );
    	sql.append( 		"\n\t and ve.number_value = ").append( userId ).append( " " );
    	sql.append(     	"\n\t AND avMain.attribute_code = 'JBR_MAINDOC' " );
    	sql.append( 		"\n\t AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cr.card_id AND attribute_code IN ('JBR_RPT_ARMFLAG','JBR_RPT_ARMFLAG2') AND string_value = '1') " );
    	sql.append(			"\n\t AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cp.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) " );
    	sql.append( 		"\n\t AND EXISTS (SELECT 1 FROM attribute_value WHERE card_id = avMain.number_value AND attribute_code = 'JBR_REGD_REGNUM') " );
    	sql.append( "\n ) crd " );
    	sql.append( 	"\n inner join card c on c.card_id = crd.cID " );
    	sql.append( "\n where c.template_id in (764,784,224) or crd.status_id <> 67425 or crd.template_id = 1144 "); 
    	sql.append( "\n union " );
    	sql.append( "select c.CARD_ID " );
    	sql.append( "\n from card c " );
    	sql.append( 	"\n\t inner join attribute_value chief on (chief.card_id = c.card_id )  " );
    	sql.append( 	"\n\t where c.status_id in (10000120) " );
    	sql.append( 	"\n\t and chief.attribute_code='JBR_HIDDEN_CHIEF' " );
    	sql.append( 	"\n\t AND chief.number_value = ").append( userId ).append( " " );
    	sql.append(		"\n\t AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = c.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) " );

        sql.append( "\n union ");
        sql.append( "select c.CARD_ID ");
        sql.append( "from card c " );
        sql.append( "\n where c.template_id = 2290 and c.status_id = 67424 " );
        sql.append(     "\n and ").append(numericValueCondition("DLGT_TO", userId)).append(" ");

        sql.append( "union ");
        sql.append( "select cp.CARD_ID ");      
        sql.append( 	"\n from card cp " );
    	sql.append( 		"\n\t inner join attribute_value vr on (vr.number_value = cp.card_id) " );
    	sql.append( 		"\n\tinner join card cr on (cr.card_id = vr.card_id) " );
    	sql.append( 		"\n\tinner join attribute_value ve on (ve.card_id = cr.card_id) " );
    	sql.append( 	"\n where cp.template_id = 1255 " );
    	sql.append( 		"\n and cp.status_id in (103,206) " );
    	sql.append( 		"\n and vr.attribute_code in ('ADMIN_702311') " );
    	sql.append( 		"\n and cr.template_id in (1044) " );
    	sql.append( 		"\n and cr.status_id IN (702239,556656,102) " );
    	sql.append( 		"\n and ve.attribute_code in ('ADMIN_702335') " );
    	sql.append( 		"\n and ve.number_value = ").append( userId ).append( " " );
    	sql.append( 		"\n AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cr.card_id AND attribute_code IN ('JBR_RPT_ARMFLAG','JBR_RPT_ARMFLAG2') AND string_value = '1') " );
    	sql.append(			"\n AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cp.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) " );    	
    	
    	sql.append( "\n union ");
    	sql.append( "select cp.CARD_ID ");      
        sql.append( 	"from card cp " );
    	sql.append( 		"\n\t inner join attribute_value vr on (vr.card_id = cp.card_id) " );
    	sql.append( 		"\n\t inner join card cr on (cr.card_id = vr.number_value) " );
    	sql.append( 		"\n\t inner join attribute_value ve on (ve.card_id = cr.card_id) " );
    	sql.append( 	"\n where cp.template_id = 1255 " );
    	sql.append( 		"\n and cp.status_id in (103,206) " );
    	sql.append( 		"\n and vr.attribute_code in ('ADMIN_713517') " );
    	sql.append( 		"\n and cr.template_id in (1144) " );
    	sql.append( 		"\n and cr.status_id IN (67424) " );
    	sql.append( 		"\n and ve.attribute_code in ('ADMIN_726874') " );
    	sql.append( 		"\n and ve.number_value = ").append( userId ).append( " " );
    	sql.append( 		"\n AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cr.card_id AND attribute_code IN ('JBR_RPT_ARMFLAG','JBR_RPT_ARMFLAG2') AND string_value = '1') " );
    	sql.append(			"\n AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cp.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) " );
    	
    	sql.append( "\n ) " );
    	sql.append( "\n and " ).append( userPermissionCheck( userId, permissionTypes ) );
    	sql.append(simpleSearchFilter(simpleSearchFilter));
        sql.append("and CARD_ID not in " + excludeCards + " ");
        if(count) {
        	sql.append( groupByHowFast() );
        } else {
        	sql.append( customOrderBy( sortAttributes ) );
        	sql.append( limitAndOffset( page, pageSize ) );
        }
        
        return sql;
    }
	
	private static StringBuilder buildSupervisorConsiderQuery( boolean count, int userId, long[] permissionTypes, 
			int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter, String excludeCards ) {
		
		StringBuilder sql = new StringBuilder();
		sql.append(Util.userPermissionCheckWithClause(userId));
		if(count) {
    		sql.append( "select count(*), " ).append( howFastColumn() );
    	} else {
    		sql.append( "select c.CARD_ID, c.TEMPLATE_ID, c.STATUS_ID " );
    	}
		
    	sql.append( "from card c  where CARD_ID in ( " );
    	sql.append( "\n\t select c.CARD_ID " );
    	sql.append( "from card c " );
    	sql.append( 	"\n\t\t inner join attribute_value vLink on (vLink.card_id = c.card_id) " );
    	sql.append( 	"\n\t\t inner join card cLink on (cLink.card_id = vLink.number_value) " );
    	sql.append( 	"\n\t\t inner join attribute_value vUser on (vUser.card_id = cLink.card_id) " );
    	sql.append( "\n\t where c.status_id in (101,102,103,104,206,48909) " );
    	sql.append( 	"\n\t and vLink.attribute_code = 'JBR_IMPL_ACQUAINT' " );
    	sql.append( 	"\n\t and cLink.status_id in (102,67424,67425) " );
    	sql.append( 	"\n\t and vUser.attribute_code = 'JBR_RASSM_PERSON' " );
    	sql.append( 	"\n\t and vUser.number_value = ").append( userId ).append( " " );
    	sql.append( 	"\n\t AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cLink.card_id AND attribute_code = 'ADMIN_1082454' AND string_value = '1') " );
    	sql.append( 	"\n\t AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = c.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) " );
    	sql.append( 	"\n\t AND EXISTS (SELECT 1 FROM attribute_value WHERE card_id = c.card_id AND attribute_code = 'JBR_REGD_REGNUM') " );
    	sql.append( 	"\n\t and ( (c.template_id in (764,784,224) and c.status_id in (103)) or cLink.status_id <> 67425) " );
    	sql.append(		"\n\t AND NOT EXISTS (select 1 from attribute_value av_r	join card c_r on c_r.card_id = av_r.card_id ");
    	sql.append(		"\n\t\t join attribute_value av_c_r on av_c_r.card_id = c_r.card_id and av_c_r.attribute_code = 'JBR_REQUEST_TYPE' and av_c_r.value_id in (1125, 1128) ");
    	sql.append(		"\n\t join attribute_value av_cons on av_cons.card_id = c_r.card_id and av_cons.attribute_code = 'JBR_REQUEST_CONS' ");
    	sql.append(		"\n\t join attribute_value av_rp on av_rp.card_id = av_cons.number_value and av_rp.attribute_code = 'JBR_RASSM_PERSON' and av_rp.number_value = ").append(userId).append(" ");
    	sql.append(		"\n\t\t where c_r.status_id = 102 and av_r.attribute_code = 'JBR_MAINDOC_REQUEST' and av_r.number_value = c.card_id) ");
    	sql.append( 	"\n UNION " );
    	sql.append( "select c.CARD_ID " );
    	sql.append( "from card c " );
    	sql.append( 	"\n\t inner join attribute_value chief on (chief.card_id = c.card_id )  " );
    	sql.append( 	"\n\t where c.status_id in (10000120) " );
    	sql.append( 	"\n\t and chief.attribute_code='JBR_HIDDEN_CHIEF' " );
    	sql.append( 	"\n\t AND chief.number_value = ").append( userId ).append( " " );
    	sql.append(		"\n\t AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = c.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) " );
    	sql.append( "\n ) " );
    	sql.append( "and " ).append( userPermissionCheck( userId, permissionTypes ) );
    	sql.append(simpleSearchFilter(simpleSearchFilter));
    	sql.append("and c.CARD_ID not in " + excludeCards + " ");
    	if(count) {
        	sql.append( groupByHowFast() );
        } else {
        	sql.append( orderBy( sortAttributes ) );
        	sql.append( limitAndOffset( page, pageSize ) );
        }
        return sql;
    }
	
	private static StringBuilder buildSupervisorAcquaintQuery( boolean count, int userId, long[] permissionTypes, 
			int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter, String excludeCards ) {
		StringBuilder sql = new StringBuilder();
		sql.append(Util.userPermissionCheckWithClause(userId));
		sql.append( /*with*/", c as ");
		sql.append(" ( select c.CARD_ID,  c.TEMPLATE_ID,  c.STATUS_ID ");
		sql.append("from card c ");
		sql.append("inner join attribute_value vLink on (vLink.card_id = c.card_id) ");
		sql.append("inner join card cLink on (cLink.card_id = vLink.number_value) ");
		sql.append("inner join attribute_value vUser on (vUser.card_id = cLink.card_id) ");
		sql.append("where c.status_id in (101, 102, 103, 206, 48909) ");
		sql.append("and c.template_id in (224, 764, 784, 864, 1226, 1255) ");
		sql.append("and vLink.attribute_code = 'JBR_INFORM_LIST' ");
		sql.append("and cLink.template_id = 524 ");
		sql.append("and cLink.status_id in (67424) ");
		sql.append("and vUser.attribute_code = 'JBR_FOR_INFORMATION' ");
		sql.append("and vUser.number_value = ").append( userId ).append( " " ); 
		sql.append("AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cLink.card_id AND attribute_code = 'ADMIN_1082454' AND string_value = '1') ");
		sql.append("AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = c.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) ");

		sql.append("union ");
		
		sql.append("select cBase.CARD_ID,  cBase.TEMPLATE_ID,  cBase.STATUS_ID ");
		sql.append("from card cRes ");
		sql.append("inner join attribute_value resOnBase on (resOnBase.card_id = cRes.card_id) ");
		sql.append("inner join attribute_value resOnAcq on (resOnAcq.card_id = cRes.card_id) ");
		sql.append("inner join card cBase on (cBase.card_id = resOnBase.number_value) ");
		sql.append("inner join attribute_value acquaint on (acquaint.card_id = resOnAcq.number_value) ");
		sql.append("inner join card cAcquaint on (cAcquaint.card_id = acquaint.card_id) ");
		sql.append("where resOnBase.attribute_code = 'JBR_MAINDOC' ");
		sql.append("and resOnAcq.attribute_code = 'ADMIN_713517' ");
		sql.append("and acquaint.attribute_code = 'ADMIN_726874' ");
		sql.append("and cBase.status_id in (101, 102, 103, 206, 48909) ");
		sql.append("and cBase.template_id in (224, 764, 784, 864, 1226) ");
		sql.append("and cRes.template_id = 324 ");
		sql.append("and cAcquaint.template_id = 1144 ");
		sql.append("and cAcquaint.status_id = 67424 ");
		sql.append("and acquaint.number_value = ").append( userId ).append( " " );
		sql.append("AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = acquaint.card_id AND attribute_code = 'JBR_ARM_HIDE' AND string_value = '1') ");
		sql.append("AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cBase.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) ");
		
		sql.append("union ");
		
		sql.append("select cBase.CARD_ID,  cBase.TEMPLATE_ID,  cBase.STATUS_ID ");
		sql.append("from card cBase ");
		sql.append("inner join attribute_value base on (base.card_id = cBase.card_id) ");
		sql.append("inner join attribute_value acquaint on (acquaint.card_id = cBase.card_id) ");
		sql.append("inner join card cAcquaint on (cAcquaint.card_id = acquaint.card_id) ");
		sql.append("where base.attribute_code = 'ADMIN_713517' ");
		sql.append("and acquaint.attribute_code = 'ADMIN_726874' ");
		sql.append("and cBase.status_id in (101, 102, 103, 206, 48909) ");
		sql.append("and cBase.template_id = 1255 ");
		sql.append("and cAcquaint.template_id = 1144 ");
		sql.append("and cAcquaint.status_id = 67424 ");
		sql.append("and acquaint.number_value = ").append( userId ).append( " " );
		sql.append("AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = acquaint.card_id AND attribute_code = 'JBR_ARM_HIDE' AND string_value = '1') ");
		sql.append("AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cBase.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) ");
		
		sql.append(" union ");

		sql.append("select c.CARD_ID,  c.TEMPLATE_ID,  c.STATUS_ID ");
		sql.append("from card c ");
		sql.append("where c.template_id = 2290 and c.status_id = 67424 " );
        sql.append(     "and ").append(numericValueCondition("DLGT_TO", userId)).append(" ");
        sql.append(") ");

        if(count) {
    		sql.append( "select count(*), " ).append( howFastColumn() );
    	} else {
    		sql.append( "select c.CARD_ID, c.TEMPLATE_ID, c.STATUS_ID " );
    	}
        sql.append(" from c where  " );
    	sql.append( userPermissionCheck( userId, permissionTypes ) );
    	sql.append(simpleSearchFilter(simpleSearchFilter));
    	sql.append("and c.CARD_ID not in " + excludeCards + " ");
        
        if(count) {
        	sql.append( groupByHowFast() );
        } else {
        	sql.append( customOrderBy( sortAttributes ) );
        	sql.append( limitAndOffset( page, pageSize ) );
        }
        
        return sql;
    }
	
	private static StringBuilder buildSupervisorExecuteQuery( boolean count, int userId, long[] permissionTypes, 
			int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter, String excludeCards ) {
		
		StringBuilder sql = new StringBuilder();
		sql.append(Util.userPermissionCheckWithClause(userId));
		sql.append( /*with*/", c as ");
        sql.append( " ( select c.CARD_ID,  c.TEMPLATE_ID,  c.STATUS_ID ");
    	sql.append( "from ( " );
    	sql.append( 	"select distinct avMain.number_value cID, cr.status_id, cr.template_id " );
    	sql.append( 	"from card cp " );
    	sql.append( 		"inner join attribute_value vr on (vr.number_value = cp.card_id) " );
    	sql.append( 		"inner join card cr on (cr.card_id = vr.card_id) " );
    	sql.append( 		"inner join attribute_value ve on (ve.card_id = cr.card_id) " );
    	sql.append(     	"INNER JOIN attribute_value avMain ON (avMain.card_id = cp.card_id ) " );
    	sql.append( 	"where cp.template_id = 324 " );
    	sql.append( 		"and cp.status_id in (103,206) " );
    	sql.append( 		"and vr.attribute_code in ('ADMIN_702311') " );
    	sql.append( 		"and cr.template_id in (1044) " );
    	sql.append( 		"and cr.status_id IN (702239,556656,102) " );
    	sql.append( 		"and ve.attribute_code in ('ADMIN_702335') " );
    	sql.append( 		"and ve.number_value = ").append( userId ).append( " " );
    	sql.append(     	"AND avMain.attribute_code = 'JBR_MAINDOC' " );
    	sql.append( 		"AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cr.card_id AND attribute_code IN ('JBR_RPT_ARMFLAG','JBR_RPT_ARMFLAG2') AND string_value = '1') " );
    	sql.append(			"AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cp.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) " );
    	
    	// �� ������� ������� �������� "������������ � ����������", ���������� �� � �������� ��������� ��������� - �� �����.
    	/*sql.append( 	" UNION " );
    	
    	sql.append( 	"select distinct avMain.number_value cID, cr.status_id, cr.template_id " );
    	sql.append( 	"from card cp " );
    	sql.append( 		"inner join attribute_value vr on (vr.card_id = cp.card_id) " );
    	sql.append( 		"inner join card cr on (cr.card_id = vr.number_value) " );
    	sql.append( 		"inner join attribute_value ve on (ve.card_id = cr.card_id) " );
    	sql.append(     	"INNER JOIN attribute_value avMain ON (avMain.card_id = cp.card_id ) " );
    	sql.append( 	"where cp.template_id = 324 " );
    	sql.append( 		"and cp.status_id in (103,206) " );
    	sql.append( 		"and vr.attribute_code in ('ADMIN_713517') " );
    	sql.append( 		"and cr.template_id in (1144) " );
    	sql.append( 		"and cr.status_id IN (67424) " );
    	sql.append( 		"and ve.attribute_code in ('ADMIN_726874') " );
    	sql.append( 		"and ve.number_value = ").append( userId ).append( " " );
    	sql.append(     	"AND avMain.attribute_code = 'JBR_MAINDOC' " );
    	sql.append( 		"AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cr.card_id AND attribute_code IN ('JBR_RPT_ARMFLAG','JBR_RPT_ARMFLAG2') AND string_value = '1') " );
    	sql.append(			"AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cp.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) " );
    	*/
    	sql.append( 	" UNION " );
    	
    	sql.append( 	"select cp.card_id as cID, cr.status_id, cr.template_id " );
    	sql.append( 	"from card cp " );
    	sql.append( 		"inner join attribute_value vr on (vr.number_value = cp.card_id) " );
    	sql.append( 		"inner join card cr on (cr.card_id = vr.card_id) " );
    	sql.append( 		"inner join attribute_value ve on (ve.card_id = cr.card_id) " );
    	sql.append( 	"where cp.template_id = 1255 " );
    	sql.append( 		"and cp.status_id in (103,206) " );
    	sql.append( 		"and vr.attribute_code in ('ADMIN_702311') " );
    	sql.append( 		"and cr.template_id in (1044) " );
    	sql.append( 		"and cr.status_id IN (702239,556656,102) " );
    	sql.append( 		"and ve.attribute_code in ('ADMIN_702335') " );
    	sql.append( 		"and ve.number_value = ").append( userId ).append( " " );
    	sql.append( 		"AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cr.card_id AND attribute_code IN ('JBR_RPT_ARMFLAG','JBR_RPT_ARMFLAG2') AND string_value = '1') " );
    	sql.append(			"AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cp.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) " );
    	
    	sql.append( 	" UNION " );
    	
    	sql.append( 	"select cp.card_id as cID, cr.status_id, cr.template_id " );
    	sql.append( 	"from card cp " );
    	sql.append( 		"inner join attribute_value vr on (vr.card_id = cp.card_id) " );
    	sql.append( 		"inner join card cr on (cr.card_id = vr.number_value) " );
    	sql.append( 		"inner join attribute_value ve on (ve.card_id = cr.card_id) " );
    	sql.append( 	"where cp.template_id = 1255 " );
    	sql.append( 		"and cp.status_id in (103,206) " );
    	sql.append( 		"and vr.attribute_code in ('ADMIN_713517') " );
    	sql.append( 		"and cr.template_id in (1144) " );
    	sql.append( 		"and cr.status_id IN (67424) " );
    	sql.append( 		"and ve.attribute_code in ('ADMIN_726874') " );
    	sql.append( 		"and ve.number_value = ").append( userId ).append( " " );
    	sql.append( 		"AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cr.card_id AND attribute_code IN ('JBR_RPT_ARMFLAG','JBR_RPT_ARMFLAG2') AND string_value = '1') " );
    	sql.append(			"AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cp.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) " );
    	
    	sql.append( ") crd " );
    	sql.append( 	"inner join card c on c.card_id = crd.cID " );
    	sql.append( "where (c.template_id in (764,784,224) or crd.status_id <> 67425 or crd.template_id = 1144) " );
    	sql.append( "AND EXISTS (SELECT 1 FROM attribute_value WHERE card_id = c.card_id AND attribute_code = 'JBR_REGD_REGNUM') " );
    	sql.append( " GROUP by c.CARD_ID, c.TEMPLATE_ID,  c.STATUS_ID ");
        sql.append( ")");
 
        if(count) {
    		sql.append( " select count(*), " ).append( howFastColumn() );
    	} else {
    		sql.append( " select c.CARD_ID, c.TEMPLATE_ID, c.STATUS_ID " );
    	}
        sql.append(" from c where  " );
        sql.append( userPermissionCheck( userId, permissionTypes ) );
    	sql.append(simpleSearchFilter(simpleSearchFilter));
    	sql.append("and c.CARD_ID not in " + excludeCards + " ");
    	
        if(count) {
        	sql.append( groupByHowFast() );
        } else {
        	sql.append( orderBy( sortAttributes ) );
        	sql.append( limitAndOffset( page, pageSize ) );
        }
        
        return sql;
    }
	
	private static StringBuilder buildMinisterCardsQuery( boolean count, int userId, long[] permissionTypes, int page, 
			int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter, String excludeCards ) {
		
		StringBuilder sql = new StringBuilder();
		sql.append(Util.userPermissionCheckWithClause(userId));
        sql.append( /*WITH*/", c as ");
        sql.append("( select ca.CARD_ID, ca.TEMPLATE_ID,  ca.STATUS_ID \n");
      	sql.append(" FROM card ca \n");
    	sql.append( " join attribute_value p on ca.card_id = p.card_id and p.attribute_code = 'JBR_IMPL_ACQUAINT' \n" );
    	sql.append( " join card c on p.number_value = c.card_id and c.status_id=102 \n" );
    	sql.append(" join attribute_value vp on c.card_id = vp.card_id \n");
    	sql.append( " and vp.attribute_code = 'JBR_RASSM_PERSON' and vp.number_value = ").append( userId ).append("\n");
        sql.append( "where \n");
    	sql.append(" NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = ca.card_id \n");
        sql.append( "   AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449)) \n" );
		if(count) {
    		sql.append( "select count(*), " ).append( howFastColumn() );
    	} else {
    		sql.append( "select c.CARD_ID, c.TEMPLATE_ID " );
    	}
        sql.append( "FROM c ");
        sql.append( " where " ).append( userPermissionCheck( userId, permissionTypes ) );
    	sql.append(simpleSearchFilter(simpleSearchFilter));
    	sql.append("and c.CARD_ID not in " + excludeCards + " ");
        
        if(count) {
        	sql.append( groupByHowFast() );
        } else {
        	sql.append( orderBy( sortAttributes ) );
        	sql.append( limitAndOffset( page, pageSize ) );
        }
        return sql;
    }

    private static StringBuilder customOrderBy(List<SortAttribute> sortAttributes) {
        if(null == sortAttributes || sortAttributes.size() != 1 ||
                (!"JBR_INFD_SHORTDESC".equals(sortAttributes.get(0).getCode()) &&
                        !"JBR_IMPL_DEADLINE".equals(sortAttributes.get(0).getCode()))) {
            return orderBy( sortAttributes );
        }

        // Overriding with Custom ordering when sorting by created
        StringBuilder orderClause = new StringBuilder("order by (");

        if("JBR_INFD_SHORTDESC".equals(sortAttributes.get(0).getCode())) {
            orderClause.append("case when c.template_id = 2290 ");
            orderClause.append("then (select string_value from attribute_value av_order where av_order.card_id = c.card_id and av_order.attribute_code = 'NAME') ");
            orderClause.append("else (select string_value from attribute_value av_order where av_order.card_id = c.card_id and av_order.attribute_code = 'JBR_INFD_SHORTDESC') ");
        } else {
            orderClause.append("case when c.template_id = 2290 ");
            orderClause.append("then (select date_value from attribute_value av_order where av_order.card_id = c.card_id and av_order.attribute_code = 'DLGT_DATE_START') ");
            orderClause.append("else (select date_value from attribute_value av_order where av_order.card_id = c.card_id and av_order.attribute_code = 'JBR_IMPL_DEADLINE') ");
        }

        orderClause.append("end) ");

        if(sortAttributes.get(0).isAsc()) {
            orderClause.append("asc ");
        } else {
            orderClause.append("desc ");
        }

        return orderClause;
    }
}
