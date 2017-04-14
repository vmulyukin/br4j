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

import java.util.LinkedList;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServletUtil;
import com.aplana.dbmi.service.SystemUser;
import com.aplana.dbmi.service.UserPrincipal;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

public class CardImportPortletSessionBean {
	protected AsyncDataServiceBean serviceBean;
	private ResourceBundle resourceBundle = null;
	protected String message = null; 
	protected String backUrl;
	protected String objectName=null;
	protected Template template;
	protected boolean checkForExistsDoublets = true;
	protected boolean updateExistsDoublets = false;
	protected boolean supportUpdateExistsDoublets = true;
	protected boolean supportCheckForExistsDoublets = true;
	
	public AsyncDataServiceBean getServiceBean() {
		return serviceBean;
	}
	
	public void setServiceBean(AsyncDataServiceBean serviceBean) {
		this.serviceBean = serviceBean;
	}

	public String getBackUrl() {
		return backUrl;
	}

	public void setBackUrl(String backUrl) {
		this.backUrl = backUrl;
	}

	public Template getTemplate() {
		return template;
	}

	public void setTemplate(Template template) {
		this.template = template;
	}

	public boolean isCheckForExistsDoublets() {
		return checkForExistsDoublets;
	}

	public void setCheckForExistsDoublets(boolean checkForExistsDoublets) {
		this.checkForExistsDoublets = checkForExistsDoublets;
	}

	public boolean isUpdateExistsDoublets() {
		return updateExistsDoublets;
	}

	public void setUpdateExistsDoublets(boolean updateExistsDoublets) {
		this.updateExistsDoublets = updateExistsDoublets;
	}

	protected DataServiceBean getServiceBean(PortletRequest request) {
		if (serviceBean == null) {
			initDataServiceBean(request);
		} else {
//    		String userName = (String) request.getPortletSession().getAttribute(DataServiceBean.USER_NAME, PortletSession.APPLICATION_SCOPE);
            //if (userName != null) {
                serviceBean.setIsDelegation(true);
                serviceBean.setRealUser(request.getUserPrincipal());
            /*} else {
                serviceBean.setUser(request.getUserPrincipal());
                serviceBean.setIsDelegation(false);
            }*/
		}
        serviceBean.setUser(new SystemUser());
		return serviceBean;
	}
	
	protected DataServiceBean getActualServiceBean(PortletRequest request) {
		DataServiceBean actualServiceBean = PortletUtil.createService(request);
		actualServiceBean.setIsDelegation(false);
		actualServiceBean.setUser(request.getUserPrincipal());
		return actualServiceBean;
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public void setResourceBundle(ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;
	}

	public boolean isSupportUpdateExistsDoublets() {
		return supportUpdateExistsDoublets;
	}

	public void setSupportUpdateExistsDoublets(boolean supportUpdateExistsDoublets) {
		this.supportUpdateExistsDoublets = supportUpdateExistsDoublets;
	}
	
	public boolean isSupportCheckForExistsDoublets() {
		return supportCheckForExistsDoublets;
	}

	public void setSupportCheckForExistsDoublets(
			boolean supportCheckForExistsDoublets) {
		this.supportCheckForExistsDoublets = supportCheckForExistsDoublets;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public void reset() {
		message = null; // message.setMessage(null);
		backUrl = null; 
		// Select Template Mode Properties	
		template = null;
	}
	
}