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

public class SignWorkstationQuery extends JdbcDaoSupport {
    
	
	public static class AllFolderMinisterQuery extends JdbcDaoSupport
			implements AreaWorkstationQueryInterface {
		/**
		 * �������/��������� folderSign.xml
		 * 
		 * @param userId
		 * @param permissionTypes
		 * @param page
		 * @param pageSize
		 * @return
		 */
		public List getCards(int userId, long[] permissionTypes,
				int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			StringBuilder sql = buildMinisterQuery(false, userId,
					permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter);
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard ( rs.getLong( 1 ), rs.getLong( 2 ), rs.getLong( 3 ) );
						}
					});
		}

		/**
		 * �������/��������� - ���-��
		 * 
		 * @param userId
		 * @param permissionTypes
		 * @return
		 */
		public List getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildMinisterQuery(true, userId,
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

	
	public static class SigningOrdersFolderMinisterQuery extends
			JdbcDaoSupport implements AreaWorkstationQueryInterface {

		/**
		 * �������/���������/������� folderSignOrder.xml
		 * 
		 * @param userId
		 * @param permissionTypes
		 * @param page
		 * @param pageSize
		 * @return
		 */
		public List getCards(int userId, long[] permissionTypes, int page,
				int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			StringBuilder sql = buildMinisterOrderQuery(false, userId,
					permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter);
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard ( rs.getLong( 1 ), rs.getLong( 2 ), rs.getLong( 3 ) );
						}
					});
		}

		/**
		 * �������/���������/������� - ���-��
		 * 
		 * @param userId
		 * @param permissionTypes
		 * @return
		 */
		public List getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildMinisterOrderQuery(true, userId,
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

	
	public static class SigningHROrdersFolderMinisterQuery extends
			JdbcDaoSupport implements AreaWorkstationQueryInterface {

		/**
		 * �������/���������/�������� ������� folderSignOrderHR.xml
		 * 
		 * @param userId
		 * @param permissionTypes
		 * @param page
		 * @param pageSize
		 * @return
		 */
		public List getCards(int userId, long[] permissionTypes, int page,
				int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			StringBuilder sql = buildMinisterHROrderQuery(false, userId,
					permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter);
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard(rs.getLong(1), rs.getLong(2), rs.getLong( 3 ));
						}
					});
		}

		/**
		 * �������/���������/�������� ������� - ���-��
		 * 
		 * @param userId
		 * @param permissionTypes
		 * @return
		 */
		public List getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildMinisterHROrderQuery(true, userId,
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

	
	public static class SigningLettersFolderMinisterQuery extends
			JdbcDaoSupport implements AreaWorkstationQueryInterface {

		/**
		 * �������/���������/������ folderSignLetter.xml
		 * 
		 * @param userId
		 * @param permissionTypes
		 * @param page
		 * @param pageSize
		 * @return
		 */
		public List getCards(int userId, long[] permissionTypes, int page,
				int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			StringBuilder sql = buildMinisterLetterQuery(false, userId,
					permissionTypes, page, pageSize, sortAttributes, simpleSearchFilter);
			return executeSimpleQuery(getJdbcTemplate(), sql.toString(),
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							return new EmptyCard(rs.getLong(1), rs.getLong(2), rs.getLong( 3 ));
						}
					});
		}

		/**
		 * �������/���������/������ - ���-��
		 * 
		 * @param userId
		 * @param permissionTypes
		 * @return
		 */
		public List getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildMinisterLetterQuery(true, userId,
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

	public static class AllFolderSupQuery extends JdbcDaoSupport
			implements AreaWorkstationQueryInterface {

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
							return new EmptyCard(rs.getLong(1), rs.getLong(2), rs.getLong( 3 ));
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
							return new EmptyCard(rs.getLong(1), rs.getLong(2), rs.getLong( 3 ));
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

	public static class ForSigningFolderSupQuery extends
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
							return new EmptyCard(rs.getLong(1), rs.getLong(2), rs.getLong( 3 ));
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

        sql.append("from card c inner join template t on (t.template_id=c.template_id) where CARD_ID in (SELECT c.card_id" +
                "  FROM card c INNER JOIN attribute_value vlink ON (vlink.card_id = c.card_id) " +
                "       INNER JOIN card clink ON (clink.card_id = vlink.number_value) " +
                "       INNER JOIN attribute_value vuser ON (vuser.card_id = clink.card_id) " +
                " left outer join attribute_value inDate on inDate.card_id = clink.card_id and inDate.attribute_code = 'JBR_INCOMEDATE' " +
                " WHERE c.status_id IN (107, 108) " +
                "   AND vlink.attribute_code IN ('JBR_SIGN_SIGNING') " +
                "   AND clink.status_id IN (107, 6833780, 108) " +
                "   AND vuser.attribute_code IN " +
                "                             ('JBR_SIGN_RESPONSIBLE') " +
                "   AND vuser.number_value = ").append(userId).append(" " +
        
                "UNION "+
    	
                "SELECT CARD_ID " + 
                "FROM card c " +
                "WHERE c.card_id IN ( " +
                    "SELECT functionbacklink(c.card_id, 'ADMIN_6814498', 'JBR_VISA_VISA') AS card_id " +
                    "FROM Card c " +
                    "INNER JOIN attribute_value avUser ON (  avUser.card_id = c.card_id AND avUser.number_value = ").append(userId).append(" ) " + 
                    "WHERE   (c.template_id=348) and (c.status_id = 107 or c.status_id = 6833780) " + // ������=�����������, ���������=������������ ��� �������������� ������������
                    "AND (avUser.attribute_code='JBR_VISA_RESPONSIBLE') " + // �����������=������� ������������ 
                    ") " +
                "union " +
                "SELECT CARD_ID " + 
                "FROM card c " +
                "WHERE c.card_id IN ( " +
                	"select avMain.number_value " +
                	"from card cp " +
                    "inner join attribute_value vr on (vr.number_value = cp.card_id) " +
					"inner join card cr on (cr.card_id = vr.card_id) " +
					"inner join attribute_value ve on (ve.card_id = cr.card_id) " +
					"INNER JOIN attribute_value avMain ON (avMain.card_id = cp.card_id) " +
					"where cp.template_id = 324 " +
					"and cp.status_id in (107) " +
					"and vr.attribute_code in ('JBR_VISA_RES_B') " +
					"and cr.template_id in (2344) " +
					"and cr.status_id IN (107) " +
					"and ve.attribute_code in ('JBR_VISA_RESPONSIBLE') " +
					"and ve.number_value = ").append( userId ).append( " "  +
					"AND avMain.attribute_code = 'JBR_MAINDOC' " +
				"union " +
			    "select cin.CARD_ID from card cin " +          
			    	" where cin.template_id = 1255 " +  
			    	" and cin.status_id =108 " +
			    	" and exists (select 1 from ATTRIBUTE_VALUE avin where avin.ATTRIBUTE_CODE = 'JBR_INFD_SGNEX_LINK' " +
			    	"   and avin.NUMBER_VALUE = ").append( userId ).append( " " +  
			    	"   and avin.CARD_ID = cin.CARD_ID " +
			    	"   )"+ 
				")" +
		")");
        sql.append(" and " ).append( userPermissionCheck( userId, permissionTypes ) );
        sql.append(simpleSearchFilter(simpleSearchFilter));
        
        if(count) {
        	sql.append( groupByHowFast() );
        } else {
        	sql.append( supervisorOrderBy ( sortAttributes ) );
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
        
        sql.append("from card c inner join template t on (t.template_id=c.template_id) where CARD_ID in (SELECT c.card_id "+
                "FROM card c " +
                "WHERE c.card_id IN ( " +
                    "SELECT functionbacklink(c.card_id, 'ADMIN_6814498', 'JBR_VISA_VISA') AS card_id " +
                    "FROM Card c " +
                    "INNER JOIN attribute_value avUser ON (  avUser.card_id = c.card_id AND avUser.number_value = ").append(userId).append(" ) " + 
                    "WHERE   (c.template_id=348) and c.status_id in (107,6833780) " + // ������=�����������, ���������=������������ ��� �������������� ������������
                    "AND (avUser.attribute_code='JBR_VISA_RESPONSIBLE') " + // �����������=������� ������������ 
                    ") " +
                "union " +
                "SELECT CARD_ID " + 
                "FROM card c " +
                "WHERE c.card_id IN ( " +
                    "select avMain.number_value " +
    				"from card cp " +
                    "inner join attribute_value vr on (vr.number_value = cp.card_id) " +
                    "inner join card cr on (cr.card_id = vr.card_id) " +
                    "inner join attribute_value ve on (ve.card_id = cr.card_id) " +
                    "INNER JOIN attribute_value avMain ON (avMain.card_id = cp.card_id) " +
                    "where cp.template_id = 324 " +
                    		"and cp.status_id in (107) " +
                    		"and vr.attribute_code in ('JBR_VISA_RES_B') " +
                    		"and cr.template_id in (2344) " +
                    		"and cr.status_id IN (107) " +
                    		"and ve.attribute_code in ('JBR_VISA_RESPONSIBLE') " +
                    		"and ve.number_value = ").append( userId ).append( " " +
                    		"AND avMain.attribute_code = 'JBR_MAINDOC' " +
                ")" +
        ")");
        sql.append(" and " ).append( userPermissionCheck( userId, permissionTypes ) );
        sql.append(simpleSearchFilter(simpleSearchFilter));
        
        if(count) {
        	sql.append( groupByHowFast() );
        } else {
        	sql.append( supervisorOrderByTemplateName ( sortAttributes ) );
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
        sql.append(" left outer join attribute_value inDate on inDate.card_id = clink.card_id and inDate.attribute_code = 'JBR_INCOMEDATE' ");
        sql.append(" WHERE c.status_id = 108 ");
        sql.append("   AND vlink.attribute_code = 'JBR_SIGN_SIGNING' ");
        sql.append("   AND clink.status_id = 108 ");
        sql.append("   AND vuser.attribute_code = 'JBR_SIGN_RESPONSIBLE' ");
        sql.append("   AND vuser.number_value = ").append(userId);
        sql.append("   AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = c.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) ");
        sql.append( "union " );
    	sql.append( 	"SELECT cin.card_id, cin.template_id, cin.status_id from card cin " );          
    	sql.append( 	" where cin.template_id = 1255 " );  
    	sql.append( 	" and cin.status_id =108 " );
    	sql.append( 	" and exists (select 1 from ATTRIBUTE_VALUE avin where avin.ATTRIBUTE_CODE = 'JBR_INFD_SGNEX_LINK' " );
    	sql.append( 	"   and avin.NUMBER_VALUE = ").append( userId ).append( " " );  
    	sql.append( 	"   and avin.CARD_ID = cin.CARD_ID " );
    	sql.append(     "   )"); 
        sql.append(" ) c inner join template t on (t.template_id=c.template_id)" );
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
    	return supervisorOrderBy(sortAttributes,false);
    }
    
    private static StringBuilder supervisorOrderByTemplateName(List<SortAttribute> sortAttributes) {
    	return supervisorOrderBy(sortAttributes,true);
    }
    
    private static StringBuilder supervisorOrderBy(List<SortAttribute> sortAttributes, boolean byTemplateName) {
    	if(null == sortAttributes || sortAttributes.size() != 1 || 
    			!"JBR_INFD_RECEIVER".equals(sortAttributes.get(0).getCode())) {
    		if(byTemplateName) {
    			return orderByTemplateName( sortAttributes );
    		}else return orderBy( sortAttributes );
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

    private static StringBuilder buildMinisterQuery(boolean count, int userId, long[] permissionTypes, 
    		int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
        StringBuilder sql = new StringBuilder();
        sql.append(Util.userPermissionCheckWithClause(userId));
        if(count) {
        	sql.append( "select count(*), " ).append( howFastColumn() );
        } else {
        	sql.append( "select CARD_ID, TEMPLATE_ID, STATUS_ID " );
        }
        
        sql.append("from (SELECT distinct c.card_id, c.template_id, c.status_id FROM card c ");
        sql.append(" LEFT JOIN attribute_value vlink ON (vlink.card_id = c.card_id AND vlink.attribute_code = 'JBR_SIGN_SIGNING') ");
        sql.append(" LEFT JOIN card clink ON (clink.card_id = vlink.number_value AND clink.status_id = 108) ");
        sql.append(" LEFT JOIN attribute_value vuser ON (vuser.card_id = clink.card_id AND vuser.attribute_code = 'JBR_SIGN_RESPONSIBLE' ) AND vuser.number_value = ").append(userId);
        sql.append(" LEFT JOIN attribute_value av_agr_link ON av_agr_link.card_id = c.card_id AND av_agr_link.attribute_code = 'JBR_VISA_VISA' ");       
        sql.append(" LEFT JOIN card cAgr ON cAgr.card_id = av_agr_link.number_value AND cAgr.status_id = 107 ");
        sql.append(" LEFT JOIN attribute_value av_agr_resp ON av_agr_resp.card_id = cAgr.card_id AND av_agr_resp.attribute_code = 'JBR_VISA_RESPONSIBLE' AND av_agr_resp.number_value = ").append(userId);
        sql.append(" WHERE c.status_id IN (107, 108) ");
        sql.append(" AND (vuser.card_id is not null OR av_agr_resp.card_id is not null) ");
        sql.append(" AND NOT EXISTS (SELECT 1 FROM attribute_value WHERE card_id = c.card_id AND attribute_code = 'JBR_ARM_HIDE' AND value_id = 1449) ");
        sql.append(") c" );
        sql.append(" WHERE " ).append( userPermissionCheck( userId, permissionTypes ) );
        sql.append(simpleSearchFilter(simpleSearchFilter));
        
        if(count) {
        	sql.append( groupByHowFast() );
        } else {
        	sql.append( orderBy( sortAttributes ) );
        	sql.append( limitAndOffset( page, pageSize ) );
        }
        
        return sql;
    }

    private static StringBuilder buildMinisterOrderQuery(boolean count, int userId, long[] permissionTypes, 
    		int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
        StringBuilder sql = new StringBuilder();
        sql.append(Util.userPermissionCheckWithClause(userId));
        if(count) {
        	sql.append( "select count(*), " ).append( howFastColumn() );
        } else {
        	sql.append( "select CARD_ID, TEMPLATE_ID, STATUS_ID " );
        }
        
        sql.append( " from (SELECT c.card_id, c.template_id, c.status_id  " +
                "                     from card c " +
                "            inner join attribute_value vLink on (vLink.card_id = c.card_id and vLink.attribute_code ='JBR_SIGN_SIGNING')  " +
                "            inner join attribute_value vType on (vType.card_id = c.card_id and vType.attribute_code ='JBR_INFD_TYPEDOC')  " +
                "            inner join card cLink on (cLink.card_id = vLink.number_value) " +
                "            inner join attribute_value vUser on (vUser.card_id = cLink.card_id and vUser.attribute_code = 'JBR_SIGN_RESPONSIBLE')\t            \t\t\t\t " +
                "      where    c.status_id = 108                               " +
                "           and cLink.status_id = 108                " +
                "           and vUser.number_value = ").append(userId).append(
                "           and vType.number_value in (900000807,900003032,900003012,900003013,900003033,900003014,900003015) " + ") c" );
        sql.append( " WHERE " ).append( userPermissionCheck( userId, permissionTypes ) );
        sql.append(simpleSearchFilter(simpleSearchFilter));
        
        if(count) {
        	sql.append( groupByHowFast() );
        } else {
        	sql.append( orderBy( sortAttributes ) );
        	sql.append( limitAndOffset( page, pageSize ) );
        }
        
        return sql;
    }

    private static StringBuilder buildMinisterHROrderQuery(boolean count, int userId, long[] permissionTypes, 
    		int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
        StringBuilder sql = new StringBuilder();
        sql.append(Util.userPermissionCheckWithClause(userId));
        if(count) {
        	sql.append( "select count(*), " ).append( howFastColumn() );
        } else {
        	sql.append( "select CARD_ID, TEMPLATE_ID, STATUS_ID " );
        }
        
        sql.append( "from (SELECT c.card_id, c.template_id, c.status_id  " +
                "        from card c " +
                "           inner join attribute_value vLink on (vLink.card_id = c.card_id and vLink.attribute_code ='JBR_SIGN_SIGNING') " +
                "            inner join attribute_value vType on (vType.card_id = c.card_id and vType.attribute_code ='JBR_INFD_TYPEDOC')  " +
                "            inner join card cLink on (cLink.card_id = vLink.number_value) " +
                "            inner join attribute_value vUser on (vUser.card_id = cLink.card_id and vUser.attribute_code = 'JBR_SIGN_RESPONSIBLE')\t            \t\t\t\t " +
                "        where c.status_id = 108                               " +
                "               and cLink.status_id = 108                " +
                "               and vUser.number_value = ").append(userId).append(" "+
                "               and vType.number_value = 900003016) c" );
        sql.append( " WHERE " ).append( userPermissionCheck( userId, permissionTypes ) );
        sql.append(simpleSearchFilter(simpleSearchFilter));
        
        if(count) {
        	sql.append( groupByHowFast() );
        } else {
        	sql.append( orderBy( sortAttributes ) );
        	sql.append( limitAndOffset( page, pageSize ) );
        }
        
        return sql;
    }

    private static StringBuilder buildMinisterLetterQuery(boolean count, int userId, long[] permissionTypes, 
    		int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
        StringBuilder sql = new StringBuilder();
        sql.append(Util.userPermissionCheckWithClause(userId));
        if(count) {
        	sql.append( "select count(*), " ).append( howFastColumn() );
        } else {
        	sql.append( "select CARD_ID, TEMPLATE_ID, STATUS_ID " );
        }
        
        sql.append( "from (SELECT c.card_id, c.template_id, c.status_id   " +
                "   from card c " +
                "inner join attribute_value vLink on (vLink.card_id = c.card_id and vLink.attribute_code ='JBR_SIGN_SIGNING')  " +
                "inner join attribute_value vType on (vType.card_id = c.card_id and vType.attribute_code ='JBR_INFD_TYPEDOC') " +
                "inner join card cLink on (cLink.card_id = vLink.number_value) " +
                "inner join attribute_value vUser on (vUser.card_id = cLink.card_id and vUser.attribute_code = 'JBR_SIGN_RESPONSIBLE')\t            \t\t\t\t " +
                "where c.status_id = 108                               " +
                "   and cLink.status_id = 108                " +
                "   and vUser.number_value = ").append(userId).append(" "+
                "   and vType.number_value in (900000805)) c" );
        sql.append( " WHERE " ).append( userPermissionCheck( userId, permissionTypes ) );
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
