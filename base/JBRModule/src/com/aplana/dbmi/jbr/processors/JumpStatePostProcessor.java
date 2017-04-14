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

import java.io.Serializable;
import java.sql.Types;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.FetchChildrenCards;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.processors.card.runcheck.CardCheckException;
import com.aplana.dbmi.jbr.processors.card.runcheck.CardChecker;
import com.aplana.dbmi.jbr.util.AttributeSelector;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.dbmi.service.impl.BasePropertySelector;
import com.aplana.dbmi.service.impl.UserData;

/**
 * ���� ��������� ��� ����������� �������� �������� � ������ ���������.
 * ��������� - ������� ��������� � ������ ��� ���� � �������� ���������, ���
 * ������� ����� ��������� �������. ��������� ��������� �������� ��������� ��
 * ������ � ������� ��������, �� � � ������������ (���� ������� �����).
 * ���������: 1) dest_wfm: id �������� ��� "������";
 * 
 * 2) getActiveCard: �������-���� ��������� �������� (�� �������) ��������,
 * default: "@SELF" �������: "action.card", "object", "card".
 * 
 * 3) attr_test: ������� ��� �������� � �������� ��������. (!) ����� �������
 * ��������� �������, ������� ����� ����������� ����������. ��������: <parameter
 * name="attr_test" value="jbr.incoming.oncontrol=jbr.incoming.control.no"/> //
 * comment="1433" <parameter name="attr_test"
 * value="jbr.incoming.oncontrol#jbr.incoming.control.yes"/> // comment="1432"
 * <parameter name="attr_test" value="list:JBR_IMPL_ONCONT#1432"/> //
 * comment="1432 == yes" <parameter name="attr_test"
 * value="jbr.deliveryItem.method=modeDeliveryMEDO"/>
 * 
 * 4) link_attr_fromParent: id �������� ��� ��������� ������������ ��������
 * ����� ��������� �������� �������� ����� getActiveCard. (!) ����� �������
 * ��������� �������, ������� ����� ����������� ����������.
 * 
 * 5) parent_attr_test: ������� ��� ������������ ��������. (!) ����� ����
 * ��������� �������, ������� ����� ����������� ����������. ��������: <parameter
 * name="parent_attr_test" value="
 * com.aplana.dbmi.jbr.processors.card.runcheck.ChkLinkedCardStates(
 *  ) "/>
 * 
 * 6) action_test: (!) ����� ������� ��������� �������, ������� �����
 * ����������� ����������. ��������: <parameter name="action_test" value="" />
 * 
 * 7) failIfStatusIsOther: false/true (default) �������� �� ����������, ����
 * �������� ������, � ������� �������� � ����� �������, �� �������� ����������
 * ��������� �������� � dest_wfm �������.
 * 
 * @author RAbdullin
 * 
 */
public class JumpStatePostProcessor extends ProcessCard {
	private static final long serialVersionUID = 1L;

	/**
	 * ����������� ��������� ����������: PARAM_XXX.
	 */
	// ������� �������� � ����: "<objectid-�������� ��������>{.���� �
	// ��������}=��������"
	static final String PARAM_DEST_WFM = "dest_wfm";
	// static final String PARAM_ATTR_CONDITION = "attr_condition";
	// static final String PARAM_ATTR_VALUE = "attr_value";
	static final String PARAM_ATTR_TEST = "attr_test";

	static final String PARAM_LINK_ATTR_FROM_PARENT = "link_attr_fromParent";
	static final String PARAM_LINK_ATTR_TO_CHILDREN = "link_attr_toChildren";
	static final String PARAM_PARENT_ATTR_TEST = "parent_attr_test";
	static final String PARAM_CHILDREN_ATTR_TEST = "children_attr_test";

	/**
	 * ������� �� �������� ����� ����� ������������ ������ ��� ��������.
	 * ����������� ����� ��� ������� <param name="card_test"
	 * value="class(name1=val1;name2=val2...)" />
	 */
	static final String PARAM_CARD_TEST = "card_test";
	
	static final String PARAM_CUR_USER = "currentUser";

	/**
	 * ��������� �������� ��� reflect-��������� �������� �������� � �������
	 * ������� (��. reflectionGetActiveCard).
	 */
	static final String PARAM_GET_ACTIVE_CARD = "getActiveCard";

	/**
	 * ��� �������, � ������ ���� �������� �� ��������� � ��� ���������, ��
	 * �������� ����� ����������� �������: true (�� ����)= "fail", �.�. �������
	 * ����������, false = ������������, �.�. �� ��������� �������.
	 */
	static final String PARAM_FAIL_IF_SRC_STATUS_IS_OTHER = "failIfStatusIsOther";

	/**
	 * ��������� ��������� ��� ��������������.
	 */
	static final String MSG_CARD_0_DID_NOT_SATISFY_CONDITIONS = "card ''{0}'' did not satisfy conditions -> no more cardstate changes performed";
	static final String MSG_CARD_0_MOVED_TO_NEW_STATE_SUCCESSFULLY = "card ''{0}'' moved to new state SUCCESSFULLY";

	/*
	 * ��� ��� workflowMode ��� �������� �������� ��� ���������� ���� �������.
	 */
	private ObjectId destWFM = null;

	/**
	 * ���� ������������ � ������, ����� �������� ��������� � ���������, ��
	 * �������� ������� ��������� ������.
	 */
	private boolean failIfStatusIsOther = true;
	
	/**
	 * ����, ��� ������� ���� ������������ ��� ������� �������������.
	 */
	private boolean currentUser = false;

	/**
	 * ������ ��� reflection-��������� ������� ��������. ��-���������,
	 * ����������� ��� ��������� ������ ��� ��������, ��� ��� ������������
	 * "action.card". ������� ���������: "action.card", "object", "card".
	 */
	private String reflectionGetActiveCard = "@SELF"; // "action.card";

	// DONE: hardcode to be changed into xml-conditions
	/*
	 * ������� ��� ������ �������� ��������, ��� �������� ���� ��� �������� ��
	 * �� ��������, ����� ������� ����� �� ������� <parameter name="attr_test"
	 * value="jbr.incoming.oncontrol=jbr.incoming.control.no" /> //
	 * comment="1433" <parameter name="attr_test"
	 * value="jbr.incoming.oncontrol#jbr.incoming.control.yes"/> //
	 * comment="1432" <parameter name="attr_test"
	 * value="list:JBR_IMPL_ONCONT#1432" /> //
	 * referencevalue.jbr.incoming.control.yes=1432 //
	 * referencevalue.jbr.incoming.control.no=1433 //
	 * listattribute.jbr.incoming.oncontrol=JBR_IMPL_ONCONT
	 */
	private final List<BasePropertySelector> conditions = new ArrayList<BasePropertySelector>();
	private final List<BasePropertySelector> parentConditions = new ArrayList<BasePropertySelector>();
	private final List<BasePropertySelector> childrenConditions = new ArrayList<BasePropertySelector>();

	ObjectId linkAttrFromParent; // jbr.reports
	ObjectId linkAttrToChildren;

	// listattribute.jbr.oncontrol=referencevalue.jbr.commission.control.no
	/**
	 * ���������� ���������.
	 */
	static final String MSG_PARAMETER_ASSIGNED_3 = "assigned parameter ''{0}''=''{1}''  ->  ''{2}''";

	@Override
	public Object process() throws DataException {
		if (destWFM == null || destWFM.getId() == null) {
			logger.warn("workFlowModeId is null -> exiting");
			return null;
		}
		logger.info(MessageFormat.format("checking conditions before workflowmove step ''{0}''", destWFM.getId()));

		// ������ �� �������� � ChangeState
		/*
		 * final ChangeState changeState = (ChangeState) getAction(); final Card
		 * card = changeState.getCard();
		 */
		Card card = null;
		try {
			if (reflectionGetActiveCard == null || "@SELF".equalsIgnoreCase(reflectionGetActiveCard))
				// �������� ������ �� ��������
				card = getCard(getSystemUser(), Attribute.ID_CHANGE_DATE); 
			else
				card = (Card) PropertyUtils.getProperty(this, reflectionGetActiveCard);
		} catch (Exception ex) {
			logger.error(MessageFormat.format(
						"Fail to get active card via reflection link ''{0}'', error is: {1}",
						reflectionGetActiveCard, ex.getMessage()));
			throw new DataException("factory.action", new Object[] { reflectionGetActiveCard }, ex);
		}
		if (card == null) {
			logger.warn("action.card is null -> exiting");
			return null;
		}

		final ObjectId cardId = card.getId();
		if (cardId == null || cardId.getId() == null) {
			logger.warn("action.card.id is null -> exiting");
			return null;
		}

		Card parentCard = null;
		if ((parentConditions.size() > 0) && (linkAttrFromParent != null)) {
			final List<Card> parents;
			if(BackLinkAttribute.class.isAssignableFrom(linkAttrFromParent.getType())) {
				parents = CardUtils.execListProject(linkAttrFromParent, cardId, 
						getQueryFactory(), getDatabase(), getUser());
			} else {
				parents = getParents(cardId);
			}
			final int count = (parents != null) ? parents.size() : 0;
			switch (count) {
			case 1: // the single ok variant
				parentCard = parents.get(0);
				break;
			case 0:
				logger.warn("No parent card found for the current one -> Conditions check for parent card skipped");
				break;
			default:
				logger.error("Multiple parents found for the current card "
							+ "but this is not allowed -> Conditions check for parent card skipped");
			}
		}
		
		List<Card> childCards = new ArrayList<Card>();
		if(!childrenConditions.isEmpty() && linkAttrToChildren != null) {
			if(BackLinkAttribute.class.isAssignableFrom(linkAttrToChildren.getType())) {
				childCards = CardUtils.execListProject(linkAttrToChildren, cardId, 
						getQueryFactory(), getDatabase(), getUser());
			} else {
				childCards = getChildren(cardId);
			}
		}

		if (checkChildCardsCondition(childrenConditions, childCards) && checkConditions(conditions, card) && checkConditions(parentConditions, parentCard)) {
			if (currentUser) {
				doWorkflowStep(card, getUser());
			} else {
				doWorkflowStep(card, getSystemUser());
			}
			logger.info(MessageFormat.format(MSG_CARD_0_MOVED_TO_NEW_STATE_SUCCESSFULLY, cardId.getId()));
		} else {
			logger.info(MessageFormat.format(MSG_CARD_0_DID_NOT_SATISFY_CONDITIONS, cardId.getId()));
		}

		// changeState.setCard( super.loadCardById(card.getId()) );

		return null;
	}
	
	private boolean checkChildCardsCondition(List<BasePropertySelector> conditions, List<Card> children) throws DataException {
		if(children == null){
			return true;
		}
		for(Card c : children) {
			if(!checkConditions(conditions, c))
				return false;
		}
		return true;
	}

	// private void doWorkflowStep(final Card card) throws DataException {
	// final ChangeState action = new ChangeState();
	// action.setWorkflowMove((WorkflowMove)
	// DataObject.createFromId(this.destWFM));
	// action.setCard(card);
	// final ActionQueryBase query = this.getQueryFactory().getActionQuery(
	// action);
	// query.setAction(action);
	// this.getDatabase().executeQuery(getSystemUser(), query);
	// }

	/**
	 * ��������� ������� ��� ��������. ������������� �������������� ������
	 * ���������� (��� ������������ operUser) �� ����� ��������: ���� ��������
	 * �� ����������� - �������� �����������, ���� �������� ����������� -
	 * �������������� ���������� ��� operUser.
	 * 
	 * @param card
	 * @param user
	 * @throws DataException
	 */
	private void doWorkflowStep(final Card card, UserData user)
			throws DataException {
		final WorkflowMove wfm = (WorkflowMove) DataObject.createFromId(this.destWFM);

		if (card.getId() != null) {
			// ��������� ����������� ���������� � ������������ �������
			// ��������...

			// �������� ������ �������� � �������, ���� �������� �� ����
			// ��������� � ������� ����� ��������� �������:

			final String sql = "select   c.card_id \n"
					+
					// " , c.template_id \n" +
					"		, c.locked_by \n"
					+ "		, c.status_id \n"
					+
					// " , wfm.wfm_id \n" +
					"		, wfm.from_status_id \n"
					+
					// " , wfm.to_status_id \n" +
					"		, coalesce( wfm.name_eng, wfm.name_rus) as name \n"
					+
					// " , wfm.action_code \n" +
					// " , wfm.workflow_id \n" +
					"from card c \n"
					+ "		join template t on t.template_id = c.template_id \n"
					+ "		join workflow_move wfm on wfm.workflow_id = t.workflow_id \n"
					+ "where c.card_id= ? and wfm.wfm_id = ? \n";

			final SqlRowSet rs = getJdbcTemplate().queryForRowSet(
							sql,
							new Object[] { card.getId().getId(), this.destWFM.getId() },
							new int[] { Types.NUMERIC, Types.NUMERIC });

			if (!rs.first()) { // ��� �� ����� ������...
				logger.error("Invalid card id " + card.getId().getId());
				throw new DataException("factory.list", new Object[] {card.getId().getId() });
			}
			final long cardStatus = rs.getLong("status_id");
			final long fromStatus = rs.getLong("from_status_id");
			final String wfmName = rs.getString("name");
			if (cardStatus != fromStatus) {
				logger.warn(MessageFormat.format(
						"Card {0} has status {1} that differs from start status {2} of wfm=''{3}''/{4}",
						new Object[] { card.getId().getId(),
						cardStatus, fromStatus,
						this.destWFM.getId(), wfmName })
					);
				// �������� ��������� �� � ��� ���������, ��� �������� ��������
				// �������
				if (failIfStatusIsOther) {
					logger.error("Card " + card.getId().getId() + " is at inconsequent state for workflowmove "
							+ this.destWFM.getId());
					throw new DataException("action.state.wrongstate", new Object[] { 
							this.destWFM.getId(),
							card.getId() + ", state: " + card.getState()
					});
				}
				logger.info("Card " + card.getId().getId() + " : no workflow move performed");
				return;
			}

			// ���������� � ���������� ��������...
			super.execAction(new LockObject(card.getId()));
		}

		try {
			final ChangeState action = new ChangeState();
			action.setWorkflowMove(wfm);
			action.setCard(card);
			super.execAction(action, user);
		} finally {
			if (card.getId() != null) {
				super.execAction(new UnlockObject(card.getId()));
			}
		}

	}

	private List<Card> getParents(ObjectId cardId) throws DataException {
		return getLinkedCards(cardId, true, linkAttrFromParent);
	}
	
	private List<Card> getChildren(ObjectId cardId) throws DataException {
		return getLinkedCards(cardId, false, linkAttrToChildren);
	}
	
	private List<Card> getLinkedCards(ObjectId cardId, boolean parent, ObjectId linkAttrId) throws DataException {
		final FetchChildrenCards action = new FetchChildrenCards();
		action.setCardId(cardId);
		action.setLinkAttributeId(linkAttrId);
		action.setReverseLink(parent);
		return CardUtils.execSearchCards(action, getQueryFactory(),
				getDatabase(), getSystemUser());
	}

	private boolean checkConditions(List<BasePropertySelector> conds, Card c)
			throws DataException {
		/*
		 * ������ �������� ������: attrList = (ListAttribute)
		 * card.getAttributeById( ((AttributeSelector) cond).attrId);
		 * attrList.getReference() = ObjectId(id='ADMIN_26973',
		 * type=com.apana.dbmi.model.Reference); attrList.getValue() =
		 * ReferenceValue( id=ObjectId(id=1433,
		 * type=com.apana.dbmi.model.ReferenceValue), active=false,
		 * children=null, ..., valueEn="No", valueRu="���")
		 */
		if (conds == null || c == null)
			return true;
		boolean cardFetched = false;
		for (BasePropertySelector cond : conds) {
			if (!cardFetched) {
				// ���������� �������� ���� ��� �� ������...
				if ((cond instanceof AttributeSelector)
						&& null == c
								.getAttributeById(((AttributeSelector) cond)
										.getAttrId())) {
					c = super.loadCardById(c.getId());
					cardFetched = true;
				}
			}
			final boolean ok = cond.satisfies(c);
			if (!ok) {
				logger.info("Card " + c.getId().getId()
						+ " did not satisfies codition {" + cond
						+ "}  -> no jump performed");
				return false;
			}
		}
		return true;
	}

	@Override
	public void setParameter(String name, String value) {
		if (name == null || value == null)
			return;
		// final int iStart = value.toLowerCase().indexOf("jbr.current_date");
		if (value.toLowerCase().indexOf("jbr.current_date") != -1) {
			final DateFormat suffix_date = new SimpleDateFormat("yyyy-MM-dd");
			final GregorianCalendar current = new GregorianCalendar();
			final String date_cur = suffix_date.format(current.getTime());
			// value = value.substring(0, iStart).concat(date_cur);
			value = value.replaceAll("jbr.current_date", date_cur);
		}

		super.setParameter(name, value);

		boolean assigned = true;
		Object result = null;
		if (PARAM_DEST_WFM.equalsIgnoreCase(name)) {
			this.destWFM = ObjectIdUtils.getObjectId(WorkflowMove.class, value,
					true);
			result = this.destWFM;
		} else if (  isMultiKey( name, PARAM_ATTR_TEST)) {
			try {
				final AttributeSelector selector =
						AttributeSelector.createSelector(value);
				if (selector != null)
					this.conditions.add(selector);
				result = selector;
			} catch (DataException ex) {
				logger.error(ex);
			}
		} else if ( isMultiKey( name, PARAM_PARENT_ATTR_TEST)) {
			try {
				final AttributeSelector selector = 
						AttributeSelector.createSelector(value);
				if (selector != null)
					this.parentConditions.add(selector);
				result = selector;
			} catch (DataException ex) {
				logger.error(ex);
			}
		} else if ( isMultiKey( name, PARAM_CHILDREN_ATTR_TEST)) {
			try {
				final AttributeSelector selector = 
						AttributeSelector.createSelector(value);
				if (selector != null);
					this.childrenConditions.add(selector);
				result = selector;
			} catch (DataException ex) {
				logger.error(ex);
			}
		} else if ( isMultiKey( name, PARAM_CARD_TEST)) {
			// "[(W*([W*;W*|W*]))++|W*]"
			final CardConditionSelector selector = createCardChecker(value,
					logger);
			if (selector != null)
				this.conditions.add(selector);
			result = selector;
		} else if (PARAM_LINK_ATTR_FROM_PARENT.equalsIgnoreCase(name)) {
			linkAttrFromParent = IdUtils.smartMakeAttrId(value, CardLinkAttribute.class);
		} else if (PARAM_LINK_ATTR_TO_CHILDREN.equalsIgnoreCase(name)) {
			linkAttrToChildren = IdUtils.smartMakeAttrId(value, CardLinkAttribute.class);
		} else if (PARAM_FAIL_IF_SRC_STATUS_IS_OTHER.equalsIgnoreCase(name)) {
			this.failIfStatusIsOther = Boolean.parseBoolean(value.trim());
		} else if (PARAM_GET_ACTIVE_CARD.equalsIgnoreCase(name)) {
			this.reflectionGetActiveCard = value.trim();
		} else if (PARAM_CUR_USER.equalsIgnoreCase(name)) {
			this.currentUser = Boolean.parseBoolean(value.trim());
		} else {
			assigned = false;
		}

		// ����������� ����������...
		if (assigned && logger.isDebugEnabled())
			logger.debug(MessageFormat.format(MSG_PARAMETER_ASSIGNED_3, name,
					value, result));
	}

	/**
	 * ��������� �������� �� name ������ ���� "keyBeginXXX"
	 * @param name
	 * @param keyBegin
	 * @return
	 */
	final static boolean isMultiKey(String name, String keyBegin) {

		// return (name != null) && name.equalsIgnoreCase(keyBegin);
		return (name != null) && (keyBegin != null) 
				&& name.toLowerCase().startsWith( keyBegin.toLowerCase() );
	}

	/**
	 * ������� ����� ���� �������� ���� ""
	 * 
	 * @param cfgStr
	 * @param logger
	 * @return
	 */
	public CardConditionSelector createCardChecker(String cfgStr, Log logger) {
		if (cfgStr == null || (cfgStr = cfgStr.trim()).length() < 1)
			return null;

		/*
		 * "classXXX(name1= x.y.z;name2=abc.def,abc2.def2)" result has 3
		 * element(s): [1] "classXXX" [2] "name1= x.y.z" [3]
		 * "name2=abc.def,abc2.def2"
		 */
		final String[] args = cfgStr.split("[(W*([W*;W*|W*]))++|W*]");
		if (args.length == 0) {
			logger.warn("Invalid parameter: " + cfgStr);
			return null;
		}

		final CardConditionSelector result = new CardConditionSelector();
		result.checkerClassName = args[0].trim();
		// ���������� ���������� ...
		for (int i = 1; i < args.length; i++) {
			final String[] pair = args[i].split("=");
			if (pair.length < 2) {
				logger.warn("Empty parameter skipped: " + args[i]);
				continue;
			}
			result.cfgMap.put(pair[0].trim(), pair[1].trim());
		}

		return result;
	}

	private class CardConditionSelector extends BasePropertySelector implements Serializable {

		private static final long serialVersionUID = -4542381276966306209L;
		private String checkerClassName;
		final Map<String, String> cfgMap = new HashMap<String, String>(3);

		/**
		 * @param propName
		 * @param value
		 */
		public CardConditionSelector() {
			super("", "");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.aplana.dbmi.service.impl.PropertySelector#satisfies(java.lang.Object)
		 */
		@Override
		public boolean satisfies(Object object) {
			boolean result = false;
			try {
				final CardChecker checker = createChecker();
				checker.setCard((Card) object);
				checker.setParameters(this.cfgMap);
				checker.checkCard(); // (!) ��������
				result = true;
			} catch (CardCheckException e) {
				logger.error("Checking fails: " + e.getCause().getMessage());
			} catch (Exception ex) {
				logger.error("Checking fails with ", ex);
			}
			return result;
		}

		/**
		 * @return
		 * @throws ClassNotFoundException
		 * @throws InstantiationException
		 * @throws IllegalAccessException
		 */
		private CardChecker createChecker()
				throws ClassNotFoundException, InstantiationException,
				IllegalAccessException {
			String className = this.checkerClassName;
			final int posDot = className.indexOf(".");
			if (posDot < 0) {
				// ����� ��� -> ���� ������������ ����� ��-���������, �.�.
				// �������
				final String curClass = this.getClass().getName();
				// �������� ���� � curClass �� ��������� ����� � ��������� ...
				className = curClass.substring(0, curClass.lastIndexOf("."))
						+ "." + className;
			}
			final Class<?> classChecker = Class.forName(className);
			final CardChecker checker = (CardChecker) classChecker
					.newInstance();
			if (checker instanceof ProcessorBase) {
				final ProcessorBase pb = (ProcessorBase) checker;
				pb.init(getCurrentQuery());
				pb.setBeanFactory(JumpStatePostProcessor.this.getBeanFactory());
				pb.setObject(JumpStatePostProcessor.this.getObject());
				pb.setAction(JumpStatePostProcessor.this.getAction());
				pb.setResult(JumpStatePostProcessor.this.getResult());
				pb.setCurExecPhase(getCurExecPhase());
			}
			return checker;
		}

	}
}
