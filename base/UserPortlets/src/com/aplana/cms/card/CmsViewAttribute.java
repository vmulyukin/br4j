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

/**
 * Represents  attribute's view for card attribute inside  SuperVisor/Minister Workstation
 * It is used for creating/editing card inside SuperVisor/Minister Workstation
 *   
 * @author skashanski
 *
 */
public class CmsViewAttribute {
	
	/**
	 * attribute's code
	 */
	private String code = null;
	 
	private boolean mandatory = false;
	private boolean hidden = false;
	private boolean readOnly  = false;
	
	
	
	public CmsViewAttribute(String code) {
		super();
		this.code = code;
	}
	
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public boolean isMandatory() {
		return mandatory;
	}
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
	public boolean isHidden() {
		return hidden;
	}
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	public boolean isReadOnly() {
		return readOnly;
	}
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}


	@Override
	public int hashCode() {
		
		return 31 + code.hashCode();
		
	}


	@Override
	public boolean equals(Object obj) {

		if (!(obj instanceof CmsViewAttribute))
			return false;
		CmsViewAttribute cmsViewAttribute = (CmsViewAttribute)obj;
		
		return code.equals(cmsViewAttribute.getCode());
	}
	
	
	
	
	
	 

}
