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
package com.aplana.dbmi.importrolesportlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.aplana.dbmi.rolelistportlet.RoleListPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import com.aplana.dbmi.action.ImportObjects;
import com.aplana.dbmi.action.ImportRoles;
import com.aplana.dbmi.action.ParseImportFile;
import com.aplana.dbmi.action.ParseImportFile.TypeImportObject;
import com.aplana.dbmi.importbaseportlet.BaseImportPortlet;
import com.aplana.dbmi.model.ContextProvider;

public class RolesImportPortlet extends BaseImportPortlet {
	protected static Log logger = LogFactory.getLog(RolesImportPortlet.class);
	
	public static final String JSP_FOLDER = "/_ImportRolesPortlet/jsp/"; // JSP folder name
    public static final String VIEW_JSP = "ImportRoles"; // JSP file name to be rendered on the view mode
    
	public static final String SESSION_BEAN = "roleImportPortletSessionBean";
	private static final String objectName = ContextProvider.getContext().getLocaleMessage("role.import.portlet.name");
	private static final String resourceBundle = "com.aplana.dbmi.importrolesportlet.nl.RoleImportPortlet";
	private static final String msgResultImport = "show.import.result.role";
	
	public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
    }

	@Override
	public String getKeySessionBean() {
		return SESSION_BEAN;
	}

	@Override
	public String getImportObjectName() {
		return objectName;
	}

	@Override
	public String getResourceBundle() {
		return resourceBundle;
	}

	@Override
	public String getJspView() {
		return VIEW_JSP;
	}

	@Override
	public String getJspFolder() {
		return JSP_FOLDER;
	}

	@Override
	public TypeImportObject getTypeImportObject() {
		return ParseImportFile.TypeImportObject.system_role;
	}

	@Override
	public ImportObjects getImportObject() {
		ImportRoles importAction = new ImportRoles();
		return importAction;
	}

	@Override
	public String getMsgParamName() {
		return RoleListPortlet.MSG_PARAM_NAME;
	}
	
	@Override
	public String getMsgResultImport() {
		return msgResultImport;
	}
}
