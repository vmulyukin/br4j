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
package com.aplana.dbmi.service.impl.workstation;

import java.io.Serializable;

/**
 * Represents DTO empty card
 * It contains card identifier and template identifier
 * 
 * @author skashanski
 *
 */
public class EmptyCard implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
     * Non defined status
     */
    public final static long NOT_DEFINED = -1L;
	
	private long cardId;
	
	private long templateId = NOT_DEFINED;

    private long statusId = NOT_DEFINED;

    public EmptyCard(long cardId) {
        this.cardId = cardId;
    }

    public EmptyCard(long cardId, long templateId) {
		this.cardId = cardId;
		this.templateId = templateId;
	}

	public EmptyCard(long cardId, long templateId, long statusId) {
		this.cardId = cardId;
		this.templateId = templateId;
		this.statusId = statusId;
	}

	public long getId() {
		return cardId;
	}

	public void setId(long cardId) {
		this.cardId = cardId;
	}

	public long getTemplateId() {
		return templateId;
	}

	public void setTemplateId(long templateId) {
		this.templateId = templateId;
	}

    public long getStatusId() {
        return statusId;
    }

    public void setStatusId(long statusId) {
        this.statusId = statusId;
    }
}
