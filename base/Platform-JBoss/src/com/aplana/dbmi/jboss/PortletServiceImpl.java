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
package com.aplana.dbmi.jboss;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
//import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.api.node.PortalNode;
import org.jboss.portal.portlet.aspects.portlet.ContextDispatcherInterceptor;
//import org.jboss.portal.portlet.impl.spi.AbstractPortletInvocationContext;
import org.jboss.portal.portlet.invocation.ActionInvocation;
import org.jboss.portal.portlet.invocation.PortletInvocation;
import org.jboss.portal.portlet.invocation.RenderInvocation;
import org.jboss.portlet.JBossActionRequest;
import org.jboss.portlet.JBossRenderRequest;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortletService;

public class PortletServiceImpl implements PortletService
{
	private static final String PARAM_ACTION = "action";
	private static final String PARAM_STATE = "windowstate";
	private static final String PARAM_MODE = "mode";
	private static final String ACTION_DEFAULT = "e";
	private static final String STATE_NORMAL = "normal";
	private static final String MODE_VIEW = "view";
	private static final String MODE_EDIT = "edit";
	private static final String MODE_HELP = "help";
	
	protected Log logger = LogFactory.getLog(getClass());
	private Properties pageIds = null;
	
	public String getPageProperty(String name, PortletRequest request, PortletResponse response) {
		//printAttributes(request);
		request = wrapRequest(request);
        PortalNode node;
	    if (request instanceof JBossActionRequest)
	        node = ((JBossActionRequest) request).getPortalNode();
	    else if (request instanceof JBossRenderRequest)
	        node = ((JBossRenderRequest) request).getPortalNode();
	    else {
	    	logger.info("Request class is: " + request.getClass().getName());
			throw new IllegalArgumentException("request should be a JBossRenderRequest or JBossActionRequest");
	    }
		return (String) node.getProperties().get(name);
	}
	
	public String getParentPageName(PortletRequest request) {
		//printAttributes(request);
		request = wrapRequest(request);
		PortalNode node;
		if (request instanceof JBossActionRequest)
			node = ((JBossActionRequest) request).getPortalNode();
		else if (request instanceof JBossRenderRequest)
			node = ((JBossRenderRequest) request).getPortalNode();
		else {
			logger.info("Request class is: " + request.getClass().getName());
			throw new IllegalArgumentException("request should be a JBossRenderRequest or JBossActionRequest");
		}
		return (String) node.getParent().getName();
	}

	public String generateLink(String page, String window, Map params,
			PortletRequest request, PortletResponse response) {
		//printAttributes(request);
		request = wrapRequest(request);
		initIds();
		if (page == null) {
			if (!(request instanceof JBossRenderRequest)) {
				logger.error("Current page can be used only in render request processors");
				return "#";
			}
			page = "$" + getCurrentPage(request);
		} else if (!page.contains("/")) {
			if (!pageIds.containsKey(page)) {
				logger.warn("WARNING! Page " + page + " not configured; empty link generated");
				return "#";
			}
			page = pageIds.getProperty(page);
		}
		StringBuffer url = new StringBuffer(page).append("/");
		if (url.toString().startsWith("$"))
			url.replace(0, 1, getPortalContextPath(request));
		if (window == null && params == null) {
			return url.toString();
		} else if (window == null && params != null) {
		    Iterator itr = params.keySet().iterator();
		    boolean first = true;
            while (itr.hasNext()) {
                Object key = itr.next();
                if (first) {
                    url.append("?").append(key).append("=").append(params.get(key));
                    first = false;
                } else {
                    url.append("&").append(key).append("=").append(params.get(key));
                }
            }
            return url.toString();
		}
		url.append(pageIds.getProperty(window)).append("?");
		url.append(PARAM_ACTION).append("=").append(ACTION_DEFAULT).append("&");
		url.append(PARAM_STATE).append("=").append(STATE_NORMAL).append("&");
		url.append(PARAM_MODE).append("=").append(MODE_VIEW);
		if (params != null) {
			Iterator itr = params.keySet().iterator();
			while (itr.hasNext()) {
				Object key = itr.next();
				url.append("&").append(key).append("=").append(params.get(key));
			}
		}
		return url.toString();
	}
	
	private void initIds() {
		if (pageIds != null)
			return;
		pageIds = new Properties();
		try {
			InputStream config = Portal.getFactory().getConfigService().loadConfigFile("dbmi/pageids.properties");
			if (config != null)
				pageIds.load(config);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getRemoteAddress(PortletRequest request) {
		//printAttributes(request);
		request = wrapRequest(request);
		if (!(request instanceof JBossRenderRequest || request instanceof JBossActionRequest))
			throw new IllegalArgumentException("request should be a JBossRenderRequest or a JBossActionRequest");
		try {
			/* http://lists.jboss.org/pipermail/jboss-user/2007-March/047061.html */
			PortletInvocation invocation =
				(PortletInvocation) request.getAttribute(ContextDispatcherInterceptor.REQ_ATT_COMPONENT_INVOCATION);
			return invocation.getDispatchedRequest().getRemoteAddr();
			//AbstractPortletInvocationContext ctx = (AbstractPortletInvocationContext) invocation.getContext();
			//return ctx.getClientRequest().getRemoteAddr();
		} catch (Throwable e) {
			LogFactory.getLog(getClass()).warn("Can't get user's IP address", e);
		}
		return null;
	}
	
	public String getPortalContextPath(PortletRequest request) {
		return (String) request.getAttribute("org.jboss.portal.PORTAL_CONTEXT_PATH") + "/portal";
	}
	
	public String getCurrentPage(PortletRequest request) {
		//printAttributes(request);
		request = wrapRequest(request);
		PortalNode node = null;
		if (request instanceof JBossRenderRequest)
			node = ((JBossRenderRequest) request).getPortalNode();
		else if (request instanceof JBossActionRequest)
			node = ((JBossActionRequest) request).getPortalNode();
		else
			throw new IllegalArgumentException("request should be a JBossRenderRequest or a JBossActionRequest");
		StringBuffer page = new StringBuffer();
		while (node != null) {
			/*if (node.getType() == PortalNode.TYPE_CONTEXT && node.getName() == null)
				page.insert(0, "/portal");
			else if (node.getType() != PortalNode.TYPE_WINDOW)*/
			if (node.getType() == PortalNode.TYPE_PAGE || node.getType() == PortalNode.TYPE_PORTAL)
				page.insert(0, "/" + node.getName());
			node = node.getParent();
		}
		return page.toString();
	}
	
	public String getUrlParameter(PortletRequest request, String name) {
		//printAttributes(request);
		request = wrapRequest(request);
		if (!(request instanceof JBossRenderRequest || request instanceof JBossActionRequest))
			throw new IllegalArgumentException("request should be a JBossRenderRequest or a JBossActionRequest");
		try {
			/* http://lists.jboss.org/pipermail/jboss-user/2007-March/047061.html */
			PortletInvocation invocation =
				(PortletInvocation) request.getAttribute(ContextDispatcherInterceptor.REQ_ATT_COMPONENT_INVOCATION);
			return invocation.getDispatchedRequest().getParameter(name);
			//AbstractPortletInvocationContext ctx = (AbstractPortletInvocationContext) invocation.getContext();
			//return ctx.getClientRequest().getParameter(name);
		} catch (Throwable e) {
			LogFactory.getLog(getClass()).warn("Can't access URL parameters", e);
		}
		return null;
	}
	
	private PortletRequest wrapRequest(PortletRequest request) {
		if (request instanceof JBossActionRequest || request instanceof JBossRenderRequest)
			return request;
		PortletInvocation invocation =
			(PortletInvocation) request.getAttribute(ContextDispatcherInterceptor.REQ_ATT_COMPONENT_INVOCATION);
		try {
			if (invocation instanceof RenderInvocation) {
				Constructor constr = JBossRenderRequest.class.getConstructor(new Class[] { RenderRequest.class });
				return (JBossRenderRequest) constr.newInstance(new Object[] { request });
				//return new JBossRenderRequest((RenderInvocation) invocation);
			}
			if (invocation instanceof ActionInvocation) {
				Constructor constr = JBossActionRequest.class.getConstructor(new Class[] { ActionRequest.class });
				return (JBossActionRequest) constr.newInstance(new Object[] { request });
				//return new JBossActionRequest((ActionInvocation) invocation);
			}
		} catch (Exception e) {
			logger.warn("Can't wrap request from " + invocation);
			return request;
		}
		logger.warn("Can't wrap request from " + invocation);
		return request;
	}

	/*private void printAttributes(PortletRequest request) {
		logger.info(request.getClass().getName() + ": Request attributes: ");
		for (Enumeration names = request.getAttributeNames(); names.hasMoreElements(); ) {
			String name = (String) names.nextElement();
			logger.info("    " + name + "=" + request.getAttribute(name));
		}
		logger.info(request.getClass().getName() + ": Request properties: ");
		for (Enumeration names = request.getPropertyNames(); names.hasMoreElements(); ) {
			String name = (String) names.nextElement();
			logger.info("    " + name + "=" + request.getProperty(name));
		}
		logger.info(request.getPortletSession().getClass().getName() + ": Session attributes: ");
		for (Enumeration names = request.getPortletSession().getAttributeNames(); names.hasMoreElements(); ) {
			String name = (String) names.nextElement();
			logger.info("    " + name + "=" + request.getPortletSession().getAttribute(name));
		}
		logger.info(request.getPortalContext().getClass().getName() + ": Portal context properties: ");
		for (Enumeration names = request.getPortalContext().getPropertyNames(); names.hasMoreElements(); ) {
			String name = (String) names.nextElement();
			logger.info("    " + name + "=" + request.getPortalContext().getProperty(name));
		}
	}*/
}
