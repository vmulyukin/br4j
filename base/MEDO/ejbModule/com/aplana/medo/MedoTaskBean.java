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
// Redacted Panichev P.N; 28.10.2010 11:20

package com.aplana.medo;

import static com.aplana.medo.converters.cards.FileCardLinkConverter.WORKING_FOLDER_KEY;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.ejb.support.AbstractStatelessSessionBean;
import org.xml.sax.SAXException;

import com.aplana.dbmi.ConfigService;
import com.aplana.dbmi.Portal;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.medo.cards.CardException;

public class MedoTaskBean extends AbstractStatelessSessionBean implements
		SessionBean {
	private static final long serialVersionUID = 3206093459760846199L;
	private static final String CONFIG_FOLDER = "dbmi/medo";
	private static final String CONFIG_FILE = CONFIG_FOLDER
			+ "/options.properties";
	private static final String PROPERTIES_FILE = CONFIG_FOLDER
			+ "/predefined.properties";

	private static DateFormat PREFIX_FILE_FORMAT = new SimpleDateFormat(
			"yyyyMMddHHmmssSSS");

	protected final Log logger = LogFactory.getLog(getClass());

	protected Properties options;

	private String inFolderPath = "";
	private String inProcessedFolderPath = "";
	private String inDiscardedFolderPath = "";
	private String templateIn = "";
	private String templateImportedDoc = "";
	private String inTicketsPath = "";
	private String processedTicketsPath = "";
	private String xmlSchemaFile = "";
	private boolean doubleXML = false;
	private DataServiceFacade serviceBean;

	// Properties ��� ��������
	protected String outFolderExport = "";
	protected String templateOutExport = "";

	private static Boolean working = false;

	private Importer importer = new Importer();
	private ExporterR exporter = new ExporterR();
	private OutNotificationR outNotification = new OutNotificationR();

	@Override
	public void setSessionContext(SessionContext sessionContext) {
		super.setSessionContext(sessionContext);
		setBeanFactoryLocator(ContextSingletonBeanFactoryLocator.getInstance());
		setBeanFactoryLocatorKey("businessBeanFactory");
	}

	@Override
	protected void onEjbCreate() throws CreateException {
		this.readConfig();
		this.initializeFolders(new String[] { inProcessedFolderPath, inDiscardedFolderPath, processedTicketsPath });
		serviceBean = (DataServiceFacade) getBeanFactory().getBean("systemDataServiceFacade");
	}

	public void process(Map<?, ?> parameters) {
		synchronized (MedoTaskBean.class) {
			if (working)
				return;
			working = true;
		}
		try {
			final ConfigService configService = Portal.getFactory()
					.getConfigService();
			try {
				final InputStream inputProperties = configService
						.loadConfigFile(PROPERTIES_FILE);
				try {
					final Properties properties = new Properties();
					properties.load(inputProperties);
					importer.setProperties(properties);
					importer.setServiceBean(serviceBean);
					exporter.setProperties(properties);
					exporter.setServiceBean(serviceBean);
					outNotification.setProperties(properties);
				} finally {
					IOUtils.closeQuietly(inputProperties);
				}
			} catch (IOException ex) {
				logger.error("Error during properties read", ex);
			}
			try {
				final String schemaFile = CONFIG_FOLDER + "/"
						+ this.xmlSchemaFile;
				final SchemaFactory schemaFactory = SchemaFactory
						.newInstance(XmlUtils.W3C_XML_SCHEMA_NS_URI);
				final URL schemaURL = configService
						.getConfigFileUrl(schemaFile);
				final Schema schema = schemaFactory.newSchema(schemaURL);
				importer.setSchema(schema);
			} catch (SAXException ex) {
				logger.error("Error during schema creation", ex);
			} catch (IOException ex) {
				logger.error("Error during schema file reading", ex);
			}
			try {
				this.processTickets();
			} catch (Exception ex) {
				logger.error("Error during process tickets", ex);
			}
			try {
				this.processIN();
			} catch (Exception ex) {
				logger.error("Error during process income files", ex);
			}
			try {
				exporter.proccessOUT();
			} catch (Exception ex) {
				logger.error("Error during process out files", ex);
			}
			try {
				this.processOutNotification();
			} catch (Exception ex) {
				logger.error("Error during process out notification", ex);
			}
		} finally {
			synchronized (MedoTaskBean.class) {
				working = false;
			}
		}
	}

	/**
	 * Reads options from {@link #CONFIG_FILE} file and properties from
	 * {@link #PROPERTIES_FILE} and initialize appropriate fields of bean
	 */
	private void readConfig() {
		try {
			final InputStream inputOptions = Portal.getFactory()
					.getConfigService().loadConfigFile(CONFIG_FILE);
			try {
				options = new Properties();
				options.load(inputOptions);
			} finally {
				IOUtils.closeQuietly(inputOptions);
			}

			this.inFolderPath = options.getProperty("InFolder");
			this.inProcessedFolderPath = options
					.getProperty("InFolderProcessed");
			this.inDiscardedFolderPath = options
					.getProperty("InFolderDiscarded");
			this.templateIn = options.getProperty("InTemplate");
			this.templateImportedDoc = options
					.getProperty("ImportedDocTemplate");

			this.inTicketsPath = options.getProperty("ticketsPath");
			this.processedTicketsPath = options
					.getProperty("processedTicketsPath");

			this.xmlSchemaFile = options.getProperty("xmlSchema");

			this.outFolderExport = options.getProperty("outFolderExport");
			this.templateOutExport = options.getProperty("templateOutExport");
			try {
				this.doubleXML = Boolean.parseBoolean(options.getProperty(
						"doubleXML", "false"));
			} catch (Exception e) {
				logger.warn("com.aplana.medo.MedoTaskBean: doubleXML is not set.");
			}
			exporter.setOptions(options);
			exporter.setOutFolderExport(outFolderExport);
			exporter.setTransformerPath(CONFIG_FOLDER + "/"
					+ this.templateOutExport);
			exporter.setDouble(this.doubleXML);
			exporter.setSendSourceAttachment(Boolean.parseBoolean(options.getProperty("sendSourceAttachment", "false")));
			outNotification.setOptions(options);
			outNotification.setOutFolderExport(outFolderExport);
			outNotification.setTransformerPath(CONFIG_FOLDER + "/"
					+ this.templateOutExport);
			outNotification.setDouble(this.doubleXML);

			final Properties configurationProperties = new Properties();
			final String ticketTemplateFile = getAbsolutePath(options
					.getProperty("ticketTemplate"));
			if (ticketTemplateFile != null) {
				configurationProperties.setProperty(
						MedoConfiguration.TICKET_TEMPLATE, ticketTemplateFile);
			}
			final String clientsFile = options.getProperty("clientsFilePath");
			if (clientsFile != null && new File(clientsFile).exists()) {
				configurationProperties.setProperty(
						MedoConfiguration.CLIENTS_FILE, clientsFile);
			}
			MedoConfiguration.initialize(configurationProperties);
		} catch (IOException ex) {
			logger.error("Error during configuration initialization", ex);
		}
	}

	/**
	 * Creates given by <code>paths</code> folders if one does not exist
	 * 
	 * @param paths
	 *            array of folder paths
	 */
	private void initializeFolders(String[] paths) {
		for (String path : paths) {
			if (path == null)
				continue;
			final File folder = new File(path);
			if (!folder.exists()) {
				folder.mkdir();
			}
		}
	}

	private String getAbsolutePath(String fileName) throws IOException {
		if (fileName == null || "".equals(fileName)) {
			return null;
		}
		final ConfigService configService = Portal.getFactory()
				.getConfigService();
		final URL fileURL = configService.getConfigFileUrl(CONFIG_FOLDER + "/"
				+ fileName);
		File file = null;
		try {
			file = new File(fileURL.toURI());
		} catch (URISyntaxException ex) {
			logger.error("Unable to convert URL to URI", ex);
		}
		if (file != null && file.exists()) {
			return file.getAbsolutePath();
		}
		return null;
	}

	/**
	 * Scans {@link #inTicketsPath} folder and start processing
	 * {@link Importer#importTicket(File)} of ticket files (ends with
	 * {@link #TICKET_NAME_SUFFIX}. In case of successful finish of operation
	 * source file with date prefix ({@link #PREFIX_FILE_FORMAT} is moved into
	 * {@link #processedTicketsPath}. In case of some error it is moved in the
	 * same directory but has '.discarded' suffix additionally.
	 */
	private void processTickets() {
		final File dir = new File(this.inTicketsPath);
		final File dest = new File(this.processedTicketsPath);

		if (dir.canRead()) {
			final File[] files = dir.listFiles(new FilenameFilter() {
				public boolean accept(File directory, String name) {
					return name.endsWith(Importer.TICKET_NAME_SUFFIX);
				}
			});
			for (int j = 0; j < files.length; j++) {
				final File processingFile = files[j];
				String destFileName = String.format("%s_%s",
						PREFIX_FILE_FORMAT.format(new Date()),
						processingFile.getName());
				try {
					importer.importTicket(processingFile);
				} catch (Exception ex) {
					logger.warn(String.format("The '%s' ticket was discarded",
							processingFile.getName()), ex);
					destFileName += ".discarded";
				}
				try {
					FileUtils.moveFile(processingFile, new File(dest,
							destFileName));
				} catch (IOException ex) {
					logger.error(String.format("The '%s' file cannot be moved",
							processingFile.getName()), ex);
				}
				logger.info(String.format("The '%s' ticket is processed.",
						processingFile.getName()));
			}
		} else {
			logger.error(String.format("Can't read from '%s' directory ",
					this.inTicketsPath));
		}
	}

	/**
	 * Scans {@link #inFolderPath} folder and start processing
	 * {@link Importer#importXml(File)} of XML files for each directory. In case
	 * of successful finish of operation source directory is moved into
	 * {@link #inProcessedFolderPath} else it is moved into
	 * {@link #inDiscardedFolderPath} directory.
	 */
	private void processIN() {
		importer.setTransformer(initializeTransformer(this.templateIn));
		importer.setImportedDocTransformer(initializeTransformer(this.templateImportedDoc));

		final File dir = new File(this.inFolderPath);
		File dest = null;

		if (dir.canRead()) {
			final File[] directories = dir.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.isDirectory();
				}
			});

			for (int i = 0; i < directories.length; i++) {
				final File processingDirectory = directories[i];
				importer.getProperties().setProperty(WORKING_FOLDER_KEY,
						processingDirectory.getAbsolutePath());

				final File[] files = processingDirectory
						.listFiles(new FilenameFilter() {
							public boolean accept(File directory, String name) {
								return name.toLowerCase().endsWith(".xml");
							}
						});

				for (int j = 0; j < files.length; j++) {
					final String destFolderName = String.format("%s_%s",
							PREFIX_FILE_FORMAT.format(new Date()),
							processingDirectory.getName());
					try {
						importer.importXml(files[j]);
						dest = new File(this.inProcessedFolderPath,
								destFolderName);
					} catch (Exception ex) {
						logger.warn(String.format(
								"The '%s' document was discarded",
								processingDirectory.getName()), ex);
						dest = new File(this.inDiscardedFolderPath,
								destFolderName);

					}
					try {
						FileUtils.moveDirectory(processingDirectory, dest);
					} catch (IOException ex) {
						logger.error(String.format(
								"The '%s' directory cannot be moved",
								processingDirectory), ex);
					}
					logger.info(String.format(
							"The '%s' directory is processed.",
							processingDirectory.getName()));
				}
			}
		} else {
			logger.error(String.format("Can't read from '%s' directory ",
					this.inFolderPath));
		}
	}

	/**
     *
     */
	private void processOutNotification() {
		try {
			outNotification.process();
		} catch (CardException ex) {
			logger.error(
					"Error during OutNotification creation (CardException) ",
					ex);
		}
	}

	/**
	 * Initializes XSLT transformer that placed in configuration folder of
	 * Portal in file with given <code>fileName</code>
	 * 
	 * @param fileName
	 *            - file name or relative path from configuration folder
	 * @return Transformer instance
	 */
	private Transformer initializeTransformer(String fileName) {
		final ConfigService configService = Portal.getFactory()
				.getConfigService();
		try {
			final TransformerFactory tFactory = TransformerFactory
					.newInstance();
			final String xsltFile = CONFIG_FOLDER + "/" + fileName;
			final InputStream inputStream = configService
					.loadConfigFile(xsltFile);
			final String URI = configService.getConfigFileUrl(xsltFile).toURI()
					.toString();
			final Transformer transformer = tFactory
					.newTransformer(new StreamSource(inputStream, URI));
			return transformer;
		} catch (TransformerConfigurationException ex) {
			logger.error("Error during transformer creation", ex);
		} catch (IOException ex) {
			logger.error("Error during template file for transformer reading: "
					+ fileName, ex);
		} catch (URISyntaxException ex) {
			logger.error("Error during xslt file URI resolve", ex);
		}
		return null;
	}
}