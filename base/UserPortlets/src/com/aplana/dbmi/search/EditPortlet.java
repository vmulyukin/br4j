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

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.springframework.validation.BindException;
import org.springframework.web.portlet.mvc.SimpleFormController;


public class EditPortlet extends SimpleFormController {
	protected Object  formBackingObject(PortletRequest request) throws Exception {
		WebEditBean editBean = (WebEditBean) super.formBackingObject(request);
		 PortletPreferences preferences = request.getPreferences();
		 editBean.setFileName(preferences.getValue(SearchPortlet.FILE_NAME_PROPERTY, null));
		return editBean;
	}
	
	public void onSubmitAction(ActionRequest request, ActionResponse response,
			Object command,	BindException errors) throws Exception {
		WebEditBean editBean = (WebEditBean) command;
		PortletPreferences pref =
		      request.getPreferences();
		pref.setValue(SearchPortlet.FILE_NAME_PROPERTY, editBean.getFileName());
		pref.store();
	}

}
