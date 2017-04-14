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
package com.aplana.dbmi.card.delivery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.ChainAsyncDeliveryAction;
import com.aplana.dbmi.action.DeliverySendingStateAction;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import edu.emory.mathcs.backport.java.util.Collections;

public class SendDeliveryDispatcher {
	
	protected Log logger = LogFactory.getLog(getClass());
	
	final public static ObjectId TEMPLATE = ObjectId.predefined(Template.class, "jbr.DistributionListElement");
	final public static ObjectId ATTACH_ATTR = ObjectId.predefined(CardLinkAttribute.class, "jbr.Distribution.DistributionList");
	final public static ObjectId TARGET_ATTR = ObjectId.predefined(CardLinkAttribute.class, "jbr.distributionItem.recipient");
	final public static ObjectId FILTER_ATTR = ObjectId.predefined(ListAttribute.class, "jbr.distributionItem.method");
	final public static ObjectId FILTER_VALUE = ObjectId.predefined(ReferenceValue.class, "jbr.distributionItem.method.delo");
	final public static ObjectId STATE_DRAFT = ObjectId.predefined(CardState.class, "draft");
	final public static ObjectId WFM_READY_FOR_SEND = ObjectId.predefined(WorkflowMove.class, "jbr.distributionItem.ready");
	
	final public static ObjectId UNSERVED_LIST_ATTR = ObjectId.predefined(CardLinkAttribute.class, "jbr.Distribution.CreateList");
	final public static ObjectId SENDING_STATE_ATTR = ObjectId.predefined(ListAttribute.class, "jbr.elm.sending.status");
	final public static ObjectId SENDING_STATE_PROCESSING = ObjectId.predefined(ReferenceValue.class, "elm.sending.status.processing");
	
	final private AsyncDataServiceBean serviceBean;
	// ������� �������� �� ���.
	final private Card card;
	// ������ ����������� ��� ������� ����� ������� �/��� ��������� ���
	final private List<ObjectId> recipients;
	
	public SendDeliveryDispatcher(AsyncDataServiceBean serviceBean, Card card, List<ObjectId> recipients) {
		this.card = card;
		this.recipients = recipients;
		this.serviceBean = serviceBean;
	}
	
	/**
	 * ��������� ������ ������� �������� � �������� (�� �������� ���. �� �����������)
	 * ����� ������� �������� � �� ���������������� ������ ����������� ("��������� �������� ����� ��������")
	 * ��� ������� ����� ������� �/��� ��������� ���
	 * ���������� ���� ��� � ������ ��������
	 * @throws ServiceException
	 * @throws DataException
	 */
	public void firstDispatch() {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("firstDispatch() for card " + card.getId());
			}
			checkProperties();
			// �������� ������ ��� �� ����������� �����������
			LinkAttribute unserved = getUnservedRecipients(card.getId(), UNSERVED_LIST_ATTR);
			if (null == unserved) {
				logger.warn("Unserved recipients list is null for card " + card.getId());
				return;
			}
			
			for(ObjectId recipient : recipients) {
				ChainAsyncDeliveryAction action = buildAction(recipient);
				serviceBean.doAction(action, ExecuteOption.ASYNC);
			}
			DeliverySendingStateAction finalAction = new DeliverySendingStateAction();
			finalAction.setCard(card);
			finalAction.setSendingStateId(SENDING_STATE_ATTR);
			finalAction.setUnservedAttr(UNSERVED_LIST_ATTR);
			serviceBean.doAction(finalAction, ExecuteOption.ASYNC);
		} catch (ServiceException e) {
			logger.error("Could not execute action " + this + " for card " + card.getId(), e);
		} catch (DataException e) {
			logger.error("Could not execute action " + this + " for card " + card.getId(), e);
		}
	}
	
	/**
	 * ��������� ��������� ������� �������� � �������� (�� ������ � �����������)
	 * ����� ������� �������� �� �������������� ������ ����������� ("��������� �������� ����� ��������"), 
	 * ���������� � ������ ������ ������������ ��, ������� �������� � �� �� ���������� ������� ��������
	 * �������� ����� ����� ���������� ���.
	 * @throws DataException
	 * @throws ServiceException
	 */
	public void retryDispatch() throws DataException, ServiceException {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("retryDispatch() for card: " + card.getId());
			}
			checkProperties();
			writeSendingState();
			for(ObjectId recipient : recipients) {
				ChainAsyncDeliveryAction action = buildAction(recipient);
				serviceBean.doAction(action, ExecuteOption.ASYNC);
			}
			DeliverySendingStateAction finalAction = new DeliverySendingStateAction();
			finalAction.setCard(card);
			finalAction.setSendingStateId(SENDING_STATE_ATTR);
			finalAction.setUnservedAttr(UNSERVED_LIST_ATTR);
			serviceBean.doAction(finalAction, ExecuteOption.ASYNC);
		} catch (ServiceException e) {
			logger.error("Could not execute action " + this + " for card " + card.getId(), e);
		} catch (DataException e) {
			logger.error("Could not execute action " + this + " for card " + card.getId(), e);
		}
	}
	
	/**
	 * �������� ������������ ������
	 * @throws DataException
	 */
	private void checkProperties() throws DataException {
		if(card == null || CollectionUtils.isEmpty(recipients) || serviceBean == null) {
			throw new DataException("No one property of " + getClass() + " can be null or empty");
		}
	}
	
	/**
	 * ���������� ������� ������
	 * @param recipient
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private ChainAsyncDeliveryAction buildAction(ObjectId recipient) {
		ChainAsyncDeliveryAction action = new ChainAsyncDeliveryAction();
		action.setCard(this.card);
		action.setRecipient(recipient);
		action.setTemplate(TEMPLATE);
		action.setAttach(ATTACH_ATTR);
		action.setTargetAttr(TARGET_ATTR);
		action.setFilterListAttr(FILTER_ATTR);
		action.setFilterValue(Collections.singletonList(FILTER_VALUE));
		action.setStatesForSend(Collections.singletonList(STATE_DRAFT));
		action.setWorkflowMove(WFM_READY_FOR_SEND);
		action.setUnservedAttr(UNSERVED_LIST_ATTR);
		return action;
	}
	
	/**
	 * ������������� � �� ������ �������� (������� ������� �������� ���)
	 * @throws ServiceException
	 * @throws DataException
	 */
	private void writeSendingState() throws ServiceException, DataException {
		if(card == null) {
			logger.error("Tech card is null");
			return;
		}
		final ListAttribute sendingState = card.getAttributeById(SENDING_STATE_ATTR);
		if(sendingState == null) {
			logger.error("Attribute " + SENDING_STATE_ATTR + " is null for card " + card.getId());
			return;
		}
		sendingState.setValue((ReferenceValue) ReferenceValue.createFromId(SENDING_STATE_PROCESSING));
		overwrite(sendingState);
	}
	
	/**
	 * ���������� ���������
	 * @param atts
	 * @throws DataException
	 * @throws ServiceException
	 */
	private void overwrite(Attribute... atts) throws DataException, ServiceException {
		final OverwriteCardAttributes overwrite = new OverwriteCardAttributes();
		overwrite.setCardId(card.getId());
		overwrite.setAttributes(Arrays.asList(atts));
		overwrite.setInsertOnly(false);
		
		serviceBean.doAction(new LockObject(card.getId()));
		try {
			serviceBean.doAction(overwrite);
		} catch(DataException e) { 
			throw new DataException(e);
		} catch (ServiceException e) {
			throw new ServiceException(e);
		} finally {
			serviceBean.doAction(new UnlockObject(card.getId()));
		}
	}
	
	/**
	 * ���������� �������-������ ������������� ����������� ������� �������� ��
	 * (������������� ���������� - ��� ��� �������� �� ������� �/��� �� ���������� �������� ���)
	 * @param id
	 * @param attrUnserved �������-������ ������������� �����������
	 * @return
	 * @throws ServiceException 
	 */
	protected LinkAttribute getUnservedRecipients(ObjectId id, ObjectId attrUnserved) throws DataException, ServiceException {
		if(id == null || attrUnserved == null) {
			return null;
		}
		Search search = new Search();
		search.setByCode(true);
		search.setWords(String.valueOf(id.getId()));
		List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(attrUnserved);
		columns.add(col);
		search.setColumns(columns);

		final SearchResult result = serviceBean.doAction(search);
		final List<Card> list = result != null ? result.getCards() : null;
		if(!CollectionUtils.isEmpty(list)) {
			return list.get(0).getAttributeById(attrUnserved);
		}
		return null;
	}
}
