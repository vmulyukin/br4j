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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * {@link Attribute} descendant used to store information about material access permissions
 * of given {@link Card} object.<br>
 * Access could be granted to all users, chosen users, for users from specific departments 
 * or to users with specific roles.
 */
public class SecurityAttribute extends Attribute 
{
	private static final long serialVersionUID = 4L;
	private Collection accessList = new ArrayList();

	/**
	 * Gets permission settings for card's material as
	 * collection of {@link AccessListItem}
	 * @return collection of {@link AccessListItem} defining access permission for material
	 */
	public Collection getAccessList() {
		return accessList;
	}

	/**
	 * Sets permission settings for card's material
	 * @param accessList collection of {@link AccessListItem} defining access permission for material
	 */
	public void setAccessList(Collection accessList) {
		if (accessList == null)
			this.accessList.clear();
		else	
			this.accessList = accessList;
	}

	/**
	 * @see Attribute#getStringValue()
	 */
	public String getStringValue() {
		if (accessList == null)
			return "";
		StringBuffer value = new StringBuffer();
		Iterator itr = accessList.iterator();
		while (itr.hasNext()) {
			AccessListItem item = (AccessListItem) itr.next();
			switch(item.getType()) {
			case AccessListItem.TYPE_ROLE:
				value.append(Role.getRoleName(item.getRoleType(), ContextProvider.getContext().getLocale()));
				break;
			case AccessListItem.TYPE_DEPARTMENT:
				value.append(item.getDepartment().getValue());
				break;
			case AccessListItem.TYPE_PERSON:
				value.append(item.getPerson().getFullName());
				break;
			}
			if (itr.hasNext())
				value.append(", ");
		}
		return value.toString();
	}

	/**
	 * @see Attribute#getType()
	 */
	public Object getType() {
		return TYPE_SECURITY;
	}

	/**
	 * Checks if this instance contains same number of records as given
	 * {@link SecurityAttribute} attr.
	 * @throws IllegalArgumentException if attr is not a {@link SecurityAttribute} instance
	 */
	public boolean equalValue(Attribute attr) {
		if (!(attr instanceof SecurityAttribute))
			throw new IllegalArgumentException("Incorrect attribute type comparison: " +
					getClass().getName() + " [" + getId().getId() + "] against " +
					attr.getClass().getName() + " [" + attr.getId().getId() + "]");
		Collection otherList = ((SecurityAttribute) attr).getAccessList();
		if (accessList == null || otherList == null)
			return accessList == otherList;
		return accessList.size() == otherList.size();	//*****
	}

	/**
	 * @see Attribute#verifyValue()
	 */
	public boolean verifyValue() {
		return true;	//*****
	}

	public boolean isEmpty() {
		return accessList.isEmpty();
	}

	public void clear() {
		accessList.clear();
	}

	@Override
	public void setValueFromAttribute(Attribute attr) {
		if(this.getClass().isAssignableFrom(attr.getClass())){
			this.clear();
			this.accessList.addAll(((SecurityAttribute) attr).getAccessList());
		}		
	}
}
