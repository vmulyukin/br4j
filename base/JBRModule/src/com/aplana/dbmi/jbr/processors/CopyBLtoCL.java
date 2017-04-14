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
import java.util.List;

import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.UserData;

public class CopyBLtoCL extends ProcessCard {
	private ObjectId toParentId;
	private ObjectId toBaseId;
	private ObjectId parentToBaseId;

	@Override
	public Object process() throws DataException {

		Card parentCard;
		if(BackLinkAttribute.class.isAssignableFrom(toParentId.getType())) {
			parentCard = getBackLinkCard(getCardId(), toParentId);
		} else {
			parentCard = getLinkedCardById(getCard(), toParentId);
		}
		Card baseCard = null;
		
		//���� �� ������ ����� ����� ���. ���������� � ���-����������, �� ����� ���. ��������� ����� � ���-���������� (������� ��� ������� ����������� ��������� 1255)
		if (parentToBaseId == null) {
			baseCard = parentCard;
		} else if(BackLinkAttribute.class.isAssignableFrom(parentToBaseId.getType())) {
			baseCard = getBackLinkCard(parentCard.getId(), parentToBaseId);
		} else {
			baseCard = getLinkedCardById(parentCard, parentToBaseId);
		}

		ObjectId cardId = getCardId();

		getJdbcTemplate().update(
			"delete from attribute_value where card_id = ? and attribute_code = ?",
			new Object[] {cardId.getId(), toBaseId.getId()},
			new int[] { Types.NUMERIC, Types.VARCHAR }
		);


		if((cardId != null) && (toBaseId != null) && (baseCard != null)){ 
			getJdbcTemplate().update(
				"insert into attribute_value (card_id, attribute_code, number_value) values (?, ?, ?)",
				new Object[] {cardId.getId(), toBaseId.getId(), baseCard.getId().getId()},
				new int[] { Types.NUMERIC, Types.VARCHAR, Types.NUMERIC }
			);	
		}

		/*
		CardLinkAttribute attr = (CardLinkAttribute) card.getAttributeById(toBaseId);
		
		attr.addLinkedId(baseCard.getId());
		
		super.saveCard(card, getSystemUser());
		*/
		return null;
	}
	
	private Card getBackLinkCard(ObjectId cardId, ObjectId backLinkId) throws DataException {
		UserData user = getSystemUser();

		ListProject action = new ListProject();
		action.setAttribute(backLinkId);
		action.setCard(cardId);

		final List<Card> list = CardUtils.execSearchCards(action, getQueryFactory(), getDatabase(), user);
		return (list == null) ? null : list.get(0); 
	}

	@Override
	public void setParameter(String name, String value) {
		if ("toParent".equals(name)) {
			toParentId = IdUtils.smartMakeAttrId(value, BackLinkAttribute.class, false);
		} else if ("toBase".equals(name)) {
			toBaseId = IdUtils.smartMakeAttrId(value, CardLinkAttribute.class, false);
		} else if ("parentToBase".equals(name)) {
			parentToBaseId = IdUtils.smartMakeAttrId(value, BackLinkAttribute.class, false);
		}
	}

}