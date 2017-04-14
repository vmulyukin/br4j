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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;

/**
 * ������������� ������� card_version
 * ��� �������� � XML
 * ������������ � ������ � AttributeValueHist
 * @author ppolushkin
 */

public class CardVersionHist extends Card {

	private static final long serialVersionUID = 9065233556964885100L;
	
	private Long versionId;
	private Date versionDate;
	private ObjectId parentId;
	private String fileName;
	private String url;
	private List<AttributeValueHist> avh;
	private Long actionLogId;
	
	
	public Long getVersionId() {
		return versionId;
	}
	public void setVersionId(Long versionId) {
		this.versionId = versionId;
	}
	public Date getVersionDate() {
		return versionDate;
	}
	public void setVersionDate(Date versionDate) {
		this.versionDate = versionDate;
	}
	public List<AttributeValueHist> getAvh() {
		if(avh == null) {
			avh = new ArrayList<AttributeValueHist>();
		}
		return avh;
	}
	public void setAvh(List<AttributeValueHist> avh) {
		this.avh = avh;
	}
	public ObjectId getParentId() {
		return parentId;
	}
	public void setParentId(ObjectId parentId) {
		this.parentId = parentId;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Long getActionLogId() {
		return actionLogId;
	}
	public void setActionLogId(Long actionLogId) {
		this.actionLogId = actionLogId;
	}
}
