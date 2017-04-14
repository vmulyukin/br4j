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
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ExpansionType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name=&quot;ExpansionType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref=&quot;{}Econtact&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element name=&quot;AnyData&quot; minOccurs=&quot;0&quot;&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *                 &lt;sequence&gt;
 *                   &lt;any/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name=&quot;organization&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;exp_ver&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExpansionType", propOrder = { "econtact", "any" })
public class ExpansionType extends DMSIObject {

    @XmlElement(name = "Econtact")
    protected List<Econtact> econtact;
	@XmlAnyElement(lax = true)
	protected List<Object> any;
    @XmlAttribute(required = true)
    protected String organization;
    @XmlAttribute(name = "exp_ver", required = true)
    protected String expVer;

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
    public List<Econtact> getEcontact() {
        if (econtact == null) {
            econtact = new ArrayList<Econtact>();
        }
        return this.econtact;
    }

    /**
     * Gets the value of the organization property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getOrganization() {
        return organization;
    }

    /**
     * Sets the value of the organization property.
     *
     * @param value
     *                allowed object is {@link String }
     *
     */
    public void setOrganization(String value) {
        this.organization = value;
    }

    /**
     * Gets the value of the expVer property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getExpVer() {
        return expVer;
    }

    /**
     * Sets the value of the expVer property.
     *
     * @param value
     *                allowed object is {@link String }
     *
     */
    public void setExpVer(String value) {
        this.expVer = value;
    }

        public List<Object> getAny() {
            if (any == null) {
                any = new ArrayList<Object>();
            }
            return this.any;
        }
    }
