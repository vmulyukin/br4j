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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.RenderRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.filter.UserIdFilter;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class PersonList implements SearchableListDataProvider
{
	public static final String JSP_SEARCH_PATH = "/WEB-INF/jsp/html/PersonSearchForm.jsp";
	public static final String FIELD_NAME = "name";
	public static final String RESOURCES_PATH = "com/aplana/dbmi/gui/nl/PersonListResource";
	
	protected Log logger = LogFactory.getLog(getClass());
	private DataServiceBean service;
	private String name = "";
	private List users; // Person list
	private List selectedUsers;
	
	private static final String COL_NAME = "name";	
	// TODO: ������� ����� ������� � ������������� � �������
	// private static final String COL_DEPT = "dept";
	private static final String[] COLUMNS = new String[] { COL_NAME /*, COL_DEPT */};
	
	public PersonList(DataServiceBean service) throws DataException, ServiceException {
		this.service = service;
		users = (List) service.listAll(Person.class);
	}

	public String getFormJspPath() {
		return JSP_SEARCH_PATH;
	}

	public void initSearchForm(RenderRequest request) {
		request.setAttribute(FIELD_NAME, name);
	}

	public void processSearch(ActionRequest request) {
		name = request.getParameter(FIELD_NAME);
		try {
			UserIdFilter filter = new UserIdFilter(name);
			users = (List) service.filter(Person.class, filter);
		} catch (Exception e) {
			logger.error("User search error", e);
			request.setAttribute(ListEditor.ATTR_MESSAGE, e.getMessage());
		}
	}
	
	

	public boolean isColumnLinked(String column) {
		//there is no possibility to display link for person list
		return false;
		
	}

	public String getColumnTitle(String column) {
		return ResourceBundle.getBundle(RESOURCES_PATH, ContextProvider.getContext().getLocale())
				.getString("user.column." + column);
	}

	public List getColumns() {
		return Arrays.asList(COLUMNS);
	}

	public List getListData() {
		return users;
	}

	public String getListTitle() {
		return ResourceBundle.getBundle(RESOURCES_PATH, ContextProvider.getContext().getLocale())
				.getString("user.list.title");
	}

	public List getSelectedListData() {
		return selectedUsers;
	}

	public String getSelectedListTitle() {
		return ResourceBundle.getBundle(RESOURCES_PATH, ContextProvider.getContext().getLocale())
				.getString("user.selected.title");
	}

	// (2010/02, RuSA) value list  =>  id list
	// OLD: public String getValue(DataObject item, String column) {
	public String getValue(ObjectId item, String column) {
		// OLD: final Person person = (Person) item;
		final Person person = findPersonById( item);
		if (person == null) return "";
		if (COL_NAME.equals(column))
			return person.getFullName();
		//if (COL_DEPT.equals(column))
		//	return person.getDepartment() == null ? "" : person.getDepartment().getValue();
		throw new IllegalArgumentException("Unknown column name: " + column);
	}

	private Person findPersonById(ObjectId userId) {
		if (userId != null && this.users != null) {
			for (Iterator iterator = this.users.iterator(); iterator.hasNext();) {
				final Person user = (Person) iterator.next();
				if (userId.equals(user.getId())) 
					return user; // FOUND
			}
		}
		return null; // NOT FOUND
	}

	public void setSelectedList(List data) {
		selectedUsers = data;
	}

	public int getColumnWidth(String column) {
		if (COL_NAME.equals(column))
			return 50;
		//if (COL_DEPT.equals(column))
		//	return 50;
		throw new IllegalArgumentException("Unknown column name: " + column);
	}
}
