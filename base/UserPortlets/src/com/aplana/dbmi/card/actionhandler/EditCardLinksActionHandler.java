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
package com.aplana.dbmi.card.actionhandler;

import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;

import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.ListEditForm;
import com.aplana.dbmi.card.PortletFormManager;
import com.aplana.dbmi.card.PortletFormManagerAware;
import com.aplana.dbmi.gui.CardLinkData;
import com.aplana.dbmi.gui.IListEditor;
import com.aplana.dbmi.gui.LinkChooser;
import com.aplana.dbmi.gui.ListDataProvider;
import com.aplana.dbmi.gui.ListEditor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

public class EditCardLinksActionHandler extends CardPortletAttributeEditorActionHandler implements PortletFormManagerAware {
	private PortletFormManager portletFormManager;
	
	@Override
	public void process(Attribute attr, List<ObjectId> cardIds, ActionRequest request, ActionResponse response) throws DataException {
		CardLinkAttribute cardLinkAttribute = (CardLinkAttribute) attr;
		ListDataProvider adapter;
		try {
			adapter = new CardLinkData(cardLinkAttribute, serviceBean);
			IListEditor editor = attr.isMultiValued() ? (IListEditor)new ListEditor() : (IListEditor)new LinkChooser();	
			editor.setDataProvider(adapter);
			portletFormManager.openForm(new ListEditForm(editor));
		} catch (ServiceException e) {
			logger.error("Exception caught", e);
		}
	}

	@SuppressWarnings("unused")
	protected CardPortletSessionBean getCardPortletSessionBean(PortletRequest request) {
		return null;
	}

	@Override
	public boolean isApplicableForCard(Card c) {
		return true;
	}

	public void setPortletFormManager(PortletFormManager portletFormManager) {
		this.portletFormManager = portletFormManager;
	}
}
