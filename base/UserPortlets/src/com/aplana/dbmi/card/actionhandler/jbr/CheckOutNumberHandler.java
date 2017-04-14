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
package com.aplana.dbmi.card.actionhandler.jbr;

import java.text.MessageFormat;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.card.actionhandler.CardPortletActionHandler;
import com.aplana.dbmi.jbr.action.CheckCardNumber;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataException;

public class CheckOutNumberHandler extends CardPortletActionHandler {
	/**
	 * ID ����������� ��������� "����� ����������" � "���� ����������".
  	 */
	private static  final ObjectId ATTRID_NUMOUT = // 'JBR_REGD_NUMOUT'
		ObjectId.predefined(StringAttribute.class, "jbr.incoming.outnumber"); 
	private static  final ObjectId ATTRID_DATEOUT = // 'JBR_REGD_DATEOUT' 
		ObjectId.predefined(DateAttribute.class, "jbr.incoming.outdate");

	public void process(List cardIds, ActionRequest request,
			ActionResponse response) throws DataException {
		CardPortletSessionBean sessionBean = getSessionBean();
		Card c = sessionBean.getActiveCard();
		CheckCardNumber action = new CheckCardNumber();
		action.setCardId(c.getId());
		StringAttribute sa = (StringAttribute)c.getAttributeById(ATTRID_NUMOUT);
		action.setNumber(sa.getStringValue());
		DateAttribute da = (DateAttribute)c.getAttributeById(ATTRID_DATEOUT);
		action.setDate(da.getValue());
		try {
			String res = (String)sessionBean.getServiceBean().doAction(action);
			ContextProvider cp = ContextProvider.getContext();
			if (res == null) {
				sessionBean.setMessage(cp.getLocaleMessage("jbr.card.check.incoming.success"));
			} else {
				res = MessageFormat.format(
					cp.getLocaleMessage("jbr.card.check.incoming.hasthesame_1"),
					new Object[] {res}
				);
				sessionBean.setMessage(res);
			}
		} catch (Exception e) {
			logger.error("Failed to check number", e);
			sessionBean.setMessageWithType(e.getLocalizedMessage(), PortletMessageType.ERROR);
		}
	}
}
