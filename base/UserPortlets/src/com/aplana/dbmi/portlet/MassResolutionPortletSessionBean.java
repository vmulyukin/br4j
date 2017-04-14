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

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.card.DateTimeAttributeEditor;
import com.aplana.dbmi.gui.EmbeddablePortletFormManager;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;

public class MassResolutionPortletSessionBean {
	public static final String DEFAULT_TIME_PATTERN = "dd.mm.yyyy";
	private String backLink; // 	
	private String doneLink;
	//header at Page
	protected String header;
	private String switchNavigatorLink = null;
	//short description 
	private String shortDescription = "";
	private Search employeesSearch;
	private List<String> standartResolutionTexts;
	private ObjectId parentId = null; // id ������������ �������� � ������� ������� ������
								  // ���������
	private boolean workflowRequired = true;

	private String regNumber = null;
	
	private String typeDoc = null;
	
	// ��������������� �� sessionBean. ��������� ������������ �� ��������
	// ���������� �������� "��������� ��� ��� ������������"
	// � ������ ������ �������������� ���������, ����� ���� �������������
	// ����������� ������� � ���������
	private String resolutionText = null;
	private DateAttribute termAttribute = null; 				// ���� ����������
	private Date controlTerm = null; 		// ���� "�� ��������"
	private Date preliminaryTerm = null;	// ��������������� ����
	private Boolean isOnControl = null; //"�� ��������"
	private String stateInit; // ����� ������ ��������: �������� ��������� ���
	private Set<ObjectId> idsResolution; // ids ���������, ���� ��� �� ��������� �
	// �������������, ��� ����� ���� ��� ����
	// �������
	private Long idControlCard = null; // id �������� "������ ��������"

	// ��������� ����������
	// ����������� ��� �������� sessionBean
	private Map<ObjectId, String> immediateEmployees; // ������������ ����� ObjectId ��������
	// ����������� � �� �������
	// ������������� �����������
	private Map<Long, String> responsibles = new HashMap<Long, String>(); // Long cardId -> String
	
	// ���������
	private Map<Long, String> controllers = new HashMap<Long, String>(5); // ������������: Long �ardId -> String namePerson

	// ������� ������������
	private Person currentPerson;
	// contains attached files from base catd : cardId -> fileName
	private Map<ObjectId, String> baseCardAttachedFiles = new LinkedHashMap<ObjectId, String>();
	
	// contains attached files  for resolution : cardId -> fileName
	private Map<ObjectId, String> attachedFiles = new LinkedHashMap<ObjectId, String>();

	// ������������ ��� ������� ���������� ������ ��������, � Person �����������
	private Map<Long, String> nonexistent = null; // ������������: Long cardId -> String
	// namePerson
	// ��������������� ���������
	private String message;

	// ���� �������
	private String signature;
	// ������� �������
	private String signTemplate = null;
	
	private boolean isMinister = false;
	
	private String decision = null;
	
	private EmbeddablePortletFormManager portletFormManager = new EmbeddablePortletFormManager();



	public EmbeddablePortletFormManager getPortletFormManager() {
		return portletFormManager;
	}

	public Map<ObjectId, String> getBaseCardAttachedFiles() {
		return baseCardAttachedFiles;
	}

	public void setBaseCardAttachedFiles(Map<ObjectId, String> attachedFiles) {
		this.baseCardAttachedFiles = attachedFiles;
	}

	public String getSwitchNavigatorLink() {
		return switchNavigatorLink;
	}

	public void setSwitchNavigatorLink(String switchNavigatorLink) {
		this.switchNavigatorLink = switchNavigatorLink;
	}

	public Map<ObjectId, String> getAttachedFiles() {
		return attachedFiles;
	}

	public void setAttachedFiles(Map<ObjectId, String> attachedFiles) {
		this.attachedFiles = attachedFiles;
	}

	public String getRegNumber() {
		return regNumber;
	}

	public void setRegNumber(String regNumber) {
		this.regNumber = regNumber;
	}

	public String getTypeDoc() {
		return typeDoc;
	}

	public void setTypeDoc(String typeDoc) {
		this.typeDoc = typeDoc;
	}

	public Search getEmployeesSearch() {
		return employeesSearch;
	}

	public void setEmployeesSearch(Search availableEmployeesSearch) {
		this.employeesSearch = availableEmployeesSearch;
	}

	public String getBackLink() {
		return backLink;
	}

	public void setBackLink(String backLink) {
		this.backLink = backLink;
	}

	public void setStandartResolutionTexts(List<String> standartResolutionTexts) {
		this.standartResolutionTexts = standartResolutionTexts;
	}

	public List<String> getStandartResolutionTexts() {
		return standartResolutionTexts;
	}

	public void setParentId(ObjectId parentId) {
		this.parentId = parentId;
	}

	public ObjectId getParentId() {
		return parentId;
	}

	public void setResolutionText(String resolutionText) {
		this.resolutionText = resolutionText;
	}

	public String getResolutionText() {
		return resolutionText;
	}

	public void setTermValue(Date term) {
		if (null != this.termAttribute){
			this.termAttribute.setValue(term);
		}
	}

	public Date getTermValue() {
		Date term = null;
		if (null != this.termAttribute){
			term = this.termAttribute.getValue();
		}
		return term;
	}

	public void setTermAttribute(DateAttribute termAttribute) {
		this.termAttribute = termAttribute;
	}

	public DateAttribute getTermAttribute() {
		return termAttribute;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}
	
	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public void setControlTerm(Date controlTerm) {
		this.controlTerm = controlTerm;
	}

	public Date getControlTerm() {
		return controlTerm;
	}

	public void setStateInit(String stateInit) {
		this.stateInit = stateInit;
	}

	public String getStateInit() {
		return stateInit;
	}

	public void setIdsResolution(Set<ObjectId> idsResolution) {
		this.idsResolution = idsResolution;
	}

	public Set<ObjectId> getIdsResolution() {
		return idsResolution;
	}

	public void setIdControlCard(Long idControlCard) {
		this.idControlCard = idControlCard;
	}

	public Long getIdControlCard() {
		return idControlCard;
	}

	public void setImmediateEmployees(Map<ObjectId, String> immediateEmployees) {
		this.immediateEmployees = immediateEmployees;
	}

	public Map<ObjectId, String> getImmediateEmployees() {
		return immediateEmployees;
	}

	public void setResponsibles(Map<Long, String> responsibles) {
		this.responsibles = responsibles;
	}

	public Map<Long, String> getResponsibles() {
		return responsibles;
	}
	
	public void setControllers(Map<Long, String> controllers) {
		this.controllers = controllers;
	}

	public Map<Long, String> getControllers() {
		return controllers;
	}

	public void setCurrentPerson(Person currentPerson) {
		this.currentPerson = currentPerson;
	}

	public Person getCurrentPerson() {
		return currentPerson;
	}

	public void setNonexistent(Map<Long, String> nonexistent) {
		this.nonexistent = nonexistent;
	}

	public Map<Long, String> getNonexistent() {
		return nonexistent;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getSignature() {
		return signature;
	}

	public void setDoneLink(String doneLink) {
		this.doneLink = doneLink;
	}

	public String getDoneLink() {
		return doneLink;
	}

	public void setSignImage(String signImg) {
		signTemplate = signImg;
	}

	public String getSignImage() {
		return signTemplate;
	}

	public boolean isMinister() {
		return isMinister;
	}

	public void setMinister(boolean isMinister) {
		this.isMinister = isMinister;
	}
	
	public String getDecision() {
		return this.decision;
	}

	public void setDecision(String decision) {
		this.decision = decision;
	}

	public Boolean getIsOnControl() {
		return isOnControl;
	}

	public void setIsOnControl(Boolean isOnControl) {
		this.isOnControl = isOnControl;
	}
	
	public void setPreliminaryTerm(Date preliminaryTerm) {
		this.preliminaryTerm = preliminaryTerm;
	}
	
	public Date getPreliminaryTerm() {
		return this.preliminaryTerm;
	}
	
	public boolean isWorkflowRequired() {
		return workflowRequired;
	}

	public void setWorkflowRequired(boolean workflowRequired) {
		this.workflowRequired = workflowRequired;
	}
	
	public String getTermTimePattern() {
		return (null != this.termAttribute) ? 
				this.termAttribute.getTimePattern() : DEFAULT_TIME_PATTERN;
	}

	public boolean isTermShowTime() {
		return (null != this.termAttribute) ? 
				this.termAttribute.isShowTime() : false;
	}

	public String getTermString() {
		return (null != this.getTermValue()) ?
			DateTimeAttributeEditor.getStringDate(termAttribute) +
			DateTimeAttributeEditor.getStringTime(termAttribute) : "";
	}
}
