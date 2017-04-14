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
package com.aplana.dmsi.expansion;

import java.util.Collections;
import java.util.List;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dmsi.DMSIException;
import com.aplana.dmsi.card.CardHandler;
import com.aplana.dmsi.expansion.fsin.FSIN0100ExpansionType;
import com.aplana.dmsi.expansion.fsin.FSIN0100ForIncomeExpansionType;
import com.aplana.dmsi.object.DMSIObjectFactory;
import com.aplana.dmsi.types.Econtact;
import com.aplana.dmsi.types.ExpansionType;
import com.aplana.dmsi.types.Header;
import com.aplana.dmsi.types.ImportedIncomeDocumentType;
import com.aplana.dmsi.types.ImportedOGDocumentType;

public class FSIN0100ExpansionProcessor implements ExpansionProcessor {

	private String author;
	private String version;
	private List<Econtact> econtact = Collections.emptyList();

	public void importPreProcess(DataServiceFacade service, Header header) throws DMSIException {
	}

	public void importPostProcess(DataServiceFacade service, Header header) throws DMSIException {
		CardHandler cardHandler = new CardHandler(service);
		ExpansionType expansion = header.getExpansion();

		if (header.getDocument() != null && (header.getDocument() instanceof ImportedIncomeDocumentType || 
											 header.getDocument() instanceof ImportedOGDocumentType)) {
			expansion.setId(header.getDocument().getId());
			cardHandler.updateCard(expansion);
		}

	}

	public void fillExpansion(DataServiceFacade service, Header header, ObjectId cardId) throws DMSIException {
		DMSIObjectFactory objectFactory = DMSIObjectFactory.newInstance(service, "fsin.FSIN0100Expansion");
		FSIN0100ExpansionType expansion = (FSIN0100ExpansionType) objectFactory.newDMSIObject(cardId);
		expansion.setOrganization(author);
		expansion.setExpVer(version);
		expansion.getEcontact().addAll(econtact);
		header.setExpansion(expansion);
	}

	public String getAuthor() {
		return this.author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<Econtact> getEcontact() {
		return this.econtact;
	}

	public void setEcontact(List<Econtact> econtact) {
		if (econtact != null) {
			this.econtact = econtact;
		}
	}

}
