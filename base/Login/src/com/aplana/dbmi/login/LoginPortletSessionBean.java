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
package com.aplana.dbmi.login;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.Portal;

public class LoginPortletSessionBean {
	static final String CONFIG_FILENAME = "dbmi/build-info.properties";
	private static Log logger = LogFactory.getLog(LoginPortletSessionBean.class);
	static final String PARAM_BUILDINFO_NUMBER="build.number";
	static final String PARAM_BUILDINFO_DATE="build.date";
	static final String BUILDINFO_UNKNOWN = "unknown";

	private boolean isEnglishLang = false;
	private String buildInfo;	
	private String affectedPortal;

		public LoginPortletSessionBean() {
		super();
		loadBuildInfo();
	}	
	
	public boolean isEnglishLang() {
		return isEnglishLang;
	}

	public void setEnglishLang(boolean isEnglishLang) {
		this.isEnglishLang = isEnglishLang;
	}

	/**
	 * Load build information from config file.
	 */
	private void loadBuildInfo() {
		
		final Properties config = new Properties();
		try {
			config.load( Portal.getFactory().getConfigService().loadConfigFile(CONFIG_FILENAME));
			setBuildInfo(config.getProperty(PARAM_BUILDINFO_NUMBER, BUILDINFO_UNKNOWN));
		} catch (IOException ex) {
			logger.warn("Error loading config from file \'" +CONFIG_FILENAME + "\'" + ". File not found.");
			setBuildInfo(BUILDINFO_UNKNOWN);
		}
	}	

	/**
	 * @return the buildInfo
	 */
	public String getBuildInfo() {
		return this.buildInfo;
	}

	/**
	 * @param buildInfo the buildInfo to set
	 */
	public void setBuildInfo(String buildInfo) {
		this.buildInfo = buildInfo;
	}
	
	public String getAffectedPortal() {
		return affectedPortal;
	}
	
	public void setAffectedPortal(String affectedPortal) {
		this.affectedPortal = affectedPortal;
	}
	
}
