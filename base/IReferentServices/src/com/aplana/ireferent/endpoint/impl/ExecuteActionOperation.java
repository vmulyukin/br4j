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
package com.aplana.ireferent.endpoint.impl;

import javax.xml.ws.WebServiceContext;

import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.actions.ActionsManager;
import com.aplana.ireferent.types.WSOContext;
import com.aplana.ireferent.types.WSOFormAction;
import com.aplana.ireferent.types.WSObject;
import com.aplana.ireferent.util.ServiceUtils;

public class ExecuteActionOperation implements
        ServiceOperation<Boolean> {

    private final WSObject object;
    private final WSOContext context;
    private final WSOFormAction action;
    private final WebServiceContext contextEndpoint;

    public ExecuteActionOperation(WSObject object, WSOFormAction action,
    	WSOContext context, WebServiceContext contextEndpoint) {
        this.object = object;
        this.context = context;
        this.action = action;
        this.contextEndpoint = contextEndpoint;
    }

    public String getName() {
        return "FormActionExecutor::execute";
    }

    public Object[] getParameters() {
        return new Object[] { this.object.getType(), this.action.getId(),
    	    this.context };
    }

    public void processInputData() {
    }

    public Boolean execute() throws Exception {
        DataServiceBean serviceBean = ServiceUtils
    	    .authenticateUser(this.context, this.contextEndpoint);
        ActionsManager actionsManager = ActionsManager.instance();
        actionsManager.doAction(serviceBean, this.object, this.action);
        return true;
    }
}