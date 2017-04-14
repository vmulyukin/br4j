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
package com.aplana.dbmi.model;

public class PersonAccessRule extends ByCardAccessRule {

	private static final long serialVersionUID = 2L;
	
	private PersonAttribute personAttr;
	
	public PersonAttribute getPersonAttribute() {
		return personAttr;
	}
	
	public void setPersonAttribute(PersonAttribute personAttr) {
		if (personAttr == null)
			throw new IllegalArgumentException("Person attribute can't be null");
		this.personAttr = personAttr;
	}
	
	public void setPersonAttribute(ObjectId personAttrId) {
		if (personAttrId == null)
			throw new IllegalArgumentException("Person attribute can't be null");
		if (!PersonAttribute.class.equals(personAttrId.getType()))
			throw new IllegalArgumentException("Not a person attribute ID");
		this.personAttr = (PersonAttribute) PersonAttribute.createFromId(personAttrId);
	}
}
