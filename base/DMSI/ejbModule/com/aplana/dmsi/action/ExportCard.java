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
package com.aplana.dmsi.action;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ExportCardToXml.Result;
import com.aplana.dbmi.model.ObjectId;

public abstract class ExportCard implements Action {

    private ObjectId cardId;
    private ObjectId recipientId;

    public Class<?> getResultType() {
	return Result.class;
    }

    public ObjectId getCardId() {
	return this.cardId;
    }

    public void setCardId(ObjectId cardId) {
	this.cardId = cardId;
    }

    public ObjectId getRecipientId() {
	return this.recipientId;
    }

    public void setRecipientId(ObjectId recipientId) {
	this.recipientId = recipientId;
    }

}
