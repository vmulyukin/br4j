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

package com.aplana.dbmi.ws.docstatisticserviceproxy;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.aplana.dbmi.ws.docstatisticserviceproxy package. 
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

    private final static QName _GetDocumentsStatisticResponse_QNAME = new QName("http://aplana.com/dbmi/ws/DocsStatisticService", "getDocumentsStatisticResponse");
    private final static QName _GetDocumentsStatistic_QNAME = new QName("http://aplana.com/dbmi/ws/DocsStatisticService", "getDocumentsStatistic");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.aplana.dbmi.ws.docstatisticserviceproxy
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link OrganizationStatistic }
     * 
     */
    public OrganizationStatistic createOrganizationStatistic() {
        return new OrganizationStatistic();
    }

    /**
     * Create an instance of {@link GetDocumentsStatisticResponse }
     * 
     */
    public GetDocumentsStatisticResponse createGetDocumentsStatisticResponse() {
        return new GetDocumentsStatisticResponse();
    }

    /**
     * Create an instance of {@link GetDocumentsStatistic }
     * 
     */
    public GetDocumentsStatistic createGetDocumentsStatistic() {
        return new GetDocumentsStatistic();
    }

    /**
     * Create an instance of {@link TemplateStatistic }
     * 
     */
    public TemplateStatistic createTemplateStatistic() {
        return new TemplateStatistic();
    }

    /**
     * Create an instance of {@link DocsStatisticResponse }
     * 
     */
    public DocsStatisticResponse createDocsStatisticResponse() {
        return new DocsStatisticResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetDocumentsStatisticResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://aplana.com/dbmi/ws/DocsStatisticService", name = "getDocumentsStatisticResponse")
    public JAXBElement<GetDocumentsStatisticResponse> createGetDocumentsStatisticResponse(GetDocumentsStatisticResponse value) {
        return new JAXBElement<GetDocumentsStatisticResponse>(_GetDocumentsStatisticResponse_QNAME, GetDocumentsStatisticResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetDocumentsStatistic }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://aplana.com/dbmi/ws/DocsStatisticService", name = "getDocumentsStatistic")
    public JAXBElement<GetDocumentsStatistic> createGetDocumentsStatistic(GetDocumentsStatistic value) {
        return new JAXBElement<GetDocumentsStatistic>(_GetDocumentsStatistic_QNAME, GetDocumentsStatistic.class, null, value);
    }

}
