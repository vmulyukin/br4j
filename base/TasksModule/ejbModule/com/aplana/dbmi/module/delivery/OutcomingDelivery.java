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
package com.aplana.dbmi.module.delivery;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.*;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.UserPrincipal;
import com.aplana.dbmi.task.AbstractTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class OutcomingDelivery extends AbstractTask {

	private static final String CONFIG_FILE = CONFIG_FOLDER + "/delivery.properties";
	
	private static final long serialVersionUID = 1L;
	private static Boolean working = false;
	private static DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	
	protected final Log logger = LogFactory.getLog(getClass());
    private Properties config = null;
    
    final public static ObjectId TEMPLATE = ObjectId.predefined(Template.class, "jbr.DistributionListElement");
	final public static ObjectId TARGET_ATTR = ObjectId.predefined(CardLinkAttribute.class, "jbr.distributionItem.recipient");
	final public static ObjectId FILTER_ATTR = ObjectId.predefined(ListAttribute.class, "jbr.distributionItem.method");
	final public static ObjectId FILTER_VALUE = ObjectId.predefined(ReferenceValue.class, "jbr.distributionItem.method.delo");
	final public static ObjectId STATE_DRAFT = ObjectId.predefined(CardState.class, "draft");
	final public static ObjectId WFM_READY_FOR_SEND = ObjectId.predefined(WorkflowMove.class, "jbr.distributionItem.ready");
    
    // ���������� ����������
    private static final ObjectId ATTR_RECEIVER = ObjectId.predefined(CardLinkAttribute.class, "jbr.outcoming.receiver");
    // ���� ��������
    private static final ObjectId ATTR_DIST_LIST = ObjectId.predefined(CardLinkAttribute.class, "jbr.Distribution.DistributionList");
    // ��������� �������� ����� ��������
    private static final ObjectId ATTR_CREATE_LIST = ObjectId.predefined(CardLinkAttribute.class, "jbr.Distribution.CreateList");
    // ���������� ���������� � ���
    private static final ObjectId ATTR_RECIPIENT = ObjectId.predefined(CardLinkAttribute.class, "jbr.distributionItem.recipient");
    // ������ ��������
    private static final ObjectId ATTR_STATUS = ObjectId.predefined(ListAttribute.class, "jbr.elm.sending.status");
    // 
    private static final ObjectId VALUE_PROCESS = ObjectId.predefined(ReferenceValue.class, "elm.sending.status.processing");
    // ���� �����������
    private static final ObjectId ATTR_REG_DATE = ObjectId.predefined(DateAttribute.class, "regdate");
    // ������ ���������������
    private static final ObjectId CARD_STATE_REG = ObjectId.predefined(CardState.class, "registration");
    // ������ ���������
    private static final ObjectId CARD_TEMPLATE_OUTCOMING = ObjectId.predefined(Template.class, "jbr.outcoming");
	
	public void process(Map<?, ?> parameters) {
        synchronized (OutcomingDelivery.class) {
            if (working) {
                logger.warn("Process is already working. Skipping.");
                return;
            }
            working = true;
        }
        logger.info(getClass() + " TASK started");
        try {
        	loadProperties();
            processDelivery();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            synchronized (OutcomingDelivery.class) {
                working = false;
                logger.info(getClass() + " TASK finished");
            }
        }
    }
	
	private void processDelivery() throws DataException, ServiceException {
		Date startDate = null;
		Date endDate = null;
		
		if(config == null) {
			throw new DataException("Config file " + CONFIG_FILE + " not found");
		}
		if(!config.containsKey("startDate")) {
			throw new DataException("Key 'startDate' not found in " + CONFIG_FILE);
		}
		
		try {
			startDate = df.parse(config.getProperty("startDate"));
			if(config.containsKey("endDate")) {
				endDate = df.parse(config.getProperty("endDate"));
			} else {
				endDate = new Date();
			}
		} catch (ParseException e) {
			logger.error("Error while date parse: ", e);
			return;
		}
		
		Search search = new Search();
		search.setByAttributes(true);
		search.setColumns(CardUtils.createColumns(Card.ATTR_ID));
		final List<DataObject> templates = new ArrayList<DataObject>(1);
		templates.add(DataObject.createFromId(CARD_TEMPLATE_OUTCOMING));
		search.setTemplates(templates);
		search.setStates(Collections.singleton(CARD_STATE_REG));
		search.addDateAttribute(ATTR_REG_DATE, startDate, endDate);
		
		List<Card> list = CardUtils.getCardsList((SearchResult) serviceBean.doAction(search));
		
		if(CollectionUtils.isEmpty(list)) {
			logger.info("No matching outcoming documents");
			return;
		}
		
		AsyncDataServiceBean service = new AsyncDataServiceBean();
		service.setAddress("localhost");
		service.setUser(new UserPrincipal("__system__"));
		service.setSessionId(serviceBean.getSessionId());
		
		for(Card cardFromList : list) {
			
			Card card = null;
			
			try {	
				card = (Card) serviceBean.getById(cardFromList.getId());
				CardLinkAttribute receivers = card.getCardLinkAttributeById(ATTR_RECEIVER);
				if(receivers == null || receivers.isEmpty()) {
					logger.debug("Card " + card.getId() + "dont contain receivers");
					continue;
				}
				CardLinkAttribute createList = card.getCardLinkAttributeById(ATTR_CREATE_LIST);
				CardLinkAttribute distList = card.getCardLinkAttributeById(ATTR_DIST_LIST);
				
				List<ObjectId> listToSend = new ArrayList<ObjectId>(receivers.getIdsLinked());
				
				if(distList != null && !distList.isEmpty()) {
					search = new Search();
					search.setByCode(true);
					search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(distList.getIdsLinked()));
					search.setColumns(CardUtils.createColumns(Card.ATTR_ID, ATTR_RECIPIENT));
					
					List<Card> esrs = CardUtils.getCardsList((SearchResult) serviceBean.doAction(search));
					
					if(!CollectionUtils.isEmpty(esrs)) {
						Collections.copy(listToSend, receivers.getIdsLinked());
						for(Card esr : esrs) {
							CardLinkAttribute recipient = esr.getCardLinkAttributeById(ATTR_RECIPIENT);
							if(recipient == null) {
								logger.debug("Delivery card " + esr.getId() + "dont contain recipient");
								continue;
							}
							if(receivers.getIdsLinked().contains(recipient.getIdsLinked().get(0))) {
								listToSend.remove(recipient.getIdsLinked().get(0));
							}
							
						}
					}
				}
				
				if(listToSend != null && !listToSend.isEmpty()) {
				
					if(createList == null) {
						createList = new CardLinkAttribute();
						createList.setId(ATTR_CREATE_LIST);
					}
					createList.clear();
					createList.addIdsLinked(listToSend);
				
					ListAttribute stateAttr = (ListAttribute) card.getAttributeById(ATTR_STATUS);
					if(stateAttr == null) {
						stateAttr = new ListAttribute();
						stateAttr.setId(ATTR_STATUS);
					}
					stateAttr.clear();
					stateAttr.setValue((ReferenceValue) ReferenceValue.createFromId(VALUE_PROCESS));
					
					try {
						TimeUnit.SECONDS.sleep(5);
					} catch (InterruptedException e) {
						logger.error("Thread " + Thread.currentThread() + " interrupted.", e);
					}
				
					serviceBean.doAction(new LockObject(card));
					OverwriteCardAttributes overwrite = new OverwriteCardAttributes();
					overwrite.setCardId(card.getId());
					overwrite.setAttributes(Arrays.asList(createList, stateAttr));
					serviceBean.doAction(overwrite);
					serviceBean.doAction(new UnlockObject(card));
					
					checkProperties(card, createList.getIdsLinked());
					for(ObjectId recipient : createList.getIdsLinked()) {
						ChainAsyncDeliveryAction action = buildAction(recipient, card);
						service.doAction(action, ExecuteOption.ASYNC);
					}
					DeliverySendingStateAction finalAction = new DeliverySendingStateAction();
					finalAction.setCard(card);
					finalAction.setSendingStateId(ATTR_STATUS);
					finalAction.setUnservedAttr(ATTR_CREATE_LIST);
					service.doAction(finalAction, ExecuteOption.ASYNC);
				
				}
			
			} catch (ServiceException e) {
				logger.error("Could not execute action " + this + " for card " + card.getId(), e);
			} catch (DataException e) {
				logger.error("Could not execute action " + this + " for card " + card.getId(), e);
			}
		}
	}
	
	/**
	 * �������� ������������ ������
	 * @throws DataException
	 */
	private void checkProperties(Card card, List<ObjectId> recipients) throws DataException {
		if(card == null || CollectionUtils.isEmpty(recipients) || serviceBean == null) {
			throw new DataException("No one property of " + getClass() + " can not be null or empty");
		}
	}
	
	/**
	 * ���������� ������� ������
	 * @param recipient
	 * @return
	 */
	private ChainAsyncDeliveryAction buildAction(ObjectId recipient, Card card) {
		ChainAsyncDeliveryAction action = new ChainAsyncDeliveryAction();
		action.setCard(card);
		action.setRecipient(recipient);
		action.setTemplate(TEMPLATE);
		action.setAttach(ATTR_DIST_LIST);
		action.setTargetAttr(TARGET_ATTR);
		action.setFilterListAttr(FILTER_ATTR);
		action.setFilterValue(Collections.singletonList((ReferenceValue) ReferenceValue.createFromId(FILTER_VALUE)));
		action.setStatesForSend(Collections.singletonList(STATE_DRAFT));
		action.setWorkflowMove(WFM_READY_FOR_SEND);
		action.setUnservedAttr(ATTR_CREATE_LIST);
		return action;
	}
	
	/**
	 * Load build information from config file.
	 * @throws IOException 
	 */
	private void loadProperties() throws IOException {
		config = new Properties();
		config.load(Portal.getFactory().getConfigService().loadConfigFile(CONFIG_FILE));
	}

}
