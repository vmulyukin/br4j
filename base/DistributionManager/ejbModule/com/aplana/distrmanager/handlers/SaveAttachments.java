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
import com.aplana.distrmanager.cards.MessageGOST;

public class SaveAttachments {
	
	private final Log logger = LogFactory.getLog(getClass());
	
	private static final String SAVE_XML_ERROR = "jbr.DistributionManager.SaveAttachments.errorSaveXml";

	private SaveAttachments() {
	}
	
	public static SaveAttachments instance() {
		return new SaveAttachments();
	}
	
	public void handle(MessageGOST msgGOSTWrap, Result outXml) throws Exception {
		// ���������� ��� � ��������� ���� � ����� ��������
		try {
			msgGOSTWrap.saveAttachments(outXml);
		} catch (Exception exSave) {
			Card card = null;
			if (null != msgGOSTWrap)
				card = msgGOSTWrap.getCard();
			logError(card, SAVE_XML_ERROR, exSave);
		    throw exSave;
		}
	}
	
	private void logError(Card cardMsgGost, String msgError, Exception e) {
		String error = String.
			format("{%s}; msgGostId: {%s};",
					(null == msgError)?"null":msgError,
					(null == cardMsgGost)?"null":cardMsgGost.getId().getId()
			);
			logger.error(error, e);
	}
}
