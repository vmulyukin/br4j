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
package com.aplana.dbmi.search.init;

import com.aplana.dbmi.action.Search.NumericIdList;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent specific initializer for (@link PersonAttribute)
 * It works with {@link PersonAttribute} instances
 * 
 * @author skashanski
 *
 */
public class SearchFlterPersonAttributeInitializer extends
		SearchFilterAttributeInitializer<PersonAttribute, NumericIdList> {

	@Override
	protected boolean isEmpty(NumericIdList searchAttributeValue) {
		return super.isEmpty(searchAttributeValue) || searchAttributeValue.isEmpty();
	}

	@Override
	protected void setValue(PersonAttribute searchFilterAttribute,
			NumericIdList searchAttributeValue) {
		List<Person> values = new ArrayList<Person>();
		
		for (ObjectId cardId : searchAttributeValue.getNumericIds()) {
			Person person = new Person();
			person.setId(cardId);
			values.add(person);	
		}
		
		searchFilterAttribute.setValues(values);
	}
}
