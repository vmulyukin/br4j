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

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.ExportCardToXml.Result;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.distrmanager.cards.ElementListMailing;
import com.aplana.distrmanager.cards.MessageGOST;
import com.aplana.distrmanager.cards.StateCard;

public class CreateLetter {
	
	private final Log logger = LogFactory.getLog(getClass());
	
    private static final String CREATE_LETTER_ERROR = "jbr.DistributionManager.CreateLetter.errorCreateLetter";
    
    private DataServiceFacade serviceBean = null;
	
	private CreateLetter() {
	}
	
	private void init(DataServiceFacade serviceBean) {
		this.serviceBean = serviceBean;
	}
	
	public static CreateLetter instance(DataServiceFacade serviceBean) {
		CreateLetter cl = new CreateLetter();
		cl.init(serviceBean);
		return cl;
	}
	
	public Result handle(MessageGOST msgGOSTWrap, StateCard stateCardWrap, ElementListMailing elmCardWrap) throws Exception {
		// ��������� �������
		try {
			Map<ObjectId, String> attachments = msgGOSTWrap.getAttachments();
			OperationsOnLetter operations = new OperationsOnLetter(serviceBean);
			operations.init(stateCardWrap.getUuid(), elmCardWrap, attachments);
			Result xmlLetter = operations.getLetter();
			return xmlLetter;
		} catch(Exception exLetter) {
			ObjectId elmCardWrapId = null;
			ObjectId msgGOSTWrapId = null; 
			String elmCardWrapUid = null;
			if (null != elmCardWrap && null != elmCardWrap.getCard()) {
				elmCardWrapId = elmCardWrap.getCard().getId();
				elmCardWrapUid = elmCardWrap.getUid();
			}
			if (null != msgGOSTWrap && null != msgGOSTWrap.getCard())	
				msgGOSTWrapId = msgGOSTWrap.getCard().getId();
			logError(elmCardWrapId, msgGOSTWrapId, elmCardWrapUid, CREATE_LETTER_ERROR, exLetter);
			throw exLetter;
		}
	}
	
	private void logError(ObjectId elmCardWrapId, ObjectId msgGOSTWrapId, String elmCardWrapUid, String msgError, Exception e) {
		String error = String.
			format("{%s}; elmId: {%s}; elmUUID: {%s}; msgGostId: {%s};", 
					(null == msgError)?"null":msgError,
					(null == elmCardWrapId)?"null":elmCardWrapId.getId(), 
					(null == elmCardWrapUid)?"null":elmCardWrapUid, 
					(null == msgGOSTWrapId)?"null":msgGOSTWrapId.getId()
			);
			logger.error(error, e);
	}
}
