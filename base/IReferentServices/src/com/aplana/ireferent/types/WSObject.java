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
package com.aplana.ireferent.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <p>
 * Java class for WSObject complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name=&quot;WSObject&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;ID&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;/&gt;
 *         &lt;element name=&quot;TITLE&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;/&gt;
 *         &lt;element name=&quot;TYPE&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;/&gt;
 *         &lt;element name=&quot;EXTENSION&quot; type=&quot;{urn:IReferent.it.com}WSO_COLLECTION&quot;/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WSOBJECT", propOrder = { "id", "title", "type", "updDate", "extension" })
public class WSObject {

    @XmlElement(name = "ID", required = true, nillable = true)
    protected String id;
    @XmlTransient
    protected Long state;
    @XmlElement(name = "TITLE", required = true, nillable = true)
    protected String title;
    @XmlElement(name = "TYPE", required = true, nillable = true)
    protected String type = "WSOBJECT";
    @XmlElement(name = "UPDDATE", required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar updDate;
    @XmlElement(name = "EXTENSION", required = true, nillable = true)
    protected WSOCollection extension;

    public WSObject() {
	XmlType typeAnnotation = getClass().getAnnotation(XmlType.class);
	if (typeAnnotation != null) {
	    String typeName = typeAnnotation.name();
	    if (typeName != null && !"".equals(typeName)) {
		setType(typeName.toUpperCase());
	    }
	}
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getId() {
	return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value
     *                allowed object is {@link String }
     *
     */
    public void setId(String value) {
	this.id = value;
    }

    @XmlTransient
    public Long getState() {
        return this.state;
    }

    public void setState(Long taskState) {
        this.state = taskState;
    }

    /**
     * Gets the value of the title property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getTitle() {
	return title;
    }

    /**
     * Sets the value of the title property.
     *
     * @param value
     *                allowed object is {@link String }
     *
     */
    public void setTitle(String value) {
	this.title = value;
    }

    /**
     * Gets the value of the type property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getType() {
	return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value
     *                allowed object is {@link String }
     *
     */
    public void setType(String value) {
	this.type = value;
    }
    
    /**
     * @return the updDate
     */
    public XMLGregorianCalendar getUpdDate() {
        return this.updDate;
    }
    
    /**
     * @param updDate the updDate to set
     */
    public void setUpdDate(XMLGregorianCalendar updDate) {
        this.updDate = updDate;
    }

    /**
     * Gets the value of the extension property.
     *
     * @return possible object is {@link WSOCollection }
     *
     */
    public WSOCollection getExtension() {
	if (extension == null) {
	    setExtension(new WSOCollection());
	}
	return extension;
    }

    /**
     * Sets the value of the extension property.
     *
     * @param value
     *                allowed object is {@link WSOCollection }
     *
     */
    public void setExtension(WSOCollection value) {
	this.extension = value;
    }

}
