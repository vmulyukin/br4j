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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.jbr.action.GetAssistants;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ObjectLockedException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.QueryFactory;

/**
 * ����������������������� ���������. ������������ 
 *
 */
public class SetMeToMyResolutions extends SetCurrentPersonTo {
	private Set<ObjectId> whitePersonList = new HashSet<ObjectId>();
	
	/**
	 * ��������������� ����� ��������� �� �������� � ������� � �������� ������ ������ 
	 * ������� ������������ ��� ��� ���������.
	 * @param �ard ����������� ��������
	 * @return true ���� �������� �������� ��� ������, false � ��������� ������
	 */
	@Override
	protected boolean isEligibleToWrite(Card card){
		PersonAttribute author = (PersonAttribute)card.getAttributeById(Attribute.ID_AUTHOR);
		if (author == null || author.getValues() == null){
			logger.error("Card #"+card.getId().getId()+" does not have attribute AUTHOR !");
			return false;
		}
		Set<ObjectId> personIds = new HashSet<ObjectId>(2);
		for (Person person : (Collection<Person>) author.getValues())
			personIds.add(person.getId());
		if (whitePersonList.containsAll(personIds))
			return true;
		return false;
	}

	@Override
	public Object process() throws DataException {
		final QueryFactory qf = getQueryFactory();
		final Database d = getDatabase();
		ActionQueryBase aq = null;
		// ������ ���������� �������� ������������ � "�����" ������...
		try {
			GetAssistants action = new GetAssistants();
			action.setChiefIds(Collections.singletonList(getUser().getPerson().getId()));
			aq = qf.getActionQuery(action);
			aq.setAction(action);			
			whitePersonList.addAll((List<ObjectId>)d.executeQuery(getSystemUser(), aq));
		} catch (DataException e) {
			logger.error("Can not get assistants list of person "+getUser().getPerson().getId());
		}
		// ... � ������ ������������ ���� ��
		whitePersonList.add(getUser().getPerson().getId());
		return super.process();
	}
}
