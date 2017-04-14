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
package com.aplana.dmsi.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <p>
 * Java class for anonymous complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref=&quot;{}Organization&quot;/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name=&quot;responsible&quot; type=&quot;{}ExecutorEnumType&quot; /&gt;
 *       &lt;attribute name=&quot;task_specified&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;deadline&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}date&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "organization" })
@XmlRootElement(name = "Executor")
public class Executor extends DMSIObject {

    @XmlElement(name = "Organization", required = true)
    protected Organization organization = new Organization();
    @XmlAttribute
    protected ExecutorEnumType responsible = ExecutorEnumType.COEXECUTOR;
    @XmlAttribute(name = "task_specified")
    protected String taskSpecified;
    @XmlAttribute
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar deadline;

    /**
     * Gets the value of the organization property.
     *
     * @return possible object is {@link Organization }
     *
     */
    public Organization getOrganization() {
	return organization;
    }

    /**
     * Sets the value of the organization property.
     *
     * @param value
     *                allowed object is {@link Organization }
     *
     */
    public void setOrganization(Organization value) {
	this.organization = value;
    }

    /**
     * Gets the value of the responsible property.
     *
     * @return possible object is {@link Byte }
     *
     */
    public ExecutorEnumType getResponsible() {
	return responsible;
    }

    /**
     * Sets the value of the responsible property.
     *
     * @param value
     *                allowed object is {@link Byte }
     *
     */
    public void setResponsible(ExecutorEnumType value) {
	this.responsible = value;
    }

    /**
     * Gets the value of the taskSpecified property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getTaskSpecified() {
	return taskSpecified;
    }

    /**
     * Sets the value of the taskSpecified property.
     *
     * @param value
     *                allowed object is {@link String }
     *
     */
    public void setTaskSpecified(String value) {
	this.taskSpecified = value;
    }

    /**
     * Gets the value of the deadline property.
     *
     * @return possible object is {@link XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getDeadline() {
	return deadline;
    }

    /**
     * Sets the value of the deadline property.
     *
     * @param value
     *                allowed object is {@link XMLGregorianCalendar }
     *
     */
    public void setDeadline(XMLGregorianCalendar value) {
	this.deadline = value;
    }

}
