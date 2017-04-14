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
package com.aplana.dbmi.ws.goststatisticservice;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class SendStatisticResponse {

	protected String uuid;
	protected Long   elmStatus;
	protected String elmStatusName;
	protected Long   basedocTemplate;
	protected Long   basedocStatus;
	protected String senderOrgFullName;
	protected String destOrgFullName;
	protected Date   basedocRegDate;
	protected String basedocRegNumber;
	protected Date   gostMessageCreateTime;
	protected String defaultOrgFullName;
	protected Date   elmCreatedDate;
	protected Date   notifReceivedCreated;
	protected Date   notifRegisteredCreated;

	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public Long getElmStatus() {
		return elmStatus;
	}
	public void setElmStatus(Long elmStatus) {
		this.elmStatus = elmStatus;
	}
	public String getElmStatusName() {
		return elmStatusName;
	}
	public void setElmStatusName(String elmStatusName) {
		this.elmStatusName = elmStatusName;
	}
	public Long getBasedocTemplate() {
		return basedocTemplate;
	}
	public void setBasedocTemplate(Long basedocTemplate) {
		this.basedocTemplate = basedocTemplate;
	}
	public Long getBasedocStatus() {
		return basedocStatus;
	}
	public void setBasedocStatus(Long basedocStatus) {
		this.basedocStatus = basedocStatus;
	}
	public String getSenderOrgFullName() {
		return senderOrgFullName;
	}
	public void setSenderOrgFullName(String senderOrgFullName) {
		this.senderOrgFullName = senderOrgFullName;
	}
	public String getDestOrgFullName() {
		return destOrgFullName;
	}
	public void setDestOrgFullName(String destOrgFullName) {
		this.destOrgFullName = destOrgFullName;
	}
	public Date getBasedocRegDate() {
		return basedocRegDate;
	}
	public void setBasedocRegDate(Date basedocRegDate) {
		this.basedocRegDate = basedocRegDate;
	}
	public String getBasedocRegNumber() {
		return basedocRegNumber;
	}
	public void setBasedocRegNumber(String basedocRegNumber) {
		this.basedocRegNumber = basedocRegNumber;
	}
	public Date getGostMessageCreateTime() {
		return gostMessageCreateTime;
	}
	public void setGostMessageCreateTime(Date gostMessageCreateTime) {
		this.gostMessageCreateTime = gostMessageCreateTime;
	}
	public String getDefaultOrgFullName() {
		return defaultOrgFullName;
	}
	public void setDefaultOrgFullName(String defaultOrgFullName) {
		this.defaultOrgFullName = defaultOrgFullName;
	}
	public Date getElmCreatedDate() {
		return elmCreatedDate;
	}
	public void setElmCreatedDate(Date elmCreatedDate) {
		this.elmCreatedDate = elmCreatedDate;
	}
	public Date getNotifReceivedCreated() {
		return notifReceivedCreated;
	}
	public void setNotifReceivedCreated(Date notifReceivedCreated) {
		this.notifReceivedCreated = notifReceivedCreated;
	}
	public Date getNotifRegisteredCreated() {
		return notifRegisteredCreated;
	}
	public void setNotifRegisteredCreated(Date notifRegisteredCreated) {
		this.notifRegisteredCreated = notifRegisteredCreated;
	}
}