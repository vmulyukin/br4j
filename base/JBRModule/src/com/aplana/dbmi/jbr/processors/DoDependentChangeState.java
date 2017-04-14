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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.LockableObject;
import com.aplana.dbmi.model.LongAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.CardLinkLoader;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.DatabaseClient;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.dbmi.service.impl.UserData;

public class DoDependentChangeState extends ProcessorBase implements Parametrized, DatabaseClient
{
	private static final long serialVersionUID = 1L;
	
	public static final String PARAM_LINK_ATTR = "linkAttr";
	public static final String PARAM_TARGET_STATE = "targetState";
	public static final String PARAM_STATES = "affectedStates";
	public static final String PARAM_EMPTY_ACTION = "ifEmpty";
	public static final String PARAM_MOVE_ID = "moveId";
	public static final String PARAM_PARENT_ATTR = "parentAttr";
	public static final String PARAM_STATE_CONDITION = "stateCondition";
	public static final String PARAM_UNLOCK_PARANT = "unlockParent";
	public static final String PARAM_FAIL_MSG = "failMessage";
	//���������� �������� ���������� ��� �������� ������� �������� changeState
	public static final String PARAM_SET_ATTR = "setAttr";
	
	protected static final String LINK_SEPARATOR = "@";
	protected static final String NAME_VALUE_SEPARATOR = "=";
	protected static final Pattern PARAM_REGEXP = Pattern.compile("(@[^@]+@)");

	public static final String[] ACTION_VALUES = { "nothing", "fail", "move" };

	private static final int ACTION_NONE = 0;
	private static final int ACTION_FAIL = 1;
	private static final int ACTION_MOVE = 2;

	protected ObjectId linkId;
	private ObjectId targetStateId;
	protected HashSet<ObjectId> sourceStateIds;

	private int emptyAction = ACTION_NONE;
	private ObjectId moveId;
	private ObjectId parentAttr;
	private ObjectId stateCondition;
	private List<ObjectId> linkAttrIds = new ArrayList<ObjectId>();	// ��������� ����������� ������ ������������� �� ������ �� ��������, ��������������� ��������� � �������, �� � �� ���, �������, ������ � ������� ����� ��������� ������� �����������

	private boolean unlockParent;

    private JdbcTemplate jdbcTemplate;
    private String failMessage;
    
    //Map �������-�������� ��� ��������� �������� ��������� � ������� ������ ������
    private Map<ObjectId, Object> attrValueForSetting = new HashMap<ObjectId, Object>();

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	@Override
	public Object process() throws DataException {
		if (linkId == null)
			throw new IllegalStateException("Mandatory parameter " + PARAM_LINK_ATTR + " not set");
		if (targetStateId == null)
			throw new IllegalStateException("Mandatory parameter " + PARAM_TARGET_STATE + " not set");

		Card actionCard = getActionCard();
		Card parent = getParentCard();

		//��������� ���������� �� ���� �������� �������, ���� ���������� �� ��������� status
		if (stateCondition!= null && !parent.getState().equals(stateCondition)){
			return null;
		}

		/*CardLinkAttribute attr = (CardLinkAttribute) parent.getAttributeById(linkId);
		if (attr == null)
			throw new DataException("jbr.linked.move.noattr",
					new Object[] { parent.getId().getId().toString(), "@attribute." + linkId.getId() });*/
		//Collection linked = getLinkedCards((CardLinkAttribute) parent.getAttributeById(linkId));
		final Collection<Card> linked = getDependentCards(parent);
		logger.info("Document " + parent.getId().getId() + ": " +
				linked.size() + " linked card(s) in " + linkId.getId());
		for (Card card : linked) {
			setAttributeValues(card, actionCard);
			processDependentCard(card, actionCard, sourceStateIds, targetStateId, parent);
		}
		if (linked.size() == 0) {
			switch (emptyAction) {
			case ACTION_FAIL:
				if (failMessage==null||failMessage.length()==0) {
					throw new DataException("jbr.linked.empty",
						new Object[] { parent.getId().getId().toString(), "@attribute" + linkId.getId() });
				} else {
					throw new DataException(failMessage);
				}
			case ACTION_MOVE:
				if (moveId == null)
					throw new IllegalStateException(PARAM_MOVE_ID + " should be defined when " +
							PARAM_EMPTY_ACTION + " is set to " + ACTION_VALUES[ACTION_MOVE]);
				doSafeChangeState(parent, (WorkflowMove) DataObject.createFromId(moveId));
				break;
			}
		}
		return getResult();
	}

	protected Card getActionCard() {
		Card actionCard = ((ChangeState) getAction()).getCard();
		return actionCard;
	}
	
	protected void setAttributeValues(Card card, Card actionCard) throws DataException {
		if(card.getId().equals(actionCard.getId()))
			return;
		for(ObjectId objId : attrValueForSetting.keySet()) {
			Attribute attr = card.getAttributeById(objId);
			if(attr == null) {
				continue;
			}
			if(TextAttribute.class.equals(objId.getType())
					|| StringAttribute.class.equals(objId.getType())) {
				String attrValue = (String) attrValueForSetting.get(objId);
				if(StringUtils.hasText(attrValue)) {
					Matcher matcher = PARAM_REGEXP.matcher(attrValue);
					while(matcher.find()) {
						String group = matcher.group();
						ObjectId id = IdUtils.smartMakeAttrId(group.replaceAll(LINK_SEPARATOR, ""), StringAttribute.class, false);
						Attribute copyAttr = actionCard.getAttributeById(id);
						if(copyAttr != null) {
							//TODO: ������� � ��������� �����
							if(TextAttribute.class.equals(id.getType())
									|| StringAttribute.class.equals(id.getType())) {
								attrValue = attrValue.replaceAll(group, ((StringAttribute) copyAttr).getValue());
							} else if(IntegerAttribute.class.isAssignableFrom(id.getType())) {
								attrValue = attrValue.replaceAll(group, String.valueOf(((IntegerAttribute) copyAttr).getValue()));
							} else {
								attrValue = attrValue.replaceAll(group, "");
							}
						}
					}
				}
				((StringAttribute) attr).setValue(attrValue);
			}
			
			lockObject(card);
			try {
				final OverwriteCardAttributes overwrite = new OverwriteCardAttributes();
				overwrite.setCardId(card.getId());
				overwrite.setAttributes(Collections.singletonList(attr));
				overwrite.setInsertOnly(false);
				
				ActionQueryBase query = getQueryFactory().getActionQuery(overwrite);
				query.setAction(overwrite);
				getDatabase().executeQuery(getOperUser(), query);
			} finally {
				unlockObject(card);
			}
		}
	}

	protected void processDependentCard(Card card, Card actionCard, HashSet<ObjectId> sourceStateIds, ObjectId targetStateId, Card parentCard) throws DataException {
		if (!targetStateId.equals(card.getState()) && (sourceStateIds == null || sourceStateIds.contains(card.getState())))
			doSafeChangeState(card, findMove(card));
	}

	protected Collection<Card> getDependentCards(Card baseCard) throws DataException {
		Collection<Card> res = null;
		if (BackLinkAttribute.class.equals(linkId.getType())) {
			res = getBackLinkedCards(linkId, baseCard.getId());
		} else {
			res = getLinkedCards((CardLinkAttribute) baseCard.getAttributeById(linkId));
		}
		
		if(!linkAttrIds.isEmpty() && res != null){
			for (ObjectId linkAttrId: linkAttrIds){
				res = getLinkCardsForAttr(res, linkAttrId);
				if (res == null || res.isEmpty()){
					logger.debug("No linked cards found. Exit.");
					return Collections.<Card>emptyList();
				}
			}
		}
		
		return res != null ? res : Collections.<Card>emptyList();
	}

	/**
	 * @return ������ ������ ������������, �� ����� �������� ��������� ������� � ����.
	 * @throws DataException
	 */
	protected UserData getOperUser() throws DataException {
		return getSystemUser();
	}

	protected Card getParentCard() throws DataException {
		Card parent = getActionCard();

		//��������� ������������ ��������
		if (parentAttr != null) {
			Collection<Card> cards = null;
			if(BackLinkAttribute.class.isAssignableFrom(parentAttr.getType())) {
				cards = getBackLinkedCards(parentAttr, parent.getId());
			} else {
				cards = getLinkedCards(parent.getId(), parentAttr);
			}
			if (!CollectionUtils.isEmpty(cards)) {
				parent = (Card)cards.toArray()[0];
			}
		}

		if (CardLinkAttribute.class.equals(linkId.getType())) {
			final CardLinkAttribute attr = (CardLinkAttribute) parent.getAttributeById(linkId);
			if (attr == null) {
				final Search search = CardUtils.getFetchAction(parent.getId(),
						new ObjectId[] { linkId, Card.ATTR_STATE, Card.ATTR_TEMPLATE });
				final List<Card> found = CardUtils.execSearchCards(search, getQueryFactory(), getDatabase(),
						getOperUser() );
				if (found == null || found.size() != 1)
					throw new DataException("jbr.linked.parent.fetch",
							new Object[] { parent.getId().getId().toString() });
				parent = found.get(0);
			}
		}
		return parent;
	}

	@SuppressWarnings("unchecked")
	protected Collection<Card> getLinkedCards(CardLinkAttribute attr) throws DataException {

		if (attr == null || attr.getLinkedCount() < 1)
			return Collections.emptyList();

		return CardLinkLoader.loadCardsByLink(attr, new ObjectId[] { Card.ATTR_STATE },
				getOperUser(), getQueryFactory(), getDatabase());
	}

	protected Collection<Card> getBackLinkedCards(ObjectId attrId, ObjectId cardId) throws DataException {
		final ListProject list = new ListProject();
		list.setAttribute(attrId);
		list.setCard(cardId);
		final List<SearchResult.Column> cols = new ArrayList<SearchResult.Column>(1);
		cols.add( CardUtils.createColumn(Card.ATTR_STATE));
		list.setColumns( cols);
		// return ((SearchResult) getDatabase().executeQuery(getSystemUser(), query)).getCards();
		return CardUtils.execSearchCards(list, getQueryFactory(), getDatabase(), getOperUser());
	}

	protected WorkflowMove findMove(Card card) throws DataException {
		return findMove(card, targetStateId);
	}

	protected WorkflowMove findMove(Card card, ObjectId dstStateId) throws DataException {
		final WorkflowMove wfm =
			CardUtils.findWorkFlowMove( card.getId(), dstStateId, getQueryFactory(), getDatabase(), getOperUser());
		if (wfm != null)
			return wfm;
		throw new DataException("jbr.linked.nomove",
			new Object[] { card.getId().getId().toString(), "@state." + dstStateId.getId() });
	}

	protected void doSafeChangeState(Card card, WorkflowMove wfm) throws DataException {
		lockObject(card);
		try {
			doChangeState(card, wfm);
		} finally {
			unlockObject(card);
		}
	}
	
	protected void lockObject(LockableObject obj) throws DataException {
		final LockObject lock = new LockObject(obj);
		ActionQueryBase query = getQueryFactory().getActionQuery(lock);
		query.setAction(lock);
		getDatabase().executeQuery(getOperUser(), query);
	}
	
	protected void unlockObject(LockableObject obj) throws DataException {
		final UnlockObject unlock = new UnlockObject(obj);
		ActionQueryBase query = getQueryFactory().getActionQuery(unlock);
		query.setAction(unlock);
		getDatabase().executeQuery(getOperUser(), query);
	}

	protected void doChangeState(Card card, WorkflowMove wfm) throws DataException {
		ChangeState move = new ChangeState();
		move.setCard(card);
		move.setWorkflowMove(wfm);
		ActionQueryBase query = getQueryFactory().getActionQuery(move);
		query.setAction(move);
		getDatabase().executeQuery(getOperUser(), query);
	}
	
	private Collection<Card> getLinkCardsForAttr(Collection<Card> cards, ObjectId linkAttrId) throws DataException {
		if (cards==null || cards.size()==0){
			return null;
		}
		List<Card> result = new ArrayList<Card>();
		for(Card card: cards){
			if (BackLinkAttribute.class.isAssignableFrom(linkAttrId.getType())){	
				result.addAll(getProjectCards(card.getId(), linkAttrId));
			}else if (CardLinkAttribute.class.isAssignableFrom(linkAttrId.getType())){
				result.addAll(getLinkedCards(card.getId(), linkAttrId));
			}
		}
		return result;
	}
	
	private Collection<Card> getProjectCards(ObjectId cardId, ObjectId attrId) throws DataException {
		final UserData user = getSystemUser();

		final ListProject action = new ListProject();
		action.setAttribute(attrId);
		action.setCard(cardId);

		final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		columns.add( CardUtils.createColumn(Card.ATTR_STATE));
		columns.add( CardUtils.createColumn(Card.ATTR_TEMPLATE));
		action.setColumns(columns);

		final List<Card> list = CardUtils.execSearchCards(action, getQueryFactory(), getDatabase(), user);
		return list;
	}
	
	@SuppressWarnings("unchecked")
	private List<Card> getLinkedCards(ObjectId cardId, ObjectId attrId) 
	{
		final List<?> cards = getJdbcTemplate().query(
				"select av.number_value, c.status_id, c.template_id \n" +
				"from attribute_value av \n" +
				"	join card c on c.card_id=av.number_value \n" +
				"where av.card_id=? and av.attribute_code=? \n", 
				new Object[]{cardId.getId(), attrId.getId()},
				new int[] { Types.NUMERIC, Types.VARCHAR },
				new RowMapper(){
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						final Card card = new Card();
						card.setId(rs.getLong(1));
						card.setState(new ObjectId(CardState.class, rs.getLong(2)));
						card.setTemplate(rs.getLong(3));
						return card;
					}
		});

		return (List<Card>)cards; 
	}	

	public void setParameter(String name, String value) {
		if (PARAM_LINK_ATTR.equalsIgnoreCase(name)) {
			String[] links = value.split(LINK_SEPARATOR);
			if (links.length==0)
				return;
			this.linkId = IdUtils.smartMakeAttrId(links[0], CardLinkAttribute.class, false);
			if(links.length>1){
				for(int i=1;i<links.length; i++){
					linkAttrIds.add(IdUtils.smartMakeAttrId(links[i], BackLinkAttribute.class, false));
				}
			}
			
		} else if (PARAM_TARGET_STATE.equalsIgnoreCase(name)) {
			targetStateId = ObjectId.predefined(CardState.class, value);
			if (targetStateId == null)
				try {
					targetStateId = new ObjectId(CardState.class, Long.parseLong(value));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(value +
							" is neither predefined nor physical card state id");
				}
		} else if (PARAM_STATES.equalsIgnoreCase(name)) {
			sourceStateIds = new HashSet<ObjectId>();
			String[] states = value.split(",");
			for (int i = 0; i < states.length; i++) {
				ObjectId stateId = ObjectId.predefined(CardState.class, states[i]);
				if (stateId == null)
					try {
						stateId = new ObjectId(CardState.class, Long.parseLong(states[i]));
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException(states[i] +
								" is neither predefined nor physical card state id");
					}
				sourceStateIds.add(stateId);
			}
		} else if (PARAM_EMPTY_ACTION.equalsIgnoreCase(name)) {
			for (int i = 0; i < ACTION_VALUES.length; i++) {
				if (ACTION_VALUES[i].equalsIgnoreCase(value)) {
					emptyAction = i;
					return;
				}
			}
			throw new IllegalArgumentException("Illegal " + PARAM_EMPTY_ACTION +
					" parameter value: " + value);
		} else if (PARAM_MOVE_ID.equalsIgnoreCase(name)) {
			moveId = ObjectId.predefined(WorkflowMove.class, value);
			if (moveId == null)
				try {
					moveId = new ObjectId(WorkflowMove.class, Long.parseLong(value));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(value +
							" is neither predefined nor physical card state id");
				}
		} else if (PARAM_PARENT_ATTR.equalsIgnoreCase(name)) {
			parentAttr = IdUtils.smartMakeAttrId(value, BackLinkAttribute.class, false);
		} else if (PARAM_STATE_CONDITION.equalsIgnoreCase(name)) {
			stateCondition = ObjectId.predefined(CardState.class, value);;
		} else if (PARAM_UNLOCK_PARANT.equalsIgnoreCase(name)) {
			if ("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value)) {
				unlockParent = true;
			}
		} else if (PARAM_FAIL_MSG.equalsIgnoreCase(name)) {
			failMessage = value.trim();
		} else if (PARAM_SET_ATTR.equalsIgnoreCase(name)) {
			String[] nameValue = value.split(NAME_VALUE_SEPARATOR);
			ObjectId attr = IdUtils.smartMakeAttrId(nameValue[0], StringAttribute.class, false);
			attrValueForSetting.put(attr, nameValue[1]);
		} else
			throw new IllegalArgumentException("Unknown parameter: " + name);
	}

	private UserData systemUser = null;
	@Override
	public UserData getSystemUser() throws DataException {
		if (systemUser == null) {
			systemUser = new UserData();
			systemUser.setPerson(getDatabase().resolveUser(Database.SYSTEM_USER));
			systemUser.setAddress("internal");
		}
		return systemUser;
	}
}