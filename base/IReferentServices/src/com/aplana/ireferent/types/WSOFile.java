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
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for WSO_FILE complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name=&quot;WSO_FILE&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base=&quot;{urn:IReferent.it.com}WSO_MFILE&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;BODY&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WSO_FILE", propOrder = { "body" })
public class WSOFile extends WSOMFile {

    @XmlSchemaType(name = "base64Binary")
    @XmlElement(name = "BODY", required = true, nillable = true)
    protected byte[] body;

    public static final String CLASS_TYPE = "WSO_FILE";

    /**
     * Gets the value of the body property.
     *
     * @return possible object is byte[]
     */
    public byte[] getBody() {
	return body;
    }

    /**
     * Sets the value of the body property.
     *
     * @param value
     *                allowed object is String
     */
    public void setBody(byte[] value) {
	this.body = (value);
    }

}
