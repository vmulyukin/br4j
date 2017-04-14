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
package com.aplana.medo.types.cardInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "rubricCode", "rubricGUID" })
public class Rubric {
    @XmlElement(name = "RubricCode")
    private String rubricCode;
    @XmlElement(name = "RubricGUID")
    private String rubricGUID;

    public String getRubricCode() {
	return this.rubricCode;
    }

    public void setRubricCode(String rubricCode) {
	this.rubricCode = rubricCode;
    }

    public String getRubricGUID() {
	return this.rubricGUID;
    }

    public void setRubricGUID(String rubricGUID) {
	this.rubricGUID = rubricGUID;
    }
}
