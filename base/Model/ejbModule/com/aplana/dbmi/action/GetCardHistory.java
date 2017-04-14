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

import com.aplana.dbmi.model.CardHistoryRecord;
import com.aplana.dbmi.model.ObjectId;

import java.util.List;

public class GetCardHistory implements Action<List<CardHistoryRecord>> {
	private static final long serialVersionUID = 1L;
	private ObjectId card;
	private long limit;
	private long offset;

	public ObjectId getCard() {
		return card;
	}

	public void setCard(ObjectId card) {
		this.card = card;
	}

	public void setLimit(long l) {
		this.limit = l;
	}

	public long getLimit() {
		return limit;
	}

	public void setOffset(long off) {
		this.offset = off;
	}

	public long getOffset() {
		return offset;
	}

	public Class getResultType() {
		return List.class;
	}
}
