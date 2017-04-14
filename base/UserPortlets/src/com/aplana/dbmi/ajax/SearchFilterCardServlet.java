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
package com.aplana.dbmi.ajax;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONWriter;

import com.aplana.dbmi.action.GetPersonSearchByNameAndArea;
import com.aplana.dbmi.model.PersonalSearch;
import com.aplana.dbmi.service.DataServiceBean;

/**
 * Provides ajax functionality for SearchFilter 
 *   
 * @author skashanski
 *
 */
public class SearchFilterCardServlet extends AbstractDBMIAjaxServlet {
	
	public static String SEARCH_AREA = "searchArea";
	
	public static String SEARCH_NAME = "searchName";
	
	@Override
	protected void generateResponse(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		DataServiceBean serviceBean = getDataServiceBean(request);
		
		
		String  area = request.getParameter(SEARCH_AREA);
		if (area == null) 
			area = "";
		
		String  name = request.getParameter(SEARCH_NAME);
		if (name == null) 
			name = "";	

		
		GetPersonSearchByNameAndArea action = new GetPersonSearchByNameAndArea();
		action.setSearchArea(area);
		action.setSearchName(name);
		PersonalSearch result = null;
		try {
			result = (PersonalSearch)serviceBean.doAction(action);
			
			writePersonalSearch(response, result);
			
		} catch (Exception e) {
			logger.error("Impossible to get PersonalSearch by passed name " + name + " and area " + area, e);
			throw new ServletException(e);
		}
		

		
	}
	
	private void writePersonalSearch(HttpServletResponse response, PersonalSearch search) throws ServletException {
		
		try {
			
			JSONWriter jw = new JSONWriter(response.getWriter());
			jw.object();
			jw.key("id").value(search.getId());
			jw.key("name").value(search.getArea());
			jw.endObject();
		} catch (Exception e) {
			logger.error("Error generating response", e);
			throw new ServletException(e);
		}
		
	}
}
