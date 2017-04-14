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
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.service.DataException;


public class CardLinkAttributeViewer extends ActionsSupportingAttributeEditor
{
	public static final String PARAM_SHOW_TITLE = "showTitle";
	public static final String PARAM_SHOW_EMPTY = "showEmpty";
	public static final String KEY_SHOW_TITLE = "showTitle";
	public static final String KEY_SHOW_EMPTY = "showEmpty";
	private boolean showTitle = true;
	private boolean showEmpty = true;
	
	public CardLinkAttributeViewer() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/LinksView.jsp");
	}

	@Override
	public void initEditor(PortletRequest request, Attribute attr)
			throws DataException 
	{
		super.initEditor(request, attr);
		loadAttributeValues(attr, request);
	}

	@Override
	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException {
		return false;
	}

	@Override
	public void loadAttributeValues(Attribute attr, PortletRequest request) {
		LinkedCardUtils.reloadLinks(request, (CardLinkAttribute) attr);
	}

	@Override
	public void writeEditorCode(RenderRequest request, RenderResponse response,
			Attribute attr) throws IOException, PortletException {
		CardPortletCardInfo info = getCardPortletSessionBean(request).getActiveCardInfo();
		info.setAttributeEditorData(attr.getId(), KEY_SHOW_TITLE, new Boolean(showTitle));
		info.setAttributeEditorData(attr.getId(), KEY_SHOW_EMPTY, new Boolean(showEmpty));
		super.writeEditorCode(request, response, attr);
	}

	@Override
	public void setParameter(String name, String value) {
		if (PARAM_SHOW_TITLE.equalsIgnoreCase(name))
			showTitle = Boolean.parseBoolean(value);
		else if (PARAM_SHOW_EMPTY.equalsIgnoreCase(name))
			showEmpty = Boolean.parseBoolean(value);
		else
			super.setParameter(name, value);
	}

	public boolean isShowTitle() {
		return showTitle;
	}

	public boolean isShowEmpty() {
		return showEmpty;
	}
}