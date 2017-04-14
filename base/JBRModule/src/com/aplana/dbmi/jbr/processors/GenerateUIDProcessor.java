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
import java.util.Arrays;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataException;

/**
 * (2011/01/19)
 * ����������� �������� attrId �������� ������ UID (java.util.UUID.randomUUID()),
 * (��������������� ������� �������� � ���� �������� �������� �������� �� �������)
 * ���� ����������� ������ ��� StringAttribute/TextAttribute/IntegerAttribute.
 * ����� ����� ������ ��� post-process.
 * @author NGaleev
 */

public class GenerateUIDProcessor extends ProcessCard {

	/**
	 * (����, ����=null) ����� ��� ��� �������� �������� (������ � �����, ���� ����).
	 * ��. {@link IdUtils.smartMakeAttrId}
	 */
	private static final String PARAM_ATTR_ID = "attrId";

	/**
	 * (����, ����="true")
	 * true = ��������� ����������, ������ ���� ������� �������� �������� ������;
	 * false = ��������� ���������� �� �������� �� �������.
	 */
	private static final String PARAM_SET_IF_EMPTY_ONLY = "setIfEmptyOnly";
	private static final boolean DEFAULT_setIfEmptyOnly = true;

	private static final String MSG_CARD_0_HAS_ASSIGNED_ATTR_1_CFG_2 =
		"Card {0} contain assigned attribute ''{1}'' "
		+ "-> skipped UID assign due to config parameter ''{2}'' is true"
		;

	// ��������� ...
	private Card card;

	@Override
	public Object process() throws DataException {

		//��������� ��� ��������
		final String id = getParameter(PARAM_ATTR_ID, null);
		if (id == null) {
			logger.error("No attribute id parameter '"+ PARAM_ATTR_ID +"'specified. Exit.");
			return null;
		}

		final ObjectId currAttrId = IdUtils.smartMakeAttrId( id, StringAttribute.class, false);

		final ObjectId cardId = getCardId();
		if (cardId != null) {
			// NOTE: (RuSA) ����� ��������� ������ �� ����� getSystemUser(), �.�. � ������������ user ����� ������ �� ���� ����.
			card = super.getCard( getSystemUser(), currAttrId);
		} else if (getResult() instanceof Card) {
			logger.warn("The card is just created (most probably). I'll use it.");
			card = (Card) getResult();
		}
		if (card == null){
			logger.error("The card does not exist. Exit.");
			return null;
		}

		final Attribute attr = card.getAttributeById(currAttrId);
		if (attr == null) {
			logger.warn( MessageFormat.format( MSG_CARD_0_HAS_NO_ATTRIBUTE_1, card, currAttrId));
			return null;
		}

		// �������� ������� ��� ���������� ���������� �������� ��������...
		if (!chkConditions(attr))
			return null;

		// ��������� ID
		final String newUID = generateUID();

		// ���������� � ���������� ...
		if (setAttributeValue( attr, newUID) && cardId != null) {
			final OverwriteCardAttributes writer = new OverwriteCardAttributes();
			writer.setCardId(cardId);
			writer.setAttributes(Arrays.asList(attr));

			// NOTE: �������� ���� ��������� ���������� �� ����� getSystemUser(), �.�. � ������������ ����� �� ���� ����.
			// ��. super.saveCard/relockObjectBy
			boolean unlock = false;
			if (cardId != null) {
				execAction(new LockObject(cardId));
				unlock = true;
			}
			try {
				execAction(writer, getSystemUser());
			} finally {
				if (unlock)
					execAction(new UnlockObject(cardId));
			}		}

		return null;
	}

	/**
	 * ���������, ����� �� ��������� �������� ��������.
	 * @param attr
	 * @return true, ���� �������� ��������� ���������� � false �����.
	 */
	private boolean chkConditions(Attribute attr) {
		if (attr == null) return false;

		final boolean flagSetIfEmptyOnly
			= getBooleanParameter(PARAM_SET_IF_EMPTY_ONLY, DEFAULT_setIfEmptyOnly);

		if (flagSetIfEmptyOnly && !attr.isEmpty()) {
			logger.info( MessageFormat.format( MSG_CARD_0_HAS_ASSIGNED_ATTR_1_CFG_2,
					card, attr.getId(), PARAM_SET_IF_EMPTY_ONLY));
			return false;
		}

		return true; // ���������� ��������
	}

	/**
	 * @param destAttr �������, ��� �������� ��������� ����������. (!) ������
	 * ���� �������������� ������ ���� String/Text/Integer-��������.
	 * @param newValue ����� �������� ��������.
	 * @return true, ���� �������� �������� ��������� � false, �����.
	 * (!) ��������, ��� ������������� �� �� ����� �������� ��� ��� � ��������, �� ������������.
	 * TODO: (2011/01/21, RuSA) ������, ����� ����� ����� ����� ����� � IdUtils.
	 */
	protected boolean setAttributeValue( Attribute destAttr, String newValue) {
		if (destAttr == null)
			return false;

		if (newValue == null) {
			destAttr.clear();
			return true;
		}

		// TextAttribute ������� �� StringAttribute ...
		if (destAttr instanceof StringAttribute) {
			((StringAttribute) destAttr).setValue(newValue);
		} else if (destAttr instanceof IntegerAttribute) {
				((IntegerAttribute) destAttr).setValue( Integer.parseInt(newValue.trim()));
		} else { // �� �������������� ��� ...
			// 	"Card {0} contain attribute ''{1}'' with class ''{2}'' but supported classes are only ''{3}'' or ''{4}''";
			logger.error( MessageFormat.format( MSG_CARD_ATTRIBUTE_HAS_INVALID_CLASS_5,
					card, destAttr.getId(), destAttr.getClass(),
					StringAttribute.class, IntegerAttribute.class));
			return false;
		}

		return true;
	}

	/**
	 * ("��������" ����������) ��������� ������ UID.
	 * @return ��������������� ���������� ID.
	 */
	protected String generateUID()
	{
		return java.util.UUID.randomUUID().toString();
	}
}
