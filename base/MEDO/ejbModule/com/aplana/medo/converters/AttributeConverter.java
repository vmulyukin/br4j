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

import static com.aplana.medo.cards.CardXMLBuilder.CODE_ATTRIBUTE;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Simple converter for the 'attribute' input element. It checks whether
 * attribute is valid (code is defined).
 *
 * @see Converter
 */
public class AttributeConverter extends Converter {

    public AttributeConverter(Properties properties, String name) {
	super(properties, name);
    }

    /**
     * Main method of class that used to convert element. Checks whether
     * 'attribute' element has defined code (does not equal to value of
     * 'code.undefined' property). Clean child elements corresponds to auxiliary
     * information.
     *
     * @param document -
     *                DOM document that use to create attribute element
     * @param element -
     *                source DOM element
     * @returns the source element itself or null if <em>attribute</em> has
     *          undefined code
     * @throws ConverterException
     *
     * @see Converter#convert(Document, Element)
     */
    @Override
    public Element convert(Document document, Element element)
	    throws ConverterException {
	final String UNDEFINED_CODE = properties.getProperty("code.undefined");
	String codeValue = element.getAttribute(CODE_ATTRIBUTE);
	if (codeValue.equals(UNDEFINED_CODE)) {
	    return null;
	}
	NodeList auxiliaryList = element.getElementsByTagName(AUXILIARY_TAG);
	for (int i = 0; i < auxiliaryList.getLength(); i++) {
	    element.removeChild(auxiliaryList.item(i));
	}

	return element;
    }
}
