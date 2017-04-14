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
package com.aplana.dbmi.ajax;

import javax.servlet.http.HttpServletRequest;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.showlist.MIShowListPortletSessionBean.GroupExecutionMode;
import com.aplana.dbmi.support.action.GetGroupExecutionReports;

public class GroupExecutionCardServlet extends DataGridAbstractServlet {
	// inherit: logger = LogFactory.getLog(CardHierarchyServlet.class);
	private static final long serialVersionUID = 1L;
	private static final String PARAM_EXECUTION_MODE = "mode";

	protected Action getAction(HttpServletRequest request){
		GetGroupExecutionReports action = new GetGroupExecutionReports();
		
		if (request.getParameter(PARAM_EXECUTION_MODE) != null) {
			action.setAsistantMode(GroupExecutionMode.ASISTENT.equals(GroupExecutionMode.valueOf(request.getParameter(PARAM_EXECUTION_MODE))));
		}
		return action;
	}
}