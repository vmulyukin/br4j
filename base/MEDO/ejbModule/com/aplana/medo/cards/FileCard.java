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
package com.aplana.medo.cards;

import java.io.File;

import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dmsi.DMSIException;
import com.aplana.dmsi.card.CardHandler;
import com.aplana.medo.CardImportClient;
import com.aplana.medo.types.ReferenceXMLImage;

/**
 * Class allows to create card in system according to given from file system
 * file.
 */
public class FileCard {

    private static final long FILE_TEMPLATE_ID = (Long) ObjectId.predefined(
	    Template.class, "jbr.file").getId();
    private static final String NAME_ATTRIBUTE = (String) ObjectId.predefined(
	    StringAttribute.class, "name").getId();
    private static final String EDS_ATTRIBUTE = (String) ObjectId.predefined(
	    HtmlAttribute.class, "jbr.uzdo.signature").getId();
    private static final String MATERIAL_NAME_ATTRIBUTE = (String) ObjectId.predefined(
	    StringAttribute.class, "jbr.materialName").getId();
    private static final String DESCR_ATTRIBUTE = "DESCR";

    private DataServiceFacade serviceBean;
    private String fileName;
    private String fileDescription;
    private File dir;
    private boolean isLink;
    private String eds;

    public FileCard(String url) {
	this.fileName = url;
	isLink = true;
    }

    public FileCard(File dir, String fileName, String fileDescription) {
	this.dir = dir;
	this.fileName = fileName;
	this.fileDescription = fileDescription;
    }

    public String getEds() {
	return this.eds;
    }

    public void setEds(String eds) {
	this.eds = eds;
    }

    /**
     * Creates an XML that describe document of 'file' type and call import card
     * service to load this document to system.
     *
     * @return cardId of created card or -1 in case of error
     * @see CardImportClient
     * @throws CardException
     */
    public long createCard() throws CardException {
	if (isLink) {
	    try {
		CardHandler cardHandler = new CardHandler(serviceBean);
		ReferenceXMLImage fileImage = new ReferenceXMLImage();
		fileImage.setFilename(fileName);
		return (Long) cardHandler.createCard(fileImage).getId();
	    } catch (DMSIException ex) {
		throw new CardException(ex);
	    }
	}
	CardXMLBuilder cardXmlBuilder = new CardXMLBuilder();
	cardXmlBuilder.setTemplateId(FILE_TEMPLATE_ID);

	cardXmlBuilder.appendAttribute(NAME_ATTRIBUTE,
		CardXMLBuilder.STRING_TYPE, fileName);
	cardXmlBuilder.appendAttribute(DESCR_ATTRIBUTE,
		CardXMLBuilder.TEXT_TYPE, fileDescription);
	cardXmlBuilder.appendAttribute(MATERIAL_NAME_ATTRIBUTE,
		CardXMLBuilder.STRING_TYPE, fileName);

	if (eds != null && !"".equals(eds)) {
	    cardXmlBuilder.appendAttribute(EDS_ATTRIBUTE,
		    CardXMLBuilder.HTML_TYPE, eds);
	}

	File material = new File(dir, fileName);
	if (!material.exists()) {
	    throw new CardException("jbr.medo.card.file.materialNotFound",
		    new Object[] { material.getAbsolutePath() });
	}

	return CardImportClient.callImportCardService(cardXmlBuilder
		.serialize().toByteArray(), material);
    }

    public DataServiceFacade getServiceBean() {
        return this.serviceBean;
}

    public void setServiceBean(DataServiceFacade serviceBean) {
        this.serviceBean = serviceBean;
    }
}
