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
package com.aplana.dbmi.replication.processors;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.replication.servernodeconfig.ReplicationNodeConfig;
import com.aplana.dbmi.replication.templateconfig.ReplicationTemplateConfig;
import com.aplana.dbmi.replication.templateconfig.ReplicationTemplateConfig.Template;
import com.aplana.dbmi.service.DataException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ReplicationConfiguration {
	public static final String replicationNodeConfigFile     = "ReplicationNodeConfig.xml";
	public static final String replicationTemplateConfigFile = "ReplicationTemplateConfig.xml";
	private static ReplicationNodeConfig replicationNodeConfig;
	private static ReplicationTemplateConfig replicationTemplateConfig;
	private static List<ObjectId> independentTemplates;

	private static Log logger = LogFactory.getLog(ReplicationConfiguration.class);
	
	private static JAXBContext nodeContext;
	private static JAXBContext templateContext;
	
	static {
		try {
			ClassLoader clr = ReplicationConfiguration.class.getClassLoader();
			nodeContext     = JAXBContext.newInstance("com.aplana.dbmi.replication.servernodeconfig", clr);
			templateContext = JAXBContext.newInstance("com.aplana.dbmi.replication.templateconfig", clr);
		} catch (JAXBException e) {
			logger.error("Can't create JAXB context", e);
			throw new RuntimeException("Can't create JAXB context", e);
		}
	}

	public static ReplicationNodeConfig getReplicationNodeConfig() throws JAXBException {
		if (replicationNodeConfig == null) {
			Unmarshaller unmarshaller = nodeContext.createUnmarshaller();
			try {
				InputStream stream = Portal.getFactory().getConfigService()
					.loadConfigFile("dbmi/replication/"+replicationNodeConfigFile);
				replicationNodeConfig = (ReplicationNodeConfig) unmarshaller.unmarshal(stream);
			} catch (IOException e) {
				replicationNodeConfig = new ReplicationNodeConfig();
			}
		}
		return replicationNodeConfig;
	}

	public static ReplicationTemplateConfig getReplicationTemplateConfig() throws JAXBException {
		if (replicationTemplateConfig == null) {
			Unmarshaller unmarshaller = templateContext.createUnmarshaller();
			try {
				InputStream stream = Portal.getFactory().getConfigService()
					.loadConfigFile("dbmi/replication/"+replicationTemplateConfigFile);
				replicationTemplateConfig = (ReplicationTemplateConfig) unmarshaller.unmarshal(stream);
			} catch (IOException e) {
				replicationTemplateConfig = new ReplicationTemplateConfig();
			}
			
		}
		return replicationTemplateConfig;
	}

	public static Template getTemplateConfig(long templateId) throws JAXBException, IOException {
		Template result = null;

		for (Template templateConfig : getReplicationTemplateConfig().getTemplate()) {
			if (templateConfig.getId() == templateId) {
				result = templateConfig;
				break;
			}
		}
		return result;
	}

	public static boolean isReplicationActive() {
		try {
			ReplicationTemplateConfig templateConfig = getReplicationTemplateConfig();
			return templateConfig != null && templateConfig.getTemplate() != null
					&& !templateConfig.getTemplate().isEmpty();
		} catch (Exception ex) {
			logger.error("It is impossible to resolve config", ex);
		}
		return false;
	}
	
	public static boolean isTemplateIndependent(Template config) {
		return config.getRoot() == null;
	}
	
	public synchronized static List<ObjectId> getIndependentTemplates() throws DataException {
		if (independentTemplates != null) {
			return independentTemplates;
		}
		try {
			independentTemplates = new ArrayList<ObjectId>();
			for(Template t : getReplicationTemplateConfig().getTemplate()) {
				if (isTemplateIndependent(t)) {
					independentTemplates.add(new ObjectId(com.aplana.dbmi.model.Template.class, t.getId()));
				}
			}
			return independentTemplates;
		} catch (Exception e) {
			independentTemplates = null;
			logger.error("Can't read " + replicationTemplateConfigFile, e);
			throw new DataException(e);
		}
	}

	public static void deactivate() throws JAXBException {
		getReplicationTemplateConfig().getTemplate().clear();
	}

	public static void activate() throws JAXBException {
		replicationTemplateConfig = null;
		replicationNodeConfig = null;
		getReplicationTemplateConfig();
		getReplicationNodeConfig();
	}
}
