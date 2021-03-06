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
package com.aplana.ireferent.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WSO_TASKREPORT", propOrder = { "task", "executor", "comments",
	"attachments", "statusName", "declineReason", "armViewed", "reportText" })
public class WSOTaskReport extends WSOMTaskReport {

    @XmlElement(name = "TASK", required = true, nillable = true)
    private WSOMTask task;
    @XmlElement(name = "EXECUTOR", required = true, nillable = true)
    private WSOMPerson executor;
    @XmlElement(name = "COMMENTS", required = true, nillable = true)
    private String comments;
    @XmlElement(name = "ATTACHMENTS", required = true, nillable = true)
    private WSOCollection attachments;
    @XmlElement(name = "STATUSNAME", required = true, nillable = true)
    private String statusName;
    @XmlElement(name = "DECLINEREASON", required = true, nillable = true)
    private String declineReason;
    @XmlElement(name = "ARMVIEWED", required = true, nillable = true)
    private String armViewed;
    @XmlElement(name = "REPORTTEXT", required = true, nillable = true)
    private String reportText;
    /**
     * @return the task
     */
    public WSOMTask getTask() {
        return this.task;
    }
    /**
     * @param task the task to set
     */
    public void setTask(WSOMTask task) {
        this.task = task;
    }
    /**
     * @return the executor
     */
    public WSOMPerson getExecutor() {
        return this.executor;
    }
    /**
     * @param executor the executor to set
     */
    public void setExecutor(WSOMPerson executor) {
        this.executor = executor;
    }
    /**
     * @return the comments
     */
    public String getComments() {
        return this.comments;
    }
    /**
     * @param comments the comments to set
     */
    public void setComments(String comments) {
        this.comments = comments;
    }
    /**
     * @return the attachments
     */
    public WSOCollection getAttachments() {
        return this.attachments;
    }
    /**
     * @param attachments the attachments to set
     */
    public void setAttachments(WSOCollection attachments) {
        this.attachments = attachments;
    }
    /**
     * @return the statusName
     */
    public String getStatusName() {
        return this.statusName;
    }
    /**
     * @param statusName the statusName to set
     */
    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
	public String getDeclineReason() {
		return declineReason;
	}
	public void setDeclineReason(String declineReason) {
		this.declineReason = declineReason;
	}
	public String getArmViewed() {
		return this.armViewed;
	}
	public void setArmViewed(String armViewed) {
		this.armViewed = armViewed;
	}
	public String getReportText() {
		return reportText;
	}
	public void setReportText(String reportText) {
		this.reportText = reportText;
	}
}
