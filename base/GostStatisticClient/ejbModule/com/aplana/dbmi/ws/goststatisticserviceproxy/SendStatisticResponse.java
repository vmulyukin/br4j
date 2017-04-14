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

package com.aplana.dbmi.ws.goststatisticserviceproxy;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for sendStatisticResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sendStatisticResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="uuid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="elmStatus" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="elmStatusName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="basedocTemplate" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="basedocStatus" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="senderOrgFullName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="destOrgFullName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="basedocRegDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="basedocRegNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="gostMessageCreateTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="defaultOrgFullName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="elmCreatedDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="notifReceivedCreated" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="notifRegisteredCreated" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sendStatisticResponse", propOrder = {
    "uuid",
    "elmStatus",
    "elmStatusName",
    "basedocTemplate",
    "basedocStatus",
    "senderOrgFullName",
    "destOrgFullName",
    "basedocRegDate",
    "basedocRegNumber",
    "gostMessageCreateTime",
    "defaultOrgFullName",
    "elmCreatedDate",
    "notifReceivedCreated",
    "notifRegisteredCreated"
})
public class SendStatisticResponse {

    protected String uuid;
    protected Long elmStatus;
    protected String elmStatusName;
    protected Long basedocTemplate;
    protected Long basedocStatus;
    protected String senderOrgFullName;
    protected String destOrgFullName;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar basedocRegDate;
    protected String basedocRegNumber;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar gostMessageCreateTime;
    protected String defaultOrgFullName;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar elmCreatedDate;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar notifReceivedCreated;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar notifRegisteredCreated;

    /**
     * Gets the value of the uuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the value of the uuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUuid(String value) {
        this.uuid = value;
    }

    /**
     * Gets the value of the elmStatus property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getElmStatus() {
        return elmStatus;
    }

    /**
     * Sets the value of the elmStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setElmStatus(Long value) {
        this.elmStatus = value;
    }

    /**
     * Gets the value of the elmStatusName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getElmStatusName() {
        return elmStatusName;
    }

    /**
     * Sets the value of the elmStatusName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setElmStatusName(String value) {
        this.elmStatusName = value;
    }

    /**
     * Gets the value of the basedocTemplate property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getBasedocTemplate() {
        return basedocTemplate;
    }

    /**
     * Sets the value of the basedocTemplate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setBasedocTemplate(Long value) {
        this.basedocTemplate = value;
    }

    /**
     * Gets the value of the basedocStatus property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getBasedocStatus() {
        return basedocStatus;
    }

    /**
     * Sets the value of the basedocStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setBasedocStatus(Long value) {
        this.basedocStatus = value;
    }

    /**
     * Gets the value of the senderOrgFullName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSenderOrgFullName() {
        return senderOrgFullName;
    }

    /**
     * Sets the value of the senderOrgFullName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSenderOrgFullName(String value) {
        this.senderOrgFullName = value;
    }

    /**
     * Gets the value of the destOrgFullName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDestOrgFullName() {
        return destOrgFullName;
    }

    /**
     * Sets the value of the destOrgFullName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDestOrgFullName(String value) {
        this.destOrgFullName = value;
    }

    /**
     * Gets the value of the basedocRegDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getBasedocRegDate() {
        return basedocRegDate;
    }

    /**
     * Sets the value of the basedocRegDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setBasedocRegDate(XMLGregorianCalendar value) {
        this.basedocRegDate = value;
    }

    /**
     * Gets the value of the basedocRegNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBasedocRegNumber() {
        return basedocRegNumber;
    }

    /**
     * Sets the value of the basedocRegNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBasedocRegNumber(String value) {
        this.basedocRegNumber = value;
    }

    /**
     * Gets the value of the gostMessageCreateTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getGostMessageCreateTime() {
        return gostMessageCreateTime;
    }

    /**
     * Sets the value of the gostMessageCreateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setGostMessageCreateTime(XMLGregorianCalendar value) {
        this.gostMessageCreateTime = value;
    }

    /**
     * Gets the value of the defaultOrgFullName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefaultOrgFullName() {
        return defaultOrgFullName;
    }

    /**
     * Sets the value of the defaultOrgFullName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefaultOrgFullName(String value) {
        this.defaultOrgFullName = value;
    }

    /**
     * Gets the value of the elmCreatedDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getElmCreatedDate() {
        return elmCreatedDate;
    }

    /**
     * Sets the value of the elmCreatedDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setElmCreatedDate(XMLGregorianCalendar value) {
        this.elmCreatedDate = value;
    }

    /**
     * Gets the value of the notifReceivedCreated property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getNotifReceivedCreated() {
        return notifReceivedCreated;
    }

    /**
     * Sets the value of the notifReceivedCreated property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setNotifReceivedCreated(XMLGregorianCalendar value) {
        this.notifReceivedCreated = value;
    }

    /**
     * Gets the value of the notifRegisteredCreated property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getNotifRegisteredCreated() {
        return notifRegisteredCreated;
    }

    /**
     * Sets the value of the notifRegisteredCreated property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setNotifRegisteredCreated(XMLGregorianCalendar value) {
        this.notifRegisteredCreated = value;
    }

}
