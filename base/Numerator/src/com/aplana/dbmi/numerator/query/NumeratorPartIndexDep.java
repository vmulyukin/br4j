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

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;

public class NumeratorPartIndexDep extends NumeratorPart {
	final String ATTR_DEPCOUNTER = "numerator.depcounter";
	final ObjectId DEP_COUNTER = ObjectId.predefined(IntegerAttribute.class, this.ATTR_DEPCOUNTER);
	
	public NumeratorPartIndexDep(DoSetRegistrationNumber cardnum) {
		super(cardnum);		
	}

	public String getValue() throws DataException{
		String result = "";
		try
		{
			ObjectId personCardId = cardNumerationObj.person.getCardId();
			Card card = cardNumerationObj.getCard(personCardId);
			ObjectId depId = cardNumerationObj.getAttributeCardId(card, DEPARTMENT);								
			cardNumerationObj.getLock(depId); 
			card = this.cardNumerationObj.getCard(depId);
			try{
				IntegerAttribute attr = (IntegerAttribute)card.getAttributeById(DEP_COUNTER);
				result = cardNumerationObj.getIndex(attr);
			}catch(NullPointerException e){throw new DataException(
				"numerator.cardlink.attributeNotSet", 
				 new Object[]{
					cardNumerationObj.getAttributeNameById(DEP_COUNTER),
					cardNumerationObj.getAttributeNameById(DEPARTMENT),
				}
			);}
			if (!isPreliminary())
				cardNumerationObj.addCardToSave(card);
			else
				result = "["+result+"]";																						
		}
		catch (NullPointerException e) 
		{
			throw new DataException("numerator.personNotSet");
		}
		return result;
	}
}
