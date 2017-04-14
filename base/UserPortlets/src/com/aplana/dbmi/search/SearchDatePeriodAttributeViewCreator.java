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

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.PropertyUtils;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.DatePeriodAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;


/**
 * Represents specific class for creating and initializing  {@link com.aplana.dbmi.gui.SearchAttributeView} for {@link DatePeriodAttribute}  
 * 
 * @author skashanski
 *
 */
public class SearchDatePeriodAttributeViewCreator extends
		SearchAttributeViewCreator<DatePeriodAttribute> {

	
	public SearchDatePeriodAttributeViewCreator(DatePeriodAttribute attribute) {
		super(attribute);
	}

	@Override
	protected void initSearchAttribute(Attribute attributeDef,
			SearchFilterPortletSessionBean sessionBean)
			throws ServiceException, DataException {

		initializeNames(attribute, attributeDef);
		
		copyProperties(attribute, attributeDef);
		if (this.attribute.getDateAttribute().getId() != null)
			attributeDef.setId(this.attribute.getDateAttribute().getId());
		attribute.setDateAttribute((DateAttribute)attributeDef);
		
	}
	
	
	
	
	@Override
	protected Attribute getAttributeById(DataServiceBean serviceBean,
			Attribute searchAttrId) throws ServiceException, DataException {

		DateAttribute dateAttribute = ((DatePeriodAttribute)searchAttrId).getDateAttribute();
		
		return super.getAttributeById(serviceBean, dateAttribute);
	}

	

}
