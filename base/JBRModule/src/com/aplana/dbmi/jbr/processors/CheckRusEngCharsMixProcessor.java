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
package com.aplana.dbmi.jbr.processors;

import java.util.ArrayList;
import java.util.List;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;

/**
 * A processor that checks whether the value of the desired attribute has english and russian symbols mixing
 * @author ahasanov
 *
 */
public class CheckRusEngCharsMixProcessor extends ProcessCard {
	
	private static final String PARAM_ATTR_ID = "attrIds"; // attribute to check
	private static final String PARAM_FAILURE_MESSAGE = "failure_message"; // the message that will be raised if check won't be passed
	private static final String PARAM_ATTR_CODE_TYPE_DELIMITER = ":";
	
	private List<ObjectId> testAttrIds;
	private String failureMessage;
	
	@Override
	public Object process() throws DataException {
		
		final Card card = getCard();
		if(card == null)
			throw new DataException("Cannot get the card");
		
		if(testAttrIds == null || testAttrIds.isEmpty())
			throw new DataException("No attribute were provided");
			
		for(ObjectId attrId : testAttrIds) {
			Attribute attr = card.getAttributeById(attrId);
			if(attr == null)
				throw new DataException("Cannot find the attribute: "+attrId+" in the card with id: "+card.getId());
			
			if(attr.isEmpty())
				return null;
			
			checkMixing(attr.getStringValue());
			
		}
		
		return getResult();
	}
	
	/**
	 * checks english and russian symbols mixing
	 * @param value
	 * @throws DataException
	 */
	private void checkMixing(String value) throws DataException {
		boolean hasEng = false;
		boolean hasRus = false;
		for(int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if(isCharEng(c))
				hasEng = true;
			if(isCharRus(c))
				hasRus = true;
			if(hasEng && hasRus)
				throw new DataException(failureMessage != null ? failureMessage : "eng and rus symbols mixing was found");

		}
	}
	
	private boolean isCharEng(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}
	
	private boolean isCharRus(char c) {
		return (c >= '�' && c <= '�') || (c >= '�' && c <= '�');
	}
	
	private void resolveAttrIds(String ids) {
		testAttrIds = new ArrayList<ObjectId>();
		String[] idsArr = ids.split(",");
		for(String idStr : idsArr)
			testAttrIds.add(ObjectIdUtils.getAttrObjectId(idStr.trim(), PARAM_ATTR_CODE_TYPE_DELIMITER));
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if(PARAM_ATTR_ID.equalsIgnoreCase(name)) {
			if(value != null && !value.equals(""))
				resolveAttrIds(value);
		} else if(PARAM_FAILURE_MESSAGE.equalsIgnoreCase(name)) {
			failureMessage = value;
		} else super.setParameter(name, value);
	}

}
