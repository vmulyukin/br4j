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

import com.aplana.dbmi.action.GetCardIdByUUID;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.replication.action.LinkResolver;
import com.aplana.dbmi.replication.packageconfig.PackageType;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Types;
import java.util.List;
import java.util.UUID;

public class CardRelationUtils {
	private final static Log logger = LogFactory.getLog(CardRelationUtils.class);
	public static final ObjectId REPLICATION_UUID    = ObjectId.predefined(StringAttribute.class,   "common.replicationUUID");
	public static final ObjectId REPLIC_OWNER        = ObjectId.predefined(StringAttribute.class,   "common.replicationOwner");
	public static final ObjectId REPLIC_FLAG         = ObjectId.predefined(IntegerAttribute.class,  "common.replicationFlag");
	public static final ObjectId REPLIC_VERSION      = ObjectId.predefined(StringAttribute.class,   "common.replicationVersion");
	public static final ObjectId REPLIC_CARDID       = ObjectId.predefined(IntegerAttribute.class,  "jbr.replication.cardID");
	public static final ObjectId REPLIC_LOCAL_GUID   = ObjectId.predefined(StringAttribute.class,   "jbr.replication.replicLocalGuid");
	public static final ObjectId REPLIC_GUID         = ObjectId.predefined(StringAttribute.class,   "jbr.replication.replicGiud");
	public static final ObjectId REPLIC_REPLICATE_OF = ObjectId.predefined(CardLinkAttribute.class, "jbr.replication.replicatedDocLink");
	public static final ObjectId REPLIC_BASEDOC_LNK  = ObjectId.predefined(CardLinkAttribute.class, "jbr.replication.baseDocLink");
	public static final ObjectId REPLIC_LOCALDOC_LNK = ObjectId.predefined(CardLinkAttribute.class, "jbr.replication.localDocLink");
	public static final ObjectId REPLIC_ADDRESSEE    = ObjectId.predefined(StringAttribute.class,   "jbr.replication.replicAddressee");
	public static final ObjectId REPLIC_SENDER       = ObjectId.predefined(StringAttribute.class,   "jbr.replication.replicSender");
	public static final ObjectId REPLIC_DATE_SENT    = ObjectId.predefined(DateAttribute.class,     "jbr.replication.replicDateSent");
	public static final ObjectId REPLIC_DATE_RECEIVE = ObjectId.predefined(DateAttribute.class,     "jbr.replication.replicDateReceipt");
	public static final ObjectId REPLIC_DOC_TYPE     = ObjectId.predefined(ListAttribute.class,     "jbr.replication.replicatingDocType");

	public static Card getLinkedCard(ObjectId sourceCardId, String path, DataServiceFacade dataService) throws DataException {
		List<ObjectId> cardIds = resolveLink(sourceCardId, dataService, path);
		if (!cardIds.isEmpty()) {
			return dataService.getById(cardIds.get(0));
		}
		return null;
	}

	public static ObjectId getReplicationCardForLocalCopy(Card card, DataServiceFacade dataService)
			throws DataException {
		StringAttribute guidAttribute = card.getAttributeById(REPLICATION_UUID);
		if (guidAttribute != null && guidAttribute.getValue() != null && !guidAttribute.getValue().isEmpty()) {
			return execGetCardIdByUUID(guidAttribute.getValue(), REPLIC_LOCAL_GUID,  dataService);
		}
		return null;
	}

	public static ObjectId getReplicationCardForLocalCopy(ObjectId cardId, DataServiceFacade dataService)
			throws DataException {
		List<String> uids = resolveLink(cardId, dataService, REPLICATION_UUID.getId());
		if (uids != null && !uids.isEmpty()) {
			return execGetCardIdByUUID(uids.get(0), REPLIC_LOCAL_GUID, dataService);
		}
		return null;
	}
	
	private static ObjectId execGetCardIdByUUID(String uuid, ObjectId attr, DataServiceFacade dataService) throws DataException {
		GetCardIdByUUID getAction = new GetCardIdByUUID();
		getAction.setUuid(uuid);
		getAction.setAttrId(attr);
		return dataService.doAction(getAction);
	}
	
	public static ObjectId getLocalCardId(ObjectId srcCardId, DataServiceFacade dataService) throws DataException {
		List<String> uids = resolveLink(srcCardId, dataService, REPLICATION_UUID.getId());
		if (uids != null && !uids.isEmpty()) {
			ObjectId replCardId = execGetCardIdByUUID(uids.get(0), REPLIC_LOCAL_GUID, dataService);
			if (replCardId != null) {
				return srcCardId;
			} else {
				replCardId = execGetCardIdByUUID(uids.get(0), REPLIC_GUID, dataService);
				if (replCardId == null) {
					return null;
				}
				List<String> localUids = resolveLink(replCardId, dataService, REPLIC_LOCAL_GUID.getId());
				if (localUids == null || localUids.isEmpty()) {
					return null;
				}
				ObjectId localCardId = execGetCardIdByUUID(localUids.get(0), REPLICATION_UUID, dataService);
				return localCardId;
			}
		}
		return null;
	}
	
	public static ObjectId getBaseCardForLocalCard(ObjectId localCardId, DataServiceFacade dataService) throws DataException {
		List<ObjectId> baseDocIds = resolveLink(localCardId, dataService, REPLIC_BASEDOC_LNK.getId());
		if (baseDocIds != null && !baseDocIds.isEmpty()) {
			return baseDocIds.get(0);
		} else {
			ObjectId replCard = getReplicationCardForLocalCopy(localCardId, dataService);
			List<String> baseUids = resolveLink(replCard, dataService, REPLIC_GUID.getId());
			if (baseUids != null && !baseUids.isEmpty()) {
				ObjectId baseDocId = execGetCardIdByUUID(baseUids.get(0), REPLICATION_UUID, dataService);
				return baseDocId;
			}
		}
		return null;
	}
	
	public static <T> List<T> resolveLink(ObjectId id, DataServiceFacade performer, Object ... args) throws DataException {
		LinkResolver<T> linkResolve = new LinkResolver<T>();
		linkResolve.setCardId(id);
		String link = StringUtils.join(args, ".");
		linkResolve.setLink(link);
		return performer.doAction(linkResolve);
	}
	
	public static void createReplicationCardForLocalCopy(Card linkedCard, String sender, String addressee, DataServiceFacade dataService, JdbcTemplate jdbc)
			throws JAXBException, IOException, DataException {
		ReplicationCardHandler replicationHandler = new ReplicationCardHandler(dataService);
		ReplicationPackage pcg = new ReplicationPackage();
		pcg.setPackageType(PackageType.CARD);
		pcg.setAddressee(addressee);
		pcg.setSender(sender);
		ReplicationPackage.Card pcgCard = new ReplicationPackage.Card();
		pcg.setCard(pcgCard);
		String cardGuid = UUID.randomUUID().toString();
		pcgCard.setGuid(cardGuid);
		Card replicCard = replicationHandler.createBlankCard(pcg);
		
		linkReplicationCard(linkedCard, replicCard, addressee, jdbc);
		setReplOrganizations(linkedCard, replicCard, dataService);
	}

	private static void linkReplicationCard(Card linkedCard, Card replicationCard, String addressee, JdbcTemplate jdbc) throws DataAccessException {
		String localUuid = UUID.randomUUID().toString();
		jdbc.update(
				"INSERT INTO attribute_value(card_id, attribute_code, string_value) VALUES (?, ?, ?);",
				new Object[] { replicationCard.getId().getId(), CardRelationUtils.REPLIC_LOCAL_GUID.getId(), localUuid },
				new int[] { Types.NUMERIC, Types.VARCHAR, Types.VARCHAR });
		jdbc.update(
				"INSERT INTO attribute_value(card_id, attribute_code, number_value) VALUES (?, ?, ?);",
				new Object[] { replicationCard.getId().getId(), CardRelationUtils.REPLIC_LOCALDOC_LNK.getId(), linkedCard.getId().getId() },
				new int[] { Types.NUMERIC, Types.VARCHAR, Types.NUMERIC });
		jdbc.update(
				"INSERT INTO attribute_value(card_id, attribute_code, string_value) VALUES (?, ?, ?);",
				new Object[] { linkedCard.getId().getId(), CardRelationUtils.REPLICATION_UUID.getId(), localUuid },
				new int[] { Types.NUMERIC, Types.VARCHAR, Types.VARCHAR });
		jdbc.update(
				"INSERT INTO attribute_value(card_id, attribute_code, string_value) VALUES (?, ?, ?);",
				new Object[] { linkedCard.getId().getId(), CardRelationUtils.REPLIC_OWNER.getId(), addressee },
				new int[] { Types.NUMERIC, Types.VARCHAR, Types.VARCHAR });
		linkedCard.<StringAttribute>getAttributeById(CardRelationUtils.REPLICATION_UUID).setValue(localUuid);
		linkedCard.<StringAttribute>getAttributeById(CardRelationUtils.REPLIC_OWNER).setValue(addressee);
	}
	
	public static void setReplOrganizations(Card mainCard, Card replicationCard, DataServiceFacade dataService) throws DataException {
		ReplicationCardHandler replicationHandler = new ReplicationCardHandler(dataService);
		boolean recipientOrganization = replicationHandler.setRecipientOrganization(mainCard, replicationCard);
		boolean senderOrganization    = replicationHandler.setSenderOrganization(mainCard, replicationCard);
		if (!recipientOrganization)
			logger.error("Not found recipient organization");
		if (!senderOrganization)
			logger.error("Not found sender organization");
	}

}