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
package com.aplana.dbmi.model.workstation;

import com.aplana.dbmi.model.ObjectId;

public class SortAttribute extends AttributeValue {

    private boolean asc;
    private boolean byStatus;
    private boolean byTemplate;
    private NullsSortingPolicy nullsFirst = NullsSortingPolicy.getDefaultValue();
    
    private ObjectId templateId;
    private ObjectId statusId;
    
    private int sortGroup;
    
    public SortAttribute(String code, boolean isAsc) {
        super(code);
        setAsc(isAsc);
    }

    public SortAttribute(boolean isByTemplate, boolean isByStatus, boolean isAsc) {
        this(null, isAsc);
        setByTemplate(isByTemplate);
        setByStatus(isByStatus);
    }

    public SortAttribute(String code, String linkedCode, boolean linkedByPerson, boolean isAsc) {
        super(code, linkedCode, linkedByPerson);
        setAsc(isAsc);
    }

    public boolean isAsc() {
        return asc;
    }

    public void setAsc(boolean asc) {
        this.asc = asc;
    }

    public boolean isByStatus() {
        return byStatus;
    }

    public void setByStatus(boolean byStatus) {
        this.byStatus = byStatus;
    }

    public boolean isByTemplate() {
        return byTemplate;
    }

    public void setByTemplate(boolean byTemplate) {
        this.byTemplate = byTemplate;
    }
    
	public NullsSortingPolicy getNullsFirst() {
		return nullsFirst;
	}

	public void setNullsFirst(NullsSortingPolicy nullsFirst) {
		this.nullsFirst = nullsFirst;
	}

	public ObjectId getTemplateId() {
		return templateId;
	}

	public void setTemplateId(ObjectId templateId) {
		this.templateId = templateId;
	}

	public ObjectId getStatusId() {
		return statusId;
	}

	public void setStatusId(ObjectId statusId) {
		this.statusId = statusId;
	}

	public int getSortGroup() {
		return sortGroup;
	}

	public void setSortGroup(int sortGroup) {
		this.sortGroup = sortGroup;
	}
}
