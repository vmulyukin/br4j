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

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.replication.processors.ReplicationConfiguration;
import com.aplana.dbmi.replication.templateconfig.ReplicationTemplateConfig;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.utils.StrUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReplicationInfo {

	private Card card;
	private ReplicationTemplateConfig.Template templateConfig;
	private DataServiceFacade dataService;
	private ReplicationCardHandler replicationHandler;

	public ReplicationInfo(Card card, DataServiceFacade dataService) {
		setDataService(dataService);
		setCard(card);
	}

	public List<String> getAddressee() throws DataException {
		String guid = calculateActualGuid();

		if (StrUtils.isStringEmpty(guid)) {
			return Collections.emptyList();
		}

		List<Card> replicationCards = replicationHandler.searchActiveReplicationCardsByGuid(guid,
				CardRelationUtils.REPLIC_ADDRESSEE);

		List<String> result = new ArrayList<String>();
		for (Card replicCard : replicationCards) {
			StringAttribute addresseeAttr = replicCard.getAttributeById(CardRelationUtils.REPLIC_ADDRESSEE);
			if (addresseeAttr != null) {
				result.add(addresseeAttr.getValue());
			}
		}

		return result;
	}

	public boolean needReplication() throws DataException {
		String guid = calculateActualGuid();

		return !StrUtils.isStringEmpty(guid) && !replicationHandler.searchActiveReplicationCardsByGuid(guid).isEmpty();
	}

	public boolean canBeLoaded() throws DataException {
		String guid = calculateActualGuid();

		return StrUtils.isStringEmpty(guid) || !replicationHandler.searchActiveReplicationCardsByGuid(guid).isEmpty();
	}

	private String calculateActualGuid() throws DataException {
		if (templateConfig != null && templateConfig.getRoot() != null && templateConfig.getRoot().getPath() != null) {
			String path = templateConfig.getRoot().getPath() + "." + CardRelationUtils.REPLICATION_UUID.getId();
			List<String> result = CardRelationUtils.resolveLink(getCard().getId(), dataService, path);
			if (result != null && result.size() > 0) {
				return result.get(0);
			}
		} else {
			StringAttribute replicationUid = card.getAttributeById(CardRelationUtils.REPLICATION_UUID);
			if (replicationUid != null) {
				return replicationUid.getValue();
			}
		}
		return null;
	}

	protected Card getCard() {
		return this.card;
	}

	public void setCard(Card card) {
		this.card = card;
		try {
			this.templateConfig = ReplicationConfiguration.getTemplateConfig((Long) card.getTemplate().getId());
		} catch (Exception ex) {
			throw new IllegalStateException("System error", ex);
		}
	}

	public void setDataService(DataServiceFacade dataService) {
		this.dataService = dataService;
		replicationHandler = new ReplicationCardHandler(dataService);
	}
}
