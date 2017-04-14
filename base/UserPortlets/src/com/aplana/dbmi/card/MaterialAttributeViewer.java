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
package com.aplana.dbmi.card;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;

import com.aplana.dbmi.action.DownloadFile;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardVersion;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;


public class MaterialAttributeViewer extends JspAttributeEditor {
	protected final static String CAN_DOWNLOAD_KEY = "canDownload";	
	
	public MaterialAttributeViewer() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/MaterialView.jsp");
	}

	public boolean doesFullRendering(Attribute attr) {
		return true;
	}

	public void initEditor(PortletRequest request, Attribute attr)
			throws DataException {
		DownloadFile downloadAction = new DownloadFile();
		CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
		Card card = sessionBean.getActiveCard();
		if (card instanceof CardVersion) {
			CardVersion cardVersion = (CardVersion)card;
			downloadAction.setCardId(new ObjectId(Card.class, cardVersion.getCardId()));
			downloadAction.setVersionId(cardVersion.getVersion());
		} else {
			downloadAction.setCardId(card.getId());
		}
		boolean canDownload;
		try {
			canDownload = sessionBean.getServiceBean().canDo(downloadAction);
		} catch (Exception e) {
			logger.error("Couldn't check access rights for downloading file. Assuming false.", e);
			canDownload = false;
		}
		sessionBean.setAttributeEditorData(attr.getId(), CAN_DOWNLOAD_KEY, new Boolean(canDownload));
	}

	protected Map getReferenceData(Attribute attr, PortletRequest request) {
		Map result = new HashMap(1);
		result.put("canDownload", getCardPortletSessionBean(request).getAttributeEditorData(attr.getId(), CAN_DOWNLOAD_KEY));
		return result;
	}

	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException {
		return false;
	}
}
