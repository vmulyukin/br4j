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
package com.aplana.dbmi.service;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility class to simplify routine portal-related tasks
 */
public class ServletUtil {

	/**
	 * Creates new DataServiceBean and initializes
	 * it with information about user who performed given HttpServletRequest.
	 * @param request servlet request for which this DataServiceBean instance is created
	 * @return {@link DataServiceBean} instance initialized with information
	 * about user performed given request
	 */
	public static AsyncDataServiceBean createService(HttpServletRequest request)
	{
		AsyncDataServiceBean bean = new AsyncDataServiceBean(request.getSession().getId());
		String userName = (String) request.getSession().getAttribute(DataServiceBean.USER_NAME);
		if (userName != null) {
		    bean.setUser(new UserPrincipal(userName));
		    bean.setIsDelegation(true);
		    bean.setRealUser(request.getUserPrincipal());
		} else {
			Principal up = request.getUserPrincipal();
			if (up == null)
				up = (Principal) request.getSession().getAttribute("MISessionUser");
    		bean.setUser(up);
    		bean.setIsDelegation(false);
		}
		bean.setAddress(request.getRemoteAddr());
		return bean;
	}
	
}
