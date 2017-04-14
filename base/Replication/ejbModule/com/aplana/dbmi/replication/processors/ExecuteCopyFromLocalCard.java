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
package com.aplana.dbmi.replication.processors;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.replication.processors.beans.CopyChangesFromLocalCardHandler;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.DatabaseClient;
import org.springframework.beans.BeansException;

public class ExecuteCopyFromLocalCard extends ReplicationProcessorTemp {
	private static final long serialVersionUID = 1L;

	private ObjectId cardId;

	public ExecuteCopyFromLocalCard(ObjectId id) {
		this.cardId = id;
	}

	@Override
	public Object process() throws DataException {
		doCopyFromLocalCard();
		return null;
	}

	private void doCopyFromLocalCard() throws DataException {
		logger.info("-------Start CopyChangesFromLocalCardProcessor-------");
		try {
			Card card = loadCardById(cardId);
			CopyChangesFromLocalCardHandler handler = createHandler(card);
			if (handler != null) {
				handler.cardChanged(card);
			}
		} catch (DataException ex) {
			logger.error("Error on execute do process", ex);
			throw ex;
		} catch (Exception ex) {
			logger.error("Error on execute do process", ex);
			throw new DataException("jbr.replication.common", new Object[] {""}, ex);
		}
	}

	private CopyChangesFromLocalCardHandler createHandler(Card card) throws BeansException {
		final String requiredBeanName = CopyChangesFromLocalCardHandler.BEAN_PREFIX + card.getTemplate().getId();
		if (!getBeanFactory().containsBean(requiredBeanName)) {
			throw new IllegalStateException("Copy handler for template [" + card.getTemplate().getId()
					+ "] is not configured. Contact administrator.");
		}
		CopyChangesFromLocalCardHandler handler = (CopyChangesFromLocalCardHandler) getBeanFactory().getBean(
				requiredBeanName);
		if (handler instanceof DatabaseClient) {
			((DatabaseClient) handler).setJdbcTemplate(getJdbcTemplate());
		}
		return handler;
	}
}
