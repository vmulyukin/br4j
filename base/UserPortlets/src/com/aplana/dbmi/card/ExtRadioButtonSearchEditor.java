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
package com.aplana.dbmi.card;

import java.util.Collection;
import java.util.Map;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;

public class ExtRadioButtonSearchEditor extends RadioButtonSearchEditor {

	public static final ObjectId NO_MATTER_ID = ObjectId.predefined(ReferenceValue.class, "jbr.isExistSearchAttr.noMatter");
	private static ReferenceValue noMatterVal = ReferenceValue.createFromId(NO_MATTER_ID);
	{
		noMatterVal.setValueRu("�� �����");
		noMatterVal.setValueEn("No matter");
	}

	@Override
	protected Map<String, Object> getReferenceData(Attribute attr, PortletRequest request) throws PortletException {
		Map<String, Object> referenceData =  super.getReferenceData(attr, request);
		Collection<ReferenceValue> valuesList = getValueList(request, attr);

		ObjectId selectedValueId = getSelectedValueId(request, attr);
		if (selectedValueId == null) {
			referenceData.put(SELECTED_ID_KEY, NO_MATTER_ID.getId());
		}

		if (!valuesList.contains(noMatterVal)) {
			valuesList.add(noMatterVal);
		}

		referenceData.put(VALUES_LIST_KEY, valuesList);
		referenceData.put("query", getDataQuery());
		return referenceData;
	}
}
