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
package com.aplana.dbmi.module.notif;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import com.aplana.dbmi.jbr.action.GetAssistants;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * �����, ����������� �������������� ��� ���������� ������ ����������� �����������
 * �� �����������. ������������ ��� ������������� � ����� <code>beans.xml</code>
 * � �������� �������� ���������, ������������ � �����
 * {@link NotificationBean#setRecipients(Collection)}.
 * 
 * <p>������������ ��������� ������ �������� {@link #setSourceGroup(RecipientGroup)}.
 * ������, ������������ � ���� �����, ���������� ��� ����������� ������� �������� ������,
 * ��� ������� ����� AssistantRecipients ��������� ����� ����������.
 * �� ���������, �������� ������ ����� ���������� � ������ �������������� ������.
 * ��� ������� ��� ����, ����� � ����� <code>beans.xml</code> �� ����� ����
 * �������� ���� � �� �� ������ ����������� ������ &nbsp; � �������� ����������������
 * �������� ��������� � ������ AssistantRecipients. ����� ���������, ������, ����� ����
 * ��������� ������ {@link #setIncludeSource(boolean)}.
 * 
 * @author apirozhkov
 */
public class AssistantRecipients extends DataServiceClient implements RecipientGroup
{
	private RecipientGroup sourceGroup;
	private boolean includeSource = true;
	
	/**
	 * ������������� ������ ��� ����������� �������� ������ �����������.
	 * 
	 * ���� ����� ����������� ������ ���� ������ ����� �������������� ������.
	 * 
	 * @param group ������, ������������ �������� ������ �����������.
	 * 		�� ����� ���� null
	 */
	public void setSourceGroup(RecipientGroup group) {
		this.sourceGroup = group;
	}
	
	/**
	 * ������������� ������� ������������� ��������� �������� ������ �����������
	 * � ������ �������������� ������.
	 * 
	 * @param flag false, ���� �������� ������ �� ������ ���������� � ��������������
	 */
	public void setIncludeSource(boolean flag) {
		this.includeSource = flag;
	}

	@Override
	public Collection discloseRecipients(NotificationObject object) {
		if (sourceGroup == null)
			throw new IllegalStateException("sourceGroup must be set before use");
		Collection<Person> originPersons = sourceGroup.discloseRecipients(object);
		if (originPersons == null || originPersons.size() == 0)
			return originPersons;
		
		try {
			HashMap<ObjectId, Person> map = new HashMap<ObjectId, Person>();
			for (Iterator<Person> itr = originPersons.iterator(); itr.hasNext(); ) {
				Person person = itr.next();
				map.put(person.getId(), person);
			}
			
			GetAssistants search = new GetAssistants();
			search.setChiefIds(map.keySet());
			
			ActionQueryBase query = getQueryFactory().getActionQuery(search);
			query.setAction(search);
			Collection<ObjectId> assistantIds =
					(Collection<ObjectId>) getDatabase().executeQuery(getSystemUser(), query);
			
			ArrayList<Person> result = new ArrayList<Person>(originPersons.size() * 2);
			if (includeSource) {
				result.addAll(originPersons);
			}
			
			ObjectQueryBase personQuery = getQueryFactory().getFetchQuery(Person.class);
			for (Iterator<ObjectId> itr = assistantIds.iterator(); itr.hasNext(); ) {
				ObjectId id = itr.next();
				Person person;
				if (!map.containsKey(id)) {
					personQuery.setId(id);
					try {
						person = (Person) getDatabase().executeQuery(getSystemUser(), personQuery);
					} catch (Exception e) {
						logger.error("Error fetching person " + id.getId() + "; skipped", e);
						continue;
					}
				} else if (!includeSource)
					person = map.get(id);
				else
					continue;
				result.add(person);
			}
			
			return result;
		} catch (DataException e) {
			logger.error("Error fetching assistants for recipient group", e);
			return includeSource ? originPersons : Collections.emptyList();
		}
	}

}
