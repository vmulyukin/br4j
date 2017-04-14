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

public class ControlWorkstationQuery {
	
	public static class AllFolderMinisterQuery extends JdbcDaoSupport implements AreaWorkstationQueryInterface {

		public List getCards(int userId, long[] permissionTypes, int page,
				int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			
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
		
		public List getCardsQty(int userId, long[] permissionTypes, String simpleSearchFilter) {
			StringBuilder sql = buildMinisterQuery(true, userId, permissionTypes, 0, 0, null, simpleSearchFilter);
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
	
	private static StringBuilder buildMinisterQuery( boolean count, int userId, long[] permissionTypes, 
			int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter ) {
		
        StringBuilder sql = new StringBuilder();
        sql.append(Util.userPermissionCheckWithClause(userId));
        if(count) {
        	sql.append( "select count(*), " ).append( howFastColumn() );
        } else {
        	sql.append( "select CARD_ID, TEMPLATE_ID, STATUS_ID " );
        }
        
        sql.append( "from CARD c " );
        sql.append( "where c.STATUS_ID = " ).append( 103 );
        sql.append( 	"and c.TEMPLATE_ID = " ).append( 224 );
        sql.append( 	"and " ).append( listValueCondition( "JBR_IMPL_ONCONT", 1432 ) );
        sql.append( 	"and " ).append( listValueCondition( "JBR_IMPL_TYPECONT", new long[] {1451, 1452, 2133} ) );
        sql.append( 	"and " ).append( userPermissionCheck( userId, permissionTypes ) );
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
