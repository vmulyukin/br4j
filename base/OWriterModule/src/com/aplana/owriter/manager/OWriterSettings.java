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
package com.aplana.owriter.manager;

import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.Portal;

/**
 * ����� ��� ��������� �������� ������ �������������� �������� �� �����
 *
 * @author Vlad Alexandrov
 * @version 1.0
 * @since   2014-10-03
 */

public class OWriterSettings {

	private static Log logger = LogFactory.getLog(OWriterSettings.class);

	private final static String CONFIG = "dbmi/owriter/service.properties";
		
	private final static String OWRITER_LOCAL_DIR = "owriter.local.shared.folder";
	private final static String OWRITER_SERVICE_DIR = "owriter.service.shared.folder";
	private final static String OWRITER_SERVICE_URL = "owriter.service.base.url";
	private final static String OWRITER_SERVICE_LOGIN = "owriter.service.login";
	private final static String OWRITER_SERVICE_PASSWORD = "owriter.service.password";
	private final static String OWRITER_SERVICE_USERS_GROUP = "owriter.service.users.group";
	private final static String OWRITER_SERVICE_APPS_GROUP = "owriter.service.apps.group";
		
	private static Properties props;
		
	private static Object synch = new Object();

	public static Properties getProps() {
		synchronized  (synch) {
			if (props == null) {			
				try {
					final InputStream is = Portal.getFactory().
							getConfigService().getConfigFileUrl(CONFIG).openStream();
					try {
					final Properties p = new Properties();
					p.load(is);
					props = p;
					} finally {
						IOUtils.closeQuietly(is);
					}
				} catch (IOException e) {
					logger.error("Couldn't read settings file " + CONFIG, e);
				}
			}
		}
		return props;
	}

	public static String getLocalSharedDir() {
		return getProps().getProperty(OWRITER_LOCAL_DIR);
	}
		
	public static String getServiceSharedDir() {
		return getProps().getProperty(OWRITER_SERVICE_DIR);
	}
		
	public static String getServiceBaseUrl() {
		return getProps().getProperty(OWRITER_SERVICE_URL);
	}

	public static String getServiceLogin(){
		return getProps().getProperty(OWRITER_SERVICE_LOGIN);
	}
		
	public static String getServicePassword(){
		return getProps().getProperty(OWRITER_SERVICE_PASSWORD);
	}

	public static String getUsersGroup(){
		return getProps().getProperty(OWRITER_SERVICE_USERS_GROUP);
	}

	public static String getAppsGroup(){
		return getProps().getProperty(OWRITER_SERVICE_APPS_GROUP);
	}
}