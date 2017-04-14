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

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.replication.action.CheckStopCondition;
import com.aplana.dbmi.replication.templateconfig.TemplateFilter;
import com.aplana.dbmi.replication.tool.ReplicationActiveProcessInfo;
import com.aplana.dbmi.replication.tool.ReplicationCardHandler;
import com.aplana.dbmi.service.DataException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Types;
import java.util.List;

public class DoCheckStopCondition extends DoCalculateReplicationState {
	private static final long serialVersionUID = 1L;
	private static final int REPLICATION_IN_PROCESS_FALSE = 0;

	@Override
	public Object processQuery() throws DataException {
		CheckStopCondition action = getAction();
		try {
			check(action.getCard());
		} catch (JAXBException e) {
			throw new DataException(e);
		} catch (IOException e) {
			throw new DataException(e);
		}
		return null;
	}

	private void check(Card changedCard) throws DataException, JAXBException, IOException {
		if (!ReplicationActiveProcessInfo.isAllProcessed(getPrimaryQuery().getUid())) {
			return;
		}

		if (!initTemplateConfig(changedCard)) {
			return;
		}

		TemplateFilter stopCondition = mainTemplateConfig.getStopCondition();
		if (stopCondition != null && checkFilters(mainTemplateConfig.getStopCondition(), mainCard)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Stop replication for [" + getCardDescription(changedCard) + "]. Use main card ["
						+ getCardDescription(mainCard) + "]");
			}
			stopReplication(mainCard);
		}
	}

	private void stopReplication(Card changedCard) throws DataException {
		ReplicationCardHandler replicationHandler = new ReplicationCardHandler(getQueryFactory(), getDatabase(), getUser());
		List<Card> replicationCards = replicationHandler.getActiveReplicationCards(changedCard);
		for (Card replicCard : replicationCards) {
			getJdbcTemplate().update(
					"UPDATE attribute_value \n SET number_value = " + REPLICATION_IN_PROCESS_FALSE + "\n"
							+ "WHERE card_id = ? AND attribute_code = ?",
					new Object[] {replicCard.getId().getId(), "REPLIC_IN_PROCESS"},
					new int[] {Types.NUMERIC, Types.VARCHAR});
		}
	}
}
