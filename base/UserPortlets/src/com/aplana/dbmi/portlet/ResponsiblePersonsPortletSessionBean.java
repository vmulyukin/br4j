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

import java.util.HashMap;
import java.util.Map;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.AsyncDataServiceBean;

/**
 * Data bean for {@link ResponsiblePersonsPortlet}. 
 * 
 * @author EStatkevich
 */
public abstract class ResponsiblePersonsPortletSessionBean {
	
	protected String cardId;
	protected AsyncDataServiceBean serviceBean;
	protected String backUrl;
	protected String doneUrl;
	protected String message;
	protected String header;
	protected String shortDescription;
	protected String switchNavigatorLink;
	protected boolean workflowRequired = true;
	protected Card baseCard;
	// contains attached files from parent catd : cardId -> fileName
	protected Map<ObjectId, String> baseCardAttachedFiles = new HashMap<ObjectId, String>();
	// ��������� ���������� (����������� ��� �������� sessionBean)
	protected Map<ObjectId, String> immediateEmployees; // ������������ ����� ObjectId �������� ����������� � �� �������
	
	public String getCardId() {
		return cardId;
	}
	
	public void setCardId(String cardId) {
		this.cardId = cardId;
	}
	
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

	public String getDoneUrl() {
		return doneUrl;
	}

	public void setDoneUrl(String doneUrl) {
		this.doneUrl = doneUrl;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public void setImmediateEmployees(Map<ObjectId, String> immediateEmployees) {
		this.immediateEmployees = immediateEmployees;
	}

	public Map<ObjectId, String> getImmediateEmployees() {
		return immediateEmployees;
	}
	
	public Map<ObjectId, String> getBaseCardAttachedFiles() {
		return baseCardAttachedFiles;
	}

	public void setBaseCardAttachedFiles(Map<ObjectId, String> attachedFiles) {
		this.baseCardAttachedFiles = attachedFiles;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}
	
	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public Card getBaseCard() {
		return baseCard;
	}

	public void setBaseCard(Card baseCard) {
		this.baseCard = baseCard;
	}

	public String getSwitchNavigatorLink() {
		return switchNavigatorLink;
	}

	public void setSwitchNavigatorLink(String switchNavigatorLink) {
		this.switchNavigatorLink = switchNavigatorLink;
	}

	public boolean isWorkflowRequired() {
		return workflowRequired;
	}

	public void setWorkflowRequired(boolean workflowRequired) {
		this.workflowRequired = workflowRequired;
	}
}
