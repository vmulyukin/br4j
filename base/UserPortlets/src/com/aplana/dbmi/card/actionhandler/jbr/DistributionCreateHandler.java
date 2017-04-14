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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.card.Parametrized;
import com.aplana.dbmi.card.actionhandler.CardPortletAttributeEditorActionHandler;
import com.aplana.dbmi.card.delivery.SendDeliveryDispatcher;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

public class DistributionCreateHandler extends CardPortletAttributeEditorActionHandler implements Parametrized
{
	protected static final String PARAM_SEND_STATES_ALLOWED = "sendStatesAllowed";
	
	protected Collection<ObjectId> sendStatesAllowed;
	
	protected void process(Attribute attr, List cardIds, ActionRequest request,
			ActionResponse response) throws DataException {
		try {
			// ��������� ����������� �������� ��� ��� ��������� ����������� �� �������� ���������� �������� ����� ��������
			final SendDeliveryDispatcher sdd = new SendDeliveryDispatcher(serviceBean, getCardPortletSessionBean().getActiveCard(), cardIds);
			sdd.retryDispatch();
			getCardPortletSessionBean().setMessageWithType(getCardPortletSessionBean().getResourceBundle()
					.getString("dist.async.create.success.msg"),
					PortletMessageType.EVENT);
		} catch (ServiceException e) {
			String msg = MessageFormat.format(getCardPortletSessionBean().getResourceBundle()
					.getString("dist.async.create.error.msg"), e.getMessage());
			getCardPortletSessionBean().setMessageWithType( msg, PortletMessageType.ERROR);
			throw new DataException(e);
		} catch (DataException e) {
			String msg = MessageFormat.format(getCardPortletSessionBean().getResourceBundle()
					.getString("dist.async.create.error.msg"), e.getMessage());
			getCardPortletSessionBean().setMessageWithType( msg, PortletMessageType.ERROR);
			throw e;
		}
	}
	
	@Override
	public boolean isApplicableForUser() {
		final Card card = getCardPortletSessionBean().getActiveCard();
		final ListAttribute sendingState = card.getAttributeById(SendDeliveryDispatcher.SENDING_STATE_ATTR);
		boolean isAllowedSendState = (sendStatesAllowed == null || sendStatesAllowed.contains(sendingState.getValue().getId()));
		
		if(!isAllowedSendState)
			return false;
		
		return super.isApplicableForUser();
	}
	
	@Override
	public void setParameter(String name, String value) {
		if(PARAM_SEND_STATES_ALLOWED.equalsIgnoreCase(name)){
			sendStatesAllowed = ObjectIdUtils.commaDelimitedStringToIds(value, ReferenceValue.class);
		}
	}
}
