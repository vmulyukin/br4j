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
package org.aplana.br4j.dynamicaccess.db_export;


public class AccessRule {
    Long ruleId;
    Long templateId;
    Long statusId;
    String permHash;
    
    public static final String EMPTY_STRING = "$##$";
    public static final String BORDER = "|";
    

    public AccessRule(Long templateId, Long statusId, String permHash) {
        this.templateId = templateId;
        this.statusId = statusId;
        this.permHash = permHash;
    }

    public String getPermHash() {
		return permHash;
	}

	public void setPermHash(String permHash) {
		this.permHash = permHash;
	}

	public Long getRuleId() {
        return ruleId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public Long getStatusId() {
        return statusId;
    }

    void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    @Override
    public String toString() {
        return "AccessRule{" +
                "ruleId=" + ruleId +
                ", templateId=" + templateId +
                ", statusId=" + statusId +
                ", permHash=" + permHash +
                '}';
    }
}
