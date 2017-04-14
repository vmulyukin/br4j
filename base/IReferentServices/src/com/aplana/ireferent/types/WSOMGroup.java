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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for WSO_MGROUP complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="WSO_MGROUP">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:IReferent.it.com}WSO_STAFFOBJECT">
 *       &lt;sequence>
 *         &lt;element name="CHILDS" type="{urn:IReferent.it.com}WSO_COLLECTION"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WSO_MGROUP", propOrder = {
    "childs"
})
public class WSOMGroup
    extends WSOStaffObject
{

    @XmlElement(name = "CHILDS", required = true, nillable = true)
    protected WSOCollection childs;
    public static final String  CLASS_TYPE = "WSO_MGROUP";

    /**
     * Gets the value of the childs property.
     *
     * @return
     *     possible object is
     *     {@link WSOCollection }
     *
     */
    public WSOCollection getChilds() {
        return childs;
    }

    /**
     * Sets the value of the childs property.
     *
     * @param value
     *     allowed object is
     *     {@link WSOCollection }
     *
     */
    public void setChilds(WSOCollection value) {
        this.childs = value;
    }

}
