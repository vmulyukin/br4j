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
package com.aplana.dbmi.service.impl.workstation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardAccess;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.LocalizedString;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.workstation.AttributeValue;
import com.aplana.dbmi.model.workstation.NullsSortingPolicy;
import com.aplana.dbmi.model.workstation.SortAttribute;
import com.aplana.dbmi.model.workstation.Status;
import com.aplana.dbmi.service.impl.mapper.AttributeValueMapperUtils;
import com.aplana.dbmi.service.impl.query.AttributeTypes;
import com.aplana.dbmi.utils.StrUtils;

/**
 * @author Denis Mitavskiy
 *         Date: 11.04.11
 */
@SuppressWarnings("deprecation")
public class Util implements ApplicationContextAware {
    public static ApplicationContext BEAN_CONTEXT;
    public static final String ENDL = "\r\n";

    private static final List<String> SORTABLE_ATTRIBUTES = Arrays.asList(
    						AttributeTypes.STRING, AttributeTypes.TEXT, AttributeTypes.INTEGER,
    						AttributeTypes.TREE, AttributeTypes.DATE, AttributeTypes.LIST, AttributeTypes.PERSON);

    public static boolean isEmpty( Collection<?> collection ) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty( String string ) {
        return string == null || string.length() == 0;
    }

	@SuppressWarnings("rawtypes")
	public static List executeSimpleQuery( JdbcTemplate jdbcTemplate, Object sql, RowMapper mapper ) {
        String caller = getCaller();
        String query = sql.toString();
        long t1 = System.nanoTime();
        List result = jdbcTemplate.query( query, new Object[ 0 ], mapper );
        long t2 = System.nanoTime();
        long nanoTime = t2 - t1;
        SQLPerformanceLogger.getInstance().log( caller, query, nanoTime, result.size() );
        return result;
    }

    public static long toMillies( long nanos ) {
        return nanos / 1000000L;
    }

    public static StringBuilder getDelimited( long[] ids ) {
        StringBuilder result = new StringBuilder();
        boolean notFirst = false;
        for ( long id : ids ) {
            if ( notFirst ) {
                result.append( ',' );
            } else {
                notFirst = true;
            }
            result.append( id );
        }
        return result;
    }

    public static <T> StringBuilder getDelimited( Collection<T> ids ) {
        StringBuilder result = new StringBuilder();
        boolean notFirst = false;
        for ( T id : ids ) {
            if ( notFirst ) {
                result.append( ',' );
            } else {
                notFirst = true;
            }
            result.append( id );
        }
        return result;
    }

    public static StringBuilder getDelimitedWithNoEscaping( Collection<String> strings ) {
        StringBuilder result = new StringBuilder();
        boolean notFirst = false;
        for ( String str : strings ) {
            if ( notFirst ) {
                result.append( ',' );
            } else {
                notFirst = true;
            }
            result.append( "'" ).append( str ).append( "'" );
        }
        return result;
    }

    private static String appendQuotes(String value) {
    	
    	return  "'"+ value + "'";
    	
    }
    
    
    public static StringBuilder dateValueLessOrEqualNow( String attributeCode) {
        StringBuilder result = new StringBuilder( 250 );
        attributeCode = appendQuotes(attributeCode);
        result.append( "exists (select 1 from ATTRIBUTE_VALUE av where av.ATTRIBUTE_CODE = " ).append( attributeCode ).append( " and av.date_VALUE <= now()  ").append(" and av.CARD_ID = c.CARD_ID) " );
        return result;
    }

    public static StringBuilder dateValueLessNow( String attributeCode) {
        StringBuilder result = new StringBuilder( 250 );
        attributeCode = appendQuotes(attributeCode);
        result.append( "exists (select 1 from ATTRIBUTE_VALUE av where av.ATTRIBUTE_CODE = " ).append( attributeCode ).append( " and av.date_VALUE < now()  ").append(" and av.CARD_ID = c.CARD_ID) " );
        return result;
    }

    public static StringBuilder dateValueGreaterOrEqualNow( String attributeCode) {
        StringBuilder result = new StringBuilder( 250 );
        attributeCode = appendQuotes(attributeCode);
        result.append( "exists (select 1 from ATTRIBUTE_VALUE av where av.ATTRIBUTE_CODE = " ).append( attributeCode ).append( " and av.date_VALUE >= now()  ").append(" and av.CARD_ID = c.CARD_ID) " );
        return result;
    }

    public static StringBuilder dateValueGreaterNow( String attributeCode) {
        StringBuilder result = new StringBuilder( 250 );
        attributeCode = appendQuotes(attributeCode);
        result.append( "exists (select 1 from ATTRIBUTE_VALUE av where av.ATTRIBUTE_CODE = " ).append( attributeCode ).append( " and av.date_VALUE > now()  ").append(" and av.CARD_ID = c.CARD_ID) " );
        return result;
    }
    
    public static StringBuilder dateValueInPastDays( String attributeCode, int days) {
        StringBuilder result = new StringBuilder( 250 );
        attributeCode = appendQuotes(attributeCode);
        result.append( "exists (select 1 from ATTRIBUTE_VALUE av where av.ATTRIBUTE_CODE = " ).append( attributeCode ).append( " and av.date_VALUE > date_trunc('day', now() - INTERVAL '")
        	.append(days).append(" days') " ).append(" and av.CARD_ID = c.CARD_ID) " );
        return result;
    }
    
    public static StringBuilder numericValueCondition( String attributeCode, long value ) {
        StringBuilder result = new StringBuilder( 250 );
        attributeCode = appendQuotes(attributeCode);
        result.append( "exists (select 1 from ATTRIBUTE_VALUE av where av.ATTRIBUTE_CODE in (" ).append( attributeCode ).append( ") and av.NUMBER_VALUE = " ).append( value ).append( " and av.CARD_ID = c.CARD_ID) " );
        return result;
    }
    
    public static StringBuilder numericValueCondition( String attributeCode, long[] values ) {
        StringBuilder result = new StringBuilder( 250 );
        attributeCode = appendQuotes(attributeCode);
        result.append( "exists (select 1 from ATTRIBUTE_VALUE av where av.ATTRIBUTE_CODE = " ).append( attributeCode ).append( " and av.NUMBER_VALUE in (" ).append( getDelimited(values) ).append( ") and av.CARD_ID = c.CARD_ID) " );
        return result;
    }

    public static StringBuilder numericValueCondition( String attributeCode, String value ) {
        StringBuilder result = new StringBuilder( 250 );
        attributeCode = appendQuotes(attributeCode);
        result.append( "exists (select 1 from ATTRIBUTE_VALUE av where av.ATTRIBUTE_CODE = " ).append( attributeCode ).append( " and av.NUMBER_VALUE in (" ).append( value ).append( ") and av.CARD_ID = c.CARD_ID) " );
        return result;
    }

    public static StringBuilder numericIntervalCondition( String attributeCode, long min, long max ) {
        StringBuilder result = new StringBuilder( 250 );
        attributeCode = appendQuotes(attributeCode);
        result.append( "exists (select 1 from ATTRIBUTE_VALUE av where av.ATTRIBUTE_CODE = " ).append( attributeCode ).append( " and av.NUMBER_VALUE between " ).append( min ).append( " and " ).append( max ).append( " and av.CARD_ID = c.CARD_ID) " );
        return result;
    }

    public static StringBuilder numericIntervalCondition( String attributeCode, String min, String max ) {
        StringBuilder result = new StringBuilder( 250 );
        attributeCode = appendQuotes(attributeCode);
        result.append( "exists (select 1 from ATTRIBUTE_VALUE av where av.ATTRIBUTE_CODE = " ).append( attributeCode ).append( " and av.NUMBER_VALUE between " ).append( min ).append( " and " ).append( max ).append( " and av.CARD_ID = c.CARD_ID) " );
        return result;
    }

    public static StringBuilder listValueCondition( String attributeCode, long value ) {
        StringBuilder result = new StringBuilder( 250 );
        attributeCode = appendQuotes(attributeCode);
        result.append( "exists (select 1 from ATTRIBUTE_VALUE av where av.ATTRIBUTE_CODE = " ).append( attributeCode ).append( " and av.VALUE_ID = " ).append( value ).append( " and av.CARD_ID = c.CARD_ID) " );
        return result;
    }

    public static StringBuilder listValueCondition( String attributeCode, long[] values ) {
        StringBuilder result = new StringBuilder( 250 );
        attributeCode = appendQuotes(attributeCode);
        result.append( "exists (select 1 from ATTRIBUTE_VALUE av where av.ATTRIBUTE_CODE = " ).append( attributeCode ).append( " and av.VALUE_ID in (" ).append( getDelimited(values) ).append( ") and av.CARD_ID = c.CARD_ID) " );
        return result;
    }
    
    private static String buildDateOrderBy(List<SortAttribute> sortAttributes){
    	StringBuilder result = new StringBuilder();
    	String attrType = null;
    	SortAttribute sortAttribute=null;
    	int originalSize = sortAttributes.size();
    	String order = null;
    	
    	
    	
    	
    	Iterator<SortAttribute> iterator = sortAttributes.iterator();
    	result.append("(");
		result.append("select tmp_2.date_value from( \n");
		result.append("select distinct tmp.card_id, max(tmp.date_value) as date_value from (\n");
		boolean first = true;
    	while(iterator.hasNext()) {
    		sortAttribute = iterator.next();	
    		attrType = AttributeDef.convertToString(sortAttribute.getType());
    		if(!(AttributeTypes.DATE.equals(attrType)&&sortAttribute.getCode()!=null)){
    			continue;
    		}
    		if(first){
    			first = false;
    		} else result.append(" UNION " );
    		
    		if(!sortAttribute.isAttributeFromLink()){
	            result.append(" Select avOrderD.card_id, avOrderD.date_value from attribute_value avOrderD ");
	            result.append("where ");
	            //result.append("avOrderD.card_id = c.card_id and ");
	            result.append("avOrderD.attribute_code in ( ");
	    		result.append("'").append(sortAttribute.getCode()).append("') ");
	    	}else{
	            result.append(" Select avOrderD.card_id, avOrderD1.date_value from attribute_value avOrderD ");
	            result.append(" join  attribute_value avOrderD1 on avOrderD1.card_id=avOrderD.number_value and avOrderD1.attribute_code in (");
	            result.append("'").append(sortAttribute.getLinkedCode()).append("') ");
	            result.append("where ");
	            //result.append("avOrderD.card_id = c.card_id and ");
	            result.append("avOrderD.attribute_code in ( ");
	    		result.append("'").append(sortAttribute.getCode()).append("') ");
	    	}
    		if(order==null){
    			order = sortAttribute.isAsc()?"asc":"desc";
    		}	
            iterator.remove();
		}
    	result.append(") as tmp\n");
    	result.append("where tmp.card_id = c.card_id\n");
    	result.append("group by tmp.card_id) as tmp_2\n");
    	result.append(") ");
    	result.append(order).append(" ");
    	return originalSize!=sortAttributes.size()?result.toString():"";
    }

    public static StringBuilder orderBy( List<SortAttribute> sortAttributes, boolean byTemplateName) {
    	StringBuilder result = new StringBuilder();
    	
    	int count = 0;
    	
    	if(null != sortAttributes && sortAttributes.size() > 0) { 
    		List<SortAttribute> sortCopyAttributes = new ArrayList<SortAttribute>();
    		sortCopyAttributes.addAll(sortAttributes);
    		String orderDate = buildDateOrderBy(sortCopyAttributes);
    		if(!orderDate.isEmpty()){
    			result.append("order by ").append(orderDate);
    			count=1;
    		}
    		for(SortAttribute sortAttribute: sortCopyAttributes) {
    			String attrType = AttributeDef.convertToString(sortAttribute.getType());
    			if(!SORTABLE_ATTRIBUTES.contains(attrType) && !sortAttribute.isByTemplate() && !sortAttribute.isByStatus()) {
    				continue;
    			}
    			
    			if(count == 0) {
    				result.append("order by ");
    			} else {
    				result.append(", ");
    			}

    			result.append(buildOrderSql(sortAttribute, count, byTemplateName));

                if(sortAttribute.isAsc()) {
                    result.append("asc ");
                } else {
                    result.append("desc ");
                }
                            
                if(NullsSortingPolicy.NULLS_ALWAYS_FIRST.equals(sortAttribute.getNullsFirst())) {
    				result.append("nulls first");
    			} else if(NullsSortingPolicy.NULLS_ALWAYS_LAST.equals(sortAttribute.getNullsFirst())) {
    				result.append("nulls last");
    			}
                
    			count ++;
    		}
    	}
    	
    	if(count == 0) {
    		result.append("order by ");
    	} else {
    		result.append(", ");
    	}
    	result.append("c.card_id desc ");
        
    	return result;
    }
    
    public static StringBuilder orderBy( List<SortAttribute> sortAttributes) {
    	return orderBy(sortAttributes, false);
    }
    
    public static StringBuilder orderByTemplateName( List<SortAttribute> sortAttributes) {
    	return orderBy(sortAttributes, true);
    }
    
    private static String buildOrderSql(SortAttribute sortAttribute, int num, boolean byTemplateName){
    	String attrType = AttributeDef.convertToString(sortAttribute.getType());
        if(sortAttribute.isByTemplate()) {
        	if(byTemplateName) {
        		return "t.template_name_rus ";
        	} else return "c.template_id ";
        } else if (sortAttribute.isByStatus()) {
        	return "c.status_id ";
        } else if(AttributeTypes.PERSON.equals(attrType)) {
        	return PersonOrderSubqueryBuilder.buildClause(sortAttribute, num);
        } else {
        	return AttributeValueOrderSubqueryBuilder.buildClause(sortAttribute, num);
        }   	
    }
    
    public static StringBuilder getOrderByColumnsList( List<SortAttribute> sortAttributes) {
    	StringBuilder result = new StringBuilder();
    	
    	int count = 0;
    	
    	if(null != sortAttributes && !sortAttributes.isEmpty()) {
    		
    		for(Map.Entry<Integer, List<SortAttribute>> entry : mapSortAttributesByGroup(sortAttributes).entrySet()) {
    			
    			StringBuilder tmpSb = new StringBuilder();
    			// ����� ������
    			int group = entry.getKey();
    			//������ ��������� � ������
    			List<SortAttribute> attrList = entry.getValue();
    			
    			for(SortAttribute sortAttribute : attrList) {
    				String attrType = AttributeDef.convertToString(sortAttribute.getType());
    				
    				if(sortAttribute.isByTemplate() || sortAttribute.isByStatus()) {
    					count++;
                        continue;
    				}
    				
    				tmpSb.append(", ");
    				
    				if(AttributeTypes.PERSON.equals(attrType)) {
                        tmpSb.append(PersonOrderSubqueryBuilder.buildClause(sortAttribute, count));
                    } else {
                        tmpSb.append(AttributeValueOrderSubqueryBuilder.buildClause(sortAttribute, count));
                    }
    				count++;
    				
    			}
    			
    			if(tmpSb != null && tmpSb.length() > 0) {
	    			result.append(", coalesce(null");
	    			result.append(tmpSb);
	    			result.append(") orderField" + group);
	    			result.append(" ");
    			}
    			
    		}
    		/*
    		for(SortAttribute sortAttribute: sortAttributes) {
    			String attrType = AttributeDef.convertToString(sortAttribute.getType());

                if(sortAttribute.isByTemplate() || sortAttribute.isByStatus()) {
                    count ++;
                    continue;
                }

    			if(!SORTABLE_ATTRIBUTES.contains(attrType)) {
    				continue;
    			}         			
    			
				result.append(", ");
    			
    			if(AttributeTypes.PERSON.equals(attrType)) {
                    result.append(PersonOrderSubqueryBuilder.buildClause(sortAttribute, count));
                } else {
                    result.append(AttributeValueOrderSubqueryBuilder.buildClause(sortAttribute, count));
                }
    			
				result.append("orderField" + count);
    			count ++;
    		}*/
    	}
    	/*
    	if(count > 0) {
    		result.append(" ");
    	}
        */
    	return result;
    }
    
    private static Map<Integer, List<SortAttribute>> mapSortAttributesByGroup(List<SortAttribute> attrs) {
		
		Map<Integer, List<SortAttribute>> result = new HashMap<Integer, List<SortAttribute>>();
		
		for(SortAttribute attr : attrs) {
			String attrType = AttributeDef.convertToString(attr.getType());
			if(!SORTABLE_ATTRIBUTES.contains(attrType))
				continue;
			
			int groupId = attr.getSortGroup();
			if(result.containsKey(groupId)) {
				result.get(groupId).add(attr);
			} else  {
				List<SortAttribute> resAttrs = new ArrayList<SortAttribute>();
				resAttrs.add(attr);
				result.put(groupId, resAttrs);
			}
		}
		return result;
	}
    
    public static StringBuilder getOrderByClauseWithColumnsList( List<SortAttribute> sortAttributes ) {
    	return getOrderByClauseWithColumnsList(sortAttributes, false);
    }
    
    public static StringBuilder getOrderByClauseWithColumnsList( List<SortAttribute> sortAttributes, boolean byTemplateName ) {
    	StringBuilder result = new StringBuilder();
    	
    	int count = 0;
    	
    	if(null != sortAttributes && sortAttributes.size() > 0) {
    		
    		for(Map.Entry<Integer, List<SortAttribute>> entry : mapSortAttributesByGroup(sortAttributes).entrySet()) {
    			
    			// ����� ������
    			int group = entry.getKey();
    			List<SortAttribute> attrList = entry.getValue();
    			//����� ������ ������� �� ������
    			SortAttribute attr = attrList.get(0);
    			
    			
    			if(count == 0) {
    				result.append("order by ");
    			} else {
    				result.append(", ");
    			}
    			
    			if(attr.isByTemplate()) {
    				if (byTemplateName){
    					result.append("c.template_name_rus ");
    				}else {
    					result.append("c.template_id ");
    				}
                } else if(attr.isByStatus()) {
                    result.append("c.status_id ");
                } else {
    			    result.append("orderField").append(group).append(" ");
                }
    			
    			if(attr.isAsc()) {
    				result.append("asc ");
    			} else {
    				result.append("desc ");
    			}
    			
                if(NullsSortingPolicy.NULLS_ALWAYS_FIRST.equals(attr.getNullsFirst())) {
    				result.append("nulls first");
    			} else if(NullsSortingPolicy.NULLS_ALWAYS_LAST.equals(attr.getNullsFirst())) {
    				result.append("nulls last");
    			}
    			
    			count++;
    		}
    		
    		
    		/*
    		for(SortAttribute sortAttribute: sortAttributes) {
    			String attrType = AttributeDef.convertToString(sortAttribute.getType());
    			if(!SORTABLE_ATTRIBUTES.contains(attrType) && !sortAttribute.isByTemplate() && !sortAttribute.isByStatus()) {
    				continue;
    			}
    			
    			if(count == 0) {
    				result.append("order by ");
    			} else {
    				result.append(", ");
    			}

                if(sortAttribute.isByTemplate()) {
                    result.append("c.template_id ");
                } else if(sortAttribute.isByStatus()) {
                    result.append("c.status_id ");
                } else {
    			    result.append("orderField").append(count).append(" ");
                }
    			
    			if(sortAttribute.isAsc()) {
    				result.append("asc ");
    			} else {
    				result.append("desc ");
    			}
    			
                if(sortAttribute.isNullsFirst()) {
    				result.append("nulls first");
    			} else {
    				result.append("nulls last");
    			}
    			
    			count ++;
    		}*/
    	}
    	
    	if(count == 0) {
    		result.append("order by ");
    	} else {
    		result.append(", ");
    	}
    	result.append("c.card_id desc ");
        
    	return result;
    }
    
    public static StringBuilder limitAndOffset( int page, int pageSize ) {
        StringBuilder result = new StringBuilder();
        if ( pageSize <= 0 ) {
            return result;
        }
        int offset = pageSize * ( page - 1 );
        result.append( "offset " ).append( offset ).append( " limit " ).append( pageSize ).append( " " );
        return result;
    }

    public static StringBuilder userPermissionCheck( int userId, long[] permissionTypes ) {
        return userPermissionCheck( userId, permissionTypes, "c" );
    }

    public static StringBuilder userPermissionCheck(int userId, long[] permissionTypes, String alias) {
    	StringBuilder destSqlBuf = new StringBuilder(1000);
    	destSqlBuf.append("\t( \n");
    	destSqlBuf.append("\t\tEXISTS (SELECT act.card_id from card act where act.card_id=" + alias + ".card_id and act.is_active = 1) OR \n");
		destSqlBuf.append("\t\tEXISTS (SELECT role_code FROM person_role WHERE person_id=" + userId + " AND role_code = 'A')) \n"); 
		
		destSqlBuf.append("AND\n");
		destSqlBuf.append("\t(EXISTS (\n");
		destSqlBuf.append("\t\tSELECT 1\n");
		destSqlBuf.append("\t\tFROM access_list a_l\n");
		destSqlBuf.append("\t\tJOIN access_card_rule a_cr1 ON a_l.rule_id=a_cr1.rule_id\n");
		destSqlBuf.append("\t\tWHERE a_cr1.operation_code='R' \n");
		destSqlBuf.append("\t\t\tAND a_l.person_id=" + userId + "\n");
		destSqlBuf.append("\t\t\tAND " + alias + ".card_id=a_l.card_id\n");
	
		destSqlBuf.append("\t) OR EXISTS (\n");
		destSqlBuf.append("\t\tSELECT 1\n");
		destSqlBuf.append("\t\tFROM access_1 ar WHERE \n");
		destSqlBuf.append("\t\t\t    (" + alias + ".template_id=ar.template_id)\n");
		destSqlBuf.append("\t\t\tAND (" + alias + ".status_id  =ar.status_id)\n");
		
		destSqlBuf.append("\t) OR EXISTS (\n");  // all users permissions
		destSqlBuf.append("\t\tSELECT 1\n");
		destSqlBuf.append("\t\tFROM access_2 ar WHERE \n");
		destSqlBuf.append("\t\t\t    (" + alias + ".template_id=ar.template_id)\n");
		destSqlBuf.append("\t\t\tAND (" + alias + ".status_id  =ar.status_id)\n");
		
		destSqlBuf.append("\t) -- EXISTS\n");
		destSqlBuf.append(") -- AND  \n");
		return destSqlBuf;
    }
    
    public static StringBuilder userPermissionCheckWithClause(int userId) {
    	StringBuilder withBlock = new StringBuilder();
		withBlock.append("WITH access_1 as (\n");
		withBlock.append("\tSELECT template_id, status_id \n");
		withBlock.append("\tFROM role_access_rule a_rr \n");
		withBlock.append("\tJOIN access_rule a_r ON a_rr.rule_id=a_r.rule_id \n");
		withBlock.append("\tJOIN access_card_rule a_cr ON a_r.rule_id=a_cr.rule_id and a_cr.operation_code='R' \n");
		withBlock.append("\tJOIN person_role a_pr ON a_rr.role_code=a_pr.role_code AND a_pr.person_id="+userId+"\n");
		withBlock.append("),\n");
		withBlock.append("access_2 as ( \n");
		withBlock.append("\tSELECT template_id, status_id \n");
		withBlock.append("\tFROM role_access_rule a_rr \n");
		withBlock.append("\tJOIN access_rule a_r ON a_rr.rule_id=a_r.rule_id AND a_rr.role_code IS NULL \n");
		withBlock.append("\tJOIN access_card_rule a_cr ON a_r.rule_id=a_cr.rule_id and a_cr.operation_code='R' \n");
		withBlock.append(") -- WITH END\n");
		
		return withBlock;
    }

    @Deprecated
    public static StringBuilder userPermissionCheckPrev( int userId, long[] permissionTypes ) {
        return userPermissionCheckPrev( userId, permissionTypes, "c" );
    }

    @Deprecated
    public static StringBuilder userPermissionCheckPrev( int userId, long[] permissionTypes, String cardTablePrefix ) {
        StringBuilder sql = new StringBuilder( 1000 );
        sql.append( " ( " );
        sql.append(     "exists ( " );
        sql.append(         "select 1 " );
        sql.append(         "from CARD_ACCESS ca " );
        sql.append(         "where 0=0 " );
        if ( permissionTypes != null && permissionTypes.length != 0 ) {
			if (CardAccess.NO_VERIFYING.longValue() == permissionTypes[0]) {
				sql.setLength(0);
				sql.append("(exists ( select 1 ))");
				return sql;
			}
            sql.append(         "and ca.PERMISSION_TYPE in (" ).append( getDelimited( permissionTypes ) ).append( ") " );
        }
        sql.append(         "and ca.OBJECT_ID = " ).append( cardTablePrefix ).append( ".STATUS_ID " );
        sql.append(         "and ca.TEMPLATE_ID = " ).append( cardTablePrefix ).append( ".TEMPLATE_ID " );


        sql.append(         "and ( " );
        sql.append(             "ca.PERSON_ATTRIBUTE_CODE is null " );
        sql.append(             "or exists (select 1 from ATTRIBUTE_VALUE av where av.CARD_ID = ").append( cardTablePrefix ).append( ".CARD_ID and av.ATTRIBUTE_CODE = ca.PERSON_ATTRIBUTE_CODE and av.NUMBER_VALUE = " ).append( userId ).append( " ) " );
        sql.append(         ") " );
        sql.append(         "and ( " );
        sql.append(             "ca.ROLE_CODE is NULL " );

//	� ������ ������ BR4J00036917 ������� ����������� ����-������
        
//        sql.append(             "or exists ( " );
//        sql.append(                 "select 1 " );
//        sql.append(                 "from PERSON_ROLE pr " );
//        sql.append(                 "left join PERSON_ROLE_TEMPLATE prt on pr.PROLE_ID = prt.PROLE_ID " );
//        sql.append(                 "where " );
//        sql.append(                 "pr.ROLE_CODE = ca.ROLE_CODE " );
//        sql.append(                 "and pr.PERSON_ID = " ).append( userId ).append( " " );
//        sql.append(                 "and (prt.TEMPLATE_ID = ca.TEMPLATE_ID or prt.PROLE_ID is null) " );
//        sql.append(             ") " );
        sql.append(         ") " );
        sql.append(     ") " );
        sql.append( ") " );
        return sql;
    }
    
    public static StringBuilder simpleSearchFilter(String simpleSearchFilter) {
        StringBuilder sql = new StringBuilder();
        
        if(null == simpleSearchFilter || simpleSearchFilter.trim().length() == 0) {
        	return sql;
        }
        
        sql.append( " and ( \n" );
        sql.append(     "exists ( \n" );
        sql.append(         "select 1 \n" );
        sql.append(         "from attribute_value avSimpleSearch \n" );
        sql.append(         "where avSimpleSearch.card_id = c.card_id and avSimpleSearch.attribute_code not in ('JBR_UUID', 'REPLICATION_VERSION', 'REPLICATION_OWNER', 'REPLICATION_UUID') \n" );
        sql.append(         "and UPPER(avSimpleSearch.string_value) like UPPER('%" + StrUtils.escapeSpecialCharactersForLikeClause(StringEscapeUtils.escapeSql(simpleSearchFilter)) + "%') \n" );
        sql.append(         ")");
        sql.append(         "\n or \n");
        sql.append(     "exists ( \n" );
        sql.append(         "select 1 \n" );
        sql.append(         "from attribute_value av_doc \n" );
        sql.append(         "join attribute_value av_linked on av_doc.number_value = av_linked.card_id and av_doc.attribute_code in ('JBR_VISA_VISA','JBR_SIGN_SIGNING', 'JBR_IMPL_ACQUAINT') \n" );
        sql.append(         "where av_doc.card_id = c.card_id and av_linked.attribute_code not in ('JBR_UUID', 'REPLICATION_VERSION', 'REPLICATION_OWNER', 'REPLICATION_UUID')\n" );
        sql.append(         "and UPPER(av_linked.string_value) like UPPER('%" + StrUtils.escapeSpecialCharactersForLikeClause(StringEscapeUtils.escapeSql(simpleSearchFilter)) + "%') \n" );
        sql.append(         "UNION \n " );
        sql.append(         "select 1 \n" );
        sql.append(         "from card c_doc \n" );
        sql.append(			"join attribute_value av_doc on av_doc.number_value = c_doc.card_id and av_doc.attribute_code = 'JBR_MAINDOC' \n" );
        sql.append(         "join attribute_value av_linked on av_doc.card_id = av_linked.card_id \n" );
        sql.append(         "where c_doc.card_id = c.card_id and av_linked.attribute_code not in ('JBR_UUID', 'REPLICATION_VERSION', 'REPLICATION_OWNER', 'REPLICATION_UUID')\n" );
        sql.append(         "and UPPER(av_linked.string_value) like UPPER('%" + StrUtils.escapeSpecialCharactersForLikeClause(StringEscapeUtils.escapeSql(simpleSearchFilter)) + "%') \n" );
        sql.append(         ") \n");
        sql.append(         "\n or \n");
        sql.append("	exists(select 1 from attribute_value av_doc\n ");
        sql.append(			"join values_list vl on av_doc.value_id = vl.value_id and attribute_code in ('ADMIN_290575')\n");
        sql.append(         "where av_doc.card_id = c.card_id\n" );
        sql.append(			"and UPPER(vl.value_rus||vl.value_eng) like UPPER('%" + StrUtils.escapeSpecialCharactersForLikeClause(StringEscapeUtils.escapeSql(simpleSearchFilter)) + "%')\n");
        sql.append(			")");
        sql.append(         "\n or \n");
        sql.append(     "exists ( " );
        sql.append(         "select 1 " );
        sql.append(         "from attribute_value avSimpleSearch\n" );
        sql.append(         "where avSimpleSearch.card_id = c.card_id\n" );
        sql.append(         "and avSimpleSearch.attribute_code = 'JBR_PROJECT_NUMBER'\n" );
        sql.append(         "and UPPER(avSimpleSearch.number_value::text) like UPPER('%" + StrUtils.escapeSpecialCharactersForLikeClause(StringEscapeUtils.escapeSql(simpleSearchFilter)) + "%')\n" );
        sql.append(         ")");
        sql.append( ") \n" );
        return sql;
    }
    
    public static String howFastColumn() {
    	return "0 ";
    	/* Uncomment if quantity by urgency is needed
    	return "(select value_id from attribute_value avHowFast " + 
    			"where avHowFast.card_id = c.card_id and avHowFast.attribute_code = 'JBR_HOWFAST') howfast ";
    	*/
    }
    
    public static String groupByHowFast() {
    	return "";
    	/* Uncomment if quantity by urgency is needed
    	return "group by howfast ";
    	*/
    }

    public static String getCaller() {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[ 3 ];
        return caller.getClassName() + "." + caller.getMethodName();
    }

    public static List<Card> createCards(List<EmptyCard> empryCards,
                                         HashMap<Long, Collection<AttributeValue>> cardAttributes,
                                         CardService cardAttributeService) {

        List<Card> result = new ArrayList<Card>();

        for (EmptyCard emptyCard : empryCards) {

            long cardIdLong = emptyCard.getId();

            Card card = (Card) Card.createFromId(new ObjectId(Card.class, emptyCard.getId()));

            Collection<AttributeValue> attributeValues = cardAttributes.get(cardIdLong);
            Collection<Attribute> attributes = AttributeValueMapperUtils.map(attributeValues);
            attributes = cardAttributeService.fillAttributeDefinitions(attributes);
            card.setAttributes(attributes);

            if (emptyCard.getTemplateId() != EmptyCard.NOT_DEFINED) {
                ObjectId templateId = new ObjectId(Template.class, emptyCard.getTemplateId());
                card.setTemplate(templateId);
                Template template = cardAttributeService.getTemplateDefinition(((Long)emptyCard.getTemplateId()).intValue());
                card.setTemplateNameRu(template.getNameRu());
                card.setTemplateNameEn(template.getNameEn());
            }

            if (emptyCard.getStatusId() != EmptyCard.NOT_DEFINED) {
                Status statusDefinition = cardAttributeService.getStatusDefinition((int) emptyCard.getStatusId());
                card.setState(new ObjectId(CardState.class, emptyCard.getStatusId()));
                // RU not a bug! need for correct work
                card.setStateName(new LocalizedString(statusDefinition.getNameRu(), statusDefinition.getNameRu()));
            }

            result.add(card);
        }
        return result;
    }

    public static <T> ArrayList<T> toArrayList( Collection<T> collection ) {
        if ( collection == null ) {
            return null;
        }

        ArrayList<T> result = new ArrayList<T>( collection.size() );
        for ( T elt : collection ) {
            result.add( elt );
        }
        return result;
    }

    private static class AttributeValueOrderSubqueryBuilder {
        static String buildClause(AttributeValue sortAttribute, int count) {
            String attrType = AttributeDef.convertToString(sortAttribute.getType());

            StringBuilder result = new StringBuilder();
            String synonym = "avOrder" + count;

            result.append("(select ");
            if(AttributeTypes.STRING.equals(attrType) || AttributeTypes.TEXT.equals(attrType)) {
                result.append("string_agg(").append(synonym).append(".string_value").append(", ',') ");
            } else if(AttributeTypes.INTEGER.equals(attrType)) {
                result.append(synonym).append(".number_value ");
            } else if(AttributeTypes.TREE.equals(attrType) || AttributeTypes.LIST.equals(attrType)) {
                result.append(synonym).append(".value_id ");
            } else if(AttributeTypes.DATE.equals(attrType)) {
                result.append(synonym).append(".date_value ");
            } else if(AttributeTypes.PERSON.equals(attrType)) {
                result.append(synonym).append(".full_name ");
            }

            if(sortAttribute.isAttributeFromLink() && sortAttribute.isLinkedByPerson()) {
                result.append("from attribute_value ").append(synonym).append(" ");
                result.append("inner join person p_").append(synonym).append(" on p_").append(synonym).append(".card_id = ").append(synonym).append(".card_id ");
                result.append("inner join attribute_value attribute_value_link_").append(synonym).append(" on attribute_value_link_").append(synonym).append(".number_value = p_").append(synonym).append(".person_id ");
                result.append("where attribute_value_link_").append(synonym).append(".attribute_code = '").append(sortAttribute.getCode()).append("' ");
                result.append("and attribute_value_link_").append(synonym).append(".card_id = c.card_id ");
                result.append("and ").append(synonym).append(".attribute_code = '").append(sortAttribute.getLinkedCode()).append("' ");
            } else if(sortAttribute.isAttributeFromLink()){
            	result.append(buildCardLinkSql(sortAttribute, "c.card_id", synonym));
            } else {
                result.append("from attribute_value ").append(synonym).append(" ");
                result.append("where ").append(synonym).append(".card_id = c.card_id and ");
                result.append(synonym).append(".attribute_code = '").append(sortAttribute.getCode()).append("' ");
            }
            
            if(sortAttribute instanceof SortAttribute) {
            	SortAttribute attr = (SortAttribute) sortAttribute;
            	if(attr.getTemplateId() != null)
            		result.append(" and c.template_id=").append(attr.getTemplateId().getId());
            	if(attr.getStatusId() != null)
            		result.append(" and c.status_id=").append(attr.getStatusId().getId());
            }
            
            result.append(") \n ");

            return result.toString();
        }
        
        private static String buildCardLinkSql(AttributeValue sortAttribute, String parentJoin, String synonym){
        	StringBuilder sqlBuf = new StringBuilder();
        	String linkName = "av_l_"+synonym;
        	sqlBuf.append("from attribute_value ").append(linkName).append(" \n ");
        	sqlBuf.append("join ").append("attribute_value ").append(synonym).append(" on ");
        	sqlBuf.append(linkName).append(".attribute_code='").append(sortAttribute.getCode()).append("' and \n ");
        	sqlBuf.append(linkName).append(".number_value=").append(synonym).append(".card_id  and \n ");        	
        	sqlBuf.append(synonym).append(".attribute_code='").append(sortAttribute.getLinkedCode()).append("' and \n ")
        	.append(linkName).append(".card_id=").append(parentJoin);
        	
        	return sqlBuf.toString();
        }
    }
    
    

    private static class PersonOrderSubqueryBuilder {
        static String buildClause(AttributeValue sortAttribute, int count) {
            StringBuilder result = new StringBuilder();
            String pSynonym = "pOrder" + count;
            String avSynonym = "avOrder" + count;

            result.append("(select ").append("string_agg(").append(pSynonym).append(".full_name").append(", ',') ");
            result.append("from attribute_value ").append(avSynonym).append(" ");
            result.append("inner join person ").append(pSynonym).append(" on ");
            result.append(avSynonym).append(".number_value = ").append(pSynonym).append(".person_id ");
            result.append("where ").append(avSynonym).append(".card_id = c.card_id and ");
            result.append(avSynonym).append(".attribute_code = '").append(sortAttribute.getCode()).append("')");

            return result.toString();
        }
    }

	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		BEAN_CONTEXT = ctx;
	}
}
