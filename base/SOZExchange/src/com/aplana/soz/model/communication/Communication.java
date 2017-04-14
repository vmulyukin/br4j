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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="header">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="source" type="{http://www.infpres.com/IEDMS}communicationPartner"/>
 *                   &lt;element name="operator" type="{http://www.infpres.com/IEDMS}qualifiedValue" minOccurs="0"/>
 *                   &lt;element name="comment" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;maxLength value="2047"/>
 *                         &lt;minLength value="1"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="type" use="required" type="{http://www.infpres.com/IEDMS}messageType" />
 *                 &lt;attribute name="uid" type="{http://www.infpres.com/IEDMS}globalUniqueIdentifier" />
 *                 &lt;attribute name="created" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *                 &lt;attribute name="cookie" type="{http://www.infpres.com/IEDMS}anyValue" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;choice>
 *           &lt;sequence>
 *             &lt;element name="document" type="{http://www.infpres.com/IEDMS}document"/>
 *             &lt;element name="files">
 *               &lt;complexType>
 *                 &lt;complexContent>
 *                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                     &lt;sequence>
 *                       &lt;element name="file" type="{http://www.infpres.com/IEDMS}associatedFile" maxOccurs="unbounded" minOccurs="0"/>
 *                     &lt;/sequence>
 *                     &lt;attribute name="cookie" type="{http://www.infpres.com/IEDMS}anyValue" />
 *                   &lt;/restriction>
 *                 &lt;/complexContent>
 *               &lt;/complexType>
 *             &lt;/element>
 *           &lt;/sequence>
 *           &lt;element name="notification" type="{http://www.infpres.com/IEDMS}messageNotification"/>
 *           &lt;element name="acknowledgment" type="{http://www.infpres.com/IEDMS}messageAcknowledgment"/>
 *         &lt;/choice>
 *         &lt;element name="deliveryIndex" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="destination" type="{http://www.infpres.com/IEDMS}deliveryDestination" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="cookie" type="{http://www.infpres.com/IEDMS}anyValue" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}token" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "header",
    "document",
    "files",
    "notification",
    "acknowledgment",
    "deliveryIndex"
})
@XmlRootElement(name = "communication")
public class Communication {

    @XmlElement(required = true)
    protected Communication.Header header;
    protected Document document;
    protected Communication.Files files;
    protected MessageNotification notification;
    protected MessageAcknowledgment acknowledgment;
    protected Communication.DeliveryIndex deliveryIndex;
    @XmlAttribute(namespace = "http://www.infpres.com/IEDMS", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String version;

    /**
     * Gets the value of the header property.
     * 
     * @return
     *     possible object is
     *     {@link Communication.Header }
     *     
     */
    public Communication.Header getHeader() {
        return header;
    }

    /**
     * Sets the value of the header property.
     * 
     * @param value
     *     allowed object is
     *     {@link Communication.Header }
     *     
     */
    public void setHeader(Communication.Header value) {
        this.header = value;
    }

    /**
     * Gets the value of the document property.
     * 
     * @return
     *     possible object is
     *     {@link Document }
     *     
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Sets the value of the document property.
     * 
     * @param value
     *     allowed object is
     *     {@link Document }
     *     
     */
    public void setDocument(Document value) {
        this.document = value;
    }

    /**
     * Gets the value of the files property.
     * 
     * @return
     *     possible object is
     *     {@link Communication.Files }
     *     
     */
    public Communication.Files getFiles() {
        return files;
    }

    /**
     * Sets the value of the files property.
     * 
     * @param value
     *     allowed object is
     *     {@link Communication.Files }
     *     
     */
    public void setFiles(Communication.Files value) {
        this.files = value;
    }

    /**
     * Gets the value of the notification property.
     * 
     * @return
     *     possible object is
     *     {@link MessageNotification }
     *     
     */
    public MessageNotification getNotification() {
        return notification;
    }

    /**
     * Sets the value of the notification property.
     * 
     * @param value
     *     allowed object is
     *     {@link MessageNotification }
     *     
     */
    public void setNotification(MessageNotification value) {
        this.notification = value;
    }

    /**
     * Gets the value of the acknowledgment property.
     * 
     * @return
     *     possible object is
     *     {@link MessageAcknowledgment }
     *     
     */
    public MessageAcknowledgment getAcknowledgment() {
        return acknowledgment;
    }

    /**
     * Sets the value of the acknowledgment property.
     * 
     * @param value
     *     allowed object is
     *     {@link MessageAcknowledgment }
     *     
     */
    public void setAcknowledgment(MessageAcknowledgment value) {
        this.acknowledgment = value;
    }

    /**
     * Gets the value of the deliveryIndex property.
     * 
     * @return
     *     possible object is
     *     {@link Communication.DeliveryIndex }
     *     
     */
    public Communication.DeliveryIndex getDeliveryIndex() {
        return deliveryIndex;
    }

    /**
     * Sets the value of the deliveryIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link Communication.DeliveryIndex }
     *     
     */
    public void setDeliveryIndex(Communication.DeliveryIndex value) {
        this.deliveryIndex = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="destination" type="{http://www.infpres.com/IEDMS}deliveryDestination" maxOccurs="unbounded"/>
     *       &lt;/sequence>
     *       &lt;attribute name="cookie" type="{http://www.infpres.com/IEDMS}anyValue" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "destination"
    })
    public static class DeliveryIndex {

        @XmlElement(required = true)
        protected List<DeliveryDestination> destination;
        @XmlAttribute(namespace = "http://www.infpres.com/IEDMS")
        protected String cookie;

        /**
         * Gets the value of the destination property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the destination property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getDestination().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link DeliveryDestination }
         * 
         * 
         */
        public List<DeliveryDestination> getDestination() {
            if (destination == null) {
                destination = new ArrayList<DeliveryDestination>();
            }
            return this.destination;
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

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="file" type="{http://www.infpres.com/IEDMS}associatedFile" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *       &lt;attribute name="cookie" type="{http://www.infpres.com/IEDMS}anyValue" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "file"
    })
    public static class Files {

        protected List<AssociatedFile> file;
        @XmlAttribute(namespace = "http://www.infpres.com/IEDMS")
        protected String cookie;

        /**
         * Gets the value of the file property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the file property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getFile().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link AssociatedFile }
         * 
         * 
         */
        public List<AssociatedFile> getFile() {
            if (file == null) {
                file = new ArrayList<AssociatedFile>();
            }
            return this.file;
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

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="source" type="{http://www.infpres.com/IEDMS}communicationPartner"/>
     *         &lt;element name="operator" type="{http://www.infpres.com/IEDMS}qualifiedValue" minOccurs="0"/>
     *         &lt;element name="comment" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;maxLength value="2047"/>
     *               &lt;minLength value="1"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="type" use="required" type="{http://www.infpres.com/IEDMS}messageType" />
     *       &lt;attribute name="uid" type="{http://www.infpres.com/IEDMS}globalUniqueIdentifier" />
     *       &lt;attribute name="created" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
     *       &lt;attribute name="cookie" type="{http://www.infpres.com/IEDMS}anyValue" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "source",
        "operator",
        "comment"
    })
    public static class Header {

        @XmlElement(required = true)
        protected CommunicationPartner source;
        protected QualifiedValue operator;
        protected String comment;
        @XmlAttribute(namespace = "http://www.infpres.com/IEDMS", required = true)
        protected MessageType type;
        @XmlAttribute(namespace = "http://www.infpres.com/IEDMS")
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        protected String uid;
        @XmlAttribute(namespace = "http://www.infpres.com/IEDMS")
        protected XMLGregorianCalendar created;
        @XmlAttribute(namespace = "http://www.infpres.com/IEDMS")
        protected String cookie;

        /**
         * Gets the value of the source property.
         * 
         * @return
         *     possible object is
         *     {@link CommunicationPartner }
         *     
         */
        public CommunicationPartner getSource() {
            return source;
        }

        /**
         * Sets the value of the source property.
         * 
         * @param value
         *     allowed object is
         *     {@link CommunicationPartner }
         *     
         */
        public void setSource(CommunicationPartner value) {
            this.source = value;
        }

        /**
         * Gets the value of the operator property.
         * 
         * @return
         *     possible object is
         *     {@link QualifiedValue }
         *     
         */
        public QualifiedValue getOperator() {
            return operator;
        }

        /**
         * Sets the value of the operator property.
         * 
         * @param value
         *     allowed object is
         *     {@link QualifiedValue }
         *     
         */
        public void setOperator(QualifiedValue value) {
            this.operator = value;
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
         *     {@link MessageType }
         *     
         */
        public MessageType getType() {
            return type;
        }

        /**
         * Sets the value of the type property.
         * 
         * @param value
         *     allowed object is
         *     {@link MessageType }
         *     
         */
        public void setType(MessageType value) {
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

    }

}
