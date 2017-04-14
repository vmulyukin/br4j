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

package com.aplana.dbmi.ws.zdproblemsserviceproxy;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.aplana.dbmi.ws.zdproblemsserviceproxy package. 
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

    private final static QName _GetZDProblemOrgsStatResponse_QNAME = new QName("http://aplana.com/dbmi/ws/ZoneDowProblemsService", "getZDProblemOrgsStatResponse");
    private final static QName _GetZDProblemOrgsStat_QNAME = new QName("http://aplana.com/dbmi/ws/ZoneDowProblemsService", "getZDProblemOrgsStat");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.aplana.dbmi.ws.zdproblemsserviceproxy
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ZoneDowProblemsResponse }
     * 
     */
    public ZoneDowProblemsResponse createZoneDowProblemsResponse() {
        return new ZoneDowProblemsResponse();
    }

    /**
     * Create an instance of {@link GetZDProblemOrgsStatResponse }
     * 
     */
    public GetZDProblemOrgsStatResponse createGetZDProblemOrgsStatResponse() {
        return new GetZDProblemOrgsStatResponse();
    }

    /**
     * Create an instance of {@link GetZDProblemOrgsStat }
     * 
     */
    public GetZDProblemOrgsStat createGetZDProblemOrgsStat() {
        return new GetZDProblemOrgsStat();
    }

    /**
     * Create an instance of {@link ZdOrganisation }
     * 
     */
    public ZdOrganisation createZdOrganisation() {
        return new ZdOrganisation();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetZDProblemOrgsStatResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://aplana.com/dbmi/ws/ZoneDowProblemsService", name = "getZDProblemOrgsStatResponse")
    public JAXBElement<GetZDProblemOrgsStatResponse> createGetZDProblemOrgsStatResponse(GetZDProblemOrgsStatResponse value) {
        return new JAXBElement<GetZDProblemOrgsStatResponse>(_GetZDProblemOrgsStatResponse_QNAME, GetZDProblemOrgsStatResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetZDProblemOrgsStat }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://aplana.com/dbmi/ws/ZoneDowProblemsService", name = "getZDProblemOrgsStat")
    public JAXBElement<GetZDProblemOrgsStat> createGetZDProblemOrgsStat(GetZDProblemOrgsStat value) {
        return new JAXBElement<GetZDProblemOrgsStat>(_GetZDProblemOrgsStat_QNAME, GetZDProblemOrgsStat.class, null, value);
    }

}
