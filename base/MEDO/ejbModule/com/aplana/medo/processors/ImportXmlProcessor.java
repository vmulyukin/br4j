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
package com.aplana.medo.processors;

import java.util.Properties;

import org.w3c.dom.Document;

import com.aplana.medo.CardImportClient;
import com.aplana.medo.XmlUtils;

/**
 * Simple <code>Processor</code> that use 'Import Card' web-service to load
 * XML into system.
 */
public class ImportXmlProcessor extends Processor {

    public ImportXmlProcessor(Properties properties) {
	super(properties);
    }

    /**
     * Imports XML data according stored in <code>document</code>
     * representation using 'Import Card' web-service.
     * 
     * @param document -
     *                DOM document contained XML data for processing
     * @return id of imported card or -1 if it is impossible to process this XML
     * @see Processor
     * @see CardImportClient#callImportCardService(byte[], java.io.File)
     * @throws ProcessorException
     */
    @Override
    public long process(Document document) {
	byte[] bytes = XmlUtils.serialize(document).toByteArray();
	return CardImportClient.callImportCardService(bytes, null);
    }
}
