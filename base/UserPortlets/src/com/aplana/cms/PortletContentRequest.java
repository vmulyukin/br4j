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

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.UserPrincipal;

public class PortletContentRequest implements ContentRequest
{
	private PortletRequest request;
	
	public PortletContentRequest(PortletRequest request) {
		this.request = request;
	}
	
	public PortletRequest getPortletRequest() {
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
		String param = request.getParameter(name);
		if (param == null)
			param = Portal.getFactory().getPortletService().getUrlParameter(request, name);
		return param;
	}

	public Map getParameterMap() {
		return request.getParameterMap();
		//TODO: Add URL parameters processing
	}

	public Enumeration getParameterNames() {
		return request.getParameterNames();
		//TODO: Add URL parameters processing
	}

	public String[] getParameterValues(String name) {
		return request.getParameterValues(name);
		//TODO: Add URL parameters processing
	}

	public String getRemoteAddr() {
		return Portal.getFactory().getPortletService().getRemoteAddress(request);
	}

	public String getRemoteUser() {
		return request.getRemoteUser();
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
		return request.getPortletSession().getAttribute(name, PortletSession.APPLICATION_SCOPE);
	}

	public Principal getUserPrincipal() {
        return request.getUserPrincipal();
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
		request.getPortletSession().removeAttribute(name, PortletSession.APPLICATION_SCOPE);
	}

	public void setAttribute(String name, Object value) {
		request.setAttribute(name, value);
	}

	public void setSessionAttribute(String name, Object value) {
		request.getPortletSession().setAttribute(name, value, PortletSession.APPLICATION_SCOPE);
	}
	
	public String getSessionId() {
		return request.getRequestedSessionId();
	}
	
	public long getSessionCreationTime() {
		return request.getPortletSession().getCreationTime();
	}
}
