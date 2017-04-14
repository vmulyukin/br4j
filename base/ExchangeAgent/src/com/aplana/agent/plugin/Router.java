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
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.aplana.agent.ExportDirIterator;
import com.aplana.agent.RouteOrder;
import com.aplana.agent.RouteOrdersBean;
import com.aplana.agent.conf.ConfigService;
import com.aplana.agent.conf.DocumentBodyReader;
import com.aplana.agent.conf.DocumentBodyReader.DocType;
import com.aplana.agent.conf.DocumentBodyReaderFactory;
import com.aplana.agent.conf.LetterDetectionResult;
import com.aplana.agent.conf.LetterTypeDetectException;
import com.aplana.agent.conf.RouteTableConfiguration;
import com.aplana.agent.conf.routetable.Node;
import com.aplana.agent.conf.routetable.ResourceType;
import com.aplana.agent.conf.routetable.Resources.Resource;
import com.aplana.agent.util.FileProperties;
import com.aplana.agent.util.FilePropsBuilder;
import com.aplana.agent.util.FileUtility;
import com.aplana.agent.util.FolderMark;

public class Router extends AbstractPlugin {
	protected final Logger logger = Logger.getLogger(getClass());
	private ExportDirIterator dirIterator;

	public Router() {
	}

	@Override
	public void setConfiguration(File config) {
	}
	
	@Override
	public void cleanResources(Node node) throws PluginException {
		URL collectorURI = getCollectorUrl(node);
		File collectorFolder = FileUtils.toFile(collectorURI);
		if (!collectorFolder.exists()) {
			logger.error("The URL " + collectorURI + " does not exists ! Skipping.");
			return;
		}
		File[] dirs = collectorFolder.listFiles((FilenameFilter)DirectoryFileFilter.INSTANCE);
		for (File dir : dirs) {
			//������� lock-����
			IOFileFilter docLockedFilter = FileFilterUtils.andFileFilter(
					FileFilterUtils.nameFileFilter(FolderMark.LOCK_FILE.toString()), 
					FileFilterUtils.fileFileFilter());
			Collection<?> lockedFiles = FileUtils.listFiles(dir, docLockedFilter, null);
			if (lockedFiles.size() > 0) {
				Properties props = FileUtility.getMarkProperties(dir, FolderMark.LOCK_FILE);
				//������� �����, ���� ��� �������� "partial" ������
				if (props.containsKey(FileProperties.PARTIAL.toString())) {
					FileUtils.deleteQuietly(dir);
				} //����� ���� �������� ���� �� ��, �� ������� ���-���� 
				else if (props.containsKey(FileProperties.TA_UUID.toString())) {
					String taUUID = props.getProperty(FileProperties.TA_UUID.toString());
					if (RouteTableConfiguration.getInstance().getTransportAgentUUID().equals(taUUID)) {
						FileUtility.unlockFolder(dir);
					}
				}
			} else {
				//���� ������ ����� ���, �� ������� ��� ����� �� queue �����
				IOFileFilter docInQueueFilter = FileFilterUtils.andFileFilter(
						FileFilterUtils.nameFileFilter(FolderMark.QUEUE_FILE.toString()), 
						FileFilterUtils.fileFileFilter());
				Collection<?> docFile = FileUtils.listFiles(dir, docInQueueFilter, null);
				if (docFile.size() > 0) {
					FileUtility.removeInQueueMarker(dir);
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void validateXML(File file, String schemaName) throws SAXException, IOException, URISyntaxException{
		logger.info("Validating "+ file.getName() +"...");
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = factory.newSchema(new File(ConfigService.getResourceURL(schemaName).toURI()));
		Validator validator = schema.newValidator();
		StreamSource source = new StreamSource(new FileInputStream(file));
		validator.validate(source);
		logger.info(file.getName() + " succefully validated.");
	}

	@Override
	public boolean getMail(Node node) throws PluginException {
		URL collectorURI = getCollectorUrl(node);
		File collectorFolder = FileUtils.toFile(collectorURI);
		if (!collectorFolder.exists()){
			logger.error("The URL " + collectorURI + " does not exists ! Skipping.");
			return false;
		}
		RouteTableConfiguration rtc = RouteTableConfiguration.getInstance();
		RouteOrdersBean rob = (RouteOrdersBean)PluginFactory.getOrdinalBean("routeOrdersBean");
		dirIterator = new ExportDirIterator(collectorFolder);
		while (dirIterator.hasNext()) {
			File messageDir = dirIterator.next();

			LetterDetectionResult ldr;
			try {
				ldr = DocumentBodyReader.detectMainDocument(messageDir);
			} catch (LetterTypeDetectException e2) {
				throw new GetMailException("Unknown letter type in " + messageDir, e2);
			}
			if (ldr.getType().equals(DocType.UNKNOWN)){
				throw new GetMailException("Unknown letter type in " + messageDir);
			}
			
			File messageDocument = (ldr.getEnvelopeFile() == null) ?
							ldr.getDocFile() :			// ���� ����� �������� ���� (?) �� ���� �������� ��������
							ldr.getEnvelopeFile();

			Properties props = new FilePropsBuilder()
					.set(FileProperties.TEXT, env.getProperty(AGENT_ID))
					.build();
			FileUtility.lockFolder(messageDir, props);
			try {
				InputStream messageDocInputStream = new BufferedInputStream(new FileInputStream(messageDocument));
				DocumentBodyReader dbr = DocumentBodyReaderFactory.getDocumentBodyReader(messageDocInputStream, messageDocument.getName());
				messageDocInputStream.close();
				if (dbr.isValid(messageDir)) {
					String toUUID = dbr.toUuid();
					RouteOrder ro = new RouteOrder(messageDir.toURI().toURL(), ldr.getEnvelopeFile(), ldr.getType(), rtc.getNodes(toUUID));
					FileUtility.markInQueue(messageDir);
					FileUtility.unlockFolder(messageDir);
					rob.putRouteOrder(ro);
				} else {
					logger.error("Error while reading " + messageDir.getAbsolutePath());
					handleInvalidMessage(messageDir, node);
				}
			} catch (JAXBException e) {
				logger.error("Error while parsing " + messageDocument.getAbsolutePath(), e);
				handleInvalidMessage(messageDir, node);
			} catch (FileNotFoundException e) {
				logger.error("Main message file for " + messageDir + " not found!", e);
				handleInvalidMessage(messageDir, node);
			} catch (IOException e) {
				logger.error("Exception while closing file " + messageDocument.getAbsolutePath(), e);
				handleInvalidMessage(messageDir, node);
			} catch (Exception e) {
				logger.error("Exception while analyzing letter " + messageDir, e);
				handleInvalidMessage(messageDir, node);
			}
		}
		return true;
	}

	private URL getCollectorUrl(Node node) throws PluginException {
		if (node == null){
			String message = "Argument \'node\' should not been null.";
			logger.error(message);
			throw new PluginException(message);
		}
		String collectorURLString = null;
		for (Resource resource : node.getResources().getResource()){
			if (resource.getType().equals(ResourceType.COLLECTOR)){
				collectorURLString = resource.getUrl();
			}
		}
		if (collectorURLString == null){
			String message = "No \'collector\' resource in this node " + node.getName();
			logger.error(message);
			throw new PluginException(message);
		}
		URL collectorURI = null;
		try {
			collectorURI = new URL(collectorURLString);	
		} catch (MalformedURLException e) {
			String message = "Invalid URL: " + collectorURLString;
			logger.error(message);
			throw new GetMailException(message);
		}
		return collectorURI;
	}

	@Override
	public boolean sendMail(URL letter, Node destination) throws PluginException {
		return true;
	}
	
	@Override
	protected boolean handleInvalidMessage(File messageDir, Node node) throws PluginException {
		boolean copied = super.handleInvalidMessage(messageDir, node);
		if (copied) {
			logger.info("Deleting invalid message " + messageDir.getAbsolutePath() + "...");
			FileUtility.silentDeleteDir(messageDir);
			logger.info("...DONE!");
		}
		return true;
	}
}
