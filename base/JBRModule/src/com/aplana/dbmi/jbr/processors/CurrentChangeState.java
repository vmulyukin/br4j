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

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * ���� ����������, ��������� ��������� ������� ������� �� �� ��������� ������� 
 * ��������� � ��������� "MoveId". ����� ������������� ������� �������� ������ �� 
 * �������� �������� �������� ���������. ������ �������������� ��������� ��������� ��
 * �� null � ��������� ��� ������������� ��������
 * @author larin
 *
 */
public class CurrentChangeState extends ProcessCard {
	public static final String PARAM_MOVE_ID = "MoveId";
	public static final String PARAM_CONDITION_ATTR_NOT_NULL = "ConditionAttrNotNull";
	public static final String PARAM_CONDITION_ATTR_HAS_VALUE = "ConditionAttrHasValue";
	

	@SuppressWarnings("unchecked")
	@Override
	public Object process() throws DataException 
	{
		String moveId = super.getParameter( PARAM_MOVE_ID, null);
		if (moveId == null){
			logger.error("Parameter: " + PARAM_MOVE_ID + " not set");
			throw new DataException("Parameter: " + PARAM_MOVE_ID + " not set");
		}

		final ObjectId cardId = super.getCardId();
		if (cardId == null){
			logger.error("Card is not set");
			throw new DataException("Card is not set");
		}

		Card document = getCard();
		
		//�������� �������
		if (isNotNullConditionSuccess(document) && isValueConditionSuccess(document)){
			ChangeState action = new ChangeState();
			action.setCard(document);
			ObjectQueryBase query = getQueryFactory().getFetchQuery(WorkflowMove.class);
			query.setId(ObjectId.predefined(WorkflowMove.class, moveId));
			WorkflowMove move = (WorkflowMove)getDatabase().executeQuery(getSystemUser(), query);
			action.setWorkflowMove(move);
			execAction(action, getSystemUser());
		}
		return null;
	}

	/**
	 * �������� �������� �� ������������ �������
	 * @param document
	 * @return
	 */
	private boolean isValueConditionSuccess(Card document) {
		boolean result = false;

		String paramConditionAttrValue = super.getParameter(PARAM_CONDITION_ATTR_HAS_VALUE, null);
		
		if (paramConditionAttrValue == null){
			result = true;
		}else{
			String[] params = paramConditionAttrValue.split("="); 
			String attrIdParam = params[0];
			String conditionValue = params[1];
			
			ObjectId attrId = AttrUtils.getAttributeId(attrIdParam);
			Attribute attr = document.getAttributeById(attrId);
			if (attr != null){
				if(attr.getClass().equals(ListAttribute.class)){
					ObjectId conditionRefId = new ObjectId(ReferenceValue.class, Long.valueOf(conditionValue));
					if (((ListAttribute)attr).getValue() != null){
						result = ((ListAttribute)attr).getValue().getId().equals(conditionRefId);
					}
				}
			}
		}
		
		return result;
	}

	/**
	 * �������� �������� �� �� null
	 * @param document
	 * @param paramConditionAttrNotNull
	 * @return
	 * @throws DataException 
	 */
	private boolean isNotNullConditionSuccess(Card document) throws DataException {
		boolean result = false;

		String paramConditionAttrNotNull = super.getParameter(PARAM_CONDITION_ATTR_NOT_NULL, null);
		
		if (paramConditionAttrNotNull == null){
			result = true;
		}else{		
			ObjectId attrId = AttrUtils.getAttributeId(paramConditionAttrNotNull);
			Attribute attr = document.getAttributeById(attrId);
			if (attr != null){
				if(attr.getClass().equals(DateAttribute.class)){
					result = ((DateAttribute)attr).getValue() != null;  
				}else if(attr.getClass().equals(StringAttribute.class)){
					result = ((StringAttribute)attr).getValue() != null;  
				}else{
					throw new DataException("Not support condition for " + attr.getClass().getName() + " attribute");
				}
			}
		}
		
		return result;
	}
}
