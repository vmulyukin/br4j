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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.WebServiceContext;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.IReferentException;
import com.aplana.ireferent.WSObjectFactory;
import com.aplana.ireferent.types.WSOCollection;
import com.aplana.ireferent.types.WSOContext;
import com.aplana.ireferent.types.WSOWrapper;
import com.aplana.ireferent.util.LocalCache;
import com.aplana.ireferent.util.ServiceUtils;
import com.aplana.ireferent.util.XmlUtils;

public class GetResolutionsWithReportsOperation implements ServiceOperation<WSOWrapper> {

    private static final String CONFIG_FILE_FORMAT = "getResolutions%s.xml";
    private static final String[] SET_IDS = { "Execution", "Control", "Project", "Approval", "Selfexecution" };

    private final WSOContext context;
    private final boolean includeAttachments;
    private final boolean isMObject;
    private final boolean isTree;
    private final String docId;
    private final WSOCollection clientIdsDocs;
    private final ObjectId RESOLUTIONS_ID = ObjectId.predefined(
	    BackLinkAttribute.class, "jbr.resolutions");
    private ObjectId docCardId;
    private DataServiceBean serviceBean;
    private final WebServiceContext contextEndpoint;

    public GetResolutionsWithReportsOperation(String docId, boolean isMObject, boolean isTree,
	    boolean includeAttachments, WSOCollection clientIdsDocs, WSOContext context, WebServiceContext contextEndpoint) {
	this.context = context;
	this.includeAttachments = includeAttachments;
	this.isMObject = isMObject;
	this.docId = docId;
	this.clientIdsDocs = clientIdsDocs;
	this.contextEndpoint = contextEndpoint;
	this.isTree = isTree;
    }

    public String getName() {
	return "getResolutionsWithReports";
    }

    public Object[] getParameters() {
	return new Object[] { this.docId, this.isMObject,
		this.includeAttachments, this.context };
    }

    public void processInputData() {
	if (this.docId != null && !"".equals(this.docId)) {
	    try {
		docCardId = new ObjectId(Card.class, Long.parseLong(this.docId));
	    } catch (NumberFormatException ex) {
		throw new IllegalArgumentException(
			"Card id should have numer format, but was "
				+ this.docId);
	    }
	}
    }

    public WSOWrapper execute() throws Exception {
	    XMLGregorianCalendar dateSyncOut = XmlUtils.getCurrentTimeZone0();
		serviceBean = ServiceUtils.authenticateUser(this.context, this.contextEndpoint);
		WSObjectFactory objectFactory = WSObjectFactory.newInstance(
			serviceBean, "TaskWithReports");
		objectFactory.setMObject(this.isMObject);
		objectFactory.setUsingMObjectsAllLevels("reports", this.isMObject); // INFINITY_LEVEL
		objectFactory.setUsingMObjectsAllLevels("attachments",
			!this.includeAttachments);
		if (isTree) {
		objectFactory.addIgnoredBeforeLevel("childs", -1);
		objectFactory.addIgnoredStartingLevel("parent", 1);
		} else {
			objectFactory.addIgnoredAllLevel("childs");
			objectFactory.addIgnoredAllExceptCurrentLevel("parent");
		}
		WSOWrapper wrapper = new WSOWrapper();
		if (docCardId == null) {
			WSOCollection userResolutions = getUserResolutions(objectFactory);
			LocalCache.add(clientIdsDocs, userResolutions);
			LocalCache.calculateDelta(context);
			wrapper.setDateSyncOut(dateSyncOut);
			wrapper.setCollection(userResolutions);
		    return wrapper;
		}
		WSOCollection docResolutions = getDocumentResolutions(objectFactory);
		LocalCache.add(clientIdsDocs, docResolutions);
		LocalCache.calculateDelta(context);
		wrapper.setCollection(docResolutions);
		return wrapper;
    }

    private WSOCollection getDocumentResolutions(WSObjectFactory objectFactory)
	    throws IReferentException {
	Card documentCard = findDocument();
	ObjectId[] resolutionIds = getResolutionIds(documentCard);
	return objectFactory.newWSOCollection(resolutionIds);
    }

    private Card findDocument() {
	Collection<ObjectId> requiredAttributes = new ArrayList<ObjectId>();
	requiredAttributes.add(RESOLUTIONS_ID);
	Collection<Card> documentCards = ServiceUtils.fetchCards(serviceBean,
		new ObjectId[] { docCardId }, requiredAttributes);
	if (documentCards.isEmpty()) {
	    throw new IllegalArgumentException(String.format(
		    "Card with id=%s was not found", this.docId));
	}
	return documentCards.iterator().next();
    }

    private ObjectId[] getResolutionIds(Card taskCard) {
    BackLinkAttribute resolutionsAttribute = (BackLinkAttribute) taskCard
		.getAttributeById(RESOLUTIONS_ID);
	if (resolutionsAttribute == null)
	    return new ObjectId[] {};
	return resolutionsAttribute.getIdsArray();
    }

    private WSOCollection getUserResolutions(WSObjectFactory objectFactory)
	    throws Exception {
	    HashMap<WSObjectFactory, String[]> setObjectFactory = new HashMap<WSObjectFactory, String[]>();
	    setObjectFactory.put(objectFactory, SET_IDS);
	ObjectsGetterByConfig objectsCreator = new ObjectsGetterByConfig(
			CONFIG_FILE_FORMAT, setObjectFactory);
	objectsCreator.setParamsAliases(XmlUtils.getParamDateSyncIn(context));
	objectsCreator.setAddExtension();
	return objectsCreator.createObjects();
    }
}