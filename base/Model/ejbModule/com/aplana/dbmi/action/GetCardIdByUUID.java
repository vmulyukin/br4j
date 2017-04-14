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

import com.aplana.dbmi.model.ObjectId;

public class GetCardIdByUUID implements Action<ObjectId> {
    private static final long serialVersionUID = 1L;
    
    private String uuid;
    private ObjectId attrId=null;

    public Class<?> getResultType() {
    	return ObjectId.class;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * ������� �� �������� ����� ������ Action
     * @return
     */
	public ObjectId getAttrId() {
		return attrId;
	}
	
	/**
	 * ������������� ������� �� �������� ����� ������ Action
	 * @param attrId
	 */
	public void setAttrId(ObjectId attrId) {
		this.attrId = attrId;
	}
}