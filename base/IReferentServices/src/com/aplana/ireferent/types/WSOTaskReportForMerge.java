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

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WSO_TASKREPORT_FOR_MERGE", propOrder = {"executor", "hidenApprover", "comments",
	"attachments", "statusName", "declineReason", "armViewed", "reportText" })
public class WSOTaskReportForMerge extends WSOMTaskReport {

    @XmlElement(name = "EXECUTOR", required = true, nillable = true)
    private WSOMPerson executor;
    @XmlElement(name = "HIDENAPPROVER", required = true, nillable = true)
    private WSOCollection hidenApprover;
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
    @XmlTransient
    protected Date doneDate;	//���� ���������� ������
    @XmlElement(name = "REPORTTEXT", required = true, nillable = true)
    private String reportText;
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
	public WSOCollection getHidenApprover() {
		return hidenApprover;
	}
	public void setHidenApprover(WSOCollection hidenApprover) {
		this.hidenApprover = hidenApprover;
	}
	public String getArmViewed() {
		return this.armViewed;
	}

	public void setArmViewed(String armViewed) {
		this.armViewed = armViewed;
	}
	@XmlTransient
	public Date getDoneDate() {
		return doneDate;
	}
	
	public void setDoneDate(Date doneDate) {
		this.doneDate = doneDate;
	}
	public String getReportText() {
		return reportText;
	}
	public void setReportText(String reportText) {
		this.reportText = reportText;
	}
	
}
