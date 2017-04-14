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
package com.aplana.dbmi.service.impl.query;

import java.sql.Types;
import java.util.Date;
import java.util.Iterator;

import com.aplana.dbmi.action.CloneCard;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.MaterialAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.TemplateBlock;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * Query used to perform {@link CloneCard} action 
 */
public class DoCopyCard extends ActionQueryBase implements WriteQuery
{
	/**
	 * Creates new {@link Card} on base of existing card and returns it as result. <br>
	 * NOTE: this new card is not persisted and should be saved manually later if necessary.
	 * @return newly created {@link Card} object.
	 */
	public Object processQuery() throws DataException
	{
		final CloneCard action = (CloneCard) getAction();
		final ObjectId cardId = action.getOrigId();

		// �������� �������� ��������...
		final ObjectQueryBase subQuery = getQueryFactory().getFetchQuery(Card.class);
		subQuery.setId(cardId);
		final Card card = (Card) getDatabase().executeQuery(getUser(), subQuery);

		card.clearId(); // (!) ��� ���������� ������� ����� ��� ����������...

		// ������� ���� ����� ...
		removeServBlocks( card);

		// ������� ��, ��� �� ���� ���������� ...
		clearRestrictedAttributes( card, action);

		// ������ �������� ��-��������� ...
		setAttributeDefaults(card, action);

		return card;
	}

	private void removeServBlocks(Card card) {
		for ( Iterator itr = card.getAttributes().iterator(); itr.hasNext(); ) {
			final TemplateBlock block = (TemplateBlock) itr.next();
			if (block == null) continue;
			if (AttributeBlock.ID_REST.equals(block.getId()) ||
				AttributeBlock.ID_REMOVED.equals(block.getId())) {
				itr.remove();
				break;
			}
		}
	}

	/**
	 * ������ ���������.
	 * @param destCard
	 */
	private void setAttributeDefaults( Card destCard, CloneCard action)	{
		// ��������� ������ ...
		long initialState = getJdbcTemplate().queryForLong(
				"select w.initial_status_id from workflow w \n" +
				"where w.workflow_id = \n" +
				"	(select t.workflow_id from template t where t.template_id=?)",
				new Object[] { destCard.getTemplate().getId()},
				new int[] {Types.NUMERIC}
		);		
		destCard.setState(CardState.getId(initialState));

		// ��������� ������ ��������...

		final Attribute material = destCard.getAttributeById(Attribute.ID_MATERIAL);
		if (!canBeCopied(material, action)) {
			if (material instanceof MaterialAttribute) {
				((MaterialAttribute) material).setMaterialType(MaterialAttribute.MATERIAL_NONE);
			}
		}

		/*card.setFileName(null);
		card.setUrl(null);*/

		final Attribute author = destCard.getAttributeById(Attribute.ID_AUTHOR);
		if (author instanceof PersonAttribute)
			((PersonAttribute) author).setPerson(getUser().getPerson());

		final Attribute dtCreate = destCard.getAttributeById(Attribute.ID_CREATE_DATE);
		if (dtCreate instanceof DateAttribute)
			((DateAttribute) dtCreate).setValue(new Date());

		final Attribute dtChange = destCard.getAttributeById(Attribute.ID_CREATE_DATE);
		if (dtChange instanceof DateAttribute)
			((DateAttribute) destCard.getAttributeById(Attribute.ID_CHANGE_DATE)).setValue(null);

		final Attribute attr = destCard.getAttributeById(Attribute.ID_FILE_SIZE);
		if (!canBeCopied(attr, action)) {
			if (attr instanceof IntegerAttribute) {
				((IntegerAttribute) attr).setValue(0);
			}
		}
	}

	/**
	 * ��������� ������ ���������� �������.
	 * @param attr
	 * @param action
	 * @return true, ���� ����, false �����.
	 */
	private static boolean canBeCopied(Attribute attr, CloneCard action)
	{
		if ( 	attr == null 
				|| attr.getId() == null 
				|| attr.getId().getId() == null)
			return false;
		
		// ����������� ������� (?)
		if (action.getDisabledAttrIds().contains(attr.getId()))
			return false;

		// ����������� ������� (?)
		if (action.getEnabledAttrIds().contains(attr.getId()))
			return true;

		// ����������� ��� (?)
		return ( (action.getDisabledTypes() == null)
				|| !action.getDisabledTypes().contains(attr.getType()));
	}

	/**
	 * �������� ��������� � action ��������.
	 * @param card
	 * @param action
	 */
	private void clearRestrictedAttributes(Card card, CloneCard action) {

		int copied = 0;
		int cleared = 0;
		for (Iterator itrBlock = card.getAttributes().iterator(); itrBlock.hasNext(); ) 
		{
			final AttributeBlock block = (AttributeBlock) itrBlock.next();
			for (Iterator itrAttr = block.getAttributes().iterator(); itrAttr.hasNext(); ) {
				final Attribute srcAttr = (Attribute) itrAttr.next();
				if (canBeCopied(srcAttr, action)) {
					copied++;
					logger.debug("Kept attribute '"+ srcAttr.getId() +"' at new card");
				} else { // ������ ������� �� ����� ��������
					// itrAttr.remove();
					srcAttr.clear();
					cleared++;
					logger.info("Cleared restricted attribute '"+ srcAttr.getId() +"' from new card");
				}
			}
		}
		logger.debug( "For NEW card "+ copied + " attribute(s) has been kept, "
				+ cleared + " cleared");
	}

}
