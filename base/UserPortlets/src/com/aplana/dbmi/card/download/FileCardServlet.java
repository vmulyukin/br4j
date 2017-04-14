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
package com.aplana.dbmi.card.download;

import java.io.IOException;
import java.security.Principal;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.aplana.dbmi.ajax.AbstractDBMIAjaxServlet;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.download.actionhandler.FileActionHandler;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataService;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.DataServiceHome;
import com.aplana.dbmi.service.SystemUser;
import com.aplana.dbmi.service.UserPrincipal;

/**
 * class dispatch the download and upload card from/in to file
 */
public class FileCardServlet extends AbstractDBMIAjaxServlet {
    public final static String PARAM_ACTION_ID = "actionid";
    public static final String PARAM_NAMESPACE = "namespace";
    public static final String SESSION_BEAN = "FileCardServlet";

    @Override
    protected void generateResponse(HttpServletRequest request,
	    HttpServletResponse response) throws ServletException, IOException {

		FileActionHandler handler;
		String actionId = null;
		try {
			actionId = getActionId(request);
			String namespace;
			namespace = getNamespace(request);
			FileActionManager actionManager;
			actionManager = getFileActionManager(request, namespace);
			handler = actionManager.createInstance(actionId);
		} catch (DataException ex) {
			logger.error("Unable to create handler to process action " + actionId, ex);
			writeError(response, ex.getMessage(),
				HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		try {
			handler.process(request, response);
		} catch (DataException ex) {
			logger.error("Processing failed due to " + ex.getMessage(), ex);
			writeError(response, ex.getMessage(),
				HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
    }


    /**
     * write error message in to response
     *
     * @param response
     *                HttpServletResponse
     * @param error
     *                message
     * @param errorType
     *                type according http protocol or custom error code
     */
    protected void writeError(HttpServletResponse response, String error,
	    int errorType) {
	try {
	    response.sendError(errorType, error);
	} catch (IOException e) {
	    logger.error("Error sending error message", e);
	}
    }

    /**
     * try to extract ActionId from request parameter
     *
     * @param request
     *                HttpServletRequest
     * @return action id
     * @throws DataException
     *                 if action id null
     */
    protected String getActionId(HttpServletRequest request)
	    throws DataException {
	String actionId = request.getParameter(PARAM_ACTION_ID);
	if (actionId == null) {
	    throw new DataException(
		    "jbr.dmsi.fileCardServlet.invalidParameter", new Object[] {
			    PARAM_ACTION_ID, actionId });
	}
	return actionId;
    }

    /**
     * try to extract Namespace from request parameter
     *
     * @param request
     *                HttpServletRequest
     * @return action id
     * @throws DataException
     *                 if Namespace null
     */
    protected String getNamespace(HttpServletRequest request)
	    throws DataException {
	String actionId = request.getParameter(PARAM_NAMESPACE);
	if (actionId == null) {
	    throw new DataException(
		    "jbr.dmsi.fileCardServlet.invalidParameter", new Object[] {
			    PARAM_NAMESPACE, actionId });
	}
	return actionId;
    }

    /**
     * try to extract FileActionManager from session or create FileActionManager
     * and put them in to the session
     *
     * @param request
     *                HttpServletRequest
     * @param namespace
     *                of the stored FileActionManager
     * @return FileActionManager
     * @throws DataException
     *                 if fail create FileActionManager
     */
    protected FileActionManager getFileActionManager(
	    HttpServletRequest request, String namespace) throws DataException {
	FileActionManager result;
	HttpSession session = request.getSession();
	result = (FileActionManager) session
		.getAttribute(getSessionBeanAttrNameForServlet(namespace));

	if (result == null) {
	    result = initFileActionManager(request, namespace);
	}

	return result;
    }

    /**
     * try to instantiate FileActionManager and init his properties
     *
     * @param request
     *                HttpServletRequest
     * @param namespace
     *                of the stored FileActionManager
     * @return FileActionManager
     * @throws DataException
     *                 if fail initDataServiceBean
     */
    protected FileActionManager initFileActionManager(
	    HttpServletRequest request, String namespace) throws DataException {

	FileActionManager am = FileActionManager
		.getInstance(getDataServiceBean(request));
	saveSessionBeanForServlet(request, namespace, am);
	return am;

    }

    private static String getSessionBeanAttrNameForServlet(String namespace) {
	return SESSION_BEAN + '.' + namespace;
    }

    private void saveSessionBeanForServlet(HttpServletRequest request,
	    String namespace, FileActionManager fam) {
	HttpSession session = request.getSession();
	session.setAttribute(getSessionBeanAttrNameForServlet(namespace), fam);
    }

}
