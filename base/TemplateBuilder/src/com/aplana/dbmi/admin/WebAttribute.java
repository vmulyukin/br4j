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
package com.aplana.dbmi.admin;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;

public class WebAttribute extends  Attribute {

	private static final long serialVersionUID = 2L;

	private Object type;
	private String newId; 
	private ObjectId reference;
	private boolean multiValued = false;
	private boolean showTime;
	private String timePattern; 
	
	public String getStringValue() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getRealId(){
		return super.getId() == null || super.getId().getId()==null ? null : super.getId().getId();
	}

	public void setRealId(Object id){
		final ObjectId objectId = new ObjectId(Attribute.class, id);
		super.setId(objectId);
	}

	public Object getType() {
		return type;
	}

	public void setType(Object type) {
		this.type = type;
	}

	public String getNewId() {
		return newId;
	}

	public void setNewId(String newId) {
		this.newId = newId;
	}

	public boolean equalValue(Attribute attr) {
		return false;
	}

	public ObjectId getReference() {
		return reference;
	}

	public void setReference(ObjectId reference) {
		this.reference = reference;
	}

	public boolean verifyValue() {
		return true;
	}

	public boolean isMultiValued() {
		return this.multiValued;
	}

	public void setMultiValued(boolean value) {
		this.multiValued = value;
	}
	
	/**
	 * @return the showTime
	 */
	public boolean isShowTime() {
		return this.showTime;
	}

	/**
	 * @param value the showTime to set
	 */
	public void setShowTime(boolean value) {
		this.showTime = value;
	}

	/**
	 * @return the timePattern
	 */
	public String getTimePattern() {
		return this.timePattern;
	}

	/**
	 * @param value the timePattern to set
	 */
	public void setTimePattern(String value) {
		this.timePattern = value;
	}

	@Override
	public void setValueFromAttribute(Attribute attr){
		throw new UnsupportedOperationException();
	}

}
