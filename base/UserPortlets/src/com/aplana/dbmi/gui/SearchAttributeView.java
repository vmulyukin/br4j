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

import com.aplana.dbmi.card.AttributeEditor;
import com.aplana.dbmi.card.AttributeEditorFactory;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.search.SearchFilterPortlet;
import com.aplana.dbmi.search.SearchFilterPortletSessionBean;
import com.aplana.dbmi.service.DataException;

import javax.portlet.PortletRequest;

/**
 * Represents View for Search Attribute Filter inside extended search form 
 * @author skashanski
 *
 */
public class SearchAttributeView extends AttributeView {
	
	private AttributeEditor searchEditor;
	
	private boolean spanedView = false;

	public SearchAttributeView(Attribute attribute) {
		super(attribute);
	}

	@Override
	public void initEditor(PortletRequest request) {
		SearchFilterPortletSessionBean sessonBean = SearchFilterPortlet.getSessionBean(request);
		searchEditor = AttributeEditorFactory.getFactory().getFilterEditor(attribute, sessonBean);
		if (searchEditor == null)
			return;
		try {
			searchEditor.initEditor(request, attribute);
		} catch (DataException e) {
			e.printStackTrace();//TODO discuss possibility to throw exception
		}
	}

	@Override
	public AttributeEditor getEditor() {
		return searchEditor;
	}

	public boolean isSpanedView() {
		return spanedView;
	}

	public void setSpanedView(boolean spanedView) {
		this.spanedView = spanedView;
	}
}
