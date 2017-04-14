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
 * <p>Java class for linkType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="linkType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="� ����� ��"/>
 *     &lt;enumeration value="�� ����������"/>
 *     &lt;enumeration value="��������-���������"/>
 *     &lt;enumeration value="�� �"/>
 *     &lt;enumeration value="�����"/>
 *     &lt;enumeration value="������ �"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "linkType")
@XmlEnum
public enum LinkType {

    @XmlEnumValue("\u0412 \u043e\u0442\u0432\u0435\u0442 \u043d\u0430")
    RESPONSE_FOR("\u0412 \u043e\u0442\u0432\u0435\u0442 \u043d\u0430"),
    @XmlEnumValue("\u0412\u043e \u0438\u0441\u043f\u043e\u043b\u043d\u0435\u043d\u0438\u0435")
    IN_EXECUTION("\u0412\u043e \u0438\u0441\u043f\u043e\u043b\u043d\u0435\u043d\u0438\u0435"),
    @XmlEnumValue("\u0414\u043e\u043a\u0443\u043c\u0435\u043d\u0442-\u043e\u0441\u043d\u043e\u0432\u0430\u043d\u0438\u0435")
    BASE_DOCUMENT("\u0414\u043e\u043a\u0443\u043c\u0435\u043d\u0442-\u043e\u0441\u043d\u043e\u0432\u0430\u043d\u0438\u0435"),
    @XmlEnumValue("\u041d\u0430 \u2116")
    FOR("\u041d\u0430 \u2116"),
    @XmlEnumValue("\u041e\u0442\u0432\u0435\u0442")
    RESPONSE("\u041e\u0442\u0432\u0435\u0442"),
    @XmlEnumValue("\u0421\u0432\u044f\u0437\u0430\u043d \u0441")
    LINKED_TO("\u0421\u0432\u044f\u0437\u0430\u043d \u0441");
    private final String value;

    LinkType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LinkType fromValue(String v) {
        for (LinkType c: LinkType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
