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
 * <p>Java class for WSO_STAFFOBJECT complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="WSO_STAFFOBJECT">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:IReferent.it.com}WSObject">
 *       &lt;sequence>
 *         &lt;element name="PARENTS" type="{urn:IReferent.it.com}WSO_COLLECTION"/>
 *         &lt;element name="ATTACHMENTS" type="{urn:IReferent.it.com}WSO_COLLECTION"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WSO_STAFFOBJECT", propOrder = {
    "parents",
    "attachments"
})
public class WSOStaffObject
    extends WSObject
{

    @XmlElement(name = "PARENTS", required = true, nillable = true)
    protected WSOCollection parents;
    @XmlElement(name = "ATTACHMENTS", required = true, nillable = true)
    protected WSOCollection attachments;

    /**
     * Gets the value of the parents property.
     *
     * @return
     *     possible object is
     *     {@link WSOCollection }
     *
     */
    public WSOCollection getParents() {
        return parents;
    }

    /**
     * Sets the value of the parents property.
     *
     * @param value
     *     allowed object is
     *     {@link WSOCollection }
     *
     */
    public void setParents(WSOCollection value) {
        this.parents = value;
    }

    /**
     * Gets the value of the attachments property.
     *
     * @return
     *     possible object is
     *     {@link WSOCollection }
     *
     */
    public WSOCollection getAttachments() {
        return attachments;
    }

    /**
     * Sets the value of the attachments property.
     *
     * @param value
     *     allowed object is
     *     {@link WSOCollection }
     *
     */
    public void setAttachments(WSOCollection value) {
        this.attachments = value;
    }

}
