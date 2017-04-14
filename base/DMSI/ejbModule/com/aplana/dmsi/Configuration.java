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
package com.aplana.dmsi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.InputStreamResource;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dmsi.config.ConfigurationException;
import com.aplana.dmsi.expansion.ExpansionManager;
import com.aplana.dmsi.util.ServiceUtils;

public class Configuration {

	private static Configuration instance;
	private static Log logger = LogFactory.getLog(Configuration.class);
	private static final String CONFIG_FILE = "config.properties";

	private ResourceBundle config;
	private static final String RESOURCE_BUNDLE = "nls.gost_messages";
	private static final String CODES_BUNDLE = "nls.codes";
	private ResourceBundle messagesBundle;
	private ResourceBundle codesBundle;
	private JAXBContext context;
	private ApplicationContext appContext;

	private static Map<Long, String> modesByThread = new ConcurrentHashMap<Long, String>();

	public static String getCurrentMode() {
		String mode = modesByThread.get(getId());
		if (logger.isDebugEnabled()) {
			logger.debug("[" + getId() + "] Mode is [" + mode + "]");
		}
		return mode;
	}

	public static void setMode(String mode) {
		if (mode == null || "".equals(mode)) {
			return;
		}
		modesByThread.put(getId(), mode);
		if (logger.isDebugEnabled()) {
			logger.debug("[" + getId() + "] Set mode [" + mode + "]");
		}
	}

	public static void reset() {
		modesByThread.remove(getId());
		if (logger.isDebugEnabled()) {
			logger.debug("[" + getId() + "] Mode is reset");
		}
	}

	private static Long getId() {
		return Thread.currentThread().getId();
	}

	private Configuration() {
	}

	public void init() throws IOException {
		this.config = loadConfig();
	}

	public synchronized static Configuration instance() {
		if (instance == null) {
			try {
				InputStream is = ServiceUtils.readConfig("beans.xml");
				GenericApplicationContext ctx = new GenericApplicationContext();
				XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
				xmlReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_DTD);
				xmlReader.loadBeanDefinitions(new InputStreamResource(is));
				instance = (Configuration) ctx.getBean("config");
				instance.setApplicationContext(ctx);
			} catch (IOException ex) {
				throw new ConfigurationException("Unable to read config from file " + "beans.xml", ex);
			} catch (RuntimeException ex) {
				throw new ConfigurationException("Unable to initialize configuration", ex);
			}
		}
		return instance;
	}

	public void setApplicationContext(ApplicationContext appContext) {
		this.appContext = appContext;
	}

	public ExpansionManager getExpansionManager() {
		return (ExpansionManager) appContext.getBean("expansionManager");
	}

	public JAXBContext getJAXBContext() {
		return this.context;
	}

	public void setJAXBContext(JAXBContext context) {
		this.context = context;
	}

	public String getStandart() {
		return getProperty("standart");
	}

	public String getVersion() {
		return getProperty("version");
	}

	public String getSysId() {
		return getProperty("sys_id");
	}

	public String getSystem() {
		return getProperty("system");
	}

	public String getSystemDetails() {
		return getProperty("system_details");
	}

	public ObjectId getDefaultOrganizationId() {
		String orgId = getProperty("default_organization_id");
		try {
			long id = Long.parseLong(orgId);
			return new ObjectId(Card.class, id);
		} catch (NumberFormatException ex) {
			throw new ConfigurationException("Incorrect value of default organization id " + orgId, ex);
		}
	}

	public String[] getMappingFileNames() {
		String fileNames = getProperty("mappingFiles");
		if (fileNames == null || "".equals(fileNames.trim()))
			return new String[] { "mapping.xml" };
		return fileNames.trim().split("\\s*,\\s*");
	}

	public String[] getTypesFileNames() {
		String fileNames = getProperty("typesFiles");
		if (fileNames == null || "".equals(fileNames.trim()))
			return new String[] { "types.xml" };
		return fileNames.trim().split("\\s*,\\s*");
	}

	public String getInFolderName() {
		return getProperty("inFolder");
	}

	public String getProcessedInFolderName() {
		return getProperty("inFolderProcessed");
	}

	public String getDiscardedInFolderName() {
		return getProperty("inFolderDiscarded");
	}

	public String getOutFolderName() {
		return getProperty("outFolder");
	}

	private String getProperty(String key) {
		String value = "";
		try {
			value = config.getString(key);
		} catch (MissingResourceException ex) {
			// return empty string if there are no such source
		}
		return value;
	}

	private ResourceBundle loadConfig() throws IOException {
		InputStream is = ServiceUtils.readConfig(CONFIG_FILE);
		ResourceBundle configProperties = new PropertyResourceBundle(is);
		is.close();
		messagesBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE, ContextProvider.getContext().getLocale());
		codesBundle = ResourceBundle.getBundle(CODES_BUNDLE, ContextProvider.getContext().getLocale());
		return configProperties;
	}

	public GOSTMessage getGOSTMessageByCode(String messageCode) throws ConfigurationException {
		Long code = getMessageCode(messageCode);
		if (code == null) {
			throw new ConfigurationException("There is no code with alias " + messageCode);
		}
		String message = getMessage(messageCode);
		if (message == null) {
			throw new ConfigurationException("There is no message with alias" + messageCode);
		}
		return new GOSTMessage(code, message);

	}

	private Long getMessageCode(String messageCode) {
		Long code = null;
		try {
			code = Long.valueOf(codesBundle.getString(messageCode));
		} catch (RuntimeException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("There is no code with alias " + messageCode, ex);
			}
		}
		return code;
	}

	private String getMessage(String messageCode) {
		String message = null;
		try {
			message = messagesBundle.getString(messageCode);
		} catch (RuntimeException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("There is no message with alias " + messageCode, ex);
			}
		}
		return message;
	}

	public static class GOSTMessage {
		private final Long code;
		private final String message;

		public GOSTMessage(Long code, String message) {
			super();
			this.code = code;
			this.message = message;
		}

		public Long getCode() {
			return this.code;
		}

		public String getMessage() {
			return this.message;
		}

	}
}
