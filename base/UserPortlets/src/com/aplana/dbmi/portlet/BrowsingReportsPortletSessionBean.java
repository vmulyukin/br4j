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
package com.aplana.dbmi.portlet;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.service.DataServiceBean;

public class BrowsingReportsPortletSessionBean {

	private String jsonReports;
	private String exportType;
	private Search employeesSearch;
    private String header;
    private String switchNavigatorLink;
    private DataServiceBean serviceBean;

	public String getJsonReports() {
		return jsonReports;
	}

	public void setJsonReports(String jsonReports) {
		this.jsonReports = jsonReports;
	}

	public String getExportType() {
		return exportType;
	}

	public void setExportType(String exportType) {
		this.exportType = exportType;
	}

	public Search getEmployeesSearch() {
		return employeesSearch;
	}

	public void setEmployeesSearch(Search employeesSearch) {
		this.employeesSearch = employeesSearch;
	}

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getSwitchNavigatorLink() {
        return switchNavigatorLink;
    }

    public void setSwitchNavigatorLink(String switchNavigatorLink) {
        this.switchNavigatorLink = switchNavigatorLink;
    }

    public DataServiceBean getServiceBean() {
        return serviceBean;
    }

    public void setServiceBean(DataServiceBean serviceBean) {
        this.serviceBean = serviceBean;
    }
}
