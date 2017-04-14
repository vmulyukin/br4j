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
package com.aplana.dbmi.search;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.card.IsExistSearchAttributeEditor;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.search.ext.RouteSearchObjectId;

public class IsExistSearchAttributeInitializer extends SearchAttributeInitializer<IsExistSearchAttribute> {

	private static ObjectId SIGN_WITH_DS_ATTR_ID = ObjectId.predefined(HtmlAttribute.class, "jbr.search.sign.with.ds");
	private static ObjectId VISA_WITH_DS_ATTR_ID = ObjectId.predefined(HtmlAttribute.class, "jbr.search.visa.with.ds");
	private static ObjectId SIGN_SET_ATTR_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.sign.set");
	private static ObjectId VISA_SET_ATTR_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.visa.set");

	@Override
	protected Object getValue() {
		return attribute.getIsExistFlag();
	}

	@Override
	protected void setValue(Object attrValue, Search search) {
		if (IsExistSearchAttributeEditor.YES_ID.equals(attribute.getIsExistFlag().getId())) {
			if (attribute.getId() instanceof RouteSearchObjectId) {
				search.addAttribute(attribute.getId(), Search.ExistAttribute.INSTANCE);
			}
		} else if (IsExistSearchAttributeEditor.NO_ID.equals(attribute.getIsExistFlag().getId())) {
			//TODO: ��������� ���-������ �������
			//���� ������� "���������� ��" ��� "��������� ��"
			//�� ������ �������: �� ������ ���� �������� �������/���� � ��,
			//�� ��� ���� ���� �������� �������/���� ������ ������������
			if (attribute.getId().equals(SIGN_WITH_DS_ATTR_ID)) {
				search.addAttribute(SIGN_SET_ATTR_ID, Search.ExistAttribute.INSTANCE);
				search.addAttribute(attribute.getId(), Search.EmptyAttribute.FULL_EMPTY_INSTANCE);
			} else if (attribute.getId().equals(VISA_WITH_DS_ATTR_ID)) {
				search.addAttribute(VISA_SET_ATTR_ID,Search.ExistAttribute.INSTANCE);
				search.addAttribute(attribute.getId(), Search.EmptyAttribute.FULL_EMPTY_INSTANCE);
			} else {
				search.addAttribute(attribute.getId(), Search.EmptyAttribute.INSTANCE);
			}
		}
	}

	@Override
	protected boolean isEmpty() {
		return attribute == null;
	}
}
