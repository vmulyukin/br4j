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

import java.util.ArrayList;
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
	"econtact" })
@XmlRootElement(name = "OfficialPerson")
public class OfficialPerson extends DMSIObject {

    protected Name name = new Name();

    protected List<Official> official;

    protected List<Rank> rank;

    protected Address address;

    protected List<Econtact> econtact;
    protected EmailEcontact emailEcontact;
    protected WorkPhoneEcontact workphoneEcontact;
    protected FaxEcontact faxEcontact;
    protected Official mainOfficial;
    protected OrganizationOnly organization;

    public OfficialPerson() {
    }

    public OfficialPerson(OfficialPerson another) {
	setName(another.getName());
	getOfficial().addAll(another.getOfficial());
	getRank().addAll(another.getRank());
	setAddress(another.getAddress());
	getEcontact().addAll(another.getEcontact());
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
     * Gets the value of the official property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the official property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getOfficial().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Official }
     *
     *
     */
    @XmlElement(name = "Official")
    public List<Official> getOfficial() {
	if (official == null) {
	    official = new ArrayList<Official>();
	}
	ListWithCallbacks<Official> officialWithCallbacks = new ListWithCallbacks<Official>(
		official);
	officialWithCallbacks
		.addCallback(new ListWithCallbacks.Callback<Official>() {
		    public void elementAdded(Official element) {
			mainOfficial = element;
		    }

		    public void elementRemoved(Object element) {
			if (element != null && element.equals(mainOfficial)) {
			    mainOfficial = null;
			}
		    }
		});
	return officialWithCallbacks;
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

    @XmlTransient
    public OrganizationOnly getOrganization() {
	return this.organization;
    }

    protected void setOrganization(OrganizationOnly organization) {
	this.organization = organization;
    }
}
