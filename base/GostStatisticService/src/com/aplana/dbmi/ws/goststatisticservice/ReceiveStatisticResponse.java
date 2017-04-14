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
public class ReceiveStatisticResponse {

	protected String uuid;
	protected Date created;
	protected Date registrationDate;
	protected Long status;
	protected String notifReceivedId;
	protected Date notifReceivedCreated;
	protected String notifRegId;
	protected Date notifRegCreated;
	protected Date incomingCreated;
	protected Date incomingRegistered;
	protected String incomingRegNum;
	protected String incomingRegRejectReason;

	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public Date getRegistrationDate() {
		return registrationDate;
	}
	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}
	public Long getStatus() {
		return status;
	}
	public void setStatus(Long status) {
		this.status = status;
	}
	public String getNotifReceivedId() {
		return notifReceivedId;
	}
	public void setNotifReceivedId(String notifReceivedId) {
		this.notifReceivedId = notifReceivedId;
	}
	public Date getNotifReceivedCreated() {
		return notifReceivedCreated;
	}
	public void setNotifReceivedCreated(Date notifReceivedCreated) {
		this.notifReceivedCreated = notifReceivedCreated;
	}
	public String getNotifRegId() {
		return notifRegId;
	}
	public void setNotifRegId(String notifRegId) {
		this.notifRegId = notifRegId;
	}
	public Date getNotifRegCreated() {
		return notifRegCreated;
	}
	public void setNotifRegCreated(Date notifRegCreated) {
		this.notifRegCreated = notifRegCreated;
	}
	public Date getIncomingCreated() {
		return incomingCreated;
	}
	public void setIncomingCreated(Date incomingCreated) {
		this.incomingCreated = incomingCreated;
	}
	public Date getIncomingRegistered() {
		return incomingRegistered;
	}
	public void setIncomingRegistered(Date incomingRegistered) {
		this.incomingRegistered = incomingRegistered;
	}
	public String getIncomingRegNum() {
		return incomingRegNum;
	}
	public void setIncomingRegNum(String incomingRegNum) {
		this.incomingRegNum = incomingRegNum;
	}
	public String getIncomingRegRejectReason() {
		return incomingRegRejectReason;
	}
	public void setIncomingRegRejectReason(String incomingRegRejectReason) {
		this.incomingRegRejectReason = incomingRegRejectReason;
	}
}