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
package com.aplana.dbmi.action;

import java.util.ArrayList;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.model.ObjectId;

public class BatchAsyncExecution<T extends Action<?>> implements Action<Void> {
	private static final long serialVersionUID = 1L;

	private ArrayList<T> actions = new ArrayList<T>(0);
	private ObjectId operationType;
	private ObjectId attrToParent;
	
	@Override
	public Class<?> getResultType() {
		return null;
	}
	
	public ArrayList<T> getActions() {
		return actions;
	}
	
	public void setActions(ArrayList<T> actions) {
		if (actions != null)
			this.actions = actions;
	}

	public ObjectId getOperationType() {
		return operationType;
	}

	public void setOperationType(ObjectId operationType) {
		this.operationType = operationType;
	}
	
	public void setAttrToParent(ObjectId attrToParent) {
		this.attrToParent = attrToParent;
	}
	
	public ObjectId getAttrToParent() {
		return attrToParent;
	}
}
