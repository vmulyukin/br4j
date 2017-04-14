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

public class ConsiderUrgentlyWorkstationQuery extends JdbcDaoSupport {
	
	public static class AllFolderSupQuery extends ParentWorkstationQuery implements AreaWorkstationQueryInterface {

		public List getCards(int userId, long[] permissionTypes, int page,
				int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			
			StringBuilder sql = buildSupervisorQuery(false, userId,
					permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter, excludeCardsOnService());
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard(rs.getLong(1), rs.getLong(2));
						}
					});
		}
		
		public List getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildSupervisorQuery(true, userId,
					permissionTypes, 0, 0, null, simpleSearchFilter, excludeCardsOnService());
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
	
	public static class ConsiderFolderSupQuery extends ParentWorkstationQuery implements AreaWorkstationQueryInterface {
	
		public List getCards(int userId, long[] permissionTypes, int page,
				int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			
			StringBuilder sql = buildSupervisorConsiderQuery(false, userId,
					permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter, excludeCardsOnService());
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard(rs.getLong(1), rs.getLong(2));
						}
					});
		}
		
		public List getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildSupervisorConsiderQuery(true, userId, permissionTypes, 0, 0, null, simpleSearchFilter, excludeCardsOnService());
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
	
	public static class AcquaintFolderSupQuery extends ParentWorkstationQuery implements AreaWorkstationQueryInterface {
	
		public List getCards(int userId, long[] permissionTypes, int page,
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
		
		public List getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildSupervisorAcquaintQuery(true, userId, permissionTypes, 0, 0, null, simpleSearchFilter, excludeCardsOnService());
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
	
	public static class ExecuteFolderSupQuery extends ParentWorkstationQuery implements AreaWorkstationQueryInterface {
		public List getCards(int userId, long[] permissionTypes, int page,
				int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			
			StringBuilder sql = buildSupervisorExecuteQuery(false, userId,
					permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter, excludeCardsOnService());
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard(rs.getLong(1), rs.getLong(2));
						}
					});
		}
		
		public List getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildSupervisorExecuteQuery(true, userId, permissionTypes, 0, 0, null, simpleSearchFilter, excludeCardsOnService());
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
	
	public static class AllFolderMinisterQuery extends JdbcDaoSupport implements AreaWorkstationQueryInterface {
	
		public List getCards(int userId, long[] permissionTypes,
				int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			
			StringBuilder sql = buildMinisterCardsQuery(userId,
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
			StringBuilder sql = buildMinisterQtyQuery(userId, permissionTypes, simpleSearchFilter);
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
			int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter, String excludeCards ) {
		StringBuilder sql = new StringBuilder();
		sql.append(Util.userPermissionCheckWithClause(userId));
		if(count) {
			sql.append( "select count(*), " ).append( howFastColumn() );
		} else {
			sql.append( "select CARD_ID, TEMPLATE_ID " );
		}
		
		sql.append( "from card c where CARD_ID in ( " );
		
		sql.append( "select c.CARD_ID " );
		sql.append( "from card c " );
		sql.append( 	"\n\t inner join attribute_value vLink on (vLink.card_id = c.card_id) " );
		sql.append( 	"\n\t inner join card cLink on (cLink.card_id = vLink.number_value) " );
		sql.append( 	"\n\t inner join attribute_value vUser on (vUser.card_id = cLink.card_id) " );
		sql.append( 	"\n\t inner join attribute_value vUrg on (vUrg.card_id = cLink.card_id) " );
		sql.append( "\n where c.status_id in (101,102,103,104,206,48909) " );
		sql.append( 	"\n and vLink.attribute_code in ('JBR_INFORM_LIST', 'JBR_IMPL_ACQUAINT' ) " );
		sql.append( 	"\n and cLink.status_id in (102,67424,67425) " );
		sql.append( 	"\n and vUser.attribute_code in ('JBR_RASSM_PERSON','JBR_FOR_INFORMATION') " );
		sql.append( 	"\n and vUser.number_value = ").append( userId ).append( " " );
		sql.append( 	"\n and vUrg.attribute_code = 'JBR_HOWFAST' " );
		sql.append( 	"\n and vUrg.value_id is not null " );
		sql.append( 	"\n AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cLink.card_id AND attribute_code = 'ADMIN_1082454' AND string_value = '1') " );
		sql.append( 	"\n AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = c.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) " );
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
		sql.append( 	"from card cp " );
		sql.append( 		"\n\t\t inner join attribute_value vr on (vr.number_value = cp.card_id) " );
		sql.append( 		"\n\t\t inner join card cr on (cr.card_id = vr.card_id) " );
		sql.append( 		"\n\t\t inner join attribute_value ve on (ve.card_id = cr.card_id) " );
		sql.append( 		"\n\t\t inner join attribute_value vUrg on (vUrg.card_id = cr.card_id) " );
		sql.append(     	"\n\t\t INNER JOIN attribute_value avMain ON (avMain.card_id = cp.card_id ) " );
		sql.append( 	"\n\t where cp.template_id = 324 " );
		sql.append( 		"\n\t and cp.status_id in (103,206) " );
		sql.append( 		"\n\t and vr.attribute_code in ('ADMIN_702311') " );
		sql.append( 		"\n\t and cr.template_id in (1044) " );
		sql.append( 		"\n\t and cr.status_id IN (702239,556656,102) " );
		sql.append( 		"\n\t and ve.attribute_code in ('ADMIN_702335') " );
		sql.append( 		"\n\t and ve.number_value = ").append( userId ).append( " " );
		sql.append( 		"\n\t and vUrg.attribute_code = 'JBR_HOWFAST' " );
		sql.append( 		"\n\t and vUrg.value_id is not null " );
		sql.append(     	"\n\t AND avMain.attribute_code = 'JBR_MAINDOC' " );
		sql.append( 		"\n\t AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cr.card_id AND attribute_code IN ('JBR_RPT_ARMFLAG','JBR_RPT_ARMFLAG2') AND string_value = '1') " );
		sql.append(			"\n\t AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cp.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) " );
		sql.append( 	"\n UNION " );
		sql.append( 	"select avMain.number_value cID, cr.status_id, cr.template_id " );
		sql.append( 	"from card cp " );
		sql.append( 		"\n\t inner join attribute_value vr on (vr.card_id = cp.card_id) " );
		sql.append( 		"\n\t inner join card cr on (cr.card_id = vr.number_value) " );
		sql.append( 		"\n\t inner join attribute_value ve on (ve.card_id = cr.card_id) " );
		sql.append( 		"\n\t inner join attribute_value vUrg on (vUrg.card_id = cr.card_id) " );
		sql.append(     	"\n\t INNER JOIN attribute_value avMain ON (avMain.card_id = cp.card_id ) " );
		sql.append( 	"\n where cp.template_id = 324 " );
		sql.append( 		"\n and cp.status_id in (103,206) " );
		sql.append( 		"\n and vr.attribute_code in ('ADMIN_713517') " );
		sql.append( 		"\n and cr.template_id in (1144) " );
		sql.append( 		"\n and cr.status_id IN (67424) " );
		sql.append( 		"\n and ve.attribute_code in ('ADMIN_726874') " );
		sql.append( 		"\n and ve.number_value = ").append( userId ).append( " " );
		sql.append( 		"\n and vUrg.attribute_code = 'JBR_HOWFAST' " );
		sql.append( 		"\n and vUrg.value_id is not null " );
		sql.append(     	"\n AND avMain.attribute_code = 'JBR_MAINDOC' " );
		sql.append( 		"\n AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cr.card_id AND attribute_code IN ('JBR_RPT_ARMFLAG','JBR_RPT_ARMFLAG2') AND string_value = '1') " );
		sql.append(			"\n AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cp.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) " );
		sql.append( "\n ) crd " );
		sql.append( 	"\n\t inner join card c on c.card_id = crd.cID " );
		sql.append( "\n where c.template_id in (764,784,224) or crd.status_id <> 67425 or crd.template_id = 1144 " );
		sql.append( "\n union " );
    	sql.append( "select c.CARD_ID " );
    	sql.append( "from card c " );
    	sql.append( 	"\n\t inner join attribute_value chief on (chief.card_id = c.card_id) " );
    	sql.append( 	"\n\t inner join attribute_value vUrg on (vUrg.card_id = c.card_id) " );
    	sql.append( 	"\n where c.status_id in (10000120) " );
    	sql.append( 	"\n and chief.attribute_code='JBR_HIDDEN_CHIEF' " );
    	sql.append( 	"\n and chief.number_value = ").append( userId ).append( " " );
    	sql.append( 	"\n and vUrg.attribute_code = 'JBR_HOWFAST' and vUrg.value_id is not null " );
    	sql.append(		"\n AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = c.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) " );
		sql.append( "\n ) " );
		sql.append( "\n and " ).append( userPermissionCheck( userId, permissionTypes ) );
		sql.append(simpleSearchFilter(simpleSearchFilter));
		sql.append("and CARD_ID not in " + excludeCards + " ");
		if(count) {
			sql.append( groupByHowFast() );
		} else {
			sql.append( orderBy( sortAttributes ) );
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
			sql.append( "select c.CARD_ID, c.TEMPLATE_ID " );
		}
		
    	sql.append( "from card c  where CARD_ID in ( " );
    	sql.append( "\n\t select c.CARD_ID " );
		sql.append( "from card c " );
		sql.append( 	"\n\t\t inner join attribute_value vLink on (vLink.card_id = c.card_id) " );
		sql.append( 	"\n\t\t inner join card cLink on (cLink.card_id = vLink.number_value) " );
		sql.append( 	"\n\t\t inner join attribute_value vUser on (vUser.card_id = cLink.card_id) " );
		sql.append( 	"\n\t\t inner join attribute_value vUrg on (vUrg.card_id = cLink.card_id) " );
		sql.append( "\n\t where c.status_id in (101,102,103,104,206,48909) " );
		sql.append( 	"\n\t and vLink.attribute_code = 'JBR_IMPL_ACQUAINT' " );
		sql.append( 	"\n\t and cLink.status_id in (102,67424,67425) " );
		sql.append(		"\n\t AND NOT EXISTS (select 1 from attribute_value av_r	join card c_r on c_r.card_id = av_r.card_id ");
    	sql.append(		"\n\t\t join attribute_value av_c_r on av_c_r.card_id = c_r.card_id and av_c_r.attribute_code = 'JBR_REQUEST_TYPE' and av_c_r.value_id in (1125, 1128) ");
    	sql.append(		"\n\t join attribute_value av_cons on av_cons.card_id = c_r.card_id and av_cons.attribute_code = 'JBR_REQUEST_CONS' ");
    	sql.append(		"\n\t join attribute_value av_rp on av_rp.card_id = av_cons.number_value and av_rp.attribute_code = 'JBR_RASSM_PERSON' and av_rp.number_value = ").append(userId).append(" ");
    	sql.append(		"\n\t\t where c_r.status_id = 102 and av_r.attribute_code = 'JBR_MAINDOC_REQUEST' and av_r.number_value = c.card_id) ");
		
		sql.append( 	"\n\t and vUser.attribute_code = 'JBR_RASSM_PERSON' " );
		sql.append( 	"\n\t and vUser.number_value = ").append( userId ).append( " " );
		sql.append( 	"\n\t and vUrg.attribute_code = 'JBR_HOWFAST' " );
		sql.append( 	"\n\t and vUrg.value_id is not null " );
		sql.append( 	"\n\t AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cLink.card_id AND attribute_code = 'ADMIN_1082454' AND string_value = '1') " );
		sql.append( 	"\n\t AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = c.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) " );
		sql.append( 	"\n\t and ( (c.template_id in (764,784,224) and c.status_id in (103)) or cLink.status_id <> 67425) " );
		sql.append( "\n\t union " );
    	sql.append( "\n\t select c.CARD_ID " );
    	sql.append( "from card c " );
    	sql.append( 	"\n\t\t inner join attribute_value chief on (chief.card_id = c.card_id) " );
    	sql.append( 	"\n\t\t inner join attribute_value vUrg on (vUrg.card_id = c.card_id) " );
    	sql.append( 	"\n\t\t where c.status_id in (10000120) " );
    	sql.append( 	"\n\t\t and chief.attribute_code='JBR_HIDDEN_CHIEF' " );
    	sql.append( 	"\n\t\t and chief.number_value = ").append( userId ).append( " " );
    	sql.append( 	"\n\t\t and vUrg.attribute_code = 'JBR_HOWFAST' and vUrg.value_id is not null " );
    	sql.append(		"\n\t\t AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = c.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) " );
    	sql.append( "\n\t ) " );
		sql.append( "\n\t and " ).append( userPermissionCheck( userId, permissionTypes ) );
		sql.append(simpleSearchFilter(simpleSearchFilter));
		sql.append("\n and c.CARD_ID not in " + excludeCards + " ");
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
		if(count) {
			sql.append( "select count(*), " ).append( howFastColumn() );
		} else {
			sql.append( "select c.CARD_ID, c.TEMPLATE_ID " );
		}
		
		sql.append( "from card c " );
		sql.append( 	"inner join attribute_value vLink on (vLink.card_id = c.card_id) " );
		sql.append( 	"inner join card cLink on (cLink.card_id = vLink.number_value) " );
		sql.append( 	"inner join attribute_value vUser on (vUser.card_id = cLink.card_id) " );
		sql.append( 	"inner join attribute_value vUrg on (vUrg.card_id = cLink.card_id) " );
		sql.append( "where c.status_id in (101,102,103,104,206,48909) " );
		sql.append( 	"and vLink.attribute_code = 'JBR_INFORM_LIST' " );
		sql.append( 	"and cLink.status_id in (102,67424,67425) " );
		sql.append( 	"and vUser.attribute_code = 'JBR_FOR_INFORMATION' " );
		sql.append( 	"and vUser.number_value = ").append( userId ).append( " " );
		sql.append( 	"and vUrg.attribute_code = 'JBR_HOWFAST' " );
		sql.append( 	"and vUrg.value_id is not null " );
		sql.append( 	"AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cLink.card_id AND attribute_code = 'ADMIN_1082454' AND string_value = '1') " );
		sql.append( 	"AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = c.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) " );
		sql.append( 	"and ( (c.template_id in (764,784,224) and c.status_id in (103)) or cLink.status_id <> 67425) " );
		
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
	
	private static StringBuilder buildSupervisorExecuteQuery( boolean count, int userId, long[] permissionTypes, 
			int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter, String excludeCards ) {
		
		StringBuilder sql = new StringBuilder();
		sql.append(Util.userPermissionCheckWithClause(userId));
		if(count) {
			sql.append( "select count(*), " ).append( howFastColumn() );
		} else {
			sql.append( "select c.CARD_ID, c.TEMPLATE_ID " );
		}
		
		sql.append( "from ( " );
		sql.append( 	"select distinct avMain.number_value cID, cr.status_id, cr.template_id " );
		sql.append( 	"from card cp " );
		sql.append( 		"inner join attribute_value vr on (vr.number_value = cp.card_id) " );
		sql.append( 		"inner join card cr on (cr.card_id = vr.card_id) " );
		sql.append( 		"inner join attribute_value ve on (ve.card_id = cr.card_id) " );
		sql.append( 		"inner join attribute_value vUrg on (vUrg.card_id = cr.card_id) " );
		sql.append(     	"INNER JOIN attribute_value avMain ON (avMain.card_id = cp.card_id ) " );
		sql.append( 	"where cp.template_id = 324 " );
		sql.append( 		"and cp.status_id in (103,206) " );
		sql.append( 		"and vr.attribute_code in ('ADMIN_702311') " );
		sql.append( 		"and cr.template_id in (1044) " );
		sql.append( 		"and cr.status_id IN (702239,556656,102) " );
		sql.append( 		"and ve.attribute_code in ('ADMIN_702335') " );
		sql.append( 		"and ve.number_value = ").append( userId ).append( " " );
		sql.append( 		"and vUrg.attribute_code = 'JBR_HOWFAST' " );
		sql.append( 		"and vUrg.value_id is not null " );
		sql.append(     	"AND avMain.attribute_code = 'JBR_MAINDOC' " );
		sql.append( 		"AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cr.card_id AND attribute_code IN ('JBR_RPT_ARMFLAG','JBR_RPT_ARMFLAG2') AND string_value = '1') " );
		sql.append(			"AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cp.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) " );
		sql.append( 	"UNION " );
		sql.append( 	"select distinct avMain.number_value cID, cr.status_id, cr.template_id " );
		sql.append( 	"from card cp " );
		sql.append( 		"inner join attribute_value vr on (vr.card_id = cp.card_id) " );
		sql.append( 		"inner join card cr on (cr.card_id = vr.number_value) " );
		sql.append( 		"inner join attribute_value ve on (ve.card_id = cr.card_id) " );
		sql.append( 		"inner join attribute_value vUrg on (vUrg.card_id = cr.card_id) " );
		sql.append(     	"INNER JOIN attribute_value avMain ON (avMain.card_id = cp.card_id ) " );
		sql.append( 	"where cp.template_id = 324 " );
		sql.append( 		"and cp.status_id in (103,206) " );
		sql.append( 		"and vr.attribute_code in ('ADMIN_713517') " );
		sql.append( 		"and cr.template_id in (1144) " );
		sql.append( 		"and cr.status_id IN (67424) " );
		sql.append( 		"and ve.attribute_code in ('ADMIN_726874') " );
		sql.append( 		"and ve.number_value = ").append( userId ).append( " " );
		sql.append( 		"and vUrg.attribute_code = 'JBR_HOWFAST' " );
		sql.append( 		"and vUrg.value_id is not null " );
		sql.append(     	"AND avMain.attribute_code = 'JBR_MAINDOC' " );
		sql.append( 		"AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cr.card_id AND attribute_code IN ('JBR_RPT_ARMFLAG','JBR_RPT_ARMFLAG2') AND string_value = '1') " );
		sql.append(			"AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = cp.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) " );
		sql.append( ") crd " );
		sql.append( 	"inner join card c on c.card_id = crd.cID " );
		sql.append( "where c.template_id in (764,784,224) or crd.status_id <> 67425 or crd.template_id = 1144 " );
		
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
	
	private static StringBuilder buildMinisterCardsQuery( int userId, long[] permissionTypes, int page, int pageSize, 
			List<SortAttribute> sortAttributes, String simpleSearchFilter ) {
		StringBuilder sql = new StringBuilder();
		sql.append(Util.userPermissionCheckWithClause(userId));
		sql.append( "select p.CARD_ID, c.TEMPLATE_ID, (SELECT value_id FROM attribute_value WHERE card_id = p.card_id AND attribute_code = 'JBR_HOWFAST') " );
		sql.append( "FROM attribute_value p " );
		sql.append( 	"INNER JOIN attribute a on (a.attribute_code = p.attribute_code and a.data_type = 'C') " );
		sql.append( 	"inner join card c on c.card_id = p.card_id " );
		sql.append( "WHERE p.number_value IN ( " );
		sql.append( 	"SELECT c.card_id " );
		sql.append( 	"FROM card c " );
		sql.append( 		"JOIN attribute_value vp ON c.card_id=vp.card_id AND vp.attribute_code='JBR_RASSM_PERSON' " );
		sql.append( 	"WHERE vp.number_value = ").append( userId ).append( " " );
		sql.append( 		"AND c.status_id=102 " );
		sql.append( " AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = p.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449)" );
		sql.append( ") and " ).append( userPermissionCheck( userId, permissionTypes ) );
		sql.append(simpleSearchFilter(simpleSearchFilter));
		sql.append( "ORDER BY coalesce((SELECT value_id FROM attribute_value WHERE card_id = p.card_id AND attribute_code = 'JBR_HOWFAST'),3000) ASC, " );
		sql.append( 	"coalesce((SELECT date_value FROM attribute_value WHERE card_id = p.card_id AND attribute_code = 'JBR_REGD_DATEREG'),now()) ASC " );
		//sql.append( orderBy( sortAttributes ) );
		sql.append( limitAndOffset( page, pageSize ) );
		
		return sql;
	}
	
	private static StringBuilder buildMinisterQtyQuery(int userId, long[] permissionTypes, String simpleSearchFilter) {
		StringBuilder sql = new StringBuilder();
		sql.append(Util.userPermissionCheckWithClause(userId));
		sql.append( "select count(*), " ).append(howFastColumn());
		sql.append( "FROM attribute_value p " );
		sql.append( 	"INNER JOIN attribute a on (a.attribute_code = p.attribute_code and a.data_type = 'C') " );
		sql.append( 	"inner join card c on c.card_id = p.card_id " );
		sql.append( "WHERE p.number_value IN ( " );
		sql.append( 	"SELECT c.card_id " );
		sql.append( 	"FROM card c " );
		sql.append( 		"JOIN attribute_value vp ON c.card_id=vp.card_id AND vp.attribute_code='JBR_RASSM_PERSON' " );
		sql.append( 	"WHERE vp.number_value = ").append( userId ).append( " " );
		sql.append( 		"AND c.status_id=102 " );
		sql.append( " AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = p.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449)" );
		sql.append( ") and " ).append( userPermissionCheck( userId, permissionTypes ) );
		sql.append(simpleSearchFilter(simpleSearchFilter));
		sql.append(groupByHowFast());
		
		return sql;
	}

}
