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
package com.aplana.medo.converters.cards;

import static com.aplana.medo.cards.CardXMLBuilder.ATTRIBUTE_TAG;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aplana.medo.cards.CardXMLBuilder;
import com.aplana.medo.converters.Converter;
import com.aplana.medo.converters.ConverterException;

/**
 * <p>
 * Base class for converters that can help to create valid XML tag for card
 * attribute of 'cardLink' type. As input it get the DOM element that contains
 * <code>attribute</code> elements as children. These attributes define
 * parameters that are used to fetch id of appropriate card.
 * </p>
 *
 * @see Converter
 */
public abstract class CardConverter extends Converter {
    private Map<String, String> values = new HashMap<String, String>();
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
    public CardConverter(Properties properties, String name) {
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
	NodeList attributes = element.getElementsByTagName(ATTRIBUTE_TAG);
	for (int i = 0; i < attributes.getLength(); ++i) {
	    Element attribute = (Element) attributes.item(i);
	    values.put(extractAuxiliaryFromAttribute(attribute),
		    extractValueFromAttribute(attribute));
	}
	long id = processValues();
	if (id == -1) {
	    return null;
	}

	return CardXMLBuilder.createAttribute(document, getOutCode(),
		CardXMLBuilder.CARD_LINK_TYPE, String.valueOf(id));
    }

    /**
     * Perform processing of read values. The {@link #processValues()} method
     * should be used to retrieve value by given key (suffix of 'code.<code>suffix</code>').
     *
     * @return id of found or created card.
     * @throws ConverterException
     */
    protected abstract long processValues() throws ConverterException;

    /**
     * Returns the value of <em>attribute</em> with code equals to
     * <code>key</code>. Can be called only in {@link #processValues()}
     * method.
     *
     * @param key -
     *                suffix of 'code.<code>suffix</code>' property key
     * @return read from <em>attribute</em> value or empty string if there was
     *         not <em>attribute</em> with appropriate code (value of
     *         property) in source element
     * @see #convert(Document, Element)
     */
    protected String getValueOfTagByKey(String key) {
	String valueOfKey = getPredefinedParam(key);
	String value = "";
	if (values.containsKey(valueOfKey)) {
	    value = values.get(valueOfKey);
	}
	return value;
    }

    /**
     * Returns value of property with 'code.<em>key</em>' key
     *
     * @param key
     *                alias for required code
     * @return value of property with 'code.<em>key</em>' key
     */
    private String getPredefinedParam(String key) {
	return properties.getProperty("param." + key);
    }

}
