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

public class ApproveWorkstationQuery extends JdbcDaoSupport implements AreaWorkstationQueryInterface {

	public List getCards( int userId, long[] permissionTypes, int page, int pageSize, 
			List<SortAttribute> sortAttributes, String simpleSearchFilter) {
        StringBuilder sql = buildSupervisorQuery( false, userId, permissionTypes, page, pageSize, 
        		sortAttributes, simpleSearchFilter );
        return executeSimpleQuery( getJdbcTemplate(), sql.toString(), new RowMapper() {
            public Object mapRow( ResultSet rs, int rowNum ) throws SQLException {
            	return new EmptyCard ( rs.getLong( 1 ), rs.getLong( 2 ), rs.getLong( 3 ) );
            }
        } );
    }

    public List getCardsQty( int userId, long[] permissionTypes, String simpleSearchFilter ) {
        StringBuilder sql = buildSupervisorQuery( true, userId, permissionTypes, 0, 0, null, simpleSearchFilter );
        List queryResult = executeSimpleQuery( getJdbcTemplate(), sql.toString(), new RowMapper() {
            public Object mapRow( ResultSet rs, int rowNum ) throws SQLException {
                return new Long[] { rs.getLong( 1 ), rs.getLong( 2 ) };
            }
        } );
        return queryResult;
    }   
	
	private static StringBuilder buildSupervisorQuery( boolean count, int userId, long[] permissionTypes, 
			int page, int pageSize, List<SortAttribute> sortAttributes, String simpleSearchFilter ) {
    	StringBuilder sql = new StringBuilder();
    	sql.append(Util.userPermissionCheckWithClause(userId));
    	if(count) {
    		sql.append( "select count(distinct c.CARD_ID), " ).append( howFastColumn() );
    	} else {
    		sql.append( "select c.CARD_ID, c.TEMPLATE_ID, STATUS_ID " );
    	}
    	//������ ��� ���������� ���������, ���� ���� ����� �� ���������� �� ����������� � ������ ��������
        sql.append( "from card c where card_id in " );
        sql.append("(select DISTINCT avMain.number_value " );
    	sql.append( "from card c " );
    	sql.append( 	"inner join attribute_value vr on (vr.number_value = c.card_id) " );
    	sql.append( 	"inner join card cr on (cr.card_id = vr.card_id) " );
    	sql.append(     "INNER JOIN attribute_value vs ON (vs.card_id = c.card_id) " );
    	sql.append(     "INNER JOIN attribute_value avParent�xec on (avParent�xec.card_id = c.card_id) " );
    	sql.append(     "INNER JOIN attribute_value avExec ON (avExec.card_id = cr.card_id ) " );
    	sql.append(     "INNER JOIN attribute_value avMain ON (avMain.card_id = c.card_id ) " );

    	//sql.append(     "INNER JOIN person pers ON (vs.number_value = pers.card_id) " );
    	sql.append( "where c.template_id = 324 " );
    	sql.append( 	"and c.status_id in (103,206) " );
    	sql.append( 	"and vr.attribute_code ='ADMIN_702311' " );
    	sql.append( 	"and cr.template_id = 1044 " );
    	sql.append( 	"and cr.status_id = 206 ");
        sql.append(     "AND vs.attribute_code IN ( 'JBR_INFD_SGNEX_LINK') " );
    	sql.append(     "AND vs.number_value = ").append( userId ).append( " " );
    	//����������� � ���������
    	sql.append(     "AND avParent�xec.attribute_code IN ('JBR_INFD_EXEC_LINK') " );
    	//����������� � ������ �� ����������
    	sql.append(     "AND avExec.attribute_code = 'ADMIN_702335' " );
    	sql.append(     "AND avExec.number_value = avParent�xec.number_value " );
    	sql.append(     "AND avMain.attribute_code = 'JBR_MAINDOC' " );
    	sql.append( 	"and not exists (select 1 from attribute_value where card_id = cr.card_id and attribute_code = 'JBR_ARM_HIDE' and value_id = 1449) " );
    	
    	sql.append(" UNION ");
    	
    	// ������ ��� ����������� ���������, ���� ����� �� ���������� ������������� ��������� � ������� ��������.   	    	
        sql.append("select DISTINCT avMain.number_value " );
    	sql.append( "from card c " );
    	sql.append( 	"inner join attribute_value vr on (vr.number_value = c.card_id) " );
    	sql.append( 	"inner join card cr on (cr.card_id = vr.card_id) " );
    	sql.append( 	"inner join attribute_value avParentCoexec on (avParentCoexec.card_id = c.card_id) " );
    	sql.append( 	"INNER JOIN attribute_value avCoexec ON (avCoexec.card_id = cr.card_id ) " );    	    	    	   
    	sql.append(     "INNER JOIN attribute_value vs ON (vs.card_id = c.card_id) " );
    	sql.append(     "INNER JOIN attribute_value avMain ON (avMain.card_id = c.card_id ) " );
    	sql.append(     "JOIN attribute_value av_rep on c.card_id = av_rep.number_value and av_rep.attribute_code = 'ADMIN_702311' ");
    	sql.append( 	"where c.template_id = 324 " );
    	sql.append( 	"and c.status_id in (103,206) " );
    	sql.append( 	"and vr.attribute_code ='ADMIN_702311' " );
        sql.append(     "and cr.template_id = 1044 " );
        sql.append(     "and cr.status_id = 206 ");
        sql.append(     "AND avParentCoexec.attribute_code IN ('ADMIN_255974') ");
        sql.append(     "AND avCoexec.attribute_code = 'ADMIN_702335' ");
        sql.append(     "AND avCoexec.number_value = avParentCoexec.number_value ");                        		
       
        sql.append(     "AND vs.attribute_code IN ( 'JBR_INFD_EXEC_LINK') " );
    	sql.append(     "AND vs.number_value = ").append( userId ).append(" ");
    	sql.append("    AND avMain.attribute_code = 'JBR_MAINDOC' ");
    	sql.append("    and not exists (select 1 from attribute_value where card_id = cr.card_id and attribute_code = 'JBR_ARM_HIDE' and value_id = 1449) ");
		sql.append(     "group by cr.card_id, avMain.number_value ");
		sql.append(     "having cr.card_id != min(av_rep.card_id) ");

    	sql.append(" UNION \n");
    	
    	sql.append("select DISTINCT avMain.number_value " );
        sql.append( "from card c " );
        sql.append(     "inner join attribute_value vr on (vr.number_value = c.card_id) " );
        sql.append(     "inner join card cr on (cr.card_id = vr.card_id) " );
        sql.append(     "inner join attribute_value ve on (ve.card_id = cr.card_id) " );
        sql.append(     "INNER JOIN attribute_value avMain ON (avMain.card_id = c.card_id ) " );
        sql.append( "where c.template_id = 324 " );
        sql.append(     "and c.status_id in (103,206) " );
        sql.append(     "and vr.attribute_code ='ADMIN_702311' " );
        sql.append(     "and cr.template_id = 1044 " );
        sql.append(     "and cr.status_id = 206 ");
        sql.append(     "AND ve.attribute_code IN ( 'JBR_RPT_DR_APPROVER') ");
        sql.append(     "AND ve.number_value = ").append( userId ).append( " " );
        sql.append(     "AND avMain.attribute_code = 'JBR_MAINDOC' " );
    	sql.append( 	"and not exists (select 1 from attribute_value where card_id = cr.card_id and attribute_code = 'JBR_ARM_HIDE' and value_id = 1449) " );
    	sql.append("UNION " );
        sql.append("select c.card_id " );
    	sql.append( "from card c " );
    	sql.append( 	"inner join attribute_value vr on (vr.number_value = c.card_id) " );
    	sql.append( 	"inner join card cr on (cr.card_id = vr.card_id) " );
    	sql.append( 	"inner join attribute_value ve on (ve.card_id = c.card_id) " );
    	sql.append( "where c.template_id = 1255 " );
    	sql.append( 	"and c.status_id in (103,206) " );
    	sql.append( 	"and vr.attribute_code ='ADMIN_702311' " );
    	sql.append( 	"and cr.template_id = 1044 " );
    	sql.append( 	"and cr.status_id = 206 ");
    	sql.append( 	"and ve.attribute_code = 'JBR_INFD_SGNEX_LINK' " );
    	sql.append( 	"and ve.number_value = ").append( userId ).append( " " );
    	sql.append( 	"and not exists (select 1 from attribute_value where card_id = cr.card_id and attribute_code = 'JBR_ARM_HIDE' and value_id = 1449) \n" );
    	sql.append("UNION \n" );
        sql.append("select c.card_id " );
    	sql.append( "from card c " );
    	sql.append( 	"inner join attribute_value vr on (vr.number_value = c.card_id) " );
    	sql.append( 	"inner join card cr on (cr.card_id = vr.card_id) " );
    	sql.append( 	"inner join attribute_value avParentCoexec on (avParentCoexec.card_id = c.card_id) " );
    	sql.append( 	"inner join attribute_value avCoexec on (avCoexec.card_id = cr.card_id ) " );
    	sql.append( 	"inner join attribute_value ve on (ve.card_id = c.card_id) " );
    	sql.append( "where c.template_id = 1255 " );
    	sql.append( 	"and c.status_id in (103,206) " );
    	sql.append( 	"and vr.attribute_code ='ADMIN_702311' " );
    	sql.append( 	"and cr.template_id = 1044 " );
    	sql.append( 	"and cr.status_id = 206 ");
    	sql.append(     "and avParentCoexec.attribute_code IN ('ADMIN_255974') ");
        sql.append(     "and avCoexec.attribute_code = 'ADMIN_702335' ");
        sql.append(     "and avCoexec.number_value = avParentCoexec.number_value ");
        sql.append(     "and ve.attribute_code IN ('JBR_INFD_EXEC_LINK') " );
    	sql.append(     "and ve.number_value = ").append( userId ).append( " " );
    	sql.append( 	"and not exists (select 1 from attribute_value where card_id = cr.card_id and attribute_code = 'JBR_ARM_HIDE' and value_id = 1449) " );
    	    	
        sql.append(")" );
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
