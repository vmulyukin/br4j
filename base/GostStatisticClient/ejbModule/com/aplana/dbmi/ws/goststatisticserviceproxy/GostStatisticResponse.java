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

package com.aplana.dbmi.ws.goststatisticserviceproxy;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for gostStatisticResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="gostStatisticResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sendStatisticResponseList" type="{http://aplana.com/dbmi/ws/GostStatisticService}sendStatisticResponse" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="receiveStatisticResponseList" type="{http://aplana.com/dbmi/ws/GostStatisticService}receiveStatisticResponse" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "gostStatisticResponse", propOrder = {
    "sendStatisticResponseList",
    "receiveStatisticResponseList"
})
public class GostStatisticResponse {

    @XmlElement(nillable = true)
    protected List<SendStatisticResponse> sendStatisticResponseList;
    @XmlElement(nillable = true)
    protected List<ReceiveStatisticResponse> receiveStatisticResponseList;

    /**
     * Gets the value of the sendStatisticResponseList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sendStatisticResponseList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSendStatisticResponseList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SendStatisticResponse }
     * 
     * 
     */
    public List<SendStatisticResponse> getSendStatisticResponseList() {
        if (sendStatisticResponseList == null) {
            sendStatisticResponseList = new ArrayList<SendStatisticResponse>();
        }
        return this.sendStatisticResponseList;
    }

    /**
     * Gets the value of the receiveStatisticResponseList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the receiveStatisticResponseList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReceiveStatisticResponseList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ReceiveStatisticResponse }
     * 
     * 
     */
    public List<ReceiveStatisticResponse> getReceiveStatisticResponseList() {
        if (receiveStatisticResponseList == null) {
            receiveStatisticResponseList = new ArrayList<ReceiveStatisticResponse>();
        }
        return this.receiveStatisticResponseList;
    }

}
