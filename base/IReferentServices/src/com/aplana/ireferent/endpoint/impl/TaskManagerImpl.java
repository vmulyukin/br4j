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

import com.aplana.ireferent.endpoint.TaskManager;
import com.aplana.ireferent.types.WSOCollection;
import com.aplana.ireferent.types.WSOContext;
import com.aplana.ireferent.types.WSOMTask;
import com.aplana.ireferent.types.WSOTask;
import com.aplana.ireferent.types.WSOWrapper;


@WebService(endpointInterface = "com.aplana.ireferent.endpoint.TaskManager", targetNamespace = "urn:DefaultNamespace")
public class TaskManagerImpl implements TaskManager {
    
    @Resource
    WebServiceContext contextEndpoint;

    public WSOTask getTask(final String id, final boolean includeAttachments,
	    final WSOContext context) {
	return ServiceOperationExecutor.execute(new GetTaskOperation(id,
		includeAttachments, context, contextEndpoint));
    }

    public WSOWrapper getReports(final String id, final boolean isMObject,
	    final boolean includeAttachments, final WSOContext context) {
	return ServiceOperationExecutor.execute(new GetReportsOperation(id,
		isMObject, includeAttachments, context, contextEndpoint));
    }

    public WSOWrapper getResolutions(final String docId,
	    final boolean isMObject, final boolean isTree, final boolean includeAttachments,
	    final WSOCollection clientIdsDocs, final WSOContext context) {
	return ServiceOperationExecutor.execute(new GetResolutionsOperation(
		docId, isMObject, isTree, includeAttachments, clientIdsDocs, context, contextEndpoint));
    }
    
    public WSOWrapper getResolutionsWithReports(final String docId,
    	    final boolean isMObject, final boolean isTree, final boolean includeAttachments,
    	    final WSOCollection clientIdsDocs, final WSOContext context) {
    	return ServiceOperationExecutor.execute(new GetResolutionsWithReportsOperation(
    		docId, isMObject, isTree, includeAttachments, clientIdsDocs, context, contextEndpoint));
        }

    public WSOTask getStructure(final String rootTaskId,
	    final Integer childsLevel, final boolean isMObject,
	    final boolean includeAttachments, final WSOContext context) {
	return ServiceOperationExecutor
		.execute(new GetTaskStructureOperation(rootTaskId, childsLevel,
			isMObject, includeAttachments, context, contextEndpoint));
    }

    public WSOMTask setTask(final WSOTask task, final WSOContext context) {	
	return ServiceOperationExecutor.execute(new SetTaskOperation(task,
		context, contextEndpoint));
    }

    public WSOWrapper getTypeResolutions(final WSOContext context) {
	return ServiceOperationExecutor
		.execute(new GetTypeResolutionsOperation(context, contextEndpoint));
    }
}
