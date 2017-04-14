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

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SendMail;

/**
 * Utility class to simplify routine portal-related tasks
 */
public class PortletUtil
{
	public static final String SESSION_ATTRIBUTE_SEARCH = Search.class.getName();
	public static final String SESSION_ATTRIBUTE_SERVICE = DataServiceBean.class.getName();
	
	/**
	 * Creates new DataServiceBean and initializes
	 * it with information about user who performed given PortletRequest.
	 * @param request portlet request for which this DataServiceBean instance is created
	 * @return {@link DataServiceBean} instance initialized with information
	 * about user performed given request
	 */
	public static AsyncDataServiceBean createService(PortletRequest request)
	{
		AsyncDataServiceBean bean = new AsyncDataServiceBean();
		String userName = (String) request.getPortletSession().getAttribute(DataServiceBean.USER_NAME, PortletSession.APPLICATION_SCOPE);
		if (userName != null) {
		    bean.setUser(new UserPrincipal(userName));
		    bean.setIsDelegation(true);
		    bean.setRealUser(request.getUserPrincipal());
		} else {
    		bean.setUser(request.getUserPrincipal());
		}
		bean.setAddress(Portal.getFactory().getPortletService().getRemoteAddress(request));
		return bean;
	}
	
	/** 
	 * @deprecated Use PortletService.getPageProperty() instead
     */
	public static String getPageProperty(String name, PortletRequest request, PortletResponse response)
	{
		return Portal.getFactory().getPortletService().getPageProperty(name, request, response);
	}


	/**
	 * Sends email with given parameters
	 * @param dataServiceBean link to initialized {@link DataServiceBean} instance
	 * @param recipient email recipient
	 * @param subject subject
	 * @param body text of email
	 * @return true if send succeed, false otherwise
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException in case of technical error
	 */
	public static boolean sendMail(DataServiceBean dataServiceBean, String recipient, String subject, String body) throws DataException, ServiceException
	{
		Boolean result = (Boolean) dataServiceBean.doAction(new SendMail(recipient, subject, body));
		return result != null ? result.booleanValue() : true;
	}
}
