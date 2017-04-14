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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.aplana.dmsi.types.adapters.EcontactAdapter;

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
 *   &lt;simpleContent&gt;
 *     &lt;extension base=&quot;&lt;http://www.w3.org/2001/XMLSchema&gt;string&quot;&gt;
 *       &lt;attribute name=&quot;type&quot; type=&quot;{}EcontactEnumType&quot; /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "value" })
@XmlRootElement(name = "Econtact")
@XmlJavaTypeAdapter(EcontactAdapter.class)
public class Econtact {

    @XmlValue
    protected String value;
    @XmlAttribute
    protected EcontactEnumType type;

    public Econtact() {
    }

    public Econtact(Econtact anotherContact) {
	setType(anotherContact.getType());
	setValue(anotherContact.getValue());
    }

    /**
     * Gets the value of the value property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getValue() {
	return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value
     *                allowed object is {@link String }
     *
     */
    public void setValue(String value) {
	this.value = value;
    }

    /**
     * Gets the value of the type property.
     *
     * @return possible object is {@link Byte }
     *
     */
    public EcontactEnumType getType() {
	return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value
     *                allowed object is {@link Byte }
     *
     */
    public void setType(EcontactEnumType value) {
	this.type = value;
    }

}
