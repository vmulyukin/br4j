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
package com.aplana.dbmi.archive.export;

import java.util.Date;

import com.aplana.dbmi.model.ObjectId;

/**
 * ������������� ������� action_log
 * ��� �������� � XML
 * @author ppolushkin
 *
 */
public class ActionLogHist {
	
	private String actionCode;
	private Date logDate;
	private ObjectId actorId;
	private String ipAddress;
	private ObjectId cardId;
	private ObjectId templateId;
	private ObjectId blockCode;
	private ObjectId attributeCode;
	private ObjectId personId;
	private ObjectId delegateUserId;
	private Long actionLogId;
	
	
	public String getActionCode() {
		return actionCode;
	}
	public void setActionCode(String actionCode) {
		this.actionCode = actionCode;
	}
	public Date getLogDate() {
		return logDate;
	}
	public void setLogDate(Date logDate) {
		this.logDate = logDate;
	}
	public ObjectId getActorId() {
		return actorId;
	}
	public void setActorId(ObjectId actorId) {
		this.actorId = actorId;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public ObjectId getCardId() {
		return cardId;
	}
	public void setCardId(ObjectId cardId) {
		this.cardId = cardId;
	}
	public ObjectId getTemplateId() {
		return templateId;
	}
	public void setTemplateId(ObjectId templateId) {
		this.templateId = templateId;
	}
	public ObjectId getBlockCode() {
		return blockCode;
	}
	public void setBlockCode(ObjectId blockCode) {
		this.blockCode = blockCode;
	}
	public ObjectId getAttributeCode() {
		return attributeCode;
	}
	public void setAttributeCode(ObjectId attributeCode) {
		this.attributeCode = attributeCode;
	}
	public ObjectId getPersonId() {
		return personId;
	}
	public void setPersonId(ObjectId personId) {
		this.personId = personId;
	}
	public ObjectId getDelegateUserId() {
		return delegateUserId;
	}
	public void setDelegateUserId(ObjectId delegateUserId) {
		this.delegateUserId = delegateUserId;
	}
	public Long getActionLogId() {
		return actionLogId;
	}
	public void setActionLogId(Long actionLogId) {
		this.actionLogId = actionLogId;
	}
	
}
