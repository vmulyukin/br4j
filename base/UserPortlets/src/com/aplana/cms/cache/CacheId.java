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
package com.aplana.cms.cache;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.ObjectAction;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.User;

public class CacheId
{
	private Object queryId;
	private ObjectId userId;
	
	public CacheId(ObjectId objectId, ObjectId userId) {
		this.queryId = objectId;
		this.userId = userId;
	}
	
	public CacheId(ObjectId objectId, User user) {
		this(objectId, user.getPerson().getId());
	}
	
	public CacheId(Action action, ObjectId userId) {
		if (action instanceof ListProject) {
			ListProject project = (ListProject) action;
			this.queryId = project.getObjectId().getId().toString() +
					":" + project.getAttribute().getId().toString();
		} else if (action instanceof Search) {
			Search search = (Search) action;
			StringBuffer id = new StringBuffer();
			if (search.isByCode()) {
				id.append("#");
				id.append(search.getWords()/*.replaceAll("\\s", "")*/);
			} else
				id.append(search.getNameEn());
			this.queryId = id.toString();
		} else if (action instanceof ObjectAction)
			this.queryId = ((ObjectAction) action).getObjectId();
		else
			this.queryId = action.getClass();
		this.userId = userId;
	}
	
	public CacheId(Action action, User user) {
		this(action, user.getPerson().getId());
	}
	
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof CacheId))
			return false;
		/*if (hashCode() != obj.hashCode())
			return false;*/
		CacheId id = (CacheId) obj;
		return queryId.equals(id.queryId) && userId.equals(id.userId);
	}

	public int hashCode() {
		return queryId.hashCode() ^ userId.hashCode();
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(userId.getId().toString()).append("::");
		if (queryId instanceof Search)
			buf.append("Search:").append(((Search) queryId).getNameEn());
		else
			buf.append(queryId.toString());
		return buf.toString();
	}
}
