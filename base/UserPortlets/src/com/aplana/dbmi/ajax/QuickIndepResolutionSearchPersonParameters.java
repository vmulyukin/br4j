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
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.portlet.QuickIndepResolutionPortlet;
import com.aplana.dbmi.portlet.QuickIndepResolutionPortletSessionBean;
import com.aplana.dbmi.service.DataServiceBean;

public class QuickIndepResolutionSearchPersonParameters extends ARMSearchResultLabelBuilder implements SearchCardServletParameters {
	public static final String CALLER = "quickIndepResolutionPortlet";
	public static final String PARAM_NAMESPACE = "namespace";
	public static final String PARAM_OPTIONS = "options";
	public static final String EXT_OPTION = "ext";
	
	private static final ObjectId ATTR_POSITION = ObjectId.predefined(StringAttribute.class, "jbr.person.position");
	
	private Search search;
	private String options;
	
	
	public ObjectId getLabelAttrId() {
		return Attribute.ID_NAME;
	}

	public Search getSearch() {
		return search;
	}

	public void initialize(HttpServletRequest request, DataServiceBean serviceBean) throws ServletException {
		String namespace = request.getParameter(PARAM_NAMESPACE);
		QuickIndepResolutionPortletSessionBean sessionBean = 
				(QuickIndepResolutionPortletSessionBean)QuickIndepResolutionPortlet.getSessionBean(request, namespace);
		options = request.getParameter(PARAM_OPTIONS);
		if (EXT_OPTION.equals(options))
			this.search = sessionBean.getExtPersonsSearch();
		else 
			this.search = sessionBean.getEmployeesSearch();
	}
	
	public String getLabel(Card card) {		
		StringBuilder label = new StringBuilder();
		label.append(super.getLabel(card));
		
		if(EXT_OPTION.equals(options)) {
			StringAttribute position = (StringAttribute) card.getAttributeById(ATTR_POSITION); 
			if(null != position) {
				String positionStr = position.getStringValue();
				if(null != positionStr && positionStr.length() > 0) {
					label.append(", ").append(positionStr);
				}
			}
		}
		
		return label.toString();
	}

	public LinkDescriptor getList() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
