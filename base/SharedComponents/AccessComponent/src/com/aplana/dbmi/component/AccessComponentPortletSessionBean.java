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

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.UserPrincipal;

/**
 *
 * A sample Java bean that stores portlet instance data in portlet session.
 *
 */
public class AccessComponentPortletSessionBean {
	
	private DataServiceBean serviceBean = null;
		
	/**
	 * Last text for the text form
	 */
	private String formText = "";

	/**
	 * Set last text for the text form.
	 * 
	 * @param formText last text for the text form.
	 */
	public void setFormText(String formText) {
		this.formText = formText;
	}

	/**
	 * Get last text for the text form.
	 * 
	 * @return last text for the text form
	 */
	public String getFormText() {
		return this.formText;
	}
	
	protected DataServiceBean getServiceBean(PortletRequest request) {
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
	private void initDataServiceBean(PortletRequest request) {
		this.serviceBean = PortletUtil.createService(request);
	}
	

}
