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
package com.aplana.cms.view_template;

import java.util.ArrayList;
import java.util.List;

import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.workstation.SortAttribute;

public class SortAttributeMapper {
	
	public static List<SortAttribute> map(CardViewData cardViewData, String columnId, Boolean isStraightOrder){

        List<ColumnSortAttributes> columns = cardViewData.getSortColumns();
        
        if(null == columns) {
        	return null;
        }
        
        if(null == columnId) {
        	columnId = "default";
        }
        
        List<SortViewAttribute> sortAttributes = findSortAttributes(columns, columnId);
        if(null == sortAttributes || sortAttributes.size() == 0) {
        	return null;
        }
        
        List<SortAttribute> attributeValues = new ArrayList<SortAttribute>(sortAttributes.size());

        for (SortViewAttribute sortAttribute : sortAttributes) {
        	if(sortAttribute.isByStatus() || sortAttribute.isByTemplate()) {
        		SortAttribute sortAttr = new SortAttribute(sortAttribute.isByTemplate(), sortAttribute.isByStatus(), sortAttribute.isAsc());
        		sortAttr.setSortGroup(sortAttribute.getSortGroup());
        		sortAttr.setAsc(isStraightOrder != null ? isStraightOrder : sortAttribute.isAsc());
        		attributeValues.add(sortAttr);
        	} else {
        		SortAttribute sortAttr = new SortAttribute(sortAttribute.getAttribute().getCode(),
                        sortAttribute.getAttribute().getLinkedCode(),
                        Boolean.valueOf(sortAttribute.getAttribute().isLinkedByPerson()), sortAttribute.isAsc());
        		
	        	if(sortAttribute.getTemplateId() != null){
		            sortAttr.setTemplateId(ObjectId.predefined(Template.class, sortAttribute.getTemplateId()));
	            }
	        	if(sortAttribute.getStatusId() != null) {
	                sortAttr.setStatusId(ObjectId.predefined(CardState.class, sortAttribute.getStatusId()));
	            }
	        	if(sortAttribute.getNullsFirst() != null) {
	        		sortAttr.setNullsFirst(sortAttribute.getNullsFirst());
	        	}
	        	sortAttr.setSortGroup(sortAttribute.getSortGroup());
	        	sortAttr.setAsc(isStraightOrder != null ? isStraightOrder : sortAttribute.isAsc());
                attributeValues.add(sortAttr);
            }
        }

        return attributeValues;
    }
	
	private static List<SortViewAttribute> findSortAttributes(List<ColumnSortAttributes> columns, String columnId) {
		for(ColumnSortAttributes column : columns) {
        	if(column.getColumnId().equals(columnId)) {
        		return column.getSortAttributes();
        	}
        }
		
		return null;
	}

}
