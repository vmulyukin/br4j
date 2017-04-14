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
package com.aplana.dbmi.replication.tool;

import com.aplana.dbmi.action.*;
import com.aplana.dbmi.action.Search.TextSearchConfigValue;
import com.aplana.dbmi.action.file.ActionPerformer;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.replication.packageconfig.PackageType;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage.Status;
import com.aplana.dbmi.replication.packageconfig.StatusType;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;

public class ReplicationNotificationHandler {
	private Log logger = LogFactory.getLog(getClass());
	private ActionPerformer serviceBean;

	public ReplicationNotificationHandler(ActionPerformer serviceBean){
		this.serviceBean = serviceBean;
	}

	public void sendNotification(ReplicationPackage pack, StatusType statusType, String errorMessage, String errorReason) 
			throws JAXBException, IOException, DataException, ServiceException {
		ReplicationPackage rpg = new ReplicationPackage();
		rpg.setPackageType(PackageType.STATUS);
		rpg.setAddressee(pack.getAddressee());
		rpg.setSender(pack.getSender());
		rpg.setDateSent(ReplicationUtils.newXMLGregorianCalendar());

		Status status = new Status();
		status.setGuid(pack.getCard().getGuid());
		status.setStatusType(statusType);
		status.setValue(errorMessage);
		status.setReason(errorReason);

		rpg.setStatus(status);

		ReplicationUtils.packageToFile(rpg);
		
		if (statusType == StatusType.ERROR) {
			Card card = getReplicationCard(rpg);
			if (card != null) {
				card = serviceBean.getById(card.getId());
				
				HtmlAttribute notif = card.getAttributeById(ObjectId.predefined(HtmlAttribute.class, "jbr.replication.replicXmlNotif"));
				TextAttribute error = card.getAttributeById(ObjectId.predefined(TextAttribute.class, "jbr.replication.error.message"));
				IntegerAttribute retries = card.getAttributeById(ObjectId.predefined(IntegerAttribute.class, "jbr.replication.retries"));
				notif.setValue(ReplicationUtils.packageToString(rpg));
				error.setValue(rpg.getStatus().getReason());
				retries.setValue(retries.getValue()+1);
				
				serviceBean.doAction(new LockObject(card.getId()));
				try {
					OverwriteCardAttributes save = new OverwriteCardAttributes();
					save.setCardId(card.getId());
					save.setAttributes(Arrays.asList(notif, error, retries));
					serviceBean.doAction(save);
				} finally {
					serviceBean.doAction(new UnlockObject(card.getId()));
				}
			}
		}
	}
	
	public void sendNotification(ReplicationPackage pack, StatusType statusType) 
			throws JAXBException, IOException, DataException, ServiceException {
		sendNotification(pack, statusType, null, null);
	}

	public void processingNotification(ReplicationPackage rpg) throws DataException, ServiceException, JAXBException {
		Card card = getReplicationCard(rpg);
		if (card != null) {
			card = serviceBean.getById(card.getId());
			Collection<Attribute> attrsToSave = new HashSet<Attribute>();
			DateAttribute receiveDateAttr = card.getAttributeById(CardRelationUtils.REPLIC_DATE_RECEIVE);
			receiveDateAttr.setValue(new Date());
			attrsToSave.add(receiveDateAttr);
			
			if (card.getState().equals(ObjectId.state("sent")) ||
				card.getState().equals(ObjectId.state("jbr.replication.error"))) {

				HtmlAttribute notif = card.getAttributeById(ObjectId.predefined(HtmlAttribute.class, "jbr.replication.replicXmlNotif"));
				TextAttribute error = card.getAttributeById(ObjectId.predefined(TextAttribute.class, "jbr.replication.error.message"));
				notif.setValue(ReplicationUtils.packageToString(rpg));
				error.setValue(rpg.getStatus().getReason());
				attrsToSave.add(error);
				attrsToSave.add(notif);
				
				if (card.getState().equals(ObjectId.state("jbr.replication.error"))) {
					IntegerAttribute retriesAttr = card.getAttributeById(ObjectId.predefined(IntegerAttribute.class, "jbr.replication.retries"));
					retriesAttr.setValue(retriesAttr.getValue()+1);
					attrsToSave.add(retriesAttr);
				}

				GetWorkflowMovesFromTargetState getWfms = new GetWorkflowMovesFromTargetState();
				getWfms.setCard(card);
				switch (rpg.getStatus().getStatusType()) {
					case CREATED:
					case UPDATE:
						getWfms.setToStateId(ObjectId.state("jbr.replication.accepted"));
						break;
					case COLLISION:
						getWfms.setToStateId(ObjectId.state("jbr.replication.collision"));
						break;
					case ERROR:
						getWfms.setToStateId(ObjectId.state("jbr.replication.error"));
				}
				if (!card.getState().equals(getWfms.getToStateId())) {
					List<Long> moveIds = serviceBean.doAction(getWfms);
					if (moveIds != null && !moveIds.isEmpty()) {
						WorkflowMove wfMove = serviceBean.getById(new ObjectId(WorkflowMove.class, moveIds.get(0)));
	
						ChangeState changeState = new ChangeState();
						changeState.setCard(card);
						changeState.setWorkflowMove(wfMove);
						serviceBean.doAction(changeState);
					} else {
						if (logger.isWarnEnabled()) {
							logger.warn("WFMs is empty for card " + ObjectIdUtils.getCardIdToString(card) + ", from state " + card.getState() + ", to state " + getWfms.getToStateId());
						}
					}
				}
			}
			
			serviceBean.doAction(new LockObject(card.getId()));
			try {
				OverwriteCardAttributes save = new OverwriteCardAttributes();
				save.setCardId(card.getId());
				save.setAttributes(attrsToSave);
				serviceBean.doAction(save);
			} finally {
				serviceBean.doAction(new UnlockObject(card.getId()));
			}
		}
	}
	
	private Card getReplicationCard(ReplicationPackage rpg) throws DataException, ServiceException {
		Search search = new Search();
		search.setTemplates(Collections.singleton(DataObject.createFromId(Template.class, "jbr.replication")));
		
		search.setByAttributes(true);
		search.addStringAttribute(CardRelationUtils.REPLIC_GUID,      rpg.getStatus().getGuid(), TextSearchConfigValue.EXACT_MATCH);
		search.addStringAttribute(CardRelationUtils.REPLIC_SENDER,    rpg.getSender(),           TextSearchConfigValue.EXACT_MATCH);
		search.addStringAttribute(CardRelationUtils.REPLIC_ADDRESSEE, rpg.getAddressee(),        TextSearchConfigValue.EXACT_MATCH);
		
		SearchResult sr = serviceBean.doAction(search);
		if (sr.getCards().isEmpty()) {
			return null;
		}
		return sr.getCards().get(0);
	}
}