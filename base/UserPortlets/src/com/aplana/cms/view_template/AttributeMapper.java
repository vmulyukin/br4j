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

import com.aplana.dbmi.model.workstation.AttributeValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author rmitenkov
 */
public class AttributeMapper {

    public static List<AttributeValue> map(CardViewData cardViewData){

        List<ViewAttribute> viewAttributes = cardViewData.getAttributes();
        List <AttributeValue> attributeValues = new ArrayList<AttributeValue>(viewAttributes.size());

        for (ViewAttribute viewAttribute : viewAttributes) {
            //attributeValues.add(new AttributeValue(viewAttribute.getCode(), viewAttribute.getLinkedCode(),
            //        Boolean.valueOf(viewAttribute.isLinkedByPerson())));
            attributeValues.add(new AttributeValue(viewAttribute.getCode(), viewAttribute.getLinkedCode(), viewAttribute.getCorrector(),
                    Boolean.valueOf(viewAttribute.isLinkedByPerson())));
        }

        return attributeValues;
    }

    public static List<AttributeValue> map(LinkViewAttribute linkViewAttribute){

        List<LinkViewAttribute.Column> columns = linkViewAttribute.getColumns();
        if (columns == null) return new ArrayList<AttributeValue>(0);
        List <AttributeValue> attributeValues = new ArrayList<AttributeValue>(columns.size());

        for (LinkViewAttribute.Column column : columns) {
            attributeValues.add(new AttributeValue(column.getCode()));
        }

        return attributeValues;
    }
}
