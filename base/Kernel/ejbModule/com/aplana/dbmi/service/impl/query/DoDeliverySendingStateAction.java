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
package com.aplana.dbmi.service.impl.query;

import com.aplana.dbmi.action.DeliverySendingStateAction;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.service.DataException;

/**
 * Query ��� ������ ���������� � �������� �������� �/��� �������� ���
 * ��� ������ ���������� ��������� � ������� ������� DoChainAsyncDeliveryAction
 * ����������� �� DoChainAsyncDeliveryAction
 * @author ppolushkin
 */
public class DoDeliverySendingStateAction extends DoChainAsyncDeliveryAction {

	private static final long serialVersionUID = 1L;
	
	final public static ObjectId SENDING_STATE_SUCCESS = ObjectId.predefined(ReferenceValue.class, "elm.sending.status.success");
	final public static ObjectId SENDING_STATE_ERROR = ObjectId.predefined(ReferenceValue.class, "elm.sending.status.error");

	@Override
	public Object calcDependencies() throws DataException {
		return null;
	}

	@Override
	public boolean isPossibleToAdd(SmartQuery sm) throws DataException {
		return false;
	}

	@Override
	public Object processQuery() throws DataException {

		final DeliverySendingStateAction action = getAction();
		final Card card = action.getCard();

		if (logger.isDebugEnabled()) {
			logger.debug("processQuery() for card " + card.getId());
		}

		if(action.getCard() == null) {
			logger.error("Card cannot be null for action " + action);
			return null;
		}
		
		if(action.getSendingStateId() == null) {
			logger.error("SendingStateId cannot be null for action " + action);
			return null;
		}
		
		if(action.getUnservedAttr() == null) {
			logger.error("UnservedAttr cannot be null for action " + action);
			return null;
		}
		
		boolean isLockedByMe = false;
		
		try {
			// �������� ������ �� ����������� �����������
			final LinkAttribute unserved = getUnservedRecipients(card.getId(), action.getUnservedAttr());
			// �������� ������� �������� ������ ��������
			final ListAttribute sendingState = card.getAttributeById(action.getSendingStateId());
		
			if(sendingState == null) {
				logger.error("Attribute " + action.getSendingStateId().getId() + " is null within card  " + card.getId());
				return null;
			}
			
			if(!canLock(card)) {
				logger.error("Card " + card.getId() + " is locked. Query for " + getClass() + " rejected");
				return null;
			}
			
			execAction(new LockObject(card.getId()), getUser());
			isLockedByMe = true;
		
			if(unserved != null && unserved.getLinkedCount() > 0) {
				sendingState.setValue((ReferenceValue) ReferenceValue.createFromId(SENDING_STATE_ERROR));
			} else {
				logger.warn("Unserved recipients: " + unserved +
						"\n\tUnserved recipients count: " + (unserved != null ? unserved.getLinkedCount() : null) +
						"\n\tfor card " + card.getId());
				sendingState.setValue((ReferenceValue) ReferenceValue.createFromId(SENDING_STATE_SUCCESS));
			}
			
			overwrite(card, sendingState);
		} catch(DataException e) {
			logger.error("Data processing error: ", e);
		} finally {
			if (isLockedByMe) {
				try {
					execAction(new UnlockObject(card.getId()), getUser());
				} catch (Exception e) {
					logger.error("Unexpected exception while unlock card " + card.getId(), e);
				}	
			}
		}
		
		return null;
	}

}
