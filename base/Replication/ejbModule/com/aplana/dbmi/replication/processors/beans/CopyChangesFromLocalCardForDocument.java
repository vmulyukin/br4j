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
package com.aplana.dbmi.replication.processors.beans;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.replication.action.LinkResolver;
import com.aplana.dbmi.replication.tool.CardRelationUtils;
import com.aplana.dbmi.service.DataException;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CopyChangesFromLocalCardForDocument extends CopyChangesFromLocalCard {

	@Override
	protected void specificCopy() throws DataException {
		super.specificCopy();
		createLastRegistrations();
	}

	protected void createLastRegistrations() throws DataException {
		StringAttribute regNumberAttribute = getChangedCard().getAttributeById(StringAttribute.class, "regnumber");
		DateAttribute regDateAttribute = getChangedCard().getAttributeById(DateAttribute.class, "regdate");
		PersonAttribute registrarAttribute = getChangedCard().getAttributeById(PersonAttribute.class, "jbr.incoming.registrar");

		if (isNeedCreateLastRegistration(regNumberAttribute, regDateAttribute, registrarAttribute)) {
			ObjectId regInfoCardId = createLastRegistrationsCard(regNumberAttribute, regDateAttribute,
					registrarAttribute);
			CardLinkAttribute lastRegistrationsAttribute = getDestinationCard().getAttributeById(
					CardLinkAttribute.class, "jbr.lastRegistrations");
			lastRegistrationsAttribute.addLinkedId(regInfoCardId);
		}
	}

	protected boolean isNeedCreateLastRegistration(StringAttribute regNumberAttribute, DateAttribute regDateAttribute,
			PersonAttribute registrarAttribute) throws DataException {

		if (regNumberAttribute == null || regDateAttribute == null || registrarAttribute == null) {
			return false;
		}

		String regNumber = regNumberAttribute.getValue();
		Date regDate = regDateAttribute.getValue();

		if (regNumber == null || regNumber.isEmpty() || regDate == null) {
			return false;
		}

		Search search = new Search();
		search.setByAttributes(true);
		search.setTemplates(Collections.singleton(DataObject.createFromId(ObjectId.predefined(Template.class, "jbr.regInfo"))));
		search.addStringAttribute(ObjectId.predefined(StringAttribute.class, "regnumber"), regNumber);
		search.addDateAttribute(ObjectId.predefined(DateAttribute.class, "regdate"), regDate, regDate);
		SearchResult searchResult = getDataService().doAction(search);
		CardLinkAttribute lastRegistrationsAttribute = getDestinationCard().getAttributeById(
				CardLinkAttribute.class, "jbr.lastRegistrations");
		return !ObjectIdUtils.isIntersectionDataObjects(searchResult.getCards(),
				lastRegistrationsAttribute.getIdsLinked());
	}

	protected ObjectId createLastRegistrationsCard(StringAttribute regNumberAttribute, DateAttribute regDateAttribute,
			PersonAttribute registrarAttribute) throws DataException {
		CreateCard createCard = new CreateCard(ObjectId.predefined(Template.class, "jbr.regInfo"));
		Card regInfoCard = getDataService().doAction(createCard);

		StringAttribute regInfoRegNumberAttribute = regInfoCard.getAttributeById(StringAttribute.class, "regnumber");
		regInfoRegNumberAttribute.setValueFromAttribute(regNumberAttribute);

		DateAttribute regInfoRegDateAttribute = regInfoCard.getAttributeById(DateAttribute.class, "regdate");
		regInfoRegDateAttribute.setValueFromAttribute(regDateAttribute);

		PersonAttribute regInfoRegistrarAttribute = regInfoCard.getAttributeById(PersonAttribute.class, "jbr.incoming.registrar");
		regInfoRegistrarAttribute.setValueFromAttribute(registrarAttribute);
		
		StringAttribute owner = regInfoCard.getAttributeById(CardRelationUtils.REPLIC_OWNER);
		owner.setValue(getAddresseeGuid());
		
		LinkResolver<ObjectId> resolve = new LinkResolver<ObjectId>();
		resolve.setCardId(regInfoRegistrarAttribute.getPerson().getCardId());
		resolve.setLink("JBR_PERS_ORG");
		List<ObjectId> organizationId = getDataService().doAction(resolve);
		if (organizationId != null) {
			CardLinkAttribute sourceOrgAttribute = regInfoCard.getAttributeById(CardLinkAttribute.class, "jbr.regInfo.sourceOrganization");
			sourceOrgAttribute.addIdsLinked(organizationId);
		}

		ObjectId regInfoCardId = null;
		try {
			regInfoCardId = getDataService().saveObject(regInfoCard);
		} finally {
			if (regInfoCardId != null) {
				UnlockObject unlock = new UnlockObject();
				unlock.setId(regInfoCardId);
				getDataService().doAction(unlock);
			}
		}
		return regInfoCardId;
	}
}
