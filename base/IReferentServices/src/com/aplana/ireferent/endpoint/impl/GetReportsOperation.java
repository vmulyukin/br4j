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

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.ws.WebServiceContext;

import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.WSObjectFactory;
import com.aplana.ireferent.types.WSOContext;
import com.aplana.ireferent.types.WSOWrapper;
import com.aplana.ireferent.util.ServiceUtils;

public class GetReportsOperation implements ServiceOperation<WSOWrapper> {
    private final String id;
    private final boolean isMObject;
    private final WSOContext context;
    private final boolean includeAttachments;
    private final ObjectId REPORTS_ID = ObjectId.predefined(
	    BackLinkAttribute.class, "jbr.reports");
    private ObjectId taskCardId;
    private DataServiceBean serviceBean;
    private final WebServiceContext contextEndpoint;

    public GetReportsOperation(String id, boolean isMObject,
	    boolean includeAttachments, WSOContext context, WebServiceContext contextEndpoint) {
	this.id = id;
	this.isMObject = isMObject;
	this.context = context;
	this.includeAttachments = includeAttachments;
	this.contextEndpoint = contextEndpoint;
    }

    public String getName() {
	return "getReports";
    }

    public Object[] getParameters() {
	return new Object[] { this.id, this.isMObject, this.includeAttachments,
		this.context };
    }

    public void processInputData() {
	if (this.id == null || "".equals(this.id)) {
	    throw new IllegalArgumentException("Id of task should be not empty");
	}

	try {
	    taskCardId = new ObjectId(Card.class, Long.parseLong(this.id));
	} catch (NumberFormatException ex) {
	    throw new IllegalArgumentException(
		    "Card id should have numer format, but was " + this.id);
	}

    }

    public WSOWrapper execute() throws Exception {
	serviceBean = ServiceUtils.authenticateUser(this.context, this.contextEndpoint);
	Card taskCard = findTask();
	ObjectId[] reportIds = getReportIds(taskCard);
	WSObjectFactory objectFactory = WSObjectFactory.newInstance(
		serviceBean, "TaskReport");
	objectFactory.setMObject(this.isMObject);
	objectFactory.setUsingMObjectsAllLevels("attachments",
		!this.includeAttachments);
	objectFactory.addIgnoredAllLevel("childs");
	WSOWrapper wrapper = new WSOWrapper();
	wrapper.setCollection(objectFactory.newWSOCollection(reportIds));
	return wrapper;
    }

    private Card findTask() {
	Collection<ObjectId> requiredAttributes = new ArrayList<ObjectId>();
	requiredAttributes.add(REPORTS_ID);
	Collection<Card> taskCards = ServiceUtils.fetchCards(serviceBean,
		new ObjectId[] { taskCardId }, requiredAttributes);
	if (taskCards.isEmpty()) {
	    throw new IllegalArgumentException(String.format(
		    "Card with id=%s was not found", taskCardId));
	}
	return taskCards.iterator().next();
    }

    private ObjectId[] getReportIds(Card taskCard) {
	CardLinkAttribute reportsAttribute = (CardLinkAttribute) taskCard
		.getAttributeById(REPORTS_ID);
	if (reportsAttribute == null)
	    return new ObjectId[] {};
	return reportsAttribute.getIdsArray();
    }
}