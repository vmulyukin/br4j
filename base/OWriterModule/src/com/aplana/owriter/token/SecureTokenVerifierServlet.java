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
package com.aplana.owriter.token;

import java.io.IOException;
import java.security.AccessControlException;

import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ResourceBundle;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ������� ��� �������� ������.
 * ������� �� ���� �� ������ ����� ������������ ��������� �������� �������� ��� ������� � ������ ����:
 *		<?xml version="1.0" ?>
 * 		<user login="{login}" />
 *
 * @author Vlad Alexandrov
 * @version 1.0
 * @since   2014-10-03
 */
public class SecureTokenVerifierServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
	
	protected final Log logger = LogFactory.getLog(getClass());
	private static final long serialVersionUID = 1L;
	private static final String PARAM_TOKEN = "token";

	public SecureTokenVerifierServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		try {
			String token = request.getParameter(PARAM_TOKEN);
			
			if (null != token) {
				if (logger.isDebugEnabled()) {
					logger.debug("Got token = " + token);
				}
				String login = SecureTokenGenerator.getCachedUnencryptedString(token);
				if (null != login) {
					PrintWriter out = response.getWriter();
					response.setContentType("text/xml;charset=UTF-8");
					out.println("<?xml version=\"1.0\" ?>");
					out.println("<user login=\"" + login + "\"/>");
				}else {
					logger.warn("Login not found in time-based cache for token " + token);
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
				}
			}else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
		} catch (AccessControlException e) {
			logger.error(e.getMessage());
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		} catch (Exception e) {
			logger.error(e.getMessage());
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}