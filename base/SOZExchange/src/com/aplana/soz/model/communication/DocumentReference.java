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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for documentReference complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="documentReference">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="region" type="{http://www.infpres.com/IEDMS}qualifiedValue" minOccurs="0"/>
 *         &lt;element name="organization" type="{http://www.infpres.com/IEDMS}qualifiedValue" minOccurs="0"/>
 *         &lt;element name="person" type="{http://www.infpres.com/IEDMS}qualifiedValue" minOccurs="0"/>
 *         &lt;element name="department" type="{http://www.infpres.com/IEDMS}qualifiedValue" minOccurs="0"/>
 *         &lt;element name="post" type="{http://www.infpres.com/IEDMS}qualifiedValue" minOccurs="0"/>
 *         &lt;element name="num" type="{http://www.infpres.com/IEDMS}documentNumber"/>
 *         &lt;element name="comment" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="2047"/>
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
@XmlType(name = "documentReference", propOrder = {
    "region",
    "organization",
    "person",
    "department",
    "post",
    "num",
    "comment"
})
public class DocumentReference {

    protected QualifiedValue region;
    protected QualifiedValue organization;
    protected QualifiedValue person;
    protected QualifiedValue department;
    protected QualifiedValue post;
    @XmlElement(required = true)
    protected DocumentNumber num;
    protected String comment;

    /**
     * Gets the value of the region property.
     * 
     * @return
     *     possible object is
     *     {@link QualifiedValue }
     *     
     */
    public QualifiedValue getRegion() {
        return region;
    }

    /**
     * Sets the value of the region property.
     * 
     * @param value
     *     allowed object is
     *     {@link QualifiedValue }
     *     
     */
    public void setRegion(QualifiedValue value) {
        this.region = value;
    }

    /**
     * Gets the value of the organization property.
     * 
     * @return
     *     possible object is
     *     {@link QualifiedValue }
     *     
     */
    public QualifiedValue getOrganization() {
        return organization;
    }

    /**
     * Sets the value of the organization property.
     * 
     * @param value
     *     allowed object is
     *     {@link QualifiedValue }
     *     
     */
    public void setOrganization(QualifiedValue value) {
        this.organization = value;
    }

    /**
     * Gets the value of the person property.
     * 
     * @return
     *     possible object is
     *     {@link QualifiedValue }
     *     
     */
    public QualifiedValue getPerson() {
        return person;
    }

    /**
     * Sets the value of the person property.
     * 
     * @param value
     *     allowed object is
     *     {@link QualifiedValue }
     *     
     */
    public void setPerson(QualifiedValue value) {
        this.person = value;
    }

    /**
     * Gets the value of the department property.
     * 
     * @return
     *     possible object is
     *     {@link QualifiedValue }
     *     
     */
    public QualifiedValue getDepartment() {
        return department;
    }

    /**
     * Sets the value of the department property.
     * 
     * @param value
     *     allowed object is
     *     {@link QualifiedValue }
     *     
     */
    public void setDepartment(QualifiedValue value) {
        this.department = value;
    }

    /**
     * Gets the value of the post property.
     * 
     * @return
     *     possible object is
     *     {@link QualifiedValue }
     *     
     */
    public QualifiedValue getPost() {
        return post;
    }

    /**
     * Sets the value of the post property.
     * 
     * @param value
     *     allowed object is
     *     {@link QualifiedValue }
     *     
     */
    public void setPost(QualifiedValue value) {
        this.post = value;
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

}