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

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.WebServiceContext;

import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.WSObjectFactory;
import com.aplana.ireferent.types.WSOCollection;
import com.aplana.ireferent.types.WSOContext;
import com.aplana.ireferent.types.WSOWrapper;
import com.aplana.ireferent.util.LocalCache;
import com.aplana.ireferent.util.ServiceUtils;
import com.aplana.ireferent.util.XmlUtils;

public class GetDocumentsOperation implements ServiceOperation<WSOWrapper> {

    private final static String CONFIG_FILE_NAME_FORMAT = "getDocuments%s.xml";
    private final static String[] SET_IDS = { "Exam", "Inform", "Sign", "Visa",
    	"Execution", "Control", "Approval", "Selfexecution" };

    private final boolean includeAttachments;
    private final WSOContext context;
    private final boolean isMObject;
    private final WSOCollection clientIdsDocs;
    private final WebServiceContext contextEndpoint;

    public GetDocumentsOperation(boolean isMObject, boolean includeAttachments,
    		WSOCollection clientIdsDocs, WSOContext context, WebServiceContext contextEndpoint) {
	this.includeAttachments = includeAttachments;
	this.context = context;
	this.isMObject = isMObject;
	this.clientIdsDocs = clientIdsDocs;
	this.contextEndpoint = contextEndpoint;
    }

    public String getName() {
	return "getDocuments";
    }

    public Object[] getParameters() {
	return new Object[] { this.isMObject, this.includeAttachments,
		this.context };
    }

    public void processInputData() {
    }

    public WSOWrapper execute() throws Exception {
    	XMLGregorianCalendar dateSyncOut = XmlUtils.getCurrentTimeZone0();
		DataServiceBean serviceBean = ServiceUtils
			.authenticateUser(this.context, this.contextEndpoint);
		WSObjectFactory objectFactory = WSObjectFactory.newInstance(
			serviceBean, "Document");
		objectFactory.setMObject(this.isMObject);
		objectFactory.setUsingMObjectsAllLevels("attachments",
			!this.includeAttachments);
		HashMap<WSObjectFactory, String[]> setObjectFactory = new HashMap<WSObjectFactory, String[]>();
		setObjectFactory.put(objectFactory, SET_IDS);
		ObjectsGetterByConfig objectsCreator = new ObjectsGetterByConfig(
			CONFIG_FILE_NAME_FORMAT, setObjectFactory);
		objectsCreator.setParamsAliases(XmlUtils.getNullDateSyncIn());
		objectsCreator.setAddExtension();
		WSOCollection docs = objectsCreator.createObjects();
		LocalCache.add(clientIdsDocs, docs);
		LocalCache.calculateDelta(context);
		WSOWrapper wrapper = new WSOWrapper();
		wrapper.setCollection(docs);
		wrapper.setDateSyncOut(dateSyncOut);
		return wrapper;
    }
}