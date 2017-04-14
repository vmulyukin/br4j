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
package com.aplana.medo.types.document;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for notificationType.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 *
 * <pre>
 * &lt;simpleType name=&quot;notificationType&quot;&gt;
 *   &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}token&quot;&gt;
 *     &lt;enumeration value=&quot;���������������&quot;/&gt;
 *     &lt;enumeration value=&quot;�������� � �����������&quot;/&gt;
 *     &lt;enumeration value=&quot;�������� �����������&quot;/&gt;
 *     &lt;enumeration value=&quot;������ �����������&quot;/&gt;
 *     &lt;enumeration value=&quot;������ ���������&quot;/&gt;
 *     &lt;enumeration value=&quot;����������&quot;/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 *
 */
@XmlType(name = "notificationType")
@XmlEnum
public enum NotificationType {

    @XmlEnumValue("���������������")
    REGISTERED, //
    @XmlEnumValue("�������� � �����������")
    REGISTRATION_REFUSED, //
    @XmlEnumValue("�������� �����������")
    EXECUTOR_ASSIGNED, //
    @XmlEnumValue("������ �����������")
    REPORT_PREPARED, //
    @XmlEnumValue("������ ���������")
    REPORT_SENT, //
    @XmlEnumValue("����������")
    EXECUTION;

}
