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
package com.aplana.medo.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "regNum", "regDate", "departName", "result",
	"replyDepart" })
public class XMLDirectionInfo {

    @XmlElement(name = "RegNum", required = true)
    protected String regNum;
    @XmlElement(name = "RegDate", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar regDate;
    @XmlElement(name = "DepartName", required = true)
    protected String departName;
    @XmlElement(name = "Result", required = true)
    protected Result result;
    @XmlElement(name = "ReplyDepart", required = true)
    protected ReplyDepart replyDepart;

    public String getRegNum() {
	return regNum;
    }

    public void setRegNum(String value) {
	this.regNum = value;
    }

    public XMLGregorianCalendar getRegDate() {
	return regDate;
    }

    public void setRegDate(XMLGregorianCalendar value) {
	this.regDate = value;
    }

    public String getDepartName() {
	return departName;
    }

    public void setDepartName(String value) {
	this.departName = value;
    }

    public Result getResult() {
	return result;
    }

    public void setResult(Result value) {
	this.result = value;
    }

    protected ReplyDepart getReplyDepart() {
	return this.replyDepart;
    }

    protected void setReplyDepart(ReplyDepart replyDepart) {
	this.replyDepart = replyDepart;
    }
}