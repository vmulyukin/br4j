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

import java.io.PrintWriter;

import javax.portlet.GenericPortlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.portlet.context.PortletApplicationContextUtils;

import com.aplana.dbmi.model.Card;

public abstract class ContentPortlet extends GenericPortlet
{
	//public static final String PARAM_AREA = "area";
	//public static final String PARAM_ITEM = "item";
	//public static final String PARAM_RESET_CACHE = "resetCache";
	
	protected Log logger = LogFactory.getLog(getClass());

	public ContentPortlet() {
		super();
	}
	
	/**
	 * Returns spring application portlet context
	 */
	protected ApplicationContext getSpringApplicationContext() {
		
		return  PortletApplicationContextUtils.getWebApplicationContext(this.getPortletContext());
	}
	

	/*protected void processParameters(PortletRequest request) {
		PortletService svc = Portal.getFactory().getPortletService();
		String param = svc.getUrlParameter(request, PARAM_AREA);
		if (param != null && param.length() > 0) {
			request.getPortletSession().setAttribute(PARAM_AREA, param, PortletSession.APPLICATION_SCOPE);
			request.getPortletSession().removeAttribute(PARAM_ITEM, PortletSession.APPLICATION_SCOPE);
		}
		param = svc.getUrlParameter(request, PARAM_ITEM);
		if (param != null && param.length() > 0)
			request.getPortletSession().setAttribute(PARAM_ITEM, param, PortletSession.APPLICATION_SCOPE);
		param = svc.getUrlParameter(request, PARAM_RESET_CACHE);
		if (param != null) {
			CacheManager.resetCache();
			logger.info("Cache reset by user's request");
		}
	}*/

	protected void writeContent(Card content, String view, PrintWriter writer,
			ContentProducer cms)
	{
		cms.writeContent(writer, view, content);
	}
}