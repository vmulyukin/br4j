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

/**
 * Base class for so-called processors. Processor is a class that is used to
 * process converted XML, that is import card in the system.
 */
public abstract class Processor {

    protected Properties properties;

    public Processor(Properties properties) {
	this.properties = properties;
    }

    /**
     * Imports XML data according to stored in <code>document</code>
     * representation.
     * 
     * @param document -
     *                DOM document contained XML data for processing
     * @return id of imported card or -1 if it is impossible to process this XML
     * @throws ProcessorException
     */
    public abstract long process(Document document) throws ProcessorException;

}
