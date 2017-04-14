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
package com.aplana.dbmi.model.search.ext;

import java.util.Collection;

import com.aplana.dbmi.model.ObjectId;

public class FilteredObjectId extends LinkedObjectId {
	
	Collection<ObjectId> linkedCardStatus;
	boolean reverseCardStatus;

	public FilteredObjectId(Class type, Object id, String linkedCode, Collection<ObjectId> linkedCardStatus) {
		super(type, id, linkedCode);
		this.linkedCardStatus = linkedCardStatus;
		this.reverseCardStatus = false;
	}
	
	public FilteredObjectId(Class type, Object id, String linkedCode, Collection<ObjectId> linkedCardStatus, boolean reverseCardStatus) {
		super(type, id, linkedCode);
		this.linkedCardStatus = linkedCardStatus;
		this.reverseCardStatus = reverseCardStatus;
	}

	public FilteredObjectId(Class type, Object id, String linkedCode,
			Collection extraIds, Collection<ObjectId> linkedCardStatus) {
		super(type, id, linkedCode, extraIds);
		this.linkedCardStatus = linkedCardStatus;
		this.reverseCardStatus = false;
	}
	
	public FilteredObjectId(Class type, Object id, String linkedCode,
			Collection extraIds, Collection<ObjectId> linkedCardStatus, boolean reverseCardStatus) {
		super(type, id, linkedCode, extraIds);
		this.linkedCardStatus = linkedCardStatus;
		this.reverseCardStatus = false;
		this.reverseCardStatus = reverseCardStatus;
	}
	
	public Collection<ObjectId> getLinkedCardStatus() {
		return linkedCardStatus;
	}

	public void setLinkedCardStatus(Collection<ObjectId> linkedCardStatus) {
		this.linkedCardStatus = linkedCardStatus;
	}

	public boolean isReverseCardStatus() {
		return reverseCardStatus;
	}

	public void setReverseCardStatus(boolean reverseCardStatus) {
		this.reverseCardStatus = reverseCardStatus;
	}

}
