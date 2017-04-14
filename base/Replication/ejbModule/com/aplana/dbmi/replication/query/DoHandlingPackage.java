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
package com.aplana.dbmi.replication.query;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.replication.action.HandlingPackage;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage;
import com.aplana.dbmi.replication.tool.CardRelationUtils;
import com.aplana.dbmi.replication.tool.ReplicationCardHandler;
import com.aplana.dbmi.replication.tool.ReplicationCardHandler.ProcessType;
import com.aplana.dbmi.replication.tool.ReplicationUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.query.WriteQuery;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Collections;

public class DoHandlingPackage extends ActionQueryBase implements WriteQuery {
	private static final long serialVersionUID = 1L;

	@Override
	public Object processQuery() throws DataException {
		try {
			Card result = null;
			HandlingPackage action = getAction();
			ReplicationPackage replicationPackage = null;
			if (action.getReplicationCardGuid() != null){
				Search search = new Search();
				search.setTemplates(Collections.singleton(DataObject.createFromId(Template.class, "jbr.replication")));
				search.setByAttributes(true);
				search.addStringAttribute(CardRelationUtils.REPLIC_GUID,      action.getReplicationCardGuid());
				search.addStringAttribute(CardRelationUtils.REPLIC_SENDER,    action.getSender());
				search.addStringAttribute(CardRelationUtils.REPLIC_ADDRESSEE, action.getAddressee());
				ActionQueryBase query = getQueryFactory().getActionQuery(search);
				query.setAction(search);
				SearchResult sr = getDatabase().executeQuery( getUser(), query);
				
				if (sr.getCards().size() == 1){
					ObjectQueryBase objectQuery = getQueryFactory().getFetchQuery(Card.class);
					objectQuery.setId(sr.getCards().get(0).getId());
					Card card = getDatabase().executeQuery(getUser(), objectQuery);
					DataServiceFacade dataService = new DataServiceFacade(getDatabase(), getSystemUser(), getQueryFactory());
					replicationPackage = ReplicationUtils.getReplicationPackageFromCard(card, dataService);
				}
			}else{
				replicationPackage = action.getPackageXml();
			}
			if (replicationPackage != null){
				result = processPackage(replicationPackage);
			}
			return result;
		} catch (DataException ex) {
			if(logger.isErrorEnabled())
				logger.error("Error on execute do process in " + getClass().getName(), ex);
			throw ex;
		} catch (Exception ex) {
			if(logger.isErrorEnabled())
				logger.error("Error on execute do process in " + getClass().getName(), ex);
			throw new DataException("Error on execute do process in " + getClass().getName(), ex);
		}
	}
	
	private Card processPackage(ReplicationPackage replicationPackage) throws DataException, JAXBException, IOException{
		ReplicationCardHandler handler = new ReplicationCardHandler(getQueryFactory(), getDatabase(), getUser());
		Card replicCard = handler.processPackage(replicationPackage, ProcessType.RECEIVE);
		return replicCard;
	}
}
