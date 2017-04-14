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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

import com.aplana.dmsi.types.DMSIObject;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "", propOrder = { "typePacket", "typeInfo", "date",
	"requestID", "author", "info" })
@XmlRootElement(name = "XMLPacket")
public class XMLPacket extends DMSIObject {

    protected TypePacketEnumType typePacket;
    protected TypeInfoEnumType typeInfo;
    protected XMLGregorianCalendar date;
    protected String requestID;
    protected Author author;
    protected Info info;

    public XMLPacket() {
    }

    public XMLPacket(XMLPacket anotherPacket) {
	setId(anotherPacket.getId());
	setTypePacket(anotherPacket.getTypePacket());
	setTypeInfo(anotherPacket.getTypeInfo());
	setDate(anotherPacket.getDate());
	setRequestID(anotherPacket.getRequestID());
	setAuthor(anotherPacket.getAuthor());
	setInfo(anotherPacket.getInfo());
    }

    @XmlElement(name = "TypePacket", required = true)
    public TypePacketEnumType getTypePacket() {
	return typePacket;
    }

    public void setTypePacket(TypePacketEnumType value) {
	this.typePacket = value;
    }

    @XmlElement(name = "TypeInfo", required = true)
    public TypeInfoEnumType getTypeInfo() {
	return typeInfo;
    }

    public void setTypeInfo(TypeInfoEnumType value) {
	this.typeInfo = value;
    }

    @XmlElement(name = "Date", required = true)
    @XmlSchemaType(name = "dateTime")
    public XMLGregorianCalendar getDate() {
	return date;
    }

    public void setDate(XMLGregorianCalendar value) {
	this.date = value;
    }

    @XmlElement(name = "RequestID", required = true)
    public String getRequestID() {
	return requestID;
    }

    public void setRequestID(String value) {
	this.requestID = value;
    }

    @XmlElement(name = "Author", required = true)
    public Author getAuthor() {
	return author;
    }

    public void setAuthor(Author value) {
	this.author = value;
	updateInfoAuthor();
    }

    @XmlElement(name = "Info", required = true)
    public Info getInfo() {
	return info;
    }

    public void setInfo(Info value) {
	this.info = value;
	updateInfoAuthor();
    }

    private void updateInfoAuthor() {
	if (this.author != null && this.info != null) {
	    this.info.setAuthor(this.author);
	}
    }
}
