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
package com.aplana.medo.types.cardInfo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "firstName", "middleName", "lastName",
	"zipCode", "address", "addressExt", "email", "orgReplyEmailMedo",
	"departGUID", "regionGUID", "rubricList", "corrRequestID",
	"corrResponseID", "cardType" })
@XmlRootElement(name = "XMLCardInfo")
public class XMLCardInfo {

    @XmlElement(name = "FirstName")
    private String firstName;
    @XmlElement(name = "MiddleName")
    private String middleName;
    @XmlElement(name = "LastName")
    private String lastName;
    @XmlElement(name = "ZipCode")
    private Long zipCode;
    @XmlElement(name = "Address")
    private Address address;
    @XmlElement(name = "AddressExt")
    private AddressExt addressExt;
    @XmlElement(name = "Email")
    private String email;
    @XmlElement(name = "OrgReplyEmailMedo")
    private String orgReplyEmailMedo;
    @XmlElement(name = "DepartGUID")
    private String departGUID;
    @XmlElement(name = "RegionGUID")
    private String regionGUID;
    @XmlElementWrapper(name = "RubricList")
    @XmlElement(name = "Rubric")
    private List<Rubric> rubricList;
    @XmlElement(name = "CorrRequestID")
    private String corrRequestID;
    @XmlElement(name = "CorrResponseID")
    private String corrResponseID;
    @XmlElement(name = "CardType")
    private String cardType;

    public String getFirstName() {
	return this.firstName;
    }

    public void setFirstName(String firstName) {
	this.firstName = firstName;
    }

    public String getMiddleName() {
	return this.middleName;
    }

    public void setMiddleName(String middleName) {
	this.middleName = middleName;
    }

    public String getLastName() {
	return this.lastName;
    }

    public void setLastName(String lastName) {
	this.lastName = lastName;
    }

    public Long getZipCode() {
	return this.zipCode;
    }

    public void setZipCode(Long zipCode) {
	this.zipCode = zipCode;
    }

    public Address getAddress() {
	return this.address;
    }

    public void setAddress(Address address) {
	this.address = address;
    }

    public AddressExt getAddressExt() {
	return this.addressExt;
    }

    public void setAddressExt(AddressExt addressExt) {
	this.addressExt = addressExt;
    }

    public String getEmail() {
	return this.email;
    }

    public void setEmail(String email) {
	this.email = email;
    }

    public String getOrgReplyEmailMedo() {
	return this.orgReplyEmailMedo;
    }

    public void setOrgReplyEmailMedo(String orgReplyEmailMedo) {
	this.orgReplyEmailMedo = orgReplyEmailMedo;
    }

    public String getDepartGUID() {
	return this.departGUID;
    }

    public void setDepartGUID(String departGUID) {
	this.departGUID = departGUID;
    }

    public String getRegionGUID() {
	return this.regionGUID;
    }

    public void setRegionGUID(String regionGUID) {
	this.regionGUID = regionGUID;
    }

    public String getCorrRequestID() {
	return this.corrRequestID;
    }

    public void setCorrRequestID(String corrRequestID) {
	this.corrRequestID = corrRequestID;
    }

    public String getCorrResponseID() {
	return this.corrResponseID;
    }

    public void setCorrResponseID(String corrResponseID) {
	this.corrResponseID = corrResponseID;
    }

    public String getCardType() {
	return this.cardType;
    }

    public void setCardType(String cardType) {
	this.cardType = cardType;
    }

    public List<Rubric> getRubricList() {
	if (this.rubricList == null) {
	    this.rubricList = new ArrayList<Rubric>();
	}
	return this.rubricList;
    }

}
