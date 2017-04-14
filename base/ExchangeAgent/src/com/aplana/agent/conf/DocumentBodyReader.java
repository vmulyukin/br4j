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
package com.aplana.agent.conf;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Read useful information from document body
 */
public abstract class DocumentBodyReader {
	protected final Logger logger = Logger.getLogger(getClass());

	public static enum DocType {
		MEDO_GOST("document.xml", "/conf/IEDMS.xsd"),
		REPLICATION("replication-package.xml", "/conf/ReplicationPackage.xsd"),
		TICKET("DistributionLetter.xml", "/conf/letter_scheme.xsd"),
		UNKNOWN("", "");
		
		private String fileName;
		private String schemaName;
		
		DocType(String name, String schema) {
			this.fileName = name;
			this.schemaName = schema;
		}
		
		public String getFileName() {
			return fileName;
		}
		
		public String getSchemaName() {
			return schemaName;
		}
	}

	public static DocType isKnownMainDocument(String name) {
		for (DocType type : DocType.values()) {
			if (type.getFileName().equals(name)) {
				return type;
			}
		}
		return DocType.UNKNOWN;
	}

	public static File getMainDocument(URL letterUrl) throws LetterTypeDetectException{
			File letterDir = FileUtils.toFile(letterUrl);
			return getMainDocument(letterDir);
	}

	public static File getMainDocument(File dir) throws LetterTypeDetectException{
		LetterDetectionResult res = detectMainDocument(dir);
		if (res == null){
			return null;
		}
		return res.getDocFile();
	}
	
	@SuppressWarnings("unchecked")
	public static LetterDetectionResult detectMainDocument(File dir) throws LetterTypeDetectException{
		if (!dir.isDirectory()) {
			throw new LetterTypeDetectException("Can\'t detect letter type inside " + dir + " directory! It is not a directory.");
		}
		LetterDetectionResult res = new LetterDetectionResult(DocType.UNKNOWN, null, null); // ���������
		Collection<File> docFile;

		// ������� ���� ������� (DISTRIBUTION_LETTER_FILENAME)
		IOFileFilter docFindFilter = FileFilterUtils.andFileFilter(FileFilterUtils.nameFileFilter(DocType.TICKET.getFileName()), 
				FileFilterUtils.fileFileFilter());
		docFile = (Collection<File>)FileUtils.listFiles(dir, docFindFilter, null);
		if (docFile.size() == 1){
			File file = (File)docFile.toArray()[0];
			res.setEnvelopeFile(file);
			res.setType(isKnownMainDocument(file.getName()));
		}
		if (docFile.size() > 1){
			throw new LetterTypeDetectException("Can\'t detect letter type inside " + dir + " directory! "
					+ "There are more than one main files:" + docFile.toArray()[0]);
		}

		// ����� ���������� ������� �������� � ������� 
		// DOCUMENT_FILENAME->REPLICATION_PACKAGE_FILENAME->DISTRIBUTION_LETTER_FILENAME
		docFindFilter = FileFilterUtils.andFileFilter(FileFilterUtils.nameFileFilter(DocType.MEDO_GOST.getFileName()), 
				FileFilterUtils.fileFileFilter());
		docFile = (Collection<File>)FileUtils.listFiles(dir, docFindFilter, null);
		if (docFile.size() == 1){
			File file = (File)docFile.toArray()[0];
			res.setDocFile(file);
			res.setType(isKnownMainDocument(file.getName()));
		}
		if (docFile.size() > 1){
			throw new LetterTypeDetectException("Can\'t detect letter type inside " + dir + " directory! "
					+ "There are more than one main files:" + docFile.toArray()[0]);
		}

		docFindFilter = FileFilterUtils.andFileFilter(FileFilterUtils.nameFileFilter(DocType.REPLICATION.getFileName()), 
				FileFilterUtils.fileFileFilter());
		docFile = (Collection<File>)FileUtils.listFiles(dir, docFindFilter, null);
		if (docFile.size() == 1){
			File file = (File)docFile.toArray()[0];
			res.setDocFile(file);
			res.setType(isKnownMainDocument(file.getName()));
		}
		if (docFile.size() > 1){
			throw new LetterTypeDetectException("Can\'t detect letter type inside " + dir + " directory! "
					+ "There are more than one main files:" + docFile.toArray()[0]);
		}
		
		if ((res.getDocFile() == null)&&(res.getEnvelopeFile()!=null)){ // ��� ����� ���� ������ �����
			res.setDocFile(res.getEnvelopeFile()); // ����� �������� � ��������� ��������� ���������
		}
		
		return res;
	}

	private List<File> attachments;

	/**
	 * check document against sender and receiver UUIDs
	 *
	 * @param dir directory where message is located
	 * @return
	 */
	public boolean isValid(File dir) {
		if (StringUtils.isBlank(toUuid())) {
			logger.error("destination UUID is empty");
			return false;
		}
		if (StringUtils.isBlank(fromUuid())) {
			logger.error("source UUID is empty");
			return false;
		}
		if (!validateAttachedFiles(dir)) {
			return false;
		}

		return true;
	}

	protected boolean validateAttachedFiles(File dir) {

		attachments = new ArrayList<File>();

		List<String> fileNames = getAttachmentFiles(dir);

		if (fileNames != null && fileNames.size() > 0) {

			for (String localFileName : fileNames) {
				if (localFileName != null) {
					File file = new File(dir, localFileName);
					if (file.exists() && file.isFile()) {
						logger.debug("Attachment \"" + localFileName + "\" is valid.");
						attachments.add(file);
						continue;
					} else if (!file.exists()) {
						logger.error("Attachment \"" + localFileName + "\" does not exists in document folder \""
								+ dir.getAbsolutePath() + "\".");
					} else if (!file.isFile()) {
						logger.error("Attachment \"" + localFileName + "\" is not a file.");
					}
					return false;
				}
			}
		}
		return true;
	}


	/**
	 * @return list of attached files. Method validate should be called before
	 */
	public List<File> getAttachments() {
		return attachments;
	}

	/**
	 * each document type should tell UUID itself
	 * @return
	 */
	public abstract String toUuid();

	/**
	 * each document type should tell UUID itself
	 * @return
	 */
	public abstract String fromUuid();

	/**
	 * each document type should return id
	 * @return
	 */
	public abstract String getId();

	/**
	 * each document type should tell names of attachments
	 * @return
	 */
	protected abstract List<String> getAttachmentFiles(File dir);
}
