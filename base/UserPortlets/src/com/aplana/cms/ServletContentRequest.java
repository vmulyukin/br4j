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

import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class ServletContentRequest implements ContentRequest
{
	private HttpServletRequest request;
	
	public ServletContentRequest(HttpServletRequest request) {
		this.request = request;
	}
	
	public HttpServletRequest getServletRequest() {
		return request;
	}

	public Object getAttribute(String name) {
		return request.getAttribute(name);
	}

	public Enumeration getAttributeNames() {
		return request.getAttributeNames();
	}

	public String getAuthType() {
		return request.getAuthType();
	}

	public String getContextPath() {
		return request.getContextPath();
	}

	public Locale getLocale() {
		return request.getLocale();
	}

	public Enumeration getLocales() {
		return request.getLocales();
	}

	public String getParameter(String name) {
		return request.getParameter(name);
	}

	public Map getParameterMap() {
		return request.getParameterMap();
	}

	public Enumeration getParameterNames() {
		return request.getParameterNames();
	}

	public String[] getParameterValues(String name) {
		return request.getParameterValues(name);
	}

	public String getRemoteAddr() {
		return request.getRemoteAddr();
	}

	public String getRemoteUser() {
		String user = request.getRemoteUser();
		if (user == null) {
			Principal principal = (Principal) getSessionAttribute(APP_ATTR_USER);
			if (principal != null)
				user = principal.getName();
		}
		return user;
	}

	public String getRequestedSessionId() {
		return request.getRequestedSessionId();
	}

	public String getScheme() {
		return request.getScheme();
	}

	public String getServerName() {
		return request.getServerName();
	}

	public int getServerPort() {
		return request.getServerPort();
	}

	public Object getSessionAttribute(String name) {
		return request.getSession().getAttribute(name);
	}

	public Principal getUserPrincipal() {
	    Principal user = request.getUserPrincipal();
        if (user == null)
            user = (Principal) getSessionAttribute(APP_ATTR_USER);
        return user;
	}

	public boolean isRequestedSessionIdValid() {
		return request.isRequestedSessionIdValid();
	}

	public boolean isSecure() {
		return request.isSecure();
	}

	public void removeAttribute(String name) {
		request.removeAttribute(name);
	}

	public void removeSessionAttribute(String name) {
		request.getSession().removeAttribute(name);
	}

	public void setAttribute(String name, Object value) {
		request.setAttribute(name, value);
	}

	public void setSessionAttribute(String name, Object value) {
		request.getSession().setAttribute(name, value);
	}
	
	public String getSessionId() {
		return request.getRequestedSessionId();
	}
	
	public long getSessionCreationTime() {
		return request.getSession().getCreationTime();
	}
}
