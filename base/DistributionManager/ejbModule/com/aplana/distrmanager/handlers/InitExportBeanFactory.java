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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;

import com.aplana.dbmi.ConfigService;
import com.aplana.dbmi.Portal;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.distrmanager.LoadSaveDocFacade;

public class InitExportBeanFactory {
	
	private final Log logger = LogFactory.getLog(getClass());
	
	private static final String CONFIG_FOLDER = "dbmi/gost";
    private static final String PROPERTIES_FILE = CONFIG_FOLDER
	    + "/config.properties";
	private static final String PROPERTIES_ERROR = "jbr.DistributionManager.InitExportManager.notFound.Properties. Properties: " + PROPERTIES_FILE;
	private static final String BEAN_ERROR = "jbr.DistributionManager.ExportDocument.errorConfigBean";

	private InitExportBeanFactory() {
	}
	
	public static InitExportBeanFactory instance() {
		return new InitExportBeanFactory();
	}
	
	private Properties initProp() throws Exception {
		Properties properties = null;
		try {
			properties = getProperties();
			return properties;
		} catch (Exception ioe) {
			logger.error(PROPERTIES_ERROR, ioe);
		    throw ioe;
		}
	}
	
	public DataServiceFacade initServiceBean(BeanFactory beanFactory) throws Exception {
		Properties properties = initProp();
		try {
			String serviceBeanName = properties.getProperty("serviceBean", "systemDataServiceFacade");
			DataServiceFacade serviceBean = (DataServiceFacade)getBean(beanFactory, serviceBeanName);
			return serviceBean;
		} catch (Exception exBean) {
			logger.error(BEAN_ERROR, exBean);
		    throw exBean;
		}
	}
	
	public LoadSaveDocFacade initChangerBean(BeanFactory beanFactory) throws Exception {
		Properties properties = initProp();
		try {
			String changerBeanName = properties.getProperty("changerBean", "gostInterchanger");
			LoadSaveDocFacade changerBean = (LoadSaveDocFacade)getBean(beanFactory, changerBeanName);
			return changerBean;
		} catch (Exception exBean) {
			logger.error(BEAN_ERROR, exBean);
		    throw exBean;
		}
	}
	
	private Properties getProperties() throws IOException {
		final ConfigService configService = Portal.getFactory().getConfigService();
		final InputStream inputProperties = configService.loadConfigFile(PROPERTIES_FILE);
		final Properties properties = new Properties();
	    properties.load(inputProperties);
	    return properties;
	}

	private Object getBean(BeanFactory beanFactory, String beanName) throws DataException {
		Object bean = beanFactory.getBean(beanName);
		if (bean == null) {
			logger.error(String.format("jbr.DistributionManager.InitExportManager.notFoundBean: {%s}.", beanName));
			throw new DataException("jbr.DistributionManager.InitExportManager.notFoundBean");
		}
		return bean;
	}
}
