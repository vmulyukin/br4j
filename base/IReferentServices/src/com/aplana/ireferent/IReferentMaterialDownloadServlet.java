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
package com.aplana.ireferent;

import java.io.IOException;
import java.security.AccessControlException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aplana.dbmi.common.utils.web.AbstractMaterialDownloadServlet;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.IReferentException;
import com.aplana.ireferent.util.ServiceUtils;

/**
 * Servlet implementation class for IReferentMaterialDownloadServlet
 * The Servlet is used by IReferent web service clients (IReferent Mobile Proxy) to download files.
 * Only service users assigned with special web service role ("JBR_WS_CLIENT") have access to this servlet.
 * The following parameters must be provided:
 * MI_CARD_ID_FIELD - file card id
 * userid - real user id who is requesting the file.
 * 
 * @author valexandrov
 * @version 1.0
 * @since   2014-05-16
 */
public class IReferentMaterialDownloadServlet extends AbstractMaterialDownloadServlet {
	
	private static final long serialVersionUID = 1L;
	public static final String PARAM_USER_ID = "userid";

	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public IReferentMaterialDownloadServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String cardId = request.getParameter(PARAM_CARD_ID);
			String userId = request.getParameter(PARAM_USER_ID);
			if (null == cardId) {
				//cardid must be supplied
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} else if (null == userId) {
				//userid must be supplied
				response.sendError(HttpServletResponse.SC_BAD_REQUEST );
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("Get card ID = " + cardId + " by user ID = " + userId);
				}
				DataServiceBean serviceBean = ServiceUtils.authenticateUser(userId, request);
				writeResponse(serviceBean, request, response);
			}
		} catch (AccessControlException e) {
			logger.error(e.getMessage());
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		} catch (IReferentException e) {
			logger.warn(e.getMessage());
			response.sendError(HttpServletResponse.SC_FORBIDDEN );
		} catch (DataException e) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND );
		} catch (Exception e) {
			logger.error(e.getMessage());
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
		}
	}
}