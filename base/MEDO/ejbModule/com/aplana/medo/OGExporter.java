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
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dmsi.DMSIException;
import com.aplana.dmsi.object.DMSIObjectFactory;
import com.aplana.medo.types.Info;
import com.aplana.medo.types.OutcomeXMLPacket;

public class OGExporter {

    private static final ObjectId DOC_TYPE_ATTRIBUTE_ID = ObjectId.predefined(
	    CardLinkAttribute.class, "jbr.reg.doctype");
    private static final ObjectId INFORMATION_REQUEST_DOC_TYPE = ObjectId
    .predefined(Card.class,
        "jbr.medo_og.informationRequestDocType");
    private static final ObjectId INFORMATION_RESPONSE_DOC_TYPE = ObjectId
	    .predefined(Card.class,
		    "jbr.medo_og.informationResponseDocType");
    private static final ObjectId REQ_TYPE_ATTRIBUTE_ID = ObjectId.predefined(
	    ListAttribute.class, "jbr.medo_og.reqType");
    private static final ObjectId FULL_REQ_TYPE = ObjectId.predefined(
	    ReferenceValue.class, "jbr.medo_og.reqType.full");
    private static final ObjectId SHORT_REQ_TYPE = ObjectId.predefined(
	    ReferenceValue.class, "jbr.medo_og.reqType.short");

    private Card card;
    private DataServiceFacade serviceBean;

    public OGExporter(Card card) {
	this.card = card;
    }

    public Document export() throws MedoException {
	String requiredType = resolveRequiredType();
	if (requiredType == null) {
	    throw new MedoException("jbr.medo.export.unknownFormat",
		    new Object[] { card.getId() });
	}

	try {
	    JAXBContext context = JAXBContext
		    .newInstance("com.aplana.medo.types");
	    Marshaller m = context.createMarshaller();
	    DMSIObjectFactory objectFactory = DMSIObjectFactory.newInstance(serviceBean, requiredType);
	    OutcomeXMLPacket packet = (OutcomeXMLPacket) objectFactory
		    .newDMSIObject(card.getId());
	    DocumentBuilderFactory factory = DocumentBuilderFactory
		    .newInstance();
	    factory.setNamespaceAware(true);
	    Document document = factory.newDocumentBuilder().newDocument();
	    // FIXME: (N.Zhegalin) Probably either all documents should be put
	    // in one XML or maybe be separated in several files
	    for (Info info : packet.getInfos()) {
		packet.setInfo(info);
	    }
	    m.marshal(packet, document);
	    return document;
	} catch (JAXBException ex) {
	    throw new MedoException("jbr.medo.system", ex);
	} catch (DMSIException ex) {
	    throw new MedoException("jbr.medo.cardToObjectError", ex);
	} catch (ParserConfigurationException ex) {
	    throw new MedoException("jbr.medo.system", ex);
	}
    }

    private String resolveRequiredType() {
	String requiredType = null;
	DocType docType = resolveDocumentType();
	ReqType reqType = resolveRequestType();
	switch (docType) {
	case REQUEST:
	    requiredType = "Request";
	    break;
	case RESPONSE:
	    switch (reqType) {
	    case FULL:
		requiredType = "FullResponse";
		break;
	    case SHORT:
		requiredType = "ShortResponse";
		break;
	    default:
		break;
	    }
	    break;
	default:
	    break;
	}
	return requiredType;
    }

    private DocType resolveDocumentType() {
	CardLinkAttribute attribute = (CardLinkAttribute) card
		.getAttributeById(DOC_TYPE_ATTRIBUTE_ID);
	if (attribute == null) {
	    return DocType.UNDEFINED;
	}
	ObjectId docTypeValueId = attribute.getSingleLinkedId();
	if (INFORMATION_REQUEST_DOC_TYPE.equals(docTypeValueId)) {
	    return DocType.REQUEST;
	} else if (INFORMATION_RESPONSE_DOC_TYPE.equals(docTypeValueId)) {
	    return DocType.RESPONSE;
	}
	return DocType.UNDEFINED;
    }

    private ReqType resolveRequestType() {
	ListAttribute attribute = (ListAttribute) card
		.getAttributeById(REQ_TYPE_ATTRIBUTE_ID);
	if (attribute == null || attribute.getValue() == null) {
	    return ReqType.UNDEFINED;
	}
	ObjectId reqTypeValueId = attribute.getValue().getId();
	if (FULL_REQ_TYPE.equals(reqTypeValueId)) {
	    return ReqType.FULL;
	} else if (SHORT_REQ_TYPE.equals(reqTypeValueId)) {
	    return ReqType.SHORT;
	}
	return ReqType.UNDEFINED;
    }

    private static enum ReqType {
	FULL, SHORT, UNDEFINED
    }

    private static enum DocType {
	REQUEST, RESPONSE, UNDEFINED
    }

    public DataServiceFacade getServiceBean() {
        return this.serviceBean;
}

    public void setServiceBean(DataServiceFacade serviceBean) {
        this.serviceBean = serviceBean;
    }
}
