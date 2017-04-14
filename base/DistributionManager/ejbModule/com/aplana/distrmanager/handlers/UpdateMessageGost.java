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

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.distrmanager.cards.ElementListMailing;
import com.aplana.distrmanager.cards.MessageGOST;
import com.aplana.distrmanager.cards.Organization;
import com.aplana.dmsi.Configuration;
import com.aplana.dmsi.DMSIException;
import com.aplana.dmsi.card.handling.CardFacade;
import com.aplana.dmsi.object.DMSIObjectFactory;

public class UpdateMessageGost {

	protected final Log logger = LogFactory.getLog(getClass());

	private static final String MESSAGE_GOST_UPDATE_ATTR_ERROR = "jbr.DistributionManager.UpdateMessageGost.errorUpdateAttr";

	protected DataServiceFacade serviceBean = null;
	protected Configuration config = Configuration.instance();

	private List<ObjectId> senderPath = Collections.emptyList();
	private MessageGOST messageGOST;
	private ElementListMailing elementListMailing;

	public UpdateMessageGost(DataServiceFacade serviceBean) {
		this.serviceBean = serviceBean;
	}

	public void handle(MessageGOST msgGOSTWrap, ElementListMailing elmCardWrap) throws Exception {
		this.messageGOST = msgGOSTWrap;
		this.elementListMailing = elmCardWrap;
		try {
			fillVersion();
			fillStandard();
			fillSender();
			fillRecipient();
			msgGOSTWrap.saveCard();
		} catch(Exception umge) {
			Card card = null;
			if (null != elmCardWrap)
				card = elmCardWrap.getCard();
			ObjectId msgGostId = null;
			if (null != msgGOSTWrap && null != msgGOSTWrap.getCard())
				msgGostId = msgGOSTWrap.getCard().getId();
			logError(card, elmCardWrap, msgGostId, MESSAGE_GOST_UPDATE_ATTR_ERROR, umge);
		    throw umge; // ��� ������ ������� ������������ ��������� ��������� ����, �������� ������� ��������� "�������� ������ ��������"
		}
	}

	private void fillVersion() {
		messageGOST.setVersion(config.getVersion());
	}

	private void fillStandard() {
		messageGOST.setStandart(config.getStandart());
	}

	protected void fillSender() throws Exception {
		com.aplana.dmsi.types.common.Organization org = getSenderOrganization();
		messageGOST.setUidSender(org.getUuid());
		messageGOST.setFullNameSender(org.getFullName());
		messageGOST.setUidSystemSender(config.getSysId());
		messageGOST.setNameSystemSender(config.getSystem());
		messageGOST.setSysSenderDetails(config.getSystemDetails());
	}

	protected void fillRecipient() throws Exception, DataException {
		Organization recipient = new Organization(serviceBean);
		recipient.init(elementListMailing.getRecipientId());
		messageGOST.setRecipient(recipient.getFullName());
		messageGOST.setRecipientUid(recipient.getUUID());
	}

	protected com.aplana.dmsi.types.common.Organization getSenderOrganization() throws DMSIException, DataException {
		ObjectId elmCardId = elementListMailing.getCard().getId();
		ObjectId currCardId = elmCardId;
		if (logger.isDebugEnabled()) {
			logger.debug("Path contains " + this.senderPath.size() + " elements. Begin sender calculating");
		}
		currCardId = goThroughPath(currCardId, senderPath);
		if (currCardId == null || currCardId.equals(elmCardId)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Card id for sender was not get by senderPath. Using default organization");
			}
			currCardId = Configuration.instance().getDefaultOrganizationId();
		}
		DMSIObjectFactory organizationFactory = DMSIObjectFactory.newInstance(serviceBean, "common.Organization");
		return (com.aplana.dmsi.types.common.Organization) organizationFactory.newDMSIObject(currCardId);
	}

	protected ObjectId goThroughPath(ObjectId currCardId, List<ObjectId> path) throws DMSIException {
		if (logger.isDebugEnabled()) {
			logger.debug("Path contains " + this.senderPath.size() + " elements. Begin calculating");
		}
		for (ObjectId attrId : path) {
			if (logger.isDebugEnabled()) {
				logger.debug("Trying to get value of [" + attrId.getId() + "] from card [" + currCardId + "]");
			}
			CardFacade cardFacade = new CardFacade(serviceBean, currCardId);
			Object linkedCardIds = cardFacade.getAttributeValue(attrId);
			if (linkedCardIds == null || ((ObjectId[]) linkedCardIds).length < 1) {
				if (logger.isDebugEnabled()) {
					logger.debug("There are no values of [" + attrId.getId() + "] in card [" + currCardId + "]. Break.");
				}
				currCardId = null;
				break;
			}
			currCardId = ((ObjectId[]) linkedCardIds)[0];
		}
		return currCardId;
	}

	public List<ObjectId> getSenderPath() {
		return Collections.unmodifiableList(this.senderPath);
	}

	public void setSenderPath(List<ObjectId> path) {
		this.senderPath = path;
		if (this.senderPath == null) {
			this.senderPath = Collections.emptyList();
		}
	}

	protected MessageGOST getMessageGOST() {
		return this.messageGOST;
	}

	protected ElementListMailing getElementListMailing() {
		return this.elementListMailing;
	}

	private void logError(Card card, ElementListMailing elmWrap, ObjectId msgGostId, String msgError, Exception e) {
		String error = String.
			format("{%s}; elmId: {%s}; elmUUID: {%s}; msgGostId: {%s};",
					(null == msgError)?"null":msgError,
					(null == card)?"null":card.getId().getId(),
					(null == elmWrap)?"null":elmWrap.getUid(),
					(null == msgGostId)?"null":msgGostId.getId()
			);
			logger.error(error, e);
	}
}
