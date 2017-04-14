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

import com.aplana.dbmi.model.workstation.AttributeValue;
import com.aplana.dbmi.model.workstation.SortAttribute;
import com.aplana.dbmi.service.impl.workstation.EmptyCard;
import com.aplana.dbmi.service.impl.workstation.Util;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.aplana.dbmi.service.impl.workstation.Util.*;

/**
 * @author Viktor Podoprigo
 */
public class AcquaintanceWorkstationQuery extends JdbcDaoSupport implements AreaWorkstationQueryInterface{

    /*public static VeryUrgentWorkstationQuery getInstance() {
        return ( VeryUrgentWorkstationQuery ) BEAN_CONTEXT.getBean( "acquaintanceWorkstationQuery" );
    }*/

    @SuppressWarnings("unchecked")
    public List getCards( int userId, long[] permissionTypes, int page, int pageSize, List<AttributeValue> sortAttributes ) {
        StringBuilder sql = buildSupervisorQuery( false, userId, permissionTypes, page, pageSize );
        return executeSimpleQuery( getJdbcTemplate(), sql.toString(), new RowMapper() {
            public Object mapRow( ResultSet rs, int rowNum ) throws SQLException {
                return new EmptyCard ( rs.getLong( 1 ), rs.getLong( 2 ) );
            }
        } );
    }

    @SuppressWarnings("unchecked")
    public List getCardsQty( int userId, long[] permissionTypes ) {
        StringBuilder sql = buildSupervisorQuery( true, userId, permissionTypes, 0, 0 );
        List queryResult = executeSimpleQuery( getJdbcTemplate(), sql.toString(), new RowMapper() {
            public Object mapRow( ResultSet rs, int rowNum ) throws SQLException {
            	return new Long[] { rs.getLong(1), rs.getLong(2) };
            }
        } );
        return queryResult;
    }

    private static StringBuilder buildSupervisorQuery( boolean count, int userId, long[] permissionTypes, int page, int pageSize ) {
        StringBuilder sql = new StringBuilder();
        sql.append(Util.userPermissionCheckWithClause(userId));
        sql.append( count ? 
                    "select count(*) " : "select CARD_ID, TEMPLATE_ID " );
        sql.append( "from card c where CARD_ID in ( " );
        
        sql.append( "select c.CARD_ID " );
        sql.append( "from card c " );
        sql.append(     "inner join attribute_value vLink on (vLink.card_id = c.card_id) " );
        sql.append(     "inner join card cLink on (cLink.card_id = vLink.number_value) " );
        sql.append(     "inner join attribute_value vUser on (vUser.card_id = cLink.card_id) " );
        sql.append(     "left outer join attribute_value inDate on (inDate.card_id = cLink.card_id and inDate.attribute_code = 'JBR_INCOMEDATE') ");
        sql.append( "where c.status_id in (101,102,103,104,48909) " );
        sql.append(     "and vLink.attribute_code = 'JBR_INFORM_LIST' " );
        sql.append(     "and cLink.status_id in (67424) " );
        sql.append(     "and vUser.attribute_code = 'JBR_FOR_INFORMATION' " );
        sql.append(     "and vUser.number_value = ").append( userId ).append( " " );
        sql.append( "and " ).append( userPermissionCheck( userId, permissionTypes ) );
        if (!count)
            sql.append(" ORDER BY inDate.date_value desc ");
        sql.append( ")");
        sql.append( limitAndOffset( page, pageSize ) );
        
        return sql;
    }

	public List getCards(int userId, long[] permissionTypes, int page,
			int pageSize, List<SortAttribute> sortAttributes,
			String simpleSearchFilter) {
		// TODO Auto-generated method stub
		return null;
	}

	public List getCardsQty(int userId, long[] permissionTypes,
			String simpleSearchFilter) {
		// TODO Auto-generated method stub
		return null;
	}
    
    
}
