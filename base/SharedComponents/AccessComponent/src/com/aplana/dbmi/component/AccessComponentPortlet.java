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
package com.aplana.dbmi.component;

import java.io.*;
import java.util.*;

import javax.portlet.*;

import com.aplana.dbmi.model.AccessListItem;
import com.aplana.dbmi.service.DataServiceBean;


/**
 * A sample portlet based on GenericPortlet
 */
public class AccessComponentPortlet extends GenericPortlet {

	public static final String JSP_FOLDER    = "/_AccessComponent/jsp/";    // JSP folder name

	public static final String VIEW_JSP      = "AccessComponentPortletView";         // JSP file name to be rendered on the view mode
	public static final String SESSION_BEAN  = "AccessComponentPortletSessionBean";  // Bean name for the portlet session
	public static final String FORM_SUBMIT   = "AccessComponentPortletFormSubmit";   // Action name for submit form
	public static final String FORM_TEXT     = "AccessComponentPortletFormText";     // Parameter name for the text input

	public static final String FORM_NAME     = "AccessComponentPortletForm";     // Parameter name for the text input

	/**
	 * @see javax.portlet.Portlet#init()
	 */
	public void init() throws PortletException{
		super.init();
	}

	/**
	 * Serve up the <code>view</code> mode.
	 * 
	 * @see javax.portlet.GenericPortlet#doView(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
	 */
	public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		// Set the MIME type for the render response
		response.setContentType(request.getResponseContentType());

		// Check if portlet session exists
		AccessComponentPortletSessionBean sessionBean = getSessionBean(request);
		if( sessionBean==null ) {
			response.getWriter().println("<b>NO PORTLET SESSION YET</b>");
			return;
		}

		// Invoke the JSP to render
		
		String jspPath = getJspFilePath(request, VIEW_JSP);
		if (getAccessComponent(request).isAccessHandlerAction()) {
			jspPath = getJspFilePath(request, AccessComponent.SELECTED_LIST_JSP);
		}
		PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(jspPath);
		rd.include(request,response);
	}


	private AccessComponent getAccessComponent(PortletRequest request) {
		PortletSession session = request.getPortletSession();
		if( session == null )
			return null;
		AccessComponent component = (AccessComponent)session.getAttribute(AccessComponent.ACCESS_HANDLER);
		if (component == null) {
			AccessComponentPortletSessionBean sessionBean = getSessionBean(request);
			DataServiceBean serviceBean = sessionBean.getServiceBean(request);
			component = new AccessComponent(serviceBean, new ArrayList(), FORM_NAME);
			session.setAttribute(AccessComponent.ACCESS_HANDLER ,component);
			
		}
		return component;
	}
	
	
	/**
	 * Process an action request.
	 * 
	 * @see javax.portlet.Portlet#processAction(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
	 */
	public void processAction(ActionRequest request, ActionResponse response) throws PortletException, java.io.IOException {
		if( request.getParameter(FORM_SUBMIT) != null ) {
			// Set form text in the session bean
			AccessComponentPortletSessionBean sessionBean = getSessionBean(request);
			if( sessionBean != null )
				sessionBean.setFormText(request.getParameter(FORM_TEXT));
		}
		
		// AccessComponent
		getAccessComponent(request).parseRequest(request, response);
		if (request.getParameter(AccessComponent.STORE_ACTION) != null ) {
			// Get Access Item List
			List access = getAccessComponent(request).getAccessItemList();
			for (Iterator iter = access.iterator(); iter.hasNext();) {
				AccessListItem element = (AccessListItem) iter.next();
			}
		}
		// AccessComponent
		
	}

	/**
	 * Get SessionBean.
	 * 
	 * @param request PortletRequest
	 * @return AccessComponentPortletSessionBean
	 */
	private static AccessComponentPortletSessionBean getSessionBean(PortletRequest request) {
		PortletSession session = request.getPortletSession();
		if( session == null )
			return null;
		AccessComponentPortletSessionBean sessionBean = (AccessComponentPortletSessionBean)session.getAttribute(SESSION_BEAN);
		if( sessionBean == null ) {
			sessionBean = new AccessComponentPortletSessionBean();
			session.setAttribute(SESSION_BEAN,sessionBean);
		}
		return sessionBean;
	}

	/**
	 * Returns JSP file path.
	 * 
	 * @param request Render request
	 * @param jspFile JSP file name
	 * @return JSP file path
	 */
	private static String getJspFilePath(RenderRequest request, String jspFile) {
		String markup = request.getProperty("wps.markup");
		if( markup == null )
			markup = getMarkup(request.getResponseContentType());
		return JSP_FOLDER + markup + "/" + jspFile + "." + getJspExtension(markup);
	}

	/**
	 * Convert MIME type to markup name.
	 * 
	 * @param contentType MIME type
	 * @return Markup name
	 */
	private static String getMarkup(String contentType) {
		if( "text/vnd.wap.wml".equals(contentType) )
			return "wml";
        else
            return "html";
	}

	/**
	 * Returns the file extension for the JSP file
	 * 
	 * @param markupName Markup name
	 * @return JSP extension
	 */
	private static String getJspExtension(String markupName) {
		return "jsp";
	}

}
