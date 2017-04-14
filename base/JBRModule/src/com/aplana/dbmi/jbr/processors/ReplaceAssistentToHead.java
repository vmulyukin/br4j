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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.SaveQueryBase;

public class ReplaceAssistentToHead extends AbstractCardProcessor implements Parametrized {
	/* ��������� ��� �������� ��������� ������������� � ������� ������� 
	 * (���� PersonAttribute ��� CardLinkAttribute) �������� ������������
	 * ���� ������� ������������ �������� �������������, � ������� �������� ��� ��������� 
	 * 
	 */
	protected static final ObjectId ATTR_MANAGER = ObjectId.predefined(PersonAttribute.class, "jbr.arm.manager");
	protected static final ObjectId ATTR_ASSISTANT = ObjectId.predefined(PersonAttribute.class, "boss.assistant");
	protected static final ObjectId TEMPLATE_SETTINGS_ARM = ObjectId.predefined(Template.class, "boss.settings");
	
	/* �������� ���������� - ������������� �������� �� ������� ����� ���������� ������������
	 * ������: ���:�������������, ��� ������������� - ��� �������� �� ��, ���� ��� ������� 
	 * �� ����� objectids.properties
	 */
	public static final String PARAM_ATTR = "attr";

	private ObjectId targetAttrId; // ������������� �������� �� ������� ����� ���������� ������������

	@Override
	public Object process() throws DataException {
		final List<ObjectId> assistants = getAssistants();
		if (assistants == null || assistants.isEmpty()) {
			return null;
		}
		replaceSigner(getCardId(), assistants, getUser().getPerson());
		return null;
	}

	public void setParameter(String name, String value) {		
		if (PARAM_ATTR.equals(name)) {
			final String[] typeCode = value.split(":");
			final String t = typeCode[0];
			final String code = typeCode[1];
			final Class<?> type = AttrUtils.getAttrClass(t); 
			targetAttrId = ObjectIdUtils.getObjectId(type, code, false);
		} else
			throw new IllegalArgumentException("Unknown parameter: " + name);
		
	}

	protected Person loadPerson(ObjectId id) throws DataException {
		final ObjectQueryBase query = getQueryFactory().getFetchQuery(Person.class);
		query.setId(id);
		return (Person) getDatabase().executeQuery(getSystemUser(), query);
	}

	protected Card loadCard(ObjectId id) throws DataException {
		final ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
		query.setId(id);
		return (Card) getDatabase().executeQuery(getSystemUser(), query);
	}

	protected void saveCard(Card card) throws DataException {
		final SaveQueryBase query = getQueryFactory().getSaveQuery(card);
		query.setObject(card);
		execAction(new LockObject(card));
		try {
			getDatabase().executeQuery(getSystemUser(), query);
		} finally {
			execAction(new UnlockObject(card));
		}
	}

	/* ���������� ��������� id �������� ������ �� ������� ��������� attr 
	 * attr ������ ���� ���� PersonAttribute ��� CardLinkAttribute
	 */
	@SuppressWarnings("unchecked")
	protected Collection<ObjectId> getPersonCards(Attribute attr) throws DataException {
		if (PersonAttribute.class.isAssignableFrom(attr.getClass())) {
			final Collection<Person> persons = ((PersonAttribute) attr).getValues();
			if (persons == null)
				return null;
			final ArrayList<ObjectId> ids = new ArrayList<ObjectId>(persons.size());
			for( Person p : persons){
				ids.add( p.getCardId());
			}
			return ids;
		} else if (CardLinkAttribute.class.isAssignableFrom(attr.getClass())) {
			return ((CardLinkAttribute) attr).getIdsLinked();
		} else {
			throw new DataException("Not suitable type to getting of the persons: " + attr.getClass());
		}
	}

	/* ������������� � �������� �������� �������� �������� �������
	 * ���� ������� ���� CardLink, �� ������������� id ������� �������� �������
	 * ���� ������� ���� Person, �� ������������� id �������
	 */
	protected void setPerson(Attribute attr, Person person) throws DataException {
		if (PersonAttribute.class.isAssignableFrom(attr.getClass())) {
			final ArrayList<Person> values = new ArrayList<Person>(1);
			values.add(person);
			((PersonAttribute) attr).setValues(values);
		} else if (CardLinkAttribute.class.isAssignableFrom(attr.getClass())){
			final ArrayList<ObjectId> values = new ArrayList<ObjectId>(1);
			values.add(person.getCardId());
			((CardLinkAttribute) attr).setIdsLinked(values);
		} else {
			throw new DataException("Not suitable type to assign of the person: " + attr.getClass());
		}
	}

	@SuppressWarnings("unchecked")
	protected List<ObjectId> getAssistants() throws DataException {
		final Person curPerson = getUser().getPerson();
		final List<ObjectId> assistants = new LinkedList<ObjectId>(); // id ��������� ����������
		// ��������� �� �������� �� ������������ ����������� ������� ���������
		if (curPerson.getId().equals(Person.ID_SYSTEM)) {
			return assistants;
		}
		// ���� �������� ���������� ��̻ � ������� ��������� ��̻ ���. curPerson
		final Search search = new Search();
		final List<DataObject> templates = new ArrayList<DataObject>(1);
		templates.add(DataObject.createFromId(TEMPLATE_SETTINGS_ARM));
		search.setTemplates(templates);

		search.setByAttributes(true);
		search.addPersonAttribute(ATTR_MANAGER, curPerson.getId());

		final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>(1);
		columns.add( CardUtils.createColumn(ATTR_ASSISTANT));
		search.setColumns(columns);

		final List<Card> result = CardUtils.execSearchCards(search, getQueryFactory(), getDatabase(), getSystemUser());
		if (result == null || result.isEmpty()) {
			return assistants;
		}
		final Card setARMCard = result.get(0);

		// �������� id �������� ���������� ����������
		final PersonAttribute personAttr = (PersonAttribute)setARMCard.getAttributeById(ATTR_ASSISTANT);
		if (personAttr == null || personAttr.getValues() == null)
			return assistants;

		final Iterator<Person> iter = personAttr.getValues().iterator();
		while (iter.hasNext()) {
			Person person = iter.next(); // �� �����, ����������� card_id
			person = loadPerson(person.getId());
			assistants.add( person.getCardId() );
		}
		return assistants;
	}

	protected void replaceSigner(ObjectId cardId, List<ObjectId> assistants, Person head) 
		throws DataException 
	{
		// �������� �������� �� targetAttrId - id �������� ������� �� �������� ��������� ������� targetAttr
		final Card source = loadCard(cardId);
		final Attribute targetAttr = source.getAttributeById(targetAttrId);
		final Collection<ObjectId> values = getPersonCards(targetAttr); 
		if (values == null || values.size() != 1) {
			return;
		}
		final ObjectId curValue = values.iterator().next();

		// ��������� �������� �� �������� �������� ����������
		if (!assistants.contains(curValue))
			return;

		// ������������� ������������ head � �������� �������� �������� targetAttr
		setPerson(targetAttr, head);

		// ��������� ��������
		saveCard(source);
	}
}
