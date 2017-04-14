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
package com.aplana.dbmi.module.docflow;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.processors.ProcessCard;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.UserData;

/*
 *	����� ������������� ������ �������� �������� ����������� 
 *	��� �������� ��������� � ���������� ����������
 *  ���� �������� ������ �������, �� ������ ���������� �� ����� �������� ������������, ��� ��������� - �� ����� �������
 */
public class FillExecutorPostProcessor extends ProcessCard {
	private static final long serialVersionUID = 1L;
	
	public static final ObjectId EXECUTOR = ObjectId.predefined(PersonAttribute.class, "jbr.resolutionExecutor");
	/*
	 * ������������� �������� ��������, ���������� � ��������� person.
	 * ���� ��������� ���, �� ��������������� �������� �������� �����������.
	 */
	public static final String PERSON = "person";
	
	@Override
	public Object process() throws DataException {
		Card card = null;

		final ObjectId cardId = getCardId();//�������� id ��������� ��������
		
		//�������� ��������
		if (cardId != null) {
			ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
			query.setId(cardId);
			card = (Card) getDatabase().executeQuery(getSystemUser(), query);
			if (card == null){
				logger.error("The card does not exist. Exit.");
				return null;
			}
		} else {
			if (!(getResult() instanceof Card)){
				logger.error("The card does not exist. Exit.");
				return null;
			}
			logger.warn("The card is just created (most probably). I'll use it.");
			card = (Card) getResult();
		}   
		
		//�������� ������������
		final  UserData user= getUser();
		
		//�������� ���������� ������� ������������
		final Person person = user.getPerson();
		
		//������������� ������� "�����������" ��������
		if(person!=null)
			if (params.containsKey("person")) {
				ObjectId pers = ObjectId.predefined(PersonAttribute.class, params.get("person"));
				((PersonAttribute) card.getAttributeById(pers)).setPerson(person);
			} else {
				((PersonAttribute) card.getAttributeById(EXECUTOR)).setPerson(person);
			}
						
		//��������� ��������
		boolean unlock = false;
		if (card.getId() != null) {
			execAction(new LockObject(card), getSystemUser());
			unlock = true;
		}
		ObjectId savedId = null;
		try {
			final SaveQueryBase sq = getQueryFactory().getSaveQuery(card);
			sq.setObject(card);
			if (card.getId() != null) {
				savedId = (ObjectId) getDatabase().executeQuery( getSystemUser(), sq);
			} else {
				savedId = (ObjectId) getDatabase().executeQuery( getUser(), sq);
			}
		} catch (Exception ex) {
			logger.error("Exception saving card "+ card.getId()+ "\n" + ex);
			throw new DataException( "general.unique", new Object[] {card.getId()}, ex);
		} finally {
			if (unlock && savedId != null) {
				execAction(new UnlockObject(card), getSystemUser());
			}
		}
		return card;
	}
}