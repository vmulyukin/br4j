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

import static com.aplana.dbmi.service.impl.workstation.Util.BEAN_CONTEXT;
import static com.aplana.dbmi.service.impl.workstation.Util.executeSimpleQuery;
import static com.aplana.dbmi.service.impl.workstation.Util.getDelimited;
import static com.aplana.dbmi.service.impl.workstation.Util.getDelimitedWithNoEscaping;
import static com.aplana.dbmi.service.impl.workstation.Util.isEmpty;
import static com.aplana.dbmi.service.impl.workstation.Util.numericValueCondition;
import static com.aplana.dbmi.service.impl.workstation.Util.userPermissionCheck;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.aplana.dbmi.model.AccessCard;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.workstation.AttributeValue;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.access.AccessRuleManager;
import com.aplana.dbmi.service.impl.workstation.AttributeDef;
import com.aplana.dbmi.service.impl.workstation.CardService;
import com.aplana.dbmi.service.impl.workstation.Util;
import com.aplana.dbmi.utils.SimpleDBUtils;

/**
 * @author Denis Mitavskiy
 *         Date: 13.04.11
 *         Time: 16:38
 */
public class CommonWorkstationQuery extends JdbcDaoSupport {
    public static CommonWorkstationQuery getInstance() {
        return ( CommonWorkstationQuery ) BEAN_CONTEXT.getBean( "commonWorkstationQuery" );
    }

    public List getTemplateIdAndStatusIdByCardId( long cardId ) {
        StringBuilder sql = new StringBuilder( "select TEMPLATE_ID, STATUS_ID from CARD c " );
        sql.append( "where CARD_ID = " ).append( cardId );
        return executeSimpleQuery( getJdbcTemplate(), sql, new RowMapper() {
            public Object mapRow( ResultSet resultSet, int i ) throws SQLException {
                return new long[] {
                    resultSet.getLong( 1 ),
                    resultSet.getLong( 2 )
                };
            }
        } );
    }
    
    public List getTemplateIdAndStatusIdByCardIds( long[] cardIds, long[] statusIds, long[] templateIds, 
    		List<List<String>> attributeCodeGroups, long currentUserId, int limit) {
        StringBuilder sql = new StringBuilder( "select CARD_ID, TEMPLATE_ID, STATUS_ID from CARD c " );
        sql.append( "where CARD_ID in (" ).append( getDelimited(cardIds) ).append( ") " );
        
        if(null != statusIds && statusIds.length > 0) {
        	sql.append( "and STATUS_ID in (" ).append( getDelimited(statusIds) ).append( ") " );
        }
        
        if(null != templateIds && templateIds.length > 0) {
        	sql.append( "and TEMPLATE_ID in (" ).append( getDelimited(templateIds) ).append( ") " );
        }
        
        if(null != attributeCodeGroups && !attributeCodeGroups.isEmpty()) {
        	for(List<String> attributeCodesGroup : attributeCodeGroups) {
        		if(null == attributeCodesGroup || attributeCodesGroup.isEmpty()) {
        			continue;	
        		}
        		
        		sql.append( "and ( " );
        		
        		boolean orIsNeeded = false;
        		for(String attributeCode : attributeCodesGroup) {
        			if(orIsNeeded) {
        				sql.append( "or " );
        			}
        			sql.append( numericValueCondition( attributeCode, currentUserId) ).append( " " );
        			if(!orIsNeeded) {
        				orIsNeeded = true;
        			}
        		}
        		sql.append(" ) ");
        	}
        }
        
        if(limit > 0) {
        	sql.append(" limit ").append(limit);
        }
        
        return executeSimpleQuery( getJdbcTemplate(), sql, new RowMapper() {
            public Object mapRow( ResultSet resultSet, int i ) throws SQLException {
                return new long[] {
                    resultSet.getLong( 1 ),
                    resultSet.getLong( 2 ), 
                    resultSet.getLong( 3 )
                };
            }
        } );
    }

    public List getBackLinkedCardsByAttribute( long[] cardIds, Collection<String> backLinkAttributeCodes ) {
        StringBuilder sql = new StringBuilder();
        sql.append( "select distinct v.NUMBER_VALUE CARD_ID, v.CARD_ID BACKLINK_CARD_ID " );
        sql.append( "from ATTRIBUTE_VALUE v " );
        sql.append( "where v.ATTRIBUTE_CODE in (" ).append( getDelimitedWithNoEscaping( backLinkAttributeCodes ) ).append( ") " );
        sql.append( "and v.NUMBER_VALUE in (" ).append(getDelimited(cardIds)).append( ") " );
        return executeSimpleQuery( getJdbcTemplate(), sql, new RowMapper() {
            public Object mapRow( ResultSet resultSet, int i ) throws SQLException {
                return new long[] {
                    resultSet.getLong( 1 ),
                    resultSet.getLong( 2 )
                };
            }
        } );
    }

    public List getAttributeDefinitionByCode( final String code ) {
        StringBuilder sql = new StringBuilder( "select ATTR_NAME_RUS, ATTR_NAME_ENG, DATA_TYPE, BLOCK_CODE, ORDER_IN_BLOCK, COLUMN_WIDTH, IS_ACTIVE, IS_SYSTEM, IS_MANDATORY, REF_CODE, IS_READONLY, IS_HIDDEN from ATTRIBUTE " );
        sql.append( "where ATTRIBUTE_CODE = '" ).append( code ).append( "' " );
        return executeSimpleQuery( getJdbcTemplate(), sql, new RowMapper() {
            public Object mapRow( ResultSet resultSet, int i ) throws SQLException {
                AttributeDef attrDef = new AttributeDef();
                attrDef.setCode( code );
                attrDef.setNameRu( resultSet.getString( 1 ) );
                attrDef.setNameEn( resultSet.getString( 2 ) );
                attrDef.setType( resultSet.getString( 3 ) );
                attrDef.setBlockCode( resultSet.getString( 4 ) );
                attrDef.setIndexInBlock( resultSet.getInt( 5 ) );
                attrDef.setColumnWidth( resultSet.getInt( 6 ) );
                attrDef.setActive( resultSet.getInt( 7 ) != 0 );
                attrDef.setSystem( resultSet.getInt( 8 ) != 0 );
                attrDef.setMandatory( resultSet.getInt( 9 ) != 0 );
                attrDef.setRefCode( resultSet.getString( 10 ) );
                attrDef.setReadOnly( resultSet.getInt( 11 ) != 0 );
                attrDef.setHidden( resultSet.getInt( 12 ) != 0 );
                return attrDef;
            }
        } );
    }

    public String getBackLinkAttributeCodeForAttributeCode( String code ) {
        StringBuilder sql = new StringBuilder( "select OPTION_VALUE from ATTRIBUTE_OPTION " );
        sql.append( "where ATTRIBUTE_CODE = '" ).append( code ).append( "' and OPTION_CODE = 'LINK' " );
        List result = executeSimpleQuery( getJdbcTemplate(), sql, new RowMapper() {
            public Object mapRow( ResultSet resultSet, int i ) throws SQLException {
                return resultSet.getString( 1 );
            }
        } );
        return result == null ? null : (String) result.get( 0 );
    }

    public List getAttributeDefinitionByRussianName( final String nameRu ) {
        StringBuilder sql = new StringBuilder( "select ATTRIBUTE_CODE, ATTR_NAME_ENG, DATA_TYPE, BLOCK_CODE, ORDER_IN_BLOCK, COLUMN_WIDTH, IS_ACTIVE, IS_SYSTEM, IS_MANDATORY, REF_CODE, IS_READONLY, IS_HIDDEN from ATTRIBUTE " );
        sql.append( "where ATTR_NAME_RUS = '" ).append( nameRu ).append( "' " );
        return executeSimpleQuery( getJdbcTemplate(), sql, new RowMapper() {
            public Object mapRow( ResultSet resultSet, int i ) throws SQLException {
                AttributeDef attrDef = new AttributeDef();
                attrDef.setCode( resultSet.getString( 1 ) );
                attrDef.setNameRu( nameRu );
                attrDef.setNameEn( resultSet.getString( 2 ) );
                attrDef.setType( resultSet.getString( 3 ) );
                attrDef.setBlockCode( resultSet.getString( 4 ) );
                attrDef.setIndexInBlock( resultSet.getInt( 5 ) );
                attrDef.setColumnWidth( resultSet.getInt( 6 ) );
                attrDef.setActive( resultSet.getInt( 7 ) != 0 );
                attrDef.setSystem( resultSet.getInt( 8 ) != 0 );
                attrDef.setMandatory( resultSet.getInt( 9 ) != 0 );
                attrDef.setRefCode( resultSet.getString( 10 ) );
                attrDef.setReadOnly( resultSet.getInt( 11 ) != 0 );
                attrDef.setHidden( resultSet.getInt( 12 ) != 0 );
                return attrDef;
            }
        } );
    }

    public List getAttributeOptions( String code ) {
        StringBuilder sql = new StringBuilder( "select OPTION_CODE, OPTION_VALUE from ATTRIBUTE_OPTION " );
        sql.append( "where ATTRIBUTE_CODE = '" ).append( code ).append( "' " );
        return executeSimpleQuery( getJdbcTemplate(), sql, new RowMapper() {
            public Object mapRow( ResultSet resultSet, int i ) throws SQLException {
                return new CardService.AttributeOptionDataRow(
                    resultSet.getString( 1 ),
                    resultSet.getString( 2 )
                );
            }
        } );
    }

    public List getAllTemplateDefinitions() {
        StringBuilder sql = new StringBuilder( "select TEMPLATE_NAME_RUS, TEMPLATE_NAME_ENG, IS_ACTIVE, IS_SYSTEM, LOCKED_BY, LOCK_TIME, SHOW_IN_CREATECARD, SHOW_IN_SEARCH, TEMPLATE_ID from TEMPLATE " );
        return executeSimpleQuery( getJdbcTemplate(), sql, new RowMapper() {
            public Object mapRow( ResultSet resultSet, int i ) throws SQLException {
                Template template = new Template();
                template.setNameRu( resultSet.getString( 1 ) );
                template.setNameEn( resultSet.getString( 2 ) );
                template.setActive( resultSet.getInt( 3 ) != 0 );
                template.setSystem( resultSet.getInt( 4 ) != 0 );
                BigDecimal locker = resultSet.getBigDecimal( 5 );
                if ( locker != null ) {
                    template.setLocker( locker.longValue() );
                }
                Timestamp lockTime = resultSet.getTimestamp( 6 );
                template.setLockTime( lockTime );
                template.setShowInCreateCard( resultSet.getInt( 7 ) == 1 );
                template.setShowInSearch( resultSet.getInt( 8 ) == 1 );
                template.setId( resultSet.getLong( 9 ) );
                return template;
            }
        } );
    }

    public List getTemplateDefinitionById( final int id ) {
        StringBuilder sql = new StringBuilder( "select TEMPLATE_NAME_RUS, TEMPLATE_NAME_ENG, IS_ACTIVE, IS_SYSTEM, LOCKED_BY, LOCK_TIME, SHOW_IN_CREATECARD, SHOW_IN_SEARCH from TEMPLATE " );
        sql.append( "where TEMPLATE_ID = " ).append( id ).append( " " );
        return executeSimpleQuery( getJdbcTemplate(), sql, new RowMapper() {
            public Object mapRow( ResultSet resultSet, int i ) throws SQLException {
                Template template = new Template();
                template.setId( id );
                template.setNameRu( resultSet.getString( 1 ) );
                template.setNameEn( resultSet.getString( 2 ) );
                template.setActive( resultSet.getInt( 3 ) != 0 );
                template.setSystem( resultSet.getInt( 4 ) != 0 );
                BigDecimal locker = resultSet.getBigDecimal( 5 );
                if ( locker != null ) {
                    template.setLocker( locker.longValue() );
                }
                Timestamp lockTime = resultSet.getTimestamp( 6 );
                template.setLockTime( lockTime );
                template.setShowInCreateCard( resultSet.getInt( 7 ) == 1 );
                template.setShowInSearch( resultSet.getInt( 8 ) == 1 );
                return template;
            }
        } );
    }

    public List getStatusDefinitionById( final int id ) {
        StringBuilder sql = new StringBuilder( "select NAME_RUS, NAME_ENG from CARD_STATUS " );
        sql.append( "where STATUS_ID = " ).append( id ).append( " " );
        return executeSimpleQuery( getJdbcTemplate(), sql, new RowMapper() {
            public Object mapRow( ResultSet resultSet, int i ) throws SQLException {
                return new Object[] { resultSet.getString( 1 ), resultSet.getString( 2 ) };
            }
        } );
    }
    
    public List getStatusDefinitionByName( final String name ) {
        StringBuilder sql = new StringBuilder( "select STATUS_ID, NAME_ENG from CARD_STATUS " );
        sql.append( "where NAME_RUS = '" ).append( name ).append( "' " );
        return executeSimpleQuery( getJdbcTemplate(), sql, new RowMapper() {
            public Object mapRow( ResultSet resultSet, int i ) throws SQLException {
                return new Object[] { resultSet.getLong( 1 ), resultSet.getString( 2 ) };
            }
        } );
    }

    public List<ObjectId> getAllowedCards( ObjectId[] cardIds, ObjectId userId ) {
        List<ObjectId> verifiedCardIds = new ArrayList<ObjectId>();
        for(ObjectId checkCardId : cardIds) {
           AccessRuleManager manager = new AccessRuleManager(getJdbcTemplate());
           if (manager.isCardOperationAllowed(checkCardId, AccessCard.READ, userId, true)) {
              verifiedCardIds.add(checkCardId);
            }
    }
        return verifiedCardIds;
    }

    public List getPlainAttributeValues( long[] cardIds, CardService.AttributesByCategory attributesByCategory, int userId, long[] userPermissionTypes ) {
    	Integer id =  getSystemUserId();
    	if(id!=null){
    		userId=id;
    	}
    	
    	
        StringBuilder directAttributesQuery = getDirectAttributeValuesQuery( cardIds, attributesByCategory.allAttributesForPlainDirectQuery, false, false, false, userId, userPermissionTypes );
        StringBuilder listAttributesQuery = getDirectAttributeValuesQuery( cardIds, attributesByCategory.listAttributes, false, false, true, userId, userPermissionTypes );
        StringBuilder indirectAttributesQuery = getLinkedAttributeValuesQuery( cardIds, attributesByCategory.plainLinkedAttributes, false, false, false, false, userId, userPermissionTypes );
        StringBuilder indirectByPersonAttributesQuery = getLinkedAttributeValuesQuery( cardIds, attributesByCategory.plainLinkedByPersonAttributes, false, false, false, true, userId, userPermissionTypes );
        StringBuilder personDirectAttributesQuery = getDirectAttributeValuesQuery( cardIds, attributesByCategory.personDirectAttributes, false, true, false, userId, userPermissionTypes );
        StringBuilder personIndirectAttributesQuery = getLinkedAttributeValuesQuery( cardIds, attributesByCategory.personLinkedAttributes, false, true, false, false, userId, userPermissionTypes );
        StringBuilder backLinkAttributesQuery = getBackLinkedAttributeValuesQuery( cardIds, attributesByCategory.backLinkAttributes, userId, userPermissionTypes );

        
        ArrayList<StringBuilder> queriesToCombine = new ArrayList<StringBuilder>();
        queriesToCombine.add( directAttributesQuery );
        queriesToCombine.add( listAttributesQuery );
        queriesToCombine.add( indirectAttributesQuery );
        queriesToCombine.add( indirectByPersonAttributesQuery );
        queriesToCombine.add( personDirectAttributesQuery );
        queriesToCombine.add( personIndirectAttributesQuery );
        queriesToCombine.add( backLinkAttributesQuery );

        ArrayList<Collection<AttributeValue>> queriesAttributes = new ArrayList<Collection<AttributeValue>>();
        queriesAttributes.add( attributesByCategory.allAttributesForPlainDirectQuery );
        queriesAttributes.add( attributesByCategory.listAttributes );
        queriesAttributes.add( attributesByCategory.plainLinkedAttributes );
        queriesAttributes.add( attributesByCategory.plainLinkedByPersonAttributes );
        queriesAttributes.add( attributesByCategory.personDirectAttributes );
        queriesAttributes.add( attributesByCategory.personLinkedAttributes );
        queriesAttributes.add( attributesByCategory.backLinkAttributes );
        StringBuilder sql = buildUnionQuery( queriesToCombine, queriesAttributes );
        if ( sql == null ) {
            return null;
        }
        final Calendar calendar = Calendar.getInstance();
        return executeSimpleQuery( getJdbcTemplate(), sql.toString(), new RowMapper() {
            public Object mapRow( ResultSet rs, int rowNum ) throws SQLException {
                return new CardService.AttributeDataRow(
                    rs.getLong( 1 ),
                    rs.getString( 2 ),
                    rs.getString( 3 ),
                    rs.getBigDecimal( 4 ),
                    rs.getString( 5 ),
                    rs.getTimestamp( 6, calendar ),
                    rs.getBigDecimal( 7 ),
                    rs.getBigDecimal( 8 )
                );
            }
        } );
    }

    public List getBinaryTextAttributeValues( long[] cardIds, CardService.AttributesByCategory attributesByCategory, int userId, long[] userPermissionTypes ) {
    	Integer id =  getSystemUserId();
    	if(id!=null){
    		userId=id;
    	}
        StringBuilder directAttributesQuery = getDirectAttributeValuesQuery( cardIds, attributesByCategory.htmlDirectAttributes, true, false, false, userId, userPermissionTypes );
        StringBuilder indirectAttributesQuery = getLinkedAttributeValuesQuery( cardIds, attributesByCategory.htmlIndirectAttributes, true, false, false, false, userId, userPermissionTypes );
        StringBuilder sql = buildUnionQuery( directAttributesQuery, attributesByCategory.htmlDirectAttributes, indirectAttributesQuery, attributesByCategory.htmlIndirectAttributes );
        if ( sql == null ) {
            return null;
        }
        return executeSimpleQuery( getJdbcTemplate(), sql.toString(), new RowMapper() {
            public Object mapRow( ResultSet rs, int rowNum ) throws SQLException {
                String text;
                try {
                    text = SimpleDBUtils.getBlobAsStr( rs, 4, "UTF-8" );
                } catch ( UnsupportedEncodingException e ) {
                    text = null;
                }
                return new CardService.TextAttributeDataRow (
                    rs.getLong( 1 ),
                    rs.getString( 2 ),
                    rs.getString( 3 ),
                    text
                );
            }
        } );
    }

    /**
     * retrieves filter for CardLinkAttribute stored in the XML_DATA table
     * @param xmlDataId id
     * @return String Blob representation
     */
    public String getXmlFilter(String xmlDataId) {
        if (xmlDataId == null || xmlDataId.length()==0 ) return null;
        String sql = "select xml_data from xml_data where xml_data_id = " + xmlDataId;
        List list = executeSimpleQuery(getJdbcTemplate(), sql, new RowMapper() {
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                String text;
                try {
                    text = SimpleDBUtils.getBlobAsStr(rs, 1, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    text = null;
                }
                return text;
            }
        });
        if (list == null || list.isEmpty()) return null;
        return (String) list.get(0);
    }

    private static StringBuilder buildUnionQuery( StringBuilder query1, Collection<AttributeValue> query1Attributes, StringBuilder query2, Collection<AttributeValue> query2Attributes ) {
        ArrayList<StringBuilder> queries = new ArrayList<StringBuilder>();
        queries.add( query1 );
        queries.add( query2 );

        ArrayList<Collection<AttributeValue>> attributes = new ArrayList<Collection<AttributeValue>>();
        attributes.add( query1Attributes );
        attributes.add( query2Attributes );
        return buildUnionQuery( queries, attributes );
    }

    private static StringBuilder buildUnionQuery( ArrayList<StringBuilder> queries, ArrayList<Collection<AttributeValue>> queriesAttributes ) {
        boolean allQueriesEmpty = true;
        ArrayList<StringBuilder> notEmptyQueries = new ArrayList<StringBuilder>( queries.size() );
        for ( int i = 0, queriesAttributesSize = queriesAttributes.size(); i < queriesAttributesSize; i++ ) {
            Collection<AttributeValue> attributes = queriesAttributes.get( i );
            if ( !isEmpty( attributes ) ) {
                allQueriesEmpty = false;
                notEmptyQueries.add( queries.get( i ) );
            }
        }
        if ( allQueriesEmpty ) {
            return null;
        }
        StringBuilder firstQuery = notEmptyQueries.get( 0 );
        if ( notEmptyQueries.size() == 1 ) {
            return firstQuery;
        }
        StringBuilder result = firstQuery;
        for ( int i = 1, notEmptyAttributesIndexSize = notEmptyQueries.size(); i < notEmptyAttributesIndexSize; i++ ) {
            result.append( " union all " );
            result.append( notEmptyQueries.get( i ) );
        }
        return result;
    }

    private StringBuilder getDirectAttributeValuesQuery( long[] cardIds, Collection<AttributeValue> attributes, boolean getBinaryText, boolean getPerson, boolean getValueId, int userId, long[] userPermissionTypes ) {
        if ( isEmpty( attributes ) ) {
            return new StringBuilder();
        }
        StringBuilder sql = new StringBuilder( "(" );
        if ( userId >= 0 )
        	sql.append(Util.userPermissionCheckWithClause(userId) );
        if ( getBinaryText ) {
            sql.append( "select av.CARD_ID, av.ATTRIBUTE_CODE, null LINKED_CARD_ATTRIBUTE_CODE, av.LONG_BINARY_VALUE STRING_VALUE " );
        } else if ( getPerson ) {
            sql.append( "select av.CARD_ID, av.ATTRIBUTE_CODE, null LINKED_CARD_ATTRIBUTE_CODE, p.PERSON_ID NUMBER_VALUE, p.FULL_NAME STRING_VALUE, null::timestamp DATE_VALUE, p.CARD_ID VALUE_ID, null::numeric PARENT_VALUE_ID " );
        } else if ( getValueId ) {
            sql.append( "select av.CARD_ID, av.ATTRIBUTE_CODE, null LINKED_CARD_ATTRIBUTE_CODE, av.NUMBER_VALUE, vl.VALUE_RUS STRING_VALUE, null::timestamp DATE_VALUE, av.VALUE_ID, vl.PARENT_VALUE_ID " );
        } else {
            sql.append( "select av.CARD_ID, av.ATTRIBUTE_CODE, null LINKED_CARD_ATTRIBUTE_CODE, av.NUMBER_VALUE, av.STRING_VALUE, av.DATE_VALUE, av.VALUE_ID, null::numeric PARENT_VALUE_ID " );
        }
        sql.append( "from ATTRIBUTE_VALUE av " );
        if ( userId >= 0 ) {
            sql.append( "inner join CARD c on av.CARD_ID = c.CARD_ID " );
        }
        if ( getPerson ) {
            sql.append( "inner join PERSON p on av.NUMBER_VALUE = p.PERSON_ID " );
        }
        if ( getValueId ) {
            sql.append( "left join VALUES_LIST vl on av.VALUE_ID = vl.VALUE_ID " );
        }
        sql.append( getWhereClauseForAttributesRetrieval( cardIds, attributes, true, userId, userPermissionTypes ) );
        sql.append("\n order by av.attr_value_id) \n");
        return sql;
    }
    
    private StringBuilder getBackLinkedAttributeValuesQuery( long[] cardIds, Collection<AttributeValue> attributes, int userId, long[] userPermissionTypes ) {
        if ( isEmpty( attributes ) ) {
            return new StringBuilder();
        }
        StringBuilder sql = new StringBuilder( "(" );
        if ( userId >= 0 )
        	sql.append(Util.userPermissionCheckWithClause(userId) );
        sql.append( "(select c.CARD_ID, o.ATTRIBUTE_CODE, null LINKED_CARD_ATTRIBUTE_CODE, av.CARD_ID, null STRING_VALUE, null::timestamp DATE_VALUE, null::numeric VALUE_ID, null::numeric PARENT_VALUE_ID \n" );
        sql.append( "from CARD c \n" );
        if ( userId >= 0 ) {
            sql.append( "inner join ATTRIBUTE_VALUE av on c.CARD_ID = av.NUMBER_VALUE \n" );
            sql.append( "inner join attribute_option o on o.option_value = av.attribute_code \n" );
        }
        
        sql.append( "where \n" );
        sql.append( "c.CARD_ID in (" ).append( getDelimited( cardIds ) ).append( ") " );
        if ( userId >= 0 ) {
        	sql.append( " \n and o.attribute_code in ( " ).append( getDelimitedWithNoEscaping( AttributeValue.getCodes( attributes ) ) ).append( ") " );
            sql.append( " \n and o.option_code ='LINK' \n" );
        	sql.append( "and " ).append( Util.userPermissionCheck( userId, userPermissionTypes, "c" ) );
        	sql.append( "\n order by av.attr_value_id \n");
        }
        sql.append( "	) \n");
        
        sql.append( "UNION \n" );
        
        sql.append( "(select c.CARD_ID, o.ATTRIBUTE_CODE, null LINKED_CARD_ATTRIBUTE_CODE, av.CARD_ID, null STRING_VALUE, null::timestamp DATE_VALUE, null::numeric VALUE_ID, null::numeric PARENT_VALUE_ID \n" );
        sql.append( "from CARD c \n" );
        if ( userId >= 0 ) {
            sql.append( "inner join ATTRIBUTE_VALUE av on c.CARD_ID = av.NUMBER_VALUE \n" );
            sql.append( "inner join attribute_option o on o.option_value = av.attribute_code \n" );
            sql.append( "inner join attribute_option o1 on o1.attribute_code = o.attribute_code \n" );
        }
        
        sql.append( "where \n" );
        sql.append( "c.CARD_ID in (" ).append( getDelimited( cardIds ) ).append( ") " );
        if ( userId >= 0 ) {
        	sql.append( " \n and o.attribute_code in ( " ).append( getDelimitedWithNoEscaping( AttributeValue.getCodes( attributes ) ) ).append( ") " );
            sql.append( " \n and o.option_code ='UPLINK' \n" );
            sql.append( "and o1.attribute_code in ( " ).append( getDelimitedWithNoEscaping( AttributeValue.getCodes( attributes ) ) ).append( ") " );
            sql.append( " \n and o1.option_code ='LINK' \n" );
        	sql.append( "and " ).append( Util.userPermissionCheck( userId, userPermissionTypes, "c" ) );
        	sql.append("\n order by av.attr_value_id \n");
        }
        sql.append("	) \n");
        sql.append(") \n");
        
        return sql;
    }

    private StringBuilder getLinkedAttributeValuesQuery( long[] cardIds, Collection<AttributeValue> attributes, boolean getBinaryText, boolean getPerson, boolean getValueId, boolean linkedByPerson, int userId, long[] userPermissionTypes ) {
        if ( isEmpty( attributes ) ) {
            return new StringBuilder();
        }
        StringBuilder sql = new StringBuilder( 500 );
        if ( userId > 0 )
        	sql.append(Util.userPermissionCheckWithClause(userId) );
        if ( getBinaryText ) {
            sql.append( "select av.CARD_ID, av.ATTRIBUTE_CODE, av_inner.ATTRIBUTE_CODE LINKED_CARD_ATTRIBUTE_CODE, av_inner.LONG_BINARY_VALUE STRING_VALUE " );
        } else if ( getValueId ) {
            sql.append( "select av.CARD_ID, av.ATTRIBUTE_CODE, null LINKED_CARD_ATTRIBUTE_CODE, av.NUMBER_VALUE, vl.VALUE_RUS STRING_VALUE, null::timestamp DATE_VALUE, av.VALUE_ID, vl.PARENT_VALUE_ID " );
        } else if ( getPerson ) {
            sql.append( "select av.CARD_ID, av.ATTRIBUTE_CODE, av_inner.ATTRIBUTE_CODE LINKED_CARD_ATTRIBUTE_CODE, p.PERSON_ID NUMBER_VALUE, p.FULL_NAME STRING_VALUE, null::timestamp DATE_VALUE, p.CARD_ID VALUE_ID, null::numeric PARENT_VALUE_ID " );
        } else {
            sql.append( "select av.CARD_ID, av.ATTRIBUTE_CODE, av_inner.ATTRIBUTE_CODE LINKED_CARD_ATTRIBUTE_CODE, av_inner.NUMBER_VALUE, av_inner.STRING_VALUE, av_inner.DATE_VALUE, av_inner.VALUE_ID, null::numeric PARENT_VALUE_ID " );
        }
        sql.append( "from ATTRIBUTE_VALUE av " );

        // we don't check that it is a link. a client which wants this linked attribute should know, whether it's a link or not!
        if(linkedByPerson) {
            sql.append( "inner join person p on av.NUMBER_VALUE = p.person_id inner join ATTRIBUTE_VALUE av_inner on p.card_id = av_inner.CARD_ID " ); 
        } else {
            sql.append( "inner join ATTRIBUTE_VALUE av_inner on av.NUMBER_VALUE = av_inner.CARD_ID " );
        }

        if ( userId >= 0 ) {
            sql.append( "inner join CARD c_inner on av_inner.CARD_ID = c_inner.CARD_ID " ); //todo on demand
        }
        if ( getValueId ) {
            sql.append( "inner join VALUES_LIST vl on av_inner.VALUE_ID = vl.VALUE_ID " );
        }
        if ( getPerson ) {
            sql.append( "inner join PERSON p on av_inner.NUMBER_VALUE = p.PERSON_ID " );
        }
        sql.append( getWhereClauseForAttributesRetrieval( cardIds, attributes, false, userId, userPermissionTypes ) );
        return sql;
    }

    private StringBuilder getWhereClauseForAttributesRetrieval( long[] cardIds, Collection<AttributeValue> attributes, boolean directAttributes, int userId, long[] userPermissionTypes ) {
        if ( directAttributes ) {
            StringBuilder fromClause = new StringBuilder( 500 );
            fromClause.append( "where " );
            fromClause.append( "av.CARD_ID in (" ).append( getDelimited( cardIds ) ).append( ") " );
            fromClause.append( "and av.ATTRIBUTE_CODE in (" ).append( getDelimitedWithNoEscaping( AttributeValue.getCodes( attributes ) ) ).append( ") " );
            if ( userId >= 0 ) {
                fromClause.append( "and " ).append( Util.userPermissionCheck( userId, userPermissionTypes, "c" ) );
            }

            return fromClause;
        }

        StringBuilder whereClause = new StringBuilder( 500 );
        whereClause.append( "where " );
        whereClause.append( "av.CARD_ID in (" ).append( getDelimited( cardIds ) ).append( ") " );
        whereClause.append( "and ( " );
        boolean addOr = false;
        for ( AttributeValue attribute : attributes ) {
            if ( addOr ) {
                whereClause.append( " or " );
            }
            whereClause.append( "av.ATTRIBUTE_CODE = '" ).append( attribute.getCode() ).append( "' and av_inner.ATTRIBUTE_CODE = '" ).append( attribute.getLinkedCode() ).append( "' " );
            addOr = true;

        }
        whereClause.append( ") " );
        if ( userId > 0 ) {
            whereClause.append( "and " ).append( Util.userPermissionCheck( userId, userPermissionTypes, "c_inner" ) );
        }
        return whereClause;
    }
    
	
	public Integer getSystemUserId(){
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("SELECT person_id FROM person WHERE person_login='")
					 .append(Database.SYSTEM_USER)
					 .append("'");
		
		List personIds = executeSimpleQuery(getJdbcTemplate(),stringBuilder.toString(), new RowMapper() {			
			public Object mapRow(ResultSet rs, int arg1) throws SQLException {				
				return new Integer(rs.getInt(1));
			}
		});
		if(!personIds.isEmpty()){
			return (Integer)personIds.get(0);
		}
		
		return null;
	}

}
