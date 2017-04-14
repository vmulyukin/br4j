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
package com.aplana.distrmanager.handlers;

import static org.apache.commons.lang.StringUtils.defaultString;

import java.util.Collections;
import java.util.List;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.distrmanager.cards.MessageGOST;
import com.aplana.dmsi.card.handling.CardFacade;

public class UpdateMessageGostForAck extends UpdateMessageGost {

	private List<ObjectId> pathToIncomingMessageGost = Collections.emptyList();
	private ObjectId toOrganizationAttributeId;
	private ObjectId toUidAttributeId;

	public UpdateMessageGostForAck(DataServiceFacade serviceBean) {
		super(serviceBean);
	}

	@Override
	protected void fillSender() throws Exception {
		MessageGOST messageGOST = getMessageGOST();
		ObjectId elmCardId = getElementListMailing().getCard().getId();
		ObjectId incomingMessageGost = goThroughPath(getElementListMailing().getCard().getId(),
				pathToIncomingMessageGost);
		if (incomingMessageGost == null || incomingMessageGost.equals(elmCardId)) {
			throw new DataException();
		}
		CardFacade incomingMessageGostCard = new CardFacade(serviceBean, incomingMessageGost);
		if (toUidAttributeId != null) {
			messageGOST.setUidSender(defaultString(((String) incomingMessageGostCard
					.getAttributeValue(toUidAttributeId))));
		}
		if (toOrganizationAttributeId != null) {
			messageGOST.setFullNameSender(defaultString((String) incomingMessageGostCard
					.getAttributeValue(toOrganizationAttributeId)));
		}
		messageGOST.setUidSystemSender(config.getSysId());
		messageGOST.setNameSystemSender(config.getSystem());
		messageGOST.setSysSenderDetails(config.getSystemDetails());
	}

	public List<ObjectId> getPathToIncomingMessageGost() {
		return Collections.unmodifiableList(this.pathToIncomingMessageGost);
	}

	public void setPathToIncomingMessageGost(List<ObjectId> pathToIncomingMessageGost) {
		this.pathToIncomingMessageGost = pathToIncomingMessageGost;
		if (this.pathToIncomingMessageGost == null) {
			this.pathToIncomingMessageGost = Collections.emptyList();
		}
	}

	public ObjectId getToOrganizationAttributeId() {
		return this.toOrganizationAttributeId;
	}

	public void setToOrganizationAttributeId(ObjectId toOrganizationAttributeId) {
		this.toOrganizationAttributeId = toOrganizationAttributeId;
	}

	public ObjectId getToUidAttributeId() {
		return this.toUidAttributeId;
	}

	public void setToUidAttributeId(ObjectId toUidAttributeId) {
		this.toUidAttributeId = toUidAttributeId;
	}

}
