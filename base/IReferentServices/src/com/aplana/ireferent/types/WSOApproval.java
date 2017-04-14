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
@XmlType(name = "WSOAPPROVAL", propOrder = {
    "object",
    "responsible",
    "deadlineDate",
    "comment"
})
public class WSOApproval extends WSOMApproval {
    @XmlElement(name = "OBJECT", required = true, nillable = true)
    private WSObject object;
    @XmlElement(name = "RESPONSIBLE", required = true, nillable = true)
    private WSOMPerson responsible;
    @XmlElement(name = "DEADLINEDATE", required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar deadlineDate;
    @XmlElement(name = "COMMENT", required = true, nillable = true)
    private String comment;

    public WSObject getObject() {
	return this.object;
    }

    public void setObject(WSObject object) {
	this.object = object;
    }

    public WSOMPerson getResponsible() {
	return this.responsible;
    }

    public void setResponsible(WSOMPerson responsible) {
	this.responsible = responsible;
    }

    public XMLGregorianCalendar getDeadlineDate() {
	return this.deadlineDate;
    }

    public void setDeadlineDate(XMLGregorianCalendar deadlineDate) {
	this.deadlineDate = deadlineDate;
    }

    public String getComment() {
	return this.comment;
    }

    public void setComment(String comment) {
	this.comment = comment;
    }
}
