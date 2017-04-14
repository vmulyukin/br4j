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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum(Byte.class)
public enum EcontactEnumType {
    @XmlEnumValue("0")
    OTHER, //
    @XmlEnumValue("1")
    WORK_PHONE, //
    @XmlEnumValue("2")
    HOME_PHONE, //
    @XmlEnumValue("3")
    MOBILE_PHONE, //
    @XmlEnumValue("4")
    FAX, //
    @XmlEnumValue("5")
    EMAIL, //
    @XmlEnumValue("6")
    URL
}
