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
package com.aplana.dbmi.action.file;

import com.aplana.dbmi.action.ObjectAction;
import com.aplana.dbmi.model.ObjectId;

/**
 * Action used to copy material file from one card to another
 * @author DSultanbekov
 */
public class CopyMaterial implements ObjectAction<Void> {
	private static final long serialVersionUID = 1L;
	
	private ObjectId fromCardId;
	private ObjectId toCardId;
	
	public ObjectId getFromCardId() {
		return fromCardId;
	}

	public void setFromCardId(ObjectId fromCardId) {
		this.fromCardId = fromCardId;
	}

	public ObjectId getToCardId() {
		return toCardId;
	}

	public void setToCardId(ObjectId toCardId) {
		this.toCardId = toCardId;
	}

	public Class<?> getResultType() {
		return null;
	}

	public ObjectId getObjectId() {
		return toCardId;
	}

}
