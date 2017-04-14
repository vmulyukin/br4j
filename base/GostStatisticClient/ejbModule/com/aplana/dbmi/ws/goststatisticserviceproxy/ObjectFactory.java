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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.aplana.dbmi.ws.goststatisticserviceproxy package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _GetStatistic_QNAME = new QName("http://aplana.com/dbmi/ws/GostStatisticService", "getStatistic");
    private final static QName _GetStatisticResponse_QNAME = new QName("http://aplana.com/dbmi/ws/GostStatisticService", "getStatisticResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.aplana.dbmi.ws.goststatisticserviceproxy
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetStatistic }
     * 
     */
    public GetStatistic createGetStatistic() {
        return new GetStatistic();
    }

    /**
     * Create an instance of {@link ReceiveStatisticResponse }
     * 
     */
    public ReceiveStatisticResponse createReceiveStatisticResponse() {
        return new ReceiveStatisticResponse();
    }

    /**
     * Create an instance of {@link SendStatisticResponse }
     * 
     */
    public SendStatisticResponse createSendStatisticResponse() {
        return new SendStatisticResponse();
    }

    /**
     * Create an instance of {@link GetStatisticResponse }
     * 
     */
    public GetStatisticResponse createGetStatisticResponse() {
        return new GetStatisticResponse();
    }

    /**
     * Create an instance of {@link GostStatisticResponse }
     * 
     */
    public GostStatisticResponse createGostStatisticResponse() {
        return new GostStatisticResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetStatistic }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://aplana.com/dbmi/ws/GostStatisticService", name = "getStatistic")
    public JAXBElement<GetStatistic> createGetStatistic(GetStatistic value) {
        return new JAXBElement<GetStatistic>(_GetStatistic_QNAME, GetStatistic.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetStatisticResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://aplana.com/dbmi/ws/GostStatisticService", name = "getStatisticResponse")
    public JAXBElement<GetStatisticResponse> createGetStatisticResponse(GetStatisticResponse value) {
        return new JAXBElement<GetStatisticResponse>(_GetStatisticResponse_QNAME, GetStatisticResponse.class, null, value);
    }

}
