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

import org.springframework.jdbc.core.JdbcTemplate;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.DatabaseClient;
import com.aplana.dbmi.service.impl.ProcessorBase;

/**
 * ��������� ���������� ��� ����-���� ���������� �������� ���������� ������.
 * ��������� � ���� � ������� person �������� card_id ������� ���������.
 * ������ ���� ������� � 2 ��������� ������, � 2 ������ ����������.
 * ��������� ���������� ��� ������ ���������� ������������ ������ ��� ��������� ������.
 * @author desu
 */
public class SyncPersonCard extends ProcessorBase implements DatabaseClient {
	private static final long serialVersionUID = 1L;
	private JdbcTemplate jdbc;
	
	@Override
	public Object process() throws DataException {
		Card card = (Card)getObject();
		PersonAttribute ownerAtt = card.getAttributeById(ObjectId.predefined(PersonAttribute.class, "jbr.person.owner"));
		Person owner = ownerAtt.getPerson();
		if (owner != null && owner.getId() != null) {
			jdbc.update("UPDATE person SET card_id=? WHERE person_id=?",
					new Object[] { card.getId().getId(), owner.getId().getId() });
		}
		
		return null;
	}

	@Override
	public void setJdbcTemplate(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}
}
