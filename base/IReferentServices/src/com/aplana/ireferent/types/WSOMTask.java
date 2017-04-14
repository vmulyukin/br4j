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
package com.aplana.ireferent.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WSO_MTASK", propOrder = { "document", "parent", "subject",
	"isControl", "childs", "datePlan", "signer", "controller",
	"mainExecutors", "executors", "statusName" })
public class WSOMTask extends WSObject {

    @XmlElement(name = "DOCUMENT", required = true, nillable = true)
    private WSOMDocument document;
    @XmlElement(name = "PARENT", required = true, nillable = true)
    private WSOMTask parent;
    @XmlElement(name = "SUBJECT", required = true, nillable = true)
    private String subject;
    @XmlElement(name = "ISCONTROL", required = true, nillable = true)
    private Boolean isControl;
    @XmlElement(name = "CHILDS", required = true, nillable = true)
    private WSOCollection childs;
    @XmlElement(name = "DATEPLAN", required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar datePlan;
    @XmlElement(name = "SIGNER", required = true, nillable = true)
    private WSOMPerson signer;
    @XmlElement(name = "CONTROLLER", required = true, nillable = true)
    private WSOMPerson controller;
    @XmlElement(name = "MAINEXECUTORS", required = true, nillable = true)
    private WSOCollection mainExecutors;
    @XmlElement(name = "EXECUTORS", required = true, nillable = true)
    private WSOCollection executors;
    @XmlElement(name = "STATUSNAME", required = true, nillable = true)
    private String statusName;

    /**
     * @return the document
     */
    public WSOMDocument getDocument() {
	return this.document;
    }

    /**
     * @param document
     *                the document to set
     */
    public void setDocument(WSOMDocument document) {
	this.document = document;
    }

    public WSOMTask getParent() {
        return this.parent;
    }

    public void setParent(WSOMTask parent) {
        this.parent = parent;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
	return this.subject;
    }

    /**
     * @param subject
     *                the subject to set
     */
    public void setSubject(String subject) {
	this.subject = subject;
    }

    /**
     * @return the isControl
     */
    public Boolean isControl() {
	return this.isControl;
    }

    /**
     * @param isControl
     *                the isControl to set
     */
    public void setControl(Boolean isControl) {
	this.isControl = isControl;
    }

    /**
     * @return the childs
     */
    public WSOCollection getChilds() {
	return this.childs;
    }

    /**
     * @param childs
     *                the childs to set
     */
    public void setChilds(WSOCollection childs) {
	this.childs = childs;
    }

    /**
     * @return the datePlan
     */
    public XMLGregorianCalendar getDatePlan() {
	return this.datePlan;
    }

    /**
     * @param datePlan
     *                the datePlan to set
     */
    public void setDatePlan(XMLGregorianCalendar datePlan) {
	this.datePlan = datePlan;
    }

    /**
     * @return the signer
     */
    public WSOMPerson getSigner() {
	return this.signer;
    }

    /**
     * @param signer
     *                the signer to set
     */
    public void setSigner(WSOMPerson signer) {
	this.signer = signer;
    }

    /**
     * @return the controller
     */
    public WSOMPerson getController() {
	return this.controller;
    }

    /**
     * @param controller
     *                the controller to set
     */
    public void setController(WSOMPerson controller) {
	this.controller = controller;
    }

    /**
     * @return the mainExecutors
     */
    public WSOCollection getMainExecutors() {
	return this.mainExecutors;
    }

    /**
     * @param mainExecutors
     *                the mainExecutors to set
     */
    public void setMainExecutors(WSOCollection mainExecutors) {
	this.mainExecutors = mainExecutors;
    }

    /**
     * @return the executors
     */
    public WSOCollection getExecutors() {
	return this.executors;
    }

    /**
     * @param executors
     *                the executors to set
     */
    public void setExecutors(WSOCollection executors) {
	this.executors = executors;
    }

    /**
     * @return the statusName
     */
    public String getStatusName() {
	return this.statusName;
    }

    /**
     * @param statusName
     *                the statusName to set
     */
    public void setStatusName(String statusName) {
	this.statusName = statusName;
    }

}
