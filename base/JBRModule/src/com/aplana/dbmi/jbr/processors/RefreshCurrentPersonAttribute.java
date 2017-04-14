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

import java.sql.Types;
import org.springframework.dao.EmptyResultDataAccessException;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.Parametrized;

public class RefreshCurrentPersonAttribute extends AbstractCardProcessor 
	implements Parametrized 
{
	private ObjectId personAttributeId;
	private boolean updateIfNullOnly = false; // если true, то обновляем дату, только если значение пусто

	@Override
	public Object process() throws DataException {

		final ObjectId cardId = getCardId();

		if (personAttributeId == null) {
			logger.warn( "parameter personAttributeId is not set -> exiting");
			return null;
		}

		final Person person = getUser().getPerson();

		/* обновление в активной карточке... */
		final Card card = this.getCurrentCard();
		if (card == null) { logger.debug( "Card is null -> exiting "); return null; }
		final PersonAttribute personAttr = (PersonAttribute) card.getAttributeById(personAttributeId);
		if (personAttr == null) {
			logger.warn( "Active card with id="+ cardId +" does not contain person attribute '"+ personAttributeId + "', only DB update will be performed");
		} else {
			if ( !updateIfNullOnly || personAttr.isEmpty()) {
				logger.debug( "Active card with id="+cardId +": setting value of attribute \'"+ personAttributeId+ "\' to " + person.toString());
				personAttr.setPerson(person);
			} else
				logger.warn( "Active card with id="+cardId +": value of attribute \'"+ personAttributeId
						+ "\' WAS NOT CHANGED due to parameter 'updateIfNullOnly' is true and value is NOT empty");
		}

		/* Обновление данных БД... */
		if (cardId == null) {
			logger.info("Active card has no id (new?) -> no changes made to DB");
		} else {
			logger.debug("Checking if this processor is applicable for the given card: cardId = " + cardId.getId() + ", personAttributeId = " + personAttributeId.getId());
			try {
				getJdbcTemplate().queryForInt(
//						"select 1 from template_attribute ta where " +
//						"ta.attribute_code = ? and exists (select 1 from card c " +
//						"where c.template_id = ta.template_id and c.card_id = ?)",
						"select 1 \n" +
						"from card c" +
						"	join template t on t.template_id = c.template_id \n" + 
						"	join template_block tb on tb.template_id = t.template_id \n"+ 
						"	join attribute a on a.block_code=tb.block_code \n"+
						"where a.attribute_code = ? and c.card_id = ?",
						new Object[] { personAttributeId.getId(), cardId.getId() },
						new int[] {Types.VARCHAR, Types.NUMERIC }
				);
			} catch (EmptyResultDataAccessException e) {
				logger.debug( "No processing required as there is no such attribute \'"+ personAttributeId+ "\' in the template of the given card "+ cardId.getId() );
				return null;
			}

			// Если updateIfNullOnly = true, то проверяем, что бы поле было пустым
			if (updateIfNullOnly) {
				logger.debug("Checking if person is empty card: cardId = " + cardId.getId() + ", personAttributeId = " + personAttributeId.getId());
				Object value = null;
				try {
					value = getJdbcTemplate().queryForObject(
							"select av.number_value \n"+
							"from attribute_value av \n"+
							"where av.attribute_code = ? and av.card_id = ?",
							new Object[] { personAttributeId.getId(), cardId.getId() },
							new int[] {Types.VARCHAR, Types.NUMERIC },
							Integer.class
					);
				} catch(Exception ex) {
					value = null;
				}
				if (value != null) {
					logger.debug("Person value already assigned in card: cardId = " 
							+ cardId.getId() + ", personAttributeId = " 
							+ personAttributeId.getId() + " -> no changes performed"
					);
					return null;
				}
			}
			logger.debug("Removing old date value from card: cardId = " + cardId.getId() + ", personAttributeId = " + personAttributeId.getId());
			execAction(new LockObject(cardId));
			try {
				getJdbcTemplate().update(
					"delete from attribute_value where card_id = ? and attribute_code = ?",
					new Object[] {cardId.getId(), personAttributeId.getId()},
					new int[] { Types.NUMERIC, Types.VARCHAR }
				);

				logger.debug( "Card" + cardId.getId() + ": db-setting value of attribute \'"+ personAttributeId+ "\' to " + person.getId().getId());
				getJdbcTemplate().update(
					"insert into attribute_value (card_id, attribute_code, number_value) values (?, ?, ?)",
					new Object[] {cardId.getId(), personAttributeId.getId(), person.getId().getId()},
					new int[] { Types.NUMERIC, Types.VARCHAR, Types.NUMERIC }
				);
			} finally {
				execAction(new UnlockObject(cardId));
			}
		}

		return null;
	}

	/**
	 * @return получить активную карточку процессора
	 */
	private Card getCurrentCard() {

		if (getObject() instanceof Card)
			return (Card) getObject();

		if (getAction() instanceof ChangeState)
			return ((ChangeState) getAction()).getCard();

		return null;
	}

	public void setParameter(String name, String value) {
		if (name.equals("personAttributeId")) {
			personAttributeId = ObjectIdUtils.getObjectId(PersonAttribute.class, value, false);
		} else if (name.equals("updateIfNullOnly")) {
			updateIfNullOnly =  value.equals("true");
		}
	}
}