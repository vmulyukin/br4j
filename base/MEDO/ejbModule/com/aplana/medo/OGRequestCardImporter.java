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
package com.aplana.medo;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.w3c.dom.Document;

import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dmsi.DMSIException;
import com.aplana.dmsi.card.CardHandler;
import com.aplana.medo.Importer.CardImporter;
import com.aplana.medo.Importer.Data;
import com.aplana.medo.types.ImportedDocument;
import com.aplana.medo.types.IncomeXMLPacket;
import com.aplana.medo.types.XMLPacket;
import com.aplana.medo.types.adapters.XMLPacketAdapter;

public class OGRequestCardImporter implements CardImporter {

    private DataServiceFacade serviceBean;

    public long importCard(Data data, long importedDocCardId)
	    throws MedoException {
	return importByMedoOG(data.getDocument(), importedDocCardId);
    }

    private long importByMedoOG(Document document, long importedDocCardId)
	    throws MedoException {
	try {
	    JAXBContext context = JAXBContext
		    .newInstance("com.aplana.medo.types");
	    Unmarshaller um = context.createUnmarshaller();
	    XMLPacket packet = (XMLPacket) um.unmarshal(document);
	    IncomeXMLPacket incomePacket = (IncomeXMLPacket) new XMLPacketAdapter()
		    .unmarshal(packet);
	    ImportedDocument importedDocument = new ImportedDocument();
	    importedDocument.setId(String.valueOf(importedDocCardId));
	    incomePacket.setImportedDocument(importedDocument);
	    CardHandler cardHandler = new CardHandler(serviceBean);
	    return (Long) cardHandler.createCard(incomePacket).getId();
	} catch (JAXBException ex) {
	    throw new MedoException("jbr.medo.system", ex);
	} catch (ServiceException ex) {
	    throw new MedoException("jbr.medo.system", ex);
	} catch (DMSIException ex) {
	    throw new MedoException("jbr.medo.objectToCardError", ex);
	} catch (Exception ex) {
	    throw new MedoException("jbr.medo.system", ex);
	}
    }

    public void setDataServiceBean(DataServiceFacade serviceFacade) {
	this.serviceBean = serviceFacade;
}

}
