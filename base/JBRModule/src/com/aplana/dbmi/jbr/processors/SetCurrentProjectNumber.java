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
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;

/*
 * ��� �������� �������� ������� �������� "����� �������" �� ������� project_number ������������� �� 1. 
 * ��� ������� ������������� �������� "����� �������" ��������.
 *  ���� �������� ������ �������, �� ������ ���������� �� ����� �������� ������������, ��� ��������� - �� ����� �������
 */
public class SetCurrentProjectNumber extends ProcessCard {
	
	// ID �������� "����� �������"
	public static final ObjectId PROJECT_NUMBER = ObjectId.predefined(IntegerAttribute.class, "jbr.projectNumber");

	// ������������ �������� �� ��������� ����� 1
	public static final String PARAM_INCREMENT = "increment";
	public static final int DEFAULT_INCREMENT = 1;

	@SuppressWarnings("unchecked")
	@Override
	public Object process() throws DataException {
		
		// ���������� �������� ���������� - ����� �������� ��������� ��� ��� �������, ���� ������ �������� �� ���������
		Integer increment = Integer.parseInt( getParameter(PARAM_INCREMENT, String.valueOf(DEFAULT_INCREMENT)));

		// ���������� ��������
		Card card = null;
		final ObjectId cardId = null;//super.getCardId();
		if (cardId != null) {
			ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
			query.setId(cardId);
			card = (Card) getDatabase().executeQuery(getSystemUser(), query);
			if (card == null)
			{
				logger.error("The card does not exist. Exit.");
				return null;
			}
		} 
		else
		{
			if (!(getResult() instanceof Card))
			{
				logger.error("The card does not exist. Exit.");
				return null;
			}
			logger.warn("The card is just created. I'll use it.");
			card = (Card) getResult();
		}

		// ���������� ������, �� �������� ��������� ��������
		Object template = card.getTemplate().getId();
		
		synchronized (SetCurrentProjectNumber.class)
		{
			Integer currentNumber = new Integer(0);
			
			// � �� ���� �������� �������� ������ ������� ��� ���������� �������
			final List<Integer> currentNumberValues = super.getJdbcTemplate().queryForList(
					"select current_number from project_number where template_id = ?",
					new Object[]{template},
					new int[]{Types.NUMERIC},
					Integer.class);
			
			// ���������, ��� ����� ���� ��������
			if (currentNumberValues.size()>1){
				logger.warn("For template " + card.getTemplateNameEn() + " current project number is multivalued. Exiting.");
				return null;
			}
			
			// ����������� �������� �������� ������ ������� �� �������� ����������
			String sql;
			if (!currentNumberValues.isEmpty()){
				currentNumber = currentNumberValues.get(0);
				sql = "update project_number set current_number = ? " +
						"where template_id = ?"; 
			} else {
				logger.warn("For template " + card.getTemplateNameEn() + " current project number is not set. I suppose that it has value 0.");
				sql = "insert into project_number (current_number, template_id) " +
						"values (?, ?)";
			}
			
			currentNumber = currentNumber.intValue() + increment;
			
			final int count = getJdbcTemplate().update(sql, 
					new Object[]{currentNumber, template},
					new int[]{Types.NUMERIC, Types.NUMERIC});
			if (count == 1)
				logger.info("Current number for template " + card.getTemplateNameEn() + " was successfully changed to " + currentNumber);
			else
				logger.error("Current number for template " + card.getTemplateNameEn() + " was not changed to " + currentNumber);
	
			// ����������� �������� �������� ������ ������� (currentNumber) �������� "����� �������" ��������
			IntegerAttribute projectNumberAttr = (IntegerAttribute)card.getAttributeById(PROJECT_NUMBER);
			projectNumberAttr.setValue(currentNumber);
		}
		
		saveAction(card);
		
		return card;
	}
	 
	// ����� ��������� ������ �� ����������
	private Object saveAction(DataObject dataObject) throws DataException
	{
		SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(dataObject);
		saveQuery.setObject(dataObject);
		return getDatabase().executeQuery((dataObject instanceof Card?(((Card)dataObject).getId()!=null?getSystemUser():getUser()):getSystemUser()), saveQuery);	
	}
}
