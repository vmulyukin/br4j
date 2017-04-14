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
package com.aplana.dbmi.model;

import java.util.Date;

public class CardHistoryRecord {
	private ObjectId actionId;
	private String actionNameEn;
	private String actionNameRu;
	private String actorFullName;
	private String ipAddress;
	private Date date;
	private String wfmNameEn;
	private String wfmNameRu;
	private ObjectId startStatusId;
	private String startStatusNameEn;
	private String startStatusNameRu;
	private ObjectId endStatusId;
	private String endStatusNameEn;
	private String endStatusNameRu;
	private String versionId;
	private String isSuccess;
	private String idRec;
	private String delegateUserFullName;
	private String visibleAttr;
	
	public ObjectId getActionId() {
		return actionId;
	}
	
	public void setActionId(ObjectId actionId) {
		this.actionId = actionId;
	}
	
	public void setActionId(String actionId) {
		this.actionId = new ObjectId(String.class, actionId);
	}
	
	public String getActionNameEn() {
		return actionNameEn;
	}
	
	public void setActionNameEn(String actionNameEn) {
		this.actionNameEn = actionNameEn;
	}
	
	public String getActionNameRu() {
		return actionNameRu;
	}
	
	public void setActionNameRu(String actionNameRu) {
		this.actionNameRu = actionNameRu;
	}
	
	public String getActorFullName() {
		return actorFullName;
	}
	
	public void setActorFullName(String actorFullName) {
		this.actorFullName = actorFullName;
	}
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getActionName() {
		return ContextProvider.getContext().getLocaleString(actionNameRu, actionNameEn);
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getWfmNameEn() {
		return wfmNameEn;
	}

	public void setWfmNameEn(String wfmNameEn) {
		this.wfmNameEn = wfmNameEn;
	}

	public String getWfmNameRu() {
		return wfmNameRu;
	}

	public void setWfmNameRu(String wfmNameRu) {
		this.wfmNameRu = wfmNameRu;
	}

	public ObjectId getStartStatusId() {
		return startStatusId;
	}

	public void setStartStatusId(ObjectId startStatusId) {
		this.startStatusId = startStatusId;
	}

	public String getStartStatusNameEn() {
		return startStatusNameEn;
	}

	public void setStartStatusNameEn(String startStatusNameEn) {
		this.startStatusNameEn = startStatusNameEn;
	}

	public String getStartStatusNameRu() {
		return startStatusNameRu;
	}

	public void setStartStatusNameRu(String startStatusNameRu) {
		this.startStatusNameRu = startStatusNameRu;
	}

	public ObjectId getEndStatusId() {
		return endStatusId;
	}

	public void setEndStatusId(ObjectId endStatusId) {
		this.endStatusId = endStatusId;
	}

	public String getEndStatusNameEn() {
		return endStatusNameEn;
	}

	public void setEndStatusNameEn(String endStatusNameEn) {
		this.endStatusNameEn = endStatusNameEn;
	}

	public String getEndStatusNameRu() {
		return endStatusNameRu;
	}

	public String getDelegateUserFullName() {
        return delegateUserFullName;
    }

    public void setDelegateUserFullName(String delegateUserFullName) {
        this.delegateUserFullName = delegateUserFullName;
    }

    public void setEndStatusNameRu(String endStatusNameRu) {
		this.endStatusNameRu = endStatusNameRu;
	}
	
	public String getComment(){
		return ContextProvider.getContext().getLocaleString(getCommentRu(), getCommentEn());
	}

	public String getCommentEn(){
		if (startStatusId == null)
			return "";
		return "Move \"" + wfmNameEn + "\" (from \"" + startStatusNameEn + "\" to \"" + endStatusNameEn + "\")";
	}
	
	public String getCommentRu(){
		if (startStatusId == null)
			return "";
		return "������� \"" + wfmNameRu+ "\" (�� \"" + startStatusNameRu + "\" � \"" + endStatusNameRu + "\")";
	}
	
	public String getVersionId() {
	    return this.versionId;
	}

	public void setVersionId(String versionId) {
	    this.versionId = versionId;
	}

	public String getSuccess() {
		return this.isSuccess;
	}

	public void isSuccess(String isSuccess) {
		this.isSuccess = isSuccess;
	}
	
	public String getRecId() {
		return this.idRec;
	}
	
	public void setRecId(String idRec) {
		this.idRec = idRec;
	}
	
	public String getVisibleAttr() {
		return visibleAttr;
	}

	public void setVisibleAttr(String visibleAttr) {
		this.visibleAttr = visibleAttr;
	}
}
