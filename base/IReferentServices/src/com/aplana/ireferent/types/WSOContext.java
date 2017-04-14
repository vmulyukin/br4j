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
package com.aplana.ireferent.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <p>
 * Java class for WSO_CONTEXT complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name=&quot;WSO_CONTEXT&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;USERID&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;/&gt;
 *         &lt;element name=&quot;CLIENTTYPE&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WSO_CONTEXT", propOrder = { "userId", "clientType", "dateSyncIn" })
public class WSOContext {
	
	public final static String DATE_SYNC_IN = "%DATE_SYNC_IN%";

    @XmlElement(name = "USERID", required = true, nillable = true)
    protected String userId;
    @XmlElement(name = "CLIENTTYPE", required = true, nillable = true)
    protected String clientType;
    @XmlElement(name = "DATESYNCIN", required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateSyncIn;
    /**
     * Gets the value of the userId property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getUserId() {
	return userId;
    }

    /**
     * Sets the value of the userId property.
     *
     * @param value
     *                allowed object is {@link String }
     *
     */
    public void setUserId(String value) {
	this.userId = value;
    }

    /**
     * Gets the value of the clientType property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getClientType() {
	return clientType;
    }

    /**
     * Sets the value of the clientType property.
     *
     * @param value
     *                allowed object is {@link String }
     *
     */
    public void setClientType(String value) {
	this.clientType = value;
    }

    public XMLGregorianCalendar getDateSyncIn() {
		return dateSyncIn;
	}

	public void setDateSyncIn(XMLGregorianCalendar dateRequest) {
		this.dateSyncIn = dateRequest;
	}

	/*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return String.format("WSOContext(userId=%s,clientType=%s, dateRequest=%s)", userId,
		clientType, dateSyncIn);
    }
}
