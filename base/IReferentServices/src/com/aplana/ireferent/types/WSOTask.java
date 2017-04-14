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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WSO_TASK", propOrder = { "comments", "attachments",
	"zamExecutors", "viewers", "dateFact", "dateSign" })
public class WSOTask extends WSOMTask {
    @XmlElement(name = "COMMENTS", required = true, nillable = true)
    private String comments;
    @XmlElement(name = "ATTACHMENTS", required = true, nillable = true)
    private WSOCollection attachments;
    @XmlElement(name = "ZAMEXECUTORS", required = true, nillable = true)
    private WSOCollection zamExecutors;
    @XmlElement(name = "VIEWERS", required = true, nillable = true)
    private WSOCollection viewers;
    @XmlElement(name = "DATEFACT", required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar dateFact;
    @XmlElement(name = "DATESIGN", required = true, nillable = true)
    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar dateSign;

    /**
     * @return the comments
     */
    public String getComments() {
	return this.comments;
    }

    /**
     * @param comments
     *                the comments to set
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
     * @param attachments
     *                the attachments to set
     */
    public void setAttachments(WSOCollection attachments) {
	this.attachments = attachments;
    }

    /**
     * @return the zamExecutors
     */
    public WSOCollection getZamExecutors() {
	return this.zamExecutors;
    }

    /**
     * @param zamExecutors
     *                the zamExecutors to set
     */
    public void setZamExecutors(WSOCollection zamExecutors) {
	this.zamExecutors = zamExecutors;
    }

    /**
     * @return the viewers
     */
    public WSOCollection getViewers() {
	return this.viewers;
    }

    /**
     * @param viewers
     *                the viewers to set
     */
    public void setViewers(WSOCollection viewers) {
	this.viewers = viewers;
    }

    /**
     * @return the dateFact
     */
    public XMLGregorianCalendar getDateFact() {
	return this.dateFact;
    }

    /**
     * @param dateFact
     *                the dateFact to set
     */
    public void setDateFact(XMLGregorianCalendar dateFact) {
	this.dateFact = dateFact;
    }

    /**
     * @return the dateSign
     */
    public XMLGregorianCalendar getDateSign() {
	return this.dateSign;
    }

    /**
     * @param dateSign
     *                the dateSign to set
     */
    public void setDateSign(XMLGregorianCalendar dateSign) {
	this.dateSign = dateSign;
    }
}
