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
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.aplana.ireferent.types.WSOApproval;
import com.aplana.ireferent.types.WSOApprovalReview;
import com.aplana.ireferent.types.WSOCollection;
import com.aplana.ireferent.types.WSOContext;

@WebService(name = "WS_ApprovalManager", targetNamespace = "urn:DefaultNamespace")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface ApprovalManager {

    @WebMethod(operationName = "GETAPPROVAL")
    WSOApproval getApproval(
	    @WebParam(name = "ID", partName = "ID")
	    String id,
	    @WebParam(name = "INCLUDEATTACHMENTS", partName = "INCLUDEATTACHMENTS")
	    Boolean includeAttachments,
	    @WebParam(name = "CONTEXT", partName = "CONTEXT")
	    WSOContext context);

    @WebMethod(operationName = "SETAPPROVAL")
    String setApproval(
	    @WebParam(name = "APPROVAL", partName = "APPROVAL")
	    WSOApproval approval,
	    @WebParam(name = "CONTEXT", partName = "CONTEXT")
	    WSOContext context);

    @WebMethod(operationName = "GETREVIEWS")
    WSOCollection getReviews(
	    @WebParam(name = "ID", partName = "ID")
	    String id,
	    @WebParam(name = "ISMOBJECT", partName = "ISMOBJECT")
	    Boolean isMObject,
	    @WebParam(name = "INCLUDEATTACHMENTS", partName = "INCLUDEATTACHMENTS")
	    Boolean includeAttachments,
	    @WebParam(name = "CONTEXT", partName = "CONTEXT")
	    WSOContext context);

    @WebMethod(operationName = "GETREVIEW")
    WSOApprovalReview getReview(
	    @WebParam(name = "ID", partName = "ID")
	    String id,
	    @WebParam(name = "INCLUDEATTACHMENTS", partName = "INCLUDEATTACHMENTS")
	    Boolean includeAttachments,
	    @WebParam(name = "CONTEXT", partName = "CONTEXT")
	    WSOContext context);

    @WebMethod(operationName = "SETREVIEW")
    String setReview(
	    @WebParam(name = "REVIEW", partName = "REVIEW")
	    WSOApprovalReview review,
	    @WebParam(name = "CONTEXT", partName = "CONTEXT")
	    WSOContext context);

}
