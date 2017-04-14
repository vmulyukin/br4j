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
 * <p>Java class for linkedDocument complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="linkedDocument">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="linkType" type="{http://www.infpres.com/IEDMS}linkType"/>
 *         &lt;choice>
 *           &lt;element name="document">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element name="kind" type="{http://www.infpres.com/IEDMS}qualifiedValue" minOccurs="0"/>
 *                     &lt;element name="num" type="{http://www.infpres.com/IEDMS}documentNumber"/>
 *                     &lt;element name="classification" type="{http://www.infpres.com/IEDMS}qualifiedValue" minOccurs="0"/>
 *                     &lt;element name="signatories">
 *                       &lt;complexType>
 *                         &lt;complexContent>
 *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                             &lt;sequence>
 *                               &lt;element name="signatory" type="{http://www.infpres.com/IEDMS}signatory" maxOccurs="unbounded"/>
 *                             &lt;/sequence>
 *                           &lt;/restriction>
 *                         &lt;/complexContent>
 *                       &lt;/complexType>
 *                     &lt;/element>
 *                     &lt;element name="addressees">
 *                       &lt;complexType>
 *                         &lt;complexContent>
 *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                             &lt;sequence>
 *                               &lt;element name="addressee" type="{http://www.infpres.com/IEDMS}addressee" maxOccurs="unbounded"/>
 *                             &lt;/sequence>
 *                           &lt;/restriction>
 *                         &lt;/complexContent>
 *                       &lt;/complexType>
 *                     &lt;/element>
 *                     &lt;element name="pages" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *                     &lt;element name="enclosuresPages" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" minOccurs="0"/>
 *                     &lt;element name="annotation" type="{http://www.infpres.com/IEDMS}shortText"/>
 *                     &lt;element name="enclosures" minOccurs="0">
 *                       &lt;complexType>
 *                         &lt;complexContent>
 *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                             &lt;sequence>
 *                               &lt;element name="enclosure" type="{http://www.infpres.com/IEDMS}enclosure" maxOccurs="unbounded"/>
 *                             &lt;/sequence>
 *                           &lt;/restriction>
 *                         &lt;/complexContent>
 *                       &lt;/complexType>
 *                     &lt;/element>
 *                     &lt;element name="correspondents" minOccurs="0">
 *                       &lt;complexType>
 *                         &lt;complexContent>
 *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                             &lt;sequence>
 *                               &lt;element name="correspondent" type="{http://www.infpres.com/IEDMS}correspondent" maxOccurs="unbounded" minOccurs="0"/>
 *                             &lt;/sequence>
 *                           &lt;/restriction>
 *                         &lt;/complexContent>
 *                       &lt;/complexType>
 *                     &lt;/element>
 *                     &lt;element name="links" minOccurs="0">
 *                       &lt;complexType>
 *                         &lt;complexContent>
 *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                             &lt;sequence>
 *                               &lt;element name="link" type="{http://www.infpres.com/IEDMS}linkedDocument" maxOccurs="unbounded"/>
 *                             &lt;/sequence>
 *                           &lt;/restriction>
 *                         &lt;/complexContent>
 *                       &lt;/complexType>
 *                     &lt;/element>
 *                     &lt;element name="clauses" minOccurs="0">
 *                       &lt;complexType>
 *                         &lt;complexContent>
 *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                             &lt;sequence>
 *                               &lt;element name="clause" type="{http://www.infpres.com/IEDMS}documentClause" maxOccurs="unbounded"/>
 *                             &lt;/sequence>
 *                           &lt;/restriction>
 *                         &lt;/complexContent>
 *                       &lt;/complexType>
 *                     &lt;/element>
 *                     &lt;element name="comment" minOccurs="0">
 *                       &lt;simpleType>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                           &lt;maxLength value="2047"/>
 *                           &lt;minLength value="1"/>
 *                         &lt;/restriction>
 *                       &lt;/simpleType>
 *                     &lt;/element>
 *                   &lt;/sequence>
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="reference" type="{http://www.infpres.com/IEDMS}documentReference"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="uid" type="{http://www.infpres.com/IEDMS}globalUniqueIdentifier" />
 *       &lt;attribute name="id" type="{http://www.infpres.com/IEDMS}identityValue" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "linkedDocument", propOrder = {
    "linkType",
    "document",
    "reference"
})
public class LinkedDocument {

    @XmlElement(required = true, defaultValue = "\u0421\u0432\u044f\u0437\u0430\u043d \u0441")
    protected LinkType linkType;
    protected LinkedDocument.Document document;
    protected DocumentReference reference;
    @XmlAttribute(namespace = "http://www.infpres.com/IEDMS")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String uid;
    @XmlAttribute(namespace = "http://www.infpres.com/IEDMS")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String id;

    /**
     * Gets the value of the linkType property.
     * 
     * @return
     *     possible object is
     *     {@link LinkType }
     *     
     */
    public LinkType getLinkType() {
        return linkType;
    }

    /**
     * Sets the value of the linkType property.
     * 
     * @param value
     *     allowed object is
     *     {@link LinkType }
     *     
     */
    public void setLinkType(LinkType value) {
        this.linkType = value;
    }

    /**
     * Gets the value of the document property.
     * 
     * @return
     *     possible object is
     *     {@link LinkedDocument.Document }
     *     
     */
    public LinkedDocument.Document getDocument() {
        return document;
    }

    /**
     * Sets the value of the document property.
     * 
     * @param value
     *     allowed object is
     *     {@link LinkedDocument.Document }
     *     
     */
    public void setDocument(LinkedDocument.Document value) {
        this.document = value;
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
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="kind" type="{http://www.infpres.com/IEDMS}qualifiedValue" minOccurs="0"/>
     *         &lt;element name="num" type="{http://www.infpres.com/IEDMS}documentNumber"/>
     *         &lt;element name="classification" type="{http://www.infpres.com/IEDMS}qualifiedValue" minOccurs="0"/>
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
     *         &lt;element name="comment" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;maxLength value="2047"/>
     *               &lt;minLength value="1"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
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
        "kind",
        "num",
        "classification",
        "signatories",
        "addressees",
        "pages",
        "enclosuresPages",
        "annotation",
        "enclosures",
        "correspondents",
        "links",
        "clauses",
        "comment"
    })
    public static class Document {

        protected QualifiedValue kind;
        @XmlElement(required = true)
        protected DocumentNumber num;
        protected QualifiedValue classification;
        @XmlElement(required = true)
        protected LinkedDocument.Document.Signatories signatories;
        @XmlElement(required = true)
        protected LinkedDocument.Document.Addressees addressees;
        @XmlElement(required = true)
        @XmlSchemaType(name = "nonNegativeInteger")
        protected BigInteger pages;
        @XmlSchemaType(name = "nonNegativeInteger")
        protected BigInteger enclosuresPages;
        @XmlElement(required = true, nillable = true)
        protected String annotation;
        protected LinkedDocument.Document.Enclosures enclosures;
        protected LinkedDocument.Document.Correspondents correspondents;
        protected LinkedDocument.Document.Links links;
        protected LinkedDocument.Document.Clauses clauses;
        protected String comment;

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
         * Gets the value of the signatories property.
         * 
         * @return
         *     possible object is
         *     {@link LinkedDocument.Document.Signatories }
         *     
         */
        public LinkedDocument.Document.Signatories getSignatories() {
            return signatories;
        }

        /**
         * Sets the value of the signatories property.
         * 
         * @param value
         *     allowed object is
         *     {@link LinkedDocument.Document.Signatories }
         *     
         */
        public void setSignatories(LinkedDocument.Document.Signatories value) {
            this.signatories = value;
        }

        /**
         * Gets the value of the addressees property.
         * 
         * @return
         *     possible object is
         *     {@link LinkedDocument.Document.Addressees }
         *     
         */
        public LinkedDocument.Document.Addressees getAddressees() {
            return addressees;
        }

        /**
         * Sets the value of the addressees property.
         * 
         * @param value
         *     allowed object is
         *     {@link LinkedDocument.Document.Addressees }
         *     
         */
        public void setAddressees(LinkedDocument.Document.Addressees value) {
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
         *     {@link LinkedDocument.Document.Enclosures }
         *     
         */
        public LinkedDocument.Document.Enclosures getEnclosures() {
            return enclosures;
        }

        /**
         * Sets the value of the enclosures property.
         * 
         * @param value
         *     allowed object is
         *     {@link LinkedDocument.Document.Enclosures }
         *     
         */
        public void setEnclosures(LinkedDocument.Document.Enclosures value) {
            this.enclosures = value;
        }

        /**
         * Gets the value of the correspondents property.
         * 
         * @return
         *     possible object is
         *     {@link LinkedDocument.Document.Correspondents }
         *     
         */
        public LinkedDocument.Document.Correspondents getCorrespondents() {
            return correspondents;
        }

        /**
         * Sets the value of the correspondents property.
         * 
         * @param value
         *     allowed object is
         *     {@link LinkedDocument.Document.Correspondents }
         *     
         */
        public void setCorrespondents(LinkedDocument.Document.Correspondents value) {
            this.correspondents = value;
        }

        /**
         * Gets the value of the links property.
         * 
         * @return
         *     possible object is
         *     {@link LinkedDocument.Document.Links }
         *     
         */
        public LinkedDocument.Document.Links getLinks() {
            return links;
        }

        /**
         * Sets the value of the links property.
         * 
         * @param value
         *     allowed object is
         *     {@link LinkedDocument.Document.Links }
         *     
         */
        public void setLinks(LinkedDocument.Document.Links value) {
            this.links = value;
        }

        /**
         * Gets the value of the clauses property.
         * 
         * @return
         *     possible object is
         *     {@link LinkedDocument.Document.Clauses }
         *     
         */
        public LinkedDocument.Document.Clauses getClauses() {
            return clauses;
        }

        /**
         * Sets the value of the clauses property.
         * 
         * @param value
         *     allowed object is
         *     {@link LinkedDocument.Document.Clauses }
         *     
         */
        public void setClauses(LinkedDocument.Document.Clauses value) {
            this.clauses = value;
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

}
