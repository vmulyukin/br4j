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
import static com.aplana.dbmi.service.impl.workstation.Util.limitAndOffset;
import static com.aplana.dbmi.service.impl.workstation.Util.orderBy;

public class FavoritesWorkstationQuery extends JdbcDaoSupport {

	public static class AllFolderSupQuery extends JdbcDaoSupport implements AreaWorkstationQueryInterface {

		public List<EmptyCard> getCards(int userId, long[] permissionTypes,
				int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter) {
			StringBuilder sql = buildSupervisorQuery(false,
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

	private static StringBuilder buildSupervisorQuery( boolean count, int userId, long[] permissionTypes,
    		int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter ) {
        StringBuilder sql = new StringBuilder();
        sql.append(Util.userPermissionCheckWithClause(userId));
        if(count) {
        	sql.append( "select count(distinct c.card_id), " ).append( howFastColumn() );
        } else {
        	sql.append( "select c.card_id, c.template_id, c.status_id " );        }

        sql.append( "from attribute_value v " );
        sql.append( 	" inner join card c on c.card_id = v.number_value " );
        sql.append( "where v.attribute_code = 'JBR_FAV_DOC_DOC' " );
        sql.append( 	"and v.card_id in ( " );
        sql.append( 		"select CARD_ID " );
        sql.append( 		"from CARD c " );
        sql.append( 		"where c.TEMPLATE_ID = " ).append( 2250 );
        sql.append( 		    " and c.STATUS_ID = " ).append( 6883560 );
        sql.append( 			" and " ).append( numericValueCondition( "JBR_FAV_DOC_PERSON", userId ));
        sql.append( 			" and not exists (select 1 from attribute_value where card_id = c.card_id and attribute_code = 'JBR_ARM_HIDE' and value_id = 1449) " );
        sql.append( 	" ) " );
        sql.append(" and c.status_id not in (206,104,48909) ");
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