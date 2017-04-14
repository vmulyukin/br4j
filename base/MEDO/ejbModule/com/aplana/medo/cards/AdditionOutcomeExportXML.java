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
/**
 * 
 */
package com.aplana.medo.cards;

import java.util.Properties;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.aplana.dbmi.model.ObjectId;

/**
 * @author PPanichev
 *
 */
public class AdditionOutcomeExportXML {

    private Document xml = null;
    private Log logger = LogFactory.getLog(getClass());
    private static Properties propertiesSt;
    
    public AdditionOutcomeExportXML(Document xml) throws CardException {
	this.xml = xml;
	if (propertiesSt == null) throw new CardException("jbr.medo.card.additionoutcomeexportxml.notProperties");
	if (xml == null) throw new CardException("jbr.medo.card.AdditionOutcomeExportXML.notXML");
    }
    
    public static void setProperties(Properties properties) {
	propertiesSt = properties;
    }
    
    public Document additionXml(String uidDocument) throws CardException {
	String documentUidCode = propertiesSt.getProperty("code.document.uid");
	if (documentUidCode == null) {
	    logger.error("code.document.uid property should be set");
	    throw new CardException();
	}
	String headerUidCode = propertiesSt.getProperty("code.header.uid");
	if (headerUidCode == null) {
	    logger.error("code.header.uid property should be set");
	    throw new CardException();
	}

	xml.getDocumentElement().appendChild(
		CardXMLBuilder.createAttribute(xml, documentUidCode,
			CardXMLBuilder.STRING_TYPE, uidDocument));
	xml.getDocumentElement().appendChild(
		CardXMLBuilder.createAttribute(xml, headerUidCode,
			CardXMLBuilder.STRING_TYPE, UUID.randomUUID()
				.toString()));
	return xml;
    }
    
    public Document additionImportedDocUIDToXml(String uidImportedDoc) throws CardException {
	String uidImportedDocCode = propertiesSt.getProperty("code.document.links.link.uid");
	if (uidImportedDoc == null) {
	    logger.error("code.document.links.link.uid property should be set");
	    throw new CardException();
	}
	xml.getDocumentElement().appendChild(
		CardXMLBuilder.createAttribute(xml, uidImportedDocCode,
			CardXMLBuilder.STRING_TYPE, uidImportedDoc));
	return xml;
    }
    
    public Document addOrganizationToXml(String FullNameOrganization) {
	
	String recipientOrganization = propertiesSt.getProperty("code.document.addressees.addressee.organization");
	if (recipientOrganization == null || recipientOrganization.equals("")) {
	    logger.error("jbr.medo.card.AdditionOutcomeExportXML.code.document.addressees.addressee.organization property should be set");
	} else {
	    xml.getDocumentElement().appendChild(
			CardXMLBuilder.createAttribute(xml, recipientOrganization,
				CardXMLBuilder.STRING_TYPE, FullNameOrganization));
	}
	
	return xml;
    }

	public Document addSignatoryOrgToXml(String orgFullName) {

		String signatoryOrganization = propertiesSt.getProperty("code.document.signatories.signatory.organization");
		if (signatoryOrganization == null || signatoryOrganization.equals("")) {
			logger.error("jbr.medo.card.AdditionOutcomeExportXML.code.document.signatories.signatory.organization property should be set");
		} else {
			xml.getDocumentElement().appendChild(CardXMLBuilder.createAttribute(xml, signatoryOrganization, CardXMLBuilder.STRING_TYPE, orgFullName));
		}

		return xml;
	}

    public Document additionRegDateInBox(String regDateInBox) {
	
	String regDateInBoxCode = propertiesSt.getProperty("code.document.links.link.reference.num.data");
	if (regDateInBoxCode.equals("")) {
	    logger.error("jbr.medo.card.AdditionOutcomeExportXML.code.document.links.link.reference.num.data property should be set");
	} else {
	    xml.getDocumentElement().appendChild(
			CardXMLBuilder.createAttribute(xml, regDateInBoxCode,
				CardXMLBuilder.STRING_TYPE, regDateInBox));
	}
	
	return xml;
    }
    
    public Document additionRegNumberInBox(String regNumberInBox) {
	
	String regNumberInBoxCode = propertiesSt.getProperty("code.document.links.link.reference.num.number");
	if (regNumberInBoxCode.equals("")) {
	    logger.error("jbr.medo.card.AdditionOutcomeExportXML.code.document.links.link.reference.num.number property should be set");
	} else {
	    xml.getDocumentElement().appendChild(
			CardXMLBuilder.createAttribute(xml, regNumberInBoxCode,
				CardXMLBuilder.STRING_TYPE, regNumberInBox));
	}
	
	return xml;
    }
    
 public Document additionTypeLink(String typeLink) {
	
	String typeLinkCode = propertiesSt.getProperty("code.document.links.link.linkType");
	if (typeLinkCode.equals("")) {
	    logger.error("jbr.medo.card.AdditionOutcomeExportXML.code.document.links.link.linkType property should be set");
	} else {
	    xml.getDocumentElement().appendChild(
			CardXMLBuilder.createAttribute(xml, typeLinkCode,
				CardXMLBuilder.STRING_TYPE, typeLink));
	}
	
	return xml;
    }
}
