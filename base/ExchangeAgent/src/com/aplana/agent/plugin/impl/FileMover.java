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
package com.aplana.agent.plugin.impl;

import com.aplana.agent.conf.*;
import com.aplana.agent.conf.DeliveryConfiguration.DeliveryLogHandler;
import com.aplana.agent.conf.DocumentBodyReader.DocType;
import com.aplana.agent.conf.delivery.IdType;
import com.aplana.agent.conf.delivery.RecordType;
import com.aplana.agent.conf.routetable.Node;
import com.aplana.agent.conf.routetable.ResourceType;
import com.aplana.agent.conf.routetable.Resources.Resource;
import com.aplana.agent.plugin.*;
import com.aplana.agent.util.FileProperties;
import com.aplana.agent.util.FilePropsBuilder;
import com.aplana.agent.util.FileUtility;
import com.aplana.agent.util.FolderMark;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

public class FileMover extends AbstractPlugin {

	@Override
	public void setConfiguration(File config) {
	}
	
	@Override
	public boolean getMail(Node node) throws PluginException {
		File collector = null;
		for (Resource res : node.getResources().getResource()) {
			if (res.getType() == ResourceType.COLLECTOR) {
				try {
					collector = new File(new URL(res.getUrl()).getFile());
				} catch (MalformedURLException e) {
					logger.error("Can't process this URL:" + res.getUrl(), e);
					return false;
				}
			}
		}
		if (collector == null) {
			throw new PluginException("Collector URL is null");
		}
		boolean result = true;
		for (Resource res : node.getResources().getResource()) {
			if (res.getType() == ResourceType.DOCUMENT || res.getType() == ResourceType.TICKET) {
				try {
					File fromDir = new File(new URL(res.getUrl()).getFile());
					if (!fromDir.exists()){
						logger.error("The URL " + res.getUrl() + " does not exists ! Skipping.");
						result = false;
						continue;
					}
					File[] mailDirs = fromDir.listFiles();
					if (mailDirs == null) {
						continue;
					}
					for (File mailDir : mailDirs) {
						File destDir = new File(collector, mailDir.getName());
						// ������� ����� ����������
						if (!mailDir.isDirectory()) {
							continue;
						}
						String agentId = env.getProperty(AGENT_ID) + "-receive-from-" + node.getName();
						String taId = env.getProperty(TRANSPORT_AGENT_UUID);
						// ���������� �����, ���������� �� ������� �������
						if (FileUtility.isLocked(mailDir) && !FileUtility.isLockedByCurrentAgent(mailDir, agentId, taId)) {
							continue;
						}
						Properties props = new FilePropsBuilder().set(FileProperties.TEXT, agentId).build();
						FileUtility.lockFolder(mailDir, props, true);
						FileUtility.lockFolder(destDir, props, true);
						FileUtility.markPartial(destDir);
						try {
							FileUtility.copyDirectory(mailDir, collector,
									FileFilterUtils.notFileFilter(
											FileFilterUtils.nameFileFilter(FolderMark.LOCK_FILE.toString())));
						} catch (RuntimeException e) {
							FileUtility.silentDeleteDir(destDir);
							throw e;
						}
						LetterDetectionResult ldr;
						try {
							ldr = DocumentBodyReader.detectMainDocument(destDir);
						} catch (LetterTypeDetectException e2) {
							throw new SendMailException("Unknown letter type in " + destDir, e2);
						}
						if (ldr.getType().equals(DocType.UNKNOWN)) {
							throw new SendMailException("Unknown letter type in " + destDir);
						}

						File mainDoc = (ldr.getEnvelopeFile() == null) ?
								ldr.getDocFile() :            // ���� ����� �������� ���� (?) �� ���� �������� ��������
								ldr.getEnvelopeFile();

						DocumentBodyReader reader = getDocumentBodyReader(mainDoc);
						DeliveryLogHandler logHandler = getDeliveryLogHandler(destDir);
						RecordType record = RecordTypeProxy.getInstance()
								.setDate(new Date())
								.setTransportAgentName(env.getProperty(TRANSPORT_AGENT_NAME))
								.setTransportAgentUuid(env.getProperty(TRANSPORT_AGENT_UUID))
								.setAgentName(env.getProperty(AGENT_ID))
								.setActionType(IdType.RECEIVE)
								.setFromNodeName(node.getName())
								.setFromURL(fromDir)
								.setToNodeName("COLLECTOR")
								.setToURL(destDir.getParentFile())
								.setPacketName(destDir.getName())
								.setPacketId((reader == null) ? "unknown" : reader.getId())
								.setLetterContent(destDir)
								.getRecord();

						try {
							logHandler.addRecord(record);
						} catch (IncorrectMD5Exception e) {
							String msg = "Failed check by MD5";
							logger.error(msg, e);
							FileUtility.silentDeleteDir(destDir);
							FileUtility.unlockFolder(mailDir);
							continue;
						}

						logHandler.save();
						FileUtility.unlockFolder(destDir);
						FileUtility.markPartial(mailDir);
						FileUtility.deleteDir(mailDir);
					}
				} catch (MalformedURLException e) {
					logger.error("Can't process this URL:" + res.getUrl(), e);
					return false;
				} catch (IOException e) {
					String message = "Can't move directory to:" + collector.getAbsolutePath();
					logger.error(message, e);
					throw new GetMailException(message, e);
				} catch (JAXBException e) {
					String message = "JAXBException";
					logger.error(message, e);
					throw new PluginException(message, e);
				} catch (DatatypeConfigurationException e) {
					logger.error("No DataTypeFactory configuration found. Please check it.", e);
					throw new PluginException(e);
				}
			}
		}
		return result;
	}

	@Override
	public boolean sendMail(URL letter, Node destinationNode) throws PluginException {
		if (destinationNode == null) {
			String message = "Argument \'destinationNode\' should not been null.";
			logger.error(message);
			throw new PluginException(message);
		}
		
		File srcDir = FileUtils.toFile(letter);
		LetterDetectionResult ldr;
		try {
			ldr = DocumentBodyReader.detectMainDocument(srcDir);
		} catch (LetterTypeDetectException e2) {
			throw new SendMailException("Unknown letter type in " + letter, e2);
		}
		if (ldr.getType().equals(DocType.UNKNOWN)){
			throw new SendMailException("Unknown letter type in " + letter);
		}
		
		File mainDoc = (ldr.getEnvelopeFile() == null) ?
						ldr.getDocFile() :			// ���� ����� �������� ���� (?) �� ���� �������� ��������
						ldr.getEnvelopeFile();

		DocType mainDocType = ldr.getType();
		DocumentBodyReader reader = getDocumentBodyReader(mainDoc);
		DeliveryLogHandler logHandlerSuccess = getDeliveryLogHandler(srcDir);
		DeliveryLogHandler logHandlerFail    = getDeliveryLogHandler(srcDir);

		File commonDestDir = null;
		try {
			String toUrl = findOutputResource(destinationNode, mainDocType, letter);
			if (toUrl == null){	// URL ��� ������� ���� ��������� �� ��������
				return false; // ���� ���� ����������� ��������� ������ ����� ��������� ����
			}
			commonDestDir = FileUtils.toFile(new URL(toUrl));
			File destDirForMail = new File(commonDestDir, srcDir.getName());

			if ((reader == null) || (!reader.isValid(srcDir))) {
				String msg = "Error while reading " + srcDir.getAbsolutePath();
				logger.error(msg);
				RecordType record = RecordTypeProxy.getInstance()
						.setDate(new Date())
						.setTransportAgentName(env.getProperty(TRANSPORT_AGENT_NAME))
						.setTransportAgentUuid(env.getProperty(TRANSPORT_AGENT_UUID))
						.setAgentName(env.getProperty(AGENT_ID))
						.setActionType(IdType.REFUSE)
						.setFromNodeName("COLLECTOR")
						.setFromURL(srcDir.getParentFile())
						.setToNodeName(destinationNode.getName())
						.setToURL(commonDestDir)
						.setPacketName(srcDir.getName())
						.setPacketId((reader==null) ? "unknown" : reader.getId())
						.setLetterContent(srcDir)
						.getRecord();
				try {
					logHandlerFail.addRecord(record);
					logHandlerFail.save();
				} catch (IncorrectMD5Exception e) {
					logger.error(e.getMessage(), e);
				}
				handleInvalidMessage(srcDir, destinationNode);
				return true;
			}

			Properties props = new FilePropsBuilder()
					.set(FileProperties.TEXT, env.getProperty(AGENT_ID) + "-send")
					.build();
			//FileUtility.lockFolder(srcDir,         props, true);
			FileUtility.lockFolder(destDirForMail, props, true);
			FileUtility.markPartial(destDirForMail);
			try {
				FileUtility.copyDirectory(srcDir, commonDestDir,
						FileFilterUtils.notFileFilter(
								FileFilterUtils.nameFileFilter(FolderMark.LOCK_FILE.toString())));
			} catch (RuntimeException e) {
				FileUtility.silentDeleteDir(destDirForMail);
				throw e;
			}

			RecordType record = RecordTypeProxy.getInstance()
					.setDate(new Date())
					.setTransportAgentName(env.getProperty(TRANSPORT_AGENT_NAME))
					.setTransportAgentUuid(env.getProperty(TRANSPORT_AGENT_UUID))
					.setAgentName(env.getProperty(AGENT_ID))
					.setActionType(IdType.SEND)
					.setFromNodeName("COLLECTOR")
					.setFromURL(srcDir.getParentFile())
					.setToNodeName(destinationNode.getName())
					.setToURL(commonDestDir)
					.setPacketName(srcDir.getName())
					.setPacketId(reader.getId())
					.setLetterContent(destDirForMail)
					.getRecord();
			try {
				logHandlerSuccess.addRecord(record);
			} catch (IncorrectMD5Exception e) {
				FileUtility.silentDeleteDir(destDirForMail);
				throw e;
			}

			logHandlerSuccess.save(new File(destDirForMail, DeliveryConfiguration.DELIVERY_LOG_FILENAME)); // ���������� �����. ������ � ����� ����������
			processCopy(destDirForMail, destinationNode); // ����������� ������� ������������� ���������
			FileUtility.unlockFolder(destDirForMail);
			FileUtility.markPartial(srcDir);
			FileUtility.deleteDir(srcDir);
		} catch (Exception e) {
			logger.error("Error while moving files. Adding a fail record to deliveryLog now...", e);
			try {
				logHandlerFail.addRecord(RecordTypeProxy.getInstance()
						.setDate(new Date())
						.setTransportAgentName(env.getProperty(TRANSPORT_AGENT_NAME))
						.setTransportAgentUuid(env.getProperty(TRANSPORT_AGENT_UUID))
						.setAgentName(env.getProperty(AGENT_ID))
						.setActionType(IdType.REFUSE)
						.setFromNodeName("COLLECTOR")
						.setFromURL(srcDir.getParentFile())
						.setToNodeName(destinationNode.getName())
						.setToURL(commonDestDir == null ? "undefined due to error" : commonDestDir.toURI().toURL().toString())
						.setPacketName(srcDir.getName())
						.setPacketId(reader == null ? "unknown" : reader.getId())
						.setLetterContent(srcDir) // ��������� ������ ������� ������ ������
						.getRecord()
				);
				logHandlerFail.save();
				return false;
			} catch (Exception e1) {
				String message = "Can't add fail record to " + DeliveryConfiguration.DELIVERY_LOG_FILENAME + ": " + e1.getMessage();
				logger.error(message, e1);
				//TODO need throw?
				//throw new PluginException(message);
			}

			FileUtility.unlockFolder(srcDir);
		}
		return true;
	}
	
	@Override
	protected boolean handleInvalidMessage(File messageDir, Node node) throws PluginException {
		super.handleInvalidMessage(messageDir, node);
		FileUtility.silentDeleteDir(messageDir);
		return true;
	}
	
	@Override
	public void cleanResources(Node node) throws PluginException {
		String outputResource = findOutputResource(node, ResourceType.DOCUMENT);
		File outputFolder;
		try {
			outputFolder = FileUtils.toFile(new URL(outputResource));
		} catch (MalformedURLException e) {
			logger.error("Bad resource URL:" + outputResource, e);
			return;
		}
		if (!outputFolder.exists()) {
			logger.error("The URL " + outputFolder + " does not exists ! Skipping.");
			return;
		}
		File[] dirs = outputFolder.listFiles((FilenameFilter)DirectoryFileFilter.INSTANCE);
		for (File dir : dirs) {
			//������� lock-����
			IOFileFilter docPartialFilter = FileFilterUtils.andFileFilter(
					FileFilterUtils.nameFileFilter(FolderMark.LOCK_FILE.toString()), 
					FileFilterUtils.fileFileFilter());
			Collection<?> partialFiles = FileUtils.listFiles(dir, docPartialFilter, null);
			if (partialFiles.size() > 0) {
				Properties props = FileUtility.getMarkProperties(dir, FolderMark.LOCK_FILE);
				
				String partial = props.getProperty(FileProperties.PARTIAL.toString());
				String lockedTaUUID  = props.getProperty(FileProperties.TA_UUID.toString());
				String currentUUID = RouteTableConfiguration.getInstance().getTransportAgentUUID();
				
				//������� �����, ���� ��� �������� "partial" ������ � ���� ������� ���� �� ��
				if (partial != null && currentUUID.equals(lockedTaUUID)) {
					FileUtils.deleteQuietly(dir);
				}
			}
		}
	}
}
