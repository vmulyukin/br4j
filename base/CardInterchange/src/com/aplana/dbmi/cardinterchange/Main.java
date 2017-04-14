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
package com.aplana.dbmi.cardinterchange;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataService;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.DataServiceHome;
import com.aplana.dbmi.service.SystemUser;

public class Main {
	private final static String SETTINGS_FILENAME = "cardInterchange.properties";
	
	public static void main(String[] args) {
		Properties settings = new Properties();
		try {
			settings.load(new FileInputStream(SETTINGS_FILENAME));
		} catch (Exception e) {
			System.err.println("Couldn't read settings file: " + SETTINGS_FILENAME);
			e.printStackTrace();
			return;
		}
		System.setProperty("jboss.server.config.url", "file:////" + settings.getProperty("portalConfig"));
		
		if (args.length < 1) {
			System.out.println("Command missing: expected commands are import|export");
			return;
		}				
		
		String command = args[0];
		
		if ("export".equals(command)) {
			if (args.length < 2) {
				System.out.println("filename to export into is missing");
				return;				
			}
			String filename = args[1];
			Exporter exporter = new Exporter();
			String[] stTemplateIds = settings.getProperty("templates").split(",");
			List templates = new ArrayList(stTemplateIds.length);
			for(int i = 0; i < stTemplateIds.length; ++i) {
				ObjectId templateId = ObjectIdUtils.getObjectId(Template.class, stTemplateIds[i], true); 
				templates.add(DataObject.createFromId(templateId));
			}
			
			exporter.setTemplates(templates);
			exporter.setFilename(filename);			
			try {
				exporter.setServiceBean(initDataServiceBean("exportJndi.properties"));
				exporter.export();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("import".equals(command)) {
			if (args.length < 2) {
				System.out.println("filename to import from is missing");
				return;				
			}
			String filename = args[1];
			Importer importer = new Importer();
			importer.setFilename(filename);			
			try {
				importer.setServiceBean(initDataServiceBean("importJndi.properties"));
				importer.importCards();
			} catch (Exception e) {				
				e.printStackTrace();
			}
		} else {
			System.out.println("Unknown command: " + command);
		}
	}
	
	private static DataServiceBean initDataServiceBean(String filename) throws Exception {
		DataServiceBean serviceBean;
		Properties jndiProps = new Properties();
		System.out.println("Reading JNDI settings from file " + filename);
		jndiProps.load(new FileInputStream(filename));
		System.out.println("Establishing connection to DataService EJB");
		InitialContext context = new InitialContext(jndiProps);
		Object homeObj = context.lookup("ejb/dbmi");
        DataServiceHome home = (DataServiceHome) PortableRemoteObject.narrow(homeObj, DataServiceHome.class);
		DataService service = home.create();
	
		serviceBean = new DataServiceBean();
		serviceBean.setService(service, service.authUser(new SystemUser(), "127.0.0.1"));
		System.out.println("DataService initialized");
		return serviceBean;
	}
}
