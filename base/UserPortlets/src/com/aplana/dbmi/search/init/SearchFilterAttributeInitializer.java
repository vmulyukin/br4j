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

	
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;

/**
 * Represents base class for initialization searchFilterAttribute value
 * It copies(initializes) attribute's value from Search
 * 
 * @author skashanski
 *
 */
public abstract class SearchFilterAttributeInitializer <AttrType extends Attribute, AttrValueType> {
	
	/**
	 * strategy for performing post-initialization logic 
	 */
	protected SearchFilterAttributePostInitStrategy postInitStrategy = null;

	public SearchFilterAttributeInitializer() {}
	
	public void setPostInitStrategy(
			SearchFilterAttributePostInitStrategy postInitStrategy) {
		this.postInitStrategy = postInitStrategy;
	}
	
	public SearchFilterAttributePostInitStrategy getPostInitStrategy() {
		return postInitStrategy;
	}

	/**
	 * Returns value from Search by given attributeId 
	 * @param search {@link com.aplana.dbmi.action.Search}
	 * @param attributeId {@link com.aplana.dbmi.model.ObjectId} attribute identifier 
	 */
	@SuppressWarnings("unchecked")
	protected AttrValueType getValue(Search search, ObjectId attributeId) {
		return (AttrValueType)search.getAttribute(attributeId);
	}
	
	/**
	 * Set(initializes) search attribute value with given object.   
	 * @param searchFilterAttribute attribute to set value  
	 * @param searchAttributeValue value to set to given search attribute
	 */
	protected abstract void setValue(AttrType searchFilterAttribute, AttrValueType searchAttributeValue);

	/**
	 * Checks if given searchFilterAtteibute is empty
	 */
	protected boolean isEmpty(AttrValueType searchAttributeValue) {
		return (searchAttributeValue == null);
	}

	public void initialize(AttrType searchFilterAttribute, Search search) {
		AttrValueType searchAttributeValue = getValue(search, searchFilterAttribute.getId());
		
		if (isEmpty(searchAttributeValue))
			return;
		
		setValue(searchFilterAttribute,searchAttributeValue);
		
		try {
			if (postInitStrategy != null)//call post-initialization logic 
				postInitStrategy.postInitialize(searchFilterAttribute);
		} catch (Exception e) {
			throw new RuntimeException(e);//TODO add throws exception to class
		}
	}
}
