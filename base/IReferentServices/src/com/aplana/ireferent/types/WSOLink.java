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
/**
 *
 */
package com.aplana.ireferent.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WSO_LINK", propOrder = { "source", "target" })
public class WSOLink extends WSObject {
    @XmlElement(name = "SOURCE", required = true, nillable = false)
    WSOMDocument source;
    @XmlElement(name = "TARGET", required = true, nillable = false)
    WSOMDocument target;
    /**
     * @return the source
     */
    public WSOMDocument getSource() {
        return this.source;
    }
    /**
     * @param source the source to set
     */
    public void setSource(WSOMDocument source) {
        this.source = source;
    }
    /**
     * @return the target
     */
    public WSOMDocument getTarget() {
        return this.target;
    }
    /**
     * @param target the target to set
     */
    public void setTarget(WSOMDocument target) {
        this.target = target;
    }


}
