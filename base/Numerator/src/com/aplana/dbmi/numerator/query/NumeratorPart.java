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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataException;


/**
 * ������������ ����� ��� ���������� ���������� ��������� ������
 * �����-������� ������ ���������� NumeratorPart<�������� ��������>, 
 * ��� <�������� ��������> - ��� �������� �������� "����������� �������" �� ����� "������. ��������"
 * �����-������� ��������� ����� getValue() 
 */

public class NumeratorPart {
	public DoSetRegistrationNumber cardNumerationObj = null;
	final String ATTR_DEP = "jbr.personInternal.department";
	final String ATTR_DEPPARENT = "jbr.department.parentDepartment";
	final String ATTR_DEPINDEX = "jbr.department.index";
	final ObjectId DEPARTMENT = ObjectId.predefined(CardLinkAttribute.class, this.ATTR_DEP);
	final ObjectId DEP_INDEX = ObjectId.predefined(StringAttribute.class, this.ATTR_DEPINDEX);
	final ObjectId PARENT_DEP = ObjectId.predefined(CardLinkAttribute.class, this.ATTR_DEPPARENT);
	private boolean preliminary = false;
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	
	public NumeratorPart(DoSetRegistrationNumber cardnum){
		this.cardNumerationObj = cardnum;
	}
	
	public String getValue() throws DataException{
		String result = "";
		
		return result;
	}
	
	/**
	 * ��������� ������� ������������� ���������� ����������
	 * @throws DataException 
	 */
	protected String getDepIndex(ObjectId personCardId, int minLevel) throws DataException{
		String result = "";
		int i = 0;
		
		Card card = cardNumerationObj.getCard(personCardId);
		try 
		{
			ObjectId depId = ((CardLinkAttribute)card.
					getAttributeById(DEPARTMENT)).getSingleLinkedId();
			while(result.length() == 0){				
				card =  this.cardNumerationObj.getCard(depId);
				if(i >= minLevel)
				{
					try
					{
					result = ((StringAttribute)card.
							getAttributeById(DEP_INDEX)).getStringValue();	
					}
					catch (NullPointerException e)
					{
						if(minLevel == 0)
							throw new DataException
							(
								"numerator.cardlink.attributeNotSet", 
								new Object[]
								{
									cardNumerationObj.getAttributeNameById(DEP_INDEX),
									cardNumerationObj.getAttributeNameById(DEPARTMENT),
								}
							);
						else break;
					}
				}		
				try
				{
				depId = ((CardLinkAttribute)card.
						getAttributeById(PARENT_DEP)).getSingleLinkedId();
				}
				catch (NullPointerException e)
				{
					throw new DataException
					(
						"numerator.attributeNotSet", 
						new Object[]{cardNumerationObj.getAttributeNameById(PARENT_DEP)}
					);
				}
				i++;
				}
		} 
		catch (NullPointerException e)
		{
			throw new DataException
			(
				"numerator.attributeNotSet", 
				new Object[]{cardNumerationObj.getAttributeNameById(DEPARTMENT)}
			);
		}
		
		if(result.length() == 0 && minLevel>0){
			result = getDepIndex(personCardId, 0);
		}
		
		return result;
	}

	public boolean isPreliminary() {
		return preliminary;
	}

	public void setPreliminary(boolean preliminary) {
		this.preliminary = preliminary;
	}
}
