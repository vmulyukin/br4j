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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.utils.StrUtils;

/**
 * This class allows to prevent executing of an action on a card
 * when any of the cards linked to it are in some specific state(s).
 * This class is intended to be used as a preprocessor for a card state change event,
 * though it can also be used for any card-related action or operation
 * such as storing and even reading. It is also possible to use it
 * as a postprocessor, but in this case unnecessary database operations
 * operation will be performed followed by transaction rollback.
 * 
 * Source card's attribute id and prohibited linked cards' states shall be
 * configured through preprocessor's parameters, {@link #PARAM_LINK_ATTR_ID} and
 * {@link #PARAM_DENIED_STATES}, corresponding.
 * If any linked cards in prohibited state(s) are found, processor
 * generates an exception, thus preventing further action's executing.
 * Concrete error message can also be configured through a parameter,
 * {@link #PARAM_MESSAGE_KEY}.
 * 
 * @author apirozhkov
 */
public class DenyActionByLinkedCardStates extends AbstractCardProcessor implements Parametrized {
	private static final long serialVersionUID = 1L;
	/**
	 * Processor's parameter - ID of attribute which is used for retrieving linked cards.
	 * <b>Mandatory.</b>
	 * 
	 * Shall contain either database attribute ID (not recommended) or its synonym
	 * (declared in <code>objectids.properties</code>). It can prefixed by
	 * attrubute's type, followed by a colon. Allowed types are as follows:
	 * <ul>
	 * <li><code>C</code> or <code>link</code> - plain card link attribute (default);
	 * <li><code>B</code> or <code>backLink</code> - back link attribute;
	 * <li><code>E</code> or <code>typedLink</code> - typed card link attribute.
	 * </ul>
	 * Example: <code>backLink: jbr.main.doc</code>
	 */
	public static final String PARAM_LINK_ATTR_ID = "linkAttr";
	/**
	 * Processor's parameter - IDs of linked cards' states, which prevent
	 * execution of action. Exactly one of this and {@link #PARAM_ALLOWED_STATES}
	 * must be defined.
	 * 
	 * Shall contain comma or semicolon separated list of card state IDs.
	 * ID can be presented either in database form (not recommended) or as a synonym
	 * (declared in <code>objectids.properties</code>).
	 * 
	 * Example: <code>registration, consideration</code>
	 */
	public static final String PARAM_DENIED_STATES = "deniedStates";
	/**
	 * Processor's parameter - IDs of linked cards' states, which allow
	 * execution of action. Exactly one of this and {@link #PARAM_DENIED_STATES}
	 * must be defined.
	 * 
	 * Format is similar to {@link #PARAM_DENIED_STATES}.
	 */
	public static final String PARAM_ALLOWED_STATES = "allowedStates";
	/**
	 * Processor's parameter - Whether the action will be denied in case of
	 * empty link. Allowed values: <code>false</code> (default) or <code>true</code>.
	 */
	public static final String PARAM_DENY_EMPTY = "denyEmpty";
	/**
	 * Processor's parameter - a key of message for exception, which will be
	 * thrown to prevent action's execution. The message itself shall be defined
	 * with the mechanism provided in DataException (currently -
	 * <code>exceptions_*.properties</code> files). 
	 */
	public static final String PARAM_MESSAGE_KEY = "messageKey";
	
	private ObjectId linkAttrId;
	private HashSet<ObjectId> states = new HashSet<ObjectId>();
	private boolean denyList;
	private boolean denyEmpty = false;
	private String messageKey = "jbr.processor.actiondenied";
	
	@Override
	public Object process() throws DataException {
		int denied = 0;
		if (BackLinkAttribute.class.equals(linkAttrId.getType())) {
			ListProject search = new ListProject();
			search.setAttribute(linkAttrId);
			search.setCard(getCardId());
			SearchResult.Column col = new SearchResult.Column();
			col.setAttributeId(Card.ATTR_STATE);
			search.setColumns(Collections.singletonList(col));
			ActionQueryBase query = getQueryFactory().getActionQuery(search);
			query.setAction(search);
			final SearchResult linked = (SearchResult) getDatabase().executeQuery(getSystemUser(), query);
			if (denyEmpty && linked.getCards().size() == 0)
				throw new DataException(messageKey);
			for (Iterator<?> itr = linked.getCards().iterator(); itr.hasNext(); ) {
				Card item = (Card) itr.next();
				if (denyList == states.contains(item.getState()))
					denied++;
			}
			/*denied = getJdbcTemplate().queryForInt(
					"SELECT COUNT(c.*) FROM attribute_value a " +
					"INNER JOIN card c ON a.card_id=c.card_id " +
					"WHERE a.number_value=? AND a.attribute_code=(" +
						"SELECT option_value FROM attribute_option " +
						"WHERE attribute_code='" + linkAttrId.getId() + "' " +
						"AND option_code='" + AttributeOptions.LINK + "') " +
					"AND c.status_id " + getStatesCondition(),
					new Object[] { getCardId().getId() });
					//new int[] { Types.VARCHAR });*/
		} else {	// card link or typed card link attribute
			if (denyEmpty && 0 == getJdbcTemplate().queryForInt(
					"SELECT COUNT(c.*) FROM attribute_value a " +
					"WHERE a.card_id=? AND a.attribute_code='" + linkAttrId.getId() + "'",
					new Object[] { getCardId().getId() }))
				throw new DataException(messageKey);
			denied = getJdbcTemplate().queryForInt(
					"SELECT COUNT(c.*) FROM attribute_value a " +
					"INNER JOIN card c ON a.number_value=c.card_id " +
					"WHERE a.card_id=? AND a.attribute_code='" + linkAttrId.getId() + "' " +
					"AND c.status_id " + getStatesCondition(),
					new Object[] { getCardId().getId() });
					//new int[] { Types.VARCHAR });
		}
		if (denied > 0)
			throw new DataException(messageKey);
		return null;
	}
	
	private String getStatesCondition() {
		StringBuffer buf = new StringBuffer();
		if (!denyList)
			buf.append("NOT ");
		buf.append("IN (").append(ObjectIdUtils.numericIdsToCommaDelimitedString(states)).append(")");
		return buf.toString();
	}

	public void setParameter(String name, String value) {
		if (PARAM_LINK_ATTR_ID.equalsIgnoreCase(name))
			linkAttrId = decodeAttributeId(value);
		else if (PARAM_DENIED_STATES.equalsIgnoreCase(name)) {
			parseStates(value);
			denyList = true;
		} else if (PARAM_ALLOWED_STATES.equalsIgnoreCase(name)) {
			parseStates(value);
			denyList = false;
		} else if (PARAM_DENY_EMPTY.equalsIgnoreCase(name)) {
			denyEmpty = Boolean.parseBoolean(value);
		} else if (PARAM_MESSAGE_KEY.equalsIgnoreCase(name))
			messageKey = value;
		else
			throw new IllegalArgumentException("Unknown parameter: " + name);
	}

	private ObjectId decodeAttributeId(String value) {
		String type = null;
		String[] parts = value.split("\\s*:\\s*");
		if (parts.length > 1) {
			type = parts[0];
			value = parts[1];
		}
		ObjectId id = null;
		if (type == null || Attribute.TYPE_CARD_LINK.equals(type) ||
				CardLinkAttribute.class.equals(AttrUtils.getAttrClass(type)))
			id = ObjectId.predefined(CardLinkAttribute.class, value);
		if (id == null && (type == null || Attribute.TYPE_BACK_LINK.equals(type) ||
				BackLinkAttribute.class.equals(AttrUtils.getAttrClass(type))))
			id = ObjectId.predefined(BackLinkAttribute.class, value);
		if (id == null && (type == null || Attribute.TYPE_TYPED_CARD_LINK.equals(type) ||
				TypedCardLinkAttribute.class.equals(AttrUtils.getAttrClass(type))))
			id = ObjectId.predefined(TypedCardLinkAttribute.class, value);
		if (id == null && type == null)
			id = new ObjectId(CardLinkAttribute.class, value);	// default: card link attribute
		return id;
	}
	
	private void parseStates(String value) {
		if (states.size() > 0)
			throw new IllegalStateException("Multiple " + PARAM_ALLOWED_STATES + " and/or " +
					PARAM_DENIED_STATES + " parameters not supported");
		String[] ids = value.split("\\s*[,;]\\s*");
		for (int i = 0; i < ids.length; i++)
			states.add(ObjectIdUtils.getObjectId(CardState.class, ids[i], true)/*.getId()*/);
	}
}
