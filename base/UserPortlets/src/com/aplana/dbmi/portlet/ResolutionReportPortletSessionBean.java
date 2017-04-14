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

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.aplana.dbmi.card.AttributeEditorDialog;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.AsyncDataServiceBean;

public class ResolutionReportPortletSessionBean {
	
	private String backLink;
	private String reportText;
	private String message;
	
	private Card resolutionReportCard = null;
	private Card resolutionCard = null;
	
	private List<Card> reportAttachments;
	private List<Card> reportPreparedDocs;
	private List<Card> childResolutions;
	private Map<ObjectId, List<Card>> resolutionToReportsMap;
	private List<Card> attachments;
	private List<Card> preparedDocuments;
	private String mainCardid;
	private Principal realUser;
	
	private List<Card> groupExecutionReports = new ArrayList<Card>();
	
	private String shortDescription;
	private Map<ObjectId, String> baseCardAttachedFiles = new LinkedHashMap<ObjectId, String>();
	private boolean workflowRequired;
	
	private AttributeEditorDialog attributeEditorDialog;
	private List<Card> groupExecutionReportsSameCard;
	
	public String getMainCardid() {
		return mainCardid;
	}

	public void setMainCardid(String mainCardid) {
		this.mainCardid = mainCardid;
	}

	private String switchNavigatorLink = null;
	protected String header;
	
	private AsyncDataServiceBean serviceBean = null;
	
	public AsyncDataServiceBean getServiceBean() {
		return serviceBean;
	}
	
	public void setDataServiceBean(AsyncDataServiceBean serviceBean) {
		this.serviceBean = serviceBean;
	}
	
	public Card getResolutionReportCard() {
		return resolutionReportCard;
	}

	public void setResolutionReportCard(Card resolutionReportCard) {
		this.resolutionReportCard = resolutionReportCard;
	}
	
	public Card getResolutionCard() {
		return resolutionCard;
	}

	public void setResolutionCard(Card resolutionCard) {
		this.resolutionCard = resolutionCard;
	}

	public String getReportText() {
		return reportText;
	}

	public void setReportText(String reportText) {
		this.reportText = reportText;
	}

	public List<Card> getReportAttachments() {
		return reportAttachments;
	}

	public void setReportAttachments(List<Card> reportAttachments) {
		this.reportAttachments = reportAttachments;
	}

	public List<Card> getReportPreparedDocs() {
		return reportPreparedDocs;
	}

	public void setReportPreparedDocs(List<Card> reportPreparedDocs) {
		this.reportPreparedDocs = reportPreparedDocs;
	}

	public List<Card> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Card> attachments) {
		this.attachments = attachments;
	}
	
	public List<Card> getPreparedDocuments() {
		return preparedDocuments;
	}

	public void setPreparedDocuments(List<Card> preparedDocuments) {
		this.preparedDocuments = preparedDocuments;
	}

	public List<Card> getChildResolutions() {
		return childResolutions;
	}

	public void setChildResolutions(List<Card> childResolutions) {
		this.childResolutions = childResolutions;
	}
	
	public List<Card> getAllResolutions() {
		List<Card> resolutionsList = new ArrayList<Card>();
		
		if(null != resolutionCard) {
			resolutionsList.add(resolutionCard);
		}
		
		if(null != childResolutions) {
			resolutionsList.addAll(childResolutions);
		}
		
		return resolutionsList;
	}
	
	public Map<ObjectId, List<Card>> getResolutionToReportsMap() {
		return resolutionToReportsMap;
	}

	public void setResolutionToReportsMap(
			Map<ObjectId, List<Card>> resolutionToReportsMap) {
		this.resolutionToReportsMap = resolutionToReportsMap;
	}

	public String getBackLink() {
		return backLink;
	}

	public void setBackLink(String backLink) {
		this.backLink = backLink;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public String getSwitchNavigatorLink() {
		return switchNavigatorLink;
	}

	public void setSwitchNavigatorLink(String switchNavigatorLink) {
		this.switchNavigatorLink = switchNavigatorLink;
	}

	public void setServiceBean(AsyncDataServiceBean serviceBean) {
		this.serviceBean = serviceBean;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public Principal getRealUser() {
		return realUser;
	}

	public void setRealUser(Principal realUser) {
		this.realUser = realUser;
	}

	public List<Card> getGroupExecutionReports() {
		return groupExecutionReports;
	}

	public void setGroupExecutionReports(List<Card> groupExecutionReports) {
		this.groupExecutionReports = groupExecutionReports;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public Map<ObjectId, String> getBaseCardAttachedFiles() {
		return baseCardAttachedFiles;
	}

	public void setBaseCardAttachedFiles(Map<ObjectId, String> baseCardAttachedFiles) {
		this.baseCardAttachedFiles = baseCardAttachedFiles;
	}

	public boolean isWorkflowRequired() {
		return workflowRequired;
	}

	public void setWorkflowRequired(boolean workflowRequired) {
		this.workflowRequired = workflowRequired;
	}
	
	public boolean isGroupExecutionMode(){
		return !this.groupExecutionReports.isEmpty();
	}
	
	public List<Card> getGroupExecutionReportsSameCard() {
		return groupExecutionReportsSameCard;
	}

	public void setGroupExecutionReportsSameCard(List<Card> groupExecutionReportsSameCard) {
		this.groupExecutionReportsSameCard = groupExecutionReportsSameCard;
	}
	
	public AttributeEditorDialog getAttributeEditorDialog() {
		return attributeEditorDialog;
	}

	public void setAttributeEditorDialog(AttributeEditorDialog attributeEditorDialog) {
		this.attributeEditorDialog = attributeEditorDialog;
	}
}
