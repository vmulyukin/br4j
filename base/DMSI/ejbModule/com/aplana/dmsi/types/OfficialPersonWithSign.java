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
package com.aplana.dmsi.types;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref=&quot;{}Name&quot;/&gt;
 *         &lt;element ref=&quot;{}Official&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element ref=&quot;{}Rank&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element ref=&quot;{}Address&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element ref=&quot;{}Econtact&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element ref=&quot;{}SignDate&quot; minOccurs=&quot;0&quot;/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "", propOrder = { "name", "official", "rank", "address",
	"econtact", "signDate" })
@XmlRootElement(name = "OfficialPersonWithSign")
public class OfficialPersonWithSign extends DMSIObject {

    private OfficialPerson officialPerson;
    protected SignDate signDate;
    protected OrganizationOnly organization;

    public OfficialPersonWithSign() {
    }

    public OfficialPersonWithSign(OfficialPersonWithSign another) {
	this.officialPerson = another.officialPerson;
	this.signDate = another.signDate;
	this.organization = another.organization;
    }

    @XmlElement(name = "Name", required = true)
    public Name getName() {
	return getOfficialPerson().getName();
    }

    public void setName(Name value) {
	getOfficialPerson().setName(value);
    }

    @XmlElement(name = "Official")
    public List<Official> getOfficial() {
	return getOfficialPerson().getOfficial();
    }

    @XmlElement(name = "Rank")
    public List<Rank> getRank() {
	return getOfficialPerson().getRank();
    }

    @XmlElement(name = "Address")
    public Address getAddress() {
	return getOfficialPerson().getAddress();
    }

    public void setAddress(Address value) {
	getOfficialPerson().setAddress(value);
    }

    @XmlElement(name = "Econtact")
    public List<Econtact> getEcontact() {
	return getOfficialPerson().getEcontact();
    }

    @XmlElement(name = "SignDate")
    public SignDate getSignDate() {
	return signDate;
    }

    public void setSignDate(SignDate value) {
	this.signDate = value;
    }

    @XmlTransient
    public OrganizationOnly getOrganization() {
	return this.organization;
    }

    public void setOrganization(OrganizationOnly organization) {
	this.organization = organization;
    }

    @XmlTransient
    protected OfficialPerson getOfficialPerson() {
	if (this.officialPerson == null) {
	    this.officialPerson = new OfficialPerson();
	}
	return this.officialPerson;
    }

    protected void setOfficialPerson(OfficialPerson officialPerson) {
        this.officialPerson = officialPerson;
    }
}
