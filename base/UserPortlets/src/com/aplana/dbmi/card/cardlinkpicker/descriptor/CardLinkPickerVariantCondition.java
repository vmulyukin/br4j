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
package com.aplana.dbmi.card.cardlinkpicker.descriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;

public class CardLinkPickerVariantCondition {
	protected Map<ObjectId, List<Object>> conditions = new HashMap<ObjectId, List<Object>>();
	protected Log logger = LogFactory.getLog(getClass());

/*	public void addCondition(ObjectId attrId, List<Object> values){
		conditions.put(attrId, values);
	}
*/
	public void addCondition(ObjectId attrId, Object value){
		// ���������� ��������� �����, ��������� ������ ��������� � ��� ���� ������, ��-�� ���� � ���������� ������� ����������� �����������
		if (value instanceof List){
			conditions.put(attrId, (List)value);
//			this.addCondition(attrId, (List)value);
		} else {
			List<Object> valuesList = new ArrayList<Object>(1);
			valuesList.add(value);
			conditions.put(attrId, valuesList);
		}
	}

	public boolean checkCondition(Card card) {
		for (Map.Entry<ObjectId, List<Object>> condition: conditions.entrySet()){
			Attribute attr = null;
			//TODO ��������� �� ������������� ���������� state & template ��� ������� ���������
			if (condition.getKey().equals(Card.ATTR_STATE)){
				attr = (ListAttribute)DataObject.createFromId(Card.ATTR_STATE);
				((ListAttribute)attr).setValue(
						(ReferenceValue)DataObject.createFromId(
								new ObjectId(ReferenceValue.class, card.getState().getId())));
			} else 	if (condition.getKey().equals(Card.ATTR_TEMPLATE)) {
				attr = (ListAttribute)DataObject.createFromId(Card.ATTR_TEMPLATE);
				((ListAttribute)attr).setValue(
						(ReferenceValue)DataObject.createFromId(
								new ObjectId(ReferenceValue.class, card.getTemplate().getId())));
			} else
				attr = card.getAttributeById(condition.getKey());
			if (attr == null){
				logger.warn("The card "+card.getId().getId()+
						" has not pointed attribute "+condition.getKey()+
						". Condition is not satisfied.");
				return false;
			}
			// TODO ���������� ������ ����������� ������������ ��� ���� ���������
			if (attr instanceof ListAttribute){
				List<Object> expectedValues = condition.getValue();
				if (expectedValues.contains(((ListAttribute)attr).getValue().getId())) {
					continue;
				}
				return false;
			} else {
				logger.warn("Other types of attributes in conditions than ListAttribute are not allowed");
				return false;
			}
		}
		// ���� ������ ������� ������
		return true;
	}
}