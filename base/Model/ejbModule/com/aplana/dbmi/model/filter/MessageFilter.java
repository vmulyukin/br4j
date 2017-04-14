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
package com.aplana.dbmi.model.filter;

import java.util.Date;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;

public class MessageFilter implements Filter {

	private static final long serialVersionUID = 1L;
	
	private ObjectId personId;
	private boolean unreadOnly = true;
	private Date startAfter = new Date(0);
	
	public MessageFilter() { }
	
	public MessageFilter(ObjectId personId) {
		setPersonId(personId);
	}
	
	public MessageFilter(ObjectId user, Date startAfter) {
		this.personId = user;
		if(startAfter != null){
			this.startAfter = startAfter;
		}
	}

	public ObjectId getPersonId() {
		return personId;
	}
	
	public void setPersonId(ObjectId personId) {
		if (personId != null && !Person.class.equals(personId.getType()))
			throw new IllegalArgumentException("personId must be a person ID");
		this.personId = personId;
	}
	
	public boolean isUnreadOnly() {
		return unreadOnly;
	}
	
	public void setUnreadOnly(boolean unreadOnly) {
		this.unreadOnly = unreadOnly;
	}
	
	public Date getStartAfter() {
		return startAfter;
	}
	
	public void setStartAfter(Date startAfter) {
		this.startAfter = startAfter;
	}
}
