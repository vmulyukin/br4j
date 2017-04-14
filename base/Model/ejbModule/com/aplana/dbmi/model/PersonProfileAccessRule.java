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

public class PersonProfileAccessRule extends ByCardAccessRule {

	private static final long serialVersionUID = 2L;
	
	private Attribute profileAttr;
	private Attribute targetAttr;
	
	public Attribute getProfileAttribute() {
		return profileAttr;
	}
	
	public void setProfileAttribute(Attribute profileAttr) {
		if (profileAttr == null)
			throw new IllegalArgumentException("Profile attribute can't be null");
		if (!CardLinkAttribute.class.equals(profileAttr.getId().getType()) &&
				!PersonAttribute.class.equals(profileAttr.getId().getType()))
				throw new IllegalArgumentException("Not a profile attribute ID");
		this.profileAttr = profileAttr;
	}
	
	public void setProfileAttribute(ObjectId profileAttrId) {
		if (profileAttrId == null)
			throw new IllegalArgumentException("Profile attribute can't be null");
		if (!CardLinkAttribute.class.equals(profileAttrId.getType()) &&
				!PersonAttribute.class.equals(profileAttrId.getType()))
				throw new IllegalArgumentException("Not a profile attribute ID");
		this.profileAttr = (Attribute) PersonAttribute.createFromId(profileAttrId);
	}
	
	public Attribute getTargetAttribute() {
		return targetAttr;
	}
	
	public void setTargetAttribute(Attribute targetAttr) {
		if (targetAttr == null)
			throw new IllegalArgumentException("Target attribute can't be null");
		if (!CardLinkAttribute.class.equals(targetAttr.getId().getType()) &&
			!PersonAttribute.class.equals(targetAttr.getId().getType()))
			throw new IllegalArgumentException("Not a target attribute ID");
		this.targetAttr = targetAttr;
	}
	
	public void setTargetAttribute(ObjectId targetAttrId) {
		if (targetAttrId == null)
			throw new IllegalArgumentException("Target attribute can't be null");
		if (!CardLinkAttribute.class.equals(targetAttrId.getType()) &&
			!PersonAttribute.class.equals(targetAttrId.getType()))
			throw new IllegalArgumentException("Not a target attribute ID");
		this.targetAttr = (Attribute) PersonAttribute.createFromId(targetAttrId);
	}
}
