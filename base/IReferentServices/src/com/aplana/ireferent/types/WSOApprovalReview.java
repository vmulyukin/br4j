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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

import com.aplana.ireferent.actions.Reviewed;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WSOAPPROVALREVIEW", propOrder = {
    "approval",
    "reviewer",
    "deadlineDate",
    "beginDate",
    "reviewDate",
    "solution",
    "attachments"
})
public class WSOApprovalReview extends WSOMApprovalReview implements Reviewed {
    @XmlElement(name = "APPROVAL", required = true, nillable = true)
    private WSOMApproval approval;
    @XmlElement(name = "REVIEWER", required = true, nillable = true)
    private WSOMPerson reviewer;
    @XmlElement(name = "DEADLINEDATE", required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar deadlineDate;
    @XmlElement(name = "BEGINDATE", required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar beginDate;
    @XmlElement(name = "REVIEWDATE", required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar reviewDate;
    @XmlElement(name = "SOLUTION", required = true, nillable = true)
    private WSOApprovalReviewSolution solution;
    @XmlElement(name = "ATTACHMENTS", required = true, nillable = true)
    private WSOCollection attachments;

    public WSOMApproval getApproval() {
        return this.approval;
    }
    public void setApproval(WSOMApproval approval) {
        this.approval = approval;
    }
    public WSOMPerson getReviewer() {
        return this.reviewer;
    }
    public void setReviewer(WSOMPerson reviewer) {
        this.reviewer = reviewer;
    }
    public XMLGregorianCalendar getDeadlineDate() {
        return this.deadlineDate;
    }
    public void setDeadlineDate(XMLGregorianCalendar deadlineDate) {
        this.deadlineDate = deadlineDate;
    }
    public XMLGregorianCalendar getBeginDate() {
        return this.beginDate;
    }
    public void setBeginDate(XMLGregorianCalendar beginDate) {
        this.beginDate = beginDate;
    }
    public XMLGregorianCalendar getReviewDate() {
        return this.reviewDate;
    }
    public void setReviewDate(XMLGregorianCalendar reviewDate) {
        this.reviewDate = reviewDate;
    }
    public WSOApprovalReviewSolution getSolution() {
        return this.solution;
    }
    public void setSolution(WSOApprovalReviewSolution solution) {
        this.solution = solution;
    }
    public WSOCollection getAttachments() {
        return this.attachments;
    }
    public void setAttachments(WSOCollection attachments) {
        this.attachments = attachments;
    }
}
