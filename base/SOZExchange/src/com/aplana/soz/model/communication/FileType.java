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

package com.aplana.soz.model.communication;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for fileType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="fileType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="pdf"/>
 *     &lt;enumeration value="tif"/>
 *     &lt;enumeration value="doc"/>
 *     &lt;enumeration value="txt"/>
 *     &lt;enumeration value="reference"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "fileType")
@XmlEnum
public enum FileType {

    @XmlEnumValue("pdf")
    PDF("pdf"),
    @XmlEnumValue("tif")
    TIF("tif"),
    @XmlEnumValue("doc")
    DOC("doc"),
    @XmlEnumValue("txt")
    TXT("txt"),
    @XmlEnumValue("reference")
    REFERENCE("reference");
    private final String value;

    FileType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FileType fromValue(String v) {
        for (FileType c: FileType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
