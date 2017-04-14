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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.BatchAsyncExecution;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.actionhandler.descriptor.ActionsDescriptor;
import com.aplana.dbmi.card.ActionsSupportingAttributeEditor;
import com.aplana.dbmi.card.AttributeEditor;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.card.HierarchicalCardLinkAttributeEditor;
import com.aplana.dbmi.card.HierarchicalCardLinkAttributeViewer;
import com.aplana.dbmi.card.actionhandler.AddLinkedCardActionHandler;
import com.aplana.dbmi.card.hierarchy.Messages;
import com.aplana.dbmi.card.hierarchy.descriptor.HierarchyDescriptor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.LocalizedString;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import static com.aplana.dbmi.card.CardPortletCardInfo.CustomStoreHandler.*;

/**
 * ����� ��� �������� ������ �������� �� �������
 *
 * @author dstarostin
 *
 */

public class AddTemplateGeneratedCardsActionHandler extends AddLinkedCardActionHandler {

	/**
	 * ������ �� �������� ����� ������� �������� ��������
	 */
	public static final String PARAM_TARGET_TEMPLATE = "target_template";
	/**
	 * ����� �� ��������� ��� ������ ������� �������� �����������
	 */
	public static final String PARAM_MAPPING_PACKAGE = "mapping_package";
	/**
	 * ������ ����������� ����� ��������� �����������.
	 * ������: <��� ���������>(<��������� ���������>):<��� ������ ��������>:<��� ��������>
	 * ��� ������ ��������� -- ������ ��� ������ ���������. ���� ����� ��������� � ������ ��������� �
	 * ��������� {@link #PARAM_MAPPING_PACKAGE}, ����� ������� ����������� ��� ������.
	 * ��������� ��������� -- ������ ��������� ����������, ���������� ��������, �������� ���� �� ������.
	 * 		������ ��������� � ��������� ����������� � ������ {@link MappingProcedure#init(Card, String[])}
	 * ��� ������ �������� -- ������ ��� ������ ��������, �� ������� ����������� �����������.
	 * ��� �������� -- ��� �������� �� ������� ����������� �����������.
	 * (2012/03/16, YNikitin) ����� ";" ����� ����������� ��������� ��������
	 */
	public static final String PARAM_MAPPING = "mapping";
	public static final String PARAM_SPECIFIC_TARGET_TEMPLATES = "specific_target_templates";

	/**
	 * ������� �������� ��� �������� �������� ��� ���� ��������� ���������������:
	 * true - ������� ��� ������� �������� ������� �������� � ����������� � ����������� �������� � �������������� ��������, 
	 * 			����� ��� ������� �� ��������� ��������� ����������� ��� �������� �� ��� ��������� ��������� ��� ������ ��������.
	 * false - � ���� ������ �������� ��������� �� ������ �������� �� ���� �������� ���������� (�������� �� �������, �������� �� ������� � �.�.), 
	 * ��� ���� ���������� ����������� �������� ����� ��������� � ����������� �������� � ����������� �������     
	 */
	public static final String PARAM_MAPPING_SPLIT = "mapping_split";
	
	/**
	 * ���������� ����������� ����� �������� � ��������� ��� ��������
	 * �� ��������� false - � ���������
	 * ��������������
	 */
	public static final String PARAM_REVERSE = "reverse";
	
	/**
	 * ���������� � ������ �������� ����� �������� ����������� ������������ �������� �� ��������
	 * ���� reverse=true, �� ������������, ����� �� ����� �������� ������
	 */
	public static final String PARAM_REVERSE_ATTRS = "reverseAttrs";
	
	/**
	 * ���������� ����� �������� ����� ��������� ������� ��� ���������� ���������� ����� ����� ��������
	 */
	public static final String PARAM_AVA_WORKFLOW = "availableWorkflow";
	
	/**
	 * ���������� �������� �������� �������� �� ������ "�������" (������� ��� ������� ������������� ��� ������ ����� ������ �� ������������� ��������)
	 */
	public static final String PARAM_CLOSE_HANDLER_POLICY = "closeHandlerPolicy";
	
	// ������ ��� ���������� �������� ���������� PARAM_MAPPING
	private Collection<String>
		strMappings = new ArrayList<String>();
	// �������� PARAM_MAPPING_PACKAGE
	private String mappingPackage;
	// �������� PARAM_TARGET_TEMPLATE
	private ObjectId targetTemplate;
	private Map<ObjectId, ObjectId> specificTargetTemplates = new HashMap<ObjectId, ObjectId>();

	// ��������� ��� ���������� �������� ������ ��������������� ��������������� ��������
	private Collection<ObjectId> storedCardIds;
	// ����� ���������� �� ��������� ��������
	private GenerateCardsHandler storehandler;
	// ����� ���������� �� ������� ��������������� �������� � ������ ������
	private ChangeStateGeneratedCardsHandler changeStateHandler;

	private boolean mappingSplit = false;	// �� ��������� false, �.�. �������� ��������� ��� ���� ��������� ����������
	
	private boolean reverse = false;	// �� ��������� false, �.�. ����� �������� �������� � ��������� ������������
	
	private List<ObjectId> reverseAttrs;
	
	private ObjectId availableWorkflow;
	
	// ���� ������������� ���� ��������� ������ ������� ��������, ���� ������������ (� ����������� �� �������� reverse)
	// �������� �� NewCardLinkItemCloseHandler
	private List<ObjectId> cardLinkIds;
	
	// ��������� ��������� �������� - ���������. ������ �� �������� � �������� ��������� ��� �������� ����������.
	private Map<Card, List<Attribute>> tempCardsAttrs = new LinkedHashMap<Card, List<Attribute>>();
	
	// ��������� ��������� ��������. ����� ����� ������� ���������, �� ��� �� ����������� ��������, ����� ������ ��� �� ���������/��������� �� ��
	private List<Card> tempCards = new ArrayList<Card>();
	
	// �������� �������� �������� �� ������ "�������" - �� ��������� ������� ��� ������� �������������
	private CloseHandlerPolicy closeHandlerPolicy = CloseHandlerPolicy.DIRECT;
	
	private static Set<ObjectId> hardStates = new HashSet<ObjectId>();
	static {
		hardStates.add(ObjectId.predefined(CardState.class, "registration"));
		hardStates.add(ObjectId.predefined(CardState.class, "consideration"));
		hardStates.add(ObjectId.predefined(CardState.class, "draft"));
	}
	
	/**
	 * ���������� �������� �������� ��������������
	 * ��������� � ������������ �������� ������ �� ��������������� ��������
	 *
	 */
	protected class NewCardLinkItemCloseHandler implements CardPortletCardInfo.CloseHandler {
		
		public NewCardLinkItemCloseHandler() {}

		public void afterClose(CardPortletCardInfo closedCardInfo, CardPortletCardInfo previousCardInfo) throws DataException, ServiceException {
			setLinks(cardLinkIds, closedCardInfo, previousCardInfo);
		}
	}

	// ������ ������ ������ ��������� ����������� "<������1>(<������2>)->(<������3>)"
	private static final Pattern pattern = Pattern.compile("(.+)\\((.*)\\)->\\((.*)\\)");
	public static final String STORE_BUTTON_TITLE = "edit.page.generate.btn";
	public static final String CHAGE_STATE_BUTTON_TITLE = "tool.change.status";
	public static final String CHAGE_STATE_SUCCESS = "changeStateSuccess";
	/**
	 * ����� ��� ��������� �� ������� � ���������� ������ ��������
	 *
	 * @author dstarostin
	 *
	 */
	public class GenerateCardsHandler implements CardPortletCardInfo.CustomStoreHandler {
		private Log logger = LogFactory.getLog(getClass());

		private CardPortletSessionBean session;
		// ����� "��������� ����������� -> ������ ID ��������� ������� ��� ��������"
		private Map<MappingProcedure, List<ObjectId>> mappings;
		private ObjectId targetTemplateId;
		
		public GenerateCardsHandler(CardPortletSessionBean session, ObjectId targetTemplateId) {
			this.session = session;
			this.targetTemplateId = targetTemplateId;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void storeCard() throws DataException {
			try {
				logger.info("Card generation started");

				// �������� ��������-������
				Card templateCard = session.getActiveCard();
				// �������� HashMap �� LinkedHashMap ����� �������� �������������� � ��� �������, � ����� ��� ������� � xml-���������.
				mappings = new LinkedHashMap<MappingProcedure, List<ObjectId>>(5);
				for (String mapping: strMappings) {
					// � ����� ������ � �������������� ������� MappingProcedure
					createMapping(mapping);
				}
				// ������ ��������� ������� ��������
				Card targetTempCard = (Card) serviceBean.doAction(new CreateCard(targetTemplateId));

				// �������� �������� �� ������ � ������������ ID -- ����������� ��������� ������� ��������
				for (AttributeBlock templateBlock: templateCard.<AttributeBlock>getAttributes()) {
					// �� �������� ���� ����� �������������
					if (templateBlock.getId().equals(AttributeBlock.ID_COMMON))
						continue;
					AttributeBlock targetBlock = targetTempCard.getAttributeBlockById(templateBlock.getId());
					if (targetBlock != null) {
						targetBlock.setAttributes(templateBlock.getAttributes());
					}
				}
				logger.info("Target card prototype is ready");

				// � ����� ��������� ����������� � ��������� ��������
				storedCardIds = new ArrayList<ObjectId>();
				// ���� ������� �������� ��������� ��� ������� ��������, �� ��������� ������
				if (mappingSplit){
					// ���� ������ ��� ��� �� ���������� ������ �������, ������ �� ������ �������� ����� ��������� �������� 
					boolean first = true;
					// �������� ����������� (����� ������ ��������������� �� ���� ��������� ������� �����������, ���� �� ������� �� ���� ������������)
					for (Map.Entry<MappingProcedure, List<ObjectId>> entry: mappings.entrySet()) {
						while (true){
							// ��������� ���
							List<Attribute> attrs = new ArrayList<Attribute>(entry.getValue().size());
							for (ObjectId attrId: entry.getValue()) {
									Attribute a = targetTempCard.getAttributeById(attrId);
								if (a == null) {
									MappingSystemException e = new MappingSystemException("Attribute not found in card " + targetTempCard.getId());
									logger.error(e.getMessage());
									throw e;
								}
								attrs.add(a);
							}
							if (!entry.getKey().execute(attrs))
								// ���� ����������� ������� false -- ������������
								break;

							if(first) {
								// ��������� �������� � ����������� �������������
								ObjectId newId = serviceBean.saveObject(targetTempCard);
								serviceBean.doAction(new UnlockObject(newId));
								storedCardIds.add(newId);
								Card newCard = (Card) serviceBean.getById(newId);
								tempCardsAttrs.put(newCard, new ArrayList<Attribute>());
								tempCards.add(newCard);
								// ������� ID: �������� ������ � ���������� ���������� �����������
								targetTempCard.clearId();
								// ������� ����������� ��������
								for (Attribute attr: attrs) {
									attr.clear();
								}
							} else {
								for (ObjectId attrId: entry.getValue()) {
									Attribute a = targetTempCard.getAttributeById(attrId);
									for(Map.Entry<Card, List<Attribute>> cEntry : tempCardsAttrs.entrySet()) {
										Attribute at = cEntry.getKey().getAttributeById(attrId);
										AttributeBlock block = getBlockByAttr(cEntry.getKey(), at);
										if(block != null) {
											List coll = (List) block.getAttributes();
											int index = coll.indexOf(at);
											coll.remove(index);
											coll.add(index, a);
											cEntry.getValue().add(a);
										} else {
											if(logger.isWarnEnabled())
												logger.warn("Block for attribute " + at != null ? at.getId() : null + " is not found");
										}
									}
								}
							}
						}
						// ����� ������� ������� ������������� false, ����� ������� ����� �������� ������ �� ���������
						first = false;
					}
					for(Map.Entry<Card, List<Attribute>> cEntry : tempCardsAttrs.entrySet()) {
						//OverwriteCardAttributes action = new OverwriteCardAttributes();
						final Card card = cEntry.getKey();
						//action.setCardId(cardId);
						//action.setAttributes(cEntry.getValue());
						serviceBean.doAction(new LockObject(card.getId()));
						serviceBean.saveObject(card, ExecuteOption.SYNC);
						serviceBean.doAction(new UnlockObject(card.getId()));
						if(logger.isInfoEnabled())
							logger.info("Card " + card.getId() + " saved successfully");
					}
				} else {// � ���� ��������� ��������� ��� ���� ���������, �� ������ ������
loop2:				while (true){ 
						List<Attribute> allAttrs = new ArrayList<Attribute>();
						for (Map.Entry<MappingProcedure,List<ObjectId>> entry: mappings.entrySet()) {
							// ��������� ���
							List<Attribute> attrs = new ArrayList<Attribute>(entry.getValue().size());
							for (ObjectId attrId: entry.getValue()) {
								Attribute a = targetTempCard.getAttributeById(attrId);
								if (a == null) {
									MappingSystemException e = new MappingSystemException("Attribute not found in card " + targetTempCard.getId());
									logger.error(e.getMessage());
									throw e;
								}
								attrs.add(a);
							}
							allAttrs.addAll(attrs);
						// ��������� �����������
						if (!entry.getKey().execute(attrs))
							// ���� ����������� ������� false -- ������������
								break loop2;
					}
					// ��������� �������� � ����������� �������������
					ObjectId newId = serviceBean.saveObject(targetTempCard, ExecuteOption.SYNC);
					serviceBean.doAction(new UnlockObject(newId));
					storedCardIds.add(newId);
					if(logger.isInfoEnabled())
						logger.info(newId + " is ready");
					// ������� ID: �������� ������ � ���������� ���������� �����������
						targetTempCard.clearId();
						// ������� ����������� ��������
						for (Attribute attr: allAttrs) {
							attr.clear();
						}
					}
				}
				if(logger.isInfoEnabled())
					logger.info("Cards generated: " + storedCardIds.size());
			} catch (MappingUserException e) {
				String message = getMessage(e.getMessage());
				if (message == null) {
					throw new DataException("Can't find message for exception " + e.getMessage(), e);
				} else {
					throw new DataException(e.getFormattedMessage(message));
				}
			} catch (MappingSystemException e) {
				throw new DataException(e.getMessage(), e);
			} catch (ServiceException e) {
				throw new DataException(e.getMessage(), e);
			} catch (DataException e) {
				throw new DataException(e.getMessage(), e);
			}
		}
		
		/**
		 * �������� ���� ��������, � ������� ���������� ������ �������
		 * @param c ��������
		 * @param a �������
		 * @return ���� ���� ������, ����� null
		 */
		private AttributeBlock getBlockByAttr(Card c, Attribute a) {
			if(a == null || c == null)
				return null;
			for (AttributeBlock templateBlock: c.<AttributeBlock>getAttributes()) {
				AttributeBlock targetBlock = c.getAttributeBlockById(templateBlock.getId());
				if (targetBlock != null) {
					for (Attribute attr: (Collection<Attribute>)targetBlock.getAttributes()) {
						if(attr.getId().equals(a.getId()))
							return targetBlock;
					}
				}
			}
			return null;
		}

		private void createMapping(String expr)
							throws MappingUserException, MappingSystemException {
			try {
				Matcher match = pattern.matcher(expr);
				if (!match.matches())
					throw new RuntimeException("Format of parameter \"" + PARAM_MAPPING + "\": \"<class name>(<comma separated arguments>)->(<comma separated attributes>)\"");
				//int grpCnt = match.groupCount();
				MappingProcedure mapfunc = getClassFromParam(match.group(1)).newInstance();
				mapfunc.init(session, match.group(2).split(","));
				String[] attrStr = match.group(3).split(",");
				List<ObjectId> attrId = new ArrayList<ObjectId>(attrStr.length);
				for (String attr: attrStr) {
					attrId.add(MappingUtils.stringToAttrId(attr));
				}
				mappings.put(mapfunc, attrId);
			} catch (InstantiationError e) {
				throw new RuntimeException(e);
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		private Class<MappingProcedure> getClassFromParam(String param)
								throws MappingUserException, MappingSystemException {
			try {
				if (param.contains(".")) {
					return (Class<MappingProcedure>)Class.forName(param);
				} else if (mappingPackage == null) {
					throw new MappingSystemException("To use short name of MappingProcedure class \"" + param +
							"\" you shoud declare a \"" + PARAM_MAPPING_PACKAGE + "\" parameter");
				} else {
					return (Class<MappingProcedure>)Class.forName(mappingPackage + "." + param);
				}
			} catch (ClassNotFoundException e) {
				throw new MappingSystemException(e.getMessage(), e);
			}
		}

		public String getStoreButtonTitle() {
			return STORE_BUTTON_TITLE;
		}

		public String getCloseActionString() {
			if(CloseHandlerPolicy.CONFIRM.equals(closeHandlerPolicy)) {
				return CardPortlet.CUSTOM_CLOSE_CARD_ACTION;
			} else {
				return CardPortlet.CLOSE_EDIT_MODE_ANYWAY_ACTION;
			}
		}
	}
	
	public class ChangeStateGeneratedCardsHandler implements CardPortletCardInfo.CustomChangeStateHandler {
		private Log logger = LogFactory.getLog(getClass());
		private WorkflowMove flow;
		
		private CardPortletSessionBean session;

		public ChangeStateGeneratedCardsHandler(CardPortletSessionBean session, WorkflowMove flow) {
			this.session = session;
			this.flow = flow;
		}

		public void changeState() throws DataException, ServiceException {
			logger.info("Cards link setting started");
			setLinks(cardLinkIds, null, parentCardInfo);
			logger.info("Cards ChangeState started");
			if(flow == null) {
				throw new IllegalStateException("flow must be not null");
			}

			ArrayList<Action<Void>> actions = null;
			if(hardStates.contains(parentCardInfo.getCard().getState()) && !CollectionUtils.isEmpty(tempCards)) {
				ChangeState action = new ChangeState();
				action.setCard(tempCards.get(0));
				action.setWorkflowMove(flow);
				actions = new ArrayList<Action<Void>>(1);
				actions.add(action);
			} else {
				actions = new ArrayList<Action<Void>>(tempCards.size());
				for(Card card : tempCards) {
					ChangeState action = new ChangeState();
					action.setCard(card);
					action.setWorkflowMove(flow);
					actions.add(action);
				}
			}
			BatchAsyncExecution batchAction = new BatchAsyncExecution();
			batchAction.setActions(actions);
			ObjectId operationType = ObjectId.predefined(BatchAsyncExecution.class, "jbr.batch_resolutions");
			batchAction.setAttrToParent(ObjectId.predefined(CardLinkAttribute.class, "jbr.main.doc"));
			batchAction.setOperationType(operationType);
			serviceBean.doAction(batchAction, ExecuteOption.ASYNC);
			logger.info("Cards sent to change state");
			String msg = getMessage(CHAGE_STATE_SUCCESS);
			PortletMessageType msgType = PortletMessageType.EVENT;
			session.setMessageWithType(msg, msgType);
		}

		public String getChangeStateButtonTitle() {
			return CHAGE_STATE_BUTTON_TITLE;
		}

		public WorkflowMove getWorkflowMove() {
			return flow;
		}

		public void setWorkflowMove(WorkflowMove flow) {
			this.flow = flow;
		}

		public String getChangeStateActionName() {
			return CardPortlet.CUSTOM_STORE_AND_CHANGE_STATE_CARD_ACTION;
		}

		public boolean isShowFirstSaveButton() {
			return true;
		}
	}

	private CardPortletCardInfo parentCardInfo;
	/**
	 * ���� ����� �������� ���������� ����� �������� �������� ���������� {@link #PARAM_BUTTON_TITLE} � {@link #PARAM_TARGET_TEMPLATE}
	 * � ����� �������� ����������� ���������� ���������� ��������
	 */
	@Override
	protected void process(Attribute attr, List cardIds, ActionRequest request,
			ActionResponse response) throws DataException {
		CardPortletSessionBean sessionBean = getCardPortletSessionBean();
		parentCardInfo = sessionBean.getActiveCardInfo();
		ObjectId targetTemplateId = targetTemplate;
		ObjectId parentCardTemplateId = parentCardInfo.getCard().getTemplate();
		if (specificTargetTemplates.containsKey(parentCardTemplateId)){
			targetTemplateId = specificTargetTemplates.get(parentCardTemplateId);
		}
		// �������� ������������ ����������
		if (targetTemplateId == null)
			throw new DataException("Parameter \"" + PARAM_TARGET_TEMPLATE + "\" is required");

		if(reverse) {
			cardLinkIds = reverseAttrs;
		} else {
			cardLinkIds = Collections.singletonList(attr.getId());
		}
		storehandler = new GenerateCardsHandler(sessionBean, targetTemplateId);
		try {
			// �������� ��������� ��������
			Card nestedCard = createCard();
			// ��������� ������ �� ������������ �������� (�� �� ��� � ��� ������� ��������), 
			// ����� ����� ���� ��� �������� ������������� �������� ��� �� ��������� �� ������������� ���� ��������� � ������������ ���������
			if(!CollectionUtils.isEmpty(reverseAttrs)) {
				for(ObjectId mainDocId : reverseAttrs) {
					LinkAttribute linkAttr = (LinkAttribute) nestedCard.getAttributeById(mainDocId);
					if(linkAttr != null) {
						linkAttr.clear();
						linkAttr.addSingleLinkedId((Long) parentCardInfo.getCard().getId().getId());
					}
				}
			}
			WorkflowMove flowMove;
			if(availableWorkflow != null) {
				sessionBean.openNestedCard(nestedCard, null, true);
				flowMove = (WorkflowMove) serviceBean.getById(availableWorkflow);
				changeStateHandler = new ChangeStateGeneratedCardsHandler(sessionBean, flowMove);
				sessionBean.getActiveCardInfo().setChangeStateHandler(changeStateHandler);
			} else {
				sessionBean.openNestedCard(nestedCard, new NewCardLinkItemCloseHandler(), true);
			}
			sessionBean.getActiveCardInfo().setParentCardInfo(parentCardInfo);
			// ���������� ������������ ����������� ���������� �������� (�� ���� ��� ����� ������ ������ sessionBean.openNestedCard())
			sessionBean.getActiveCardInfo().setStoreHandler(storehandler);
			parentCardInfo.setRefreshRequired(true);
		} catch (Exception e) {
			logger.error("Can't redirect to card editing page", e);
			sessionBean.setMessageWithType("edit.link.error.create", new Object[] { e.getMessage() }, PortletMessageType.ERROR);
		}
	}
	
	/**
	 * ���������� �������� �� ���������� ��������� �� id
	 * @param cardId id ��������
	 * @return ��������� ��������. ���� �������� �� ������� ��� ��������� ��������� ������, �� null
	 */
	private Card getTempCard(ObjectId cardId) {
		for(Card c : tempCards) {
			if(c != null && c.getId().equals(cardId))
				return c;
		}
		return null;
	}
	
	private void setLinks(List<ObjectId> cardLinkIds, CardPortletCardInfo closedCardInfo, CardPortletCardInfo previousCardInfo) throws DataException, ServiceException {
		if (storedCardIds == null || storedCardIds.isEmpty()) {
			logger.warn("There's no card ID's to add into parent card's attribute");
			return;
		}
		final Card previousCard = previousCardInfo.getCard();
		Card card = previousCard;
		boolean first = true;
		
		for (ObjectId newCardId: storedCardIds) {
			Card tempCard = getTempCard(newCardId);
			if(reverse && tempCard == null)
				card = (Card) serviceBean.getById(newCardId);
			else if(reverse && tempCard != null)
				card = tempCard;
			// �������� ������� � ������� ����� ����������� ID ����� ��������
			for(ObjectId cardLinkId : cardLinkIds) {
				CardLinkAttribute attr = (CardLinkAttribute)card.getAttributeById(cardLinkId);
				if (first) {
					first = false;
					// �������� ��� �������� �������� ����������
					if(!reverse)
						previousCardInfo.setAttributeEditorData(cardLinkId, AttributeEditor.KEY_VALUE_CHANGED, Boolean.TRUE);
					if (!attr.isMultiValued()) {
						// ���� ��������� ������ ���� �������� -- ���������� ��� � ������������
						attr.addSingleLinkedId(reverse ? previousCard.getId() : newCardId);
						continue;
					}
				}
				// ��������� ID
				attr.addLinkedId(reverse ? previousCard.getId() : newCardId);
			}
			if(reverse) {
				/*OverwriteCardAttributes action = new OverwriteCardAttributes();
				action.setCardId(newCardId);
				action.setAttributes(Collections.singletonList(attr));*/
				serviceBean.doAction(new LockObject(newCardId));
				serviceBean.saveObject(card);
				serviceBean.doAction(new UnlockObject(newCardId));
				if(availableWorkflow != null) {
					int index = tempCards.indexOf(tempCard);
					if(index != -1) {
						tempCards.remove(index);
						tempCard = (Card) serviceBean.getById(newCardId);
						tempCards.add(index, tempCard);
					}
				}
				/*serviceBean.doAction(action);*/
				if(logger.isInfoEnabled())
					logger.info("Card " + newCardId + " mapped to parentCard successfully");
			}
		}
		logger.info("Generated card ID's added into parent card's attribute");
	}

	@Override
	public void setParameter(String name, String value) {
		// ��������� ����������
		if (PARAM_TARGET_TEMPLATE.equals(name)) {
			targetTemplate = ObjectIdUtils.getObjectId(Template.class, value, true);
		} else if (PARAM_MAPPING_PACKAGE.equals(name)) {
			mappingPackage = value;
		} else if (PARAM_MAPPING.equals(name)) {
			final String[] mappings = value.trim().split("\\s*[;]\\s*");
			for(String mapping: mappings){
				strMappings.add(mapping.trim().replaceAll("\n", "").replaceAll("\r", ""));
			}
		} else if (PARAM_MAPPING_SPLIT.equals(name)){
			mappingSplit = Boolean.parseBoolean(value);
		} else if (PARAM_SPECIFIC_TARGET_TEMPLATES.equals(name)) {
			String[] pairs = value.split("\\s+;\\s+");
			for (String pair : pairs){
				String[] splittedPair = pair.split("->");
				if (splittedPair.length != 2) {
					throw new IllegalArgumentException(PARAM_SPECIFIC_TARGET_TEMPLATES + " parameter should be in format: " +
						"'templateId1->targetTemplateId1;...;templateIdN->targetTemplateIdN'");
				}
				ObjectId templateId = ObjectIdUtils.getObjectId(Template.class, splittedPair[0], true);
				ObjectId targetTemplateId = ObjectIdUtils.getObjectId(Template.class, splittedPair[1], true);
				specificTargetTemplates.put(templateId, targetTemplateId);
			}
		} else if (PARAM_REVERSE.equals(name)){
			reverse = Boolean.parseBoolean(value.trim());
		} else if (PARAM_REVERSE_ATTRS.equals(name)) {
			final String[] attrs = value.trim().split("\\s*[;]\\s*");
			reverseAttrs = new ArrayList<ObjectId>(attrs.length);
			for(int i = 0; i < attrs.length; i++) {
				reverseAttrs.add(ObjectIdUtils.getObjectId(CardLinkAttribute.class, attrs[i], true));
			}
		} else if(PARAM_AVA_WORKFLOW.equals(name)) {
			availableWorkflow = ObjectIdUtils.getObjectId(WorkflowMove.class, value.trim(), true);
		} else if(PARAM_CLOSE_HANDLER_POLICY.equals(name)) {
			for (CloseHandlerPolicy policy : CloseHandlerPolicy.values()) {
				if(policy.getPolicyName().equalsIgnoreCase(value)) {
					closeHandlerPolicy = policy;
					break;
				}
			}
		} else {
			super.setParameter(name, value);
		}
	}

	// ��������� ��������� �� actionsConfig
	private Messages messages = null;
	private String getMessage(String key, Object[] params) {
		if (messages == null) {
			ActionsDescriptor desc = (ActionsDescriptor)parentCardInfo.getAttributeEditorData(getAttribute().getId(),
							ActionsSupportingAttributeEditor.ACTIONS_DESCRIPTOR);
			//��������� ��� �������������� ��������
			if (desc == null){
				HierarchyDescriptor descriptor = (HierarchyDescriptor)
					parentCardInfo.getAttributeEditorData(getAttribute().getId(),
						HierarchicalCardLinkAttributeEditor.HIERARCHY_EDIT_DESCRIPTOR);
				if (descriptor != null){
					desc = descriptor.getActionsDescriptor();
				}
				if (descriptor == null){
					descriptor = (HierarchyDescriptor)
							parentCardInfo.getAttributeEditorData(getAttribute().getId(),
								HierarchicalCardLinkAttributeViewer.HIERARCHY_VIEW_DESCRIPTOR);
					if (descriptor != null){
						desc = descriptor.getActionsDescriptor();
					}
				}
			}
			messages = desc.getMessages();
		}
		LocalizedString message = messages.getMessage(key);
		if (message == null)
			return null;
		return MessageFormat.format(message.getValue(), params);
	}

	private String getMessage(String key) {
		return getMessage(key, null);
	}

}
