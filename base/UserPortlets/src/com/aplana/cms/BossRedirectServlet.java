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
package com.aplana.cms;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.aplana.cms.ContentDataAdapter;
import com.aplana.cms.ContentProducer;
import com.aplana.cms.ServletContentRequest;
import com.aplana.cms.ServletContentResponse;
import com.aplana.dbmi.model.Card;

/**
 * The servlet designed to redirect request to a desired workstation(ARM) portlet
 */
public class BossRedirectServlet extends HttpServlet {
	
	private static final String DEFAULT_REDIRECT_URL = "/portal/auth/portal/boss"; //ContentViewPortlet
	private static final String ADVANCED_SEARCH_REDIRECT_URL = "/portal/auth/portal/boss/advancedSearchRes"; //WorkstationAdvancedSearchPortlet

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException 
	{
		resp.setContentType("text/html");
		resp.setCharacterEncoding("UTF-8");
		
		ServletContentRequest wrappedReq = new ServletContentRequest(req);
		ServletContentResponse wrappedResp = new ServletContentResponse(req, resp);
		ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext()); 
		ContentProducer cms = new ContentProducer(wrappedReq, wrappedResp, applicationContext, false, false);
		Card area = cms.getCurrentSiteArea(false); // current site area
		
		String url = DEFAULT_REDIRECT_URL;
		//if the current site area is Advanced search area then redirecting request to WorkstationAdvancedSearchPortlet
		if (area != null
				&& area.getId() != null
				&& area.getId().getId() != null
				&& ContentDataAdapter.ADVANCED_SEARCH_FOLDER_ID.equals(area.getId().getId().toString()))
			url = ADVANCED_SEARCH_REDIRECT_URL;
		
		resp.sendRedirect(url);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException 
	{
		doGet(req, resp);
	}
}
