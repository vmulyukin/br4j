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

import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.TextAttribute;

import java.util.Collection;
import java.util.Iterator;


/**
 * Represents  attribute value mapper for {@link com.aplana.dbmi.model.TextAttribute}
 * @author skashanski
 *
 */
public class HtmlAttributeValueMapper extends AbstractAttributeValueMapper<HtmlAttribute> {
	
 

	@Override
	protected HtmlAttribute createAttribute() {
		
		return new HtmlAttribute();
		
	}
	
	

	@Override
	protected void setValue(HtmlAttribute attr, Object value) {
		Collection linkedIds = (Collection)value;
		Iterator iterator = linkedIds.iterator();
		
		String strValue = null;
		if (iterator.hasNext())
			strValue = (String)iterator.next();
		
		attr.setValue(strValue);
		
	}
	

	
	
}
