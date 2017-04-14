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
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataException;

public class NumeratorPartDelo extends NumeratorPart {
	final String ATTR_INDEX = "index";
	final String ATTR_INDEXNUM = "jbr.delo";
	final ObjectId INDEX = ObjectId.predefined(CardLinkAttribute.class, this.ATTR_INDEX);
	final ObjectId INDEXNUM = ObjectId.predefined(StringAttribute.class, this.ATTR_INDEXNUM);
			
	public NumeratorPartDelo(DoSetRegistrationNumber cardnum) {
		super(cardnum);		
	}

	public String getValue() throws DataException {
		String result = "";
		try
		{
			CardLinkAttribute attr = (CardLinkAttribute)cardNumerationObj.getContextCard()
			   .getAttributeById(INDEX);
			Card card = cardNumerationObj.getCard(attr.getSingleLinkedId());
			try 
			{
				result = card.getAttributeById(INDEXNUM)
				.getStringValue();
			} 
			catch (NullPointerException e)
			{
				throw new DataException
				(
					"numerator.cardlink.attributeNotSet", 
					new Object[]
					{
						cardNumerationObj.getAttributeNameById(INDEXNUM),
						cardNumerationObj.getAttributeNameById(INDEX)
					}
				);
			}
		} 
		catch (NullPointerException e)
		{
			throw new DataException
			(
				"numerator.attributeNotSet", 
				new Object[]{cardNumerationObj.getAttributeNameById(INDEX)}
			);
		}			
		return result;
	}
}
