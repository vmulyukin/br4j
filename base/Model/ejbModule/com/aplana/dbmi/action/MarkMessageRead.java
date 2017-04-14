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

import com.aplana.dbmi.model.Message;
import com.aplana.dbmi.model.ObjectId;

public class MarkMessageRead implements Action {
	
	private static final long serialVersionUID = 1L;
	private ObjectId messageId;
	private ObjectId personId;

	public Class getResultType() {
		return null;
	}

	public ObjectId getMessageId() {
		return messageId;
	}

	public void setMessageId(ObjectId messageId) {
		if (!Message.class.equals(messageId.getType()))
			throw new IllegalArgumentException("messageId must be a message ID");
		this.messageId = messageId;
	}

	public ObjectId getPersonId() {
		return personId;
	}

	public void setPersonId(ObjectId personId) {
		if (!Message.class.equals(messageId.getType()))
			throw new IllegalArgumentException("personId must be a person ID");
		this.personId = personId;
	}

}
