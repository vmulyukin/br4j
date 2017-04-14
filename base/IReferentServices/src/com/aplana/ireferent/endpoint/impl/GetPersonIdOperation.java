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

import java.security.Principal;

import javax.xml.ws.WebServiceContext;

import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.types.WSObject;
import com.aplana.ireferent.util.IReferentUser;
import com.aplana.ireferent.util.ServiceUtils;

public class GetPersonIdOperation implements
        ServiceOperation<WSObject> {

    private final String person_login;
    private final WebServiceContext contextEndpoint;

    public GetPersonIdOperation(String person_login, WebServiceContext contextEndpoint) {
        this.person_login = person_login;
        this.contextEndpoint = contextEndpoint;
    }

    public String getName() {
        return "GetPersonIdOperation::execute";
    }

    public Object[] getParameters() {
        return new Object[] { this.person_login };
    }

    public void processInputData() {
    }

    public WSObject execute() throws Exception {
	Principal user = new IReferentUser(this.person_login);
	DataServiceBean serviceBean_login = ServiceUtils.getServiceBean(user, this.contextEndpoint);
	Person person = serviceBean_login.getPerson();
	String card_id = person.getCardId().getId().toString();
	WSObject wsobject = new WSObject();
	wsobject.setId(card_id);
	wsobject.setTitle(person.getFullName());
        return wsobject;
    }
}