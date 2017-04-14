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
@XmlType(name = "WSO_MNP", propOrder = {
    "isControl",
    "subjectnp",
    "controller",
    "mainExecutors",
    "executors",
    "signer",
    "childs",
    "datePlan"
})
public class WSOMNp  extends WSOMDocument {

    @XmlElement(name = "SUBJECTNP", required = true, nillable = true)
    private String subjectnp;
    @XmlElement(name = "DATEPLAN", required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar datePlan;
    @XmlElement(name = "ISCONTROL", required = true, nillable = true)
    private Boolean isControl;
    @XmlElement(name = "CHILDS", required = true, nillable = true)
    private WSOCollection childs;
    @XmlElement(name = "SIGNER", required = true, nillable = true)
    private WSOMPerson signer;
    @XmlElement(name = "CONTROLLER", required = true, nillable = true)
    private WSOMPerson controller;
    @XmlElement(name = "MAINEXECUTORS", required = true, nillable = true)
    private WSOCollection mainExecutors;
    @XmlElement(name = "EXECUTORS", required = true, nillable = true)
    private WSOCollection executors;
	

    public static final String  CLASS_TYPE = "WSO_MNP";

	public String getSubjectNp() {
		return subjectnp;
	}

	public void setSubjectNp(String subjectnp) {
		this.subjectnp = subjectnp;
	}

	public XMLGregorianCalendar getDatePlan() {
		return datePlan;
	}

	public void setDatePlan(XMLGregorianCalendar datePlan) {
		this.datePlan = datePlan;
	}

	public Boolean getIsControl() {
		return isControl;
	}

	public void setIsControl(Boolean isControl) {
		this.isControl = isControl;
	}

	public WSOCollection getChilds() {
		return childs;
	}

	public void setChilds(WSOCollection childs) {
		this.childs = childs;
	}

	public WSOMPerson getSigner() {
		return signer;
	}

	public void setSigner(WSOMPerson signer) {
		this.signer = signer;
	}

	public WSOMPerson getController() {
		return controller;
	}

	public void setController(WSOMPerson controller) {
		this.controller = controller;
	}

	public WSOCollection getMainExecutors() {
		return mainExecutors;
	}

	public void setMainExecutors(WSOCollection mainExecutors) {
		this.mainExecutors = mainExecutors;
	}

	public WSOCollection getExecutors() {
		return executors;
	}

	public void setExecutors(WSOCollection executors) {
		this.executors = executors;
	}
}
