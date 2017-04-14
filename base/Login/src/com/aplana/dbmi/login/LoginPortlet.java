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
package com.aplana.dbmi.login;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.jboss.web.tomcat.security.login.WebAuthentication;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortletService;
import com.aplana.dbmi.action.Login;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;

//import com.aplana.dbmi.service.SystemUser;

/**
 * Login portlet
 */
public class LoginPortlet extends javax.portlet.GenericPortlet {

    private static final String DEFAULT_DEFAULT_PAGE_ID = "dbmi.defaultPage";
    private static final String DEFAULT_PAGE_ID_PREFIX = "dbmi.defaultPage.";
    private static final String AFFECTED_PORTAL =  "affectedPortal";
    public static final String BOSS_PORTAL =  "boss";
    
    public static final String DEFAULT_ARM_PAGE_ID = "dbmi.armPage";

    public static final String JSP_FOLDER = "/_Login/jsp/"; // JSP folder name

    public static final String LOGIN_JSP = "LoginView.jsp"; // JSP file name to
                                                            // be rendered on
                                                            // the view mode
    public static final String LOGGED_IN_JSP = "LoggedInView.jsp"; // JSP file
                                                                    // name to
                                                                    // be
                                                                    // rendered
                                                                    // on the
                                                                    // view mode

    public static final String HELP_JSP = "LoginHelp.jsp"; // JSP file name to
                                                            // be rendered on
                                                            // the help mode

    public static final String LOGIN_FIELD_NAME = "LoginField"; // Parameter
                                                                // login for the
                                                                // text input
    public static final String PSW_FIELD_NAME = "PasswordField"; // Parameter
                                                                    // pswd for
                                                                    // the text
                                                                    // input

    // public static final String PUMA_REQUEST_ATTR_NAME =
    // "com.ibm.portal.puma.request-user";
    // public static final String AUTH_PORTLET_SERVICE_NAME =
    // "portletservice/com.ibm.wps.portletservice.authentication.AuthenticationPortletService";

    public static final String AUTH_ERROR_ATTR_NAME = "LoginAuthError";

    public static final String SESSION_BEAN = "LoginPortletSessionBean"; // Bean
                                                                            // name
                                                                            // for
                                                                            // the
                                                                            // portlet
                                                                            // session

    public static final String FORM_ACTION_FIELD_NAME = "FormAction"; // Bean
                                                                        // name
                                                                        // for
                                                                        // the
                                                                        // portlet
                                                                        // session
    public static final String CHANGE_LANG = "ChangeLanguage"; // Bean name for
                                                                // the portlet
                                                                // session

    private PortletService portletService;

    /**
     * @see javax.portlet.Portlet#init()
     */
    public void init() throws PortletException {
        super.init();
        portletService = Portal.getFactory().getPortletService();
    }

    /**
     * Serve up the <code>help</code> mode.
     *
     * @see javax.portlet.GenericPortlet#doHelp(javax.portlet.RenderRequest,
     *      javax.portlet.RenderResponse)
     */
    public void doHelp(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        // Set the MIME type for the render response
        response.setContentType(request.getResponseContentType());

        // Check if portlet session exists
        LoginPortletSessionBean sessionBean = getSessionBean(request);
        if (sessionBean == null) {
            response.getWriter().println("<b>NO PORTLET SESSION YET</b>");
            return;
        }

        // Invoke the JSP to render
        PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(JSP_FOLDER + HELP_JSP);
        rd.include(request, response);
    }

    /**
     * Serve up the <code>view</code> mode.
     *
     * @see javax.portlet.GenericPortlet#doView(javax.portlet.RenderRequest,
     *      javax.portlet.RenderResponse)
     */
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        // Set the MIME type for the render response
        response.setContentType(request.getResponseContentType());

        // Check if portlet session exists
        LoginPortletSessionBean sessionBean = getSessionBean(request);
        if (sessionBean == null) {
            response.getWriter().println("<b>NO PORTLET SESSION YET</b>");
            return;
        }

        // Invoke the JSP to render
        PortletRequestDispatcher rd;
        // if (request.getAttribute(PUMA_REQUEST_ATTR_NAME) == null) {
        if (request.getUserPrincipal() == null) {
        	sessionBean.setAffectedPortal(portletService.getUrlParameter(request,AFFECTED_PORTAL));
            rd = getPortletContext().getRequestDispatcher(JSP_FOLDER + LOGIN_JSP);
        } else {
            rd = getPortletContext().getRequestDispatcher(JSP_FOLDER + LOGGED_IN_JSP);
        }
        rd.include(request, response);
    }

    /**
     * Process an action request.
     *
     * @see javax.portlet.Portlet#processAction(javax.portlet.ActionRequest,
     *      javax.portlet.ActionResponse)
     */
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException, java.io.IOException {
        if (request.getParameter(FORM_ACTION_FIELD_NAME) != null) {
            String actionName = request.getParameter(FORM_ACTION_FIELD_NAME);
            if (actionName.equalsIgnoreCase(CHANGE_LANG)) {
                LoginPortletSessionBean sessionBean = getSessionBean(request);
                sessionBean.setEnglishLang(!sessionBean.isEnglishLang());
            }
        } else {
            final String login = request.getParameter(LOGIN_FIELD_NAME);
            final String password = request.getParameter(PSW_FIELD_NAME);
            if (login != null && password != null) {
                try {
                    WebAuthentication webAuthentication = new WebAuthentication();
                    webAuthentication.login(login, password);

                    Principal userPrincipal1 = request.getUserPrincipal();
                    System.out.println("LoginPortlet: request.userPrincipal1=" + userPrincipal1);

                } catch (Exception e) {
                    e.printStackTrace(System.out);
                    response.setRenderParameter(AUTH_ERROR_ATTR_NAME, AUTH_ERROR_ATTR_NAME);
                }
                Principal user = request.getUserPrincipal();
                if (user != null) {
                    try {
                        HashMap map = new HashMap();
                        map.put("logged", "true");
                        DataServiceBean service = PortletUtil.createService(request);
                        user = request.getUserPrincipal();
                        /*
                         * if (user == null) { user = new SystemUser(); }
                         */
                        service.setUser(user);
                        service.doAction(new Login());

                        String defaultPageId = DEFAULT_DEFAULT_PAGE_ID;
                        
                        /*
                         * DSultanbekov: ���� �� ����� ��� ���� ����������� ��������� 
                         * ������ ��������� �������� ��� ����������� �� ������ �������
                         * ����� ���� ��� ����� �� ������� � �������� ������� ���� ��������
                         * ����� ����� ���������� (�� ��� ������� ������)
                        Person currentUser = (Person) service.getById(Person.ID_CURRENT);
                        ReferenceValue department = currentUser.getDepartment();

                        if (department != null) {
                            defaultPageId = DEFAULT_PAGE_ID_PREFIX + department.getId().getId();
                        }
                        */

                        // request.setAttribute(PUMA_REQUEST_ATTR_NAME, user);
                        // response.setRenderParameter(PUMA_REQUEST_ATTR_NAME,
                        // user.getName());
                        LoginPortletSessionBean sessionBean = getSessionBean(request);
                        if(sessionBean.getAffectedPortal()!=null && sessionBean.getAffectedPortal().equals(BOSS_PORTAL)){
                        	response.sendRedirect(portletService.generateLink(DEFAULT_ARM_PAGE_ID, null, map, request, response));
                        } else {
                            response.sendRedirect(portletService.generateLink(defaultPageId, null, map, request, response));                        	
                        }

                    } catch (Exception e) {
                        e.printStackTrace(System.out);
                        response.setRenderParameter(AUTH_ERROR_ATTR_NAME, AUTH_ERROR_ATTR_NAME);
                    }
                } else {
                    response.setRenderParameter(AUTH_ERROR_ATTR_NAME, AUTH_ERROR_ATTR_NAME);
                }
            }
        }
    }

    /**
     * Get SessionBean.
     *
     * @param request
     *                PortletRequest
     * @return LoginPortletSessionBean
     */
    private static LoginPortletSessionBean getSessionBean(PortletRequest request) {
        PortletSession session = request.getPortletSession();
        if (session == null)
            return null;
        LoginPortletSessionBean sessionBean = (LoginPortletSessionBean) session.getAttribute(SESSION_BEAN);
        if (sessionBean == null) {
            sessionBean = new LoginPortletSessionBean();
            session.setAttribute(SESSION_BEAN, sessionBean);
        }
        return sessionBean;
    }

}
