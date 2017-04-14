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
 *         &lt;element ref=&quot;{}Address&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element ref=&quot;{}Econtact&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name=&quot;organization_string&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;fullname&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;shortname&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;ownership&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;ogrn&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}unsignedLong&quot; /&gt;
 *       &lt;attribute name=&quot;inn&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}unsignedLong&quot; /&gt;
 *       &lt;attribute name=&quot;kpp&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}unsignedLong&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "", propOrder = { "address", "econtact" })
@XmlRootElement(name = "OrganizationOnly")
public class OrganizationOnly extends DMSIObject {

    protected Address address;
    protected List<Econtact> econtact;
    protected EmailEcontact emailEcontact;
    protected WorkPhoneEcontact workphoneEcontact;
    protected FaxEcontact faxEcontact;
    protected String organizationString = "";
    protected String fullname;
    protected String shortname;
    protected String ownership;
    protected BigInteger ogrn;
    protected BigInteger inn;
    protected BigInteger kpp;
    protected String organizationid;
    protected byte isExternal = 1;

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
		    emailEcontact = (EmailEcontact) element;
		} else if (element instanceof WorkPhoneEcontact) {
		    workphoneEcontact = (WorkPhoneEcontact) element;
		} else if (element instanceof FaxEcontact) {
		    faxEcontact = (FaxEcontact) element;
		}
	    }

	    public void elementRemoved(Object element) {
		if (element instanceof EmailEcontact) {
		    emailEcontact = null;
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
     * Gets the value of the organizationString property.
     *
     * @return possible object is {@link String }
     *
     */
    @XmlAttribute(name = "organization_string", required = true)
    public String getOrganizationString() {
	return organizationString;
    }

    /**
     * Sets the value of the organizationString property.
     *
     * @param value
     *                allowed object is {@link String }
     *
     */
    public void setOrganizationString(String value) {
	this.organizationString = value;
    }

    /**
     * Gets the value of the fullname property.
     *
     * @return possible object is {@link String }
     *
     */
    @XmlAttribute
    public String getFullname() {
	return fullname;
    }

    /**
     * Sets the value of the fullname property.
     *
     * @param value
     *                allowed object is {@link String }
     *
     */
    public void setFullname(String value) {
	this.fullname = value;
    }

    /**
     * Gets the value of the shortname property.
     *
     * @return possible object is {@link String }
     *
     */
    @XmlAttribute
    public String getShortname() {
	return shortname;
    }

    /**
     * Sets the value of the shortname property.
     *
     * @param value
     *                allowed object is {@link String }
     *
     */
    public void setShortname(String value) {
	this.shortname = value;
	if(this.fullname==null || this.fullname.isEmpty()){
		this.fullname = value;
	}
    }

    /**
     * Gets the value of the ownership property.
     *
     * @return possible object is {@link String }
     *
     */
    @XmlAttribute
    public String getOwnership() {
	return ownership;
    }

    /**
     * Sets the value of the ownership property.
     *
     * @param value
     *                allowed object is {@link String }
     *
     */
    public void setOwnership(String value) {
	this.ownership = value;
    }

    /**
     * Gets the value of the ogrn property.
     *
     * @return possible object is {@link BigInteger }
     *
     */
    @XmlAttribute
    @XmlSchemaType(name = "unsignedLong")
    public BigInteger getOgrn() {
	return ogrn;
    }

    /**
     * Sets the value of the ogrn property.
     *
     * @param value
     *                allowed object is {@link BigInteger }
     *
     */
    public void setOgrn(BigInteger value) {
	this.ogrn = value;
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
     * Gets the value of the kpp property.
     *
     * @return possible object is {@link BigInteger }
     *
     */
    @XmlAttribute
    @XmlSchemaType(name = "unsignedLong")
    public BigInteger getKpp() {
	return kpp;
    }

    /**
     * Sets the value of the kpp property.
     *
     * @param value
     *                allowed object is {@link BigInteger }
     *
     */
    public void setKpp(BigInteger value) {
	this.kpp = value;
    }

    @XmlTransient
    public String getOrganizationid() {
	return organizationid;
    }

    public void setOrganizationid(String organizationid) {
	this.organizationid = organizationid;
    }
}
