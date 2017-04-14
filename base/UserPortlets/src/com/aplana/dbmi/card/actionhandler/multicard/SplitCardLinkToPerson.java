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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.filter.PersonCardIdFilter;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

public class SplitCardLinkToPerson implements MappingProcedure {

	public static final String ERROR_INTEGER_FIELD_IS_EMPTY = "errorIntegerFieldIsEmpty";
	public static final String ERROR_SOME_PERSON_NOT_FOUND = "errorSomePersonNotFound";

	protected Log logger = LogFactory.getLog(getClass());

	public void init(CardPortletSessionBean session, String[] args)
			throws MappingUserException, MappingSystemException{
		// �������� ������ 1 ��������
		if (args.length != 1)
			throw new MappingSystemException("One argument expected, but " + args.length + " given: " + args);
		CardLinkAttribute splitting;
		try {
			// �������� CardLinkAttribute, �������� �������� ����� ������������
			splitting = (CardLinkAttribute)session.getActiveCard().getAttributeById(
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
			persIter = Collections.EMPTY_LIST.iterator();
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
			// ������� ��������
			persIter = persons.iterator();
		} catch (DataException e) {
			logger.error(e.getMessage());
			throw new MappingSystemException(e.getMessage(), e);
		} catch (ServiceException e) {
			logger.error(e.getMessage());
			throw new MappingSystemException(e.getMessage(), e);
		}
	}

	private Iterator<Person> persIter;
	
	private final Collection<Person> singlePersonList = new ArrayList<Person>(1);
	public boolean execute(List<Attribute> attrs) throws MappingSystemException {
		// �������� ����� 1 �������
		if (attrs.size() != 1) {
			MappingSystemException e = new MappingSystemException(getClass().getSimpleName() + " needs exactly 2 attributes");
			logger.error(e.getMessage(), e);
			throw e;
		}
		// ���� ��������� ������, ���������� false
		if (!persIter.hasNext())
			return false;
		
		// ���������� ������� � ������
		singlePersonList.clear();
		singlePersonList.add(persIter.next());
		try {
			// ���������� ������ �� ����� ������� � ������ �������
			((PersonAttribute)(attrs.get(0))).setValues(singlePersonList);
		} catch (ClassCastException e) {
			MappingSystemException me = new MappingSystemException("First attribute must be a type of " + PersonAttribute.class.getSimpleName(), e);
			logger.error(me.getMessage());
			throw me;
		}
		return true;
	}

}
