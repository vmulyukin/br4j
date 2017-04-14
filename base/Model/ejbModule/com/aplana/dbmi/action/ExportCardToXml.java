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

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TypeStandard;

public class ExportCardToXml implements ObjectAction {

    private static final long serialVersionUID = 1L;

    private ObjectId cardId;
    private ObjectId recipientId;
    private TypeStandard typeStandard;

    public Class<?> getResultType() {
	return Result.class;
    }

    public ObjectId getObjectId() {
	return this.cardId;
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

    public TypeStandard getTypeStandard() {
		return typeStandard;
	}

	public void setTypeStandard(TypeStandard typeStandard) {
		this.typeStandard = typeStandard;
	}

    public static class Result {
	private final transient InputStream data;
	private final Map<ObjectId, String> files;
	private Map<String, Object> additionalInfo;

	public Result(InputStream data, Map<ObjectId, String> files) {
	    this.data = data;
	    this.files = files;
	}

	public InputStream getData() {
	    return this.data;
	}

	public Map<ObjectId, String> getFiles() {
	    return Collections.unmodifiableMap(this.files);
	}

	private Map<String, Object> getAdditionalInfo(){
	    if (this.additionalInfo == null) {
		this.additionalInfo = new HashMap<String, Object>();
	    }
	    return this.additionalInfo;
	}

	public void addInfo(String key, Object infoData) {
	    getAdditionalInfo().put(key, infoData);
	}

	public Object getInfo(String key) {
	    return getAdditionalInfo().get(key);
	}

	public Map<String, Object> getInfos() {
	    return Collections.unmodifiableMap(getAdditionalInfo());
	}
    }

}
