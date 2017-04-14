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
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.replication.query.DoGetPerson.PersonWrapper;
import com.aplana.dbmi.replication.action.GetPerson;
import com.aplana.dbmi.replication.action.LinkResolver;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage.Card.Attribute;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage.Card.Attribute.PersonValue;
import com.aplana.dbmi.replication.tool.CardRelationUtils;
import com.aplana.dbmi.service.DataException;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.List;

public class CopyChangesToLocalCardForDocument extends CopyChangesToLocalCard {
	private String regNumber;
	private Date regDate;
	private Person registrar;

	@Override
	protected void preProcessAttr(Attribute pckgAttr) throws DataException {
		if ("JBR_REGD_REGNUM".equals(pckgAttr.getCode())) {
			List<String> values = pckgAttr.getStringValue();
			if (values.size() > 0) {
				regNumber = values.get(0);
			}
		} else if ("JBR_REGD_DATEREG".equals(pckgAttr.getCode())) {
			List<XMLGregorianCalendar> values = pckgAttr.getDateValue();
			if (values.size() > 0) {
				regDate = values.get(0).toGregorianCalendar().getTime();
			}
		} else if ("JBR_REGD_REGISTRAR".equals(pckgAttr.getCode())) {
			List<PersonValue> pckgPersons = pckgAttr.getPersonValue();
			if (pckgPersons.size() > 0) {
				if (pckgPersons.get(0).getLogin() != null) {
					String login = pckgPersons.get(0).getLogin();
					String email = pckgPersons.get(0).getEmail();
					String fname = pckgPersons.get(0).getFullName();
					String uuid  = pckgPersons.get(0).getUuid();
					GetPerson getPerson = new GetPerson(login, email, fname, uuid);
					PersonWrapper wrap = getDataService().doAction(getPerson);
					registrar = wrap != null ? wrap.getPerson() : null;
				}
			}
		}
	}

	@Override
	public void postProcessPackage(Card card) throws DataException {
		super.postProcessPackage(card);
	}

	@Override
	public void postProcessPackageForNewCard(Card card) throws DataException {
		super.postProcessPackageForNewCard(card);
		CardRelationUtils.setReplOrganizations(card, (Card)dataService.getById(replicationCardId), dataService);
		setLinkToReplicatedDocument(card);
		setReplicationTypes(card);
		createRegInfo(card);
	}

	private void setLinkToReplicatedDocument(Card card) {
		CardLinkAttribute link = card.getAttributeById(CardRelationUtils.REPLIC_REPLICATE_OF);
		link.addLinkedId(getSourceCardId());
	}

	private void setReplicationTypes(Card card) {
		ListAttribute replicatingDocTypeAttribute = card.getAttributeById(CardRelationUtils.REPLIC_DOC_TYPE);
		final ObjectId replicatingDocTypeLocalDocValueId = ObjectId.predefined(ReferenceValue.class, "jbr.replication.localDocument");
		replicatingDocTypeAttribute.setValue(ReferenceValue.<ReferenceValue>createFromId(replicatingDocTypeLocalDocValueId));
	}

	private void createRegInfo(Card card) throws DataException {
		CreateCard createCard = new CreateCard(ObjectId.predefined(Template.class, "jbr.regInfo"));
		Card regInfoCard = getDataService().doAction(createCard);
		StringAttribute regNumberAttribute = regInfoCard.getAttributeById(StringAttribute.class, "regnumber");
		regNumberAttribute.setValue(regNumber);
		DateAttribute regDateAttribute = regInfoCard.getAttributeById(DateAttribute.class, "regdate");
		regDateAttribute.setValue(regDate);
		PersonAttribute registrarAttribute = regInfoCard.getAttributeById(PersonAttribute.class, "jbr.incoming.registrar");
		registrarAttribute.setPerson(registrar);
		LinkResolver<ObjectId> resolve = new LinkResolver<ObjectId>();
		resolve.setCardId(registrar.getCardId());
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
		if (regInfoCardId != null) {
			CardLinkAttribute lastRegistrationsAttribute = card.getAttributeById(CardLinkAttribute.class, "jbr.lastRegistrations");
			lastRegistrationsAttribute.addLinkedId(regInfoCardId);
		}
	}
}