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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.gui.EmbeddablePortletFormManager;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;

public class QuickIndepResolutionPortletSessionBean {
	private String backUrl; //
	private String doneUrl;
	// header at Page
	protected String header;
	private String switchNavigatorLink = null;
	private Search employeesSearch;
	private Search extPersonsSearch;
	private List<String> standartResolutionTexts;
	private List<String> typicalVisasTexts;

	private String regNumber = null;

	private String resolutionText = null;
	private Date term = null; // ���� ����������
	private Date controlTerm = null; // ���� "�� ��������"
	private Boolean isOnControl = null; // "�� ��������"

	private Card resolutionCard; 			// �������� ���������
//	private Card personalControlCard;		// �������� "������ ��������"
	private boolean cardLinked;		// ���� ����, �������� �� ������� �������� ���������

	private Long idControlCard = null; // id �������� "������ ��������"

	// ��������� ����������; ����������� ��� �������� sessionBean
	// ���� [ID �������� �������, ��� �������]
	private Map<ObjectId, String> immediateEmployees;  
	
	// ���� [ID �������� �������, ��� �������]
	// ������������� �����������
	private Map<Long, String> responsible = new HashMap<Long, String>();
	// �������������
	private Map<Long, String> additionals = new HashMap<Long, String>();
	// ���������
	private Map<Long, String> controllers = new HashMap<Long, String>(5); 

	//��������� ��������
	private ObjectId linkedDoc;
	// ������� ������������
	private Person currentPerson;

	// contains attached files for resolution : cardId -> fileName
	private Map<ObjectId, String> attachedFiles = new LinkedHashMap<ObjectId, String>();

	// ��������������� ���������
	private String message;

	// ���� �������
	private String signature;
	// ���� ����������� ���������
	FileItem grapFile;
	// ���� ��������� ����������� ���������
	boolean changeGrap = false;
	// �������� ����������� ���������
	Long grapResId = null;
	// ������� �������
	private String signTemplate = null;

	private boolean isMinister = false;

	private ObjectId workflowMoveId;


	private EmbeddablePortletFormManager portletFormManager = new EmbeddablePortletFormManager();

	public EmbeddablePortletFormManager getPortletFormManager() {
		return portletFormManager;
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

	public Search getEmployeesSearch() {
		return employeesSearch;
	}

	public void setEmployeesSearch(Search availableEmployeesSearch) {
		this.employeesSearch = availableEmployeesSearch;
	}

	public Search getExtPersonsSearch() {
		return extPersonsSearch;
	}

	public void setExtPersonsSearch(Search extPersonsSearch) {
		this.extPersonsSearch = extPersonsSearch;
	}

	public String getBackUrl() {
		return backUrl;
	}

	public void setBackUrl(String backUrl) {
		this.backUrl = backUrl;
	}

	public void setStandartResolutionTexts(List<String> standartResolutionTexts) {
		this.standartResolutionTexts = standartResolutionTexts;
	}

	public List<String> getStandartResolutionTexts() {
		return standartResolutionTexts;
	}

	public List<String> getTypicalVisasTexts() {
		return typicalVisasTexts;
	}

	public void setTypicalVisasTexts(List<String> typicalVisasTexts) {
		this.typicalVisasTexts = typicalVisasTexts;
	}

	public void setResolutionText(String resolutionText) {
		this.resolutionText = resolutionText;
	}

	public String getResolutionText() {
		return resolutionText;
	}

	public void setTerm(Date term) {
		this.term = term;
	}

	public Date getTerm() {
		return term;
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

	public ObjectId getResolutionCardId() {
		return resolutionCard.getId();
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

	public void setResponsible(Long id, String name) {
		responsible = new HashMap<Long, String>();
		responsible.put(id, name);
	}
	
	public void resetResponsible() {
		responsible = new HashMap<Long, String>();
	}
	
	public Map<Long, String> getResponsible() {
		if (responsible == null)
			responsible = new HashMap<Long, String>();
		return responsible;
	}

	public Long getResponsibleId() {
		final Iterator<Long> i = responsible.keySet().iterator();
		return (i.hasNext()) ? i.next() : null;
	}

	public String getResponsibleName() {
		final Iterator<Long> i = responsible.keySet().iterator();
		return (i.hasNext()) ? responsible.get(i.next()) : null;
	}

	public void setAdditionals(Map<Long, String> additionals) {
		this.additionals = additionals;
	}

	public Map<Long, String> getAdditionals() {
		return additionals;
	}

	public void setControllers(Map<Long, String> controllers) {
		this.controllers = controllers;
	}

	public Map<Long, String> getControllers() {
		return controllers;
	}

	/**
	 * Checks if given immediate employee was used It means that it has been
	 * used for the one of following lists : responsible, additionals, fyi
	 * 
	 * @param employeeId
	 *            identifier of employee to check
	 * @return true if passed employee was selected otherwise returns false
	 */
	public boolean isImmediateEmployeeUsed(ObjectId employeeId) {

		final Long id = Long.valueOf(employeeId.getId().toString());

		if (responsible.containsKey(id))
			return true;

		if (additionals.containsKey(id))
			return true;

		return false;

	}

	public void setCurrentPerson(Person currentPerson) {
		this.currentPerson = currentPerson;
	}

	public Person getCurrentPerson() {
		return currentPerson;
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

	public void setDoneUrl(String doneUrl) {
		this.doneUrl = doneUrl;
	}

	public String getDoneUrl() {
		return doneUrl;
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

	public ObjectId getWorkflowMoveId() {
		return workflowMoveId;
	}

	public void setWorkflowMoveId(ObjectId workflowMoveId) {
		this.workflowMoveId = workflowMoveId;
	}

	public Boolean getIsOnControl() {
		return isOnControl;
	}

	public void setIsOnControl(Boolean isOnControl) {
		this.isOnControl = isOnControl;
	}

	public void setResolutionCard(Card resolutionCard) {
		this.resolutionCard = resolutionCard;
	}

	public Card getResolutionCard() {
		return this.resolutionCard;
	}
	
	public ObjectId getLinkedDoc() {
		return linkedDoc;
	}

	public void setLinkedDoc(ObjectId linkedDoc) {
		this.linkedDoc = linkedDoc;
	}

	public boolean isCardLinked() {
		return cardLinked;
	}

	public void setCardLinked(boolean cardLinked) {
		this.cardLinked = cardLinked;
	}
}
