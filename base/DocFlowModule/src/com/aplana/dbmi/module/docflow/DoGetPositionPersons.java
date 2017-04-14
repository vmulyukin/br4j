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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.QueryFactory;

/**
 * ���������� ������ Action GetPositionPersons. ��������� ������ ������� ������������ ��������� �, 
 * �����������, ���������� � �������������.
 * @author larin
 *
 */
public class DoGetPositionPersons extends ActionQueryBase {
	private static final long serialVersionUID = 1L;
	
	private static final ObjectId INT_PERSON_TEMPLATE_OBJECT_ID = 
		ObjectId.predefined(Template.class, "jbr.internalPerson");
	private static final ObjectId PERSON_POSITION_ATTRIBUTE_ID = 
		ObjectId.predefined(CardLinkAttribute.class, "jbr.person.pos");
	private static final ObjectId PERSON_DEPARTMENT_ATTRIBUTE_ID = 
		ObjectId.predefined(CardLinkAttribute.class, "jbr.personInternal.department");
	
		
	public Object processQuery() throws DataException {
		GetPositionPersons action = (GetPositionPersons) getAction();
		QueryFactory queryFactory = getQueryFactory();		
		
		//�������� ������ ������� ����������
		ObjectQueryBase fetchQuery = queryFactory.getFetchQuery(Template.class);
		fetchQuery.setId(INT_PERSON_TEMPLATE_OBJECT_ID);		
		Template template = 
			(Template)getDatabase().executeQuery(getUser(), fetchQuery);
 
		//���� ��� ���������� ������� � ��������� ����������
		Search search = new Search();
		ArrayList<Template> templates = new ArrayList<Template>();
		templates.add(template);
		search.setTemplates(templates);
		search.setByAttributes(true);
		
		search.addCardLinkAttribute(PERSON_POSITION_ATTRIBUTE_ID, 
				action.getPositionId());
		
		//���� �������� ������������� �� ��������� ��������� �������
		if (action.getDepartmentId() != null){
			search.addCardLinkAttribute(PERSON_DEPARTMENT_ATTRIBUTE_ID, 
					action.getDepartmentId());			
		}

		//�������� �������� ������ ����������
		ActionQueryBase query = queryFactory.getActionQuery(search);
		query.setAction(search);
		SearchResult result = (SearchResult)getDatabase().executeQuery(getUser(), query);
		
		//���� �� ���� ��������� ���������� ������ � �� ������ �������� �������
		ArrayList<ObjectId> personsIds = new ArrayList<ObjectId>();
		for (int i=0; i<result.getCards().size(); i++){
			ArrayList<ObjectId> internalPersons = new ArrayList<ObjectId>();
			internalPersons.add(((Card)result.getCards().get(i)).getId());
			Set<Person> personList = CardUtils.getPersonsByCards(
					internalPersons, 
					queryFactory, getDatabase(), getUser());
			Iterator<Person> personListIterator = personList.iterator();
			//���������� ������� � ��������� � ���������
			while (personListIterator.hasNext()){
				Person person = personListIterator.next(); 
				personsIds.add(person.getId());
			}
		} 

		return personsIds.toArray(new ObjectId[personsIds.size()]);
	}
}
