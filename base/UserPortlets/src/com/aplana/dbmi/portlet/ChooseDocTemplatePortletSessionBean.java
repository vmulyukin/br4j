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

import java.util.Map;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataServiceBean;

/**
 * Data bean for {@link ChooseDocTemplatePortlet}. 
 * 
 * @author EStatkevich
 */
public class ChooseDocTemplatePortletSessionBean {
	
	protected DataServiceBean serviceBean;
	protected String backUrl;
	protected String message;
	protected String header;
	protected String switchNavigatorLink;
	protected String linkToCard;
	protected Map<ObjectId, String> allowedTemplates; 
	
	public DataServiceBean getServiceBean() {
		return serviceBean;
	}
	
	public void setServiceBean(DataServiceBean serviceBean) {
		this.serviceBean = serviceBean;
	}

	public String getBackUrl() {
		return backUrl;
	}

	public void setBackUrl(String backUrl) {
		this.backUrl = backUrl;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
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

	public Map<ObjectId, String> getAllowedTemplates() {
		return allowedTemplates;
	}

	public void setAllowedTemplates(Map<ObjectId, String> allowedTemplates) {
		this.allowedTemplates = allowedTemplates;
	}

	public String getLinkToCard() {
		return linkToCard;
	}

	public void setLinkToCard(String linkToCard) {
		this.linkToCard = linkToCard;
	}
}
