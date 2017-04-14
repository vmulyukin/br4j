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
package com.aplana.ireferent.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WSO_MDOCUMENT", propOrder = {
    "regNum",
    "regDate",
    "subject",
    "statusName",
	"urgencyLevel"
})
public class WSOMDocument extends WSObject {

    @XmlElement(name = "REGNUM", required = true, nillable = true)
    private String regNum;
    @XmlElement(name = "REGDATE", required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar regDate;
    @XmlElement(name = "SUBJECT", required = true, nillable = true)
    private String subject;
    @XmlElement(name = "STATUSNAME", required = true, nillable = true)
    private String statusName;
    @XmlElement(name = "URGENCYLEVEL", required = true, nillable = true)
    String urgencyLevel;
    /**
     * @return the regNum
     */
    public String getRegNum() {
        return this.regNum;
    }
    /**
     * @param regNum the regNum to set
     */
    public void setRegNum(String regNum) {
        this.regNum = regNum;
    }
    /**
     * @return the regDate
     */
    public XMLGregorianCalendar getRegDate() {
        return this.regDate;
    }
    /**
     * @param regDate the regDate to set
     */
    public void setRegDate(XMLGregorianCalendar regDate) {
        this.regDate = regDate;
    }
    /**
     * @return the subject
     */
    public String getSubject() {
        return this.subject;
    }
    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }
    /**
     * @return the statusName
     */
    public String getStatusName() {
        return this.statusName;
    }
    /**
     * @param statusName the statusName to set
     */
    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
    
	/**
     * @return the urgencyLevel
     */
    public String getUrgencyLevel() {
        return this.urgencyLevel;
    }

    /**
     * @param urgencyLevel the urgencyLevel to set
     */
    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }
}
