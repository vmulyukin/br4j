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

package com.aplana.soz.model.communication;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for notification complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="notification">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="time" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="foundation" type="{http://www.infpres.com/IEDMS}documentReference" minOccurs="0"/>
 *         &lt;element name="clause" type="{http://www.infpres.com/IEDMS}documentClause" minOccurs="0"/>
 *         &lt;element name="correspondent" type="{http://www.infpres.com/IEDMS}addressee" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "notification", propOrder = {
    "time",
    "foundation",
    "clause",
    "correspondent"
})
@XmlSeeAlso({
    com.aplana.soz.model.communication.MessageNotification.DocumentAccepted.class,
    com.aplana.soz.model.communication.MessageNotification.DocumentRefused.class,
    com.aplana.soz.model.communication.MessageNotification.ExecutorAssigned.class,
    com.aplana.soz.model.communication.MessageNotification.ReportPrepared.class,
    com.aplana.soz.model.communication.MessageNotification.ReportSent.class,
    com.aplana.soz.model.communication.MessageNotification.CourseChanged.class
})
public class Notification {

    @XmlElement(required = true)
    protected XMLGregorianCalendar time;
    protected DocumentReference foundation;
    protected DocumentClause clause;
    protected Addressee correspondent;

    /**
     * Gets the value of the time property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTime() {
        return time;
    }

    /**
     * Sets the value of the time property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTime(XMLGregorianCalendar value) {
        this.time = value;
    }

    /**
     * Gets the value of the foundation property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentReference }
     *     
     */
    public DocumentReference getFoundation() {
        return foundation;
    }

    /**
     * Sets the value of the foundation property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentReference }
     *     
     */
    public void setFoundation(DocumentReference value) {
        this.foundation = value;
    }

    /**
     * Gets the value of the clause property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentClause }
     *     
     */
    public DocumentClause getClause() {
        return clause;
    }

    /**
     * Sets the value of the clause property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentClause }
     *     
     */
    public void setClause(DocumentClause value) {
        this.clause = value;
    }

    /**
     * Gets the value of the correspondent property.
     * 
     * @return
     *     possible object is
     *     {@link Addressee }
     *     
     */
    public Addressee getCorrespondent() {
        return correspondent;
    }

    /**
     * Sets the value of the correspondent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Addressee }
     *     
     */
    public void setCorrespondent(Addressee value) {
        this.correspondent = value;
    }

}
