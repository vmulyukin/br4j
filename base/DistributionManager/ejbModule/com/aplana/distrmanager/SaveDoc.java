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

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.distrmanager.LoadSaveDocFacade.PacketDescriptor;
import com.aplana.distrmanager.exceptions.ConfigurationException;
import com.aplana.distrmanager.exceptions.LockDeleteException;
import com.aplana.distrmanager.exceptions.SaveException;
import com.aplana.distrmanager.util.ReadConfig;
import com.aplana.distrmanager.util.UtilsWorkingFiles;

public class SaveDoc {

	protected final Log logger = LogFactory.getLog(getClass());

    protected String outFolderPath = "";
    private File outFolder;
    private File deletedFolder;
    private Properties optionsBean;

	public void setOptions(Properties options) {
		this.optionsBean = options;
	}

	public File downloadDocument(Map<ObjectId, String> attachments, PacketDescriptor logDescriptor) throws SaveException {
		ObjectId idDocBase = logDescriptor.getBaseDocumentId();
		ObjectId idElm = logDescriptor.getDistribElementId();
		ObjectId msgGostId = logDescriptor.getMsgGostId();
		File directoryDownloader = null;
		boolean deleteRandomFolder = false;
		try {
			readConfig();
		} catch (IOException ioe) {
			deleteRandomFolder = true;
			logger.error("com.aplana.distrmanager.util.SaveDoc::readConfig -> Error !", ioe);
		}
		try {
			if (!deleteRandomFolder)
				initializeFolder();
		} catch (ConfigurationException ce) {
			deleteRandomFolder = true;
			logger.error("com.aplana.distrmanager.util.SaveDoc::initializeFolder -> Error !", ce);
		}
		try {
			if (!deleteRandomFolder)
				directoryDownloader = processSave(idDocBase, idElm, msgGostId, attachments);
		} catch (Exception e) {
			deleteRandomFolder = true;
			logger.error("com.aplana.distrmanager.util.SaveDoc::processSave -> Error !", e);
		}
		try{
			if (deleteRandomFolder && deletedFolder != null && deletedFolder.exists()) {
				UtilsWorkingFiles.deleteDirectory(deletedFolder);
			}
		} catch (Exception e) {
			String deleteError = "com.aplana.distrmanager.util.SaveDoc::deleteDirectory -> Error !";
			logger.error(deleteError, e);
			throw new SaveException(deleteError, e);
		}
		finally {
			this.deletedFolder = null;
		}
		return directoryDownloader;
	}

	public void unlockDirectory(File randomFolder) throws LockDeleteException {
		if (UtilsWorkingFiles.isDirectoryLocked(randomFolder)) {
			// ������� �����
			UtilsWorkingFiles.unlockDirectory(randomFolder);
		} else {
			logger.warn("Directory: " + randomFolder.getAbsolutePath() + " has not been locked.");
		}
	}

	private void readConfig() throws IOException {
		ReadConfig readConfig = new ReadConfig(optionsBean);
		this.outFolderPath = readConfig.getOutFolderPath();
	}

	private void initializeFolder() throws ConfigurationException {
		this.outFolder = UtilsWorkingFiles.initializeFolder(outFolderPath);
	}

	/**
	 * Method for export documents
	 * @throws LockDeleteException
	 * @throws Exception
	 */
	private File processSave(ObjectId idDocBase, ObjectId idElm, ObjectId msgGostId,
			Map<ObjectId, String> attachments) throws Exception
	{
		if( !outFolder.canWrite() )
		{
		    throw new SaveException("It is impossible to write to " + outFolder.getAbsolutePath());
		}
		// ������ ��������� ����������, ��������� � � ���������� � ��������
		File randomFolder = UtilsWorkingFiles.createRandomFolder(outFolder);
		this.deletedFolder = randomFolder;
		if( !randomFolder.canWrite() )
		{
		    throw new SaveException("It is impossible to write to " + randomFolder.getAbsolutePath());
		}
		UtilsWorkingFiles.lockDirectory(randomFolder, getClass().toString()); // ����� ����������

		// ��������� �������� � ����������
		try {
			UtilsWorkingFiles.saveAttachmentsToFiles(randomFolder, attachments);
		} catch (Exception e) {
			throw new SaveException(String.format("Error saving attachment for card {%s} and recipient {%s}, messageGOST {%s} in directory {%s}:",
					idDocBase.getId(), idElm.getId(), msgGostId.getId(), randomFolder.getAbsolutePath()), e);
		}
		// ����� ������� ����� ������� ��������� ��� � ������ ���������� � ������ ���������� ���������.
		return randomFolder;
	}
}
