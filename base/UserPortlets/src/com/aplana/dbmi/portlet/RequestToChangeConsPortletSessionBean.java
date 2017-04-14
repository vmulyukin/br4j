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

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.gui.EmbeddablePortletFormManager;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;

public class RequestToChangeConsPortletSessionBean {
	public static final String DEFAULT_TIME_PATTERN = "dd.MM.yyyy";
	private String backLink; // 	
	private String doneLink;
	//header at Page
	protected String header;
	private String switchNavigatorLink = null;
	//short description 
	private String shortDescription = "";
	private Search employeesSearch;
	private List<String> standartResolutionTexts;
	private Card parent = null; // id ������������ �������� � ������� ������� ������
								  // ���������
	private boolean workflowRequired = true;

	private String regNumber = null;
	private Date regDate = null;

	private String jsonTypes;
	private String jsonResp;

	private String jsonConsiderators;
	private Card currentConsidDoc;

	private String comment = null;
	private Date changeTerm = null; // ��������� �����
	private String stateInit; // ����� ������ ��������: �������� ��������� ���
	private Set<ObjectId> idsResolution; // ids ���������, ���� ��� �� ��������� �
	// �������������, ��� ����� ���� ��� ����
	// �������
	private Long idControlCard = null; // id �������� "������ ��������"

	// ��������� ����������
	// ����������� ��� �������� sessionBean
	private Map<ObjectId, String> immediateEmployees; // ������������ ����� ObjectId ��������
	// ����������� � �� �������
	
	private List<Long> types = new ArrayList<Long>();
	
	private List<NewConsiderator> news = new ArrayList<NewConsiderator>();

	private Long consideratorCardId = null;

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
	
	public List<Long> getTypes() {
		return types;
	}

	public void setNews(List<NewConsiderator> news) {
		this.news = news;
	}
	
	public List<NewConsiderator> getNews() {
		return news;
	}

	public Long getConsideratorCardId() {
		return consideratorCardId;
	}

	public void setConsideratorCardId(Long consideratorCardId) {
		this.consideratorCardId = consideratorCardId;
	}

	public void setTypes(List<Long> types) {
		this.types = types;
	}
	
	public Card getCurrentConsidDoc() {
		return currentConsidDoc;
	}

	public void setCurrentConsidDoc(Card currentConsidDoc) {
		this.currentConsidDoc = currentConsidDoc;
	}

	public String getRegNumber() {
		return regNumber;
	}

	public void setRegNumber(String regNumber) {
		this.regNumber = regNumber;
	}
	
	public Date getRegDate() {
		return regDate;
	}

	public void setRegDate(Date regDate) {
		this.regDate = regDate;
	}
	
	public String getJsonTypes() {
		return jsonTypes;
	}

	public void setJsonTypes(String jsonTypes) {
		this.jsonTypes = jsonTypes;
	}
	
	public String getJsonResp() {
		return jsonResp;
	}

	public void setJsonResp(String jsonResp) {
		this.jsonResp = jsonResp;
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

	public void setParent(Card parent) {
		this.parent = parent;
	}

	public Card getParent() {
		return parent;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
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

	public void setChangeTerm(Date changeTerm) {
		this.changeTerm = changeTerm;
	}

	public Date getChangeTerm() {
		return changeTerm;
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
	
	public boolean isWorkflowRequired() {
		return workflowRequired;
	}

	public void setWorkflowRequired(boolean workflowRequired) {
		this.workflowRequired = workflowRequired;
	}
	
	public String getJsonConsiderators() {
		return jsonConsiderators;
	}

	public void setJsonConsiderators(String jsonConsiderators) {
		this.jsonConsiderators = jsonConsiderators;
	}

	// ����� ���������������
	public static class NewConsiderator {
		
		private Long considerator;
		private Long resp;
		private Date term;

		public Long getConsiderator() {
			return considerator;
		}

		public void setConsiderator(Long considerator) {
			this.considerator = considerator;
		}

		public Long getResp() {
			return resp;
		}

		public void setResp(Long resp) {
			this.resp = resp;
		}
		
		public Date getTerm() {
			return term;
		}

		public void setTerm(Date term) {
			this.term = term;
		}
		
	}
}
