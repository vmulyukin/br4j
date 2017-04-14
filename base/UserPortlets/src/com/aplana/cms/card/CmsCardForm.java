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

import com.aplana.dbmi.gui.AttributeView;



/**
 * Represents form for creating/editing inside Supervisor/Minister Workstation 
 * 
 * @author skashanski
 *
 */
public class CmsCardForm {
	
	/** view ...URL to jsp-file */
	private String view = null;
	
	private List<CmsCardBlock> blockAttributes = new ArrayList<CmsCardBlock>();

	public CmsCardForm() {
		super();
	}


	public String getView() {
		return view;
	}


	public void setView(String view) {
		this.view = view;
	}


	public List<CmsCardBlock> getBlockAttributes() {
		return blockAttributes;
	}


	public void setBlockAttributes(List<CmsCardBlock> blockAttributes) {
		this.blockAttributes = blockAttributes;
	}



	/**
	 * Returns {@CmsViewAttribute) by passed attribute's name 
	 * @param attributeName attribute name
	 */
	public CmsViewAttribute getCmsViewByAttribute(String attributeName) {
		
		for(CmsCardBlock cmsCardBlock : blockAttributes ) {
			if (cmsCardBlock.containCmsAttributeView(attributeName))
				return cmsCardBlock.getCmsAttributeView(attributeName);
		}
		
		return null;
		
	}
	
	/**
     * Verifies if there {@CmsViewAttribute) for given attribute's name		
	 * @param attributeName attribute name
	 */
	public boolean containCmsViewByAttribute(String attributeName) {
		
		for(CmsCardBlock cmsCardBlock : blockAttributes ) {
			if (cmsCardBlock.containCmsAttributeView(attributeName))
				return true;
		}
		
		return false;
		
	}	
	
	/**
	 * Returns {@CmsViewAttribute) by passed block name and attribute name
	 * 
	 * @param blockName - block's name
	 * @param attributeName attribute's name
	 */
	public CmsViewAttribute getViewByBlockAttribute(String blockName, String attributeName) {
		
		for(CmsCardBlock cmsCardBlock : blockAttributes ) {
			
			if (!blockName.equals(cmsCardBlock.getName()))
				continue;
			
			if (cmsCardBlock.containCmsAttributeView(attributeName))
				return cmsCardBlock.getCmsAttributeView(attributeName);
		}
		
		return null;
		
	}	
	
	
	
	

}
