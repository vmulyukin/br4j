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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
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
@XmlType(name = "", propOrder = { "name", "rank", "address", "econtact" })
@XmlRootElement(name = "PrivatePerson")
public class PrivatePerson extends DMSIObject {

    protected Name name = new Name();
    protected List<Rank> rank;
    protected Address address;
    protected List<Econtact> econtact;
    protected EmailEcontact emailEcontact;
    protected WorkPhoneEcontact workphoneEcontact;
    protected FaxEcontact faxEcontact;
    protected BigInteger inn;
    protected String docKind;
    protected String docNum;
    protected String docOrg;
    protected XMLGregorianCalendar docDate;
    protected Department department;
    protected OrganizationOnly organization;

    public PrivatePerson() {
    }

    public PrivatePerson(PrivatePerson another) {
	setName(another.getName());
	getRank().addAll(another.getRank());
	setAddress(another.getAddress());
	getEcontact().addAll(another.getEcontact());
	setInn(another.getInn());
	setDocKind(another.getDocKind());
	setDocNum(another.getDocNum());
	setDocOrg(another.getDocOrg());
	setDocDate(another.getDocDate());
	setDepartament(another.getDepartament());
	setOrganization(another.getOrganization());
    }

    /**
     * Gets the {@link Department} of the current {@link PrivatePerson}.
     *
     * @return {@link Department} object.
     */
    @XmlTransient
    public Department getDepartament() {
	return department;
    }

    public void setDepartament(Department department) {
	this.department = department;
    }

    @XmlTransient
    public OrganizationOnly getOrganization() {
	return organization;
    }

    public void setOrganization(OrganizationOnly organization) {
	this.organization = organization;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link Name }
     *
     */
    @XmlElement(name = "Name", required = true)
    public Name getName() {
	return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value
     *                allowed object is {@link Name }
     *
     */
    public void setName(Name value) {
	this.name = value;
    }

    /**
     * Gets the value of the rank property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the rank property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getRank().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Rank }
     *
     *
     */
    @XmlElement(name = "Rank")
    public List<Rank> getRank() {
	if (rank == null) {
	    rank = new ArrayList<Rank>();
	}
	return this.rank;
    }

    /**
     * Gets the value of the address property.
     *
     * @return possible object is {@link Address }
     *
     */
    @XmlElement(name = "Address")
    public Address getAddress() {
	return address;
    }

    /**
     * Sets the value of the address property.
     *
     * @param value
     *                allowed object is {@link Address }
     *
     */
    public void setAddress(Address value) {
	this.address = value;
    }

    /**
     * Gets the value of the econtact property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the econtact property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getEcontact().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Econtact }
     *
     *
     */
    @XmlElement(name = "Econtact")
    public List<Econtact> getEcontact() {
	if (econtact == null) {
	    econtact = new ArrayList<Econtact>();
	}
	ListWithCallbacks<Econtact> econtactWrapper = new ListWithCallbacks<Econtact>(
		this.econtact);
	econtactWrapper.addCallback(new ListWithCallbacks.Callback<Econtact>() {
	    public void elementAdded(Econtact element) {
		if (element instanceof EmailEcontact) {
		    setEmailEcontact((EmailEcontact) element);
		} else if (element instanceof WorkPhoneEcontact) {
		    workphoneEcontact = (WorkPhoneEcontact) element;
		} else if (element instanceof FaxEcontact) {
		    faxEcontact = (FaxEcontact) element;
		}
	    }

	    public void elementRemoved(Object element) {
		if (element instanceof EmailEcontact) {
		    setEmailEcontact(null);
		} else if (element instanceof WorkPhoneEcontact) {
		    workphoneEcontact = null;
		} else if (element instanceof FaxEcontact) {
		    faxEcontact = null;
		}
	    }
	});
	return econtactWrapper;
    }

    /**
     * Gets the value of the inn property.
     *
     * @return possible object is {@link BigInteger }
     *
     */
    @XmlAttribute
    @XmlSchemaType(name = "unsignedLong")
    public BigInteger getInn() {
	return inn;
    }

    /**
     * Sets the value of the inn property.
     *
     * @param value
     *                allowed object is {@link BigInteger }
     *
     */
    public void setInn(BigInteger value) {
	this.inn = value;
    }

    /**
     * Gets the value of the docKind property.
     *
     * @return possible object is {@link String }
     *
     */
    @XmlAttribute(name = "doc_kind")
    public String getDocKind() {
	return docKind;
    }

    /**
     * Sets the value of the docKind property.
     *
     * @param value
     *                allowed object is {@link String }
     *
     */
    public void setDocKind(String value) {
	this.docKind = value;
    }

    /**
     * Gets the value of the docNum property.
     *
     * @return possible object is {@link String }
     *
     */
    @XmlAttribute(name = "doc_num")
    public String getDocNum() {
	return docNum;
    }

    /**
     * Sets the value of the docNum property.
     *
     * @param value
     *                allowed object is {@link String }
     *
     */
    public void setDocNum(String value) {
	this.docNum = value;
    }

    /**
     * Gets the value of the docOrg property.
     *
     * @return possible object is {@link String }
     *
     */
    @XmlAttribute(name = "doc_org")
    public String getDocOrg() {
	return docOrg;
    }

    /**
     * Sets the value of the docOrg property.
     *
     * @param value
     *                allowed object is {@link String }
     *
     */
    public void setDocOrg(String value) {
	this.docOrg = value;
    }

    /**
     * Gets the value of the docDate property.
     *
     * @return possible object is {@link XMLGregorianCalendar }
     *
     */
    @XmlAttribute(name = "doc_date")
    @XmlSchemaType(name = "date")
    public XMLGregorianCalendar getDocDate() {
	return docDate;
    }

    /**
     * Sets the value of the docDate property.
     *
     * @param value
     *                allowed object is {@link XMLGregorianCalendar }
     *
     */
    public void setDocDate(XMLGregorianCalendar value) {
	this.docDate = value;
    }

    @XmlTransient
    protected EmailEcontact getEmailEcontact() {
	return this.emailEcontact;
    }

    protected void setEmailEcontact(EmailEcontact emailEcontact) {
	this.emailEcontact = emailEcontact;
    }

}
