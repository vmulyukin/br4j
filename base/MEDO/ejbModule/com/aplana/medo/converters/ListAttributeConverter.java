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
import com.aplana.medo.cards.CardXMLBuilder;
import com.aplana.medo.converters.Converter;
import com.aplana.medo.converters.ConverterException;

/**
 * <p>
 * Base class for converters that can help to create valid XML tag for card
 * attribute of 'list' type. As input it get the DOM element that contains
 * <code>attribute</code> elements as children. These attributes define
 * parameters that are used to fetch id of appropriate card.
 * </p>
 *
 * @see Converter
 */
public abstract class ListAttributeConverter extends Converter {
    private String outCode;

    /**
     * Creates a new instance from a <code>properties</code> configuration and
     * a tag name of input DOM element.
     *
     * @param properties -
     *                configuration that should contain all necessary
     *                information that will be used to convert. This is
     *                'code.fromTag.<code>name</code>' that define 'code'
     *                XML-attribute of result <em>attribute</em> element
     * @param name -
     *                tag name of input DOM element. Used to get concrete
     *                property from appropriate set of properties (e.g.
     *                'code.fromTag' sets).
     */
    public ListAttributeConverter(Properties properties, String name) {
	super(properties, name);
	this.outCode = properties.getProperty("code.fromTag." + name);
    }

    /**
     * <p>
     * Returns the code of creating <em>attribute</em> element. Value is
     * defined according to value of property with 'code.fromTag.<code>name</code>'
     * key.
     * </p>
     *
     * @return the code of creating <em>attribute</em> element
     */
    public String getOutCode() {
	return outCode == null ? "" : outCode;
    }

    /**
     * Main method of class that used to convert element contained
     * <em>attribute</em>s children that specify parameters for card search.
     *
     * @param document -
     *                DOM document that use to create <em>attribute</em>
     *                element
     * @param element -
     *                source DOM element
     * @return <em>attribute</em> element of 'cardLink' type with id of card
     *         as value or null if card was not found or was not created
     *         correctly (-1 id was returned)
     * @throws ConverterException
     */
    @Override
    public Element convert(Document document, Element element)
	    throws ConverterException {
    final String UNDEFINED_CODE = properties.getProperty("code.undefined");
    String codeValue = element.getAttribute(CODE_ATTRIBUTE);
	if (codeValue.equals(UNDEFINED_CODE)) {
	    return null;
	}
	long id = processValues(element);
	if (id == -1) {
	    return null;
	}

	return CardXMLBuilder.createAttribute(document, getOutCode(),
		CardXMLBuilder.LIST_TYPE, String.valueOf(id));
    }

    /**
     * Perform processing of read values. The {@link #processValues()} method
     * should be used to retrieve value by given key (suffix of 'code.<code>suffix</code>').
     *
     * @return id of found or created card.
     * @throws ConverterException
     */
    protected abstract long processValues(Element element) throws ConverterException;

    /**
     * Returns value of property with 'code.<em>key</em>' key
     *
     * @param key
     *                alias for required code
     * @return value of property with 'code.<em>key</em>' key
     */
    protected String getPredefinedParam(String key) {
	return properties.getProperty("param." + key);
    }

}
