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

import java.util.Collection;

/**
 * {@link Notification} descendant that could be only 
 * created and changed by users with 'Editor' or 'Administrator' roles. <br>
 * Each Distribution object defines set of users who will receive notification messages.
 * Set of notifications recipients is defined as collection of {@link AccessListItem}
 * objects complemented by set of regions to which user receiving notification should belong.<br>  
 * All of these users will receive notification about changes in {@link Card cards} subset
 * monitored by given Distribution object.
 */
public class Distribution extends Notification
{
	private static final long serialVersionUID = 2L;
	private ObjectId creator;
	private String creatorName;
	private Collection regions;
	private Collection accessList;
	
	/**
	 * Sets Distribution object identifier
	 * @param id desired value of identifier
	 */
	public void setId(long id) {
		super.setId(new ObjectId(Distribution.class, id));
	}

	/**
	 * Gets identifier of user who created this Distribution object.
	 * @return identifier of {@link Person} object representing user
	 * 	who created this Distribution object.
	 */
	public ObjectId getCreator() {
		return creator;
	}

	/**
	 * Gets name of user who created this Distribution object.
	 * @return name of user who created this Distribution object.
	 */
	public String getCreatorName() {
		return creatorName;
	}

	/**
	 * Sets name of user who created this Distribution object.
	 * @param creatorName name of user who created this Distribution object.
	 */
	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}

	/**
	 * Sets identifier of user who created this Distribution object.
	 * @param creator identifier of user who created this Distribution object.
	 * @throws IllegalArgumentException if value of creator argument is null or
	 * if it is not a identifier of {@link Person} object.
	 */
	public void setCreator(ObjectId creator) {
		if (creator == null || !Person.class.equals(creator.getType()))
			throw new IllegalArgumentException("Not a person id");
		this.creator = creator;
	}

	/**
	 * Returns collection of regions to which 
	 * recipients of notification messages should belong  
	 * @return collection of {@link ReferenceValue} objects 
	 * from dictionary {@link Reference#ID_REGION}
	 */
	public Collection getRegions() {
		return regions;
	}

	/**
	 * Sets collection of regions to which 
	 * recipients of notification messages should belong 
 	 * @param regions collection of {@link ReferenceValue} objects 
	 * from dictionary {@link Reference#ID_REGION}
	 */
	public void setRegions(Collection regions) {
		this.regions = regions;
	}

	/**
	 * Gets collection of {@link AccessListItem} objects used 
	 * to define set of users who will receive notification messages.  
	 * @return collection of {@link AccessListItem} objects
	 * defining set of users who will receive notification messages.
	 */
	public Collection getAccessList() {
		return accessList;
	}

	/**
	 * Sets collection of {@link AccessListItem} objects used 
	 * to define set of users who will receive notification messages.  
	 * @param accessList collection of {@link AccessListItem} objects
	 * defining set of users who will receive notification messages.
	 */
	public void setAccessList(Collection accessList) {
		this.accessList = accessList;
	}
}
