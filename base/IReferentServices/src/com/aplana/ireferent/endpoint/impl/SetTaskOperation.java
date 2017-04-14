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

import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.CardHandler;
import com.aplana.ireferent.types.WSOContext;
import com.aplana.ireferent.types.WSOMTask;
import com.aplana.ireferent.types.WSOTask;
import com.aplana.ireferent.util.ServiceUtils;

public class SetTaskOperation implements ServiceOperation<WSOMTask> {
    private final WSOTask task;
    private final WSOContext context;
    private final WebServiceContext contextEndpoint;

    public SetTaskOperation(WSOTask task, WSOContext context, WebServiceContext contextEndpoint) {
	this.task = task;
	this.context = context;
	this.contextEndpoint = contextEndpoint;
    }

    public String getName() {
	return "setTask";
    }

    public Object[] getParameters() {
	return new Object[] { this.task, this.context };
    }

    public void processInputData() {
    }

    public WSOMTask execute() throws Exception {
	DataServiceBean serviceBean = ServiceUtils
		.authenticateUser(this.context, this.contextEndpoint);
	CardHandler cardCreator = new CardHandler(serviceBean);
	String cardId = String.valueOf(cardCreator.createCard(this.task)
		.getId());
	WSOMTask mTask = new WSOMTask();
	mTask.setId(cardId);
	return mTask;
    }
}