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
 * <p>Java class for notificationType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="notificationType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="���������������"/>
 *     &lt;enumeration value="�������� � �����������"/>
 *     &lt;enumeration value="�������� �����������"/>
 *     &lt;enumeration value="������ �����������"/>
 *     &lt;enumeration value="������ ���������"/>
 *     &lt;enumeration value="����������"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "notificationType")
@XmlEnum
public enum NotificationType {

    @XmlEnumValue("\u0417\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043e\u0432\u0430\u043d")
    REGISTERED("\u0417\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043e\u0432\u0430\u043d"),
    @XmlEnumValue("\u041e\u0442\u043a\u0430\u0437\u0430\u043d\u043e \u0432 \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u0438")
    DENIED_REGISTRATION("\u041e\u0442\u043a\u0430\u0437\u0430\u043d\u043e \u0432 \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u0438"),
    @XmlEnumValue("\u041d\u0430\u0437\u043d\u0430\u0447\u0435\u043d \u0438\u0441\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044c")
    EXECUTOR_ASSIGNED("\u041d\u0430\u0437\u043d\u0430\u0447\u0435\u043d \u0438\u0441\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044c"),
    @XmlEnumValue("\u0414\u043e\u043a\u043b\u0430\u0434 \u043f\u043e\u0434\u0433\u043e\u0442\u043e\u0432\u043b\u0435\u043d")
    REPORT_PREPARED("\u0414\u043e\u043a\u043b\u0430\u0434 \u043f\u043e\u0434\u0433\u043e\u0442\u043e\u0432\u043b\u0435\u043d"),
    @XmlEnumValue("\u0414\u043e\u043a\u043b\u0430\u0434 \u043d\u0430\u043f\u0440\u0430\u0432\u043b\u0435\u043d")
    REPORT_SENT("\u0414\u043e\u043a\u043b\u0430\u0434 \u043d\u0430\u043f\u0440\u0430\u0432\u043b\u0435\u043d"),
    @XmlEnumValue("\u0418\u0441\u043f\u043e\u043b\u043d\u0435\u043d\u0438\u0435")
    EXECUTION("\u0418\u0441\u043f\u043e\u043b\u043d\u0435\u043d\u0438\u0435");
    private final String value;

    NotificationType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static NotificationType fromValue(String v) {
        for (NotificationType c: NotificationType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
