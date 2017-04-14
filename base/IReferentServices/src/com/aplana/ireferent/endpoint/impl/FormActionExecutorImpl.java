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

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import com.aplana.ireferent.endpoint.FormActionExecutor;
import com.aplana.ireferent.types.WSOContext;
import com.aplana.ireferent.types.WSOFormAction;
import com.aplana.ireferent.types.WSObject;

@WebService(endpointInterface = "com.aplana.ireferent.endpoint.FormActionExecutor", targetNamespace = "urn:DefaultNamespace")
public class FormActionExecutorImpl implements FormActionExecutor {

    @Resource
    WebServiceContext contextEndpoint;
    
    public Boolean execute(final WSObject object, final WSOFormAction action,
	    final WSOContext context) {
	return ServiceOperationExecutor.execute(new ExecuteActionOperation(
		object, action, context, contextEndpoint));
    }
}
