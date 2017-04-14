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
package com.aplana.agent.conf;

import com.aplana.ws.soz.model.communication.Communication;

import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;

/**
 * Helper class to work with JAXB model
 */
public class CommunicationConfiguration {
    public final static String MODEL_PACKAGE = "com.aplana.ws.soz.model.communication";
    protected static final Logger logger = Logger.getLogger(CommunicationConfiguration.class);
    private static CommunicationConfiguration instance;
    protected static JAXBContext context;
    
    static {
    	try {
    		context = JAXBContext.newInstance(MODEL_PACKAGE);
    	} catch (JAXBException e) {
    		logger.error("Error while initializing JAXB Context", e);
    		throw new RuntimeException("Error while loading context for package " + MODEL_PACKAGE, e);
    	}
    }

    public static synchronized CommunicationConfiguration getInstance() {
    	if (instance == null) {
    		instance = new CommunicationConfiguration();
    	}
    	return instance;
    }
    
    public Communication unmarshal(InputStream xml) throws JAXBException {
    	Unmarshaller unmarshaller = context.createUnmarshaller();
    	return (Communication) unmarshaller.unmarshal(xml);
    }
}
