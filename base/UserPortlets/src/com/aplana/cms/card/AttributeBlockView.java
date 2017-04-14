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
package com.aplana.cms.card;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.aplana.dbmi.gui.AttributeView;
import com.aplana.dbmi.model.BlockViewParam;

/**
 * Represents View class for block of attributes
 * 
 * @author skashanski
 *
 */
public class AttributeBlockView {

	public enum Region {LEFT, RIGHT, BOTTOM};
	
	/**
	 * Unique Identifier of Block
	 */
	private Object id = null;
	
	/** Block displayed name  */
	private String name = "";
	
	/** UI widget region */
	private Region region = Region.BOTTOM;


	Map<String, AttributeView> attributeViews = new LinkedHashMap<String, AttributeView>();
	
	private int currentState = BlockViewParam.OPEN;
	

	
	
	public Region getRegion() {
		return region;
	}




	public void setRegion(Region region) {
		this.region = region;
	}




	public AttributeBlockView() {
		super();
	}
	
	
	

	public AttributeBlockView(Object id) {
		super();
		this.id = id;
	}




	public Map<String, AttributeView> getAttributeViews() {
		return attributeViews;
	}

    public Collection<AttributeView> getAttributeViewsCollection() {
        return attributeViews.values();
    }


	public void setAttributeViews(Map<String, AttributeView> attributeViews) {
		this.attributeViews = attributeViews;
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	
	public boolean containAttributeView(String attributeName) {
		
		return attributeViews.containsKey(attributeName);
		
	}	
	
	public AttributeView getAttributeView(String attributeName) {
		
		return attributeViews.get(attributeName);
		
	}

	public int getCurrentState() {
		return currentState;
	}

	public void setCurrentState(int currentState) {
		this.currentState = currentState;
	}
	
}
