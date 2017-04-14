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
package com.aplana.medo.converters;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Simple converter for elements that should not be present in result attribute
 * set
 * 
 * @see Converter
 */
public class StubConverter extends Converter {

    public StubConverter(Properties properties, String name) {
	super(properties, name);
    }

    /**
     * Main method of class that used to convert element.
     * 
     * @param document -
     *                DOM document that use to create attribute element
     * @param element -
     *                source DOM element
     * @returns null
     * @throws ConverterException
     * 
     * @see Converter#convert(Document, Element)
     */
    @Override
    public Element convert(Document document, Element element)
	    throws ConverterException {
	return null;
    }

}
