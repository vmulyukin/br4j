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
/**
 *
 */
package com.aplana.ireferent.completion.cards;

import javax.xml.bind.annotation.XmlTransient;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.ireferent.IReferentException;
import com.aplana.ireferent.card.GroupCard;
import com.aplana.ireferent.card.PersonCard;
import com.aplana.ireferent.types.WSOCollection;
import com.aplana.ireferent.types.WSOMPerson;
import com.aplana.ireferent.types.WSOPerson;

/**
 * @author PPanichev
 *
 */
@XmlTransient
public class CompletionPerson extends WSOPerson {

    /**
     *
     */
    private static final transient long serialVersionUID = -8828376389222838200L;
    private transient PersonCard person_card = null;
    private transient WSOCollection parents = null;
    private transient Boolean isActive = false;

    public CompletionPerson() {

    }

    public CompletionPerson(PersonCard pc, Boolean includeAttachments,
	    Boolean isMObject) throws DataException,
	    ServiceException, IReferentException {
	this.person_card = pc;
	DataServiceBean serviceBean = person_card.getServiceBean();
	parents = new WSOCollection();
	ObjectId depId = person_card.getDepartmentId();
	// ����� ���������� WSOMGroup
	if (depId != null && depId.getId() != null
		&& !"".equals(depId.getId().toString())) {
	    GroupCard groupCardIReferent = new GroupCard(depId.getId()
		    .toString(), serviceBean);
	    CompletionGroup cg = new CompletionGroup(groupCardIReferent, false,
		    -1, true);
	    parents.getData().add(cg);
	}
	WSOCollection attach = new WSOCollection(); // �� ������ ������ � BR4J �������� ��� ������ �� ������������
	/*if (includeAttachments && person_card.getDownloadFile() != null) {
	    try {
	    FileCard fc = new FileCard(person_card.getDownloadFile(), serviceBean);
	    CompletionFile cf = new CompletionFile(fc, false); // �� MObject
	    attach.getData().add(cf);
	    } catch(Exception e) {
		logger.error("com.aplana.ireferent.completion.cards.CompletionPerson.errorAttach", e);
	    }
	}
	if (!includeAttachments && person_card.getDownloadFile() != null) {
	    try {
		FileCard fc = new FileCard(person_card.getDownloadFile(), serviceBean);
		CompletionFile cf = new CompletionFile(fc, true); // MObject
		attach.getData().add(cf);
	    } catch(Exception e) {
		logger.error("com.aplana.ireferent.completion.cards.CompletionPerson.errorAttach", e);
	    }
	}*/
	setAttributePerson(attach, isMObject);
    }

    public CompletionPerson(PersonCard pc, GroupCard gc, Boolean includeAttachments,
	    Boolean isMObject) throws DataException, ServiceException, IReferentException {
	this.person_card = pc;
	parents = new WSOCollection();
	if (gc != null) {
	    CompletionGroup cg = new CompletionGroup(gc, false, -1, true);
	    parents.getData().add(cg);
	}
	WSOCollection attach = new WSOCollection(); // �� ������ ������ � BR4J �������� ��� ������ �� ������������
	setAttributePerson(attach, isMObject);
    }

    private void setAttributePerson(WSOCollection attach, Boolean isMObject) {
	this.setAttachments(attach);
	this.setId(person_card.getId());
	this.setTitle(person_card.getName());
	this.setExtension(new WSOCollection()); //                       ��������
	this.setParents(parents);
	isActive = person_card.getTemplateId().equals(
		PersonCard.TEMPLATE_PERSON_ID.getId().toString());
	this.setIsActive(isActive);
	if (!isMObject) {
	    this.setType(WSOPerson.CLASS_TYPE);
	    this.setEmail(person_card.getEMail());
	    this.setFax("");
	    this.setFirstName(person_card.getFirstName());
	    this.setLastName(person_card.getLastName());
	    this.setMiddleName(person_card.getMiddleName());
	    this.setTrusters(new WSOCollection()); //                    ��������
	    this.setProxies(new WSOCollection()); //                     ��������
	    this.setPhone(person_card.getContactInfo());
	    this.setPost(person_card.getPosition()); // ���������
	    this.setWorkPhone("");
	    this.setPhoto(new WSOCollection()); //                       ��������
	} else this.setType(WSOMPerson.CLASS_TYPE);
    }
}