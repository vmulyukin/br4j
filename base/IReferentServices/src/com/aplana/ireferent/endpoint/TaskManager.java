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
package com.aplana.ireferent.endpoint;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.aplana.ireferent.types.WSOCollection;
import com.aplana.ireferent.types.WSOContext;
import com.aplana.ireferent.types.WSOMTask;
import com.aplana.ireferent.types.WSOTask;
import com.aplana.ireferent.types.WSOWrapper;

@WebService(name = "WSBR_TaskManager", targetNamespace = "urn:DefaultNamespace")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface TaskManager {
    @WebMethod(operationName = "GETTASK")
    @WebResult(name = "WSO_TASK", partName = "WSO_TASK")
    WSOTask getTask(
	    @WebParam(name = "ID", partName = "ID")
	    String id,
	    @WebParam(name = "INCLUDEATTACHMENTS", partName = "INCLUDEATTACHMENTS")
	    boolean includeAttachments,
	    @WebParam(name = "CONTEXT", partName = "CONTEXT")
	    WSOContext context);

    @WebMethod(operationName = "GETRESOLUTIONS")
    @WebResult(name = "WSO_WRAPPER", partName = "WSO_WRAPPER")
    WSOWrapper getResolutions(
	    @WebParam(name = "DOCID", partName = "DOCID")
	    String docId,
	    @WebParam(name = "ISMOBJECT", partName = "ISMOBJECT")
	    boolean isMObject,
	    @WebParam(name = "ISTREE", partName = "ISTREE")
	    boolean isTree,
	    @WebParam(name = "INCLUDEATTACHMENTS", partName = "INCLUDEATTACHMENTS")
	    boolean includeAttachments,
	    @WebParam(name = "CLIENTIDSDOCS", partName = "CLIENTIDSDOCS")
    	WSOCollection clientIdsDocs, 
	    @WebParam(name = "CONTEXT", partName = "CONTEXT")
	    WSOContext context);
    
    @WebMethod(operationName = "GETRESOLUTIONSWITHREPORTS")
    @WebResult(name = "WSO_WRAPPER", partName = "WSO_WRAPPER")
    WSOWrapper getResolutionsWithReports(
	    @WebParam(name = "DOCID", partName = "DOCID")
	    String docId,
	    @WebParam(name = "ISMOBJECT", partName = "ISMOBJECT")
	    boolean isMObject,
	    @WebParam(name = "ISTREE", partName = "ISTREE")
	    boolean isTree,
	    @WebParam(name = "INCLUDEATTACHMENTS", partName = "INCLUDEATTACHMENTS")
	    boolean includeAttachments,
	    @WebParam(name = "CLIENTIDSDOCS", partName = "CLIENTIDSDOCS")
    	WSOCollection clientIdsDocs, 
	    @WebParam(name = "CONTEXT", partName = "CONTEXT")
	    WSOContext context);

    @WebMethod(operationName = "GETSTRUCTURE")
    @WebResult(name = "WSO_TASK", partName = "WSO_TASK")
    WSOTask getStructure(
	    @WebParam(name = "ROOTTASKID", partName = "ROOTTASKID")
	    String rootTaskId,
	    @WebParam(name = "CHILDSLEVEL", partName = "CHILDSLEVEL")
	    Integer childsLevel,
	    @WebParam(name = "ISMOBJECT", partName = "ISMOBJECT")
	    boolean isMObject,
	    @WebParam(name = "INCLUDEATTACHMENTS", partName = "INCLUDEATTACHMENTS")
	    boolean includeAttachments,
	    @WebParam(name = "CONTEXT", partName = "CONTEXT")
	    WSOContext context);

    @WebMethod(operationName = "GETREPORTS")
    @WebResult(name = "WSO_WRAPPER", partName = "WSO_WRAPPER")
    WSOWrapper getReports(
	    @WebParam(name = "ID", partName = "ID")
	    String id,
	    @WebParam(name = "ISMOBJECT", partName = "ISMOBJECT")
	    boolean isMObject,
	    @WebParam(name = "INCLUDEATTACHMENTS", partName = "INCLUDEATTACHMENTS")
	    boolean includeAttachments,
	    @WebParam(name = "CONTEXT", partName = "CONTEXT")
	    WSOContext context);

    @WebMethod(operationName = "SETTASK")
    @WebResult(name = "WSO_MTASK", partName = "WSO_MTASK")
    WSOMTask setTask(
	    @WebParam(name = "TASK", partName = "TASK")
	    WSOTask task,
	    @WebParam(name = "CONTEXT", partName = "CONTEXT")
	    WSOContext context);

    @WebMethod(operationName = "GETTYPERESOLUTIONS")
    @WebResult(name = "WSO_WRAPPER", partName = "WSO_WRAPPER")
    WSOWrapper getTypeResolutions(
	    @WebParam(name = "CONTEXT", partName = "CONTEXT")
	    WSOContext context);
}
