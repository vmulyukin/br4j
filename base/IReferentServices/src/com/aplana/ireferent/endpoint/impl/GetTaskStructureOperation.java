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

public class GetTaskStructureOperation implements ServiceOperation<WSOTask> {
    private final boolean isMObject;
    private final Integer childsLevel;
    private final boolean includeAttachments;
    private final WSOContext context;
    private final String rootTaskId;
    private ObjectId taskId;
    private final WebServiceContext contextEndpoint;

    public GetTaskStructureOperation(String rootTaskId, Integer childsLevel,
	    boolean isMObject, boolean includeAttachments, WSOContext context, WebServiceContext contextEndpoint) {
	this.isMObject = isMObject;
	this.childsLevel = childsLevel;
	this.includeAttachments = includeAttachments;
	this.context = context;
	this.rootTaskId = rootTaskId;
	this.contextEndpoint = contextEndpoint;
    }

    public String getName() {
	return "getStructure";
    }

    public Object[] getParameters() {
	return new Object[] { this.rootTaskId, this.childsLevel,
		this.isMObject, this.includeAttachments, this.context };
    }

    public void processInputData() {
	if (this.rootTaskId == null || "".equals(this.rootTaskId)) {
	    throw new IllegalArgumentException("Id of task should be not empty");
	}

	try {
	    taskId = new ObjectId(Card.class, Long.parseLong(this.rootTaskId));
	} catch (NumberFormatException ex) {
	    throw new IllegalArgumentException(
		    "Card id should have numer format, but was "
			    + this.rootTaskId);
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
	objectFactory.setUsingMObjectsAllLevels("childs", this.isMObject);
	objectFactory.addIgnoredAllLevel("parent");
	final int level = this.childsLevel.intValue();
	if (level < 0) {
	    objectFactory.addIgnoredAllLevel("childs");
	} else if (level > 0) {
		objectFactory.addIgnoredBeforeLevel("childs", -1);
	    objectFactory.addIgnoredStartingLevel("childs", level+1);
	}
	return (WSOTask) objectFactory.newWSObject(taskId);
    }
}