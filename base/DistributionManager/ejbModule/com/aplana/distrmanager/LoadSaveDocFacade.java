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
package com.aplana.distrmanager;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.distrmanager.exceptions.ConfigurationException;
import com.aplana.distrmanager.exceptions.LockDeleteException;
import com.aplana.distrmanager.exceptions.SaveException;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

public class LoadSaveDocFacade {

	private LoadDoc documentsUploader;
	private SaveDoc documentsDownloader;
	private String configFilePath;

	public LoadSaveDocFacade() {
	}

	public void init() {
		URL configUrl;
		Properties options = null;
		try {
			configUrl = Portal.getFactory().getConfigService().getConfigFileUrl(configFilePath);
			options = PropertiesLoaderUtils.loadProperties(new UrlResource(configUrl));
		} catch (IOException ex) {
			throw new IllegalStateException("It is impossible to read properties from " + configFilePath, ex);
		}

		if (documentsDownloader == null) {
			throw new ConfigurationException("Documents downloader was not set");
		}
		if (documentsUploader == null) {
			throw new ConfigurationException("Documents uploader was not set");
		}
		documentsDownloader.setOptions(options);
		documentsUploader.setOptions(options);
	}

	public File downloadDocument(DocumentDescriptor descriptor) throws SaveException {
		return documentsDownloader.downloadDocument(
						descriptor.getAttachments(),
						descriptor.getLogDescriptor()
						);
	}
	
	public void unlockDirectoriesDownloader(File file) throws LockDeleteException {
		documentsDownloader.unlockDirectory(file);
	}

	public static class DocumentDescriptor {
		
		private Map<ObjectId, String> attachments;
		private PacketDescriptor logDescriptor;

		public DocumentDescriptor(Map<ObjectId, String> attachments, PacketDescriptor logDescriptor) {
			super();
			this.attachments = attachments;
			this.logDescriptor = logDescriptor;
		}

		public Map<ObjectId, String> getAttachments() {
			return this.attachments;
		}

		public PacketDescriptor getLogDescriptor() {
			return logDescriptor;
		}
	}
	
	public static class PacketDescriptor {
		
		private ObjectId baseDocumentId;
		private ObjectId distribElementId;
		private ObjectId msgGostId;
		
		public PacketDescriptor(){
			
		}

		public ObjectId getBaseDocumentId() {
			return baseDocumentId;
		}

		public void setBaseDocumentId(ObjectId baseDocumentId) {
			this.baseDocumentId = baseDocumentId;
		}

		public ObjectId getDistribElementId() {
			return distribElementId;
		}

		public void setDistribElementId(ObjectId distribElementId) {
			this.distribElementId = distribElementId;
		}

		public ObjectId getMsgGostId() {
			return msgGostId;
		}

		public void setMsgGostId(ObjectId msgGostId) {
			this.msgGostId = msgGostId;
		}
	}

	public void uploadDocuments() {
		documentsUploader.uploadDocuments();
	}

	public void interrupt ()
	{
		this.documentsUploader.interrupt();
	}

	public void setConfigFilePath(String configFilePath) {
		this.configFilePath = configFilePath;
	}

	public void setDocumentsDownloader(SaveDoc documentsDownloader) {
		this.documentsDownloader = documentsDownloader;
	}

	public void setDocumentsUploader(LoadDoc documentsUploader) {
		this.documentsUploader = documentsUploader;
	}

}
