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

import java.text.MessageFormat;
import java.util.*;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.jbr.util.AttributeSelector;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.BasePropertySelector;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.service.impl.Validator;

import org.springframework.dao.IncorrectResultSizeDataAccessException;

/**
 * <B> ��������� ��������� �������� ����������� ��������� � �������� ����� ��
 * �������� ��������.</B>
 * 
 * <P>
 * ��� ������������� ���������� ����������� ���������� ����:
 * 
 * <pre>
 * The attribute "{0}" has invalid value in the child card: "{1}"
 * </pre>
 * 
 * ��� ���������� �������� ���������� �������� �� �����������. <br>
 * �������� �������� �������� �����������:
 * <ul>
 * <li>{@link #PARAM_ATTR_TEST} - attr_test � query.xml</li>
 * <li>{@link #PARAM_LINK_ATTR} - linkAttr � query.xml</li>
 * <li>{@link #PARAM_IGNORED_STATES} - ignoredStates � query.xml</li>
 * <BR>
 * <B>������:</B><BR>
 * 
 * <pre>
 * {@code
 * 
 * <pre-process class="CheckAttribute">
 * 	<parameter name="linkAttr" value="jbr.resolutions"/>
 * 	<parameter name="attr_test" value="jbr.AssignmentExecutor#NULL"/>
 * 	<parameter name="ignoredStates" value="trash;closedviaparent;poruchcancelled"/>
 * </pre-process>
 * 
 * } </pre>
 * <P>
 * 
 */
public class CheckAttribute extends ProcessCard implements Validator {

	private static final long serialVersionUID = 1L;

	/**
	 * <B>������������ �������� {@value} ������ ������� ��� �������� ��������
	 * �����.</B>
	 * <P>
	 * �������� ��������� �������, ������� ����� �����������
	 * ���������.��������:
	 * 
	 * <pre>
	 * {@code 
	 * <parameter name="attr_test" value="jbr.AssignmentExecutor#NULL"/>
	 * <parameter name="attr_test" value="jbr.incoming.oncontrol=jbr.incoming.control.no"/> 
	 * <parameter name="attr_test" value="jbr.incoming.oncontrol#jbr.incoming.control.yes"/> 
	 * <parameter name="attr_test" value="list:JBR_IMPL_ONCONT#1432"/> 
	 * <parameter name="attr_test" value="jbr.deliveryItem.method=modeDeliveryMEDO"/>
	 * }
	 * </pre>
	 * <P>
	 * 
	 * @see com.aplana.dbmi.jbr.util.AttributeSelector
	 */
	public static final String PARAM_ATTR_TEST = "attr_test";

	/**
	 * <B>������������ �������� {@value} ��������� �� ������� � ������� �����,
	 * ����� ������� ��� ������� � �������� ������ (cardLink-�������).</B>
	 * <P>
	 * ��������:
	 * 
	 * <pre>
	 * {@code
	 * <parameter name="linkAttr" value="jbr.resolutions"/>
	 * }
	 * </pre>
	 * <P>
	 */
	public static final String PARAM_LINK_ATTR = "linkAttr";

	/**
	 * <B>�������� {@value} ��������� ������� �������� ����, � ������� �� �����
	 * ��������� �������� ���������.</B>
	 * <P>
	 * ������� ������������� ����� ";". ��������:
	 * 
	 * <pre>
	 * {@code
	 * <parameter name="ignoredStates" value="draft;cancelled"/>	
	 * }
	 * </pre>
	 * 
	 * ���� �������� �� ������, ����������� ��� �������� �����
	 * <P>
	 */
	public static final String PARAM_IGNORED_STATES = "ignoredStates";

	/**
	 * �������� ��������� ��� �������� msgid ������ � ���������� �� ������ ��� ���������.
	 * ���� msgid - �� ����� �������� ��� DataException � ����� ����������� (����� 
	 * ������������� ��� ��� ������������):
	 * 	{0} = id ��������� ��������
	 * 	{1} = id ���������� ��������
	 * 	{2} = �������� ���������� ��������
	 * ������ �� ��� ./Model/ejbModule/nls/messages_xxx.properties 
	 */
	public static final String PARAM_NLS_MSGID_1CARD_2ATTR_3VAL = "nls_err_msgid_arg3";
	public static final String DEFAULT_PARAM_NLS_MSGID_ARG3 = "jbr.card.check.attribute";

	private final List<BasePropertySelector> conditions = new ArrayList<BasePropertySelector>();
	private ObjectId targetAttr;
	private Set<ObjectId> ignoredStateIds;
	private String nls_err_msgId_arg3 = DEFAULT_PARAM_NLS_MSGID_ARG3;

	public static final String PARAM_REVERSE_ROLES_CONDITION = "reverseRolesCondition";
	private boolean reverseRolesConditon = false;

	public static final String PARAM_CHECK_IF_USER_HAS_ROLES = "checkIfUserHasRole";
	private Set<ObjectId> checkedRoles = new HashSet<ObjectId>();

	@SuppressWarnings("unchecked")
	@Override
	public Object process() throws DataException {
		// check User Roles
		boolean hasSatisfiedRole = false;

		if (checkedRoles == null || checkedRoles.isEmpty()) {
			hasSatisfiedRole = true;
		} else {
			UserData curUser = getUser();
			Person person = loadPerson(curUser.getPerson().getId());

			Collection<Role> userRoles = person != null ? person.getRoles() : new ArrayList<Role>();

			for (Role role : userRoles) {
				ObjectId cRole = role.getSystemRole().getId();
				if (checkedRoles.contains(cRole)) {
					hasSatisfiedRole = true;
					break;
				}
			}

			if (reverseRolesConditon) {
				hasSatisfiedRole = !hasSatisfiedRole;
			}
		}

		if (hasSatisfiedRole) {
			final Card card = getCard();
			List<Card> linkedCards = null;
			if (targetAttr == null) {
				logger.debug("analizing currect card " + card.getId());
				if (getObject() instanceof Card || card.getId() == null)
					// ��� ���������� �������� ��� � �������� ��� id -> ���������� ������ ��� �������� ...
					linkedCards = Arrays.asList(card);
				else // ��������� �������� �� �� � ������� ���������� ...
					linkedCards = getCheckingCardAttr(Arrays.asList(card.getId()));
			} else {
				// ������ �������� �� ������ �������� �������� ...
				final CardLinkAttribute attr = card.getCardLinkAttributeById(targetAttr);
				if (attr == null) {
					logger.warn(MessageFormat.format(MSG_CARD_0_HAS_NO_ATTRIBUTE_1, card.getId(), targetAttr));
				} else {
					logger.debug("analizing cards via attribute " + attr.getId() + ": [" + attr.getLinkedIds() + "]");
					linkedCards = getCheckingCardAttr(attr.getIdsLinked());
				}
			}

			if (linkedCards == null || linkedCards.isEmpty()) {
				logger.warn("no card for check ->  check not performed, supposed to be 'check successfull'");
			} else {
				for (Card aCard : linkedCards) {
					if ((ignoredStateIds != null) && ignoredStateIds.contains(aCard.getState())) {
						logger.info("Card " + aCard.getId()
								+ " state " + aCard.getState()
								+ " is listsed among ignored states and won't be proccessed");
						continue;
					}
					boolean attrFlagOR = false;
					ObjectId attrId = null;
					for (BasePropertySelector cond : conditions) {
						if (cond.satisfies(aCard)) {
							attrFlagOR = true;
							logger.info("Card " + aCard.getId() + " satisfies condition " + cond);
							break;
						}
						if (attrId == null && (cond instanceof AttributeSelector)) {
							attrId = ((AttributeSelector) cond).getAttrId();
						}
					}

					if (!attrFlagOR) {
						logger.error("Card " + aCard.getId()
								+ " last checked attribute " + attrId + "\n"
								+ " fails check: all theese conditions are FALSE \n"
								+ getPrintableInfo(conditions));
						final Attribute attr = aCard.getAttributeById(attrId);
						final String msgId = (this.nls_err_msgId_arg3 != null)
								? this.nls_err_msgId_arg3
								: DEFAULT_PARAM_NLS_MSGID_ARG3;
						final Object cardId = (aCard.getId() != null ? aCard.getId().getId() : null);
						final String attrValue = (attr != null) ? attr.getStringValue() : "null";
						throw new DataException(msgId, new Object[]{cardId, attrId, attrValue});
					}
				}
			}
			logger.warn("Checking attributes in cards [" + ObjectIdUtils.numericIdsToCommaDelimitedString(linkedCards) + "] completed with true");
		}

		return null;
	}

	private String getPrintableInfo(List<BasePropertySelector> cond) {
		if (cond == null) return "";
		final StringBuilder result = new StringBuilder();
		int i = 0;
		for (BasePropertySelector item : cond) {
			++i;
			result.append(MessageFormat.format("\t[{0}]\t{1}\n", i, item.toString()));
		}
		return result.toString();
	}

	protected List<Card> getCheckingCardAttr( Collection<ObjectId> cardIds
			) throws DataException 
	{
		if (cardIds == null || cardIds.isEmpty())
			return null;

		final Search search = new Search();
		search.setByCode(true);

		// ������ Id �������� ...
		search.setWords( ObjectIdUtils.numericIdsToCommaDelimitedString(cardIds) );

		// ������������ ������� ...
		final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();

		CardUtils.addColumns(columns, Card.ATTR_STATE, Attribute.ID_NAME);

		for (BasePropertySelector condition : conditions) {
			CardUtils.addColumns(columns, ((AttributeSelector) condition).getAttrId());
		}

		search.setColumns(columns);

		final List<Card> linkedCards = CardUtils.execSearchCards(search, getQueryFactory(), getDatabase(), getSystemUser());
		return linkedCards;
	}

	@Override
	public void setParameter(String name, String value) {

		if (PARAM_ATTR_TEST.equalsIgnoreCase(name)) {
			try {
				String[] val = value.split(";");
				for(int i = 0; i < val.length; i++){
					final AttributeSelector selector = AttributeSelector.createSelector(val[i]);
					if (selector != null)
						this.conditions.add(selector);
				}
			} catch (DataException e) {

				e.printStackTrace();
			}
		} else if (PARAM_LINK_ATTR.equalsIgnoreCase(name)) {
			targetAttr = ObjectIdUtils.getObjectId(CardLinkAttribute.class,
					value, false);
		} else if (PARAM_IGNORED_STATES.equalsIgnoreCase(name)) {
			ignoredStateIds = IdUtils.makeStateIdsList(value);
		} else if (PARAM_NLS_MSGID_1CARD_2ATTR_3VAL.equalsIgnoreCase(name)) {
			nls_err_msgId_arg3 = (value != null) ? value.trim() : nls_err_msgId_arg3;
		} else if (PARAM_REVERSE_ROLES_CONDITION.equalsIgnoreCase(name)) {
			reverseRolesConditon = Boolean.parseBoolean(value);
		} else if (PARAM_CHECK_IF_USER_HAS_ROLES.equalsIgnoreCase(name)) {
			String[] stringRoles = value.trim().split(";");
			checkedRoles = new HashSet<ObjectId>();
			for (String role : stringRoles) {
				checkedRoles.add(ObjectId.predefined(SystemRole.class, role.trim()));
			}
		} else
			super.setParameter(name, value);
	}

	// ���� ��� �������� ��� ������ ��������� � DataException
	private Object[] message(String name, ObjectId attr) {
		final String colName = attr.toString().substring( attr.toString().indexOf(":") + 1);
		final boolean isRuLocale = (new Locale("ru", "RU")).equals(
					ContextProvider.getContext().getLocale()
				);
		final String sql = "select "
			+ ( isRuLocale ? "attr_name_rus" : "attr_name_eng")+ 
			" from attribute where attribute_code='" + colName + "'";
		Object attr_info = colName; // by default
		try {
			attr_info = getJdbcTemplate().queryForObject(sql, String.class);
		} catch (IncorrectResultSizeDataAccessException ex) {
			logger.error("no attribute found by attribute_code= '"+ colName+ "'", ex);
		}

		return new Object[] { attr_info, name };
	}

	protected Person loadPerson(ObjectId id) throws DataException {
		final ObjectQueryBase query = getQueryFactory().getFetchQuery(Person.class);
		query.setId(id);
		return (Person) getDatabase().executeQuery(getSystemUser(), query);
	}
}