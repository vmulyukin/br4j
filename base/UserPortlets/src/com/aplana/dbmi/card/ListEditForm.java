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

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.aplana.dbmi.gui.IListEditor;
import com.aplana.dbmi.model.ObjectId;

public class ListEditForm implements PortletForm
{
	private IListEditor editor;
	
	public ListEditForm(IListEditor editor) {
		this.editor = editor;
	}

	public void doFormView(RenderRequest request, RenderResponse response)
			throws IOException, PortletException {
    	request.setAttribute(IListEditor.ATTR_INSTANCE, editor);
		if (!editor.doView(request, response))
			CardPortlet.getSessionBean(request).closeForm();
	}

	public void processFormAction(ActionRequest request, ActionResponse response)
			throws IOException, PortletException 
	{
		if (!editor.processAction(request, response))
			CardPortlet.getSessionBean(request).closeForm();
		if (editor.getDataProvider().getSelectedListData() != null 
				&& editor.getDataProvider().getSelectedListData().size() > 0) 
		{
			final CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
			sessionBean.getLinkChooserBean()
				.setSelected((ObjectId) editor.getDataProvider().getSelectedListData().get(0));
			sessionBean.getLinkChooserBean().setDataAvailable(true);
		}
	}
}
