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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChainAsyncDeliveryAction;
import com.aplana.dbmi.action.DeliverySendingStateAction;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.DataServiceBean;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.service.impl.locks.LockManagement;
import com.aplana.dbmi.service.impl.locks.LockManagement.OperationResult;

/**
 * Query ��� �������� �/��� �������� �������� ��� ��� ������ ����������
 * ��������� SmartQuery ��� ����������� ��������� ��������� �������� ������� � �������
 * @author ppolushkin
 */
public class DoChainAsyncDeliveryAction extends ActionQueryBase implements WriteQuery, SmartQuery {

	private static final long serialVersionUID = 1L;

	@Override
	public Object calcDependencies() throws DataException {
		return null;
	}
	
	/**
	 * ������ ������������ �� ������� Query ������� ������ � ��� ������������ ������� ����� �� Query
	 * @param sm ������� Query
	 * this ������ � ������� (��������) Query � ������ �������
	 * @return true - ������������, false - �� ������������
	 */
	@Override
	public boolean isPossibleToAdd(SmartQuery sm) throws DataException {
		
		try {
		
			ChainAsyncDeliveryAction action = getAction();
			
			ActionQueryBase actionQueryBase = (ActionQueryBase) sm;
			Action actionQuery = actionQueryBase.getAction();
			// ������������ Query ������� ������
			if(DoChainAsyncDeliveryAction.class.equals(sm.getClass())
					&& getUser() != null && actionQueryBase.getUser() != null
					&& getUser().getPerson().equals(actionQueryBase.getUser().getPerson())
					&& action.getCard() != null && action.getCard().getId() != null
					&& ((ChainAsyncDeliveryAction) actionQuery).getCard() != null 
					&& action.getCard().getId().equals(((ChainAsyncDeliveryAction) actionQuery).getCard().getId())) {
				return true;
			}
			// ����� ������������ Query ��� ������ ����������
			if(DoDeliverySendingStateAction.class.equals(sm.getClass()) 
					&& getUser() != null && actionQueryBase.getUser() != null
					&& getUser().getPerson().equals(actionQueryBase.getUser().getPerson())
					&& action.getCard() != null && action.getCard().getId() != null
					&& ((DeliverySendingStateAction) actionQuery).getCard() != null 
					&& action.getCard().getId().equals(((DeliverySendingStateAction) actionQuery).getCard().getId())) {
				return true;
			}
		} catch(Exception e) {
			logger.error("Could not bind query to chain" + this);
			return false;
		}
		return false;
	}

	@Override
	public Object processQuery() throws DataException {

		final ChainAsyncDeliveryAction action = getAction();
		final Card card = action.getCard();

		if (logger.isDebugEnabled()) {
			logger.debug("processQuery() for card " + card.getId());
		}
		// �������� ���������� ������� � ������� Query, ����� ��������� ������ � ���� ������� ��������������
		// ��� �������� ������ Query ����� �� ������ ��������������, ���� ������ ���������� ������� ������
		if(getQueryContainer().getPrev() == null) {
			try {
				TimeUnit.MILLISECONDS.sleep(1500);
			} catch (InterruptedException e) {
				logger.warn("Interrupted exception: ", e);
			}
		}

		boolean isLockedByMe = false;
		
		List<Card> deliveryInfo = null;
		ObjectId newDelivery = null;
		
		try {
			// �������� ������ ��� �� ����������� �����������
			LinkAttribute unserved = getUnservedRecipients(card.getId(), action.getUnservedAttr());
		
			if(!checkParameters(action) || unserved == null) {
				return null;
			}
		
			if(!unserved.getIdsLinked().contains(action.getRecipient())) {
				logger.error("Current recipient not in list unserved recipients");
				return null;
			}
		
			// ��������� ����� �� �� ������������� �� ������� ������ (����� ���� �� �� ������������ ��� ��� ������������ ������� �������������)
			// ���� �� ����� ������������� �� - ������� �� Query �� ������ ������
			if(!canLock(card)) {
				logger.error("Card " + card.getId() + " is locked. Query for recipient " + action.getRecipient() + " rejected");
				return null;
			}
			
			execAction(new LockObject(card.getId()), getUser());
			isLockedByMe = true;
			
			// �������� �������� ��� � ������� ����������� � �������� (���� ����������)
			deliveryInfo = selectDeliveryCard(card.getId(), action.getAttach(), action.getTargetAttr(), action.getRecipient());
			// ���� �� ���������� ��� � ������� ����������� - ������� ����� ������� � ���������� �� ��������
			if(CollectionUtils.isEmpty(deliveryInfo)) {
				newDelivery = createDelivery(action, card);
				// ��� �������� ������� ���, ���� ������ ���������� �� ������ ��������, 
				// �� ��� ���������� ������������, �� ������ �� �������������� �������� ����� ��������� � ������.
				try {
					sendDelivery(action, newDelivery);
				} catch(DataException e) {
					unlinkDelivery(card, deliveryInfo, action.getAttach(), newDelivery);
					throw new DataException("DataException while delivery sending ", e);
				} catch(Exception e) {
					unlinkDelivery(card, deliveryInfo, action.getAttach(), newDelivery);
					throw new RuntimeException("Exception while delivery sending ", e);
				}
			}
			// ���� ��� � ������� ����������� ����������, �� ��������� � ��������� - ���������� �� ��������
			else {
				boolean needSend = false;
				ObjectId deliveryToSend = null;
				for(Card c : deliveryInfo) {
					if(action.getStatesForSend().contains(c.getState())) {
						needSend = true;
						deliveryToSend = c.getId();
						break;
					}
				}
			
				if(needSend) {
					sendDelivery(action, deliveryToSend);
				}
			}
			// ����� ���� ����������� � ��� (���� ���� � ��� ������ �� �������� ������), 
			// ������� �� ������ ������������� ����������� �������� � ������������� ���� ������� � ��
			unserved.removeLinkedId(action.getRecipient());
			overwrite(card, unserved);
		} catch(DataException e) {
			logger.error("Data processing error: ", e);
		} catch(Exception e) {
			logger.error("Unexpected exception: ", e);
		} finally {
			// ������������ ��������, ���� �����������
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
	
	/**
	 * � ������ ����� ������� � ������ ���������� �������� ����� �������� ��� �� �� � ������
	 * ����� � �� ����� �������� ������ �� �������������� ��������
	 * @param card
	 * @param deliveryInfo
	 * @param attachAttrId
	 * @param deliveryId
	 */
	private void unlinkDelivery(Card card, List<Card> deliveryInfo, ObjectId attachAttrId, ObjectId deliveryId) {
		if(CollectionUtils.isEmpty(deliveryInfo) && deliveryId != null) {
			final LinkAttribute attachAttr = card.getAttributeById(attachAttrId);
			attachAttr.removeLinkedId(deliveryId);
		}
	}
	
	/**
	 * ����������� ������� ���������� Query (�� ������)
	 * @param action
	 * @return
	 */
	private boolean checkParameters(ChainAsyncDeliveryAction action) {
		if(action.getUnservedAttr() == null) {
			logger.error("No one recipient to delivery");
			return false;
		}
		
		if(action.getRecipient() == null) {
			logger.error("Recipient cannot be null for action " + action);
			return false;
		}
		
		if(action.getCard() == null) {
			logger.error("Card cannot be null for action " + action);
			return false;
		}
		
		if(action.getTargetAttr() == null) {
			logger.error("TargetAttr cannot be null for action " + action);
			return false;
		}
		
		if(action.getAttach() == null) {
			logger.error("Attach cannot be null for action " + action);
			return false;
		}
		
		if(action.getTemplate() == null) {
			logger.error("Template cannot be null for action " + action);
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * �������� �� �������� �������� ���
	 * @param action
	 * @param delivery id �������� ���
	 * @throws DataException
	 */
	private void sendDelivery(ChainAsyncDeliveryAction action, ObjectId delivery) throws DataException {
		//������� ��� � ����� � ��������
		final ObjectQueryBase getQuery = getQueryFactory().getFetchQuery(Card.class);
		getQuery.setId(delivery);
		getQuery.setSessionId(getSessionId());
		Card cardDelivery = getDatabase().executeQuery(getSystemUser(), getQuery);
		
		// �������� �������, ����� �� ���������� ��� �� ��������
		if(action.getFilterListAttr() != null && action.getFilterValue() != null) {
			final ListAttribute listAttr = cardDelivery.getAttributeById(action.getFilterListAttr());
			if(action.getFilterValue().equals(listAttr.getValue())) {
				logger.info("Delivery " + cardDelivery.getId() + " have unnecessary send method " + action.getFilterValue() + ", sending cancelled");
				return;
			}
		}
		
		//��������������� ����� �������
		final LockObject lock = new LockObject(cardDelivery);
		execAction(lock);
		
		final ObjectQueryBase fetchQuery = getQueryFactory().getFetchQuery(WorkflowMove.class);
		fetchQuery.setId(action.getWorkflowMove());
		WorkflowMove flow = getDatabase().executeQuery(getSystemUser(), fetchQuery);
		
		try {
			ChangeState changeState = new ChangeState();
			changeState.setCard(cardDelivery);
			changeState.setWorkflowMove(flow);
			execAction(changeState);
		} finally {
			final UnlockObject unlock = new UnlockObject(cardDelivery);
			execAction(unlock);
		}
	}
	
	/**
	 * �������� �������� ���
	 * @param action
	 * @param card ��
	 * @return id ����� ��������
	 * @throws DataException
	 */
	private ObjectId createDelivery(ChainAsyncDeliveryAction action, Card card) throws DataException {
		final LinkAttribute attachAttr = card.getAttributeById(action.getAttach());
		final CreateCard create = new CreateCard(action.getTemplate());
		final Card child = (Card) execAction(create, getSystemUser());
		final LinkAttribute targetAttr = child.getAttributeById(action.getTargetAttr());
		
		if (action.getRecipient() != null && targetAttr != null) {
			targetAttr.addSingleLinkedId(action.getRecipient());
		}
		
		// ������ ���������� �������� - �������� ����� ������� ������������� ...
		final SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(child);
		saveQuery.setObject(child);
		final ObjectId id = (ObjectId) getDatabase().executeQuery(getSystemUser(), saveQuery);
		logger.info("new child card created: id=" + id+ " by template "+ action.getTemplate() + " in basedoc " + card.getId());

		// ������������� �������� ...
		execAction(new UnlockObject(id), getSystemUser());

		logger.debug("new child card unlocked successfully, card id = " + id);
		
		attachAttr.addLinkedId(id);
		
		// ������������� ������ �� ��� � ��
		overwrite(card, attachAttr);
		
		return id;
	}
	
	/**
	 * ���������� ��������� � ��������
	 * @param card ��������
	 * @param attrs �������� ��� ���������� (����� ���-��)
	 */
	protected void overwrite(Card card, Attribute... attrs) throws DataException {
		final OverwriteCardAttributes overwrite = new OverwriteCardAttributes();
		overwrite.setCardId(card.getId());
		overwrite.setAttributes(Arrays.asList(attrs));
		overwrite.setInsertOnly(false);
		execAction(new LockObject(card.getId()), getUser());
		try {
			execAction(overwrite, getUser());
		} finally {
			execAction(new UnlockObject(card.getId()), getUser());
		}
	}
	
	/**
	 * ��������� �����
	 * @param action
	 * @param user ������������ �� ����� ����� ���������
	 * @return
	 * @throws DataException
	 */
	protected <T> T execAction(Action action, UserData user) throws DataException {
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		return getDatabase().executeQuery(user, query);
	}
	
	/**
	 * ��������� ����� �� ����� �������
	 * @param action
	 * @return
	 * @throws DataException
	 */
	protected <T> T execAction(Action action) throws DataException {
		return execAction(action, getSystemUser());
	}
	
	/**
	 * ����� ������������ �������� ��� ��� �������� ���������� � ������� �������� ��
	 * @param id �������� ��
	 * @param attrAttach ������� � �� � �������� ������ ���� ���������� ��� (��������)
	 * @param attrTarget ������� � ��� ��� ���������� ���������� (��������)
	 * @param recipient id �������� ����������
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Card> selectDeliveryCard(ObjectId id, ObjectId attrAttach, ObjectId attrTarget, ObjectId recipient) {
		
		if(id == null || attrAttach == null || attrTarget == null) {
			logger.error("No one of delivery's parameters can not be null");
			return null;
		}
		
		StringBuilder stringQuery = new StringBuilder();
		stringQuery.append("select c1.card_id, c1.status_id from card c \n")
		.append("join attribute_value av on c.card_id = av.card_id and av.attribute_code = ''{1}'' \n")
		.append("join card c1 on c1.card_id = av.number_value \n")
		.append("join attribute_value av1 on av.number_value = av1.card_id and av1.attribute_code = ''{2}'' \n")
		.append("where c.card_id = {0} and av1.number_value = {3}");
		
		try {
			return getJdbcTemplate().query(MessageFormat.format(stringQuery.toString(),
					String.valueOf(id.getId()),
					String.valueOf(attrAttach.getId()),
					String.valueOf(attrTarget.getId()),
					String.valueOf(recipient.getId())),
				new RowMapper(){
					@Override
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						Card card = new Card();
						card.setId(rs.getLong(1));
						card.setState(new ObjectId(CardState.class, rs.getLong(2)));
						return card;
					}
				
				}
			);
		} catch(DataAccessException e) {
			logger.error("Error while select operation", e);
			return null;
		}
	}
	
	/**
	 * ���������� �������-������ ������������� ����������� ������� �������� ��
	 * (������������� ���������� - ��� ��� �������� �� ������� �/��� �� ���������� �������� ���)
	 * @param id
	 * @param attrUnserved �������-������ ������������� �����������
	 * @return
	 */
	protected LinkAttribute getUnservedRecipients(ObjectId id, ObjectId attrUnserved) throws DataException {
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

		final SearchResult result = execAction(search);
		final List<Card> list = result != null ? result.getCards() : null;
		if(!CollectionUtils.isEmpty(list)) {
			return list.get(0).getAttributeById(attrUnserved);
		}
		return null;
	}
	
	/**
	 * �������� ����� �� ������� ������������ ������������� ��������
	 * (����� ������������� ���� �� �� ������������ ��� ��� ������������ ������� �������������)
	 * @param card
	 * @return
	 */
	protected boolean canLock(Card card) {
		LockManagement storage = (LockManagement) getBeanFactory().getBean(DataServiceBean.LOCK_MANAGEMENT_BEAN);
		return OperationResult.SUCCESS.equals(storage.canLock(card.getId(), getUser().getPerson(), getSessionId()));
	}

}
