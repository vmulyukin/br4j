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
package com.aplana.dbmi.card.actionhandler.multicard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.filter.PersonCardIdFilter;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

public class SplitTypedCardLinkToPersonAndInteger implements MappingProcedure {

	public static final String ERROR_INTEGER_FIELD_IS_EMPTY = "errorIntegerFieldIsEmpty";
	public static final String ERROR_SOME_PERSON_NOT_FOUND = "errorSomePersonNotFound";

	protected Log logger = LogFactory.getLog(getClass());
	/**
	 * �������� �� ������� �������� ������� {@link TypedCardLinkAttribute} � ID ��������� � args[0]
	 * (��������� ������� ������ ����� ������ "������� (����������)", ��� ����� ������ ����� �������� ��������),
	 * �������� �� ��� ��������� ��������������� �������� {@link Person} � {@link Integer}, 
	 * � ��������� ��� ������ ����������� ������ {@link #execute(Attribute)} ���������� �� ������ �� ���� �������� 
	 * � �������� {@link PersonAttribute} � {@link IntegerAttribute}
	 * @param session ������� ������
	 * @param args ������ �� ������ ��������: ��������, �������� �������� �������������� �� �������� ���������
	 * @throws MappingUserException � ����� errorSomePersonNotFound ���� ���������� ��������� {@link Person} �� ������������� ���������� {@link ObjectId}: {0} - ���������� ���������� ��������, {1} - ���������� ��������� ������, {2} - ������ ��� ��������� ������ ����� ������� 
	 * @throws MappingUserException � ����� errorIntegerFieldIsEmpty ���� ���� �� ���� �� �������� ����� �� ���������: {0} - ��� ������� ��� ������� �� ��������� �������� ���� 
	 * @throws MappingSystemException ��� ������� � ���������������� ����� ��� ��
	 */
	public void init(CardPortletSessionBean session, String[] args) 
								throws MappingUserException, MappingSystemException {
		// �������� ������ 1 ��������
		if (args.length != 1)
			throw new MappingSystemException("One argument expected, but " + args.length + " given: " + args);
		TypedCardLinkAttribute splitting;
		try {
			// �������� TypedCardLinkAttribute, �������� �������� ����� ������������
			splitting = (TypedCardLinkAttribute)session.getActiveCard().getAttributeById(
					MappingUtils.stringToAttrId(args[0]));
		} catch (ClassCastException e) {
			MappingSystemException me = new MappingSystemException("Type of attribute \"" + args[0] + "\" must be " + TypedCardLinkAttribute.class.getCanonicalName(), e);
			logger.error(me.getMessage());
			throw me;
		}
		if (splitting == null) {
			MappingSystemException e = new MappingSystemException("Attribute " + args[0] + " not found");
			logger.error(e.getMessage());
			throw e;
		}
		// ��� ������ ����� ��������� �������� ���
		if (splitting.getIdsLinked().isEmpty()) {
			persIntIter = Collections.EMPTY_LIST.iterator();
			return;
		}
		// ������ ��� ����� ������ �� ���������
		PersonCardIdFilter filter = new PersonCardIdFilter();
		filter.setCardIds(splitting.getIdsLinked());
		try {
			// �������� ������ �� ���������
			Collection<Person> persons = session.getServiceBean().filter(Person.class, filter);
			// ���� �� �����-�� ��������� �� ������� �������� ������� -- ����������� ����������
			if (persons.size() != splitting.getIdsLinked().size()) {
				StringBuilder add = new StringBuilder();
				// ������ ��������� ������
				if (persons.size() != 0) {
					for (Person p: persons) {
						add.append(p.getFullName());
						add.append(", ");
					}
					add.delete(add.length() - 2, add.length() - 1);
				} else {
					add.append("-");
				}
				logger.error(ERROR_SOME_PERSON_NOT_FOUND);
				throw new MappingUserException(ERROR_SOME_PERSON_NOT_FOUND,
						new Object[] {splitting.getIdsLinked().size(), persons.size(), add.toString()});
			}
			// ����� "������� -> �����" �� ������� ����� ���������� ��������
			Map<Person,Integer> personInteger = new HashMap<Person,Integer>(persons.size());
			// ��������� ����� "ID �������� -> ID �������� �� �����������"
			Map<Long, Long> types = splitting.getTypes();
			for (Person person: persons) {
				// �������� ID �������� �� ����������� (���� ������������ �� ������, ����� null)
				Long linkTypeId = types.get((Long)person.getCardId().getId());
				if (linkTypeId == null) {
					logger.error(ERROR_INTEGER_FIELD_IS_EMPTY);
					throw new MappingUserException(ERROR_INTEGER_FIELD_IS_EMPTY, new Object[] {person.getFullName()});
				}
				// �������� �������� �� ID
				ReferenceValue val = (ReferenceValue)session.getServiceBean().getById(
						new ObjectId(ReferenceValue.class, linkTypeId));
				if (val == null) {
					MappingSystemException e = new MappingSystemException(ReferenceValue.class.getName() + " not found");
					logger.error(e.getMessage());
					throw e;
				}
				try {
					// ���������� ������������ ������������ -> �����
					personInteger.put(person, new Integer(val.getValue()));
				} catch (NumberFormatException e) {
					MappingSystemException me = new MappingSystemException("Reference value don't contain an Integer: " + val.getValue(), e);
					logger.error(me.getMessage());
					throw me;
				}
			}
			// ������� ��������
			persIntIter = sortByValue(personInteger).entrySet().iterator();
		} catch (DataException e) {
			logger.error(e.getMessage());
			throw new MappingSystemException(e.getMessage(), e);
		} catch (ServiceException e) {
			logger.error(e.getMessage());
			throw new MappingSystemException(e.getMessage(), e);
		}
	}

	// ���������� ����� �� ��������� �����, � ���� ����� ��������� -- �� ������ ������
	private static Map<Person,Integer> sortByValue(Map<Person,Integer> map) {
	     List<Map.Entry<Person,Integer>> list = new LinkedList<Map.Entry<Person,Integer>>(map.entrySet());
	     Collections.sort(list, new Comparator<Map.Entry<Person,Integer>>() {
	          public int compare(Map.Entry<Person,Integer> o1, Map.Entry<Person,Integer> o2) {
	               int comp = o1.getValue().compareTo(o2.getValue());
	               return comp != 0 ? comp : o1.getKey().getFullName().compareTo(o2.getKey().getFullName());
	          }
	     });
	     Map<Person,Integer> result = new LinkedHashMap<Person,Integer>();
	     for (Iterator<Map.Entry<Person,Integer>> it = list.iterator(); it.hasNext();) {
	    	 Map.Entry<Person,Integer> entry = (Map.Entry<Person,Integer>)it.next();
	    	 result.put(entry.getKey(), entry.getValue());
	     }
	     return result;
	}
	
	private Iterator<Map.Entry<Person, Integer>> persIntIter;
	
	private final Collection<Person> singlePersonList = new ArrayList<Person>(1);
	public boolean execute(List<Attribute> attrs) throws MappingSystemException {
		// ��������� ����� 2 ��������
		if (attrs.size() != 2) {
			MappingSystemException e = new MappingSystemException(getClass().getSimpleName() + " needs exactly 2 attributes");
			logger.error(e.getMessage(), e);
			throw e;
		}
		// ���� ��������� ������, ���������� false
		if (!persIntIter.hasNext())
			return false;
		
		// ���� ������ �� ���������
		Map.Entry<Person, Integer> persInt = persIntIter.next();
		// ���������� ������� � ������
		singlePersonList.clear();
		singlePersonList.add(persInt.getKey());
		try {
			// ���������� ������ �� ����� ������� � ������ �������
			((PersonAttribute)(attrs.get(0))).setValues(singlePersonList);
		} catch (ClassCastException e) {
			MappingSystemException me = new MappingSystemException("First attribute must be a type of " + PersonAttribute.class.getSimpleName(), e);
			logger.error(me.getMessage());
			throw me;
		}
		try {
			// ���������� ��������������� ����� �� ������ �������
			((IntegerAttribute)(attrs.get(1))).setValue(persInt.getValue());
		} catch (ClassCastException e) {
			MappingSystemException me = new MappingSystemException("Second attribute must be a type of " + IntegerAttribute.class.getSimpleName(), e);
			logger.error(me.getMessage());
			throw me;
		}
		return true;
	}

}
