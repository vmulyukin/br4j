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
package com.aplana.dbmi.ajax;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;

public abstract class ARMSearchResultLabelBuilder {
	
	private static final ObjectId ATTR_FIRST_NAME = ObjectId.predefined(StringAttribute.class, "jbr.person.firstName");
	private static final ObjectId ATTR_LAST_NAME = ObjectId.predefined(StringAttribute.class, "jbr.person.lastName");
	private static final ObjectId ATTR_PATRONYMIC = ObjectId.predefined(StringAttribute.class, "jbr.person.middleName");
	
	
	public String getLabel(Card card) {
		StringAttribute firstName = (StringAttribute) card.getAttributeById(ATTR_FIRST_NAME); 
		StringAttribute lastName = (StringAttribute) card.getAttributeById(ATTR_LAST_NAME);
		StringAttribute patronymic = (StringAttribute) card.getAttributeById(ATTR_PATRONYMIC);
		
		StringBuilder label = new StringBuilder();
		
		if(null != lastName) {
			String lastNameStr = lastName.getStringValue();
			if(null != lastNameStr && lastNameStr.length() > 0) {
				label.append(lastNameStr);
			}
		}
		
		if(null != firstName) {
			String firstNameStr = firstName.getStringValue();
			if(null != firstNameStr && firstNameStr.length() > 0) {
				label.append(" ").append(firstNameStr.substring(0, 1)).append(".");
			}
		}
		
		if(null != patronymic) {
			String patronymicStr = patronymic.getStringValue();
			if(null != patronymicStr && patronymicStr.length() > 0) {
				label.append(" ").append(patronymicStr.substring(0, 1)).append(".");
			}
		}
		
		return label.toString();
	}

}
