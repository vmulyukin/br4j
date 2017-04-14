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
package com.aplana.dbmi.card.actionhandler.multicard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.portlet.RenderRequest;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.BatchAsyncExecution;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.CardPortletCardInfo.CustomStoreHandler;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.support.action.ProcessGroupExecution;

public class GroupExecutionHandler extends SpecificCustomStoreHandler {

	private final String TEXT_EXECUTE_BUTTON = "edit.page.execute.button";

	protected final String REPORTS_PARAM = "REPORT_GROUP";
	
	private final ObjectId CURRENT_REPORT_ATTR_ID = ObjectId.predefined(
			TextAttribute.class, "jbr.report.currentText");

	private boolean wasCopied = false;

	protected List<ObjectId> reports;

	private final static ObjectId REPORT_CARDS_ATTR_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.report.cards");

	public GroupExecutionHandler(CardPortletSessionBean sessionBean,
			RenderRequest request) {
		super(sessionBean, request);
	}

	@Override
	protected void preProcessCard() {
		Card templateCard = sessionBean.getActiveCard();
		CardLinkAttribute reportCards = (CardLinkAttribute) templateCard
				.getAttributeById(REPORT_CARDS_ATTR_ID);
		reportCards.addIdsLinked(reports);
	}

	protected void processParameters(RenderRequest request) {
		String reportsString = portletService.getUrlParameter(request,
				REPORTS_PARAM);
		for (String reportString : reportsString.split("_")) {
			if (reports == null) {
				reports = new ArrayList<ObjectId>();
			}
			reports.add(new ObjectId(Card.class, Long.parseLong(reportString)));
		}
	}

	public void storeCard() throws DataException {
		ProcessGroupExecution processGroupExecution = new ProcessGroupExecution();
		
		Attribute currentReportAttr = sessionBean.getActiveCard()
				.getAttributeById(CURRENT_REPORT_ATTR_ID);
		if (currentReportAttr == null || currentReportAttr.isEmpty()) {
			throw new DataException("jbr.test.attr.empty",
					new Object[] { currentReportAttr.getName() });
		}

		processGroupExecution
				.setCurrentReport(sessionBean.getActiveCard())
				.setReports(((CardLinkAttribute)sessionBean.getActiveCard().getAttributeById(REPORT_CARDS_ATTR_ID)).getIdsLinked())
				.setOnlyCopy(false);
		try{
			sessionBean.getServiceBean().doAction(processGroupExecution,ExecuteOption.ASYNC);
			wasCopied = true;
			sessionBean.setMessageWithType(sessionBean.getResourceBundle().getString("edit.page.async.change.state.success.msg"),
					PortletMessageType.EVENT);
		} catch (ServiceException e) {
			e.printStackTrace();
			throw new DataException(e);
		}
	}

	public String getStoreButtonTitle() {
		return TEXT_EXECUTE_BUTTON;
	}

	public String getCloseActionString() {
		return CardPortlet.CLOSE_CARD_ACTION;
	}
}
