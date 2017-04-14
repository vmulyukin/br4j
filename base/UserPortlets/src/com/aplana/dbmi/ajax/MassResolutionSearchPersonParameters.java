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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.card.hierarchy.descriptor.LinkDescriptor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.portlet.MassResolutionPortlet;
import com.aplana.dbmi.portlet.MassResolutionPortletSessionBean;
import com.aplana.dbmi.service.DataServiceBean;

public class MassResolutionSearchPersonParameters extends ARMSearchResultLabelBuilder implements SearchCardServletParameters {
	public static final String CALLER = "massResolutionPortlet";
	public static final String PARAM_NAMESPACE = "namespace";
	
	private Search search;
	
	
	public ObjectId getLabelAttrId() {
		return Attribute.ID_NAME;
	}

	public Search getSearch() {
		return search;
	}

	public void initialize(HttpServletRequest request, DataServiceBean serviceBean) throws ServletException {
		String namespace = request.getParameter(PARAM_NAMESPACE);
		MassResolutionPortletSessionBean sessionBean = 
				(MassResolutionPortletSessionBean) MassResolutionPortlet.getSessionBean(request, namespace);
		this.search = sessionBean.getEmployeesSearch();
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
	
}
