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
package com.aplana.dbmi.search;

import java.util.Collection;

import javax.portlet.PortletRequest;

import com.aplana.dbmi.gui.SearchAttributeView;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TemplateSearchAttribute;
import com.aplana.dbmi.model.filter.TemplateIdListFilter;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class TemplateSearchAttributeViewCreator extends
		SearchAttributeViewCreator<TemplateSearchAttribute> {

	public TemplateSearchAttributeViewCreator(TemplateSearchAttribute attribute) {
		super(attribute);
	}
	
	
	@Override
	public SearchAttributeView create(PortletRequest request,
			SearchFilterPortletSessionBean sessionBean)
			throws ServiceException, DataException {

		DataServiceBean serviceBean = sessionBean.getServiceBean();
		
		initSearchAttribute(attribute, sessionBean);
		
		SearchAttributeView attributeView = createAndInitSearchAttributeView(request);
		
		return attributeView;
		
		
	}
	
	
	@Override
	protected void initSearchAttribute(Attribute attributeDef, SearchFilterPortletSessionBean sessionBean) throws ServiceException, DataException {
		
		DataServiceBean serviceBean = sessionBean.getServiceBean();
		TemplateIdListFilter filter = new TemplateIdListFilter(attribute.getCardTemplates());
		Collection<Template> templates =(Collection<Template>) serviceBean.filter(Template.class, filter);

		attribute.setCardTemplates(templates);
		
	}
	
	
	
	
	
	

}
