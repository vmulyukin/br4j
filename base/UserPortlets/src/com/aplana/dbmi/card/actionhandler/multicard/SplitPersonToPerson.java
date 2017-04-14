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
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;

public class SplitPersonToPerson implements MappingProcedure {

	public static final String ERROR_MANDATORY_ATTR_IS_EMPTY = "errorMandatoryAttrIsEmpty";
	protected Log logger = LogFactory.getLog(getClass());

	@SuppressWarnings("unchecked")
	public void init(CardPortletSessionBean session, String[] args)
			throws MappingUserException, MappingSystemException{
		// �������� ������ 1 ��������
		if (args.length != 1)
			throw new MappingSystemException("One argument expected, but " + args.length + " given: " + args);
		PersonAttribute splitting;
		try {
			// �������� PersonAttribute, �������� �������� ����� ������������
			splitting = (PersonAttribute)session.getActiveCard().getAttributeById(
					MappingUtils.stringToAttrId(args[0]));
		} catch (ClassCastException e) {
			MappingSystemException me = new MappingSystemException("Type of attribute \"" + MappingUtils.stringToAttrId(args[0]) + "\" must be " + PersonAttribute.class.getCanonicalName(), e);
			logger.error(me.getMessage());
			throw me;
		}
		if (splitting == null) {
			MappingSystemException e = new MappingSystemException("Attribute " + MappingUtils.stringToAttrId(args[0]) + " not found");
			logger.error(e.getMessage());
			throw e;
		}
		if (splitting.isEmpty()) {
			logger.error(ERROR_MANDATORY_ATTR_IS_EMPTY);
			throw new MappingUserException(ERROR_MANDATORY_ATTR_IS_EMPTY, new Object[] {splitting.getName()});
		}
		// ��� ������ ����� ��������� ������ ���
		if (splitting.getValues().isEmpty()) {
			persIter = Collections.EMPTY_LIST.iterator();
			return;
		}
		
		// �������� ������
		Collection<Person> persons = splitting.getValues();
		// ������� ��������
		persIter = persons.iterator();
	}

	private Iterator<Person> persIter;
	
	private final Collection<Person> singlePersonList = new ArrayList<Person>(1);
	
	public boolean execute(List<Attribute> attrs) throws MappingSystemException {
		// �������� ����� 1 �������
		if (attrs.size() != 1) {
			MappingSystemException e = new MappingSystemException(getClass().getSimpleName() + " needs exactly 1 attribute");
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
