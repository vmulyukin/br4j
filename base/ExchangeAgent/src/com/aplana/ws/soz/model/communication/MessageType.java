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

package com.aplana.ws.soz.model.communication;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for messageType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="messageType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="��������"/>
 *     &lt;enumeration value="�����������"/>
 *     &lt;enumeration value="���������"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "messageType")
@XmlEnum
public enum MessageType {

    @XmlEnumValue("\u0414\u043e\u043a\u0443\u043c\u0435\u043d\u0442")
    DOCUMENT("\u0414\u043e\u043a\u0443\u043c\u0435\u043d\u0442"),
    @XmlEnumValue("\u0423\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u0435")
    NOTIFICATION("\u0423\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u0435"),
    @XmlEnumValue("\u041a\u0432\u0438\u0442\u0430\u043d\u0446\u0438\u044f")
    RECEIPT("\u041a\u0432\u0438\u0442\u0430\u043d\u0446\u0438\u044f");
    private final String value;

    MessageType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MessageType fromValue(String v) {
        for (MessageType c: MessageType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
