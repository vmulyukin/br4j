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

import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

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
 *         &lt;element ref=&quot;{}Rank&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element ref=&quot;{}Address&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element ref=&quot;{}Econtact&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element ref=&quot;{}SignDate&quot; minOccurs=&quot;0&quot;/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name=&quot;inn&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}unsignedLong&quot; /&gt;
 *       &lt;attribute name=&quot;doc_kind&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;doc_num&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;doc_org&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;doc_date&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}date&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "", propOrder = { "name", "rank", "address", "econtact",
	"signDate" })
@XmlRootElement(name = "PrivatePersonWithSign")
public class PrivatePersonWithSign extends DMSIObject {

    private PrivatePerson privatePerson;

    protected SignDate signDate;

    @XmlElement(name = "Name", required = true)
    public Name getName() {
	return getPrivatePerson().getName();
    }

    public void setName(Name value) {
	getPrivatePerson().setName(value);
    }

    @XmlElement(name = "Rank")
    public List<Rank> getRank() {
	return getPrivatePerson().getRank();
    }

    @XmlElement(name = "Address")
    public Address getAddress() {
	return getPrivatePerson().getAddress();
    }

    public void setAddress(Address value) {
	getPrivatePerson().setAddress(value);
    }

    @XmlElement(name = "Econtact")
    public List<Econtact> getEcontact() {
	return getPrivatePerson().getEcontact();
    }

    @XmlElement(name = "SignDate")
    public SignDate getSignDate() {
	return this.signDate;
    }

    public void setSignDate(SignDate value) {
	this.signDate = value;
    }

    @XmlAttribute
    @XmlSchemaType(name = "unsignedLong")
    public BigInteger getInn() {
	return getPrivatePerson().getInn();
    }

    public void setInn(BigInteger value) {
	getPrivatePerson().setInn(value);
    }

    @XmlAttribute(name = "doc_kind")
    public String getDocKind() {
	return getPrivatePerson().getDocKind();
    }

    public void setDocKind(String value) {
	getPrivatePerson().setDocKind(value);
    }

    @XmlAttribute(name = "doc_num")
    public String getDocNum() {
	return getPrivatePerson().getDocNum();
    }

    public void setDocNum(String value) {
	getPrivatePerson().setDocNum(value);
    }

    @XmlAttribute(name = "doc_org")
    public String getDocOrg() {
	return getPrivatePerson().getDocOrg();
    }

    public void setDocOrg(String value) {
	getPrivatePerson().setDocOrg(value);
    }

    @XmlAttribute(name = "doc_date")
    @XmlSchemaType(name = "date")
    public XMLGregorianCalendar getDocDate() {
	return getPrivatePerson().getDocDate();
    }

    public void setDocDate(XMLGregorianCalendar value) {
	getPrivatePerson().setDocDate(value);
    }

    public PrivatePerson getPrivatePerson() {
	if (this.privatePerson == null) {
	    this.privatePerson = new PrivatePerson();
	}
	return this.privatePerson;
    }
}
