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
package com.aplana.dbmi.jbr.processors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.BatchAsyncExecution;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.FetchChildrenCards;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.jbr.util.AttributeSelector;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.CheckingAttributes;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.WorkflowMoveRequiredField;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.BasePropertySelector;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;

/**
 * ��������� ��������� �������������� ��������. � ���� ������� ������, 
 * �� ������� ����������.
 * � ������ ����� ������� ����� ��� cardlink, �� ����� ������� ��������
 * hidden_status. ���� �������� ������ ������ �������� ��� ��������
 * �������� ��������, ������� �� ����� �����������, ��� ����������� ����, ���� ������� ��� ���. 
 * �.�. ���� ������� �������� ��������
 * ������ � ���� ��������, �� ����� ���������, ��� ���� ������� ������.
 * 
 * �������������� �������� �����������, ������ � ��� ������ ����� 
 * ������� condition ����������� ��� �� ������ � ����������� ������� ��� �������� ������� ��������
 * 
 * (YNikitin, 2011/08/29) �������������� ������� �������� - ������ � ��� ������, ����� ������� ������������ �� ������� (������ ��� ������������� ������ � �������������� �������� � ����������)
 * (YNikitin, 2011/08/31) ����� �������� ����������:
 * 1. ������ ����������, ����� ������� �� ������, �������� ������� �����������
 * 2. ������� ������������ ��������� ��� ������ � ������ ������� �������� ��� ��������� �������� 
 * 
 * (SMedvedev, 2013/07/24) ����� ������ ������� �������� - ��������� �� ���������� �� �� �� ������� workflow_move_required_field.
 * ���� ��������� �������� <{@value #checkWfmRequiredFields} = true, �� ����� �� �������� �������� <{@value #mustEmpty}, �.�. �� ����� ��������
 * � ����������� �� ������� � ��.
 * ���������� ������� ����� wfm_id, ������� ����������� ���� ���������, ����� ��������� �������� ������ ��� ����� ��������.
 * @author agadelshin
 *
 */
public class TestEmptyAttrProcessor extends ProcessCard {

	private static final long serialVersionUID = 1L;
	/**
	 * ������������ ��������. ������ ������� ��� ������ ������������
	 * ���� �� ��� ���.
	 * ������: ���: ���_��������
	 * ��� ���_�������� - attribute_code ��� ��� �������
	 */
	private static final String PARAM_TEST_EMPTY_ATTR = "test_attr";
	/**
	 * ��������, ����������, ��� ���� �������� �������� � ������� �������� (� �� � ���������, ��� ������������� linked_attr)
	 */
	private static final String PARAM_CURRENT_ATTR = "current@";
	/**
	 * �������������� ��������. � ������ ����� ������� ���� cardlink,
	 * ������ ������ �������� ��� ��������
	 * �������� ��������, ������� �� ����� �����������, ��� ����������� ����, 
	 * ���� ������� ��� ���. 
	 * ������: �������_c������, �������_�������,...
	 */
	private static final String PARAM_HIDDEN_STATUS = "hidden_status";
	/**
	 * �������������� ��������. ������ ������� (�������� ��������� ��������)
	 * ��� ������� �������������� �������� �� ������������ �������� test_attr.
	 * ���� �������� �� ����� ��� �����������, �� ������� ������������ �������� test_attr
	 * �����������
	 * ������: ����1=��������11, ��������12 ; a���2=�������21, ��������22
	 * ��� ����: ���:�������_��������
	 * �������� - �������� �������� (����� ��������, ��� cardLink, ������ ��� string).
	 * ����� ��������� ����� ���� ��������� CURRENT_USER, BOSS, � �.�. (�� CheckingAttributes)
	 * = - ���� ���������, ������ ���� ����� ������������ ����, #, 
	 * ����� ����� ����������� �������� �� ������������.
	 * ������� ������� � ����� condition, ���������� ;, ������������ ������ "���".
	 * ����� �������� ��������� ���������� condition. ������ �� ������� ����� ���������� 
	 * ������ "�"
	 */
	private static final String PARAM_CONDITION_ATTR = "condition";
	
	/**
	 * ���� ���� ������� �������������� �������� � ����������� ����. ��������, ������� ��������,
	 * ����� ���� ������������ ��� �� ������������ �� ������ ������� workflow_move_required_field
	 * ��� ������-���� �������� ��������� ��������. ����� �� ������ ��� ��������� �� �� � ������������
	 * �������� <{@value #mustEmpty} � ����������� �� ���������� ��������.
	 *   0 - mustEmpty = true; ������� ������ ���� ���� ��� ��������
	 *   1 - mustEmpty = false; ������ ���� �������� ��� ��������
	 *   2 - ��� �������� � ������� -> �� ��������� ������� -> return null;
	 */
	private static final String PARAM_CHECK_FROM_DB = "checkWfmRequiredFields";
	
	/**
	 * ������ ������������� ������������ ��������, ��� �������� ����� ��������� ������������ ��������
	 */
	private static final String PARAM_WFM = "wfm";
	
	// ToDo: (YNikitin, 2011/06/02) ��������� ��� ������� �������� ���� � ProcessCard, ���� � ��������� ����������� �����, ����� ����� ��������� ��� � ���� ��������   
	// ��� �������� ������� � ������������ ��������...
	/* 
	 * AGadelshin, 2011/12/19
	 * ��� �������� ������� ������������ �������� ����� ��������������� ����������� linked_attr, linked_attr_test � linked_attr_state
	 * ������ � �������� linked_attr ������� �� ������������ � ������� �������� � ����� �������� is_reverse_link ������ true
	static final String PARAM_LINK_ATTR_FROM_PARENT = "link_attr_fromParent"; 
	static final String PARAM_PARENT_ATTR_TEST = "parent_attr_test";
	*/
	/**
	 * �������������� ��������. ������������ ��� ������ ��������� ��������, ������ � ��������� linked_attr
	 * ���� ����� true - �� ������� �������� �������� �������� ������������ � �������, �
	 * ������� linked_attr ��� �������� �� ������� ������������ � �������
	 * ���� ����� false (�� ���������) - �� ������� �������� �������� �������� �������� � �������,
	 * � ������� linked_attr ��� ������� �� ������� � ������� ��������
	 */
	static final String PARAM_IS_REVERSE_LINK = "is_reverse_link";

	// (YNikitin, 2011/08/31) ��������� ������� � ��������� ��������� (true - ��� ���������� ������� ���� �� � ����� ��������� ��������)
	// (AGadelshin, 2011/12/19) ��� ������ ��������� �������� ���� 
	// TODO: ������� ��� �� ������� �������������, ����� ������ ��������� �������� ���� (AGadelshin, 2011/12/19)
	protected static final String PARAM_LINKED_ATTR = "linked_attr"; 
	protected static final String PARAM_LINKED_ATTR_TEST = "linked_attr_test";
	protected static final String PARAM_LINKED_ATTR_STATE = "linked_attr_state";
	
	protected static final String PARAM_NO_SYSTEM_USER = "no_system_user";	// (YNikitin, 2011/08/29) �������������� ������� �������� - ������ � ��� ������, 
																	// ����� ������� ������������ �� ������� (������ ��� ������������� ������ � �������������� �������� � ����������)
	
	protected static final String PARAM_MUST_EMPTY = "must_empty";			// (YNikitin, 2011/08/31) "��������" ������ ���������� - ���������� ����� �������� � ������ ��������� �������� 
	protected static final String PARAM_EMPTY_MESSAGE = "empty_message";		// (YNikitin, 2011/08/31) ������������� ��������� ��� ���������� � ������ ������� �������� 
	protected static final String PARAM_NOT_EMPTY_MESSAGE = "not_empty_message";			// (YNikitin, 2011/08/31) ��������� ��� ���������� � ������ ��������� �������� 
	protected static final String PARAM_LINKED_ATTR_TYPE = "linked_attr_type";

	/*
	private final List<PropertySelector> parentConditions = new ArrayList<PropertySelector>();
	private ObjectId linkAttrFromParent; //jbr.reports
	*/
	private boolean isReverseLink = false;

	protected ObjectId linkedAttr;		// ���� �� ��������� ��������, ��� ������� ����� ����������� �������
	protected ObjectId wfm;			//����������� �������, ��� �������� ����������� �������� ���������
	protected CheckingAttributes linkedConditions;	// �������
	protected HashSet<ObjectId> linkedStates = new HashSet<ObjectId>();	// ���������� ������� ��������� ��������
	
	protected CheckingAttributes conditionAttrs;
	ObjectId emptyAttrId;
	private boolean checkCurCardAttr = false;
	Set<ObjectId> hiddenStatus = null;
	protected Set<String> conditionStrs = new HashSet<String>();		// ������ ��������� �������� ������� �������
	protected Set<String> linkedConditionStrs = new HashSet<String>();	// ������ ��������� �������� ������� �������
	protected boolean noSystemUser = false;	// �� ��������� ����������� ������������ �� ����� ��������
	protected boolean mustEmpty = false;		// �� ��������� ��������� ���������, ����� ������� ��� ����������� ��������
	protected boolean checkWfmRequiredFields = false;
	protected String emptyMessage;
	protected String notEmptyMessage;
	protected Set<ObjectId> linkedAttrTypes;

	@Override
	public Object process() throws DataException {
		//Card card = getCard();
		try {
			// ��������� ������� ������� ��� �������� ������� ��������
			conditionAttrs = new CheckingAttributes(getQueryFactory(), getDatabase(), getSystemUser());
			for (String value : conditionStrs){
				conditionAttrs.addCondition(value);
			}
			// ��������� ������� ������� ��� �������� ��������� ��������
			linkedConditions = new CheckingAttributes(getQueryFactory(), getDatabase(), getSystemUser());
			for (String value : linkedConditionStrs){
				linkedConditions.addCondition(value);
			}
		} catch (Exception e) {
			logger.error("������ ��� �������� ���������", e);
			throw new DataException(e);
		}

		List<Action<?>> actions = new ArrayList<Action<?>>();
		if(getAction() instanceof BatchAsyncExecution) {
			actions = ((BatchAsyncExecution)getAction()).getActions();
		} else actions.add(getAction());
		
		for(Action<?> action : actions) {
			Card card = getCard();
			if(card == null
					&& getAction() instanceof BatchAsyncExecution && action instanceof ChangeState) {
				// ����� �������� �� �����
				card = ((ChangeState)action).getCard();
			}
			if(card == null)
				throw new DataException("�� ������� �������� ��� TestEmptyAttrProcessor");
			//���� ���� ������� �������������� �� ���������� �� ���� (WorkflowMoveRequiredField)
			if (checkWfmRequiredFields) {
				if (wfm == null&&!(action instanceof ChangeState)) {
					throw new DataException("�� ����� �������� wfm ��� TestEmptyAttrProcessor, ���� ������� ���� �� ChangeState");
				}
				// ���� �������� �������� �� ����� � ������ ���� - ChangeState, �� ����� �������� ����� �� ����
				if (wfm == null&&(action instanceof ChangeState)) {
					wfm = ((ChangeState)action).getWorkflowMove().getId();
				}
				// ����������� workflow move required fields ��� ������� ������� ��������
				final ChildrenQueryBase viewQuery = getQueryFactory().getChildrenQuery(Template.class, WorkflowMoveRequiredField.class);
				viewQuery.setParent(card.getTemplate());
				final List<WorkflowMoveRequiredField> wmrf = getDatabase().executeQuery(getUser(), viewQuery);
					// �������� �������� ������� (emptyAttrId) ��� ��������� �������� (wfm)
				boolean found = false;
				for (Iterator<WorkflowMoveRequiredField> i = wmrf.iterator(); i.hasNext(); ) {
					final WorkflowMoveRequiredField rec = i.next();
					if (wfm.getId().equals(rec.getWorkflowMoveId()) && rec.getAttributeCode().equals(emptyAttrId.getId())) {
						switch (rec.getMustBeSetCode()) {
							case 0: mustEmpty = true;  found = true; break;
							case 1: mustEmpty = false; found = true; break;
							case 2: return null;
						}
						break;
					}
				}
				//���� �������� ��� ����� ������������ � WorkflowMoveRequiredFields �� �� ��������� ��� ��������������
				if (!found) {
					return null;
				}
			}
			
			//Card parentCard = null;
			List<Card> linkedCards = null;
			if (noSystemUser&&getUser().getPerson().getId().getId().equals(getSystemUser().getPerson().getId().getId())){
				if(logger.isWarnEnabled())
				logger.warn("Proceesor working when current user is only no _SYSTEM_, but current user is "+getUser().getPerson().getFullName());
				return null;
			}
			// ��������� ������������ �������� ��� ������������� 
			/*
			if ( (parentConditions.size() > 0) && (linkAttrFromParent != null) ){
				final List<Card> parents = getLinked(card.getId(), linkAttrFromParent);
				final int count = (parents != null) ? parents.size() : 0;
				switch(count)
				{
					case 1: // the single ok variant
						parentCard = parents.get(0);
						break;
					case 0:
						logger.warn("No parent card found for the current one -> Conditions check for parent card skipped");
						break;
					default:
						logger.error("Multiple parents found for the current card " +
							" but this is not allowed -> Conditions check for parent card skipped");
				}	
			}
			*/
			if (linkedAttr != null){
				if(BackLinkAttribute.class.isAssignableFrom(linkedAttr.getType())) {
					linkedCards = listLinkedCards(linkedAttr);
				} else {
					linkedCards = getLinked(card.getId(), linkedAttr, linkedAttrTypes);
				}
			}
	
			List<Card> cardsForCheck = new ArrayList<Card>();
			if(linkedCards != null) {
				cardsForCheck = linkedCards;
			} else if (linkedAttr == null){
				cardsForCheck.add(card);
			}
			// ���� ������� �������� ������ ��� ��� ����������, ���� ������� �������� � ������������ �������� �����������
			if ((conditionAttrs != null && conditionAttrs.check(card, getUser()))||conditionAttrs==null) {
				for(Card c : cardsForCheck) {
					// ��������� ��������� ��������, ��� ���� ����� ��� �������� ���� ��������
					if(linkedCards != null)
						c = loadCardById(c.getId());
					// ���� ������� � ��������� ��������� �����������, �� ��������� �������� �� �������
					if (checkLinkedConditions(linkedConditions, Collections.singletonList(c))) {
						Attribute attr = checkCurCardAttr ? card.getAttributeById(emptyAttrId) : c.getAttributeById(emptyAttrId);
						if (mustEmpty&&!isEmpty(attr)) { // ������� ������ ���� ������, �� �� �� ������
							logger.error("Attribute " + attr.getId().getId() + " is not empty, but must be in card " + (c.getId() != null? c.getId().getId() : "null"));
							throw new DataException(notEmptyMessage);
						} 
						if (!mustEmpty&&isEmpty(attr)) { // ������� ������ ���� ��������, �� �� ������
							logger.error("Attribute " + attr.getId().getId() + " is empty, but must is filled " +  (c.getId() != null? c.getId().getId() : "null"));
							if (emptyMessage!=null&&emptyMessage!="")
								throw new DataException(emptyMessage);
							else
								throw new DataException("jbr.test.attr.empty", new Object[] {attr.getName(), (c.getId() != null? c.getId().getClass() : "null")});
						}
					} else if ((linkedAttr.getId().equals(emptyAttrId.getId())&&!mustEmpty&&linkedConditions!=null&&!linkedCards.isEmpty()&&!checkLinkedConditions(linkedConditions, Collections.singletonList(c)))) {
						// ������ ������: ��� ������� �����������, ����������� �� ������� ������� ��������� � ����������� �����������, �� ������ ���� ������, ������������ ������� �� �����������, � ������ ����������� �������� �� ������
						Attribute attr = c.getAttributeById(emptyAttrId);
						logger.error("Attribute " + attr.getId().getId() + " is empty, but must is filled " +  (c.getId() != null? c.getId().getId() : "null") + " (special case).");
						if (emptyMessage!=null&&emptyMessage!="")
							throw new DataException(emptyMessage);
						else
							throw new DataException("jbr.test.attr.empty", new Object[] { (c.getId() != null? c.getId().getId() : "null")});
					}
				}
			}
		}
		return null;
	}
	
	protected boolean checkConditions(List<BasePropertySelector> conds, Card c) throws DataException 
	{
		/*
		 * ������ �������� ������:
		 * 		attrList = (ListAttribute) card.getAttributeById( ((AttributeSelector) cond).attrId);
		 * 		attrList.getReference() = ObjectId(id='ADMIN_26973', type=com.apana.dbmi.model.Reference);
		 * 		attrList.getValue() = ReferenceValue( 
		 *			id=ObjectId(id=1433, type=com.apana.dbmi.model.ReferenceValue), 
		 *			active=false, children=null, ..., valueEn="No", valueRu="���")
		 */
		if (conds == null || c == null) return true;
		boolean cardFetched = false;
		for (BasePropertySelector cond: conds) 
		{
			if (!cardFetched) {
				// ���������� �������� ���� ��� �� ������...
				if ( 	(cond instanceof AttributeSelector)
						&& null == c.getAttributeById( ((AttributeSelector) cond).getAttrId() ) )
				{
					c = super.loadCardById(c.getId());
					cardFetched = true;
				}
			}
			final boolean ok = cond.satisfies(c);
			if (!ok) {
				if(logger.isDebugEnabled())
				logger.debug("Card "+  (c.getId() != null? c.getId().getId() : "null") +" did not satisfies condition " + cond);
				return false;
			}
		}
		return true;
	}

	/**
	 * �������� ������� �� ������ ��������� �������� 
	 * @param conds			- ������ ����������� �������
	 * @param linkedCards	- ������ ��������� ��������
	 * @return TRUE 		- ���� ���� �� �� ����� ��������� �������� ��� ������� ����������� �������, false - ���� ������� ������� �� ���� ������ ��������
	 * @throws DataException
	 */
	protected boolean checkLinkedConditions(CheckingAttributes conds, List<Card> linkedCards) throws DataException 
	{
		/*
		 * ������ �������� ������:
		 * 		attrList = (ListAttribute) card.getAttributeById( ((AttributeSelector) cond).attrId);
		 * 		attrList.getReference() = ObjectId(id='ADMIN_26973', type=com.apana.dbmi.model.Reference);
		 * 		attrList.getValue() = ReferenceValue( 
		 *			id=ObjectId(id=1433, type=com.apana.dbmi.model.ReferenceValue), 
		 *			active=false, children=null, ..., valueEn="No", valueRu="���")
		 */
		if (conds == null || linkedCards == null || linkedCards.isEmpty()) return true;
		for(Card c: linkedCards){
			Card card = super.loadCardById(c.getId());
			if (linkedStates.isEmpty() || linkedStates.contains(card.getState())) {
				if(conds.check(card, getUser()))
					return true;
			}
		}
		return false;
	}
	protected List<Card> getLinked(ObjectId cardId, ObjectId linkAttr) 
			throws DataException
		{
			return getLinked(cardId, linkAttr, null);
		}

	/**
	 * ��������� ������ ���������� �������� (�� ������� ��� ��������� �����) 
	 */
	protected List<Card> getLinked(ObjectId cardId, ObjectId linkAttr, Set<ObjectId> linkedAttrTypes) 
		throws DataException
	{
		final FetchChildrenCards action =  new FetchChildrenCards();
		action.setCardId(cardId);
		action.setLinkAttributeId(linkAttr);
		action.setLinkAttrTypes(linkedAttrTypes);
		action.setReverseLink(isReverseLink);
		return CardUtils.execSearchCards(action, getQueryFactory(), getDatabase(), getSystemUser());
	}
	
	protected List<Card> listLinkedCards(ObjectId attrId) 
	{
		final ListProject list = new ListProject();
		list.setAttribute(attrId);
		list.setCard(getCardId());
		final List<SearchResult.Column> cols = new ArrayList<SearchResult.Column>(1);
		cols.add( CardUtils.createColumn(Card.ATTR_TEMPLATE));
		list.setColumns( cols);
		try {
			ActionQueryBase query = getQueryFactory().getActionQuery(list);
			query.setAction(list);
			return ((SearchResult) getDatabase().executeQuery(getSystemUser(), query)).getCards();
		} catch (DataException e) {
			logger.error("Error searching back links to card ...", e);
			return null;
		}
	}

	protected boolean isEmpty(Attribute attr) throws DataException {
		if (attr == null){
			return true;
		} else if (attr instanceof BackLinkAttribute){		// ������� ��� �������� ������� ��������
			final Collection<Card> parents = listLinkedCards(attr.getId());
			if (parents==null||parents.isEmpty())
				return true;
			else
				return false;
		} else if (attr.isEmpty()) {
			return true;
		} else {
			if (attr instanceof CardLinkAttribute) {
				if (hiddenStatus == null)
					return false;
				CardLinkAttribute cl_attr = (CardLinkAttribute) attr;
				Collection<ObjectId> cardIds = (Collection<ObjectId>)cl_attr.getIdsLinked();
				for (ObjectId cardId : cardIds) {
					Card res = loadCardById(cardId);
					if (!hiddenStatus.contains(res.getState())) {
						return false;
					}
				}
				return true;
			}
			return false;
		}
	}

	@Override
	public void setParameter(String name, String value) {
		if (PARAM_TEST_EMPTY_ATTR.equals(name)) {
			if(value.contains(PARAM_CURRENT_ATTR)) {
				checkCurCardAttr = true;
				value = value.replaceAll(PARAM_CURRENT_ATTR, "");
			}
			emptyAttrId = AttrUtils.getAttributeId(value);
		} else if (PARAM_HIDDEN_STATUS.equals(name)) {
			hiddenStatus = new HashSet<ObjectId>();
			String[] statuses = value.split(",");
			for (String s : statuses) {
				ObjectId statusId = ObjectId.predefined(CardState.class, s.trim());
				hiddenStatus.add(statusId);
			}
		} else if (PARAM_CONDITION_ATTR.equalsIgnoreCase(name)) {
			conditionStrs.add(value);
		} if (PARAM_IS_REVERSE_LINK.equalsIgnoreCase(name)) { 
			isReverseLink = Boolean.parseBoolean(value);
		} else if (PARAM_NO_SYSTEM_USER.equalsIgnoreCase(name)){
			noSystemUser = Boolean.parseBoolean(value);
		} else if (PARAM_MUST_EMPTY.equalsIgnoreCase(name)){
			mustEmpty = Boolean.parseBoolean(value);
		} else if (PARAM_EMPTY_MESSAGE.equalsIgnoreCase(name)){
			emptyMessage = value.trim(); 
		} else if (PARAM_NOT_EMPTY_MESSAGE.equalsIgnoreCase(name)){
			notEmptyMessage = value.trim(); 
		} else if (PARAM_CHECK_FROM_DB.equalsIgnoreCase(name)){
			checkWfmRequiredFields = Boolean.parseBoolean(value);
		} else if (PARAM_WFM.equalsIgnoreCase(name)){
			wfm = IdUtils.tryFindPredefinedObjectId(value, WorkflowMove.class, true);
		} else if (PARAM_LINKED_ATTR.equalsIgnoreCase(name)){
			linkedAttr = IdUtils.smartMakeAttrId(value, CardLinkAttribute.class);
		} else if (PARAM_LINKED_ATTR_TYPE.equalsIgnoreCase(name)){
			linkedAttrTypes = new HashSet<ObjectId>();
			String[] types = value.split(",");
			for (String type : types) {
				linkedAttrTypes.add(IdUtils.smartMakeAttrId(type.trim(), ReferenceValue.class));
			}
		} else if (PARAM_LINKED_ATTR_TEST.equalsIgnoreCase(name)){
			linkedConditionStrs.add(value);
		} else if (PARAM_LINKED_ATTR_STATE.equalsIgnoreCase(name)){
			parseStates(value);
		}
			
		super.setParameter(name, value);
	}

	private void parseStates(String value) {
		String[] ids = value.split("\\s*[,;]\\s*");
		for (int i = 0; i < ids.length; i++)
			linkedStates.add(ObjectIdUtils.getObjectId(CardState.class, ids[i], true)/*.getId()*/);
	}

}
