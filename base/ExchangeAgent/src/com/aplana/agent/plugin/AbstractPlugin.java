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
package com.aplana.agent.plugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.agent.conf.DeliveryConfiguration;
import com.aplana.agent.conf.DeliveryConfiguration.DeliveryLogHandler;
import com.aplana.agent.conf.DocumentBodyReader;
import com.aplana.agent.conf.DocumentBodyReader.DocType;
import com.aplana.agent.conf.DocumentBodyReaderFactory;
import com.aplana.agent.conf.delivery.ActionType;
import com.aplana.agent.conf.delivery.AgentType;
import com.aplana.agent.conf.delivery.FileType;
import com.aplana.agent.conf.delivery.FromType;
import com.aplana.agent.conf.delivery.IdType;
import com.aplana.agent.conf.delivery.PacketType;
import com.aplana.agent.conf.delivery.RecordType;
import com.aplana.agent.conf.delivery.ToType;
import com.aplana.agent.conf.delivery.TransportAgentType;
import com.aplana.agent.conf.routetable.Node;
import com.aplana.agent.conf.routetable.ResourceType;
import com.aplana.agent.conf.routetable.Resources.Resource;
import com.aplana.agent.util.FileUtility;
	
/**
 * base plugin implementation with common actions
 */
public abstract class AbstractPlugin implements Plugin {
	protected Log logger = LogFactory.getLog(getClass());
	protected Properties env;
	
	public Properties getEnvironment(){
		return env;
	}

	@Override
	public void setEnvironment(Properties envMap) {
		env = envMap;
	}

	public String findOutputResource(Node node, DocType type, URL letter) throws PluginException {
		String toUrl = null;
		for (Resource res : node.getResources().getResource()) {
			if (type == DocType.TICKET) {
				if (res.getType() == ResourceType.TICKET) {
					toUrl = res.getUrl();
				}
			} else {
				if (res.getType() == ResourceType.DOCUMENT) {
					toUrl = res.getUrl();
				}
			}
		}
		if (toUrl == null){	// URL ��� ������� ���� ��������� �� ��������
			toUrl = findOutputResource(node, ResourceType.TRASH); // ������� ����� ����� "�������"
			if (toUrl == null){	// � "�������" �� �������
				logger.error("No defined resource in node \'" + node.getName() + "\' for document type " + type + ", letter " + letter);
			}
		}
		return toUrl;
	}

	public String findOutputResource(Node node, ResourceType type) throws PluginException {
		String toUrl = null;
		for (Resource res : node.getResources().getResource()) {
			if (res.getType() == type) {
				toUrl = res.getUrl();
			}
		}
		return toUrl;
	}

	protected boolean handleInvalidMessage(File messageDir, Node node) throws PluginException {
		String trashUrl = findOutputResource(node, ResourceType.TRASH);
		if (trashUrl != null) {
			URL url = null;
			try {
				url = new URL(trashUrl);
			} catch (MalformedURLException e) {
				logger.error("Bad trash URL: " + trashUrl, e);
			}
			if (url!= null && url.getProtocol()!=null && url.getProtocol().equals("file")){
				File trashFolder = FileUtils.toFile(url);
				logger.warn("Moving message " + messageDir.getName() + " to trash folder " + trashFolder.getAbsolutePath());
				FileUtility.copyDirectory(messageDir, trashFolder);
				FileUtility.unlockFolder(new File(trashFolder, messageDir.getName()));
				return true;
			} else {
				logger.error("URL '" + url + "' is not suitable for resource type \"trash\". Only \"file\" protocol is supported.");
			}
		} else {
			logger.error("Trash resource does not exists for node [" + node.getName() + "]");
		}
		return false;
	}

	public DocumentBodyReader getDocumentBodyReader(File doc) throws PluginException {
		InputStream messageDocInputStream = null;
		DocumentBodyReader reader = null;
		try {
			messageDocInputStream = new BufferedInputStream(new FileInputStream(doc));
			reader = DocumentBodyReaderFactory.getDocumentBodyReader(messageDocInputStream, doc.getName());
		} catch (JAXBException e) {
			String message = "Error while reading document body" + doc;
			logger.error(message, e);
		} catch (Exception e) {
			String message = "Error while creating document body reader for file " + doc;
			logger.error(message, e);
			throw new PluginException(message);
		} finally {
			IOUtils.closeQuietly(messageDocInputStream);
		}
		return reader;
	}

	public DeliveryLogHandler getDeliveryLogHandler(File dir) throws PluginException {
		DeliveryLogHandler logHandler;
		try {
			logHandler = DeliveryConfiguration.openDeliveryLog(new File(dir, DeliveryConfiguration.DELIVERY_LOG_FILENAME));
		} catch (JAXBException e) {
			String msg = "Can't unmarshall delivery log from file \'" +DeliveryConfiguration.DELIVERY_LOG_FILENAME + "\'";
			logger.error(msg, e);
			throw new PluginException(msg, e);
		}
		return logHandler;
	}

	protected RecordType createRecord(File fromDir, File letterDir, Node node, DocumentBodyReader reader, IdType type, DocType docType) throws IOException, PluginException {
		try {
			URL url = letterDir.toURI().toURL();
			return createRecord(fromDir, url, node, reader, type, docType);
		} catch (MalformedURLException e) {
			throw new PluginException("Can't create URL from file:" + letterDir.getAbsolutePath(), e);
		}
	}

	// ����������� ��������� � ������ ��� ����������� ������� ������������ ���������. ��� ������ "���������" ��� ��� �������� �� ��������.	
	protected void processCopy(File sourceFolder, Node currentNode){
		try{
			String copyUrl = findOutputResource(currentNode, ResourceType.COPY);
			if (copyUrl == null){
				return;
			}
			URL url = new URL(copyUrl);
			if (url.getProtocol()==null || !url.getProtocol().equals("file")){
				logger.error("URL " + url + " is not suitable for resource type \"copy\". Only \"file\" protocol is supported.");
				return;
			}
			File commonDestDir = FileUtils.toFile(new URL(copyUrl));
			File destDirForMail = new File(commonDestDir, sourceFolder.getName());
			try {
				FileUtility.copyDirectory(sourceFolder, commonDestDir);
			} finally {
				FileUtility.unlockFolder(destDirForMail);
			}
		}catch(Exception e){
			logger.error("Error while copying files", e);
		}
	}

	protected RecordType createRecord(File fromDir, URL letter, Node node, DocumentBodyReader reader, IdType type, DocType docType) throws IOException, PluginException {
		RecordType record = new RecordType();
		try {
			record.setDate(DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar)Calendar.getInstance()));
		} catch (DatatypeConfigurationException e) {
			logger.error("No DataTypeFactory configuration found. Please check it.", e);
			throw new PluginException(e);
		}

		TransportAgentType tranport = new TransportAgentType();
		tranport.setName(env.getProperty(Plugin.TRANSPORT_AGENT_NAME));
		tranport.setUuid(env.getProperty(Plugin.TRANSPORT_AGENT_UUID));
		
		AgentType agent = new AgentType();
		agent.setName(node.getAgent().getName());
		
		ActionType action = new ActionType();
		action.setId(type);
		
		FromType from = new FromType();
		from.setUrl(fromDir.getParent());
		from.setNodeName(env.getProperty(Plugin.AGENT_NAME));
		
		ToType to = new ToType();
		String toUrl = null;
		for (Resource res : node.getResources().getResource()) {
			if (docType == DocType.TICKET) {
				if (res.getType() == ResourceType.TICKET) {
					toUrl = res.getUrl();
					break;
				}
			} else {
				if (res.getType() == ResourceType.DOCUMENT) {
					toUrl = res.getUrl();
					break;
				}
			}
		}
		to.setUrl(toUrl);
		to.setNodeName(node.getName());
		
		PacketType packet = new PacketType();
		packet.setMessageId(reader.getId());

		File letterFile = FileUtils.toFile(letter);
		packet.setName(letterFile.getName());

		for(File listItem : letterFile.listFiles()) {
			if (!FileUtility.isSpecialFile(listItem)) {
				FileType file = new FileType();
				file.setName(listItem.getName());
				file.setSize(listItem.length());

				String md5 = DigestUtils.md5Hex(new FileInputStream(listItem));

				file.setMd5(md5);
				packet.getFile().add(file);
			}
		}
		
		record.setTransportAgent(tranport);
		record.setAgent(agent);
		record.setAction(action);
		record.setFrom(from);
		record.setTo(to);
		record.setPacket(packet);
		
		return record;
	}
}
