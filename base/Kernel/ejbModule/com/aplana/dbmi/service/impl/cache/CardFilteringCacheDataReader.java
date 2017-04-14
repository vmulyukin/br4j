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
package com.aplana.dbmi.service.impl.cache;

import com.aplana.util.Table;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.aplana.dbmi.service.impl.workstation.Util.getDelimited;

/**
 * @author Denis Mitavskiy
 *         Date: 18.02.11
 */
public class CardFilteringCacheDataReader extends JdbcDaoSupport {
    public static final int CARD_ID_COLUMN = 0;
    public static final int UPPERCASED_STRING_VALUE_COLUMN = 1;
    public static final int STRING_VALUE_COLUMN = 2;

    public Table findCards( Set<Integer> templateIds, Integer statusId, String attributeCode ) {
        StringBuilder query = new StringBuilder();
        query.append( "select c.CARD_ID, av.STRING_VALUE " );
        query.append( "from CARD c " );
        query.append( "left outer join ATTRIBUTE_VALUE av on c.CARD_ID = av.CARD_ID and av.ATTRIBUTE_CODE = ? " );
        query.append( "where c.TEMPLATE_ID in ( " ).append( getDelimited( templateIds ) ).append( " ) " );
        if ( statusId != null ) {
            query.append( "and c.STATUS_ID = ? " );
        }

        Object[] queryParams = statusId == null ? new Object[] { attributeCode } : new Object[] { attributeCode, statusId };
        List queryResult = getJdbcTemplate().query( query.toString(), queryParams, new RowMapper() {
            public Object mapRow( ResultSet rs, int rowNum ) throws SQLException {
                return new Object[] { rs.getLong( 1 ), rs.getString( 2 ) };
            }
        } );

        Table result = new Table( queryResult.size() );
        result.add( "CARD_ID" );
        result.add( "STRING_VALUE_UPPERCASE" );
        result.add( "STRING_VALUE" );
        int rowNum = 0;
        for ( Object rowObj : queryResult ) {
            Object[] row = ( Object[] ) rowObj;
            String stringValue = (String) row[ 1 ];
            result.set( CARD_ID_COLUMN, row[ 0 ], rowNum );
            result.set( UPPERCASED_STRING_VALUE_COLUMN, stringValue == null ? null : stringValue.toUpperCase(), rowNum );
            result.set( STRING_VALUE_COLUMN, stringValue, rowNum );
            ++rowNum;
        }
        result.sort( "STRING_VALUE", true );
        return result.trimToSize();
    }

    public Map<Long, Boolean> findCardsPermissions( List<Long> cardIds, Long personId, List<Long> permissionTypes ) {
        StringBuilder query = new StringBuilder();
        query.append( "select c.CARD_ID " );
        query.append( "from CARD c " );
        query.append( "where c.CARD_ID in ( " ).append( getDelimited( cardIds ) ).append( ") " );
        query.append( "and ( " );
        query.append(     "exists ( " );
        query.append(         "select 1 " );
        query.append(         "from CARD_ACCESS ca " );
        query.append(         "where " );
        query.append(         "ca.OBJECT_ID = c.STATUS_ID " );
        query.append(         "and ca.TEMPLATE_ID = c.TEMPLATE_ID " );

        if ( permissionTypes != null && !permissionTypes.isEmpty() ) {
            query.append(         "and ca.PERMISSION_TYPE in ( " ).append( getDelimited( permissionTypes ) ).append( ") " );
        }

        query.append(         "and ( " );
        query.append(             "ca.PERSON_ATTRIBUTE_CODE is null " );
        query.append(             "or exists (select 1 from ATTRIBUTE_VALUE av where av.CARD_ID =c.CARD_ID and av.ATTRIBUTE_CODE = ca.PERSON_ATTRIBUTE_CODE and av.NUMBER_VALUE = ? ) " );
        query.append(         ") " );
        query.append(         "and ( " );
        query.append(             "ca.ROLE_CODE is NULL " );
        
//    	� ������ ������ BR4J00036917 ������� ����������� ����-������
        
//        query.append(             "or exists ( " );
//        query.append(                 "select 1 " );
//        query.append(                 "from PERSON_ROLE pr " );
//        query.append(                 "left join PERSON_ROLE_TEMPLATE prt on pr.PROLE_ID = prt.PROLE_ID " );
//        query.append(                 "where " );
//        query.append(                 "pr.ROLE_CODE = ca.ROLE_CODE " );
//        query.append(                 "and pr.PERSON_ID = ? " );
//        query.append(                 "and (prt.TEMPLATE_ID = ca.TEMPLATE_ID or prt.PROLE_ID is null) " );
//        query.append(             ") " );
        query.append(         ") " );
        query.append(     ") " );
        query.append( ") " );

        List queryResult = getJdbcTemplate().query( query.toString(), new Object[] { personId, personId }, new RowMapper() {
            public Object mapRow( ResultSet rs, int rowNum ) throws SQLException {
                return new Object[] { rs.getLong( 1 ) };
            }
        } );

        HashMap<Long, Boolean> result = new HashMap<Long, Boolean>( queryResult.size() );
        for ( Long cardId : cardIds ) { // we suppose person has no access to any of these cards
            result.put( cardId, Boolean.FALSE );
        }
        for ( Object row : queryResult ) { // and now we correct those card ids person really has access to
            Object[] rowArray = ( Object[] ) row;
            result.put( ( Long ) rowArray[ 0 ], Boolean.TRUE );
        }
        return result;
    }
}
