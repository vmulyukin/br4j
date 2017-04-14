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
package com.aplana.cms.view_template;

import com.aplana.dbmi.model.workstation.NullsSortingPolicy;

public class SortViewAttribute {
	
	private ViewAttribute attribute;
	private boolean asc;
    private boolean byStatus;
    private boolean byTemplate;
    private NullsSortingPolicy nullsFirst;
    
    private String templateId;
    private String statusId;
    
    private int sortGroup;

	public ViewAttribute getAttribute() {
		return attribute;
	}

	public void setAttribute(ViewAttribute attribute) {
		this.attribute = attribute;
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

	public String getTemplateId() {
		return templateId;
	}
	
    public NullsSortingPolicy getNullsFirst() {
		return nullsFirst;
	}

	public void setNullsFirst(NullsSortingPolicy nullsFirst) {
		this.nullsFirst = nullsFirst;
	}
	
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getStatusId() {
		return statusId;
	}

	public void setStatusId(String statusId) {
		this.statusId = statusId;
	}

	public int getSortGroup() {
		return sortGroup;
	}

	public void setSortGroup(int sortGroup) {
		this.sortGroup = sortGroup;
	}
}
