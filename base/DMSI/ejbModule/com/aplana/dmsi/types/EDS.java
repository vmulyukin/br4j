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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EDS")
public class EDS {
    @XmlValue
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    @XmlSchemaType(name = "hexBinary")
    private byte[] value;
    @XmlAttribute(name = "date")
    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar date;
    @XmlAttribute(name = "kind")
    private String kind;
    @XmlAttribute(name = "id_kind")
    private EDSIdKindEnumType idKind;
    @XmlAttribute(name = "certificate")
    private String certificate;
    @XmlAttribute(name = "certificate_owner")
    private String certificate_owner;

	public byte[] getValue() {
	return this.value;
    }

    public void setValue(byte[] value) {
	this.value = value;
    }

    public XMLGregorianCalendar getDate() {
	return this.date;
    }

    public void setDate(XMLGregorianCalendar date) {
	this.date = date;
    }

    public String getKind() {
	return this.kind;
    }

    public void setKind(String kind) {
	this.kind = kind;
    }

    public String getCertificate() {
	return this.certificate;
    }

    public void setCertificate(String certificate) {
	this.certificate = certificate;
    }

    public EDSIdKindEnumType getIdKind() {
	return this.idKind;
    }

    public void setIdKind(EDSIdKindEnumType idKind) {
	this.idKind = idKind;
    }
    public String getCertificate_owner() {
		return certificate_owner;
	}

	public void setCertificate_owner(String certificate_owner) {
		this.certificate_owner = certificate_owner;
	}
}
