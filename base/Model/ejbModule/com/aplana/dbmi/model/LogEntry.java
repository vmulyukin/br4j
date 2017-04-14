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

import java.util.Date;

/**
 * Class used to represent entry in system log (record from ACTION_LOG table).
 * Every logged event should belong to one of types, defined in ACTION table in database.
 * Additionally LogEntry instance contains information about user, who performed logged action,
 * and information about object which was changed during this action.
 */
public class LogEntry extends DataObject
{
	private static final long serialVersionUID = 1L;
	private String event;
	private ObjectId object;
	private Person user;
	private String address;
	private Date timestamp;
	private Person realUser;
	private ObjectId uid;
	private ObjectId parentUid;
	private Integer idLogAction;
	
	/**
	 * Address of user who performed logged action
	 * @param address address of user who performed logged action
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * Sets identifier of the type of logged action.
	 * Should be one of types, listed in ACTION table
	 * @param event String identifier of the type of logged action.
	 */
	public void setEvent(String event) {
		this.event = event;
	}

	/**
	 * Sets identifier of object changed during logged action.
	 * It should be identifier of one of the following types: {@link Card},
	 * {@link Person}, {@link Template}, {@link TemplateBlock},
	 * or one of {@link Attribute} descendants. 
	 * @param object identifier of object changed during logged action
	 */
	public void setObject(ObjectId object) {
		this.object = object;
	}

	/**
	 * Sets timestamp of logged action
	 * @param timestamp timestamp of logged action
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Sets user who performed logged action
	 * @param user {@link Person} object representing user, who performed logged action
	 */
	public void setUser(Person user) {
		this.user = user;
	}

	/**
	 * Gets identifier of the type of logged action.
	 * Should be one of types, listed in ACTION table
	 * @return String identifier of the type of logged action
	 */
	public String getEvent() {
		return event;
	}

	/**
	 * Sets identifier of object changed during logged action
	 * @return identifier of object changed during logged action
	 */
	public ObjectId getObject() {
		return object;
	}

	/**
	 * Gets user who performed logged action		
	 * @return {@link Person} object representing user, who performed logged action
	 */
	public Person getUser() {
		return user;
	}
	
	/**
	 * Gets address of user who performed logged action
	 * @return address address of user who performed logged action
	 */
	public String getAddress() {
		return address;
	}
	
	/**
	 * Gets timestamp of logged action
	 * @return timestamp of logged action
	 */
	public Date getTimestamp() {
		return timestamp;
	}

    public Person getRealUser() {
        return realUser;
    }

    public void setRealUser(Person realUser) {
        this.realUser = realUser;
    }
    
    public ObjectId getUid() {
		return uid;
	}
    
    public void setUid(ObjectId uid) {
		this.uid = uid;
	}
    
    public ObjectId getParentUid() {
		return parentUid;
	}
    
    public void setParentUid(ObjectId parentUid) {
		this.parentUid = parentUid;
	}

	public Integer getIdLogAction() {
		return idLogAction;
	}

	public void setIdLogAction(Integer idLogAction) {
		this.idLogAction = idLogAction;
	}
}
