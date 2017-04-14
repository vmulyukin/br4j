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
package com.aplana.dbmi.numerator.query;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataException;

public class NumeratorPartLastTwoDigitsOfDeloIndex extends NumeratorPart  {
	final String ATTR_INDEX = "index";
	final String ATTR_NAME = "name";
	final ObjectId INDEX = ObjectId.predefined(CardLinkAttribute.class, this.ATTR_INDEX);
	
			
	public NumeratorPartLastTwoDigitsOfDeloIndex(DoSetRegistrationNumber cardnum) {
		super(cardnum);		
	}

	public String getValue() throws DataException{
		String result = "";
		try{
			CardLinkAttribute attr = (CardLinkAttribute)cardNumerationObj.getContextCard()
			   .getAttributeById(INDEX);	
			Card card = cardNumerationObj.getCard(attr.getSingleLinkedId());
			StringAttribute indexNameAttr = (StringAttribute)card.
				getAttributeById(Attribute.ID_NAME);
			try {
				String[] nameParts = indexNameAttr.getName().split(" ");
				result = nameParts[0].substring(nameParts[0].length()-2);
			} catch (NullPointerException e) {
				throw new DataException
				(
					"numerator.attributeNotSet", 
					new Object[]{cardNumerationObj.getAttributeNameById(PARENT_DEP)}
				);
			}
		}catch(NullPointerException e){
			throw new DataException
			(
				"numerator.attributeNotSet", 
				new Object[]{cardNumerationObj.getAttributeNameById(INDEX)}
			);
		}
		return result;
	}
}

