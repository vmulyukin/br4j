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
package com.aplana.dbmi.admin;

import java.io.*;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import javax.portlet.*;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortletService;
import com.aplana.dbmi.action.ListFavorites;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataServiceBean;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.PortletSession;
import com.aplana.dbmi.task.TaskInfoBuilder;


/**
 * A sample portlet based on GenericPortlet
 */
public class TaskControlPortlet extends GenericPortlet {

	public static final String JSP_FOLDER    = "/WEB-INF/jsp/";    // JSP folder name

	public static final String ERROR_JSP     = "TaskControlPortletError";        // JSP file name to be rendered on the view mode
	public static final String VIEW_JSP      = "TaskControlPortletView";         // JSP file name to be rendered on the view mode
	public static final String SESSION_BEAN  = "TaskControlPortletSessionBean";  // Bean name for the portlet session

	public static final String EDIT_ACCESS_ROLES = "editAccessRoles";
	public static final String PARAM_ACTION  = "action";
	public static final String PARAM_PARAMS_DELETE  = "paramsDelete";
	public static final String ACTION_CREATE = "create";
	public static final String ACTION_CANCEL = "cancel";
	public static final String PARAM_ID      = "id";
	public static final String PARAM_NAME    = "name";
	public static final String PARAM_INTERVAL= "interval";
	public static final String PARAM_UNIT    = "unit";
	public static final String PARAM_START_DAY  = "startd";
	public static final String PARAM_START_HOUR = "starth";
	public static final String PARAM_START_MIN  = "startm";
	public static final String PARAM_CRON_EXPR  = "cron_expr";
	public static final String PARAM_TASK_TYPE  = "task_type";
	public static final String PARAM_TASK_INFO  = "task_info";
	
	public static final String INTERVAL_TASK  = "interval_task";
	public static final String CRON_TASK  = "cron_task";

	/**
	 * @see javax.portlet.Portlet#init()
	 */
	public void init() throws PortletException{
		super.init();
	}

	private void initBean(PortletRequest request, PortletResponse response) {
		TaskControlPortletSessionBean sessionBean = getSessionBean(request);
		final PortletService psrvc = Portal.getFactory().getPortletService();
		String editAccessRoles = psrvc.getPageProperty(EDIT_ACCESS_ROLES, request, response);
		if (editAccessRoles != null&&!editAccessRoles.isEmpty()){
			sessionBean.setEditAccessRoles(editAccessRoles);
		}
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
		TaskControlPortletSessionBean sessionBean = getSessionBean(request);
		if( sessionBean==null ) {
			response.getWriter().println("<b>NO PORTLET SESSION YET</b>");
			return;
		}
		initBean(request, response);
		
		if (sessionBean.getError() != null) {
			PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(getJspFilePath(request, ERROR_JSP));
			rd.include(request,response);
			sessionBean.clearError();
		}

		// Invoke the JSP to render
		PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(getJspFilePath(request, VIEW_JSP));
		rd.include(request,response);
	}

	/**
	 * Process an action request.
	 * 
	 * @see javax.portlet.Portlet#processAction(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
	 */
	public void processAction(ActionRequest request, ActionResponse response) throws PortletException, java.io.IOException
	{
		String action = (String) request.getParameter(PARAM_ACTION);
		TaskControlPortletSessionBean sessionBean = getSessionBean(request);
		// ��� ����� �������� ���������� id ��������� ��������� � ������� ����������� ���������������� ���������  
		sessionBean.setActiveTaskId(null);
		sessionBean.setShowWarningMessage(false);

		if (ACTION_CREATE.equals(action)) {
			
			String taskType = request.getParameter(PARAM_TASK_TYPE);
			if (taskType == null || taskType.length() == 0) {
				sessionBean.setError("Task type not set");
				return;
			}
			
			String name = request.getParameter(PARAM_NAME);
			if (name == null || name.length() == 0) {
				sessionBean.setError("Task name not set");
				return;
			}
			
			String day = request.getParameter(PARAM_START_DAY);
			if (day == null || day.length() == 0) {
				sessionBean.setError("Start day not set");
				return;
			}
			String hour = request.getParameter(PARAM_START_HOUR);
			if (hour == null || hour.length() == 0) {
				sessionBean.setError("Start hour not set");
				return;
			}
			String min = request.getParameter(PARAM_START_MIN);
			if (min == null || min.length() == 0) {
				sessionBean.setError("Start minute not set");
				return;
			}
			String info = request.getParameter(PARAM_TASK_INFO);
			
			Calendar now = Calendar.getInstance();
			Calendar start = Calendar.getInstance();
			try {
				start.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
				if (now.after(start))
					start.add(Calendar.MONTH, 1);
				start.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
				if (now.after(start))
					start.add(Calendar.DATE, 1);
				start.set(Calendar.MINUTE, Integer.parseInt(min));
				if (now.after(start))
					start.add(Calendar.HOUR_OF_DAY, 1);
				start.clear(Calendar.SECOND);
			} catch (NumberFormatException e) {
				sessionBean.setError(e.getMessage());
				return;
			}
			
			if (INTERVAL_TASK.equals(taskType))
			{
			
				String interval = request.getParameter(PARAM_INTERVAL);
				if (interval == null || interval.length() == 0) {
					sessionBean.setError("Repeat interval not set");
					return;
				}
				String unit = request.getParameter(PARAM_UNIT);
				if (unit == null || unit.length() == 0) {
					sessionBean.setError("Repeat interval unit not set");
					return;
				}
				
				sessionBean.startTask(
						TaskInfoBuilder.newTaskInfo()
							.forJob(name)
							.withSchedule(Integer.valueOf(interval), unit)
							.withInfo(info)
							.startAt(start.getTime()).build());
			}
			else if (CRON_TASK.equals(taskType))
			{
				String cronExpr = request.getParameter(PARAM_CRON_EXPR);
				if (cronExpr == null || cronExpr.length() == 0) {
					sessionBean.setError("Cron expression not set");
					return;
				}
				sessionBean.startTask(
						TaskInfoBuilder.newTaskInfo()
							.forJob(name)
							.withSchedule(cronExpr)
							.withInfo(info)
							.startAt(start.getTime()).build());
			}
			else {
				sessionBean.setError("Unknown task type: " + taskType);
				return;
			}

		} else if (ACTION_CANCEL.equals(action)) {
			String id = request.getParameter(PARAM_ID);
			if (id == null || id.length() == 0) {
				sessionBean.setError("Task id not set");
				return;
			}
			String isParamsDelete = (String) request.getParameter(PARAM_PARAMS_DELETE);
			/* ���� �� ���� ��������� ������� �������� ��� ������� ���������� ���������� ���������, 
			 * �� ������������ �������� ��������� � �������������� ���������� 
			 */
			if (isParamsDelete!=null&&!isParamsDelete.isEmpty()){
				sessionBean.cancelTask(id, Boolean.parseBoolean(isParamsDelete));
			} else {
				// ����� �������� ������� ���������� ���������� � ���������� ���������
				if (sessionBean.isTaskParamsExists(id)){
				/* ���� ��� ����, �� ������������ ����� ��������������
				 * � ���� �� ����� ������� ������������ ��� ����� ��������� (�������� ����������),
				 * �������� ������� ������������ (� ���������� ����) ������� ������� ������� �� ��������    
				 */
					sessionBean.setActiveTaskId(id);
					sessionBean.setShowWarningMessage(true);
				} else
					sessionBean.cancelTask(id);
			}
		} else {
			sessionBean.setError("Unknown action: " + action);
		}
	}

	/**
	 * Get SessionBean.
	 * 
	 * @param request PortletRequest
	 * @return TaskControlPortletSessionBean
	 */
	private static TaskControlPortletSessionBean getSessionBean(PortletRequest request) {
		PortletSession session = request.getPortletSession();
		if( session == null )
			return null;
		TaskControlPortletSessionBean sessionBean = (TaskControlPortletSessionBean)session.getAttribute(SESSION_BEAN);
		if( sessionBean == null ) {
			sessionBean = new TaskControlPortletSessionBean();
			session.setAttribute(SESSION_BEAN,sessionBean);
			sessionBean.getServiceBean(request);
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
