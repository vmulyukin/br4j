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
package com.aplana.distrmanager.util;

import com.aplana.dbmi.action.*;
import com.aplana.dbmi.action.file.DatabaseIOException;
import com.aplana.dbmi.action.file.UploadFilePart;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.distrmanager.exceptions.ConfigurationException;
import com.aplana.distrmanager.exceptions.LockDeleteException;
import com.aplana.distrmanager.exceptions.SaveCardException;
import com.aplana.distrmanager.exceptions.SaveException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.*;
import java.util.*;

public abstract class UtilsWorkingFiles {
	private static final String LOCK_FILE_NAME = "folder.lock";
	private static final String PARTIAL_FILE_NAME = "_partial";
	private static final ObjectId MATERIAL_NAME = ObjectId.predefined(
			StringAttribute.class, "jbr.materialName");

	private static final int PART_SIZE = 100 * 1024; // 100kb
	private static final int BUFSIZE_UPLOADPART = 32768;

	protected static final Log logger = LogFactory.getLog(UtilsWorkingFiles.class);

	/**
	 * Initialization of the specified directory path (directory must already exist).
	 *
	 * @param name
	 * @return File
	 * @throws ConfigurationException
	 */
	public static File initializeFolder(String name) throws ConfigurationException {
		File folder = new File(name);
		if (!folder.isAbsolute()) {
			throw new ConfigurationException("Absolute path should be defined for " + name);
		}
		if (!folder.exists()) {
			// TODO: attempt to create?
			throw new ConfigurationException("Directory " + name + " does not exist");
		}
		if (!folder.isDirectory()) {
			throw new ConfigurationException("File " + name + " is not directory");
		}
		return folder;
	}

	/**
	 * Saves the stream to a file in the specified path.
	 *
	 * @param directory
	 * @param inStream
	 * @param fileName
	 * @return File
	 * @throws SaveException
	 * @throws IOException
	 */
	public static File saveStreamToFile(File directory, InputStream inStream, String fileName) throws SaveException, IOException {
		File file = new File(directory, fileName);
		logger.debug("Create new file: " + fileName + " in directory " + directory.getAbsolutePath());
		FileOutputStream outStream = null;
		try {
			outStream = new FileOutputStream(file);
			byte[] buffer = new byte[4 * 1024];
			int read;
			while ((read = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, read);
			}
			outStream.flush();
			logger.debug("Created file: " + fileName);
		} catch (Exception e) {
			logger.error("Can't read input stream to file " + fileName + ": " + e.getMessage());
			throw new SaveException(e);
		} finally {
			if (outStream != null) {
				outStream.close();
			}
			if (inStream != null) {
				inStream.close();
			}
		}
		return file;
	}

	/**
	 * Checks folder.lock or partial file existence in directory
	 *
	 * @param processingDirectory checked directory
	 * @return boolean
	 */
	public static boolean isDirectoryLocked(File processingDirectory) {
		File [] systemFiles = processingDirectory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return LOCK_FILE_NAME.equals(name) || name.contains(PARTIAL_FILE_NAME);
			}
		});
		return systemFiles != null && systemFiles.length > 0;
	}

	/**
	 * Checks partial file existence in directory
	 *
	 * @param processingDirectory checked directory
	 * @return boolean
	 */
	public static boolean isDirectoryPartial(File processingDirectory) {
		File [] systemFiles = processingDirectory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.contains(PARTIAL_FILE_NAME);
			}
		});
		return systemFiles != null && systemFiles.length > 0;
	}

	/**
	 * Create lock file in directory
	 *
	 * @param processingDirectory checked directory
	 * @throws SaveException
	 */
	public static void lockDirectory(File processingDirectory, String locker) throws SaveException {
		File flock = new File(processingDirectory, LOCK_FILE_NAME);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(flock);
			fos.write(("Locked by distribution manager [" + new Date() + "] by " + locker).getBytes());
			fos.flush();
			fos.close();
		} catch (IOException e) {
			logger.error("Can't create " + LOCK_FILE_NAME + " in directory: " + processingDirectory.getAbsolutePath() + " !");
			throw new SaveException(e);
		}
	}

	public static void markDirectory(File processingDirectory, String name) throws IOException {
		File partial = new File(processingDirectory, name+PARTIAL_FILE_NAME);
		FileOutputStream fos = new FileOutputStream(partial);
		fos.close();
	}

	public static void unmarkDirectory(File processingDirectory, String name) {
		File partial = new File(processingDirectory, name+PARTIAL_FILE_NAME);
		if (partial.exists()) {
			partial.delete();
		}
	}

	/**
	 * Close file stream and delete lock file from directory
	 *
	 * @param processingDirectory - dir
	 * @param lock                - lock file
	 * @return true on OK, false on error
	 */
	public static boolean unlockDirectory(File processingDirectory) throws LockDeleteException {
		boolean unlock = false;
		try {
			File flock = new File(processingDirectory, LOCK_FILE_NAME);
			if (flock.exists())
				unlock = flock.delete();
		} catch (Exception e) {
			logger.error("Can't delete folder.lock in directory: " + processingDirectory.getAbsolutePath() + " !");
			throw new LockDeleteException(e);
		}
		return unlock;
	}

	/**
	 * Creates a directory, as indicated in path with a random name based on UUID.
	 *
	 * @param outFolder
	 * @return File
	 * @throws SaveException
	 */
	public static File createRandomFolder(File outFolder) throws SaveException {
		String uid = UUID.randomUUID().toString();
		try {
			//String randomFolderPath = outFolder.getAbsolutePath() + "/" + uid; // ������� ������� ��� ������
			File randomFolder = new File(outFolder, uid);
			if (randomFolder.exists()) { // ���� ������� ����������, ���� � ������������ UUID
				throw new SaveException("Create random folder error (incorrect UUID) !");
			} else { // ����� ������ �������
				randomFolder.mkdir();
			}
			return randomFolder;
		} catch (Exception e) {
			logger.error("Can't create random folder !");
			throw new SaveException(e);
		}
	}

	/**
	 * Saves the attachments listed in Map<ObjectId, String> in the specified path (outDirectory) to the disk.
	 *
	 * @param outDirectory
	 * @param attachments
	 * @throws DataException
	 * @throws ServiceException
	 * @throws SaveException
	 * @throws IOException
	 */
	public static void saveAttachmentsToFiles(File outDirectory, Map<ObjectId, String> attachments)
			throws DataException, ServiceException, IOException {
		if (outDirectory == null)
			throw new SaveException("Out dir is not defined");
		for (Map.Entry<ObjectId, String> attachment : attachments.entrySet()) {
			ObjectId cardId = attachment.getKey();
			String name = attachment.getValue();
			DownloadFile downloadFile = new DownloadFile();
			downloadFile.setCardId(cardId);
			Material material = getServiceBeanStatic().doAction(downloadFile);
			InputStream inStream = material.getData();
			saveStreamToFile(outDirectory, inStream, name);
			inStream.close();
		}
	}

	/**
	 * Deletes directory with subdirs and subfolders
	 *
	 * @param dir    Directory to delete
	 * @param locker Removed last.
	 * @throws SaveException
	 */
	public static void deleteDirectory(File dir) throws SaveException { // ����������������, �� �� ����� ���-�� ������ �������� ����������
		boolean isDeleted;
		final String locker = LOCK_FILE_NAME;
		if (dir.isDirectory()) {
			String[] children = dir.list();
			List<String> dirList = new ArrayList<String>(Arrays.asList(children));
			if (!"".equals(locker) && dirList.contains(locker)) {
				if (dirList.indexOf(locker) != dirList.size() - 1) { // ���� ����� �� � ����� ������
					dirList.remove(locker); // ������� �����
					dirList.add(locker); // ��������� �� ����� � ����� ������
				}
			}
			for (String aChildren : children) {
				File f = new File(dir, aChildren);
				deleteDirectory(f);
			}
			isDeleted = dir.delete();
		} else
			isDeleted = dir.delete();
		if (!isDeleted)
			throw new SaveException("The directory: " + dir.getAbsolutePath() + "could not be removed !");
	}

	/**
	 * Returns {@link DataServiceBean} instance or throws {@link DataException}
	 * if it is impossible.
	 *
	 * @return DataServiceBean
	 * @throws DataException
	 */
	public static DataServiceBean getServiceBeanStatic() {
		return ServicesProvider.serviceBeanInstance();
	}

	/**
	 * Returns card material (file)
	 *
	 * @param serviceBean Service bean
	 * @param templateId  Id template card
	 * @param fileName    Name file upload
	 * @return Card
	 * @throws Exception
	 */
	// �������� �������� �������� (����)
	public static Card createFileCard(DataServiceFacade serviceBean, ObjectId templateId, String fileName) throws Exception {
		final CreateCard createCardFile = new CreateCard(templateId);
		Card cardFile;
		try {
			cardFile = serviceBean.doAction(createCardFile);
			if (cardFile == null) {
				throw new DataException("CardFile was not created by unspecified reason.");
			}
		} catch (DataException ex) {
			throw new DataException("jbr.DistributionManager.util.UtilsWorkingFiles.fileCardNotCreated", ex);
		} catch (Exception e) {
			throw new DataException("jbr.DistributionManager.util.UtilsWorkingFiles.unknownErrorCreateFile", e);
		}
		StringAttribute name = cardFile.getAttributeById(Attribute.ID_NAME);
		name.setValue(fileName);
		final ObjectId cardIdFile = saveCardCreated(cardFile, serviceBean);
		if (cardFile.getId() == null)
			cardFile.setId((Long) cardIdFile.getId());
		return cardFile;
	}

	/**
	 * The file uploaded to the card material
	 *
	 * @param cardFile     Card file (material)
	 * @param outXmlResult Outgoing XML
	 * @param fileName     Name file upload
	 * @param serviceBean  Service bean
	 * @throws DataException
	 * @throws ServiceException
	 * @throws IOException
	 */
	// �������� ����� � �������� ��������
	public static void attachFile(Card cardFile, InputStream file,
								  String fileName, DataServiceFacade serviceBean) throws DataException, ServiceException, IOException {
		try {
			UploadFile uploadAction = new UploadFile();
			uploadAction.setCardId(cardFile.getId());
			uploadAction.setFileName(fileName);
			uploadAction.setData(file);

			byte[] data = new byte[PART_SIZE];

			uploadPart(file, uploadAction, data, serviceBean);

			if (logger.isDebugEnabled()) {
				logger.debug("data:" + Arrays.toString(data));
			}

			serviceBean.doAction(uploadAction);
			cardFile = serviceBean.getById(cardFile.getId());
			MaterialAttribute attr = cardFile.getAttributeById(Attribute.ID_MATERIAL);
			attr.setMaterialName(fileName);
			attr.setMaterialType(MaterialAttribute.MATERIAL_FILE);
			StringAttribute nameMaterial = cardFile.getAttributeById(MATERIAL_NAME);
			nameMaterial.setValue(fileName);
			saveCard(cardFile, serviceBean);
		} finally {
			if (null != file)
				file.close();
		}
	}

	private static boolean uploadPart(InputStream is, UploadFile upload, byte[] data, DataServiceFacade serviceBean) {
		try {
			PositionHolder posHolder = new PositionHolder();
			final byte[] buf = new byte[BUFSIZE_UPLOADPART];
			while (true) {
				int len = is.read(buf);
				if (len == -1)
					break;
				write(buf, 0, len, upload, data, posHolder, serviceBean);
			}
			flush(upload, data, posHolder, serviceBean);

			return true;

		} catch (IOException e) {
			return false;
		}
	}

	private static void write(byte[] b, int off, int len, UploadFile upload, byte[] data, PositionHolder posHolder, DataServiceFacade serviceBean)
			throws IOException {
		while (posHolder.localPos + len >= PART_SIZE) {
			final int wrLen = PART_SIZE - posHolder.localPos;
			System.arraycopy(b, off, data, posHolder.localPos, wrLen);
			off += wrLen;
			len -= wrLen;
			posHolder.localPos = PART_SIZE;
			flush(upload, data, posHolder, serviceBean);
		}
		System.arraycopy(b, off, data, posHolder.localPos, len);
		posHolder.localPos += len;
	}

	private static void flush(UploadFile upload, byte[] data, PositionHolder posHolder, DataServiceFacade serviceBean) throws IOException {
		final UploadFilePart part = new UploadFilePart();

		part.setCardId(upload.getCardId());
		part.setData(data, posHolder.localPos);
		part.setOffset(posHolder.globalPos);
		part.setUrl(upload.getUrl());
		try {
			serviceBean.doAction(part);

			// ��������� ���������������� �����
			if (upload.getUrl() == null)
				upload.setUrl(part.getUrl());

		} catch (DataException e) {
			throw new DatabaseIOException(e);
		}
		posHolder.globalPos += posHolder.localPos;
		posHolder.localPos = 0;
	}

	/**
	 * Saves the target card, only created card
	 *
	 * @param card        Target card
	 * @param serviceBean Service bean
	 * @throws SaveCardException
	 */
	// ���������� ��������
	public static ObjectId saveCardCreated(Card card, DataServiceFacade serviceBean) throws SaveCardException {
		try {
			ObjectId idCard = null;
			try {
				idCard = serviceBean.saveObject(card);
			} finally {
				final UnlockObject unlock = new UnlockObject(idCard);
				serviceBean.doAction(unlock);
			}
			return idCard;
		} catch (Exception e) {
			throw new SaveCardException("jbr.DistributionManager.util.UtilsWorkingFiles.saveCardCreated", e);
		}
	}

	/**
	 * Saves the target card
	 *
	 * @param card        Target card
	 * @param serviceBean Service bean
	 * @throws SaveCardException
	 */
	public static ObjectId saveCard(Card card, DataServiceFacade serviceBean) throws SaveCardException {
		try {
			ObjectId idCard = null;
//				boolean wasLocked = false; // ���� �� �������������
//				UserData prevUser = serviceBean.getUser(); // ���������� �������� �����
			LockObject lock = new LockObject(card);
			serviceBean.doAction(lock); // ��������� ��������
			try {
				idCard = serviceBean.saveObject(card); // ��������� ������� �����
			} finally {
				UnlockObject unlock = new UnlockObject(card); // ������������
				serviceBean.doAction(unlock);
			}
				/*if (card.isLocked()) {
					wasLocked = true;
	    			ObjectId lockerPerson = card.getLocker();
	    			Person person = (Person)serviceBean.getById(lockerPerson);
	    			UserData userData = new UserData();
	    			userData.setAddress("127.0.0.1");
	    			userData.setPerson(person);
	    			serviceBean.setUser(userData); // ������ �� �����-������
	    		} else {
	    			wasLocked = false;
	    			LockObject lock = new LockObject(card);
	    	        serviceBean.doAction(lock); // ��������� ��������
	    		}
	    		try {
	    			idCard = serviceBean.saveObject(card); // ��������� ������� �����
	    		} finally {
	    			if (!wasLocked) {
	    				UnlockObject unlock = new UnlockObject(card); // ������������
	    				serviceBean.doAction(unlock);
	    			} else {
	    				serviceBean.setUser(prevUser); // ���������� ��� ����
	    			}
	    		}*/
			return idCard;
		} catch (Exception e) {
			throw new SaveCardException("jbr.DistributionManager.util.UtilsWorkingFiles.saveCard", e);
		}

	}

	/**
	 * Saves the target card with QueryFactory
	 *
	 * @param card        Target card
	 * @param serviceBean Service bean
	 * @throws SaveCardException
	 */
	public static void saveCardParent(Card card, DataServiceFacade serviceBean, Attribute attr, JdbcTemplate jdbc) throws SaveCardException {
		try {
			final OverwriteCardAttributes overwriteAttributes = new OverwriteCardAttributes();
			overwriteAttributes.setCardId(card.getId());
			overwriteAttributes.setAttributes(Collections.singletonList(attr));
			overwriteAttributes.setInsertOnly(false);
			serviceBean.doAction(new LockObject(card));
			try {
				serviceBean.doAction(overwriteAttributes);
			} finally {
				serviceBean.doAction(new UnlockObject(card));
			}
		} catch (Exception e) {
			throw new SaveCardException("jbr.DistributionManager.util.UtilsWorkingFiles.saveCardParent", e);
		}

	}

	/*
	 * ������� ���������� globalPos � localPos �� ���������� �������
	 */
	static class PositionHolder {
		int globalPos = 0;
		int localPos = 0;
	}
}
