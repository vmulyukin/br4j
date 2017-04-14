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
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataException;

public class NumeratorPartOGAuthorF extends NumeratorPart {	
	final String ATTR_PERS_SNAME = "jbr.og.lastname";
	final String ATTR_AUTH_CARD = "jbr.ReqAuthor";
	final String ATTR_PREFIX = "authortype.prefix";
	final String ATTR_AUTHTYPE = "jbr.AuthType";
	final ObjectId SURNAME = ObjectId.predefined(StringAttribute.class, this.ATTR_PERS_SNAME);
	final ObjectId AUTH_CARD = ObjectId.predefined(CardLinkAttribute.class, this.ATTR_AUTH_CARD);
	
	public NumeratorPartOGAuthorF(DoSetRegistrationNumber cardnum) {
		super(cardnum);		
	}

	public String getValue() throws DataException{
		//������� ������� �������. ���� ����, �� ����� �������
		String result = "";
		try{
			result = this.getPrefix();
			if(result.length() > 0) return result;	
			StringAttribute attr = (StringAttribute)cardNumerationObj.getContextCard()
			   .getAttributeById(SURNAME);
			String lastName = attr.getStringValue();
			if(!(lastName != null && lastName.trim().length() > 0)){
				lastName=getSurnFromCard();
			}
			if(lastName.length() >= 1) lastName = lastName.substring(0, 1);
			result = lastName;				
		}catch(NullPointerException e){
			throw new DataException
			(
				"numerator.attributeNotSet", 
				new Object[]{cardNumerationObj.getAttributeNameById(SURNAME)}
			);
		}
		return result;
	}
	
	private String getPrefix(){
		String result = "";
		try{
			CardLinkAttribute attr = (CardLinkAttribute) cardNumerationObj.getContextCard()
			   .getAttributeById(ObjectId.predefined(CardLinkAttribute.class, this.ATTR_AUTHTYPE));
			if(attr != null && attr.isEmpty() == false){
				Card card = cardNumerationObj.getCard(attr.getSingleLinkedId());
				if(card != null){
					StringAttribute attrPr = (StringAttribute)card
					   .getAttributeById(ObjectId.predefined(StringAttribute.class, this.ATTR_PREFIX));
					if(attrPr != null){				
						result = attrPr.getStringValue();								
					}else{
						logger.warn("authtype card has no " + this.ATTR_PREFIX + " atribute");
					}
				}else{
					logger.warn("authtype card not found ");	
				}
			}else{
				logger.warn("card has no " + this.ATTR_AUTHTYPE + " atribute");	
			}
		}catch(Exception e){
			logger.error("Error getting prefix", e);		
		}
		return result;
	}
	
	private String getSurnFromCard(){
		String result = "";
		try{
			CardLinkAttribute attr = (CardLinkAttribute) cardNumerationObj.getContextCard()
			   .getAttributeById(this.AUTH_CARD);
			if(attr != null && attr.isEmpty() == false){
				Card card = cardNumerationObj.getCard(attr.getSingleLinkedId());
				if(card != null){
					StringAttribute attrPr = (StringAttribute)card
					   .getAttributeById(SURNAME);
					if(attrPr != null){				
						result = attrPr.getStringValue();								
					}else{
						logger.warn("Author card has no " + this.ATTR_AUTH_CARD + " atribute");
					}
				}else{
					logger.warn("Author card not found ");	
				}
			}else{
				logger.warn("card has no " + this.ATTR_AUTH_CARD + " atribute");	
			}
		}catch(Exception e){
			logger.error("Error getting author card", e);		
		}
		return result;
	}
}
