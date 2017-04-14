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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for messageNotification complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="messageNotification">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="documentAccepted">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;extension base="{http://www.infpres.com/IEDMS}notification">
 *                   &lt;sequence>
 *                     &lt;element name="num" type="{http://www.infpres.com/IEDMS}documentNumber"/>
 *                   &lt;/sequence>
 *                 &lt;/extension>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="documentRefused">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;extension base="{http://www.infpres.com/IEDMS}notification">
 *                   &lt;sequence>
 *                     &lt;element name="reason" type="{http://www.infpres.com/IEDMS}reasonForRejection"/>
 *                   &lt;/sequence>
 *                 &lt;/extension>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="executorAssigned">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;extension base="{http://www.infpres.com/IEDMS}notification">
 *                   &lt;sequence>
 *                     &lt;element name="secretary" type="{http://www.infpres.com/IEDMS}addressee"/>
 *                     &lt;element name="manager" type="{http://www.infpres.com/IEDMS}addressee"/>
 *                     &lt;element name="executor" type="{http://www.infpres.com/IEDMS}addressee"/>
 *                   &lt;/sequence>
 *                 &lt;/extension>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="reportPrepared">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;extension base="{http://www.infpres.com/IEDMS}notification">
 *                   &lt;sequence>
 *                     &lt;element name="signatory" type="{http://www.infpres.com/IEDMS}signatory"/>
 *                   &lt;/sequence>
 *                 &lt;/extension>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="reportSent">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;extension base="{http://www.infpres.com/IEDMS}notification">
 *                   &lt;sequence>
 *                     &lt;element name="report" type="{http://www.infpres.com/IEDMS}documentReference"/>
 *                   &lt;/sequence>
 *                 &lt;/extension>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="courseChanged">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;extension base="{http://www.infpres.com/IEDMS}notification">
 *                   &lt;sequence>
 *                     &lt;element name="courseText" type="{http://www.infpres.com/IEDMS}shortText"/>
 *                     &lt;element name="reference" type="{http://www.infpres.com/IEDMS}documentReference" minOccurs="0"/>
 *                   &lt;/sequence>
 *                 &lt;/extension>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *         &lt;/choice>
 *         &lt;element name="comment" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="2047"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="type" use="required" type="{http://www.infpres.com/IEDMS}notificationType" />
 *       &lt;attribute name="uid" use="required" type="{http://www.infpres.com/IEDMS}globalUniqueIdentifier" />
 *       &lt;attribute name="id" type="{http://www.infpres.com/IEDMS}identityValue" />
 *       &lt;attribute name="cookie" type="{http://www.infpres.com/IEDMS}anyValue" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "messageNotification", propOrder = {
    "documentAccepted",
    "documentRefused",
    "executorAssigned",
    "reportPrepared",
    "reportSent",
    "courseChanged",
    "comment"
})
public class MessageNotification {

    protected MessageNotification.DocumentAccepted documentAccepted;
    protected MessageNotification.DocumentRefused documentRefused;
    protected MessageNotification.ExecutorAssigned executorAssigned;
    protected MessageNotification.ReportPrepared reportPrepared;
    protected MessageNotification.ReportSent reportSent;
    protected MessageNotification.CourseChanged courseChanged;
    protected String comment;
    @XmlAttribute(namespace = "http://www.infpres.com/IEDMS", required = true)
    protected NotificationType type;
    @XmlAttribute(namespace = "http://www.infpres.com/IEDMS", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String uid;
    @XmlAttribute(namespace = "http://www.infpres.com/IEDMS")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String id;
    @XmlAttribute(namespace = "http://www.infpres.com/IEDMS")
    protected String cookie;

    /**
     * Gets the value of the documentAccepted property.
     * 
     * @return
     *     possible object is
     *     {@link MessageNotification.DocumentAccepted }
     *     
     */
    public MessageNotification.DocumentAccepted getDocumentAccepted() {
        return documentAccepted;
    }

    /**
     * Sets the value of the documentAccepted property.
     * 
     * @param value
     *     allowed object is
     *     {@link MessageNotification.DocumentAccepted }
     *     
     */
    public void setDocumentAccepted(MessageNotification.DocumentAccepted value) {
        this.documentAccepted = value;
    }

    /**
     * Gets the value of the documentRefused property.
     * 
     * @return
     *     possible object is
     *     {@link MessageNotification.DocumentRefused }
     *     
     */
    public MessageNotification.DocumentRefused getDocumentRefused() {
        return documentRefused;
    }

    /**
     * Sets the value of the documentRefused property.
     * 
     * @param value
     *     allowed object is
     *     {@link MessageNotification.DocumentRefused }
     *     
     */
    public void setDocumentRefused(MessageNotification.DocumentRefused value) {
        this.documentRefused = value;
    }

    /**
     * Gets the value of the executorAssigned property.
     * 
     * @return
     *     possible object is
     *     {@link MessageNotification.ExecutorAssigned }
     *     
     */
    public MessageNotification.ExecutorAssigned getExecutorAssigned() {
        return executorAssigned;
    }

    /**
     * Sets the value of the executorAssigned property.
     * 
     * @param value
     *     allowed object is
     *     {@link MessageNotification.ExecutorAssigned }
     *     
     */
    public void setExecutorAssigned(MessageNotification.ExecutorAssigned value) {
        this.executorAssigned = value;
    }

    /**
     * Gets the value of the reportPrepared property.
     * 
     * @return
     *     possible object is
     *     {@link MessageNotification.ReportPrepared }
     *     
     */
    public MessageNotification.ReportPrepared getReportPrepared() {
        return reportPrepared;
    }

    /**
     * Sets the value of the reportPrepared property.
     * 
     * @param value
     *     allowed object is
     *     {@link MessageNotification.ReportPrepared }
     *     
     */
    public void setReportPrepared(MessageNotification.ReportPrepared value) {
        this.reportPrepared = value;
    }

    /**
     * Gets the value of the reportSent property.
     * 
     * @return
     *     possible object is
     *     {@link MessageNotification.ReportSent }
     *     
     */
    public MessageNotification.ReportSent getReportSent() {
        return reportSent;
    }

    /**
     * Sets the value of the reportSent property.
     * 
     * @param value
     *     allowed object is
     *     {@link MessageNotification.ReportSent }
     *     
     */
    public void setReportSent(MessageNotification.ReportSent value) {
        this.reportSent = value;
    }

    /**
     * Gets the value of the courseChanged property.
     * 
     * @return
     *     possible object is
     *     {@link MessageNotification.CourseChanged }
     *     
     */
    public MessageNotification.CourseChanged getCourseChanged() {
        return courseChanged;
    }

    /**
     * Sets the value of the courseChanged property.
     * 
     * @param value
     *     allowed object is
     *     {@link MessageNotification.CourseChanged }
     *     
     */
    public void setCourseChanged(MessageNotification.CourseChanged value) {
        this.courseChanged = value;
    }

    /**
     * Gets the value of the comment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComment(String value) {
        this.comment = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link NotificationType }
     *     
     */
    public NotificationType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link NotificationType }
     *     
     */
    public void setType(NotificationType value) {
        this.type = value;
    }

    /**
     * Gets the value of the uid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets the value of the uid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUid(String value) {
        this.uid = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the cookie property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCookie() {
        return cookie;
    }

    /**
     * Sets the value of the cookie property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCookie(String value) {
        this.cookie = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://www.infpres.com/IEDMS}notification">
     *       &lt;sequence>
     *         &lt;element name="courseText" type="{http://www.infpres.com/IEDMS}shortText"/>
     *         &lt;element name="reference" type="{http://www.infpres.com/IEDMS}documentReference" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "courseText",
        "reference"
    })
    public static class CourseChanged
        extends Notification
    {

        @XmlElement(required = true)
        protected String courseText;
        protected DocumentReference reference;

        /**
         * Gets the value of the courseText property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCourseText() {
            return courseText;
        }

        /**
         * Sets the value of the courseText property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCourseText(String value) {
            this.courseText = value;
        }

        /**
         * Gets the value of the reference property.
         * 
         * @return
         *     possible object is
         *     {@link DocumentReference }
         *     
         */
        public DocumentReference getReference() {
            return reference;
        }

        /**
         * Sets the value of the reference property.
         * 
         * @param value
         *     allowed object is
         *     {@link DocumentReference }
         *     
         */
        public void setReference(DocumentReference value) {
            this.reference = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://www.infpres.com/IEDMS}notification">
     *       &lt;sequence>
     *         &lt;element name="num" type="{http://www.infpres.com/IEDMS}documentNumber"/>
     *       &lt;/sequence>
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "num"
    })
    public static class DocumentAccepted
        extends Notification
    {

        @XmlElement(required = true)
        protected DocumentNumber num;

        /**
         * Gets the value of the num property.
         * 
         * @return
         *     possible object is
         *     {@link DocumentNumber }
         *     
         */
        public DocumentNumber getNum() {
            return num;
        }

        /**
         * Sets the value of the num property.
         * 
         * @param value
         *     allowed object is
         *     {@link DocumentNumber }
         *     
         */
        public void setNum(DocumentNumber value) {
            this.num = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://www.infpres.com/IEDMS}notification">
     *       &lt;sequence>
     *         &lt;element name="reason" type="{http://www.infpres.com/IEDMS}reasonForRejection"/>
     *       &lt;/sequence>
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "reason"
    })
    public static class DocumentRefused
        extends Notification
    {

        @XmlElement(required = true)
        protected ReasonForRejection reason;

        /**
         * Gets the value of the reason property.
         * 
         * @return
         *     possible object is
         *     {@link ReasonForRejection }
         *     
         */
        public ReasonForRejection getReason() {
            return reason;
        }

        /**
         * Sets the value of the reason property.
         * 
         * @param value
         *     allowed object is
         *     {@link ReasonForRejection }
         *     
         */
        public void setReason(ReasonForRejection value) {
            this.reason = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://www.infpres.com/IEDMS}notification">
     *       &lt;sequence>
     *         &lt;element name="secretary" type="{http://www.infpres.com/IEDMS}addressee"/>
     *         &lt;element name="manager" type="{http://www.infpres.com/IEDMS}addressee"/>
     *         &lt;element name="executor" type="{http://www.infpres.com/IEDMS}addressee"/>
     *       &lt;/sequence>
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "secretary",
        "manager",
        "executor"
    })
    public static class ExecutorAssigned
        extends Notification
    {

        @XmlElement(required = true)
        protected Addressee secretary;
        @XmlElement(required = true)
        protected Addressee manager;
        @XmlElement(required = true)
        protected Addressee executor;

        /**
         * Gets the value of the secretary property.
         * 
         * @return
         *     possible object is
         *     {@link Addressee }
         *     
         */
        public Addressee getSecretary() {
            return secretary;
        }

        /**
         * Sets the value of the secretary property.
         * 
         * @param value
         *     allowed object is
         *     {@link Addressee }
         *     
         */
        public void setSecretary(Addressee value) {
            this.secretary = value;
        }

        /**
         * Gets the value of the manager property.
         * 
         * @return
         *     possible object is
         *     {@link Addressee }
         *     
         */
        public Addressee getManager() {
            return manager;
        }

        /**
         * Sets the value of the manager property.
         * 
         * @param value
         *     allowed object is
         *     {@link Addressee }
         *     
         */
        public void setManager(Addressee value) {
            this.manager = value;
        }

        /**
         * Gets the value of the executor property.
         * 
         * @return
         *     possible object is
         *     {@link Addressee }
         *     
         */
        public Addressee getExecutor() {
            return executor;
        }

        /**
         * Sets the value of the executor property.
         * 
         * @param value
         *     allowed object is
         *     {@link Addressee }
         *     
         */
        public void setExecutor(Addressee value) {
            this.executor = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://www.infpres.com/IEDMS}notification">
     *       &lt;sequence>
     *         &lt;element name="signatory" type="{http://www.infpres.com/IEDMS}signatory"/>
     *       &lt;/sequence>
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "signatory"
    })
    public static class ReportPrepared
        extends Notification
    {

        @XmlElement(required = true)
        protected Signatory signatory;

        /**
         * Gets the value of the signatory property.
         * 
         * @return
         *     possible object is
         *     {@link Signatory }
         *     
         */
        public Signatory getSignatory() {
            return signatory;
        }

        /**
         * Sets the value of the signatory property.
         * 
         * @param value
         *     allowed object is
         *     {@link Signatory }
         *     
         */
        public void setSignatory(Signatory value) {
            this.signatory = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://www.infpres.com/IEDMS}notification">
     *       &lt;sequence>
     *         &lt;element name="report" type="{http://www.infpres.com/IEDMS}documentReference"/>
     *       &lt;/sequence>
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "report"
    })
    public static class ReportSent
        extends Notification
    {

        @XmlElement(required = true)
        protected DocumentReference report;

        /**
         * Gets the value of the report property.
         * 
         * @return
         *     possible object is
         *     {@link DocumentReference }
         *     
         */
        public DocumentReference getReport() {
            return report;
        }

        /**
         * Sets the value of the report property.
         * 
         * @param value
         *     allowed object is
         *     {@link DocumentReference }
         *     
         */
        public void setReport(DocumentReference value) {
            this.report = value;
        }

    }

}
