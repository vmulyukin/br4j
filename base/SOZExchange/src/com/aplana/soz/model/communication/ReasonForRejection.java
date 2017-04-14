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
 * <p>Java class for reasonForRejection.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="reasonForRejection">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}normalizedString">
 *     &lt;enumeration value="�� �������� �����������"/>
 *     &lt;enumeration value="������ ���������"/>
 *     &lt;enumeration value="�� ������ �������������"/>
 *     &lt;enumeration value="��� �������� ���������"/>
 *     &lt;enumeration value="����������� �����"/>
 *     &lt;enumeration value="��� �������"/>
 *     &lt;enumeration value="���������� �����������"/>
 *     &lt;enumeration value="������ ���������� ������ ��������"/>
 *     &lt;enumeration value="������������ ���������� ���������� � ������������ �����������"/>
 *     &lt;maxLength value="127"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "reasonForRejection")
@XmlEnum
public enum ReasonForRejection {

    @XmlEnumValue("\u041d\u0435 \u043f\u043e\u0434\u043b\u0435\u0436\u0438\u0442 \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u0438")
    NOT_FOR_REGISTRATION("\u041d\u0435 \u043f\u043e\u0434\u043b\u0435\u0436\u0438\u0442 \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u0438"),
    @XmlEnumValue("\u041e\u0448\u0438\u0431\u043a\u0430 \u0430\u0434\u0440\u0435\u0441\u0430\u0446\u0438\u0438")
    ADDRESSING_ERROR("\u041e\u0448\u0438\u0431\u043a\u0430 \u0430\u0434\u0440\u0435\u0441\u0430\u0446\u0438\u0438"),
    @XmlEnumValue("\u041d\u0435 \u0443\u043a\u0430\u0437\u0430\u043d \u043a\u043e\u0440\u0440\u0435\u0441\u043f\u043e\u043d\u0434\u0435\u043d\u0442")
    HAS_NO_RECIPIENT("\u041d\u0435 \u0443\u043a\u0430\u0437\u0430\u043d \u043a\u043e\u0440\u0440\u0435\u0441\u043f\u043e\u043d\u0434\u0435\u043d\u0442"),
    @XmlEnumValue("\u041d\u0435\u0442 \u0438\u0441\u043a\u043e\u0432\u043e\u0433\u043e \u0437\u0430\u044f\u0432\u043b\u0435\u043d\u0438\u044f")
    HAS_NO_STATEMENT_OF_CLAIM("\u041d\u0435\u0442 \u0438\u0441\u043a\u043e\u0432\u043e\u0433\u043e \u0437\u0430\u044f\u0432\u043b\u0435\u043d\u0438\u044f"),
    @XmlEnumValue("\u041e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u0435\u0442 \u0442\u0435\u043a\u0441\u0442")
    NO_TEXT("\u041e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u0435\u0442 \u0442\u0435\u043a\u0441\u0442"),
    @XmlEnumValue("\u041d\u0435\u0442 \u043f\u043e\u0434\u043f\u0438\u0441\u0438")
    NO_SIGN("\u041d\u0435\u0442 \u043f\u043e\u0434\u043f\u0438\u0441\u0438"),
    @XmlEnumValue("\u041f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u0435 \u043e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u0435\u0442")
    NO_ATTACHMENT("\u041f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u0435 \u043e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u0435\u0442"),
    @XmlEnumValue("\u041b\u0438\u0441\u0442\u0430\u0436 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u044f \u0443\u043a\u0430\u0437\u0430\u043d \u043e\u0448\u0438\u0431\u043e\u0447\u043d\u043e")
    WRONG_ATTACHMENT_PAGING("\u041b\u0438\u0441\u0442\u0430\u0436 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u044f \u0443\u043a\u0430\u0437\u0430\u043d \u043e\u0448\u0438\u0431\u043e\u0447\u043d\u043e"),
    @XmlEnumValue("\u041d\u0435\u0441\u043e\u0432\u043f\u0430\u0434\u0435\u043d\u0438\u0435 \u0440\u0435\u043a\u0432\u0438\u0437\u0438\u0442\u043e\u0432 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u044f \u0441 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u043d\u044b\u043c\u0438 \u0434\u043e\u043a\u0443\u043c\u0435\u043d\u0442\u0430\u043c\u0438")
    ATTACHMENT_DETAILS_MISMATCHES_WITH_ATTACHED_DOCUMENTS("\u041d\u0435\u0441\u043e\u0432\u043f\u0430\u0434\u0435\u043d\u0438\u0435 \u0440\u0435\u043a\u0432\u0438\u0437\u0438\u0442\u043e\u0432 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u044f \u0441 \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u043d\u044b\u043c\u0438 \u0434\u043e\u043a\u0443\u043c\u0435\u043d\u0442\u0430\u043c\u0438");
    private final String value;

    ReasonForRejection(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ReasonForRejection fromValue(String v) {
        for (ReasonForRejection c: ReasonForRejection.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
