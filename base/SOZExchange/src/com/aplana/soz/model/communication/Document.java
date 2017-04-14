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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for document complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="document">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="type" type="{http://www.infpres.com/IEDMS}qualifiedValue" minOccurs="0"/>
 *         &lt;element name="kind" type="{http://www.infpres.com/IEDMS}qualifiedValue" minOccurs="0"/>
 *         &lt;element name="num" type="{http://www.infpres.com/IEDMS}documentNumber"/>
 *         &lt;element name="classification" type="{http://www.infpres.com/IEDMS}qualifiedValue" minOccurs="0"/>
 *         &lt;element name="urgency" type="{http://www.infpres.com/IEDMS}qualifiedValue" minOccurs="0"/>
 *         &lt;element name="insteadOfDistributed" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="specialMark" type="{http://www.infpres.com/IEDMS}stringValue" minOccurs="0"/>
 *         &lt;element name="signatories">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="signatory" type="{http://www.infpres.com/IEDMS}signatory" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="addressees">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="addressee" type="{http://www.infpres.com/IEDMS}addressee" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="pages" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *         &lt;element name="enclosuresPages" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" minOccurs="0"/>
 *         &lt;element name="annotation" type="{http://www.infpres.com/IEDMS}shortText"/>
 *         &lt;element name="enclosures" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="enclosure" type="{http://www.infpres.com/IEDMS}enclosure" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="correspondents" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="correspondent" type="{http://www.infpres.com/IEDMS}correspondent" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="links" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="link" type="{http://www.infpres.com/IEDMS}linkedDocument" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="clauses" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="clause" type="{http://www.infpres.com/IEDMS}documentClause" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="executor" type="{http://www.infpres.com/IEDMS}addressee" minOccurs="0"/>
 *         &lt;element name="comment" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="2047"/>
 *               &lt;minLength value="1"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
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
@XmlType(name = "document", propOrder = {
    "type",
    "kind",
    "num",
    "classification",
    "urgency",
    "insteadOfDistributed",
    "specialMark",
    "signatories",
    "addressees",
    "pages",
    "enclosuresPages",
    "annotation",
    "enclosures",
    "correspondents",
    "links",
    "clauses",
    "executor",
    "comment"
})
public class Document {

    protected QualifiedValue type;
    protected QualifiedValue kind;
    @XmlElement(required = true)
    protected DocumentNumber num;
    protected QualifiedValue classification;
    protected QualifiedValue urgency;
    protected Boolean insteadOfDistributed;
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String specialMark;
    @XmlElement(required = true)
    protected Document.Signatories signatories;
    @XmlElement(required = true)
    protected Document.Addressees addressees;
    @XmlElement(required = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger pages;
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger enclosuresPages;
    @XmlElement(required = true, nillable = true)
    protected String annotation;
    protected Document.Enclosures enclosures;
    protected Document.Correspondents correspondents;
    protected Document.Links links;
    protected Document.Clauses clauses;
    protected Addressee executor;
    protected String comment;
    @XmlAttribute(namespace = "http://www.infpres.com/IEDMS", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String uid;
    @XmlAttribute(namespace = "http://www.infpres.com/IEDMS")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String id;
    @XmlAttribute(namespace = "http://www.infpres.com/IEDMS")
    protected String cookie;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link QualifiedValue }
     *     
     */
    public QualifiedValue getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link QualifiedValue }
     *     
     */
    public void setType(QualifiedValue value) {
        this.type = value;
    }

    /**
     * Gets the value of the kind property.
     * 
     * @return
     *     possible object is
     *     {@link QualifiedValue }
     *     
     */
    public QualifiedValue getKind() {
        return kind;
    }

    /**
     * Sets the value of the kind property.
     * 
     * @param value
     *     allowed object is
     *     {@link QualifiedValue }
     *     
     */
    public void setKind(QualifiedValue value) {
        this.kind = value;
    }

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

    /**
     * Gets the value of the classification property.
     * 
     * @return
     *     possible object is
     *     {@link QualifiedValue }
     *     
     */
    public QualifiedValue getClassification() {
        return classification;
    }

    /**
     * Sets the value of the classification property.
     * 
     * @param value
     *     allowed object is
     *     {@link QualifiedValue }
     *     
     */
    public void setClassification(QualifiedValue value) {
        this.classification = value;
    }

    /**
     * Gets the value of the urgency property.
     * 
     * @return
     *     possible object is
     *     {@link QualifiedValue }
     *     
     */
    public QualifiedValue getUrgency() {
        return urgency;
    }

    /**
     * Sets the value of the urgency property.
     * 
     * @param value
     *     allowed object is
     *     {@link QualifiedValue }
     *     
     */
    public void setUrgency(QualifiedValue value) {
        this.urgency = value;
    }

    /**
     * Gets the value of the insteadOfDistributed property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isInsteadOfDistributed() {
        return insteadOfDistributed;
    }

    /**
     * Sets the value of the insteadOfDistributed property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInsteadOfDistributed(Boolean value) {
        this.insteadOfDistributed = value;
    }

    /**
     * Gets the value of the specialMark property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpecialMark() {
        return specialMark;
    }

    /**
     * Sets the value of the specialMark property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpecialMark(String value) {
        this.specialMark = value;
    }

    /**
     * Gets the value of the signatories property.
     * 
     * @return
     *     possible object is
     *     {@link Document.Signatories }
     *     
     */
    public Document.Signatories getSignatories() {
        return signatories;
    }

    /**
     * Sets the value of the signatories property.
     * 
     * @param value
     *     allowed object is
     *     {@link Document.Signatories }
     *     
     */
    public void setSignatories(Document.Signatories value) {
        this.signatories = value;
    }

    /**
     * Gets the value of the addressees property.
     * 
     * @return
     *     possible object is
     *     {@link Document.Addressees }
     *     
     */
    public Document.Addressees getAddressees() {
        return addressees;
    }

    /**
     * Sets the value of the addressees property.
     * 
     * @param value
     *     allowed object is
     *     {@link Document.Addressees }
     *     
     */
    public void setAddressees(Document.Addressees value) {
        this.addressees = value;
    }

    /**
     * Gets the value of the pages property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getPages() {
        return pages;
    }

    /**
     * Sets the value of the pages property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setPages(BigInteger value) {
        this.pages = value;
    }

    /**
     * Gets the value of the enclosuresPages property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getEnclosuresPages() {
        return enclosuresPages;
    }

    /**
     * Sets the value of the enclosuresPages property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setEnclosuresPages(BigInteger value) {
        this.enclosuresPages = value;
    }

    /**
     * Gets the value of the annotation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAnnotation() {
        return annotation;
    }

    /**
     * Sets the value of the annotation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAnnotation(String value) {
        this.annotation = value;
    }

    /**
     * Gets the value of the enclosures property.
     * 
     * @return
     *     possible object is
     *     {@link Document.Enclosures }
     *     
     */
    public Document.Enclosures getEnclosures() {
        return enclosures;
    }

    /**
     * Sets the value of the enclosures property.
     * 
     * @param value
     *     allowed object is
     *     {@link Document.Enclosures }
     *     
     */
    public void setEnclosures(Document.Enclosures value) {
        this.enclosures = value;
    }

    /**
     * Gets the value of the correspondents property.
     * 
     * @return
     *     possible object is
     *     {@link Document.Correspondents }
     *     
     */
    public Document.Correspondents getCorrespondents() {
        return correspondents;
    }

    /**
     * Sets the value of the correspondents property.
     * 
     * @param value
     *     allowed object is
     *     {@link Document.Correspondents }
     *     
     */
    public void setCorrespondents(Document.Correspondents value) {
        this.correspondents = value;
    }

    /**
     * Gets the value of the links property.
     * 
     * @return
     *     possible object is
     *     {@link Document.Links }
     *     
     */
    public Document.Links getLinks() {
        return links;
    }

    /**
     * Sets the value of the links property.
     * 
     * @param value
     *     allowed object is
     *     {@link Document.Links }
     *     
     */
    public void setLinks(Document.Links value) {
        this.links = value;
    }

    /**
     * Gets the value of the clauses property.
     * 
     * @return
     *     possible object is
     *     {@link Document.Clauses }
     *     
     */
    public Document.Clauses getClauses() {
        return clauses;
    }

    /**
     * Sets the value of the clauses property.
     * 
     * @param value
     *     allowed object is
     *     {@link Document.Clauses }
     *     
     */
    public void setClauses(Document.Clauses value) {
        this.clauses = value;
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
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="addressee" type="{http://www.infpres.com/IEDMS}addressee" maxOccurs="unbounded"/>
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
        "addressee"
    })
    public static class Addressees {

        @XmlElement(required = true)
        protected List<Addressee> addressee;

        /**
         * Gets the value of the addressee property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the addressee property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAddressee().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Addressee }
         * 
         * 
         */
        public List<Addressee> getAddressee() {
            if (addressee == null) {
                addressee = new ArrayList<Addressee>();
            }
            return this.addressee;
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
     *         &lt;element name="clause" type="{http://www.infpres.com/IEDMS}documentClause" maxOccurs="unbounded"/>
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
        "clause"
    })
    public static class Clauses {

        @XmlElement(required = true)
        protected List<DocumentClause> clause;

        /**
         * Gets the value of the clause property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the clause property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getClause().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link DocumentClause }
         * 
         * 
         */
        public List<DocumentClause> getClause() {
            if (clause == null) {
                clause = new ArrayList<DocumentClause>();
            }
            return this.clause;
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
     *         &lt;element name="correspondent" type="{http://www.infpres.com/IEDMS}correspondent" maxOccurs="unbounded" minOccurs="0"/>
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
        "correspondent"
    })
    public static class Correspondents {

        protected List<Correspondent> correspondent;

        /**
         * Gets the value of the correspondent property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the correspondent property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCorrespondent().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Correspondent }
         * 
         * 
         */
        public List<Correspondent> getCorrespondent() {
            if (correspondent == null) {
                correspondent = new ArrayList<Correspondent>();
            }
            return this.correspondent;
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
     *         &lt;element name="enclosure" type="{http://www.infpres.com/IEDMS}enclosure" maxOccurs="unbounded"/>
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
        "enclosure"
    })
    public static class Enclosures {

        @XmlElement(required = true)
        protected List<Enclosure> enclosure;

        /**
         * Gets the value of the enclosure property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the enclosure property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getEnclosure().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Enclosure }
         * 
         * 
         */
        public List<Enclosure> getEnclosure() {
            if (enclosure == null) {
                enclosure = new ArrayList<Enclosure>();
            }
            return this.enclosure;
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
     *         &lt;element name="link" type="{http://www.infpres.com/IEDMS}linkedDocument" maxOccurs="unbounded"/>
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
        "link"
    })
    public static class Links {

        @XmlElement(required = true)
        protected List<LinkedDocument> link;

        /**
         * Gets the value of the link property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the link property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getLink().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link LinkedDocument }
         * 
         * 
         */
        public List<LinkedDocument> getLink() {
            if (link == null) {
                link = new ArrayList<LinkedDocument>();
            }
            return this.link;
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
     *         &lt;element name="signatory" type="{http://www.infpres.com/IEDMS}signatory" maxOccurs="unbounded"/>
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
        "signatory"
    })
    public static class Signatories {

        @XmlElement(required = true)
        protected List<Signatory> signatory;

        /**
         * Gets the value of the signatory property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the signatory property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSignatory().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Signatory }
         * 
         * 
         */
        public List<Signatory> getSignatory() {
            if (signatory == null) {
                signatory = new ArrayList<Signatory>();
            }
            return this.signatory;
        }

    }

}
