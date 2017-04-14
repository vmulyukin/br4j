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

import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;
import java.util.ArrayList;
import java.util.Collection;

public class IsExistSearchAttributeEditor extends RadioButtonSearchEditor {

	public static final ObjectId YES_ID = ObjectId.predefined(ReferenceValue.class, "jbr.incoming.control.yes");
	public static final ObjectId NO_ID = ObjectId.predefined(ReferenceValue.class, "jbr.incoming.control.no");
	public static final ObjectId NO_MATTER_ID = ObjectId.predefined(ReferenceValue.class, "jbr.isExistSearchAttr.noMatter");

	private ReferenceValue noMatterVal = ReferenceValue.createFromId(NO_MATTER_ID);

	public IsExistSearchAttributeEditor() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/IsExistSearchAttributeEditor.jsp");
	}

	@Override
	protected Collection<ReferenceValue> loadReference(Attribute attribute, PortletRequest request) throws DataException {
		ArrayList<ReferenceValue> list = new ArrayList<ReferenceValue>();
		DataServiceBean service = getDataServiceBean(request);
		
		try {
			for (ObjectId id : new ObjectId[] {YES_ID, NO_ID, NO_MATTER_ID}) {
				ReferenceValue ref = service.getById(id);
				if (ref == null) {
					throw new DataException("Unable to load ReferenceValue by " + YES_ID.getId() + " id");
				}
				list.add(ref);
			}
		} catch (ServiceException e) {
			throw new DataException("Unable to initialize IsExistSearchAttributeEditor due to" + e.getMessage(), e);
		}

		return list;
	}

	@Override
	protected ObjectId getSelectedValueId(PortletRequest request, Attribute attr) {
		IsExistSearchAttribute searchAttr = (IsExistSearchAttribute)attr;
		if (searchAttr.getIsExistFlag() == null) {
			searchAttr.setIsExistFlag(noMatterVal);
		}

		return searchAttr.getIsExistFlag().getId();
	}
	
	@Override
	public boolean gatherData(ActionRequest request, Attribute attr) throws DataException {
		String value = request.getParameter(getAttrHtmlId(attr));
		if (value == null)
			return false;

		value = value.trim();
		if (INVALID_VALUE_ID.equals(value)) {
			throw new DataException("edit.page.error.id", new Object[] { attr.getName() });
		} else if (value.equals("") || value.equals(EMPTY_VALUE_ID)) {
			((IsExistSearchAttribute) attr).setIsExistFlag(noMatterVal);
		} else {
			try {
				long id = Long.parseLong(value);
				ReferenceValue refVal = new ReferenceValue();
				refVal.setId(id);
				((IsExistSearchAttribute) attr).setIsExistFlag(refVal);
			} catch (NumberFormatException e) {
				throw new DataException("edit.page.error.id", new Object[] { attr.getName() });
			}
		}
		return true;
	}

}
