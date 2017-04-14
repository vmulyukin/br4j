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


import java.io.InputStream;

import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.ListEditForm;
import com.aplana.dbmi.card.Parametrized;
import com.aplana.dbmi.card.PortletFormManager;
import com.aplana.dbmi.card.PortletFormManagerAware;
import com.aplana.dbmi.gui.AddSubReportCardAttachmentData;

import com.aplana.dbmi.gui.ListDataProvider;
import com.aplana.dbmi.gui.SubReportCardAttachmentListEditor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

/**
 * Represents card action for adding subordinate report card's attachments
 * 
 * @author skashanski
 * 
 */
public class AddSubReportCardAttachmentActionHandler extends
		CardPortletAttributeEditorActionHandler implements
		PortletFormManagerAware,  Parametrized  {
	
	private PortletFormManager portletFormManager;
	public static final String CONFIG = "config";
	public static final String CONFIG_FOLDER = "dbmi/card/";
	private Search search;

	public void setPortletFormManager(PortletFormManager portletFormManager) {
		this.portletFormManager = portletFormManager;
	}


	/**
	 * Returns Active card
	 */
	protected Card getActiveCard() {
		
		CardPortletSessionBean sessionBean = getCardPortletSessionBean();
		
		return sessionBean.getActiveCard();
		
	}
	
	
	

	public void setParameter(String name, String value) {
		
		if (CONFIG.equals(name)) {
			loadActionConfig(value);
		}
		
		
	}


	private void loadActionConfig(String value) {
		search = new Search();
		InputStream xml = null;
		try {
			String fullPath = CONFIG_FOLDER + value;
			xml = Portal.getFactory().getConfigService().loadConfigFile(fullPath);
			search.initFromXml(xml);
			
		} catch (Exception e) {
			logger.error("Error initializing search ", e);
		} finally {
			try {
				xml.close();
			} catch (Exception e2) {  }
		}
	}


	@Override
	protected void process(Attribute attr, List<ObjectId> cardIds,
			ActionRequest request, ActionResponse response)
			throws DataException {
		
		try {
			ListDataProvider adapter = new AddSubReportCardAttachmentData(search,
					getActiveCard(), (CardLinkAttribute) attr,serviceBean);
			SubReportCardAttachmentListEditor editor = new SubReportCardAttachmentListEditor();
			editor.setDataProvider(adapter);
			initDisplayParameters(editor);
			portletFormManager.openForm(new ListEditForm(editor));
		} catch (ServiceException e) {
			handleSystemException(e);
		}


	}
	
	private void handleSystemException(Exception e) {
		logger.error("Exception caught", e);
		
		CardPortletSessionBean sessionBean = getCardPortletSessionBean();
		sessionBean.setMessage("db.side.error.msg.param", new Object[] { e
				.getMessage() });
	}
	
	
	private void initDisplayParameters(SubReportCardAttachmentListEditor editor) {
		
		//display linked columns as "Link:
		editor.setDisplayLinkedColumns(true);
		
	}

	
	
	
	/**
	 * Checks if we can modify given card
	 * 
	 * @return true if we can modify passed card
	 */
	private boolean canModifyCard(Card card)  {
		
		if (card.getId() == null)
			return true;

		try {
			if (!serviceBean.canChange(card.getId()))
				return false;
			
			return true;
			
		} catch (ServiceException e) {
			handleSystemException(e);
			return false;
		} catch (DataException e) {
			handleSystemException(e);
			return false;
		}
		
		
		
	}
	

	@Override
	public boolean isApplicableForUser() {
		Card card = getActiveCard();
		if (!canModifyCard(card))
			return false;
		
		return super.isApplicableForUser();
	}
	
	

	
	

}
