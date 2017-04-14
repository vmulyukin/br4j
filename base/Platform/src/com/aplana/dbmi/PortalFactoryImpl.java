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

import java.util.Properties;

import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.task.Scheduler;

public class PortalFactoryImpl implements PortalFactory
{
	public static final String CONFIG_PATH = "com/aplana/dbmi/platform.properties";
	
	private Properties services = new Properties();
	
	public PortalFactoryImpl() {
		try {
			services.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_PATH));
		} catch (Exception e) {
			LogFactory.getLog(getClass()).error("Can't read platform configuration", e);
		}
	}
	
	private Object getService(String type) {
		if (!services.containsKey(type)) {
			LogFactory.getLog(getClass()).error("Service implementation for " + type + " not defined");
			return null;
		}
		try {
			return Class.forName(services.getProperty(type)).newInstance();
		} catch (Exception e) {
			LogFactory.getLog(getClass()).error("Error loading " + services.getProperty(type), e);
			return null;
		}
	}
	
	public PortletService getPortletService() {
		return (PortletService) getService(PortletService.class.getName());
	}

	public Scheduler getSchedulerService() {
		return (Scheduler) getService(Scheduler.class.getName());
	}

	public UserService getUserService() {
		return (UserService) getService(UserService.class.getName());
	}

	public ConfigService getConfigService() {
		return (ConfigService) getService(ConfigService.class.getName());
	}
}
