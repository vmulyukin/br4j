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

import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.distrmanager.cards.ElementListMailing;
import com.aplana.distrmanager.util.UtilsWorkingFiles;

public class SetTimeInMessage {
	
	private final Log logger = LogFactory.getLog(getClass());
	
	private static final String SET_TIME_ERROR = "jbr.DistributionManager.SetTimeInMessage.errorSetCreationTime";
	
	private DataServiceFacade serviceBean = null;

	private SetTimeInMessage() {
	}
	
	private void init(DataServiceFacade serviceBean) {
		this.serviceBean = serviceBean;
	}
	
	public static SetTimeInMessage instance(DataServiceFacade serviceBean) {
		SetTimeInMessage stim = new SetTimeInMessage();
		stim.init(serviceBean);
		return stim;
	}
	
	public void handle(ElementListMailing elmCardWrap, JdbcTemplate jdbcTemplate) throws Exception {
		Card card = null;
		String elmCardUuid = null;
		try {
			elmCardWrap.setMessageCreationTime(new GregorianCalendar().getTime());
			card = elmCardWrap.getCard();
			elmCardUuid = elmCardWrap.getUid();
			UtilsWorkingFiles.saveCardParent(card, serviceBean, elmCardWrap.getMessageCreationTimeAttr(), jdbcTemplate);
		} catch(Exception exSetTime) {
			logError(card, elmCardUuid, SET_TIME_ERROR, exSetTime);
		    throw exSetTime;
		}
	}
	
	private void logError(Card card, String elmCardUuid, String msgError, Exception e) {
		String error = String.
			format("{%s}; elmId: {%s}; elmUUID: {%s};",
					(null == msgError)?"null":msgError,
					(null == card)?"null":card.getId().getId(),
					(null == elmCardUuid)?"null":elmCardUuid
			);
			logger.error(error, e);
	}
}
