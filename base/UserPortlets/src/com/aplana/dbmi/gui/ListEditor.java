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
import com.aplana.dbmi.model.ObjectId;

public class ListEditor implements IListEditor
{
	public static final String JSP_PATH = "/WEB-INF/jsp/html/ListEdit.jsp";
	public static final String ATTR_MESSAGE = "message";
	
	public static final String FORM_NAME = "listEditor";
	public static final String FIELD_ADD_PREFIX = "add_";
	public static final String FIELD_REMOVE_PREFIX = "remove_";
	
	public static final String ACTION_ADD = ACTION_PREFIX + "add";
	public static final String ACTION_REMOVE = ACTION_PREFIX + "remove";
	public static final String DISPLAY_FIELD_BY_NUMBER = "displayById";
	public static final String DISPLAY_FIELD_BY_ATTR = "displayByAttr";
	public static final String DISPLAY_FIELD_BY_TEXT = "displayByText";	

	protected ListDataProvider dataProvider;
	private int listSize = 20;
	protected final List selectedIds = new ArrayList();
	private boolean active = true;
	
	/**
	 * flag to indicate if we need to display Search Parameter :  "By number"
	 */
	private boolean displaySearchByNumber = true;
	
	/**
	 * flag to indicate if we need to display Search Parameter :  "By attributes"
	 */
	private boolean displaySearchByAttributes = true;
	
	/**
	 * flag to indicate if we need to display Search Parameter :  "By material"
	 */
	private boolean displaySearchByMaterial = true;	
	
	
	
	/**
	 * flag to indicate if we need to display linked columns as "Link"
	 */
	private boolean displayLinkedColumns = false;
	

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}
	
	public boolean isDisplaySearchByNumber() {
		return displaySearchByNumber;
	}

	public void setDisplaySearchByNumber(boolean displaySearchByNumber) {
		this.displaySearchByNumber = displaySearchByNumber;
	}

	public boolean isDisplaySearchByAttributes() {
		return displaySearchByAttributes;
	}

	public void setDisplaySearchByAttributes(boolean displaySearchByAttributes) {
		this.displaySearchByAttributes = displaySearchByAttributes;
	}

	public boolean isDisplaySearchByMaterial() {
		return displaySearchByMaterial;
	}

	public void setDisplaySearchByMaterial(boolean displaySearchByMaterial) {
		this.displaySearchByMaterial = displaySearchByMaterial;
	}

	public boolean isDisplayLinkedColumns() {
		return displayLinkedColumns;
	}

	public void setDisplayLinkedColumns(boolean displayLinkedColumns) {
		this.displayLinkedColumns = displayLinkedColumns;
	}

	public ListDataProvider getDataProvider() {
		return dataProvider;
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.gui.IListEditor#setDataProvider(com.aplana.dbmi.gui.ListDataProvider)
	 */
	public void setDataProvider(ListDataProvider dataProvider) {
		this.dataProvider = dataProvider;
		this.selectedIds.clear();
		if (dataProvider != null && (dataProvider.getSelectedListData() != null)) 
			this.selectedIds.addAll( dataProvider.getSelectedListData() );
	}

	public int getListSize() {
		return listSize;
	}

	public void setListSize(int listSize) {
		this.listSize = listSize;
	}

	public List getCurrentList() {
		return selectedIds;
	}

	/**
	 * {@inheritDoc}
	 **/
	public boolean doView(RenderRequest request, RenderResponse response)
		throws PortletException, IOException 
	{
		if (dataProvider == null)
			return false;
		if (dataProvider instanceof SearchableListDataProvider) {
			 changeSearchDisplayParameters(request);
			((SearchableListDataProvider) dataProvider).initSearchForm(request);
		}	
		String jspPage = getJspPage();
		
		request.getPortletSession().getPortletContext().getRequestDispatcher(jspPage)
				.include(request, response);
		return true;
	}
	
	protected String getJspPage() {
		return JSP_PATH;
	}
	
	
	protected void changeSearchDisplayParameters(RenderRequest request) {
		
		request.setAttribute(DISPLAY_FIELD_BY_NUMBER, displaySearchByNumber);
		
		request.setAttribute(DISPLAY_FIELD_BY_ATTR, displaySearchByAttributes);
		
		request.setAttribute(DISPLAY_FIELD_BY_TEXT, displaySearchByMaterial);
	}
	

	/**
	 * {@inheritDoc}
	 **/
	public boolean processAction(ActionRequest request, ActionResponse response) {
		String action = request.getParameter(FIELD_ACTION);
		if (action == null || !action.startsWith(ACTION_PREFIX))
			return false;
		if (action !=null)
		if (ACTION_ADD.equals(action))
			processAdd(request);
		else if (ACTION_REMOVE.equals(action))
			processRemove(request);
		else if (ACTION_SEARCH.equals(action))
			processSearch(request);
		else if (ACTION_SAVE.equals(action))
			processSave(request);
		else if (ACTION_COMPLETE.equals(action)) {
			
			processSave(request);		
			setActive(false);
			return false;

		} else if (ACTION_CLOSE.equals(action)) {
			setActive(false);
			return false;	
		}
		return true;
	}

	private void processAdd(ActionRequest request) {
		final Map params = request.getParameterMap();
		final List items = dataProvider.getListData();
		//selectedIds.clear();
		for (Iterator itr = items.iterator(); itr.hasNext(); ) {
			final DataObject item = (DataObject) itr.next();
			if (params.containsKey(FIELD_ADD_PREFIX + item.getId().getId()) &&
					!isSelected(item))
				selectedIds.add( item.getId());
		}
	}

	private void processRemove(ActionRequest request) {
		final Map params = request.getParameterMap();
		for (Iterator itr = selectedIds.iterator(); itr.hasNext(); ) {
			// DataObject item = (DataObject) itr.next();
			final ObjectId item = (ObjectId) itr.next();
			if (params.containsKey(FIELD_REMOVE_PREFIX + item.getId()))
				itr.remove();
		}
	}

	private boolean isSelected(DataObject object) {
		return (object != null) && (dataProvider != null) 
				&& (dataProvider.getSelectedListData() != null)
				&& (dataProvider.getSelectedListData().contains(object.getId()));
//		(2010/02, RuSA) ������� �� Collection<ObjectId> ������ Collection<DataObject>
//		for (Iterator itr = dataProvider.getSelectedListData().iterator(); itr.hasNext(); ) {
//			// DataObject item = (DataObject) itr.next();
//			final ObjectId item = (ObjectId) itr.next();
//			if (item.equals(object.getId()))
//				return true;
//		}
//		return false;
	}

	protected void processSave(ActionRequest request) {
		dataProvider.setSelectedList(selectedIds);
	}

	private void processSearch(ActionRequest request) {
		if (dataProvider instanceof SearchableListDataProvider)
			((SearchableListDataProvider) dataProvider).processSearch(request);
	}
}
