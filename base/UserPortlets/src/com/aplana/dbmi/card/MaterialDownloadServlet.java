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
import java.security.AccessControlException;

import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ResourceBundle;

import com.aplana.dbmi.common.utils.web.AbstractMaterialDownloadServlet;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServletUtil;

/**
 * Servlet implementation class for Servlet: MaterialDownloadServlet
 * 
 */
public class MaterialDownloadServlet extends AbstractMaterialDownloadServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		try {
			String cardId = request.getParameter(PARAM_CARD_ID);
			
			if (cardId != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Get card ID = " + cardId);
				}
				DataServiceBean serviceBean = ServletUtil.createService(request);
				writeResponse(serviceBean, request, response);
			}
		} catch (AccessControlException e) {
			logger.error(e.getMessage());
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		} catch (DataException e) {
			sendError(request, response, e);
		} catch (Exception e) {
			logger.error(e.getMessage());
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	protected void sendError(HttpServletRequest request, HttpServletResponse response, Throwable t) throws ServletException, IOException 
	{
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		ResourceBundle messages = ResourceBundle.getBundle("com.aplana.dbmi.card.nl.CardPortletResource", request.getLocale());
		try {
			out.println("<html>");
			out.println("<body>");
			out.println("<br>");
			out.println("<font color=\"red\">" + t.getMessage() + "<br></font>");
			out.println("<br>");
			out.println("<br>");

			out.println("<a href=\"javascript: history.go(-1)\">"); 
			out.println(messages.getString("view.page.back.link"));
			out.println("</a>");

			out.println("</body>");
			out.println("</html>");
		} finally {
			out.close();
		}
	}
}