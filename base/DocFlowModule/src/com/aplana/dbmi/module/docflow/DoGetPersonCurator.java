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

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

public class DoGetPersonCurator extends ActionQueryBase {
	private static final long serialVersionUID = 1L;
	
	public static final ObjectId personDepartmentAttrId = ObjectId.predefined(CardLinkAttribute.class, "jbr.person.dept");
	public static final ObjectId departmentParentAttrId = ObjectId.predefined(CardLinkAttribute.class, "jbr.department.parentDepartment");
	public static final ObjectId departmentCuratorAttrId = ObjectId.predefined(PersonAttribute.class, "jbr.department.curator");

	@Override
	public Object processQuery() throws DataException {
		GetPersonCurator action = (GetPersonCurator) getAction();
		return searchPersonCurator(action.getUserPerson().getCardId());
	}

	private Person searchPersonCurator(ObjectId personId) throws DataException {
		final Card personCard = loadCard(personId);
		final CardLinkAttribute department = (CardLinkAttribute)personCard.getAttributeById(personDepartmentAttrId);
		if (department.getSingleLinkedId() != null) {
			return searchDepartmentCurator(department.getSingleLinkedId());
		}
		else {
			return null;
		}
	}

	private Person searchDepartmentCurator(ObjectId departmentId) throws DataException {
		final Card departmentCard = loadCard(departmentId);
		final PersonAttribute curator = (PersonAttribute)departmentCard.getAttributeById(departmentCuratorAttrId);
		final CardLinkAttribute parent = (CardLinkAttribute)departmentCard.getAttributeById(departmentParentAttrId);
		if (!curator.isEmpty()) {
			return curator.getPerson();
		}
		else if (parent.getSingleLinkedId() != null){
			return searchDepartmentCurator(parent.getSingleLinkedId());
		}
		else {
			return null;
		}
	}
	
	private Card loadCard(ObjectId cardId) throws DataException{
		final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(Card.class);
		cardQuery.setAccessChecker(null);
		cardQuery.setId(cardId);
		return (Card) getDatabase().executeQuery(getUser(), cardQuery);
	}
}
