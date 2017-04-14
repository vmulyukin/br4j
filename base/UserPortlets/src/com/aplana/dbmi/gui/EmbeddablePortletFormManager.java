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
package com.aplana.dbmi.gui;

import java.io.IOException;
import java.util.Stack;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.aplana.dbmi.card.PortletForm;
import com.aplana.dbmi.card.PortletFormManager;

public class EmbeddablePortletFormManager implements PortletFormManager {
	private PortletForm activeForm = null;
	private Stack<PortletForm> forms = new Stack<PortletForm>();
	
	public void openForm(PortletForm form) {
		if (form == null) {
			closeForm();
			return;
		}
		forms.push(activeForm);
		activeForm = form;
	}

	public void closeForm() {
		activeForm = forms.pop();
	}
	
	protected PortletForm getActiveForm() {
		return activeForm;
	}
	
	public boolean processRender(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        PortletForm form = getActiveForm();
        if (form != null) {
        	form.doFormView(request, response);
        	return true;
        }
        return false;
	}
	
	public boolean processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
        PortletForm form = getActiveForm();
        if (form != null) {
        	form.processFormAction(request, response);
        	return true;
        }
        return false;
	}
	
}
