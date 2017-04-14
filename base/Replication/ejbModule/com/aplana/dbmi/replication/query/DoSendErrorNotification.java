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
import com.aplana.dbmi.replication.action.SendErrorNotification;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage;
import com.aplana.dbmi.replication.packageconfig.StatusType;
import com.aplana.dbmi.replication.tool.ReplicationNotificationHandler;
import com.aplana.dbmi.replication.tool.ReplicationUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.query.WriteQuery;

public class DoSendErrorNotification extends ActionQueryBase implements WriteQuery {
	private static final long serialVersionUID = 1L;

	@Override
	public Object processQuery() throws DataException {
		try {
			SendErrorNotification action = getAction();
			Card card = action.getCard();
			DataServiceFacade service = new DataServiceFacade(getDatabase(), getSystemUser(), getQueryFactory());
			ReplicationPackage rpg = ReplicationUtils.getReplicationPackageFromCard(card, service);
			String message = action.getMessage();
			String reason  = action.getReason();

			ReplicationNotificationHandler handler = new ReplicationNotificationHandler(service);
			handler.sendNotification(rpg, StatusType.ERROR, message, reason);

			return null;
		} catch (DataException ex) {
			if(logger.isErrorEnabled())
				logger.error("Error on execute do process in " + this.getClass().getName(), ex);
			throw ex;
		} catch (Exception ex) {
			if(logger.isErrorEnabled())
				logger.error("Error on execute do process in " + this.getClass().getName(), ex);
			throw new DataException("Error on execute do process in " + this.getClass().getName(), ex);
		}
	}
}