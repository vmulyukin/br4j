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

import com.aplana.dmsi.types.DMSIObject;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "", propOrder = { "lastName", "firstName", "midleName",
	"depart", "zipCode", "fullRegionName", "address", "email" })
public class Author extends DMSIObject {

    protected String lastName;
    protected String firstName;
    protected String midleName;
    protected String depart;
    protected Long zipCode;
    protected String fullRegionName;
    protected String address;
    protected String email;
    protected String name;

    @XmlElement(name = "LastName", required = true)
    public String getLastName() {
	return lastName;
    }

    public void setLastName(String value) {
	this.lastName = value;
	this.name = value;
    }

    @XmlElement(name = "FirstName", required = true)
    public String getFirstName() {
	return firstName;
    }

    public void setFirstName(String value) {
	this.firstName = value;
    }

    @XmlElement(name = "MidleName", required = true)
    public String getMidleName() {
	return midleName;
    }

    public void setMidleName(String value) {
	this.midleName = value;
    }

    @XmlElement(name = "Depart", required = true)
    public String getDepart() {
	return depart;
    }

    public void setDepart(String value) {
	this.depart = value;
    }

    @XmlElement(name = "ZipCode")
    @XmlSchemaType(name = "unsignedInt")
    public Long getZipCode() {
	return zipCode;
    }

    public void setZipCode(Long value) {
	this.zipCode = value;
    }

    @XmlElement(name = "FullRegionName", required = true)
    public String getFullRegionName() {
	return fullRegionName;
    }

    public void setFullRegionName(String value) {
	this.fullRegionName = value;
    }

    @XmlElement(name = "Address", required = true)
    public String getAddress() {
	return address;
    }

    public void setAddress(String value) {
	this.address = value;
    }

    @XmlElement(name = "Email", required = true)
    public String getEmail() {
	return email;
    }

    public void setEmail(String value) {
	this.email = value;
    }
}