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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.aplana.cms.card.AttributeBlockView.Region;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.gui.AttributeView;

public class WorkstationCardPortletCardInfo extends CardPortletCardInfo {
	
	
	private List<AttributeBlockView> attributeBlockViews = new ArrayList<AttributeBlockView>();
	
	

	
	public List<AttributeBlockView> getAttributeBlockViews() {
		return attributeBlockViews;
	}
	
	public List<AttributeBlockView> getAttributeBlockViewsByRegion(Region region) {
		List<AttributeBlockView> regionAttributeBlockViews = new ArrayList<AttributeBlockView>();
		
		boolean isVisibleBlock;
		for(AttributeBlockView  attributeBlockView : attributeBlockViews) {
			
			isVisibleBlock = false;
			for(Entry<String, AttributeView> e : attributeBlockView.getAttributeViews().entrySet()) {
				final AttributeView av = (AttributeView)e.getValue();
				if (av.isVisible()) {
					isVisibleBlock = true;
					break;
				}
			}
			
			if(isVisibleBlock && region.equals(attributeBlockView.getRegion()))
				regionAttributeBlockViews.add(attributeBlockView);
			
		}
		
		return regionAttributeBlockViews;
	}	

	public void setAttributeBlockViews(List<AttributeBlockView> attributeBlockViews) {
		this.attributeBlockViews = attributeBlockViews;
	}


	public boolean containAttributeView(String attributeName) {
		
		for(AttributeBlockView attributeBlockView : attributeBlockViews ) {
			if (attributeBlockView.containAttributeView(attributeName))
				return true;
		}
		
		return false;
		
	}

	
	/**
	 * Returns {@AttributeView) by passed attribute's name 
	 * @param attributeName attribute name
	 */
	public AttributeView getViewByAttribute(String attributeName) {
		
		for(AttributeBlockView attributeBlockView : attributeBlockViews ) {
			if (attributeBlockView.containAttributeView(attributeName))
				return attributeBlockView.getAttributeView(attributeName);
		}
		
		return null;
		
	}	
	
	/**
	 * Returns {@AttributeView) by passed block name and attribute name
	 * 
	 * @param blockName - block name
	 * @param attributeName attribute name
	 */
	public AttributeView getViewByBlockAttribute(String blockName, String attributeName) {
		
		for(AttributeBlockView attributeBlockView : attributeBlockViews ) {
			
			if (!blockName.equals(attributeBlockView.getName()))
				continue;
			
			if (attributeBlockView.containAttributeView(attributeName))
				return attributeBlockView.getAttributeView(attributeName);
		}
		
		return null;
		
	}	
	

	
}
