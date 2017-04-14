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
package com.aplana.dbmi.universalportlet;

import java.util.HashMap;
import java.util.List;

import com.aplana.dbmi.action.CheckRolesForUser;
import com.aplana.dbmi.service.DataServiceBean;

import javax.servlet.http.HttpServletRequest;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServletUtil;
import com.aplana.dbmi.service.UserPrincipal;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

public class UniversalPortletSessionBean {

    private boolean externalParameters = false;

    private String title;
    
    private String customImportTitle=null;

    private int pageSize;

    private List<List<String>> data;

    private List<ColumnDescription> columnsMetaData;

    private List<ParameterDescription> parametersMetaData;

    private MapSqlParameterSource parametersValues;
    
    private String[] initialParameters;

    private boolean printMode;
    
    //represents the ability to export data to excel file
    private boolean canBeExportedToExcel = true;
    
	// ���� ����������� ������� �����/����� �� Excel
	private boolean canImport=false;
    
    //represents the ability to print data
    private boolean canBePrinted = true;
    
    private String downloadImportTemplate; // ������ ��� ������� ��������
    
    private boolean showRefreshButton;
    
    //true - don't have to submit form, submits when portlet is loaded
    //without parameters
    private boolean submitOnLoad;
    
    private UniversalSearchFilter filter;
    
	private String createAccessRoles;
	private boolean showCreate;

    /**
     * Map from language to QueryDescription.
     */
    private HashMap<String, QueryDescription> queryDescriptions;

    /**
     * Map from language to TableDescription.
     */
    private HashMap<String, TableDescription> tableDescriptions;

	private DataServiceBean serviceBean = null;
	private String editAccessRoles;
	
	public DataServiceBean getServiceBean(PortletRequest request) {
		if (serviceBean == null) {
			initDataServiceBean(request);
		} else {
    		String userName = (String) request.getPortletSession().getAttribute(DataServiceBean.USER_NAME, PortletSession.APPLICATION_SCOPE);
            if (userName != null) {
                serviceBean.setUser(new UserPrincipal(userName));
                serviceBean.setIsDelegation(true);
                serviceBean.setRealUser(request.getUserPrincipal());
            } else {
                serviceBean.setUser(request.getUserPrincipal());
                serviceBean.setIsDelegation(false);
            }
		}
		return serviceBean;
	}
	
	public DataServiceBean getServiceBean(HttpServletRequest request, String namespace) {
		if (serviceBean == null) {
			initDataServiceBean(request);
		} else {
    		String userName = (String) request.getSession().getAttribute(DataServiceBean.USER_NAME);
            if (userName != null) {
                serviceBean.setUser(new UserPrincipal(userName));
                serviceBean.setIsDelegation(true);
                serviceBean.setRealUser(request.getUserPrincipal());
            } else {
                serviceBean.setUser(request.getUserPrincipal());
                serviceBean.setIsDelegation(false);
            }
		}
		return serviceBean;
	}
	
	private void initDataServiceBean(PortletRequest request) {
		this.serviceBean = PortletUtil.createService(request);
	}
	
	private void initDataServiceBean(HttpServletRequest request) {
		this.serviceBean = ServletUtil.createService(request);
	}
	
	public boolean isExternalParameters() {
        return externalParameters;
    }

    public void setExternalParameters(boolean externalParameters) {
        this.externalParameters = externalParameters;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getCustomImportTitle() {
		return customImportTitle;
	}

	public void setCustomImportTitle(String customImportTitle) {
		this.customImportTitle = customImportTitle;
	}

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<List<String>> getData() {
        return data;
    }

    public void setData(List<List<String>> data) {
        this.data = data;
    }

    public List<ColumnDescription> getColumnsMetaData() {
        return columnsMetaData;
    }

    public void setColumnsMetaData(List<ColumnDescription> metaData) {
        this.columnsMetaData = metaData;
    }

    public List<ParameterDescription> getParametersMetaData() {
        return parametersMetaData;
    }

    public void setParametersMetaData(List<ParameterDescription> parametersMetaData) {
        this.parametersMetaData = parametersMetaData;
    }

    public MapSqlParameterSource getParametersValues() {
        return parametersValues;
    }

    public void setParametersValues(MapSqlParameterSource parametersValues) {
        this.parametersValues = parametersValues;
    }

    public String[] getInitialParameters() {
        return initialParameters;
    }

    public void setInitialParameters(String[] initialParameters) {
        this.initialParameters = initialParameters;
	}

	public boolean isPrintMode() {
        return printMode;
    }

    public void setPrintMode(boolean printMode) {
        this.printMode = printMode;
    }

    public HashMap<String, QueryDescription> getQueryDescriptions() {
        return queryDescriptions;
    }

    public void setQueryDescriptions(HashMap<String, QueryDescription> queryDescriptions) {
        this.queryDescriptions = queryDescriptions;
    }

    public HashMap<String, TableDescription> getTableDescriptions() {
        return tableDescriptions;
    }

    public void setTableDescriptions(HashMap<String, TableDescription> tableDescriptions) {
        this.tableDescriptions = tableDescriptions;
    }

	public boolean isSubmitOnLoad() {
		return submitOnLoad;
	}

	public void setSubmitOnLoad(boolean submitOnLoad) {
		this.submitOnLoad = submitOnLoad;
	}

	public UniversalSearchFilter getFilter() {
		return filter;
	}

	public void setFilter(UniversalSearchFilter filter) {
		this.filter = filter;
	}

	public boolean isCanBePrinted() {
		return canBePrinted;
	}

	public void setCanBePrinted(boolean canBePrinted) {
		this.canBePrinted = canBePrinted;
	}

	public boolean isCanBeExportedToExcel() {
		return canBeExportedToExcel;
	}

	public void setCanBeExportedToExcel(boolean canBeExportedToExcel) {
		this.canBeExportedToExcel = canBeExportedToExcel;
	}
	
	public boolean isCanImport() {
		return canImport;
	}
	
	public void setCanImport(boolean canImport) {
		this.canImport = canImport;
	}

	public String getDownloadImportTemplate() {
		return downloadImportTemplate;
	}

	public void setDownloadImportTemplate(String downloadImportTemplate) {
		this.downloadImportTemplate = downloadImportTemplate;
	}

	public boolean isShowRefreshButton() {
		return showRefreshButton;
	}

	public void setShowRefreshButton(boolean showRefreshButton) {
		this.showRefreshButton = showRefreshButton;
	}

	public String getEditAccessRoles() {
		return editAccessRoles;
	}

	public void setEditAccessRoles(String editAccessRoles) {
		this.editAccessRoles = editAccessRoles;
	}

	/**
	 * ��������� ������� � �������� ������������ ���� �� �������������� �������� � ��������.
	 * ����� ���������� ���������� ���������� � ������ ������ � ����� ��� ����, 
	 * ����� ��� ���������/������������ ���� � ������ ������ ���� ��� ����� ������ ���������/��������� ��� ������������ 
	 * @return true - ����� ����, false - ���� ���
	 */
	public boolean isEditAccessExists() throws DataException, ServiceException{
		if (editAccessRoles != null&&!editAccessRoles.isEmpty()){
			CheckRolesForUser checkAction = new CheckRolesForUser();
			checkAction.setPersonLogin(serviceBean.getUserName());
			checkAction.setRoles(editAccessRoles);
			return (Boolean)serviceBean.doAction(checkAction);
		}
		// ���� ��������������� ���� �� ������, �� ����� �� �������������� � �������� ������������ ����
		return true;
	}
	
	public String getCreateAccessRoles() {
		return createAccessRoles;
	}

	public void setCreateAccessRoles(String createAccessRoles) {
		this.createAccessRoles = createAccessRoles;
	}
	
	/**
	 * ��������� ������� � �������� ������������ ���� �� �������� �������� � ��������.
	 * ����� ���������� ���������� ���������� � ������ ������ � ����� ��� ����, 
	 * ����� ��� ���������/������������ ���� � ������ ������ ���� ��� ����� ������ ���������/��������� ��� ������������ 
	 * @return true - ����� ����, false - ���� ���
	 */
	public boolean isCreateAccessExists() throws DataException, ServiceException{
		if (createAccessRoles != null&&!createAccessRoles.isEmpty()){
			CheckRolesForUser checkAction = new CheckRolesForUser();
			checkAction.setPersonLogin(serviceBean.getUserName());
			checkAction.setRoles(createAccessRoles);
			return (Boolean)serviceBean.doAction(checkAction);
		}
		// ���� ��������������� ���� �� ������, �� ����� �� �������� � �������� ������������ ����
		return true;
	}

	public boolean isShowCreate() {
		return showCreate;
	}

	public void setShowCreate(boolean showCreate) {
		this.showCreate = showCreate;
	}
}
