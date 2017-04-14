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
package com.aplana.dbmi.service.impl.processors;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.util.DateUtils;
import com.aplana.dbmi.service.CardVersionException;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.DatabaseClient;
import com.aplana.dbmi.service.impl.ProcessorBase;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Types;
import java.util.Date;

/**
 * ���������, ����������� �������� �� ���������� ������ �������� � ������ � � �� 
 * @author desu
 */
public class CheckCardForVersion extends ProcessorBase implements DatabaseClient {

	private static final long serialVersionUID = 1L;
	private JdbcTemplate jdbc;

	@Override
	public Object process() throws DataException {
		Card card = getCard();

		// ���� �������� ���, �� � ��������� ������ �� ����
		if (card == null)
			return null;
		// ���� ������ �������� ���������� �� ���, ��� � ��, ���������� ����������
		if (!checkCardForVersion(card)) {
			throw new CardVersionException("card.version.not.equals", new Object[] {card.getId().getId()});
		}

		return null;
	}

	public void setJdbcTemplate(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbc;
	}

	/**
	 * �������� �������� �� ���������� ������� ������ � � ������ � ��
	 *
	 * @param card - ������� ������ ��������
	 * @return true - ������ ���������, false - �� ���������
	 */
	private boolean checkCardForVersion(Card card) {
		DateAttribute changed = card.getAttributeById(Attribute.ID_CHANGE_DATE);
		// ID �������� ���, ���� ���� ��������� ���, ������ �������� ������ ������, �������� ������ ������������� ��� ����������
		if (card.getId() == null || changed == null || changed.getValue() == null) {
			return true;
		}
		Date dt = changed.getValue();
		dt = DateUtils.toUTC(dt);
		/*
		 * ���������, ����� ���� ��������� � �� ���� ������ ��� ����� ���� ��������� � ������, ���� � �� ���� �������� �� ������.
		 * ���� � �� ���� �������� �� ������, �� ������ ����� �������� ������ ��� ��� ������ � ������ � ���� ������ � �������� ������������ � �������.
		 * ���� � ���������� "dt" ���������� ���� � ������� ������ 1398209645785 �������������, �� � SQL ������� - ��� ���������������� � �������� 1398209645.785 ������.
		 * �������� ���� PostgreSQL ������ � Unix TimeStamp, �� ��� ����� ���� � ������ �������� �������� � ��������� ��� � ������� 1398209645.785, ��� � � ������� 1398209645.78597.
		 * ��� ��������� ���� � PostgreSQL ������� �������� ����������� ������ � ��������� �������� 1398209645.785000 ������, ������� ������ ������ �� ������� � ����� ���������� false.
		 * ������� "date_trunc('milliseconds', av.date_value)" �������� �������� ������ �� ������� 1398209645.785 � ��������� � SQL ������� ����������� ���������.
		 */
		final int count = getJdbcTemplate().queryForInt("SELECT count(card_id) FROM card c WHERE c.card_id = ?" +
						" AND ( EXISTS ( SELECT 1 FROM attribute_value av WHERE av.card_id=c.card_id AND av.attribute_code = 'CHANGED' AND date_trunc('milliseconds', av.date_value) <= ? ) " +
						"OR NOT EXISTS ( SELECT 1 FROM attribute_value av WHERE av.card_id=c.card_id AND av.attribute_code = 'CHANGED' ) )",
				new Object[] {card.getId().getId(), dt},
				new int[] {Types.NUMERIC, Types.TIMESTAMP});
		if (logger.isDebugEnabled()) {
			logger.debug("SELECT count(card_id) FROM card c WHERE c.card_id = " + card.getId().getId().toString() +
					" AND ( EXISTS ( SELECT 1 FROM attribute_value av WHERE av.card_id=c.card_id AND av.attribute_code = 'CHANGED' AND date_trunc('milliseconds', av.date_value) <= '" + dt.toString() + "' ) " +
					"OR NOT EXISTS ( SELECT 1 FROM attribute_value av WHERE av.card_id=c.card_id AND av.attribute_code = 'CHANGED' ) )");
		}
		return count > 0;
	}

	/**
	 * �������� �������� �������� ��� ������� ����������.
	 * @return (������) �������� ��� null, ���� ��� �������� �������� � �� �����
	 * id ��� �������� ���������� (��������, ��� ��������).
	 * @throws DataException
	 */
	public Card getCard() throws DataException {
		// ��������� �������� �������� � ������ ��������� (����� ���� � ��� ��
		// ��������� ��� ��������� � ������ ���� ���������:
		// Store/ChangeState, pre/post.
		Card card = null;
		if (getObject() instanceof Card) {
			card = (Card) getObject();
		} else if (getAction() instanceof ChangeState) {
			card = ((ChangeState) getAction()).getCard();
		} else if (getResult() instanceof Card) {
			card = (Card) getResult();
		}

		return card;
	}
}