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
package com.aplana.dmsi.expansion.fsin;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlRootElement(name = "RegHistoryFSIN")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "RegHistoryFSIN", propOrder = { "registrator" })
public class RegHistory {

	protected String regnumber;
	protected XMLGregorianCalendar regdate;
	protected Registrator registrator;

	@XmlAttribute
	public String getRegnumber() {
		return this.regnumber;
	}

	public void setRegnumber(String regnumber) {
		this.regnumber = regnumber;
	}

	@XmlSchemaType(name = "date")
	@XmlAttribute
	public XMLGregorianCalendar getRegdate() {
		return this.regdate;
	}

	public void setRegdate(XMLGregorianCalendar regdate) {
		this.regdate = regdate;
	}

	public Registrator getRegistrator() {
		return this.registrator;
	}

	@XmlElement(name = "Registrator")
	public void setRegistrator(Registrator registrator) {
		this.registrator = registrator;
	}

}
