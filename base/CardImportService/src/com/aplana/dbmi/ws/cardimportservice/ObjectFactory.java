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

package com.aplana.dbmi.ws.cardimportservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {

    private final static QName _ImportCardResponse_QNAME = new QName("http://aplana.com/dbmi/ws/CardImportService", "importCardResponse");
    private final static QName _ImportCard_QNAME = new QName("http://aplana.com/dbmi/ws/CardImportService", "importCard");

    public ObjectFactory() {
    }

    public ImportCard createImportCard() {
        return new ImportCard();
    }

    public ImportCardResponse createImportCardResponse() {
        return new ImportCardResponse();
    }

    @XmlElementDecl(namespace = "http://aplana.com/dbmi/ws/CardImportService", name = "importCardResponse")
    public JAXBElement<ImportCardResponse> createImportCardResponse(ImportCardResponse value) {
        return new JAXBElement<ImportCardResponse>(_ImportCardResponse_QNAME, ImportCardResponse.class, null, value);
    }

    @XmlElementDecl(namespace = "http://aplana.com/dbmi/ws/CardImportService", name = "importCard")
    public JAXBElement<ImportCard> createImportCard(ImportCard value) {
        return new JAXBElement<ImportCard>(_ImportCard_QNAME, ImportCard.class, null, value);
    }

}
