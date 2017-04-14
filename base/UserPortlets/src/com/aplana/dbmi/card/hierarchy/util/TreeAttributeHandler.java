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
package com.aplana.dbmi.card.hierarchy.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.model.util.ReferenceValueUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class TreeAttributeHandler extends ListAttributeHandler {
	private static class TreeValuesRange implements ValuesRange {
		private Set possibleValues; 
		public boolean match(Attribute attr) {
			TreeAttribute ta = (TreeAttribute)attr;
			if (possibleValues == null) {
				return ta.isEmpty();
			}			
			if (ta.isEmpty()) {
				return false;
			}
			Iterator i = ta.getValues().iterator();
			while (i.hasNext()) {
				ReferenceValue ref = (ReferenceValue)i.next();
				if (possibleValues.contains(ref.getId())) {
					return true;
				}
			}
			return false;
		}
	}
	
	public ValuesRange parseValuesRange(String stRange, DataServiceBean serviceBean) throws Exception {
		TreeValuesRange result = new TreeValuesRange();
		ObjectId refId = ObjectIdUtils.getObjectId(ReferenceValue.class, stRange, true);
		Collection refCol = getPossibleValuesForRange(refId, serviceBean);
		result.possibleValues = ObjectIdUtils.collectionToSetOfIds(refCol);
		return result;
	}
	
	private Collection getPossibleValuesForRange(ObjectId refId, DataServiceBean serviceBean) throws DataException, ServiceException {
		ReferenceValue refVal = (ReferenceValue)serviceBean.getById(refId);
		if (refVal == null) {
			return null;
		}
		// ����� �������� ��� ������ �� ����������� ����� �������������� �����������
		// ��� ����� ����� ���� ���������� � �������� �� ���� ������ ��� ������		
		Collection values = serviceBean.listChildren(refVal.getReference(), ReferenceValue.class);
		ReferenceValue ref = ReferenceValueUtils.findReferenceValueInHierarchicalCollection(values, refVal.getId());
		return ReferenceValueUtils.referenceValueWithChildrenToCollection(ref);
	}	

	public TreeAttributeHandler(String value, Class clazz) {
		super(value, clazz);
	}
	
	public boolean matchValue(Attribute attr, Object value) {		
		TreeAttribute a = (TreeAttribute)attr;
		if (value == null) {
			return a.isEmpty();
		}
		ReferenceValue valueRef = (ReferenceValue)value;
		Iterator i = a.getValues().iterator();		
		while (i.hasNext()) {
			ReferenceValue ref = (ReferenceValue)i.next();				 
			if (valueRef.getId().equals(ref.getId())) {
				return true;
			}
		}
		return false;
	}
}
