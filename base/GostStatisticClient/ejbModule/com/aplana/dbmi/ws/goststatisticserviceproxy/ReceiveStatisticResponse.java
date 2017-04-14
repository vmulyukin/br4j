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
 * <p>Java class for receiveStatisticResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="receiveStatisticResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="uuid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="created" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="registrationDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="notifReceivedId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="notifReceivedCreated" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="notifRegId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="notifRegCreated" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="incomingCreated" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="incomingRegistered" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="incomingRegNum" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="incomingRegRejectReason" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "receiveStatisticResponse", propOrder = {
    "uuid",
    "created",
    "registrationDate",
    "status",
    "notifReceivedId",
    "notifReceivedCreated",
    "notifRegId",
    "notifRegCreated",
    "incomingCreated",
    "incomingRegistered",
    "incomingRegNum",
    "incomingRegRejectReason"
})
public class ReceiveStatisticResponse {

    protected String uuid;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar created;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar registrationDate;
    protected Long status;
    protected String notifReceivedId;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar notifReceivedCreated;
    protected String notifRegId;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar notifRegCreated;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar incomingCreated;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar incomingRegistered;
    protected String incomingRegNum;
    protected String incomingRegRejectReason;

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
     * Gets the value of the created property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCreated() {
        return created;
    }

    /**
     * Sets the value of the created property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCreated(XMLGregorianCalendar value) {
        this.created = value;
    }

    /**
     * Gets the value of the registrationDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getRegistrationDate() {
        return registrationDate;
    }

    /**
     * Sets the value of the registrationDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setRegistrationDate(XMLGregorianCalendar value) {
        this.registrationDate = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setStatus(Long value) {
        this.status = value;
    }

    /**
     * Gets the value of the notifReceivedId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNotifReceivedId() {
        return notifReceivedId;
    }

    /**
     * Sets the value of the notifReceivedId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNotifReceivedId(String value) {
        this.notifReceivedId = value;
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
     * Gets the value of the notifRegId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNotifRegId() {
        return notifRegId;
    }

    /**
     * Sets the value of the notifRegId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNotifRegId(String value) {
        this.notifRegId = value;
    }

    /**
     * Gets the value of the notifRegCreated property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getNotifRegCreated() {
        return notifRegCreated;
    }

    /**
     * Sets the value of the notifRegCreated property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setNotifRegCreated(XMLGregorianCalendar value) {
        this.notifRegCreated = value;
    }

    /**
     * Gets the value of the incomingCreated property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getIncomingCreated() {
        return incomingCreated;
    }

    /**
     * Sets the value of the incomingCreated property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setIncomingCreated(XMLGregorianCalendar value) {
        this.incomingCreated = value;
    }

    /**
     * Gets the value of the incomingRegistered property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getIncomingRegistered() {
        return incomingRegistered;
    }

    /**
     * Sets the value of the incomingRegistered property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setIncomingRegistered(XMLGregorianCalendar value) {
        this.incomingRegistered = value;
    }

    /**
     * Gets the value of the incomingRegNum property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIncomingRegNum() {
        return incomingRegNum;
    }

    /**
     * Sets the value of the incomingRegNum property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIncomingRegNum(String value) {
        this.incomingRegNum = value;
    }

    /**
     * Gets the value of the incomingRegRejectReason property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIncomingRegRejectReason() {
        return incomingRegRejectReason;
    }

    /**
     * Sets the value of the incomingRegRejectReason property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIncomingRegRejectReason(String value) {
        this.incomingRegRejectReason = value;
    }

}
