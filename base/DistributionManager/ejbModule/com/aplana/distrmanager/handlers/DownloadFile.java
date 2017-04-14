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
package com.aplana.distrmanager.handlers;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.distrmanager.LoadSaveDocFacade;

public class DownloadFile {
	
	private final Log logger = LogFactory.getLog(getClass());
	
	private static final String DOWNLOAD_ERROR = "jbr.DistributionManager.DownloadFile.errorDownload";
	
	private LoadSaveDocFacade changerBean = null;
	
	private DownloadFile() {
	}
	
	private void init(LoadSaveDocFacade changerBean) {
		this.changerBean = changerBean;
	}
	
	public static DownloadFile instance(LoadSaveDocFacade changerBean) {
		DownloadFile df = new DownloadFile();
		df.init(changerBean);
		return df;
	}

	public File handle(ObjectId docBaseId, ObjectId elmCardId, ObjectId msgGostId, Map<ObjectId, String> attachments) throws Exception {
		try {
			// ��������� ���������� ������
			final LoadSaveDocFacade.PacketDescriptor packetDescriptor = new LoadSaveDocFacade.PacketDescriptor();
			packetDescriptor.setBaseDocumentId(docBaseId);
			packetDescriptor.setDistribElementId(elmCardId);
			packetDescriptor.setMsgGostId(msgGostId);
			
			//��������� ��� � ��
			File file = changerBean.downloadDocument(
							new LoadSaveDocFacade.DocumentDescriptor(
									Collections.unmodifiableMap(attachments), 
									packetDescriptor
							)
			);
			return file;
		} catch(Exception exDownload) {
			logError(docBaseId, elmCardId, msgGostId, DOWNLOAD_ERROR, exDownload);
			throw exDownload;
		}
	}
	
	private void logError(ObjectId docBaseId, ObjectId elmCardId, ObjectId msgGostId, String msgError, Exception e) {
		String error = String.
			format("{%s} docBaseId: {%s}; elmId: {%s}; msgGostId: {%s};", 
					(null == msgError)?"null":msgError,
					(null == docBaseId)?"null":docBaseId.getId(), 
					(null == elmCardId)?"null":elmCardId.getId(), 
					(null == msgGostId)?"null":msgGostId.getId()
			);
			logger.error(error, e);
	}
}
