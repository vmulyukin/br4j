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

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataServiceBean;

public class ListAttributeHandler extends AttributeHandler {
	public ListAttributeHandler(String value, Class clazz) {
		super(value, clazz);
	}
	
	public boolean matchValue(Attribute attr, Object value) {		
		ListAttribute a = (ListAttribute)attr;
		if (a.getValue() == null || value == null) {
			return value == a.getValue();
		} else {
			ReferenceValue ref = (ReferenceValue)value;
			return a.getValue().getId().equals(ref.getId());
		}
	}

	public Object stringToValue(String st, DataServiceBean serviceBean) throws Exception {
		if (st != null && !"".equals(st)) {
			ObjectId refId = ObjectIdUtils.getObjectId(ReferenceValue.class, st, true);
			if (((Long)refId.getId()).longValue() < 0) {
				// ���� Id < 0, �� ��� ��������� ����������, ������� ����� ��������� ��������
				// �������� �� ��
				return (ReferenceValue)serviceBean.getById(refId);	
			} else {
				return (ReferenceValue)DataObject.createFromId(refId);
			}
		} else {
			return null;
		}
	}
}
