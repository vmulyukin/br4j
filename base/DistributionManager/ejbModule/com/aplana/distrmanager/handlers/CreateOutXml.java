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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.ExportCardToXml.Result;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TypeStandard;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.distrmanager.OutXml;

public class CreateOutXml {
	
	private final Log logger = LogFactory.getLog(getClass());
	
	private static final String CREATE_XML_DOCBASE_ERROR = "jbr.DistributionManager.CreateOutXml.errorCreateXmlDocbase";
	
	private DataServiceFacade serviceBean = null;
	
	private CreateOutXml() {
	}
	
	private void init(DataServiceFacade serviceBean) {
		this.serviceBean = serviceBean;
	}
	
	public static CreateOutXml instance(DataServiceFacade serviceBean) {
		CreateOutXml cox = new CreateOutXml();
		cox.init(serviceBean);
		return cox;
	}
	
	public Result handle(Card elmCard, ObjectId docBaseId) throws Exception {
		Result xmlOutResult = null;
		try {
			ObjectId idElm = elmCard.getId();
			TypeStandard typeStandard = TypeStandard.GOST;
			// ������� ���
			OutXml xmlOut = new OutXml(serviceBean);
			xmlOutResult = xmlOut.createMessageXml(docBaseId, idElm, typeStandard);
			return xmlOutResult;
		} catch(Exception exXmlDocBase) {
			logError(elmCard, docBaseId, CREATE_XML_DOCBASE_ERROR, exXmlDocBase);
		    throw exXmlDocBase;
		}
	}
	
	private void logError(Card card, ObjectId docBaseId, String msgError, Exception e) {
		String error = String.
			format("{%s} docBaseId: {%s}; elmId: {%s};",
					(null == msgError)?"null":msgError,
					(null == docBaseId)?"null":docBaseId.getId(),	
					(null == card)?"null":card.getId().getId()
			);
			logger.error(error, e);
	}
}
