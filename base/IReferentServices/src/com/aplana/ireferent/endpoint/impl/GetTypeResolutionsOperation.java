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

import java.util.HashMap;

import javax.xml.ws.WebServiceContext;

import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.WSObjectFactory;
import com.aplana.ireferent.types.WSOContext;
import com.aplana.ireferent.types.WSOWrapper;
import com.aplana.ireferent.util.ServiceUtils;

public class GetTypeResolutionsOperation implements
	ServiceOperation<WSOWrapper> {
    private final WSOContext context;
    private static final String CONFIG_FILE_FORMAT = "getTypeResolutions%s.xml";
    private static final String[] SET_IDS = { "" };
    private final WebServiceContext contextEndpoint;

    public GetTypeResolutionsOperation(WSOContext context, WebServiceContext contextEndpoint) {
	this.context = context;
	this.contextEndpoint = contextEndpoint;
    }

    public String getName() {
	return "getTypeResolutions";
    }

    public Object[] getParameters() {
	return new Object[] { this.context };
    }

    public void processInputData() {
    }

    public WSOWrapper execute() throws Exception {
		DataServiceBean serviceBean = ServiceUtils
			.authenticateUser(this.context, this.contextEndpoint);
		WSObjectFactory objectFactory = WSObjectFactory.newInstance(
			serviceBean, "TypeResolution");
		HashMap<WSObjectFactory, String[]> setObjectFactory = new HashMap<WSObjectFactory, String[]>();
		setObjectFactory.put(objectFactory, SET_IDS);
		ObjectsGetterByConfig objectsCreator = new ObjectsGetterByConfig(
			CONFIG_FILE_FORMAT, setObjectFactory);
		WSOWrapper wrapper = new WSOWrapper();
		wrapper.setCollection(objectsCreator.createObjects());
		return wrapper;
    }
}