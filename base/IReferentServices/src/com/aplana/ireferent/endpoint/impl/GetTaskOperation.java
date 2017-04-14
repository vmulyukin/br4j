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
import com.aplana.ireferent.types.WSOTask;
import com.aplana.ireferent.util.ServiceUtils;

public class GetTaskOperation implements ServiceOperation<WSOTask> {
    private final String id;
    private final WSOContext context;
    private final boolean includeAttachments;
    private ObjectId cardId;
    private final WebServiceContext contextEndpoint;

    public GetTaskOperation(String id, boolean includeAttachments,
	    WSOContext context, WebServiceContext contextEndpoint) {
	this.id = id;
	this.context = context;
	this.includeAttachments = includeAttachments;
	this.contextEndpoint = contextEndpoint;
    }

    public String getName() {
	return "getTask";
    }

    public Object[] getParameters() {
	return new Object[] { this.id, this.includeAttachments, this.context };
    }

    public void processInputData() {
	if (this.id == null || "".equals(this.id)) {
	    throw new IllegalArgumentException("Id of task should be not empty");
	}

	try {
	    cardId = new ObjectId(Card.class, Long.parseLong(this.id));
	} catch (NumberFormatException ex) {
	    throw new IllegalArgumentException(
		    "Card id should have numer format, but was " + this.id);
	}
    }

    public WSOTask execute() throws Exception {
	DataServiceBean serviceBean = ServiceUtils
		.authenticateUser(this.context, this.contextEndpoint);
	WSObjectFactory objectFactory = WSObjectFactory.newInstance(
		serviceBean, "Task");
	objectFactory.setMObject(false);
	objectFactory.setUsingMObjectsAllLevels("attachments",
		!this.includeAttachments);
	objectFactory.addIgnoredAllLevel("childs");
	return (WSOTask) objectFactory.newWSObject(cardId);
    }
}