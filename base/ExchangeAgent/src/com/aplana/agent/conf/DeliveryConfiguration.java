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

import com.aplana.agent.conf.delivery.DeliveryLog;
import com.aplana.agent.conf.delivery.FileType;
import com.aplana.agent.conf.delivery.RecordType;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class DeliveryConfiguration {
	public final static String MODEL_PACKAGE = "com.aplana.agent.conf.delivery";
	public final static String DELIVERY_LOG_FILENAME = "deliveryLog.xml";
    protected static final Logger logger = Logger.getLogger(DeliveryConfiguration.class);
    protected static JAXBContext context;
    
    static {
    	try {
			context = JAXBContext.newInstance(MODEL_PACKAGE);
		} catch (JAXBException e) {
			logger.error("Error while initializing JAXB Context", e);
			throw new RuntimeException("Error while loading context for package " + MODEL_PACKAGE, e);
		}
    }
    
    public static DeliveryLogHandler openDeliveryLog(File xmlFile) throws JAXBException {
    	Unmarshaller unmarshaller = context.createUnmarshaller();
    	if (xmlFile.exists()) {
    		return new DeliveryLogHandler((DeliveryLog) unmarshaller.unmarshal(xmlFile), xmlFile);
    	}
    	return new DeliveryLogHandler(new DeliveryLog(), xmlFile);
    }
    
    /**
     * Handler for DeliveryLog JAXB object with xml file.
     * Allows to add new record and save object into xml view.
     * @author desu
     */
    public static class DeliveryLogHandler {
    	protected DeliveryLog delivery;
    	protected File xmlFile;
    	
    	private DeliveryLogHandler(DeliveryLog log, File xmlFile) {
    		this.delivery = log;
    		this.xmlFile = xmlFile;
    	}
    	
    	/**
    	 * Add new record to delivery log with checking all files by MD5 hash. Record is appended to the end of list.
		 * If new MD5 hash will be different from previous value, then throw {@link IncorrectMD5Exception}
		 * @param newRecord new record with information about current operation
    	 * @throws JAXBException
		 * @throws IncorrectMD5Exception
    	 */
    	public void addRecord(RecordType newRecord) throws JAXBException, IncorrectMD5Exception {
			checkMD5(newRecord);

    		delivery.getRecord().add(newRecord);
        }

		/**
		 * Check difference between new record and the last stored record in delivery log
		 * @param newRecord new record with information about current operation
		 * @throws IncorrectMD5Exception if there is at least one file with difference MD5 hash
		 */
		private void checkMD5(RecordType newRecord) throws IncorrectMD5Exception {
			if (delivery.getRecord().isEmpty()) {
				delivery.getRecord().add(newRecord);
				return;
			}
			RecordType lastRecord = delivery.getRecord().get(delivery.getRecord().size() - 1);
			for (FileType lastFileType : lastRecord.getPacket().getFile()) {
				String lastName = lastFileType.getName();
				String lastMD5 = lastFileType.getMd5();
				if (DELIVERY_LOG_FILENAME.equals(lastName)) {
					continue;
				}
				for (FileType newFileType : newRecord.getPacket().getFile()) {
					if (newFileType.getName().equals(lastName)) {
						if (!newFileType.getMd5().equals(lastMD5)) {
							throw new IncorrectMD5Exception("Incorrect MD5 for " + newFileType.getName() + " with hash: " + newFileType.getMd5());
						} else {
							//file ok, go next
							break;
						}
					}
				}
			}
		}

		/**
    	 * Save delivery log info into specified file. If file exists it will
    	 * be overwritten.
    	 * @param newXmlFile full path to xml file where to store (marshal) delivery log
    	 * @throws JAXBException
    	 */
        public void save(File newXmlFile) throws JAXBException {
        	Marshaller marshaller = context.createMarshaller();
        	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        	marshaller.marshal(delivery, newXmlFile);
        }
        
        /**
         * Save delivery log info into the same file
         * @throws JAXBException
         */
        public void save() throws JAXBException {
        	save(this.xmlFile);
        }
    }
}
