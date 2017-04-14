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

import java.io.File;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.aplana.agent.conf.envelope.Letter;

/**
 * Offers functionality for converting from xml to model class {@link Letter} and vice versa.
 * @author atsvetkov
 *
 */
public class DistributionLetterParser {
    public final static String MODEL_PACKAGE = "com.aplana.agent.conf.envelope";
    protected static final Logger logger = Logger.getLogger(DeliveryConfiguration.class);
    private static DistributionLetterParser instance;
    protected static JAXBContext context;
    
    static {
    	try {
			context = JAXBContext.newInstance(MODEL_PACKAGE);
		} catch (JAXBException e) {
			logger.error("Error while initializing JAXB Context", e);
			throw new RuntimeException("Error while loading context for package " + MODEL_PACKAGE, e);
		}
    }
    
    public static synchronized DistributionLetterParser getInstance() {
        if (instance == null) {
            instance = new DistributionLetterParser();
        }
        return instance;
    }
    
    /**
     * Converts from xml file to model class {@link Letter}.
     * @param xml
     * @return
     * @throws JAXBException
     */
    public Letter unmarshal(InputStream xml) throws JAXBException {
    	Unmarshaller unmarshaller = context.createUnmarshaller();
        return (Letter) unmarshaller.unmarshal(xml);
    }
    
    /**
     * Saves model class {@link Letter} in xml file destFile.
     * @param letter
     * @param destFile
     * @throws JAXBException
     */
    public void marshal(Letter letter, File destFile) throws JAXBException {
    	Marshaller marshaller = context.createMarshaller();
    	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(letter, destFile);
    }
}
