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
package com.aplana.dbmi.support.action;

import java.util.Date;
import java.util.List;

import com.aplana.dbmi.action.CardAction;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;

public class ProcessGroupResolution implements CardAction<List<ChangeState>> {
	
	private static final long serialVersionUID = 1L;
	
	private Card currentResolution;
	private Date personalControlDate;
	
	private boolean onlyCreate = false;
	private List<ObjectId> docs;

	@Override
	public Class<?> getResultType() {
		return null;
	}

	@Override
	public ObjectId getObjectId() {
		return getCard().getId();
	}

	public Card getCard() {
		return currentResolution;
	}

	public void setCurrentResolution(Card currentResolution) {
		this.currentResolution = currentResolution;
	}

	public Date getPersonalControlDate() {
		return personalControlDate;
	}

	public void setPersonalControlDate(Date personalControlDate) {
		this.personalControlDate = personalControlDate;
	}

	public boolean isOnlyCreate() {
		return onlyCreate;
	}

	public void setOnlyCreate(boolean onlyCreate) {
		this.onlyCreate = onlyCreate;
	}

	public List<ObjectId> getDocs() {
		return docs;
	}

	public void setDocs(List<ObjectId> docs) {
		this.docs = docs;
	}

}
