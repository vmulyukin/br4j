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
package com.aplana.dbmi.card.download.actionhandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aplana.dbmi.action.DownloadFileWithSignatures;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class WriteHistoryAction extends FileActionHandler {
	
	public static final String PARAM_CARD_ID = "cardId";

	public void process(HttpServletRequest request, HttpServletResponse response)
			throws DataException {
		DataServiceBean ds = this.getServiceBean();
		DownloadFileWithSignatures action = new DownloadFileWithSignatures();
		String target_cardId = request.getParameter( PARAM_CARD_ID );
		ObjectId oidCard = new ObjectId( Card.class, Long.parseLong(target_cardId) );
		action.setCardId(oidCard);
		try {
			ds.doAction(action);
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		
	}

}
