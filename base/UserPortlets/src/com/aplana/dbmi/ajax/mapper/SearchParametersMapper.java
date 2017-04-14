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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerVariantDescriptor.SearchDependency;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataServiceBean;

public abstract class SearchParametersMapper {
	
	private Map<String, String> parameterMap = new HashMap<String, String>();
	private ObjectId attributeId = null;
	private DataServiceBean dataServiceBean = null;
	
	public abstract void perform(Search search, String parameter) throws ServletException;
	
	public static SearchParametersMapper newInstance(DataServiceBean dataServiceBean, SearchDependency dependency) throws ServletException {
		Class<?> mapperClass = null;
		Map<String, String> parameterMap = null;
		if (dependency.isMapperDefined()){
			try {
				mapperClass = Class.forName(dependency.getMapperClassPath());
			} catch (Exception e) {
				throw new ServletException("Can't find class for mapper", e);
			}
			parameterMap = dependency.getMapperParameterMap();
		}
		else if (CardLinkAttribute.class.equals(dependency.getFilterAttrId().getType())) { 
			mapperClass = CardLinkAttributeMapper.class;
		}
		else if (DateAttribute.class.equals(dependency.getFilterAttrId().getType())) { 
			mapperClass = DateAttributeMapper.class;
		}
		else if (StringAttribute.class.equals(dependency.getFilterAttrId().getType())) { 
			mapperClass = StringAttributeMapper.class;
		}
		else if (TextAttribute.class.equals(dependency.getFilterAttrId().getType())) { 
			mapperClass = TextAttributeMapper.class;
		}
		else if (IntegerAttribute.class.equals(dependency.getFilterAttrId().getType())) { 
			mapperClass = IntegerAttributeMapper.class;
		}
		else if (ListAttribute.class.equals(dependency.getFilterAttrId().getType())) { 
			mapperClass = ListAttributeMapper.class;
		}
		else if (PersonAttribute.class.equals(dependency.getFilterAttrId().getType())) { 
			mapperClass = PersonAttributeMapper.class;
		}

		SearchParametersMapper result = null;
		try {
			result = (SearchParametersMapper) mapperClass.newInstance();
		} catch (Exception e) {
			throw new ServletException("Can't create new mapper instance", e);
		}
		result.dataServiceBean = dataServiceBean;
		result.setAttributeId(dependency.getFilterAttrId());
		if (parameterMap != null) {
			result.setParameterMap(parameterMap);
		}
		return result;
	}

	public DataServiceBean getDataServiceBean() {
		return dataServiceBean;
	}
	
	public void setParameter(String name, String value) {
		parameterMap.put(name, value);
	}

	public String getParameter(String name) {
		return parameterMap.get(name);
	}

	public void setParameterMap(Map<String, String> parameterMap) {
		if (parameterMap != null) {
			this.parameterMap = parameterMap;
		}
	}
	
	public ObjectId getAttributeId() {
		return attributeId;
	}
	
	public void setAttributeId(ObjectId attributeId) {
		this.attributeId = attributeId;
	}
}
