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
package com.aplana.dbmi.jbr.util;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.service.impl.Selector;

import java.io.IOException;
import java.io.InputStream;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Checking if notificationSendSelector flag  in conf/dbmi/dmsi/config.properties file is set to true. 
 * Used as condition of running PopulateChildrenWithConditions post-processor in queries.xml
 * 
 * @author odvoryaninov
 */

public class NotificationSendSelector implements Selector {

	private static final String CONFIG_FILE = "dbmi/dmsi/config.properties";
	private static final String KEY = "notificationSendSelector";
	private ResourceBundle configProperties;
	protected final Log logger = LogFactory.getLog(getClass());

	@Override
	public boolean satisfies(Object object) {
		InputStream inputStream = null;
		try {
			inputStream = Portal.getFactory().getConfigService().loadConfigFile(CONFIG_FILE);
			configProperties = new PropertyResourceBundle(inputStream);
			inputStream.close();
		} catch (IOException e) {
			logger.error("Can't read notificationSendSelector flag from config file: ", e);
		}
		String value;
		try {
			value = configProperties.getString(KEY);
		} catch(Exception excep) {
			logger.info("NotificationSendSelector flag value is false (by default).  Add \""+ KEY + "=1\" to " + CONFIG_FILE + " file to set the flag to true" );
			return false; 
		}	
		logger.info("NotificationSendSelector flag value is " + (value.equals("1") ? "true" : "false"));
		return value.equals("1") ? true : false;		
	}
}
