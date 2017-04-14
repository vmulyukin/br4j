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

import com.aplana.ireferent.endpoint.ApprovalManager;
import com.aplana.ireferent.types.WSOApproval;
import com.aplana.ireferent.types.WSOApprovalReview;
import com.aplana.ireferent.types.WSOCollection;
import com.aplana.ireferent.types.WSOContext;

@WebService(endpointInterface = "com.aplana.ireferent.endpoint.ApprovalManager", targetNamespace = "urn:DefaultNamespace")
public class ApprovalManagerImpl implements ApprovalManager {
    
    @Resource
    WebServiceContext contextEndpoint;
    
    public WSOApproval getApproval(String id, Boolean includeAttachments,
	    WSOContext context) {
	throw new UnsupportedOperationException(
		"This operation is not supported");
    }

    public String setApproval(WSOApproval approval, WSOContext context) {
	throw new UnsupportedOperationException(
		"This operation is not supported");
    }

    public WSOCollection getReviews(String id, Boolean isMObject,
	    Boolean includeAttachments, WSOContext context) {
	throw new UnsupportedOperationException(
		"This operation is not supported");
    }

    public WSOApprovalReview getReview(String id, Boolean includeAttachments,
	    WSOContext context) {
	throw new UnsupportedOperationException(
		"This operation is not supported");
    }

    public String setReview(WSOApprovalReview review, WSOContext context) {
	throw new UnsupportedOperationException(
		"This operation is not supported");
    }

}
