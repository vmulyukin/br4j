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
package com.aplana.dbmi.service.impl.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.workstation.AttributeValue;

/**
 * Provides utility methods for mapping  
 * 
 * @author skashanski
 *
 */
public class AttributeValueMapperUtils {
	
	
	public static Collection<Attribute> map(Collection<AttributeValue> attrValues) {
		
		List<Attribute> result = new ArrayList<Attribute>();

		AttributeValueMapperFactory mapperFactory = AttributeValueMapperFactory.getFactory();
		
		for(AttributeValue attributeValue : attrValues) {
			AbstractAttributeValueMapper getAttributeValueMapper = mapperFactory.getAttributeValueMapper(attributeValue);
			Attribute attribute = getAttributeValueMapper.map(attributeValue);
			result.add(attribute);
		}
		
		return result;
	}
	
	


}
