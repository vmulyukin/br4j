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
package com.aplana.dbmi.module.autounlock;

import java.io.InputStream;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.ejb.support.AbstractStatelessSessionBean;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.service.impl.locks.LockManagementMBean;

public class AutoUnlock extends AbstractStatelessSessionBean implements SessionBean {

	private static final long serialVersionUID = 217270251863896953L;
	private static final int DEFAULT_TIME_UNIT = 60 * 60 * 1000; //hour
	private static final int DEFAULT_LOCK_DURATION = 2 * DEFAULT_TIME_UNIT; //2 hours in ms
	private static final String CONFIG_FILE = "dbmi/AutoUnlockConfig.xml";
	private static final Logger logger = Logger.getLogger(AutoUnlock.class);

	private int lockDuration;
	private int timeUnit;

	public AutoUnlock() {
		setBeanFactoryLocator(ContextSingletonBeanFactoryLocator.getInstance());
		setBeanFactoryLocatorKey("businessBeanFactory");
	}

	protected void onEjbCreate() throws CreateException {
		readConfig();
	}

	@SuppressWarnings("rawtypes")
	public void process(Map<?, ?> parameters) {
		try {
			LockManagementMBean lockManager = (LockManagementMBean) getBeanFactory().getBean("lockManagement");
			lockManager.releaseAgedLocks(lockDuration);
		} catch (Exception ex) {
			logger.error("Error on auto unlock", ex);
		}
	}

	private void readConfig() {
		try {
			InputStream input = Portal.getFactory().getConfigService().loadConfigFile(CONFIG_FILE);
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
			Element root = doc.getDocumentElement();
			NodeList confList = root.getElementsByTagName("lockDuration");
			if (confList.getLength() > 0) {
				Element configElement = (Element) confList.item(0);
				lockDuration = Integer.parseInt(configElement.getTextContent());
			} else {
				lockDuration = DEFAULT_LOCK_DURATION;
			}
			confList = root.getElementsByTagName("timeUnit");
			if (confList.getLength() > 0) {
				Element configElement = (Element) confList.item(0);
				String time = configElement.getTextContent();
				if (time.equals("h")) {
					timeUnit = 60 * 60 * 1000;
				} else if (time.equals("m")) {
					timeUnit = 60 * 1000;
				} else if (time.equals("s")) {
					timeUnit = 1000;
				} else if (time.equals("ms")) {
					timeUnit = 1;
				}
			} else {
				timeUnit = DEFAULT_TIME_UNIT;
			}
			lockDuration = lockDuration * timeUnit;
		} catch (Exception ex) {
			lockDuration = DEFAULT_LOCK_DURATION;
			logger.warn("Error read AutoUnlock config. Use default settings: lockDuration = 2 hours", ex);
		}
	}

}
