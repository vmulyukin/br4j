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
package com.aplana.dbmi.ajax;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.card.hierarchy.descriptor.LinkDescriptor;
import com.aplana.dbmi.card.util.ARMUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataServiceBean;

public class UserRolesSearchInternalPersonParameters extends ARMSearchResultLabelBuilder implements SearchCardServletParameters {
	public static final String CALLER = "userRolesAndGroupsAttribute";
	public static final String PARAM_NAMESPACE = "namespace";
	
	private Search search;
	
	public ObjectId getLabelAttrId() {
		return Attribute.ID_NAME;
	}

	public Search getSearch() {
		return search;
	}

	public void initialize(HttpServletRequest request, DataServiceBean serviceBean) throws ServletException {
		this.search = getSearchPerson();
	}
	
	public String getLabel(Card card) {		
		StringBuilder label = new StringBuilder();
		label.append(super.getLabel(card));
		return label.toString();
	}

	public LinkDescriptor getList() {
		// TODO Auto-generated method stub
		return null;
	}

	private Search getSearchPerson() {
		final Search search = new Search();
		search.setColumns(ARMUtils.getFullNameColumns());

		search.setByAttributes(true);
		search.setWords(null);
		final List<DataObject> templates = new ArrayList<DataObject>(1);
		templates.add(DataObject.createFromId(ObjectId.predefined(Template.class, "jbr.internalPerson")));
		search.setTemplates(templates);
		search.addStringAttribute(Attribute.ID_NAME);

		final List<String> states = new ArrayList<String>(1);
		states.add(ObjectId.predefined(CardState.class, "user.active").getId().toString());
		search.setStates(states);

		return search;
	}
}
