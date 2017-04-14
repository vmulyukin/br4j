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

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage.Card.Attribute;
import com.aplana.dbmi.replication.tool.CardRelationUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.DatabaseClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CopyChangesToLocalCard implements CopyChangesToLocalCardHandler, DatabaseClient {
	protected static Log logger = LogFactory.getLog(CopyChangesToLocalCard.class);
	protected List<String> filteredAttributes;
	protected ReplicationPackage replicationPackage;
	protected DataServiceFacade dataService;
	protected ObjectId sourceCardId;
	protected ObjectId replicationCardId;
	protected JdbcTemplate jdbc;

	@Override
	public void setReplicationPackage(ReplicationPackage pckg) {
		this.replicationPackage = pckg;
	}
	
	@Override
	public void setReplicationCardId(ObjectId card) {
		this.replicationCardId = card;
	}

	@Override
	public void preProcessPackage() throws DataException {
		ReplicationPackage.Card pckgCard = replicationPackage.getCard();
		for (Iterator<Attribute> iter = pckgCard.getAttribute().iterator(); iter.hasNext();) {
			Attribute pckgAttr = iter.next();
			preProcessAttr(pckgAttr);
			if (getFilteredAttributes().contains(pckgAttr.getCode())) {
				iter.remove();
			}
		}
	}

	protected void preProcessAttr(Attribute pckgAttr) throws DataException {
	}

	@Override
	public void postProcessPackage(Card card) throws DataException {
	}

	@Override
	public void postProcessPackageForNewCard(Card card) throws DataException {
		setLinkToReplicationCard(card);
	}

	protected void setLinkToReplicationCard(Card card) {
		jdbc.update("INSERT INTO attribute_value(card_id, attribute_code, number_value) VALUES (?, ?, ?);",
			new Object[] { replicationCardId.getId(), CardRelationUtils.REPLIC_LOCALDOC_LNK.getId(), card.getId().getId() }, 
			new int[]    { Types.NUMERIC,             Types.VARCHAR,                                 Types.NUMERIC });
	}

	public ReplicationPackage getReplicationPackage() {
		if (this.replicationPackage == null) {
			throw new IllegalStateException("Replication package was not set before using");
		}
		return this.replicationPackage;
	}

	public DataServiceFacade getDataService() {
		if (this.dataService == null) {
			throw new IllegalStateException("Data service was not set before using");
		}
		return this.dataService;
	}

	public void setDataService(DataServiceFacade dataService) {
		this.dataService = dataService;
	}

	public List<String> getFilteredAttributes() {
		if (this.filteredAttributes == null) {
			this.filteredAttributes = new ArrayList<String>();
		}
		return this.filteredAttributes;
	}

	public void setFilteredAttributes(List<String> filteredAttributes) {
		this.filteredAttributes = filteredAttributes;
	}

	@Override
	public void setSourceCardId(ObjectId cardId) {
		this.sourceCardId = cardId;
	}

	protected ObjectId getSourceCardId() {
		return this.sourceCardId;
	}

	@Override
	public void setJdbcTemplate(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

}