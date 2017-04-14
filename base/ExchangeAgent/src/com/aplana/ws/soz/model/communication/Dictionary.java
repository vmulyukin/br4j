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

package com.aplana.ws.soz.model.communication;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
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
 *         &lt;element name="clients" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="entry" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="uniqueId" type="{http://www.infpres.com/IEDMS}globalUniqueIdentifier"/>
 *                             &lt;element name="localId" type="{http://www.infpres.com/IEDMS}identityValue" minOccurs="0"/>
 *                             &lt;element name="value" type="{http://www.infpres.com/IEDMS}stringValue"/>
 *                             &lt;element name="comment" type="{http://www.infpres.com/IEDMS}stringValue" minOccurs="0"/>
 *                             &lt;element name="departId" type="{http://www.infpres.com/IEDMS}identityValue" minOccurs="0"/>
 *                             &lt;element name="organization" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="shortName" type="{http://www.infpres.com/IEDMS}stringValue"/>
 *                                       &lt;element name="fullName" type="{http://www.infpres.com/IEDMS}stringValue" minOccurs="0"/>
 *                                     &lt;/sequence>
 *                                     &lt;attribute name="id" type="{http://www.infpres.com/IEDMS}identityValue" />
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                             &lt;element name="startDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *                             &lt;element name="paperless" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *                             &lt;element name="mail">
 *                               &lt;simpleType>
 *                                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}normalizedString">
 *                                   &lt;maxLength value="127"/>
 *                                 &lt;/restriction>
 *                               &lt;/simpleType>
 *                             &lt;/element>
 *                             &lt;element name="retro" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *                             &lt;element name="modified" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="comment" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="2047"/>
 *               &lt;minLength value="1"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
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
    "clients",
    "comment"
})
@XmlRootElement(name = "dictionary")
public class Dictionary {

    protected Dictionary.Clients clients;
    protected String comment;
    @XmlAttribute(namespace = "http://www.infpres.com/IEDMS")
    protected XMLGregorianCalendar created;
    @XmlAttribute(namespace = "http://www.infpres.com/IEDMS")
    protected String cookie;

    /**
     * Gets the value of the clients property.
     * 
     * @return
     *     possible object is
     *     {@link Dictionary.Clients }
     *     
     */
    public Dictionary.Clients getClients() {
        return clients;
    }

    /**
     * Sets the value of the clients property.
     * 
     * @param value
     *     allowed object is
     *     {@link Dictionary.Clients }
     *     
     */
    public void setClients(Dictionary.Clients value) {
        this.clients = value;
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
     *         &lt;element name="entry" maxOccurs="unbounded">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="uniqueId" type="{http://www.infpres.com/IEDMS}globalUniqueIdentifier"/>
     *                   &lt;element name="localId" type="{http://www.infpres.com/IEDMS}identityValue" minOccurs="0"/>
     *                   &lt;element name="value" type="{http://www.infpres.com/IEDMS}stringValue"/>
     *                   &lt;element name="comment" type="{http://www.infpres.com/IEDMS}stringValue" minOccurs="0"/>
     *                   &lt;element name="departId" type="{http://www.infpres.com/IEDMS}identityValue" minOccurs="0"/>
     *                   &lt;element name="organization" minOccurs="0">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element name="shortName" type="{http://www.infpres.com/IEDMS}stringValue"/>
     *                             &lt;element name="fullName" type="{http://www.infpres.com/IEDMS}stringValue" minOccurs="0"/>
     *                           &lt;/sequence>
     *                           &lt;attribute name="id" type="{http://www.infpres.com/IEDMS}identityValue" />
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                   &lt;element name="startDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
     *                   &lt;element name="paperless" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
     *                   &lt;element name="mail">
     *                     &lt;simpleType>
     *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}normalizedString">
     *                         &lt;maxLength value="127"/>
     *                       &lt;/restriction>
     *                     &lt;/simpleType>
     *                   &lt;/element>
     *                   &lt;element name="retro" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
     *                   &lt;element name="modified" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "entry"
    })
    public static class Clients {

        @XmlElement(required = true)
        protected List<Dictionary.Clients.Entry> entry;

        /**
         * Gets the value of the entry property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the entry property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getEntry().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Dictionary.Clients.Entry }
         * 
         * 
         */
        public List<Dictionary.Clients.Entry> getEntry() {
            if (entry == null) {
                entry = new ArrayList<Dictionary.Clients.Entry>();
            }
            return this.entry;
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
         *         &lt;element name="uniqueId" type="{http://www.infpres.com/IEDMS}globalUniqueIdentifier"/>
         *         &lt;element name="localId" type="{http://www.infpres.com/IEDMS}identityValue" minOccurs="0"/>
         *         &lt;element name="value" type="{http://www.infpres.com/IEDMS}stringValue"/>
         *         &lt;element name="comment" type="{http://www.infpres.com/IEDMS}stringValue" minOccurs="0"/>
         *         &lt;element name="departId" type="{http://www.infpres.com/IEDMS}identityValue" minOccurs="0"/>
         *         &lt;element name="organization" minOccurs="0">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence>
         *                   &lt;element name="shortName" type="{http://www.infpres.com/IEDMS}stringValue"/>
         *                   &lt;element name="fullName" type="{http://www.infpres.com/IEDMS}stringValue" minOccurs="0"/>
         *                 &lt;/sequence>
         *                 &lt;attribute name="id" type="{http://www.infpres.com/IEDMS}identityValue" />
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *         &lt;element name="startDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
         *         &lt;element name="paperless" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
         *         &lt;element name="mail">
         *           &lt;simpleType>
         *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}normalizedString">
         *               &lt;maxLength value="127"/>
         *             &lt;/restriction>
         *           &lt;/simpleType>
         *         &lt;/element>
         *         &lt;element name="retro" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
         *         &lt;element name="modified" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "uniqueId",
            "localId",
            "value",
            "comment",
            "departId",
            "organization",
            "startDate",
            "paperless",
            "mail",
            "retro",
            "modified"
        })
        public static class Entry {

            @XmlElement(required = true)
            @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
            protected String uniqueId;
            @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
            protected String localId;
            @XmlElement(required = true)
            @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
            protected String value;
            @XmlElementRef(name = "comment", namespace = "http://www.infpres.com/IEDMS", type = JAXBElement.class)
            protected JAXBElement<String> comment;
            @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
            protected String departId;
            protected Dictionary.Clients.Entry.Organization organization;
            @XmlElementRef(name = "startDate", namespace = "http://www.infpres.com/IEDMS", type = JAXBElement.class)
            protected JAXBElement<XMLGregorianCalendar> startDate;
            protected Boolean paperless;
            @XmlElement(required = true)
            @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
            protected String mail;
            protected boolean retro;
            @XmlElement(required = true)
            protected XMLGregorianCalendar modified;

            /**
             * Gets the value of the uniqueId property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getUniqueId() {
                return uniqueId;
            }

            /**
             * Sets the value of the uniqueId property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setUniqueId(String value) {
                this.uniqueId = value;
            }

            /**
             * Gets the value of the localId property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getLocalId() {
                return localId;
            }

            /**
             * Sets the value of the localId property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setLocalId(String value) {
                this.localId = value;
            }

            /**
             * Gets the value of the value property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setValue(String value) {
                this.value = value;
            }

            /**
             * Gets the value of the comment property.
             * 
             * @return
             *     possible object is
             *     {@link JAXBElement }{@code <}{@link String }{@code >}
             *     
             */
            public JAXBElement<String> getComment() {
                return comment;
            }

            /**
             * Sets the value of the comment property.
             * 
             * @param value
             *     allowed object is
             *     {@link JAXBElement }{@code <}{@link String }{@code >}
             *     
             */
            public void setComment(JAXBElement<String> value) {
                this.comment = ((JAXBElement<String> ) value);
            }

            /**
             * Gets the value of the departId property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getDepartId() {
                return departId;
            }

            /**
             * Sets the value of the departId property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setDepartId(String value) {
                this.departId = value;
            }

            /**
             * Gets the value of the organization property.
             * 
             * @return
             *     possible object is
             *     {@link Dictionary.Clients.Entry.Organization }
             *     
             */
            public Dictionary.Clients.Entry.Organization getOrganization() {
                return organization;
            }

            /**
             * Sets the value of the organization property.
             * 
             * @param value
             *     allowed object is
             *     {@link Dictionary.Clients.Entry.Organization }
             *     
             */
            public void setOrganization(Dictionary.Clients.Entry.Organization value) {
                this.organization = value;
            }

            /**
             * Gets the value of the startDate property.
             * 
             * @return
             *     possible object is
             *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
             *     
             */
            public JAXBElement<XMLGregorianCalendar> getStartDate() {
                return startDate;
            }

            /**
             * Sets the value of the startDate property.
             * 
             * @param value
             *     allowed object is
             *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
             *     
             */
            public void setStartDate(JAXBElement<XMLGregorianCalendar> value) {
                this.startDate = ((JAXBElement<XMLGregorianCalendar> ) value);
            }

            /**
             * Gets the value of the paperless property.
             * 
             * @return
             *     possible object is
             *     {@link Boolean }
             *     
             */
            public Boolean isPaperless() {
                return paperless;
            }

            /**
             * Sets the value of the paperless property.
             * 
             * @param value
             *     allowed object is
             *     {@link Boolean }
             *     
             */
            public void setPaperless(Boolean value) {
                this.paperless = value;
            }

            /**
             * Gets the value of the mail property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getMail() {
                return mail;
            }

            /**
             * Sets the value of the mail property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setMail(String value) {
                this.mail = value;
            }

            /**
             * Gets the value of the retro property.
             * 
             */
            public boolean isRetro() {
                return retro;
            }

            /**
             * Sets the value of the retro property.
             * 
             */
            public void setRetro(boolean value) {
                this.retro = value;
            }

            /**
             * Gets the value of the modified property.
             * 
             * @return
             *     possible object is
             *     {@link XMLGregorianCalendar }
             *     
             */
            public XMLGregorianCalendar getModified() {
                return modified;
            }

            /**
             * Sets the value of the modified property.
             * 
             * @param value
             *     allowed object is
             *     {@link XMLGregorianCalendar }
             *     
             */
            public void setModified(XMLGregorianCalendar value) {
                this.modified = value;
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
             *         &lt;element name="shortName" type="{http://www.infpres.com/IEDMS}stringValue"/>
             *         &lt;element name="fullName" type="{http://www.infpres.com/IEDMS}stringValue" minOccurs="0"/>
             *       &lt;/sequence>
             *       &lt;attribute name="id" type="{http://www.infpres.com/IEDMS}identityValue" />
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "shortName",
                "fullName"
            })
            public static class Organization {

                @XmlElement(required = true)
                @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
                protected String shortName;
                @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
                protected String fullName;
                @XmlAttribute(namespace = "http://www.infpres.com/IEDMS")
                @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
                protected String id;

                /**
                 * Gets the value of the shortName property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getShortName() {
                    return shortName;
                }

                /**
                 * Sets the value of the shortName property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setShortName(String value) {
                    this.shortName = value;
                }

                /**
                 * Gets the value of the fullName property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getFullName() {
                    return fullName;
                }

                /**
                 * Sets the value of the fullName property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setFullName(String value) {
                    this.fullName = value;
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

            }

        }

    }

}
