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
/**
 * 
 */
package com.aplana.dbmi.jbr.processors;

import java.util.List;

import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.UserData;

/**
 * @author DSultanbekov
 */
public class ForceSaveCardProcessor extends ProcessCard {
	private static final long serialVersionUID = 1L;
	
	/**
	 * �������-������ ��� ��������� ��������� ��������:
	 * 		���� null: �� ��������� ���������� ������ ������� ��������;
	 * 		���� ������ - �� �������� � ������� �������� ������ � ���������
	 * �� ���������������� ����������.
	 */
	private ObjectId linkAttrId;

	@Override
	public Object process() throws DataException 
	{
		final ObjectId activeCardId = getCardId();

		this.linkAttrId = IdUtils.smartMakeAttrId( 
						super.getParameter("linkAttrId", null),
						CardLinkAttribute.class
					);
		// ���� �� ����� �������-������, �� ������� ��� ����������� ������� ��������...
		final boolean isSaveCurrent = (linkAttrId == null);

		// ������������ �� ����� �������� ����� ��������� �������� ������-������
		// ��������. �������� ��������, ����� ����� ���� ������. 
		// UserData operRWUser = (isSaveCurrent) ? getUser() : getSystemUser();
		UserData operRWUser = getSystemUser();

		// ������ ������� �������� � ����� ������...
		Card activeCard;
		if (activeCardId != null) {
			activeCard = super.loadCardById(activeCardId, operRWUser);
		} else {
			// �������� ������ ���������...
			if (!(getResult() instanceof Card)){
				logger.error("The card does not exist. Exit.");
				return null;
			}
			logger.warn("The card is just created (most probably). I'll save it.");
			activeCard = (Card) getResult();
			operRWUser = getUser();
		}

		if (isSaveCurrent) { // ��������� ������ ������� ��������...
			saveCard( activeCard, operRWUser);
			reloadCard(operRWUser);
			return null;
		}

		/* 
		 * ���� ����� linkAttr, �� ���������� �������� ...
		 */
		final List<Card> linkedList = super.loadAllLinkedCardsByAttr(
				activeCardId, 
				activeCard.getAttributeById(linkAttrId),
				operRWUser);

		// ���������� ��������(��)
		if (linkedList != null) {
			for (Card cardItem : linkedList) {
				saveCard(cardItem, operRWUser);
			}
		}

		return null;
	}
}
