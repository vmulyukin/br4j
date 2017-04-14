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

/**
 * Simple {@link Notification} descendant. Could be created by any user, who will
 * became owner of Subscription and couldn't be changed. Modifications
 * and deleting are only permitted for owner.<br>
 * Notification messages about changes in {@link Card cards} subset monitored by
 * Subscription are sent to user who created it. 
 */
public class Subscription extends Notification 
{
	private static final long serialVersionUID = 2L;
	private ObjectId personId;

	/**
	 * Sets identifier for this Subscription object
	 * @param id desired value of identifier
	 */
	public void setId(long id) {
		super.setId(new ObjectId(Subscription.class, id));
	}

	/**
	 * Gets owner of Subscription object
	 * @return identifier of this Subscription object owner
	 */
	public ObjectId getPersonId() {
		return personId;
	}

	/**
	 * Sets owner of subscription object
	 * @param personId identifier of Subscription object owner
	 */
	public void setPersonId(ObjectId personId) {
		if (personId == null || !Person.class.equals(personId.getType()))
			throw new IllegalArgumentException("Not a person id");
		this.personId = personId;
	}
}
