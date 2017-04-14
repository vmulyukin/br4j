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

package com.aplana.ireferent.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for WSO_PERSON complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="WSO_EXTERNALPERSON">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:IReferent.it.com}WSO_MEXTERNALPERSON">
 *       &lt;sequence>
 *         &lt;element name="EMAIL" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PHONE" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="WORKPHONE" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="FAX" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="POST" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PROXIES" type="{urn:IReferent.it.com}WSO_MPERSON"/>
 *         &lt;element name="TRUSTERS" type="{urn:IReferent.it.com}WSO_COLLECTION"/>
 *         &lt;element name="FIRSTNAME" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="MIDDLENAME" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="LASTNAME" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PHOTO" type="{urn:IReferent.it.com}WSO_COLLECTION"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WSO_EXTERNALPERSON", propOrder = {
    "email",
    "phone",
    "workPhone",
    "fax",
    "post",
    "proxies",
    "trusters",
    "firstName",
    "middleName",
    "lastName",
    "photo"
})
public class WSOExternalPerson
    extends WSOMExternalPerson
{

    @XmlElement(name = "EMAIL", required = true, nillable = true)
    protected String email;
    @XmlElement(name = "PHONE", required = true, nillable = true)
    protected String phone;
    @XmlElement(name = "WORKPHONE", required = true, nillable = true)
    protected String workPhone;
    @XmlElement(name = "FAX", required = true, nillable = true)
    protected String fax;
    @XmlElement(name = "POST", required = true, nillable = true)
    protected String post;
    @XmlElement(name = "PROXIES", required = true, nillable = true)
    protected WSOCollection proxies;
    @XmlElement(name = "TRUSTERS", required = true, nillable = true)
    protected WSOCollection trusters;
    @XmlElement(name = "FIRSTNAME", required = true, nillable = true)
    protected String firstName;
    @XmlElement(name = "MIDDLENAME", required = true, nillable = true)
    protected String middleName;
    @XmlElement(name = "LASTNAME", required = true, nillable = true)
    protected String lastName;
    @XmlElement(name = "PHOTO", required = true, nillable = true)
    protected WSOCollection photo;

    public static final String  CLASS_TYPE = "WSO_EXTERNALPERSON";

    /**
     * Gets the value of the email property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the value of the email property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setEmail(String value) {
        this.email = value;
    }

    /**
     * Gets the value of the phone property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the value of the phone property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPhone(String value) {
        this.phone = value;
    }

    /**
     * Gets the value of the workPhone property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getWorkPhone() {
        return workPhone;
    }

    /**
     * Sets the value of the workPhone property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setWorkPhone(String value) {
        this.workPhone = value;
    }

    /**
     * Gets the value of the fax property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFax() {
        return fax;
    }

    /**
     * Sets the value of the fax property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFax(String value) {
        this.fax = value;
    }

    /**
     * Gets the value of the post property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPost() {
        return post;
    }

    /**
     * Sets the value of the post property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPost(String value) {
        this.post = value;
    }

    /**
     * Gets the value of the proxies property.
     *
     * @return
     *     possible object is
     *     {@link WSOMPerson }
     *
     */
    public WSOCollection getProxies() {
        return proxies;
    }

    /**
     * Sets the value of the proxies property.
     *
     * @param value
     *     allowed object is
     *     {@link WSOMPerson }
     *
     */
    public void setProxies(WSOCollection value) {
        this.proxies = value;
    }

    /**
     * Gets the value of the trusters property.
     *
     * @return
     *     possible object is
     *     {@link WSOCollection }
     *
     */
    public WSOCollection getTrusters() {
        return trusters;
    }

    /**
     * Sets the value of the trusters property.
     *
     * @param value
     *     allowed object is
     *     {@link WSOCollection }
     *
     */
    public void setTrusters(WSOCollection value) {
        this.trusters = value;
    }

    /**
     * Gets the value of the firstName property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the value of the firstName property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFirstName(String value) {
        this.firstName = value;
    }

    /**
     * Gets the value of the middleName property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMiddleName() {
        return middleName;
    }

    /**
     * Sets the value of the middleName property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMiddleName(String value) {
        this.middleName = value;
    }

    /**
     * Gets the value of the lastName property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the value of the lastName property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setLastName(String value) {
        this.lastName = value;
    }

    /**
     * Gets the value of the photo property.
     *
     * @return
     *     possible object is
     *     {@link WSOCollection }
     *
     */
    public WSOCollection getPhoto() {
        return photo;
    }

    /**
     * Sets the value of the photo property.
     *
     * @param value
     *     allowed object is
     *     {@link WSOCollection }
     *
     */
    public void setPhoto(WSOCollection value) {
        this.photo = value;
    }

}
