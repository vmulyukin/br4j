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
package com.aplana.dbmi;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import org.jboss.logging.Logger;

/**
 * The class is represented as a container
 * that holds configurations loaded from properties file
 * 
 */
public class ConfigHolder {

	private static final Logger logger = Logger.getLogger(ConfigHolder.class);
	
	public static final String CONFIG_FOLDER = "dbmi/";
	public static final String PAGE_LABELS = CONFIG_FOLDER + "page-labels.properties";
	private static final Properties pageLabels = new Properties();
		
	static {
		loadProperties(pageLabels, PAGE_LABELS);
	}
	
	public static String getPageLabel(final String key) {
		return pageLabels.getProperty(key) != null ? pageLabels.getProperty(key) : "";
	}
	
	public static String getPageLabel(final String... keys) {
		StringBuilder sb = new StringBuilder();
		for(String key : keys)
			sb.append(getPageLabel(key));
		return sb.toString();
	}
	
	private static void loadProperties(Properties properties, String fileName) {
		InputStream in = null;
		try {
			in = Portal.getFactory().getConfigService().loadConfigFile(fileName);
			if (in != null){
				Reader reader = new InputStreamReader(in, "UTF-8");
				properties.load(reader);
			}
		} catch(IOException e) {
			logger.error("Cannot load the config file: " + fileName + " cause of: ", e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch(IOException e) {
				logger.error("Cannot close input stream of config file " + fileName + " cause of: ", e);
			}
		}
	}
}
