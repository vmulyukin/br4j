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

import java.io.File;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.medo.Importer.Data;

// TODO: (N.Zhegalin) Create configs for rules description
public class FormatsManager {

    private static final String OG_DOCUMENT_FILENAME_PART = "��������� �������";

    private Log logger = LogFactory.getLog(getClass());

    private static FormatsManager instance;
    private static final String MEDO_OG_ROOT_ELEMENT_NAME = "XMLPacket";
    private static final String MEDO_ROOT_ELEMENT_NAME = "communication";

    private static final ObjectId DOC_TYPE_ATTRIBUTE_ID = ObjectId.predefined(
	    CardLinkAttribute.class, "jbr.reg.doctype");
    private static final ObjectId INFORMATION_REQUEST_DOC_TYPE = ObjectId
	    .predefined(Card.class,
		    "jbr.medo_og.informationRequestDocType");
    private static final ObjectId INFORMATION_RESPONSE_DOC_TYPE = ObjectId
	    .predefined(Card.class,
		    "jbr.medo_og.informationResponseDocType");

    private FormatsManager() {
    }

    public static FormatsManager instance() {
	if (instance == null) {
	    instance = new FormatsManager();
	}
	return instance;
    }

    public static enum Format {
	MEDO, MEDO_OG, OG_DOC, UNDEFINED
    }

    public Format resolve(Data data) throws MedoException {
	File parentFolder = data.getFile().getParentFile();
	if (parentFolder == null || !parentFolder.isDirectory()) {
	    throw new IllegalStateException(
		    "Error during format resolving. Parent of "
			    + data.getFile().getAbsolutePath()
			    + "is not directory ");
	}
	String folderName = parentFolder.getName();
	boolean isOG = folderName.contains(OG_DOCUMENT_FILENAME_PART);

	Document doc = data.getDocument();
	if (doc == null) {
	    return Format.UNDEFINED;
	}
	String rootElementName = doc.getDocumentElement().getLocalName();
	if (MEDO_OG_ROOT_ELEMENT_NAME.equals(rootElementName)) {
	    return Format.MEDO_OG;
	} else if (MEDO_ROOT_ELEMENT_NAME.equals(rootElementName)) {
	    if (isOG) {
		return Format.OG_DOC;
	    }
	    return Format.MEDO;
	}
	return Format.UNDEFINED;
    }

    public Format resolve(Card card) {
	if (card == null) {
	    logger.debug("Format is undefined because card is null");
	    return Format.UNDEFINED;
	}
	CardLinkAttribute docTypeAttribute = (CardLinkAttribute) card
		.getAttributeById(DOC_TYPE_ATTRIBUTE_ID);
	if (docTypeAttribute == null) {
	    logger.debug("Format is undefined because document type  is null");
	    return Format.UNDEFINED;
	}	
	ObjectId docTypeValueId = docTypeAttribute.getSingleLinkedId();
	if (docTypeValueId == null)
		return Format.UNDEFINED;
	if (INFORMATION_REQUEST_DOC_TYPE.equals(docTypeValueId)
		|| INFORMATION_RESPONSE_DOC_TYPE.equals(docTypeValueId)) {
	    return Format.MEDO_OG;
	}
	return Format.MEDO;
    }
}
