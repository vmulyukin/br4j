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
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for documentClause complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="documentClause">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="designation" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}normalizedString">
 *               &lt;maxLength value="255"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="text" type="{http://www.infpres.com/IEDMS}shortText" minOccurs="0"/>
 *         &lt;element name="deadline" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="principal" type="{http://www.infpres.com/IEDMS}addressee"/>
 *           &lt;element name="principals" type="{http://www.infpres.com/IEDMS}addresseeList"/>
 *         &lt;/choice>
 *         &lt;element name="parcipants" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;choice maxOccurs="unbounded">
 *                     &lt;element name="parcipant" type="{http://www.infpres.com/IEDMS}addressee"/>
 *                     &lt;element name="parcipants" type="{http://www.infpres.com/IEDMS}addresseeList"/>
 *                   &lt;/choice>
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
 *       &lt;attribute name="localId" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       &lt;attribute name="id" type="{http://www.infpres.com/IEDMS}identityValue" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "documentClause", propOrder = {
    "designation",
    "text",
    "deadline",
    "principal",
    "principals",
    "parcipants",
    "comment"
})
public class DocumentClause {

    @XmlElementRef(name = "designation", namespace = "http://www.infpres.com/IEDMS", type = JAXBElement.class)
    protected JAXBElement<String> designation;
    protected String text;
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar deadline;
    protected Addressee principal;
    protected AddresseeList principals;
    protected DocumentClause.Parcipants parcipants;
    protected String comment;
    @XmlAttribute(namespace = "http://www.infpres.com/IEDMS")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger localId;
    @XmlAttribute(namespace = "http://www.infpres.com/IEDMS")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String id;

    /**
     * Gets the value of the designation property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getDesignation() {
        return designation;
    }

    /**
     * Sets the value of the designation property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setDesignation(JAXBElement<String> value) {
        this.designation = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the text property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setText(String value) {
        this.text = value;
    }

    /**
     * Gets the value of the deadline property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDeadline() {
        return deadline;
    }

    /**
     * Sets the value of the deadline property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDeadline(XMLGregorianCalendar value) {
        this.deadline = value;
    }

    /**
     * Gets the value of the principal property.
     * 
     * @return
     *     possible object is
     *     {@link Addressee }
     *     
     */
    public Addressee getPrincipal() {
        return principal;
    }

    /**
     * Sets the value of the principal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Addressee }
     *     
     */
    public void setPrincipal(Addressee value) {
        this.principal = value;
    }

    /**
     * Gets the value of the principals property.
     * 
     * @return
     *     possible object is
     *     {@link AddresseeList }
     *     
     */
    public AddresseeList getPrincipals() {
        return principals;
    }

    /**
     * Sets the value of the principals property.
     * 
     * @param value
     *     allowed object is
     *     {@link AddresseeList }
     *     
     */
    public void setPrincipals(AddresseeList value) {
        this.principals = value;
    }

    /**
     * Gets the value of the parcipants property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentClause.Parcipants }
     *     
     */
    public DocumentClause.Parcipants getParcipants() {
        return parcipants;
    }

    /**
     * Sets the value of the parcipants property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentClause.Parcipants }
     *     
     */
    public void setParcipants(DocumentClause.Parcipants value) {
        this.parcipants = value;
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
     * Gets the value of the localId property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getLocalId() {
        return localId;
    }

    /**
     * Sets the value of the localId property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setLocalId(BigInteger value) {
        this.localId = value;
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
     *         &lt;choice maxOccurs="unbounded">
     *           &lt;element name="parcipant" type="{http://www.infpres.com/IEDMS}addressee"/>
     *           &lt;element name="parcipants" type="{http://www.infpres.com/IEDMS}addresseeList"/>
     *         &lt;/choice>
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
        "parcipantOrParcipants"
    })
    public static class Parcipants {

        @XmlElements({
            @XmlElement(name = "parcipants", type = AddresseeList.class),
            @XmlElement(name = "parcipant", type = Addressee.class)
        })
        protected List<Object> parcipantOrParcipants;

        /**
         * Gets the value of the parcipantOrParcipants property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the parcipantOrParcipants property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getParcipantOrParcipants().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link AddresseeList }
         * {@link Addressee }
         * 
         * 
         */
        public List<Object> getParcipantOrParcipants() {
            if (parcipantOrParcipants == null) {
                parcipantOrParcipants = new ArrayList<Object>();
            }
            return this.parcipantOrParcipants;
        }

    }

}
