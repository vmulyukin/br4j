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


import com.aplana.dbmi.gui.SearchAttributeView;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.web.tag.util.StringUtils;
import org.apache.commons.beanutils.PropertyUtils;

import javax.portlet.PortletRequest;
import java.lang.reflect.InvocationTargetException;

/**
 * Represents class for creating and initializing   {@link com.aplana.dbmi.gui.SearchAttributeView} 
 * 
 * @author skashanski
 *
 */
public class SearchAttributeViewCreator<T extends Attribute> {
	
	protected T attribute = null; 
	
	public SearchAttributeViewCreator(T attribute) {
		this.attribute = attribute;
	}
	
	public SearchAttributeView create(PortletRequest request,
			SearchFilterPortletSessionBean sessionBean)
			throws ServiceException, DataException {

		DataServiceBean serviceBean = sessionBean.getServiceBean();
		//returns attribute's definition from database
		Attribute attributeDef = getAttributeById(serviceBean, this.attribute);
		if (this.attribute.getId() != null)
			attributeDef.setId(this.attribute.getId());
		
		// overwrite date attribute time pattern by provided time pattern in search config
		if(attribute instanceof DateAttribute
				&& attributeDef instanceof DateAttribute) {
			String timePattern = ((DateAttribute)attribute).getTimePattern();
			if(timePattern != null)
				((DateAttribute) attributeDef).setTimePattern(timePattern);
		}
		
		initSearchAttribute(attributeDef, sessionBean);
		SearchAttributeView attributeView = createAndInitSearchAttributeView(request);
		return attributeView;
	}
	
	/**
	 * do specific intialization logic for searchAttribute
	 */
	protected void initSearchAttribute(Attribute attributeDef,
			SearchFilterPortletSessionBean sessionBean)
			throws ServiceException, DataException {
		
		initializeNames(attribute, attributeDef);
		copyProperties(attribute, attributeDef);
	}

	protected SearchAttributeView createAndInitSearchAttributeView(
			PortletRequest request) {
		
		SearchAttributeView attributeView = new SearchAttributeView(this.attribute);
		attributeView.initEditor(request);
		return attributeView;
	}

	protected Attribute getAttributeById(DataServiceBean serviceBean,
			Attribute searchAttrId) throws ServiceException, DataException {

		if (this.attribute == null)
			return null;
		return (Attribute) serviceBean.getById(searchAttrId.getId());
	}
	
	protected void initializeNames(Attribute searchAttributeId,
			Attribute searchAttribute) {
		
		if (StringUtils.hasText(searchAttributeId.getNameRu()))
			searchAttribute.setNameRu(searchAttributeId.getNameRu());
		
		if (StringUtils.hasText(searchAttributeId.getNameEn()))
			searchAttribute.setNameEn(searchAttributeId.getNameEn());
	}

	/**
	 * Copies all properties values from passed attribute
	 * @param attributeCloneTo clone to copy to 
	 * @param attrCloneFrom clone to copy from
	 */
	protected void copyProperties(Attribute attributeCloneTo, Attribute attrCloneFrom) {
		
		try {
			PropertyUtils.copyProperties(attributeCloneTo, attrCloneFrom);
		} catch (NoSuchMethodException  e) {
			throw new RuntimeException(e);
		} catch(InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}