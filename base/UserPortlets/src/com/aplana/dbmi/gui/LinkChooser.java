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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.aplana.dbmi.model.DataObject;

/**
 * Supports the LinkChooser JSP page portlet implementation. 
 * Even if this class do not implements directly the portlet interface
 * its methods {@link #doView(RenderRequest, RenderResponse)} 
 * and {@link #processAction(ActionRequest, ActionResponse)} are developer
 * to be "injected" in the code of a real calling portlet class.
 * 
 * This class has been implemented following the {@link ListEditor} previous
 * implementation.
 * 
 * @author Mnagni
 **/
public class LinkChooser implements IListEditor {
	public static final String JSP_PATH = "/WEB-INF/jsp/html/LinkChooser.jsp";
	
	public static final String SELECTED = "editor_SelectedID";
    public static final String CONTENT_TYPE = "CONTENT_TYPE";
    
	private ListDataProvider dataProvider;
	private int listSize = 20; 
	final private List selected = new ArrayList/*ObjectId[]*/(); 

	public ListDataProvider getDataProvider() {
		return dataProvider;
	}

	public int getListSize() {
		return listSize;
	}
	
	public void setDataProvider(ListDataProvider dataProvider) {
		this.dataProvider = dataProvider;
		this.selected.clear();
		if (dataProvider != null)
			selected.addAll(dataProvider.getSelectedListData());
	}
	
	public List getCurrentList() {
		return selected;
	}
	
	/**
	 * {@inheritDoc}
	 **/
	public boolean doView(RenderRequest request, RenderResponse response)
	throws PortletException, IOException {
		if (dataProvider == null)
			return false;
		if (dataProvider instanceof SearchableListDataProvider)
			((SearchableListDataProvider) dataProvider).initSearchForm(request);
		request.getPortletSession().getPortletContext().getRequestDispatcher(JSP_PATH)
				.include(request, response);
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 **/
	public boolean processAction(ActionRequest request, ActionResponse response) {
		String action = request.getParameter(FIELD_ACTION);
		if (action !=null)
			if (!action.startsWith(ACTION_PREFIX))
				return false;
			else if (ACTION_SEARCH.equals(action))
				processSearch(request);
			else if (ACTION_SAVE.equals(action))
				processSingleSelection(request);
			else if (ACTION_CLOSE.equals(action)) {
				return false;	
			}			
		return true;
	}

	private void processSingleSelection(ActionRequest request) {		
		final Map params = request.getParameterMap();
		final List items = dataProvider.getListData();
		selected.clear();
		for (Iterator itr = items.iterator(); itr.hasNext(); ) {
			DataObject item = (DataObject) itr.next();
			if (	params.containsKey(SELECTED) && 
					((String[])params.get(SELECTED))[0].equals(item.getId().getId().toString())) 
			{
				selected.add(item.getId());
				break;
			}
		}		
		dataProvider.setSelectedList(selected);
	}	

	private void processSearch(ActionRequest request) {
		if (dataProvider instanceof SearchableListDataProvider)
			((SearchableListDataProvider) dataProvider).processSearch(request);
	}
}
