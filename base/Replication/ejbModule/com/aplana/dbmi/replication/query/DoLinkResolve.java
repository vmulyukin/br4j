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
package com.aplana.dbmi.replication.query;

import com.aplana.dbmi.model.*;
import com.aplana.dbmi.replication.action.LinkResolver;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.query.WriteQuery;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class DoLinkResolve extends ActionQueryBase implements WriteQuery {
	private static final long serialVersionUID = 1L;

	@Override
	public Object processQuery() throws DataException {
		LinkResolver<?> action = getAction();
		return resolveLink(action.getCardId(), action.getLink());
	}

	public List<Object> resolveLink(ObjectId cardId, String link) throws DataException {
		List<Object> result = new ArrayList<Object>();
		String[] linkParts = link.split("\\.");
		if (linkParts.length > 0) {
			result = resolveOneLink(linkParts, 0, cardId);
		}
		return result;
	}

	private List<Object> resolveOneLink(String[] linkParts, int currentItem, ObjectId cardId) throws DataException {
		List<Object> result = new ArrayList<Object>();
		String linkPart = linkParts[currentItem];

		if (Card.ATTR_STATE.getId().equals(linkPart)) {
			Long cardState = getCardState(cardId);
			if (cardState != null) {
				ObjectId stateId = new ObjectId(CardState.class, cardState);
				result.add(stateId);
			}
		} else if (Card.ATTR_TEMPLATE.getId().equals(linkPart)) {
			Long template = getCardTemplate(cardId);
			if (template != null) {
				ObjectId templateId = new ObjectId(Template.class, template);
				result.add(templateId);
			}
		} else {
			Attribute attribute;
			if (linkPart.startsWith("^")) {
				linkPart = linkPart.substring(1);
				BackLinkAttribute fakeAttribute = new BackLinkAttribute();
				fakeAttribute.setLinkSource(new ObjectId(CardLinkAttribute.class, linkPart));
				attribute = fakeAttribute;
			} else {
				ObjectQueryBase getAttributeQuery = getQueryFactory().getFetchQuery(Attribute.class);
				getAttributeQuery.setId(new ObjectId(Attribute.class, linkPart));
				attribute = getDatabase().executeQuery(getUser(), getAttributeQuery);
			}

			if (attribute instanceof StringAttribute) {
				String value = getStringValue(cardId, attribute);
				if (value != null && value.length() > 0) {
					result.add(value);
				}
			} else if (attribute instanceof CardLinkAttribute) {
				List<ObjectId> links = getCardLinkValue(cardId, attribute);
				if (links != null) {
					for (ObjectId objectId : links) {
						if (linkParts.length > currentItem + 1) {
							result.addAll(resolveOneLink(linkParts, currentItem + 1, objectId));
						} else {
							result.add(objectId);
						}
					}
				}
			} else if (attribute instanceof PersonAttribute) {
				List<ObjectId> personCards = getPersonValues(cardId, attribute);
				if (personCards != null) {
					for (ObjectId personCardId : personCards) {
						if (linkParts.length > currentItem + 1) {
							result.addAll(resolveOneLink(linkParts, currentItem + 1, personCardId));
						} else {
							result.add(personCardId);
						}
					}
				}
			} else if (attribute instanceof BackLinkAttribute) {
				List<ObjectId> parentCards = getBackLinkedValues(cardId, attribute);
				if (parentCards != null) {
					for (ObjectId parentCardId : parentCards) {
						if (linkParts.length > currentItem + 1) {
							result.addAll(resolveOneLink(linkParts, currentItem + 1, parentCardId));
						} else {
							result.add(parentCardId);
						}
					}
				}
			}
		}
		return result;
	}

	protected Long getCardState(ObjectId cardId) {
		try {
			long cardState = getJdbcTemplate().queryForLong("select status_id from card where card_id = ?",
					new Object[] { cardId.getId() }, new int[] { Types.NUMERIC });
			if (cardState != 0) {
				return cardState;
			}
		} catch (IncorrectResultSizeDataAccessException ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("There is no state for card for cardId [" + cardId.getId() + "]");
			}
		}
		return null;

	}
	
	protected Long getCardTemplate(ObjectId cardId) {
		try {
			long cardTemplate = getJdbcTemplate().queryForLong("select template_id from card where card_id = ?",
					new Object[] { cardId.getId() }, new int[] { Types.NUMERIC });
			if (cardTemplate != 0) {
				return cardTemplate;
			}
		} catch (IncorrectResultSizeDataAccessException ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("There is no template_id for card for cardId [" + cardId.getId() + "]");
			}
		}
		return null;

	}

	private String getStringValue(ObjectId cardId, Attribute attribute) {
		String value = null;
		try {
			value = (String) getJdbcTemplate().queryForObject(
					"select string_value from attribute_value where card_id = ? and attribute_code = ?",
					new Object[] { cardId.getId(), attribute.getId().getId() },
					new int[] { Types.NUMERIC, Types.VARCHAR }, String.class);
		} catch (IncorrectResultSizeDataAccessException ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("There is no value with code [" + attribute.getId().getId() + "] for cardId ["
						+ cardId.getId() + "]");
			}
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	private List<ObjectId> getCardLinkValue(ObjectId cardId, Attribute attribute) {
		List<ObjectId> cardIds = getJdbcTemplate().query(
				"select number_value from attribute_value where card_id = ? and attribute_code = ?",
				new Object[] { cardId.getId(), attribute.getId().getId() }, new int[] { Types.NUMERIC, Types.VARCHAR },
				new RowMapper() {

					@Override
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						return new ObjectId(Card.class, rs.getLong(1));
					}

				});
		return cardIds;
	}

	@SuppressWarnings("unchecked")
	private List<ObjectId> getPersonValues(ObjectId cardId, Attribute attribute) {
		List<ObjectId> cardIds = getJdbcTemplate().query(
				"select card_id from person where person_id in (\n\tselect number_value from attribute_value av\n"
						+ "\twhere av.card_id = ? and attribute_code = ?)",
				new Object[] { cardId.getId(), attribute.getId().getId() }, new int[] { Types.NUMERIC, Types.VARCHAR },
				new RowMapper() {

					@Override
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						long id = rs.getLong(1);
						if (id != 0) {
							return new ObjectId(Card.class, rs.getLong(1));
						} else {
							return null;
						}
					}
				});
		while (cardIds.remove(null));
		return cardIds;
	}

	@SuppressWarnings("unchecked")
	private List<ObjectId> getBackLinkedValues(ObjectId cardId, Attribute attribute) {
		ObjectId linkId = ((BackLinkAttribute) attribute).getLinkSource();
		List<ObjectId> cardIds = getJdbcTemplate().query(
				"select card_id from attribute_value where number_value = ? and attribute_code = ?",
				new Object[] { cardId.getId(), linkId.getId() }, new int[] { Types.NUMERIC, Types.VARCHAR },
				new RowMapper() {
					@Override
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						return new ObjectId(Card.class, rs.getLong(1));
					}
				});

		return cardIds;
	}
}
