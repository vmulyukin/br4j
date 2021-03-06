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
import com.aplana.ireferent.types.WSOWrapper;
import com.aplana.ireferent.types.WSOFile;
import com.aplana.ireferent.types.WSOMDocument;

@WebService(name = "WS_DocumentManager", targetNamespace = "urn:DefaultNamespace")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface DocumentManager {

    @WebMethod(operationName = "GETDOCUMENT")
    @WebResult(name = "WSO_MDOCUMENT", partName = "WSO_MDOCUMENT")
    WSOMDocument getDocument(
	    @WebParam(name = "ID", partName = "ID")
	    String id,
	    @WebParam(name = "IN_CONTEXT", partName = "IN_CONTEXT")
	    WSOContext context);

    @WebMethod(operationName = "GETDOCUMENTS")
    @WebResult(name = "WSO_WRAPPER", partName = "WSO_WRAPPER")
    WSOWrapper getDocuments(
	    @WebParam(name = "ISMOBJECT", partName = "ISMOBJECT")
	    boolean isMObject,
	    @WebParam(name = "INCLUDEATTACHMENTS", partName = "INCLUDEATTACHMENTS")
	    boolean includeAttachments,
	    @WebParam(name = "CLIENTIDSDOCS", partName = "CLIENTIDSDOCS")
    	WSOCollection clientIdsDocs, 
	    @WebParam(name = "IN_CONTEXT", partName = "IN_CONTEXT")
	    WSOContext context);
    
    @WebMethod(operationName = "GETFILE")
    @WebResult(name = "WSO_FILE", partName = "WSO_FILE")
    WSOFile getFile(
    	@WebParam(name = "ID", partName = "ID")
    	String id,
    	@WebParam(name = "IN_CONTEXT", partName = "IN_CONTEXT")
    	WSOContext context);
}
