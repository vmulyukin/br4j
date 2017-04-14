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
package com.aplana.dbmi.replication.management;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.StrictSearch;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.replication.action.CalculateReplicationState;
import com.aplana.dbmi.replication.action.CreateReplicationPackage;
import com.aplana.dbmi.replication.packageconfig.PackageType;
import com.aplana.dbmi.replication.processors.ReplicationConfiguration;
import com.aplana.dbmi.replication.tool.ReplicationInfo;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jmx.export.annotation.*;

import java.util.*;

@ManagedResource(objectName="br4j:name=replicationManagerBean", 
description="MBean for Replication Management. Can replicate card by id, cards by template id, cards by all templates")
public class ReplicationManagerBean {
	private Log logger = LogFactory.getLog(getClass());
	private DataServiceFacade dataService;
	
	private WorkType type = WorkType.NONE;
	
	private ObjectId objId;
	private List<ObjectId> allCardsByTemplate 		= Collections.emptyList();
	private Set<ObjectId> replicated 				= Collections.emptySet();
	private Set<ObjectId> failed	 				= Collections.emptySet();
	private Map<ObjectId, Integer> countByTemplate	= Collections.emptyMap();
	
	private StringBuffer resultLog = new StringBuffer();
	private StringBuffer resultLogExt = new StringBuffer();
	private String lastResult;

	private boolean interrupt = false;
	
	private enum WorkType {
		NONE,
		ONE_CARD,
		ONE_TEMPLATE,
		ALL_TEMPLATES
	}
	
	@ManagedOperation(description="Replicate all cards of selected vocabulary template")
	  @ManagedOperationParameters({
	    @ManagedOperationParameter(name = "id", description = "Template's ID")})
	public String replicateTemplateById(final long id) throws DataException {
		validate(WorkType.ONE_TEMPLATE);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					replicateTemplate(id);
				} catch (Exception e) {
					logger.error("Error", e);
				} finally {
					endWork();
				}
			}
		}).start();
		
		return "Started. Check status by method getWorkStatus()";
	}
	
	@ManagedOperation(description="Replicate card by id")
	  @ManagedOperationParameters({
	    @ManagedOperationParameter(name = "id", description = "card_id")})
	public String replicateCardById(final long id) throws DataException {
		validate(WorkType.ONE_CARD);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					objId = new ObjectId(Card.class, id);
					replicateCard(objId);
				} catch (Exception e) {
					logger.error("Can't replicate card with id:" + id, e);
					resultLog.append(e.getMessage()).append("\n").append(e);
				} finally {
					endWork();
				}
			}
		}).start();
		
		return "Started. Check status by method getWorkStatus()";
	}

	@ManagedOperation(description="Replicate all vocabulary templates")
	protected String replicateAll() throws DataException {
		validate(WorkType.ALL_TEMPLATES);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					List<ObjectId> indep = ReplicationConfiguration.getIndependentTemplates();
					for (ObjectId templateId : indep) {
						if (interrupt) {
							break;
						}
						replicateTemplate((Long)templateId.getId());
						resultLog.append("\n\n=======================\n\n");
					}
					String result = resultLog.toString();
					logger.info(result);
				} catch (Exception e) {
					logger.error("Error", e);
				} finally {
					endWork();
				}
			}
		}).start();
		
		return "Started. Check status by method getWorkStatus()";
	}
	
	@ManagedOperation(description="Status")
	  @ManagedOperationParameters({
		    @ManagedOperationParameter(name = "Extended log", description = "Empty - simple log, not empty (any value) - extended log")})
	public String getWorkStatus(String ext) {
		String res = null;
		switch (type) {
			case NONE:
				if (lastResult == null) {
					return "Not working";
				} else {
					res = "Not working\nLast execution log:\n" + lastResult;
					break;
				}
			case ALL_TEMPLATES:
			case ONE_TEMPLATE:
				StringBuilder sb = new StringBuilder(resultLog);
				sb.append("\n=========CURRENT EXECUTING=========\n");
				sb.append("Summary count of cards by template '").append(objId).append("' is: ").append(allCardsByTemplate.size());
				sb.append("\n");
				sb.append("Summary replicated: ").append(replicated.size()).append(" cards.");
				sb.append("\n");
				sb.append("Summary failed: ").append(failed.size()).append(" cards.");
				sb.append("\n\n");
				sb.append("Additional info by templates:\n");
				for (ObjectId template : countByTemplate.keySet()) {
					sb.append("\tTemplate '").append(template.getId()).append("'\t:  replicated ").append(countByTemplate.get(template)).append(" cards.");
					sb.append("\n");
				}
				res = sb.toString();
				break;
			case ONE_CARD:
				res = "\n\n=========CURRENT EXECUTING=========\nReplicating card by id: " + objId;
				break;
		}
		if (!ext.isEmpty()) {
			StringBuilder extLog = new StringBuilder(res);
			extLog.append("\n\nEXTENDED LOG\n\n").append(resultLogExt);
			extLog.append("\n\nFailed cards:\n");
			for (ObjectId failId : failed) {
				extLog.append(failId).append("\n");
			}
			res = extLog.toString();
		}
		return res;
	}
	
	@ManagedOperation(description="Summary info about count of cards by all vocabulary templates. Need to know which templates to replicate")
	public String getCardInfoByAllTemplates(String stub) throws DataException {
		StringBuilder sb = new StringBuilder();
		List<ObjectId> templates = ReplicationConfiguration.getIndependentTemplates();
		for (ObjectId templateId : templates) {
			Search search = new Search();
			search.setByAttributes(true);
			search.setTemplates(Collections.singleton(DataObject.createFromId(templateId)));
			search.setCountOnly(true);
			search.setDontFetch(true);
			dataService.doAction(search);
			sb.append("Template ");
			sb.append(templateId.getId());
			sb.append(", count: ");
			sb.append(search.getFilter().getWholeSize());
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public void setDataService(DataServiceFacade dataService) {
		this.dataService = dataService;
	}
	
	private String replicateTemplate(long id) throws DataException {
		objId = new ObjectId(Template.class, id);
		StrictSearch search = new StrictSearch();
		search.setTemplates(Collections.singleton(DataObject.createFromId(objId)));
		allCardsByTemplate = dataService.doAction(search);
		replicated 		= new HashSet<ObjectId>();
		countByTemplate = new HashMap<ObjectId, Integer>();
		int countFailure = 0;

		for (ObjectId cardId : allCardsByTemplate) {
			if (interrupt) {
				break;
			}
			if (!replicated.contains(cardId)) {
				try {
					replicateCard(cardId);
				} catch (Exception e) {
					logger.error("Can't replicate card: " + cardId, e);
					failed.add(cardId);
					countFailure++;
				}
			}
		}
		resultLog.append("\n");
		resultLog.append("Summary count of cards by template '").append(objId).append("' is: ").append(allCardsByTemplate.size());
		resultLog.append("\n");
		resultLog.append("Summary replicated: ").append(replicated.size()).append(" cards.");
		resultLog.append("\n");
		resultLog.append("Failed by this template: ").append(countFailure).append(" cards.");
		resultLog.append("\n\n");
		resultLog.append("Additional info by templates:\n");
		for (ObjectId template : countByTemplate.keySet()) {
			resultLog.append("\tTemplate '").append(template.getId()).append("'\t:  replicated ");
			resultLog.append(countByTemplate.get(template)).append(" cards.");
			resultLog.append("\n");
		}
		String result = resultLog.toString();
		logger.error(result);
		return result;
	}
	
	private void replicateCard(ObjectId cardId) throws DataException {
		Card card = dataService.getById(cardId);
		
		CalculateReplicationState calcState = new CalculateReplicationState();
		calcState.setCard(card);
		dataService.doAction(calcState);
		
		if (needReplication(card)) {
			CreateReplicationPackage create = new CreateReplicationPackage();
			create.setPackageType(PackageType.CARD);
			create.setCard(card);
			Set<Card> replicatedCards = dataService.doAction(create);
			for (Card c : replicatedCards) {
				replicated.add(c.getId());
				Integer cnt = countByTemplate.get(c.getTemplate());
				if (cnt == null) {
					cnt = 0;
				}
				countByTemplate.put(c.getTemplate(), ++cnt);
			}
			Collection<?> ids = CollectionUtils.collect(replicatedCards, new Transformer() {
				@Override
				public Object transform(Object arg) {
					return ((Card)arg).getId();
				}
			});
			resultLogExt.append("Replicated cards:\n\t").append(StringUtils.join(ids, "\n\t"));
			resultLogExt.append("\n");
		}
	}
	
	private boolean needReplication(Card card) throws DataException {
		ReplicationInfo replicationInfo = new ReplicationInfo(card, dataService);
		return replicationInfo.needReplication();
	}
	
	private void validate(WorkType wt) {
		synchronized (this) {
			switch (type) {
				case ALL_TEMPLATES:
				case ONE_TEMPLATE:
				case ONE_CARD:
					throw new RuntimeException("Already working "+type+" replication. Check status by method 'getWorkStatus()'");
			}
			interrupt = false;
			replicated 		= new HashSet<ObjectId>();
			failed     		= new HashSet<ObjectId>();
			countByTemplate = new HashMap<ObjectId, Integer>();
			type = wt;
			resultLog = new StringBuffer("Started ");
			resultLog.append(wt).append(" at ").append(new Date()).append("...\n\n");
			lastResult = null;
		}
	}
	
	private void endWork() {
		resultLog.append("\nDONE at ").append(new Date());
		lastResult = resultLog.toString();
		type = WorkType.NONE;
	}
	
	@ManagedOperation(description="Interrupt execution")
	public void stop() throws DataException {
		//throw new RuntimeException("Stopping execution");
		interrupt = true;
	}

	@ManagedOperation(description="Switch off replication (not scheduled replication task)")
	public void deactivateReplication() throws Exception {
		ReplicationConfiguration.deactivate();
	}

	@ManagedOperation(description="Switch on replication")
	public void activateReplication() throws Exception {
		ReplicationConfiguration.activate();
	}

	@ManagedAttribute
	public boolean isReplicationActive() {
		return ReplicationConfiguration.isReplicationActive();
	}
}
