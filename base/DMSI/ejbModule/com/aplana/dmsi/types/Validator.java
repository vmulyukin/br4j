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
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
 *       &lt;choice&gt;
 *         &lt;sequence&gt;
 *           &lt;element ref=&quot;{}OrganizationWithSign&quot;/&gt;
 *         &lt;/sequence&gt;
 *         &lt;sequence&gt;
 *           &lt;element ref=&quot;{}PrivatePersonWithSign&quot;/&gt;
 *         &lt;/sequence&gt;
 *         &lt;sequence&gt;
 *           &lt;element ref=&quot;{}DocNumber&quot;/&gt;
 *         &lt;/sequence&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name=&quot;attestation&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "containedObject" })
@XmlRootElement(name = "Validator")
public class Validator extends DMSIObject implements ContainerObject {

    @XmlElements( {
	    @XmlElement(name = "OrganizationWithSign", type = OrganizationWithSign.class),
	    @XmlElement(name = "PrivatePersonWithSign", type = PrivatePersonWithSign.class),
	    @XmlElement(name = "DocNumber", type = DocNumber.class) })
    protected Object containedObject;

    @XmlAttribute(required = true)
    protected String attestation = "";

    public Object getContainedObject() {
	return this.containedObject;
    }

    public void setContainedObject(Object containedObject) {
	this.containedObject = containedObject;
    }

    /**
     * Gets the value of the attestation property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getAttestation() {
	return attestation;
    }

    /**
     * Sets the value of the attestation property.
     *
     * @param value
     *                allowed object is {@link String }
     *
     */
    public void setAttestation(String value) {
	this.attestation = value;
    }

}
