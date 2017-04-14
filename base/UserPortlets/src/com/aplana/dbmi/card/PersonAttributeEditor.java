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

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.gui.IListEditor;
import com.aplana.dbmi.gui.ListDataProvider;
import com.aplana.dbmi.gui.ListEditor;
import com.aplana.dbmi.gui.PersonAttributeData;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.service.DataException;

public class PersonAttributeEditor extends JspAttributeEditor
{
	public static final String EDIT_PERSON_ACTION = "editPerson";
    public static final String EDIT_FORM_NAME = "EditCardForm";
    public static final String ID_PERSON_LIST = "_list";
    public static final String ID_EDIT_BUTTON = "_edit";
    
	public PersonAttributeEditor() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/Person.jsp");
		setParameter(PARAM_INIT_JSP, "/WEB-INF/jsp/html/attr/PersonInclude.jsp");
	}

	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException {
		return false;
	}

	public boolean processAction(ActionRequest request,
			ActionResponse response, Attribute attr) throws DataException {
		String action = request.getParameter(CardPortlet.ACTION_FIELD);
		String attrId = request.getParameter(CardPortlet.ATTR_ID_FIELD);
		if (!EDIT_PERSON_ACTION.equals(action) || !attrId.equals(attr.getId().getId()))
			return false;
		CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
    	try {
			ListDataProvider adapter = new PersonAttributeData((PersonAttribute) attr,
					sessionBean.getServiceBean());
    		IListEditor editor = new ListEditor();
			editor.setDataProvider(adapter);
			sessionBean.openForm(new ListEditForm(editor));
		} catch (Exception e) {
			e.printStackTrace();
            //sessionBean.setMessage(getMessage(request, "db.side.error.msg") + e.getMessage());
			throw new DataException("db.side.error.msg", e);
		}
		return true;
	}
}
