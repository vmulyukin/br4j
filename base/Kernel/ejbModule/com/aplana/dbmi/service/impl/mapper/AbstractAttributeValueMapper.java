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

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Attribute;

import com.aplana.dbmi.model.workstation.AttributeValue;

/**
 * Represents  parent attribute mapper for converting {@link com.aplana.dbmi.model.workstation.AttributeValue} -> {@link Attribute}
 *  
 * @author skashanski
 *
 */
public abstract class AbstractAttributeValueMapper<T extends Attribute> {
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	protected abstract T createAttribute();
	
	protected abstract void setValue(T attr, Object value);
	
	public T map(AttributeValue attr) {
		
		T attrCreated = createAttribute();
		
		init(attr, attrCreated);
		
		
		Collection value = (Collection)attr.getValue();
		if(attr.getCorrector()!=null) {
			Iterator iterator = value.iterator();
			if (iterator.hasNext()) {
				Object v = iterator.next();
				attr.getCorrector().correctValue(v);
	        }	
		}
		setValue(attrCreated, attr.getValue());
		
		return attrCreated; 
		
	}

	protected void init(AttributeValue attr, T attrCreated) {
		
		String id = attr.getCode()/*attr.isAttributeFromCardLink() ? attr.getLinkedCode() : attr.getCode()*/;  
		attrCreated.setId(id);
		
	}

}
