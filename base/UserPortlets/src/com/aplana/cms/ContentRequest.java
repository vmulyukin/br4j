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

public interface ContentRequest
{
	public static final String PARAM_AREA = "area";
	public static final String PARAM_ITEM = "item";
	public static final String PARAM_NEXT_ITEM = "nextItem";	
	public static final String PARAM_SORT_COLUMN_ID = "sortColumnId";
	public static final String PARAM_RESET_CACHE = "resetCache";
	
	public static final String APP_ATTR_USER = "cms.user";
	
	// copied from PortletRequest
	public Object getAttribute(String name);
	public Enumeration getAttributeNames();
	public String getAuthType();
	public String getContextPath();
	public Locale getLocale();
	public Enumeration getLocales();
	public String getParameter(String name);
	public Map getParameterMap();
	public Enumeration getParameterNames();
	public String[] getParameterValues(String name);
	public String getRemoteAddr();
	public String getRemoteUser();
	public String getRequestedSessionId();
	public String getScheme();
	public String getServerName();
	public int getServerPort();
	public Principal getUserPrincipal();
	public boolean isRequestedSessionIdValid();
	public boolean isSecure();
	public void removeAttribute(String name);
	public void setAttribute(String name, Object value);
	public String getSessionId();
	public long getSessionCreationTime();
	
	// session replacing attributes
	public Object getSessionAttribute(String name);
	public void removeSessionAttribute(String name);
	public void setSessionAttribute(String name, Object value);
}
