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
package com.aplana.dbmi.ajax.mapper;

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletException;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.filter.PersonCardIdFilter;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class PersonAttributeMapper extends SearchParametersMapper{

	@Override
	public void perform(Search search, String parameter) throws ServletException {
		Collection<ObjectId> selectedCards = ObjectIdUtils.commaDelimitedStringToNumericIds(parameter, Card.class);
		PersonCardIdFilter f = new PersonCardIdFilter();
		f.setCardIds(selectedCards); 
		DataServiceBean serviceBean = getDataServiceBean();
		Collection<Person> personIdCollection;
		try {
			personIdCollection = serviceBean.filter(Person.class, f);
		} catch (DataException e) {
			throw new ServletException("Can't parse PersonAttribute", e);
		} catch (ServiceException e) {
			throw new ServletException("Can't parse PersonAttribute", e);
		}
		Iterator<Person> personIds = personIdCollection.iterator();
		while (personIds.hasNext()) {
			search.addPersonAttribute(getAttributeId(), personIds.next().getId());
		}
	}
}
