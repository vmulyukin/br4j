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
package com.aplana.ireferent.endpoint.impl;

import javax.xml.ws.WebServiceContext;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.WSObjectFactory;
import com.aplana.ireferent.types.WSOContext;
import com.aplana.ireferent.types.WSOMDocument;
import com.aplana.ireferent.util.ServiceUtils;

class GetDocumentOperation implements ServiceOperation<WSOMDocument> {

    private final WSOContext context;
    private final String id;
    private ObjectId cardId;
    private WebServiceContext contextEndpoint;

    public GetDocumentOperation(String id, WSOContext context, WebServiceContext contextEndpoint) {
	this.context = context;
	this.id = id;
	this.contextEndpoint =  contextEndpoint;
    }

    public String getName() {
	return "getDocument";
    }

    public Object[] getParameters() {
	return new Object[] { this.id, this.context };
    }

    public void processInputData() {
		if (this.id == null || "".equals(this.id)) {
		    throw new IllegalArgumentException(
			    "Id of document should be not empty");
		}
	
		try {
		    cardId = new ObjectId(Card.class, Long.parseLong(this.id));
		} catch (NumberFormatException ex) {
		    throw new IllegalArgumentException(
			    "Card id should have numer format, but was " + this.id);
		}
    }

    public WSOMDocument execute() throws Exception {
	DataServiceBean serviceBean = ServiceUtils
		.authenticateUser(this.context, this.contextEndpoint);
	WSObjectFactory objectFactory = WSObjectFactory.newInstance(
		serviceBean, "Document");
	objectFactory.setMObject(false);
	objectFactory.setUsingMObjectsForCurrentLevel("attachments", false);
	return (WSOMDocument) objectFactory.newWSObject(cardId);
    }
}