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
package com.aplana.dmsi.types.common;

import javax.xml.datatype.XMLGregorianCalendar;

import com.aplana.dmsi.types.DMSIObject;

public class ReceiptSource extends DMSIObject {

    private Organization correspondent;
    private String regNumber;
    private XMLGregorianCalendar regDate;

    public Organization getCorrespondent() {
	return this.correspondent;
    }

    public void setCorrespondent(Organization correspondent) {
	this.correspondent = correspondent;
    }

    public String getRegNumber() {
	return this.regNumber;
    }

    public void setRegNumber(String regNumber) {
	this.regNumber = regNumber;
    }

    public XMLGregorianCalendar getRegDate() {
	return this.regDate;
    }

    public void setRegDate(XMLGregorianCalendar regDate) {
	this.regDate = regDate;
    }

}
