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
package com.aplana.dbmi.portlet;

import org.json.JSONArray;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.service.DataServiceBean;

/**
 * Data bean for {@link DocumentDataPortlet}. 
 * 
 * @author EStatkevich
 */
public class DocumentDataPortletSessionBean {
	
	private DataServiceBean serviceBean;
	private String message;
	private String header;
	private String docId;
	private Card baseCard;
	private String archiveReportURL;
	private boolean deloState = false;

	private JSONArray linkedFromDocs;
	private JSONArray linkedToDocs;
	private JSONArray attachments;
	private JSONArray infoMaterials;
	 
	
	
	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

	public JSONArray getAttachments() {
		return attachments;
	}

	public void setAttachments(JSONArray attachments) {
		this.attachments = attachments;
	}
	
	public JSONArray getLinkedFromDocs() {
		return linkedFromDocs;
	}

	public void setLinkedFromDocs(JSONArray linkedFromDocs) {
		this.linkedFromDocs = linkedFromDocs;
	}

	public JSONArray getLinkedToDocs() {
		return linkedToDocs;
	}

	public void setLinkedToDocs(JSONArray linkedToDocs) {
		this.linkedToDocs = linkedToDocs;
	}
	
	public Card getBaseCard() {
		return baseCard;
	}

	public void setBaseCard(Card baseCard) {
		this.baseCard = baseCard;
	}
	
	public DataServiceBean getServiceBean() {
		return serviceBean;
	}
	
	public void setServiceBean(DataServiceBean serviceBean) {
		this.serviceBean = serviceBean;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getArchiveReportURL() {
		return archiveReportURL;
	}

	public void setArchiveReportURL(String archiveReportURL) {
		this.archiveReportURL = archiveReportURL;
	}

	public boolean isDeloState() {
		return deloState;
	}

	public void setDeloState(boolean deloState) {
		this.deloState = deloState;
	}

	public JSONArray getInfoMaterials() {
		return infoMaterials;
	}

	public void setInfoMaterials(JSONArray infoMaterials) {
		this.infoMaterials = infoMaterials;
	}
}
