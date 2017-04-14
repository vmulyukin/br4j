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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.card.DateTimeAttributeEditor;
import com.aplana.dbmi.gui.EmbeddablePortletFormManager;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.portlet.QuickResolutionPortlet.TypeLink;

public class QuickResolutionPortletSessionBean {
	public static final String DEFAULT_TIME_PATTERN = "dd.mm.yyyy";
	private String mode; // ��������� ��������: ����� 1(����� ������������)
	// ��� ����� 2(�������������� ���������)
	private String backLink; // 	
	private String doneLink;
	//header at Page
	protected String header;
	private String switchNavigatorLink = null;
	//flag to indicate if need to display workflow's information on page 
	private boolean workflowRequired = true;
	//short description 
	private String shortDescription = "";
	private Search employeesSearch;
	private Search extPersonsSearch;
	private List<String> standartResolutionTexts;
	private List<String> typicalVisasTexts;
	private Long parentId = null; // id ������������ �������� � ������� ������� ������
								  // ���������
	private Long baseId = null; // id �������� �������� ��������� � ������� �������
								// ������ ���������
	
	private String typeRes = null; // ��� ���������
	
	private TypeLink typeLink = TypeLink.CARDLINK; // ��� ���������� �������� ��������

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
	// �������������� ���������
	private String targetInit; // ���� ������ ��������: ������ � "��������" ���
	// � "���������"
	private ObjectId idResolution; // id ���������, ���� ��� �� ��������� �
	// �������������, ��� ����� ���� ��� ����
	// �������
	private Long idControlCard = null; // id �������� "������ ��������"

	// ��������� ����������
	// ����������� ��� �������� sessionBean
	private Map<ObjectId, String> immediateEmployees; // ������������ ����� ObjectId ��������
	// ����������� � �� �������
	// ������������� �����������
	private Map<Long, String> responsible = new HashMap<Long, String>(); // Long cardId -> String
	// namePerson
	// �������������
	private Map<Long, String> additionals = new HashMap<Long, String>(); // ������������: Long cardId ->
	// String namePerson
	
	// ��� ��������
	private Map<Long, String> fyi = new HashMap<Long, String>(); // ������������: Long cardId ->

	// ������� �����������
	private Map<Long, String> externals = new HashMap<Long, String>(); // ������������: Long cardId ->
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

	// ���� ������ �����������
	private String otherExec = null;
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
	
	private String decision = null;
	
	private EmbeddablePortletFormManager portletFormManager = new EmbeddablePortletFormManager();

	private boolean isLockedBaseDoc = false;
	
	private boolean validStateBaseDoc = true;

	private ObjectId baseDocState = null;
	
	private Map<ObjectId, Card> docsGroup = new HashMap<ObjectId, Card>();
	

	public boolean isLockedBaseDoc() {
		return isLockedBaseDoc;
	}

	public void setLockedBaseDoc(boolean isLockedBaseDoc) {
		this.isLockedBaseDoc = isLockedBaseDoc;
	}

	public boolean isValidStateBaseDoc() {
		return validStateBaseDoc;
	}
	
	public void setValidStateBaseDoc(boolean isValidStateBaseDoc) {
		this.validStateBaseDoc = isValidStateBaseDoc;
	}
	
	public void setBaseDocState(ObjectId baseDocState) {
		this.baseDocState = baseDocState;
	}
	
	public ObjectId getBaseDocState() {
		return baseDocState;
	}

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

	public boolean isWorkflowRequired() {
		return workflowRequired;
	}

	public void setWorkflowRequired(boolean workflowRequired) {
		this.workflowRequired = workflowRequired;
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

	public Search getExtPersonsSearch() {
		return extPersonsSearch;
	}

	public void setExtPersonsSearch(Search extPersonsSearch) {
		this.extPersonsSearch = extPersonsSearch;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
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

	public List<String> getTypicalVisasTexts() {
		return typicalVisasTexts;
	}

	public void setTypicalVisasTexts(List<String> typicalVisasTexts) {
		this.typicalVisasTexts = typicalVisasTexts;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public Long getParentId() {
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

	public void setIdResolution(ObjectId idResolution) {
		this.idResolution = idResolution;
	}

	public ObjectId getIdResolution() {
		return idResolution;
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

	public Map<Long, String> getResponsible() {
		if (responsible == null)
			responsible = new HashMap<Long, String>();
		return responsible;
	}

	public Long getResponsibleId() {
		final Iterator<Long> i = responsible.keySet().iterator();
		return (i.hasNext()) ?  i.next() : null;
	}

	public String getResponsibleName() {
		final Iterator<Long> i = responsible.keySet().iterator();
		return (i.hasNext()) ? responsible.get(i.next()) : null;
	}
	
	public String getResponsibleNames() {
		return collectionNameToString(getResponsible().values());
	}

	public void setAdditionals(Map<Long, String> additionals) {
		this.additionals = additionals;
	}

	public Map<Long, String> getAdditionals() {
		return additionals;
	}
	
	public void setFyi(Map<Long, String> fyi) {
		this.fyi = fyi;
	}

	public Map<Long, String> getFyi() {
		return fyi;
	}

	public void setExternals(Map<Long, String> externals) {
		this.externals = externals;
	}

	public Map<Long, String> getExternals() {
		return externals;
	}
	
	public void setControllers(Map<Long, String> controllers) {
		this.controllers = controllers;
	}

	public Map<Long, String> getControllers() {
		return controllers;
	}
	
	/**
	 * Checks if given immediate employee was used
	 * It means that it has been used for the one of following lists :  responsible, additionals, fyi 
	 * @param employeeId identifier of employee to check
	 * @return true if passed employee was selected otherwise returns false
	 */
	public boolean isImmediateEmployeeUsed(ObjectId employeeId) {
		
		final Long id = Long.valueOf(employeeId.getId().toString());
		
		if(responsible.containsKey(id))
			return true;
		
		if(additionals.containsKey(id))
			return true;
		
		if(fyi.containsKey(id))
			return true;		
		
		return false;
		
	}
	
/*
	public void setController(Long id, String name) {
		// controller = new HashMap<Long, String>();
		// controller.put(id, name);
		if (id != null)
			controller.add(id);
	}

	public Long getControllerId() {
		// final Iterator<Long> i = controller.keySet().iterator();
		// return (i.hasNext()) ? i.next() : null;
		return (controller == null || controller.isEmpty()) ? null : controller.iterator().next();
	}
	

	public void setControllerIds(Set<Long> ctrls) {
		controller.clear(); 
		controller.addAll(ctrls);
	}

	public Set<Long> getControllerIds() {
		return controller;
	}

	public String getControllerName() {
		// final Long id = getControllerId();
		// return (id != null) ? controller.get(id) : null;
		return null;
	}
*/
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

	public void setOtherExec(String otherExec) {
		this.otherExec = otherExec;
	}

	public String getOtherExec() {
		return otherExec;
	}

	public void setTargetInit(String targetInit) {
		this.targetInit = targetInit;
	}

	public String getTargetInit() {
		return targetInit;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getSignature() {
		return signature;
	}

	public void setGrapFile(FileItem grapFile) {
		this.grapFile = grapFile;
	}

	public FileItem getGrapFile() {
		return grapFile;
	}

	public void setChangeGrap(boolean changeGrap) {
		this.changeGrap = changeGrap;
	}

	public boolean getChangeGrap() {
		return changeGrap;
	}

	public void setDoneLink(String doneLink) {
		this.doneLink = doneLink;
	}

	public String getDoneLink() {
		return doneLink;
	}

	public void setGrapResId(Long grapResId) {
		this.grapResId = grapResId;
	}

	public Long getGrapResId() {
		return grapResId;
	}

	public void setSignImage(String signImg) {
		signTemplate = signImg;
	}

	public String getSignImage() {
		return signTemplate;
	}

	public Long getBaseId() {
		return baseId;
	}

	public void setBaseId(Long baseId) {
		this.baseId = baseId;
	}

	public String getTypeRes() {
		return typeRes;
	}

	public void setTypeRes(String typeRes) {
		this.typeRes = typeRes;
	}

	public String getAdditionalsNames() {
		return collectionNameToString(getAdditionals().values());
	}
	
	public String getFyiNames() {
		return collectionNameToString(getFyi().values());
	}
	
	public String getExternalsNames() {
		return collectionNameToString(getExternals().values());
	}
	
	private String collectionNameToString(Collection<String> c) {
		final StringBuffer str = new StringBuffer();
		final Iterator<String> iter = c.iterator();
		while (iter.hasNext()) {
			str.append( iter.next());
			if (iter.hasNext()) {
				str.append(", ");
			}
		}
		return str.toString();
	}

	public boolean isMinister() {
		return isMinister;
	}

	public void setMinister(boolean isMinister) {
		this.isMinister = isMinister;
	}
	
	// ���� ���������� � ������������ ��������������� ������ ��� ����������
	public boolean isVisaOrSign() {
		return  QuickResolutionPortlet.TARGET_INIT_SIGNING.equals(getTargetInit())
				|| QuickResolutionPortlet.TARGET_INIT_VISA.equals(getTargetInit());
	}
	
	public ObjectId getWorkflowMoveId(){return workflowMoveId;}
	
	public void setWorkflowMoveId(ObjectId workflowMoveId){this.workflowMoveId = workflowMoveId;}
	
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
	
	public TypeLink getTypeLink() {
		return typeLink;
	}

	public void setTypeLink(TypeLink typeLink) {
		this.typeLink = typeLink;
	}

	public Map<ObjectId, Card> getDocsGroup() {
		return docsGroup;
	}

	public void setDocsGroup(Map<ObjectId, Card> docsGroup) {
		this.docsGroup = docsGroup;
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
