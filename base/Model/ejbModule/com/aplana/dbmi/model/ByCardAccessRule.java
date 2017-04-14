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

public abstract class ByCardAccessRule extends AccessRule {

	private Attribute linkAttr;
	private Attribute intermedLinkAttr;
	private ObjectId linkedStateId;
	private ObjectId roleId;
	
	public Attribute getLinkAttribute() {
		return linkAttr;
	}
	
	public void setLinkAttribute(Attribute linkAttr) {
		if (linkAttr != null &&
				!CardLinkAttribute.class.equals(linkAttr.getId().getType()) &&
				!TypedCardLinkAttribute.class.equals(linkAttr.getId().getType()) &&
				!BackLinkAttribute.class.equals(linkAttr.getId().getType())/*&&
           		// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующую строчку необходимо раскомментарить
				!PersonAttribute.class.equals(linkAttr.getId().getType())*/)
			throw new IllegalArgumentException("Not a link attribute");
		this.linkAttr = linkAttr;
		if (linkAttr == null) {
			this.intermedLinkAttr = null;
			this.linkedStateId = null;
		}
	}
	
	public void setLinkAttribute(ObjectId linkAttrId) {
		if (linkAttrId == null) {
			this.linkAttr = null;
			this.intermedLinkAttr = null;
			this.linkedStateId = null;
		} else {
			if (!CardLinkAttribute.class.equals(linkAttrId.getType()) &&
				!TypedCardLinkAttribute.class.equals(linkAttrId.getType()) &&
				!BackLinkAttribute.class.equals(linkAttrId.getType()) /*&&
           		// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующую строчку необходимо раскомментарить
				!PersonAttribute.class.equals(linkAttrId.getType())*/)
				throw new IllegalArgumentException("Not a link attribute ID");
			this.linkAttr = (Attribute) Attribute.createFromId(linkAttrId);
		}
	}
	
	public Attribute getIntermediateLinkAttribute() {
		return intermedLinkAttr;
	}
	
	public void setIntermediateLinkAttribute(Attribute intermedLinkAttr) {
		if (intermedLinkAttr != null &&
				!CardLinkAttribute.class.equals(intermedLinkAttr.getId().getType()) &&
				!TypedCardLinkAttribute.class.equals(intermedLinkAttr.getId().getType()) &&
           		// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующую строчку необходимо раскомментарить
				/*!PersonAttribute.class.equals(intermedLinkAttr.getId().getType()) &&*/
				!BackLinkAttribute.class.equals(intermedLinkAttr.getId().getType()))
			throw new IllegalArgumentException("Not a link attribute");
		this.intermedLinkAttr = intermedLinkAttr;
	}
	
	public void setIntermediateLinkAttribute(ObjectId intermedLinkAttrId) {
		if (intermedLinkAttrId == null)
			this.intermedLinkAttr = null;
		else {
			if (linkAttr == null)
				throw new IllegalStateException("Set a link attribute first");
			if (!CardLinkAttribute.class.equals(intermedLinkAttrId.getType()) &&
				!TypedCardLinkAttribute.class.equals(intermedLinkAttrId.getType()) &&
           		// Если надо, чтобы в АС и АПС в профильных и персональных правилах использовались персон-атрибуты, следующую строчку необходимо раскомментарить
				/*!PersonAttribute.class.equals(intermedLinkAttr.getId().getType()) &&*/
				!BackLinkAttribute.class.equals(intermedLinkAttrId.getType()))
				throw new IllegalArgumentException("Not a link attribute ID");
			this.intermedLinkAttr = (Attribute) Attribute.createFromId(intermedLinkAttrId);
		}
	}

	public ObjectId getLinkedStateId() {
		return linkedStateId;
	}

	public void setLinkedStateId(ObjectId linkedStateId) {
		if (linkedStateId != null) {
			if (linkAttr == null)
				throw new IllegalStateException("Set a link attribute first");
			if (!CardState.class.equals(linkedStateId.getType()))
				throw new IllegalArgumentException("Not a status ID");
		}
		this.linkedStateId = linkedStateId;
	}
	
	public void setLinkedState(CardState linkedState) {
		setLinkedStateId(linkedState == null ? null : linkedState.getId());
	}

	public ObjectId getRoleId() {
		return roleId;
	}

	public void setRoleId(ObjectId roleId) {
		if (roleId != null && !SystemRole.class.equals(roleId.getType()))
			throw new IllegalArgumentException("Not a role ID");
		this.roleId = roleId;
	}
	
	public void setRole(SystemRole role) {
		setRoleId(role == null ? null : role.getId());
	}
}
