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
package com.aplana.dbmi.service.impl;

import com.aplana.dbmi.model.ObjectId;

/**
 * {@link QueryBase} descendant, base class for queries
 * used to fetch list of children {@link com.aplana.dbmi.model.DataObject objects} for given
 * parent object.
 * <br>
 * Usually single {@link ChildrenQueryBase} descendant needs to be implemented
 * for each type of children
 * <br>
 * @see DataServiceBean#listChildren(com.aplana.dbmi.service.User, ObjectId, Class)  
 */
public abstract class ChildrenQueryBase extends QueryBase
{
	private ObjectId parent;

	/**
	 * Gets identifier of parent object children of which needs to be fetched
	 * @return identifier of parent object children of which needs to be fetched
	 */
	public ObjectId getParent() {
		return parent;
	}

	/**
	 * Sets identifier of parent object
	 * @param parent identifier of parent object
	 */
	public void setParent(ObjectId parent) {
		this.parent = parent;
	}
	
	@Override
	public int hashCode() {
		int hash = super.hashCode();
		hash = hash ^ ((parent != null) ? parent.hashCode() : 12314678);
		return hash;
	}
}
