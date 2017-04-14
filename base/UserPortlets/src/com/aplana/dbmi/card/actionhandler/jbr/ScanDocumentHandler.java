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
package com.aplana.dbmi.card.actionhandler.jbr;

import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.card.Parametrized;
import com.aplana.dbmi.card.PortletFormManager;
import com.aplana.dbmi.card.PortletFormManagerAware;
import com.aplana.dbmi.card.actionhandler.CardPortletAttributeEditorActionHandler;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.scanner.CardScanForm;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.util.IdUtils;

public class ScanDocumentHandler extends CardPortletAttributeEditorActionHandler implements PortletFormManagerAware, Parametrized {
	private PortletFormManager portletFormManager;
	private static final String PAGE_PARAM = "page";
	private static final String PAGE_NAME_SRC_ATTR = "nameSrcAttr";
	private String pageName = "";
	private ObjectId nameSrcAttr;
	
	protected void process(Attribute attr, List cardIds, ActionRequest request,
			ActionResponse response) throws DataException {
		CardScanForm form = new CardScanForm();
		form.setTargetAttrId(attr.getId());
		form.setNameSrcAttr(nameSrcAttr);
		if(pageName.length() > 0) form.setRenderJSP(pageName);
		portletFormManager.openForm(form);
	}

	public void setPortletFormManager(PortletFormManager portletFormManager) {
		this.portletFormManager = portletFormManager;
	}
	
	public void setParameter(String name, String value) {
		if (PAGE_PARAM.equalsIgnoreCase(name)) {
			this.pageName = value;
		} else if (PAGE_NAME_SRC_ATTR.equals(name)){
			this.nameSrcAttr = IdUtils.smartMakeAttrId(value, CardLinkAttribute.class);
		}
	}

}
